package com.android.emobilepos.history.details;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.CustomersHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ProductsImagesHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.database.StoredPayments_DB;
import com.android.database.VoidTransactionsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProducts;
import com.android.emobilepos.models.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class HistoryTransactionDetails_FA extends FragmentActivity implements OnClickListener,OnItemClickListener{

	private boolean hasBeenCreated = false;
	private Global global;
	
	private ListViewAdapter myAdapter;

	private final int CASE_TOTAL = 0;
	private final int CASE_OVERALL_PAID_AMOUNT = 1;
	private final int CASE_TIP_AMOUNT = 2;
	private final int CASE_CLERK_ID = 3;
	private final int CASE_ORD_COMMENT = 4;
	private final int CASE_SHIPVIA = 5;
	private final int CASE_ORD_TERMS = 6;
	private final int CASE_ORD_DELIVERY = 7;
	private final int CASE_CUST_EMAIL = 8;
	private final int CASE_PO = 9;
	
	private final int CASE_PAYMETHOD_NAME = 10;
	private final int CASE_PAY_ID = 11;
	private final int CASE_PAID_AMOUNT = 12;
	private final int CASE_PAID_AMOUNT_NO_CURRENCY = 13;
	
	
	private static List<String> allInfoLeft;
	private String order_id;
	private List<OrderProducts> orderedProd;
	private Drawable mapDrawable;
	private ProgressDialog myProgressDialog;
	
	
	
	private ListView myListView;
	private HashMap<String, String> orderHashMap = new HashMap<String, String>();
	private List<HashMap<String,String>> paymentMapList = new ArrayList<HashMap<String,String>>();
	private String empstr = "";

	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	
	private String paymethodName = "";
	private int offsetForPayment =0;
	private TextView custNameView;
	//private Button btnPrint;
	private Button btnPrint,btnVoid;
	private Activity activity;
	private MyPreferences myPref;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.order_detailslv_layout);
		global = (Global)getApplication();
		activity = this;
		

		myPref = new MyPreferences(activity);
		myListView = (ListView) findViewById(R.id.orderDetailsLV);
		btnPrint = (Button) findViewById(R.id.printButton);
		btnVoid = (Button) findViewById(R.id.btnVoid);
		btnVoid.setOnClickListener(this);
		
		TextView headerTitle = (TextView) findViewById(R.id.ordDetailsHeaderTitle);
		headerTitle.setText(getString(R.string.trans_details_title));

		
		View headerView = getLayoutInflater().inflate(R.layout.orddetails_lvheader_adapter, (ViewGroup) findViewById(R.id.order_header_root));
		custNameView = (TextView) headerView.findViewById(R.id.ordLVHeaderTitle);
		TextView date = (TextView) headerView.findViewById(R.id.ordLVHeaderSubtitle);
		ImageView receipt = (ImageView) headerView.findViewById(R.id.ordTicketImg);

		
		
		
		allInfoLeft = Arrays.asList(new String[]{getString(R.string.trans_details_total),getString(R.string.trans_details_amount_paid),
				getString(R.string.trans_details_tip),getString(R.string.trans_details_clerk_id),getString(R.string.trans_details_comment),
				getString(R.string.trans_details_ship_via),getString(R.string.trans_details_terms),getString(R.string.trans_details_delivery),
				getString(R.string.trans_details_email),getString(R.string.trans_details_po)});
		
		
		
		final Bundle extras = activity.getIntent().getExtras();
		OrdersHandler handler = new OrdersHandler(activity);
		
		order_id = extras.getString("ord_id");
		orderHashMap = handler.getOrderDetails(order_id);
		OrderProductsHandler handler2 = new OrderProductsHandler(activity);
		orderedProd = handler2.getOrderedProducts(order_id);
		
		CustomersHandler handler3 = new CustomersHandler(activity);
		
		PaymentsHandler paymentHandler = new PaymentsHandler(activity);
		paymentMapList = paymentHandler.getPaymentDetailsForTransactions(order_id);

		String encodedImg = getOrderData("ord_signature");
		if (!encodedImg.isEmpty()) {
			Resources resources = activity.getResources();
			Drawable[] layers = new Drawable[2];
			layers[0] = resources.getDrawable(R.drawable.torn_paper);
			byte[] img = Base64.decode(encodedImg, Base64.DEFAULT);
			layers[1] = new BitmapDrawable(resources, BitmapFactory.decodeByteArray(img, 0, img.length));
			LayerDrawable layered = new LayerDrawable(layers);
			layered.setLayerInset(1, 100, 30, 50, 60);
			receipt.setImageDrawable(layered);
		}

		custNameView.setText(handler3.getSpecificValue("cust_name", getOrderData("cust_id")));
		StringBuilder sb = new StringBuilder();
		sb.append(getCaseData(CASE_TOTAL,0)).append(" on ").append(getOrderData("ord_timecreated"));
		date.setText(sb.toString());

		myListView.addHeaderView(headerView);

		View footerView = getLayoutInflater().inflate(R.layout.orddetails_lvfooter_adapter, (ViewGroup) findViewById(R.id.order_footer_root));
		final ImageView mapImg = (ImageView) footerView.findViewById(R.id.ordDetailsMapImg);

		DisplayMetrics displayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int width = displayMetrics.widthPixels;
		
		
		final Handler mHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	if(mapDrawable==null)
	        		mapImg.setImageResource(R.drawable.map_no_image);
	        	else
	        		mapImg.setImageDrawable(mapDrawable);
	            //call setText here
	        }};
		new Thread(new Runnable() {
			   public void run() {
				   Message msg = new Message();
				   StringBuilder sb = new StringBuilder();
				   
				   String latitude = orderHashMap.get("ord_latitude");
				   String longitude = orderHashMap.get("ord_longitude");
				   
				   if(!latitude.isEmpty()&&!longitude.isEmpty())
				   {
					   sb.append("https://maps.googleapis.com/maps/api/staticmap?center=");
					   sb.append(latitude).append(",").append(longitude);
					   sb.append("&markers=color:red|label:S|");
					   sb.append(latitude).append(",").append(longitude);
					   sb.append("&zoom=16&size=").append(width).append("x").append(width).append("&sensor=false");
					   mapDrawable = createDrawableFromURL(sb.toString());
					   
					   mHandler.sendMessage(msg);
				   }
				   else
					   mHandler.sendMessage(msg);
				   
			   }                        
			}).start();
		myListView.addFooterView(footerView);

		
		//Handle the click even and begin the process for Printing the transaction
		
		if(myPref.getPreferences(MyPreferences.pref_enable_printing))
		{
			btnPrint.setBackgroundResource(R.drawable.blue_button_selector);
			btnPrint.setOnClickListener(this);
		}
		else
		{
			btnPrint.setBackgroundResource(R.drawable.disabled_gloss_button_selector);
		}
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy",Locale.getDefault());
		String curDate = sdf.format(new Date());
		if(orderHashMap.get("isVoid")!=null&&(orderHashMap.get("isVoid").equals("1")||!curDate.equals(getOrderData("ord_timecreated"))))
		{
			btnVoid.setBackgroundResource(R.drawable.disabled_gloss_button_selector);
			btnVoid.setClickable(false);
		}
		
		
		myPref = new MyPreferences(activity);
		myAdapter = new ListViewAdapter(activity);

		imageLoader = ImageLoader.getInstance();
		imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.emobile_icon).cacheInMemory(true).cacheOnDisc(true)
				.showImageForEmptyUri(R.drawable.ic_launcher).build();
		myListView.setAdapter(myAdapter);
		myListView.setOnItemClickListener(this);

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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.printButton:
			btnPrint.setClickable(false);
			new printAsync().execute("");
			btnPrint.setClickable(true);
			break;
		case R.id.btnVoid:
			if(myPref.getPreferences(MyPreferences.pref_require_manager_pass_to_void_trans))
			{
				promptManagerPassword();
			}
			else
			{
				confirmVoid();
//				btnVoid.setBackgroundResource(R.drawable.disabled_gloss_button_selector);
//				btnVoid.setClickable(false);
//				if(myPref.getPreferences(MyPreferences.pref_use_store_and_forward))
//				{
//					StoredPayments_DB dbStoredPayments = new StoredPayments_DB(this);
//					if(dbStoredPayments.getRetryTransCount(order_id)>0)
//					{
//						//there are pending stored&forward cannot void
//						Global.showPrompt(activity, R.string.dlog_title_error, getString(R.string.dlog_msg_pending_stored_forward));
//						btnVoid.setClickable(true);
//						btnVoid.setBackgroundResource(R.drawable.blue_button_selector);
//						
//					}
//					else
//					{
//						voidTransaction();
//					}
//					
//				}
//				else
//					voidTransaction();
			}
			break;
		}
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		// TODO Auto-generated method stub
		if (pos > offsetForPayment && pos < offsetForPayment + paymentMapList.size() + 1) {
			int listIndex = pos - offsetForPayment - 1;
			String paymethodName = getCaseData(CASE_PAYMETHOD_NAME, listIndex);
			if (paymethodName != null && !paymethodName.isEmpty()) {
				Intent intent = new Intent(parent.getContext(), HistoryPaymentDetails_FA.class);
				intent.putExtra("histpay", true);
				intent.putExtra("pay_id", getCaseData(CASE_PAY_ID, listIndex));
				intent.putExtra("pay_amount", getCaseData(CASE_PAID_AMOUNT_NO_CURRENCY, listIndex));
				intent.putExtra("cust_name", custNameView.getText().toString());
				intent.putExtra("paymethod_name", paymethodName);
				startActivity(intent);
			}
		}
	}
	
	
	private class printAsync extends AsyncTask<String, String, String> 
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
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			Bundle extras = activity.getIntent().getExtras();
			String trans_type = extras.getString("trans_type");
			int type = Integer.parseInt(trans_type);
			if(Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice!=null)
			{
				if(Global.OrderType.getByCode(Integer.parseInt(trans_type)) == Global.OrderType.CONSIGNMENT_FILLUP)
				{
					
				}
				else if(Global.OrderType.getByCode(Integer.parseInt(trans_type)) == Global.OrderType.CONSIGNMENT_PICKUP)
				{
					
				}
				else
					printSuccessful = Global.mainPrinterManager.currentDevice.printTransaction(order_id, Global.OrderType.getByCode(Integer.parseInt(trans_type)),true,false);
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss();
			
			if(!printSuccessful)
				showPrintDlg();
		}
	}
	
	private void showPrintDlg() {
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(false);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);

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
				new printAsync().execute();
				
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
	
	private String getOrderData(String key) {
		String text = orderHashMap.get(key);
		if (text == null)
			return empstr;
		return text;
	}
	
	private String getPaymentData(int pos,String key)
	{
		String text = paymentMapList.get(pos).get(key);
		if(text == null)
			return empstr;
		return text;
	}

	private String getCaseData(int type,int position) {
		String data = empstr;
		
		switch (type) 
		{
			case CASE_TOTAL:				//total
				data = Global.formatDoubleStrToCurrency(getOrderData("ord_total"));
				break;
			case CASE_OVERALL_PAID_AMOUNT:				//amount paid
				int size = paymentMapList.size();
				String temp = "0.00";
				String otherAmount = "0";
				if(size>0)
				{
					
					if(paymentMapList.get(0).get("paymethod_name").equals("LoyaltyCard"))
						otherAmount = Global.addSubsStrings(true, otherAmount, Global.formatNumToLocale(Double.parseDouble(paymentMapList.get(0).get("pay_amount"))));
					else
						temp =  Global.formatNumToLocale(Double.parseDouble(paymentMapList.get(0).get("pay_amount")));
					
					for(int i = 1; i < size; i++)
					{
						if(paymentMapList.get(i).get("paymethod_name").equals("LoyaltyCard"))
							otherAmount = Global.addSubsStrings(true, otherAmount, Global.formatNumToLocale(Double.parseDouble(paymentMapList.get(i).get("pay_amount"))));
						else
							temp = Global.addSubsStrings(true, temp, Global.formatNumToLocale(Double.parseDouble(paymentMapList.get(i).get("pay_amount"))));
					}
				}
				temp = Double.toString(Global.formatNumFromLocale(temp));
				if(otherAmount.equals("0"))
					data = Global.formatDoubleStrToCurrency(temp);
				else
					data = Global.formatDoubleStrToCurrency(temp)+" + " + otherAmount + " Points";
				break;
			case CASE_TIP_AMOUNT:				//Tip
				int size1 = paymentMapList.size();
				String temp1 = "0.00";
				String storedVal = "0.00";
				if(size1>0)
				{
					temp1 = paymentMapList.get(0).get("pay_tip");
					if(temp1==null||temp1.isEmpty())
						temp1="0.00";
					for(int i = 1; i < size1; i++)
					{
						storedVal = paymentMapList.get(i).get("pay_tip");
						if(storedVal==null||storedVal.isEmpty())
							storedVal = "0.00";
						temp1 = Global.addSubsStrings(true, temp1, Global.formatNumToLocale(Double.parseDouble(storedVal)));
					}
				}
				temp1 = Double.toString(Global.formatNumFromLocale(temp1));
				data = Global.formatDoubleStrToCurrency(temp1);
				break;
			case CASE_CLERK_ID:				//clerk id
				data = getOrderData("clerk_id");
				break;
			case CASE_ORD_COMMENT:				//comment
				data = getOrderData("ord_comment");
				break;
			case CASE_SHIPVIA:				//ship via
				data = getOrderData("ord_shipvia");
				break;
			case CASE_ORD_TERMS:				//terms
				data = getOrderData("ord_terms");
				break;
			case CASE_ORD_DELIVERY:				//delivery
				data = getOrderData("ord_delivery");
				break;
			case CASE_CUST_EMAIL:				//e-mail
				data = getOrderData("c_email");
				break;
			case CASE_PAYMETHOD_NAME:
				data = getPaymentData(position,"paymethod_name");
				break;
			case CASE_PAY_ID:
				data = getPaymentData(position,"pay_id");
				break;
			case CASE_PAID_AMOUNT:
				data = Global.formatDoubleStrToCurrency(getPaymentData(position,"pay_amount"));
				break;
			case CASE_PO:
				data = getOrderData("ord_po");
				break;
			case CASE_PAID_AMOUNT_NO_CURRENCY:
				data = getPaymentData(position,"pay_amount");
				break;
		}
		return data;
	}

	private Drawable createDrawableFromURL(String urlString) {
		Drawable image = null;
		try {
			URL url = new URL(urlString);
			
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			
			image = Drawable.createFromStream(is, "src");
			
		} catch (MalformedURLException e) {
			image = null;
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.HistTransDetailsFragment (at Class.createDrawableFromURL)]");
			
//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} catch (IOException e) {
			image = null;
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.HistTransDetailsFragment (at Class.createDrawableFromURL)]");
			
