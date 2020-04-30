package org.openhab.binding.helvar.internal.parser;

public class UnsupportedCommand extends Exception {
    public UnsupportedCommand(String errorMessage) {
        super(errorMessage);
    }
}
