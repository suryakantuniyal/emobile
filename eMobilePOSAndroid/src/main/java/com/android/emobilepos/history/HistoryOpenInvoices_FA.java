package com.android.emobilepos.history;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.android.database.InvoicePaymentsHandler;
import com.android.database.InvoicesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.history.details.HistoryOpenInvoicesDetails_FA;
import com.android.emobilepos.payment.SelectPayMethod_FA;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class HistoryOpenInvoices_FA extends BaseFragmentActivityActionBar implements OnClickListener, OnItemClickListener {

	private boolean hasBeenCreated = false;
	private Global global;
	private Activity activity;
	
	private CustomCursorAdapter myAdapter;
	private Cursor myCursor;

	private ListView myListView;
	private InvoicesHandler handler;
	private String chosenInvID = "";
	private String balanceDue, totalCostAmount = "";
	private boolean isFromMainMenu = false;
	private MyPreferences myPref;
	private List<String>inv_list;
	private List<String>txnIDList;
	private List<Double>total_list ;
	private List<String>chosenInvIDList ;
	private boolean isMultiInvoice = false;
	private ProgressDialog myProgressDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hist_invoices_listview);
		global = (Global)getApplication();
		activity = this;
		
		myListView = (ListView) findViewById(R.id.invoiceLV);
		TextView headerTitle = (TextView) findViewById(R.id.invoicesHeaderTitle);
		Button payButton = (Button) findViewById(R.id.payInvoiceButton);
		headerTitle.setText(getResources().getString(R.string.hist_open_inv));
		this.handler = new InvoicesHandler(this);
		
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
			isFromMainMenu = extras.getBoolean("isFromMainMenu", false);
		myPref = new MyPreferences(this);

		if (!isFromMainMenu)
		{
			myCursor = handler.getInvoicesList();
			payButton.setVisibility(View.INVISIBLE);
		}
		else
		{
			myCursor = handler.getListSpecificInvoice(myPref.getCustID());
		}
		
		

		EditText field = (EditText) findViewById(R.id.searchField);
		field.setOnEditorActionListener(getEditorActionListener());
		field.addTextChangedListener(getTextChangedListener());

		payButton.setOnClickListener(this);
			
		myAdapter = new CustomCursorAdapter(this, myCursor,CursorAdapter.NO_SELECTION);
		myListView.setOnItemClickListener(this);
		myListView.setAdapter(myAdapter);
		
		hasBeenCreated = true;
	}
	
	@Override
	public void onResume() {

		if(global.isApplicationSentToBackground(activity))
			global.loggedIn = false;
		global.stopActivityTransitionTimer();
		
		if(hasBeenCreated&&!global.loggedIn)
		{
			if(global.getGlobalDlog()!=null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(activity);
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		myCursor.moveToPosition(position);
		if (myCursor.getString(myCursor.getColumnIndex("inv_ispaid"))
				.equals("0"))
			showPrintDlg(position,false);
		else
			showPrintDlg(position,true);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int size = myAdapter.getCheckedItemSize();
		if(size>0)
		{
			isMultiInvoice = true;
			intentMultiplePayment(-1);
		}
		else
			Toast.makeText(this, "Please select an Invoice...",Toast.LENGTH_LONG).show();
	}
	
	
	private TextWatcher getTextChangedListener()
	{
		TextWatcher txtWatcher = new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				String test = s.toString().trim();
				if (test.isEmpty()) {
					if (myCursor != null)
						myCursor.close();

					if (!isFromMainMenu)
						myCursor = handler.getInvoicesList();
					else
						myCursor = handler.getListSpecificInvoice(myPref.getCustID());

					myAdapter = new CustomCursorAdapter(activity,myCursor, CursorAdapter.NO_SELECTION);
					myListView.setAdapter(myAdapter);
				}
			}
		};
		
		return txtWatcher;
	}
	
	private OnEditorActionListener getEditorActionListener()
	{
		OnEditorActionListener actionListener = new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					String text = v.getText().toString().trim();
					if (!text.isEmpty())
						performSearch(text);
					return true;
				}
				return false;
			}
		};
		
		return actionListener;
	}


	
	private void intentSinglePayment(int position)
	{
		myCursor.moveToPosition(position);
		Intent intent = new Intent(this,SelectPayMethod_FA.class);
		intent.putExtra("histinvoices", true);
		intent.putExtra("isMultipleInvoice", isMultiInvoice);
		chosenInvID = myCursor.getString(myCursor.getColumnIndex("_id"));
		String txnIDChosen = myCursor.getString(myCursor.getColumnIndex("txnID"));
		totalCostAmount = myCursor.getString(myCursor.getColumnIndex("inv_total"));
		balanceDue = myCursor.getString(myCursor.getColumnIndex("inv_balance"));

		intent.putExtra("cust_id", myCursor.getString(myCursor.getColumnIndex("cust_id")));
		intent.putExtra("custidkey", myCursor.getString(myCursor.getColumnIndex("custidkey")));
		intent.putExtra("inv_id", txnIDChosen);

		String temp = myCursor.getString(myCursor
				.getColumnIndex("inv_balance"));
		intent.putExtra("amount", temp);
		
		temp = Double.toString(Global.formatNumFromLocale(Global.addSubsStrings(false, Global.formatNumToLocale(Double.parseDouble(totalCostAmount)),Global.formatNumToLocale(Double.parseDouble(balanceDue)))));
		intent.putExtra("paid", temp);

		startActivityForResult(intent,Global.FROM_OPEN_INVOICES);
	}
	
	private void intentMultiplePayment(int position)
	{
		myAdapter.getCheckedItems();
		Intent intent = new Intent(this,SelectPayMethod_FA.class);
		intent.putExtra("histinvoices", true);
		intent.putExtra("isMultipleInvoice", isMultiInvoice);
		
		String [] inv_array = null;
		Double [] balance_array=null;
		String[] txnID_array=null;
	
		if(inv_list.size()==0)
		{
			myCursor.moveToPosition(position);
			chosenInvIDList.add(myCursor.getString(myCursor.getColumnIndex("_id")));
			txnIDList.add(myCursor.getString(myCursor.getColumnIndex("txnID")));
			inv_list.add(myCursor.getString(myCursor.getColumnIndex("_id")));
			String tempBalanceDue = myCursor.getString(myCursor.getColumnIndex("inv_balance"));
			total_list.add(Double.parseDouble(tempBalanceDue));
			
			totalCostAmount = myCursor.getString(myCursor.getColumnIndex("inv_total"));
			balanceDue = tempBalanceDue;
			
			intent.putExtra("cust_id", myCursor.getString(myCursor.getColumnIndex("cust_id")));
			intent.putExtra("custidkey", myCursor.getString(myCursor.getColumnIndex("custidkey")));
			
			inv_array = new String[inv_list.size()];
			balance_array = new Double[total_list.size()];
			txnID_array = new String[txnIDList.size()];
		}
		else
		{

			intent.putExtra("cust_id", myPref.getCustID());
			intent.putExtra("custidkey", myPref.getCustIDKey());
			inv_array = new String[inv_list.size()];
			balance_array = new Double[total_list.size()];
			txnID_array = new String[txnIDList.size()];
		}
		
		
		inv_list.toArray(inv_array);
		total_list.toArray(balance_array);
		txnIDList.toArray(txnID_array);
		
		intent.putExtra("inv_id_array", inv_array);
		intent.putExtra("txnID_array", txnID_array);
		intent.putExtra("balance_array",toPrimitiveDouble(balance_array));
		

		String tempBalance = balanceDue;
		intent.putExtra("amount", tempBalance);
		
		tempBalance = Double.toString(Global.formatNumFromLocale(Global.addSubsStrings(false, Global.formatNumToLocale(Double.parseDouble(totalCostAmount)),Global.formatNumToLocale(Double.parseDouble(balanceDue)))));
		intent.putExtra("paid", tempBalance);

		startActivityForResult(intent,Global.FROM_OPEN_INVOICES);
	}
	
	
	

	
	private void showPrintDlg(final int pos,boolean isPaid) 
	{
		myCursor.moveToPosition(pos);
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(true);
		dlog.setCanceledOnTouchOutside(true);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_choose_action);
		viewMsg.setVisibility(View.GONE);
		Button btnPrint = (Button)dlog.findViewById(R.id.btnDlogLeft);
		Button btnPay = (Button)dlog.findViewById(R.id.btnDlogRight);
		btnPrint.setText(R.string.button_print);
		btnPay.setText(R.string.button_pay);
		if(isPaid)
			btnPay.setVisibility(View.GONE);
		
		btnPrint.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				new printAsync().execute(myCursor.getString(myCursor.getColumnIndex("_id")));
			
			}
		});
		btnPay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				intentSinglePayment(pos);
			}
		});
		dlog.show();
	}
	
	
	private void showReprintDlg(final String _id) {
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(false);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);

		viewTitle.setText(R.string.dlog_title_error);
		viewMsg.setText(R.string.dlog_msg_failed_print);

		
		Button btnYes = (Button)dlog.findViewById(R.id.btnDlogLeft);
		Button btnNo = (Button)dlog.findViewById(R.id.btnDlogRight);
		btnYes.setText(R.string.button_yes);
		btnNo.setText(R.string.button_no);
		
		btnYes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				new printAsync().execute(_id);
			}
		});
		btnNo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
			}
		});
		dlog.show();
	}
	
	
	
	private class printAsync extends AsyncTask<String, String, String> 
	{
		private String _inv_id = "";
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
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			_inv_id = params[0];
			if(Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice!=null)
				printSuccessful = Global.mainPrinterManager.currentDevice.printOpenInvoices(_inv_id);
			
			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss();	
			if(!printSuccessful)
				showReprintDlg(_inv_id);
		}
	}
	
	
	
	
	private double[] toPrimitiveDouble(Double [] val)
	{
		int size = val.length;
		double[] tempArray = new double[size];
		for(int i = 0 ;i<size;i++) {
			tempArray[i] = (double)val[i];
		}
		return tempArray;
	}

	
	public void performSearch(String text) {
		if (myCursor != null)
			myCursor.close();

		myCursor = this.handler.getSearchedInvoicesList(text, isFromMainMenu);

		myAdapter = new CustomCursorAdapter(this, myCursor,
				CursorAdapter.NO_SELECTION);
		myListView.setAdapter(myAdapter);

	}

	
	public class CustomCursorAdapter extends CursorAdapter {
		LayoutInflater inflater;
		boolean isPaidDivider = false;
		boolean isOpenedDivider = false;

		boolean insideOpened = false;
		boolean insidePaid = false;
		SparseBooleanArray mSparseBooleanArray;

		public CustomCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			// TODO Auto-generated constructor stub
			inflater = LayoutInflater.from(context);
			mSparseBooleanArray = new SparseBooleanArray();
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub

			StringBuilder sb = new StringBuilder();
			TextView clientName = (TextView) view
					.findViewById(R.id.invoiceClientName);
			TextView txnID = (TextView) view.findViewById(R.id.invoiceTXNID);
			final TextView uid = (TextView) view.findViewById(R.id.invoiceUID);
			TextView createdDate = (TextView) view
					.findViewById(R.id.invoiceCreatedContent);
			TextView dueDate = (TextView) view
					.findViewById(R.id.invoiceDueContent);
			TextView shipDate = (TextView) view
					.findViewById(R.id.invoiceShipContent);
			TextView total = (TextView) view
					.findViewById(R.id.invoiceTotalContent);
			TextView balance = (TextView) view
					.findViewById(R.id.invoiceBalanceContent);
			TextView isPaidTag = (TextView) view
					.findViewById(R.id.invoicePaidTitle);

			ImageView moreDetails = (ImageView) view
					.findViewById(R.id.invoiceMoreDetailIcon);

			CheckBox checkBox = (CheckBox) view.findViewById(R.id.invoiceCheckBox);
			if(isFromMainMenu)
			{
				
				checkBox.setTag(cursor.getPosition());
				checkBox.setChecked(mSparseBooleanArray.get(cursor.getPosition()));
				checkBox.setOnCheckedChangeListener(mCheckedChangeListener);
			}
			else
				checkBox.setVisibility(View.INVISIBLE);
				

			if (clientName != null && txnID != null && uid != null
					&& createdDate != null && dueDate != null
					&& shipDate != null && total != null && balance != null) {
				clientName.setText(cursor.getString(cursor
						.getColumnIndex("cust_name")));
				uid.setText(cursor.getString(cursor.getColumnIndex("_id")));
				sb.append("(txnID: ")
						.append(cursor.getString(cursor.getColumnIndex("txnID")))
						.append(")");
				txnID.setText(sb.toString());
				createdDate.setText(Global.formatToDisplayDate(cursor
						.getString(cursor.getColumnIndex("inv_timecreated")),
						activity, 0));
				dueDate.setText(Global.formatToDisplayDate(
						cursor.getString(cursor.getColumnIndex("inv_duedate")),
						activity, 0));
				shipDate.setText(Global.formatToDisplayDate(
						cursor.getString(cursor.getColumnIndex("inv_shipdate")),
						activity, 0));

				double tempVal = Double.parseDouble(cursor.getString(cursor
						.getColumnIndex("inv_total")));
				total.setText(Global.getCurrencyFormat(Global
						.formatNumToLocale(tempVal)));
				tempVal = Double.parseDouble(cursor.getString(cursor
						.getColumnIndex("inv_balance")));
				balance.setText(Global.getCurrencyFormat(Global
						.formatNumToLocale(tempVal)));


				moreDetails.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(activity,HistoryOpenInvoicesDetails_FA.class);
						intent.putExtra("uid", uid.getText().toString());
						chosenInvID = uid.getText().toString();
						startActivityForResult(intent, 0);
					}
				});

				if (cursor.getString(cursor.getColumnIndex("inv_ispaid"))
						.equals("0"))
					isPaidTag.setVisibility(View.INVISIBLE);
				else
				{
					checkBox.setVisibility(View.INVISIBLE);
					isPaidTag.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return myCursor.getCount(); // plus the 2 dividers
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			View retView = inflater.inflate(R.layout.hist_invoices_lvadapter,
					parent, false);

			return retView;
		}

		public void getCheckedItems() {
			inv_list = new ArrayList<String>();
			txnIDList = new ArrayList<String>();
			total_list = new ArrayList<Double>();
			chosenInvIDList = new ArrayList<String>();
			
			int size = myCursor.getCount();
			myCursor.moveToFirst();
			int i_id = myCursor.getColumnIndex("_id");
			int i_txnid = myCursor.getColumnIndex("txnID");
			int i_balance = myCursor.getColumnIndex("inv_balance");
			int i_total = myCursor.getColumnIndex("inv_total");
			double totalAmount = 0.0;
			double totalBalance = 0.0;
			String temp;
			for (int i = 0; i < size; i++)
			{
				if (mSparseBooleanArray.get(i))
				{
					chosenInvIDList.add(myCursor.getString(i_id));
					txnIDList.add(myCursor.getString(i_txnid));
					inv_list.add(myCursor.getString(i_id));
					temp = myCursor.getString(i_balance);
					total_list.add(Double.parseDouble(temp));
					totalAmount+=Double.parseDouble(myCursor.getString(i_total));
					totalBalance +=Double.parseDouble(temp);
				}
				myCursor.moveToNext();
			}
			

			totalCostAmount = Double.toString(totalAmount);
			balanceDue =  Double.toString(totalBalance);
		}
		
		public int getCheckedItemSize()
		{
			int size = myCursor.getCount();
			int counter = 0;
			for(int i = 0 ; i<size;i++)
			{
				if(mSparseBooleanArray.get(i))
				{
					counter++;
				}
			}
			return counter;
		}


		OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				mSparseBooleanArray.put((Integer) buttonView.getTag(),isChecked);

			}

		};

	}

	public String format(String text) {
		DecimalFormat frmt = new DecimalFormat("0.00");
		if (text.isEmpty())
			return "0.00";
		return frmt.format(Double.parseDouble(text));
	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Global.FROM_PAYMENT || resultCode == 3) 
		{
			InvoicesHandler invHandler = new InvoicesHandler(this);
			if(!isMultiInvoice)
			{
				
				String t = Global.addSubsStrings(false, Global.formatNumToLocale(Double.parseDouble(balanceDue)), Global.formatNumToLocale(Global.overallPaidAmount));
				Global.overallPaidAmount = 0;
	
				
				double remainingBalance = Global.formatNumFromLocale(t);
	
				if (remainingBalance <= 0) {
					// has been paid in total
					invHandler.updateIsPaid(true, chosenInvID, null);
				} else {
					// hasn't been paid in total
					invHandler.updateIsPaid(false, chosenInvID,Double.toString(remainingBalance));
				}
			}
			else
			{
				InvoicePaymentsHandler invPayHandler = new InvoicePaymentsHandler(this);
				double invPaid = 0.0;
				int size = inv_list.size();
				double remainingBalance = 0.0;
				for(int i = 0 ; i < size; i++)
				{
					invPaid = invPayHandler.getTotalPaidAmount(inv_list.get(i));
					if(invPaid!=-1)
					{
						remainingBalance = total_list.get(i)- invPaid;
						if(remainingBalance <=0)
							invHandler.updateIsPaid(true, chosenInvIDList.get(i), null);
						else
							invHandler.updateIsPaid(false, chosenInvIDList.get(i), Double.toString(remainingBalance));
					}
				}
				
			}
			finish();
		} else if (resultCode == Global.FROM_OPEN_INVOICES_DETAILS) {
			finish();
		}
		else
			myAdapter.notifyDataSetChanged();
	}
	
}
