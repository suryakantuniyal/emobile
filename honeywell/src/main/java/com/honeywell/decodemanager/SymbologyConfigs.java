package com.honeywell.decodemanager;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

public class SymbologyConfigs
  implements Parcelable
{
  public ArrayList<SymbologyConfigBase> symConfigArrayList;
  public static final Parcelable.Creator<SymbologyConfigs> CREATOR = new Parcelable.Creator<SymbologyConfigs>()
  {
    public SymbologyConfigs createFromParcel(Parcel source)
    {
      return new SymbologyConfigs(source);
    }

    public SymbologyConfigs[] newArray(int size)
    {
      return new SymbologyConfigs[size];
    }
  };

  public SymbologyConfigs()
  {
    this.symConfigArrayList = new ArrayList();
  }

  public boolean addSymbologyConfig(SymbologyConfigBase symCnfBase)
  {
    return this.symConfigArrayList.add(symCnfBase);
  }

  private SymbologyConfigs(Parcel in) {
    readFromParcel(in);
  }

  public void readFromParcel(Parcel in)
  {
    this.symConfigArrayList = new ArrayList();
    in.readList(this.symConfigArrayList, getClass().getClassLoader());
  }

  public int describeContents()
  {
    return 0;
  }

  public void writeToParcel(Parcel dest, int flags)
  {
    dest.writeList(this.symConfigArrayList);
  }
}