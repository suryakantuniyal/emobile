package com.android.emobilepos;

import com.android.support.Global;
import com.android.support.MyPreferences;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;

public class CatalogPickerFragActivity extends FragmentActivity {

	private boolean hasBeenCreated = false;
	private Activity activity;
	private Global global;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		MyPreferences myPref = new MyPreferences(this);
		
		if(!myPref.getIsTablet())						//reset to default layout (not as dialog)
			super.setTheme(R.style.AppTheme);
		
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		
		
		setContentView(R.layout.catalog_picker_fragment);
		
		if(myPref.getIsTablet())						//add margins to the picker layout for better view
		{
			LinearLayout layout = (LinearLayout)findViewById(R.id.rootPickerLinearLayout);
			LayoutParams params = (LayoutParams)layout.getLayoutParams();
			
			params.setMargins(0, 35, 0, 25);
			layout.setLayoutParams(params);
		}
		
		
		activity = this;
		global = (Global)getApplication();
		hasBeenCreated = true;
	}
	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 2) {
			setResult(2);
		}
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
}
