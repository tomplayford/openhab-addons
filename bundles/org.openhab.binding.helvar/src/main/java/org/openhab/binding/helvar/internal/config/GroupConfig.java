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

import org.openhab.binding.helvar.internal.HelvarAddress;

/**
 * Configuration settings for a {@link org.openhab.binding.helvar.internal.handler.GroupHandler}.
 *
 * @author Tom Playford - Initial contribution
 */
public class GroupConfig {

    private Integer groupId;
    private String name;
    private Integer blockId;

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBlockId() {
        return blockId;
    }

    public void setBlockId(Integer blockId) {
        this.blockId = blockId;
    }


    public Boolean checkConfigIsValid() {
        if (this.groupId == null || this.groupId > 16383 || this.groupId < 1) {
            return false;
        }
        return true;
    }

}



