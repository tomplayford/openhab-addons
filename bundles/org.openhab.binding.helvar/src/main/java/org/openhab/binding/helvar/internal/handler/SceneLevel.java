package org.openhab.binding.helvar.internal.handler;

public class SceneLevel {

    private final SceneLevelType type;
    private final Double value;

    public SceneLevel(SceneLevelType type, Double value) {
        this.type = type;
        this.value = value;
    }

    public SceneLevel(SceneLevelType type, int value) {
        this.type = type;
        this.value = (double) value;
    }

    public SceneLevel(SceneLevelType type) {
        this.type = type;
        this.value = null;

        if (this.type == SceneLevelType.VALUE) {
            throw new RuntimeException("Need a value");
        }

    }

    public SceneLevelType getType() {
        return this.type;
    }

    public Double getValue() {
        return this.value;
    }
}
