package com.honeywell.decodeconfigcommon;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import com.honeywell.decodemanager.barcode.DecodeResult;

public abstract interface IDecodeComplete extends IInterface
{
  public abstract void onDecodeComplete(int paramInt, DecodeResult paramDecodeResult)
    throws RemoteException;

  public static abstract class Stub extends Binder
    implements IDecodeComplete
  {
    private static final String DESCRIPTOR = "com.honeywell.decoderservice.IDecodeComplete";
    static final int TRANSACTION_onDecodeComplete = 1;

    public Stub()
    {
      attachInterface(this, "com.honeywell.decoderservice.IDecodeComplete");
    }

    public static IDecodeComplete asInterface(IBinder obj)
    {
      if (obj == null) {
        return null;
      }
      IInterface iin = obj.queryLocalInterface("com.honeywell.decoderservice.IDecodeComplete");
      if ((iin != null) && ((iin instanceof IDecodeComplete))) {
        return (IDecodeComplete)iin;
      }
      return new Proxy(obj);
    }

    public IBinder asBinder() {
      return this;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
      switch (code)
      {
      case 1598968902:
        reply.writeString("com.honeywell.decoderservice.IDecodeComplete");
        return true;
      case 1:
        data.enforceInterface("com.honeywell.decoderservice.IDecodeComplete");

        int _arg0 = data.readInt();
        DecodeResult _arg1;
        if (0 != data.readInt()) {
          _arg1 = (DecodeResult)DecodeResult.CREATOR.createFromParcel(data);
        }
        else {
          _arg1 = null;
        }
        onDecodeComplete(_arg0, _arg1);
        reply.writeNoException();
        return true;
      }

      return super.onTransact(code, data, reply, flags);
    }

    private static class Proxy implements IDecodeComplete {
      private IBinder mRemote;

      Proxy(IBinder remote) {
        this.mRemote = remote;
      }

      public IBinder asBinder() {
        return this.mRemote;
      }

      public String getInterfaceDescriptor() {
        return "com.honeywell.decoderservice.IDecodeComplete";
      }

      public void onDecodeComplete(int error_code, DecodeResult decode_result) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
          _data.writeInterfaceToken("com.honeywell.decoderservice.IDecodeComplete");
          _data.writeInt(error_code);
          if (decode_result != null) {
            _data.writeInt(1);
            decode_result.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          this.mRemote.transact(1, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
  }
}