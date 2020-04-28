package org.openhab.binding.helvar.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.helvar.internal.*;
import org.openhab.binding.helvar.internal.config.DimmerConfig;
import org.openhab.binding.helvar.internal.config.HelvarBridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.helvar.internal.HelvarBindingConstants.CHANNEL_LIGHTLEVEL;
import static org.openhab.binding.helvar.internal.HelvarCommandParameterType.*;

/**
 * Handler for devices on the Helvar networks that behave like simple dimmers.
 *
 * Simple means:
 *  - 1 channel
 *
 * @author Tom Playford - Initial Contribution
 *
 */
@NonNullByDefault
public class DimmerHandler extends HelvarHandler {

    private final Logger logger = LoggerFactory.getLogger(DimmerHandler.class);

    private final int DEFAULT_FADE_TIME = 50;

    private @Nullable DimmerConfig config;

    public DimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public HelvarAddress getAddress() {
        if (this.config == null || this.getBridgeHandler() == null) {
            throw new IllegalStateException("Handler not initialized correctly");
        }
        HelvarAddress address = this.getBridgeHandler().getAddress();
        address.setDeviceId(this.config.getDeviceId());
        address.setSubnetId(this.config.getSubnetId());
        return address;
    }

    @Override
    public void initialize() {
        this.config = getThing().getConfiguration().as(DimmerConfig.class);
        if (!getAddress().isValidForDevice()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Address invalid");
            return;
        }
        logger.debug("Initializing Dimmer handler for ID {}", getAddress());

        initDeviceState();
    }



    @Override
    public void handleUpdate(HelvarCommandType type, String... parameters) {

    }

    @Override
    protected void initDeviceState() {
        logger.debug("Initializing device state for Dimmer {}", getAddress());
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No router configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            //updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Awaiting initial response");
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            //queryOutput(ACTION_ZONELEVEL); // handleUpdate() will set thing status to online when response arrives
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_LIGHTLEVEL)) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            } else if (command instanceof PercentType) {
                setOutput(command.toString());
            } else if (command.equals(OnOffType.ON)) {
                setOutput("100");
            } else if (command.equals(OnOffType.OFF)) {
                setOutput("0");
            }
        }
    }

    private void setOutput(String outputLevel){
            sendCommand(new HelvarCommand(
                    HelvarCommandType.DIRECT_LEVEL_DEVICE,
                    new HelvarCommandParameter(LEVEL, outputLevel),
                    new HelvarCommandParameter(FADE_TIME, this.DEFAULT_FADE_TIME),
                    this.getAddress()
            ));
        }

}
