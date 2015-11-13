package com.honeywell.decodemanager.barcode;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class IQBitmapParcel
  implements Parcelable
{
  private Bitmap mBitmap = null;

  public static final Parcelable.Creator<IQBitmapParcel> CREATOR = new Parcelable.Creator()
  {
    public IQBitmapParcel createFromParcel(Parcel source) {
      return new IQBitmapParcel(source);
    }

    public IQBitmapParcel[] newArray(int size)
    {
      return new IQBitmapParcel[size];
    }
  };

  public IQBitmapParcel(Bitmap mBitmap)
  {
    this.mBitmap = mBitmap;
  }

  public IQBitmapParcel(Parcel in) {
    this.mBitmap = ((Bitmap)Bitmap.CREATOR.createFromParcel(in));
  }

  public void readFromParcel(Parcel in)
  {
    this.mBitmap.recycle();
    this.mBitmap = null;
    this.mBitmap = ((Bitmap)Bitmap.CREATOR.createFromParcel(in));
  }

  public int describeContents()
  {
    return 0;
  }

  public void writeToParcel(Parcel parcel, int flags)
  {
    if (this.mBitmap != null)
      this.mBitmap.writeToParcel(parcel, flags);
  }

  public Bitmap getBitmap() {
    return this.mBitmap;
  }

  public void releaseBitmap() {
    if (!this.mBitmap.isRecycled()) {
      this.mBitmap.recycle();
      this.mBitmap = null;
    }
  }
}