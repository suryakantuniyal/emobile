package com.android.catalog.fragments;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.android.database.CustomersHandler;
import com.android.database.OrdersHandler;
import com.android.database.ProductsHandler;
import com.android.database.ProductsImagesHandler;
import com.android.emobilepos.CatalogPickerFragActivity;

import com.android.emobilepos.CatalogProdDetailsActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.ShowProductImageActivity2;
import com.android.emobilepos.CustomerSelectionMenuActivity.CustomCursorAdapter;
import com.android.support.GenerateOrdID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.OrderProducts;
import com.android.support.Orders;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CatalogFragOne extends Fragment {

	// private ListViewAdapter adap;
	private ImageLoader imageLoader;
	private ImageLoaderConfiguration config;
	private DisplayImageOptions options;
	private Cursor myCursor;
	private String empStr = "";
	ListView lView;

	private CustomCursorAdapter adap2;
	// private Context thisContext;

	private List<String> allTitles = new ArrayList<String>();
	private List<String> allDetails = new ArrayList<String>();
	private List<String> allAmounts = new ArrayList<String>();
	private List<String> allQty = new ArrayList<String>();
	private List<String> allProdID = new ArrayList<String>();
	private List<String> allLinks = new ArrayList<String>();
	/*
	 * private List<String> allLinks = Arrays.asList(new
	 * String[]{"http://24.media.tumblr.com/tumblr_m6pfael3AW1r7jdcjo1_500.jpg",
	 * "http://operationhandhug.com/wp-content/uploads/2012/07/electric-zoo-Chor-Boogie.jpeg"
	 * ,
	 * "http://www.hdwallpapersarena.com/wp-content/uploads/2012/07/HD-wallpapers-desktop-car-background.jpg"
	 * });
	 */
	// private HashMap<String,String> imgLinks = new HashMap<String,String>();

	private List<String> filteredTitles;
	private List<String> filteredAmounts;
	private List<String> filteredQty;
	private List<String> filteredDetails;
	private List<String> filteredLinks;

	private Global global;

	private MyPreferences myPref;
	private ProductsHandler prodHandler;
	private ImageView clearData, searchButton;
	private ProductsImagesHandler imgHandler;
	private String empstr = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.catalog_listview_layout, container, false);
		lView = (ListView) view.findViewById(R.id.catalogListview);
		clearData = (ImageView) view.findViewById(R.id.clearSearchField);
		searchButton = (ImageView) view.findViewById(R.id.searchButton);
		global = (Global) getActivity().getApplication();

		final EditText search = (EditText) view.findViewById(R.id.catalogSearchField);

		clearData.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				search.setText(empstr);
			}
		});
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = search.getText().toString().trim();
				if (!text.isEmpty()) {
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
					performSearch(text);
				}
			}
		});
		search.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					String text = v.getText().toString().trim();
					if (!text.isEmpty()) {
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
						performSearch(text);
					}
					return true;
				}
				return false;
			}
		});
		search.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				String test = s.toString().trim();
				if (test.isEmpty()) {
					if (myCursor != null)
						myCursor.close();

					initAllProducts();
					lView.setAdapter(adap2);
				}
				// adap.getFilter().filter(s);
			}
		});

		lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
				// Toast.makeText(arg0.getContext(),
				// filteredTitles.get(position)+" pressed",
				// Toast.LENGTH_SHORT).show();

				if (myCursor.moveToPosition(position)) {
					String[] data = new String[6];

					data[0] = myCursor.getString(myCursor.getColumnIndex("_id"));
					data[1] = myCursor.getString(myCursor.getColumnIndex("prod_name"));
					data[2] = myCursor.getString(myCursor.getColumnIndex("prod_price"));
					data[3] = myCursor.getString(myCursor.getColumnIndex("prod_desc"));
					data[4] = myCursor.getString(myCursor.getColumnIndex("prod_onhand"));
					data[5] = myCursor.getString(myCursor.getColumnIndex("prod_img_name"));

					/*
					 * data[0] =
					 * myCursor.getString(myCursor.getColumnIndex(myCursor
					 * .getColumnName(0))); data[1] =
					 * myCursor.getString(myCursor
					 * .getColumnIndex(myCursor.getColumnName(1))); data[2] =
					 * myCursor
					 * .getString(myCursor.getColumnIndex(myCursor.getColumnName
					 * (2))); data[3] =
					 * myCursor.getString(myCursor.getColumnIndex
					 * (myCursor.getColumnName(3))); data[4] =
					 * myCursor.getString
					 * (myCursor.getColumnIndex(myCursor.getColumnName(4)));
					 */

					/*
					 * String prodID =
					 * myCursor.getString(myCursor.getColumnIndex
					 * (myCursor.getColumnName(0))); String prodName =
					 * myCursor.getString
					 * (myCursor.getColumnIndex(myCursor.getColumnName(1)));
					 * String onHand =
					 * myCursor.getString(myCursor.getColumnIndex
					 * (myCursor.getColumnName(4))); String prodPrice =
					 * myCursor.
					 * getString(myCursor.getColumnIndex(myCursor.getColumnName
					 * (2))); String prodDesc =
					 * myCursor.getString(myCursor.getColumnIndex
					 * (myCursor.getColumnName(3)));
					 */

					if (!myPref.getSettings("Fast-Scanning Mode")) {
						Intent intent = new Intent(getActivity(), CatalogPickerFragActivity.class);
						intent.putExtra("prod_id", data[0]);
						intent.putExtra("prod_name", data[1]);
						intent.putExtra("prod_on_hand", data[4]);
						intent.putExtra("prod_price", format(data[2]));
						intent.putExtra("prod_desc", data[3]);
						intent.putExtra("url", data[5]);
						startActivityForResult(intent, 0);
					} else {
						automaticAddOrder(data);
					}
				}

				/*
				 * Intent intent = new
				 * Intent(arg0.getContext(),HistTransOrderDetailMenu.class);
				 * startActivity(intent);
				 */
			}
		});

		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder().showStubImage(-1).cacheInMemory().cacheOnDisc().showImageForEmptyUri(-1)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).build();
		config = new ImageLoaderConfiguration.Builder(getActivity()).defaultDisplayImageOptions(options).threadPoolSize(2)
				.threadPriority(Thread.NORM_PRIORITY - 1).offOutOfMemoryHandling().memoryCache(new WeakMemoryCache()).build();
		imageLoader.init(config);
		return view;

	}

	/*
	 * @Override public void onStop() { super.onStop(); myCursor.close();
	 * 
	 * }
	 * 
	 * @Override public void onPause() { super.onPause(); myCursor.close();
	 * 
	 * }
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		myCursor.close();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();

		if (activity != null) {
			/*
			 * myPref = new MyPreferences(getActivity());
			 * 
			 * final SQLiteDatabase myDB =
			 * SQLiteDatabase.openDatabase(myPref.getDBpath(), null,
			 * SQLiteDatabase
			 * .NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READWRITE);
			 * prodHandler = new ProductsHandler(getActivity());
			 * 
			 * myCursor = prodHandler.getCatalogData(myDB); adap2 = new
			 * CustomCursorAdapter
			 * (getActivity(),myCursor,CursorAdapter.NO_SELECTION);
			 * 
			 * imgHandler = new ProductsImagesHandler(getActivity()); imgLinks =
			 * imgHandler.getLinks(myDB,"I"); initLinks(); myDB.close();
			 */

			initAllProducts();

			if (lView != null) {
				lView.setAdapter(adap2);
			}

		}

	}

	public void performSearch(String text) {
		if (myCursor != null)
			myCursor.close();
		switch (global.searchType) {
		case 0: // search by Name
		{
			myCursor = prodHandler.searchProducts(text, "prod_name");
			// myCursor.moveToFirst();
			int s = myCursor.getPosition();
			adap2 = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			lView.setAdapter(adap2);
			break;
		}
		case 1: // search by Description
		{
			myCursor = prodHandler.searchProducts(text, "prod_desc");
			adap2 = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			break;
		}
		case 2: // search by Type
		{
			myCursor = prodHandler.searchProducts(text, "prod_type");
			adap2 = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			break;
		}
		case 3: // search by UPC
		{
			myCursor = prodHandler.searchProducts(text, "prod_upc");
			adap2 = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			break;
		}
		}
		/*
		 * myCursor = prodHandler.getSearchCust(text); adap2 = new
		 * CustomCursorAdapter(getActivity,myCursor,CursorAdapter.NO_SELECTION);
		 */

		lView.setAdapter(adap2);

	}

	public void initAllProducts() {
		myPref = new MyPreferences(getActivity());

		// final SQLiteDatabase myDB =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(), null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READWRITE);
		prodHandler = new ProductsHandler(getActivity());

		myCursor = prodHandler.getCatalogData();
		adap2 = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);

		/*
		 * imgHandler = new ProductsImagesHandler(getActivity()); imgLinks =
		 * imgHandler.getLinks(myDB,"I"); initLinks();
		 */
	}

	/*
	 * public void initLinks() { int size = allProdID.size(); for(int i =
	 * 0;i<size;i++) { allLinks.add(getLink(allProdID.get(i))); } } public
	 * String getLink(String tag) { String value = imgLinks.get(tag);
	 * if(value!=null) { return value; } return empStr; }
	 */

	/*
	 * public class ListViewAdapter extends BaseAdapter implements Filterable {
	 * private LayoutInflater myInflater; private Bitmap myIcon; private Context
	 * context;
	 * 
	 * 
	 * private MyLVFilter filter; //public ManageImages manager;
	 * 
	 * public ListViewAdapter(Context context) { myInflater =
	 * LayoutInflater.from(context); this.context = context;
	 * 
	 * int width = getResources().getDisplayMetrics().widthPixels; int height =
	 * getResources().getDisplayMetrics().heightPixels;
	 * 
	 * //manager = new
	 * ManageImages(getActivity().getApplicationContext(),10,true,width,height);
	 * //imageLoader.init(ImageLoaderConfiguration.createDefault(context));
	 * 
	 * filteredTitles = allTitles; filteredAmounts = allAmounts; filteredQty =
	 * allQty; filteredDetails = allDetails; //filteredLinks = allLinks;
	 * 
	 * }
	 * 
	 * @Override public int getCount() { // TODO Auto-generated method stub
	 * return filteredTitles.size(); }
	 * 
	 * @Override public Object getItem(int position) { // TODO Auto-generated
	 * method stub return filteredTitles.get(position); }
	 * 
	 * @Override public long getItemId(int position) { // TODO Auto-generated
	 * method stub return 0; }
	 * 
	 * @Override public View getView(int position, View convertView, ViewGroup
	 * parent) { // TODO Auto-generated method stub
	 * 
	 * ViewHolder holder; final int index = position; final String link =
	 * getLink(allProdID.get(position)); if(convertView==null) { holder = new
	 * ViewHolder(); convertView =
	 * myInflater.inflate(R.layout.catalog_listview_adapter, null); holder.title
	 * = (TextView)convertView.findViewById(R.id.catalogItemName); holder.qty =
	 * (TextView)convertView.findViewById(R.id.catalogItemQty); holder.amount =
	 * (TextView)convertView.findViewById(R.id.catalogItemPrice); holder.detail
	 * = (TextView)convertView.findViewById(R.id.catalogItemInfo);
	 * holder.iconImage =
	 * (ImageView)convertView.findViewById(R.id.catalogRightIcon);
	 * holder.itemImage =
	 * (ImageView)convertView.findViewById(R.id.catalogItemPic);
	 * 
	 * holder.title.setText(filteredTitles.get(position));
	 * holder.qty.setText(filteredQty.get(position));
	 * holder.amount.setText(filteredAmounts.get(position));
	 * holder.detail.setText(filteredDetails.get(position));
	 * 
	 * holder.iconImage.setOnClickListener(new View.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { // TODO Auto-generated method
	 * stub Intent intent = new
	 * Intent(getActivity(),CatalogProdDetailsActivity.class);
	 * intent.putExtra("url",getLink(allProdID.get(index)));
	 * startActivity(intent); } });
	 * 
	 * 
	 * if(!link.isEmpty()) { holder.itemImage.setTag(link);
	 * UrlImageViewHelper.setUrlDrawable(holder.itemImage,
	 * getLink(allProdID.get(position)), R.drawable.emobile_icon, null); } else
	 * holder.itemImage.setImageResource(R.drawable.emobile_icon);
	 * 
	 * 
	 * 
	 * convertView.setTag(holder); }
	 * 
	 * else { holder = (ViewHolder) convertView.getTag(); }
	 * 
	 * holder.title.setText(filteredTitles.get(position));
	 * holder.qty.setText(filteredQty.get(position));
	 * holder.amount.setText(filteredAmounts.get(position));
	 * holder.detail.setText(filteredDetails.get(position));
	 * 
	 * 
	 * holder.iconImage.setOnClickListener(new View.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { // TODO Auto-generated method
	 * stub Intent intent = new
	 * Intent(getActivity(),CatalogProdDetailsActivity.class);
	 * intent.putExtra("url", allLinks.get(index)); startActivity(intent); } });
	 * 
	 * 
	 * //if(!getLink(allProdID.get(position)).isEmpty())
	 * imageLoader.displayImage(getLink(allProdID.get(position)),
	 * holder.itemImage,options);
	 * 
	 * holder.itemImage.setOnClickListener(new View.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { // TODO Auto-generated method
	 * stub Intent intent = new
	 * Intent(getActivity(),ShowProductImageActivity2.class);
	 * intent.putExtra("url", getLink(allProdID.get(index)));
	 * startActivity(intent); } }); return convertView; }
	 * 
	 * @Override public Filter getFilter() { // TODO Auto-generated method stub
	 * if(filter==null) { filter = new MyLVFilter();
	 * 
	 * } return filter; }
	 * 
	 * private class MyLVFilter extends Filter { ArrayList<String>filtTitle ;
	 * ArrayList<String>filtQty ; ArrayList<String>filtAmount;
	 * ArrayList<String>filtDetails; //ArrayList<String>filtLinks;
	 * 
	 * @Override protected FilterResults performFiltering(CharSequence
	 * constraint) { // TODO Auto-generated method stub
	 * 
	 * constraint = constraint.toString().toLowerCase();
	 * 
	 * FilterResults result = new FilterResults();
	 * 
	 * if(constraint!=null && constraint.toString().length()>0) { filtTitle =
	 * new ArrayList<String>(); filtQty = new ArrayList<String>(); filtAmount =
	 * new ArrayList<String>(); filtDetails = new ArrayList<String>();
	 * //filtLinks = new ArrayList<String>(); Global global =
	 * (Global)getActivity().getApplication(); int type = global.searchType;
	 * for(int i = 0, l = allTitles.size();i<l;i++) {
	 * 
	 * switch(type) { case 0: { String curTitle = allTitles.get(i);
	 * if(curTitle.toLowerCase().contains(constraint)) {
	 * filtTitle.add(curTitle); filtAmount.add(allAmounts.get(i));
	 * filtQty.add(allQty.get(i)); filtDetails.add(allDetails.get(i));
	 * //filtLinks.add(allLinks.get(1)); } break; } case 1: { String curDesc =
	 * allDetails.get(i); if(curDesc.toLowerCase().contains(constraint)) {
	 * filtTitle.add(allTitles.get(i)); filtAmount.add(allAmounts.get(i));
	 * filtQty.add(allQty.get(i)); filtDetails.add(curDesc);
	 * //filtLinks.add(allLinks.get(1)); } break; } case 2: {
	 * 
	 * break; } case 3: {
	 * 
	 * break; } }
	 * 
	 * 
	 * 
	 * } result.count = filtTitle.size(); result.values = filtTitle;
	 * 
	 * } else { synchronized(this) { result.values = allTitles; result.count =
	 * allTitles.size(); } } return result; }
	 * 
	 * @Override protected void publishResults(CharSequence constraint,
	 * FilterResults results) { // TODO Auto-generated method stub
	 * 
	 * String t = constraint.toString();
	 * if(results.count>0&&constraint.toString().length()>0) { filteredTitles =
	 * filtTitle; filteredQty = filtQty; filteredAmounts = filtAmount;
	 * filteredDetails = filtDetails; //filteredLinks = filtLinks;
	 * adap.notifyDataSetChanged(); } else {
	 * if(constraint.toString().length()==0||constraint==null) { filteredTitles
	 * = allTitles; filteredQty = allQty; filteredAmounts = allAmounts;
	 * filteredDetails = allDetails; //filteredLinks = allLinks;
	 * adap.notifyDataSetInvalidated();
	 * 
	 * } else { filteredTitles = filtTitle; filteredQty = filtQty;
	 * filteredAmounts = filtAmount; filteredDetails = filtDetails;
	 * //filteredLinks = filtLinks; adap.notifyDataSetInvalidated(); } } }
	 * 
	 * } public class ViewHolder { TextView title; TextView amount; TextView
	 * qty; TextView detail; ImageView iconImage; ImageView itemImage; }
	 * 
	 * }
	 */

	public String getQty(String id) {
		String value = global.qtyCounter.get(id);

		if (value == null) {
			return "0";
		}
		return value;
	}

	public int getIntQty(String id) {
		Global global = (Global) getActivity().getApplication();
		String value = global.qtyCounter.get(id);

		if (value == null) {
			return 0;
		}
		;
		return Integer.parseInt(value);
	}

	public class CustomCursorAdapter extends CursorAdapter {
		LayoutInflater inflater;

		public CustomCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			// TODO Auto-generated constructor stub
			inflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			final int position = cursor.getPosition();
			TextView title = (TextView) view.findViewById(R.id.catalogItemName);
			TextView qty = (TextView) view.findViewById(R.id.catalogItemQty);
			TextView amount = (TextView) view.findViewById(R.id.catalogItemPrice);
			TextView detail = (TextView) view.findViewById(R.id.catalogItemInfo);

			ImageView iconImage = (ImageView) view.findViewById(R.id.catalogRightIcon);
			ImageView itemImage = (ImageView) view.findViewById(R.id.catalogItemPic);

			final String prod_id = cursor.getString(cursor.getColumnIndex("_id"));
			final String prod_name = cursor.getString(cursor.getColumnIndex("prod_name"));
			final String prod_price = cursor.getString(cursor.getColumnIndex("prod_price"));
			final String prod_desc = cursor.getString(cursor.getColumnIndex("prod_desc"));
			final String urlLink = cursor.getString(cursor.getColumnIndex("prod_img_name"));

			title.setText(prod_name);
			qty.setText(getQty(prod_id));
			// String amnt =
			// cursor.getString(cursor.getColumnIndex(cursor.getColumnName(3)));
			amount.setText(format(prod_price));
			detail.setText(prod_desc);

			// imageLoader.displayImage(getLink(prod_id),itemImage,options);
			imageLoader.displayImage(urlLink, itemImage, options);

			iconImage.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(getActivity(), CatalogProdDetailsActivity.class);
					// intent.putExtra("url", getLink(prod_id));
					intent.putExtra("url", urlLink);
					intent.putExtra("prod_id", prod_id);
					startActivity(intent);
				}
			});

			itemImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(getActivity(), ShowProductImageActivity2.class);
					// intent.putExtra("url", getLink(prod_id));
					intent.putExtra("url", urlLink);
					startActivity(intent);
				}
			});
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			final View retView = inflater.inflate(R.layout.catalog_listview_adapter, parent, false);
			return retView;
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 2) {
			adap2.notifyDataSetChanged();
			getActivity().setResult(-2);
		}
	}

	public String format(String text) {
		DecimalFormat frmt = new DecimalFormat("0.00");
		// StringBuilder sb = new StringBuilder();
		if (text.isEmpty())
			return "0.00";
		// sb.append("$").append(frmt.format(Double.parseDouble(text)));
		return frmt.format(Double.parseDouble(text));
	}

	public void automaticAddOrder(String[] data) {
		int a = global.searchType;
		// TextView qty = (TextView)lView.findViewById(R.id.catalogItemQty);
		// int i = global.cur_orders.size();
		// List<Orders> orders = new ArrayList<Orders>();
		Orders order = new Orders();
		OrderProducts ord = new OrderProducts();

		// String t = qty.getText().toString();
		int sum = getIntQty(data[0]);
		order.setQty("1");

		global.qtyCounter.put(data[0], Integer.toString(sum + 1));

		order.setName(data[1]);
		order.setValue(data[2]);
		order.setProdID(data[0]);
		order.setDiscount("$0.00");
		order.setTax("$0.00");
		order.setDistQty("0");
		order.setTaxQty("0");

		String val = data[2];
		if (val.isEmpty() || val == null)
			val = "0.00";
		double total = Double.parseDouble(val);
		DecimalFormat frmt = new DecimalFormat("0.00");
		order.setTotal(frmt.format(total));

		// add order to db
		ord.getSetData("ordprod_qty", false, "1");
		ord.getSetData("ordprod_name", false, data[1]);
		ord.getSetData("ordprod_desc", false, data[3]);
		ord.getSetData("prod_id", false, data[0]);
		ord.getSetData("overwrite_price", false, data[2]);

		// Still need to do add the appropriate tax/discount value
		ord.getSetData("prod_taxValue", false, "0.00");
		ord.getSetData("discount_value", false, "0.00");

		ord.getSetData("taxAmount", false, "0");
		ord.getSetData("taxTotal", false, "0.00");
		ord.getSetData("disAmount", false, "0");
		ord.getSetData("disTotal", false, "0.00");
		ord.getSetData("itemTotal", false, data[2]);

		OrdersHandler handler = new OrdersHandler(getActivity());

		GenerateOrdID generator = new GenerateOrdID(getActivity());

		if (handler.getDBSize() == 0)
			global.lastOrdID = generator.generate("");
		else
			global.lastOrdID = generator.generate(handler.getLastOrdID());

		ord.getSetData("ord_id", false, global.lastOrdID);

		// OrderProductsHandler handler2 = new
		// OrderProductsHandler(getActivity());

		if (global.orderProducts == null) {
			global.orderProducts = new ArrayList<OrderProducts>();
		}

		UUID uuid = UUID.randomUUID();
		String randomUUIDString = uuid.toString();

		global.orderProducts.add(ord);
		ord.getSetData("ordprod_id", false, randomUUIDString);

		// end of adding to db;

		if (global.cur_orders == null) {
			global.cur_orders = new ArrayList<Orders>();
		}
		global.cur_orders.add(order);

		adap2.notifyDataSetChanged();
		getActivity().setResult(-2);
	}
}
