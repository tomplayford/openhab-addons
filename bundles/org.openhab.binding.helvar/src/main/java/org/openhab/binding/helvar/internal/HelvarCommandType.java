package org.openhab.binding.helvar.internal;

import java.util.Arrays;

public enum HelvarCommandType {
    // Query
    QUERY_CLUSTERS(101, "Query Clusters"),
    QUERY_DEVICE_TYPES_AND_ADDRESSES(100, "Query Device Types and Addresses"),
    QUERY_DEVICE_STATE(110, "Query Device State"),
    QUERY_DEVICE_LOAD_LEVEL(152, "Query Device Load Level"),

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


