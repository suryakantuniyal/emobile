package com.android.emobilepos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

import com.android.database.OrdersHandler;
import com.android.database.ProductAddonsHandler;
import com.android.menuadapters.SectionedGridViewAdapter;
import com.android.menuadapters.SectionedGridViewAdapter.OnGridItemClickListener;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.OrderProducts;
import com.android.support.Orders;
import com.android.testimgloader.ImageLoaderTest;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class AddonsPickerFragment extends Fragment implements OnGridItemClickListener
{
	private ListView listView;
	private SectionedGridViewAdapter adapter = null;
	private LinkedHashMap<String, Cursor> cursorMap;
	private Activity activity;
	private ImageLoaderTest imageLoaderTest;
	private final int SELECT_EMPTY = 0, SELECT_CHECKED = 1, SELECT_CROSS = 2;
	private Global global;
	private boolean isEditAddon = false;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.addons_picker_layout, container, false);
		activity = getActivity();
		global = (Global) activity.getApplication();
		final Bundle extras =activity.getIntent().getExtras();
		isEditAddon = extras.getBoolean("isEditAddon",false);
		
		
		ProductAddonsHandler prodAddonsHandler = new ProductAddonsHandler(activity);
		
		cursorMap = prodAddonsHandler.getChildAddons(extras.getString("prod_id"),Global.productParentAddons);
		
		listView = (ListView) view.findViewById(R.id.listview);
		listView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						
						if (Build.VERSION.SDK_INT < 16) {
							removeLayoutListenerPre16(listView.getViewTreeObserver(),this);
					    } else {
					    	removeLayoutListenerPost16(listView.getViewTreeObserver(), this);
					    }

						// now check the width of the list view
						//int width = listView.getWidth();

						imageLoaderTest = new ImageLoaderTest(activity);
						adapter = new SectionedGridViewAdapter(activity,imageLoaderTest, cursorMap, listView.getWidth(), listView.getHeight(),getResources().getDimensionPixelSize(R.dimen.grid_item_size));
						
						adapter.setListener(AddonsPickerFragment.this);
						listView.setAdapter(adapter);

						listView.setDividerHeight(20);
					}
				});
		
		
		Button done = (Button) view.findViewById(R.id.addonDoneButton);
		done.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Toast.makeText(activity, "Done...", Toast.LENGTH_LONG).show();
				String[] keys = global.addonSelectionType.keySet().toArray(new String[global.addonSelectionType.size()]);
				int size = keys.length;
				String[] values;
				for(int i = 0 ; i < size;i++)
				{
					values = global.addonSelectionType.get(keys[i]);
					switch(Integer.parseInt(values[0]))
					{
					case SELECT_EMPTY:
						
						break;
					case SELECT_CHECKED:
						addOrderAddons(values[1],Integer.parseInt(values[2]),true);
						break;
					case SELECT_CROSS:
						addOrderAddons(values[1],Integer.parseInt(values[2]),false);
						break;
					}
				}
				
				if(!isEditAddon)
				{
					Intent intent = new Intent(getActivity(), CatalogPickerFragActivity.class);
					intent.putExtra("prod_id", extras.getString("prod_id"));
					intent.putExtra("prod_name", extras.getString("prod_name"));
					intent.putExtra("prod_on_hand", extras.getString("prod_on_hand"));
					intent.putExtra("prod_price", extras.getString("prod_price"));
					intent.putExtra("prod_desc", extras.getString("prod_desc"));
					intent.putExtra("url", extras.getString("url"));
					intent.putExtra("prod_istaxable", extras.getString("prod_istaxable"));
					intent.putExtra("prod_type", extras.getString("prod_type"));
					intent.putExtra("prod_taxcode",extras.getString("prod_taxcode"));
					intent.putExtra("prod_taxtype", extras.getString("prod_taxtype"));
					intent.putExtra("isFromAddon", true);
					intent.putExtra("cat_id", extras.getString("cat_id"));
					
					startActivityForResult(intent, 0);
				}
				else
				{
					Global.addonSelectionMap.put(extras.getString("addon_map_key"), global.addonSelectionType);
					Global.orderProductAddonsMap.put(extras.getString("addon_map_key"), global.orderProductsAddons);
					global.addonSelectionType = new HashMap<String,String[]>();
					global.orderProductsAddons = new ArrayList<OrderProducts>();
				}
				activity.finish();
			}
		});
		
		return view;
	}
	
	
	
	
	private void addOrderAddons(String key,int pos,boolean isAdded)
	{
		Cursor c = cursorMap.get(key);
		if (c!=null&&c.moveToPosition(pos)) 
		{
			Orders order = new Orders();
			OrderProducts ord = new OrderProducts();

			//String val = pickedQty;
			//double sum = Global.formatNumFromLocale(val) + getQty(prodID);
			

			//double total = Double.parseDouble(c.getString(arg0));
			//String temp = Double.toString(total);
			//order.setTotal(temp);

			//calculateAll(total); 					// calculate taxes and discount
			
			
			ord.getSetData("prod_istaxable", false, c.getString(c.getColumnIndex("prod_istaxable")));
			
			/*
			if(!myPref.getSettings(Global.allow_decimal))
			{
				val = Integer.toString((int)Double.parseDouble(val));
				global.qtyCounter.put(prodID, Global.formatNumber(false, sum));
			}
			else
			{
				global.qtyCounter.put(prodID, Global.formatNumber(true, sum));
			}
			
			
			order.setQty(val);
			order.setName(extrasMap.get("prod_name"));
			order.setValue(Double.toString(Global.formatNumFromLocale(extrasMap.get("prod_price"))));
			order.setProdID(prodID);
			order.setDiscount("0.00");
			order.setTax("0.00");
			order.setDistQty("0");
			order.setTaxQty("0");
			*/
			
			// add order to db
			ord.getSetData("ordprod_qty", false, "1");
			ord.getSetData("ordprod_name", false, c.getString(c.getColumnIndex("prod_name")));
			ord.getSetData("ordprod_desc", false, c.getString(c.getColumnIndex("prod_desc")));
			ord.getSetData("prod_id", false, c.getString(c.getColumnIndex("_id")));
			
			String tempPrice = c.getString(c.getColumnIndex("volume_price"));
			if(tempPrice == null||tempPrice.isEmpty())
			{
				tempPrice = c.getString(c.getColumnIndex("pricelevel_price"));
				if(tempPrice == null||tempPrice.isEmpty())
				{
					tempPrice = c.getString(c.getColumnIndex("chain_price"));
					if(tempPrice == null || tempPrice.isEmpty())
						tempPrice = c.getString(c.getColumnIndex("master_price"));
				}
			}
			if(tempPrice.isEmpty()||!isAdded)
				tempPrice = "0";
			
			if(isAdded)
				Global.addonTotalAmount+=Double.parseDouble(tempPrice);
			
			ord.getSetData("overwrite_price", false, tempPrice);
			ord.getSetData("onHand", false,c.getString(c.getColumnIndex("master_prod_onhand")));
			ord.getSetData("imgURL", false, c.getString(c.getColumnIndex("prod_img_name")));

			// Still need to do add the appropriate tax/discount value
			if(ord.getSetData("prod_istaxable", true, null).equals("1"))
			{
				float temp = (float)(Double.parseDouble(tempPrice)*(Global.taxAmount/100));
				ord.getSetData("prod_taxValue", false, Float.toString(temp));
				ord.getSetData("prod_taxId", false, Global.taxID);
			}
			//ord.getSetData("discount_id", false, discount_id);
			ord.getSetData("taxAmount", false, "");
			ord.getSetData("taxTotal", false, "");
			//ord.getSetData("disAmount", false, disAmount);
			//ord.getSetData("disTotal", false, disTotal);
			
			//ord.getSetData("pricelevel_id", false, priceLevelID);
			//ord.getSetData("priceLevelName", false, priceLevelName);
			
			ord.getSetData("prod_price", false, tempPrice);
			//ord.getSetData("tax_position", false, Integer.toString(tax_position));
			//ord.getSetData("discount_position", false, Integer.toString(discount_position));
			//ord.getSetData("pricelevel_position", false, Integer.toString(pricelevel_position));
			//ord.getSetData("uom_position", false, Integer.toString(uom_position));
			
			ord.getSetData("prod_type", false, c.getString(c.getColumnIndex("prod_type")));
			
			ord.getSetData("addon", false, "1");
			if(isAdded)
				ord.getSetData("isAdded", false, "1");
			else
				ord.getSetData("isAdded", false, "0");
			
			//Add UOM attributes to the order
			//ord.getSetData("uom_name", false, uomName);
			//ord.getSetData("uom_id", false, uomID);
			//ord.getSetData("uom_conversion", false, Double.toString(uomMultiplier));
			
			/*if(discountIsTaxable)
			{
				ord.getSetData("discount_is_taxable", false, "1");
			}
			if(isFixed)
				ord.getSetData("discount_is_fixed", false, "1");
			*/
			

			/*double itemTotal = total - toDouble(disTotal);
			if (itemTotal < 0)
				itemTotal = 0.00;*/
			
			ord.getSetData("itemTotal", false, tempPrice);
			ord.getSetData("itemSubtotal", false, tempPrice);
			ord.getSetData("addon_position", false, Integer.toString(pos));
			ord.getSetData("addon_section_name", false,key );

			OrdersHandler handler = new OrdersHandler(activity);

			GenerateNewID generator = new GenerateNewID(activity);

			if(!Global.isFromOnHold)
			{
			if (handler.getDBSize() == 0)
				Global.lastOrdID = generator.generate("",0);
			else
				Global.lastOrdID = generator.generate(handler.getLastOrdID(),0);

			}
			
			ord.getSetData("ord_id", false, Global.lastOrdID);


			if (global.orderProductsAddons == null) {
				global.orderProductsAddons = new ArrayList<OrderProducts>();
			}

			UUID uuid = UUID.randomUUID();
			String randomUUIDString = uuid.toString();

			
			ord.getSetData("ordprod_id", false, randomUUIDString);
			global.orderProductsAddons.add(ord);
			// end of adding to db;
			//global.cur_orders.add(order);
		}
	}
	
	
	public void onBackPressed()
	{
		//global.addonSelectionType = new HashMap<String,String[]>();
		if(imageLoaderTest!=null)
			imageLoaderTest.clearCache();
	}
	
	
	@SuppressWarnings("deprecation")
	private void removeLayoutListenerPre16(ViewTreeObserver observer, OnGlobalLayoutListener listener){
	    observer.removeGlobalOnLayoutListener(listener);
	}

	@TargetApi(16)
	private void removeLayoutListenerPost16(ViewTreeObserver observer, OnGlobalLayoutListener listener){
	    observer.removeOnGlobalLayoutListener(listener);
	}

	@Override
	public void onGridItemClicked(String sectionName, int position, View v) {
		Cursor sectionCursor = cursorMap.get(sectionName);
		if (sectionCursor!=null&&sectionCursor.moveToPosition(position)) {
			String data = sectionCursor.getString(sectionCursor.getColumnIndex("prod_name"));
			String msg = "Item clicked is:" + data;
			//Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
		}
	}
	
}
