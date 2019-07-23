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

    public static String getStatus(String reportedStatus) {
        String[] status = {
                "Ready for CARD INPUT",
                "Ready for PIN ENTRY",
                "Ready for SIGNATURE",
                "Ready for ONLINE PROCESSING",
                "Ready for NEW CARD INPUT"
        };
        try {
            return status[Integer.parseInt(reportedStatus)];
        } catch (Exception e) {
            return "";
        }
    }

    public static String getEntryModeValue(String entryMode) {
        String[] entryModeValues = {
                "Manual",
                "Swipe",
                "Contactless",
                "Scanner",
                "Chip",
                "Chip Fall Back Swipe"
        };
        try {
            return entryModeValues[Integer.parseInt(entryMode)];
        } catch (Exception e) {
            return "";
        }
    }

    public static String getCvmMessage(String cvm) {
        String[] cvmMessages = {
                "Fail CVM processing",
                "Plaintext Offline PIN Verification",
                "OnlinePIN",
                "Plaintext Offline PIN and Signature",
                "Enciphered Offline PIN Verification",
                "Enciphered Offline PIN Verification and Signature",
                "Signature",
                "No CVM Required"
        };
        try {
            return cvmMessages[Integer.parseInt(cvm)];
        } catch (Exception e) {
            return "";
        }
    }

    public static String payMethodDictionary(String value) {
        if (value == null) return "";

        switch (value.toUpperCase()) {
            case "VISA":
                return "Visa";
            case "MASTERCARD":
                return "MasterCard";
            case "AMEX":
                return "AmericanExpress";
            case "DISCOVER":
                return "Discover";
            case "DINERCLUB":
                return "DinersClub";
            case "JCB":
                return "JCB";
            case "DEBIT":
                return "DebitCard";
            default:
                return "Visa";
        }
    }
}