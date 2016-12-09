package com.android.emobilepos.adapters;


import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.database.ShiftPeriodsDBHandler;
import com.android.emobilepos.R;
import com.android.support.Global;


public class ReportsShiftAdapter extends BaseAdapter implements Filterable {

	private LayoutInflater mInflater;
	private String []curDate;

	
	private int offset = 2; 					//will include 2 dividers,the report date, and the total.
	private String reportDate;
	private Cursor myCursor;
	private int listViewSize; 
	private Activity activity;
	private Resources resource;
	private String temp;
	

	public ReportsShiftAdapter(Activity activity, String[] date) {
		if(activity!=null)
		{
			mInflater = LayoutInflater.from(activity.getApplicationContext());
			this.curDate = date;
			resource = activity.getResources();
			this.activity = activity;
			reportDate = resource.getString(R.string.report_but_title)+"\n\n"+curDate[0];
			ShiftPeriodsDBHandler handler = new ShiftPeriodsDBHandler(activity);
			
			myCursor =handler.getAllShiftsReport(curDate[1]); 
			listViewSize = myCursor.getCount();
		}
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

		return listViewSize+offset;
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
		int type = getItemViewType(position);

		if (convertView == null) {

			holder = new ViewHolder();
			
			switch(type)
			{
			case 0:
			{
				convertView = mInflater.inflate(R.layout.report_listviewheader, null);
				holder.top = (TextView)convertView.findViewById(R.id.reportHeader);
				
				setHolderValues(type,position,holder);
				break;
			}
			case 1:		//transaction divider
			{
				convertView = mInflater.inflate(R.layout.report_listviewdivider, null);
				holder.top = (TextView)convertView.findViewById(R.id.reportTitle);
				
				setHolderValues(type,position,holder);
				break;
			}
			case 2:				//transaction content
			{
				convertView = mInflater.inflate(R.layout.report_shift_lv_adapter, null);
				
				myCursor.moveToPosition(position-offset);
				holder.i_startTime = myCursor.getColumnIndex("startTime");
				holder.i_end_type = myCursor.getColumnIndex("end_type");
				holder.i_assignee_name = myCursor.getColumnIndex("assignee_name");
				holder.i_beginning_petty_cash = myCursor.getColumnIndex("beginning_petty_cash");
				holder.top = (TextView)convertView.findViewById(R.id.shiftPeriod);
				holder.bottom = (TextView)convertView.findViewById(R.id.clerkName);
				
				setHolderValues(type,position,holder);
				break;
			}

			}
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
			setHolderValues(type,position,holder);
		}
		return convertView;
	}

	private void setHolderValues(int type,int position,ViewHolder holder)
	{
		switch(type)
		{
		case 0:
		{
			holder.top.setText(reportDate);
			break;
		}
		case 1:
		{
			holder.top.setText(R.string.report_per_shift);
			break;
		}
		case 2:
		{
			myCursor.moveToPosition(position-offset);
			temp = myCursor.getString(holder.i_end_type);
			if(!temp.equals("Open"))
				temp = Global.formatToDisplayDate(temp,  3);
			holder.top.setText(Global.formatToDisplayDate(myCursor.getString(holder.i_startTime),  3)+" - "+temp);
			holder.bottom.setText(myCursor.getString(holder.i_assignee_name)+" - "+
			Global.formatDoubleStrToCurrency(myCursor.getString(holder.i_beginning_petty_cash)));
			break;
		}

		}
	}
	public class ViewHolder {
		TextView top,bottom;
		int i_startTime,i_end_type,i_assignee_name,i_beginning_petty_cash;

	}
	
	public String getShiftID(int position)
	{
		myCursor.moveToPosition(position-offset);
		return myCursor.getString(myCursor.getColumnIndex("shift_id"));
	}

	@Override
	public int getItemViewType(int position) {
		
		if(position==0)													//report date
			return 0;
		else if(position == 1) 											//transaction divider
			return 1;
		/*else if(position>=2&&position<listViewSize+2)			//display all transactions
			return 2;
		
		else 						//show gran total
			return 3;*/
		else
			return 2;
		
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		return null;
	}
}
