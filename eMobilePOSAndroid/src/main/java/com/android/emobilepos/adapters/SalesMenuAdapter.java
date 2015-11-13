package com.android.emobilepos.adapters;

import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.support.MyPreferences;

import java.util.ArrayList;
import java.util.List;

public class SalesMenuAdapter extends BaseAdapter implements Filterable {
	private LayoutInflater mInflater;
	private Activity activity;
	
	private int listViewSize = 0;
	 
	private boolean[] enabledSettings;
	private List<Integer> indexOfEnabled = new ArrayList<Integer>();
	private String[] mainMenuList;
	private  SparseArray<String>mainMenuIconsMap;
	

	public SalesMenuAdapter(Activity activity, boolean custSelected) {
		mInflater = LayoutInflater.from(activity);
		this.activity = activity;
		

		mainMenuList = activity.getResources().getStringArray(R.array.mainMenuArray);
		
		mainMenuIconsMap = new SparseArray<String>();
		mainMenuIconsMap.put(0, "list");	//Sales Receipt
		mainMenuIconsMap.put(1, "order");	//Order
		mainMenuIconsMap.put(2, "return_icon");	//Return
		mainMenuIconsMap.put(3, "invoice");	//Invoice
		mainMenuIconsMap.put(4, "estimate");	//Estimate
		mainMenuIconsMap.put(5, "payment");	//Payment
		mainMenuIconsMap.put(6, "gift_card");	//Gift Card
		mainMenuIconsMap.put(7, "loyalty_card");	//Loyalty Card
		mainMenuIconsMap.put(8, "gift_card");	//Reward Card
		mainMenuIconsMap.put(9, "return_icon");	//Refund
		mainMenuIconsMap.put(10, "routes");	//Route
		mainMenuIconsMap.put(11, "list_red");	//On Hold
		mainMenuIconsMap.put(12, "list");	//Consignment
		mainMenuIconsMap.put(13, "list"); //Transfer Inventory
		
		
		
		MyPreferences myPref = new MyPreferences(activity);
		boolean[] temp = myPref.getMainMenuPreference();
		
		if(custSelected)
		{
			enabledSettings = temp;
		}
		else
		{
			enabledSettings = new boolean[]{temp[0],false,temp[2],false,false,temp[5],temp[6],temp[7],temp[8],temp[9],false,temp[11],false,temp[13]};
		}
		
		int size = mainMenuList.length;
		for(int i = 0 ; i < size; i++)
		{
			if(enabledSettings[i])
			{
				indexOfEnabled.add(i);
				listViewSize++;
			}
		}
	}

	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.main_menu_listviewadapter, null);

			holder = new ViewHolder();
			holder.textLine = (TextView) convertView.findViewById(R.id.salesText);
			holder.iconLine = (ImageView) convertView.findViewById(R.id.salesIcon);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		int gradientId = R.drawable.blue_btn_selector;
		String resourceName = mainMenuIconsMap.get(indexOfEnabled.get(position));
		int iconId = activity.getResources().getIdentifier(resourceName, "drawable", activity.getPackageName());
		holder.iconLine.setImageResource(iconId);
		
		holder.textLine.setText(mainMenuList[indexOfEnabled.get(position)]);
		
		convertView.setBackgroundResource(gradientId);

		return convertView;
	}

	
	public class ViewHolder 
	{
		TextView textLine;
		ImageView iconLine;
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
		
		return listViewSize;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return indexOfEnabled.get(position);
	}

}