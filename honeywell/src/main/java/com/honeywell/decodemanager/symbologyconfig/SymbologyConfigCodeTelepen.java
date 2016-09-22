package com.honeywell.decodemanager.symbologyconfig;

public class SymbologyConfigCodeTelepen extends SymbologyConfigCodeMinMaxProperty
{
  public SymbologyConfigCodeTelepen()
  {
    this.m_symID = 40;
    this.m_mask = 7;
  }

  public void EnableTelepenOldStyle(boolean b)
  {
    if (b)
      this.m_flags |= 67108864;
    else
      this.m_flags &= -67108865;
  }
}