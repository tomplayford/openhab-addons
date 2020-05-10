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

import java.util.Arrays;

/**
 * The set of possible Helvar Command Parameter Types
 *
 * An entry in this Enum does not mean a parameter is supported.
 *
 * @author Tom Playford - Initial contribution
 */
public enum HelvarCommandParameterType {
    VERSION("V"),
    COMMAND("C"),
    ADDRESS("@"),
    GROUP("G"),
    SCENE("S"),
    BLOCK("B"),
    FADE_TIME("F"),
    LEVEL("L"),
    PROPORTION("P"),
    DISPLAY_SCREEN("D"),
    SEQUENCE_NUMBER("Q"),
    TIME("T"),
    ACK("A"),
    LATITUDE("L"),
    LONGITUDE("E"),
    TIME_ZONE_DIFFERENCE("Z"),
    DAYLIGHT_SAVING_TIME("Y"),
    CONSTANT_LIGHT_SCENE("K"),
    FORCE_STORE_SCENE("O");

    private final String commandTypeChar;

    HelvarCommandParameterType(String commandTypeChar) {

        this.commandTypeChar = commandTypeChar;
    }

    @Override
    public String toString() {
        return this.commandTypeChar;
    }

    /**
     * @return the Enum representation for the given string.
     * @throws IllegalArgumentException if unknown string.
     */
    public static HelvarCommandParameterType fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(HelvarCommandParameterType.values())
                .filter(v -> v.commandTypeChar.equals(s))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown value: " + s));
    }

    }
