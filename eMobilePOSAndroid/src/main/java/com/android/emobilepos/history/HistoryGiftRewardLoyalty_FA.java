package com.android.emobilepos.history;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.history.details.HistoryPaymentDetails_FA;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class HistoryGiftRewardLoyalty_FA extends BaseFragmentActivityActionBar implements OnItemClickListener, OnTabChangeListener{

	public static final int CASE_GIFTCARD = 0, CASE_LOYALTY = 1, CASE_REWARD = 2;
	private static final String[] TABS = new String[] { "payments", "addbalance"};
	private static String[] TABS_TAG;// = new String[] { "Cash", "Check", "Card", "Other" };
	private static final int[] TABS_ID = new int[] {R.id.payment_tab, R.id.addbalance_tab};

	private TabHost tabHost;
	private Activity activity;
	
	private int currSelectedTab = R.id.payment_tab;
	
	
	private Cursor myCursor;
	private ListView lView;
	private CustomCursorAdapter adapter;
	private PaymentsHandler handler;
	private int cardTypeCase;
	private boolean useLoyalPatron = false;
	
	private Global global;
	private boolean hasBeenCreated = false;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_loyalty_reward_layout);
		activity = this;
		global = (Global)getApplication();
		MyPreferences myPref = new MyPreferences(this);
		tabHost = (TabHost) findViewById(android.R.id.tabhost);

		TextView headTitle = (TextView) findViewById(R.id.transHeaderTitle);
		
		Resources resources = this.getResources();
		Bundle extras = getIntent().getExtras();
		cardTypeCase = extras.getInt("cardTypeCase",CASE_GIFTCARD);
		
		switch(cardTypeCase)
		{
		case CASE_GIFTCARD:
			headTitle.setText(R.string.pay_tab_giftcard);
			break;
		case CASE_LOYALTY:
			headTitle.setText(R.string.loyalty);
			useLoyalPatron = myPref.getPreferences(MyPreferences.pref_use_loyal_patron);
			break;
		case CASE_REWARD:
			headTitle.setText(R.string.rewards);
			break;
		}
		
		lView = (ListView)findViewById(R.id.listView);
		handler = new PaymentsHandler(this);
		lView.setOnItemClickListener(this);
		
		
		EditText field = (EditText) findViewById(R.id.searchField);
		setupSearchField(field);
		
		
		
		TABS_TAG = new String[] {resources.getString(R.string.payment),resources.getString(R.string.add_balance)};

		
		initTabs();

		
		tabHost.setOnTabChangedListener(this);
		tabHost.setCurrentTab(0);

		updateMyTabs(TABS[0], TABS_ID[0]);
		hasBeenCreated = true;
	}
	
	
	@Override
	public void onResume() {

		if(global.isApplicationSentToBackground())
			Global.loggedIn = false;
		global.stopActivityTransitionTimer();
		
		if(hasBeenCreated&&!Global.loggedIn)
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
			Global.loggedIn = false;
		global.startActivityTransitionTimer();
	}
	
	
	private void initTabs() {
		tabHost.setup();
		int length = TABS.length;
		for (int i = 0; i < length; i++) {
			tabHost.addTab(newTab(TABS[i], TABS_TAG[i], TABS_ID[i]));
		}
	}
	
	private void setupSearchField(EditText field)
	{
		field.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					String text = v.getText().toString().trim();
					if (!text.isEmpty())
						performSearch(text);
					return true;
				}
				return false;
			}
		});
		
		
		field.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				String test = s.toString().trim();
				if (test.isEmpty()) {
					if (myCursor != null)
						myCursor.close();
					getCursorData(currSelectedTab);
				}
			}
		});
	}
	
	

	private TabSpec newTab(String tag, String label, int tabView) {
		// TODO Auto-generated method stub

		View indicator = LayoutInflater.from(activity).inflate(R.layout.tabs_layout, (ViewGroup) findViewById(android.R.id.tabs), false);

		TextView tabLabel = (TextView) indicator.findViewById(R.id.tabTitle);

		tabLabel.setText(label);

		TabSpec tabSpec = tabHost.newTabSpec(tag);
		tabSpec.setIndicator(indicator);

		tabSpec.setContent(tabView);

		return tabSpec;
	}

	
	
	private void updateMyTabs(String tabID, int placeHolder) {
		currSelectedTab = placeHolder;
		getCursorData(currSelectedTab);
	}
	
	
	
	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		if (tabId.equals(TABS[0])) {
			updateMyTabs(tabId, TABS_ID[0]);
		}
		else
		{
			updateMyTabs(tabId, TABS_ID[1]);
		}
	}
	
	
	private void getCursorData(int _tab_id) {
		switch (_tab_id) {
		case R.id.payment_tab:
			if(cardTypeCase == CASE_GIFTCARD)
			{
				myCursor = handler.getCashCheckGiftPayment("GiftCard", false);
			}
			else if(cardTypeCase == CASE_LOYALTY)
			{
				myCursor = handler.getLoyaltyPayments();
			}
			else//rewards
			{
				myCursor = handler.getRewardPayments();
			}
			
			break;
		case R.id.addbalance_tab:
			if(cardTypeCase == CASE_GIFTCARD)
			{
				myCursor = handler.getGiftCardAddBalance();
			}
			else if(cardTypeCase == CASE_LOYALTY)
			{
				myCursor = handler.getLoyaltyAddBalance();
			}
			else
			{
				myCursor = handler.getRewardAddBalance();
			}
			
			break;
		}
		adapter = new CustomCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);
		lView.setAdapter(adapter);
	}
	
	
	
	public void performSearch(String text) {
		if (myCursor != null)
			myCursor.close();
		
		switch(currSelectedTab)
		{
		case R.id.payment_tab:
			//myCursor = handler.searchCashCheckGift("Cash", text);
			if(cardTypeCase == CASE_GIFTCARD)
			{
				myCursor = handler.searchCashCheckGift("GiftCard",text);
			}
			else if(cardTypeCase == CASE_LOYALTY)
			{
				
			}
			else
			{
				
			}
			break;
		case R.id.addbalance_tab:
			//myCursor = handler.searchCashCheckGift("Check", text);
			if(cardTypeCase == CASE_GIFTCARD)
			{
				
			}
			else if(cardTypeCase == CASE_LOYALTY)
			{
				
			}
			else
			{
				
			}
			break;
		}

		adapter = new CustomCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);
		lView.setAdapter(adapter);

	}
	
	
	
	public class CustomCursorAdapter extends CursorAdapter {
		LayoutInflater inflater;
		ViewHolder myHolder;
		String tempCustName = new String(),tempPayAmount = new String(),tempTipAmount = new String();
		String empStr = "";

		public CustomCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			// TODO Auto-generated constructor stub
			inflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stubs

			myHolder = (ViewHolder)view.getTag();
			
			tempCustName = cursor.getString(myHolder.i_cust_name);
			if(tempCustName==null)
				tempCustName = empStr;
			
			tempPayAmount = cursor.getString(myHolder.i_pay_amount);
			if(tempPayAmount==null)
				tempPayAmount = empStr;
//			else
//				tempPayAmount = Global.formatDoubleStrToCurrency(tempPayAmount);
			
			tempTipAmount = "(Tip: "+Global.formatDoubleStrToCurrency(cursor.getString(myHolder.i_pay_tip))+")";
			
			
			
			if(cursor.getString(myHolder.i_pay_issync).equals("1"))//it is synch
				myHolder.iconImage.setImageResource(R.drawable.is_sync);
			else
				myHolder.iconImage.setImageResource(R.drawable.is_not_sync);
			
			if(cursor.getString(myHolder.i_isVoid).equals("0"))//is not VOID
				myHolder.voidText.setVisibility(View.INVISIBLE);
			else
				myHolder.voidText.setVisibility(View.VISIBLE);
			
			
			switch(cardTypeCase)
			{
			case CASE_GIFTCARD:
			case CASE_REWARD:
				myHolder.title.setText(tempCustName);
				myHolder.amount.setText(Global.formatDoubleStrToCurrency(tempPayAmount));
				myHolder.tip.setText(tempTipAmount);
				break;
			case CASE_LOYALTY:
				if(useLoyalPatron)
					myHolder.title.setText(tempPayAmount+" Appetizers");
				else
					myHolder.title.setText(tempPayAmount+" Points");
				myHolder.amount.setVisibility(View.GONE);
				myHolder.tip.setVisibility(View.GONE);
				break;
			}
			
			

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			View retView = inflater.inflate(R.layout.histpay_lvadapter, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.title = (TextView) retView.findViewById(R.id.histpayTitle);
			holder.amount = (TextView)retView.findViewById(R.id.histpaySubtitle);
			holder.voidText = (TextView)retView.findViewById(R.id.histpayVoidText);
			holder.iconImage = (ImageView)retView.findViewById(R.id.histpayIcon);
			holder.tip = (TextView) retView.findViewById(R.id.histpayTipText);
			
			holder.i_cust_name = cursor.getColumnIndex("cust_name");
			holder.i_pay_amount = cursor.getColumnIndex("pay_amount");
			holder.i_pay_issync = cursor.getColumnIndex("pay_issync");
			holder.i_isVoid = cursor.getColumnIndex("isVoid");
			holder.i_pay_tip = cursor.getColumnIndex("pay_tip");
			retView.setTag(holder);
			
			return retView;
		}

		
		private class ViewHolder
		{
			TextView title,amount,voidText,tip;
			
			ImageView iconImage;
			
			int i_cust_name,i_pay_amount,i_pay_issync,i_isVoid,i_pay_tip;
		}
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(arg0.getContext(), HistoryPaymentDetails_FA.class);
		intent.putExtra("histpay", true);

		myCursor.moveToPosition(position);
		String pay_id = myCursor.getString(myCursor.getColumnIndex("_id")); // pay_id is returned as _id
		intent.putExtra("pay_id", pay_id);
		intent.putExtra("job_id", myCursor.getString(myCursor.getColumnIndex("job_id")));
		intent.putExtra("pay_amount", myCursor.getString(myCursor.getColumnIndex("pay_amount")));
		intent.putExtra("cust_name", myCursor.getString(myCursor.getColumnIndex("cust_name")));

		intent.putExtra("paymethod_name", "LoyaltyCard");

		startActivity(intent);
	}

}
