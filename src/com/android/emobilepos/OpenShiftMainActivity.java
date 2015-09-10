package com.android.emobilepos;

import com.android.support.Global;
import com.android.support.MyPreferences;
import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

public class OpenShiftMainActivity extends FragmentActivity{
	
	private Global global;
	private boolean hasBeenCreated = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		MyPreferences myPref = new MyPreferences(this);
		global = (Global)getApplication();
		if(!myPref.getIsTablet())						//reset to default layout (not as dialog)
			super.setTheme(R.style.AppTheme);
		
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.open_shift_fragment);
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

}
