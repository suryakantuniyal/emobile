package com.honeywell.decodemanager.barcode;

import android.graphics.Bitmap;
import android.util.Log;

public class Decoder
{
  private static final String TAG = "Decoder.java";
  private int mMayContinueDoDecode = 4;
  private boolean mayContinue = true;

  private native int Connect();

  private native int Disconnect();

  private native String GetErrorMessage(int paramInt);

  private native int GetMaxMessageLength();

  private native int GetEngineID();

  private native int GetEngineType();

  private native String GetEngineSerialNumber();

  private native String GetAPIRevision();

  private native String GetDecoderRevision();

  private native String GetSecondaryDecoderRevision();

  private native String GetControlLogicRevision();

  private native String GetDecThreadsRevision();

  private native String GetScanDriverRevision();

  private native void GetImagerProperties(ImagerProperties paramImagerProperties);

  private native byte[] GetLastImage(ImageAttributes paramImageAttributes);

  private native int GetCenteringWindowLimits(CenteringWindowLimits paramCenteringWindowLimits);

  private native int SetDecodeCenteringWindow(CenteringWindow paramCenteringWindow);

  private native int EnableDecodeCenteringWindow(boolean paramBoolean);

  private native int EnableSymbology(int paramInt);

  private native int DisableSymbology(int paramInt);

  private native int SetSymbologyDefaults(int paramInt);

  private native int GetSymbologyConfig(SymbologyConfig paramSymbologyConfig, boolean paramBoolean);

  private native int SetSymbologyConfig(SymbologyConfig paramSymbologyConfig);

  private native int GetSymbologyMinRange(int paramInt);

  private native int GetSymbologyMaxRange(int paramInt);

  private native int GetPSOCMajorRev();

  private native int GetPSOCMinorRev();

  private native int SetLightsMode(int paramInt);

  private native int SetScanMode(int paramInt);

  private native int SetExposureMode(int paramInt);

  private native int SetDecodeSearchLimit(int paramInt);

  private native int SetDecodeAttemptLimit(int paramInt);

  private native int SetOCRTemplates(int paramInt);

  private native int SetOCRUserTemplate(int paramInt, byte[] paramArrayOfByte);

  private native int SetExposureSettings(int[] paramArrayOfInt);

  private native int WaitForDecode(int paramInt);

  private native byte GetBarcodeCodeID();

  private native byte GetBarcodeAimID();

  private native byte GetBarcodeAimModifier();

  private native int GetBarcodeLength();

  public native String GetBarcodeData();

  private native int GetLastDecodeTime();

  private native int GetIQImage(IQImagingProperties paramIQImagingProperties, Bitmap paramBitmap);

  private native int WaitForDecodeTwo(int paramInt, DecodeResult paramDecodeResult);

  private native int DecodeImage(byte[] paramArrayOfByte, DecodeResult paramDecodeResult, int paramInt1, int paramInt2);

  private native int GetSingleFrame(Bitmap paramBitmap);

  private native int GetPreviewFrame(Bitmap paramBitmap);

  private native int GetImageWidth();

  private native int GetImageHeight();

  private native int StartScanning();

  private native int StopScanning();

  private native byte[] GetBarcodeByteData();

  private native int SetProperty(int paramInt1, int paramInt2);

  public int connectToDecoder()
  {
    Log.d("Decoder.java", "========connectToDecoder========");
    return Connect();
  }

  public int disconnectFromDecoder() {
    Log.d("Decoder.java", "========disconnectFromDecoder========");
    return Disconnect();
  }

  public String getErrorMessage(int error) {
    Log.d("Decoder.java", "========getErrorMEssage {error_code = " + error + "}========");
    return GetErrorMessage(error);
  }

  public int getMaxMessageLength() {
    Log.d("Decoder.java", "========getMaxMessageLength========");
    return GetMaxMessageLength();
  }

  public int getEngineID() {
    Log.d("Decoder.java", "========getEngineID========");
    return GetEngineID();
  }

  public int getEngineType() {
    Log.d("Decoder.java", "========getEngineType========");
    return GetEngineType();
  }

  public String getEngineSerialNumber() {
    Log.d("Decoder.java", "========getEngineSerialNumber========");
    return GetEngineSerialNumber();
  }

  public String getAPIRevision() {
    Log.d("Decoder.java", "========getAPIRevision========");
    return GetAPIRevision();
  }

