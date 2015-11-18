package com.honeywell.decodemanager.barcode;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CenteringWindow
  implements Parcelable
{
  public int UpperLeftX;
  public int UpperLeftY;
  public int LowerRightX;
  public int LowerRightY;
  public static final Parcelable.Creator<CenteringWindow> CREATOR = new Parcelable.Creator<CenteringWindow>()
  {
    public CenteringWindow createFromParcel(Parcel source)
    {
      return new CenteringWindow(source);
    }

    public CenteringWindow[] newArray(int size)
    {
      return new CenteringWindow[size];
    }
  };

  public CenteringWindow()
  {
  }

  private CenteringWindow(Parcel in)
  {
    readFromParcel(in);
  }

  private void readFromParcel(Parcel in)
  {
    this.UpperLeftX = in.readInt();
    this.UpperLeftY = in.readInt();

    this.LowerRightX = in.readInt();
    this.LowerRightY = in.readInt();
  }

  public int describeContents()
  {
    return 0;
  }

  public void writeToParcel(Parcel dest, int flags)
  {
    dest.writeInt(this.UpperLeftX);
    dest.writeInt(this.UpperLeftY);

    dest.writeInt(this.LowerRightX);
    dest.writeInt(this.LowerRightY);
  }
}