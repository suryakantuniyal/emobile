package com.honeywell.decodeconfigcommon;

import java.util.ArrayList;

abstract interface SymbologyConfigDocIf
{
  public abstract void loadSymbologyConfig(IDecoderService paramIDecoderService, boolean paramBoolean);

  public abstract void loadSymbologyConfigwithProcess(IDecoderService paramIDecoderService, String paramString, boolean paramBoolean);

  public abstract void SaveSymbologyConfig();

  public abstract void SaveSymbologyConfigWithProcess(String paramString);

  public abstract ArrayList<Integer> getAllCommonSymbologyId();

  public abstract ArrayList<Integer> getAllPostalSymbologyId();

  public abstract ArrayList<Integer> getAllSymbologyId();

  public abstract ArrayList<SymbologyCommonProperty> getOneSymbologyProerty(int paramInt);

  public abstract boolean modifyOneSymbologyProperty(int paramInt, SymbologyCommonProperty paramSymbologyCommonProperty);

  public abstract boolean getOneSymbologyStatus(int paramInt);

  public abstract boolean modifyOneSymbologyStatus(int paramInt, boolean paramBoolean);

  public abstract boolean HasMinMaxProperty(int paramInt);

  public abstract MinMaxProperty getOneSymbologyMinValue(int paramInt);

  public abstract MinMaxProperty getOneSymbologyMaxValue(int paramInt);

  public abstract boolean modifyOneSymbologyMinValue(int paramInt1, int paramInt2);

  public abstract boolean modifyOneSymbologyMaxValue(int paramInt1, int paramInt2);

  public abstract boolean addOneSymToConfig(int paramInt);

  public abstract boolean removeOneSymFromConfig(int paramInt);

  public abstract boolean removeAllSymFromConfig();

  public abstract boolean restoreDefaultSymToConfig();

  public abstract int getOcrMode();

  public abstract void setOcrMode(int paramInt);

  public abstract int getOcrLength();

  public abstract void setOcrLength(int paramInt);

  public abstract boolean getbOcrSupport();
}