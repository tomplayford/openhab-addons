package org.openhab.binding.helvar.internal;

/**
 * Simple Helvar command parameter
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
