package com.honeywell.decodemanager.symbologyconfig;

public class SymbologyConfigCodeRss extends SymbologyConfigCodeMinMaxProperty
{
  public SymbologyConfigCodeRss()
  {
    this.m_symID = 18;
    this.m_mask = 7;
  }

  public void enableRssEnable(boolean b)
  {
    if (b)
      this.m_flags |= 33554432;
    else
      this.m_flags &= -33554433;
  }

  public void enableRslEnable(boolean b)
  {
    if (b)
      this.m_flags |= 16777216;
    else
      this.m_flags &= -16777217;
  }

  public void enableRseEnable(boolean b)
  {
    if (b)
      this.m_flags |= 8388608;
    else
      this.m_flags &= -8388609;
  }
}