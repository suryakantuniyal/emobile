package com.honeywell.decodemanager.barcode;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class IQImagingProperties
  implements Parcelable
{
  public int AspectRatio;
  public int X_Offset;
  public int Y_Offset;
  public int Width;
  public int Height;
  public int Resolution;
  public int Format;
  public int Reserved;
  public static final Parcelable.Creator<IQImagingProperties> CREATOR = new Parcelable.Creator<IQImagingProperties>()
  {
    public IQImagingProperties createFromParcel(Parcel source)
    {
      return new IQImagingProperties(source);
    }

    public IQImagingProperties[] newArray(int size)
    {
      return new IQImagingProperties[size];
    }
  };

  public IQImagingProperties()
  {
  }

  public void readFromParcel(Parcel in)
  {
    this.AspectRatio = in.readInt();
    this.X_Offset = in.readInt();
    this.Y_Offset = in.readInt();
    this.Width = in.readInt();
    this.Height = in.readInt();
    this.Resolution = in.readInt();
    this.Format = in.readInt();
    this.Reserved = in.readInt();
  }

  private IQImagingProperties(Parcel in) {
    readFromParcel(in);
  }

  public int describeContents()
  {
    return 0;
  }

  public void writeToParcel(Parcel dest, int flags)
  {
    dest.writeInt(this.AspectRatio);
    dest.writeInt(this.X_Offset);
    dest.writeInt(this.Y_Offset);
    dest.writeInt(this.Width);
    dest.writeInt(this.Height);
    dest.writeInt(this.Resolution);
    dest.writeInt(this.Format);
    dest.writeInt(this.Reserved);
  }
}