package org.openhab.binding.helvar.internal;

public class HelvarCommand {

    private final HelvarMessageType messageType;
    private final HelvarCommandType commandType;

//    private final int integrationId;
    private final Object[] parameters;

    private final HelvarMessageType defaultMessageType = HelvarMessageType.COMMAND;
    private final String defaultHelvarNetVersion = "1";
    private final String defaultHelvarTerminationChar = "#";

    public HelvarCommand(HelvarCommandType commandType, Object... parameters) {

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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append(this.messageType).append(this.buildVersionStatement()).
                append(",").append(this.buildCommandStatement());


        if (parameters != null) {
            for (Object parameter : parameters) {
                builder.append(',').append(parameter);
            }
        }

        return builder.append(this.defaultHelvarTerminationChar).toString();
    }

    private String buildVersionStatement() {
        return "V:" + defaultHelvarNetVersion;
    }

    private String buildCommandStatement() {
        return "C:" + Integer.toString(commandType.getCommandId());
    }

}



