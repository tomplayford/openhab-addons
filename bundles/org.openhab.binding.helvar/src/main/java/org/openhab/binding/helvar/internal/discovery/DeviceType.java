package org.openhab.binding.helvar.internal.discovery;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import java.util.HashMap;
import java.util.Map;

import static org.openhab.binding.helvar.internal.HelvarBindingConstants.THING_TYPE_DIMMER;

public enum DeviceType {

    DALI_FLUORESCENT_LAMPS((byte) 0x00, (byte)0x00, (byte)0x01, (byte)0x01, "Fluorescent lamps", THING_TYPE_DIMMER),
    DALI_EMERGENCY_LIGHTING((byte) 0x00, (byte)0x00, (byte)0x01, (byte)0x01, "Emergency lighting", THING_TYPE_DIMMER),
    DALI_DISCHARGE_LAMPS((byte) 0x00, (byte)0x00, (byte)0x02, (byte)0x01, "Discharge lamps", THING_TYPE_DIMMER),
    DALI_LOW_VOLTAGE_HALOGEN_LAMPS((byte) 0x00, (byte)0x00, (byte)0x03, (byte)0x01, "Low voltage halogen lamps", THING_TYPE_DIMMER),
    DALI_INCANDESCENT_LAMPS((byte) 0x00, (byte)0x00, (byte)0x04, (byte)0x01, "Incandescent lamps", THING_TYPE_DIMMER),
    DALI_DC_DIMMER((byte) 0x00, (byte)0x00, (byte)0x05, (byte)0x01, "DC dimmer", THING_TYPE_DIMMER),
    DALI_LED_MODULE((byte) 0x00, (byte)0x00, (byte)0x06, (byte)0x01, "LED lamps", THING_TYPE_DIMMER),
    DALI_RELAY((byte) 0x00, (byte)0x00, (byte)0x07, (byte)0x01, "Relay", THING_TYPE_DIMMER);

    private final byte byte3;
    private final byte byte2;
    private final byte byte1;
    private final byte byte0;
    private final String description;
    private final ThingTypeUID thingTypeUID;

    DeviceType(byte byte3, byte byte2, byte byte1, byte byte0, String description, ThingTypeUID thingTypeUID) {
        this.byte3 = byte3;
        this.byte2 = byte2;
        this.byte1 = byte1;
        this.byte0 = byte0;
        this.description = description;
        this.thingTypeUID = thingTypeUID;
    }

    public int toInt() {
        return (int) (this.byte3 << 8*3) + (this.byte2 << 8*2) + (this.byte1 << 8) + this.byte0;
    }

    public ThingTypeUID getThingTypeUID() {
        return thingTypeUID;
    }

    public String getDescription(){
        return this.description;
    }

    private static Map<Integer, DeviceType> getMap() {

        Map<Integer, DeviceType> map = new HashMap<Integer, DeviceType>();

        for (DeviceType type : DeviceType.values()) {

            map.put(type.toInt(), type);

        }
        return map;
    }

    public static DeviceType getDeviceTypeFromHelvarType(int helvarType) {

        Map<Integer, DeviceType> map = getMap();

        return map.get(helvarType);

    }


}
