/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.helvar.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.helvar.internal.handler.HelvarBridgeHandler;

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
    private int routerId = 1;

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

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }
}
