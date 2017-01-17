package drivers.miurasample.core;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by mgadzala on 19/07/2016.
 */
public class Config {

    public static final String REQUIRED_OS_VERSION = "7-7";
    public static final String REQUIRED_MPI_VERSION = "1-41";
    public static final String REQUIRED_RPI_VERSION = "1-3";
    public static final String REQUIRED_RPI_OS_VERSION = "1-3";
    public static final String REQUIRED_CONFIG_FILE = "emv.cfg";
    public static final String REQUIRED_CONFIG_VERSION = "4.2";
    public static final int MAX_TIME_DIFFERENCE_SECONDS = 10;
    public static final int MIN_BATTERY_LEVEL = 10;


    public static boolean isOsVersionValid(String osVersion) {
        return osVersion.equals(REQUIRED_OS_VERSION);
    }

    public static boolean isRpiVersionValid(String mpiVersion) {
        return mpiVersion.equals(REQUIRED_RPI_VERSION);
    }

    public static boolean isRpiOsVersionValid(String mpiVersion) {
        return mpiVersion.equals(REQUIRED_RPI_OS_VERSION);
    }

    public static boolean isMpiVersionValid(String mpiVersion) {
        return mpiVersion.equals(REQUIRED_MPI_VERSION);
    }

    public static boolean isConfigVersionValid(HashMap<String, String> configVersions) {
        return configVersions.containsValue(REQUIRED_CONFIG_VERSION);
    }

    public static boolean isTimeValid(Date dateTime) {
        return (new Date().getTime() - dateTime.getTime()) < (MAX_TIME_DIFFERENCE_SECONDS * 1000);
    }

    public static boolean isBatteryValid(int batteryLevel) {
        return batteryLevel >= MIN_BATTERY_LEVEL;
    }
}
