package com.android.emobilepos;



import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;




import com.android.database.CustomersHandler;
import com.android.database.InvoicePaymentsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Payment;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProcessCashMenuActivity extends Activity implements OnClickListener{
	private ProgressDialog myProgressDialog;
	private AlertDialog.Builder dialog;
	private Context thisContext = this;
	private Activity activity = this;

	private Payment payment;
	private Global global;
	private boolean hasBeenCreated = false;
	private String inv_id;
	private boolean isFromSalesReceipt = false;
	private boolean isFromMainMenu = false;
	private EditText  paid,amount,reference,tipAmount,promptTipField;//,tipAmount,promptTipField
	private EditText customerNameField,customerEmailField,phoneNumberField;
	private TextView change;
	private boolean isMultiInvoice = false;
	
	
	private String[]inv_id_array,txnID_array;
	private double[] balance_array;
	private boolean isInvoice = false;
	
	private boolean showTipField = true;
	private String custidkey = "";
	
	private double amountToTip = 0,amountToTipFromField = 0;
	private double amountToBePaid = 0,grandTotalAmount = 0,actualAmount = 0;
	private boolean isRefund = false;
	
	private MyPreferences myPref;
	private	TextView dlogGrandTotal;
	private HashMap<String,String>customerInfo;
	private Bundle extras;
	private Button btnProcess;
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.process_cash_layout);
		global = (Global) this.getApplication();
		myPref = new MyPreferences(activity);
		
		if(!myPref.getPreferences(MyPreferences.pref_show_tips_for_cash))
		{
			showTipField = false;
			LinearLayout layout = (LinearLayout)findViewById(R.id.tipFieldMainHolder);
			layout.removeAllViews();
		}
		
		TextView headerTitle = (TextView) findViewById(R.id.HeaderTitle);
		extras = this.getIntent().getExtras();

		if (extras.getBoolean("salespayment")||extras.getBoolean("salesreceipt")) {
			headerTitle.setText(getString(R.string.cash_payment_title));
		} else if (extras.getBoolean("salesrefund")) {
			headerTitle.setText(getString(R.string.cash_refund_title));
			isRefund = true;
		} else if (extras.getBoolean("histinvoices")) {
			headerTitle.setText(getString(R.string.cash_payment_title));
			isInvoice = true;
		} else if (extras.getBoolean("salesinvoice")) {
			headerTitle.setText("Cash Invoice");
		}

		amount = (EditText) findViewById(R.id.amountCashEdit);
		reference = (EditText)findViewById(R.id.referenceNumber);
		tipAmount = (EditText)findViewById(R.id.tipAmountField);
		
		customerNameField = (EditText)findViewById(R.id.processCashName);
		customerEmailField = (EditText)findViewById(R.id.processCashEmail);
		phoneNumberField = (EditText)findViewById(R.id.processCashPhone);
		
		Button btnFive = (Button)findViewById(R.id.btnFive);
		Button btnTen = (Button)findViewById(R.id.btnTen);
		Button btnTwenty = (Button)findViewById(R.id.btnTwenty);
		Button btnFourty = (Button)findViewById(R.id.btnFourty);
		btnFive.setOnClickListener(this);
		btnTen.setOnClickListener(this);
		btnTwenty.setOnClickListener(this);
		btnFourty.setOnClickListener(this);
		
		if(showTipField)
			this.tipAmount.setText(Global.formatDoubleToCurrency(0.00));

		amount.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));
		
		
		
		
		isFromSalesReceipt = extras.getBoolean("isFromSalesReceipt");
		isFromMainMenu = extras.getBoolean("isFromMainMenu");
		custidkey = extras.getString("custidkey");
		if(custidkey==null)
			custidkey = "";

		if (!isFromMainMenu) {
			amount.setEnabled(false);
		}
		
		this.paid = (EditText) findViewById(R.id.paidCashEdit);
		
		
		
		this.paid.setText(Global.formatDoubleToCurrency(0.00));
		this.paid.setSelection(5);
		this.paid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(v.hasFocus())
				{
					int lent = paid.getText().length();
					Selection.setSelection(paid.getText(), lent);
				}
			}
		});
		this.amount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(v.hasFocus())
				{
					Selection.setSelection(amount.getText(),amount.getText().length());
				}
				
			}
		});
		

		
		change = (TextView) findViewById(R.id.changeCashText);

		Button exactBut = (Button) findViewById(R.id.exactAmountBut);
		btnProcess = (Button) findViewById(R.id.processCashBut);

		if (extras.getBoolean("histinvoices"))
		{
			isMultiInvoice = extras.getBoolean("isMultipleInvoice");
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

		btnProcess.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btnProcess.setEnabled(false);
				double enteredAmount = Global.formatNumFromLocale(paid.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
				if(enteredAmount<=0)
				{
					paid.setBackgroundResource(R.drawable.edittext_wrong_input);
					Global.showPrompt(activity, R.string.validation_failed, activity.getString(R.string.error_wrong_amount));
					
//					dialog = new AlertDialog.Builder(thisContext);
//					dialog.setTitle("Validation Failed");
//					dialog.setMessage(R.string.error_wrong_amount);
//					dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							// TODO Auto-generated method stub
//							dialog.dismiss();
//						}
//					}).create();
//					dialog.show();
				}
				else
				{
					paid.setBackgroundResource(R.drawable.edittext_border);
					
					if(Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice!=null)
						Global.mainPrinterManager.currentDevice.openCashDrawer();
					
					if(!isInvoice||(isInvoice&&!isMultiInvoice))
						new processPaymentAsync().execute(false);
					else
					{
						new processPaymentAsync().execute(true);
					}
				}
				btnProcess.setEnabled(true);
				
			}
		});

		exactBut.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				amountToBePaid = Global.formatNumFromLocale(amount.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
				grandTotalAmount = amountToBePaid+amountToTip;
				paid.setText(amount.getText().toString());
				
			}
			
		});
		
		
		this.amount.addTextChangedListener(new TextWatcher() 
		{
	        public void afterTextChanged(Editable s) {
				recalculateChange();
				
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	        public void onTextChanged(CharSequence s, int start, int before, int count) {parseInputedCurrency(s,1);}
	    });
		
		
		this.paid.addTextChangedListener(new TextWatcher() {
	        public void afterTextChanged(Editable s) {
				if (!paid.getText().toString().isEmpty())
					recalculateChange();
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	        public void onTextChanged(CharSequence s, int start, int before, int count) {parseInputedCurrency(s,0);}
	    });
		
		
		if(showTipField)
		{
			Button tipButton = (Button)findViewById(R.id.tipAmountBut);
			tipButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					promptTipConfirmation();
				}
			});
		}
		
		
		
		if(!Global.getValidString(extras.getString("cust_id")).isEmpty())
		{
			
			CustomersHandler handler2 = new CustomersHandler(activity);
			customerInfo = handler2.getCustomerMap(extras.getString("cust_id"));
			
			
			if(customerInfo!=null)
			{
				if(!customerInfo.get("cust_name").isEmpty())
					customerNameField.setText(customerInfo.get("cust_name"));
				if(!customerInfo.get("cust_phone").isEmpty())
					phoneNumberField.setText(customerInfo.get("cust_phone"));
				if(!customerInfo.get("cust_email").isEmpty())
					customerEmailField.setText(customerInfo.get("cust_email"));
			}
		}
		
		
		hasBeenCreated = true;
	}
	
	
	
	private void recalculateChange()
	{
		
		double totAmount = Global.formatNumFromLocale(amount.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		double totalPaid = Global.formatNumFromLocale(paid.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		
		if(totalPaid>totAmount)
		{
			double tempTotal = Math.abs(totAmount - totalPaid);
			change.setText(Global.getCurrencyFormat(Global.formatNumToLocale(tempTotal)));
		}
		else
		{
			change.setText(Global.formatDoubleToCurrency(0.00));
		}
			
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
		
		//*****Method that works only with gingerbread and removes background
		/*final Dialog dialog = new Dialog(activity,R.style.TransparentDialog);
		dialog.setContentView(dialogLayout);*/
		
		amountToBePaid = Global.formatNumFromLocale(paid.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		grandTotalAmount = amountToBePaid + amountToTip;
		
		Button tenPercent = (Button) dialogLayout.findViewById(R.id.tenPercent);
		Button fifteenPercent = (Button) dialogLayout.findViewById(R.id.fifteenPercent);
		Button twentyPercent = (Button) dialogLayout.findViewById(R.id.twentyPercent);
		dlogGrandTotal = (TextView) dialogLayout.findViewById(R.id.grandTotalView);
		dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
		
		
		promptTipField = (EditText)dialogLayout.findViewById(R.id.otherTipAmountField);
		promptTipField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		promptTipField.setText("");
		
		Button cancelTip = (Button)dialogLayout.findViewById(R.id.cancelTipButton);
		Button saveTip = (Button)dialogLayout.findViewById(R.id.acceptTipButton);
		Button noneButton = (Button)dialogLayout.findViewById(R.id.noneButton);
		
		
		
		promptTipField.addTextChangedListener(new TextWatcher() 
		{
	        public void afterTextChanged(Editable s) {				
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	        public void onTextChanged(CharSequence s, int start, int before, int count) {parseInputedCurrency(s,2);}
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
				amountToTip = (float)(amountToBePaid*(0.1));
				grandTotalAmount = amountToBePaid+amountToTip;
				dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
				promptTipField.setText("");
			}
		});

		fifteenPercent.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				amountToTip = (float)(amountToBePaid*(0.15));
				grandTotalAmount = amountToBePaid+amountToTip;
				dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
				promptTipField.setText("");
			}
		});
		
		twentyPercent.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				amountToTip = (float)(amountToBePaid*(0.2));
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

					if(tipAmount!=null)
						tipAmount.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(Double.toString((double)amountToTip)))));
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
            case 0:
            	this.paid.setText(cashAmountBuilder.toString());
            	amountToBePaid = Global.formatNumFromLocale(cashAmountBuilder.toString().replaceAll("[^\\d\\,\\.]", "").trim());
            	grandTotalAmount = amountToBePaid + amountToTip;
            	break;
            case 1:
            	this.amount.setText(cashAmountBuilder.toString());
            	actualAmount = Global.formatNumFromLocale(cashAmountBuilder.toString().replaceAll("[^\\d\\,\\.]", "").trim());
            	//amountToBePaid = (float)(Global.formatNumFromLocale(cashAmountBuilder.toString().replaceAll("[^\\d\\,\\.]", "").trim()));
            	//grandTotalAmount = amountToBePaid + amountToTip;
            	break;
            case 2:
            	this.promptTipField.setText(cashAmountBuilder);
            	amountToTipFromField = Global.formatNumFromLocale(cashAmountBuilder.toString().replaceAll("[^\\d\\,\\.]", "").trim());
            	if(amountToTipFromField>0)
            	{
            		amountToTip = amountToTipFromField;
	            	grandTotalAmount = amountToBePaid + amountToTip;
	            	dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
            	}
            	break;
            }           
        }

        	switch(type)
        	{
        	case 0:
        		Selection.setSelection(paid.getText(), this.paid.getText().length());
        		break;
        	case 1:
        		Selection.setSelection(this.amount.getText(), this.amount.getText().length());
        		break;
        	case 2:
        		Selection.setSelection(this.promptTipField.getText(), this.promptTipField.getText().length());
        		break;
        	}
	}
	
	

	private void processPayment() {
		PaymentsHandler payHandler = new PaymentsHandler(activity);
		actualAmount = Global.formatNumFromLocale(amount.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		

		payment = new Payment(activity);

		payment.getSetData("pay_id", false, extras.getString("pay_id"));

		payment.getSetData("emp_id", false, myPref.getEmpID());
		payment.getSetData("cust_id", false, extras.getString("cust_id"));
		if (!extras.getBoolean("histinvoices")) {
			payment.getSetData("job_id", false, inv_id);
		} else {
			payment.getSetData("inv_id", false, inv_id);
		}

		if (!myPref.getShiftIsOpen())
			payment.getSetData("clerk_id", false, myPref.getShiftClerkID());
		else if (myPref.getPreferences(MyPreferences.pref_use_clerks))
			payment.getSetData("clerk_id", false, myPref.getClerkID());

		payment.getSetData("custidkey", false, custidkey);

		// String tempPaid = Double.toString(grandTotalAmount);

		payment.getSetData("paymethod_id", false, extras.getString("paymethod_id"));

		
		payment.getSetData("pay_dueamount", false, Double.toString(amountToBePaid));
		
		if(amountToBePaid>actualAmount)
			payment.getSetData("pay_amount", false, Double.toString(actualAmount));
		else
			payment.getSetData("pay_amount", false, Double.toString(amountToBePaid));
		
		
		payment.getSetData("pay_name", false, customerNameField.getText().toString());
		payment.getSetData("processed", false, "1");
		payment.getSetData("ref_num", false, reference.getText().toString());

		payment.getSetData("pay_phone", false, phoneNumberField.getText().toString());
		payment.getSetData("pay_email", false, customerEmailField.getText().toString());

		if (showTipField) {
			Global.tipPaid = Double.toString(amountToTip);
			payment.getSetData("pay_tip", false, Global.tipPaid);
		}

		if (Global.isIvuLoto) {
			payment.getSetData("IvuLottoNumber", false, extras.getString("IvuLottoNumber"));
			payment.getSetData("IvuLottoDrawDate", false, extras.getString("IvuLottoDrawDate"));
			payment.getSetData("IvuLottoQR", false,
					Global.base64QRCode(extras.getString("IvuLottoNumber"), extras.getString("IvuLottoDrawDate")));

			if (!extras.getString("Tax1_amount").isEmpty()) {
				payment.getSetData("Tax1_amount", false, extras.getString("Tax1_amount"));
				payment.getSetData("Tax1_name", false, extras.getString("Tax1_name"));

				payment.getSetData("Tax2_amount", false, extras.getString("Tax2_amount"));
				payment.getSetData("Tax2_name", false, extras.getString("Tax2_name"));
			} else {
				BigDecimal tempRate;
				double tempPayAmount = Global.formatNumFromLocale(this.paid.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
				tempRate = new BigDecimal(tempPayAmount * 0.06).setScale(2, BigDecimal.ROUND_UP);
				payment.getSetData("Tax1_amount", false, tempRate.toPlainString());
				payment.getSetData("Tax1_name", false, "Estatal");

				tempRate = new BigDecimal(tempPayAmount * 0.01).setScale(2, BigDecimal.ROUND_UP);
				payment.getSetData("Tax2_amount", false, tempRate.toPlainString());
				payment.getSetData("Tax2_name", false, "Municipal");
			}
		}

		String[] location = Global.getCurrLocation(activity);
		payment.getSetData("pay_latitude", false, location[0]);
		payment.getSetData("pay_longitude", false, location[1]);
		payment.getSetData("card_type", false, "Cash");

		if (extras.getBoolean("salesrefund", false)) {
			payment.getSetData("is_refund", false, "1");
			payment.getSetData("pay_type", false, "2");
		} else
			payment.getSetData("pay_type", false, "0");

		Global.amountPaid = Double.toString(amountToBePaid);

		payHandler.insert(payment);

		if (!myPref.getLastPayID().isEmpty())
			myPref.setLastPayID("0");

		updateShiftAmount();

		if (extras.getBoolean("histinvoices") || extras.getBoolean("salesinvoice") || isFromSalesReceipt) {
			setResult(-2);
		} else if (extras.getBoolean("salespayment") || extras.getBoolean("salesrefund")) {
			Intent result = new Intent();
			result.putExtra("total_amount",
					Double.toString(Global.formatNumFromLocale(this.amount.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim())));
			setResult(-2, result);
		} else
			setResult(-1);
	}
	
	
	
	
	
	private void processMultiInvoicePayment()
	{
		InvoicePaymentsHandler invHandler = new InvoicePaymentsHandler(activity);
		List<Double>appliedAmount = new ArrayList<Double>();
		List<String[]>contentList = new ArrayList<String[]>();
		String[]content = new String[4];
		
		int size = inv_id_array.length;
		String payID = extras.getString("pay_id");
		
		double value = 0;
		
		for(int i = 0 ; i <size;i++)
		{
			value = invHandler.getTotalPaidAmount(inv_id_array[i]);
			if(value!=-1)
			{
				if(balance_array[i]>=value)
					balance_array[i]-=value;
				else
					balance_array[i] = 0.0;
			}
			
		}
		
		double tempPaid = Global.formatNumFromLocale(this.paid.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		Global.amountPaid = Double.toString(grandTotalAmount);
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
				contentList.add(content);
				content = new String[4];
				if(endBreak)
					break;
			}
		}
		
		if(contentList.size()>0)
			invHandler.insert(contentList);
		
		
		PaymentsHandler payHandler = new PaymentsHandler(activity);
		payment = new Payment(activity);
		actualAmount = Global.formatNumFromLocale(amount.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
		
		payment.getSetData("pay_id", false, extras.getString("pay_id"));
		payment.getSetData("cust_id", false, extras.getString("cust_id"));
		payment.getSetData("custidkey", false, custidkey);
		payment.getSetData("emp_id", false, myPref.getEmpID());
		
		
		if(!myPref.getShiftIsOpen())
			payment.getSetData("clerk_id", false, myPref.getShiftClerkID());
		else if(myPref.getPreferences(MyPreferences.pref_use_clerks))
			payment.getSetData("clerk_id", false, myPref.getClerkID());
		
		
		payment.getSetData("paymethod_id", false, extras.getString("paymethod_id"));		
		payment.getSetData("pay_dueamount", false, Double.toString(amountToBePaid));
		
		if(amountToBePaid>actualAmount)
			payment.getSetData("pay_amount", false, Double.toString(actualAmount));
		else
			payment.getSetData("pay_amount", false, Double.toString(amountToBePaid));
		//payment.getSetData("pay_amount", false,Double.toString(amountToBePaid));
		payment.getSetData("pay_name", false, customerNameField.getText().toString());
		payment.getSetData("pay_phone", false, phoneNumberField.getText().toString());
		payment.getSetData("pay_email", false, customerEmailField.getText().toString());
		payment.getSetData("processed", false, "1");
		payment.getSetData("ref_num", false, reference.getText().toString());
		payment.getSetData("inv_id", false, "");
		
		String[] location = Global.getCurrLocation(activity);
		payment.getSetData("pay_latitude", false, location[0]);
		payment.getSetData("pay_longitude", false, location[1]);
		payment.getSetData("pay_type", false, "0");
		payment.getSetData("card_type", false, "Cash");
		
		payHandler.insert(payment);
		if(!myPref.getLastPayID().isEmpty())
			myPref.setLastPayID("0");
		
		updateShiftAmount();
		
		setResult(-2);
	}


	
	
	private void updateShiftAmount()
	{
		
		if(!myPref.getShiftIsOpen())
		{
			boolean isReturn = false;
			if(Global.ord_type==Global.IS_RETURN||isRefund)
				isReturn = true;
			ShiftPeriodsDBHandler handler = new ShiftPeriodsDBHandler(activity);
			if(amountToBePaid<=actualAmount)
			{
				handler.updateShiftAmounts(myPref.getShiftID(), amountToBePaid, isReturn);
				//handler.updateShift(myPref.getShiftID(), "total_transaction_cash", Double.toString(amountToBePaid));
			}
			else
			{
				handler.updateShiftAmounts(myPref.getShiftID(), actualAmount, isReturn);
				//handler.updateShift(myPref.getShiftID(), "total_transaction_cash", Double.toString(actualAmount));
			}
		}
			
	}
	
	
	private class processPaymentAsync extends AsyncTask<Boolean, String, String> {

		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(thisContext);
			myProgressDialog.setMessage("Processing Payment...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			// myProgressDialog.setMax(100);
			myProgressDialog.show();
		}
		

		@Override
		protected String doInBackground(Boolean... params) {
			// TODO Auto-generated method stub
			boolean isMultiPayment = params[0];
			try {
				if(isMultiPayment)
					processMultiInvoicePayment();
				else
					processPayment();
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
//			if(myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
//				showPrintDlg();
//			else
//				finish();
			
			if(myPref.getPreferences(MyPreferences.pref_print_receipt_transaction_payment)&&!isFromMainMenu)
			{
				
				new printAsync().execute();
				
				if(amountToBePaid>actualAmount)
					showChangeDlg();
			}
			else if(amountToBePaid>actualAmount)
				showChangeDlg();
			else
				finish();
			
		}
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
			if(myProgressDialog.isShowing())
				myProgressDialog.dismiss();
			myProgressDialog.show();

		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

//			if(isFromMainMenu||isInvoice)
//				Global.mainPrinterManager.currentDevice.printPaymentDetails(payment.getSetData("pay_id", true, null),1,false);
//			else
//				Global.mainPrinterManager.currentDevice.printPaymentDetails(payment.getSetData("job_id", true, null),0,false);
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
			
//			if(amountToBePaid<=actualAmount)
//				finish();
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
		sb.append("Change: ").append(change.getText().toString());
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
				finish();
			}
		});
		dlog.show();
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
		super.onDestroy();
		if (dialog != null) {
			dialog.create().dismiss();
		}
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int temp = 0;
		switch(v.getId())
		{
		case R.id.btnFive:
			temp = 5;
			break;
		case R.id.btnTen:
			temp = 10;
			break;
		case R.id.btnTwenty:
			temp = 20;
			break;
		case R.id.btnFourty:
			temp = 40;
			break;
		}
		
		amountToBePaid+=temp;
		grandTotalAmount = amountToBePaid+amountToTip;
		paid.setText(Global.formatDoubleToCurrency(amountToBePaid));
	}

}
