package com.android.emobilepos.ordering;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.database.ProductAddonsHandler;
import com.emobilepos.app.R;
import com.android.support.Global;
import com.android.support.MyPreferences;

public class ReceiptMainLV_Adapter extends BaseAdapter {

	private LayoutInflater myInflater;
	private Global global;
	private Activity activity;
	private MyPreferences myPref;

	public ReceiptMainLV_Adapter(Activity activity) {

		this.activity = activity;
		myInflater = LayoutInflater.from(activity);
		global = (Global)activity.getApplication();
		myPref = new MyPreferences(activity);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (global.orderProducts != null) {
			return global.orderProducts.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	@Override
	public int getItemViewType(int position) {
		String t = global.orderProducts.get(position).item_void;
		if (t.equals("")||t.equals("0")) //divider
		{
			return 0;
		}
		return 1;
	}
	

	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ViewHolder holder;
		int type = getItemViewType(position);
		if (convertView == null) {
			holder = new ViewHolder();

			switch(type)
			{
				case 0:
					convertView = myInflater.inflate(R.layout.product_receipt_adapter, null);
					break;
				case 1:
					convertView = myInflater.inflate(R.layout.product_receiptvoid_adapter, null);
					break;
			}
			
			holder.itemQty = (TextView) convertView.findViewById(R.id.itemQty);
			holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
			holder.itemAmount = (TextView) convertView.findViewById(R.id.itemAmount);
			holder.distQty = (TextView) convertView.findViewById(R.id.distQty);
			holder.distAmount = (TextView) convertView.findViewById(R.id.distAmount);
			holder.granTotal = (TextView) convertView.findViewById(R.id.granTotal);
			
			holder.addonButton = (Button)convertView.findViewById(R.id.addonButton);
			if(holder.addonButton!=null)
				holder.addonButton.setFocusable(false);
			
			

			setHolderValues(holder, position);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			setHolderValues(holder, position);
		}

		return convertView;
	}

	public void setHolderValues(ViewHolder holder, int position) {

		final int pos = position;
		final String tempId = global.orderProducts.get(pos).ordprod_id;
		
		if(!myPref.getPreferences(MyPreferences.pref_restaurant_mode)||(myPref.getPreferences(MyPreferences.pref_restaurant_mode)&&(Global.addonSelectionMap==null||(Global.addonSelectionMap!=null&&!Global.addonSelectionMap.containsKey(tempId)))))
		{
			if(holder.addonButton!=null)
				holder.addonButton.setVisibility(View.INVISIBLE);
		}
		else
		{
			if(holder.addonButton!=null)
			{
				holder.addonButton.setVisibility(View.VISIBLE);
				holder.addonButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(activity, PickerAddon_FA.class);
						String prodID = global.orderProducts.get(pos).prod_id;
						global.addonSelectionType = Global.addonSelectionMap.get(tempId);
						
						intent.putExtra("addon_map_key", tempId);
						intent.putExtra("isEditAddon", true);						
						intent.putExtra("prod_id",prodID);
						intent.putExtra("item_position", pos);
						
						
						ProductAddonsHandler prodAddonsHandler = new ProductAddonsHandler(activity);
						Global.productParentAddons  = prodAddonsHandler.getParentAddons(prodID);
						
						activity.startActivityForResult(intent, 0);
					}
				});
			}
		}
		
		holder.itemQty.setText(global.orderProducts.get(position).ordprod_qty);
		holder.itemName.setText(global.orderProducts.get(position).ordprod_name);
		
		String temp = Global.formatNumToLocale(Double.parseDouble(global.orderProducts.get(position).overwrite_price));
		holder.itemAmount.setText(Global.getCurrencyFormat(temp));
		
		
		holder.distQty.setText(global.orderProducts.get(position).disAmount);
		temp = Global.formatNumToLocale(Double.parseDouble(global.orderProducts.get(position).disTotal));
		holder.distAmount.setText(Global.getCurrencyFormat(temp));

		// to-do calculate tax

		temp = Global.formatNumToLocale(Double.parseDouble(global.orderProducts.get(position).itemTotal));
		holder.granTotal.setText(Global.getCurrencyFormat(temp));

	}

	
	public class ViewHolder 
	{
		TextView itemQty;
		TextView itemName;
		TextView itemAmount;
		TextView distQty;
		TextView distAmount;
		TextView granTotal;
		
		Button addonButton;
	}
}
