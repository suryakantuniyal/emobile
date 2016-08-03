package com.android.emobilepos.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.StoredPayments_DB;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ViewStoreForwardTrans_FA extends BaseFragmentActivityActionBar implements OnItemClickListener, OnClickListener{
	private Activity activity;
	private Global global;
	private boolean hasBeenCreated = false;
	private Cursor myCursor;
	//private SQLiteDatabase db;
	private StoredPayments_DB dbStoredPay;
	private CustomCursorAdapter adapter;
	private ListView listView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_store_forward_trans_layout);
		activity = this;
		global = (Global)getApplication();
		
		Button btnProcessAll = (Button)findViewById(R.id.btnProcessAll);
		btnProcessAll.setOnClickListener(this);
		
		//DBManager dbManager = new DBManager(this);
		//db = dbManager.openWritableDB();
		dbStoredPay = new StoredPayments_DB(this);
		myCursor = dbStoredPay.getStoredPayments();
		listView = (ListView)findViewById(R.id.listView);
		adapter = new CustomCursorAdapter(this, myCursor, CursorAdapter.NO_SELECTION);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		
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
	public void onDestroy()
	{
		super.onDestroy();
		myCursor.close();
		//db.close();
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btnProcessAll:
			new processLivePaymentAsync().execute();
			break;
		}
	}
	
		
	private boolean livePaymentRunning = false;
	private String _charge_xml = "";
	private String _verify_payment_xml = "";
	private ProgressDialog myProgressDialog;
	
	private class processLivePaymentAsync extends AsyncTask<Void, Void, Void> {

		private HashMap<String, String> parsedMap = new HashMap<String, String>();
		private int _count_decline = 0, _count_conn_error = 0, _count_merch_account = 0;
		
		
		private void checkPaymentStatus(String verify_payment_xml, String charge_xml) throws SAXException, ParserConfigurationException, IOException
		{
			OrdersHandler dbOrdHandler = new OrdersHandler(activity);
			Post httpClient = new Post();
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);
			String xml = httpClient.postData(13, activity, verify_payment_xml);
			
			if (xml.equals(Global.TIME_OUT) || xml.equals(Global.NOT_VALID_URL) || xml.isEmpty()) {
				//do nothing
				_count_conn_error++;
			} else {
				InputSource inSource = new InputSource(new StringReader(xml));

				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				xr.setContentHandler(handler);
				xr.parse(inSource);
				parsedMap = handler.getData();
				
				
				if(parsedMap != null && parsedMap.size() > 0)
				{
					String _job_id = myCursor.getString(myCursor.getColumnIndex("job_id"));
					String _pay_uuid = myCursor.getString(myCursor.getColumnIndex("pay_uuid"));
					
					if(parsedMap.get("epayStatusCode").equals("APPROVED"))
					{
						//Create Payment and delete from StoredPayment
						saveApprovedPayment(parsedMap);
						//Remove as pending stored & forward if no more payments are pending to be processed.
						if(dbStoredPay.getCountPendingStoredPayments(_job_id)<=0)
							dbOrdHandler.updateOrderStoredFwd(_job_id,"0");
					}
					else if(parsedMap.get("epayStatusCode").equals("DECLINE"))
					{
						if(parsedMap.get("statusCode").equals("102"))
						{
							_count_merch_account++;
							dbStoredPay.updateStoredPaymentForRetry(_pay_uuid);
						}
						else
						{
							//remove from StoredPayment and change order to Invoice
							StringBuilder sb = new StringBuilder();
							sb.append(dbOrdHandler.getColumnValue("ord_comment", _job_id)).append("  ");
							sb.append("(Card Holder: ").append(myCursor.getString(myCursor.getColumnIndex("pay_name")));
							sb.append("; Last 4: ").append(myCursor.getString(myCursor.getColumnIndex("ccnum_last4")));
							sb.append("; Exp date: ").append(myCursor.getString(myCursor.getColumnIndex("pay_expmonth")));
							sb.append("/").append(myCursor.getString(myCursor.getColumnIndex("pay_expyear")));
							sb.append("; Status Msg: ").append(parsedMap.get("statusMessage"));
							sb.append("; Status Code: ").append(parsedMap.get("statusCode"));
							sb.append("; TransID: ").append(parsedMap.get("CreditCardTransID"));
							sb.append("; Auth Code: ").append(parsedMap.get("AuthorizationCode")).append(")");
							
							dbStoredPay.deleteStoredPaymentRow(_pay_uuid);
							if(dbOrdHandler.getColumnValue("ord_type", _job_id).equals(Global.OrderType.SALES_RECEIPT.getCodeString()))
								dbOrdHandler.updateOrderTypeToInvoice(_job_id);
							dbOrdHandler.updateOrderComment(_job_id, sb.toString());
							
							//Remove as pending stored & forward if no more payments are pending to be processed.
							if(dbStoredPay.getCountPendingStoredPayments(_job_id)<=0)
								dbOrdHandler.updateOrderStoredFwd(_job_id,"0");
							
							_count_decline++;
						}
					}
					else
					{
						//Payment doesn't exist try to process the payment
						processPayment(charge_xml);
					}
				}
				else
				{
					//mark StoredPayment for retry
					_count_conn_error++;
				}
			}
		}
		
		private void processPayment(String charge_xml) throws ParserConfigurationException, SAXException, IOException
		{
			OrdersHandler dbOrdHandler = new OrdersHandler(activity);
			Post httpClient = new Post();
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);
			String xml = httpClient.postData(13, activity, charge_xml);
			if (xml.equals(Global.TIME_OUT) || xml.equals(Global.NOT_VALID_URL) || xml.isEmpty()) {
				//mark StoredPayment for retry
				dbStoredPay.updateStoredPaymentForRetry(myCursor.getString(myCursor.getColumnIndex("pay_uuid")));
				_count_conn_error++;
			} else {
				InputSource inSource = new InputSource(new StringReader(xml));

				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				xr.setContentHandler(handler);
				xr.parse(inSource);
				parsedMap = handler.getData();
				
				
				if(parsedMap != null && parsedMap.size() > 0)
				{
					String _job_id = myCursor.getString(myCursor.getColumnIndex("job_id"));
					String _pay_uuid = myCursor.getString(myCursor.getColumnIndex("pay_uuid"));
					
					if(parsedMap.get("epayStatusCode").equals("APPROVED"))
					{
						//Create Payment and delete from StoredPayment
						saveApprovedPayment(parsedMap);
						//Remove as pending stored & forward if no more payments are pending to be processed.
						if(dbStoredPay.getCountPendingStoredPayments( _job_id)<=0)
							dbOrdHandler.updateOrderStoredFwd(_job_id,"0");
					}
					else if(parsedMap.get("epayStatusCode").equals("DECLINE"))
					{
						if(parsedMap.get("statusCode").equals("102"))
						{
							_count_merch_account++;
							dbStoredPay.updateStoredPaymentForRetry(_pay_uuid);
						}
						else
						{
							//remove from StoredPayment and change order to Invoice
							StringBuilder sb = new StringBuilder();
							sb.append(dbOrdHandler.getColumnValue("ord_comment", _job_id)).append("  ");
							sb.append("(Card Holder: ").append(myCursor.getString(myCursor.getColumnIndex("pay_name")));
							sb.append("; Last 4: ").append(myCursor.getString(myCursor.getColumnIndex("ccnum_last4")));
							sb.append("; Exp date: ").append(myCursor.getString(myCursor.getColumnIndex("pay_expmonth")));
							sb.append("/").append(myCursor.getString(myCursor.getColumnIndex("pay_expyear")));
							sb.append("; Status Msg: ").append(parsedMap.get("statusMessage"));
							sb.append("; Status Code: ").append(parsedMap.get("statusCode"));
							sb.append("; TransID: ").append(parsedMap.get("CreditCardTransID"));
							sb.append("; Auth Code: ").append(parsedMap.get("AuthorizationCode")).append(")");
							
							
							dbStoredPay.deleteStoredPaymentRow(_pay_uuid);
							if(dbOrdHandler.getColumnValue("ord_type", _job_id).equals(Global.OrderType.SALES_RECEIPT.getCodeString()))
								dbOrdHandler.updateOrderTypeToInvoice(_job_id);
							dbOrdHandler.updateOrderComment(_job_id, sb.toString());
							
							//Remove as pending stored & forward if no more payments are pending to be processed.
							if(dbStoredPay.getCountPendingStoredPayments( _job_id)<=0)
								dbOrdHandler.updateOrderStoredFwd(_job_id,"0");
							
							_count_decline++;
						}
					}
					else
					{
						//mark StoredPayment for retry
						dbStoredPay.updateStoredPaymentForRetry(_pay_uuid);
					}
				}
				else
				{
					//mark StoredPayment for retry
					dbStoredPay.updateStoredPaymentForRetry(myCursor.getString(myCursor.getColumnIndex("pay_uuid")));
					_count_conn_error++;
				}
			}
		}
		
		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage("Please wait...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();
			
			_count_merch_account = 0;
			_count_conn_error = 0;
			_count_decline = 0;

		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			if(myCursor!=null&&myCursor.moveToPosition(0))
			{
				int size = myCursor.getCount();
				int i_payment_xml = myCursor.getColumnIndex("payment_xml");
				int i_is_retry = myCursor.getColumnIndex("is_retry");
				//OrdersHandler dbOrdHandler = new OrdersHandler(activity);
				for(int i = 0; i<size; i++)
				{
					myCursor.moveToPosition(i);
					//new processLivePaymentAsync().execute(myCursor.getString(i_payment_xml));
					if (NetworkUtils.isConnectedToInternet(activity) && !livePaymentRunning) {
						livePaymentRunning = true;

						
						_charge_xml = myCursor.getString(i_payment_xml);
						_verify_payment_xml = _charge_xml.replaceAll("<action>.*?</action>", "<action>"+EMSPayGate_Default.getPaymentAction("CheckTransactionStatus")+"</action>");
						
						try {
							
							if (myCursor.getString(i_is_retry).equals("1"))
								checkPaymentStatus(_verify_payment_xml, _charge_xml);
							else
								processPayment(_charge_xml);
							
						} catch (ParserConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch(Exception e)
						{
							dbStoredPay.updateStoredPaymentForRetry(myCursor.getString(myCursor.getColumnIndex("pay_uuid")));
						}
						
						livePaymentRunning = false;
					}
					else
					{
						_count_conn_error++;
					}
				}
			}
			

			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			myProgressDialog.dismiss();
			
			//refresh the list view;
			//adapter.notifyDataSetChanged();
			myCursor = dbStoredPay.getStoredPayments();
			adapter = new CustomCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);
			listView.setAdapter(adapter);
			StringBuilder sb = new StringBuilder();
			if(_count_conn_error > 0)
			{
				sb.append("\t -").append("Connection Error (").append(Integer.toString(_count_conn_error)).append("): ");
				sb.append(getString(R.string.dlog_msg_please_try_again)).append("\n");
			}
			if(_count_merch_account > 0)
			{
				sb.append("\t -").append("Merchant Account (").append(Integer.toString(_count_merch_account)).append("): ");
				sb.append(getString(R.string.dlog_msg_contact_support)).append("\n");
			}
			if(_count_decline > 0)
			{
				sb.append("\t -").append("Decline (").append(Integer.toString(_count_decline)).append("): ");
				sb.append(getString(R.string.dlog_msg_orders_changed_invoice)).append("\n");
			}
			
			if(!sb.toString().isEmpty())
				Global.showPrompt(activity, R.string.dlog_title_transaction_failed_to_process, sb.toString());
		}
	}
	
	
	private void saveApprovedPayment(HashMap<String,String> parsedMap)
	{
		Payment newPayment = new Payment(this);
		
		GenerateNewID generator = new GenerateNewID(this);
		MyPreferences myPref = new MyPreferences(this);
		
		newPayment.pay_id =generator.getNextID(IdType.PAYMENT_ID);
		
		newPayment.emp_id = myCursor.getString(myCursor.getColumnIndex("emp_id"));
		

		newPayment.job_id = myCursor.getString(myCursor.getColumnIndex("job_id"));

		newPayment.inv_id = myCursor.getString(myCursor.getColumnIndex("inv_id"));
		
		
		newPayment.clerk_id = myCursor.getString(myCursor.getColumnIndex("clerk_id"));
		
		newPayment.cust_id = myCursor.getString(myCursor.getColumnIndex("cust_id"));
		newPayment.custidkey = myCursor.getString(myCursor.getColumnIndex("custidkey"));
		
		
		newPayment.ref_num = myCursor.getString(myCursor.getColumnIndex("ref_num"));
		newPayment.paymethod_id = myCursor.getString(myCursor.getColumnIndex("paymethod_id"));
		
		//Global.amountPaid= Double.toString(amountToBePaid);
		
		
		newPayment.pay_dueamount = myCursor.getString(myCursor.getColumnIndex("pay_dueamount"));
		
		
		newPayment.pay_amount = myCursor.getString(myCursor.getColumnIndex("pay_amount"));
		newPayment.pay_name = myCursor.getString(myCursor.getColumnIndex("pay_name"));
		
		newPayment.pay_phone = myCursor.getString(myCursor.getColumnIndex("pay_phone"));
		newPayment.pay_email = myCursor.getString(myCursor.getColumnIndex("pay_email"));
		
		newPayment.pay_ccnum = myCursor.getString(myCursor.getColumnIndex("pay_ccnum"));
		
		
		newPayment.ccnum_last4 = myCursor.getString(myCursor.getColumnIndex("ccnum_last4"));
		newPayment.pay_expmonth = myCursor.getString(myCursor.getColumnIndex("pay_expmonth"));
		newPayment.pay_expyear = myCursor.getString(myCursor.getColumnIndex("pay_expyear"));
		newPayment.pay_poscode = myCursor.getString(myCursor.getColumnIndex("pay_poscode"));
		
		
		newPayment.pay_seccode = myCursor.getString(myCursor.getColumnIndex("pay_seccode"));
		
		//String tempPaid = Double.toString(Global.formatNumFromLocale(tipAmount.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim()));
		
		newPayment.pay_tip = myCursor.getString(myCursor.getColumnIndex("pay_tip"));
		//newPayment.track_one = myCursor.getString(myCursor.getColumnIndex("track_one")));
		//newPayment.track_two = myCursor.getString(myCursor.getColumnIndex("track_two")));
		
		//String[] location = Global.getCurrLocation(activity);
		newPayment.pay_latitude = myCursor.getString(myCursor.getColumnIndex("pay_latitude"));
		newPayment.pay_longitude = myCursor.getString(myCursor.getColumnIndex("pay_longitude"));
		newPayment.card_type = myCursor.getString(myCursor.getColumnIndex("card_type"));
		
		
		
		if(Global.isIvuLoto)
		{
			newPayment.IvuLottoNumber = myCursor.getString(myCursor.getColumnIndex("IvuLottoNumber"));
			newPayment.IvuLottoDrawDate = myCursor.getString(myCursor.getColumnIndex("IvuLottoDrawDate"));
			newPayment.IvuLottoQR = myCursor.getString(myCursor.getColumnIndex("IvuLottoQR"));
			
			
	
				newPayment.Tax1_amount = myCursor.getString(myCursor.getColumnIndex("Tax1_amount"));
				newPayment.Tax1_name = myCursor.getString(myCursor.getColumnIndex("Tax1_name"));
				
				newPayment.Tax2_amount = myCursor.getString(myCursor.getColumnIndex("Tax2_amount"));
				newPayment.Tax2_name = myCursor.getString(myCursor.getColumnIndex("Tax2_name"));

		}
		
		
		
		newPayment.is_refund = myCursor.getString(myCursor.getColumnIndex("is_refund"));
		newPayment.pay_type = myCursor.getString(myCursor.getColumnIndex("pay_type"));
		newPayment.pay_transid = myCursor.getString(myCursor.getColumnIndex("pay_transid"));
		newPayment.authcode =myCursor.getString(myCursor.getColumnIndex("authcode"));
		
		
			
		
		
		
		
		
		
		
		
		newPayment.pay_resultcode = parsedMap.get("pay_resultcode");
		newPayment.pay_resultmessage =parsedMap.get("pay_resultmessage");
		newPayment.pay_transid = parsedMap.get("CreditCardTransID");
		newPayment.authcode = parsedMap.get("AuthorizationCode");
		newPayment.processed = "9";
		
	
		
		PaymentsHandler payHandler = new PaymentsHandler(this);
		payHandler.insert(newPayment);
		
		//delete from StoredPayments
		dbStoredPay.deleteStoredPaymentRow(myCursor.getString(myCursor.getColumnIndex("pay_uuid")));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private class CustomCursorAdapter extends CursorAdapter
	{
		private LayoutInflater inflater;
		private ViewHolder myHolder;

		public CustomCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			// TODO Auto-generated constructor stub
			inflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor c) {
			// TODO Auto-generated method stub
			
			
			myHolder = (ViewHolder)view.getTag();
			
			myHolder.title.setText(c.getString(myHolder.i_card_type)+"  ("+Global.formatDoubleStrToCurrency(c.getString(myHolder.i_pay_amount))+")");
			myHolder.subtitle.setText(c.getString(myHolder.i_pay_name));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			View view = inflater.inflate(R.layout.view_store_forward_layout, parent, false);
			
			
			ViewHolder holder = new ViewHolder();
			holder.title = (TextView) view.findViewById(R.id.tvTitle);
			holder.subtitle = (TextView) view.findViewById(R.id.tvSubtitle);
			
			holder.i_card_type = cursor.getColumnIndex("card_type");
			holder.i_pay_amount = cursor.getColumnIndex("pay_amount");
			holder.i_pay_name = cursor.getColumnIndex("pay_name");
			
			view.setTag(holder);
			
			return view;
		}
		
		
		private class ViewHolder
		{
			TextView title,subtitle;
			
			int i_pay_amount,i_card_type,i_pay_name;
		}
	}
}
