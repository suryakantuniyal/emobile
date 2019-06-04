package drivers.weightScales;

import android.content.Context;
import com.android.support.Global;
import interfaces.EMSDeviceManagerWeightDelegate;

public class WSDeviceManager extends WSDeviceDriver {

    private WSDeviceDriver wsDevice = null;
    private EMSDeviceManagerWeightDelegate currentWeightDevice;
    private int deviceType = -1;

    public WSDeviceManager getManagerWS() {
        return this;
    }

    public boolean loadWeightScaleDriver(Context context, int type, boolean isautoConnect,String macAddress) {
        deviceType = type;
        switch (type) {
            case Global.STARSCALE_S8200:
                wsDevice = new StarScaleS8200(context, this,isautoConnect,macAddress);
                wsDevice.connect();
                return true;
        }

        return false;
    }

    public int getSelectedScale(int type) {
        deviceType = type;
        switch (type) {
            case Global.STARSCALE_S8200:
                return Global.STARSCALE_S8200;
        }

        return -1;
    }

    public void disconnectWeightScaleDriver() {
        if (wsDevice != null) {
            wsDevice.disconnect();
        }
    }

    public int getDeviceType(){
        return deviceType;
    }

    public boolean isWeightScaleConnected(){
        if(wsDevice != null){
            return wsDevice.isConnected();
        }else{return false;}
    }
    public String GetDeviceName(){
        return wsDevice.getDeviceName();
    }
    public String GetMacAddress(){
        return wsDevice.getDeviceMacAddress();
    }
    public EMSDeviceManagerWeightDelegate getCurrentWeightDevice() {
        return currentWeightDevice;
    }

    public void setCurrentWeightDevice(EMSDeviceManagerWeightDelegate currentWeightDevice) {
        this.currentWeightDevice = currentWeightDevice;
    }

    public void WeightDeviceDidConnect(WSDeviceDriver  weightDevice){
        weightDevice.setDevice();
    }

}
