package com.honeywell.decodemanager.barcode;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CenteringWindowLimits
  implements Parcelable
{
  public int UpperLeft_X_Min;
  public int UpperLeft_X_Max;
  public int UpperLeft_Y_Min;
  public int UpperLeft_Y_Max;
  public int LowerRight_X_Min;
  public int LowerRight_X_Max;
  public int LowerRight_Y_Min;
  public int LowerRight_Y_Max;
  public static final Parcelable.Creator<CenteringWindowLimits> CREATOR = new Parcelable.Creator<CenteringWindowLimits>()
  {
    public CenteringWindowLimits createFromParcel(Parcel source)
    {
      return new CenteringWindowLimits(source);
    }

    public CenteringWindowLimits[] newArray(int size)
    {
      return new CenteringWindowLimits[size];
    }
  };

  public CenteringWindowLimits()
  {
  }

  private CenteringWindowLimits(Parcel in)
  {
    readFromParcel(in);
  }

  public void readFromParcel(Parcel in)
  {
    this.UpperLeft_X_Min = in.readInt();
    this.UpperLeft_X_Max = in.readInt();

    this.UpperLeft_Y_Min = in.readInt();
    this.UpperLeft_Y_Max = in.readInt();

    this.LowerRight_X_Min = in.readInt();
    this.LowerRight_X_Max = in.readInt();

    this.LowerRight_Y_Min = in.readInt();
    this.LowerRight_Y_Max = in.readInt();
  }

  public int describeContents()
  {
    return 0;
  }

  public void writeToParcel(Parcel dest, int flags)
  {
    dest.writeInt(this.UpperLeft_X_Min);
    dest.writeInt(this.UpperLeft_X_Max);

    dest.writeInt(this.UpperLeft_Y_Min);
    dest.writeInt(this.UpperLeft_Y_Max);

    dest.writeInt(this.LowerRight_X_Min);
    dest.writeInt(this.LowerRight_X_Max);

    dest.writeInt(this.LowerRight_Y_Min);
    dest.writeInt(this.LowerRight_Y_Max);
  }
}