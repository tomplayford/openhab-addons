package org.openhab.binding.helvar.internal.discovery;

import org.openhab.binding.helvar.internal.parser.HelvarAddress;

public class FoundDevice {

    private HelvarAddress address;
    private Integer type;
    private String name;
    private DeviceType deviceType;


    public FoundDevice(HelvarAddress address, Integer type) {
        this.address = address;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public HelvarAddress getAddress() {
        return address;
    }

    public void setAddress(HelvarAddress address) {
        this.address = address;
    }

    @Override
    public String toString() {

        return "Found Device: " + this.address + ", " + this.name + ", " + this.type;

    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}
