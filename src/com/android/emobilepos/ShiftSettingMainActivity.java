package com.android.emobilepos;

import com.android.support.Global;
import com.android.support.MyPreferences;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ShiftSettingMainActivity extends Activity
{
	private Global global;
	private boolean hasBeenCreated = false;
	private Activity activity;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.shifts_main_layout);
		
		activity = this;
		global = (Global)getApplication();
		MyPreferences myPref = new MyPreferences(this);
		ListView lView = (ListView)findViewById(R.id.shiftsListView);
		ArrayAdapter<String>menuAdapter;
		String[] lViewValues = new String[2];
		if(myPref.getShiftIsOpen())
			lViewValues[0] = getString(R.string.shift_close_shift)+" <"+myPref.getClerkName()+">";
		else
			lViewValues[0] = getString(R.string.shift_open_shift);
		
		lViewValues[1] = getString(R.string.shift_expenses);
		menuAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,lViewValues);
		
		lView.setCacheColorHint(Color.TRANSPARENT);
		lView.setAdapter(menuAdapter);
		lView.setItemsCanFocus(false);
		//lView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				if(position==0)
				{
					Intent intent = new Intent(activity,OpenShiftMainActivity.class);
					startActivity(intent);
				}
				
			}
		});
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
