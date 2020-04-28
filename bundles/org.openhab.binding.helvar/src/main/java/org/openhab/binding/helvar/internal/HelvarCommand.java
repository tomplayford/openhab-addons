package org.openhab.binding.helvar.internal;

import static org.openhab.binding.helvar.internal.HelvarCommandParameterType.COMMAND;
import static org.openhab.binding.helvar.internal.HelvarCommandParameterType.VERSION;

public class HelvarCommand {

    private final HelvarMessageType messageType;
    private final HelvarCommandType commandType;

    //    private final int integrationId;
    private final Object[] parameters;

    private final HelvarMessageType defaultMessageType = HelvarMessageType.COMMAND;
    private final String defaultHelvarNetVersion = "1";
    private final String defaultHelvarTerminationChar = "#";

    public HelvarCommand(HelvarCommandType commandType, HelvarCommandParameter... parameters) {

        this.messageType = defaultMessageType;
        this.commandType = commandType;
//        this.integrationId = integrationId;
        this.parameters = parameters;
    }

    public HelvarCommandType getCommandType() {
        return this.commandType;
    }

//    public int getIntegrationId() {
//        return this.integrationId;
//    }

    public Object[] getParameters() {
        return this.parameters;
    }

    private HelvarCommandParameter[] buildBaseParameters() {

        HelvarCommandParameter[] parameters = {
                new HelvarCommandParameter(VERSION, defaultHelvarNetVersion),
                new HelvarCommandParameter(COMMAND, Integer.toString(commandType.getCommandId())),
        };

        return parameters;
    }

    @Override
    public String toString() {

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

        return builder.append(this.defaultHelvarTerminationChar).toString();
    }
}



