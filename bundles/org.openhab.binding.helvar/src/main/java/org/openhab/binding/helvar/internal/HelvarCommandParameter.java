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

/**
 * Simple Helvar command parameter
 *
 * One character for the command and a string for the single argument.
 *
 * @author Tom Playford - Initial contribution
 */
public class HelvarCommandParameter {
    HelvarCommandParameterType commandParameterType;
    String argument;

    public HelvarCommandParameter(HelvarCommandParameterType commandParameterType, String argument){

        this.commandParameterType = commandParameterType;
        this.argument = argument;
    }

    public HelvarCommandParameter(HelvarCommandParameterType commandParameterType, int argument){

        this.commandParameterType = commandParameterType;
        this.argument = Integer.toString(argument);
    }

    @Override
    public String toString() {
        return this.commandParameterType + ":" + this.argument;
    }

}
