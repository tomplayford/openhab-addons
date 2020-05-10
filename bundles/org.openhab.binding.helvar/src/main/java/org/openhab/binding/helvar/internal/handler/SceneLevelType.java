package org.openhab.binding.helvar.internal.handler;

import java.util.Arrays;

public enum SceneLevelType {
    IGNORE("*"),
    LAST_LEVEL("<"),
    VALUE("V");

    private final String value;

    SceneLevelType(String value) {
        this.value = value;
    }
}
