package com.android.emobilepos.history.details;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.InvProdHandler;
import com.android.database.InvoicesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.payment.SelectPayMethod_FA;
import com.android.support.Global;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;
import java.util.List;

public class HistoryOpenInvoicesDetails_FA extends FragmentActivity 
{
	private ListView myListView;
	private CustomAdapter myAdapter;
	private String[] leftInfo;
	private String[] rightInfo = new String[]{};
	private List<String[]> productInfo = new ArrayList<String[]>();
	
	private ImageLoader imageLoader;
	private ImageLoaderConfiguration config;
	private DisplayImageOptions options;
	private Context context;
	private String invID,totalCostAmount,balanceDue;
	private Activity activity;
	private boolean hasBeenCreated = false;
	private Global global;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.hist_inv_details_layout);
		myListView = (ListView) findViewById(R.id.invoiceDetailsLV);
		Button payButton = (Button)findViewById(R.id.invPaymentButton);
		Bundle extras = this.getIntent().getExtras();
		context = this;
		activity = this;
		global = (Global)getApplication();
		
		
		
		
		
		leftInfo = new String[]{getAString(R.string.inv_details_name),getAString(R.string.inv_details_uid),getAString(R.string.inv_details_txnid),
				getAString(R.string.inv_details_total),getAString(R.string.inv_details_balance),getAString(R.string.inv_details_createdate),
				getAString(R.string.inv_details_duedate),getAString(R.string.inv_details_shipdate),getAString(R.string.inv_details_paid)};
		
		
			
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder().cacheOnDisc(true).showImageForEmptyUri(R.drawable.no_image)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).build();
		config = new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(options).threadPoolSize(2)
				.threadPriority(Thread.NORM_PRIORITY - 1).memoryCache(new WeakMemoryCache()).build();
		imageLoader.init(config);
		
		invID = extras.getString("uid");
		
		InvoicesHandler invHandler = new InvoicesHandler(this);
		rightInfo = invHandler.getSpecificInvoice(invID);
		
		InvProdHandler invProdHandler = new InvProdHandler(this);
		productInfo = invProdHandler.getInvProd(invID);
		
		if(rightInfo[8].equals("Yes"))
			payButton.setVisibility(View.INVISIBLE);
		
		
		payButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Toast.makeText(context, "button pressed", Toast.LENGTH_SHORT).show();
				
				
				Intent intent = new Intent(context,SelectPayMethod_FA.class);
				intent.putExtra("histinvoices", true);
				
				totalCostAmount = Double.toString(Global.formatNumWithCurrFromLocale(rightInfo[3]));
				balanceDue = Double.toString(Global.formatNumWithCurrFromLocale(rightInfo[4]));
				intent.putExtra("inv_id", invID);
				intent.putExtra("amount", balanceDue);
				intent.putExtra("paid", Double.toString(Global.formatNumFromLocale(Global.addSubsStrings(false, 
						Global.formatNumToLocale(Double.parseDouble(totalCostAmount)), Global.formatNumToLocale(Double.parseDouble(balanceDue))))));
				startActivityForResult(intent,Global.FROM_OPEN_INVOICES);
			}
		});
		
		
		myAdapter = new CustomAdapter(this);
		myListView.setAdapter(myAdapter);
	
		hasBeenCreated = true;
	}
	
	@Override
	public void onResume() {

		if(global.isApplicationSentToBackground(activity))
			global.loggedIn = false;
		global.stopActivityTransitionTimer();
		
		if(hasBeenCreated&&!global.loggedIn)
		{
			if(global.getGlobalDlog()!=null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(activity);
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
	
	
	private String getAString(int id)
	{
		Resources resource = activity.getResources();
		return resource.getString(id);
	}
	
	
	
	
	
	private class CustomAdapter extends BaseAdapter
	{

		private LayoutInflater inflater;
		
		public CustomAdapter(Activity activity)
		{
			inflater = LayoutInflater.from(activity);
			
		}
		@Override
		public int getViewTypeCount() {
			return 4;
		}

		@Override
		public int getItemViewType(int position) 
		{
			if(position == 0)									//info divider
				return 0;
			else if(position < leftInfo.length+1)				//info content
				return 1;
			else if(position == (leftInfo.length+1))			//items divider
				return 2;
			
			return 3;											//items content
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return leftInfo.length+productInfo.size()+2;		//+2 for the two dividers
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			
			ViewHolder holder;
			int type = getItemViewType(position);
			
			if(convertView == null)
			{
				holder = new ViewHolder();
				switch(type)
				{
				case 0:
					convertView = inflater.inflate(R.layout.hist_invoices_lvdivider, null);
					holder.leftText = (TextView) convertView.findViewById(R.id.invoiceDivider);
					break;
				case 1:
					convertView = inflater.inflate(R.layout.orddetails_lvinfo_adapter, null);
					holder.leftText = (TextView) convertView.findViewById(R.id.ordInfoLeft);
					holder.rightText = (TextView) convertView.findViewById(R.id.ordInfoRight);
					break;
				case 2:
					convertView = inflater.inflate(R.layout.hist_invoices_lvdivider, null);
					holder.leftText = (TextView) convertView.findViewById(R.id.invoiceDivider);
					break;
				case 3:
					convertView = inflater.inflate(R.layout.catalog_listview_adapter, null);
					
					holder.title = (TextView) convertView.findViewById(R.id.catalogItemName);
					holder.qty = (TextView) convertView.findViewById(R.id.catalogItemQty);
					holder.amount = (TextView) convertView.findViewById(R.id.catalogItemPrice);
					holder.detail = (TextView) convertView.findViewById(R.id.catalogItemInfo);

					holder.iconImage = (ImageView) convertView.findViewById(R.id.catalogRightIcon);
					holder.itemImage = (ImageView) convertView.findViewById(R.id.catalogItemPic);
					break;
				}
				setHolderValues(type,position,holder);
								
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
				
				setHolderValues(type,position,holder);
			}
			return convertView;
		}
		
		private class ViewHolder
		{
			TextView leftText,rightText;
			
			TextView title,qty,amount,detail;
			ImageView iconImage,itemImage;
			
			
			
		}
		
		private void setHolderValues(int type,int position,ViewHolder holder)
		{
			int pos = 0;
			switch(type)
			{
			case 0:																	//Shipping
				holder.leftText.setText("Info");
				break;
			case 1:																	//Terms
				pos = position - 1;
				holder.leftText.setText(leftInfo[pos]);
				holder.rightText.setText(rightInfo[pos]);
				break;
			case 2:																	//Delivery
				holder.leftText.setText("Items");
				break;
			case 3:																	//Address
				
				pos = position-leftInfo.length-2;
				
				holder.title.setText(productInfo.get(pos)[0]);
				holder.detail.setText(productInfo.get(pos)[1]);
				holder.qty.setText(productInfo.get(pos)[2]);
				holder.amount.setText(Global.getCurrencyFormat(productInfo.get(pos)[3]));
				
				holder.iconImage.setVisibility(View.INVISIBLE);
				imageLoader.displayImage(validateNullVal(productInfo.get(pos)[4]), holder.itemImage, options);
				
				break;

				
			}
		}
		
		private String validateNullVal(String val)
		{
			String tempValue = "";
			if(val != null)
				tempValue = val;
			
			return tempValue;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode==Global.FROM_PAYMENT||resultCode==3)
		{
			InvoicesHandler invHandler = new InvoicesHandler(activity);
			double remainingBalance = Double.parseDouble(Global.addSubsStrings(false, balanceDue, Global.amountPaid));
			
			if(remainingBalance<=0)
			{
				//has been paid in total
				invHandler.updateIsPaid(true, invID, null);
				
				
			}
			else
			{
				//hasn't been paid in total
				invHandler.updateIsPaid(false, invID, Double.toString(remainingBalance));
			}
			myListView.invalidateViews();
			myAdapter.notifyDataSetChanged();
			setResult(Global.FROM_OPEN_INVOICES_DETAILS);
			activity.finish();
			
			
		}

	}
}
