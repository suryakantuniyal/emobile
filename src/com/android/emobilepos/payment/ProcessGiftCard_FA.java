package com.android.emobilepos.payment;

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

import com.android.database.PaymentsHandler;
import com.android.emobilepos.DrawReceiptActivity;
import com.android.emobilepos.models.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Post;
import com.emobilepos.app.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import drivers.EMSIDTechUSB;
import drivers.EMSMagtekAudioCardReader;
import drivers.EMSRover;
import drivers.EMSUniMagDriver;
import protocols.EMSCallBack;

public class ProcessGiftCard_FA extends FragmentActivity implements EMSCallBack, OnClickListener {

	private EditText fieldAmountDue, fieldAmountPaid, fieldCardNum, fieldHidden;
	private Global global;
	private MyPreferences myPref;
	private CreditCardInfo cardInfoManager;
	private static CheckBox cardSwipe, redeemAll;
	private boolean isFromMainMenu = false;
	private boolean isRefund = false;

	private Activity activity;
	private boolean wasReadFromReader = false;

	private static boolean cardReaderConnected = false;
	private EMSUniMagDriver uniMagReader;
	private EMSMagtekAudioCardReader magtekReader;
	private EMSRover roverReader;

	private ProgressDialog myProgressDialog;
	private String inv_id, custidkey;
	private Payment payment;
	private PaymentsHandler payHandler;
	private static String ourIntentAction = "";
	private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
	private boolean hasBeenCreated = false;
	private EMSCallBack callBack;
	private EMSIDTechUSB _msrUsbSams;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		global = (Global) getApplication();
		activity = this;
		callBack = (EMSCallBack) this;
		Global.isEncryptSwipe = true;
		myPref = new MyPreferences(activity);
		setContentView(R.layout.process_giftcard_layout);

		cardInfoManager = new CreditCardInfo();
		cardSwipe = (CheckBox) findViewById(R.id.checkboxCardSwipe);
		redeemAll = (CheckBox) findViewById(R.id.checkboxRedeemAll);
		fieldAmountDue = (EditText) findViewById(R.id.processCardAmount);
		fieldAmountDue.addTextChangedListener(Global.amountTextWatcher(fieldAmountDue));
		fieldAmountDue.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		fieldAmountPaid = (EditText) findViewById(R.id.processCardAmountPaid);
		fieldAmountPaid.addTextChangedListener(Global.amountTextWatcher(fieldAmountPaid));
		fieldCardNum = (EditText) findViewById(R.id.cardNumEdit);
		fieldHidden = (EditText) findViewById(R.id.hiddenField);

		fieldHidden.addTextChangedListener(hiddenTxtWatcher(fieldHidden));
		Button btnExact = (Button) findViewById(R.id.exactAmountBut);
		Button btnProcess = (Button) findViewById(R.id.processButton);
		btnExact.setOnClickListener(this);
		btnProcess.setOnClickListener(this);

