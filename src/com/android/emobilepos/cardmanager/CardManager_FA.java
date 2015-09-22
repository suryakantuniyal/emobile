package com.android.emobilepos.cardmanager;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import protocols.EMSCallBack;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.database.OrderProductsHandler;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.models.Payment;
import com.emobilepos.app.R;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Post;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import drivers.EMSIDTechUSB;
import drivers.EMSMagtekAudioCardReader;
import drivers.EMSRover;
import drivers.EMSUniMagDriver;

public class CardManager_FA extends FragmentActivity implements EMSCallBack,OnClickListener {
	
	public static final int CASE_GIFT = 0 , CASE_LOYALTY = 1, CASE_REWARD = 2;
	public static final int CASE_ACTIVATE = 0,CASE_ADD_BALANCE = 1,CASE_BALANCE_INQUIRY = 2,CASE_MANUAL_ADD = 3;
	private int cardTypeCase,processTypeCase;
	private String PAYMENT_ACTION,LOADING_MSG;
	
	private EMSCallBack msrCallBack;
	private Global global;
	private boolean hasBeenCreated = false,cardWasFound = false;
	private static CheckBox cardSwipe;
	private CreditCardInfo cardInfoManager;
	private EMSUniMagDriver uniMagReader;
	private EMSMagtekAudioCardReader magtekReader;
	private EMSRover roverReader;
	private static boolean cardReaderConnected = false;
	private MyPreferences myPref;
	private EditText fieldCardNum,fieldAmountToAdd;
	private boolean wasReadFromReader = false;
	private static String ourIntentAction = "";
	private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
	private Activity activity;
	private ProgressDialog myProgressDialog;
	private OrderProductsHandler ordProdDB;
	private HashMap<String,String> giftCardMap = new HashMap<String,String>();
	private PaymentsHandler paymentHandlerDB;
	private EMSIDTechUSB _msrUsbSams;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Bundle extras = getIntent().getExtras();
		cardTypeCase = extras.getInt("CARD_TYPE");
		processTypeCase = extras.getInt("PROCESS_TYPE");
		
		paymentHandlerDB = new PaymentsHandler(this);
		ordProdDB = new OrderProductsHandler(this);
		activity = this;
		msrCallBack = (EMSCallBack)this;
		global = (Global)getApplication();
		myPref = new MyPreferences(this);
		Global.isEncryptSwipe = false;
		
		setContentView(R.layout.activate_card_layout);
		
		Button btnProcess = (Button)findViewById(R.id.processButton);
		btnProcess.setOnClickListener(this);
		fieldCardNum = (EditText)findViewById(R.id.fieldCardNumber);
		cardSwipe = (CheckBox)findViewById(R.id.checkboxCardSwipe);
		
