package com.android.emobilepos.history;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.CustomersHandler;
import com.android.database.TransferLocations_DB;
import com.android.emobilepos.R;
import com.android.emobilepos.history.details.HistoryInventoryTransferDetails_FA;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class HistoryInventoryTransfer_FA extends BaseFragmentActivityActionBar implements OnItemClickListener{
	
	private Activity activity;
	private Cursor c;
	private TransferLocations_DB dbHandler;
	private Global global;
	private boolean hasBeenCreated = false;
	private ListView lView;
	private CustomCursorAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_inventory_transfer_layout);
		activity = this;
		global = (Global)getApplication();
		lView = (ListView)findViewById(R.id.listView);
		dbHandler = new TransferLocations_DB(this);
		c = dbHandler.getAllTransactions();
		adapter = new CustomCursorAdapter(this, c, CursorAdapter.NO_SELECTION);
		lView.setAdapter(adapter);
		lView.setOnItemClickListener(this);
		
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
	
	
	
	public void performSearch(String text) {
		if (c != null)
			c.close();

		adapter = new CustomCursorAdapter(activity, c, CursorAdapter.NO_SELECTION);
		lView.setAdapter(adapter);
	}
	
	
	
	public class CustomCursorAdapter extends CursorAdapter {
		
		
		LayoutInflater inflater;
		CustomersHandler custHandler = new CustomersHandler(activity);
		Global global = (Global) activity.getApplication();
		ViewHolder myHolder;
		String temp = new String();
		String empStr = "";

		public CustomCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			// TODO Auto-generated constructor stub
			inflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub

			myHolder = (ViewHolder)view.getTag();
			
			myHolder.transaction_id.setText(cursor.getString(myHolder.i_trans_id));
			//myHolder.clientName.setText(cursor.getString(myHolder.i_cust_name));
			
			
			
			if(cursor.getString(myHolder.i_is_synched).equals("1")) //it is synched
				myHolder.syncIcon.setImageResource(R.drawable.is_sync);
			else
				myHolder.syncIcon.setImageResource(R.drawable.is_not_sync);
			
		}

		public String format(String text) {

			if (TextUtils.isEmpty(text))
				return Global.formatDoubleToCurrency(0.00);
			return Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(text)));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			View view = inflater.inflate(R.layout.history_transfer_lv_adapter, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.transaction_id = (TextView) view.findViewById(R.id.transferID);
			holder.from_location = (TextView) view.findViewById(R.id.transferFrom);
			holder.to_location = (TextView)view.findViewById(R.id.transferTo);
			holder.syncIcon = (ImageView)view.findViewById(R.id.syncIcon);
			
			holder.i_trans_id = cursor.getColumnIndex(TransferLocations_DB.trans_id);
			//holder.i_cust_name = cursor.getColumnIndex(TransferLocations_DB.);
			holder.i_is_synched = cursor.getColumnIndex(TransferLocations_DB.issync);
			
			
			view.setTag(holder);
			return view;
		}
		
		private class ViewHolder
		{
			TextView transaction_id,from_location,to_location;
			ImageView syncIcon;
			
			int i_trans_id,i_from_location,i_to_location,i_is_synched;
		}
	}



	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		c.moveToPosition(position);
		Intent intent = new Intent(this, HistoryInventoryTransferDetails_FA.class);
		intent.putExtra("transfer_id", c.getString(c.getColumnIndex(TransferLocations_DB.trans_id)));
		intent.putExtra("trans_date", c.getString(c.getColumnIndex(TransferLocations_DB.trans_timecreated)));
		intent.putExtra("loc_key_from", c.getString(c.getColumnIndex(TransferLocations_DB.loc_key_from)));
		intent.putExtra("loc_key_to", c.getString(c.getColumnIndex(TransferLocations_DB.loc_key_to)));
		startActivity(intent);
	}
}
