package org.openhab.binding.helvar.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Representation of a connection to a Helvar Router
 *
 * @author Tom Playford - Initial contribution
 *
 */

@NonNullByDefault
public class HelvarBridge {

    private final Logger logger = LoggerFactory.getLogger(HelvarBridge.class);

    private final String ip;
    private final int port;

    public HelvarBridge(String ip, int port){
        this.ip = ip;
        this.port = port;

        try {
            boolean connectionAttempt = verifyConnection();
        } catch (Exception e) {
            logger.error("Unable to connect to Helvar Router. host={} port={} error={}", ip, port, e.getMessage());
            throw e;
        }

    }

    /**
     * Verify ability to communicate with router.
     */
    private Boolean verifyConnection() {

        return true;

    }


}
