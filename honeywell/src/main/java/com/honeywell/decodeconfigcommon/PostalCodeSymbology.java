package com.honeywell.decodeconfigcommon;

import java.util.ArrayList;

public class PostalCodeSymbology
{
  private ArrayList<Integer> mPostSymbologyIdList;
  private static final int[] PostSymbologyId = { 25, 16, 23, 24, 28, 29, 30, 45, 44, 38, 39 };

  public PostalCodeSymbology()
  {
    this.mPostSymbologyIdList = new ArrayList();

    InitialPostalSymbologyIdList();
  }

  private void InitialPostalSymbologyIdList() {
    this.mPostSymbologyIdList.clear();

    for (int i = 0; i < PostSymbologyId.length; i++)
      this.mPostSymbologyIdList.add(Integer.valueOf(PostSymbologyId[i]));
  }

  public int findPostSymbologyId(int id)
  {
    int postSymbologyid = -1;
    for (int i = 0; i < this.mPostSymbologyIdList.size(); i++) {
      if (((Integer)this.mPostSymbologyIdList.get(i)).intValue() == id) {
        postSymbologyid = ((Integer)this.mPostSymbologyIdList.get(i)).intValue();
        break;
      }
    }
    return postSymbologyid;
  }
}