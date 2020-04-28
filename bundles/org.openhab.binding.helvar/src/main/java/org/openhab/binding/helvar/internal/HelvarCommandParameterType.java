package org.openhab.binding.helvar.internal;

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

    }
