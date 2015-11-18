package com.honeywell.decodemanager.symbologyconfig;

public class SymbologyConfigCodeInt25 extends SymbologyConfigCodeMinMaxProperty
{
  public SymbologyConfigCodeInt25()
  {
    this.m_symID = 11;
    this.m_mask = 7;
  }

  public void enableCheckEnable(boolean b)
  {
    if (b)
      this.m_flags |= 2;
    else
      this.m_flags &= -3;
  }

  public void enableCheckTransmit(boolean b)
  {
    if (b)
      this.m_flags |= 4;
    else
      this.m_flags &= -5;
  }
}