package drivers.pax.utils;

import com.pax.poslink.CommSetting;

/**
 * Created by Luis Camayd on 3/21/2019.
 */
public class PosLinkHelper {

    public static CommSetting getCommSetting() {
        CommSetting commsetting = new CommSetting();
        commsetting.setTimeOut("60000");
        commsetting.setType(CommSetting.AIDL);
        commsetting.setSerialPort("COM1");
        commsetting.setBaudRate("9600");
        commsetting.setDestIP("172.16.20.15");
        commsetting.setDestPort("10009");
        commsetting.setMacAddr("");
        return commsetting;
    }

    public static String getStatus(int reportedStatus) {
        String status = "";
        try {
            switch (reportedStatus) {
                case 0:
                    status = "Ready for CARD INPUT";
                    break;
                case 1:
                    status = "Ready for PIN ENTRY";
                    break;
                case 2:
                    status = "Ready for SIGNATURE";
                    break;
                case 3:
                    status = "Ready for ONLINE PROCESSING";
                    break;
                case 4:
                    status = "Ready for NEW CARD INPUT";
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }
}