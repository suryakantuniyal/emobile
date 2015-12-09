package com.android.emobilepos.consignment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class ConsignmentCheckout_FA extends BaseFragmentActivityActionBar
{
	private Activity activity;
	private Global global;
	private boolean hasBeenCreated = false;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras =this.getIntent().getExtras();
		if(extras.getInt("consignmentType")==2)
			setContentView(R.layout.consign_fragment_container);
		else
			setContentView(R.layout.consign_pickup_fragment_container);

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
	public void onBackPressed() {
		showExitDlog();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (resultCode == 1) 
		{
			ConsignmentVisit_FR fragment = (ConsignmentVisit_FR) getSupportFragmentManager().findFragmentByTag("Consign");
			if(fragment!=null)
			{
				fragment.notifyListViewChange();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
		
	}
	
	
	private void showExitDlog()
	{
		final Dialog dlog = new Dialog(this,R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(false);
		dlog.setCanceledOnTouchOutside(false);
		dlog.setContentView(R.layout.dlog_btn_left_right_layout);
		
		TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
		
		
		Button btnLeft = (Button)dlog.findViewById(R.id.btnDlogLeft);
		Button btnRight = (Button)dlog.findViewById(R.id.btnDlogRight);

			viewTitle.setText(R.string.warning_title);
			viewMsg.setText(R.string.warning_exit_now);
			btnLeft.setText(R.string.button_yes);
			btnRight.setText(R.string.button_no);
		
		btnLeft.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
					finish();
			}
		});
		btnRight.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();
			}
		});
		dlog.show();
	}
	
}
