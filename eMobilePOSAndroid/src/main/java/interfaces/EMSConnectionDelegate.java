package interfaces;

import android.content.Context;

import drivers.EMSDeviceDriver;

public interface EMSConnectionDelegate {

    void driverDidConnectToDevice(EMSDeviceDriver theDevice, boolean showPrompt, Context activity);

    void driverDidDisconnectFromDevice(EMSDeviceDriver theDevice, boolean showPrompt);

    void driverDidNotConnectToDevice(EMSDeviceDriver theDevice, String msg, boolean showPrompt, Context activity);
}
