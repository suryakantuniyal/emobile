package drivers;

import com.android.support.CardParser;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import net.homeatm.rover.RoverController;
import net.homeatm.rover.RoverController.RoverStateChangedListener;
import net.homeatm.rover.RoverController.TransactionType;
import interfaces.EMSCallBack;


public class EMSRover implements RoverStateChangedListener {

	private final static String INTENT_ACTION_CALL_STATE = "net.homeatm.rover.CALL_STATE";
	private RoverController roverController;

	// private IncomingCallServiceReceiver incomingCallServiceReceiver;

	private Activity activity;
	private CreditCardInfo cardManager;
	private boolean isDebit = false;
	private boolean isConnected = false;
	private boolean isSwipping = false;
	private Handler handler = new Handler();
	
	public void initializeReader(Activity activity, boolean _isDebit)
	{
    	this.activity = activity;
    	cardManager = new CreditCardInfo();
    	isDebit = _isDebit;
    	
    	//startCallStateService();
    	
		roverController = new RoverController(this.activity, this);
		roverController.setDetectDeviceChange(false);
    	
    	initConnect();
    	

	}
    
    public void release()
    {
    	roverController.deleteRover();
    }
    
    
	private void initConnect() 
	{
		if(isDebit)
		{
			roverController.startRover(TransactionType.PINNED_TRANSACTION);
		}
		else
		{
			roverController.startRover(TransactionType.SWIPED_TRANSACTION);
		}
	}
	
	
    
    public void startReading()
    {
		if(isDebit)
		{
			roverController.startRover(TransactionType.PINNED_TRANSACTION);
		}
		else
		{
			roverController.startRover(TransactionType.SWIPED_TRANSACTION);
		}
    }
    

 	public boolean readerIsConnected()
 	{
 		return isConnected;
 	}
 	
 	
 	public boolean readerIsSwipping()
 	{
 		return isSwipping;
 	}
 	
	// displays data from card swiping
	private Runnable doUpdateViews = new Runnable() {
		public void run() {
			try {
				EMSCallBack callBack = (EMSCallBack) activity;
				callBack.cardWasReadSuccessfully(true, cardManager);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	private Runnable doUpdateDidConnect = new Runnable() {
		public void run() {
			try {
				EMSCallBack callBack = (EMSCallBack) activity;
				callBack.readerConnectedSuccessfully(true);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	@Override
	public void onBackButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCardDecodeCompleted(TransactionType transType, String first6Digits,
			String last4Digits, String expiryDate, String name, String ksn, 
			String timestamp, String track1, String track2, String track3) {
		// TODO Auto-generated method stub
		//still need to determine if swipe was encrypted
		if(!ksn.isEmpty())
		{
//			StringBuilder tracks = new StringBuilder();
//			tracks.append(track1).append(track2.replace("B", ";").replace("D", "=").replace("F", "?"));
//			this.cardManager = Global.parseSimpleMSR(activity, tracks.toString());
//			this.cardManager.setCardLast4(last4Digits);
//			this.cardManager.setCardOwnerName(name);
			
			CardParser.parseEncryptedCreditCard(activity, cardManager, track1, track2, ksn, name,first6Digits, last4Digits, expiryDate, "", "");
		}
		else
		{
			if(expiryDate!=null && !expiryDate.isEmpty() && expiryDate.length()==4)
			{
				cardManager.setCardExpYear(expiryDate.substring(0, 2));
				cardManager.setCardExpMonth(expiryDate.substring(2, 4));
			}
			Encrypt encrypt = new Encrypt(activity);
			cardManager.setTrackDataKSN(ksn);
			cardManager.setCardLast4(last4Digits);
			cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(track1));
			cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(track2));
			StringBuilder sb = new StringBuilder();
			cardManager.setEncryptedBlock(sb.append(track1).append(track2).toString());
			cardManager.setEncryptedTrack1(track1);
			cardManager.setEncryptedTrack2(track2);
			int i = track2.lastIndexOf("B");
			int j = track2.lastIndexOf("D");
			cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(track2.substring(i, j)));
			//cardManager.setDeviceSerialNumber(cd.getSerialNumber());
			cardManager.setCardOwnerName(name);
		}
		if(!isDebit)
			handler.post(doUpdateViews);
	}

	@Override
	public void onDecodingCardData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDecodingEPBData() {
		// TODO Auto-generated method stub
		//handler.post(doUpdateViews);
	}

	@Override
	public void onDevicePlugged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeviceUnplugged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEPBDecodeCompleted(String ksnForEPB, String epb) {
		// TODO Auto-generated method stub
		//this.cardManager.setDebitPinNum(epb);
		this.cardManager.setDebitPinBlock(epb);
		this.cardManager.setDebitPinSerialNum(ksnForEPB);
		handler.post(doUpdateViews);
	}

	@Override
	public void onEnterButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInterrupted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNoDeviceDetected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPinButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTimeout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWaitingForCardSwipe() {
		// TODO Auto-generated method stub
		isConnected = true;
		isSwipping = true;
		handler.post(doUpdateDidConnect);
	}

	@Override
	public void onWaitingForDevice() {
		// TODO Auto-generated method stub
		
	}
	
	private void startCallStateService() {
		activity.startService(new Intent(INTENT_ACTION_CALL_STATE));
//		if (incomingCallServiceReceiver == null) {
//			incomingCallServiceReceiver = new IncomingCallServiceReceiver();
//			IntentFilter intentFilter = new IntentFilter();
//			intentFilter.addAction(RoverCallStateService.INTENT_ACTION_INCOMING_CALL);
//			activity.registerReceiver(incomingCallServiceReceiver, intentFilter);
//		}
	}
	
	private void endCallStateService() {
		activity.stopService(new Intent(INTENT_ACTION_CALL_STATE));
//		if (incomingCallServiceReceiver != null) {
//			activity.unregisterReceiver(incomingCallServiceReceiver);
//			incomingCallServiceReceiver = null;
//		}
	}

	
	// -----------------------------------------------------------------------
		// Inner classes
		// -----------------------------------------------------------------------
		
//		private class IncomingCallServiceReceiver extends BroadcastReceiver {		
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				
//				if (intent.getAction().equals(RoverCallStateService.INTENT_ACTION_INCOMING_CALL)) {
//					try {
//						if (roverController.getRoverState() != RoverControllerState.STATE_IDLE) {
//							roverController.stopRover();
//						}
//					}
//					catch (IllegalStateException ex) {
//					}
//				}			
//			} // end-of onReceive		
//		}
}
