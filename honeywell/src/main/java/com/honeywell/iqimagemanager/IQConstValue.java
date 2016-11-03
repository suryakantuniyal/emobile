package com.honeywell.iqimagemanager;

public abstract interface IQConstValue
{
  public static final int IQTYPE_DELIVERY = 0;
  public static final int IQTYPE_POSTAL = 1;
  public static final int IQTYPE_EMBED = 2;
  public static final int IQTYPE_NOFORMAT = 3;
  public static final int IQTYPE_DEFAULT = 4;
  public static final int IQ_RESOLUTION = 4;
  public static final int POSTAL_WIDTH = 180;
  public static final int POSTAL_HEIGHT = 90;
  public static final int DELIVERY_WIDTH = 124;
  public static final int DELIVERY_HEIGHT = 58;
  public static final int MESSAGE_DECODE_SUC = 99;

  public static enum IqImageFormats
  {
    Binary, Gray;
  }
}