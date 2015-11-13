package com.honeywell.decodemanager.barcode;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class BitmapParcel
  implements Parcelable
{
  private Bitmap mBitmap = null;

  public static final Parcelable.Creator<BitmapParcel> CREATOR = new Parcelable.Creator()
  {
    public BitmapParcel createFromParcel(Parcel source) {
      return new BitmapParcel(source);
    }

    public BitmapParcel[] newArray(int size)
    {
      return new BitmapParcel[size];
    }
  };

  public BitmapParcel(Bitmap mBitmap)
  {
    this.mBitmap = mBitmap;
  }

  public BitmapParcel(Parcel in) {
    this.mBitmap = ((Bitmap)Bitmap.CREATOR.createFromParcel(in));
  }

  public void readFromParcel(Parcel in)
  {
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