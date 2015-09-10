package com.android.emobilepos.adapters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;


import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;

import android.widget.TextView;


import com.android.database.ConsignmentTransactionHandler;
import com.android.database.CustomersHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.TemplateHandler;
import com.android.database.TransferLocations_DB;
import com.android.database.VoidTransactionsHandler;

import com.emobilepos.app.R;
import com.android.support.DBManager;
import com.android.support.Global;
import com.android.support.MyPreferences;


public class SynchMenuAdapter extends BaseAdapter implements Filterable {

	private Activity activity;

	private LayoutInflater mInflater;
	
	private String[] listText;
	private String[] listText2;

	
	private MyPreferences myPref;
	private Map<String,String> hashedPending;
	private String connectedNetwork;
	private String lastSendSync,lastReceiveSync;
	
	private DBManager dbManager;
	private AlertDialog.Builder alertDlogBuilder;
	private Dialog promptDialog;
	
	
	

	public SynchMenuAdapter(Activity activity) {
		this.activity = activity;
		mInflater = LayoutInflater.from(activity.getApplicationContext());

		myPref = new MyPreferences(activity);
		listText = new String[] { "", getString(R.string.sync_connected_to), "", getString(R.string.sync_payments), 
				getString(R.string.sync_sales), getString(R.string.sync_signatures),getString(R.string.sync_consignment), "Dates", };
		listText2 = new String[] { "", "", "", getString(R.string.sync_templates), getString(R.string.sync_customers), 
				getString(R.string.sync_void),getString(R.string.sync_transfer), "" };
		
		
		
		dbManager = new DBManager(activity,Global.FROM_SYNCH_ACTIVITY);
		if(dbManager.isNewDBVersion()&&dbManager.unsynchItemsLeft())
		{
			alertDlogBuilder = new AlertDialog.Builder(activity);
			
			promptDialog = alertDlogBuilder.setTitle("Urgent").setCancelable(false)
					.setMessage("A new Database version needs to be installed.\n You must synchronize items before proceeding...").
					setPositiveButton("Synchronize", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface thisDialog, int which) {
							// TODO Auto-generated method stub
							//SQLiteDatabase db = dbManager.openWritableDB();
							dbManager.synchSend(true,false);
							promptDialog.dismiss();
						}
					}).create();
			
