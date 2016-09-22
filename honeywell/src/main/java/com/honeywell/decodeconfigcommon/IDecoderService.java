package com.honeywell.decodeconfigcommon;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import com.honeywell.decodemanager.SymbologyConfigs;
import com.honeywell.decodemanager.barcode.BitmapParcel;
import com.honeywell.decodemanager.barcode.DecodeResult;
import com.honeywell.decodemanager.barcode.IQBitmapParcel;
import com.honeywell.decodemanager.barcode.IQImagingProperties;
import com.honeywell.decodemanager.barcode.ImageAttributes;
import com.honeywell.decodemanager.barcode.ImagerProperties;
import com.honeywell.decodemanager.barcode.SymbologyConfig;

public abstract interface IDecoderService extends IInterface
{
  public abstract String getErrorMessage(int paramInt)
    throws RemoteException;

  public abstract int getMaxMessageLength()
    throws RemoteException;

  public abstract int getEngineID()
    throws RemoteException;

  public abstract int getEngineType()
    throws RemoteException;

  public abstract String getEngineSerialNumber()
    throws RemoteException;

  public abstract String getAPIRevision()
    throws RemoteException;

  public abstract String getDecoderRevision()
    throws RemoteException;

  public abstract String getSecondaryDecoderRevision()
    throws RemoteException;

  public abstract String getControlLogicRevision()
    throws RemoteException;

  public abstract String getDecThreadsRevision()
    throws RemoteException;

  public abstract String getScanDriverRevision()
    throws RemoteException;

  public abstract int getPSOCMajorRev()
    throws RemoteException;

  public abstract int getPSOCMinorRev()
    throws RemoteException;

  public abstract int enableSymbology(int paramInt)
    throws RemoteException;

  public abstract int disableSymbology(int paramInt)
    throws RemoteException;

  public abstract int setSymbologyDefaults(int paramInt)
    throws RemoteException;

  public abstract int getSymbologyMinRange(int paramInt)
    throws RemoteException;

  public abstract int getSymbologyMaxRange(int paramInt)
    throws RemoteException;

  public abstract void getImagerProperties(ImagerProperties paramImagerProperties)
    throws RemoteException;

  public abstract byte[] getLastImage(ImageAttributes paramImageAttributes)
    throws RemoteException;

  public abstract int decodeImage(byte[] paramArrayOfByte, DecodeResult paramDecodeResult, int paramInt1, int paramInt2)
    throws RemoteException;

  public abstract int setOCRTemplates(int paramInt)
    throws RemoteException;

  public abstract int setOCRUserTemplate(int paramInt, byte[] paramArrayOfByte)
    throws RemoteException;

  public abstract int getSymbologyConfig(SymbologyConfig paramSymbologyConfig, boolean paramBoolean)
    throws RemoteException;

  public abstract int setSymbologyConfig(SymbologyConfig paramSymbologyConfig)
    throws RemoteException;

  public abstract int setSymbologyConfigs(SymbologyConfigs paramSymbologyConfigs)
    throws RemoteException;

  public abstract int setSymbologyConfigWithProcess(String paramString, SymbologyConfig paramSymbologyConfig)
    throws RemoteException;

  public abstract void doDecode(IDecodeComplete paramIDecodeComplete, int paramInt)
    throws RemoteException;

  public abstract void cancelDecode(IDecodeComplete paramIDecodeComplete)
    throws RemoteException;

  public abstract int setLightMode(int paramInt)
    throws RemoteException;

  public abstract int setScanMode(int paramInt)
    throws RemoteException;

  public abstract int setExposureMode(int paramInt)
    throws RemoteException;

  public abstract int setDecodeSearchLimit(int paramInt)
    throws RemoteException;

  public abstract int setDecodeAttemptLimit(int paramInt)
    throws RemoteException;

  public abstract int setExposureSettings(int[] paramArrayOfInt)
    throws RemoteException;

  public abstract byte getBarcodeCodeID()
    throws RemoteException;

