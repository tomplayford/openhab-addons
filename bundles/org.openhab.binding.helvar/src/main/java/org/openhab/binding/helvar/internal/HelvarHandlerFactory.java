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

import static org.openhab.binding.helvar.internal.HelvarBindingConstants.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.helvar.internal.discovery.HelvarDiscoveryService;
import org.openhab.binding.helvar.internal.handler.DimmerHandler;
import org.openhab.binding.helvar.internal.handler.GroupHandler;
import org.openhab.binding.helvar.internal.handler.HelvarBridgeHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link HelvarHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tom Playford - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.helvar", service = ThingHandlerFactory.class)
public class HelvarHandlerFactory extends BaseThingHandlerFactory {

    // Other types that can be initiated but not discovered
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_ROUTER, THING_TYPE_DIMMER, THING_TYPE_GROUP).collect(Collectors.toSet()));

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ROUTER)) {
            HelvarBridgeHandler helvarBridgeHandler = new HelvarBridgeHandler((Bridge) thing);
            registerDiscoveryService(helvarBridgeHandler);
            return helvarBridgeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_DIMMER)){
            return new DimmerHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_GROUP)){
            return new GroupHandler(thing);
        }

        return null;
    }


    private synchronized void registerDiscoveryService(HelvarBridgeHandler bridgeHandler) {
        HelvarDiscoveryService discoveryService = new HelvarDiscoveryService(bridgeHandler);
//        discoveryService.startScan();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof HelvarBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                HelvarDiscoveryService service = (HelvarDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (service != null) {
                    service.stopScan();
                }
            }
        }
    }

}
