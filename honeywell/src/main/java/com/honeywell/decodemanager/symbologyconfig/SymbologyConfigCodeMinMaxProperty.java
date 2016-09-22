package com.honeywell.decodemanager.symbologyconfig;

import com.honeywell.decodemanager.SymbologyConfigBase;

public class SymbologyConfigCodeMinMaxProperty extends SymbologyConfigBase
{
  public void setMinLength(int minlength)
  {
    this.m_minLength = minlength;
  }

  public void setMaxLength(int maxlength)
  {
    this.m_maxLength = maxlength;
  }
}