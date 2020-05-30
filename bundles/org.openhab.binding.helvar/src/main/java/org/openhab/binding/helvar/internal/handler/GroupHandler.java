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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.helvar.internal.exception.InvalidAddress;
import org.openhab.binding.helvar.internal.exception.NotFoundInCommand;
import org.openhab.binding.helvar.internal.parser.HelvarAddress;
import org.openhab.binding.helvar.internal.parser.HelvarCommand;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.helvar.internal.parser.HelvarCommandParameter;
import org.openhab.binding.helvar.internal.parser.HelvarCommandType;
import org.openhab.binding.helvar.internal.config.GroupConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.StrictMath.floor;
import static org.openhab.binding.helvar.internal.HelvarBindingConstants.*;
import static org.openhab.binding.helvar.internal.parser.HelvarCommandParameterType.*;
import static org.openhab.binding.helvar.internal.parser.HelvarCommandType.*;

/**
 * Handler for Helvar Groups
 *
 * @author Tom Playford - Initial Contribution
 *
 */
@NonNullByDefault
public class GroupHandler extends BaseHelvarHandler {

    private final Logger logger = LoggerFactory.getLogger(DimmerHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_TYPE_GROUP).collect(Collectors.toSet());

    private final int DEFAULT_FADE_TIME = 50;
    private final int DEFAULT_BLOCK_ID = 1;

    private ArrayList<HelvarAddress> devices;

    private @Nullable GroupConfig config;

    public GroupHandler(Thing thing) {
        super(thing);
        this.config = getThing().getConfiguration().as(GroupConfig.class);

        this.devices = new ArrayList<HelvarAddress>();
    }

    @Override
    public void handleUpdate(HelvarCommand helvarCommand) {
        // do nothing
    }

    public int getGroupId() {

        if (this.config == null) {
            this.config = getThing().getConfiguration().as(GroupConfig.class);
        }

        return this.config.getGroupId();
    }

    @Override
    public void initialize() {

        this.config = getThing().getConfiguration().as(GroupConfig.class);

        if (!this.config.checkConfigIsValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No group ID configured");
            return;
        }

        logger.debug("Initializing Group handler for ID {}", getGroupId());

        initDeviceState();
    }



