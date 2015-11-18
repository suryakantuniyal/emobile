package com.honeywell.decodemanager;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SymbologyConfigBase
  implements Parcelable
{
  protected int m_symID;
  protected int m_mask;
  protected int m_flags;
  protected int m_minLength;
  protected int m_maxLength;
  public static final Parcelable.Creator<SymbologyConfigBase> CREATOR = new Parcelable.Creator<SymbologyConfigBase>()
  {
    public SymbologyConfigBase createFromParcel(Parcel source) {
      return new SymbologyConfigBase(source);
    }

    public SymbologyConfigBase[] newArray(int size)
    {
      return new SymbologyConfigBase[size];
    }
  };

  public SymbologyConfigBase()
  {
    this.m_symID = 0;
    this.m_flags = 0;
    this.m_minLength = 0;
    this.m_maxLength = 0;
    this.m_mask = 0;
  }

  private SymbologyConfigBase(Parcel in) {
    readFromParcel(in);
  }

  public void readFromParcel(Parcel in)
  {
    this.m_symID = in.readInt();
    this.m_mask = in.readInt();
    this.m_flags = in.readInt();
    this.m_minLength = in.readInt();
    this.m_maxLength = in.readInt();
  }

  public int describeContents()
  {
    return 0;
  }

  public void writeToParcel(Parcel dest, int flags)
  {
    dest.writeInt(this.m_symID);
    dest.writeInt(this.m_mask);
    dest.writeInt(this.m_flags);
    dest.writeInt(this.m_minLength);
    dest.writeInt(this.m_maxLength);
  }

  public void enableSymbology(boolean b)
  {
    if (b)
      this.m_flags |= 1;
    else
      this.m_flags &= -2;
  }

  protected int getSymID()
  {
    return this.m_symID;
  }

  protected int getFlags()
  {
    return this.m_flags;
  }

  protected int getMask()
  {
    return this.m_mask;
  }

  protected int getMinLength()
  {
    return this.m_minLength;
  }

  protected int getMaxLength()
  {
    return this.m_maxLength;
  }
}