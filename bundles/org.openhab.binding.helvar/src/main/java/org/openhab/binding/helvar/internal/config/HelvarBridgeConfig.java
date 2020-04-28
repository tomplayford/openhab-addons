package org.openhab.binding.helvar.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.helvar.internal.HelvarBridgeHandler;

/**
 * Configuration for the {@link HelvarBridgeHandler}.
 *
 * @author Tom Playford - Initial contribution
 */
@NonNullByDefault
public class HelvarBridgeConfig {

    private @NonNullByDefault({}) String ipAddress;
    private int port = 50000;
    private int clusterId = 1;
    private int driverId = 1;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    // Default HelvarNet port 50000
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }
}
