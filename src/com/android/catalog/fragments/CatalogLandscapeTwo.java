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
import com.android.emobilepos.AddonsPickerFragActivity;
import com.android.emobilepos.CatalogPickerFragActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.SalesReceiptFragment;
import com.android.support.DBManager;

import com.android.support.Global;
import com.android.support.MyPreferences;


import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CatalogLandscapeTwo extends SherlockFragment {
	public GridView gridView;
	
	private ImageLoader imageLoader;
	private DisplayImageOptions options;

	
	public CustomCursorAdapter adapter;
	public CustomCursorCategoriesAdapter categoriesGVAdapter;
	private HashMap<String, String> imgLinks = new HashMap<String, String>();

	private Cursor myCursor;
	private Global global;
	//private AlertDialog promptDialog;
	private MyPreferences myPref;
	public ProductsHandler prodHandler;
	private CategoriesHandler categoriesHandler;
	private DBManager dbManager;
	public SQLiteDatabase db;
	private LinearLayout catButLayout;
	
	private boolean onRestaurantMode = false;
	private boolean restModeViewingProducts = false;
	private List<String>list = new ArrayList<String>();
	private String selectedProdName = "";
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.catalog_landscape_layout, container, false);

		gridView = (GridView) view.findViewById(R.id.catalogGridview);
		catButLayout = (LinearLayout)view.findViewById(R.id.categoriesButtonLayoutHolder);
		global = (Global) getActivity().getApplication();
		myPref = new MyPreferences(getActivity());
		dbManager = new DBManager(getActivity());
		db = dbManager.openReadableDB();

		
		
    	

		File cacheDir = new File(myPref.getCacheDir());
		
		if(!cacheDir.exists())
			cacheDir.mkdirs();
		
		imageLoader = ImageLoader.getInstance();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration
				.Builder(getActivity()).memoryCacheExtraOptions(100, 100).discCache(new UnlimitedDiscCache(cacheDir)).build();
		
		imageLoader.init(config);
		options = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(600)).showImageOnLoading(R.drawable.loading_image)
				.showImageForEmptyUri(R.drawable.no_image).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT).build();
		
		
		
		
		if(myPref.getPreferences(MyPreferences.pref_restaurant_mode))
			onRestaurantMode = true;
		
		int displayWidth = getResources().getDisplayMetrics().widthPixels;

		int imageWidth;
		
		//displayWidth/#of images we want peer row
		if(myPref.getIsTablet())
			imageWidth = displayWidth / 6;
		else
			imageWidth = displayWidth / 5;
			
		if(displayWidth<=900)
			imageWidth = 190;
		gridView.setColumnWidth(imageWidth);

		
		if(Global.cat_id=="0")
			Global.cat_id = myPref.getPreferencesValue(MyPreferences.pref_default_category);

		
		
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
					int size = list.size();
					if(size>0)
					{
						for(int i = 0 ;i<size;i++)
							removeCategoryButton(list.get(i));
						list.clear();
						
						Global.cat_id = "0";
						categoriesHandler = new CategoriesHandler(getActivity());
						myCursor = categoriesHandler.getCategoriesCursor(db);
						categoriesGVAdapter = new CustomCursorCategoriesAdapter(getActivity(),myCursor,CursorAdapter.NO_SELECTION);
						gridView.setAdapter(categoriesGVAdapter);
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
		/*if(imageLoaderTest!=null)
			imageLoaderTest.clearCache();*/
		super.onDestroy();
	}
	

	public void initAllProducts() 
	{
		if(gridView!=null)
		{
			myPref = new MyPreferences(getActivity());
		
			if(!onRestaurantMode)
			{
				
				prodHandler = new ProductsHandler(getActivity());
				myCursor = prodHandler.getCatalogData(db);
				adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
				gridView.setAdapter(adapter);
			}
			else	//restaurant mode load categories
			{
				categoriesHandler = new CategoriesHandler(getActivity());
				myCursor = categoriesHandler.getCategoriesCursor(db);
				categoriesGVAdapter = new CustomCursorCategoriesAdapter(getActivity(),myCursor,CursorAdapter.NO_SELECTION);
				if(!Global.cat_id.equals("0"))
				{
					getCategoryCursor(myCursor.getColumnIndex("_id"),myCursor.getColumnIndex("cat_name"),myCursor.getColumnIndex("num_subcategories"));
				}
				else
					gridView.setAdapter(categoriesGVAdapter);
			}
		}
	}
	
	
	
	public Cursor getCursor() 
	{
		return myCursor;
	}
	
	public void setCursor(Cursor cursor)
	{
		this.myCursor = cursor;
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
				
				int size1 = list.size();
				int temp = list.indexOf((String)v.getTag());
				
				for(int i = temp+1 ; i < size1; i ++)
				{
					removeCategoryButton(list.get(i));
					list.remove(i);
				}
				
				int size2 = list.size();
				if(size2<size1)
				{
					myCursor.close();
					Global.cat_id = list.get(size2 - 1);
					myCursor = categoriesHandler.getSubcategoriesCursor(db,Global.cat_id);
					categoriesGVAdapter = new CustomCursorCategoriesAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
					gridView.setAdapter(categoriesGVAdapter);
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
	

	public void performSearch(String text) 
	{
		if(!onRestaurantMode)
		{
			if (myCursor != null)
				myCursor.close();
			switch (global.searchType) {
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
				gridView.setAdapter(adapter);
			else
				{
					myCursor.close();
					initAllProducts();
					gridView.setAdapter(adapter);
					Toast.makeText(getActivity(), "Product not found...", Toast.LENGTH_LONG).show();
				}
		}

	}

	public String getLink(String tag) 
	{
		String value = imgLinks.get(tag);
		if (value != null) {
			return value;
		}
		return "";
	}
	
	
	private void performClickEvent()
	{
		
		String [] data = populateDataForIntent();
		
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
			

			selectedProdName = data[1];
			//imageLoaderTest.clearCache();
			startActivityForResult(intent, 0);
		} else {
			double onHandQty = Double.parseDouble(data[4]);
			double newQty = 1;
			String addedQty = global.qtyCounter.get(data[0]); 
			
			if(addedQty!=null&&!addedQty.isEmpty())
				newQty = Double.parseDouble(addedQty)+1;
			
			if(myPref.getPreferences(MyPreferences.pref_limit_products_on_hand)&&(Global.ord_type==Global.IS_SALES_RECEIPT||Global.ord_type==Global.IS_INVOICE)&&(newQty>onHandQty))
			{
				Global.showPrompt(getActivity(), R.string.dlog_title_error, getActivity().getString(R.string.limit_onhand));
			}
			
			else
			{
				if(myPref.getPreferences(MyPreferences.pref_group_receipt_by_sku))
				{
					int orderIndex = global.checkIfGroupBySKU(getActivity(), data[0], "1");
					if(orderIndex!=-1)
					{
						global.refreshParticularOrder(myPref,orderIndex);
						performListViewUpdate();
					}
					else
					{
						automaticAddOrder(data);
						
					}
				}
				else
				{
					automaticAddOrder(data);
					
				}
			}
		}	
	}

	private String[] populateDataForIntent()
	{
		String[] data = new String[9];
		data[0] = myCursor.getString(myCursor.getColumnIndex("_id"));
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
	
	
	private void getCategoryCursor(int i_id,int i_cat_name,int i_num_subcategories)
	{
		if(!myPref.getPreferences(MyPreferences.pref_enable_multi_category))
		{
			restModeViewingProducts = true;
			Global.cat_id = myCursor.getString(myCursor.getColumnIndex("_id"));
			list.add(Global.cat_id);
			addCategoryButton(myCursor.getString(myCursor.getColumnIndex("cat_name")),Global.cat_id);
			
			
			myCursor.close();
			
			
			prodHandler = new ProductsHandler(getActivity());
			myCursor = prodHandler.getCatalogData(db);
			adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			gridView.setAdapter(adapter);
		}
		else
		{
		int num_subcategories = Integer.parseInt(myCursor.getString(myCursor.getColumnIndex("num_subcategories")));
		if(num_subcategories>0)
		{
			Global.cat_id = myCursor.getString(myCursor.getColumnIndex("_id"));
			list.add(Global.cat_id);
			addCategoryButton(myCursor.getString(myCursor.getColumnIndex("cat_name")),Global.cat_id);
			
			
			myCursor.close();

			
			myCursor = categoriesHandler.getSubcategoriesCursor(db, Global.cat_id);
			categoriesGVAdapter = new CustomCursorCategoriesAdapter(getActivity(),myCursor,CursorAdapter.NO_SELECTION);
			gridView.setAdapter(categoriesGVAdapter);
		}
		else
		{
			restModeViewingProducts = true;
			Global.cat_id = myCursor.getString(myCursor.getColumnIndex("_id"));
			list.add(Global.cat_id);
			addCategoryButton(myCursor.getString(myCursor.getColumnIndex("cat_name")),Global.cat_id);
			
			
			myCursor.close();
			
			
			prodHandler = new ProductsHandler(getActivity());
			myCursor = prodHandler.getCatalogData(db);
			adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			gridView.setAdapter(adapter);
		}
		}
	}
	
	
	public class CustomCursorAdapter extends CursorAdapter 
	{
		LayoutInflater inflater;

		public CustomCursorAdapter(Context context, Cursor c, int flags) 
		{
			super(context, c, flags);
			// TODO Auto-generated constructor stub
			inflater = LayoutInflater.from(context);
			
			//imageLoaderTest = new ImageLoaderTest(getActivity());
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) 
		{
			// TODO Auto-generated method stub
			final int position = cursor.getPosition();

			
			final ViewHolder holder = (ViewHolder)view.getTag();
			
			final String link = cursor.getString(holder.i_prod_img_name);

			//imageLoaderTest.DisplayImage(link, holder.iconImage, true);
			imageLoader.displayImage(link, holder.iconImage, options);
			holder.imageTitleBar.setText(cursor.getString(holder.i_prod_name));
			
			holder.iconImage.setOnTouchListener(Global.opaqueImageOnClick());
			holder.iconImage.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					if (myCursor.moveToPosition(position)) 
					{
						if(!onRestaurantMode)
							performClickEvent();
						else if(onRestaurantMode&&!restModeViewingProducts)
						{
							int num_subcategories = Integer.parseInt(myCursor.getString(myCursor.getColumnIndex("num_subcategories")));
							if(num_subcategories>0)
							{
								Global.cat_id = myCursor.getString(myCursor.getColumnIndex("_id"));
								list.add(Global.cat_id);
								addCategoryButton(myCursor.getString(myCursor.getColumnIndex("cat_name")),Global.cat_id);
								
								//imageLoaderTest.clearCache();
								myCursor.close();

								
								myCursor = categoriesHandler.getSubcategoriesCursor(db, Global.cat_id);
								categoriesGVAdapter = new CustomCursorCategoriesAdapter(getActivity(),myCursor,CursorAdapter.NO_SELECTION);
								gridView.setAdapter(categoriesGVAdapter);
							}
							else
							{
								Global.cat_id = myCursor.getString(myCursor.getColumnIndex("_id"));
								list.add(Global.cat_id);
								addCategoryButton(myCursor.getString(myCursor.getColumnIndex("cat_name")),Global.cat_id);
								
								
								//imageLoaderTest.clearCache();
								myCursor.close();
								restModeViewingProducts = true;
								
								prodHandler = new ProductsHandler(getActivity());
								myCursor = prodHandler.getCatalogData(db);
								adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
								gridView.setAdapter(adapter);
							}
						}
						else
						{
							ProductAddonsHandler prodAddonsHandler = new ProductAddonsHandler(getActivity());
							List<HashMap<String,String>> tempListMap = prodAddonsHandler.getParentAddons(db, myCursor.getString(myCursor.getColumnIndex("_id")));
							if(tempListMap!=null&&tempListMap.size()>0)
							{
								/*
								Intent intent = new Intent(getActivity(), AddonsPickerFragActivity.class);
								intent.putExtra("prod_id", myCursor.getString(myCursor.getColumnIndex("_id")));
								Global.productParentAddons = tempListMap;
								if(global.addonSelectionType==null)
									global.addonSelectionType = new HashMap<String,String[]>();
								imageLoaderTest.clearCache();
								startActivityForResult(intent, 0);*/
								
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

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			View retView = null;

				retView = inflater.inflate(R.layout.catalog_gridview_adapter, parent, false);
			
			
			ViewHolder holder = new ViewHolder();
			holder.imageTitleBar = (TextView) retView.findViewById(R.id.gridViewImageTitle);
			holder.iconImage = (ImageView) retView.findViewById(R.id.gridViewImage);
			

			
			
			holder.i_prod_name = cursor.getColumnIndex("prod_name");
			holder.i_prod_img_name = cursor.getColumnIndex("prod_img_name");
			
			retView.setTag(holder);
			return retView;
		}
		
		private class ViewHolder
		{
			TextView imageTitleBar;
			ImageView iconImage;
			
			int i_prod_name,i_prod_img_name;
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
			
			//imageLoaderTest = new ImageLoaderTest(getActivity());
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) 
		{
			// TODO Auto-generated method stub
			final int position = cursor.getPosition();

			
			final ViewHolder holder = (ViewHolder)view.getTag();
			
			final String link = cursor.getString(holder.i_url_icon);

			//imageLoaderTest.DisplayImage(link, holder.iconImage, true);
			imageLoader.displayImage(link, holder.iconImage, options);
			holder.imageTitleBar.setText(cursor.getString(holder.i_cat_name));
			
			
			holder.iconImage.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					
					if (myCursor.moveToPosition(position)) 
					{
						getCategoryCursor(holder.i_id,holder.i_cat_name,holder.i_num_subcategories);
						/*if(!myPref.getSettings(Global.enable_recursive))
						{
							restModeViewingProducts = true;
							Global.cat_id = myCursor.getString(holder.i_id);
							list.add(Global.cat_id);
							addCategoryButton(myCursor.getString(holder.i_cat_name),Global.cat_id);
							
							
							myCursor.close();
							
							
							prodHandler = new ProductsHandler(getActivity());
							myCursor = prodHandler.getCatalogData(db);
							adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
							gridView.setAdapter(adapter);
						}
						else
						{
							num_subcategories = Integer.parseInt(myCursor.getString(holder.i_num_subcategories));
							if(num_subcategories>0)
							{
								Global.cat_id = myCursor.getString(holder.i_id);
								list.add(Global.cat_id);
								addCategoryButton(myCursor.getString(holder.i_cat_name),Global.cat_id);
								
								
								myCursor.close();
	
								
								myCursor = categoriesHandler.getSubcategoriesCursor(db, Global.cat_id);
								categoriesGVAdapter = new CustomCursorCategoriesAdapter(getActivity(),myCursor,CursorAdapter.NO_SELECTION);
								gridView.setAdapter(categoriesGVAdapter);
							}
							else
							{
								restModeViewingProducts = true;
								Global.cat_id = myCursor.getString(holder.i_id);
								list.add(Global.cat_id);
								addCategoryButton(myCursor.getString(holder.i_cat_name),Global.cat_id);
								
								
								myCursor.close();
								
								
								prodHandler = new ProductsHandler(getActivity());
								myCursor = prodHandler.getCatalogData(db);
								adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
								gridView.setAdapter(adapter);
							}
						}*/
					}
				}
			});

			
			holder.iconImage.setOnLongClickListener(new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					if(myCursor.moveToPosition(position))
					{
						
						restModeViewingProducts = true;
						Global.cat_id = myCursor.getString(holder.i_id);
						list.add(Global.cat_id);
						addCategoryButton(myCursor.getString(holder.i_cat_name),Global.cat_id);
						
						
						myCursor.close();
						
						
						prodHandler = new ProductsHandler(getActivity());
						myCursor = prodHandler.getCatalogData(db);
						adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
						gridView.setAdapter(adapter);
					}
					return false;
				}
			});
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub

			View retView = null;

				retView = inflater.inflate(R.layout.catalog_gridview_adapter, parent, false);
			

			
			
			ViewHolder holder = new ViewHolder();
			holder.imageTitleBar = (TextView) retView.findViewById(R.id.gridViewImageTitle);
			holder.iconImage = (ImageView) retView.findViewById(R.id.gridViewImage);
			

			
			holder.i_id = cursor.getColumnIndex("_id");
			holder.i_cat_name = cursor.getColumnIndex("cat_name");
			holder.i_url_icon = cursor.getColumnIndex("url_icon");
			holder.i_num_subcategories = cursor.getColumnIndex("num_subcategories");

			
			retView.setTag(holder);
			return retView;
		}
		
		private class ViewHolder
		{
			TextView imageTitleBar;
			ImageView iconImage;
			
			int i_id,i_cat_name,i_url_icon,i_num_subcategories;
		}
	}
	
	
	
	public String format(String text) {
		DecimalFormat frmt = new DecimalFormat("0.00");
		if (text.isEmpty())
			return "0.00";
		return frmt.format(Double.parseDouble(text));
	}
	

//	private void promptInvalidQty()
//	{
//		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
//		promptDialog = dialogBuilder.setTitle("Error").setMessage(getString(R.string.limit_onhand)).setCancelable(false)
//				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface thisDialog, int which) {
//						// TODO Auto-generated method stub
//						thisDialog.dismiss();
//					}
//				}).create();
//		
//		promptDialog.show();
//	}
	
	
	public void automaticAddOrder(String[] data)
	{
		global.automaticAddOrder(getActivity(),false, global, data);
		performListViewUpdate();
	}
	
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == 9)
		{
			myCursor.close();
			myCursor = prodHandler.viewOtherTypes(db, selectedProdName);
			adapter = new CustomCursorAdapter(getActivity(), myCursor, CursorAdapter.NO_SELECTION);
			gridView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	}
	
	
	
	private void performListViewUpdate()
	{
		Fragment frag = getActivity().getSupportFragmentManager().findFragmentById(R.id.receiptsFragment);
		if (frag != null) {
			View test = (View) frag.getView();
			ListView lview = (ListView) test.findViewById(R.id.receiptListView);
			lview.invalidateViews();
			lview.setSelection(lview.getAdapter().getCount() - 1);

			SalesReceiptFragment fragment = (SalesReceiptFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.receiptsFragment);
			fragment.reCalculate();
			
			
		}
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
	}

}
