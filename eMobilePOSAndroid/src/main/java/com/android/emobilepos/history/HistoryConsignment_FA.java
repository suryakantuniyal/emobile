package com.android.emobilepos.history;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.android.database.ConsignmentTransactionHandler;
import com.android.database.CustomersHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.history.details.HistoryConsignmentDetails_FA;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class HistoryConsignment_FA extends BaseFragmentActivityActionBar implements OnTabChangeListener {
	private static final String[] TABS = new String[] { "transactions", "pickup"};
	private static String[] TABS_TAG;
	private static final int[] TABS_ID = new int[] {R.id.consignment_tab, R.id.pickup_tab};

	private TabHost tabHost;
	private Activity activity;
	
	
	private Cursor myCursor;
	private ConsignmentTransactionHandler handler;
	private ListView lView;
	private CustomCursorAdapter adapter;
	private boolean isPickup = false;
	
	private boolean hasBeenCreated = false;
	private Global global;
	
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_consignment_layout);
		activity = this;
		global = (Global)getApplication();
		tabHost = (TabHost) findViewById(android.R.id.tabhost);

		TextView headTitle = (TextView) findViewById(R.id.transHeaderTitle);

		Resources resources = this.getResources();
		headTitle.setText(resources.getString(R.string.consignment));
		
		
		lView = (ListView)findViewById(R.id.consignmentListView);
		handler = new ConsignmentTransactionHandler(activity);
		

		
		lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub

				myCursor.moveToPosition(position);
				Intent intent = new Intent(arg0.getContext(),HistoryConsignmentDetails_FA.class);
				intent.putExtra("ConsTrans_ID", myCursor.getString(myCursor.getColumnIndex("_id")));
				intent.putExtra("cust_name", myCursor.getString(myCursor.getColumnIndex("cust_name")));
				intent.putExtra("Cons_timecreated", myCursor.getString(myCursor.getColumnIndex("Cons_timecreated")));
				intent.putExtra("isPickup", isPickup);
				startActivity(intent);
			}
		});
		
		
		
		
		
		EditText field = (EditText) findViewById(R.id.searchField);
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

					adapter = new CustomCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);
					lView.setAdapter(adapter);
				}
			}
		});
		
		
		TABS_TAG = new String[] {resources.getString(R.string.consignment_summary),resources.getString(R.string.consignment_pickup),};

		
		initTabs();

		
		tabHost.setOnTabChangedListener(this);
		tabHost.setCurrentTab(0);

		updateMyTabs(TABS[0], TABS_ID[0]);
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
	
	
	private void initTabs() {
		tabHost.setup();
		int length = TABS.length;
		for (int i = 0; i < length; i++) {
			tabHost.addTab(newTab(TABS[i], TABS_TAG[i], TABS_ID[i]));
		}
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

			if (tabID.equals(TABS[0])) {
				myCursor = handler.getConsignmentCursor(false);
			} else {
				myCursor = handler.getConsignmentCursor(true);
			} 
			
			
			adapter = new CustomCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);
			lView.setAdapter(adapter);
	}
	

	@Override
	public void onTabChanged(String tabID) {
		// TODO Auto-generated method stub
		if (tabID.equals(TABS[0])) {
			updateMyTabs(tabID, TABS_ID[0]);
			isPickup = false;
			return;
		} else {
			updateMyTabs(tabID, TABS_ID[1]);
			isPickup = true;
			return;
		} 
	}
	
	
	public void performSearch(String text) {
		if (myCursor != null)
			myCursor.close();

		adapter = new CustomCursorAdapter(activity, myCursor, CursorAdapter.NO_SELECTION);
		lView.setAdapter(adapter);
	}
	
	
	
	public class CustomCursorAdapter extends CursorAdapter {
		
		
		LayoutInflater inflater;
		CustomersHandler custHandler = new CustomersHandler(activity);
		Global global = (Global) activity.getApplication();
		ViewHolder myHolder;
		String temp = new String();
		String empStr = "";

		public CustomCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			// TODO Auto-generated constructor stub
			inflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub

			myHolder = (ViewHolder)view.getTag();
			
			myHolder.title.setText(cursor.getString(myHolder.i_trans_id));
			myHolder.clientName.setText(cursor.getString(myHolder.i_cust_name));
			
			
			
			if(cursor.getString(myHolder.i_is_synched).equals("0")) //it is synched
				myHolder.syncIcon.setImageResource(R.drawable.is_sync);
			else
				myHolder.syncIcon.setImageResource(R.drawable.is_not_sync);
			
			
			myHolder.voidText.setVisibility(View.GONE);
			
			
		}

		public String format(String text) {

			if (text.isEmpty())
				return Global.formatDoubleToCurrency(0.00);
			return Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(text)));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			View retView = inflater.inflate(R.layout.trans_lvadapter, parent, false);
			
			ViewHolder holder = new ViewHolder();
			
			holder.title = (TextView)retView.findViewById(R.id.transLVtitle);
			holder.clientName = (TextView) retView.findViewById(R.id.transLVid);
			holder.amount = (TextView)retView.findViewById(R.id.transLVamount);
			holder.voidText = (TextView) retView.findViewById(R.id.transVoidText);
			holder.syncIcon = (ImageView)retView.findViewById(R.id.transIcon);
			
			holder.i_trans_id = cursor.getColumnIndex("_id");
			holder.i_cust_name = cursor.getColumnIndex("cust_name");
			holder.i_is_synched = cursor.getColumnIndex("is_synched");
			holder.amount.setVisibility(View.GONE);
			
			
			retView.setTag(holder);
			return retView;
		}
		
		private class ViewHolder
		{
			TextView title,clientName,amount,voidText;
			ImageView syncIcon;
			
			int i_trans_id,i_cust_name,i_is_synched;
		}
	}
}
