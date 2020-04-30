package org.openhab.binding.helvar.internal;

import java.util.Arrays;

public enum HelvarCommandParameterType {
    VERSION("V"),
    COMMAND("C"),
    ADDRESS("@"),
    GROUP("G"),
    SCENE("S"),
    BLOCK("B"),
    FADE_TIME("F"),
    LEVEL("L"),
    PROPORTION("P"),
    DISPLAY_SCREEN("D");

    private final String commandTypeChar;

    HelvarCommandParameterType(String commandTypeChar) {

        this.commandTypeChar = commandTypeChar;
    }

    @Override
    public String toString() {
        return this.commandTypeChar;
    }

    /**
     * @return the Enum representation for the given string.
     * @throws IllegalArgumentException if unknown string.
     */
    public static HelvarCommandParameterType fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(HelvarCommandParameterType.values())
                .filter(v -> v.commandTypeChar.equals(s))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown value: " + s));
    }

    }
