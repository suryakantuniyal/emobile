package com.honeywell.decodemanager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;
import com.honeywell.decodeconfigcommon.ConfigDoc;
import com.honeywell.decodeconfigcommon.IDecodeComplete;
import com.honeywell.decodeconfigcommon.IDecodeComplete.Stub;
import com.honeywell.decodeconfigcommon.IDecoderService;
import com.honeywell.decodemanager.barcode.DecodeResult;
import com.honeywell.decodemanager.barcode.ImageAttributes;
import com.honeywell.decodemanager.barcode.ImagerProperties;
import com.honeywell.decodemanager.barcode.SymbologyConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DecodeManager
{
  private IDecoderService mDecoderService;
  private static final String TAG = "DecodeManager";
  private Context m_Context = null;
  private Device mDevice;
  private Symbology mSymbology;
  private Decode mDecode;
  private Handler mScanResultHandler = null;
  public static final int MESSAGE_DECODER_COMPLETE = 4096;
  public static final int MESSAGE_DECODER_FAIL = 4097;
  public static final int MESSAGE_DECODER_READY = 4098;
  private String mSourceConfigName;
  private SymConfigActivityOpeartor mSymConfigActivity = null;
  private boolean isConnected = false;

  private IDecodeComplete.Stub mDecodeResult = new IDecodeComplete.Stub()
  {
    public void onDecodeComplete(int error_code, DecodeResult res)
      throws RemoteException
    {
      String strResult = "";
      if (error_code == 0)
      {
        if (DecodeManager.this.mScanResultHandler != null) {
          DecodeManager.this.mScanResultHandler.obtainMessage(4096, res).sendToTarget();
        }

      }
      else if (DecodeManager.this.mScanResultHandler != null) {
        DecodeManager.this.mScanResultHandler.obtainMessage(4097, res).sendToTarget();

        strResult = "scan fail";
        Log.e("DecodeManager", strResult);
      }
    }
  };

  private ServiceConnection mConnection = new ServiceConnection()
  {
    public void onServiceDisconnected(ComponentName arg0) {
      DecodeManager.this.mDecoderService = null;
      Log.e("DecodeManager", "Disconnected from DecoderService");
      isConnected = false;
    }

    public void onServiceConnected(ComponentName name, IBinder service)
    {
      DecodeManager.this.mDecoderService = IDecoderService.Stub.asInterface(service);
      Log.e("DecodeManager", "Success Connect to DecorderService ");

      if (DecodeManager.this.mScanResultHandler != null) {
        DecodeManager.this.mScanResultHandler.obtainMessage(4098, "").sendToTarget();
      }

      ConfigDoc.getSingle().LoadConfig(DecodeManager.this.mDecoderService, false);
      isConnected = true;
    }
  };

  public boolean isConnected()
  {
	  return this.isConnected;
  }
  private boolean bindDecoderService()
  {
    Intent intent = new Intent("com.honeywell.decoderservice.STARTSERVICE");
    intent.setPackage("com.honeywell");
    return this.m_Context.bindService(intent, this.mConnection, 1);
  }

  private void unbindDecoderService()
  {
    this.m_Context.unbindService(this.mConnection);
    Log.e("DecodeManager", "unbindDecoderService()");
  }

  public DecodeManager(Context context, Handler handler)
  {
    Log.e("DecodeManager", "Success: DecodeManager()");
    this.m_Context = context;
    this.mDevice = new Device();
    this.mSymbology = new Symbology();
    this.mDecode = new Decode();
    this.mSourceConfigName = this.m_Context.getPackageName();

    this.mDecoderService = null;
    this.mScanResultHandler = handler;

    this.mSymConfigActivity = new SymConfigActivityOpeartor();

    if (bindDecoderService())
      Log.e("DecodeManager", "Success: bindDecoderService()");
    else {
      Log.e("DecodeManager", "Fail: bindDecoderService()");
    }
    ConfigDoc.getSingle().InitialConfig(this.mSourceConfigName, this.m_Context);
  }

  public void release()
    throws IOException
  {
    this.mDevice.release();
    unbindDecoderService();
  }

  public SymConfigActivityOpeartor getSymConfigActivityOpeartor()
  {
    return this.mSymConfigActivity;
  }

  public String getErrorMessage(int error)
    throws RemoteException
  {
    if (this.mDevice != null) {
      return this.mDevice.getErrorMessage(error);
    }
    return null;
  }

  public int getMaxMessageSize()
    throws RemoteException
  {
    if (this.mDevice != null) {
      return this.mDevice.getMaxMessageSize();
    }
    return -1;
  }

  public int getEngineID()
    throws RemoteException
  {
    if (this.mDevice != null) {
      return this.mDevice.getEngineID();
    }
    return -1;
  }

  public int getEngineType()
    throws RemoteException
  {
    if (this.mDevice != null) {
      return this.mDevice.getEngineType();
    }
    return -1;
  }

  public String getEngineSerialNumber()
    throws RemoteException
  {
    if (this.mDevice != null) {
      return this.mDevice.getEngineSerialNumber();
    }
    return null;
  }

  public String getAPIRevision()
    throws RemoteException
  {
    if (this.mDevice != null) {
      return this.mDevice.getAPIRevision();
    }
    return null;
  }

  public String getDecoderRevision()
    throws RemoteException
  {
    if (this.mDevice != null) {
      return this.mDevice.getDecoderRevision();
    }
    return null;
  }

  public String getSecondaryDecoderRevision()
    throws RemoteException
  {
    if (this.mDevice != null) {
      return this.mDevice.SecondaryDecoderRevision();
    }
    return null;
  }

  public String getControlLogicRevision()
    throws RemoteException
  {
    if (this.mDevice != null) {
      return this.mDevice.getControlLogicRevision();
    }
    return null;
  }

  public String getDecThreadsRevison()
    throws RemoteException
  {
    if (this.mDevice != null) {
      return this.mDevice.getDecThreadsRevision();
    }
    return null;
  }

  public String getScanDriverRevison()
    throws RemoteException
  {
    if (this.mDevice != null) {
      return this.mDevice.getScanDriverRevision();
    }
    return null;
  }

  public int getPSOCMajorRev()
    throws RemoteException
  {
    if (this.mDevice != null) {
      return this.mDevice.getPSOCMajorRev();
    }
    return -1;
  }

  public int getPSOCMinorRev()
    throws RemoteException
  {
    if (this.mDevice != null) {
      return this.mDevice.getPSOCMinorRev();
    }
    return -1;
  }

  public int enableSymbology(int symbologyID)
    throws RemoteException
  {
    if (this.mSymbology != null) {
      return this.mSymbology.enableSymbology(symbologyID);
    }
    return -1;
  }

  public int disableSymbology(int symbologyID)
    throws RemoteException
  {
    if (this.mSymbology != null) {
      return this.mSymbology.disableSymbology(symbologyID);
    }
    return -1;
  }

  public int setSymbologyDefaults(int symbologyID)
    throws RemoteException
  {
    if (this.mSymbology != null) {
      return this.mSymbology.setSymbologyDefaults(symbologyID);
    }
    return -1;
  }

  public int getSymbologyMinRange(int symbologyID)
    throws RemoteException
  {
    if (this.mSymbology != null) {
      return this.mSymbology.getSymbologyMinRange(symbologyID);
    }
    return -1;
  }

  public int getSymbologyMaxRange(int symbologyID)
    throws RemoteException
  {
    if (this.mSymbology != null) {
      return this.mSymbology.getSymbologyMaxRange(symbologyID);
    }
    return -1;
  }

  public int getImagerProperties(ImagerProperties imgProp)
    throws RemoteException
  {
    if (this.mSymbology != null) {
      return this.mSymbology.getImagerProperties(imgProp);
    }
    return -1;
  }

  public int setOCRTemplates(int templates)
    throws RemoteException
  {
    if (this.mSymbology != null) {
      return this.mSymbology.setOCRTemplates(templates);
    }
    return -1;
  }

  public int setOCRUserTemplate(int mode, byte[] template)
    throws RemoteException
  {
    if (this.mSymbology != null) {
      return this.mSymbology.setOCRUserTemplate(mode, template);
    }
    return -1;
  }

  public int getSymbologyConfig(SymbologyConfig symConfig, boolean defaultValues)
    throws RemoteException
  {
    if (this.mSymbology != null) {
      return this.mSymbology.getSymbologyConfig(symConfig, defaultValues);
    }
    return -1;
  }

  public int setSymbologyConfig(SymbologyConfig symConfg)
    throws RemoteException
  {
    if (this.mSymbology != null) {
      return this.mSymbology.setSymbologyConfig(symConfg);
    }
    return -1;
  }

  public int setSymbologyConfigs(SymbologyConfigs symConfg)
    throws RemoteException
  {
    if (this.mSymbology != null) {
      return this.mSymbology.setSymbologyConfigs(symConfg);
    }
    return -1;
  }

  public void doDecode(int timeout)
    throws RemoteException
  {
    Log.i("DecodeManager", "doDecode");
    if (this.mDecode != null)
      this.mDecode.doDecode(timeout);
  }

  public int setLightMode(int mode)
    throws RemoteException
  {
    if (this.mDecode != null) {
      return this.mDecode.setLightMode(mode);
    }
    return -1;
  }

  public int setDecodeSearchLimit(int limit)
    throws RemoteException
  {
    if (this.mDecode != null) {
      return this.mDecode.setDecodeSearchLimit(limit);
    }
    return -1;
  }

  public int setDecodeAttemptLimit(int limit)
    throws RemoteException
  {
    if (this.mDecode != null) {
      return this.mDecode.setDecodeAttemptLimit(limit);
    }
    return -1;
  }

  public byte getBarcodeCodeID()
    throws RemoteException
  {
    if (this.mDecode != null) {
      return this.mDecode.getBarcodeCodeID();
    }
    return -1;
  }

  public byte getBarcodeAimID()
    throws RemoteException
  {
    if (this.mDecode != null) {
      return this.mDecode.getBarcodeAimID();
    }
    return -1;
  }

  public byte getBarcodeAimModifier()
    throws RemoteException
  {
    if (this.mDecode != null) {
      return this.mDecode.getBarcodeAimModifier();
    }
    return -1;
  }

  public int getBarcodeLength()
    throws RemoteException
  {
    if (this.mDecode != null) {
      return this.mDecode.getBarcodeLength();
    }
    return -1;
  }

  public String getBarcodeData()
    throws RemoteException
  {
    if (this.mDecode != null) {
      return this.mDecode.getBarcodeData();
    }
    return null;
  }

  public int getLastDecodeTime()
    throws RemoteException
  {
    if (this.mDecode != null) {
      return this.mDecode.getLastDecodeTime();
    }
    return -1;
  }

  public byte[] getLastImage(ImageAttributes imgAtt)
    throws RemoteException
  {
    if (this.mDecoderService != null) {
      return this.mDecoderService.getLastImage(imgAtt);
    }
    return null;
  }

  public int decodeIamge(byte[] image, DecodeResult result, int width, int height)
    throws RemoteException
  {
    if (this.mDecoderService != null) {
      return this.mDecoderService.decodeImage(image, result, width, height);
    }
    return -1;
  }

  public int setExposureMode(int mode)
    throws RemoteException
  {
    if (this.mDecoderService != null) {
      return this.mDecoderService.setExposureMode(mode);
    }
    return -1;
  }

  public int setScanMode(int mode)
    throws RemoteException
  {
    Log.e("DecodeManager", "It is not support in this version now!");
    return 0;
  }

  public void cancelDecode()
    throws RemoteException
  {
    if (this.mDecoderService != null)
      this.mDecoderService.cancelDecode(this.mDecodeResult);
  }

  public byte[] getBarcodeByteData()
    throws RemoteException
  {
    if (this.mDecoderService != null) {
      return this.mDecoderService.getBarcodeByteData();
    }
    return null;
  }

  public int setProperty(int property, int value)
    throws RemoteException
  {
    if (this.mDecoderService != null) {
      return this.mDecoderService.setProperty(property, value);
    }
    return -1;
  }

  private class Decode
  {
    private Decode()
    {
    }

    public void doDecode(int timeout)
      throws RemoteException
    {
      if (DecodeManager.this.mDecoderService != null)
        DecodeManager.this.mDecoderService.doDecode(DecodeManager.this.mDecodeResult, timeout);
      else
        Log.i("DecodeManager", "=========Error:mDecoderService is null=============");
    }

    public int setLightMode(int mode) throws RemoteException
    {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.setLightMode(mode);
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int setDecodeSearchLimit(int limit) throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.setDecodeSearchLimit(limit);
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int setDecodeAttemptLimit(int limit) throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.setDecodeAttemptLimit(limit);
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public byte getBarcodeCodeID() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getBarcodeCodeID();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public byte getBarcodeAimID() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getBarcodeAimID();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public byte getBarcodeAimModifier() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getBarcodeAimModifier();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int getBarcodeLength() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getBarcodeLength();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public String getBarcodeData() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getBarcodeData();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return null;
    }

    public int getLastDecodeTime() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getLastDecodeTime();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }
  }

  class Symbology
  {
    Symbology()
    {
    }

    public int enableSymbology(int symbologyID)
      throws RemoteException
    {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.enableSymbology(symbologyID);
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int disableSymbology(int symbologyID) throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.disableSymbology(symbologyID);
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int setSymbologyDefaults(int symbologyID) throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.setSymbologyDefaults(symbologyID);
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int getSymbologyMinRange(int symbologyID) throws RemoteException {
      return DecodeManager.this.mDecoderService.getSymbologyMinRange(symbologyID);
    }

    public int getSymbologyMaxRange(int symbologyID) throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getSymbologyMaxRange(symbologyID);
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int getImagerProperties(ImagerProperties imgProp) throws RemoteException
    {
      if (DecodeManager.this.mDecoderService != null)
        DecodeManager.this.mDecoderService.getImagerProperties(imgProp);
      else {
        Log.i("DecodeManager", "=========Error:mDecoderService is null=============");
      }
      return 0;
    }

    public int setOCRTemplates(int templates) throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.setOCRTemplates(templates);
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int setOCRUserTemplate(int mode, byte[] template) throws RemoteException
    {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.setOCRUserTemplate(mode, template);
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int getSymbologyConfig(SymbologyConfig symConfig, boolean defaultValues)
      throws RemoteException
    {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getSymbologyConfig(symConfig, defaultValues);
      }

      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int setSymbologyConfig(SymbologyConfig symConfg) throws RemoteException
    {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.setSymbologyConfig(symConfg);
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int setSymbologyConfigs(SymbologyConfigs symConfg) throws RemoteException
    {
      Log.i("DecodeManager", "=========setSymbologyConfigs [symConfig.size = " + symConfg.symConfigArrayList.size() + "]=============");

      return DecodeManager.this.mDecoderService.setSymbologyConfigs(symConfg);
    }
  }

  private class Device
  {
    private boolean mVibrate;
    private Vibrator mVibrator;

    public Device()
    {
    }

    public void release()
    {
    }

    public void playSound(int id, int speed)
    {
    }

    public void vibrate(long milliseconds)
    {
      if (this.mVibrate)
        this.mVibrator.vibrate(milliseconds);
    }

    public void vibrate(long[] pattern, int repeat) {
      if (this.mVibrate)
        this.mVibrator.vibrate(pattern, repeat);
    }

    public String getErrorMessage(int error)
      throws RemoteException
    {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getErrorMessage(error);
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return null;
    }

    public byte getBarCodeID() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getBarcodeCodeID();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int getBarcodeLength() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getBarcodeLength();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int getMaxMessageSize() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getMaxMessageLength();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int getEngineID() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getEngineID();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int getEngineType() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getEngineType();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public String getEngineSerialNumber() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getEngineSerialNumber();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return null;
    }

    public String getAPIRevision() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getAPIRevision();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return null;
    }

    public String getDecoderRevision() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getDecoderRevision();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return null;
    }

    public String SecondaryDecoderRevision() throws RemoteException
    {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getSecondaryDecoderRevision();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return null;
    }

    public String getControlLogicRevision() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getControlLogicRevision();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return null;
    }

    public String getDecThreadsRevision() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getDecThreadsRevision();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return null;
    }

    public String getScanDriverRevision() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getScanDriverRevision();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return null;
    }

    public int getPSOCMajorRev() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getPSOCMajorRev();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }

    public int getPSOCMinorRev() throws RemoteException {
      if (DecodeManager.this.mDecoderService != null) {
        return DecodeManager.this.mDecoderService.getPSOCMinorRev();
      }
      Log.i("DecodeManager", "=========Error:mDecoderService is null=============");

      return -1;
    }
  }

  public class SymConfigActivityOpeartor
  {
    private boolean mbModify = false;

    public SymConfigActivityOpeartor()
    {
    }

    public boolean addSymToConfigActivity(int symbologyId)
    {
      boolean b = ConfigDoc.getSingle().addSymToConfig(symbologyId);
      if (b) {
        this.mbModify = true;
        ConfigDoc.getSingle().SaveConfigure();
        return true;
      }
      return false;
    }

    public boolean removeSymFromConfigActivity(int symbologyId)
    {
      boolean b = ConfigDoc.getSingle().removeSymFromConfig(symbologyId);
      if (b) {
        this.mbModify = true;
        ConfigDoc.getSingle().SaveConfigure();
        return true;
      }
      return false;
    }

    public boolean removeAllSymFromConfigActivity()
    {
      boolean b = ConfigDoc.getSingle().removeAllSymFromConfig();
      if (b) {
        this.mbModify = true;
        ConfigDoc.getSingle().SaveConfigure();
        return true;
      }
      return false;
    }

    public boolean restoreDefaultSymToConfigActivity()
    {
      boolean b = ConfigDoc.getSingle().restoreDefaultSymToConfig();
      if (b) {
        this.mbModify = true;
        ConfigDoc.getSingle().SaveConfigure();
        return true;
      }
      return false;
    }

    public ArrayList<Integer> getAllSymbologyId()
    {
      return ConfigDoc.getSingle().getAllSymbologyId();
    }

    public boolean IsExistedInDefaultActivity(int symbologyId)
    {
      return ConfigDoc.getSingle().getAllSymbologyId().contains(Integer.valueOf(symbologyId));
    }

    public void start()
    {
      if (DecodeManager.this.m_Context == null) {
        Log.e("DecodeManager", "Fail: Context is null!");
        return;
      }
      Intent settingsActivity = new Intent("android.intent.action.STARTSYMBOLOGYSETTING");

      settingsActivity.putExtra("ConfigureFileName", DecodeManager.this.m_Context.getPackageName());

      settingsActivity.putExtra("MODIFY", this.mbModify);
      settingsActivity.putExtra("processname", getProcessNameByid(Process.myPid()));

      settingsActivity.putIntegerArrayListExtra("SYMID", getAllSymbologyId());

      this.mbModify = false;

      DecodeManager.this.m_Context.startActivity(settingsActivity);
    }

    private String getProcessNameByid(int pid) {
      String ret = null;
      ActivityManager am = (ActivityManager)DecodeManager.this.m_Context.getSystemService("activity");

      List<RunningAppProcessInfo> shareStublist = am.getRunningAppProcesses();

      for (ActivityManager.RunningAppProcessInfo processinfo : shareStublist) {
        if (processinfo.pid == pid) {
          ret = processinfo.processName;
          break;
        }
      }
      return ret;
    }
  }
}