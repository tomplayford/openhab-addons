package org.openhab.binding.helvar.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.helvar.internal.config.HelvarBridgeConfig;
import org.openhab.binding.helvar.internal.net.TelnetSession;
import org.openhab.binding.helvar.internal.net.TelnetSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.helvar.internal.HelvarBindingConstants.HOST;
import static org.openhab.binding.helvar.internal.HelvarCommandType.QUERY_CLUSTERS;
import static org.openhab.binding.helvar.internal.HelvarCommandType.QUERY_DEVICE_TYPES_AND_ADDRESSES;

public class HelvarBridgeHandler extends ConfigStatusBridgeHandler {


    private static final int DEFAULT_RECONNECT_MINUTES = 5;
    private static final int DEFAULT_HEARTBEAT_MINUTES = 5;
    private static final int DEFAULT_SEND_DELAY_SECONDS = 0;
    private static final long KEEPALIVE_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(HelvarBridgeHandler.class);

    private @NonNullByDefault({}) HelvarBridge helvarBridge = null;
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
        return new HelvarAddress(this.helvarBridgeConfig.getClusterId(), this.helvarBridgeConfig.getDriverId(), null, null);
    }

    private void parseUpdates() {

        String paramString;
        String scrubbedLine;

        for (String line : this.session.readLines()) {
            if (line.trim().equals("")) {
                logger.trace("Received a blank line, ignoring");
                // Sometimes we get an empty line (possibly only when prompts are disabled). Ignore them.
                continue;
            }

            logger.debug("Received message {}", line);

//            // System is alive, cancel reconnect task.
//            if (this.keepAliveReconnect != null) {
//                this.keepAliveReconnect.cancel(true);
//            }

//            Matcher matcher = RESPONSE_REGEX.matcher(line);
//            boolean responseMatched = matcher.find();
//
//            if (!responseMatched) {
//                logger.debug("Ignoring message {}", line);
//                continue;
//
//            } else {
//
//                // We have a good response message
//                LutronCommandType type = LutronCommandType.valueOf(matcher.group(1));
//
//                if (type == LutronCommandType.SYSTEM) {
//                    // SYSTEM messages are assumed to be a response to the SYSTEM_DBEXPORTDATETIME
//                    // query. The response returns the last time the device database was updated.
//                    setDbUpdateDate(matcher.group(2), matcher.group(3));
//
//                    continue;
//                }
//
//                Integer integrationId;
//
//                try {
//                    integrationId = Integer.valueOf(matcher.group(2));
//                } catch (NumberFormatException e1) {
//                    logger.warn("Integer conversion error parsing update: {}", line);
//                    continue;
//                }
//                paramString = matcher.group(3);
//
//                // Now dispatch update to the proper thing handler
//                LutronHandler handler = findThingHandler(integrationId);
//
//                if (handler != null) {
//                    try {
//                        handler.handleUpdate(type, paramString.split(","));
//                    } catch (NumberFormatException e) {
//                        logger.warn("Number format exception parsing update: {}", line);
//                    } catch (RuntimeException e) {
//                        logger.warn("Runtime exception while processing update: {}", line, e);
//                    }
//                } else {
//                    logger.debug("No thing configured for integration ID {}", integrationId);
//                }
//            }
        }

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
                logger.trace("Input Available called");
                parseUpdates();
            }

            @Override
            public void error(IOException exception) {
                logger.debug("IO error {}", exception.getMessage());
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
//            scheduleConnectRetry(reconnectInterval); // Possibly a temporary problem. Try again later.

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Unknown error connecting to router at {}. {}", this.helvarBridgeConfig.getIpAddress(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "login interrupted");
            disconnect();
        }

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

                    // reconnect() will start a new thread; terminate this one
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

//        logger.debug("Starting keepAlive job with interval {}", DEFAULT_HEARTBEAT_MINUTES);
//        keepAlive = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, DEFAULT_HEARTBEAT_MINUTES, DEFAULT_HEARTBEAT_MINUTES,
//                TimeUnit.MINUTES);
    }

    private void sendKeepAlive() {
        logger.debug("Scheduling keepalive reconnect job");

        // Reconnect if no response is received within 30 seconds.
        keepAliveReconnect = scheduler.schedule(this::reconnect, KEEPALIVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

//        logger.trace("Sending keepalive query");
//        sendCommand(new LutronCommand(LutronOperation.QUERY, LutronCommandType.SYSTEM, -1, SYSTEM_DBEXPORTDATETIME));
    }

    private void sendTestCommand() {
        this.sendCommand(new HelvarCommand(QUERY_CLUSTERS));
        this.sendCommand(new HelvarCommand(QUERY_DEVICE_TYPES_AND_ADDRESSES, new HelvarAddress(1,1,1, null)));

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

        // Not sure if and when this is called?!

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
