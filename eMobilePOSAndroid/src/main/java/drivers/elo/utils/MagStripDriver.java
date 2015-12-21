package drivers.elo.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

import com.magtek.mobile.android.libDynamag.MagTeklibDynamag;

public class MagStripDriver 
{
	public interface MagStripeListener
	{
		public abstract void OnCardSwiped(MagTeklibDynamag cardData);
		public abstract void OnDeviceDisconnected();
		public abstract void OnDeviceConnected();
	}

	protected static final int DEVICE_CONNECTED = 4;
	protected static final int DEVICE_DISCONNECTED = 5;
	protected static final int DEVICE_CARD_SWIPED = 3;

	private MagTeklibDynamag mMagStripe = null;
	MagStripeListener mListener = null;
	
	private Handler MagStripeHandler = new Handler(new Callback()
	{ 
		//Callback to handle message from Magstripe Class
		@Override
		public boolean handleMessage(Message msg) {
			return handleMagStripeMessage(msg);
		}

	});

	public MagStripDriver(Context context)
	{
		mMagStripe = new MagTeklibDynamag(context, MagStripeHandler);
	}

	public void registerMagStripeListener(MagStripeListener listener)
	{
		mListener = listener;
	}

	private boolean handleMagStripeMessage(Message msg){

		if(msg != null)
		{
			switch(msg.what)
			{
				case DEVICE_CONNECTED : 
				{
					if(mListener != null)
						mListener.OnDeviceConnected();
					break;
				}
				case DEVICE_DISCONNECTED :
				{
					if(mListener != null) mListener.OnDeviceDisconnected();
					break;
				}
				case DEVICE_CARD_SWIPED : 
				{
					if(mListener != null){ 
						try {
							mMagStripe.setCardData((String)msg.obj);
							if ((mMagStripe.getTrack2Masked().length() > 0)&& (!mMagStripe.getTrack2Masked().equalsIgnoreCase(";E?"))){
								mListener.OnCardSwiped(mMagStripe);
							}
						} catch (Exception e) {
							// This is basically to catch the runtime exceptions
							Log.e("PPT Diagnistics", e.getMessage(), e);
						}
					}
					break;
				}
				default : 
				break;
			
			}
		}
		return true;

	}
	
	public void startDevice(){ //Start the Device.
		if(!mMagStripe.isDeviceConnected()){
			mMagStripe.openDevice();	
		}
	}
	public byte[] sendCommand(String command) {
		CharSequence csCommand = command;
		byte[] bResponse = mMagStripe.sendCommand(csCommand);
		return bResponse;
	}
	public byte[] sendCommandWithLength(String lCommand)
	{
		CharSequence csCommand = lCommand;
		byte[] bResponse = mMagStripe.sendCommand(csCommand);
		return bResponse;
	}
	public void stopDevice()
	{ 
		//Disable the Device
		mMagStripe.clearCardData();
		if(mMagStripe.isDeviceConnected())
		{
			mMagStripe.closeDevice();
		}
	}
	public void stopAllListener()
	{ 
		MagStripeHandler.removeCallbacksAndMessages(null);
		MagStripeHandler = null;
		if(mMagStripe!=null)
		{
			mMagStripe = null;
		}
	}
}