  public String getDecoderRevision() {
    Log.d("Decoder.java", "========getDecoderRevision========");
    return GetDecoderRevision();
  }

  public String getSecondaryDecoderRevision() {
    Log.d("Decoder.java", "========getSecondaryDecoderRevision========");
    return GetSecondaryDecoderRevision();
  }

  public String getControlLogicRevision() {
    Log.d("Decoder.java", "========getControlLogicRevision========");
    return GetControlLogicRevision();
  }

  public String getDecThreadsRevision() {
    Log.d("Decoder.java", "========getDecThreadsRevision========");
    return GetDecThreadsRevision();
  }

  public String getScanDriverRevision() {
    Log.d("Decoder.java", "========getScanDriverRevision========");
    return GetScanDriverRevision();
  }

  public int enableSymbology(int symbologyID) {
    Log.d("Decoder.java", "========enableSymbology {symbology ID = " + symbologyID + "}========");
    return EnableSymbology(symbologyID);
  }

  public int disableSymbology(int symbologyID) {
    Log.d("Decoder.java", "========disableSymbology {symbology id = " + symbologyID + "}========");
    return DisableSymbology(symbologyID);
  }

  public int setSymbologyDefaults(int symbologyID) {
    Log.d("Decoder.java", "========setSymbologyDefaults {Symbology ID = " + symbologyID + "}========");
    return DisableSymbology(symbologyID);
  }

  public int getSymbologyConfig(SymbologyConfig symConfig, boolean DefaultValues) {
    Log.d("Decoder.java", "========getSymbologyConfig ========");
    return GetSymbologyConfig(symConfig, DefaultValues);
  }

  public int setSymbologyConfig(SymbologyConfig symConfig) {
    Log.d("Decoder.java", "========setSymbologyConfig========");
    return SetSymbologyConfig(symConfig);
  }

  public int getSymbologyMinRange(int symbologyID) {
    Log.d("Decoder.java", "========getSymbologyMinRange {symbologyID = " + symbologyID + "}========");
    return GetSymbologyMinRange(symbologyID);
  }

  public int getSymbologyMaxRange(int symbologyID) {
    Log.d("Decoder.java", "========GetSymbologyMinRange {symbologyID = " + symbologyID + "}========");
    return GetSymbologyMaxRange(symbologyID);
  }

  public int getPSOCMajorRev() {
    Log.d("Decoder.java", "========getPSOCMajorRev========");
    return GetPSOCMajorRev();
  }

  public int getPSOCMinorRev() {
    Log.d("Decoder.java", "========getPSOCMinorRev========");
    return GetPSOCMinorRev();
  }

  public void getImagerProperties(ImagerProperties imgProp) {
    Log.d("Decoder.java", "========getImagerProperties========");
    GetImagerProperties(imgProp);
  }

  public byte[] getLastImage(ImageAttributes imgAtt)
  {
    Log.d("Decoder.java", "========getLastImage========");
    return GetLastImage(imgAtt);
  }

  public int decodeImage(byte[] image, DecodeResult result, int width, int height) {
    return DecodeImage(image, result, width, height);
  }

  public int setLightsMode(int Mode) {
    Log.d("Decoder.java", "========setLightsMode {Mode = " + Mode + "}========");
    return SetLightsMode(Mode);
  }

  public int setScanMode(int Mode) {
    Log.d("Decoder.java", "========setScanMode {Mode = " + Mode + "}========");
    return SetScanMode(Mode);
  }

  public int setExposureMode(int Mode) {
    Log.d("Decoder.java", "========setExposureMode {Mode = " + Mode + "}========");
    return SetExposureMode(Mode);
  }

  public int setDecodeSearchLimit(int limit) {
    Log.d("Decoder.java", "========setDecodeSearchLimit {limit = " + limit + "}========");
    return SetDecodeSearchLimit(limit);
  }

  public int setDecodeAttemptLimit(int limit) {
    Log.d("Decoder.java", "========setDecodeAttemptLimit {limit = " + limit + "}========");
    return SetDecodeAttemptLimit(limit);
  }

  public int getCenteringWindowLimits(CenteringWindowLimits limits) {
    Log.d("Decoder.java", "========GetCenteringWindowLimits========");
    return GetCenteringWindowLimits(limits);
  }

  public int setDecodeCenteringWindow(CenteringWindow window) {
    Log.d("Decoder.java", "========setDecodeCenteringWindow========");
    return SetDecodeCenteringWindow(window);
  }

