package com.honeywell.decodemanager.barcode;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SymbologyConfig
  implements Parcelable
{
  public int symID;
  public int Mask;
  public int Flags;
  public int MinLength;
  public int MaxLength;
  public static final Parcelable.Creator<SymbologyConfig> CREATOR = new Parcelable.Creator<SymbologyConfig>()
  {
    public SymbologyConfig createFromParcel(Parcel in) {
      return new SymbologyConfig(in);
    }

    public SymbologyConfig[] newArray(int size)
    {
      return new SymbologyConfig[size];
    }
  };

  public SymbologyConfig()
  {
  }

  public SymbologyConfig(int symbologyID)
  {
    this.symID = symbologyID;
  }

  private SymbologyConfig(Parcel in) {
    readFromParcel(in);
  }

  public void readFromParcel(Parcel in)
  {
    this.symID = in.readInt();
    this.Mask = in.readInt();
    this.Flags = in.readInt();
    this.MinLength = in.readInt();
    this.MaxLength = in.readInt();
  }

  public int describeContents()
  {
    return 0;
  }

  public void writeToParcel(Parcel dest, int flags)
  {
    dest.writeInt(this.symID);
    dest.writeInt(this.Mask);
    dest.writeInt(this.Flags);
    dest.writeInt(this.MinLength);
    dest.writeInt(this.MaxLength);
  }
}