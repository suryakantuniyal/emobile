package com.honeywell.test;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import com.honeywell.decodemanager.barcode.DecodeResult;
import com.honeywell.decodemanager.barcode.IQBitmapParcel;
import com.honeywell.decodemanager.barcode.IQImagingProperties;
import com.honeywell.iqimagemanager.IQConstValue;
import com.honeywell.iqimagemanager.IQImageManager;

public class IQTest extends Activity
  implements IQConstValue
{
  private ImageView mView;
  private Bitmap mBitmap;
  private IQImagingProperties mProperty;
  private IQImageManager mIQManger;
  private String TAG = "IQTest";
  private Handler mHandler = new Handler() {
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case 99:
        IQTest.this.mProperty = new IQImagingProperties();
        IQTest.this.mProperty.Reserved = -1;

        IQTest.this.mProperty.AspectRatio = 1;
        IQTest.this.mProperty.X_Offset = 2;
        IQTest.this.mProperty.Y_Offset = 44;
        IQTest.this.mProperty.Width = 124;
        IQTest.this.mProperty.Height = 58;
        IQTest.this.mProperty.Resolution = 4;
        try
        {
          IQTest.this.mBitmap.eraseColor(-65536);
          IQBitmapParcel bmpparcel = new IQBitmapParcel(IQTest.this.mBitmap);
          int ress = IQTest.this.mIQManger.getIQImage(IQTest.this.mProperty, bmpparcel);
          Log.e(IQTest.this.TAG, "the return of getIQImage is " + ress);
          if (ress == 0) {
            Log.e(IQTest.this.TAG, "the value of [0][0] is " + (IQTest.this.mBitmap.getPixel(0, 0) >> 24));

            IQTest.this.mView.invalidate();
          }
        }
        catch (RemoteException e) {
          e.printStackTrace();
        }
      }
    }
  };

  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.mView = new ImageView(this);
    this.mBitmap = Bitmap.createBitmap(496, 232, Bitmap.Config.RGB_565);
    this.mBitmap.eraseColor(-16776961);
    this.mView.setBackgroundDrawable(new BitmapDrawable(this.mBitmap));
    this.mIQManger = new IQImageManager(this, this.mHandler);
    setContentView(this.mView);
  }

  public boolean onKeyDown(int keyCode, KeyEvent event)
  {
    switch (keyCode) {
    case 92:
      DecodeResult decRes = new DecodeResult();
      try {
        this.mIQManger.decodeIQWithTimout(1000, decRes);
      }
      catch (RemoteException e) {
        e.printStackTrace();
      }
      if (decRes.length != 0);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  public void onResume()
  {
    super.onResume();
    this.mIQManger.initIQEnv();
  }

  public void onPause()
  {
    super.onPause();
    this.mIQManger.reaseIQEnv();
  }

  public void onDestroy()
  {
    super.onDestroy();
    this.mBitmap.recycle();
  }
}