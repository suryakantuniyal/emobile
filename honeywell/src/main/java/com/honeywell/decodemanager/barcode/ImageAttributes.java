package com.honeywell.decodemanager.barcode;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ImageAttributes
  implements Parcelable
{
  public int ImageSize;
  public int ExposureValue;
  public int GainValue;
  public int IlluminationValue;
  public int IlluminationMaxValue;
  public int IlluminationClipValue;
  public static final Parcelable.Creator<ImageAttributes> CREATOR = new Parcelable.Creator<ImageAttributes>()
  {
    public ImageAttributes createFromParcel(Parcel source)
    {
      return new ImageAttributes(source);
    }

    public ImageAttributes[] newArray(int size)
    {
      return new ImageAttributes[size];
    }
  };

  public ImageAttributes()
  {
  }

  private ImageAttributes(Parcel in)
  {
    readFromParcel(in);
  }

  public void readFromParcel(Parcel in)
  {
    this.ImageSize = in.readInt();
    this.ExposureValue = in.readInt();
    this.GainValue = in.readInt();
    this.IlluminationValue = in.readInt();
    this.IlluminationMaxValue = in.readInt();
    this.IlluminationClipValue = in.readInt();
  }

  public int describeContents()
  {
    return 0;
  }

  public void writeToParcel(Parcel dest, int flags)
  {
    dest.writeInt(this.ImageSize);
    dest.writeInt(this.ExposureValue);
    dest.writeInt(this.GainValue);
    dest.writeInt(this.IlluminationValue);
    dest.writeInt(this.IlluminationMaxValue);
    dest.writeInt(this.IlluminationClipValue);
  }
}