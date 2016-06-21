package com.android.emobilepos.ordering;

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

import com.android.dao.OrderProductAttributeDAO;
import com.android.database.OrdProdAttrList_DB;
import com.android.emobilepos.R;
import com.android.emobilepos.models.ProductAttribute;
import com.android.support.Global;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class OrderAttributes_FA extends BaseFragmentActivityActionBar implements OnItemClickListener{
		
	
	private List<ProductAttribute> listAttr;
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
		setContentView(R.layout.order_attributes_layout);
		
		global = (Global)getApplication();
		Bundle extras = getIntent().getExtras();
		String prodID = extras.getString("prod_id");
		isModify = extras.getBoolean("isModify",false);
		ordprod_id = extras.getString("ordprod_id","");
		OrdProdAttrList_DB ordProdAttrList = new OrdProdAttrList_DB(this);
		listAttr = OrderProductAttributeDAO.getByProdId(prodID);
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
			if(String.valueOf(global.ordProdAttr.get(i).getProductId()).equals(ordprod_id))
			{
				//attr_value = ;
				savedAttr.put(global.ordProdAttr.get(i).getAttributeId(), global.ordProdAttr.get(i).getValue());
				break;
			}
		}
		}
	}
	
	private class OrdProdAttrListAdapter extends ArrayAdapter<ProductAttribute> {
		ViewHolder holder;
		final int TYPE_REQUIRED = 0;
		int viewType;

		public OrdProdAttrListAdapter(Context context, List<ProductAttribute> users) {
			super(context, R.layout.lv_adapter_two_column, users);
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			if (listAttr.get(position).isRequired())
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
				holder.leftText.setText(listAttr.get(position).getAttributeName());
				holder.leftText.setTextColor(Color.RED);
			} else {
				holder.leftText.setText(listAttr.get(position).getAttributeName());
			}
			if(savedAttr.containsKey(listAttr.get(position).getAttributeId()))
			{
				String val = savedAttr.get(listAttr.get(position).getAttributeId());
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
		
		intent.putExtra("ordprodattr_id", listAttr.get(pos).getId());
		intent.putExtra("attr_id", listAttr.get(pos).getAttributeId());
		intent.putExtra("attr_name", listAttr.get(pos).getAttributeName());
		intent.putExtra("isModify", isModify);
		intent.putExtra("ordprod_id", ordprod_id);
		
		if (listAttr.get(pos).isRequired())
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
                int id = extras.getInt("ordprodattr_id");
				global.ordProdAttrPending.remove(OrderProductAttributeDAO.getById(id));
			}
			loadSavedAttributes();
			mAdapter.notifyDataSetChanged();
		}
		
	}

	
}
