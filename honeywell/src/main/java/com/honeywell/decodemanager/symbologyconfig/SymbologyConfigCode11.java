package com.honeywell.decodemanager.symbologyconfig;

public class SymbologyConfigCode11 extends SymbologyConfigCodeMinMaxProperty
{
  public SymbologyConfigCode11()
  {
    this.m_symID = 2;
    this.m_mask = 7;
  }

  public void enableCheckEnable(boolean b)
  {
    if (b)
      this.m_flags |= 2;
    else
      this.m_flags &= -3;
  }
}