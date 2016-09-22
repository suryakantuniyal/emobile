package com.elotouch.paypoint.register.barcodereader;

public class BarcodeReader 
{
	private native void setJNIBarCodeReader();
    private native int isBcrTurnedOn();

    public void turnOnLaser()
	{
		setJNIBarCodeReader();
	}
    
    public boolean isBcrOn()
   	{
		return isBcrTurnedOn() == 1;
    	
   	}
}
