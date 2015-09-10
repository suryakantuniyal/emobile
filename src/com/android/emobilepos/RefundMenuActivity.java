package com.android.emobilepos;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import drivers.EMSPAT100;

import com.android.database.DrawInfoHandler;
import com.android.database.OrdersHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.TaxesHandler;
import com.android.database.VoidTransactionsHandler;
import com.android.emobilepos.payment.ProcessBoloro_FA;
import com.android.emobilepos.payment.ProcessGiftCard_FA;
import com.android.emobilepos.payment.ProcessTupyx_FA;
import com.android.ivu.MersenneTwisterFast;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.CreditCardInfo;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Payment;
import com.android.support.Post;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.zzzapi.uart.uart;

public class RefundMenuActivity extends Activity implements OnClickListener{

	private CardsListAdapter myAdapter;
	private ListView myListview;
	private String total;
	private String paid;
	private Activity activity;
	private String pay_id;
	private String job_id = "";							//invoice #
	private List<String[]> payType;
	private Bundle extras;
	
	private Global global;
	private boolean hasBeenCreated = false;
	private boolean isFromMainMenu = false;				//It was called from the main menu (display no Invoice#)
	private int typeOfProcedure = 0;
	
	private double overAllRemainingBalance = 0.00;
	private double currentPaidAmount = 0.00;
	private double tipPaidAmount = 0.00;
	private MyPreferences myPref;
	private ProgressDialog myProgressDialog;
	private int selectedPosition = 0;
	private PaymentsHandler paymentHandlerDB;
	private String previous_pay_id = "";
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		activity = this;
		setContentView(R.layout.card_list_layout);
		myListview = (ListView) findViewById(R.id.cardsListview);
		global = (Global)getApplication();
		extras = this.getIntent().getExtras();
		myPref = new MyPreferences(this);
		total = extras.getString("amount");
		paid = extras.getString("paid");
		isFromMainMenu = extras.getBoolean("isFromMainMenu");
		overAllRemainingBalance = Double.parseDouble(total);
		
		if(!isFromMainMenu)
		{
			job_id = extras.getString("job_id");
			typeOfProcedure = extras.getInt("typeOfProcedure");
		}
		
		paymentHandlerDB = new PaymentsHandler(this);
		GenerateNewID generator = new GenerateNewID(this);
		
		if (paymentHandlerDB.getDBSize() == 0)
			pay_id = generator.generate("",1);
		else
			pay_id = generator.generate(paymentHandlerDB.getLastPayID(),1);

		
		myAdapter = new CardsListAdapter(this);
		myListview.setAdapter(myAdapter);

		myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub.

