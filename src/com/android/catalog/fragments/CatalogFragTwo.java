package com.android.catalog.fragments;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



import com.actionbarsherlock.app.SherlockFragment;
import com.android.database.CategoriesHandler;

import com.android.database.ProductAddonsHandler;
import com.android.database.ProductsHandler;
import com.android.database.VolumePricesHandler;
import com.android.emobilepos.AddonsPickerFragActivity;
import com.android.emobilepos.CatalogPickerFragActivity;
import com.android.emobilepos.CatalogProdDetailsActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.ShowProductImageActivity2;
import com.android.support.DBManager;

import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.testimgloader.ImageLoaderTest;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;




public class CatalogFragTwo extends SherlockFragment {

	
	//private AlertDialog promptDialog;
	
	
	
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	
	
	
	private Cursor myCursor;
	public ListView lView;

	public CustomCursorAdapter adapter;
	public CustomCursorCategoriesAdapter categoriesGVAdapter;
	private Global global;

	private MyPreferences myPref;
	private ProductsHandler prodHandler;
	private VolumePricesHandler volPriceHandler;
	private DBManager dbManager;
	private SQLiteDatabase db;
	
	private boolean onRestaurantMode = false;
	private LinearLayout catButLayout;
	private CategoriesHandler categoriesHandler;
	private List<String>categoriesList = new ArrayList<String>();
	private boolean restModeViewingProducts = false;
	
