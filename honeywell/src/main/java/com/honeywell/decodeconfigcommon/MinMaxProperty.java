package com.honeywell.decodeconfigcommon;

public class MinMaxProperty
{
  private MinMax minmax;
  private int ValueFrom;
  private int ValueTo;
  private int Value;

  public MinMaxProperty()
  {
    this.minmax = MinMax.MIN;
    this.ValueFrom = 0;
    this.ValueTo = 0;
    this.Value = 0;
  }

  public MinMax getMinMax()
  {
    return this.minmax;
  }

  public int getValueFrom()
  {
    return this.ValueFrom;
  }

  public int getValueTo()
  {
    return this.ValueTo;
  }

  public int getValue()
  {
    return this.Value;
  }

  protected void setMinMax(MinMax Minmax)
  {
    this.minmax = Minmax;
  }

  protected void setValueFrom(int value)
  {
    this.ValueFrom = value;
  }

  protected void setValueTo(int value)
  {
    this.ValueTo = value;
  }

  protected void setValue(int value)
  {
    this.Value = value;
  }

  public static enum MinMax
  {
    MIN, MAX;
  }
}