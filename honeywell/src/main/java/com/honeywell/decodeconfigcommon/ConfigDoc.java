package com.honeywell.decodeconfigcommon;

import android.content.Context;
import java.util.ArrayList;

public class ConfigDoc
{
  private static ConfigDoc st = null;
  private SymbologyConfigDocIf mSymbologyConfigDoc = null;

  public static ConfigDoc getSingle() {
    if (st == null) {
      st = new ConfigDoc();
    }
    return st;
  }

  public void InitialConfig(String strSourceConfigName, Context context) {
    this.mSymbologyConfigDoc = new SymbologyConfigDoc(strSourceConfigName, context);
  }

  public void LoadConfig(IDecoderService Decoderservers, boolean bDefaultSymbologySetting) {
    if (this.mSymbologyConfigDoc != null)
      this.mSymbologyConfigDoc.loadSymbologyConfig(Decoderservers, bDefaultSymbologySetting);
  }

  public void LoadConfigwithProcess(IDecoderService Decoderservers, String processname, boolean bDefaultSymbologySetting)
  {
    if (this.mSymbologyConfigDoc != null)
      this.mSymbologyConfigDoc.loadSymbologyConfigwithProcess(Decoderservers, processname, bDefaultSymbologySetting);
  }

  public void SaveConfigure()
  {
    this.mSymbologyConfigDoc.SaveSymbologyConfig();
  }

  public void SaveConfigurewithProcess(String processname) {
    this.mSymbologyConfigDoc.SaveSymbologyConfigWithProcess(processname);
  }

  public ArrayList<Integer> getAllCommonSymbologyId() {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.getAllCommonSymbologyId();
    }
    return null;
  }

  public ArrayList<Integer> getAllSymbologyId() {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.getAllSymbologyId();
    }
    return null;
  }

  public ArrayList<Integer> getAllPostSymbologyId() {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.getAllPostalSymbologyId();
    }
    return null;
  }

  public ArrayList<SymbologyCommonProperty> getOneSymbologyProerty(int symbologyId)
  {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.getOneSymbologyProerty(symbologyId);
    }
    return null;
  }

  public boolean modifyOneSymbologyProperty(int symbologyId, SymbologyCommonProperty SymComPro) {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.modifyOneSymbologyProperty(symbologyId, SymComPro);
    }
    return false;
  }

  public boolean getOneSymbologyStatus(int symbologyId) {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.getOneSymbologyStatus(symbologyId);
    }
    return false;
  }

  public boolean modifyOneSymbologyStatus(int symbologyId, boolean b) {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.modifyOneSymbologyStatus(symbologyId, b);
    }
    return false;
  }

  public boolean HasMinMaxProperty(int symbologyId) {
    if (this.mSymbologyConfigDoc != null)
    {
      return this.mSymbologyConfigDoc.HasMinMaxProperty(symbologyId);
    }
    return false;
  }

  public MinMaxProperty getOneSymbologyMinValue(int symbologyId) {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.getOneSymbologyMinValue(symbologyId);
    }
    return null;
  }

  public MinMaxProperty getOneSymbologyMaxValue(int symbologyId) {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.getOneSymbologyMaxValue(symbologyId);
    }
    return null;
  }

  public boolean modifyOneSymbologyMinValue(int symbologyId, int value) {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.modifyOneSymbologyMinValue(symbologyId, value);
    }
    return false;
  }

  public boolean modifyOneSymbologyMaxValue(int symbologyId, int value) {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.modifyOneSymbologyMaxValue(symbologyId, value);
    }
    return false;
  }

  public boolean addSymToConfig(int id)
  {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.addOneSymToConfig(id);
    }
    return false;
  }

  public boolean removeSymFromConfig(int id)
  {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.removeOneSymFromConfig(id);
    }
    return false;
  }

  public boolean removeAllSymFromConfig()
  {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.removeAllSymFromConfig();
    }
    return false;
  }

  public boolean restoreDefaultSymToConfig()
  {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.restoreDefaultSymToConfig();
    }
    return false;
  }

  public int getOcrMode() {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.getOcrMode();
    }
    return -1;
  }

  public void setOcrMode(int OcrMode)
  {
    if (this.mSymbologyConfigDoc != null)
      this.mSymbologyConfigDoc.setOcrMode(OcrMode);
  }

  public int getOcrLength()
  {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.getOcrLength();
    }
    return -1;
  }

  public void setOcrLength(int length)
  {
    if (this.mSymbologyConfigDoc != null)
      this.mSymbologyConfigDoc.setOcrLength(length);
  }

  public boolean getbOcrSupport()
  {
    if (this.mSymbologyConfigDoc != null) {
      return this.mSymbologyConfigDoc.getbOcrSupport();
    }
    return false;
  }
}