package com.android.emobilepos;

import com.android.support.Global;

import android.app.Activity;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

public class HistRefundMenuActivity extends FragmentActivity {

	private boolean hasBeenCreated = false;
	private Global global;
	private Activity activity;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.hist_refund_layout);
		global = (Global)getApplication();
		activity = this;
		hasBeenCreated = true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(hasBeenCreated&&!global.loggedIn)
		{
			if(global.getGlobalDlog()!=null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(activity);
		}
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
}