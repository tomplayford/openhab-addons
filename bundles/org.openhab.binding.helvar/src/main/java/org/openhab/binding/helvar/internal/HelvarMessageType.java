package org.openhab.binding.helvar.internal;

public enum HelvarMessageType {
    COMMAND(">"),
    INTERNAL_COMMAND("<"),
    REPLY("?"),
    ERROR("!");

    private final String messageTypeChar;

    HelvarMessageType(String messageTypeChar) {
        this.messageTypeChar = messageTypeChar;
    }

    @Override
    public String toString() {
        return this.messageTypeChar;
    }
}
