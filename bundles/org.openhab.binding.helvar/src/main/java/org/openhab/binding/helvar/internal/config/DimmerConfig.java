package org.openhab.binding.helvar.internal.config;


import org.openhab.binding.helvar.internal.HelvarAddress;

/**
 * Configuration settings for a {@link org.openhab.binding.helvar.internal.handler.DimmerHandler}.
 *
 * @author Tom Playford - Initial contribution
 */
public class DimmerConfig {

    private Integer subnetId;
    private Integer deviceId;

    public HelvarAddress getAddress(Integer clusterId, Integer routerId) {
        return new HelvarAddress(clusterId, routerId, subnetId, deviceId);
    }

    public void setSubnetId(Integer subnetId) {
        this.subnetId = subnetId;
    }

    public Integer getSubnetId(){
        return this.subnetId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getDeviceId() {
        return this.deviceId;
    }
}

