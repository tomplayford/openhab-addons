/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.helvar.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.helvar.internal.config.HelvarBridgeConfig;
import org.openhab.binding.helvar.internal.net.TelnetSession;
import org.openhab.binding.helvar.internal.net.TelnetSessionListener;
import org.openhab.binding.helvar.internal.parser.HelvarCommandParser;
import org.openhab.binding.helvar.internal.parser.UnsupportedCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;
import static org.openhab.binding.helvar.internal.HelvarBindingConstants.HOST;
import static org.openhab.binding.helvar.internal.HelvarCommandType.*;


/**
 * The {@link HelvarBridgeHandler} handles the connection to Helvar Routers.
 *
 * @author Tom Playford - Initial contribution - heavily based on Lutron IPBridgeHandler.
 */
public class HelvarBridgeHandler extends ConfigStatusBridgeHandler {


    private static final int DEFAULT_RECONNECT_SECONDS = 5;
    private static final int DEFAULT_HEARTBEAT_MINUTES = 5;
    private static final int DEFAULT_SEND_DELAY_SECONDS = 0;
    private static final long KEEPALIVE_TIMEOUT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(HelvarBridgeHandler.class);

    private @NonNullByDefault({}) HelvarBridgeConfig helvarBridgeConfig = null;

    private TelnetSession session;
    private BlockingQueue<HelvarCommand> sendQueue = new LinkedBlockingQueue<>();

    private Thread messageSender;
    private ScheduledFuture<?> keepAlive;
    private ScheduledFuture<?> keepAliveReconnect;
    private ScheduledFuture<?> connectRetryJob;

//    private Thread messageSender;


    public HelvarBridgeHandler(Bridge bridge) {
        super(bridge);

        logger.debug("Initializing Helvar bridge handler.");

        this.session = new TelnetSession();

    }

