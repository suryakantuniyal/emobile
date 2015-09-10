package com.android.emobilepos;


import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.android.catalog.fragments.CatalogFragTwo;
import com.android.catalog.fragments.CatalogLandscapeTwo;
import com.android.database.ProductsHandler;
import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.MyPreferences;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Window;

public class CatalogMenuFragmentActivity extends SherlockFragmentActivity {

	private static String ourIntentAction = "";
	private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";

	private MyPreferences myPref;
	private CatalogLandscapeTwo landscapeFrag;
	private CatalogFragTwo portraitFrag;
	private Global global;
	private ProductsHandler handler;
	private int orientation;
	private boolean isPortrait;
	private boolean hasBeenCreated = false;
	private Activity activity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		
		
		setContentView(R.layout.catalog_mainfrag_layout);

		myPref = new MyPreferences(this);
		global = (Global) getApplication();
		handler = new ProductsHandler(this);
		activity = this;
		orientation = getResources().getConfiguration().orientation;

		//global.initSettingsValues(this);
		
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			isPortrait = false;
			landscapeFrag = (CatalogLandscapeTwo) getSupportFragmentManager().findFragmentByTag("Landscape");
		} else {
			isPortrait = true;
			portraitFrag = (CatalogFragTwo) getSupportFragmentManager().findFragmentByTag("Portrait");
		}

		hasBeenCreated = true;
		ourIntentAction = getString(R.string.intentAction2);
		// in case we have been launched by the DataWedge intent plug-in
		// using the StartActivity method let's handle the intent
		Intent i = getIntent();
		handleDecodeData(i);
	}


	@Override
	public void onResume() {
		
	if(hasBeenCreated&&!global.loggedIn&&global.getGlobalDlog()!=null&&!global.getGlobalDlog().isShowing())
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
    public void onBackPressed() 
	{
		super.onBackPressed();
		Global.cat_id = "0";
		this.finish();
			
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 2) {
			setResult(2);
		}
	}

	@Override
	public void onNewIntent(Intent i) {
		super.onNewIntent(i);
		handleDecodeData(i);
	}

	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == 0)
		{
			CatalogMainActivity fragTest = (CatalogMainActivity)getSupportFragmentManager().findFragmentById(R.id.catalogMainFragmentID);
			if(fragTest!=null)
				fragTest.fragOnKeyDown(keyCode);
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private void handleDecodeData(Intent i) {
		// check the intent action is for us
		
		if (i.getAction() != null && i.getAction().contentEquals(ourIntentAction)) {

			// get the data from the intent
			String [] dataArray;
			String data = i.getStringExtra(DATA_STRING_TAG);

			int temp = global.searchType;
			global.searchType = 3;

			if (isPortrait) {
				portraitFrag = (CatalogFragTwo) getSupportFragmentManager().findFragmentByTag("Portrait");
				if (myPref.getPreferences(MyPreferences.pref_fast_scanning_mode))
				{
					dataArray = handler.getUPCProducts(data);
					if(dataArray != null)
					{
						if(myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku))
						{
							int foundPosition = global.checkIfGroupBySKU(activity, dataArray[0], "1");
							if(foundPosition!=-1)			//product already exist in list
							{
								global.refreshParticularOrder(myPref,foundPosition);
							}
							else
							{
								portraitFrag.automaticAddOrder(dataArray);
							}
						}
						else
							portraitFrag.automaticAddOrder(dataArray);
					}
					
				}
				else
					portraitFrag.performSearch(data);
			} 
			else 
			{
				landscapeFrag = (CatalogLandscapeTwo) getSupportFragmentManager().findFragmentByTag("Landscape");
				if (myPref.getPreferences(MyPreferences.pref_fast_scanning_mode)) 
				{
					dataArray = handler.getUPCProducts(data);
					if (dataArray != null)
					{
						if(myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku))
						{
							int foundPosition = global.checkIfGroupBySKU(activity, dataArray[0], "1");
							if(foundPosition!=-1)			//product already exist in list
							{
								global.refreshParticularOrder(myPref,foundPosition);
							}
							else
								landscapeFrag.automaticAddOrder(dataArray);
						}
						else
							landscapeFrag.automaticAddOrder(dataArray);
					}
				} 
				else
					landscapeFrag.performSearch(data);
			}

			global.searchType = temp;

		}

	}

}
