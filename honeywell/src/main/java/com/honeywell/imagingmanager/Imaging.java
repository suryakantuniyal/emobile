package com.honeywell.imagingmanager;

import android.graphics.Bitmap;
import android.util.Log;

public class Imaging
  implements ImageConst
{
  private static final String TAG = "Imaging.java";
  private boolean DEBUG = false;

  private native int SetPorfileType(int paramInt);

  private native int ImageProcessingFrame(Bitmap paramBitmap1, Bitmap paramBitmap2, int paramInt);

  public int setProfileType(int type)
  {
    Log.d("Imaging.java", "========setProfileType========");
    return SetPorfileType(type);
  }

  public int imageProcessingFrame(Bitmap bmpin, Bitmap bmpout, int type)
  {
    return ImageProcessingFrame(bmpin, bmpout, type);
  }

  static
  {
    try
    {
      System.loadLibrary("Imaging");
      Log.d("Imaging.java", "Imaging.so loaded successfully");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}