		TextView headerTitle = (TextView)findViewById(R.id.HeaderTitle);
		LinearLayout ll = (LinearLayout)findViewById(R.id.placeHolderInfo);
		
		
		switch(processTypeCase)
		{
		case CASE_ACTIVATE:
			headerTitle.setText(getString(R.string.activate));
			switch(cardTypeCase)
			{
			case CASE_GIFT:
				PAYMENT_ACTION = "ActivateGiftCardAction";
				break;
			case CASE_LOYALTY:
				PAYMENT_ACTION = "ActivateLoyaltyCardAction";
				break;
			case CASE_REWARD:
				PAYMENT_ACTION = "ActivateRewardAction";
				break;
			}
			
			LOADING_MSG = "Activating...";
			if(cardTypeCase == CASE_GIFT)
				ll.setVisibility(View.VISIBLE);
			break;
		case CASE_ADD_BALANCE:
			headerTitle.setText(getString(R.string.add_balance));
			PAYMENT_ACTION = "AddValueGiftCardAction";
			switch(cardTypeCase)
			{
			case CASE_GIFT:
				PAYMENT_ACTION = "AddValueGiftCardAction";
				break;
			case CASE_LOYALTY:
				PAYMENT_ACTION = "AddValueLoyaltyCardAction";
				break;
			case CASE_REWARD:
				PAYMENT_ACTION = "AddValueRewardAction";
				break;
			}
			LOADING_MSG = "Adding Balance...";
			if(cardTypeCase == CASE_GIFT)
				ll.setVisibility(View.VISIBLE);
			break;
		case CASE_BALANCE_INQUIRY:
			headerTitle.setText(getString(R.string.balance_inquiry));		
			
			switch(cardTypeCase)
			{
			case CASE_GIFT:
				PAYMENT_ACTION = "BalanceGiftCardAction";
				break;
			case CASE_LOYALTY:
				PAYMENT_ACTION = "BalanceLoyaltyCardAction";
				break;
			case CASE_REWARD:
				PAYMENT_ACTION = "BalanceRewardAction";
				break;
			}
			LOADING_MSG = "Retrieving Balance...";
			break;
		case CASE_MANUAL_ADD:
			headerTitle.setText(getString(R.string.add_balance));
			switch(cardTypeCase)
			{
			case CASE_GIFT:
				PAYMENT_ACTION = "AddValueGiftCardAction";
				break;
			case CASE_LOYALTY:
				PAYMENT_ACTION = "AddValueLoyaltyCardAction";
				break;
			case CASE_REWARD:
				PAYMENT_ACTION = "AddValueRewardAction";
				break;
			}
			
			LOADING_MSG = "Adding Balance...";
			fieldAmountToAdd = (EditText)findViewById(R.id.fieldAmountToAdd);
			fieldAmountToAdd.setVisibility(View.VISIBLE);
			fieldAmountToAdd.addTextChangedListener(Global.amountTextWatcher(fieldAmountToAdd));
			
			break;
		}
		setUpCardReader();
		hasBeenCreated = true;
	}
	
	
	@Override
	public void onResume() {

		if(global.isApplicationSentToBackground(this))
			global.loggedIn = false;
		global.stopActivityTransitionTimer();
		
		if(hasBeenCreated&&!global.loggedIn)
		{
			if(global.getGlobalDlog()!=null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(this);
		}
		super.onResume();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
		boolean isScreenOn = powerManager.isScreenOn();
		if(!isScreenOn)
			global.loggedIn = false;
		global.startActivityTransitionTimer();
	}
	
	
	@Override
	protected void onDestroy() {
		cardReaderConnected = false;

		if(uniMagReader!=null)
			uniMagReader.release();
		else if(magtekReader!=null)
			magtekReader.closeDevice();
		else if(Global.btSwiper!=null&&Global.btSwiper.currentDevice!=null)
			Global.btSwiper.currentDevice.releaseCardReader();
		else if (Global.mainPrinterManager != null&&Global.mainPrinterManager.currentDevice!=null)
			Global.mainPrinterManager.currentDevice.releaseCardReader();
		
		if(_msrUsbSams!=null&&_msrUsbSams.isDeviceOpen())
		{
			_msrUsbSams.CloseTheDevice();
		}
				
		super.onDestroy();
	}
	
	
	@SuppressWarnings("deprecation")
	private void setUpCardReader() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.isWiredHeadsetOn()) {
			String _audio_reader_type = myPref.getPreferencesValue(MyPreferences.pref_audio_card_reader);
			if(_audio_reader_type != null && !_audio_reader_type.isEmpty() && !_audio_reader_type.equals("-1"))
			{
				if(_audio_reader_type.equals(Global.AUDIO_MSR_UNIMAG))
				{
					uniMagReader = new EMSUniMagDriver();
					uniMagReader.initializeReader(activity);
				}
				else if (_audio_reader_type.equals(Global.AUDIO_MSR_MAGTEK))
				{
					magtekReader = new EMSMagtekAudioCardReader(activity);
					new Thread(new Runnable(){
						public void run()
						{
							magtekReader.connectMagtek(true,msrCallBack);
						}
					}).start();
				}
				else if (_audio_reader_type.equals(Global.AUDIO_MSR_ROVER))
				{
					roverReader = new EMSRover();
					roverReader.initializeReader(activity, false);
				}
			}
//			if (!myPref.getPreferences(MyPreferences.pref_use_magtek_card_reader)) {
//				uniMagReader = new EMSUniMagDriver();
//				uniMagReader.initializeReader(this);
//			} else {
//				magtekReader = new EMSMagtekAudioCardReader(this);
//				new Thread(new Runnable() {
//					public void run() {
//						magtekReader.connectMagtek(true,msrCallBack);
//					}
//				}).start();
//			}
		} else {
			int _swiper_type = myPref.swiperType(true, -2);
			int _printer_type = myPref.printerType(true, -2);
			if (_swiper_type != -1 && Global.btSwiper != null && Global.btSwiper.currentDevice != null && !cardReaderConnected) {
				Global.btSwiper.currentDevice.loadCardReader(msrCallBack, false);
			} else if (_printer_type != -1
					&& (_printer_type == Global.STAR || _printer_type == Global.BAMBOO || _printer_type == Global.ZEBRA)) {
				if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null && !cardReaderConnected)
					Global.mainPrinterManager.currentDevice.loadCardReader(msrCallBack, false);
			}
		}
		// }
		if (myPref.isET1(true, false)||myPref.isMC40(true, false)) {
			ourIntentAction = getString(R.string.intentAction5);
			Intent i = getIntent();
			handleDecodeData(i);
			cardSwipe.setChecked(true);
		} else if (myPref.isSam4s(true, false)) {
			cardSwipe.setChecked(true);
			_msrUsbSams = new EMSIDTechUSB(activity,msrCallBack);
			if(_msrUsbSams.OpenDevice())
				_msrUsbSams.StartReadingThread();
		}
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.processButton:
			if(processTypeCase == CASE_MANUAL_ADD)
			{
				if(isValidAmount())
					processInquiry();
				else
					Global.showPrompt(this, R.string.dlog_title_error, getString(R.string.error_wrong_amount));
			}
			else
				processInquiry();
			break;
		}
	}

	
	@Override
	public void onNewIntent(Intent i) 
	{
		super.onNewIntent(i);
		handleDecodeData(i);
	}
	
	
	
	private void handleDecodeData(Intent i) {
		// check the intent action is for us
		if (i.getAction() != null && i.getAction().contentEquals(ourIntentAction)) {

			// get the data from the intent
			String data = i.getStringExtra(DATA_STRING_TAG);
			this.cardInfoManager = Global.parseSimpleMSR(this, data);
			updateViewAfterSwipe();
		}
	}
	
	
	private boolean isValidAmount()
	{
		BigDecimal bd = Global.getBigDecimalNum(fieldAmountToAdd.getText().toString());
		if(bd.compareTo(Global.getBigDecimalNum("0"))==1)
			return true;
		return false;
	}
	
	
	private boolean populateCardInfo() {
		if (!wasReadFromReader) {
			Encrypt encrypt = new Encrypt(activity);
			cardInfoManager = new CreditCardInfo();
			int size = fieldCardNum.getText().toString().length();
			if (size > 4) {
				String last4Digits = (String) fieldCardNum.getText().toString().subSequence(size - 4, size);
				cardInfoManager.setCardLast4(last4Digits);
			}
			cardInfoManager.setCardNumAESEncrypted(encrypt.encryptWithAES(fieldCardNum.getText().toString()));
			cardInfoManager.setCardNumUnencrypted(fieldCardNum.getText().toString());

			switch(processTypeCase)
			{
			case CASE_ACTIVATE:
			case CASE_ADD_BALANCE:
				giftCardMap = ordProdDB.getOrdProdGiftCard(cardInfoManager.getCardNumUnencrypted());
				if (giftCardMap.isEmpty())
					return false;
				else {
					TextView labelAmount = (TextView) findViewById(R.id.labelAmount);
					String temp = giftCardMap.get("overwrite_price") == null ? "-1" : giftCardMap.get("overwrite_price");
					giftCardMap.put("overwrite_price", temp);
					labelAmount.setText(Global.formatDoubleStrToCurrency(temp));
					return true;
				}
			case CASE_BALANCE_INQUIRY:
			case CASE_MANUAL_ADD:
				return true;
			}
		}
		else if((processTypeCase == CASE_ACTIVATE||processTypeCase==CASE_ADD_BALANCE)&&(giftCardMap.isEmpty()||!cardWasFound))
			return false;
		return true;
	}

	
	
	private Payment payment;
	private void processInquiry() 
	{
		if (populateCardInfo()) {
			payment = new Payment(this);

			
			GenerateNewID generator = new GenerateNewID(this);
			String tempPay_id;
			
//			if (paymentHandlerDB.getDBSize() == 0)
//				tempPay_id = generator.generate("",1);
//			else
//				tempPay_id = generator.generate(paymentHandlerDB.getLastPayID(),1);
			
			
			tempPay_id = generator.getNextID();
			payment.pay_id = tempPay_id;
			

			payment.cust_id = myPref.getCustID();
			payment.custidkey = myPref.getCustIDKey();
			payment.emp_id = myPref.getEmpID();	
			

			payment.pay_name = cardInfoManager.getCardOwnerName();
			payment.pay_ccnum = cardInfoManager.getCardNumAESEncrypted();

			payment.ccnum_last4 = cardInfoManager.getCardLast4();
			payment.pay_expmonth = cardInfoManager.getCardExpMonth();
			payment.pay_expyear = cardInfoManager.getCardExpYear();
			payment.pay_seccode = cardInfoManager.getCardEncryptedSecCode();

			payment.track_one = cardInfoManager.getEncryptedAESTrack1();
			payment.track_two = cardInfoManager.getEncryptedAESTrack2();


			
			String cardType = "GiftCard";
			if(cardTypeCase == CASE_LOYALTY)
				cardType = "LoyaltyCard";
			else if(cardTypeCase == CASE_REWARD)
				cardType = "Reward";
			
			payment.paymethod_id = cardType;
			payment.card_type = cardType;
			
			
			payment.pay_type = "0";

			switch (processTypeCase) {
			case CASE_ACTIVATE:
			case CASE_ADD_BALANCE:
				payment.pay_amount = giftCardMap.get("overwrite_price");
				payment.paymethod_id = cardType+"Balance";
				break;
			case CASE_MANUAL_ADD:
				BigDecimal bd = Global.getBigDecimalNum(fieldAmountToAdd.getText().toString());
				payment.paymethod_id = cardType+"Balance";
				payment.pay_amount = bd.toString();
				break;
			}
			
			EMSPayGate_Default payGate = new EMSPayGate_Default(this, payment);
			String generatedURL = new String();
			
			generatedURL = payGate.paymentWithAction(PAYMENT_ACTION, wasReadFromReader, cardType, cardInfoManager);
			

			new processAsync().execute(generatedURL);
		} else {
			Global.showPrompt(activity, R.string.dlog_title_error, "Card has already been processed");
		}
	}

	
	
	private class processAsync extends AsyncTask<String, String, String> {

		private HashMap<String, String> parsedMap = new HashMap<String, String>();
		private String urlToPost;
		private boolean wasProcessed = false;
		private String errorMsg = "Request could not be processed.";

		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage(LOADING_MSG);
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

			if(_msrUsbSams!=null && _msrUsbSams.isDeviceOpen())
			{
				_msrUsbSams.CloseTheDevice();
			}
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			Post httpClient = new Post();

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);
			urlToPost = params[0];

			try {
				
				String xml = httpClient.postData(13, activity, urlToPost);
				
				if (xml.equals(Global.TIME_OUT)) {
					errorMsg = "TIME OUT, would you like to try again?";
				} else if (xml.equals(Global.NOT_VALID_URL)) {
					errorMsg = "Can not proceed...";
				} else {
					InputSource inSource = new InputSource(new StringReader(xml));

					SAXParser sp = spf.newSAXParser();
					XMLReader xr = sp.getXMLReader();
					xr.setContentHandler(handler);
					xr.parse(inSource);
					parsedMap = handler.getData();

					if (parsedMap != null && parsedMap.size() > 0 && parsedMap.get("epayStatusCode").equals("APPROVED"))
						wasProcessed = true;
					else if (parsedMap != null && parsedMap.size() > 0) {
						StringBuilder sb = new StringBuilder();
						sb.append("statusCode = ").append(parsedMap.get("statusCode")).append("\n");
						sb.append(parsedMap.get("statusMessage"));
						errorMsg = sb.toString();
					} else
						errorMsg = xml;
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Tracker tracker = EasyTracker.getInstance(activity);
				tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
			}
			return null;
		}

		
		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss();

			if (wasProcessed) // payment processing succeeded
			{
				StringBuilder sb = new StringBuilder();
				switch(processTypeCase)
				{
				case CASE_ACTIVATE:
				case CASE_ADD_BALANCE:
					ordProdDB.updateOrdProdCardActivated(giftCardMap.get("ordprod_id"));
					break;
				}
				
				if(processTypeCase == CASE_ADD_BALANCE || processTypeCase == CASE_MANUAL_ADD)
				{
					payment.pay_resultcode = parsedMap.get("pay_resultcode");
					payment.pay_resultmessage = parsedMap.get("pay_resultmessage");
					payment.pay_transid = parsedMap.get("CreditCardTransID");
					payment.authcode = parsedMap.get("AuthorizationCode");
					payment.pay_issync = "1";
					paymentHandlerDB.insert(payment);
				}
				
				String temp = (parsedMap.get("CardBalance")==null?"0.0":parsedMap.get("CardBalance"));
				
				if(cardTypeCase==CASE_LOYALTY)
					sb.append("Card Balance: ").append(temp);
				else
					sb.append("Card Balance: ").append(Global.getCurrencyFrmt(temp));

				showBalancePrompt(sb.toString());

			} else // payment processing failed
			{
				Global.showPrompt(activity, R.string.dlog_title_error, errorMsg);
			}
		}
	}
	
	public void showBalancePrompt(String msg) {
		final Dialog dlog = new Dialog(this, R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setContentView(R.layout.dlog_btn_single_layout);
		dlog.setCancelable(false);
		TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);
		viewMsg.setText(msg);
		Button btnOk = (Button) dlog.findViewById(R.id.btnDlogSingle);
		btnOk.setText(R.string.button_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				finish();
			}
		});
		dlog.show();
	}

	
	
	
	private void updateViewAfterSwipe()
	{
		//month.setText(cardInfoManager.getCardExpMonth());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy",Locale.getDefault());
		SimpleDateFormat dt2 = new SimpleDateFormat("yy",Locale.getDefault());
		String formatedYear = new String();
		try 
		{
			Date date = dt2.parse(cardInfoManager.getCardExpYear());
			formatedYear = dt.format(date);
		} 
		catch (ParseException e) 
		{
			// TODO Auto-generated catch block
			Tracker tracker = EasyTracker.getInstance(activity);
			tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
		}
		
		cardInfoManager.setCardExpYear(formatedYear);
		fieldCardNum.setText(cardInfoManager.getCardNumUnencrypted());
		
		
		switch(processTypeCase)
		{
		case CASE_ACTIVATE:
		case CASE_ADD_BALANCE:
			giftCardMap = ordProdDB.getOrdProdGiftCard(cardInfoManager.getCardNumUnencrypted());
			if(!giftCardMap.isEmpty())
				cardWasFound = true;
			TextView labelAmount = (TextView)findViewById(R.id.labelAmount);
			String temp = giftCardMap.get("overwrite_price") == null?"-1":giftCardMap.get("overwrite_price");
			giftCardMap.put("overwrite_price", temp);
			labelAmount.setText(Global.formatDoubleStrToCurrency(temp));
			break;
		}
		
		
		wasReadFromReader = true;
	}
	
	
	@Override
	public void cardWasReadSuccessfully(boolean read, CreditCardInfo cardManager) {
		// TODO Auto-generated method stub
		this.cardInfoManager = cardManager;
		updateViewAfterSwipe();
		if(uniMagReader!=null&&uniMagReader.readerIsConnected())
		{
			uniMagReader.startReading();
		}
		else if(magtekReader==null&&Global.btSwiper==null&&_msrUsbSams==null&&Global.mainPrinterManager!=null)
			Global.mainPrinterManager.currentDevice.loadCardReader(msrCallBack, false);
	}
	
	
	@Override
	public void readerConnectedSuccessfully(boolean didConnect) {
		// TODO Auto-generated method stub
		if(didConnect)
		{
			cardReaderConnected = true;
			if(uniMagReader!=null&&uniMagReader.readerIsConnected())
				uniMagReader.startReading();
			if(!cardSwipe.isChecked())
				cardSwipe.setChecked(true);
		}
		else
		{
			cardReaderConnected = false;
			if(cardSwipe.isChecked())
				cardSwipe.setChecked(false);
		}
	}
	
	@Override
	public void scannerWasRead(String data) {
		// TODO Auto-generated method stub
		
	}
}