    private synchronized void reconnect() {
        logger.debug("Keepalive timeout, attempting to reconnect to the router");

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE);
        disconnect();
        connect();
    }

    public HelvarAddress getAddress(){
        return new HelvarAddress(this.helvarBridgeConfig.getClusterId(), this.helvarBridgeConfig.getRouterId(), null, null);
    }


    /**
     * Parse messages coming in from router, and do sensible things with them.
     */
    private void handleMessagesFromRouter() {

        for (String line : this.session.readLines()) {
            if (line.trim().equals("")) {
                logger.trace("Received a blank line, ignoring");
                // Sometimes we get an empty line (possibly only when prompts are disabled). Ignore them.
                continue;
            }

            logger.debug("Received message {}", line);

            // System is alive, cancel reconnect task.
            if (this.keepAliveReconnect != null) {
                this.keepAliveReconnect.cancel(true);
            }

            HelvarCommandParser commandParser = new HelvarCommandParser();

            HelvarCommand helvarCommand;
            try {
                helvarCommand = commandParser.parseCommand(line);
            } catch (UnsupportedCommand e) {
                logger.debug("Received an unsupported command: {}", e.getMessage());
                continue; // next line.
            }

            switch (helvarCommand.getMessageType()) {
                case COMMAND:
                    logger.warn("Received a COMMAND from Router @{}.{} this shouldn't happen: {}",
                            this.helvarBridgeConfig.getClusterId(),
                            this.helvarBridgeConfig.getRouterId(),
                            line);
                    break;
                case INTERNAL_COMMAND:
                    logger.debug("Received an INTERNAL_COMMAND from Router @{}.{} not " +
                                    "sure what to do with these, ignoring: {}",
                            this.helvarBridgeConfig.getClusterId(),
                            this.helvarBridgeConfig.getRouterId(),
                            line);
                    break;
                case ERROR:
                    logger.warn("Received a ERROR from Router @{}.{}: {}",
                            this.helvarBridgeConfig.getClusterId(),
                            this.helvarBridgeConfig.getRouterId(),
                            line);
                    break;
                case REPLY:
                    logger.trace("Received a REPLY from Router @{}.{}: {}",
                            this.helvarBridgeConfig.getClusterId(),
                            this.helvarBridgeConfig.getRouterId(),
                            line);
                    handleReplyCommand(helvarCommand);
                    break;
            }
        }
    }

    private void handleReplyCommand(HelvarCommand command) {

        switch (command.getCommandType()) {
            case QUERY_DEVICE_LOAD_LEVEL:
            case QUERY_DEVICE_STATE:
                // Expecting full address and a value

                HelvarHandler handler = findThingHandler(command.getAddress());

                if (isNull(handler)) {
                    logger.trace("Received a '{}' REPLY from Router @{}.{} with address {}. " +
                            "Couldn't find Thing with that address. Ignoring message.", command.getCommandType(), this.helvarBridgeConfig.getClusterId(),
                            this.helvarBridgeConfig.getRouterId(), command.getAddress());
                    return;
                }

                handler.handleRouterCommand(command);

                break;
            case QUERY_CLUSTERS:

                break;

            case QUERY_DEVICE_TYPES_AND_ADDRESSES:
                break;

            case DIRECT_LEVEL_DEVICE:
                break;

            case QUERY_ROUTER_TIME:
                break;
        }

    }

    private HelvarHandler findThingHandler(HelvarAddress address) {

        // TODO: find a better way of looking up a Thing based on it's address. This seems pretty horrid.

        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof HelvarHandler) {
                HelvarHandler handler = (HelvarHandler) thing.getHandler();

                try {
                    if (handler != null && handler.getAddress().equals(address)) {
                        return handler;
                    }
                } catch (IllegalStateException e) {
                    logger.trace("Handler for id {} not initialized", address);
                }
            }
        }

        return null;

    }

    private synchronized void connect() {

        if (this.session.isConnected()) {
            logger.debug("Already connected");
            updateStatus(ThingStatus.ONLINE);
            return;
        }

        this.session.addListener(new TelnetSessionListener() {
            @Override
            public void inputAvailable() {
                handleMessagesFromRouter();
            }

            @Override
            public void error(IOException exception) {

                logger.debug("IO error {}", exception.getMessage());
//                disconnect();
//                scheduleConnectRetry(DEFAULT_RECONNECT_SECONDS);
            }
        });


        String ip = this.helvarBridgeConfig.getIpAddress();

        if (ip == null || ip.isEmpty()) {

            logger.error("Config error.  No IP address defined for router.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-ip-address");
            return;
        }

        logger.debug("Connecting to router at {}", this.helvarBridgeConfig.getIpAddress());

        try {
            if (!login()) {
                logger.error("Config error. Cannot connect to router at {}.", this.helvarBridgeConfig.getIpAddress());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Could not connect to router");
                disconnect();
            } else {
                // Success!
                logger.debug("Successfully connected to router at {}", this.helvarBridgeConfig.getIpAddress());
                updateStatus(ThingStatus.ONLINE);

                sendTestCommand();
                startSenderThread();
            }

        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.error("Communication error connecting to router at {}. {}", this.helvarBridgeConfig.getIpAddress(), e.getMessage());
            disconnect();
            scheduleConnectRetry(DEFAULT_RECONNECT_SECONDS); // Possibly a temporary problem. Try again later.

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Unknown error connecting to router at {}. {}", this.helvarBridgeConfig.getIpAddress(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "login interrupted");
            disconnect();
        }

    }

    private void scheduleConnectRetry(long waitSeconds) {
        logger.debug("Scheduling connection retry in {} minutes", waitSeconds);
        connectRetryJob = scheduler.schedule(this::connect, waitSeconds, TimeUnit.SECONDS);
    }

    private void sendCommandsThread() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                HelvarCommand command = sendQueue.take();

                logger.debug("Sending command {}", command);

                try {
                    session.writeLine(command.toString());
                } catch (IOException e) {
                    logger.warn("Communication error, will try to reconnect. Error: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);

                    sendQueue.add(command); // Requeue command

                    reconnect();

                    break;
                }
                if (DEFAULT_SEND_DELAY_SECONDS > 0) {
                    Thread.sleep(DEFAULT_SEND_DELAY_SECONDS); // introduce delay to throttle send rate
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void startSenderThread() {
        messageSender = new Thread(this::sendCommandsThread, "Helvar sender");
        messageSender.start();

        logger.debug("Starting keepAlive job with interval {}", DEFAULT_HEARTBEAT_MINUTES);
        keepAlive = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, DEFAULT_HEARTBEAT_MINUTES, DEFAULT_HEARTBEAT_MINUTES,
                TimeUnit.MINUTES);
    }

    private void sendKeepAlive() {
        logger.debug("Scheduling keepalive reconnect job");

        // Reconnect if no response is received within 30 seconds.
        keepAliveReconnect = scheduler.schedule(this::reconnect, KEEPALIVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        logger.trace("Sending keepalive query");
        sendCommand(new HelvarCommand(HelvarCommandType.QUERY_ROUTER_TIME));
    }

    private void sendTestCommand() {
        this.sendCommand(new HelvarCommand(HelvarCommandType.QUERY_ROUTER_TIME));
        this.sendCommand(new HelvarCommand(QUERY_DEVICE_TYPES_AND_ADDRESSES, new HelvarAddress(0,1,1, null)));
    }

    void sendCommand(HelvarCommand command) {
        this.sendQueue.add(command);
    }

    private synchronized void disconnect() {
        logger.debug("Disconnecting from Router");

        if (connectRetryJob != null) {
            connectRetryJob.cancel(true);
        }

        if (this.keepAlive != null) {
            this.keepAlive.cancel(true);
        }

        if (this.keepAliveReconnect != null) {
            // This method can be called from the keepAliveReconnect thread. Make sure
            // we don't interrupt ourselves, as that may prevent the reconnection attempt.
            this.keepAliveReconnect.cancel(false);
        }

        if (messageSender != null && messageSender.isAlive()) {
            messageSender.interrupt();
        }

        this.session.clearListeners();

        try {
            this.session.close();
        } catch (IOException e) {
            logger.warn("Error disconnecting: {}", e.getMessage());
        }
    }

    private boolean login() throws IOException, InterruptedException {
        this.session.open(helvarBridgeConfig.getIpAddress(), helvarBridgeConfig.getPort());
        return true;
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        // The bridge IP address to be used for checks
        Collection<ConfigStatusMessage> configStatusMessages;

        // Check whether an IP address is provided
        if (helvarBridgeConfig.getIpAddress() == null || helvarBridgeConfig.getIpAddress().isEmpty()) {
            configStatusMessages = Collections.singletonList(ConfigStatusMessage.Builder.error(HOST)
                    .withMessageKeySuffix("ip-address-missing-from-config").withArguments(HOST).build());
        } else {
            configStatusMessages = Collections.emptyList();
        }

        return configStatusMessages;
    }

    /** @deprecated */
    @Deprecated
    public void initialize() {
        // Apparently this is deprecated? Docs don't indicate what it's going to be replaced with. **shrug**.

        this.helvarBridgeConfig = getConfigAs(HelvarBridgeConfig.class);

        connect();
    }

    @Override
    public void thingUpdated(Thing thing) {

        logger.debug("Thing updated called");

        disconnect();

        HelvarBridgeConfig newConfig = thing.getConfiguration().as(HelvarBridgeConfig.class);
        logger.debug("Thing updated with new IP address: {}", newConfig.getIpAddress());

        this.thing = thing;
        this.helvarBridgeConfig = newConfig;

        connect();
    }


    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void dispose() {
        disconnect();
    }

}
