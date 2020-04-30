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

import java.util.Arrays;


/**
 * The set of supported Helvar Command Message Types
 *
 * There are many missing here.
 *
 * TODO: Add missing commands
 *
 * @author Tom Playford - Initial contribution
 */
public enum HelvarCommandType {
    // Query
    QUERY_CLUSTERS(101, "Query Clusters"),
    QUERY_DEVICE_TYPES_AND_ADDRESSES(100, "Query Device Types and Addresses"),
    QUERY_DEVICE_STATE(110, "Query Device State"),
    QUERY_DEVICE_LOAD_LEVEL(152, "Query Device Load Level"),
    QUERY_ROUTER_TIME(185, "Query Router Time"),

    // Command
    DIRECT_LEVEL_DEVICE(14,"Direct Level, Device");

    private final int commandId;
    private final String commandName;

    HelvarCommandType(int commandId, String commandName) {

        this.commandId = commandId;
        this.commandName = commandName;
    }

    @Override
    public String toString() {
        return this.commandName;
    }

    public int getCommandId() { return this.commandId; }


    /**
     * @return the Enum representation for the given string.
     * @throws IllegalArgumentException if unknown string.
     */
    public static HelvarCommandType fromInteger(int i) throws IllegalArgumentException {
        return Arrays.stream(HelvarCommandType.values())
                .filter(v -> v.commandId == i)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown value: " + i));
    }

}


