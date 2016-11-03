package com.honeywell.imagingmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.honeywell.decodeconfigcommon.IDecodeComplete;
import com.honeywell.decodeconfigcommon.IDecodeComplete.Stub;
import com.honeywell.decodeconfigcommon.IDecoderService;
import com.honeywell.decodemanager.barcode.BitmapParcel;
import com.honeywell.decodemanager.barcode.DecodeResult;
import com.honeywell.decodemanager.barcode.ImageAttributes;
import com.honeywell.decodemanager.barcode.ImagerProperties;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImagingManager
  implements ImageConst
{
  private final String TAG = "ImagingManager";

  final String STRING_IMAGE_FOLDER = Environment.getExternalStorageDirectory().toString() + File.separatorChar + "honeywell" + File.separatorChar + "imagedemo";

  private Imaging mImaging = new Imaging();
  private Context mContext;
  private IDecoderService mDecoderService;
  private SimpleDateFormat timeStampFormat = new SimpleDateFormat("MMddHHmmssSS");
  private Handler mHandler;
  private IDecodeComplete.Stub mDecodeResult = new IDecodeComplete.Stub()
  {
    public void onDecodeComplete(int type, DecodeResult decode_result) throws RemoteException
    {
      if (ImagingManager.this.mHandler != null)
        if (type == 24)
          ImagingManager.this.mHandler.sendEmptyMessage(18);
        else if (type == 25)
          ImagingManager.this.mHandler.sendEmptyMessage(19);
        else if (type == 26)
          ImagingManager.this.mHandler.sendEmptyMessage(20);
    }
  };

  private ServiceConnection mConnection = new ServiceConnection()
  {
    public void onServiceDisconnected(ComponentName arg0)
    {
      ImagingManager.this.mDecoderService = null;
      Log.e("ImagingManager", "Disconnected from DecoderService");
    }

    public void onServiceConnected(ComponentName name, IBinder service)
    {
      ImagingManager.this.mDecoderService = IDecoderService.Stub.asInterface(service);
      Log.e("ImagingManager", "Success Connect to DecorderService ");

      if (ImagingManager.this.mHandler != null)
      {
        ImagingManager.this.mHandler.sendEmptyMessage(17);
      }
    }
  };

  public ImagingManager(Context context, Handler hander)
  {
    this.mContext = context;
    this.mHandler = hander;
    bindDecoderService();
    Log.i("ImagingManager", "Begin to new ImagingManager");
  }

  public int release()
    throws IOException
  {
    unbindDecoderService();
    return 0;
  }

  public int getImageWidth()
  {
    int ret = 0;
    try {
      return this.mDecoderService.getImageWidth();
    }
    catch (RemoteException e) {
      e.printStackTrace();
    }
    return ret;
  }

  public int getImageHeight()
  {
    int ret = 0;
    try {
      return this.mDecoderService.getImageHeight();
    }
    catch (RemoteException e) {
      e.printStackTrace();
    }
    return ret;
  }

  public int getPreviewingWidth()
  {
    return getImageWidth() / 4;
  }

  public int getPreviewngHeight()
  {
    return getImageHeight() / 4;
  }

  public int doImageAction(BitmapParcel bmpparcel, int type)
  {
    int ret = -1;

    if (this.mDecoderService != null) {
      try {
        return this.mDecoderService.doGetImage(bmpparcel, type);
      }
      catch (RemoteException e) {
        e.printStackTrace();
      }
    }
    return ret;
  }

  public void sendImageRequest(int type)
  {
    if (this.mDecoderService != null)
      try {
        this.mDecoderService.doImageRequest(this.mDecodeResult, type);
      }
      catch (RemoteException e) {
        e.printStackTrace();
      }
  }

  public int setProfileType(int type)
  {
    return this.mImaging.setProfileType(type);
  }

  public int ImageProcessingFrame(Bitmap bmpin, Bitmap bmpout, int type)
  {
    return this.mImaging.imageProcessingFrame(bmpin, bmpout, type);
  }

  public int setLightMode(int mode)
  {
    int ret = -1;
    if (this.mDecoderService != null) {
      try
      {
        return this.mDecoderService.setLightMode(mode);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
    return ret;
  }

  public boolean saveBitmap(String filename, int imagetype, Bitmap bitmap)
  {
    int type = 0;

    boolean resultcompress = false;
    String image_name;
    if (filename == "") {
      type = 6;
      image_name = this.STRING_IMAGE_FOLDER + "/" + "image " + this.timeStampFormat.format(new Date()) + ".png";
    }
    else
    {
      type = imagetype;
      image_name = filename;
    }

    OutputStream outStream = null;
    try
    {
      outStream = new FileOutputStream(image_name);
      resultcompress = bitmap.compress(type == 6 ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, outStream);

      outStream.flush();
      outStream.close();
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return resultcompress;
  }

  public int getEngineID()
  {
    int ret = -1;
    if (this.mDecoderService != null) {
      try {
        return this.mDecoderService.getEngineID();
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
    return ret;
  }

  public int getEngineType()
  {
    int ret = -1;
    if (this.mDecoderService != null) {
      try {
        return this.mDecoderService.getEngineType();
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
    return ret;
  }

  public int startScanning()
  {
    int ret = -1;
    if (this.mDecoderService != null) {
      try {
        return this.mDecoderService.startScanning();
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
    return ret;
  }

  public int stopScanning()
  {
    int ret = -1;
    if (this.mDecoderService != null) {
      try {
        return this.mDecoderService.stopScanning();
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
    return ret;
  }

  public int setExposureSettings(int[] expSettings)
  {
    try {
      return this.mDecoderService.setExposureSettings(expSettings);
    }
    catch (RemoteException e) {
      e.printStackTrace();
    }
    return -1;
  }

  public int setExposureMode(int mode)
  {
    try {
      return this.mDecoderService.setExposureMode(mode);
    }
    catch (RemoteException e) {
      e.printStackTrace();
    }
    return -1;
  }

  public void getImagerProperties(ImagerProperties imgProp) {
    try {
      this.mDecoderService.getImagerProperties(imgProp);
    }
    catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  public String getAPIRevision()
  {
    if (this.mDecoderService != null) {
      try {
        return this.mDecoderService.getAPIRevision();
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  public byte[] getLastImage(ImageAttributes imgProperty) {
    try {
      return this.mDecoderService.getLastImage(imgProperty);
    }
    catch (RemoteException e) {
      e.printStackTrace();
    }
    return null;
  }

  private boolean bindDecoderService() {
    Intent intent = new Intent("com.honeywell.decoderservice.STARTSERVICE");
    return this.mContext.bindService(intent, this.mConnection, 1);
  }

  private void unbindDecoderService() {
    this.mContext.unbindService(this.mConnection);
  }
}