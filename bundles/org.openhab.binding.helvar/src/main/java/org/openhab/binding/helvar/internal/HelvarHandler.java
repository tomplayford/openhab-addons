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

import static java.util.Objects.isNull;
import static org.openhab.binding.helvar.internal.HelvarBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * The {@link HelvarHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tom Playford - Initial contribution
 */
@NonNullByDefault
public abstract class HelvarHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HelvarHandler.class);

    public HelvarHandler(Thing thing) {
        super(thing);
    }

    public abstract HelvarAddress getAddress();

    public abstract void handleUpdate(HelvarCommand helvarCommand);

    /**
     * Queries for any device state needed at initialization time or after losing connectivity to the bridge, and
     * updates device status. Will be called when bridge status changes to ONLINE and thing has status
     * OFFLINE:BRIDGE_OFFLINE.
     */
    protected abstract void initDeviceState();

    /**
     * Called when changing thing status to offline. Subclasses may override to take any needed actions.
     */
    protected void thingOfflineNotify() {
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge status changed to {} for helvar device handler", bridgeStatusInfo.getStatus());

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            initDeviceState();

        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            thingOfflineNotify();
        }
    }

    protected @Nullable HelvarBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();

        if (isNull(bridge)) { return null;}

        return (HelvarBridgeHandler) bridge.getHandler();
    }

    protected void sendCommand(HelvarCommand command) {
        HelvarBridgeHandler bridgeHandler = getBridgeHandler();

        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "No router associated");
            thingOfflineNotify();
        } else {
            bridgeHandler.sendCommand(command);
        }
    }

    protected void queryDeviceState() {
        sendCommand(new HelvarCommand(
                HelvarCommandType.QUERY_DEVICE_STATE,
                this.getAddress()
        ));
    }

    protected void queryDeviceLoad() {
        sendCommand(new HelvarCommand(
                HelvarCommandType.QUERY_DEVICE_LOAD_LEVEL,
                this.getAddress()
        ));
    }

    public void handleRouterCommand(HelvarCommand command) {


        switch (command.getCommandType()) {
            case QUERY_DEVICE_LOAD_LEVEL:
                handleDeviceLoadResponse(command);
                break;
            case QUERY_DEVICE_STATE:
                handleDeviceStateResponse(command);
                break;
            default:
                logger.debug("Thing {} does not support HelvarCommandType {}", this.toString(), command.getCommandType());
        }

    }

    private void handleDeviceStateResponse(HelvarCommand command) {

        if (!this.getAddress().equals(command.getAddress())) {
            logger.warn("Thing handler received a Helvar command that is not addressed to it. command address: {}, thing address: {}", command.getAddress(), this.getAddress());
            return;
        }

        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    private void handleDeviceLoadResponse(HelvarCommand command) {
        if (!this.getAddress().equals(command.getAddress())) {
            logger.warn("Thing handler received a Helvar command that is not addressed to it. command address: {}, thing address: {}", command.getAddress(), this.getAddress());
            return;
        }

        // load should be a integer representing the % load on the channel.
        // There are cases where this can be over 100 - if that's the case, model the channel as off - load of 0.

        Double load = Double.parseDouble(command.getQueryResponse());

        if (load > 100) {
            load = 0.0d;
        }

        logger.debug("Updating thing {} channel 'CHANNEL_LIGHTLEVEL' to valve of {}", this.toString(), load);

        updateState(CHANNEL_LIGHTLEVEL, new PercentType(new BigDecimal(load)));

    }

    @Override
    public String toString (){
        return getClass().getName() + " address: " + this.getAddress();
    }


}
