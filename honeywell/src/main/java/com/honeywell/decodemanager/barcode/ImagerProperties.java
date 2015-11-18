package com.honeywell.decodemanager.barcode;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class ImagerProperties
  implements Parcelable
{
  public int Size;
  public int EngineID;
  public int Rows;
  public int Columns;
  public int BitsPerPixel;
  public int Rotation;
  public int AimerXoffset;
  public int AimerYoffset;
  public int YDepth;
  public int ColorFormat;
  public int NumBuffers;
  public int PSOCMajorRev;
  public int PSOCMinorRev;
  public String EngineSerialNum;
  public int FirmwareEngineID;
  public int AimerType;
  public int AimerColor;
  public int IllumColor;
  public int Optics;
  public String EnginePartNum;
  public static final Parcelable.Creator<ImagerProperties> CREATOR = new Parcelable.Creator<ImagerProperties>()
  {
    public ImagerProperties createFromParcel(Parcel source)
    {
      return new ImagerProperties(source);
    }

    public ImagerProperties[] newArray(int size)
    {
      return new ImagerProperties[size];
    }
  };

  public ImagerProperties()
  {
  }

  private ImagerProperties(Parcel in)
  {
    readFromParcel(in);
  }

  public int describeContents()
  {
    return 0;
  }

  public void writeToParcel(Parcel dest, int flags)
  {
    dest.writeInt(this.Size);
    dest.writeInt(this.EngineID);
    dest.writeInt(this.Rows);
    dest.writeInt(this.Columns);
    dest.writeInt(this.BitsPerPixel);
    dest.writeInt(this.Rotation);
    dest.writeInt(this.AimerXoffset);
    dest.writeInt(this.AimerYoffset);
    dest.writeInt(this.YDepth);

    dest.writeInt(this.ColorFormat);
    dest.writeInt(this.NumBuffers);
    dest.writeInt(this.PSOCMajorRev);
    dest.writeInt(this.PSOCMinorRev);
    dest.writeString(this.EngineSerialNum);
    dest.writeInt(this.FirmwareEngineID);
    dest.writeInt(this.AimerType);
    dest.writeInt(this.AimerColor);
    dest.writeInt(this.IllumColor);
    dest.writeInt(this.Optics);
    dest.writeString(this.EnginePartNum);
  }

  public void readFromParcel(Parcel in)
  {
    this.Size = in.readInt();
    this.EngineID = in.readInt();
    this.Rows = in.readInt();
    this.Columns = in.readInt();
    this.BitsPerPixel = in.readInt();
    this.Rotation = in.readInt();
    this.AimerXoffset = in.readInt();
    this.AimerYoffset = in.readInt();
    this.YDepth = in.readInt();
    this.ColorFormat = in.readInt();
    this.NumBuffers = in.readInt();
    this.PSOCMajorRev = in.readInt();
    this.PSOCMinorRev = in.readInt();
    this.EngineSerialNum = in.readString();
    this.FirmwareEngineID = in.readInt();
    this.AimerType = in.readInt();
    this.AimerColor = in.readInt();
    this.IllumColor = in.readInt();
    this.Optics = in.readInt();
    this.EnginePartNum = in.readString();
  }
}