package protocols;

import drivers.EMSDeviceDriver;

public interface EMSConnectionDelegate {

	public void driverDidConnectToDevice(EMSDeviceDriver theDevice,boolean showPrompt);

	public void driverDidDisconnectFromDevice(EMSDeviceDriver theDevice,boolean showPrompt);

	public void driverDidNotConnectToDevice(EMSDeviceDriver theDevice,String msg,boolean showPrompt);
}
