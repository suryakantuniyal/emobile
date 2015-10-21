package com.android.emobilepos.adapters;

import com.emobilepos.app.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class HistoryMenuAdapter extends BaseAdapter implements Filterable {
	private LayoutInflater mInflater;
	Context context;
	private  String[] lvTitle;//

	
	
	
	public HistoryMenuAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		this.context = context;
		lvTitle = new String[]{getString(R.string.hist_transac), getString(R.string.hist_payments), 
				getString(R.string.hist_refunds),getString(R.string.pay_tab_giftcard),
				getString(R.string.loyalty),getString(R.string.rewards), 
				getString(R.string.hist_open_inv),getString(R.string.consignment),getString(R.string.inventory_transfer)};
	}

	
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.history_listviewadapter, null);

			holder = new ViewHolder();
			holder.textLine = (TextView) convertView.findViewById(R.id.historylvTitle);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		int gradientId = context.getResources().getIdentifier("blue_btn_selector", "drawable", context.getString(R.string.pkg_name));

		holder.textLine.setText(lvTitle[position]);
		convertView.setBackgroundResource(gradientId);

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
	public Filter getFilter() {
		// TODO Auto-generated method stub
		return null;
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
