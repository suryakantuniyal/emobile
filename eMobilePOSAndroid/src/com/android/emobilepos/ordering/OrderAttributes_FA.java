package com.android.emobilepos.ordering;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.database.OrdProdAttrList_DB;
import com.android.support.Global;
import com.emobilepos.app.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class OrderAttributes_FA extends FragmentActivity implements OnItemClickListener{
		
	
	private ArrayList<OrdProdAttrHolder>listAttr;
	private boolean hasBeenCreated = false;
	private Global global;
	private boolean isModify = false;
	private String ordprod_id;
	public static int REQUEST_REQ_ATTR = 100;
	private HashMap<String,String> savedAttr = new HashMap<String,String>();
	private OrdProdAttrListAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
//		MyPreferences myPref = new MyPreferences(this);
//		if(!myPref.getIsTablet())						//reset to default layout (not as dialog)
//			super.setTheme(R.style.AppTheme);
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.order_attributes_layout);
		
		global = (Global)getApplication();
		Bundle extras = getIntent().getExtras();
		String prodID = extras.getString("prod_id");
		isModify = extras.getBoolean("isModify",false);
		ordprod_id = extras.getString("ordprod_id","");
		OrdProdAttrList_DB ordProdAttrList = new OrdProdAttrList_DB(this);
		listAttr = ordProdAttrList.getRequiredOrdAttrList(prodID);
		mAdapter = new OrdProdAttrListAdapter(this,listAttr);
		
		ListView listView = (ListView)findViewById(R.id.listView);
		listView.setAdapter(mAdapter);
		
		listView.setOnItemClickListener(this);
		loadSavedAttributes();
		hasBeenCreated = true;
	}
	
	
	
	@Override
	public void onResume() {

		if(global.isApplicationSentToBackground(this))
			global.loggedIn = false;
		global.stopActivityTransitionTimer();
		
		if(hasBeenCreated&&!global.loggedIn)
		{
			if(global.getGlobalDlog()!=null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(this);
		}
		super.onResume();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
		boolean isScreenOn = powerManager.isScreenOn();
		if(!isScreenOn)
			global.loggedIn = false;
		global.startActivityTransitionTimer();
	}
	
	private void loadSavedAttributes()
	{
		if(global.ordProdAttr!=null)
		{
			int size = global.ordProdAttr.size();
		for(int i = 0 ; i < size;i++)
		{
			if(global.ordProdAttr.get(i).ordprod_id.equals(ordprod_id))
			{
				//attr_value = ;
				savedAttr.put(global.ordProdAttr.get(i).Attrid, global.ordProdAttr.get(i).value);
				break;
			}
		}
		}
	}
	
	private class OrdProdAttrListAdapter extends ArrayAdapter<OrdProdAttrHolder> {
		ViewHolder holder;
		final int TYPE_REQUIRED = 0;
		int viewType;

		public OrdProdAttrListAdapter(Context context, ArrayList<OrdProdAttrHolder> users) {
			super(context, R.layout.lv_adapter_two_column, users);
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			if (listAttr.get(position).required)
				return TYPE_REQUIRED;
			return 1;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			// Get the data item for this position

			viewType = getItemViewType(position);
			// Check if an existing view is being reused, otherwise inflate the
			// view
			if (v == null) {
				holder = new ViewHolder();
				v = LayoutInflater.from(getContext()).inflate(R.layout.lv_adapter_two_column, null);
				holder.leftText = (TextView) v.findViewById(R.id.leftColumn);
				holder.rightText = (TextView) v.findViewById(R.id.rightColumn);
				v.setTag(holder);
			} else
				holder = (ViewHolder) v.getTag();

			//holder.rightText.setVisibility(View.INVISIBLE);
			if (viewType == TYPE_REQUIRED) {
				holder.leftText.setText(listAttr.get(position).ordprod_attr_name);
				holder.leftText.setTextColor(Color.RED);
			} else {
				holder.leftText.setText(listAttr.get(position).ordprod_attr_name);
			}
			if(savedAttr.containsKey(listAttr.get(position).Attrid))
			{
				String val = savedAttr.get(listAttr.get(position).Attrid);
				holder.rightText.setText(val);
			}


			return v;
		}

		private class ViewHolder {
			TextView leftText, rightText;
		}
	}




	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this,OrderAttrEdit_FA.class);
		
		intent.putExtra("ordprodattr_id", listAttr.get(pos).ordprodattr_id);
		intent.putExtra("attr_id", listAttr.get(pos).Attrid);
		intent.putExtra("attr_name", listAttr.get(pos).ordprod_attr_name);
		intent.putExtra("isModify", isModify);
		intent.putExtra("ordprod_id", ordprod_id);
		
		if (listAttr.get(pos).required)
		{
			intent.putExtra("required", true);
			startActivityForResult(intent,REQUEST_REQ_ATTR);
		}
		else
			startActivity(intent);
		
	}
	
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode==REQUEST_REQ_ATTR)
		{
			if(resultCode==0&&data!=null)	//success
			{
				Bundle extras = data.getExtras();
				global.ordProdAttrPending.remove(extras.get("ordprodattr_id"));
			}
			loadSavedAttributes();
			mAdapter.notifyDataSetChanged();
		}
		
	}

	
}