//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		}
		return image;
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
					//Void transaction
					globalDlog.dismiss();
					startVoidingTransaction();
				}
				else
				{
					globalDlog.dismiss();
					Global.showPrompt(activity, R.string.dlog_title_error, getString(R.string.invalid_password));
				}
			}
		});
		globalDlog.show();
	}
	
	private void confirmVoid()
	{
		
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(true);
		dlog.setCanceledOnTouchOutside(true);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);
		viewMsg.setVisibility(View.GONE);
		Button btnVoid = (Button)dlog.findViewById(R.id.btnDlogRight);
		Button btnCancel = (Button)dlog.findViewById(R.id.btnDlogLeft);
		btnVoid.setText(R.string.button_void);
		btnCancel.setText(R.string.button_cancel);
		
		btnVoid.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				startVoidingTransaction();
			}
		});
		btnCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
			}
		});
		dlog.show();
	
	}
	
	
	private void startVoidingTransaction()
	{
		btnVoid.setBackgroundResource(R.drawable.disabled_gloss_button_selector);
		btnVoid.setClickable(false);
		
		if(myPref.getPreferences(MyPreferences.pref_use_store_and_forward))
		{
			StoredPayments_DB dbStoredPayments = new StoredPayments_DB(activity);
			if(dbStoredPayments.getRetryTransCount(order_id)>0)
			{
				//There are pending stored&forward cannot void
				Global.showPrompt(activity, R.string.dlog_title_error, getString(R.string.dlog_msg_pending_stored_forward));
				btnVoid.setBackgroundResource(R.drawable.blue_button_selector);
				btnVoid.setClickable(true);
			}
			else
			{
				voidTransaction();
			}
		}
		else
			voidTransaction();
	}
	
	private List<Payment>listVoidPayments;
	private PaymentsHandler payHandler;

	private void voidTransaction()
	{
		double amountToBeSubstracted;

		OrdersHandler handler = new OrdersHandler(activity);
		handler.updateIsVoid(order_id);
		handler.updateIsProcessed(order_id, "9");

		VoidTransactionsHandler voidHandler = new VoidTransactionsHandler(activity);
		/*HashMap<String,String> voidedTrans = new HashMap<String,String>();
		voidedTrans.put("ord_id", order_id);
		voidedTrans.put("ord_type",orderHashMap.get("ord_type") );
		voidHandler.insert(voidedTrans);*/
		
		Order order = new Order(activity);
		order.ord_id = order_id;
		order.ord_type = orderHashMap.get("ord_type");
		voidHandler.insert(order);

		//Section to update the local ShiftPeriods database to reflect the VOID
		ShiftPeriodsDBHandler handlerSP = new ShiftPeriodsDBHandler(activity);

		amountToBeSubstracted = Double.parseDouble(NumberUtils.cleanCurrencyFormatedNumber(orderHashMap.get("ord_total"))); //find total to be credited

        //update ShiftPeriods (isReturn set to true)
		handlerSP.updateShiftAmounts(myPref.getShiftID(), amountToBeSubstracted, true);
		
		
		
		//Check if Stored&Forward active and delete from record if any payment were made
		if(myPref.getPreferences(MyPreferences.pref_use_store_and_forward))
		{
			handler.updateOrderStoredFwd(order_id,"0");
			StoredPayments_DB dbStoredPayments = new StoredPayments_DB(this);
			dbStoredPayments.deletePaymentFromJob(order_id);
		}
		
		payHandler = new PaymentsHandler(activity);
		listVoidPayments = payHandler.getOrderPayments(order_id);
		int size = listVoidPayments.size();
		if(size>0)
		{
			new voidPaymentAsync().execute();
		}
		else			
			Global.showPrompt(activity, R.string.dlog_title_success, getString(R.string.dlog_msg_transaction_voided));
		
		activity.setResult(100);
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
				paymentType = listVoidPayments.get(i).card_type.toUpperCase(Locale.getDefault()).trim();
				if(paymentType.equals("GIFTCARD"))
				{					
					payGate = new EMSPayGate_Default(activity,listVoidPayments.get(i));
					xml = post.postData(13, activity, payGate.paymentWithAction("VoidGiftCardAction",false,listVoidPayments.get(i).card_type,null));
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
					xml = post.postData(13, activity, payGate.paymentWithAction("VoidCreditCardAction",false,listVoidPayments.get(i).card_type,null));
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
				
//				Tracker tracker = EasyTracker.getInstance(activity);
//				tracker.send(MapBuilder.createException(sb.toString(), false).build());
			}
			

			
			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			myProgressDialog.dismiss();
			
			Global.showPrompt(activity, R.string.dlog_title_success, getString(R.string.dlog_msg_transaction_voided));
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public class ListViewAdapter extends BaseAdapter implements Filterable {
		private LayoutInflater myInflater;
		private ProductsImagesHandler imgHandler;
		private Context context;

		public ListViewAdapter(Context context) {
			this.context = context;
			imgHandler = new ProductsImagesHandler(activity);
			myInflater = LayoutInflater.from(context);
			offsetForPayment = allInfoLeft.size()+orderedProd.size()+3;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return (allInfoLeft.size() + orderedProd.size()+paymentMapList.size() + 4); //the +4 is to include the dividers,+1 for the map
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
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
				case 0: // divider
				{
					convertView = myInflater.inflate(R.layout.orddetails_lvdivider_adapter, null);
					holder.textLine1 = (TextView) convertView.findViewById(R.id.orderDivLeft);
					holder.textLine2 = (TextView) convertView.findViewById(R.id.orderDivRight);

					if (position == 0) 
						holder.textLine1.setText("Info");
					else if (position == allInfoLeft.size() + 1) 
						holder.textLine1.setText("Items");
					else if(position == (orderedProd.size() + allInfoLeft.size() + 2))
						holder.textLine1.setText("Payments");
					else
						holder.textLine1.setText("Map");
					break;
				}
				case 1: // content in info divider
				{
					convertView = myInflater.inflate(R.layout.orddetails_lvinfo_adapter, null);

					holder.textLine1 = (TextView) convertView.findViewById(R.id.ordInfoLeft);
					holder.textLine2 = (TextView) convertView.findViewById(R.id.ordInfoRight);

					holder.textLine1.setText(allInfoLeft.get(position - 1));
					
					String temp = getCaseData((position-1),0);
					if(temp!=null&&!temp.isEmpty())
						holder.textLine2.setText(getCaseData((position - 1),0));

					break;
				}
				case 2: {
					convertView = myInflater.inflate(R.layout.orddetails_lvproducts_adapter, null);

					holder.textLine1 = (TextView) convertView.findViewById(R.id.ordProdTitle);
					holder.textLine2 = (TextView) convertView.findViewById(R.id.ordProdSubtitle);
					holder.ordProdPrice = (TextView)convertView.findViewById(R.id.ordProdPrice);
					holder.ordProdQty = (TextView)convertView.findViewById(R.id.ordProdQty);
					holder.iconImage = (ImageView) convertView.findViewById(R.id.prodIcon);
					int ind = position - allInfoLeft.size() - 2;


					holder.textLine1.setText(orderedProd.get(ind).ordprod_name);
					holder.textLine2.setText(orderedProd.get(ind).ordprod_desc);
					
					holder.ordProdQty.setText(orderedProd.get(ind).ordprod_qty+" x");
					holder.ordProdPrice.setText(Global.formatDoubleStrToCurrency(orderedProd.get(ind).overwrite_price));
					

					break;
				}
				case 3:
				{

					convertView = myInflater.inflate(R.layout.orddetails_lvpayment_adapter, null);

					holder.textLine1 = (TextView) convertView.findViewById(R.id.paidAmount);
					holder.moreDetails = (ImageView) convertView.findViewById(R.id.paymentMoreDetailsIcon);
					int listIndex = position-offsetForPayment;
					paymethodName = getCaseData(CASE_PAYMETHOD_NAME,listIndex);
					if(paymethodName!=null&&!paymethodName.isEmpty())
					{
						holder.textLine1.setText(getCaseData(CASE_PAID_AMOUNT,listIndex));
					
						String iconName = Global.paymentIconsMap.get(paymethodName);
						if(iconName == null)
							iconId = R.drawable.debit;// context.getResources().getIdentifier("debit", "drawable", context.getString(R.string.pkg_name));
						else
							iconId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
						
					
						holder.textLine1.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(iconId), null, null, null);
						holder.textLine1.setCompoundDrawablePadding(5);
						holder.textLine1.setGravity(Gravity.CENTER_VERTICAL);
					
					}
					
					break;
				}
				}
				convertView.setTag(holder);
			} 
			
			else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (type == 0) {
				if (position == 0)
					holder.textLine1.setText("Info");
				else if (position == allInfoLeft.size() + 1) 
					holder.textLine1.setText("Items");
				else if(position == (orderedProd.size() + allInfoLeft.size() + 2))
					holder.textLine1.setText("Payments");
				else
					holder.textLine1.setText("Map");
			}

			else if (type == 1) {
				holder.textLine1.setText(allInfoLeft.get(position - 1));
				holder.textLine2.setText(getCaseData((position - 1),0));
			} else if (type == 2) {
				int ind = position - allInfoLeft.size() - 2;

				holder.textLine1.setText(orderedProd.get(ind).ordprod_name);
				holder.textLine2.setText(orderedProd.get(ind).ordprod_desc);
				
				holder.ordProdQty.setText(orderedProd.get(ind).ordprod_qty+" x");
				holder.ordProdPrice.setText(Global.formatDoubleStrToCurrency(orderedProd.get(ind).overwrite_price));
				

				imageLoader.displayImage(imgHandler.getSpecificLink("I", orderedProd.get(ind).prod_id), holder.iconImage, options);
			}

			return convertView;
		}

		@Override
		public Filter getFilter() {
			// TODO Auto-generated method stub
			return null;
		}

		public class ViewHolder {
			TextView textLine1;
			TextView textLine2;
			TextView ordProdQty;
			TextView ordProdPrice;
			ImageView iconImage;
			ImageView moreDetails;

		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0 || (position == (allInfoLeft.size() + 1)) || (position == (orderedProd.size() + allInfoLeft.size() + 2))||(position==(orderedProd.size() + allInfoLeft.size()+paymentMapList.size() + 3))) //divider
				//info				//items											//payments														//map
			{
				return 0;
			} else if (position > 0 && position <= allInfoLeft.size()) 			//info content
			{
				return 1;
			} else if (position > (allInfoLeft.size() + 1) && position <= orderedProd.size() + allInfoLeft.size() + 1) 		//items content
			{
				return 2;
			}
			return 3;					//PAYMENTS

		}

		@Override
		public int getViewTypeCount() {
			return 4;
		}
	}
	
}
