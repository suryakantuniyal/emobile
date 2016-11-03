package com.honeywell.decodemanager.symbologyconfig;

import com.honeywell.decodemanager.SymbologyConfigBase;

public class SymbologyConfigCodeEan13 extends SymbologyConfigBase
{
  public SymbologyConfigCodeEan13()
  {
    this.m_symID = 10;
    this.m_mask = 1;
  }

  public void enableCheckTransmit(boolean b)
  {
    if (b)
      this.m_flags |= 4;
    else
      this.m_flags &= -5;
  }

  public void enableAddenda2Digit(boolean b)
  {
    if (b)
      this.m_flags |= 128;
    else
      this.m_flags &= -129;
  }

  public void enableAddenda5Digit(boolean b)
  {
    if (b)
      this.m_flags |= 256;
    else
      this.m_flags &= -257;
  }

  public void enableAddendaRequired(boolean b)
  {
    if (b)
      this.m_flags |= 512;
    else
      this.m_flags &= -513;
  }

  public void enableAddendaSeparator(boolean b)
  {
    if (b)
      this.m_flags |= 1024;
    else
      this.m_flags &= -1025;
  }
}