				if (position > 0) {
					selectedPosition = position;
					if (payType.get(position - 1)[2].equals("Cash")) {
						Intent intent = new Intent(arg0.getContext(), ProcessCashMenuActivity.class);
						intent.putExtra("paymethod_id", payType.get(position - 1)[0]);

						initIntents(extras, intent);
					} else if (payType.get(position - 1)[2].equals("Check")) {
						Intent intent = new Intent(arg0.getContext(), ProcessCheckActivity.class);
						intent.putExtra("paymethod_id", payType.get(position - 1)[0]);
						initIntents(extras, intent);
					}
					else if(payType.get(position-1)[2].equals("Genius"))
					{
						Intent intent = new Intent(arg0.getContext(), ProcessGeniusActivity.class);
						intent.putExtra("paymethod_id", payType.get(position - 1)[0]);

						initIntents(extras, intent);
					}
					else if(payType.get(position-1)[2].equals("Wallet"))
					{
						Intent intent = new Intent(activity,ProcessTupyx_FA.class);
						intent.putExtra("paymethod_id", payType.get(position - 1)[0]);
						initIntents(extras,intent);
					}
					else if(payType.get(position-1)[2].equals("Boloro"))
					{
						showBoloroDlog();
					}
					else if(payType.get(position-1)[2].toUpperCase(Locale.getDefault()).contains("GIFT"))
					{
						Intent intent = new Intent(activity,ProcessGiftCard_FA.class);
						intent.putExtra("paymethod_id", payType.get(position - 1)[0]);
						initIntents(extras,intent);
					}
					else {
						Intent intent = new Intent(arg0.getContext(), ProcessCardMenuActivity.class);
						intent.putExtra("paymethod_id", payType.get(position - 1)[0]);

						initIntents(extras, intent);
					}
				}
			}
		});
		
		hasBeenCreated = true;
		
		
		
		if(Double.parseDouble(total)==0) //total to be paid is 0
		{
			setResult(-1);
			
			if(Global.loyaltyCardInfo!=null&&!Global.loyaltyCardInfo.getCardNumUnencrypted().isEmpty())
			{
				showPaymentSuccessDlog(true,true,"0");
			}
			else if(Global.rewardCardInfo!=null&&!Global.rewardCardInfo.getCardNumUnencrypted().isEmpty())
			{
				showPaymentSuccessDlog(true,true,"0");
			}
		}
		
		
		if(myPref.isSam4s(true, true))
		{
			StringBuilder sb = new StringBuilder();
			String row1 = "Grand Total";
			String row2 = sb.append(Global.formatDoubleStrToCurrency(total)).toString();
			uart uart_tool = new uart();
			uart_tool.config(3, 9600, 8, 1);
			uart_tool.write(3, Global.emptySpaces(40, 0, false));
			uart_tool.write(3, Global.formatSam4sCDT(row1, row2));
		} else if (myPref.isPAT100(true, true)) {
			StringBuilder sb = new StringBuilder();
			String row1 = "Grand Total";
			String row2 = sb.append(Global.formatDoubleStrToCurrency(total)).toString();
			EMSPAT100.getTerminalDisp().clearText();
			EMSPAT100.getTerminalDisp().displayText(Global.formatSam4sCDT(row1.toString(), row2.toString()));
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
	public void onDestroy()
	{
		super.onDestroy();
		if(dlog!=null)
			dlog.dismiss();
	}
	
	
	@Override
	public void onBackPressed() {

		if (typeOfProcedure==Integer.parseInt(Global.IS_SALES_RECEIPT)) {
			final Dialog dialog =  new Dialog(activity,R.style.Theme_TransparentTest);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setCancelable(true);
			dialog.setContentView(R.layout.void_dialog_layout);
			Button voidBut = (Button) dialog.findViewById(R.id.voidBut);
			Button notVoid = (Button) dialog.findViewById(R.id.notVoidBut);

			voidBut.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					if(myPref.getPreferences(MyPreferences.pref_require_password_to_remove_void))
					{
						dialog.dismiss();
						promptManagerPassword();
					}
					else
					{
						dialog.dismiss();
						voidTransaction();
					}
				}
			});
			notVoid.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			dialog.show();
		} else {
			if(typeOfProcedure== Integer.parseInt(Global.IS_RETURN))
			{
				setResult(3);
				finish();
			}
			else if((typeOfProcedure == Global.FROM_JOB_INVOICE || typeOfProcedure== Integer.parseInt(Global.IS_INVOICE)) && myPref.getPreferences(MyPreferences.pref_enable_printing))
			{
				if(!myPref.getPreferences(MyPreferences.pref_automatic_printing))
					showPrintDlg(false,false);
				else
					new printAsync().execute(false);
			}
			else
			{
				setResult(3);
				finish();
			}
			
		}
	}
	
	

	private void initIntents(Bundle extras, final Intent intent) {
		String payingAmount = total;
		int requestCode = 0;
		boolean isReceipt = false;

		String drawDate = new String();
		String ivuLottoNum = new String();

		if (Global.isIvuLoto) {
			DrawInfoHandler drawDateInfo = new DrawInfoHandler(activity);
			MersenneTwisterFast mersenneTwister = new MersenneTwisterFast();
			drawDate = drawDateInfo.getDrawDate();
			ivuLottoNum = mersenneTwister.generateIVULoto();
		}

		intent.putExtra("custidkey", Global.getValidString(extras.getString("custidkey")));
		intent.putExtra("cust_id", Global.getValidString(extras.getString("cust_id")));

		if (overAllRemainingBalance != 0)
			payingAmount = Global.formatNumber(true, overAllRemainingBalance);

		if (extras.getBoolean("salespayment")) {
			intent.putExtra("salespayment", true);
		} else if (extras.getBoolean("salesrefund")) {
			intent.putExtra("salesrefund", true);
			drawDate = "";
			ivuLottoNum = "";
		} else if (extras.getBoolean("salesreceipt")) {
			intent.putExtra("salesreceipt", true);
			isReceipt = true;
			requestCode = Global.FROM_JOB_SALES_RECEIPT;
		} else if (extras.getBoolean("histinvoices")) {
			intent.putExtra("histinvoices", true);
			requestCode = Global.FROM_OPEN_INVOICES;

			if (!extras.getBoolean("isMultipleInvoice")) {
				intent.putExtra("isMultipleInvoice", false);
				intent.putExtra("inv_id", extras.getString("inv_id"));

			} else {
				intent.putExtra("isMultipleInvoice", true);
				intent.putExtra("inv_id_array", extras.getStringArray("inv_id_array"));
				intent.putExtra("balance_array", extras.getDoubleArray("balance_array"));
				intent.putExtra("txnID_array", extras.getStringArray("txnID_array"));

			}
		} else if (extras.getBoolean("salesinvoice")) {
			intent.putExtra("salesinvoice", true);
			requestCode = Global.FROM_JOB_INVOICE;
		}

		if (Global.isIvuLoto) {
			intent.putExtra("IvuLottoNumber", ivuLottoNum);
			intent.putExtra("IvuLottoDrawDate", drawDate);

			if (!extras.getBoolean("salesrefund") && !extras.getBoolean("salespayment") && !extras.getBoolean("histinvoices")) {
				double subtotal = Double.parseDouble(extras.getString("ord_subtotal"));
				String taxID = extras.getString("ord_taxID");

				TaxesHandler taxHandler = new TaxesHandler(activity);
				List<String[]> groupTax = taxHandler.getGroupTaxRate(taxID);

				if (groupTax.size() > 0) {
					BigDecimal tempRate;
					if (groupTax.get(0)[2].equals("Tax1")) {
						tempRate = new BigDecimal(subtotal * Double.parseDouble(groupTax.get(0)[1])).setScale(2, BigDecimal.ROUND_UP);
						intent.putExtra("Tax1_amount", tempRate.toPlainString());
						intent.putExtra("Tax1_name", groupTax.get(0)[0]);

						tempRate = new BigDecimal(subtotal * Double.parseDouble(groupTax.get(1)[1])).setScale(2, BigDecimal.ROUND_UP);
						intent.putExtra("Tax2_amount", tempRate.toPlainString());
						intent.putExtra("Tax2_name", groupTax.get(0)[0]);
					} else {
						tempRate = new BigDecimal(subtotal * Double.parseDouble(groupTax.get(0)[1])).setScale(2, BigDecimal.ROUND_UP);
						intent.putExtra("Tax2_amount", tempRate.toPlainString());
						intent.putExtra("Tax2_name", groupTax.get(0)[0]);

						tempRate = new BigDecimal(subtotal * Double.parseDouble(groupTax.get(1)[1])).setScale(2, BigDecimal.ROUND_UP);
						intent.putExtra("Tax1_amount", tempRate.toPlainString());
						intent.putExtra("Tax1_name", groupTax.get(0)[0]);
					}
				} else {
					BigDecimal tempRate;
					tempRate = new BigDecimal(subtotal * 0.06).setScale(2, BigDecimal.ROUND_UP);
					intent.putExtra("Tax1_amount", tempRate.toPlainString());
					intent.putExtra("Tax1_name", "Estatal");

					tempRate = new BigDecimal(subtotal * 0.01).setScale(2, BigDecimal.ROUND_UP);
					intent.putExtra("Tax2_amount", tempRate.toPlainString());
					intent.putExtra("Tax2_name", "Municipal");
				}
			} else {
				BigDecimal tempRate;
				double tempAmount = Double.parseDouble(payingAmount);
				if (tempAmount > 0) {
					tempRate = new BigDecimal(tempAmount * 0.06).setScale(2, BigDecimal.ROUND_UP);
					intent.putExtra("Tax1_amount", tempRate.toPlainString());
					intent.putExtra("Tax1_name", "Estatal");

					tempRate = new BigDecimal(tempAmount * 0.01).setScale(2, BigDecimal.ROUND_UP);
					intent.putExtra("Tax2_amount", tempRate.toPlainString());
					intent.putExtra("Tax2_name", "Municipal");
				} else {
					intent.putExtra("Tax1_amount", "");
					intent.putExtra("Tax1_name", "");
					intent.putExtra("Tax2_amount", "");
					intent.putExtra("Tax2_name", "");
				}
			}
		}

		intent.putExtra("amount", payingAmount);
		intent.putExtra("job_id", job_id);
		intent.putExtra("pay_id", pay_id);
		intent.putExtra("isFromSalesReceipt", isReceipt);
		intent.putExtra("isFromMainMenu", isFromMainMenu);

		startActivityForResult(intent, requestCode);
	}
	
	

	private class CardsListAdapter extends BaseAdapter implements Filterable {
		private Context context;
		private LayoutInflater myInflater;

		public CardsListAdapter(Activity activity) {
			this.context = activity.getApplicationContext();
			myInflater = LayoutInflater.from(context);
			
			PayMethodsHandler handler = new PayMethodsHandler(activity);
			payType = handler.getPayMethod();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			ViewHolder holder;
			int type = getItemViewType(position);
			int iconId = 0;

			if (convertView == null) {

				holder = new ViewHolder();
				switch (type) {
				case 0:// dividers layout
					convertView = myInflater.inflate(R.layout.card_listrow1_adapter, null);

					holder.totalView = (TextView) convertView.findViewById(R.id.totalValue);
					holder.paidView = (TextView) convertView.findViewById(R.id.paidValue);
					holder.tipView	=	(TextView)convertView.findViewById(R.id.tipValue);
					holder.dueView	=	(TextView)convertView.findViewById(R.id.dueValue);

					holder.totalView.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(total))));
					holder.paidView.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(paid))));
					holder.dueView.setText(Global.getCurrencyFormat(Global.formatNumToLocale(overAllRemainingBalance)));
					holder.tipView.setText(Global.getCurrencyFormat(Global.formatNumToLocale(tipPaidAmount)));
					
					break;
				case 1: // header
					convertView = myInflater.inflate(R.layout.card_listrow2_adapter, null);

					holder.textLine2 = (TextView) convertView.findViewById(R.id.cardsListname);
					String key = payType.get(position - 1)[2];
					String name = payType.get(position-1)[1];
				
					String iconName = Global.paymentIconsMap.get(key);
					if(iconName == null)
						iconId = context.getResources().getIdentifier("debit", "drawable", context.getString(R.string.pkg_name));
					else
						iconId = context.getResources().getIdentifier(iconName, "drawable", context.getString(R.string.pkg_name));
					holder.textLine2.setTag(name);
					holder.textLine2.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(iconId), null, null, null);
					holder.textLine2.setText(name);

					break;
				}

				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
				if (type == 0)
				{
					holder.totalView.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(total))));
					holder.paidView.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(paid))));
					holder.dueView.setText(Global.getCurrencyFormat(Global.formatNumToLocale(overAllRemainingBalance)));
					holder.tipView.setText(Global.getCurrencyFormat(Global.formatNumToLocale(tipPaidAmount)));
				}

				else 
				{
					String key = payType.get(position - 1)[2];
					String name = payType.get(position-1)[1];
					String iconName = Global.paymentIconsMap.get(key);
					if(iconName == null)
						iconId = context.getResources().getIdentifier("debit", "drawable", context.getString(R.string.pkg_name));
					else
						iconId = context.getResources().getIdentifier(iconName, "drawable", context.getString(R.string.pkg_name));
					holder.textLine2.setTag(name);
					holder.textLine2.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(iconId), null, null, null);
					holder.textLine2.setText(name);
				}
			}
			return convertView;
		}

		public class ViewHolder {
			TextView totalView,paidView,tipView,dueView,textLine2;

		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0) {
				return 0;
			}
			return 1;

		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return payType.size() + 1;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Filter getFilter() {
			// TODO Auto-generated method stub
			return null;
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
				viewMsg.setText(R.string.dlog_msg_want_to_reprint);
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
				if(overAllRemainingBalance<=0)
					activity.finish();
			}
		});
		dlog.show();
	}
		
	
	
	private class printAsync extends AsyncTask<Boolean, String, String> 
	{
		private boolean wasReprint = false;
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
		protected String doInBackground(Boolean... params) {
			// TODO Auto-generated method stub

			wasReprint = params[0];
			if(Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice!=null)
			{
				if(isFromMainMenu||extras.getBoolean("histinvoices"))
					printSuccessful = Global.mainPrinterManager.currentDevice.printPaymentDetails(previous_pay_id,1,wasReprint);
				else
					printSuccessful = Global.mainPrinterManager.currentDevice.printTransaction(job_id, typeOfProcedure,wasReprint,false);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss();
			if(printSuccessful)
			{
				if(overAllRemainingBalance<=0||(typeOfProcedure == Global.FROM_JOB_INVOICE || typeOfProcedure== Integer.parseInt(Global.IS_INVOICE)))
					activity.finish();
			}
			else
			{
				showPrintDlg(wasReprint,true);
			}
		}
	}
		
	
	
	private void promptManagerPassword()
	{
		final Dialog globalDlog = new Dialog(activity,R.style.Theme_TransparentTest);
		globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		globalDlog.setCancelable(true);
		globalDlog.setContentView(R.layout.dlog_field_single_layout);
		
		final EditText viewField = (EditText)globalDlog.findViewById(R.id.dlogFieldSingle);
		viewField.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
		TextView viewTitle = (TextView)globalDlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)globalDlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);

			viewMsg.setText(R.string.dlog_title_enter_manager_password);
		
		Button btnOk = (Button)globalDlog.findViewById(R.id.btnDlogSingle);
		btnOk.setText(R.string.button_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				globalDlog.dismiss();
				String pass = viewField.getText().toString();
				if(!pass.isEmpty()&&myPref.posManagerPass(true, null).equals(pass.trim()))
				{
					voidTransaction();
				}
				else
				{
					promptManagerPassword();
				}
			}
		});
		globalDlog.show();
	}
	
	
	
	private List<Payment>listVoidPayments;
	private PaymentsHandler payHandler;
	private void voidTransaction()
	{
		OrdersHandler handler = new OrdersHandler(activity);
		handler.updateIsVoid(job_id);
		handler.updateIsProcessed(job_id, "9");
		
		VoidTransactionsHandler voidHandler = new VoidTransactionsHandler(activity);
		HashMap<String,String> voidedTrans = new HashMap<String,String>();
		
		voidedTrans.put("ord_id", job_id);
		voidedTrans.put("ord_type", extras.getString("ord_type"));
		voidHandler.insert(voidedTrans);
		
		payHandler = new PaymentsHandler(activity);
		
		listVoidPayments = payHandler.getOrderPayments(job_id);
		int size = listVoidPayments.size();
		if(size>0)
		{
			new voidPaymentAsync().execute();
		}
		else
		{
			setResult(3);
			finish();
		}
	}
	
	
	public class voidPaymentAsync extends AsyncTask<Void, Void, Void> {
		
		//private String[]returnedPost;
		boolean wasProcessed = false;
		HashMap<String,String>parsedMap = new HashMap<String,String>();
		private String errorMsg = "Could not process the payment.";
		private int errCount = 0;
		

		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage("Voiding Payments...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			
			int size = listVoidPayments.size();
			EMSPayGate_Default payGate;
			
			Post post = new Post();
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);
			String xml = "";
			InputSource inSource;
			SAXParser sp;
			XMLReader xr;
			
			try {
				sp = spf.newSAXParser();
				xr = sp.getXMLReader();
				String paymentType = "";
			for(int i = 0;i<size;i++)
			{
				paymentType = listVoidPayments.get(i).getSetData("card_type", true, null).toUpperCase(Locale.getDefault()).trim();
				if(paymentType.equals("GIFTCARD"))
				{					
					payGate = new EMSPayGate_Default(activity,listVoidPayments.get(i));
					xml = post.postData(13, activity, payGate.paymentWithAction("VoidGiftCardAction",false,listVoidPayments.get(i).getSetData("card_type", true, null),null));
					inSource = new InputSource(new StringReader(xml));
					
					xr.setContentHandler(handler);
					xr.parse(inSource);
					parsedMap = handler.getData();
					
					if(parsedMap!=null&&parsedMap.size()>0&&parsedMap.get("epayStatusCode").equals("APPROVED"))
						payHandler.createVoidPayment(listVoidPayments.get(i), true, parsedMap);
					else
						errCount++;
					parsedMap.clear();
				}
				else if(paymentType.equals("CASH"))
				{
					
					//payHandler.updateIsVoid(pay_id);
					payHandler.createVoidPayment(listVoidPayments.get(i), false, null);
				}
				else if(!paymentType.equals("CHECK")&&!paymentType.equals("WALLET"))
				{
					payGate = new EMSPayGate_Default(activity,listVoidPayments.get(i));
					xml = post.postData(13, activity, payGate.paymentWithAction("VoidCreditCardAction",false,listVoidPayments.get(i).getSetData("card_type", true, null),null));
					inSource = new InputSource(new StringReader(xml));
					
					xr.setContentHandler(handler);
					xr.parse(inSource);
					parsedMap = handler.getData();
					
					if(parsedMap!=null&&parsedMap.size()>0&&parsedMap.get("epayStatusCode").equals("APPROVED"))
						payHandler.createVoidPayment(listVoidPayments.get(i), true, parsedMap);
					else
						errCount++;
					
					parsedMap.clear();
				}
			}
			}catch (Exception e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.emobilepos.HistPayDetailsFragment (at Class.processVoidCardAsync)]");
				
				Tracker tracker = EasyTracker.getInstance(activity);
				tracker.send(MapBuilder.createException(sb.toString(), false).build());
			}
			

			
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			myProgressDialog.dismiss();
			
			setResult(3);
			finish();
		}
	}
	
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	
		if (resultCode == -1) 
		{
			if(requestCode==Global.FROM_OPEN_INVOICES)
				setResult(Global.FROM_PAYMENT);
			else
				setResult(-1);
			showPaymentSuccessDlog(true,false,null);
		}
		else if(resultCode==-2)
		{
			
			currentPaidAmount = currentPaidAmount + Double.parseDouble(Global.amountPaid);
			Global.overallPaidAmount = currentPaidAmount;
			tipPaidAmount+=Double.parseDouble(Global.tipPaid);
			paid = Global.formatNumber(true, currentPaidAmount);

			if(total.replaceAll("[^\\d\\,\\.]", "").trim().equals("0.00")&&data!=null)
			{
				total = data.getStringExtra("total_amount");
			}
			overAllRemainingBalance = Global.formatNumFromLocale(Global.addSubsStrings(false, Global.formatNumToLocale(Double.parseDouble(total)), Global.formatNumToLocale(Double.parseDouble(paid))));

			myAdapter.notifyDataSetChanged();
			
			
			if(requestCode==Global.FROM_OPEN_INVOICES)
				setResult(Global.FROM_PAYMENT);
			else
				setResult(-1);
			
			if(overAllRemainingBalance>0)
			{

				GenerateNewID generator = new GenerateNewID(this);
				previous_pay_id = pay_id;
				pay_id = generator.generate(pay_id,1);
				String temp = Global.formatDoubleStrToCurrency("0.00");
				
				if(isFromMainMenu||extras.getBoolean("histinvoices"))
					showPaymentSuccessDlog(true,true,temp);
				else
					showPaymentSuccessDlog(false,true,temp);
				
//				if(myPref.getPreferences(MyPreferences.pref_automatic_printing))
//					showPaymentSuccessDlog(false,true,temp);
//				else
//					showPaymentSuccessDlog(true,true,temp);
			}
			
			else
			{
				String temp = Global.formatDoubleStrToCurrency(Double.toString(overAllRemainingBalance));
				previous_pay_id = pay_id;
//				if(isFromMainMenu)
//					finish();
//				else
//				if(myPref.getPreferences(MyPreferences.pref_automatic_printing))
//					showPaymentSuccessDlog(false,true,temp);
//				else
					showPaymentSuccessDlog(true,true,temp);
			}
		}
	}

	
	
	
	private Dialog dlog;
	
	private void showBoloroDlog()
	{
		dlog = new Dialog(this,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(true);
		dlog.setCanceledOnTouchOutside(true);
		dlog.setContentView(R.layout.dlog_btn_top_bottom_layout);
		
		TextView title = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView msg = (TextView)dlog.findViewById(R.id.dlogMessage);
		Button btnTapPay = (Button)dlog.findViewById(R.id.btnDlogTop);
		Button btnManual = (Button)dlog.findViewById(R.id.btnDlogBottom);
		btnTapPay.setOnClickListener(this);
		btnManual.setOnClickListener(this);
		btnTapPay.setText(R.string.tap_and_pay);
		btnManual.setText(R.string.manual);
		
		title.setText(R.string.dlog_title_choose_action);
		msg.setText(R.string.boloro_payment_method);
		dlog.show();
	}
	
	
	
	private void showPaymentSuccessDlog(final boolean withPrintRequest,boolean showRefund,String amount)
	{
		
		dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(false);
		dlog.setContentView(R.layout.dlog_btn_single_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);
		viewMsg.setText(R.string.payment_saved_successfully);
		

		
		
		Button btnOk = (Button)dlog.findViewById(R.id.btnDlogSingle);
		btnOk.setText(R.string.button_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				if(withPrintRequest)
				{
					if(Global.loyaltyCardInfo!=null&&!Global.loyaltyCardInfo.getCardNumUnencrypted().isEmpty())
					{
						processInquiry(true);
					}
					else if(Global.rewardCardInfo!=null&&!Global.rewardCardInfo.getCardNumUnencrypted().isEmpty())
					{
						processInquiry(false);
					}
					else
					{
						if(myPref.getPreferences(MyPreferences.pref_enable_printing)&&!myPref.getPreferences(MyPreferences.pref_automatic_printing))
						{
							showPrintDlg(false,false);
						}
//						else
//						{
//							finish();
//						}
					}
				}
				else if(overAllRemainingBalance <=0)
				{
					finish();
				}
			}
		});
		dlog.show();
		
		dlog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				handler.removeCallbacks(runnable);
			}
		});
		
		
		if(withPrintRequest&&myPref.getPreferences(MyPreferences.pref_enable_printing)&&myPref.getPreferences(MyPreferences.pref_automatic_printing))
		{
			if(Global.loyaltyCardInfo!=null&&!Global.loyaltyCardInfo.getCardNumUnencrypted().isEmpty())
			{
				processInquiry(true);
			}
			else if(Global.rewardCardInfo!=null&&!Global.rewardCardInfo.getCardNumUnencrypted().isEmpty())
			{
				processInquiry(false);
			}
			else
			{
				new printAsync().execute(false);
			}
			
			//handler.postDelayed(runnable, 2000);
		}
	}

	
	private Handler handler = new Handler();
	private Runnable runnable = new Runnable()
	{
		@Override
		public void run()
		{
			if(dlog!=null&&dlog.isShowing())
				dlog.dismiss();
		}
	};
	
	
	
	
	

	
	private CreditCardInfo cardInfoManager;
	private String reqChargeLoyaltyReward,reqAddLoyalty;
	private Payment loyaltyRewardPayment;

	private void processInquiry(boolean isLoyalty) 
	{
	
			loyaltyRewardPayment = new Payment(this);
			Bundle extras = getIntent().getExtras();
			
			String cardType = "LoyaltyCard";
			if(isLoyalty)
				cardInfoManager = Global.loyaltyCardInfo;
			else
			{
				cardInfoManager = Global.rewardCardInfo;
				cardType = "Reward";
			}
			
			
			GenerateNewID generator = new GenerateNewID(this);
			String tempPay_id;
			if (paymentHandlerDB.getDBSize() == 0)
				tempPay_id = generator.generate("",1);
			else
				tempPay_id = generator.generate(paymentHandlerDB.getLastPayID(),1);
			
			loyaltyRewardPayment.getSetData("pay_id", false, tempPay_id);
			

			loyaltyRewardPayment.getSetData("cust_id", false, Global.getValidString(extras.getString("cust_id")));
			loyaltyRewardPayment.getSetData("custidkey", false, Global.getValidString(extras.getString("custidkey")));
			loyaltyRewardPayment.getSetData("emp_id", false, myPref.getEmpID());	
			loyaltyRewardPayment.getSetData("job_id", false, job_id);
			
			loyaltyRewardPayment.getSetData("pay_name", false, cardInfoManager.getCardOwnerName());
			loyaltyRewardPayment.getSetData("pay_ccnum", false, cardInfoManager.getCardNumAESEncrypted());

			loyaltyRewardPayment.getSetData("ccnum_last4", false, cardInfoManager.getCardLast4());
			loyaltyRewardPayment.getSetData("pay_expmonth", false, cardInfoManager.getCardExpMonth());
			loyaltyRewardPayment.getSetData("pay_expyear", false, cardInfoManager.getCardExpYear());
			loyaltyRewardPayment.getSetData("pay_seccode", false, cardInfoManager.getCardEncryptedSecCode());

			loyaltyRewardPayment.getSetData("track_one", false, cardInfoManager.getEncryptedAESTrack1());
			loyaltyRewardPayment.getSetData("track_two", false, cardInfoManager.getEncryptedAESTrack2());

			

			
			
			cardInfoManager.setRedeemAll("1");
			cardInfoManager.setRedeemType("Only");
			
			
			
			
			loyaltyRewardPayment.getSetData("paymethod_id", false, cardType);
			loyaltyRewardPayment.getSetData("card_type", false, cardType);
			loyaltyRewardPayment.getSetData("pay_type", false, "0");
			
			if(isLoyalty)
			{
				loyaltyRewardPayment.getSetData("pay_amount", false, Global.loyaltyCharge);
				EMSPayGate_Default payGate = new EMSPayGate_Default(this, loyaltyRewardPayment);
				boolean wasSwiped = cardInfoManager.getWasSwiped();
				
				reqChargeLoyaltyReward = payGate.paymentWithAction("ChargeLoyaltyCardAction", wasSwiped, cardType, cardInfoManager);
				
	
				loyaltyRewardPayment.getSetData("pay_amount", false, Global.loyaltyAddAmount);
				payGate = new EMSPayGate_Default(this,loyaltyRewardPayment);
				reqAddLoyalty = payGate.paymentWithAction("AddValueLoyaltyCardAction", wasSwiped, cardType, cardInfoManager);
				loyaltyRewardPayment.getSetData("pay_amount", false, Global.loyaltyCharge);
				
				new processLoyaltyAsync().execute();
			}
			else
			{
				BigDecimal bdTotal = new BigDecimal(total);
				BigDecimal bdOrigAmount = new BigDecimal(cardInfoManager.getOriginalTotalAmount());
				
				loyaltyRewardPayment.getSetData("originalTotalAmount", false, bdTotal.add(bdOrigAmount).subtract(Global.rewardChargeAmount).toString());
				loyaltyRewardPayment.getSetData("pay_amount", false, Global.rewardChargeAmount.toString());
				EMSPayGate_Default payGate = new EMSPayGate_Default(this, loyaltyRewardPayment);
				boolean wasSwiped = cardInfoManager.getWasSwiped();
				reqChargeLoyaltyReward = payGate.paymentWithAction("ChargeRewardAction", wasSwiped, cardType, cardInfoManager);
				
				new processRewardAsync().execute();
			}
			
	}

	
	
	private class processLoyaltyAsync extends AsyncTask<Void, Void, Void> {

		private HashMap<String, String> parsedMap = new HashMap<String, String>();
		private boolean wasProcessed = false;
		private String errorMsg = "Loyalty could not be processed.";

		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage(getString(R.string.processing_loyalty_card));
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			Post httpClient = new Post();

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);

			try {
				String xml = httpClient.postData(13, activity, reqChargeLoyaltyReward);
				if (xml.equals(Global.TIME_OUT)) {
					errorMsg = "TIME OUT, would you like to try again?";
				} else if (xml.equals(Global.NOT_VALID_URL)) {
					errorMsg = "Loyalty could not be processed....";
				} else {
					InputSource inSource = new InputSource(new StringReader(xml));

					SAXParser sp = spf.newSAXParser();
					XMLReader xr = sp.getXMLReader();
					xr.setContentHandler(handler);
					xr.parse(inSource);
					parsedMap = handler.getData();

					if (parsedMap != null && parsedMap.size() > 0 && parsedMap.get("epayStatusCode").equals("APPROVED"))
					{
						xml = httpClient.postData(13, activity, reqAddLoyalty);
						inSource = new InputSource(new StringReader(xml));
						xr.parse(inSource);
						parsedMap = handler.getData();
						
						if (parsedMap != null && parsedMap.size() > 0 && parsedMap.get("epayStatusCode").equals("APPROVED"))
						{
							wasProcessed = true;
						}
						else if (parsedMap != null && parsedMap.size() > 0) {
							StringBuilder sb = new StringBuilder();
							sb.append("statusCode = ").append(parsedMap.get("statusCode")).append("\n");
							sb.append(parsedMap.get("statusMessage"));
							errorMsg = sb.toString();
						} else
							errorMsg = xml;

					}
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
		protected void onPostExecute(Void unused) {
			myProgressDialog.dismiss();

			if (wasProcessed) // payment processing succeeded
			{
				StringBuilder sb = new StringBuilder();
				String temp = (parsedMap.get("CardBalance")==null?"0.0":parsedMap.get("CardBalance"));
				
				//sb.append("Card Balance:  ").append(temp);
				sb.append("Card was processed");
				loyaltyRewardPayment.getSetData("pay_issync", false, "1");
				paymentHandlerDB.insert(loyaltyRewardPayment);
				showBalancePrompt(sb.toString());
			} else // payment processing failed
			{
				showBalancePrompt(errorMsg);
			}
		}
	}
	
	
	
	
	private class processRewardAsync extends AsyncTask<Void, Void, Void> {

		private HashMap<String, String> parsedMap = new HashMap<String, String>();
		private boolean wasProcessed = false;
		private String errorMsg = "Reward could not be processed.";

		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage(getString(R.string.processing_reward));
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			Post httpClient = new Post();

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);

			try {
				String xml = httpClient.postData(13, activity, reqChargeLoyaltyReward);
				Global.generateDebugFile(reqChargeLoyaltyReward);
				if (xml.equals(Global.TIME_OUT)) {
					errorMsg = "TIME OUT, would you like to try again?";
				} else if (xml.equals(Global.NOT_VALID_URL)) {
					errorMsg = "Loyalty could not be processed....";
				} else {
					InputSource inSource = new InputSource(new StringReader(xml));

					SAXParser sp = spf.newSAXParser();
					XMLReader xr = sp.getXMLReader();
					xr.setContentHandler(handler);
					xr.parse(inSource);
					parsedMap = handler.getData();

					if (parsedMap != null && parsedMap.size() > 0 && parsedMap.get("epayStatusCode").equals("APPROVED"))
					{
						wasProcessed = true;
//						xml = httpClient.postData(13, activity, reqAddLoyalty);
//						inSource = new InputSource(new StringReader(xml));
//						xr.parse(inSource);
//						parsedMap = handler.getData();
//						
//						if (parsedMap != null && parsedMap.size() > 0 && parsedMap.get("epayStatusCode").equals("APPROVED"))
//						{
//							wasProcessed = true;
//						}
//						else if (parsedMap != null && parsedMap.size() > 0) {
//							StringBuilder sb = new StringBuilder();
//							sb.append("statusCode = ").append(parsedMap.get("statusCode")).append("\n");
//							sb.append(parsedMap.get("statusMessage"));
//							errorMsg = sb.toString();
//						} else
//							errorMsg = xml;

					}
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
		protected void onPostExecute(Void unused) {
			myProgressDialog.dismiss();

			if (wasProcessed) // payment processing succeeded
			{
				StringBuilder sb = new StringBuilder();
				String temp = (parsedMap.get("CardBalance")==null?"0.0":parsedMap.get("CardBalance"));
				
				//sb.append("Card Balance:  ").append(temp);
				sb.append("Card was processed");
				loyaltyRewardPayment.getSetData("pay_issync", false, "1");
				paymentHandlerDB.insert(loyaltyRewardPayment);
				showBalancePrompt(sb.toString());
			} else // payment processing failed
			{
				showBalancePrompt(errorMsg);
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
				if(myPref.getPreferences(MyPreferences.pref_enable_printing))
				{
					if(!myPref.getPreferences(MyPreferences.pref_automatic_printing))
						showPrintDlg(false,false);
					else
						new printAsync().execute(false);
				}
				else
				{
					finish();
				}
			}
		});
		dlog.show();
	}
	
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(activity,ProcessBoloro_FA.class);
		intent.putExtra("paymethod_id",  payType.get(selectedPosition - 1)[0]);
		switch(v.getId())
		{
		case R.id.btnDlogTop:
			dlog.dismiss();
			intent.putExtra("isNFC", true);
			initIntents(extras,intent);
			break;
		case R.id.btnDlogBottom:
			dlog.dismiss();
			intent.putExtra("isNFC", false);
			initIntents(extras,intent);
			break;
		}
	}
}
