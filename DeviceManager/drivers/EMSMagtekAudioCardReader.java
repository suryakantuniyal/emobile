package drivers;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import main.EMSDeviceManager;

import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;


import com.android.emobilepos.models.Orders;
import com.android.emobilepos.payment.ProcessCreditCard_FA;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.magtek.mobile.android.scra.MTSCRAException;
import com.magtek.mobile.android.scra.MagTekSCRA;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;

import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.widget.Toast;



public class EMSMagtekAudioCardReader extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate
{
	public static final String CONFIG_FILE = "MTSCRADevConfig.cfg";
	private Handler handler;// = new Handler();
	private Activity activity;
	private AudioManager mAudioMgr;	
	private CreditCardInfo cardManager;
	private Encrypt encrypt;
	private EMSDeviceManager edm;
	private ProgressDialog myProgressDialog;
	private EMSDeviceDriver thisInstance;
	private MagTekSCRA mMTSCRA;
	private EMSCallBack callBack;
	
	public EMSMagtekAudioCardReader()
	{
		
	}
	
	
	public EMSMagtekAudioCardReader(Activity activity)
	{
		this.activity = activity;
		init();
	}
	
	private void init()
	{
		mMTSCRA = new MagTekSCRA(new Handler(new SCRAHandlerCallback()));
		mAudioMgr = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);	
		if(handler==null)
			handler = new Handler();
		cardManager = new CreditCardInfo();
		encrypt = new Encrypt(activity);
	}
	
	public void connectMagtek(boolean isAudio,EMSCallBack _callBack)
	{
		
		
		if(isAudio)
		{
	       if(!mMTSCRA.isDeviceConnected())
		   {
	    	   callBack = _callBack;
	      		   mMTSCRA.setDeviceType(MagTekSCRA.DEVICE_TYPE_AUDIO);
	        	   openDevice();
		   }
		}
		else
		{
			MyPreferences myPref = new MyPreferences(activity);
			mMTSCRA.setDeviceType(MagTekSCRA.DEVICE_TYPE_BLUETOOTH);
			mMTSCRA.setDeviceID(myPref.swiperMACAddress(true, null));
			openDevice();
		}
	}
	
	
	private void openDevice()
	{
		if(mMTSCRA.getDeviceType()==MagTekSCRA.DEVICE_TYPE_AUDIO)
		{
			Thread tSetupAudioParams = new Thread() {
				public void run()
				{
					try
					{
						if(setupAudioParameters().equals("OK"))
							mMTSCRA.openDevice();
					}
					catch(Exception ex)
					{
							
					}
				}
			};
			tSetupAudioParams.start();
			
		}
		else
		{
			mMTSCRA.openDevice();
			
		}
	}
	
	
	public void closeDevice()
	{
		mMTSCRA.closeDevice();
	}
	
	
	private String setupAudioParameters()throws MTSCRAException
    {
		//mStringLocalConfig="";
		String strResult="OK";
		
		try
		{
			String strXMLConfig="";

			if (strXMLConfig.length() <= 0)
			{
				setAudioConfigManual();					
			}
			else
			{
				mMTSCRA.setConfigurationXML(strXMLConfig);//Convert XML to Response Object
				return strResult;
			}
		}
		catch(MTSCRAException ex)
		{
			
			//throw new MTSCRAException(ex.getMessage());
		}
		return strResult;
    }
	
	
	private void setAudioConfigManual()throws MTSCRAException
	{
    	String model = android.os.Build.MODEL.toUpperCase(Locale.getDefault());
		try
		{
	    	if(model.contains("DROID RAZR") || model.toUpperCase(Locale.getDefault()).contains("XT910"))
	        {
				    
				   mMTSCRA.setConfigurationParams("INPUT_SAMPLE_RATE_IN_HZ=48000,");
				   
	        }
	        else if ((model.equals("DROID PRO"))||
	        		 (model.equals("MB508"))||
	        		 (model.equals("DROIDX"))||
	        		 (model.equals("DROID2"))||
	        		 (model.equals("MB525")))
	        {
				 
				
				  mMTSCRA.setConfigurationParams("INPUT_SAMPLE_RATE_IN_HZ=32000,");
	        }    	
	        else if ((model.equals("GT-I9300"))||//S3 GSM Unlocked
	        		 (model.equals("SPH-L710"))||//S3 Sprint
	        		 (model.equals("SGH-T999"))||//S3 T-Mobile
	        		 (model.equals("SCH-I535"))||//S3 Verizon
	        		 (model.equals("SCH-R530"))||//S3 US Cellular
	        		 (model.equals("SAMSUNG-SGH-I747"))||// S3 AT&T
	        		 (model.equals("M532"))||//Fujitsu
	        		 (model.equals("GT-N7100"))||//Notes 2 
	        		 (model.equals("GT-N7105"))||//Notes 2 
	        		 (model.equals("SAMSUNG-SGH-I317"))||// Notes 2
	        		 (model.equals("SCH-I605"))||// Notes 2
	        		 (model.equals("SCH-R950"))||// Notes 2
	        		 (model.equals("SGH-T889"))||// Notes 2
	        		 (model.equals("SPH-L900"))||// Notes 2
	        		 (model.equals("SAMSUNG-SGH-I337"))||// S4
	        		 (model.equals("GT-P3113")))//Galaxy Tab 2, 7.0
	        		
	        {
				 
	        	  mMTSCRA.setConfigurationParams("INPUT_AUDIO_SOURCE=VRECOG,");
	        }
	        else if ((model.equals("XT907")))
	        {
				  mMTSCRA.setConfigurationParams("INPUT_WAVE_FORM=0,");
	        }    	
	        else
	        {
	        	  debugMsg("Found Setting for :"  + model); 
	        	  mMTSCRA.setConfigurationParams("INPUT_AUDIO_SOURCE=VRECOG,");
	        }
		}
		catch(MTSCRAException ex)
		{
			
			throw new MTSCRAException(ex.getMessage());
		}
		
	}


	private void debugMsg(String lpstrMessage)
	{
		Log.i("MagTekSCRA.Demo:",lpstrMessage);
		
	}
	
	private class SCRAHandlerCallback implements Callback {
        public boolean handleMessage(Message msg) 
        {
        	
        	try
        	{
            	switch (msg.what) 
            	{
    			case MagTekSCRA.DEVICE_MESSAGE_STATE_CHANGE:
    				switch (msg.arg1) {
    				case MagTekSCRA.DEVICE_STATE_CONNECTED:
    					//Toast.makeText(activity, "state connected..", Toast.LENGTH_LONG).show();
    					if(mMTSCRA.getDeviceType()==MagTekSCRA.DEVICE_TYPE_AUDIO)
    						maxVolume();
    					handler.post(doUpdateDidConnect);
    					break;
    				case MagTekSCRA.DEVICE_STATE_CONNECTING:
    					break;
    				case MagTekSCRA.DEVICE_STATE_DISCONNECTED:
    					break;
    				}
    				break;
    			case MagTekSCRA.DEVICE_MESSAGE_DATA_START:
    	        	if (msg.obj != null) 
    	        	{
    	        		//Toast.makeText(activity, "msr..", Toast.LENGTH_LONG).show();
    	                return true;
    	            }
    				break;  
    			case MagTekSCRA.DEVICE_MESSAGE_DATA_CHANGE:
    	        	if (msg.obj != null) 
    	        	{
    	        		//Toast.makeText(activity, "swipe..", Toast.LENGTH_LONG).show();
    	        		retrieveCardInfo();
    	        		msg.obj = null;
    	        		
    	        		handler.post(doUpdateViews);
    	                return true;
    	            }
    				break;  
    			case MagTekSCRA.DEVICE_MESSAGE_DATA_ERROR:
    				Toast.makeText(activity, "swipe error..", Toast.LENGTH_LONG).show();
	                return true;
    			default:
    	        	if (msg.obj != null) 
    	        	{
    	                return true;
    	            }
    				break;
            	};
        		
        	}
        	catch(Exception ex)
        	{
        		
        	}

            return false;
        	
        	
        }
    }	
	
	
	private void retrieveCardInfo()
	{
		StringBuilder sb = new StringBuilder();
		
		if(mMTSCRA.getResponseData()!=null)
		{
			cardManager.setCardOwnerName(mMTSCRA.getCardName());
			if(mMTSCRA.getCardExpDate()!=null&&!mMTSCRA.getCardExpDate().isEmpty())
			{
				String year= mMTSCRA.getCardExpDate().substring(0, 2);
				String month = mMTSCRA.getCardExpDate().substring(2, 4);
				cardManager.setCardExpYear(year);
				cardManager.setCardExpMonth(month);
			}
			cardManager.setCardType(ProcessCreditCard_FA.cardType(mMTSCRA.getCardIIN()));
			cardManager.setCardLast4(mMTSCRA.getCardLast4());
			cardManager.setEncryptedBlock(sb.append(mMTSCRA.getTrack1()).append(mMTSCRA.getTrack2()).toString());
			cardManager.setEncryptedTrack1(mMTSCRA.getTrack1());
			cardManager.setEncryptedTrack2(mMTSCRA.getTrack2());
			cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(mMTSCRA.getTrack2Masked().split("=")[0].replace(";", "")));
			if(!Global.isEncryptSwipe)
				cardManager.setCardNumUnencrypted(mMTSCRA.getTrack2Masked().split("=")[0].replace(";", "").replace("?", ""));
			if(mMTSCRA.getTrack1Masked()!=null&&!mMTSCRA.getTrack1Masked().isEmpty())
				cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(mMTSCRA.getTrack1Masked()));
			if(mMTSCRA.getTrack2Masked()!=null&&!mMTSCRA.getTrack2Masked().isEmpty())
				cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(mMTSCRA.getTrack2Masked()));
			cardManager.setDeviceSerialNumber(mMTSCRA.getDeviceSerial());
			cardManager.setMagnePrint(mMTSCRA.getMagnePrint());
			cardManager.setMagnePrintStatus(mMTSCRA.getMagnePrintStatus());
			cardManager.setTrackDataKSN(mMTSCRA.getKSN());
		}
	}
	
	
	private void maxVolume()
	{
		mAudioMgr.setStreamVolume(AudioManager.STREAM_MUSIC,mAudioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC),AudioManager.FLAG_SHOW_UI);	
			    	
	                
	}

	
	// displays data from card swiping
	private Runnable doUpdateViews = new Runnable() {
		public void run() {
			try {
				if(callBack!=null)
				{
					//Toast.makeText(activity, "card was swiper calling callback..", Toast.LENGTH_LONG).show();
					callBack.cardWasReadSuccessfully(true,cardManager);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	private Runnable doUpdateDidConnect = new Runnable() {
		public void run() {
			try {
				if(callBack!=null)
				{
					//Toast.makeText(activity, "update did connected..", Toast.LENGTH_LONG).show();
					callBack.readerConnectedSuccessfully(true);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	@Override
	public void connect(Activity activity,int paperSize,boolean isPOSPrinter,EMSDeviceManager edm) 
	{
		this.edm = edm;
		this.activity = activity;
		thisInstance  = this;
		mMTSCRA = new MagTekSCRA(new Handler(new SCRAHandlerCallback()));
		mAudioMgr = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);	
		cardManager = new CreditCardInfo();
		encrypt = new Encrypt(activity);
		new connectAsync().execute();
	}
	
	
	@Override
	public boolean autoConnect(Activity _activity,EMSDeviceManager edm,int paperSize,boolean isPOSPrinter, final String portName, String portNumber)
	{
		this.edm = edm;
		
		thisInstance  = this;
		boolean didConnect = false;
		this.activity = _activity;
		
		activity.runOnUiThread(new Runnable() {
			  public void run() {
				  mMTSCRA = new MagTekSCRA(new Handler(new SCRAHandlerCallback()));
				  
			  }
			});
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		mAudioMgr = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);	
		cardManager = new CreditCardInfo();
		encrypt = new Encrypt(activity);
		if(mMTSCRA!=null)
		{
			mMTSCRA.setDeviceType(MagTekSCRA.DEVICE_TYPE_BLUETOOTH);
			mMTSCRA.setDeviceID(portName);
			openDevice();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			didConnect = mMTSCRA.isDeviceConnected();
			
			if(didConnect)
				mMTSCRA.closeDevice();
			
			

		}
		if (didConnect) {
			this.edm.driverDidConnectToDevice(thisInstance,false);
		} else {

			this.edm.driverDidNotConnectToDevice(thisInstance, null,false);
		}
		
		return didConnect;
	}
	
	
	
	
	public class connectAsync extends AsyncTask<Integer, String, String> {

		String msg = new String();
		boolean didConnect = false;

		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage("Connecting MAGTEK...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

		}

		@Override
		protected String doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			MyPreferences myPref = new MyPreferences(activity);
			mMTSCRA.setDeviceType(MagTekSCRA.DEVICE_TYPE_BLUETOOTH);
			mMTSCRA.setDeviceID(myPref.swiperMACAddress(true, null));
			openDevice();
			try {
				Thread.sleep(6000);
			} catch (InterruptedException e) {
			}
			didConnect = mMTSCRA.isDeviceConnected();
			
			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss();

			if (didConnect) {
				mMTSCRA.closeDevice();
				edm.driverDidConnectToDevice(thisInstance, true);
				
			} else {

				edm.driverDidNotConnectToDevice(thisInstance, msg, true);
			}

		}
	}
	

	@Override
	public boolean printTransaction(String ordID, int type, boolean isFromHistory, boolean fromOnHold) {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint) {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
		// TODO Auto-generated method stub
		return true;	
	}


	@Override
	public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public void printStationPrinter(List<Orders> orderProducts, String ordID) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean printOpenInvoices(String invID) {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public boolean printOnHold(Object onHold) {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public void setBitmap(Bitmap bmp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printEndOfDayReport(String date, String clerk_id)
	{
		
	}

	@Override
	public boolean printReport(String curDate) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void registerAll() {
		this.registerPrinter();
	}
	
	@Override
	public void registerPrinter() {
		// TODO Auto-generated method stub
		edm.currentDevice = this;
	}


	@Override
	public void unregisterPrinter() {
		// TODO Auto-generated method stub
		edm.currentDevice= null;
	}


	@Override
	public void loadCardReader(EMSCallBack _callBack) {
		// TODO Auto-generated method stub
		callBack = _callBack;
		if(handler==null)
			handler = new Handler();
		if(mMTSCRA!=null)
		{
			new Thread(new Runnable(){
				public void run()
				{
					mMTSCRA.openDevice();
				}
			}).start();
			
		}
		
	}

	@Override
	public void releaseCardReader() {
		// TODO Auto-generated method stub
		callBack = null;
		mMTSCRA.closeDevice();
	}


	@Override
	public void openCashDrawer() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void printHeader() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void printFooter() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadScanner(EMSCallBack _callBack) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean isUSBConnected()
	{
		return false;
	}
}

