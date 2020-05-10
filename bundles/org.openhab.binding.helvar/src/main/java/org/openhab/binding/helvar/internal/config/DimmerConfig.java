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

import org.openhab.binding.helvar.internal.parser.HelvarAddress;

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

