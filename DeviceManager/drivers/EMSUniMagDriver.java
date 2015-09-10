package drivers;

import protocols.EMSCallBack;
import util.CardData;

import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;

import IDTech.MSR.XMLManager.StructConfigParameters;
import IDTech.MSR.uniMag.uniMagReader;
import IDTech.MSR.uniMag.uniMagReaderMsg;
import IDTech.MSR.uniMag.UniMagTools.uniMagReaderToolsMsg;
import IDTech.MSR.uniMag.UniMagTools.uniMagSDKTools;
import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;


public class EMSUniMagDriver implements  uniMagReaderMsg ,uniMagReaderToolsMsg{
	

	private uniMagReader myUniMagReader = null;
	private uniMagSDKTools firmwareUpdateTool = null;
	
	private Activity activity;
	private boolean isWaitingForCommandResult=false;
	private String strMsrData;
	private boolean isSwipping = false;
	
	private Handler handler = new Handler();
	private boolean isConnected = false;
	private CreditCardInfo cardManager;
	private String fileNameWithPath ="/data/data/com.android.emobilepos/files/idt_unimagcfg_default.xml";
	private EMSUniMagDriver callBack;
    
    
    public void initializeReader(Activity activity)
	{
    	this.activity = activity;
    	cardManager = new CreditCardInfo();
    	callBack = this;
    	initConnect();
	}
    
    public void release()
    {
    	myUniMagReader.unregisterListen();
    	myUniMagReader.release();
    	isSwipping = false;
    	isConnected = false;
    }
    
    
	private void initConnect() {

		if (myUniMagReader != null) {
			myUniMagReader.unregisterListen();
			myUniMagReader.release();
			myUniMagReader = null;
		}

		myUniMagReader = new uniMagReader(this, activity);

		myUniMagReader.setVerboseLoggingEnable(true);
		myUniMagReader.registerListen();

		// ----File Name Path was hardcoded in order to improve performance

		/*
		 * fileNameWithPath = getXMLFileFromRaw();
		 * if(!isFileExist(fileNameWithPath)) { fileNameWithPath = null; }
		 */

		new Thread(new Runnable() {
			public void run() {
				myUniMagReader.setXMLFileNameWithPath(fileNameWithPath);
				myUniMagReader.loadingConfigurationXMLFile(true);

			}
		}).start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		firmwareUpdateTool = new uniMagSDKTools(callBack, activity);
		firmwareUpdateTool.setUniMagReader(myUniMagReader);
		myUniMagReader.setSDKToolProxy(firmwareUpdateTool.getSDKToolProxy());

	}
	
	
    
