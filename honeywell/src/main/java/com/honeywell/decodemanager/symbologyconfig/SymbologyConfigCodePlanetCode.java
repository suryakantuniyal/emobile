package com.honeywell.decodemanager.symbologyconfig;

import com.honeywell.decodemanager.SymbologyConfigBase;

public class SymbologyConfigCodePlanetCode extends SymbologyConfigBase
{
  public SymbologyConfigCodePlanetCode()
  {
    this.m_symID = 29;
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