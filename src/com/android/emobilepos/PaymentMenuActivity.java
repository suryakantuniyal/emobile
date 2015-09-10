package com.android.emobilepos;

import com.android.menuadapters.CardsListAdapter;
import com.android.support.Global;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class PaymentMenuActivity extends Activity {

	private CardsListAdapter myAdapter;
	private ListView myListview;
	private String total;
	private String paid;
	
	private boolean hasBeenCreated = false;
	private Global global;
	private Activity activity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.card_list_layout);
		myListview = (ListView) findViewById(R.id.cardsListview);
		global = (Global)getApplication();
		activity = this;
		total = "$0.00";
		paid = "$0.00";

		myAdapter = new CardsListAdapter(this, total, paid, this);
		myListview.setAdapter(myAdapter);

		myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub

				if (position != 1) {
					Intent intent = new Intent(arg0.getContext(), ProcessCardMenuActivity.class);
					startActivity(intent);
				}

			}
		});
		hasBeenCreated = true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(hasBeenCreated&&!global.loggedIn)
		{
			if(global.getPromptDialog()!=null)
				global.getPromptDialog().dismiss();
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
