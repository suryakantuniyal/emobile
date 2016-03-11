package interfaces;

import drivers.EMSDeviceDriver;

public interface EMSConnectionDelegate {

	void driverDidConnectToDevice(EMSDeviceDriver theDevice, boolean showPrompt);

	void driverDidDisconnectFromDevice(EMSDeviceDriver theDevice, boolean showPrompt);

	void driverDidNotConnectToDevice(EMSDeviceDriver theDevice, String msg, boolean showPrompt);
}
