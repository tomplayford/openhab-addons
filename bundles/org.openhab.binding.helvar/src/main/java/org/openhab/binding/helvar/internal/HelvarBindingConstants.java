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
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HelvarBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tom Playford - Initial contribution
 */
@NonNullByDefault
public class HelvarBindingConstants {

    private static final String BINDING_ID = "helvar";


    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_ROUTER = new ThingTypeUID(BINDING_ID, "router");
    public static final ThingTypeUID THING_TYPE_GROUP = new ThingTypeUID(BINDING_ID, "group");

    // List of all Channel ids
    public static final String CHANNEL_LIGHTLEVEL = "lightlevel";
    public static final String SCENE_LIGHTLEVEL = "sceneLightLevel";
    public static final String SCENE_SELECTION = "scene";

    // Bridge config properties
    public static final String HOST = "hostName";
    public static final String PORT = "port";
}
