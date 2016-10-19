package com.honeywell.decodeconfigcommon;

import java.util.ArrayList;
import java.util.Map;

class ParseSymbologyProperty
{
  private ArrayList<SymbologyProperty> mPropertyList;
  private ArrayList<SymbologyNameAndId> mSymbologyNameAndId;
  private static final SymbologyProperty[] SymbologyPropertyArray = { new SymbologyProperty(1), new SymbologyProperty(128), new SymbologyProperty(256), new SymbologyProperty(512), new SymbologyProperty(536870912), new SymbologyProperty(1024), new SymbologyProperty(65536), new SymbologyProperty(2), new SymbologyProperty(4), new SymbologyProperty(8192), new SymbologyProperty(16), new SymbologyProperty(32), new SymbologyProperty(2048), new SymbologyProperty(64), new SymbologyProperty(134217728), new SymbologyProperty(268435456), new SymbologyProperty(8388608), new SymbologyProperty(16777216), new SymbologyProperty(33554432), new SymbologyProperty(8), new SymbologyProperty(67108864) };

  private static final SymbologyNameAndId[] SymbologyNameAndIdArray = { new SymbologyNameAndId("Aztec Code", 0), new SymbologyNameAndId("China Post", 38), new SymbologyNameAndId("Codabar", 1), new SymbologyNameAndId("Codablock F", 27), new SymbologyNameAndId("Code 11", 2), new SymbologyNameAndId("Code 128", 3), new SymbologyNameAndId("Code 32 Pharmaceutical (PARAF)", 34), new SymbologyNameAndId("Code 39", 4), new SymbologyNameAndId("Code 93/93i", 6), new SymbologyNameAndId("Composite", 7), new SymbologyNameAndId("EAN-13", 10), new SymbologyNameAndId("EAN-8", 9), new SymbologyNameAndId("EAN.UCC (RSS)", 18), new SymbologyNameAndId("Grid Matrix", 49), new SymbologyNameAndId("GS1_128", 47), new SymbologyNameAndId("Han Xin", 48), new SymbologyNameAndId("Interleaved 2 of 5", 11), new SymbologyNameAndId("ISBT 128", 22), new SymbologyNameAndId("Korea Post", 39), new SymbologyNameAndId("Matrix 2 of 5", 36), new SymbologyNameAndId("MaxiCode", 12), new SymbologyNameAndId("MicroPDF417", 13), new SymbologyNameAndId("MSI", 31), new SymbologyNameAndId("PDF417", 15), new SymbologyNameAndId("QR Code / Micro QR Code", 17), new SymbologyNameAndId("Straight 2 of 5 (IATA)", 26), new SymbologyNameAndId("Straight 2 of 5 (Industrial)", 35), new SymbologyNameAndId("Telepen", 40), new SymbologyNameAndId("TCIF Linked Code 39 (TLC39)", 32), new SymbologyNameAndId("Trioptic Code", 33), new SymbologyNameAndId("UPC-A", 19), new SymbologyNameAndId("UPC-A (Coupon Code)", 43), new SymbologyNameAndId("UPC-E", 20), new SymbologyNameAndId("Data Matrix", 8), new SymbologyNameAndId("Postnet", 16), new SymbologyNameAndId("British Post", 23), new SymbologyNameAndId("Canadian Post", 24), new SymbologyNameAndId("Australian Post", 25), new SymbologyNameAndId("Japanese Post", 28), new SymbologyNameAndId("Planet Code", 29), new SymbologyNameAndId("KIX Code", 30), new SymbologyNameAndId("ID Tag", 45), new SymbologyNameAndId("USPS 4 State", 44) };

  public ParseSymbologyProperty()
  {
    this.mPropertyList = new ArrayList();
    this.mSymbologyNameAndId = new ArrayList();
    InitialPropertyList();
    InitialSymbologyNameAndIdMap();
  }

  private void InitialPropertyList() {
    this.mPropertyList.clear();
    for (int i = 0; i < SymbologyPropertyArray.length; i++)
    {
      this.mPropertyList.add(SymbologyPropertyArray[i]);
    }
  }

  private void InitialSymbologyNameAndIdMap()
  {
    this.mSymbologyNameAndId.clear();

    for (int i = 0; i < SymbologyNameAndIdArray.length; i++)
    {
      this.mSymbologyNameAndId.add(SymbologyNameAndIdArray[i]);
    }
  }

  public ArrayList<SymbologyPropertyStatus> ParseSymbologyPropertyint(int OneSymbologyPropertyList, int OneSymbologyPropertyValue) {
    ArrayList PropertyStrValueList = new ArrayList();

    for (int i = 0; i < this.mPropertyList.size(); i++)
    {
      int pro = ((SymbologyProperty)this.mPropertyList.get(i)).IntProperty;
      if ((pro & OneSymbologyPropertyList) != 0)
      {
        SymbologyPropertyStatus ProValue = new SymbologyPropertyStatus();
        ProValue.PropertyId = ((SymbologyProperty)this.mPropertyList.get(i)).IntProperty;
        ProValue.checked = false;

        PropertyStrValueList.add(ProValue);
      }
    }

    for (int j = 0; j < this.mPropertyList.size(); j++) {
      int property = ((SymbologyProperty)this.mPropertyList.get(j)).IntProperty;
      if ((property & OneSymbologyPropertyValue) != 0)
      {
        for (int m = 0; m < PropertyStrValueList.size(); m++) {
          if (((SymbologyPropertyStatus)PropertyStrValueList.get(m)).PropertyId == ((SymbologyProperty)this.mPropertyList.get(j)).IntProperty) {
            ((SymbologyPropertyStatus)PropertyStrValueList.get(m)).checked = true;
          }
        }
      }
    }

    return PropertyStrValueList;
  }

  public int ParsePropertyFromStrToInt(Map<String, SymbologyCommonProperty> Property)
  {
    int iproperty = 0;

    for (int i = 0; i < Property.size(); i++)
    {
      int proId = ((SymbologyCommonProperty)Property.get(Integer.valueOf(i))).PropertyId;
      boolean checked = ((SymbologyCommonProperty)Property.get(Integer.valueOf(i))).checked;

      if (checked) {
        for (int j = 0; j < this.mPropertyList.size(); j++) {
          int pro = ((SymbologyProperty)this.mPropertyList.get(j)).IntProperty;
          if (pro == proId) {
            iproperty |= ((SymbologyProperty)this.mPropertyList.get(j)).IntProperty;
          }
        }
      }
    }

    return iproperty;
  }

  public String findBarcodeNameFromId(int id) {
    String strBarcodeName = null;
    for (int i = 0; i < this.mSymbologyNameAndId.size(); i++) {
      if (((SymbologyNameAndId)this.mSymbologyNameAndId.get(i)).symbologyId == id) {
        strBarcodeName = ((SymbologyNameAndId)this.mSymbologyNameAndId.get(i)).strSymbologyName;
        break;
      }
    }
    return strBarcodeName;
  }

  public int findSymbologyIdFromName(String strName) {
    int id = 0;
    for (int i = 0; i < this.mSymbologyNameAndId.size(); i++) {
      if (((SymbologyNameAndId)this.mSymbologyNameAndId.get(i)).strSymbologyName.equals(strName)) {
        id = ((SymbologyNameAndId)this.mSymbologyNameAndId.get(i)).symbologyId;
        break;
      }
    }
    return id;
  }
}