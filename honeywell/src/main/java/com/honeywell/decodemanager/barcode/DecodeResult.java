package com.honeywell.decodemanager.barcode;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class DecodeResult
  implements Parcelable
{
  public String barcodeData;
  public byte codeId;
  public byte aimId;
  public byte aimModifier;
  public int length;
  public static final Parcelable.Creator<DecodeResult> CREATOR = new Parcelable.Creator<DecodeResult>() {
    public DecodeResult createFromParcel(Parcel in) {
      return new DecodeResult(in);
    }
    public DecodeResult[] newArray(int size) {
      return new DecodeResult[size];
    }
  };

  public DecodeResult()
  {
  }

  private DecodeResult(Parcel in)
  {
    readFromParcel(in);
  }

  public void readFromParcel(Parcel in)
  {
    this.barcodeData = in.readString();
    this.codeId = in.readByte();
    this.aimId = in.readByte();
    this.aimModifier = in.readByte();
    this.length = in.readInt();
  }

  public int describeContents()
  {
    return 0;
  }

  public void writeToParcel(Parcel dest, int flags)
  {
    dest.writeString(this.barcodeData);
    dest.writeByte(this.codeId);
    dest.writeByte(this.aimId);
    dest.writeByte(this.aimModifier);
    dest.writeInt(this.length);
  }
}