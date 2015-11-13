package com.honeywell.decodemanager.symbologyconfig;

import com.honeywell.decodemanager.SymbologyConfigBase;

public class SymbologyConfigCodePostnet extends SymbologyConfigBase
{
  public SymbologyConfigCodePostnet()
  {
    this.m_symID = 16;
    this.m_mask = 1;
  }

  public void enableCheckTransmit(boolean b)
  {
    if (b)
      this.m_flags |= 4;
    else
      this.m_flags &= -5;
  }
}