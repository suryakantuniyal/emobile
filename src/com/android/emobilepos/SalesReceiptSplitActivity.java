package com.android.emobilepos;


import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.android.catalog.fragments.CatalogLandscapeTwo;

import com.android.database.ProductsHandler;

import com.android.support.Global;
import com.android.support.MyPreferences;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SalesReceiptSplitActivity extends SherlockFragmentActivity {
	private EditText custName;

	private static String ourIntentAction = "";
	private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";

	private MyPreferences myPref;
	private CatalogLandscapeTwo fragment;
	private SalesReceiptFragment portraitFragment;
	private Global global;
	private ProductsHandler handler;
	private int orientation;
	private boolean isPortrait;
	private Activity activity;
	private boolean hasBeenCreated = false;
	private CatalogMainActivity catalogFrag;
	private LinearLayout catalogContainer;
	private Button addProd;
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		activity = this;
		myPref = new MyPreferences(this);
		hasBeenCreated = true;
		handler = new ProductsHandler(this);

		global = (Global) getApplication();
		orientation = getResources().getConfiguration().orientation;
		setContentView(R.layout.orders_splitscreen_layout);
		catalogContainer = (LinearLayout)findViewById(R.id.showcatalog_frag_container);
		catalogFrag = (CatalogMainActivity) getSupportFragmentManager().findFragmentByTag("CatalogFragment");
		if(orientation==Configuration.ORIENTATION_PORTRAIT)
		{
			isPortrait = true;
			if(catalogFrag!=null&&catalogContainer!=null)
			{
				FragmentManager fm = getSupportFragmentManager();
				fm.beginTransaction().hide(catalogFrag).commit();
				catalogContainer.setVisibility(View.GONE);
				
			}
		}
		else
		{
			isPortrait = false;
			if(myPref.getIsTablet()&&catalogFrag!=null&&catalogContainer!=null)
			{
				FragmentManager fm = getSupportFragmentManager();
				fm.beginTransaction().show(catalogFrag).commit();
				catalogContainer.setVisibility(View.VISIBLE);
				
			}
			else if(catalogFrag!=null&&catalogContainer!=null)
			{
				FragmentManager fm = getSupportFragmentManager();
				fm.beginTransaction().hide(catalogFrag).commit();
				catalogContainer.setVisibility(View.GONE);
				
			}
		}
		
		portraitFragment = (SalesReceiptFragment)getSupportFragmentManager().findFragmentById(R.id.receiptsFragment);
		custName = (EditText) findViewById(R.id.membersField);
		addProd = (Button) findViewById(R.id.addProdButton);
		ourIntentAction = getString(R.string.intentAction);
		
		// in case we have been launched by the DataWedge intent plug-in
		// using the StartActivity method let's handle the intent
		Intent i = getIntent();
		handleDecodeData(i);
	}

	
	
	public EditText invisibleSearchView()
	{
		EditText invisibleSearch = (EditText)findViewById(R.id.invisibleEditText);
		return invisibleSearch;
	}
	
	
	
	public EditText visibleSearchView()
	{
		EditText invisibleSearch = (EditText)findViewById(R.id.catalogSearchField);
		return invisibleSearch;
	}
	

	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		if(newConfig.orientation != orientation)
	    {
	    	orientation = newConfig.orientation;
	    	catalogFrag.updateFragment();
	    	
	    	if(orientation==Configuration.ORIENTATION_PORTRAIT)
			{
	    		isPortrait = true;
	    		if(addProd!=null)
					addProd.setVisibility(View.VISIBLE);
	    		
				if(catalogFrag!=null&&catalogContainer!=null)
				{
					FragmentManager fm = getSupportFragmentManager();
					fm.beginTransaction().hide(catalogFrag).commitAllowingStateLoss();
					catalogContainer.setVisibility(View.GONE);
				}
			}
			else
			{
				isPortrait = false;
				if(myPref.getIsTablet()&&catalogFrag!=null&&catalogContainer!=null)
				{
					if(addProd!=null)
						addProd.setVisibility(View.GONE);
					
					FragmentManager fm = getSupportFragmentManager();
					fm.beginTransaction().show(catalogFrag).commitAllowingStateLoss();
					catalogContainer.setVisibility(View.VISIBLE);
				}
			}
	    }
	    super.onConfigurationChanged(newConfig);
	}
	
	
	@Override
	public void onResume() {
		
		if(hasBeenCreated&&!global.loggedIn&&!global.getGlobalDlog().isShowing())
		{
			global.promptForMandatoryLogin(activity);
		}
		super.onResume();
	}
	
	
	@Override
	public void onStop()
	{
			if(global.getGlobalDlog()!=null&&global.getGlobalDlog().isShowing())
			{
				global.getGlobalDlog().dismiss();
			}
		super.onStop();
	}
	
	
	
	@Override
	public void onPause()
	{
		super.onPause();
		PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
		boolean isScreenOn = powerManager.isScreenOn();
		if(!isScreenOn||global.isApplicationSentToBackground(activity))
			global.loggedIn = false;
	}
	
	@Override
	public void onDestroy()
	{
		global.resetOrderDetailsValues();
		global.clearListViewData();
		super.onDestroy();
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

			int temp = global.searchType;
			global.searchType = 3;
			if (!isPortrait) {
				fragment = (CatalogLandscapeTwo) getSupportFragmentManager().findFragmentByTag("Landscape");
				if (myPref.getPreferences(MyPreferences.pref_fast_scanning_mode)) {
					String[] listData = handler.getUPCProducts(data);
					if (listData[0] != null)
					{
						if(myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku))
						{
							int foundPosition = global.checkIfGroupBySKU(activity, listData[0], "1");
							if(foundPosition!=-1)			//product already exist in list
							{
								global.refreshParticularOrder(myPref,foundPosition);
							}
							else
								automaticAddOrder(listData);
						}
						else
							automaticAddOrder(handler.getUPCProducts(data));
					}
				} else
					fragment.performSearch(data);
			} else {
				if (myPref.getPreferences(MyPreferences.pref_fast_scanning_mode)) {
					String[] listData = handler.getUPCProducts(data);
					if (listData[0] != null)
					{
						if(myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku))
						{
							int foundPosition = global.checkIfGroupBySKU(activity, listData[0], "1");
							if(foundPosition!=-1)			//product already exist in list
							{
								global.refreshParticularOrder(myPref,foundPosition);
							}
							else
								automaticAddOrder(listData);
						}
						else
							automaticAddOrder(listData);
					}
				}
			}
			global.searchType = temp;
		}
	}

	
	
	@Override
    public void onBackPressed() 
	{
		if(Global.isFromOnHold)
		{
			showDlog(true);
		}
		else
		{
			showDlog(false);
		}
    }
	
	
	private void showDlog(final boolean isFromOnHold)
	{
		final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(false);
		dlog.setCanceledOnTouchOutside(false);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		
		
		Button btnLeft = (Button)dlog.findViewById(R.id.btnDlogLeft);
		Button btnRight = (Button)dlog.findViewById(R.id.btnDlogRight);
		if(isFromOnHold)
		{
			viewMsg.setVisibility(View.GONE);
		viewTitle.setText(R.string.cust_dlog_choose_action);
		btnLeft.setText(R.string.button_void);
		btnRight.setText(R.string.button_cancel);
		}
		else
		{
			viewTitle.setText(R.string.warning_title);
			viewMsg.setText(R.string.warning_exit_now);
			btnLeft.setText(R.string.button_yes);
			btnRight.setText(R.string.button_no);
		}
		
		btnLeft.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				if(isFromOnHold)
					portraitFragment.voidCancelOnHold(1);
				else
					finish();
			}
		});
		btnRight.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
				if(isFromOnHold)
					portraitFragment.voidCancelOnHold(2);
					
			}
		});
		dlog.show();
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		
		if(resultCode == Global.FROM_DRAW_RECEIPT_PORTRAIT)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
		else if (resultCode == -1) 
		{
			global.resetOrderDetailsValues();
			global.clearListViewData();
			finish();
		}
		else if (resultCode == 1) {

			Bundle extras = data.getExtras();

			String newName = extras.getString("customer_name");
			Intent results = new Intent();
			results.putExtra("customer_name", newName);
			custName.setText(newName);
			setResult(1, results);
			//portraitFragment.initSpinners();
			fragment = (CatalogLandscapeTwo) getSupportFragmentManager().findFragmentByTag("Landscape");
			
			if(fragment!=null)
			{
				fragment.initAllProducts();
				if(fragment.categoriesGVAdapter!=null)
				{
					fragment.categoriesGVAdapter.notifyDataSetChanged();
					fragment.gridView.setAdapter(fragment.categoriesGVAdapter);
				}
				else if(fragment.adapter!=null)
				{
					fragment.adapter.notifyDataSetChanged();
					fragment.gridView.setAdapter(fragment.adapter);
				}
				
			}
		}
		else if(resultCode ==3)				//Void transaction from Sales Receipt
		{
			global.resetOrderDetailsValues();
			global.clearListViewData();
			finish();
		}
		else if(resultCode == 9)
		{
			Bundle extras = data.getExtras();

			String newName = extras.getString("prod_name");
			
			fragment = (CatalogLandscapeTwo) getSupportFragmentManager().findFragmentByTag("Landscape");
			
			if(fragment!=null)
			{
				Cursor c = fragment.getCursor();
				c.close();
				c = fragment.prodHandler.viewOtherTypes(fragment.db, newName);
				fragment.setCursor(c);
				fragment.adapter = fragment.new CustomCursorAdapter(activity, c, CursorAdapter.NO_SELECTION);
				fragment.gridView.setAdapter(fragment.adapter);
				fragment.adapter.notifyDataSetChanged();
				
			}
		}
	}
	
	
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) 
	{
		if(keyCode == 0)
		{
			if(isPortrait)
			{
				portraitFragment.fragOnKeyDown(keyCode);
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	
	public int getIntQty(String id) {
		String value = global.qtyCounter.get(id);

		if (value == null) {
			return 0;
		}
		return Integer.parseInt(value);
	}

	
	public void automaticAddOrder(String[] data)
	{
		global.automaticAddOrder(activity, false,global, data);
		ListView myListView = (ListView) findViewById(R.id.receiptListView);
		myListView.invalidateViews();
	}

	
}
