package com.android.emobilepos;

import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.android.support.Global;
import com.google.analytics.tracking.android.EasyTracker;

public class ConsignmentFragmentActivity extends FragmentActivity
{
	private Activity activity;
	private Global global;
	private boolean hasBeenCreated = false;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.consignment_fragment_container);
		global = (Global)getApplication();
		activity = this;
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
	public void onDestroy()
	{
		//fragWasCreated = false;
		global.resetOrderDetailsValues();
		global.clearListViewData();
		super.onDestroy();
	}
}