    @Override
    protected void initDeviceState() {
        logger.debug("Initializing device state for Dimmer {}", getGroupId());
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No router configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Awaiting initial response");
//            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            queryGroupScene();
            queryGroupDevices();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    private void queryGroupDevices() {
        sendCommand(new HelvarCommand(
                QUERY_GROUP,
                new HelvarCommandParameter(GROUP, this.config.getGroupId())
        ));
    }

    private void queryGroupScene() {
        sendCommand(new HelvarCommand(
                QUERY_LAST_SCENE_IN_GROUP,
                new HelvarCommandParameter(GROUP, this.config.getGroupId())
        ));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(SCENE_SELECTION)) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            } else if (command instanceof StringType) {
                setScene((StringType) command);
            } else {
                logger.warn("GroupHandler is not sure what to do with a command of type {} for SCENE_SELECTION", command.getClass().getName() );
            }
        }
    }

    private void setScene(StringType command) {

        String[] block_scene = command.toString().split("\\.");

        sendCommand(new HelvarCommand(
                HelvarCommandType.RECALL_SCENE,
                new HelvarCommandParameter(GROUP, this.getGroupId()),
                new HelvarCommandParameter(BLOCK, block_scene[0]),
                new HelvarCommandParameter(SCENE, block_scene[1]),
                new HelvarCommandParameter(FADE_TIME, this.DEFAULT_FADE_TIME)
        ));
    }

    public void handleRouterCommand(HelvarCommand command) {

        checkGroupIdIsCorrect(command);

        switch (command.getCommandType()) {
            case QUERY_LAST_SCENE_IN_BLOCK:
            case QUERY_LAST_SCENE_IN_GROUP:
                handleQueryLastScene(command);
                break;
            case RECALL_SCENE:
                handleCommandRecallScene(command);
                break;
            case QUERY_GROUP:
                handleQueryGroup(command);
                break;
            default:
                logger.debug("Thing {} does not support HelvarCommandType {}.", this.toString(), command.getCommandType());
        }

    }

    private void handleQueryGroup(HelvarCommand command) {
        String response = command.getQueryResponse();

        String[] addresses = response.split(",");

        this.devices.clear();

        for (String address: addresses) {

            try {
                this.devices.add(new HelvarAddress(address));
            } catch (InvalidAddress e) {
                continue;
            }
        }

        logger.debug("Registered {} devices for group {}: {}", this.devices.size(), this.getGroupId(), this.devices.toString());
    }

    private void handleCommandRecallScene(HelvarCommand command) {
        int scene;
        int block;

        try {
            scene = command.getSceneId();
        } catch (NotFoundInCommand e) {
            logger.warn("HelvarGroup Thing handler received a {} Helvar command {} with an unexpected response of '{}'. Ignoring", command.getCommandType(), command, command.getQueryResponse());
            return;
        }

        try {
            block = command.getBlockId();
        } catch (NotFoundInCommand e) {
            logger.warn("HelvarGroup Thing handler received a {} Helvar command {} with an unexpected response of '{}'. Ignoring", command.getCommandType(), command, command.getQueryResponse());
            return;
        }

        if (scene > 16 || scene < 1) {
            logger.warn("HelvarGroup Thing handler received a {} Helvar command {} with an unexpected group ID of '{}' should be between 1 and 16. Ignoring", command.getCommandType(), command, scene);
            return;
        }
        if (block > 8 || block < 1) {
            logger.warn("HelvarGroup Thing handler received a {} Helvar command {} with an unexpected block ID of '{}' should be between 1 and 8. Ignoring", command.getCommandType(), command, block);
            return;
        }

        updateGroupScene(scene, block);
        updateDevices(scene, block);

    }

    private void updateDevices(int scene, int block) {

        logger.debug("Updating group {} devices to levels for scene {}.{}", this.getGroupId(), block, scene);

        for (HelvarAddress address : this.devices) {
            HelvarDeviceHandler thing = this.getBridgeHandler().findThingHandler(address);
            if (thing != null) {
                thing.setToScene(scene, block);
            }

        }

    }

    private void handleQueryLastScene(HelvarCommand command) {

        int scene;

        try {
            scene = Integer.parseInt(command.getQueryResponse());
        } catch (NumberFormatException e) {
            logger.warn("HelvarGroup Thing handler received a {} Helvar command {} with an unexpected response of '{}'. Ignoring", command.getCommandType(), command, command.getQueryResponse());
            return;
        }

        int block;

        if (command.getCommandType() == QUERY_LAST_SCENE_IN_GROUP) {

            if (scene > 128 || scene < 1) {

                logger.warn("HelvarGroup Thing handler received a {} Helvar command {} with an unexpected group ID of '{}' should be between 1 and 128. Ignoring", command.getCommandType(), command, command.getQueryResponse());

                return;
            }

            block = (int) (floor((double) (scene -1 ) / 16) + 1);
            scene = ((scene - 1) % 16) + 1;

        } else {

            if (scene > 16 || scene < 1) {

                logger.warn("HelvarGroup Thing handler received a {} Helvar command {} with an unexpected group ID of '{}' should be between 1 and 16. Ignoring", command.getCommandType(), command, command.getQueryResponse());

                return;
            }

            block = this.DEFAULT_BLOCK_ID;
        }

        // This is the command we use to verify that a group is "online" - update Thing state.
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);

        updateGroupScene(scene, block);
    }


    private void updateGroupScene(int scene, int block) {

        logger.debug("Updating thing {} channel 'SCENE_SELECTION' to valve of {}", this.toString(), scene);

        updateState(SCENE_SELECTION, new StringType(block + "." + scene));

    }

    private void checkGroupIdIsCorrect(HelvarCommand command) {

        try {
            if (command.getGroupId() == this.config.getGroupId() ) {
                return;
            }
        } catch (Exception ignored) {
        }
        logger.warn("Thing handler received a Helvar command that is not addressed to it. Thing groupId: {}", this.getGroupId());

    }




}
