package com.honeywell.decodemanager.symbologyconfig;

public class SymbologyConfigCode39 extends SymbologyConfigCodeMinMaxProperty
{
  public SymbologyConfigCode39()
  {
    this.m_symID = 4;
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

  public void enableFullAscii(boolean b)
  {
    if (b)
      this.m_flags |= 32;
    else
      this.m_flags &= -33;
  }

  public void enableStartStopTransmit(boolean b)
  {
    if (b)
      this.m_flags |= 8;
    else
      this.m_flags &= -9;
  }
}