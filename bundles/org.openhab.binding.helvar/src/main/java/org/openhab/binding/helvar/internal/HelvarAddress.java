package org.openhab.binding.helvar.internal;


import static java.util.Objects.isNull;
import static org.openhab.binding.helvar.internal.HelvarCommandParameterType.ADDRESS;

public class HelvarAddress extends HelvarCommandParameter {

    private Integer clusterId;
    private Integer routerId;
    private Integer subnetId;
    private Integer deviceId;

    public HelvarAddress(Integer clusterId, Integer routerId, Integer subnetId, Integer deviceId) {
        super(ADDRESS, "");

        this.clusterId = clusterId;
        this.routerId = routerId;
        this.subnetId = subnetId;
        this.deviceId = deviceId;
    }

    private String buildCommandArgument(){
        String address = this.clusterId + "." + this.routerId;

        if (!isNull(this.subnetId)) {
            address = address + "." + this.subnetId;

            if (!isNull(this.deviceId)) {
                address = address + "." + this.deviceId;

            }
        }
        return address;

    }

    public boolean isValidForDevice(){
        return !(isNull(this.clusterId) || isNull(this.routerId) || isNull(this.subnetId) || isNull(this.deviceId));
    }

    @Override
    public String toString() {
        return this.commandParameterType + this.buildCommandArgument();
    }
    
    public Integer getSubnetId(){
        return this.subnetId;
    }
    
    public void setSubnetId(Integer subnetId) {
        this.subnetId = subnetId;
    }

    public Integer getDeviceId(){
        return this.deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }
    
    

}
