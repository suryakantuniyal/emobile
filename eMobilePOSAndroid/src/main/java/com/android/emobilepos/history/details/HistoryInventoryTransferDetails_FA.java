package com.android.emobilepos.history.details;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.database.Locations_DB;
import com.android.database.ProductsImagesHandler;
import com.android.database.TransferInventory_DB;
import com.android.emobilepos.R;
import com.android.emobilepos.holders.Locations_Holder;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HistoryInventoryTransferDetails_FA extends BaseFragmentActivityActionBar {

	private boolean hasBeenCreated = false;
	private Global global;
	private ListViewAdapter myAdapter;
	private final int CASE_TRANS_ID = 0;
	private final int CASE_LOCATION_FROM = 1;
	private final int CASE_LOCATION_TO = 2;
	private final int CASE_DATE = 3;
	private static List<String> allInfoLeft;
	private ListView myListView;
	private List<HashMap<String,String>> listProducts = new ArrayList<HashMap<String,String>>();
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	private String trans_id,trans_date;
	private Activity activity;
	private MyPreferences myPref;
	private Locations_Holder locationFrom,locationTo;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_detailslv_layout);
		global = (Global)getApplication();
		activity = this;
		myPref = new MyPreferences(activity);
		Button btnPrint = (Button) findViewById(R.id.printButton);
		Button btnVoid = (Button) findViewById(R.id.btnVoid);
		btnPrint.setVisibility(View.GONE);
		btnVoid.setVisibility(View.GONE);
		myListView = (ListView) findViewById(R.id.orderDetailsLV);
		TextView headerTitle = (TextView) findViewById(R.id.ordDetailsHeaderTitle);
		headerTitle.setText(getString(R.string.inventory_transfer_details));
		allInfoLeft = Arrays.asList(getString(R.string.trans_details_transfer_id),getString(R.string.trans_details_origin),
				getString(R.string.trans_details_destination),getString(R.string.trans_details_date));
		final Bundle extras = activity.getIntent().getExtras();
		trans_id = extras.getString("transfer_id");
		trans_date = extras.getString("trans_date");
		TransferInventory_DB dbHandler = new TransferInventory_DB();
		Locations_DB dbLocations = new Locations_DB();
		listProducts = dbHandler.getInventoryTransactionMap(trans_id);
		locationFrom = dbLocations.getLocationInfo(extras.getString("loc_key_from"));
		locationTo = dbLocations.getLocationInfo(extras.getString("loc_key_to"));
		myPref = new MyPreferences(activity);
		myAdapter = new ListViewAdapter(activity);
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading_image).cacheInMemory(true).cacheOnDisc(true)
				.showImageForEmptyUri(R.drawable.no_image).build();
		myListView.setAdapter(myAdapter);
		hasBeenCreated = true;
	}
	
	@Override
	public void onResume() {
		if(global.isApplicationSentToBackground())
			Global.loggedIn = false;
		global.stopActivityTransitionTimer();
		
		if(hasBeenCreated&&!Global.loggedIn)
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
//		PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
//		boolean isScreenOn = powerManager.isScreenOn();
//		if(!isScreenOn && myPref.isExpireUserSession())
//			Global.loggedIn = false;
		global.startActivityTransitionTimer();
	}
		

	private String getCaseData(int type) {
		String data = "";
		switch (type) 
		{
			case CASE_TRANS_ID:
				data = trans_id;
				break;
			case CASE_LOCATION_FROM:
				data = locationFrom.getLoc_name();
				break;
			case CASE_LOCATION_TO:
				data = locationTo.getLoc_name();
				break;
			case CASE_DATE:
				data = Global.formatToDisplayDate(trans_date,  0);
				break;
		}
		return data;
	}
	

	
	
	
	
	
	
	public class ListViewAdapter extends BaseAdapter implements Filterable {
		private LayoutInflater myInflater;
		private ProductsImagesHandler imgHandler;

		public ListViewAdapter(Context context) {
			imgHandler = new ProductsImagesHandler(activity);
			myInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return (allInfoLeft.size() + listProducts.size() + 2); // 2 is to
																	// include
																	// the
																	// dividers
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			int type = getItemViewType(position);
			if (convertView == null) {
				holder = new ViewHolder();

				switch (type) {
				case 0: // divider
				{
					convertView = myInflater.inflate(R.layout.orddetails_lvdivider_adapter, null);
					holder.textLine1 = (TextView) convertView.findViewById(R.id.orderDivLeft);
					holder.textLine2 = (TextView) convertView.findViewById(R.id.orderDivRight);
					break;
				}
				case 1: // content in info divider
				{
					convertView = myInflater.inflate(R.layout.orddetails_lvinfo_adapter, null);

					holder.textLine1 = (TextView) convertView.findViewById(R.id.ordInfoLeft);
					holder.textLine2 = (TextView) convertView.findViewById(R.id.ordInfoRight);
					break;
				}
				case 2: {
					convertView = myInflater.inflate(R.layout.orddetails_lvproducts_adapter, null);

					holder.textLine1 = (TextView) convertView.findViewById(R.id.ordProdTitle);
					holder.textLine2 = (TextView) convertView.findViewById(R.id.ordProdSubtitle);
					holder.ordProdPrice = (TextView) convertView.findViewById(R.id.ordProdPrice);
					holder.ordProdQty = (TextView) convertView.findViewById(R.id.ordProdQty);
					holder.iconImage = (ImageView) convertView.findViewById(R.id.prodIcon);
					break;
				}
				}
				convertView.setTag(holder);
			}

			else {
				holder = (ViewHolder) convertView.getTag();
			}

			switch(type)
			{
			case 0: // divider
			{
				if (type == 0) {
					if (position == 0)
						holder.textLine1.setText("Info");
					else if (position == allInfoLeft.size() + 1)
						holder.textLine1.setText("Products");
				}
				break;
			}
			case 1: // content in info divider
			{
				holder.textLine1.setText(allInfoLeft.get(position - 1));
				holder.textLine2.setText(getCaseData((position - 1)));
				break;
			}
			case 2: {
				int ind = position - allInfoLeft.size() - 2;

				holder.textLine1.setText(listProducts.get(ind).get("prod_name"));
				holder.ordProdQty.setText(listProducts.get(ind).get("prod_qty") + " x");
				imageLoader.displayImage(imgHandler.getSpecificLink("I", listProducts.get(ind).get("prod_id")), holder.iconImage, options);

				break;
			}
			}

			return convertView;
		}

		@Override
		public Filter getFilter() {
			return null;
		}

		public class ViewHolder {
			TextView textLine1;
			TextView textLine2;
			TextView ordProdQty;
			TextView ordProdPrice;
			ImageView iconImage;
			ImageView moreDetails;

		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0 || (position == (allInfoLeft.size() + 1))) // divider
			// info //items //payments //map
			{
				return 0;
			} else if (position > 0 && position <= allInfoLeft.size()) // info
																		// content
			{
				return 1;
			}
			return 2;

		}

		@Override
		public int getViewTypeCount() {
			return 3;
		}
	}
	
}
