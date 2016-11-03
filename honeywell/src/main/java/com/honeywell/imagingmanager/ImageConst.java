package com.honeywell.imagingmanager;

public abstract interface ImageConst
{
  public static final int IMAGE_PNG = 6;
  public static final int IMAGE_JPEG = 7;
  public static final int MESSAGE_UPDATE_IMG = 3;
  public static final int MESSAGE_UPDATE_ZOOMBUTTON = 4;
  public static final int MESSAGE_GETFRAME = 5;
  public static final int MESSAGE_UPDATE_PREVEIW = 6;
  public static final int MESSAGE_UPDATE_ZOOMBAR = 7;
  public static final int MESSAGE_BEGIN_SAVE = 8;
  public static final int MESSAGE_PREVIEWING = 12;
  public static final int MESSAGE_READY_DOCODER = 17;
  public static final int MESSAGE_READY_TO_IMAGE = 18;
  public static final int MESSAGE_READY_TO_PREVIEW = 19;
  public static final int MESSAGE_STOP_PREVIEW = 20;
  public static final int CAPTUERED_IMAGE_WIDTH = 832;
  public static final int CAPTUERED_IMAGE_HEIGHT = 640;
  public static final int PREVIEW_RATIO = 4;
  public static final int PROFILE_LOW_LIGHT = 0;
  public static final int PROFILE_DOCUMENT = 1;
  public static final int PROFILE_DISTANT = 2;
  public static final int PROFILE_BW = 3;
  public static final int PROFILE_GS = 4;
  public static final int PROFILE_NORMAL = 5;
  public static final int OPERATION_FRAME = 0;
  public static final int OPERATION_PREVIEWING = 1;
  public static final int OPERATION_STOPPREVIEW = 2;
}