  public int enableDecodeCenteringWindow(boolean enable) {
    Log.d("Decoder.java", "========EnableDecodeCenteringWindow {enable = " + enable + "}========");
    return EnableDecodeCenteringWindow(enable);
  }

  public int setOCRTemplates(int templates) {
    Log.d("Decoder.java", "=========setOCRTemplates {templates = " + templates + "}==========");
    return SetOCRTemplates(templates);
  }

  public int setOCRUserTemplate(int mode, byte[] template) {
    Log.d("Decoder.java", "=========setOCRUserTemplate {mode = " + mode + ", template = " + template + "}=========");
    return SetOCRUserTemplate(mode, template);
  }

  public int waitForDecode(int timeOut) {
    Log.d("Decoder.java", "=========waitForDecode in {timeout = " + timeOut + "}=============");
    Log.d("Decoder.java", "=========set mMayContinueDoDecode = 4 =============");
    this.mMayContinueDoDecode = 4;
    this.mayContinue = true;
    Log.d("Decoder.java", "=========waitForDecode out =============");
    return WaitForDecode(timeOut);
  }

  public byte getBarcodeCodeID() {
    Log.d("Decoder.java", "========getBarcodeCodeID========");
    return GetBarcodeCodeID();
  }

  public byte getBarcodeAimID() {
    Log.d("Decoder.java", "========getBarcodeAimID========");
    return GetBarcodeAimID();
  }

  public byte getBarcodeAimModifier() {
    Log.d("Decoder.java", "========getBarcodeAimModifier========");
    return GetBarcodeAimModifier();
  }

  public int getBarcodeLength() {
    Log.d("Decoder.java", "========getBarcodeLength========");
    return GetBarcodeLength();
  }

  public String getBarcodeData() {
    Log.d("Decoder.java", "========getBarcodeData========");
    return GetBarcodeData();
  }

  public int getLastDecodeTime() {
    Log.d("Decoder.java", "========getLastDecodeTime========");
    return GetLastDecodeTime();
  }

  public int waitForDecodeTwo(int timeOut, DecodeResult result) {
    Log.d("Decoder.java", "========waitForDecodeTwo========");
    this.mMayContinueDoDecode = 4;
    this.mayContinue = true;
    return WaitForDecodeTwo(timeOut, result);
  }

  public void cancelDecode() {
    Log.d("Decoder.java", "=========cancelDecode in. set mMayContinueDoDecode = 0 =============");
    this.mMayContinueDoDecode = 0;
    this.mayContinue = false;
    Log.d("Decoder.java", "=========cancelDecode out =============");
  }

  public int getIQImage(IQImagingProperties propery, Bitmap iqbmp) {
    Log.d("Decoder.java", "=========getIQImage=========");
    return GetIQImage(propery, iqbmp);
  }

  public int getSingleFrame(Bitmap frame)
  {
    Log.d("Decoder.java", "=========getSingleFrame=========");
    return GetSingleFrame(frame);
  }

  public int getPreviewFrame(Bitmap frame)
  {
    return GetPreviewFrame(frame);
  }

  public int getImageWidth()
  {
    Log.d("Decoder.java", "=========getImageWidth=========");
    return GetImageWidth();
  }

  public int getImageHeight()
  {
    Log.d("Decoder.java", "=========getImageHeight=========");
    return GetImageHeight();
  }

  public int startScanning()
  {
    Log.d("Decoder.java", "=========startScanning=========");
    return StartScanning();
  }

  public int stopScanning()
  {
    Log.d("Decoder.java", "=========stopScanning=========");
    return StopScanning();
  }

  private int KeepGoing() {
    Log.d("Decoder.java", "=========KeepGoing=========");
    return 1;
  }

  public int setExposureSettings(int[] expSettings) {
    Log.d("Decoder.java", "=========SetExposureSettings=========");
    return SetExposureSettings(expSettings);
  }

  public byte[] getBarcodeByteData() {
    Log.d("Decoder.java", "=========getBarcodeByteData=========");
    return GetBarcodeByteData();
  }

  public int setProperty(int property, int value) {
    Log.d("Decoder.java", "=========getBarcodeByteData=========");
    return SetProperty(property, value);
  }

  static
  {
    try
    {
      System.loadLibrary("Decoder");
      Log.d("Decoder.java", "Decoder.so loaded");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}