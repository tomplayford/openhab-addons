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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.helvar.internal.HelvarCommand;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.helvar.internal.HelvarCommandParameter;
import org.openhab.binding.helvar.internal.HelvarCommandType;
import org.openhab.binding.helvar.internal.config.GroupConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static java.lang.StrictMath.floor;
import static org.openhab.binding.helvar.internal.HelvarBindingConstants.CHANNEL_LIGHTLEVEL;
import static org.openhab.binding.helvar.internal.HelvarBindingConstants.SCENE_SELECTION;
import static org.openhab.binding.helvar.internal.HelvarCommandParameterType.*;
import static org.openhab.binding.helvar.internal.HelvarCommandType.QUERY_LAST_SCENE_IN_BLOCK;

/**
 * Handler for Helvar Groups
 *
 * @author Tom Playford - Initial Contribution
 *
 */
@NonNullByDefault
public class GroupHandler extends BaseHelvarHandler {

    private final Logger logger = LoggerFactory.getLogger(DimmerHandler.class);

    private final int DEFAULT_FADE_TIME = 50;

    private @Nullable GroupConfig config;

    public GroupHandler(Thing thing) {
        super(thing);
        this.config = getThing().getConfiguration().as(GroupConfig.class);
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
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    private void queryGroupScene() {
        sendCommand(new HelvarCommand(
                QUERY_LAST_SCENE_IN_BLOCK,
                new HelvarCommandParameter(GROUP, this.config.getGroupId()),
                new HelvarCommandParameter(BLOCK, this.config.getBlockId())
        ));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(SCENE_SELECTION)) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            } else if (command instanceof DecimalType) {
                setScene((DecimalType) command);
            } else {
                logger.warn("GroupHandler is not sure what to do with a command of type {} for SCENE_SELECTION", command.getClass().getName() );
            }
        }
    }

    private void setScene(DecimalType command) {

        sendCommand(new HelvarCommand(
                HelvarCommandType.RECALL_SCENE,
                new HelvarCommandParameter(GROUP, this.getGroupId()),
                new HelvarCommandParameter(BLOCK, this.config.getBlockId()),
                new HelvarCommandParameter(SCENE, command.intValue()),
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
            default:
                logger.debug("Thing {} does not support HelvarCommandType {}.", this.toString(), command.getCommandType());
        }

    }

    private void handleQueryLastScene(HelvarCommand command) {

        int response;

        try {
            response = Integer.parseInt(command.getQueryResponse());
        } catch (NumberFormatException e) {
            logger.warn("HelvarGroup Thing handler received a {} Helvar command {} with an unexpected response of '{}'. Ignoring", command.getCommandType(), command, command.getQueryResponse());
            return;
        }

        if (response > 128 || response < 1) {

            logger.warn("HelvarGroup Thing handler received a {} Helvar command {} with an unexpected group ID of '{}' should be between 1 and 128. Ignoring", command.getCommandType(), command, command.getQueryResponse());

            return;
        }

        int block;

        if (command.getCommandType() == QUERY_LAST_SCENE_IN_BLOCK) {

            block = (int) (floor((double) (response -1 ) / 8) + 1);

            if (block != this.config.getBlockId()) {
                logger.warn("HelvarGroup Thing handler received a {} Helvar command width a different block id of '{}' " +
                        "which is different to the current static block id of '{}'. Ignoring.", command.getCommandType(), block, this.config.getBlockId());
                return;
            }

        } else {
            block = this.config.getBlockId();
        }

        int scene = ((response - 1) % 8) + 1;

        logger.error("We are in block: {} and scene: {} ", block, scene);

        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);

        logger.debug("Updating thing {} channel 'SCENE_SELECTION' to valve of {}", this.toString(), scene);

        updateState(SCENE_SELECTION, new DecimalType(scene));

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
