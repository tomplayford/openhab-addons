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

package org.openhab.binding.helvar.internal.parser;

import org.openhab.binding.helvar.internal.exception.InvalidAddress;

import static java.util.Objects.isNull;
import static org.openhab.binding.helvar.internal.parser.HelvarCommandParameterType.ADDRESS;

/**
 * Represents a Helvar address.
 *
 * Address format is @c.r.s.d
 *   c - cluster: 0-253
 *   r - router: 1-254
 *   s - subnet: 1-4
 *   d - device: 1-255
 *
 * incomplete addresses are possible, but must include at least cluster and router
 *
 *   @0.1
 *
 *   cluster: 0
 *   router: 1
 *
 *
 * @author Tom Playford - Initial contribution
 */
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

    public HelvarAddress(String address) throws InvalidAddress {
        super(ADDRESS, "");

        String[] parts = address.replaceAll("@", "").split("\\.");
        Integer[] addressParts = {null, null, null, null};

        if (parts.length < 1) {
            throw new InvalidAddress("Cannot parse invalid address string: " + address);
        }

        try {

            for (int i = 0; i < parts.length; i++) {
                addressParts[i] = Integer.parseInt(parts[i]);
            }
        } catch (NumberFormatException e) {
            throw new InvalidAddress("Cannot parse invalid address string: " + address);
        }

        this.clusterId = addressParts[0];
        this.routerId = addressParts[1];
        this.subnetId = addressParts[2];
        this.deviceId = addressParts[3];

    }

    private String buildCommandArgument(String joiningChar){
        String address = this.clusterId + joiningChar + this.routerId;

        if (!isNull(this.subnetId)) {
            address = address + joiningChar + this.subnetId;

            if (!isNull(this.deviceId)) {
                address = address + joiningChar + this.deviceId;

            }
        }
        return address;

    }

    private String buildCommandArgument(){
        return this.buildCommandArgument(".");
    }

    public boolean isValidForDevice(){
        return !(isNull(this.clusterId) || isNull(this.routerId) || isNull(this.subnetId) || isNull(this.deviceId));
    }

    public String getBusType() {
        switch (this.subnetId) {
            case 1:
            case 2:
                return "DALI";
            case 3:
                return "S-DIM";
            case 4:
                return "DMX";
        }
        return null;
    }

    @Override
    public String toString() {
        return this.commandParameterType + this.buildCommandArgument();
    }

    public String toUID() {
        return this.buildCommandArgument("-");
    }

    @Override
    public boolean equals(Object other) {
        
        HelvarAddress otherAddress = (HelvarAddress) other;
        
        if (this.clusterId != null) {
            if (otherAddress.clusterId == null) {
                return false;
            }

            if (this.clusterId != otherAddress.clusterId){
                return false;
            }

        } else if (otherAddress.clusterId != null){
            return false;
        }

        if (this.routerId != null) {
            if (otherAddress.routerId == null) {
                return false;
            }

            if (this.routerId != otherAddress.routerId){
                return false;
            }

        } else if (otherAddress.routerId != null){
            return false;
        }
        
        if (this.subnetId != null) {
            if (otherAddress.subnetId == null) {
                return false;
            }
            if (this.subnetId != otherAddress.subnetId){
                return false;
            }

        } else if (otherAddress.subnetId != null){
            return false;
        }
        
        if (this.deviceId != null) {
            if (otherAddress.deviceId == null) {
                return false;
            }

            return this.deviceId == otherAddress.deviceId;

        } else return otherAddress.deviceId == null;

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
    
    public Integer getClusterId() {
        return this.clusterId;
    }

    public Integer getRouterId() {
        return this.routerId;
    }

}
