package com.android.emobilepos;


import com.android.support.Global;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ConsignEditFragment extends Fragment
{

	private ListView myListview; 
	private ListViewAdapter myAdapter;
	private int mapKey;
	private String[] lvQty;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.consignedit_fragment_layout, container, false);

		mapKey = getActivity().getIntent().getExtras().getInt("position");
		myListview = (ListView) view.findViewById(R.id.consignModifyListView);
		myAdapter = new ListViewAdapter(getActivity());
		myListview.setAdapter(myAdapter);
		
		myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
				Toast.makeText(getActivity(), "Item clicked", Toast.LENGTH_LONG).show();
			}
		});
		
		
		Button saveButton = (Button)view.findViewById(R.id.saveModifyButton);
		saveButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(lvQty[0]!=null&&!lvQty[0].isEmpty())
					Global.consignSummaryMap.get(Global.consignMapKey.get(mapKey)).put("rack", lvQty[0]);
				if(lvQty[1]!=null&&!lvQty[1].isEmpty())
					Global.consignSummaryMap.get(Global.consignMapKey.get(mapKey)).put("return", lvQty[1]);
				if(lvQty[2]!=null&&!lvQty[2].isEmpty())
					Global.consignSummaryMap.get(Global.consignMapKey.get(mapKey)).put("fillup", lvQty[2]);
				if(lvQty[3]!=null&&!lvQty[3].isEmpty())
					Global.consignSummaryMap.get(Global.consignMapKey.get(mapKey)).put("invoice", lvQty[3]);
				
				getActivity().setResult(1);
				getActivity().finish();
			}
		});
		return view;

	}

	/* if update information is needed on layout */
	/*@Override
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
						Toast.makeText(getActivity(), "Item clicked", Toast.LENGTH_LONG).show();
					}
				});
			}
		}
	}*/
	

	
	private class ListViewAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context context;
		private  String[] lvTitle;
		
		public ListViewAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			this.context = context;
			lvTitle = new String[]{getString(R.string.consignment_stacked),getString(R.string.consignment_returned), getString(R.string.consignment_filledup), 
					getString(R.string.consignment_issued)};
			lvQty = new String[4];
			initList();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.consignedit_lisview_adapter, null);

				holder = new ViewHolder();
				holder.title= (TextView) convertView.findViewById(R.id.consignEditTitle);
				holder.qty = (TextView)convertView.findViewById(R.id.consignEditQty);
				holder.add = (Button) convertView.findViewById(R.id.modifyAddButton);
				holder.remove = (Button)convertView.findViewById(R.id.modifyRemoveButton);
				
				holder.add.setFocusable(false);
				holder.remove.setFocusable(false);
				
				convertView.setTag(holder);

			} 
			else 
			{
				holder = (ViewHolder) convertView.getTag();
			}
			
	
			holder.title.setText(lvTitle[position]);
			holder.qty.setText(lvQty[position]);
			holder.add.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String temp = holder.qty.getText().toString();
					if(temp!=null&&!temp.isEmpty())
					{
						int value = Integer.parseInt(holder.qty.getText().toString());
						value+=1;
						lvQty[position] = Integer.toString(value);
						notifyDataSetChanged();
					}
				}
				
			});
			
			holder.remove.setOnClickListener(new View.OnClickListener() 
			{
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String temp = holder.qty.getText().toString();
					if(temp!=null&&!temp.isEmpty())
					{
						int value = Integer.parseInt(holder.qty.getText().toString());
						value-=1;
						if(value<0)
							value=0;
							
						lvQty[position] = Integer.toString(value);
						notifyDataSetChanged();
					}
				}
			});
			
			return convertView;
		}

		private void initList()
		{
			lvQty[0] = Global.consignSummaryMap.get(Global.consignMapKey.get(mapKey)).get("rack");
			lvQty[1] = Global.consignSummaryMap.get(Global.consignMapKey.get(mapKey)).get("return");
			lvQty[2] = Global.consignSummaryMap.get(Global.consignMapKey.get(mapKey)).get("fillup");
			lvQty[3] = Global.consignSummaryMap.get(Global.consignMapKey.get(mapKey)).get("invoice");
		}
		
		
		private String getString(int id)
		{
			return context.getResources().getString(id);
		}
		
		public class ViewHolder {
			TextView title,qty;
			Button remove,add;
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
