package drivers.weightScales;

import android.content.Context;
import com.android.support.Global;
import interfaces.EMSDeviceManagerWeightDelegate;

public class WSDeviceManager extends WSDeviceDriver {

    private WSDeviceDriver wsDevice = null;
    private EMSDeviceManagerWeightDelegate currentWeightDevice;

    public WSDeviceManager getManagerWS() {
        return this;
    }

    public boolean loadWeightScaleDriver(Context context, int type) {
        switch (type) {
            case Global.STARSCALE_S8200:
                wsDevice = new StarScaleS8200(context, this);
                wsDevice.connect();
                return true;
        }

        return false;
    }

    public void disconnectWeightScaleDriver() {
        if (wsDevice != null) {
            wsDevice.disconnect();
        }
    }

    public boolean isWeightScaleConnected(){
        if(wsDevice != null){
            return wsDevice.isConnected();
        }else{return false;}
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