  public abstract byte getBarcodeAimID()
    throws RemoteException;

  public abstract byte getBarcodeAimModifier()
    throws RemoteException;

  public abstract int getBarcodeLength()
    throws RemoteException;

  public abstract String getBarcodeData()
    throws RemoteException;

  public abstract int getLastDecodeTime()
    throws RemoteException;

  public abstract int getIQImage(IQImagingProperties paramIQImagingProperties, IQBitmapParcel paramIQBitmapParcel)
    throws RemoteException;

  public abstract void doImageRequest(IDecodeComplete paramIDecodeComplete, int paramInt)
    throws RemoteException;

  public abstract int doGetImage(BitmapParcel paramBitmapParcel, int paramInt)
    throws RemoteException;

  public abstract int getImageWidth()
    throws RemoteException;

  public abstract int getImageHeight()
    throws RemoteException;

  public abstract int startScanning()
    throws RemoteException;

  public abstract int stopScanning()
    throws RemoteException;

  public abstract byte[] getBarcodeByteData()
    throws RemoteException;

  public abstract int setProperty(int paramInt1, int paramInt2)
    throws RemoteException;

  public static abstract class Stub extends Binder
    implements IDecoderService
  {
    private static final String DESCRIPTOR = "com.honeywell.decoderservice.IDecoderService";
    static final int TRANSACTION_getErrorMessage = 1;
    static final int TRANSACTION_getMaxMessageLength = 2;
    static final int TRANSACTION_getEngineID = 3;
    static final int TRANSACTION_getEngineType = 4;
    static final int TRANSACTION_getEngineSerialNumber = 5;
    static final int TRANSACTION_getAPIRevision = 6;
    static final int TRANSACTION_getDecoderRevision = 7;
    static final int TRANSACTION_getSecondaryDecoderRevision = 8;
    static final int TRANSACTION_getControlLogicRevision = 9;
    static final int TRANSACTION_getDecThreadsRevision = 10;
    static final int TRANSACTION_getScanDriverRevision = 11;
    static final int TRANSACTION_getPSOCMajorRev = 12;
    static final int TRANSACTION_getPSOCMinorRev = 13;
    static final int TRANSACTION_enableSymbology = 14;
    static final int TRANSACTION_disableSymbology = 15;
    static final int TRANSACTION_setSymbologyDefaults = 16;
    static final int TRANSACTION_getSymbologyMinRange = 17;
    static final int TRANSACTION_getSymbologyMaxRange = 18;
    static final int TRANSACTION_getImagerProperties = 19;
    static final int TRANSACTION_getLastImage = 20;
    static final int TRANSACTION_decodeImage = 21;
    static final int TRANSACTION_setOCRTemplates = 22;
    static final int TRANSACTION_setOCRUserTemplate = 23;
    static final int TRANSACTION_getSymbologyConfig = 24;
    static final int TRANSACTION_setSymbologyConfig = 25;
    static final int TRANSACTION_setSymbologyConfigs = 26;
    static final int TRANSACTION_setSymbologyConfigWithProcess = 27;
    static final int TRANSACTION_doDecode = 28;
    static final int TRANSACTION_cancelDecode = 29;
    static final int TRANSACTION_setLightMode = 30;
    static final int TRANSACTION_setScanMode = 31;
    static final int TRANSACTION_setExposureMode = 32;
    static final int TRANSACTION_setDecodeSearchLimit = 33;
    static final int TRANSACTION_setDecodeAttemptLimit = 34;
    static final int TRANSACTION_setExposureSettings = 35;
    static final int TRANSACTION_getBarcodeCodeID = 36;
    static final int TRANSACTION_getBarcodeAimID = 37;
    static final int TRANSACTION_getBarcodeAimModifier = 38;
    static final int TRANSACTION_getBarcodeLength = 39;
    static final int TRANSACTION_getBarcodeData = 40;
    static final int TRANSACTION_getLastDecodeTime = 41;
    static final int TRANSACTION_getIQImage = 42;
    static final int TRANSACTION_doImageRequest = 43;
    static final int TRANSACTION_doGetImage = 44;
    static final int TRANSACTION_getImageWidth = 45;
    static final int TRANSACTION_getImageHeight = 46;
    static final int TRANSACTION_startScanning = 47;
    static final int TRANSACTION_stopScanning = 48;
    static final int TRANSACTION_getBarcodeByteData = 49;
    static final int TRANSACTION_setProperty = 50;

    public Stub()
    {
      attachInterface(this, "com.honeywell.decoderservice.IDecoderService");
    }

    public static IDecoderService asInterface(IBinder obj)
    {
      if (obj == null) {
        return null;
      }
      IInterface iin = obj.queryLocalInterface("com.honeywell.decoderservice.IDecoderService");
      if ((iin != null) && ((iin instanceof IDecoderService))) {
        return (IDecoderService)iin;
      }
      return new Proxy(obj);
    }

    public IBinder asBinder() {
      return this;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
     int _result;
     byte _resultByte;
     String _strResult;
    	switch (code)
      {
      case 1598968902:
        reply.writeString("com.honeywell.decoderservice.IDecoderService");
        return true;
      case 1:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg0 = data.readInt();
        _strResult = getErrorMessage(_arg0);
        reply.writeNoException();
        reply.writeString(_strResult);
        return true;
      case 2:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _result = getMaxMessageLength();
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 3:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _result = getEngineID();
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 4:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _result = getEngineType();
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 5:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _strResult = getEngineSerialNumber();
        reply.writeNoException();
        reply.writeString(_strResult);
        return true;
      case 6:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _strResult = getAPIRevision();
        reply.writeNoException();
        reply.writeString(_strResult);
        return true;
      case 7:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _strResult = getDecoderRevision();
        reply.writeNoException();
        reply.writeString(_strResult);
        return true;
      case 8:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _strResult = getSecondaryDecoderRevision();
        reply.writeNoException();
        reply.writeString(_strResult);
        return true;
      case 9:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _strResult = getControlLogicRevision();
        reply.writeNoException();
        reply.writeString(_strResult);
        return true;
      case 10:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _strResult = getDecThreadsRevision();
        reply.writeNoException();
        reply.writeString(_strResult);
        return true;
      case 11:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _strResult = getScanDriverRevision();
        reply.writeNoException();
        reply.writeString(_strResult);
        return true;
      case 12:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _result = getPSOCMajorRev();
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 13:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _result = getPSOCMinorRev();
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 14:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg14 = data.readInt();
        _result = enableSymbology(_arg14);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 15:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg15 = data.readInt();
        _result = disableSymbology(_arg15);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 16:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg16 = data.readInt();
        _result = setSymbologyDefaults(_arg16);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 17:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg17 = data.readInt();
        _result = getSymbologyMinRange(_arg17);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 18:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg18 = data.readInt();
        _result = getSymbologyMaxRange(_arg18);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 19:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        ImagerProperties _arg19 = new ImagerProperties();
        getImagerProperties(_arg19);
        reply.writeNoException();
        if (_arg19 != null) {
          reply.writeInt(1);
          _arg19.writeToParcel(reply, 1);
        }
        else {
          reply.writeInt(0);
        }
        return true;
      case 20:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        ImageAttributes _arg20 = new ImageAttributes();
        byte[] _result20 = getLastImage(_arg20);
        reply.writeNoException();
        reply.writeByteArray(_result20);
        if (_arg20 != null) {
          reply.writeInt(1);
          _arg20.writeToParcel(reply, 1);
        }
        else {
          reply.writeInt(0);
        }
        return true;
      case 21:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        byte[] _arg21 = data.createByteArray();

        DecodeResult _arg211 = new DecodeResult();

        int _arg212 = data.readInt();

        int _arg213 = data.readInt();
        _result = decodeImage(_arg21, _arg211, _arg212, _arg213);
        reply.writeNoException();
        reply.writeInt(_result);
        if (_arg211 != null) {
          reply.writeInt(1);
          _arg211.writeToParcel(reply, 1);
        }
        else {
          reply.writeInt(0);
        }
        return true;
      case 22:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg22 = data.readInt();
        _result = setOCRTemplates(_arg22);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 23:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg23 = data.readInt();

        byte[] _arg231 = data.createByteArray();
        _result = setOCRUserTemplate(_arg23, _arg231);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 24:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        SymbologyConfig _arg24 = new SymbologyConfig();

        boolean _arg241 = 0 != data.readInt();
        _result = getSymbologyConfig(_arg24, _arg241);
        reply.writeNoException();
        reply.writeInt(_result);
        if (_arg24 != null) {
          reply.writeInt(1);
          _arg24.writeToParcel(reply, 1);
        }
        else {
          reply.writeInt(0);
        }
        return true;
      case 25:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        SymbologyConfig _arg25;
        if (0 != data.readInt()) {
          _arg25 = (SymbologyConfig)SymbologyConfig.CREATOR.createFromParcel(data);
        }
        else {
          _arg25 = null;
        }
        _result = setSymbologyConfig(_arg25);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 26:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        SymbologyConfigs _arg26;
        if (0 != data.readInt()) {
          _arg26 = (SymbologyConfigs)SymbologyConfigs.CREATOR.createFromParcel(data);
        }
        else {
          _arg26 = null;
        }
        _result = setSymbologyConfigs(_arg26);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 27:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        String _arg27 = data.readString();
        SymbologyConfig _arg271;
        if (0 != data.readInt()) {
          _arg271 = (SymbologyConfig)SymbologyConfig.CREATOR.createFromParcel(data);
        }
        else {
          _arg271 = null;
        }
        _result = setSymbologyConfigWithProcess(_arg27, _arg271);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 28:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        IDecodeComplete _arg28 = IDecodeComplete.Stub.asInterface(data.readStrongBinder());

        int _arg281 = data.readInt();
        doDecode(_arg28, _arg281);
        reply.writeNoException();
        return true;
      case 29:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        IDecodeComplete _arg29 = IDecodeComplete.Stub.asInterface(data.readStrongBinder());
        cancelDecode(_arg29);
        reply.writeNoException();
        return true;
      case 30:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg30 = data.readInt();
        _result = setLightMode(_arg30);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 31:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg31 = data.readInt();
        _result = setScanMode(_arg31);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 32:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg32 = data.readInt();
        _result = setExposureMode(_arg32);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 33:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg33 = data.readInt();
        _result = setDecodeSearchLimit(_arg33);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 34:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg34 = data.readInt();
        _result = setDecodeAttemptLimit(_arg34);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 35:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int[] _arg35 = data.createIntArray();
        _result = setExposureSettings(_arg35);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 36:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _resultByte = getBarcodeCodeID();
        reply.writeNoException();
        reply.writeByte(_resultByte);
        return true;
      case 37:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _resultByte = getBarcodeAimID();
        reply.writeNoException();
        reply.writeByte(_resultByte);
        return true;
      case 38:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _resultByte = getBarcodeAimModifier();
        reply.writeNoException();
        reply.writeByte(_resultByte);
        return true;
      case 39:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _result = getBarcodeLength();
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 40:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _strResult = getBarcodeData();
        reply.writeNoException();
        reply.writeString(_strResult);
        return true;
      case 41:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _result = getLastDecodeTime();
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 42:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        IQImagingProperties _arg42;
        if (0 != data.readInt()) {
          _arg42 = (IQImagingProperties)IQImagingProperties.CREATOR.createFromParcel(data);
        }
        else
          _arg42 = null;
        IQBitmapParcel _arg421;
        if (0 != data.readInt()) {
          _arg421 = (IQBitmapParcel)IQBitmapParcel.CREATOR.createFromParcel(data);
        }
        else {
          _arg421 = null;
        }
        _result = getIQImage(_arg42, _arg421);
        reply.writeNoException();
        reply.writeInt(_result);
        if (_arg421 != null) {
          reply.writeInt(1);
          _arg421.writeToParcel(reply, 1);
        }
        else {
          reply.writeInt(0);
        }
        return true;
      case 43:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        IDecodeComplete _arg43 = IDecodeComplete.Stub.asInterface(data.readStrongBinder());

        int _arg431 = data.readInt();
        doImageRequest(_arg43, _arg431);
        reply.writeNoException();
        return true;
      case 44:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        BitmapParcel _arg44;
        if (0 != data.readInt()) {
          _arg44 = (BitmapParcel)BitmapParcel.CREATOR.createFromParcel(data);
        }
        else {
          _arg44 = null;
        }

        int _arg441 = data.readInt();
        _result = doGetImage(_arg44, _arg441);
        reply.writeNoException();
        reply.writeInt(_result);
        if (_arg44 != null) {
          reply.writeInt(1);
          _arg44.writeToParcel(reply, 1);
        }
        else {
          reply.writeInt(0);
        }
        return true;
      case 45:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _result = getImageWidth();
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 46:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _result = getImageHeight();
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 47:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _result = startScanning();
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 48:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        _result = stopScanning();
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      case 49:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");
        byte[] _result49 = getBarcodeByteData();
        reply.writeNoException();
        reply.writeByteArray(_result49);
        return true;
      case 50:
        data.enforceInterface("com.honeywell.decoderservice.IDecoderService");

        int _arg50 = data.readInt();

        int _arg501 = data.readInt();
        _result = setProperty(_arg50, _arg501);
        reply.writeNoException();
        reply.writeInt(_result);
        return true;
      }

      return super.onTransact(code, data, reply, flags);
    }

    private static class Proxy implements IDecoderService {
      private IBinder mRemote;

      Proxy(IBinder remote) {
        this.mRemote = remote;
      }

      public IBinder asBinder() {
        return this.mRemote;
      }

      public String getInterfaceDescriptor() {
        return "com.honeywell.decoderservice.IDecoderService";
      }

      public String getErrorMessage(int error) throws RemoteException
      {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        String _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(error);
          this.mRemote.transact(1, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int getMaxMessageLength() throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        
       int  _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(2, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int getEngineID() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(3, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int getEngineType() throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(4, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public String getEngineSerialNumber() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        String _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(5, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public String getAPIRevision() throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        String _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(6, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public String getDecoderRevision() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        String _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(7, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public String getSecondaryDecoderRevision() throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        String _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(8, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public String getControlLogicRevision() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        String _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(9, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public String getDecThreadsRevision() throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        String _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(10, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public String getScanDriverRevision() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        String _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(11, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int getPSOCMajorRev() throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(12, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int getPSOCMinorRev() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(13, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int enableSymbology(int symbologyID) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(symbologyID);
          this.mRemote.transact(14, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int disableSymbology(int symbologyID) throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(symbologyID);
          this.mRemote.transact(15, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int setSymbologyDefaults(int symbologyID) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(symbologyID);
          this.mRemote.transact(16, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int getSymbologyMinRange(int symbologyID) throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(symbologyID);
          this.mRemote.transact(17, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int getSymbologyMaxRange(int symbologyID) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(symbologyID);
          this.mRemote.transact(18, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }

      public void getImagerProperties(ImagerProperties imgProp) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(19, _data, _reply, 0);
          _reply.readException();
          if (0 != _reply.readInt())
            imgProp.readFromParcel(_reply);
        }
        finally
        {
          _reply.recycle();
          _data.recycle();
        }
      }

      public byte[] getLastImage(ImageAttributes imgAtt) throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        byte[] _result;
        try { _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(20, _data, _reply, 0);
          _reply.readException();
          _result = _reply.createByteArray();
          if (0 != _reply.readInt())
            imgAtt.readFromParcel(_reply);
        }
        finally
        {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int decodeImage(byte[] image, DecodeResult result, int width, int height) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeByteArray(image);
          _data.writeInt(width);
          _data.writeInt(height);
          this.mRemote.transact(21, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
          if (0 != _reply.readInt())
            result.readFromParcel(_reply);
        }
        finally
        {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int setOCRTemplates(int templates) throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(templates);
          this.mRemote.transact(22, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int setOCRUserTemplate(int mode, byte[] template) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(mode);
          _data.writeByteArray(template);
          this.mRemote.transact(23, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int getSymbologyConfig(SymbologyConfig symConfig, boolean defaultValues) throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(defaultValues ? 1 : 0);
          this.mRemote.transact(24, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
          if (0 != _reply.readInt())
            symConfig.readFromParcel(_reply);
        }
        finally
        {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int setSymbologyConfig(SymbologyConfig symConfg) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          if (symConfg != null) {
            _data.writeInt(1);
            symConfg.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          this.mRemote.transact(25, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int setSymbologyConfigs(SymbologyConfigs symConfigs) throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          if (symConfigs != null) {
            _data.writeInt(1);
            symConfigs.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          this.mRemote.transact(26, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int setSymbologyConfigWithProcess(String processname, SymbologyConfig symConfg) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeString(processname);
          if (symConfg != null) {
            _data.writeInt(1);
            symConfg.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          this.mRemote.transact(27, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }

      public void doDecode(IDecodeComplete callback, int timeout)
        throws RemoteException
      {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
          _data.writeInt(timeout);
          this.mRemote.transact(28, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }

      public void cancelDecode(IDecodeComplete callback) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
          this.mRemote.transact(29, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }

      public int setLightMode(int mode) throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try { _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(mode);
          this.mRemote.transact(30, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        } finally
        {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int setScanMode(int mode) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(mode);
          this.mRemote.transact(31, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int setExposureMode(int mode) throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(mode);
          this.mRemote.transact(32, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int setDecodeSearchLimit(int limit) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(limit);
          this.mRemote.transact(33, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int setDecodeAttemptLimit(int limit) throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(limit);
          this.mRemote.transact(34, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int setExposureSettings(int[] expSettings) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeIntArray(expSettings);
          this.mRemote.transact(35, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public byte getBarcodeCodeID() throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        byte _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(36, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readByte();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public byte getBarcodeAimID() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        byte _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(37, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readByte();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public byte getBarcodeAimModifier() throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        byte _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(38, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readByte();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int getBarcodeLength() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(39, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public String getBarcodeData() throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        String _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(40, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int getLastDecodeTime() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(41, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }

      public int getIQImage(IQImagingProperties propery, IQBitmapParcel bmpparcel) throws RemoteException
      {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          if (propery != null) {
            _data.writeInt(1);
            propery.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          if (bmpparcel != null) {
            _data.writeInt(1);
            bmpparcel.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          this.mRemote.transact(42, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
          if (0 != _reply.readInt())
            bmpparcel.readFromParcel(_reply);
        }
        finally
        {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }

      public void doImageRequest(IDecodeComplete callback, int imgtype)
        throws RemoteException
      {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
          _data.writeInt(imgtype);
          this.mRemote.transact(43, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }

      public int doGetImage(BitmapParcel bmpparcel, int type) throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try { _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          if (bmpparcel != null) {
            _data.writeInt(1);
            bmpparcel.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeInt(type);
          this.mRemote.transact(44, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
          if (0 != _reply.readInt())
            bmpparcel.readFromParcel(_reply);
        }
        finally
        {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int getImageWidth() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(45, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int getImageHeight() throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(46, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public int startScanning() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(47, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int stopScanning() throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(48, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result; } 
      public byte[] getBarcodeByteData() throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        byte[] _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          this.mRemote.transact(49, _data, _reply, 0);
          _reply.readException();
          _result = _reply.createByteArray();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public int setProperty(int property, int value) throws RemoteException { Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecoderService");
          _data.writeInt(property);
          _data.writeInt(value);
          this.mRemote.transact(50, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
  }
}