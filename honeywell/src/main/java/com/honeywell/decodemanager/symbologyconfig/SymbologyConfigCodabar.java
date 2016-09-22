package com.honeywell.decodemanager.symbologyconfig;

public class SymbologyConfigCodabar extends SymbologyConfigCodeMinMaxProperty
{
  public SymbologyConfigCodabar()
  {
    this.m_symID = 1;
    this.m_mask = 7;
  }

  public void enableConcatenate(boolean b)
  {
    if (b)
      this.m_flags |= 536870912;
    else
      this.m_flags &= -536870913;
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

  public void enableStartStopTransmit(boolean b)
  {
    if (b)
      this.m_flags |= 8;
    else
      this.m_flags &= -9;
  }
}