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
package org.openhab.binding.helvar.internal.handler;

import static org.openhab.binding.helvar.internal.HelvarBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.*;
import org.openhab.binding.helvar.internal.parser.HelvarAddress;
import org.openhab.binding.helvar.internal.parser.HelvarCommand;
import org.openhab.binding.helvar.internal.parser.HelvarCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * The {@link HelvarDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tom Playford - Initial contribution
 */
@NonNullByDefault
public abstract class HelvarDeviceHandler extends BaseHelvarHandler {

    private final Logger logger = LoggerFactory.getLogger(HelvarDeviceHandler.class);

    public HelvarDeviceHandler(Thing thing) {
        super(thing);
    }

    public abstract HelvarAddress getAddress();



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

    @Override
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