	private String selectedProdName = "";
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.catalog_listview_layout, container, false);
		lView = (ListView) view.findViewById(R.id.catalogListview);
		catButLayout = (LinearLayout)view.findViewById(R.id.categoriesButtonLayoutHolder);
		global = (Global) getActivity().getApplication();
		myPref = new MyPreferences(getActivity());
		//global.initSettingsValues(getActivity());
		
		
    	MyPreferences myPref = new MyPreferences(getActivity());
		File cacheDir = new File(myPref.getCacheDir());
		if(!cacheDir.exists())
			cacheDir.mkdirs();
		
		imageLoader = ImageLoader.getInstance();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration
				.Builder(getActivity()).memoryCacheExtraOptions(100, 100).discCacheExtraOptions(1000, 1000, CompressFormat.JPEG, 100, null).discCache(new UnlimitedDiscCache(cacheDir))
				.build();
		
		
		imageLoader.init(config);
		imageLoader.handleSlowNetwork(true);
		options = new DisplayImageOptions.Builder().
				cacheOnDisc(true).resetViewBeforeLoading(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).build();
		
		
		
		dbManager = new DBManager(getActivity());
		db = dbManager.openReadableDB();
		
		if(myPref.getPreferences(MyPreferences.pref_restaurant_mode))
			onRestaurantMode = true;
		
		if(Global.cat_id=="0")
			Global.cat_id = myPref.getPreferencesValue(MyPreferences.pref_default_category);
		
		lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub

				if (myCursor.moveToPosition(position)) 
				{
					if(!onRestaurantMode)
						performClickEvent();
					else if(onRestaurantMode&&!restModeViewingProducts)
					{
						getCategoryCursor();
					}
					else
					{
						ProductAddonsHandler prodAddonsHandler = new ProductAddonsHandler(getActivity());
						List<HashMap<String,String>> tempListMap = prodAddonsHandler.getParentAddons(db, myCursor.getString(myCursor.getColumnIndex("_id")));
						if(tempListMap!=null&&tempListMap.size()>0)
						{
							Intent intent = new Intent(getActivity(), AddonsPickerFragActivity.class);
							//intent.putExtra("prod_id", myCursor.getString(myCursor.getColumnIndex("_id")));
							String data[] =  populateDataForIntent();
							intent.putExtra("prod_id", data[0]);
							intent.putExtra("prod_name", data[1]);
							intent.putExtra("prod_on_hand", data[4]);
							intent.putExtra("prod_price", data[2]);
							intent.putExtra("prod_desc", data[3]);
							intent.putExtra("url", data[5]);
							intent.putExtra("prod_istaxable", data[6]);
							intent.putExtra("prod_type", data[7]);
							intent.putExtra("prod_taxcode", myCursor.getString(myCursor.getColumnIndex("prod_taxcode")));
							intent.putExtra("prod_taxtype", myCursor.getString(myCursor.getColumnIndex("prod_taxtype")));
							intent.putExtra("cat_id", myCursor.getString(myCursor.getColumnIndex("cat_id")));
							
							
							selectedProdName = data[1];
							
							Global.productParentAddons = tempListMap;
							
							global.addonSelectionType = new HashMap<String,String[]>();
							startActivityForResult(intent, 0);
						}
						else
							performClickEvent();
					}
				}
			}
		});
		
		initAllProducts();	
		if(!onRestaurantMode)
			catButLayout.setVisibility(View.GONE);
		else
		{
			Button but = (Button)catButLayout.findViewById(R.id.buttonAllCategories);
			but.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int size = categoriesList.size();
					if(size>0)
					{
						for(int i = 0 ;i<size;i++)
							removeCategoryButton(categoriesList.get(i));
						categoriesList.clear();
						
						Global.cat_id = "0";
						
						categoriesHandler = new CategoriesHandler(getActivity());
						myCursor = categoriesHandler.getCategoriesCursor(db);
						categoriesGVAdapter = new CustomCursorCategoriesAdapter(getActivity(),myCursor,CursorAdapter.NO_SELECTION);
						lView.setAdapter(categoriesGVAdapter);
					}
				}
			});
		}
		
		return view;

	}
	
	@Override
	public void onDestroy() 
	{
		
		if(db.isOpen())
			db.close();
		myCursor.close();
		super.onDestroy();
	}

	
	
	private String[] populateDataForIntent()
	{
		String[] data = new String[9];
		data[0] = myCursor.getString(myCursor.getColumnIndex("_id"));
		
		String val = myPref.getPreferencesValue(MyPreferences.pref_attribute_to_display);
		
		if(val.equals("prod_desc"))
			data[1] = myCursor.getString(myCursor.getColumnIndex("prod_desc"));
		else
			data[1] = myCursor.getString(myCursor.getColumnIndex("prod_name"));
		
		String tempPrice = myCursor.getString(myCursor.getColumnIndex("volume_price"));
		if(tempPrice == null||tempPrice.isEmpty())
		{
			tempPrice = myCursor.getString(myCursor.getColumnIndex("pricelevel_price"));
			if(tempPrice == null||tempPrice.isEmpty())
			{
				tempPrice = myCursor.getString(myCursor.getColumnIndex("chain_price"));
				
				if(tempPrice == null || tempPrice.isEmpty())
					tempPrice = myCursor.getString(myCursor.getColumnIndex("master_price"));
			}
		}

		data[2] = tempPrice;
		data[3] = myCursor.getString(myCursor.getColumnIndex("prod_desc"));
		
		tempPrice = new String();
		tempPrice = myCursor.getString(myCursor.getColumnIndex("local_prod_onhand"));
		if(tempPrice == null || tempPrice.isEmpty())
			tempPrice = myCursor.getString(myCursor.getColumnIndex("master_prod_onhand"));
		if(tempPrice.isEmpty())
			tempPrice = "0";
		data[4] = tempPrice;
		
		data[5] = myCursor.getString(myCursor.getColumnIndex("prod_img_name"));
		data[6] = myCursor.getString(myCursor.getColumnIndex("prod_istaxable"));
		data[7] = myCursor.getString(myCursor.getColumnIndex("prod_type"));
		data[8] = myCursor.getString(myCursor.getColumnIndex("cat_id"));
		
		return data;
	}
	
	private void performClickEvent()
	{
		String[]data = populateDataForIntent();
		
		
		if (!myPref.getPreferences(MyPreferences.pref_fast_scanning_mode)) {
			Intent intent = new Intent(getActivity(), CatalogPickerFragActivity.class);
			intent.putExtra("prod_id", data[0]);
			intent.putExtra("prod_name", data[1]);
			intent.putExtra("prod_on_hand", data[4]);
			intent.putExtra("prod_price", data[2]);
			intent.putExtra("prod_desc", data[3]);
			intent.putExtra("url", data[5]);
			intent.putExtra("prod_istaxable", data[6]);
			intent.putExtra("prod_type", data[7]);
			intent.putExtra("prod_taxcode", myCursor.getString(myCursor.getColumnIndex("prod_taxcode")));
			intent.putExtra("prod_taxtype", myCursor.getString(myCursor.getColumnIndex("prod_taxtype")));
			intent.putExtra("cat_id", myCursor.getString(myCursor.getColumnIndex("cat_id")));
			
			selectedProdName = data[1];
			startActivityForResult(intent, 0);
		} 
		else 
		{
			double onHandQty = Double.parseDouble(data[4]);
			double newQty = 1;
			String addedQty = global.qtyCounter.get(data[0]); 
			
			if(addedQty!=null)
				newQty = Double.parseDouble(addedQty)+1;
			
			if(myPref.getPreferences(MyPreferences.pref_limit_products_on_hand)&&!data[7].equals("Service")
					&&(Global.ord_type==Global.IS_SALES_RECEIPT||Global.ord_type==Global.IS_INVOICE)
					&&(newQty>onHandQty))
			{
				Global.showPrompt(getActivity(), R.string.dlog_title_error, getActivity().getString(R.string.limit_onhand));
				//promptInvalidQty();
			}
			
			else
			{
				if(myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku))
				{
					int orderIndex = global.checkIfGroupBySKU(getActivity(), data[0], "1");
					if(orderIndex!=-1)
					{
						global.refreshParticularOrder(myPref,orderIndex);
						adapter.notifyDataSetChanged();
						getActivity().setResult(-2);
					}
					else
						automaticAddOrder(data);
				}
				else
					automaticAddOrder(data);
			}
		}
	}

	
	
	public void performSearch(String text) {
		
		if(!onRestaurantMode)
		{
		
			if (myCursor != null)
				myCursor.close();
			switch (global.searchType) 
			{
				case 0: // search by Name
				{
					myCursor = prodHandler.searchProducts(text, "prod_name");
					adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
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
			if(myCursor.getCount()>0)
				lView.setAdapter(adapter);
			else
				{
					myCursor.close();
					initAllProducts();
					lView.setAdapter(adapter);
					Toast.makeText(getActivity(), "Product not found...", Toast.LENGTH_LONG).show();
				}
		}
	}
	
	

	public void initAllProducts() 
	{
		if(lView!=null)
		{
			volPriceHandler = new VolumePricesHandler(getActivity());
		if(!onRestaurantMode)
		{
			prodHandler = new ProductsHandler(getActivity());
			
	
			myCursor = prodHandler.getCatalogData(db);
			adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			lView.setAdapter(adapter);
		}
		else
		{
			categoriesHandler = new CategoriesHandler(getActivity());
			myCursor = categoriesHandler.getCategoriesCursor(db);
			categoriesGVAdapter = new CustomCursorCategoriesAdapter(getActivity(),myCursor,CursorAdapter.NO_SELECTION);
			
			if(!Global.cat_id.equals("0"))
			{
				getCategoryCursor();
			}
			else
				lView.setAdapter(categoriesGVAdapter);
		}
		}
	}

	
	private void getCategoryCursor()
	{
		if(!myPref.getPreferences(MyPreferences.pref_enable_multi_category))
		{
			restModeViewingProducts = true;
			Global.cat_id = myCursor.getString(myCursor.getColumnIndex("_id"));
			categoriesList.add(Global.cat_id);
			addCategoryButton(myCursor.getString(myCursor.getColumnIndex("cat_name")),Global.cat_id);
			
			
			myCursor.close();
			
			
			prodHandler = new ProductsHandler(getActivity());
			myCursor = prodHandler.getCatalogData(db);
			adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			lView.setAdapter(adapter);
		}
		else
		{
			int num_subcategories = Integer.parseInt(myCursor.getString(myCursor.getColumnIndex("num_subcategories")));
			if(num_subcategories>0)
			{
				Global.cat_id = myCursor.getString(myCursor.getColumnIndex("_id"));
				categoriesList.add(Global.cat_id);
				addCategoryButton(myCursor.getString(myCursor.getColumnIndex("cat_name")),Global.cat_id);
				
				
				myCursor.close();

				
				myCursor = categoriesHandler.getSubcategoriesCursor(db, Global.cat_id);
				categoriesGVAdapter = new CustomCursorCategoriesAdapter(getActivity(),myCursor,CursorAdapter.NO_SELECTION);
				lView.setAdapter(categoriesGVAdapter);
			}
			else
			{
				Global.cat_id = myCursor.getString(myCursor.getColumnIndex("_id"));
				categoriesList.add(Global.cat_id);
				addCategoryButton(myCursor.getString(myCursor.getColumnIndex("cat_name")),Global.cat_id);
				
				
				myCursor.close();
				restModeViewingProducts = true;
				
				prodHandler = new ProductsHandler(getActivity());
				myCursor = prodHandler.getCatalogData(db);
				adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
				lView.setAdapter(adapter);
				//Toast.makeText(getActivity(), "Show products", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	
	private void addCategoryButton(String categoryName,String cat_id)
	{
		Button btn = new Button(getActivity(), null, android.R.attr.buttonStyleSmall);
		btn.setTag(cat_id);
	    btn.setText(categoryName);
	    btn.setPadding(18, 0, 18, 0);
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
	    params.gravity = Gravity.CENTER_VERTICAL;
	    params.setMargins(0, 0, 5, 0);
	    catButLayout.addView(btn,params);
	    
	    btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				int size1 = categoriesList.size();
				int temp = categoriesList.indexOf((String)v.getTag());
				
				for(int i = temp+1 ; i < size1; i ++)
				{
					removeCategoryButton(categoriesList.get(i));
					categoriesList.remove(i);
				}
				
				int size2 = categoriesList.size();
				if(size2<size1)
				{
					myCursor.close();
					Global.cat_id = categoriesList.get(size2 - 1);
					myCursor = categoriesHandler.getSubcategoriesCursor(db,Global.cat_id);
					categoriesGVAdapter = new CustomCursorCategoriesAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
					lView.setAdapter(categoriesGVAdapter);
				}
			}
		});   
	}
	
	private void removeCategoryButton(String cat_id)
	{
	    Button temp = (Button)catButLayout.findViewWithTag(cat_id);
	    if(temp!=null)
	    {
	    	restModeViewingProducts = false;
	    	catButLayout.removeView(temp);
	    }
	}
	
	
	
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
		return Integer.parseInt(value);
	}

	public class CustomCursorAdapter extends CursorAdapter 
	{
		String prod_name;
		boolean showProdName = true;
		LayoutInflater inflater;
		public CustomCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			// TODO Auto-generated constructor stub
			inflater = LayoutInflater.from(context);
			String val = myPref.getPreferencesValue(MyPreferences.pref_attribute_to_display);
			if(val.equals("prod_desc"))
				showProdName = false;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			
			ViewHolder holder = (ViewHolder)view.getTag();

			final String prod_id = cursor.getString(holder.i_id);
			holder.qty.setText(getQty(prod_id));
			if(showProdName)
				prod_name = cursor.getString(holder.i_prod_name);
			else
				prod_name = cursor.getString(holder.i_prod_desc);
			holder.title.setText(prod_name);
			
			
			String tempPrice = cursor.getString(holder.i_volume_price);
			if(tempPrice == null||tempPrice.isEmpty())
			{
				tempPrice = cursor.getString(holder.i_pricelevel_price);
				if(tempPrice == null||tempPrice.isEmpty())
				{
					tempPrice = cursor.getString(holder.i_chain_price);
					if(tempPrice == null || tempPrice.isEmpty())
						tempPrice = cursor.getString(holder.i_master_price);
				}
			}
			else
			{
				String [] temp = volPriceHandler.getVolumePrice(holder.qty.getText().toString(),prod_id);
				if(temp[1]!=null&&!temp[1].isEmpty())
					tempPrice = temp[1];
			}
			
			
			final String prod_price = tempPrice;
			final String prod_desc = cursor.getString(holder.i_prod_desc);
			final String urlLink = cursor.getString(holder.i_prod_img_name);

			
			holder.amount.setText(format(prod_price));
			holder.detail.setText(prod_desc);
			
			//imageLoaderTest.DisplayImage(urlLink, holder.itemImage,false);
			imageLoader.displayImage(urlLink, holder.itemImage, options);

			holder.iconImage.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(getActivity(), CatalogProdDetailsActivity.class);
					intent.putExtra("url", urlLink);
					intent.putExtra("prod_id", prod_id);
					startActivity(intent);
				}
			});

			
			holder.itemImage.setOnTouchListener(Global.opaqueImageOnClick());
			holder.itemImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(getActivity(), ShowProductImageActivity2.class);
					intent.putExtra("url", urlLink);
					startActivity(intent);
				}
			});
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			View retView = inflater.inflate(R.layout.catalog_listview_adapter, parent, false);
			
			ViewHolder holder = new ViewHolder();
			holder.title = (TextView) retView.findViewById(R.id.catalogItemName);
			holder.qty = (TextView) retView.findViewById(R.id.catalogItemQty);
			holder.amount = (TextView) retView.findViewById(R.id.catalogItemPrice);
			holder.detail = (TextView) retView.findViewById(R.id.catalogItemInfo);
			holder.iconImage = (ImageView) retView.findViewById(R.id.catalogRightIcon);
			holder.itemImage = (ImageView) retView.findViewById(R.id.catalogItemPic);
			
			holder.i_id = cursor.getColumnIndex("_id");
			holder.i_prod_name = cursor.getColumnIndex("prod_name");
			holder.i_master_price = cursor.getColumnIndex("master_price");
			holder.i_volume_price = cursor.getColumnIndex("volume_price");
			holder.i_pricelevel_price = cursor.getColumnIndex("pricelevel_price");
			holder.i_prod_desc = cursor.getColumnIndex("prod_desc");
			holder.i_prod_img_name = cursor.getColumnIndex("prod_img_name");
			holder.i_chain_price = cursor.getColumnIndex("chain_price");
			
			retView.setTag(holder);
			
			
			return retView;
		}
		
		private class ViewHolder
		{
			TextView title;
			TextView qty;
			TextView amount;
			TextView detail;

			ImageView iconImage;
			ImageView itemImage;
			
			int i_id,i_prod_name,i_chain_price,i_master_price,i_volume_price,i_pricelevel_price,i_prod_desc,i_prod_img_name;
		}

	}

	
	
	public class CustomCursorCategoriesAdapter extends CursorAdapter
	{
		LayoutInflater inflater;
		int num_subcategories = 0;

		public CustomCursorCategoriesAdapter(Context context, Cursor c, int flags) 
		{
			super(context, c, flags);
			// TODO Auto-generated constructor stub
			inflater = LayoutInflater.from(context);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) 
		{
			// TODO Auto-generated method stub
			final int position = cursor.getPosition();

			
			final ViewHolder holder = (ViewHolder)view.getTag();
			
			final String link = cursor.getString(holder.i_url_icon);

			//imageLoaderTest.DisplayImage(link, holder.itemImage, true);
			imageLoader.displayImage(link, holder.itemImage, options);
			holder.title.setText(cursor.getString(holder.i_cat_name));
			
			holder.qty.setVisibility(View.GONE);
			holder.amount.setVisibility(View.GONE);
			holder.detail.setVisibility(View.GONE);
			
			
			holder.itemImage.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					
					if (myCursor.moveToPosition(position)) 
					{
						Intent intent = new Intent(getActivity(), ShowProductImageActivity2.class);
						intent.putExtra("url", link);
						startActivity(intent);
					}
				}
			});

			
			holder.iconImage.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(myCursor.moveToPosition(position))
					{
						restModeViewingProducts = true;
						Global.cat_id = myCursor.getString(holder.i_id);
						categoriesList.add(Global.cat_id);
						addCategoryButton(myCursor.getString(holder.i_cat_name),Global.cat_id);
						
						
						myCursor.close();
						
						
						prodHandler = new ProductsHandler(getActivity());
						myCursor = prodHandler.getCatalogData(db);
						adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
						lView.setAdapter(adapter);
					}
				}
			});
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			View retView = null;
			retView = inflater.inflate(R.layout.catalog_listview_adapter, parent, false);

			ViewHolder holder = new ViewHolder();
			holder.title = (TextView) retView.findViewById(R.id.catalogItemName);
			holder.itemImage = (ImageView) retView.findViewById(R.id.catalogItemPic);
			holder.iconImage = (ImageView) retView.findViewById(R.id.catalogRightIcon);

			holder.qty = (TextView) retView.findViewById(R.id.catalogItemQty);
			holder.amount = (TextView) retView.findViewById(R.id.catalogItemPrice);
			holder.detail = (TextView) retView.findViewById(R.id.catalogItemInfo);
			
			
			holder.i_id = cursor.getColumnIndex("_id");
			holder.i_cat_name = cursor.getColumnIndex("cat_name");
			holder.i_url_icon = cursor.getColumnIndex("url_icon");

			
			retView.setTag(holder);
			return retView;
		}
		
		private class ViewHolder
		{
			TextView title,qty,amount,detail;
			ImageView itemImage;
			ImageView iconImage;
			
			int i_id,i_cat_name,i_url_icon;
		}
	}
	
	
	
	
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == 2) {
			adapter.notifyDataSetChanged();
			getActivity().setResult(-2);
		}
		else if(resultCode == 9)
		{
			myCursor.close();
			myCursor = prodHandler.viewOtherTypes(db, selectedProdName);
			adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			lView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	}
	
	

	public Cursor getCursor() 
	{
		return myCursor;
	}
	
	

	public String format(String text) {
		DecimalFormat frmt = new DecimalFormat("0.00");
		if (text.isEmpty())
			return "0.00";
		return frmt.format(Double.parseDouble(text));
	}
	
	public void automaticAddOrder(String[] data)
	{
		global.automaticAddOrder(getActivity(),false, global, data);
		adapter.notifyDataSetChanged();
		getActivity().setResult(-2);
	}
}
