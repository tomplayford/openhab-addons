package org.openhab.binding.helvar.internal.discovery;

import org.openhab.binding.helvar.internal.parser.HelvarAddress;

public class FoundGroup {

    private Integer groupId;
    private String description;

    public FoundGroup(Integer groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {

        return "Found Group: " + this.groupId + ", " + this.description;

    }

    public Integer getGroupId() {
        return groupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
