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

import static java.util.Objects.isNull;
import static org.openhab.binding.helvar.internal.HelvarCommandParameterType.*;

/**
 * Represents a Helvar Command.
 *
 * TODO: Document this.
 *
 * @author Tom Playford - Initial contribution
 */
public class HelvarCommand {

    private final HelvarMessageType messageType;
    private final HelvarCommandType commandType;

    private HelvarAddress address;

    private String queryResponse;

    //    private final int integrationId;
    private final HelvarCommandParameter[] parameters;

    private final HelvarMessageType defaultMessageType = HelvarMessageType.COMMAND;
    private final String defaultHelvarNetVersion = "1";
    private final String defaultHelvarTerminationChar = "#";

    public HelvarCommand(HelvarCommandType commandType, HelvarCommandParameter... parameters) {

        this.messageType = defaultMessageType;
        this.commandType = commandType;
        this.parameters = parameters;
    }

    public HelvarCommand(HelvarMessageType messageType, HelvarCommandType commandType, HelvarCommandParameter... parameters) {
        this.messageType = messageType;
        this.commandType = commandType;
        this.parameters = parameters;
    }

    public HelvarCommand(HelvarMessageType messageType, HelvarCommandType commandType, HelvarAddress helvarAddress, HelvarCommandParameter... parameters) {
        this.messageType = messageType;
        this.commandType = commandType;
        this.parameters = parameters;
        this.address = helvarAddress;
    }

    public HelvarCommand(HelvarMessageType messageType, HelvarCommandType commandType, HelvarAddress helvarAddress,
                         String queryResponse, HelvarCommandParameter... parameters) {
        this.messageType = messageType;
        this.commandType = commandType;
        this.parameters = parameters;
        this.address = helvarAddress;
        this.queryResponse = queryResponse;

    }

    public HelvarCommandType getCommandType() {
        return this.commandType;
    }

    public HelvarCommandParameter[] getParameters() {
        return this.parameters;
    }

    public HelvarAddress getAddress() {
        return this.address;
    }

    public String getQueryResponse() {
        return this.queryResponse;
    }

    public HelvarMessageType getMessageType() {
        return this.messageType;
    }

    private HelvarCommandParameter[] buildBaseParameters() {

        HelvarCommandParameter[] parameters = {
                new HelvarCommandParameter(VERSION, defaultHelvarNetVersion),
                new HelvarCommandParameter(COMMAND, Integer.toString(commandType.getCommandId())),
        };

        return parameters;
    }

    public int getGroupId() throws Exception {

        for (HelvarCommandParameter param : this.parameters) {
            if (param.commandParameterType == GROUP) {
                return Integer.parseInt(param.argument);
            }
        }
        throw new Exception("No Group");
    }

    /**
     * Builds the command string for transmission to a Helvar Router.
     *
     * @return The raw helvar command string
     */
    @Override
    public String toString() {

        if (this.messageType != HelvarMessageType.COMMAND) {
           // Only COMMAND messages can be sent.
           return "";
        }

        HelvarCommandParameter[] baseParameters = buildBaseParameters();

        StringBuilder builder = new StringBuilder().append(this.messageType);

        boolean first = true;
        for (HelvarCommandParameter parameter : baseParameters) {

            if (first) {
                builder.append(parameter);
                first = false;
                continue;
            }

            builder.append(',').append(parameter);
        }

        if (parameters != null) {
            for (Object parameter : parameters) {
                builder.append(',').append(parameter);
            }
        }

        if (!isNull(this.address)) {
            builder.append(',').append(address);

        }
        return builder.append(this.defaultHelvarTerminationChar).toString();
    }
}