			promptDialog.show();
		}
		
		
		initializeListValues();
	}
	
	
	private String getString(int id)
	{
		return activity.getResources().getString(id);
	}
	
	
	private void initializeListValues()
	{
		hashedPending =  createPendingMap();
		connectedNetwork = getWifiConnectivityName();
		
		lastSendSync = myPref.getLastSendSync();
		lastReceiveSync = myPref.getLastReceiveSync();
	}
	
	
	private final Map<String, String> createPendingMap() {
		HashMap<String, String> result = new HashMap<String, String>();
		final String defaultVal = "0";
		
		
		PaymentsHandler paymentHandler = new PaymentsHandler(activity);
		int unsyncPayments = (int) paymentHandler.getNumUnsyncPayments();
		if(unsyncPayments>0)
			result.put(getString(R.string.sync_payments), Integer.toString(unsyncPayments));
		else
			result.put(getString(R.string.sync_payments), defaultVal);
		
		
		
		OrdersHandler ordersHandler = new OrdersHandler(activity);
		int unsycOrders = (int) ordersHandler.getNumUnsyncOrders();
		if(unsycOrders>0)
			result.put(getString(R.string.sync_sales), Integer.toString(unsycOrders));
		else
			result.put(getString(R.string.sync_sales), defaultVal);
		
		
		
		VoidTransactionsHandler voidHandler = new VoidTransactionsHandler(activity);
		int unsyncVoids = (int) voidHandler.getNumUnsyncVoids();
		if(unsyncVoids>0)
			result.put(getString(R.string.sync_void), Integer.toString(unsyncVoids));
		else
			result.put(getString(R.string.sync_void), defaultVal);
			
		
		result.put(getString(R.string.sync_signatures), defaultVal);
		result.put(getString(R.string.sync_templates), defaultVal);
		
		
		
		CustomersHandler custHandler = new CustomersHandler(activity);
		int unsyncCust = (int) custHandler.getNumUnsyncCustomers();
		if(unsyncCust>0)
			result.put(getString(R.string.sync_customers), Integer.toString(unsyncCust));
		else
			result.put(getString(R.string.sync_customers), defaultVal);
		
		
		
		TemplateHandler templateHandler = new TemplateHandler(activity);
		int unsyncTemplates = (int) templateHandler.getNumUnsyncTemplates();
		if(unsyncTemplates>0)
			result.put(getString(R.string.sync_templates), Integer.toString(unsyncTemplates));
		else
			result.put(getString(R.string.sync_templates), defaultVal);
		
		
		ConsignmentTransactionHandler consignmentHandler = new ConsignmentTransactionHandler(activity);
		int unsyncConsignment = (int)consignmentHandler.getNumUnsyncItems();
		if(unsyncConsignment>0)
			result.put(getString(R.string.sync_consignment), Integer.toString(unsyncConsignment));
		else
			result.put(getString(R.string.sync_consignment), defaultVal);
		
		TransferLocations_DB transferDB = new TransferLocations_DB(activity);
		int unsyncTransfer = (int)transferDB.getNumUnsyncTransfers();
		if(unsyncTransfer>0)
			result.put(getString(R.string.sync_transfer), Integer.toString(unsyncTransfer));
		else
			result.put(getString(R.string.sync_transfer), defaultVal);
		
		return Collections.unmodifiableMap(result);
	}
	
	
	private String getUnsyncCount(String key)
	{
		return hashedPending.get(key);
	}
	
	
	private String getWifiConnectivityName()
	{
		String wifiName = getString(R.string.sync_no_connectivity);
		StringBuilder sb = new StringBuilder();
		
		ConnectivityManager connManager = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo myWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo myMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if(myWifi!=null&&myWifi.isConnected())
		{
			WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			sb.append(getString(R.string.sync_connected_to)).append(": ").append(wifiInfo.getSSID());
			wifiName = sb.toString();
		}
		else if(myMobile!=null&&myMobile.isConnected())
		{
			wifiName = sb.append(getString(R.string.sync_connected_to)).append(": Carrier's Network").toString();
		}
		
		return wifiName;
	}
	
	
	public boolean findValue(int[] array, int position) {
		int size = array.length;

		for (int i = 0; i < size; i++) {
			if (array[i] == position) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub

		return listText.length;
	}

	@Override
	public Object getItem(int index) {
		// TODO Auto-generated method stub
		return listText[index];
	}

	@Override
	// use the 'position' or array index as item id
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ViewHolder holder;
		int type = getItemViewType(position);
		

		if (convertView == null) {

			holder = new ViewHolder();
			switch (type) {
			case 0:// Buttons---->synch_row1
				convertView = mInflater.inflate(R.layout.synch_row1adapter, null);
				holder.leftButton = (Button) convertView.findViewById(R.id.synchButton1);
				holder.rightButton = (Button) convertView.findViewById(R.id.synchButton2);

				holder.leftButton.setText(getString(R.string.sync_send));
				holder.rightButton.setText(getString(R.string.sync_receive));
				break;
			case 1: // synch_row2
				convertView = mInflater.inflate(R.layout.synch_row2adapter, null);
				holder.lTitle = (TextView) convertView.findViewById(R.id.synchFeedText);
				holder.lTitle.setText(connectedNetwork);
				break;
			case 2:// synch_row3
				convertView = mInflater.inflate(R.layout.synch_row3adapter, null);

				break;
			case 3: // synch_row4
				convertView = mInflater.inflate(R.layout.synch_row4adapter, null);
				holder.lTitle = (TextView) convertView.findViewById(R.id.leftTitle);
				holder.lValue = (TextView) convertView.findViewById(R.id.leftQty);
				holder.rTitle = (TextView) convertView.findViewById(R.id.rightTitle);
				holder.rValue = (TextView) convertView.findViewById(R.id.rightQty);

				holder.lTitle.setText(listText[position]);
				holder.lValue.setText(getUnsyncCount(listText[position]));
				holder.rTitle.setText(listText2[position]);
				holder.rValue.setText(getUnsyncCount(listText2[position]));
				break;

			case 4: // synch_row5
				convertView = mInflater.inflate(R.layout.synch_row5adapter, null);
				holder.lTitle = (TextView) convertView.findViewById(R.id.synchSendDate);
				holder.rTitle = (TextView) convertView.findViewById(R.id.synchReceiveDate);

				holder.lTitle.setText(lastSendSync);
				holder.rTitle.setText(lastReceiveSync);
				break;
			}

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (type == 0) {
			holder.leftButton.setText(getString(R.string.sync_send));

			holder.leftButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					DBManager dbManager = new DBManager(activity,Global.FROM_SYNCH_ACTIVITY);
					//SQLiteDatabase db = dbManager.openWritableDB();
					dbManager.synchSend(false,false);
					
				}
			});

			holder.rightButton.setText(getString(R.string.sync_receive));

			holder.rightButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					DBManager dbManager = new DBManager(activity,Global.FROM_SYNCH_ACTIVITY);
					if(dbManager.unsynchItemsLeft())
					{
						
						Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.send_unsync_items_first));
					}
					else
					{
						dbManager.synchReceive();
					}
				}
			});
		}

		else if (type == 1) {
			holder.lTitle.setText(connectedNetwork);
		} else if (type == 3) {
			holder.lTitle.setText(listText[position]);
			holder.lValue.setText(getUnsyncCount(listText[position]));
			holder.rTitle.setText(listText2[position]);
			holder.rValue.setText(getUnsyncCount(listText2[position]));
		} else if (type == 4) {
			holder.lTitle.setText(lastSendSync);
			holder.rTitle.setText(lastReceiveSync);
		}
		return convertView;
	}

	public class ViewHolder {
		TextView lTitle;
		TextView lValue;
		TextView rTitle;
		TextView rValue;
		Button leftButton;
		Button rightButton;

	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return 0;
		} else if (position == 1) {
			return 1;
		} else if (position == 2) {
			return 2;
		} else if (position == 7) {
			return 4;
		}
		return 3;
	}

	  @Override
	  public void notifyDataSetChanged() 
	  {
		  initializeListValues();
	    super.notifyDataSetChanged();
	  }
	  
	@Override
	public int getViewTypeCount() {
		return 5;
	}

	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		return null;
	}
}
