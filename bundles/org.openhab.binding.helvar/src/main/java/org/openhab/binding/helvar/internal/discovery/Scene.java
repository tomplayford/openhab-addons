package org.openhab.binding.helvar.internal.discovery;

public class Scene {

    private final Integer groupId;
    private final Integer sceneId;
    private final Integer blockId;
    private String name;

    public Scene(Integer groupId, Integer blockId, Integer sceneId ) {
        this.groupId = groupId;
        this.sceneId = sceneId;
        this.blockId = blockId;
    }

    public Scene(Integer groupId, Integer blockId, Integer sceneId, String name) {
        this.groupId = groupId;
        this.sceneId = sceneId;
        this.blockId = blockId;
        this.name = name;
    }

    @Override
    public String toString() {

        return String.format("@%s.%s %s", this.blockId, this.sceneId, this.name);

    }
}