    public void startReading()
    {
        if (myUniMagReader!=null)
		{	
			if (!isWaitingForCommandResult) 
			{
				myUniMagReader.startSwipeCard();
			}
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
 	
	



	@Override
	public boolean getUserGrant(int type, String arg1) {
		// TODO Auto-generated method stub
		boolean getUserGranted = false;
		switch(type)
		{
		case uniMagReaderMsg.typeToPowerupUniMag:
			//pop up dialog to get the user grant
			getUserGranted = true;
			break;
		case uniMagReaderMsg.typeToUpdateXML:
			//pop up dialog to get the user grant
			getUserGranted = true;
			break;
		case uniMagReaderMsg.typeToOverwriteXML:
			//pop up dialog to get the user grant
			getUserGranted = true;
			break;
		case uniMagReaderMsg.typeToReportToIdtech:
			//pop up dialog to get the user grant
			getUserGranted = true;
			break;
		default:
			getUserGranted = false;
			break;
		}
		return getUserGranted;
	}

	
	// displays data from card swiping
		private Runnable doUpdateViews = new Runnable()
		{
			public void run()
			{
				try
				{
					EMSCallBack callBack = (EMSCallBack) activity;
					callBack.cardWasReadSuccessfully(true,cardManager);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		};
		
		
		private Runnable doUpdateDidConnect = new Runnable()
		{
			public void run()
			{
				try
				{
					EMSCallBack callBack = (EMSCallBack) activity;
					callBack.readerConnectedSuccessfully(true);

				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		};

	@Override
	public void onReceiveMsgAutoConfigCompleted(StructConfigParameters arg0) {
		// TODO Auto-generated method stub		
	}



	@Override
	public void onReceiveMsgAutoConfigProgress(int arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onReceiveMsgAutoConfigProgress(int arg0, double arg1, String arg2) {
		// TODO Auto-generated method stub
	}



	@Override
	public void onReceiveMsgCardData(byte flagOfCardData,byte[] cardData) {
		// TODO Auto-generated method stub
		
		byte flag = (byte) (flagOfCardData&0x04);
		
		
		if(flag==0x00)
		{
			strMsrData = new String (cardData);
		}
		if(flag==0x04)
		{
			//You need to decrypt the data here first.
			strMsrData = new String (cardData);
		}
		
		
	
//		CardData cd = new CardData(cardData);
//		StringBuilder tracks= new StringBuilder();
//		
//		if(cd.isDataEncrypted()&&cd.getT2Encrypted()!=null)
//		{
//			Encrypt encrypt = new Encrypt(activity);
//			cardManager.setTrackDataKSN(cd.getKSN());
//			cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(cd.getT1DataAscii()));
//			cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(cd.getT2DataAscii()));
//			StringBuilder sb = new StringBuilder();
//			cardManager.setEncryptedBlock(sb.append(cd.getT1Encrypted()).append(cd.getT2Encrypted()).toString());
//			cardManager.setEncryptedTrack1(cd.getT1Encrypted());
//			cardManager.setEncryptedTrack2(cd.getT2Encrypted());
//			cardManager.setDeviceSerialNumber(cd.getSerialNumber());
//		}
//		else
//		{
//			if(cd.getT1Data().length()>0&&cd.getT2Data().length()==0&&cd.getT1DataAscii().contains(";"))
//				tracks.append("").append(cd.getT1DataAscii());
//			else if(cd.getT1Data().length()>0&&cd.getT2Data().length()==0&&cd.getT1DataAscii().contains("%"))
//				tracks.append(cd.getT1DataAscii()).append("");
//			else
//				tracks.append(cd.getT1DataAscii()).append(cd.getT2DataAscii());
//			
//			this.cardManager = Global.parseSimpleMSR(activity, tracks.toString());
//		}
		
		this.cardManager = parseCardData(activity,cardData);
		isSwipping = false;
		handler.post(doUpdateViews);
	}

	public static CreditCardInfo parseCardData(Activity activity,byte[] cardData)
	{
		CreditCardInfo cardManager = new CreditCardInfo();
		CardData cd = new CardData(cardData);
		StringBuilder tracks= new StringBuilder();
		
		if(cd.getT1DataAscii()==null&&(cd.getT2DataAscii()==null))
		{
			return cardManager;
		}
		else
		{
			if(cd.isDataEncrypted()&&cd.getT2Encrypted()!=null)
			{
				Encrypt encrypt = new Encrypt(activity);
				cardManager.setTrackDataKSN(cd.getKSN());
				cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(cd.getT1DataAscii()));
				cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(cd.getT2DataAscii()));
				StringBuilder sb = new StringBuilder();
				//cardManager.setEncryptedBlock(sb.append(cd.getT1Encrypted()).append(cd.getT2Encrypted()).toString());
				cardManager.setEncryptedTrack1(cd.getT1Encrypted());
				cardManager.setEncryptedTrack2(cd.getT2Encrypted());
				cardManager.setDeviceSerialNumber(cd.getSerialNumber());
				
				String tr1 = cd.getT1DataAscii()==null?"":cd.getT1DataAscii();
				String tr2 = cd.getT2DataAscii()==null?"":cd.getT2DataAscii();
				CreditCardInfo temp = Global.parseSimpleMSR(activity, tr1.replace("%*", "%").replace("?*", "?")+tr2.replace("?*", "?"));
				cardManager.setCardExpMonth(temp.getCardExpMonth());
				cardManager.setCardExpYear(temp.getCardExpYear());
				cardManager.setCardLast4(temp.getCardLast4());
				cardManager.setCardOwnerName(temp.getCardOwnerName());
				cardManager.setCardType(temp.getCardType());
				cardManager.setCardNumAESEncrypted(temp.getCardNumAESEncrypted());
				
			}
			else
			{
				if(cd.getT1DataAscii()!=null&&cd.getT1Data().length()>0&&cd.getT2Data().length()==0&&cd.getT1DataAscii().contains(";"))
					tracks.append("").append(cd.getT1DataAscii());
				else if(cd.getT1DataAscii()!=null&&cd.getT1Data().length()>0&&(cd.getT2Data()==null||cd.getT2Data().length()==0)
						&&cd.getT1DataAscii().contains("%"))
					tracks.append(cd.getT1DataAscii()).append("");
				else
					tracks.append(cd.getT1DataAscii()==null?"":cd.getT1DataAscii()).append(cd.getT2DataAscii()==null?"":cd.getT2DataAscii());
				
				cardManager = Global.parseSimpleMSR(activity, tracks.toString());
			}
			
			return cardManager;
		}
	}

	public static CreditCardInfo parseCardData(Activity activity,String cardData)
	{
		CreditCardInfo cardManager = new CreditCardInfo();
		StringBuilder hexCard = new StringBuilder();
		
		String sub1 = cardData.substring(0, cardData.indexOf("%"));
		String sub2 = cardData.substring(cardData.indexOf("%"),cardData.lastIndexOf("*")+1);
		String sub3 = cardData.substring(cardData.lastIndexOf("*")+1,cardData.length());
		sub2 = CardData.asciiToHex(sub2);
		hexCard.append(sub1).append(sub2).append(sub3);
		
		
		CardData cd = new CardData(hexCard.toString());
		StringBuilder tracks= new StringBuilder();
		
		if(cd.isDataEncrypted()&&cd.getT2Encrypted()!=null)
		{
			Encrypt encrypt = new Encrypt(activity);
			cardManager.setTrackDataKSN(cd.getKSN());
			cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(cd.getT1DataAscii()));
			cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(cd.getT2DataAscii()));
			StringBuilder sb = new StringBuilder();
			cardManager.setEncryptedBlock(sb.append(cd.getT1Encrypted()).append(cd.getT2Encrypted()).toString());
			cardManager.setEncryptedTrack1(cd.getT1Encrypted());
			cardManager.setEncryptedTrack2(cd.getT2Encrypted());
			cardManager.setDeviceSerialNumber(cd.getSerialNumber());
			
			CreditCardInfo temp = Global.parseSimpleMSR(activity, cd.getT1DataAscii().replace("%*", "%").replace("?*", "?")+cd.getT2DataAscii().replace("?*", "?"));
			cardManager.setCardExpMonth(temp.getCardExpMonth());
			cardManager.setCardExpYear(temp.getCardExpYear());
			cardManager.setCardLast4(temp.getCardLast4());
			cardManager.setCardOwnerName(temp.getCardOwnerName());
			cardManager.setCardType(temp.getCardType());
			cardManager.setCardNumAESEncrypted(temp.getCardNumAESEncrypted());
			
		}
		else
		{
			if(cd.getT1Data().length()>0&&cd.getT2Data().length()==0&&cd.getT1DataAscii().contains(";"))
				tracks.append("").append(cd.getT1DataAscii());
			else if(cd.getT1Data().length()>0&&cd.getT2Data().length()==0&&cd.getT1DataAscii().contains("%"))
				tracks.append(cd.getT1DataAscii()).append("");
			else
				tracks.append(cd.getT1DataAscii()).append(cd.getT2DataAscii());
			
			cardManager = Global.parseSimpleMSR(activity, tracks.toString());
		}
		
		return cardManager;
	}

	@Override
	public void onReceiveMsgCommandResult(int arg0, byte[] arg1) {
		// TODO Auto-generated method stub
	}



	@Override
	public void onReceiveMsgConnected() {
		// TODO Auto-generated method stub
		isConnected = true;
		handler.post(doUpdateDidConnect);
	}



	@Override
	public void onReceiveMsgDisconnected() {
		// TODO Auto-generated method stub
		isConnected = false;
	}



	@Override
	public void onReceiveMsgFailureInfo(int arg0, String arg1) {
		// TODO Auto-generated method stub
	}



	@Override
	@Deprecated
	public void onReceiveMsgSDCardDFailed(String arg0) {
		// TODO Auto-generated method stub
	}



	@Override
	public void onReceiveMsgTimeout(String arg0) {
		// TODO Auto-generated method stub
		isConnected = false;		
	}



	@Override
	public void onReceiveMsgToConnect() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onReceiveMsgProcessingCardData() {
	}



	@Override
	public void onReceiveMsgToSwipeCard() {
		// TODO Auto-generated method stub
		isSwipping = true;
	}



	@Override
	public void onReceiveMsgChallengeResult(int arg0, byte[] arg1) {
		// TODO Auto-generated method stub
	}



	@Override
	public void onReceiveMsgUpdateFirmwareProgress(int arg0) {
		// TODO Auto-generated method stub
	}



	@Override
	public void onReceiveMsgUpdateFirmwareResult(int arg0) {
		// TODO Auto-generated method stub
	}
	
}
