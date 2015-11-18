package com.honeywell.ioctl;

import android.util.Log;

public class HoneywellIOCTL
{
  private static final String TAG = HoneywellIOCTL.class.getSimpleName();
  public static final int HSM_XLOADER_VERSION = 1;
  public static final int HSM_UBOOT_VERSION = 2;
  public static final int HSM_RECOVERY_VERSION = 3;
  public static final int HSM_OSIMAGE_VERSION = 4;
  public static final int HSM_MCU_VERSION = 5;
  public static final int HSM_BATTERY_SERIAL_NUM = 6;
  public static final int HSM_FLASH_SIZE = 7;
  public static final int HSM_KEYBOARD_TYPE = 8;
  public static final int HSM_HARDWARE_REV = 9;
  public static final int HSM_MFG_DATE = 12;
  public static final int HSM_MFG_MODEL_NUM = 13;
  public static final int HSM_MFG_SERIAL_NUM = 14;
  public static final int HSM_MFG_PART_NUM = 15;
  public static final int HSM_ODM_TRACKING_NUM = 16;
  public static final int HSM_CONFIG_NUM = 17;
  public static final int HSM_CUSTOM_SERIAL_NUM = 18;
  public static final int HSM_LICENSE_KEY = 19;
  public static final int HSM_FEATURE_SET = 20;
  public static final int HSM_RESET_REASON = 21;
  public static final int HSM_WAKE_SOURCE = 22;
  public static final int HSM_MFG_DATA_VALID = 23;

  private native int HsmReadData(IoctlRWStruct paramIoctlRWStruct);

  private native int HsmWriteData(IoctlRWStruct paramIoctlRWStruct);

  public int hsmReadData(IoctlRWStruct data)
  {
    return HsmReadData(data);
  }

  public int hsmWriteData(IoctlRWStruct data)
  {
    return HsmWriteData(data);
  }

  static
  {
    try {
      System.loadLibrary("honeywellioctl");
      Log.d(TAG, "libhoneywellioctl.so loaded");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}