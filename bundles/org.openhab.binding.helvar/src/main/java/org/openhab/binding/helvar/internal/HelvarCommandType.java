package org.openhab.binding.helvar.internal;

public enum HelvarCommandType {
    QUERY_CLUSTERS(101, "Query Clusters");

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


