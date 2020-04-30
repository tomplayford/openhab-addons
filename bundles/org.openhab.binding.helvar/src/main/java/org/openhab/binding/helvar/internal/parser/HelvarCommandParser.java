package org.openhab.binding.helvar.internal.parser;


import org.openhab.binding.helvar.internal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;


/**
 * HelvarNet command parser.
 *
 * Takes a String and returns a HelvarCommand.
 *
 * Raises a CommandNotSupported exception if it receives a command that it does not currently support.
 */
public class HelvarCommandParser {

    private final Logger logger = LoggerFactory.getLogger(HelvarCommandParser.class);

    private final Pattern RESPONSE_REGEX = Pattern
            .compile("^(?<type>[<>?!])V:(?<version>\\d),C:(?<command>\\d+),?(?<params>[^=@#]+)?(?<address>@[^=#]+)?(=(?<result>[^=#]+))?#?$");

    private final Pattern PARAM_REGEX = Pattern.compile("(?:(?<command>[A-Z]:\\d+)+)");

    public HelvarCommand parseCommand(String rawCommand) throws UnsupportedCommand {

        //throw new UnsupportedCommand("Unsupported Command type");

        Matcher matcher = RESPONSE_REGEX.matcher(rawCommand);
        boolean responseMatched = matcher.find();

        if (!responseMatched) {
            throw new UnsupportedCommand("We can't match command: " + rawCommand);
        }

        HelvarAddress address = null;

        if (matcher.group("address") != null && !matcher.group("address").equals("")) {
            String[] parts = matcher.group("address").replaceAll("@", "").split("\\.");
            Integer[] addressParts = {null, null, null, null};
            for (int i = 0; i < parts.length; i++) {
                addressParts[i] = Integer.parseInt(parts[i]);
            }
            address = new HelvarAddress(addressParts[0], addressParts[1], addressParts[2], addressParts[3]);
        }

        List<HelvarCommandParameter> params = new ArrayList<>();

        if (matcher.group("params") != null && !matcher.group("params").equals("") && !matcher.group("params").equals(",")) {
            String[] parts = matcher.group("params").split(",");
            logger.trace("Command parts: {}", (Object) parts);

            for (String part : parts) {

                String[] paramArgPair = part.split(":");

                if (paramArgPair.length == 2) {
                    try {
                        params.add(new HelvarCommandParameter(HelvarCommandParameterType.fromString(paramArgPair[0]), paramArgPair[1]));
                    } catch (IllegalArgumentException e) {
                        logger.debug("Unsupported Parameter Type: {}", paramArgPair[0]);
                        throw new UnsupportedCommand(String.format(
                                "Unsupported Parameter Type: '%s' in received helvar command '%s'",
                                paramArgPair[0], rawCommand));
                    }
                } else {
                    logger.debug("Couldn't identify command pair in string: {}", part);
                }
            }
        }

        HelvarCommandParameter[] paramsArray = new HelvarCommandParameter[params.size()];
        paramsArray = params.toArray(paramsArray);

        return new HelvarCommand(
                HelvarMessageType.fromString(matcher.group("type")),
                HelvarCommandType.fromInteger(
                        Integer.parseInt(matcher.group("command"))
                ),
                address,
                matcher.group("result"),
                paramsArray
        );

    }

}
