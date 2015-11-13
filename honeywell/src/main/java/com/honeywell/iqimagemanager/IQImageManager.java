package com.honeywell.iqimagemanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.honeywell.decodeconfigcommon.IDecodeComplete;
import com.honeywell.decodeconfigcommon.IDecodeComplete.Stub;
import com.honeywell.decodeconfigcommon.IDecoderService;
import com.honeywell.decodemanager.barcode.DecodeResult;
import com.honeywell.decodemanager.barcode.IQBitmapParcel;
import com.honeywell.decodemanager.barcode.IQImagingProperties;

public class IQImageManager
  implements IQConstValue
{
  private IDecoderService mDecoderService;
  private Context mContext;
  private Handler mIQhanler;
  private String TAG = "IQImageManager";
  public static final int DECODEFAIL = 4097;
  public static final int DECODEREADY = 4098;
  private IDecodeComplete.Stub mDecodeResult = new IDecodeComplete.Stub()
  {
    public void onDecodeComplete(int error_code, DecodeResult res) throws RemoteException
    {
      String strResult = "";
      if (error_code == 0)
      {
        if (IQImageManager.this.mIQhanler != null) {
          IQImageManager.this.mIQhanler.obtainMessage(99, res).sendToTarget();
          strResult = "result is " + res.barcodeData;
        }
      }
      else if (IQImageManager.this.mIQhanler != null) {
        IQImageManager.this.mIQhanler.obtainMessage(4097, res).sendToTarget();
        strResult = "scan fail";
      }

      Log.e(IQImageManager.this.TAG, strResult);
    }
  };

  private ServiceConnection mConnection = new ServiceConnection()
  {
    public void onServiceDisconnected(ComponentName arg0) {
      IQImageManager.this.mDecoderService = null;
      Log.e(IQImageManager.this.TAG, "Disconnected from DecoderService");
    }

    public void onServiceConnected(ComponentName name, IBinder service)
    {
      IQImageManager.this.mDecoderService = IDecoderService.Stub.asInterface(service);
      if (IQImageManager.this.mIQhanler != null)
        IQImageManager.this.mIQhanler.obtainMessage(4098, "").sendToTarget();
    }
  };

  public IQImageManager(Context context, Handler hanler)
  {
    this.mContext = context;
    this.mIQhanler = hanler;
    if (bindDecoderService())
      Log.e(this.TAG, "Success: bindDecoderService()");
    else
      Log.e(this.TAG, "Fail: bindDecoderService()");
  }

  public int getIQImage(IQImagingProperties propery, IQBitmapParcel bmpparcel)
    throws RemoteException
  {
    if (this.mDecoderService != null)
      return this.mDecoderService.getIQImage(propery, bmpparcel);
    return -1;
  }

  public int initIQEnv()
  {
    return 0;
  }

  public int reaseIQEnv()
  {
    unbindDecoderService();
    return 0;
  }

  public int activateSYMID(int symbologyID)
    throws RemoteException
  {
    if (this.mDecoderService != null) {
      return this.mDecoderService.enableSymbology(symbologyID);
    }
    return 1;
  }

  public int inactivateSYMID(int symbologyID)
    throws RemoteException
  {
    if (this.mDecoderService != null) {
      return this.mDecoderService.disableSymbology(symbologyID);
    }
    return 1;
  }

  public int decodeIQWithTimout(int timeout, DecodeResult decRes)
    throws RemoteException
  {
    if (this.mDecoderService != null)
      this.mDecoderService.doDecode(this.mDecodeResult, timeout);
    return 0;
  }

  private boolean bindDecoderService()
  {
    Intent intent = new Intent("com.honeywell.decoderservice.STARTSERVICE");
    return this.mContext.bindService(intent, this.mConnection, 1);
  }

  private void unbindDecoderService() {
    this.mContext.unbindService(this.mConnection);
    Log.e(this.TAG, "unbindDecoderService()");
  }
}