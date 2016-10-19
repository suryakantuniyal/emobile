package com.android.emobilepos.consignment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class ConsignmentMain_FA extends BaseFragmentActivityActionBar implements OnItemClickListener
{
	private Activity activity;
	private Global global;
	private boolean hasBeenCreated = false;
	private ListView myListview;
	private CustomAdapter_LV myAdapter;
	private final int CASE_VISIT = 0, CASE_PICKUP = 1, CASE_HISTORY = 2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.consignment_main_fragment);

		this.activity = this;
		global = (Global)getApplication();
		myListview = (ListView) findViewById(R.id.consignmentListView);
		myAdapter = new CustomAdapter_LV(this);
		myListview.setAdapter(myAdapter);
		myListview.setOnItemClickListener(this);
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		switch(position)
		{
		case CASE_VISIT:
			startConsignmentVisit();
			break;
		case CASE_PICKUP:
			startConsignmentPickup();
			break;
		case CASE_HISTORY:
			startConsignmentHistory();
			break;
		}
	}
	
	private void startConsignmentVisit()
	{
		global.resetOrderDetailsValues();
		global.clearListViewData();
		Intent intent = new Intent(this,OrderingMain_FA.class);
		intent.putExtra("option_number", Global.TransactionType.CONSIGNMENT);
		intent.putExtra("consignmentType", Global.OrderType.ORDER);
		startActivity(intent);
	}
	
	private void startConsignmentPickup()
	{
		global.resetOrderDetailsValues();
		global.clearListViewData();
		Intent intent = new Intent(this,OrderingMain_FA.class);
		//Intent intent = new Intent(arg0.getContext(), SalesReceiptSplitActivity.class);
		intent.putExtra("option_number", Global.TransactionType.CONSIGNMENT);
		intent.putExtra("consignmentType", Global.OrderType.CONSIGNMENT_PICKUP);
		startActivity(intent);
	}
	
	private void startConsignmentHistory()
	{
		Intent intent = new Intent(this,ConsignmentHistory_FA.class);
		startActivity(intent);
	}
	
	
	private class CustomAdapter_LV extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context context;
		private  String[] lvTitle;

		
		public CustomAdapter_LV(Context context) 
		{
			mInflater = LayoutInflater.from(context);
			this.context = context;
			lvTitle = new String[]{getString(R.string.consignment_consign), getString(R.string.consignment_pickup),getString(R.string.consignment_history)};
		}

		
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			ViewHolder holder;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.consignment_main_lvadapter, null);

				holder = new ViewHolder();
				holder.textLine = (TextView) convertView.findViewById(R.id.consignmentLVTitle);

				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.textLine.setText(lvTitle[position]);

			return convertView;
		}

		private String getString(int id)
		{
			return context.getResources().getString(id);
		}
		
		public class ViewHolder {
			TextView textLine;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return lvTitle.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return lvTitle[position];
		}
	}
	
	
}
