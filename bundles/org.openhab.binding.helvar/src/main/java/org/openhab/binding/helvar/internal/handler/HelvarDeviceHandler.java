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
    private static final int SCENE_LEVELS_COUNT = 136; // (16 scenes * 8 blocks) + 8 somethings?
    private SceneLevel sceneLevels[];

    public HelvarDeviceHandler(Thing thing) {

        super(thing);

        this.sceneLevels = new SceneLevel[SCENE_LEVELS_COUNT];

        for (int i = 0; i < this.sceneLevels.length; i++) {
            this.sceneLevels[i] = new SceneLevel(SceneLevelType.IGNORE);
        }

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
            case QUERY_SCENE_INFO:
                handleDeviceSceneLevels(command);
                break;
            default:
                logger.debug("Thing {} does not support HelvarCommandType {}", this.toString(), command.getCommandType());
        }

    }

    private void handleDeviceSceneLevels(HelvarCommand command) {
        if (!this.getAddress().equals(command.getAddress())) {
            logger.warn("Thing handler received a Helvar command that is not addressed to it. command address: {}, thing address: {}", command.getAddress(), this.getAddress());
            return;
        }

        String response = command.getQueryResponse();

        String[] responses = response.split(",");

        assert responses.length == SCENE_LEVELS_COUNT;

        for (int i = 0; i < SCENE_LEVELS_COUNT; i++) {

            if (responses[i].equals("*")) {
                this.sceneLevels[i] = new SceneLevel(SceneLevelType.IGNORE);
            } else if (responses[i].equals("L")) {
                this.sceneLevels[i] = new SceneLevel(SceneLevelType.LAST_LEVEL);
            } else {

                int value;

                try {
                    value = Integer.parseInt(responses[i]);
                } catch (NumberFormatException e) {
                    logger.warn("Received unexpected scene level value of '{}' at position '{}', ignoring", command, i);
                    continue;
                }

                this.sceneLevels[i] = new SceneLevel(SceneLevelType.LAST_LEVEL, value);
            }

        }

        logger.debug("Updated scene levels for device {}", this.getAddress());

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


    /*
    *
    *
     */
    public void setToScene(int scene, int block) {
        int loc = ((block - 1) * 16) + (scene - 1);

        SceneLevel sceneLevel = this.sceneLevels[loc];

        if (sceneLevel.getType() == SceneLevelType.VALUE) {

            Double load = sceneLevel.getValue();

            if (load > 100) {
                load = 0.0d;
            }

            logger.debug("Updating thing {} channel 'CHANNEL_LIGHTLEVEL' to valve of {}", this.toString(), load);

            updateState(CHANNEL_LIGHTLEVEL, new PercentType(new BigDecimal(load)));

        }


    }

}
