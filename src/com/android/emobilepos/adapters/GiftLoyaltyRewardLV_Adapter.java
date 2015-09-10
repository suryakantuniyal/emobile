package com.android.emobilepos.adapters;

import com.emobilepos.app.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GiftLoyaltyRewardLV_Adapter extends BaseAdapter{

	private ViewHolder holder;
	private LayoutInflater inflater;
	private String[] dataArray;
	private Activity activity;
	public GiftLoyaltyRewardLV_Adapter(Activity _activity,int type)
	{
		activity = _activity;
		inflater = LayoutInflater.from(activity);
		if(type == 0)
			dataArray = new String[]{getString(R.string.activate),getString(R.string.add_balance),
				getString(R.string.balance_inquiry),getString(R.string.manually_add_balance)};
		else
			dataArray = new String[]{getString(R.string.activate),
				getString(R.string.balance_inquiry),getString(R.string.manually_add_balance)};
	}
	
	private String getString(int id)
	{
		return activity.getString(id);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return dataArray.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int pos, View view, ViewGroup arg2) {
		// TODO Auto-generated method stub
		if(view==null)
		{
			view = inflater.inflate(R.layout.adapter_two_column_with_icon, null);
			holder = new ViewHolder();
			holder.label= (TextView)view.findViewById(R.id.twoColumnRightText);
			holder.icon = (ImageView)view.findViewById(R.id.twoColumnLeftIcon);
			
			view.setTag(holder);
		}
		else
		{
			holder = (ViewHolder)view.getTag();
		}
		
		holder.label.setText(dataArray[pos]);
		return view;
	}
	
	private class ViewHolder
	{
		TextView label;
		ImageView icon;
	}

}
