package drivers.miurasample.module.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.Nullable;

import com.miurasystems.miuralibrary.BaseBluetooth;
import com.miurasystems.miuralibrary.BluetoothService;
import com.miurasystems.miuralibrary.MPIConnectionDelegate;
import com.miurasystems.miuralibrary.enums.BatteryData;
import com.miurasystems.miuralibrary.enums.DeviceStatus;
import com.miurasystems.miuralibrary.enums.M012Printer;
import com.miurasystems.miuralibrary.tlv.CardData;

import java.util.ArrayList;

public class BluetoothModule {

    private BluetoothService bluetoothService;
    private BaseBluetooth baseBluetooth;

    @Nullable
    private BluetoothDevice defaultBluetoothDevice;
    private static BluetoothModule instance;
    private BluetoothConnectionListener connectionListener;

    private BluetoothModule() {

        if (bluetoothService == null) {
            bluetoothService = BluetoothService.getInstance();
        }

        if (baseBluetooth == null) {
            baseBluetooth = BaseBluetooth.getInstance();
        }
    }

    /**
     * @return BluetoothModule instance
     */
    public static BluetoothModule getInstance() {
        if (instance == null) {
            instance = new BluetoothModule();
        }

        return instance;
    }

    /**
     * @param context Application Context
     * @return List of paired {@link BluetoothDevice}
     */
    public ArrayList<BluetoothDevice> getPairedDevices(Context context) {
        BluetoothPairing bluetoothPairing = new BluetoothPairing(context);
        return bluetoothPairing.getPairedDevices();
    }

    /**
     * @param context Application Context
     * @return List of non-paired {@link BluetoothDevice}
     */
    public ArrayList<BluetoothDevice> getNonPairedDevices(Context context) {
        BluetoothPairing bluetoothPairing = new BluetoothPairing(context);
        return bluetoothPairing.getNonPairedDevices();
    }

    /**
     * Opening session with previous set as default BluetoothDevice via {@link BluetoothModule#setSelectedBluetoothDevice(BluetoothDevice)}
     *
     * @param connectionListener Listener for connection state
     * @throws IllegalStateException if there is no default device
     */
    public void openSessionDefaultDevice(final BluetoothConnectionListener connectionListener) throws IllegalStateException {

        if (defaultBluetoothDevice == null) {
            throw new IllegalStateException("There is no default device, call setSelectedBluetoothDevice");
        }

        openSession(defaultBluetoothDevice.getAddress(), connectionListener);
    }

    /**
     * Opening session with selected BluetoothDevice
     *
     * @param deviceAddress        Bluetooth device address
     * @param btConnectionListener Listener for connection state
     */
    public void openSession(String deviceAddress, BluetoothConnectionListener btConnectionListener) {
        this.connectionListener = btConnectionListener;
        baseBluetooth.openSession(bluetoothService, deviceAddress, new MPIConnectionDelegate() {
            @Override
            public void connected() {
                if (connectionListener != null) {
                    connectionListener.onConnected();
                }
            }

            @Override
            public void disconnected() {
            }

            @Override
            public void connectionState(boolean flg) {
                if (connectionListener != null && !flg)  {
                    connectionListener.onDisconnected();
                }
            }

            @Override
            public void onKeyPressed(int keyCode) {
            }

            @Override
            public void onCardStatusChange(CardData cardData) {

            }

            @Override
            public void onDeviceStatusChange(DeviceStatus deviceStatus, String statusText) {
            }

            @Override
            public void onBatteryStatusChange(BatteryData batteryData) {
            }

            @Override
            public void onBarcodeScan(String scanned) {

            }
            public void onPrintSledStatus(M012Printer m012PrinterStatus) {
                
            }
        }, null);
    }

    public void setDevice(BluetoothDevice device) {
        baseBluetooth.selectPriorityDevice(device);
    }

    /**
     * Enable or disable Bluetooth timeout
     *
     * @param enable timeout state
     */
    public void setTimeoutEnable(boolean enable) {
        bluetoothService.setTimeOutEnable(enable);
    }

    /**
     * Closes session with connected device
     */
    public void closeSession() {
        this.connectionListener = null;
        bluetoothService.closeSession();
    }

    /**
     * Sets selected device as default in communication
     *
     * @param defaultBluetoothDevice {@link BluetoothDevice} object
     */
    public void setSelectedBluetoothDevice(BluetoothDevice defaultBluetoothDevice) {
        this.defaultBluetoothDevice = defaultBluetoothDevice;
    }

    @Nullable
    public BluetoothDevice getSelectedBluetoothDevice() {
        return defaultBluetoothDevice;
    }

    /**
     * Sets default device in categor {@link com.miurasystems.miuralibrary.api.executor.MiuraManager.DeviceType}
     *
     * @param context         Application Context
     * @param bluetoothDevice Selected BluetoothDevice
     */
    public void setDefaultDevice(Context context, BluetoothDevice bluetoothDevice) {
        BluetoothDeviceType type = BluetoothDeviceType.getByDeviceTypeByName(bluetoothDevice.getName());
        BluetoothPairing.setDefaultDevice(context, type, bluetoothDevice.getAddress());
    }

    /**
     * Unsets default selected device in category
     *
     * @param context         Application Context
     * @param bluetoothDevice Device to remove form defaults
     */
    public void unsetDefaultDevice(Context context, BluetoothDevice bluetoothDevice) {
        BluetoothDeviceType type = BluetoothDeviceType.getByDeviceTypeByName(bluetoothDevice.getName());
        BluetoothPairing.setDefaultDevice(context, type, null);
    }

    /**
     * Check if selected device is default in category {@link com.miurasystems.miuralibrary.api.executor.MiuraManager.DeviceType}
     *
     * @param context         Application Context
     * @param bluetoothDevice Selected BluetoothDevice
     * @return result
     */
    public boolean isDefaultDevice(Context context, BluetoothDevice bluetoothDevice) {
        BluetoothDeviceType type = BluetoothDeviceType.getByDeviceTypeByName(bluetoothDevice.getName());
        String defaultAddress = BluetoothPairing.getDefaultDeviceAddress(context, type);
        return bluetoothDevice.getAddress().equals(defaultAddress);
    }

    /**
     * @param context    Application Context
     * @param deviceType {@link com.miurasystems.miuralibrary.api.executor.MiuraManager.DeviceType}
     * @return Default device in selected category {@link com.miurasystems.miuralibrary.api.executor.MiuraManager.DeviceType}
     */
    public BluetoothDevice getDefaultSelectedDevice(Context context, BluetoothDeviceType deviceType) {
        BluetoothPairing bluetoothPairing = new BluetoothPairing(context);
        return bluetoothPairing.getDefaultByType(deviceType);
    }

    /**
     * Async operation for getting selected and available devices. Method trying to connect to every device on list and check if it's possible
     *
     * @param context  Application Context
     * @param listener Event listener with selected and available Devices {@link com.miurasample.module.bluetooth.BluetoothDeviceChecking.DevicesListener}
     */
    public void getBluetoothDevicesWithChecking(Context context, BluetoothDeviceChecking.Mode mode, BluetoothDeviceChecking.DevicesListener listener) {
        BluetoothDeviceChecking checks = new BluetoothDeviceChecking(context, mode, listener);
        checks.findDevices();
    }

}