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
import org.openhab.binding.helvar.internal.handler.HelvarBridgeHandler;
import org.openhab.binding.helvar.internal.parser.HelvarAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(Stream
            .of(DimmerHandler.SUPPORTED_THING_TYPES.stream())
            .flatMap(i -> i).collect(Collectors.toSet()));

    private HelvarBridgeHandler helvarBridgeHandler;

    public HelvarDiscoveryService(HelvarBridgeHandler helvarBridgeHandler) {
        super((Set) null, 0, false);
        this.helvarBridgeHandler = helvarBridgeHandler;
        foundDevices = new Map[]{new HashMap<Integer, FoundDevice>(), new HashMap<Integer, FoundDevice>(), new HashMap<Integer, FoundDevice>(), new HashMap<Integer, FoundDevice>()};

    }


    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void startScan() {


        logger.info("Starting discovery scan...");

        helvarBridgeHandler.startSearch(this);
    }

    @Override
    public synchronized void stopScan() {
        super.stopScan();
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

    private @Nullable ThingUID getThingUID(FoundDevice foundDevice) {
        ThingUID bridgeUID = helvarBridgeHandler.getThing().getUID();
        if (foundDevice.getDeviceType() == null) {
            return null;
        }
        ThingTypeUID thingTypeUID = foundDevice.getDeviceType().getThingTypeUID();

        if (thingTypeUID != null && getSupportedThingTypes().contains(thingTypeUID)) {
            return new ThingUID(thingTypeUID, bridgeUID, foundDevice.getAddress().toUID());
        } else {
            return null;
        }
    }

    public void foundDeviceName(HelvarAddress address, String name) {


        logger.debug("Setting name of device '{}' to '{}'", address, name);

        this.foundDevices[address.getSubnetId()-1].get(address.getDeviceId()).setName(name);

        FoundDevice foundDevice = this.foundDevices[address.getSubnetId()-1].get(address.getDeviceId());

        DeviceType deviceType = DeviceType.getDeviceTypeFromHelvarType(foundDevice.getType());

        logger.error("Found device type: '{}' for device '{}'", deviceType, foundDevice);

        foundDevice.setDeviceType(deviceType);

        if (deviceType == null) {
            logger.debug("Found a new device '{}' that we don't recognise. Ignoring.", foundDevice);
            return;
        }
        ThingUID thingUID = getThingUID(foundDevice);
        ThingTypeUID thingTypeUID = foundDevice.getDeviceType().getThingTypeUID();

        if (thingUID != null && thingTypeUID != null) {

            ThingUID bridgeUID = helvarBridgeHandler.getThing().getUID();

            Map<String, Object> properties = new HashMap<>();

            properties.put("subnetId", foundDevice.getAddress().getSubnetId());
            properties.put("deviceId", foundDevice.getAddress().getDeviceId());
            properties.put("deviceTypeId", foundDevice.getDeviceType().name());
            properties.put("helvarAddress", foundDevice.getAddress().toString());
            properties.put("busType", foundDevice.getAddress().getBusType());
            properties.put("deviceTypeName", foundDevice.getDeviceType().getDescription());

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withRepresentationProperty("helvarAddress")
                    .withLabel(foundDevice.getName()).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.debug("discovered unsupported device of type '{}' and name '{}' with address {}", deviceType,
                    foundDevice.getName(), foundDevice.getAddress());
        }


//        this.submitFoundDevice(

//        logger.debug("Devices are: {}", this.foundDevices[address.getSubnetId()-1]);

    }



}
