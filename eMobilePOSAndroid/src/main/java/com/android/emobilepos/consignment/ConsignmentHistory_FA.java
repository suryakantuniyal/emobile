package com.android.emobilepos.consignment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.CustomerInventoryHandler;
import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

public class ConsignmentHistory_FA extends BaseFragmentActivityActionBar
{
	private Activity activity;
	private Global global;
	private boolean hasBeenCreated = false;
	private ListView myListview;
	private CustomAdapter_LV myAdapter;
	private CustomerInventoryHandler custInventoryHandler;
	private Cursor custInventoryCursor;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.consignment_history_layout);
		global = (Global)getApplication();
		activity = this;
		custInventoryHandler = new CustomerInventoryHandler(activity);
		custInventoryCursor = custInventoryHandler.getCustomerInventoryCursor();
		myListview = (ListView) findViewById(R.id.consignmentHistoryListView);
		
		
		
		myAdapter = new CustomAdapter_LV(activity, custInventoryCursor, CursorAdapter.NO_SELECTION);
		myListview.setAdapter(myAdapter);
		hasBeenCreated = true;
	}
	
	@Override
	public void onResume() {

		if(global.isApplicationSentToBackground())
			Global.loggedIn = false;
		global.stopActivityTransitionTimer();
		
		if(hasBeenCreated&&!Global.loggedIn)
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
			Global.loggedIn = false;
		global.startActivityTransitionTimer();
	}
	
	
	public class CustomAdapter_LV extends CursorAdapter {
		LayoutInflater inflater;
		String tempPrice;
		double total;
		public CustomAdapter_LV(Context context, Cursor c, int flags) {
			super(context, c, flags);
			// TODO Auto-generated constructor stub
			inflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			
			ViewHolder holder = (ViewHolder)view.getTag();
			
			holder.prod_id.setText(cursor.getString(holder.i_prod_id));
			holder.prod_name.setText(cursor.getString(holder.i_prod_name));
			holder.last_update.setText(Global.formatToDisplayDate(cursor.getString(holder.i_last_update),  0));
			holder.prod_qty.setText(cursor.getString(holder.i_prod_qty));
			
			tempPrice = cursor.getString(holder.i_cust_price);
			if(tempPrice == null || tempPrice.isEmpty())
			{
				tempPrice = cursor.getString(holder.i_volume_price);
				if(tempPrice == null||tempPrice.isEmpty())
				{
					tempPrice = cursor.getString(holder.i_pricelevel_price);
					if(tempPrice == null||tempPrice.isEmpty())
					{
						tempPrice = cursor.getString(holder.i_chain_price);
						
						if(tempPrice == null || tempPrice.isEmpty())
						{
							tempPrice = cursor.getString(holder.i_master_price);
							if(tempPrice ==null||tempPrice.isEmpty())
								tempPrice = "0";
						}
					}
				}
			}
			
			holder.prod_price.setText(Global.getCurrencyFormat(tempPrice));
			total = Global.formatNumFromLocale(tempPrice)*Double.parseDouble(cursor.getString(holder.i_prod_qty));
			holder.prod_total.setText(Global.getCurrencyFormat(Double.toString(total)));

			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			View retView = inflater.inflate(R.layout.consignment_history_lvadapter, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.prod_name = (TextView) retView.findViewById(R.id.consignHistoryProdName);
			holder.prod_id = (TextView) retView.findViewById(R.id.consignHistoryProdID);
			holder.last_update = (TextView)retView.findViewById(R.id.consignHistoryDate);
			holder.prod_price = (TextView) retView.findViewById(R.id.consignHistoryPrice);
			holder.prod_qty = (TextView) retView.findViewById(R.id.consignHistoryQty);
			holder.prod_total = (TextView)retView.findViewById(R.id.consignHistoryTotal);
			
			
			holder.i_prod_name = cursor.getColumnIndex("prod_name");
			
			holder.i_prod_id = cursor.getColumnIndex("_id");
			holder.i_last_update = cursor.getColumnIndex("cust_update");
			
			holder.i_prod_qty = cursor.getColumnIndex("qty");
			holder.i_cust_price = cursor.getColumnIndex("cust_price");
			holder.i_volume_price = cursor.getColumnIndex("volume_price");
			holder.i_pricelevel_price = cursor.getColumnIndex("pricelevel_price");
			holder.i_chain_price = cursor.getColumnIndex("chain_price");
			holder.i_master_price = cursor.getColumnIndex("master_price");
			
			retView.setTag(holder);
			
			
			return retView;
		}
		
		private class ViewHolder
		{
			TextView prod_name;
			TextView prod_id;
			TextView last_update;
			TextView prod_price;
			TextView prod_qty;
			TextView prod_total;
			

			
			int i_prod_name,i_prod_id,i_last_update,i_prod_qty,
			i_cust_price,i_volume_price,i_pricelevel_price,i_chain_price,i_master_price;
		}

	}
}
