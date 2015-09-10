package com.android.emobilepos;


import com.android.database.ConsignmentTransactionHandler;
import com.android.database.CustomerInventoryHandler;
import com.android.support.Global;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


public class ConsignmentHistoryFragment extends Fragment
{
	private ListView myListview;
	private CustomCursorAdapter myAdapter;
	private CustomerInventoryHandler custInventoryHandler;
	private Cursor custInventoryCursor;
	private Activity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.consignment_history_layout, container, false);
		activity = getActivity();
		
		custInventoryHandler = new CustomerInventoryHandler(activity);
		custInventoryCursor = custInventoryHandler.getCustomerInventoryCursor();
		myListview = (ListView) view.findViewById(R.id.consignmentHistoryListView);
		
		
		
		myAdapter = new CustomCursorAdapter(activity, custInventoryCursor, CursorAdapter.NO_SELECTION);
		myListview.setAdapter(myAdapter);
		return view;
	}
	
	@Override
	public void onDestroy()
	{
		
		custInventoryCursor.close();
		super.onDestroy();
	}
	
	
	
//	private class printAsync extends AsyncTask<String, String, String> 
//	{
//		@Override
//		protected void onPreExecute() {
//			myProgressDialog = new ProgressDialog(activity);
//			myProgressDialog.setMessage("Printing...");
//			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//			myProgressDialog.setCancelable(false);
//			myProgressDialog.show();
//
//		}
//
//		@Override
//		protected String doInBackground(String... params) {
//			// TODO Auto-generated method stub
//
//			if(Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice!=null)
//				Global.mainPrinterManager.currentDevice.printConsignment(consTransHandler.getLastConsTransaction(),global.encodedImage);
//			global.encodedImage = new String();
//			
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(String unused) {
//			myProgressDialog.dismiss();	
//		}
//	}
	
	
	
	public class CustomCursorAdapter extends CursorAdapter {
		LayoutInflater inflater;
		String tempPrice;
		double total;
		public CustomCursorAdapter(Context context, Cursor c, int flags) {
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
			holder.last_update.setText(Global.formatToDisplayDate(cursor.getString(holder.i_last_update), activity, 0));
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
			
			holder.prod_price.setText(Global.formatDoubleStrToCurrency(tempPrice));
			total = Global.formatNumFromLocale(tempPrice)*Double.parseDouble(cursor.getString(holder.i_prod_qty));
			holder.prod_total.setText(Global.formatDoubleStrToCurrency(Double.toString(total)));

			
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
			TextView last_update;// = (TextView) view.findViewById(R.id.catalogItemName);
			TextView prod_price;// = (TextView) view.findViewById(R.id.catalogItemQty);
			TextView prod_qty;// = (TextView) view.findViewById(R.id.catalogItemPrice);
			TextView prod_total;// = (TextView) view.findViewById(R.id.catalogItemInfo);
			

			
			int i_prod_name,i_prod_id,i_last_update,i_prod_qty,
			i_cust_price,i_volume_price,i_pricelevel_price,i_chain_price,i_master_price;
		}

	}
	

}
