package com.android.catalog.fragments;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.android.database.OrdersHandler;
import com.android.database.ProductsHandler;
import com.android.database.ProductsImagesHandler;
import com.android.emobilepos.CatalogPickerFragActivity;
import com.android.emobilepos.CatalogProdDetailsActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.ShowProductImageActivity2;
import com.android.emobilepos.HistPayDetailsFragment.ListViewAdapter;
import com.android.emobilepos.SalesReceiptFragment.CustomAdapter;
import com.android.support.GenerateOrdID;
import com.android.support.Global;
import com.android.support.ManageImages;
import com.android.support.MyPreferences;
import com.android.support.OrderProducts;
import com.android.support.Orders;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class CatalogLandscapeOne extends Fragment {
	private Context thisContext;
	// private ImageLoader imageLoader;
	GridView gridView;

	private ImageLoader imageLoader;
	private DisplayImageOptions options;

	private CustomCursorAdapter adapter;
	private List<String> allTitles = new ArrayList<String>();
	private List<String> allProdID = new ArrayList<String>();
	private HashMap<String, String> imgLinks = new HashMap<String, String>();
	private String[] filterValues = new String[] { "Name", "Description", "Type", "UPC" };

	private Cursor myCursor;
	private Global global;
	private String empstr = "";

	private MyPreferences myPref;
	private ProductsHandler prodHandler;
	private ImageView clearData, searchButton;
	private ImageLoaderConfiguration config;
	private Spinner filterSpinner;
	private CustomAdapter filterAdapter;
	private ProductsImagesHandler imgHandler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.catalog_landscape_layout, container, false);

		gridView = (GridView) view.findViewById(R.id.catalogGridview);
		clearData = (ImageView) view.findViewById(R.id.clearSearchField);
		searchButton = (ImageView) view.findViewById(R.id.searchButton);
		filterSpinner = (Spinner) view.findViewById(R.id.filterButton);
		global = (Global) getActivity().getApplication();

		final EditText search = (EditText) view.findViewById(R.id.catalogSearchField);
		// adapter = new
		// ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,
		// numbers);
		// adapter = new GridViewAdapter(getActivity(),getActivity());
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
					gridView.setAdapter(adapter);
				}
				// adap.getFilter().filter(s);
			}
		});

		int displayWidth = getResources().getDisplayMetrics().widthPixels;

		int imageWidth;
		if (displayWidth <= 540) {
			imageWidth = displayWidth / 6;
		} else if (displayWidth <= 860) {
			imageWidth = displayWidth / 7;
		} else {
			imageWidth = displayWidth / 8;

		}
		gridView.setColumnWidth(imageWidth);

		filterAdapter = new CustomAdapter(getActivity(), android.R.layout.simple_spinner_item, filterValues);
		filterSpinner.setAdapter(filterAdapter);

		filterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				// TODO Auto-generated method stub
				global.searchType = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		// gridView.setAdapter(adapter);

		return view;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();

		if (activity != null) {
			/*
			 * MyPreferences myPref = new MyPreferences(activity);
			 * SQLiteDatabase myDB =
			 * SQLiteDatabase.openDatabase(myPref.getDBpath(), null,
			 * SQLiteDatabase
			 * .NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READWRITE);
			 * ProductsHandler prodHandler = new ProductsHandler(getActivity());
			 * 
			 * allTitles = prodHandler.getColumn(myDB, "prod_name"); allProdID =
			 * prodHandler.getColumn(myDB, "prod_id");
			 * 
			 * ProductsImagesHandler imgHandler = new
			 * ProductsImagesHandler(getActivity()); imgLinks =
			 * imgHandler.getLinks(myDB,"I");
			 * 
			 * myDB.close();
			 * 
			 * imageLoader = ImageLoader.getInstance();
			 * imageLoader.init(ImageLoaderConfiguration
			 * .createDefault(getActivity())); options = new
			 * DisplayImageOptions.
			 * Builder().showStubImage(R.drawable.emobile_icon
			 * ).cacheInMemory().cacheOnDisc
			 * ().showImageForEmptyUri(R.drawable.ic_launcher).build();
			 * 
			 * 
			 * adapter = new GridViewAdapter(activity,activity);
			 */
			initAllProducts();

			if (gridView != null) {

				gridView.setAdapter(adapter);

			}

		}
	}

	/*
	 * @Override public void onResume() { super.onResume(); int orientation =
	 * getResources().getConfiguration().orientation;
	 * if(orientation==Configuration.ORIENTATION_PORTRAIT) { final
	 * FragmentTransaction ft = this.getFragmentManager().beginTransaction();
	 * ft.replace(R.id.catalog_name_tab, new CatalogFragOne(),"Portrait");
	 * ft.commit(); } }
	 */
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

	public void initAllProducts() {
		myPref = new MyPreferences(getActivity());

		// final SQLiteDatabase myDB =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(), null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READWRITE);
		prodHandler = new ProductsHandler(getActivity());

		myCursor = prodHandler.getCatalogData();
		adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);

		/*
		 * imageLoader = ImageLoader.getInstance();
		 * imageLoader.init(ImageLoaderConfiguration
		 * .createDefault(getActivity())); options = new
		 * DisplayImageOptions.Builder
		 * ().showStubImage(R.drawable.loading_image).
		 * cacheInMemory().cacheOnDisc
		 * ().showImageForEmptyUri(R.drawable.no_image).build();
		 */

		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().showImageForEmptyUri(R.drawable.no_image)
				.showStubImage(R.drawable.loading_image).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).build();
		config = new ImageLoaderConfiguration.Builder(getActivity()).defaultDisplayImageOptions(options).threadPoolSize(2)
				.threadPriority(Thread.NORM_PRIORITY - 1).offOutOfMemoryHandling().memoryCache(new WeakMemoryCache()).build();
		imageLoader.init(config);

		// myDB.close();
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
			adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			gridView.setAdapter(adapter);
			break;
		}
		case 1: // search by Description
		{
			myCursor = prodHandler.searchProducts(text, "prod_desc");
			adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			break;
		}
		case 2: // search by Type
		{
			myCursor = prodHandler.searchProducts(text, "prod_type");
			adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			break;
		}
		case 3: // search by UPC
		{
			myCursor = prodHandler.searchProducts(text, "prod_upc");
			adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			break;
		}
		}
		/*
		 * myCursor = prodHandler.getSearchCust(text); adap2 = new
		 * CustomCursorAdapter(getActivity,myCursor,CursorAdapter.NO_SELECTION);
		 */

		gridView.setAdapter(adapter);

	}

	public String getLink(String tag) {
		String value = imgLinks.get(tag);
		if (value != null) {
			return value;
		}
		return "";
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

			ImageView iconImage = (ImageView) view.findViewById(R.id.gridViewImage);

			final String link = cursor.getString(cursor.getColumnIndex("prod_img_name"));

			imageLoader.displayImage(link, iconImage, options);

			final int index = position;
			iconImage.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					/*
					 * Intent intent = new
					 * Intent(getActivity(),ShowProductImageActivity2.class);
					 * intent.putExtra("url", link); startActivity(intent);
					 */
					if (myCursor.moveToPosition(position)) {
						String[] data = new String[6];

						data[0] = myCursor.getString(myCursor.getColumnIndex("_id"));
						data[1] = myCursor.getString(myCursor.getColumnIndex("prod_name"));
						data[2] = myCursor.getString(myCursor.getColumnIndex("prod_price"));
						data[2] = data[2].replace("$", "");
						data[3] = myCursor.getString(myCursor.getColumnIndex("prod_desc"));
						data[4] = myCursor.getString(myCursor.getColumnIndex("prod_onhand"));
						data[5] = myCursor.getString(myCursor.getColumnIndex("prod_img_name"));

						/*
						 * data[0] =
						 * myCursor.getString(myCursor.getColumnIndex(myCursor
						 * .getColumnName(0))); data[1] =
						 * myCursor.getString(myCursor
						 * .getColumnIndex(myCursor.getColumnName(1))); data[2]
						 * =
						 * myCursor.getString(myCursor.getColumnIndex(myCursor.
						 * getColumnName(2))); data[3] =
						 * myCursor.getString(myCursor
						 * .getColumnIndex(myCursor.getColumnName(3))); data[4]
						 * =
						 * myCursor.getString(myCursor.getColumnIndex(myCursor.
						 * getColumnName(4)));
						 */

						/*
						 * String prodID =
						 * myCursor.getString(myCursor.getColumnIndex
						 * (myCursor.getColumnName(0))); String prodName =
						 * myCursor
						 * .getString(myCursor.getColumnIndex(myCursor.getColumnName
						 * (1))); String onHand =
						 * myCursor.getString(myCursor.getColumnIndex
						 * (myCursor.getColumnName(4))); String prodPrice =
						 * myCursor
						 * .getString(myCursor.getColumnIndex(myCursor.getColumnName
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
				}
			});

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			final View retView = inflater.inflate(R.layout.catalog_gridview_adapter, parent, false);
			return retView;
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

		double total = (sum + 1) * Double.parseDouble(val);
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

		// adapter.notifyDataSetChanged();
		// getActivity().setResult(2);
		/*
		 * Fragment frag =
		 * getActivity().getSupportFragmentManager().findFragmentById
		 * (R.id.receiptsFragment); if(frag!=null) { View test =
		 * (View)frag.getView(); ListView lview =
		 * (ListView)test.findViewById(R.id.receiptListView);
		 * lview.invalidateViews(); } else getActivity().setResult(2);
		 */
		Fragment frag = getActivity().getSupportFragmentManager().findFragmentById(R.id.receiptsFragment);
		if (frag != null) {
			View test = (View) frag.getView();
			ListView lview = (ListView) test.findViewById(R.id.receiptListView);
			TextView granTotal = (TextView) test.findViewById(R.id.grandTotalValue);
			TextView subTotal = (EditText) test.findViewById(R.id.subtotalField);
			lview.invalidateViews();
			lview.setSelection(lview.getAdapter().getCount() - 1);

			final Global global = (Global) getActivity().getApplication();

			int size = global.orderProducts.size();
			if (size > 0) {
				double amount = 0.00;
				int qty = 0;
				double prodPrice = 0.00;
				for (int i = 0; i < size; i++) {
					qty = Integer.parseInt(global.orderProducts.get(i).getSetData("ordprod_qty", true, empstr));
					String value = global.orderProducts.get(i).getSetData("overwrite_price", true, empstr);
					if (value.isEmpty() || value == null)
						value = "0.00";

					prodPrice = Double.parseDouble(value.replace("$", ""));
					amount += (qty * prodPrice);
				}
				DecimalFormat formt = new DecimalFormat("0.00");
				subTotal.setText(formt.format(amount));
				granTotal.setText(formt.format(amount));
			}
		}

	}

	/*
	 * public void onBackPressed() { DisplayMetrics metrics = new
	 * DisplayMetrics();
	 * getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
	 * int width = metrics.widthPixels; int height = metrics.heightPixels;
	 * 
	 * if((width>=600&&height>=1024)) //portrait mode and is Tablet {
	 * getActivity().setResult(2); } else { getActivity().recreate(); Fragment
	 * frag = getActivity().getSupportFragmentManager().findFragmentById(R.id.
	 * receiptsFragment); if(frag!=null) { View test = (View)frag.getView();
	 * ListView lview = (ListView)test.findViewById(R.id.receiptListView);
	 * lview.invalidateViews(); } }
	 * 
	 * }
	 */

	public int getIntQty(String id) {
		Global global = (Global) getActivity().getApplication();
		String value = global.qtyCounter.get(id);

		if (value == null) {
			return 0;
		}
		;
		return Integer.parseInt(value);
	}

	public class CustomAdapter extends ArrayAdapter<String> {
		private Activity context;
		String[] leftData = null;

		public CustomAdapter(Activity activity, int resource, String[] left) {
			super(activity, resource, left);
			this.context = activity;
			this.leftData = left;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);

			// we know that simple_spinner_item has android.R.id.text1 TextView:

			TextView text = (TextView) view.findViewById(android.R.id.text1);
			text.setTextColor(Color.WHITE);// choose your color
			text.setTextSize(8);
			text.setPadding(40, 0, 0, 0);

			return view;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				row = inflater.inflate(R.layout.spinner_layout, parent, false);
			}
			ImageView checked = (ImageView) row.findViewById(R.id.checkMark);
			TextView taxName = (TextView) row.findViewById(R.id.taxName);
			checked.setVisibility(View.INVISIBLE);
			taxName.setText(leftData[position]);

			return row;
		}

		/*
		 * public void setValues(TextView taxValue,int position) { StringBuilder
		 * sb = new StringBuilder(); if(isTax) {
		 * sb.append("%").append(rightData.get(position-1)[2]);
		 * taxValue.setText(sb.toString()); } else {
		 * if(rightData.get(position-1)[1].equals("Fixed")) { DecimalFormat frmt
		 * = new DecimalFormat("0.00"); double value =
		 * Double.parseDouble(rightData.get(position-1)[2]);
		 * 
		 * sb.append("$").append(frmt.format(value));
		 * taxValue.setText(sb.toString()); } else {
		 * sb.append("%").append(rightData.get(position-1)[2]);
		 * taxValue.setText(sb.toString()); } } }
		 */

	}

	public class GridViewAdapter extends BaseAdapter {

		private LayoutInflater myInflater;

		// public ManageImages manager;

		public Activity thisActivity;
		public int width;
		public int height;

		public GridViewAdapter(Context context, Activity activity) {
			myInflater = LayoutInflater.from(context);
			width = activity.getResources().getDisplayMetrics().widthPixels;
			height = activity.getResources().getDisplayMetrics().heightPixels;
			// manager = new
			// ManageImages(activity.getApplicationContext(),10,true,width,height);
			// imageLoader = new ImageLoader(context);
			thisActivity = activity;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return allTitles.size();
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
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = myInflater.inflate(R.layout.catalog_gridview_adapter, null);

				holder.iconImage = (ImageView) convertView.findViewById(R.id.gridViewImage);

				/*
				 * if(position%2==0) {
				 * holder.iconImage.setTag(allLinks.get(position));
				 * if(manager.isInCache(allLinks.get(position)))
				 * holder.iconImage
				 * .setImageBitmap(ManageImages.imageMap.get(allLinks
				 * .get(position)).get()); else
				 * manager.queueImage(allLinks.get(position), holder.iconImage,
				 * R.drawable.emobile_icon); } else {
				 * holder.iconImage.setTag(allLinks.get(position));
				 * if(manager.isInCache(allLinks.get(position)))
				 * holder.iconImage
				 * .setImageBitmap(ManageImages.imageMap.get(allLinks
				 * .get(position)).get()); else
				 * manager.queueImage(allLinks.get(position), holder.iconImage,
				 * R.drawable.emobile_icon); }
				 */

				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// holder.iconImage.setTag(allLinks.get(position));
			// manager.queueImage(allLinks.get(position), holder.iconImage,
			// R.drawable.emobile_icon);

			/*
			 * if(position%2==0) {
			 * holder.iconImage.setTag(allLinks.get(position));
			 * if(manager.isInCache(allLinks.get(position)))
			 * holder.iconImage.setImageBitmap
			 * (ManageImages.imageMap.get(allLinks.get(position)).get()); else
			 * manager.queueImage(allLinks.get(position), holder.iconImage,
			 * R.drawable.emobile_icon); } else {
			 * holder.iconImage.setTag(allLinks.get(position));
			 * if(manager.isInCache(allLinks.get(position)))
			 * holder.iconImage.setImageBitmap
			 * (ManageImages.imageMap.get(allLinks.get(position)).get()); else
			 * manager.queueImage(allLinks.get(position), holder.iconImage,
			 * R.drawable.emobile_icon); }
			 */

			imageLoader.displayImage(getLink(allProdID.get(position)), holder.iconImage, options);

			final int index = position;
			holder.iconImage.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(thisActivity, ShowProductImageActivity2.class);
					intent.putExtra("url", getLink(allProdID.get(index)));
					startActivity(intent);
				}
			});

			return convertView;
		}

		public class ViewHolder {
			ImageView iconImage;

		}

	}
}
