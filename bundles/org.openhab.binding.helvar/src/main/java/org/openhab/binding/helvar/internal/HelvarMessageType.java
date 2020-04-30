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

package org.openhab.binding.helvar.internal;

import java.util.Arrays;

/**
 * The set of possible Helvar Command Message Types
 *
 * @author Tom Playford - Initial contribution
 */
public enum HelvarMessageType {
    COMMAND(">"),
    INTERNAL_COMMAND("<"),
    REPLY("?"),
    ERROR("!");

    private final String messageTypeChar;

    HelvarMessageType(String messageTypeChar) {
        this.messageTypeChar = messageTypeChar;
    }

    @Override
    public String toString() {
        return this.messageTypeChar;
    }

    // Build an immutable map of String name to enum pairs.
    // Any Map impl can be used.

    public String getMessageTypeChar() {
        return this.messageTypeChar;
    }

    /**
     * @return the Enum representation for the given string.
     * @throws IllegalArgumentException if unknown string.
     */
    public static HelvarMessageType fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(HelvarMessageType.values())
                .filter(v -> v.messageTypeChar.equals(s))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown value: " + s));
    }
}
