package com.honeywell.decodemanager.symbologyconfig;

public class SymbologyConfigCodeMsi extends SymbologyConfigCodeMinMaxProperty
{
  public SymbologyConfigCodeMsi()
  {
    this.m_symID = 31;
    this.m_mask = 7;
  }

  public void enableCheckTransmit(boolean b)
  {
    if (b)
      this.m_flags |= 4;
    else
      this.m_flags &= -5;
  }
}