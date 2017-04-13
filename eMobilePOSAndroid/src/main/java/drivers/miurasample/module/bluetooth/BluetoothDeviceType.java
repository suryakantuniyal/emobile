package drivers.miurasample.module.bluetooth;

/**
 * Created by mgadzala on 2016-06-09.
 */
public enum BluetoothDeviceType {

    PED("Miura"),   // PED
    POS("POS"),     // POSzle
    ITP("ITP");     // ITP Device

    private String deviceTypeName;

    BluetoothDeviceType(String deviceName) {
        this.deviceTypeName = deviceName;
    }

    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    public static BluetoothDeviceType getByDeviceTypeByName(String deviceName) {
        if(deviceName.toLowerCase().contains(POS.name().toLowerCase()) || deviceName.toLowerCase().contains(ITP.name().toLowerCase())){
            return POS;
        }else{
            return PED;
        }
    }
}
