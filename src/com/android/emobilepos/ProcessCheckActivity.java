package com.android.emobilepos;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.android.database.CustomersHandler;
import com.android.database.InvoicePaymentsHandler;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.payment.CaptureCheck_FA;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCheckHandler;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Payment;
import com.android.support.Post;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ProcessCheckActivity extends Activity implements OnCheckedChangeListener,OnClickListener{
	
	private Global global;
	private Activity activity;
	private boolean hasBeenCreated = false;
	private final int CHECK_NAME = 0,CHECK_EMAIL = 1, CHECK_PHONE = 2, CHECK_AMOUNT = 3, CHECK_AMOUNT_PAID = 4,CHECK_REFERENCE=5,CHECK_ACCOUNT = 6,
			CHECK_ROUTING = 7,CHECK_NUMBER = 8,CHECK_CITY = 9, CHECK_STATE = 10,CHECK_ZIPCODE = 11,COMMENTS = 12,CHECK_ADDRESS = 13
			,CHECK_DL_NUMBER = 14, CHECK_DL_STATE = 15, CHECK_DL_DOB = 16;

	private boolean timedOut = false;
	private boolean isFromSalesReceipt = false;
	private boolean isFromMainMenu = false;
	private EditText[] field;

	private boolean isMultiInvoice = false,isOpenInvoice = false;
	private String accountType = "Savings";
	private String checkType = "Personal";
	private String inv_id;
	private MyPreferences myPref;

	private ProgressDialog myProgressDialog;
	private Payment payment;
	private PaymentsHandler payHandler;
	private InvoicePaymentsHandler invPayHandler;
	
	private String[] inv_id_array,txnID_array;
	private double[] balance_array;
	private List<String[]>invPaymentList;
	
	private boolean isRefund = false;
	private boolean isLivePayment = false;
	private String custidkey = "";
	private HashMap<String,String>customerInfo;
	private RadioGroup radioGroupCheckType,radioGroupAddressType;
	private final int INTENT_CAPTURE_CHECK = 100;
	private Bundle extras;
	private boolean checkWasCapture = false;
	private double amountToBePaid = 0,actualAmount = 0;
	private Button btnProcess;
	private TextView tvCheckChange;
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		extras = this.getIntent().getExtras();
		activity = this;
		myPref = new MyPreferences(activity);
		
		if(myPref.getPreferences(MyPreferences.pref_process_check_online))
		{
			isLivePayment = true;
			setContentView(R.layout.process_live_check_layout);
		}
		else
		{
			setContentView(R.layout.process_local_check_layout);
		}
		

		
		field = new EditText[] { (EditText) findViewById(R.id.checkName), (EditText) findViewById(R.id.checkEmail),
				(EditText) findViewById(R.id.checkPhone), (EditText) findViewById(R.id.checkAmount),
				(EditText) findViewById(R.id.checkAmountPaid), (EditText) findViewById(R.id.checkInvoice),
				(EditText) findViewById(R.id.checkAccount), (EditText) findViewById(R.id.checkRouting),
				(EditText) findViewById(R.id.checkNumber),(EditText)findViewById(R.id.checkCity),(EditText)findViewById(R.id.checkState),
				(EditText) findViewById(R.id.checkZipcode),(EditText)findViewById(R.id.checkComment),(EditText)findViewById(R.id.checkAddress),
				(EditText) findViewById(R.id.checkDLNumber), (EditText)findViewById(R.id.checkDLState), (EditText)findViewById(R.id.checkDOBYear)};
				
		
		if(isLivePayment)
		{
			ImageButton btnCaptureCheck = (ImageButton)findViewById(R.id.btnCheckCapture);
			btnCaptureCheck.setOnClickListener(this);
			btnCaptureCheck.setOnTouchListener(Global.opaqueImageOnClick());
			radioGroupCheckType = (RadioGroup)findViewById(R.id.radioGroupCheckType);
			radioGroupAddressType = (RadioGroup)findViewById(R.id.radioGroupAddressType);
			radioGroupCheckType.setOnCheckedChangeListener(this);
			radioGroupAddressType.setOnCheckedChangeListener(this);
		}
		
		
		global = (Global)getApplication();
		
		hasBeenCreated = true;
		
		TextView headerTitle = (TextView) findViewById(R.id.HeaderTitle);
		tvCheckChange = (TextView) findViewById(R.id.changeCheckText);
		
		
		if(!Global.getValidString(extras.getString("cust_id")).isEmpty())
		{
			CustomersHandler handler2 = new CustomersHandler(activity);
			customerInfo = handler2.getCustomerMap(extras.getString("cust_id"));
			if(customerInfo!=null)
			{
				if(customerInfo!=null)
				{
					if(!customerInfo.get("cust_name").isEmpty())
						field[CHECK_NAME].setText(customerInfo.get("cust_name"));
					if(!customerInfo.get("cust_phone").isEmpty())
						field[CHECK_PHONE].setText(customerInfo.get("cust_phone"));
					if(!customerInfo.get("cust_email").isEmpty())
						field[CHECK_EMAIL].setText(customerInfo.get("cust_email"));
				}
			}	
		}
		
		
		
		if (extras.getBoolean("salespayment")||extras.getBoolean("salesreceipt")) {
			headerTitle.setText(getString(R.string.check_payment_title));
		} else if (extras.getBoolean("salesrefund")) {
			headerTitle.setText(getString(R.string.check_refund_title));
			isRefund = true;
		} else if (extras.getBoolean("histinvoices")) {
			headerTitle.setText(getString(R.string.check_payment_title));
		} else if (extras.getBoolean("salesinvoice")) {
			headerTitle.setText("Check Invoice");
		}

		this.field[this.CHECK_AMOUNT].setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));
		field[CHECK_AMOUNT_PAID].setText(Global.formatDoubleToCurrency(0));
		
		isFromSalesReceipt = extras.getBoolean("isFromSalesReceipt");
		isFromMainMenu = extras.getBoolean("isFromMainMenu");
		
		if (!isFromMainMenu) {
			this.field[this.CHECK_AMOUNT].setEnabled(false);
		}
		

		Button exactBut = (Button) findViewById(R.id.exactAmountBut);
		btnProcess = (Button) findViewById(R.id.processCheckBut);
		exactBut.setOnClickListener(this);
		btnProcess.setOnClickListener(this);

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

		
		
		custidkey = extras.getString("custidkey");
		if(custidkey==null)
			custidkey = "";
		
		
		
		field[CHECK_AMOUNT].addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				recalculateChange();
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {parseInputedCurrency(s,0);}
		});
		
		
		
		field[CHECK_AMOUNT_PAID].addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				if(!field[CHECK_AMOUNT_PAID].getText().toString().isEmpty())
					recalculateChange();
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {parseInputedCurrency(s,1);}
		});

		hasBeenCreated = true;
	}
	
	
	private void recalculateChange()
	{
		
		double totAmount = Global.formatNumFromLocale(field[CHECK_AMOUNT].getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		double totalPaid = Global.formatNumFromLocale(field[CHECK_AMOUNT_PAID].getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		
		if(totalPaid>totAmount)
		{
			double tempTotal = Math.abs(totAmount - totalPaid);
			tvCheckChange.setText(Global.getCurrencyFormat(Global.formatNumToLocale(tempTotal)));
		}
		else
		{
			tvCheckChange.setText(Global.formatDoubleToCurrency(0.00));
		}
			
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
            case 0:
            	field[CHECK_AMOUNT].setText(cashAmountBuilder.toString());
            	break;
            case 1:
            	field[CHECK_AMOUNT_PAID].setText(cashAmountBuilder.toString());
            	break;
            }        
        }
      
            // keeps the cursor always to the right
        	switch(type)
        	{
        	case 0:
        		Selection.setSelection(field[CHECK_AMOUNT].getText(), field[CHECK_AMOUNT].getText().length());
        		break;
        	case 1:
        		Selection.setSelection(field[CHECK_AMOUNT_PAID].getText(), field[CHECK_AMOUNT_PAID].getText().length());
        		break;
        	}
	}
	
	
	private boolean validInput()
	{
		String check_amount_paid = field[CHECK_AMOUNT_PAID].getText().toString();
		String check_name = field[CHECK_NAME].getText().toString();
		if(isLivePayment)
		{
			String check_acct_num = field[CHECK_ACCOUNT].getText().toString();
			String check_routing = field[CHECK_ROUTING].getText().toString();
			
			if(!check_acct_num.isEmpty()&&!check_routing.isEmpty()&&!check_name.isEmpty()&&!check_amount_paid.isEmpty())
			{
				field[CHECK_ACCOUNT].setBackgroundResource(android.R.drawable.edit_text);
				field[CHECK_ROUTING].setBackgroundResource(android.R.drawable.edit_text);
				field[CHECK_NAME].setBackgroundResource(android.R.drawable.edit_text);
				field[CHECK_AMOUNT_PAID].setBackgroundResource(android.R.drawable.edit_text);
				return true;
			}
			else
			{
				if(check_acct_num.isEmpty())
					field[CHECK_ACCOUNT].setBackgroundResource(R.drawable.edittext_wrong_input);
				else
					field[CHECK_ACCOUNT].setBackgroundResource(android.R.drawable.edit_text);
				if(check_routing.isEmpty())
					field[CHECK_ROUTING].setBackgroundResource(R.drawable.edittext_wrong_input);
				else
					field[CHECK_ROUTING].setBackgroundResource(android.R.drawable.edit_text);
				if(check_name.isEmpty())
					field[CHECK_NAME].setBackgroundResource(R.drawable.edittext_wrong_input);
				else
					field[CHECK_NAME].setBackgroundResource(android.R.drawable.edit_text);
				return false;
			}
		}
		else
		{
			String check_check_num = field[CHECK_NUMBER].getText().toString();
			if(!check_check_num.isEmpty()&&!check_name.isEmpty()&&!check_amount_paid.isEmpty())
			{
				field[CHECK_NUMBER].setBackgroundResource(android.R.drawable.edit_text);
				field[CHECK_NAME].setBackgroundResource(android.R.drawable.edit_text);
				field[CHECK_AMOUNT_PAID].setBackgroundResource(android.R.drawable.edit_text);
				return true;
			}
			else
			{
				if(check_check_num.isEmpty())
					field[CHECK_NUMBER].setBackgroundResource(R.drawable.edittext_wrong_input);
				else
					field[CHECK_NUMBER].setBackgroundResource(android.R.drawable.edit_text);
				
				if(check_name.isEmpty())
					field[CHECK_NAME].setBackgroundResource(R.drawable.edittext_wrong_input);
				else
					field[CHECK_NAME].setBackgroundResource(android.R.drawable.edit_text);
				
				if(check_amount_paid.isEmpty())
					field[CHECK_AMOUNT_PAID].setBackgroundResource(R.drawable.edittext_wrong_input);
				else
					field[CHECK_AMOUNT_PAID].setBackgroundResource(android.R.drawable.edit_text);
				return false;
			}
		}	
	}
	
	
	
	private void processPayment()
	{
		MyPreferences myPref = new MyPreferences(activity);
		actualAmount = Global.formatNumFromLocale(field[this.CHECK_AMOUNT].getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		amountToBePaid = Global.formatNumFromLocale(field[CHECK_AMOUNT_PAID].getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		
		
	
		//custName = field[CHECK_NAME].getText().toString();
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
		
		payment.getSetData("cust_id", false, extras.getString("cust_id"));
		payment.getSetData("custidkey", false, custidkey);
		
		
		if(!myPref.getShiftIsOpen())
			payment.getSetData("clerk_id", false, myPref.getShiftClerkID());
		else if(myPref.getPreferences(MyPreferences.pref_use_clerks))
			payment.getSetData("clerk_id", false, myPref.getClerkID());
		
		payment.getSetData("ref_num", false, field[CHECK_REFERENCE].getText().toString());
		payment.getSetData("paymethod_id", false, extras.getString("paymethod_id"));
		
		Global.amountPaid = Double.toString(amountToBePaid);
		
		payment.getSetData("pay_dueamount", false, Double.toString(amountToBePaid));
		
		if(amountToBePaid>actualAmount)
			payment.getSetData("pay_amount", false, Double.toString(actualAmount));
		else
			payment.getSetData("pay_amount", false, Double.toString(amountToBePaid));
		
		
		payment.getSetData("pay_name", false, field[CHECK_NAME].getText().toString());
		payment.getSetData("pay_phone", false, field[CHECK_PHONE].getText().toString());
		payment.getSetData("pay_email", false, field[CHECK_EMAIL].getText().toString());
		payment.getSetData("pay_check", false, this.field[CHECK_NUMBER].getText().toString());

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
		
		payment.getSetData("card_type", false, "Check");
				
		if(extras.getBoolean("salesrefund",false))
		{
			payment.getSetData("is_refund", false, "1");
			payment.getSetData("pay_type", false, "2");
		}
		else
			payment.getSetData("pay_type", false, "0");

		if(!isLivePayment)
		{
			payment.getSetData("processed", false, "1");
			payHandler.insert(payment);
		
			if(!myPref.getLastPayID().isEmpty())
				myPref.setLastPayID("0");
			
	
			if (extras.getBoolean("histinvoices") || extras.getBoolean("salesinvoice")||isFromSalesReceipt)
				setResult(-2);
			else if(extras.getBoolean("salespayment")||extras.getBoolean("salesrefund"))
			{
				Intent result = new Intent();
				result.putExtra("total_amount", Double.toString(Global.formatNumFromLocale(field[CHECK_AMOUNT].getText().toString().replaceAll("[^\\d\\,\\.]", "").trim())));
				setResult(-2,result);
			}
			else
				setResult(-1);
			
			
//			if(myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
//				showPrintDlg();
//			else
//				finish();
			
			if(myPref.getPreferences(MyPreferences.pref_print_receipt_transaction_payment))
			{
				if(amountToBePaid>actualAmount)
					showChangeDlg();
				new printAsync().execute();
			}
			else if(amountToBePaid>actualAmount)
				showChangeDlg();
			else
				finish();
			

		}
		else
		{
			
			if(checkWasCapture)
			{
				payment.getSetData("frontImage", false, Global.imgFrontCheck);
				payment.getSetData("backImage", false, Global.imgBackCheck);
				
				StringBuilder sb = new StringBuilder();
				sb.append("O").append(field[CHECK_NUMBER].getText().toString()).append("OT");
				sb.append(field[CHECK_ROUTING].getText().toString()).append("T");
				
				String value = field[CHECK_ACCOUNT].getText().toString();
				String str1 = value.substring(0, 3);
				String str2 = value.substring(3, value.length());
				
				sb.append(str1).append("-").append(str2).append("O");
				payment.getSetData("micrData", false, sb.toString());
			}
			
			payment.getSetData("processed", false, "9");
			Encrypt encrypt = new Encrypt(activity);
			
			
			
			
			
			
			payment.getSetData("check_account_number",false, encrypt.encryptWithAES(field[CHECK_ACCOUNT].getText().toString()));
			payment.getSetData("check_routing_number", false, encrypt.encryptWithAES(field[CHECK_ROUTING].getText().toString()));
			payment.getSetData("check_check_number", false, field[CHECK_NUMBER].getText().toString());
			payment.getSetData("check_check_type", false, checkType);
			payment.getSetData("check_account_type", false, accountType);
			payment.getSetData("pay_addr", false, field[CHECK_ADDRESS].getText().toString());
			payment.getSetData("check_city", false, field[CHECK_CITY].getText().toString().trim());
			payment.getSetData("check_state",false, field[CHECK_STATE].getText().toString().trim());
			payment.getSetData("pay_poscode", false, field[CHECK_ZIPCODE].getText().toString());
			payment.getSetData("dl_number", false, field[CHECK_DL_NUMBER].getText().toString());
			payment.getSetData("dl_state", false, field[CHECK_DL_STATE].getText().toString());
			payment.getSetData("dl_dob", false, field[CHECK_DL_DOB].getText().toString());
			
			
			EMSPayGate_Default payGate = new EMSPayGate_Default(activity,payment);
			String generatedURL = new String();
			
			if(!isRefund)
			{
				generatedURL = payGate.paymentWithAction("ChargeCheckAction", false,null,null);
			}
			else
			{
				generatedURL = payGate.paymentWithAction("ReturnCheckAction", false,null,null);
			}
			
			new processLivePaymentAsync().execute(generatedURL);
		}
	}
	
	
	
	
	
	private void processMultiInvoicePayment()
	{
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
		
		actualAmount = Global.formatNumFromLocale(field[this.CHECK_AMOUNT].getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		amountToBePaid = Global.formatNumFromLocale(field[CHECK_AMOUNT_PAID].getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		
		Global.amountPaid = Double.toString(amountToBePaid);
		boolean endBreak = false;
		for(int i = 0 ; i < size;i++)
		{
			if(balance_array[i]>0)
			{
				if(amountToBePaid>=balance_array[i])
				{
					content[2] = Double.toString(balance_array[i]);
					appliedAmount.add(balance_array[i]);
					amountToBePaid-=balance_array[i];
				}
				else
				{
					content[2] = Double.toString(amountToBePaid);
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
			
		MyPreferences myPref = new MyPreferences(activity);
		Encrypt encrypt = new Encrypt(activity);

		
		payHandler = new PaymentsHandler(activity);

		payment = new Payment(activity);

		payment.getSetData("pay_id", false, extras.getString("pay_id"));
		payment.getSetData("emp_id", false, myPref.getEmpID());
		payment.getSetData("cust_id", false,extras.getString("cust_id"));
		payment.getSetData("custidkey", false, custidkey);
		
		if(!myPref.getShiftIsOpen())
			payment.getSetData("clerk_id", false, myPref.getShiftClerkID());
		else if(myPref.getPreferences(MyPreferences.pref_use_clerks))
			payment.getSetData("clerk_id", false, myPref.getClerkID());
		
		payment.getSetData("ref_num", false, field[CHECK_REFERENCE].getText().toString());
		payment.getSetData("paymethod_id", false, extras.getString("paymethod_id"));
		
		if((amountToBePaid - actualAmount)>0)
			payment.getSetData("pay_dueamount", false, Double.toString(actualAmount));
		else
			payment.getSetData("pay_dueamount", false, Double.toString(amountToBePaid));
		
		payment.getSetData("pay_amount", false, Global.amountPaid);
		payment.getSetData("pay_name", false, field[CHECK_NAME].getText().toString());
		payment.getSetData("pay_phone", false, field[CHECK_PHONE].getText().toString());
		payment.getSetData("pay_email", false, field[CHECK_EMAIL].getText().toString());
		payment.getSetData("processed", false, "1");
		payment.getSetData("pay_check", false, this.field[CHECK_NUMBER].getText().toString());
		
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
		
		
		payment.getSetData("pay_type", false, "0");
		payment.getSetData("card_type", false, "Check");
		
		if(!isLivePayment)
		{
			payment.getSetData("processed", false, "1");
			if(invPaymentList.size()>0)
				invPayHandler.insert(invPaymentList);
			
			payHandler.insert(payment);
			if(!myPref.getLastPayID().isEmpty())
				myPref.setLastPayID("0");
			
			setResult(-2);		
			
			if(myPref.getPreferences(MyPreferences.pref_print_receipt_transaction_payment))
			{
				if(amountToBePaid>actualAmount)
					showChangeDlg();
				new printAsync().execute();
			}
			else if(amountToBePaid>actualAmount)
				showChangeDlg();
			else
				finish();
			
		}
		else
		{	
			
			if(checkWasCapture)
			{
				payment.getSetData("frontImage", false, Global.imgFrontCheck);
				payment.getSetData("backImage", false, Global.imgBackCheck);
				
				StringBuilder sb = new StringBuilder();
				sb.append("O").append(field[CHECK_NUMBER].getText().toString()).append("OT");
				sb.append(field[CHECK_ROUTING].getText().toString()).append("T");
				
				String tempVal = field[CHECK_ACCOUNT].getText().toString();
				String str1 = tempVal.substring(0, 3);
				String str2 = tempVal.substring(3, tempVal.length());
				
				sb.append(str1).append("-").append(str2).append("O");
				payment.getSetData("micrData", false, sb.toString());
			}
			
			
			payment.getSetData("processed", false, "9");
			
			payment.getSetData("check_account_number",false, encrypt.encryptWithAES(field[CHECK_ACCOUNT].getText().toString()));
			payment.getSetData("check_routing_number", false, encrypt.encryptWithAES(field[CHECK_ROUTING].getText().toString()));
			payment.getSetData("check_check_number", false, this.field[CHECK_NUMBER].getText().toString());
			payment.getSetData("check_check_type", false, checkType);
			payment.getSetData("check_account_type", false, accountType);
			payment.getSetData("pay_addr", false, field[CHECK_ADDRESS].getText().toString());
			payment.getSetData("check_city", false, field[CHECK_CITY].getText().toString().trim());
			payment.getSetData("check_state",false, field[CHECK_STATE].getText().toString().trim());
			payment.getSetData("pay_poscode", false, field[CHECK_ZIPCODE].getText().toString());
			
			EMSPayGate_Default payGate = new EMSPayGate_Default(activity,payment);
			String generatedURL = new String();
			
			if(!isRefund)
			{
				generatedURL = payGate.paymentWithAction("ChargeCheckAction", false,null,null);
			}
			else
			{
				generatedURL = payGate.paymentWithAction("ReturnCheckAction", false,null,null);
			}
			
			new processLivePaymentAsync().execute(generatedURL);
		}
		
	}
	
	
	public class processLivePaymentAsync extends AsyncTask<String, String, String> {
		
		//private String[]returnedPost;
		private HashMap<String,String>responseMap;
		private String statusCode = "";
		private String urlToPost;
		
		private String errorMsg = "Could not process the payment.";
		

		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage("Processing Payment...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			Post httpClient = new Post();
			//MyHttpClient httpClient = new MyHttpClient(activity);
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXProcessCheckHandler handler = new SAXProcessCheckHandler();
			urlToPost = params[0];
			try {
				String xml = httpClient.postData(13, activity,urlToPost);
				
				if(xml.equals(Global.TIME_OUT))
				{
					errorMsg = "Could not process the payment, would you like to try again?";
					timedOut = true;
				}
				else if(xml.equals(Global.NOT_VALID_URL))
				{
					errorMsg = "Can not proceed...";
				}
				InputSource inSource = new InputSource(new StringReader(xml));

				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				xr.setContentHandler(handler);
				xr.parse(inSource);
				//returnedPost = new String[handler.getEmpData().length];
				//returnedPost = handler.getEmpData();
				responseMap = handler.getResponseMap();
				//String val = "";
				statusCode = responseMap.get("epayStatusCode");
				if(statusCode!=null&&!statusCode.equals("APPROVED"))
				{
					StringBuilder sb = new StringBuilder();
					sb.append(responseMap.get("epayStatusCode")).append("\nstatusCode = ");
					sb.append(responseMap.get("statusCode")).append("\n").append(responseMap.get("statusMessage"));
					errorMsg = sb.toString();
				}
				else if(statusCode==null)
				{
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
			
			if(responseMap!=null&&statusCode!=null&&statusCode.equals("APPROVED"))
			{
				payment.getSetData("pay_resultcode", false, responseMap.get("pay_resultcode"));
				payment.getSetData("pay_resultmessage", false, responseMap.get("pay_resultmessage"));
				payment.getSetData("pay_transid", false, responseMap.get("CreditCardTransID"));
				payment.getSetData("authcode", false, responseMap.get("AuthorizationCode"));
				payment.getSetData("pay_receipt", false, responseMap.get("pay_receipt"));
				payment.getSetData("pay_refnum", false, responseMap.get("pay_refnum"));
				payment.getSetData("pay_maccount", false, responseMap.get("pay_maccount"));
				payment.getSetData("pay_groupcode", false, responseMap.get("pay_groupcode"));
				payment.getSetData("pay_stamp", false, responseMap.get("pay_stamp"));
				payment.getSetData("pay_expdate", false, responseMap.get("pay_expdate"));
				payment.getSetData("pay_result", false, responseMap.get("pay_result"));
				payment.getSetData("recordnumber", false, responseMap.get("recordnumber"));
				
				
				Global.imgBackCheck = "";
				Global.imgFrontCheck = "";
								
				if(isOpenInvoice&&isMultiInvoice)
				{
					if(invPaymentList.size()>0)
					{
						invPayHandler.insert(invPaymentList);
					}
				}
				payHandler.insert(payment);
				setResult(-2);
				
				if(myPref.getPreferences(MyPreferences.pref_print_receipt_transaction_payment))
				{
					if(amountToBePaid>actualAmount)
						showChangeDlg();
					new printAsync().execute();
				}
				else if(amountToBePaid>actualAmount)
					showChangeDlg();
				else
					finish();
								
			}
			else
				showFailedPrompt(errorMsg,urlToPost);
			
		}
	}
	
	
	private void showPrintDlg(boolean isRetry) {
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

		Button btnYes = (Button)dlog.findViewById(R.id.btnDlogLeft);
		Button btnNo = (Button)dlog.findViewById(R.id.btnDlogRight);
		btnYes.setText(R.string.button_yes);
		btnNo.setText(R.string.button_no);
		
		btnYes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				new printAsync().execute();
				
			}
		});
		btnNo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				//activity.finish();
				if(amountToBePaid<=actualAmount)
					finish();
			}
		});
		dlog.show();
	}
	
	
	private void showChangeDlg() {
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(false);
		dlog.setContentView(R.layout.dlog_btn_single_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);
		StringBuilder sb = new StringBuilder();
		//sb.append(getString(R.string.dlog_msg_print_cust_copy)).append("\n\n");
		
		sb.append("Change: ").append(Global.formatDoubleToCurrency(amountToBePaid-actualAmount));
		viewMsg.setText(sb.toString());
		Button btnOK = (Button)dlog.findViewById(R.id.btnDlogSingle);
		btnOK.setText(R.string.button_ok);
		
		btnOK.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				finish();
			}
		});
		dlog.show();
	}
	
	
	private void showFailedPrompt(String msg,final String urlToPost)
	{
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(true);
		dlog.setCanceledOnTouchOutside(true);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_error);
		viewMsg.setText(msg);
		Button btnOK = (Button)dlog.findViewById(R.id.btnDlogLeft);
		Button btnNo = (Button)dlog.findViewById(R.id.btnDlogRight);
		btnOK.setText(R.string.button_ok);
		btnNo.setText(R.string.button_no);
		
		btnOK.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				if(timedOut)
					new processLivePaymentAsync().execute(urlToPost);
			}
		});
		btnNo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				finish();
			}
		});
		if(!timedOut)
			btnNo.setVisibility(View.GONE);
		dlog.show();
	}
	
	
	private class printAsync extends AsyncTask<Void, Void, Void> 
	{
		private boolean printSuccessful = true;
		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage("Printing...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			if(Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice!=null)
			{
				printSuccessful = Global.mainPrinterManager.currentDevice.printPaymentDetails(payment.getSetData("pay_id", true, null),1,false);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			myProgressDialog.dismiss();
			if(printSuccessful)
			{
				if(amountToBePaid<=actualAmount)
					finish();
			}
			else
			{
				showPrintDlg(true);
			}
		}
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
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		switch(checkedId)
		{
		case R.id.radioTypeSavings:
			accountType = "Savings";
			break;
		case R.id.radioTypeChecking:
			accountType = "Checking";
			break;
		case R.id.radioTypePersonal:
			checkType = "Personal";
			break;
		case R.id.radioTypeCorporate:
			checkType = "Corporate";
			break;
		}
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.processCheckBut:
			btnProcess.setEnabled(false);
			if(!validInput())
			{
				Global.showPrompt(activity, R.string.validation_failed, activity.getString(R.string.card_validation_error));
			}
			else
			{
				if(!isLivePayment&&Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice!=null)
					Global.mainPrinterManager.currentDevice.openCashDrawer();
				
				
				if(!isOpenInvoice||(isOpenInvoice&&!isMultiInvoice))
					processPayment();
				else
					processMultiInvoicePayment();

			}
			btnProcess.setEnabled(true);
			break;
		case R.id.exactAmountBut:
			field[CHECK_AMOUNT_PAID].setText(field[CHECK_AMOUNT].getText().toString().replace(",", ""));
			break;
		case R.id.btnCheckCapture:
			Intent intent = new Intent(this,CaptureCheck_FA.class);
			startActivityForResult(intent,INTENT_CAPTURE_CHECK);
			break;
		}
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == INTENT_CAPTURE_CHECK && resultCode == RESULT_OK) {
			checkWasCapture = true;
		} 
	}
}
