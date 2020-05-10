package org.openhab.binding.helvar.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.helvar.internal.HelvarCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;


/**
 * The {@link org.openhab.binding.helvar.internal.handler.BaseHelvarHandler} provides some methods and structures
 * used by all Helvar Handlers
 *
 * @author Tom Playford - Initial contribution
 */
@NonNullByDefault
public abstract class BaseHelvarHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HelvarDeviceHandler.class);

    public BaseHelvarHandler(Thing thing) {
        super(thing);
    }

    public void handleUpdate(HelvarCommand helvarCommand) {

    }

    /**
     * Queries for any device state needed at initialization time or after losing connectivity to the bridge, and
     * updates device status. Will be called when bridge status changes to ONLINE and thing has status
     * OFFLINE:BRIDGE_OFFLINE.
     */
    protected abstract void initDeviceState();

    /**
     * Called when changing thing status to offline. Subclasses may override to take any needed actions.
     */
    protected void thingOfflineNotify() {
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge status changed to {} for helvar device handler", bridgeStatusInfo.getStatus());

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            initDeviceState();

        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            thingOfflineNotify();
        }
    }

    protected @Nullable HelvarBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();

        if (isNull(bridge)) { return null;}

        return (HelvarBridgeHandler) bridge.getHandler();
    }

    protected void sendCommand(HelvarCommand command) {
        HelvarBridgeHandler bridgeHandler = getBridgeHandler();

        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "No router associated");
            thingOfflineNotify();
        } else {
            bridgeHandler.sendCommand(command);
        }
    }

    public void handleRouterCommand(HelvarCommand command) {

        switch (command.getCommandType()) {
            default:
                logger.debug("Thing {} does not support HelvarCommandType {}", this.toString(), command.getCommandType());
        }

    }

}

