package com.android.emobilepos;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import protocols.EMSCallBack;

import com.android.database.CustomersHandler;
import com.android.database.InvoicePaymentsHandler;
import com.android.database.PaymentsHandler;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;

import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Payment;
import com.android.support.Post;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import drivers.EMSMagtekAudioCardReader;
import drivers.EMSUniMagDriver;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class ProcessCardMenuActivity extends Activity implements EMSCallBack {

	private static final String CREDITCARD_TYPE_JCB = "JCB",CREDITCARD_TYPE_CUP = "CUP",
			CREDITCARD_TYPE_DISCOVER = "Discover",CREDITCARD_TYPE_VISA = "Visa",CREDITCARD_TYPE_DINERS = "DinersClub",
			CREDITCARD_TYPE_MASTERCARD = "MasterCard",CREDITCARD_TYPE_AMEX = "AmericanExpress";
	
	
	private String creditCardType = "";
	final Context thisContext = this;
	private AlertDialog.Builder dialog;
	private static CheckBox cardSwipe = null;
	private static boolean cardReaderConnected = false;

	private MyPreferences myPref;
	
	public final int MESSAGE_STATE_CHANGE = 1;
	public final int STATE_SHOW_CARDINFO = 9;


	private EditText hiddenField;
	private static EditText month, year, cardNum, ownersName,secCode,zipCode;
	
	
	private Global global;
	private Activity activity;
	private boolean hasBeenCreated = false;
	private ProgressDialog myProgressDialog;
	
	private Payment payment;
	private PaymentsHandler payHandler;
	private InvoicePaymentsHandler invPayHandler;
	private String inv_id;
	private boolean wasReadFromReader = false;
	
	private boolean isFromMainMenu = false;
	private int orientation = 0;
	private int requestCode = 0;
	private boolean isRefund = false;
	private EditText tipAmount,reference,promptTipField;
	private EditText amountField;
	private EditText amountPaidField;
	private EditText phoneNumberField,customerEmailField;
	private EditText authIDField,transIDField;
	
	private String errorMsg;
	private boolean timedOut = false;
	
	private boolean isMultiInvoice = false,isOpenInvoice = false;
	private String[] inv_id_array,txnID_array;
	private double[] balance_array;
	private List<String[]>invPaymentList;
	private EMSUniMagDriver uniMagReader;
	private EMSMagtekAudioCardReader magtekReader;
	private String custidkey = "";
	
	private HashMap<String,String> customerInfo;
	
	
	private float amountToTip = 0,amountToTipFromField = 0;
	private double amountToBePaid = 0,grandTotalAmount = 0,actualAmount = 0;
	
	private TextView dlogCardType;
	private TextView dlogCardExpDate;
	private TextView dlogCardNum;
	private TextView dlogGrandTotal;
	private EMSCallBack callBack;
	private CreditCardInfo cardInfoManager;
	
	private static String ourIntentAction = "";
	private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
	private Bundle extras;
	private Button btnProcess;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		callBack = (EMSCallBack)this;
		setContentView(R.layout.procress_card_layout);

		activity = this;
		global = (Global)getApplication();
		myPref = new MyPreferences(activity);
		Global.isEncryptSwipe = true;
		cardInfoManager = new CreditCardInfo();
		reference = (EditText)findViewById(R.id.referenceNumber);
		TextView headerTitle = (TextView) findViewById(R.id.HeaderTitle);
		cardSwipe = (CheckBox) findViewById(R.id.checkBox1);
		extras = this.getIntent().getExtras();
		
		
		if (extras.getBoolean("salespayment")) {
			headerTitle.setText(getString(R.string.card_payment_title));
			isFromMainMenu = true;
		}
		else if(extras.getBoolean("salesreceipt"))
		{
			headerTitle.setText(getString(R.string.card_payment_title));
		requestCode = Global.FROM_JOB_SALES_RECEIPT;
		}else if (extras.getBoolean("salesrefund")) {
			isRefund = true;
			isFromMainMenu = true;
			headerTitle.setText(getString(R.string.card_refund_title));
		} else if (extras.getBoolean("histinvoices")) {
			headerTitle.setText(getString(R.string.card_payment_title));
			requestCode = Global.FROM_OPEN_INVOICES;
		} else if (extras.getBoolean("salesinvoice")) {
			headerTitle.setText("Card Invoice");
		}

		custidkey = extras.getString("custidkey");
		if(custidkey==null)
			custidkey = "";
		
		hiddenField = (EditText)findViewById(R.id.hiddenField);
		hiddenField.addTextChangedListener(hiddenTxtWatcher(hiddenField));
		zipCode = (EditText)findViewById(R.id.processCardZipCode);
		zipCode.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		month = (EditText) findViewById(R.id.monthEdit);
		month.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		listener(0, month, null);

		year = (EditText) findViewById(R.id.yearEdit);
		year.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		listener(0, year, null);

		
		authIDField = (EditText)findViewById(R.id.cardAuthIDField);
		transIDField = (EditText)findViewById(R.id.cardTransIDField);
		
		
		this.amountField = (EditText) findViewById(R.id.processCardAmount);
		this.amountField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		this.amountField.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));
		actualAmount = Global.formatNumFromLocale(amountField.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		
		
		
		this.amountField.addTextChangedListener(new TextWatcher() 
		{
	        public void afterTextChanged(Editable s) {				
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	        public void onTextChanged(CharSequence s, int start, int before, int count) {parseInputedCurrency(s,R.id.processCardAmount);}
	    });

		
		this.amountField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(v.hasFocus())
				{
					Selection.setSelection(amountField.getText(),amountField.getText().length());
				}
				
			}
		});
		
		if (!isFromMainMenu) {
			amountField.setEnabled(false);
		}
		
		
		
		this.amountPaidField = (EditText) findViewById(R.id.processCardAmountPaid);
		this.amountPaidField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		
		
		this.amountPaidField.addTextChangedListener(new TextWatcher() 
		{
	        public void afterTextChanged(Editable s) {				
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	        public void onTextChanged(CharSequence s, int start, int before, int count) {parseInputedCurrency(s,R.id.processCardAmountPaid);}
	    });

		
		this.amountPaidField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(v.hasFocus())
				{
					Selection.setSelection(amountPaidField.getText(),amountPaidField.getText().length());
				}
				
			}
		});
		this.amountPaidField.setText("");
		
		
		
		Button exactBut = (Button) findViewById(R.id.exactAmountBut);
		exactBut.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				amountToBePaid = Global.formatNumFromLocale(amountField.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
				grandTotalAmount = amountToBePaid+amountToTip;
				amountPaidField.setText(amountField.getText().toString());
				
			}
			
		});
		
		
		

		
		cardNum = (EditText) findViewById(R.id.cardNumEdit);
		cardNum.setInputType(InputType.TYPE_CLASS_NUMBER);
		cardNum.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		cardNum.setTransformationMethod(PasswordTransformationMethod.getInstance());
		
		ownersName = (EditText) findViewById(R.id.nameOnCardEdit);
		ownersName.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		
		
		if (extras.getBoolean("histinvoices"))
		{
			isMultiInvoice = extras.getBoolean("isMultipleInvoice");
			isOpenInvoice = true;
			if(!isMultiInvoice)
				inv_id = extras.getString("inv_id");
			else
			{
				inv_id_array = extras.getStringArray("inv_id_array");
				balance_array = extras.getDoubleArray("balance_array");
				txnID_array = extras.getStringArray("txnID_array");
			}
		}
		else
			inv_id = extras.getString("job_id");

		
		secCode = (EditText)findViewById(R.id.processCardSeccode);
		secCode.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		
		
		

		
		btnProcess = (Button) findViewById(R.id.processButton);
		btnProcess.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub\
				errorMsg = getString(R.string.card_validation_error);
				boolean error = false;
				if (cardNum.getText().toString().isEmpty()||(!wasReadFromReader&&!cardIsValid(cardNum.getText().toString()))) 
				{
					// cardNum.setBackground(getResources().getDrawable(R.drawable.edittext_wrong_input));
					//cardNum.setBackgroundDrawable(getResources().getDrawable(R.drawable.edittext_wrong_input));
					cardNum.setBackgroundResource(R.drawable.edittext_wrong_input);
					error = true;
				} else {
					// cardNum.setBackground(getResources().getDrawable(R.drawable.edittext_border));
					//cardNum.setBackgroundDrawable(getResources().getDrawable(R.drawable.edittext_border));
					cardNum.setBackgroundResource(R.drawable.edittext_border);
				}
				int myMonth = -1;
				int myYear = -1;
				if (!month.getText().toString().isEmpty()) {
					myMonth = Integer.parseInt(month.getText().toString());
				}
				if (!year.getText().toString().isEmpty()) {
					myYear = Integer.parseInt(year.getText().toString());
				}

				int curMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
				int curYear = Calendar.getInstance().get(Calendar.YEAR);
				if (myYear <= curYear) {
					if (myYear < curYear) {

						// year.setBackground(getResources().getDrawable(R.drawable.edittext_wrong_input));
						//year.setBackgroundDrawable(getResources().getDrawable(R.drawable.edittext_wrong_input));
						year.setBackgroundResource(R.drawable.edittext_wrong_input);
						error = true;
					} else {
						// year.setBackground(getResources().getDrawable(R.drawable.edittext_border));
						//year.setBackgroundDrawable(getResources().getDrawable(R.drawable.edittext_border));
						year.setBackgroundResource(R.drawable.edittext_border);
					}
					if ((myMonth < curMonth && myYear != -1) || myMonth < 1 || myMonth > 12 || (myMonth < curMonth && myYear == curYear)) {
						// month.setBackground(getResources().getDrawable(R.drawable.edittext_wrong_input));
						//month.setBackgroundDrawable(getResources().getDrawable(R.drawable.edittext_wrong_input));
						month.setBackgroundResource(R.drawable.edittext_wrong_input);
						error = true;
					} else {
						// month.setBackground(getResources().getDrawable(R.drawable.edittext_border));
						//month.setBackgroundDrawable(getResources().getDrawable(R.drawable.edittext_border));
						month.setBackgroundResource(R.drawable.edittext_border);
					}

				} else {
					if (myMonth <= 0 || myMonth > 12) {
						// month.setBackground(getResources().getDrawable(R.drawable.edittext_wrong_input));
						//month.setBackgroundDrawable(getResources().getDrawable(R.drawable.edittext_wrong_input));
						month.setBackgroundResource(R.drawable.edittext_wrong_input);
						
						error = true;
					} else {
						// month.setBackground(getResources().getDrawable(R.drawable.edittext_border));
						//month.setBackgroundDrawable(getResources().getDrawable(R.drawable.edittext_border));
						month.setBackgroundResource(R.drawable.edittext_border);
					}
				}
				
				if(!isFromMainMenu)
				{
					double enteredAmount = Global.formatNumFromLocale(amountPaidField.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
					double actualAmount = Double.parseDouble(extras.getString("amount"));
					
					if(enteredAmount>actualAmount)
					{
						errorMsg = getString(R.string.card_overpaid_error);
						error = true;
					}
					else if(enteredAmount<=0)
					{
						errorMsg = getString(R.string.error_wrong_amount);
						error = true;
					}
						
				}
				else
				{
					double enteredAmount = Global.formatNumFromLocale(amountPaidField.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
					
					
					if(enteredAmount<=0)
					{
						errorMsg = getString(R.string.error_wrong_amount);
						error = true;
					}
				}

				if (ownersName.getText().toString().isEmpty()) {
					// ownersName.setBackground(getResources().getDrawable(R.drawable.edittext_wrong_input));
					//ownersName.setBackgroundDrawable(getResources().getDrawable(R.drawable.edittext_wrong_input));
					ownersName.setBackgroundResource(R.drawable.edittext_wrong_input);
					error = true;
				} else {
					// ownersName.setBackground(getResources().getDrawable(R.drawable.edittext_border));
					//ownersName.setBackgroundDrawable(getResources().getDrawable(R.drawable.edittext_border));
					ownersName.setBackgroundResource(R.drawable.edittext_border);
				}

				if (error) {
					
					Global.showPrompt(activity, R.string.validation_failed, errorMsg);
				}
				else
				{
					btnProcess.setEnabled(false);
					if(myPref.getPreferences(MyPreferences.pref_show_confirmation_screen))
					{
						promptAmountConfirmation();
					}
					else
					{
						if(!extras.getBoolean("histinvoices")||(isOpenInvoice&&!isMultiInvoice))
							processPayment();
						else
							processMultiInvoicePayment();
					}
					
				}

			}
		});
		
		
		
		Button tipButton = (Button)findViewById(R.id.tipAmountBut);
		tipButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				promptTipConfirmation();
				//promptAmountConfirmation();
			}
		});
		
		
		this.tipAmount = (EditText)findViewById(R.id.processCardTip);
		if(myPref.getPreferences(MyPreferences.pref_show_confirmation_screen))
		{
			this.tipAmount.setVisibility(View.GONE);
			tipButton.setVisibility(View.GONE);
			//findViewById(R.id.tipLabel).setVisibility(View.GONE);
		}
		else
		{
			this.tipAmount.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
			this.tipAmount.setText(Global.formatDoubleToCurrency(0.00));
			this.tipAmount.addTextChangedListener(new TextWatcher() 
			{
		        public void afterTextChanged(Editable s) {}
		        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		        public void onTextChanged(CharSequence s, int start, int before, int count) {parseInputedCurrency(s,R.id.processCardTip);}
		    });
			
			
			this.tipAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					if(v.hasFocus())
					{
						int lent = tipAmount.getText().length();
						Selection.setSelection(tipAmount.getText(), lent);
					}
				}
			});
		}
		
		
		phoneNumberField = (EditText) findViewById(R.id.processCardPhone);
		customerEmailField = (EditText)findViewById(R.id.processCardEmail);
		
		
		if(!Global.getValidString(extras.getString("cust_id")).isEmpty())
		{
			CustomersHandler handler2 = new CustomersHandler(activity);
			customerInfo = handler2.getCustomerMap(extras.getString("cust_id"));
			if(customerInfo!=null)
			{
				if(!customerInfo.get("cust_name").isEmpty())
					ownersName.setText(customerInfo.get("cust_name"));
				if(!customerInfo.get("cust_phone").isEmpty())
					phoneNumberField.setText(customerInfo.get("cust_phone"));
				if(!customerInfo.get("cust_email").isEmpty())
					customerEmailField.setText(customerInfo.get("cust_email"));
			}
		}
		
		
		
		hasBeenCreated = true;
		setUpCardReader();
	}

	
	
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //special case for "quit the app"
        if(event.getDevice()==null){ 
            return super.dispatchKeyEvent(event);
        }

        //retrieve the input device of current event
        String device_desc=event.getDevice().getName();
        if(device_desc.equals("Sam4s SPT-4000 USB MCR")){
            if(getCurrentFocus()!= hiddenField){
                hiddenField.setText("");
                hiddenField.setFocusable(true);
                hiddenField.requestFocus();
            }
            
        }
        return super.dispatchKeyEvent(event);
    }
	
	
	
	private TextWatcher hiddenTxtWatcher(final EditText hiddenField) {

		TextWatcher tw = new TextWatcher() {
			boolean doneScanning = false;
			String temp;

			@Override
			public void afterTextChanged(Editable s) {
				if (doneScanning) {
					doneScanning = false;
					String data = hiddenField.getText().toString().replace("\n", "");
					hiddenField.setText("");
					cardInfoManager = Global.parseSimpleMSR(activity, data);
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
				temp = s.toString();
				if (temp.contains("\n")&&temp.split("\n").length>=2&&temp.substring(temp.length()-1).contains("\n"))
				{
					doneScanning = true;
				}
					
			}
		};
		return tw;
	}
	
	@SuppressWarnings("deprecation")
	private void setUpCardReader()
	{
			AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			if(audioManager.isWiredHeadsetOn())
			{
				if(!myPref.getPreferences(MyPreferences.pref_use_magtek_card_reader))
				{
					uniMagReader = new EMSUniMagDriver();
					uniMagReader.initializeReader(activity);
				}
				else
				{
					magtekReader = new EMSMagtekAudioCardReader(activity);
					new Thread(new Runnable(){
						public void run()
						{
							magtekReader.connectMagtek(true,callBack);
						}
					}).start();
				}
			}
			else
			{
				int _swiper_type = myPref.swiperType(true, -2);
				int _printer_type = myPref.printerType(true, -2);
				if(_swiper_type!=-1&&Global.btSwiper!=null&&Global.btSwiper.currentDevice!=null&&!cardReaderConnected)
				{
						Global.btSwiper.currentDevice.loadCardReader(callBack);				
				}
				else if(_printer_type!=-1&&Global.deviceHasMSR(_printer_type))
				{
					if (Global.mainPrinterManager != null&&Global.mainPrinterManager.currentDevice!=null&&!cardReaderConnected)
						Global.mainPrinterManager.currentDevice.loadCardReader(callBack);
				}
			}
			
			
			if(myPref.isET1(true, false)||myPref.isMC40(true, false))
			{
				ourIntentAction = getString(R.string.intentAction3);
				Intent i = getIntent();
				handleDecodeData(i);
				cardSwipe.setChecked(true);
			}
			else if(myPref.isSam4s(true, false))
			{
				cardSwipe.setChecked(true);
			}
	}
	
	
	private void populateCardInfo()
	{
		if(!wasReadFromReader)
		{
			Encrypt encrypt = new Encrypt(activity);
			int size = cardNum.getText().toString().length();
			String last4Digits = "";
			if(size>4)
				last4Digits = (String) cardNum.getText().toString().subSequence(size-4, size);
			cardInfoManager.setCardExpMonth(month.getText().toString());
			cardInfoManager.setCardExpYear(year.getText().toString());
			cardInfoManager.setCardLast4(last4Digits);
			cardInfoManager.setCardOwnerName(ownersName.getText().toString());
			//cardInfoManager.setCardEncryptedNum(encrypt.encryptWithAES(cardNum.getText().toString()));
			cardInfoManager.setCardNumAESEncrypted(encrypt.encryptWithAES(cardNum.getText().toString()));
			cardInfoManager.setCardEncryptedSecCode(encrypt.encryptWithAES(secCode.getText().toString()));
			
		}
	}
	
	
	
	private void processPayment()
	{
		
		populateCardInfo();
		
		
		

		payHandler = new PaymentsHandler(activity);
		
		payment = new Payment(activity);

		payment.getSetData("pay_id", false, extras.getString("pay_id"));
		
		payment.getSetData("emp_id", false, myPref.getEmpID());
		
		if (!extras.getBoolean("histinvoices"))
		{
			payment.getSetData("job_id", false, inv_id);
		}
		else
		{
			payment.getSetData("inv_id", false, inv_id);
		}
		
		
		if(!myPref.getShiftIsOpen())
			payment.getSetData("clerk_id", false, myPref.getShiftClerkID());
		else if(myPref.getPreferences(MyPreferences.pref_use_clerks))
			payment.getSetData("clerk_id", false, myPref.getClerkID());
		
		payment.getSetData("cust_id", false, extras.getString("cust_id"));
		payment.getSetData("custidkey", false, custidkey);
		
		
		payment.getSetData("ref_num", false, reference.getText().toString());
		payment.getSetData("paymethod_id", false, extras.getString("paymethod_id"));
		
		Global.amountPaid= Double.toString(amountToBePaid);
		
		if((amountToBePaid - actualAmount)>0)
		{
			payment.getSetData("pay_dueamount", false, Double.toString(actualAmount));
		}
		else
		{
			payment.getSetData("pay_dueamount", false, Double.toString(amountToBePaid));
		}
		
		
		payment.getSetData("pay_amount", false, Double.toString(amountToBePaid));
		payment.getSetData("pay_name", false, cardInfoManager.getCardOwnerName());
		
		payment.getSetData("pay_phone", false, phoneNumberField.getText().toString());
		payment.getSetData("pay_email", false, customerEmailField.getText().toString());
		
		payment.getSetData("pay_ccnum", false,cardInfoManager.getCardNumAESEncrypted());
		
		
		payment.getSetData("ccnum_last4", false,cardInfoManager.getCardLast4() );
		payment.getSetData("pay_expmonth", false,cardInfoManager.getCardExpMonth());
		payment.getSetData("pay_expyear", false,cardInfoManager.getCardExpYear() );
		payment.getSetData("pay_poscode", false, zipCode.getText().toString());
		
		
		payment.getSetData("pay_seccode", false, cardInfoManager.getCardEncryptedSecCode());
		
		
		//String tempPaid = Double.toString(Global.formatNumFromLocale(tipAmount.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim()));
		Global.tipPaid = Double.toString(amountToTip);
		payment.getSetData("pay_tip", false, Global.tipPaid);
		payment.getSetData("track_one", false, cardInfoManager.getEncryptedAESTrack1());
		payment.getSetData("track_two", false, cardInfoManager.getEncryptedAESTrack2());
		
		String[] location = Global.getCurrLocation(activity);
		payment.getSetData("pay_latitude", false, location[0]);
		payment.getSetData("pay_longitude", false, location[1]);
		payment.getSetData("card_type", false, creditCardType);
		
		
		
		if(Global.isIvuLoto)
		{
			payment.getSetData("IvuLottoNumber", false, extras.getString("IvuLottoNumber"));
			payment.getSetData("IvuLottoDrawDate", false, extras.getString("IvuLottoDrawDate"));
			payment.getSetData("IvuLottoQR", false, Global.base64QRCode(extras.getString("IvuLottoNumber"),extras.getString("IvuLottoDrawDate")));
			
			
			if(!extras.getString("Tax1_amount").isEmpty())
			{
				payment.getSetData("Tax1_amount", false, extras.getString("Tax1_amount"));
				payment.getSetData("Tax1_name", false, extras.getString("Tax1_name"));
				
				payment.getSetData("Tax2_amount", false, extras.getString("Tax2_amount"));
				payment.getSetData("Tax2_name", false, extras.getString("Tax2_name"));
			}
			else
			{
				BigDecimal tempRate;
				double tempPayAmount = Global.formatNumFromLocale(Global.amountPaid);
				tempRate = new BigDecimal(tempPayAmount*0.06).setScale(2, BigDecimal.ROUND_UP);
				payment.getSetData("Tax1_amount",false,tempRate.toPlainString());
				payment.getSetData("Tax1_name",false, "Estatal");
				
				tempRate = new BigDecimal(tempPayAmount*0.01).setScale(2, BigDecimal.ROUND_UP);
				payment.getSetData("Tax2_amount",false,tempRate.toPlainString());
				payment.getSetData("Tax2_name",false, "Municipal");
			}
		}
		
		
		
		
		
		
		EMSPayGate_Default payGate = new EMSPayGate_Default(activity,payment);
		String generatedURL = new String();
		
		if(!isRefund)
		{
			payment.getSetData("pay_type", false, "0");
			generatedURL = payGate.paymentWithAction("ChargeCreditCardAction", wasReadFromReader,creditCardType,cardInfoManager);
			
		}
		else
		{
			payment.getSetData("is_refund", false, "1");
			payment.getSetData("pay_type", false, "2");
			payment.getSetData("pay_transid",false, transIDField.getText().toString());
			payment.getSetData("authcode", false,authIDField.getText().toString() );
			
			generatedURL = payGate.paymentWithAction("ReturnCreditCardAction", wasReadFromReader,creditCardType,cardInfoManager);
		}
		
		new processLivePaymentAsync().execute(generatedURL);
	}
	

	
	
	private void processMultiInvoicePayment()
	{
		populateCardInfo();
		invPayHandler = new InvoicePaymentsHandler(activity);
		List<Double>appliedAmount = new ArrayList<Double>();
		invPaymentList = new ArrayList<String[]>();
		String[]content = new String[4];
		
		int size = inv_id_array.length;
		String payID = extras.getString("pay_id");
		
		double value = 0;
		
		for(int i = 0 ; i <size;i++)
		{
			value = invPayHandler.getTotalPaidAmount(inv_id_array[i]);
			if(value!=-1)
			{
				if(balance_array[i]>=value)
					balance_array[i]-=value;
				else
					balance_array[i] = 0.0;
			}
			
		}
		
		double tempPaid = Global.formatNumFromLocale(amountPaidField.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		Global.amountPaid = Double.toString(tempPaid);
		boolean endBreak = false;
		for(int i = 0 ; i < size;i++)
		{
			if(balance_array[i]>0)
			{
				if(tempPaid>=balance_array[i])
				{
					content[2] = Double.toString(balance_array[i]);
					appliedAmount.add(balance_array[i]);
					tempPaid-=balance_array[i];
				}
				else
				{
					content[2] = Double.toString(tempPaid);
					endBreak = true;
				}
				
				content[0] = payID;
				content[1] = inv_id_array[i];
				content[3] = txnID_array[i];
				invPaymentList.add(content);
				content = new String[4];
				if(endBreak)
					break;
			}
		}
		
		//if(contentList.size()>0)
			//invHandler.insert(contentList);
		
		
		//MyPreferences myPref = new MyPreferences(activity);
		

		payHandler = new PaymentsHandler(activity);
		

		payment = new Payment(activity);

		payment.getSetData("pay_id", false, extras.getString("pay_id"));
		payment.getSetData("cust_id", false, extras.getString("cust_id"));
		payment.getSetData("custidkey", false, custidkey);
		payment.getSetData("emp_id", false, myPref.getEmpID());	
		
		
		if(!myPref.getShiftIsOpen())
			payment.getSetData("clerk_id", false, myPref.getShiftClerkID());
		else if(myPref.getPreferences(MyPreferences.pref_use_clerks))
			payment.getSetData("clerk_id", false, myPref.getClerkID());
		
		
		payment.getSetData("ref_num", false, reference.getText().toString());
		payment.getSetData("paymethod_id", false, extras.getString("paymethod_id"));
		
		//String tempPaid = Double.toString(Global.formatNumFromLocale(amountField.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim()));
		payment.getSetData("pay_dueamount", false, extras.getString("amount"));
		payment.getSetData("pay_amount", false, Double.toString(amountToBePaid));
		payment.getSetData("pay_name", false,cardInfoManager.getCardOwnerName());
		
		payment.getSetData("pay_phone", false, phoneNumberField.getText().toString());
		payment.getSetData("pay_email", false, customerEmailField.getText().toString());
		
		payment.getSetData("pay_ccnum", false,cardInfoManager.getCardNumAESEncrypted());
		
		payment.getSetData("ccnum_last4", false,cardInfoManager.getCardLast4() );
		payment.getSetData("pay_expmonth", false,cardInfoManager.getCardExpMonth() );
		payment.getSetData("pay_expyear", false,cardInfoManager.getCardExpYear() );
		payment.getSetData("pay_poscode", false, zipCode.getText().toString());
		
		/*if(secCode.getText().toString().trim().isEmpty())
			payment.getSetData("pay_seccode", false, "");
		else
			payment.getSetData("pay_seccode", false, );*/
		payment.getSetData("pay_seccode", false, cardInfoManager.getCardEncryptedSecCode());
		
		//tempPaid = Global.formatNumFromLocale(tipAmount.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		Global.tipPaid = Double.toString(amountToTip);
		payment.getSetData("pay_tip", false,Global.tipPaid);
		payment.getSetData("track_one", false, cardInfoManager.getEncryptedAESTrack1());
		payment.getSetData("track_two", false, cardInfoManager.getEncryptedAESTrack2());
		payment.getSetData("card_type", false, creditCardType);
		
		String[] location = Global.getCurrLocation(activity);
		payment.getSetData("pay_latitude", false, location[0]);
		payment.getSetData("pay_longitude", false, location[1]);
		
		
		if(Global.isIvuLoto)
		{
			payment.getSetData("IvuLottoNumber", false, extras.getString("IvuLottoNumber"));
			payment.getSetData("IvuLottoDrawDate", false, extras.getString("IvuLottoDrawDate"));
			payment.getSetData("IvuLottoQR", false, Global.base64QRCode(extras.getString("IvuLottoNumber"),extras.getString("IvuLottoDrawDate")));
			
			
			if(!extras.getString("Tax1_amount").isEmpty())
			{
				payment.getSetData("Tax1_amount", false, extras.getString("Tax1_amount"));
				payment.getSetData("Tax1_name", false, extras.getString("Tax1_name"));
				
				payment.getSetData("Tax2_amount", false, extras.getString("Tax2_amount"));
				payment.getSetData("Tax2_name", false, extras.getString("Tax2_name"));
			}
			else
			{
				BigDecimal tempRate;
				double tempPayAmount = Global.formatNumFromLocale(Global.amountPaid);
				tempRate = new BigDecimal(tempPayAmount*0.06).setScale(2, BigDecimal.ROUND_UP);
				payment.getSetData("Tax1_amount",false,tempRate.toPlainString());
				payment.getSetData("Tax1_name",false, "Estatal");
				
				tempRate = new BigDecimal(tempPayAmount*0.01).setScale(2, BigDecimal.ROUND_UP);
				payment.getSetData("Tax2_amount",false,tempRate.toPlainString());
				payment.getSetData("Tax2_name",false, "Municipal");
			}
		}
		
		
		
		
		EMSPayGate_Default payGate = new EMSPayGate_Default(activity,payment);
		String generatedURL = new String();
		//generatedURL = payGate.paymentWithAction("ChargeCreditCardAction", wasReadFromReader,cardType(cardNum.getText().toString()));
		
		if(!isRefund)
		{
			payment.getSetData("pay_type", false, "0");
			generatedURL = payGate.paymentWithAction("ChargeCreditCardAction", wasReadFromReader,creditCardType,cardInfoManager);
			/*if(wasReadFromReader)
				generatedURL = payGate.defaultPaymentWithAction("ChargeCreditCardAction","1");
			else
				generatedURL = payGate.defaultPaymentWithAction("ChargeCreditCardAction","0");*/
		}
		else
		{
			payment.getSetData("is_refund", false, "1");
			payment.getSetData("pay_type", false, "2");
			payment.getSetData("pay_transid",false, authIDField.getText().toString());
			payment.getSetData("authcode", false, transIDField.getText().toString());
			generatedURL = payGate.paymentWithAction("ReturnCreditCardAction", wasReadFromReader,creditCardType,cardInfoManager);
			
			/*if(wasReadFromReader)
				generatedURL = payGate.defaultPaymentWithAction("ReturnCreditCardAction","1");
			else
				generatedURL = payGate.defaultPaymentWithAction("ReturnCreditCardAction","0");*/
		}
		
		new processLivePaymentAsync().execute(generatedURL);
		
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
		
		if (dialog != null)
			dialog.create().dismiss();
		
		
		if(uniMagReader!=null)
			uniMagReader.release();
		else if(magtekReader!=null)
			magtekReader.closeDevice();
		else if(Global.btSwiper!=null&&Global.btSwiper.currentDevice!=null)
			Global.btSwiper.currentDevice.releaseCardReader();
		else if (Global.mainPrinterManager != null&&Global.mainPrinterManager.currentDevice!=null)
			Global.mainPrinterManager.currentDevice.releaseCardReader();
		
		
		super.onDestroy();
	}
	
	
	
	private void promptTipConfirmation()
	{
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dialogLayout = inflater.inflate(R.layout.tip_dialog_layout, null);
		

		//****Method that works with both jelly bean/gingerbread
		//AlertDialog.Builder dialog = new AlertDialog.Builder(this,R.style.TransparentDialog);
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final AlertDialog dialog = builder.create();
		dialog.setView(dialogLayout,0,0,0,0);
		dialog.setInverseBackgroundForced(true);
		dialog.setCancelable(false);
		//*****Method that works only with gingerbread and removes background
		/*final Dialog dialog = new Dialog(activity,R.style.TransparentDialog);
		dialog.setContentView(dialogLayout);*/
		
		amountToBePaid = Global.formatNumFromLocale(amountPaidField.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		grandTotalAmount = amountToBePaid + amountToTip;
		
		Button tenPercent = (Button) dialogLayout.findViewById(R.id.tenPercent);
		Button fifteenPercent = (Button) dialogLayout.findViewById(R.id.fifteenPercent);
		Button twentyPercent = (Button) dialogLayout.findViewById(R.id.twentyPercent);
		dlogGrandTotal = (TextView) dialogLayout.findViewById(R.id.grandTotalView);
		
		dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
		
		
		promptTipField = (EditText)dialogLayout.findViewById(R.id.otherTipAmountField);
		promptTipField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		promptTipField.clearFocus();
		promptTipField.setText("");
		
		Button cancelTip = (Button)dialogLayout.findViewById(R.id.cancelTipButton);
		Button saveTip = (Button)dialogLayout.findViewById(R.id.acceptTipButton);
		Button noneButton = (Button)dialogLayout.findViewById(R.id.noneButton);
		
		
		
		
		promptTipField.addTextChangedListener(new TextWatcher() 
		{
	        public void afterTextChanged(Editable s) {				
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	        public void onTextChanged(CharSequence s, int start, int before, int count) {parseInputedCurrency(s,R.id.otherTipAmountField);}
	    });

		
		promptTipField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(v.hasFocus())
				{
					Selection.setSelection(promptTipField.getText(),promptTipField.getText().length());
				}
				
			}
		});

		tenPercent.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				amountToTip = (float)(amountToBePaid*0.1);
				grandTotalAmount = amountToBePaid+amountToTip;
				dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
				promptTipField.setText("");
			}
		});

		fifteenPercent.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				amountToTip = (float)(amountToBePaid*0.15);
				grandTotalAmount = amountToBePaid+amountToTip;
				dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
				promptTipField.setText("");
			}
		});
		
		twentyPercent.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				amountToTip = (float)(amountToBePaid*0.2);
				grandTotalAmount = amountToBePaid+amountToTip;
				dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
				promptTipField.setText("");
			}
		});
		
		
		noneButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				amountToTip = 0;
				grandTotalAmount = amountToBePaid;
				dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
				//dialog.dismiss();
			}
		});
		
		
		cancelTip.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				amountToTip = 0;
				grandTotalAmount = amountToBePaid;
				dialog.dismiss();
			}
		});
		
		saveTip.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(myPref.getPreferences(MyPreferences.pref_show_confirmation_screen))
				{
					dialog.dismiss();
					
					if(!extras.getBoolean("histinvoices")||(isOpenInvoice&&!isMultiInvoice))
						processPayment();
					else
						processMultiInvoicePayment();
				}
				else
				{
					if(tipAmount!=null)
						tipAmount.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(Double.toString((double)amountToTip)))));
					dialog.dismiss();
				}
				
			}
		});
		dialog.show();
	}
	
	
	private void promptAmountConfirmation()
	{
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dialogLayout = inflater.inflate(R.layout.confirmation_amount_layout, null);
		

		//****Method that works with both jelly bean/gingerbread
		//AlertDialog.Builder dialog = new AlertDialog.Builder(this,R.style.TransparentDialog);
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final AlertDialog dialog = builder.create();
		dialog.setView(dialogLayout,0,0,0,0);
		dialog.setInverseBackgroundForced(true);
		dialog.setCancelable(false);
		//*****Method that works only with gingerbread and removes background
		/*final Dialog dialog = new Dialog(activity,R.style.TransparentDialog);
		dialog.setContentView(dialogLayout);*/
		

		dlogGrandTotal = (TextView) dialogLayout.findViewById(R.id.confirmTotalView);
		dlogCardType = (TextView)dialogLayout.findViewById(R.id.confirmCardType);
		dlogCardExpDate = (TextView)dialogLayout.findViewById(R.id.confirmExpDate);
		dlogCardNum = (TextView)dialogLayout.findViewById(R.id.confirmCardNumber);
		
		grandTotalAmount = amountToBePaid;
		dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
		dlogCardType.setText(creditCardType);
		int size = cardNum.getText().toString().length();
		String last4Digits = "";
		if(size >0)
			last4Digits = (String) cardNum.getText().toString().subSequence(size-4, size);
		dlogCardNum.setText("*"+last4Digits);
		dlogCardExpDate.setText(month.getText().toString()+"/"+year.getText().toString());
		
		
		Button cancelButton = (Button)dialogLayout.findViewById(R.id.cancelButton);
		Button nextButton = (Button)dialogLayout.findViewById(R.id.nextButton);
		
		
		
		
		
		
		
		cancelButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		
		nextButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				promptTipConfirmation();
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	
	
	private void parseInputedCurrency(CharSequence s,int type)
	{
    	DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
        DecimalFormatSymbols sym = format.getDecimalFormatSymbols();
    	StringBuilder sb = new StringBuilder();
    	sb.append("^\\").append(sym.getCurrencySymbol()).append("\\s(\\d{1,3}(\\").append(sym.getGroupingSeparator()).append("\\d{3})*|(\\d+))(");
    	sb.append(sym.getDecimalSeparator()).append("\\d{2})?$");
    	
        if(!s.toString().matches(sb.toString()))
        {
            String userInput= ""+s.toString().replaceAll("[^\\d]", "");
            StringBuilder cashAmountBuilder = new StringBuilder(userInput);

            while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0') {
                cashAmountBuilder.deleteCharAt(0);
            }
            while (cashAmountBuilder.length() < 3) {
                cashAmountBuilder.insert(0, '0');
            }

            cashAmountBuilder.insert(cashAmountBuilder.length()-2, sym.getDecimalSeparator());
            cashAmountBuilder.insert(0, sym.getCurrencySymbol()+" ");
            switch(type)
            {

            case R.id.processCardAmount:
            	this.amountField.setText(cashAmountBuilder.toString());
            	break;
            case R.id.processCardAmountPaid:
            	amountPaidField.setText(cashAmountBuilder);
            	amountToBePaid = (float)(Global.formatNumFromLocale(cashAmountBuilder.toString().replaceAll("[^\\d\\,\\.]", "").trim()));
            	grandTotalAmount = amountToBePaid + amountToTip;
            	break;
            case R.id.processCardTip:
            	this.tipAmount.setText(cashAmountBuilder.toString());
            	amountToTip = (float)(Global.formatNumFromLocale(cashAmountBuilder.toString().replaceAll("[^\\d\\,\\.]", "").trim()));
            	grandTotalAmount = amountToBePaid + amountToTip;
            	break;
            case R.id.otherTipAmountField:
            	this.promptTipField.setText(cashAmountBuilder);
            	amountToTipFromField = (float)(Global.formatNumFromLocale(cashAmountBuilder.toString().replaceAll("[^\\d\\,\\.]", "").trim()));
            	if(amountToTipFromField>0)
            	{
            		amountToTip = amountToTipFromField;
	            	grandTotalAmount = amountToBePaid + amountToTip;
	            	dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
            	}
            	break;
            
            }
        }
        
            // keeps the cursor always to the right
        	switch(type)
        	{

        	case R.id.processCardAmount:
        		Selection.setSelection(this.amountField.getText(), this.amountField.getText().length());
        		break;
        	case R.id.processCardAmountPaid:
        		Selection.setSelection(this.amountPaidField.getText(), this.amountPaidField.getText().length());
        		break;
        	case R.id.processCardTip:
        		Selection.setSelection(this.tipAmount.getText(), this.tipAmount.getText().length());
        		break;
        	case R.id.otherTipAmountField:	//Add gratuity prompt
        		Selection.setSelection(this.promptTipField.getText(), this.promptTipField.getText().length());
        		break;
        	}
	}
	
	
	
	public void listener(int cases, final EditText x, Button but) {

		switch (cases) 
		{
			case 0: 
			{
				x.setOnFocusChangeListener(new View.OnFocusChangeListener() 
				{
	
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						// TODO Auto-generated method stub
						if (hasFocus) {
							x.setGravity(Gravity.LEFT);	
						} else {
							x.setGravity(Gravity.CENTER);
						}
	
					}
				});
				break;
			}
		}
	}

	
	private boolean cardIsValid(String number)
	{
		creditCardType = cardType (number);
		if(creditCardType.isEmpty())
			return false;
		else if(creditCardType.equals("CUP"))
		{
			creditCardType = CREDITCARD_TYPE_DISCOVER;
			return true;
		}
		else
		{
			return luhnTest(number);
		}
	}
	
	
	
	// For validating the credit card number
	private boolean luhnTest(String number) {
		int s1 = 0, s2 = 0;
		String reverse = new StringBuffer(number).reverse().toString();
		for (int i = 0; i < reverse.length(); i++) {
			int digit = Character.digit(reverse.charAt(i), 10);
			if (i % 2 == 0) {// this is for odd digits, they are 1-indexed in
								// the algorithm
				s1 += digit;
			} else {// add 2 * digit for 0-4, add 2 * digit - 9 for 5-9
				s2 += 2 * digit;
				if (digit >= 5) {
					s2 -= 9;
				}
			}
		}
		if ((s1 + s2) % 10 == 0) {
			return true;
		}

		return false;
	}

	
	
	public static String cardType(String number) 
	{
		String ccType = "";
		long cardNumber = 0;
		try
		{
		cardNumber = Long.parseLong(number);
		}catch(NumberFormatException e)
		{
			return ccType ="";
		}
		    if (cardNumber>=14&&Integer.parseInt(number.substring(0, 6))>=622126  && Integer.parseInt(number.substring(0, 6))<=622925) {
		        ccType = CREDITCARD_TYPE_CUP;
		    }else if (cardNumber>=14&&Integer.parseInt(number.substring(0, 6))==564182 || Integer.parseInt(number.substring(0, 6))==633110) {
		        ccType = CREDITCARD_TYPE_DISCOVER;
		    }else{
		        switch (Integer.parseInt(number.substring(0, 4))) {
		            case 2014:
		            case 2149:
		                ccType = CREDITCARD_TYPE_DINERS;
		                break;
		            case 2131:
		            case 1800:
		            case 3528:
		            case 3529:
		                ccType = CREDITCARD_TYPE_JCB;
		                break;
		            case 6011:
		                ccType = CREDITCARD_TYPE_DISCOVER;
		                break;
		            case 3095:
		                ccType = CREDITCARD_TYPE_DINERS;
		                break;
		            case 6222:
		            case 6223:
		            case 6224:
		            case 6225:
		            case 6226:
		            case 6227:
		            case 6228:
		            case 6282:
		            case 6283:
		            case 6284:
		            case 6285:
		            case 6286:
		            case 6287:
		            case 6288:
		                ccType = CREDITCARD_TYPE_CUP;
		                break;
		            case 5018:
		            case 5020:
		            case 5038:
		            case 6304:
		            case 6759:
		            case 6761:
		            case 6763:
		                ccType = CREDITCARD_TYPE_MASTERCARD;
		                break;
		            case 6333:
		                ccType = CREDITCARD_TYPE_VISA;
		                break;
		            default:
		            {
		                switch (Integer.parseInt(number.substring(0, 3))) {
		                    case 300:
		                    case 301:
		                    case 302:
		                    case 303:
		                    case 304:
		                    case 305:
		                        ccType = CREDITCARD_TYPE_DINERS;
		                        break;
		                    case 353:
		                    case 354:
		                    case 355:
		                    case 356:
		                    case 357:
		                    case 358:
		                        ccType = CREDITCARD_TYPE_JCB;
		                        break;
		                    case 644:
		                    case 645:
		                    case 646:
		                    case 647:
		                    case 648:
		                    case 649:
		                        ccType = CREDITCARD_TYPE_DISCOVER;
		                        break;
		                    case 624:
		                    case 625:
		                    case 626:
		                        ccType = CREDITCARD_TYPE_CUP;
		                        break;
		                    default:
		                    {
		                        switch (Integer.parseInt(number.substring(0, 2))) {
		                            case 34:
		                            case 37:
		                                ccType = CREDITCARD_TYPE_AMEX;
		                                break;
		                            case 36:
		                            case 38:
		                            case 39:
		                                ccType = CREDITCARD_TYPE_DINERS;
		                                break;
		                            case 51:
		                            case 52:
		                            case 53:
		                            case 54:
		                            case 55:
		                                ccType = CREDITCARD_TYPE_MASTERCARD;
		                                break;
		                            case 65:
		                                ccType = CREDITCARD_TYPE_DISCOVER;
		                                break;
		                            default:
		                            {
		                                
		                                switch (Integer.parseInt(number.substring(0, 1))) {
		                                    case 3:
		                                        ccType = CREDITCARD_TYPE_JCB;
		                                        break;
		                                    case 5:
		                                    case 6:
		                                        ccType = CREDITCARD_TYPE_MASTERCARD;
		                                        break;
		                                    case 4:
		                                    case 9:
		                                        ccType = CREDITCARD_TYPE_VISA;
		                                        break;
		                                    default:
		                                    {
		                                    }
		                                        break;
		                                }
		                                
		                            }
		                                break;
		                        }
		                    }
		                        break;
		                }
		            }
		                break;
		        }
		    }

		
		return ccType;
	}

	
	
	
	public class processLivePaymentAsync extends AsyncTask<String, String, String> {
		
		private HashMap<String,String>parsedMap = new HashMap<String,String>();
		private String urlToPost;
		private boolean wasProcessed = false;
		private String errorMsg = "Could not process the payment.";
		

		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(thisContext);
			myProgressDialog.setMessage("Processing Payment...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			Post httpClient = new Post();
			
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);
			urlToPost = params[0];
			
			try {
				String xml = httpClient.postData(13, activity,urlToPost);
				
				if(xml.equals(Global.TIME_OUT))
				{
					errorMsg = "TIME OUT, would you like to try again?";
					timedOut = true;
				}
				else if(xml.equals(Global.NOT_VALID_URL))
				{
					errorMsg = "Can not proceed...";
				}
				else
				{
					InputSource inSource = new InputSource(new StringReader(xml));
	
					SAXParser sp = spf.newSAXParser();
					XMLReader xr = sp.getXMLReader();
					xr.setContentHandler(handler);
					xr.parse(inSource);
					parsedMap = handler.getData();
					
					if(parsedMap!=null&&parsedMap.size()>0&&parsedMap.get("epayStatusCode").equals("APPROVED"))
						wasProcessed = true;
					else if(parsedMap!=null&&parsedMap.size()>0)
					{
						StringBuilder sb = new StringBuilder();
						sb.append("statusCode = ").append(parsedMap.get("statusCode")).append("\n");
						sb.append(parsedMap.get("statusMessage"));
						errorMsg = sb.toString();
					}
					else
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
			
			if(wasProcessed)		//payment processing succeeded
			{
				payment.getSetData("pay_resultcode", false, parsedMap.get("pay_resultcode"));
				payment.getSetData("pay_resultmessage", false,parsedMap.get("pay_resultmessage"));
				payment.getSetData("pay_transid", false, parsedMap.get("CreditCardTransID"));
				payment.getSetData("authcode", false, parsedMap.get("AuthorizationCode"));
				payment.getSetData("processed", false, "9");
				
				orientation = getResources().getConfiguration().orientation;
				global.orientation = orientation;
				
				
				if(isOpenInvoice&&isMultiInvoice)
				{
					if(invPaymentList.size()>0)
					{
						payment.getSetData("inv_id",false,"");
						invPayHandler.insert(invPaymentList);
					}
				}
				payHandler.insert(payment);
				
				if(myPref.getPreferences(MyPreferences.pref_handwritten_signature))
				{
					new printAsync().execute(false);
				}
				else
				{
					Intent intent = new Intent(activity,DrawReceiptActivity.class);
					intent.putExtra("isFromPayment", true);
					startActivityForResult(intent,requestCode);
				}
			}
			else																						//payment processing failed
			{
				btnProcess.setEnabled(true);
				dialog = new AlertDialog.Builder(thisContext);
				dialog.setTitle("Error");
				dialog.setMessage(errorMsg);
				dialog.setCancelable(true);
				dialog.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						if(timedOut)
							new processLivePaymentAsync().execute(urlToPost);
					}
				});
				if(timedOut)
				{
					dialog.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
							finish();
						}
					});
				}
				dialog.create().show();
			}
		}
	}
	

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(global.orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
		
		
		if (resultCode == -1) 		//payment was signed finish inserting to DB
		{
			
			PaymentsHandler payHandler = new PaymentsHandler(this);		
			Global.amountPaid = payHandler.updateSignaturePayment(extras.getString("pay_id"));
			
			
			if(myPref.getPreferences(MyPreferences.pref_enable_printing))
			{
				if(myPref.getPreferences(MyPreferences.pref_automatic_printing))
					new printAsync().execute(false);
				else
					showPrintDlg(false,false);
			}
			else
				finishPaymentTransaction();
			
//			if(myPref.getPreferences(MyPreferences.pref_handwritten_signature))
//				new printAsync().execute(false);
//			else if(myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
//				showPrintDlg(true,false);
//			else
//				finishPaymentTransaction();
				
		}
	}

	
	private void finishPaymentTransaction()
	{
		if(!myPref.getLastPayID().isEmpty())
			myPref.setLastPayID("0");
					
		
		global.encodedImage = new String();
		if(requestCode == Global.FROM_JOB_INVOICE||requestCode == Global.FROM_OPEN_INVOICES||requestCode == Global.FROM_JOB_SALES_RECEIPT)
			setResult(-2);
		else
		{
			Intent result = new Intent();
			result.putExtra("total_amount",  Double.toString(Global.formatNumFromLocale(this.amountField.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim())));
			setResult(-2,result);
		}
		
		finish();
	}
	
	
	private class printAsync extends AsyncTask<Boolean, String, String> 
	{
		private boolean wasReprint = false;
		private boolean printingSuccessful = true;
		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage("Printing...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			if(myProgressDialog.isShowing())
				myProgressDialog.dismiss();
			myProgressDialog.show();

		}

		@Override
		protected String doInBackground(Boolean... params) {
			// TODO Auto-generated method stub

			wasReprint = params[0];
			if(Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice!=null)
			{
				printingSuccessful = Global.mainPrinterManager.currentDevice.printPaymentDetails(payment.getSetData("pay_id", true, null),1,wasReprint);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			if(myProgressDialog.isShowing())
				myProgressDialog.dismiss();
			if(printingSuccessful)
			{
				if(!wasReprint&&myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
					showPrintDlg(true,false);
				else
				{
					finishPaymentTransaction();
				}
			}
			else
			{
				showPrintDlg(wasReprint,true);
			}
		}
	}
	
	
	
	private void showPrintDlg(final boolean isReprint,boolean isRetry) {
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(false);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);
		if(isRetry)
		{
			viewTitle.setText(R.string.dlog_title_error);
			viewMsg.setText(R.string.dlog_msg_failed_print);
		}
		else
		{
			if(isReprint)
				viewMsg.setText(R.string.dlog_msg_print_cust_copy);
			else
				viewMsg.setText(R.string.dlog_msg_want_to_print);
		}
		Button btnYes = (Button)dlog.findViewById(R.id.btnDlogLeft);
		Button btnNo = (Button)dlog.findViewById(R.id.btnDlogRight);
		btnYes.setText(R.string.button_yes);
		btnNo.setText(R.string.button_no);
		
		btnYes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				new printAsync().execute(isReprint);
				
			}
		});
		btnNo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				//activity.finish();
				finishPaymentTransaction();
			}
		});
		dlog.show();
	}
	
	
	
	private void showConfirmDlog()
	{
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(false);
		dlog.setContentView(R.layout.dlog_btn_single_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);
		viewMsg.setText("When done printing customer copy press OK");
		Button btnOk = (Button)dlog.findViewById(R.id.btnDlogSingle);
		btnOk.setText(R.string.button_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				finishPaymentTransaction();
			}
		});
		dlog.show();
	}
	
	
	@Override
	public void cardWasReadSuccessfully(boolean read,CreditCardInfo cardManager) {
		// TODO Auto-generated method stub
		this.cardInfoManager = cardManager;
		updateViewAfterSwipe();
		if(uniMagReader!=null&&uniMagReader.readerIsConnected())
		{
			uniMagReader.startReading();
		}
		else if(magtekReader==null&&Global.btSwiper==null&&Global.mainPrinterManager!=null)
			Global.mainPrinterManager.currentDevice.loadCardReader(callBack);
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
	
	
	private void updateViewAfterSwipe()
	{
		month.setText(cardInfoManager.getCardExpMonth());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy",Locale.getDefault());
		SimpleDateFormat dt2 = new SimpleDateFormat("yy",Locale.getDefault());
		String formatedYear = new String();
		try 
		{
			Date date = dt2.parse(cardInfoManager.getCardExpYear());
			formatedYear = dt.format(date);
			//creditCardType = cardInfoManager.getCardType();
		} 
		catch (ParseException e) 
		{
			// TODO Auto-generated catch block
			Tracker tracker = EasyTracker.getInstance(activity);
			tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
		}
		
		cardInfoManager.setCardExpYear(formatedYear);
		year.setText(formatedYear);
		ownersName.setText(cardInfoManager.getCardOwnerName());
		cardNum.setText(cardInfoManager.getCardNumAESEncrypted());
		
		wasReadFromReader = true;
		creditCardType = cardInfoManager.getCardType();
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
			this.cardInfoManager = Global.parseSimpleMSR(activity, data);
			updateViewAfterSwipe();
		}
	}



	@Override
	public void scannerWasRead(String data) {
		// TODO Auto-generated method stub
		
	}
}
