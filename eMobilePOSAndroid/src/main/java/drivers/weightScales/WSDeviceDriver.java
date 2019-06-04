package drivers.weightScales;

import com.android.support.Global;

public class WSDeviceDriver {

    private double weightResult;
    private String weightUnit = "";
    private String mac,Name = "";
    private boolean conn;
    WSDeviceManager wsdm;

    public void setDevice() {
    }
    public void connect() {
    }
    public boolean isConnected() {
        return conn;
    }
    public void disconnect() {
    }
    public String getDeviceMacAddress() {
        return mac;
    }
    public String getDeviceName() {
        return Name;
    }


    public double getWeightFromScale() {
        if (wsdm == null) {
            wsdm = new WSDeviceManager();
            wsdm.getManagerWS();
        }
        if (wsdm != null) {
            weightResult = Global.mainWeightScaleManager.getCurrentWeightDevice().getScaleWeight();
        }
        return weightResult;
    }

    public String getUnitFromScale() {
        if (wsdm != null) {
            weightUnit = wsdm.getCurrentWeightDevice().getScaleUnit();
        }
        return weightUnit;
    }

    public void setConnectionDetails(boolean connectionStatus,String name,String mMac) {
        conn = connectionStatus;
        Name = name;
        mac = mMac;
    }
}
