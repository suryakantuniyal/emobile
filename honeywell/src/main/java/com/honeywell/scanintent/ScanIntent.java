package com.honeywell.scanintent;

public final class ScanIntent
{
  public static final String SCAN_ACTION = "com.google.zxing.client.android.SCAN";
  public static final String EXTRA_SCAN_MODE = "scan_mode";
  public static final String EXTRA_RESULT_BARCODE_DATA = "barcode_data";
  public static final String EXTRA_RESULT_BARCODE_FORMAT = "barcode_format";
  public static final int SCAN_MODE_SHOW_NO_RESULT = 0;
  public static final int SCAN_MODE_SHOW_RESULT_UI = 1;
  public static final int SCAN_MODE_SHARE_BY_SMS = 2;
  public static final int SCAN_MODE_SHARE_BY_MMS = 3;
  public static final int SCAN_MODE_SHARE_BY_EMAIL = 4;
  public static final int SCAN_MODE_RESULT_AS_URI = 5;
  public static final int SCAN_RESULT_FAILED = -1000;
  public static final int SCAN_RESULT_SUCCESSED = -9999;
}