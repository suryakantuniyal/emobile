package com.honeywell.test;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import com.honeywell.decodemanager.barcode.BitmapParcel;
import com.honeywell.imagingmanager.ImageConst;
import com.honeywell.imagingmanager.ImagingManager;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ImagingManagerTest extends Activity
  implements ImageConst
{
  private ImagingView mImageView;
  private ImagingManager mImagingManager;
  private Lock mImagingServiceLock = new ReentrantLock();
  private Bitmap mCaptureBmp = Bitmap.createBitmap(832, 640, Bitmap.Config.ALPHA_8);
  private BitmapParcel mFrameParcel;
  private String TAG = "";

  private Handler mHandler = new Handler() {
    public void handleMessage(Message msg) {
      switch (msg.what)
      {
      case 17:
        break;
      case 18:
        ImagingManagerTest.this.captureFrame();
        break;
      case 3:
        ImagingManagerTest.this.mImageView.setShowBitmapflag(true);
        ImagingManagerTest.this.mImageView.invalidate();
        break;
      }
    }
  };

  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.mImageView = new ImagingView(this);
    this.mImageView.setHandler(this.mHandler);
    setContentView(this.mImageView);
    this.mImagingManager = new ImagingManager(this, this.mHandler);
  }

  public boolean onKeyDown(int keyCode, KeyEvent event)
  {
    switch (keyCode)
    {
    case 66:
    case 92:
      scanImage();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  public void onResume()
  {
    super.onResume();
    super.onResume();
  }

  public void onDestroy()
  {
    super.onDestroy();
    if (this.mImageView != null) {
      this.mImageView.stop();
      this.mImageView = null;
    }

    this.mCaptureBmp.recycle();
    this.mCaptureBmp = null;
  }

  public void onPause()
  {
    super.onPause();
    try {
      this.mImagingManager.release();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void scanImage()
  {
  }

  private void captureFrame()
  {
    this.mImageView.setShowBitmapflag(true);
    new Thread(new FrameRunnable()).start();
  }

  private class FrameRunnable
    implements Runnable
  {
    private FrameRunnable()
    {
    }

    public void run()
    {
      ImagingManagerTest.this.mImagingServiceLock.lock();
      if (ImagingManagerTest.this.mCaptureBmp != null)
      {
        ImagingManagerTest.this.mFrameParcel = new BitmapParcel(ImagingManagerTest.this.mCaptureBmp);

        int ret = ImagingManagerTest.this.mImagingManager.doImageAction(ImagingManagerTest.this.mFrameParcel, 0);
        if (ret != 0) {
          Log.e(ImagingManagerTest.this.TAG, "Error in capturing from decoder");
        } else {
          ImagingManagerTest.this.mImagingManager.ImageProcessingFrame(ImagingManagerTest.this.mFrameParcel.getBitmap(), ImagingManagerTest.this.mImageView.getCaptureBitmap(), 0);

          ImagingManagerTest.this.mHandler.sendEmptyMessage(3);
        }
        ImagingManagerTest.this.mImageView.setPreviewFlag(false);
        ImagingManagerTest.this.mFrameParcel.releaseBitmap();
        ImagingManagerTest.this.mFrameParcel = null;
      }
      ImagingManagerTest.this.mImagingServiceLock.unlock();
    }
  }
}