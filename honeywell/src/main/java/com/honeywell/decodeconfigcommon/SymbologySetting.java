package com.honeywell.decodeconfigcommon;

import java.util.ArrayList;

public class SymbologySetting
{
  private ArrayList<SymbologyCommonProperty> m_arryConfigData;
  private ArrayList<MinMaxProperty> m_arryMinMaxProperty;
  private boolean mbSymbologyActive = true;
  private int mSymbologyID = -1;

  public SymbologySetting() {
    this.m_arryConfigData = new ArrayList();
    this.m_arryMinMaxProperty = new ArrayList();
  }

  protected ArrayList<SymbologyCommonProperty> GetConfigDataArry() {
    return this.m_arryConfigData;
  }

  protected ArrayList<MinMaxProperty> GetMinMaxPropertyArry() {
    return this.m_arryMinMaxProperty;
  }

  public boolean getbSymbologyActive() {
    return this.mbSymbologyActive;
  }

  protected void setbSymbologyActive(boolean b) {
    this.mbSymbologyActive = b;
  }

  public int getSymbologyId() {
    return this.mSymbologyID;
  }

  protected void setSymbologyId(int id) {
    this.mSymbologyID = id;
  }
}