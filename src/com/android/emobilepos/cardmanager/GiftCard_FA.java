package com.android.emobilepos.cardmanager;

import com.android.emobilepos.adapters.GiftLoyaltyRewardLV_Adapter;
import com.android.emobilepos.adapters.GiftLoyaltyRewardLV_Adapter.ViewHolder;
import com.android.emobilepos.cardmanager.CardManager_FA.GiftCardActions;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.emobilepos.app.R;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class GiftCard_FA extends FragmentActivity implements OnItemClickListener{
	
	
	private boolean hasBeenCreated = false;
	private Global global;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gift_loyal_reward_main_layout);
		
		global = (Global)getApplication();
		TextView headerTitle = (TextView)findViewById(R.id.HeaderTitle);
		headerTitle.setText(getString(R.string.header_title_gift_card));
		
		ListView lView = (ListView)findViewById(R.id.listView);
		GiftLoyaltyRewardLV_Adapter adapter = new GiftLoyaltyRewardLV_Adapter(this,0);
		lView.setAdapter(adapter);
		lView.setOnItemClickListener(this);
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
	
	
	
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int pos, long arg3) {

		Intent intent = new Intent(this,CardManager_FA.class);
		intent.putExtra("CARD_TYPE", CardManager_FA.CASE_GIFT);
		ViewHolder viewHolder = (ViewHolder) view.getTag();
		switch(viewHolder.giftCardActions)
		{
		case CASE_ACTIVATE:
			intent.putExtra("PROCESS_TYPE", CardManager_FA.GiftCardActions.CASE_ACTIVATE.getCode());
			startActivity(intent);
			break;
		case CASE_ADD_BALANCE:
			intent.putExtra("PROCESS_TYPE", CardManager_FA.GiftCardActions.CASE_ADD_BALANCE.getCode());
			startActivity(intent);
			break;
		case CASE_BALANCE_INQUIRY:
			intent.putExtra("PROCESS_TYPE", CardManager_FA.GiftCardActions.CASE_BALANCE_INQUIRY.getCode());
			startActivity(intent);
			break;
		case CASE_MANUAL_ADD:
			intent.putExtra("PROCESS_TYPE", CardManager_FA.GiftCardActions.CASE_MANUAL_ADD.getCode());
			promptManagerPassword(intent);
			break;
		}
		
	}
	
	
	private void promptManagerPassword(final Intent intent)
	{
		final Dialog globalDlog = new Dialog(this,R.style.Theme_TransparentTest);
		globalDlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		globalDlog.setCancelable(true);
		globalDlog.setContentView(R.layout.dlog_field_single_layout);
		
		
		final MyPreferences myPref = new MyPreferences(this);
		final EditText viewField = (EditText)globalDlog.findViewById(R.id.dlogFieldSingle);
		viewField.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
		TextView viewTitle = (TextView)globalDlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView)globalDlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);

			viewMsg.setText(R.string.dlog_title_enter_manager_password);
		
		Button btnOk = (Button)globalDlog.findViewById(R.id.btnDlogSingle);
		btnOk.setText(R.string.button_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				globalDlog.dismiss();
				String pass = viewField.getText().toString();
				if(!pass.isEmpty()&&myPref.posManagerPass(true, null).equals(pass.trim()))
				{
					startActivity(intent);
				}
				else
				{
					promptManagerPassword(intent);
				}
			}
		});
		globalDlog.show();
	}
	
	
}
