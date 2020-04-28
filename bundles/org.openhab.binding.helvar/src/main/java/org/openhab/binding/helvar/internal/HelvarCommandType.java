package org.openhab.binding.helvar.internal;

public enum HelvarCommandType {
    // Query
    QUERY_CLUSTERS(101, "Query Clusters"),
    QUERY_DEVICE_TYPES_AND_ADDRESSES(100, "Query Device Types and Addresses"),

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
}


