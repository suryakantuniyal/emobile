package interfaces;

public interface EMSDeviceManagerWeightDelegate {

    double getScaleWeight();
    String getFormatedScaleWeight();
    String getScaleUnit();
    String getMacAddress();
    String getName();
    void setMacAddress(String mac);
    void setWeightScaleDevice();
}
