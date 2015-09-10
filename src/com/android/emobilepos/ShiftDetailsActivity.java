package com.android.emobilepos;

import com.android.menuadapters.ShiftDetailsAdapter;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;

public class ShiftDetailsActivity extends FragmentActivity{
	
	private Global global;
	private boolean hasBeenCreated = false;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.shift_details_layout);
		global = (Global)getApplication();
		ListView lView = (ListView)findViewById(R.id.shiftDetailsListView);
		Bundle extras = this.getIntent().getExtras();
		ShiftDetailsAdapter adapter = new ShiftDetailsAdapter(this,extras.getString("shift_id"));
		lView.setAdapter(adapter);
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
