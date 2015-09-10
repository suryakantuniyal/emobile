package com.android.emobilepos;



import com.android.emobilepos.ordering.OrderingMain_FA;
import com.android.support.Global;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ConsignmentMainFragment extends Fragment
{
	private ListView myListview;
	private ConsignmentLVAdapter myAdapter;
	private Global global;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.consignment_main_fragment, container, false);

		global = (Global)getActivity().getApplication();
		myListview = (ListView) view.findViewById(R.id.consignmentListView);
		myAdapter = new ConsignmentLVAdapter(getActivity());
		return view;

	}

	/* if update information is needed on layout */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();

		if (activity != null) {
			if (myListview != null) {
				myListview.setAdapter(myAdapter);
				myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
						// TODO Auto-generated method stub

//						if (position == 0)//Transactions - (Add/Scan -> 0. Rack, 1. Return, 2. Fill-up) 
//						{
//							global.resetOrderDetailsValues();
//							global.clearListViewData();
//							Intent intent = new Intent(arg0.getContext(),OrderingMain_FA.class);
//							//Intent intent = new Intent(arg0.getContext(), SalesReceiptSplitActivity.class);
//							intent.putExtra("option_number", 9);
//							intent.putExtra("consignmentType", 0);
//							startActivity(intent);
//							
//							
//							
//						} 
//						else if (position == 1) //Pick-up - Add/Scan products to be returned
//						{
//							global.resetOrderDetailsValues();
//							global.clearListViewData();
//							Intent intent = new Intent(arg0.getContext(),OrderingMain_FA.class);
//							//Intent intent = new Intent(arg0.getContext(), SalesReceiptSplitActivity.class);
//							intent.putExtra("option_number", 9);
//							intent.putExtra("consignmentType", 3);
//							startActivity(intent);
//						}
//						else if(position == 2)
//						{
//							Intent intent = new Intent(arg0.getContext(),ConsignmentHistFragmentActivity.class);
//							startActivity(intent);
//						}
					}
				});
			}
		}
	}
	
	

	
	private class ConsignmentLVAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context context;
		private  String[] lvTitle;

		
		public ConsignmentLVAdapter(Context context) 
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

			int gradientId = context.getResources().getIdentifier("blue_button_selector", "drawable", context.getString(R.string.pkg_name));

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
