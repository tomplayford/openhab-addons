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
package org.openhab.binding.helvar.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.helvar.internal.handler.DimmerHandler;
import org.openhab.binding.helvar.internal.handler.GroupHandler;
import org.openhab.binding.helvar.internal.handler.HelvarBridgeHandler;
import org.openhab.binding.helvar.internal.parser.HelvarAddress;
import org.openhab.binding.helvar.internal.parser.HelvarCommand;
import org.openhab.binding.helvar.internal.parser.HelvarCommandParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openhab.binding.helvar.internal.HelvarBindingConstants.*;
import static org.openhab.binding.helvar.internal.parser.HelvarCommandParameterType.GROUP;
import static org.openhab.binding.helvar.internal.parser.HelvarCommandType.*;

/**
 *
 * Current limitations:
 *  - only support networks with one router
 *
 */
@NonNullByDefault
public class HelvarDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(HelvarDiscoveryService.class);


    private Map<Integer, FoundDevice>[] foundDevices;
    private Map<Integer, FoundGroup> foundGroups;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(Stream
            .of(DimmerHandler.SUPPORTED_THING_TYPES.stream(), GroupHandler.SUPPORTED_THING_TYPES.stream())
            .flatMap(i -> i).collect(Collectors.toSet()));

    private HelvarBridgeHandler helvarBridgeHandler;

    public HelvarDiscoveryService(HelvarBridgeHandler helvarBridgeHandler) {
        super((Set) null, 0, false);
        this.helvarBridgeHandler = helvarBridgeHandler;
        foundDevices = new Map[]{new HashMap<Integer, FoundDevice>(), new HashMap<Integer, FoundDevice>(), new HashMap<Integer, FoundDevice>(), new HashMap<Integer, FoundDevice>()};
        foundGroups =  new HashMap<Integer, FoundGroup>();
    }


    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void startScan() {

        logger.info("Starting helvar discovery scan...");

        helvarBridgeHandler.startSearch(this);
    }

    @Override
    public synchronized void stopScan() {
        super.stopScan();
        logger.info("Stopping helvar discovery scan...");
        helvarBridgeHandler.stopSearch();
        removeOlderResults(getTimestampOfLastScan());
    }

    public void foundDevices(int subnetId, Map<Integer, FoundDevice> foundDevices) {
        this.foundDevices[subnetId-1] = foundDevices;

        this.findNamesForDevices(subnetId);
    }

    private void findNamesForDevices(int subnetId) {

        for (FoundDevice foundDevice : this.foundDevices[subnetId-1].values()) {
            helvarBridgeHandler.findNamesforfoundDevice(foundDevice);
        }

    }

    private @Nullable ThingUID getThingUID(ThingTypeUID thingTypeUID, String thingIdentifier) {
        ThingUID bridgeUID = helvarBridgeHandler.getThing().getUID();

        if (getSupportedThingTypes().contains(thingTypeUID)) {
            return new ThingUID(thingTypeUID, bridgeUID, thingIdentifier);
        } else {
            return null;
        }
    }

    private @Nullable ThingUID getThingUID(FoundDevice foundDevice) {
        if (foundDevice.getDeviceType() == null) {
            return null;
        }
        ThingTypeUID thingTypeUID = foundDevice.getDeviceType().getThingTypeUID();

        if (thingTypeUID != null) {
            return getThingUID(thingTypeUID, foundDevice.getAddress().toUID());
        } else {
            return null;
        }
    }

    public void foundDeviceName(HelvarAddress address, String name) {

        logger.trace("Setting name of device '{}' to '{}'", address, name);

        FoundDevice foundDevice = this.foundDevices[address.getSubnetId()-1].get(address.getDeviceId());

        if (foundDevice == null) {
            logger.debug("Could find a FoundDevice for address id {}, ignoring.", address);
            return;
        }

        foundDevice.setName(name);

        DeviceType deviceType = DeviceType.getDeviceTypeFromHelvarType(foundDevice.getType());

        if (deviceType == null) {
            logger.debug("Found a new device '{}' that we don't recognise. Ignoring.", foundDevice);
            return;
        }
        logger.debug("Found device type: '{}' for device '{}'", deviceType, foundDevice);

        foundDevice.setDeviceType(deviceType);

        ThingUID thingUID = getThingUID(foundDevice);
        ThingTypeUID thingTypeUID = foundDevice.getDeviceType().getThingTypeUID();

        if (thingUID != null && thingTypeUID != null) {

            ThingUID bridgeUID = helvarBridgeHandler.getThing().getUID();

            Map<String, Object> properties = new HashMap<>();

            properties.put(SUBNET_ID, foundDevice.getAddress().getSubnetId());
            properties.put(DEVICE_ID, foundDevice.getAddress().getDeviceId());
            properties.put(DEVICE_TYPE_ID, foundDevice.getDeviceType().name());
            properties.put(HELVAR_ADDRESS, foundDevice.getAddress().toString());
            properties.put(BUS_TYPE, foundDevice.getAddress().getBusType());
            properties.put(DEVICE_TYPE_NAME, foundDevice.getDeviceType().getDescription());
            properties.put(DEVICE_HELVAR_NAME, foundDevice.getName());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withRepresentationProperty(HELVAR_ADDRESS)
                    .withLabel(foundDevice.getName()).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovered unsupported device of type '{}' and name '{}' with address {}", deviceType,
                    foundDevice.getName(), foundDevice.getAddress());
        }

    }

    public void foundGroupName(int groupId, String queryResponse) {
        logger.trace("Setting name of group '{}' to '{}'", groupId, queryResponse);

        FoundGroup foundGroup = this.foundGroups.get(groupId);

        if (foundGroup == null) {
            logger.debug("Could find a FoundGroup for group id {}, ignoring.", groupId);
            return;
        }

        foundGroup.setDescription(queryResponse);

        ThingUID thingUID = getThingUID(THING_TYPE_GROUP, String.valueOf(groupId));

        if (thingUID != null) {
            ThingUID bridgeUID = helvarBridgeHandler.getThing().getUID();

            Map<String, Object> properties = new HashMap<>();

            properties.put(GROUP_ID, foundGroup.getGroupId().toString());
            properties.put(GROUP_NAME, foundGroup.getDescription());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_GROUP)
                    .withProperties(properties).withBridge(bridgeUID).withRepresentationProperty(GROUP_NAME)
                    .withLabel(foundGroup.getDescription()).build();

            thingDiscovered(discoveryResult);

        } else {

        }

    }


    /**
     *
     * List of all groups IDs.
     *
     * Next step is to send out individual queries for each group name.
     *
     */
    public void foundGroups(HelvarAddress address, String queryResponse) {

        if (queryResponse.length() == 0) {
            // No groups defined
            logger.debug("No groups defined on router {}.", address);
            return;
        }

        String[] groupIds = queryResponse.split(",");

        logger.debug("Found {} groups defined on router {}.", groupIds.length, address);

        foundGroups.clear();

        for (String groupId: groupIds) {

            Integer groupIdInt = null;

            try {
                groupIdInt = Integer.parseInt(groupId);
            } catch (NumberFormatException e) {
                logger.warn("Received an unexpected response to QUERY_GROUP command.");
                continue;
            }

            if (groupIdInt < 1) {
                logger.warn("Received an unexpected response to QUERY_GROUP command.");
                continue;
            }

            FoundGroup newGroup = new FoundGroup(Integer.parseInt(groupId));
            foundGroups.put(Integer.valueOf(groupId), newGroup);

        }

        // Query group scenes. Waiting 'til we'd populated the FoundGroups Map
        this.helvarBridgeHandler.sendCommand(new HelvarCommand(QUERY_SCENE_NAMES));


    }

    public void foundSceneNames(String queryResponse) {
        if (queryResponse.length() == 0) {
            // No groups defined
            logger.debug("No scene names defined on router.");
            return;
        }

//        logger.debug("Found {} scene names defined on router {}.", .length, address);

        // TODO: Handle scene names

        for (FoundGroup foundGroup: this.foundGroups.values()) {

            this.helvarBridgeHandler.sendCommand(
                    new HelvarCommand(
                            QUERY_GROUP_DESCRIPTION,
                            new HelvarCommandParameter(GROUP, foundGroup.getGroupId())
                    )
            );



        }

    }

}
