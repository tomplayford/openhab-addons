package org.openhab.binding.helvar.internal;

import java.util.Arrays;


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

    // Build an immutable map of String name to enum pairs.
    // Any Map impl can be used.

    public String getMessageTypeChar() {
        return this.messageTypeChar;
    }

    /**
     * @return the Enum representation for the given string.
     * @throws IllegalArgumentException if unknown string.
     */
    public static HelvarMessageType fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(HelvarMessageType.values())
                .filter(v -> v.messageTypeChar.equals(s))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown value: " + s));
    }
}
