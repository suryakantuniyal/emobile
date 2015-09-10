package com.android.emobilepos.adapters;


import android.app.Activity;
import android.content.res.Resources;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.database.ShiftPeriodsDBHandler;
import com.emobilepos.app.R;


public class ShiftDetailsAdapter extends BaseAdapter
{

	private LayoutInflater mInflater;
	
	private Resources resource;
	private String[] title;	
	private SparseArray<String>map;
	

	public ShiftDetailsAdapter(Activity activity,String shiftID) {
		if(activity!=null)
		{
			
			ShiftPeriodsDBHandler handler = new ShiftPeriodsDBHandler(activity);
			
			map = handler.getShiftDetails(shiftID);
			
			resource = activity.getResources();
			title = new String[]{getString(R.string.sales_clerk),getString(R.string.begging_petty_cash),
					getString(R.string.total_expenses),getString(R.string.ending_petty_cash),
					getString(R.string.total_transactions_cash),getString(R.string.total_ending_cash),
					getString(R.string.entered_close_amount)};
			mInflater = LayoutInflater.from(activity.getApplicationContext());
			
			
		}
	}
	
	
	private String getString(int id)
	{
		return resource.getString(id);
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

		return title.length;
	}

	@Override
	public Object getItem(int index) {
		// TODO Auto-generated method stub
		return null;
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
		

		if (convertView == null) {

			holder = new ViewHolder();
			
			
			convertView = mInflater.inflate(R.layout.report_shift_lv_adapter,null);

			holder.top = (TextView) convertView.findViewById(R.id.shiftPeriod);
			holder.bottom = (TextView) convertView.findViewById(R.id.clerkName);

			setHolderValues(position, holder);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
			setHolderValues(position,holder);
		}
		return convertView;
	}

	private void setHolderValues(int position,ViewHolder holder)
	{
	
			holder.top.setText(title[position]);
			holder.bottom.setText(map.get(position));
	}
	public class ViewHolder {
		TextView top,bottom;

	}

}