		setupHeaderTitle();
		setUpCardReader();
		hasBeenCreated = true;
	}

	@Override
	public void onResume() {

		if (global.isApplicationSentToBackground(this))
			global.loggedIn = false;
		global.stopActivityTransitionTimer();

		if (hasBeenCreated && !global.loggedIn) {
			if (global.getGlobalDlog() != null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(this);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		boolean isScreenOn = powerManager.isScreenOn();
		if (!isScreenOn)
			global.loggedIn = false;
		global.startActivityTransitionTimer();
	}

	@Override
	protected void onDestroy() {
		cardReaderConnected = false;

		if (uniMagReader != null)
			uniMagReader.release();
		else if (magtekReader != null)
			magtekReader.closeDevice();
		else if (Global.btSwiper != null && Global.btSwiper.currentDevice != null)
			Global.btSwiper.currentDevice.releaseCardReader();
		else if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null)
			Global.mainPrinterManager.currentDevice.releaseCardReader();
		if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen()) {
			_msrUsbSams.CloseTheDevice();
		}

		super.onDestroy();
	}

	private void setupHeaderTitle() {
		TextView headerTitle = (TextView) findViewById(R.id.HeaderTitle);
		Bundle extras = getIntent().getExtras();
		if (extras.getBoolean("salespayment")) {
			headerTitle.setText(getString(R.string.card_payment_title));
			isFromMainMenu = TextUtils.isEmpty(extras.getString("amount"))
					|| Double.parseDouble(extras.getString("amount")) == 0;
		} else if (extras.getBoolean("salesreceipt")) {
			headerTitle.setText(getString(R.string.card_payment_title));
		} else if (extras.getBoolean("salesrefund")) {
			isRefund = true;
			isFromMainMenu = TextUtils.isEmpty(extras.getString("amount"))
					|| Double.parseDouble(extras.getString("amount")) == 0;
			headerTitle.setText(getString(R.string.card_refund_title));
		} else if (extras.getBoolean("histinvoices")) {
			headerTitle.setText(getString(R.string.card_payment_title));
		} else if (extras.getBoolean("salesinvoice")) {
			headerTitle.setText("Card Invoice");
		}

		custidkey = extras.getString("custidkey");
		if (custidkey == null)
			custidkey = "";
		inv_id = extras.getString("job_id");

		fieldAmountDue.setText(
				Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));
		if (!isFromMainMenu) {
			fieldAmountDue.setEnabled(false);
		}
	}

	@SuppressWarnings("deprecation")
	private void setUpCardReader() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.isWiredHeadsetOn()) {
			String _audio_reader_type = myPref.getPreferencesValue(MyPreferences.pref_audio_card_reader);
			if (_audio_reader_type != null && !_audio_reader_type.isEmpty() && !_audio_reader_type.equals("-1")) {
				if (_audio_reader_type.equals(Global.AUDIO_MSR_UNIMAG)) {
					uniMagReader = new EMSUniMagDriver();
					uniMagReader.initializeReader(activity);
				} else if (_audio_reader_type.equals(Global.AUDIO_MSR_MAGTEK)) {
					magtekReader = new EMSMagtekAudioCardReader(activity);
					new Thread(new Runnable() {
						public void run() {
							magtekReader.connectMagtek(true, callBack);
						}
					}).start();
				} else if (_audio_reader_type.equals(Global.AUDIO_MSR_ROVER)) {
					roverReader = new EMSRover();
					roverReader.initializeReader(activity, false);
				}
			}
			// if
			// (!myPref.getPreferences(MyPreferences.pref_use_magtek_card_reader))
			// {
			// uniMagReader = new EMSUniMagDriver();
			// uniMagReader.initializeReader(activity);
			// } else {
			// magtekReader = new EMSMagtekAudioCardReader(activity);
			// new Thread(new Runnable() {
			// public void run() {
			// magtekReader.connectMagtek(true,callBack);
			// }
			// }).start();
			// }
		} else {
			int _swiper_type = myPref.swiperType(true, -2);
			int _printer_type = myPref.printerType(true, -2);
			if (_swiper_type != -1 && Global.btSwiper != null && Global.btSwiper.currentDevice != null
					&& !cardReaderConnected) {
				Global.btSwiper.currentDevice.loadCardReader(callBack, false);
			} else if (_printer_type != -1 && Global.deviceHasMSR(_printer_type)) {
				if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null
						&& !cardReaderConnected)
					Global.mainPrinterManager.currentDevice.loadCardReader(callBack, false);
			}
		}
		// }
		if (myPref.isET1(true, false) || myPref.isMC40(true, false)) {
			ourIntentAction = getString(R.string.intentAction2);
			Intent i = getIntent();
			handleDecodeData(i);
			cardSwipe.setChecked(true);
		} else if (myPref.isSam4s(true, false)) {
			cardSwipe.setChecked(true);
			_msrUsbSams = new EMSIDTechUSB(activity, callBack);
			if (_msrUsbSams.OpenDevice())
				_msrUsbSams.StartReadingThread();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// special case for "quit the app"
		if (event.getDevice() == null) {
			return super.dispatchKeyEvent(event);
		}

		// retrieve the input device of current event
		String device_desc = event.getDevice().getName();
		if (device_desc.equals("Sam4s SPT-4000 USB MCR")) {
			if (getCurrentFocus() != fieldHidden) {
				fieldHidden.setText("");
				fieldHidden.setFocusable(true);
				fieldHidden.requestFocus();
			}
		}
		return super.dispatchKeyEvent(event);
	}

	private void populateCardInfo() {
		if (!wasReadFromReader) {
			Encrypt encrypt = new Encrypt(activity);
			int size = fieldCardNum.getText().toString().length();
			if (size > 4) {
				String last4Digits = (String) fieldCardNum.getText().toString().subSequence(size - 4, size);
				cardInfoManager.setCardLast4(last4Digits);
			}
			cardInfoManager.setCardNumAESEncrypted(encrypt.encryptWithAES(fieldCardNum.getText().toString()));
		}
	}

	private void processPayment() {

		populateCardInfo();

		String cardType = "GiftCard";
		Bundle extras = activity.getIntent().getExtras();

		payHandler = new PaymentsHandler(activity);

		payment = new Payment(activity);

		payment.pay_id = extras.getString("pay_id");

		payment.emp_id = myPref.getEmpID();

		if (!extras.getBoolean("histinvoices")) {
			payment.job_id = inv_id;
		} else {
			payment.inv_id = "";
		}

		if (!myPref.getShiftIsOpen())
			payment.clerk_id = myPref.getShiftClerkID();
		else if (myPref.getPreferences(MyPreferences.pref_use_clerks))
			payment.clerk_id = myPref.getClerkID();

		payment.cust_id = extras.getString("cust_id");
		payment.custidkey = custidkey;

		payment.paymethod_id = extras.getString("paymethod_id");

		String amountToBePaid = Double.toString(
				Global.formatNumFromLocale(fieldAmountPaid.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim()));
		Global.amountPaid = amountToBePaid;
		payment.pay_dueamount = Double
				.toString(Double.parseDouble(Global.amountPaid) - Double.parseDouble(amountToBePaid));
		// payment.pay_dueamount = Global.amountPaid;
		payment.pay_amount = Global.amountPaid;
		payment.pay_name = cardInfoManager.getCardOwnerName();

		payment.originalTotalAmount = Global.amountPaid;
		payment.pay_ccnum = cardInfoManager.getCardNumAESEncrypted();

		payment.ccnum_last4 = cardInfoManager.getCardLast4();
		payment.pay_expmonth = cardInfoManager.getCardExpMonth();
		payment.pay_expyear = cardInfoManager.getCardExpYear();
		payment.pay_seccode = cardInfoManager.getCardEncryptedSecCode();

		payment.track_one = cardInfoManager.getEncryptedAESTrack1();
		payment.track_two = cardInfoManager.getEncryptedAESTrack2();

		String[] location = Global.getCurrLocation(activity);
		payment.pay_latitude = location[0];
		payment.pay_longitude = location[1];
		payment.card_type = cardType;

		if (Global.isIvuLoto) {
			payment.IvuLottoNumber = extras.getString("IvuLottoNumber");
			payment.IvuLottoDrawDate = extras.getString("IvuLottoDrawDate");
			payment.IvuLottoQR = Global.base64QRCode(extras.getString("IvuLottoNumber"),
					extras.getString("IvuLottoDrawDate"));

			if (!extras.getString("Tax1_amount").isEmpty()) {
				payment.Tax1_amount = extras.getString("Tax1_amount");
				payment.Tax1_name = extras.getString("Tax1_name");

				payment.Tax2_amount = extras.getString("Tax2_amount");
				payment.Tax2_name = extras.getString("Tax2_name");
			} else {
				BigDecimal tempRate;
				double tempPayAmount = Global.formatNumFromLocale(Global.amountPaid);
				tempRate = new BigDecimal(tempPayAmount * 0.06).setScale(2, BigDecimal.ROUND_UP);
				payment.Tax1_amount = tempRate.toPlainString();
				payment.Tax1_name = "Estatal";

				tempRate = new BigDecimal(tempPayAmount * 0.01).setScale(2, BigDecimal.ROUND_UP);
				payment.Tax2_amount = tempRate.toPlainString();
				payment.Tax2_name = "Municipal";
			}
		}

		if (redeemAll.isChecked())
			cardInfoManager.setRedeemAll("1");
		else
			cardInfoManager.setRedeemAll("0");
		cardInfoManager.setRedeemType("Only");

		EMSPayGate_Default payGate = new EMSPayGate_Default(activity, payment);
		String generatedURL = new String();

		if (!isRefund) {
			payment.pay_type = "0";
			generatedURL = payGate.paymentWithAction("ChargeGiftCardAction", wasReadFromReader, cardType,
					cardInfoManager);

		} else {
			payment.is_refund = "1";
			payment.pay_type = "2";
			generatedURL = payGate.paymentWithAction("ReturnGiftCardAction", wasReadFromReader, cardType,
					cardInfoManager);
		}

		new processLivePaymentAsync().execute(generatedURL);
	}

	private class processLivePaymentAsync extends AsyncTask<String, String, String> {

		private HashMap<String, String> parsedMap = new HashMap<String, String>();
		private String urlToPost;
		private boolean wasProcessed = false;
		private String errorMsg = "Could not process the payment.";

		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage("Processing Payment...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();
			if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen()) {
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
					payment.pay_amount = parsedMap.get("AuthorizedAmount");
					double due = Double.parseDouble(payment.originalTotalAmount)
							- Double.parseDouble(payment.pay_amount);
					payment.pay_dueamount = String.valueOf(due);
					Global.amountPaid = payment.pay_amount;
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
				payment.pay_resultcode = parsedMap.get("pay_resultcode");
				payment.pay_resultmessage = parsedMap.get("pay_resultmessage");
				payment.pay_transid = parsedMap.get("CreditCardTransID");
				payment.authcode = parsedMap.get("AuthorizationCode");

				Intent intent = new Intent(activity, DrawReceiptActivity.class);
				intent.putExtra("isFromPayment", true);

				payHandler.insert(payment);

				StringBuilder sb = new StringBuilder();
				sb.append("Status: ").append(parsedMap.get("epayStatusCode")).append("\n");
				sb.append("Auth. Amount: ")
						.append(parsedMap.get("AuthorizedAmount") == null ? "0.00" : parsedMap.get("AuthorizedAmount"))
						.append("\n");
				sb.append("Card Balance: ").append(Global.getCurrencyFrmt(parsedMap.get("CardBalance"))).append("\n");

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
		dlog.setCancelable(false);
		dlog.setContentView(R.layout.dlog_btn_single_layout);

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

				Intent data = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("originalTotalAmount", payment.originalTotalAmount);

				bundle.putString("pay_dueamount", payment.pay_dueamount);
				bundle.putString("pay_amount", payment.pay_amount);
				Global.amountPaid = payment.pay_amount;
				data.putExtras(bundle);
				setResult(-2, data);
				finish();
			}
		});
		dlog.show();
	}

	private void updateViewAfterSwipe() {
		SimpleDateFormat dt = new SimpleDateFormat("yyyy", Locale.getDefault());
		SimpleDateFormat dt2 = new SimpleDateFormat("yy", Locale.getDefault());
		String formatedYear = new String();
		try {
			Date date = dt2.parse(cardInfoManager.getCardExpYear());
			formatedYear = dt.format(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			Tracker tracker = EasyTracker.getInstance(activity);
			tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
		}

		cardInfoManager.setCardExpYear(formatedYear);
		fieldCardNum.setText(cardInfoManager.getCardNumAESEncrypted());

		wasReadFromReader = true;
	}

	private TextWatcher hiddenTxtWatcher(final EditText hiddenField) {

		TextWatcher tw = new TextWatcher() {
			boolean doneScanning = false;

			@Override
			public void afterTextChanged(Editable s) {
				if (doneScanning) {
					doneScanning = false;
					String data = hiddenField.getText().toString().trim().replace("\n", "");
					hiddenField.setText("");
					cardInfoManager = Global.parseSimpleMSR(activity, data);
					cardInfoManager.setCardType("GiftCard");
					updateViewAfterSwipe();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if (s.toString().contains("\n"))
					doneScanning = true;
			}
		};
		return tw;
	}

	@Override
	public void cardWasReadSuccessfully(boolean read, CreditCardInfo cardManager) {
		// TODO Auto-generated method stub
		this.cardInfoManager = cardManager;
		updateViewAfterSwipe();
		if (uniMagReader != null && uniMagReader.readerIsConnected()) {
			uniMagReader.startReading();
		} else if (magtekReader == null && Global.btSwiper == null && Global.mainPrinterManager != null)
			Global.mainPrinterManager.currentDevice.loadCardReader(callBack, false);
	}

	@Override
	public void readerConnectedSuccessfully(boolean didConnect) {
		// TODO Auto-generated method stub
		if (didConnect) {
			cardReaderConnected = true;
			if (uniMagReader != null && uniMagReader.readerIsConnected())
				uniMagReader.startReading();
			if (!cardSwipe.isChecked())
				cardSwipe.setChecked(true);
		} else {
			cardReaderConnected = false;
			if (cardSwipe.isChecked())
				cardSwipe.setChecked(false);
		}
	}

	@Override
	public void scannerWasRead(String data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewIntent(Intent i) {
		super.onNewIntent(i);
		handleDecodeData(i);
	}

	private void handleDecodeData(Intent i) {
		// check the intent action is for us
		if (i.getAction() != null && i.getAction().contentEquals(ourIntentAction)) {

			// get the data from the intent
			String data = i.getStringExtra(DATA_STRING_TAG);
			this.cardInfoManager = Global.parseSimpleMSR(activity, data);
			updateViewAfterSwipe();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.exactAmountBut:
			fieldAmountPaid.setText(fieldAmountDue.getText().toString());
			break;
		case R.id.processButton:
			if (validatePaymentData())
				processPayment();
			break;
		}
	}

	private boolean validatePaymentData() {
		String errorMsg = activity.getString(R.string.card_validation_error);
		fieldAmountDue.setBackgroundResource(android.R.drawable.edit_text);
		fieldAmountPaid.setBackgroundResource(android.R.drawable.edit_text);
		fieldCardNum.setBackgroundResource(android.R.drawable.edit_text);
		boolean isValid = true;
		if (TextUtils.isEmpty(fieldAmountDue.getText().toString())
				|| Double.parseDouble(fieldAmountDue.getText().toString()) <= 0) {
			isValid = false;
			fieldAmountDue.setBackgroundResource(R.drawable.edittext_wrong_input);
		}

		if (TextUtils.isEmpty(fieldAmountPaid.getText().toString())
				|| Double.parseDouble(fieldAmountPaid.getText().toString()) <= 0) {
			isValid = false;
			fieldAmountPaid.setBackgroundResource(R.drawable.edittext_wrong_input);
			errorMsg = activity.getString(R.string.error_wrong_amount);
		}
		if (TextUtils.isEmpty(fieldCardNum.getText().toString())) {
			isValid = false;
			fieldCardNum.setBackgroundResource(R.drawable.edittext_wrong_input);
		}

		double amountDue = TextUtils.isEmpty(fieldAmountDue.getText().toString()) ? 0
				: Double.parseDouble(fieldAmountDue.getText().toString());
		double amountPaid = TextUtils.isEmpty(fieldAmountPaid.getText().toString()) ? 0
				: Double.parseDouble(fieldAmountPaid.getText().toString());

		if (amountPaid > amountDue) {
			isValid = false;
			fieldAmountPaid.setBackgroundResource(R.drawable.edittext_wrong_input);
			errorMsg = activity.getString(R.string.card_overpaid_error);
		}
		if (!isValid) {
			Global.showPrompt(activity, R.string.validation_failed, errorMsg);
		}
		return isValid;
	}
}
