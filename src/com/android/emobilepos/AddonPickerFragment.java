package com.android.emobilepos;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.android.database.OrdersHandler;
import com.android.database.ProductAddonsHandler;
import com.android.database.ProductsHandler;
import com.android.emobilepos.ordering.PickerProduct_FA;
import com.android.emobilepos.ordering.Receipt_FR;
import com.android.menuadapters.AddonListAdapter;
import com.android.support.DBManager;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.OrderProducts;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class AddonPickerFragment extends Fragment{

	private StickyGridHeadersGridView myGridView;
	private Activity activity;
	private Cursor c;
	private SQLiteDatabase db;
	private DBManager dbManager;
	private AddonListAdapter adapter;
	
	private final int SELECT_EMPTY = 0, SELECT_CHECKED = 1, SELECT_CROSS = 2;
	private Global global;
	private boolean isEditAddon = false;
	private MyPreferences myPref;
	private int item_position = 0;
	private String _prod_id = "";
	private StringBuilder _ord_desc = new StringBuilder();
	
	private BigDecimal addedAddon = new BigDecimal("0"),removedAddon = new BigDecimal("0");
	
	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 
		 activity = getActivity();
		 return inflater.inflate(R.layout.addon_picker_layout, container, false);
		 }
	
	

	 @Override
	 public void onDestroy()
	 {
		 if(db.isOpen())
			 db.close();
		 if(!c.isClosed())
			 c.close();
		 super.onDestroy();
	 }
	 
	 
	 @Override
	    public void onViewCreated(View view, Bundle savedInstanceState) {
	        super.onViewCreated(view, savedInstanceState);
	        Global.addonTotalAmount = 0;
	        final Bundle extras =activity.getIntent().getExtras();
	        global = (Global) activity.getApplication();
	        myPref = new MyPreferences(activity);
	        ProductAddonsHandler prodAddonsHandler = new ProductAddonsHandler(activity);
			dbManager = new DBManager(activity);
			db = dbManager.openReadableDB();
			_prod_id = extras.getString("prod_id");
			c = prodAddonsHandler.getChildAddons2(db,_prod_id,Global.productParentAddons);
	        myGridView = (StickyGridHeadersGridView)view.findViewById(R.id.asset_grid);
	        isEditAddon = extras.getBoolean("isEditAddon",false);
	        item_position = extras.getInt("item_position");

	        
	        adapter = new AddonListAdapter(activity,c);
	        
	        myGridView.setAreHeadersSticky(false);
	        myGridView.setAdapter(adapter);
	        
	        
	        
	        Button done = (Button) view.findViewById(R.id.addonDoneButton);
			done.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
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
							addOrderAddons(Integer.parseInt(values[1]),true);
							break;
						case SELECT_CROSS:
							addOrderAddons(Integer.parseInt(values[1]),false);
							break;
						}
					}
					
					if(!isEditAddon)
					{
						String [] data = new String[13];
						data[0] = extras.getString("prod_id");
						data[1] = extras.getString("prod_name");
						data[2] = extras.getString("prod_price");
						data[3] = extras.getString("prod_desc");
						data[4] = extras.getString("prod_on_hand");
						data[5] = extras.getString("url");
						data[6] = extras.getString("prod_istaxable");
						data[7] = extras.getString("prod_type");
						data[8] = extras.getString("cat_id");
						data[12] = extras.getString("prod_taxcode");
						data[11] = extras.getString("prod_taxtype");
						data[9] = extras.getString("prod_price_points");
						data[10] = extras.getString("prod_value_points");
						
						
						if(!myPref.getPreferences(MyPreferences.pref_fast_scanning_mode))
						{
							Intent intent = new Intent(getActivity(), PickerProduct_FA.class);
							intent.putExtra("prod_id", data[0]);
							intent.putExtra("prod_name", data[1]);
							intent.putExtra("prod_price", data[2]);
							intent.putExtra("prod_desc", data[3]);
							intent.putExtra("prod_on_hand", data[4]);
							intent.putExtra("url", data[5]);
							intent.putExtra("prod_istaxable", data[6]);
							intent.putExtra("prod_type", data[7]);
							intent.putExtra("prod_taxcode",data[12]);
							intent.putExtra("prod_taxtype", data[11]);
							intent.putExtra("prod_price_points", data[9]);
							intent.putExtra("prod_value_points", data[10]);
							intent.putExtra("isFromAddon", true);
							intent.putExtra("cat_id", extras.getString("cat_id"));
							startActivityForResult(intent, 0);
						}
						else
						{
							global.automaticAddOrder(activity,true, global, data);
							activity.setResult(2);
							activity.finish();
						}
						
					}
					else
					{
						updateLineItem(item_position);
						Global.addonSelectionMap.put(extras.getString("addon_map_key"), global.addonSelectionType);
						Global.orderProductAddonsMap.put(extras.getString("addon_map_key"), global.orderProductsAddons);
						global.addonSelectionType = new HashMap<String,String[]>();
						global.orderProductsAddons = new ArrayList<OrderProducts>();
						
						if(Receipt_FR.fragInstance!=null)
							Receipt_FR.fragInstance.reCalculate();
					}
					activity.finish();
				}
			});
	        

	    }
	 

	private void addOrderAddons(int pos, boolean isAdded) {
		if (c != null && c.moveToPosition(pos)) {
			OrderProducts ord = new OrderProducts();

			// String val = pickedQty;
			// double sum = Global.formatNumFromLocale(val) + getQty(prodID);

			// double total = Double.parseDouble(c.getString(arg0));
			// String temp = Double.toString(total);
			// order.setTotal(temp);

			// calculateAll(total); // calculate taxes and discount

			ord.getSetData("prod_istaxable", false, c.getString(c.getColumnIndex("prod_istaxable")));

			/*
			 * if(!myPref.getSettings(Global.allow_decimal)) { val =
			 * Integer.toString((int)Double.parseDouble(val));
			 * global.qtyCounter.put(prodID, Global.formatNumber(false, sum)); }
			 * else { global.qtyCounter.put(prodID, Global.formatNumber(true,
			 * sum)); }
			 * 
			 * 
			 * order.setQty(val); order.setName(extrasMap.get("prod_name"));
			 * order
			 * .setValue(Double.toString(Global.formatNumFromLocale(extrasMap
			 * .get("prod_price")))); order.setProdID(prodID);
			 * order.setDiscount("0.00"); order.setTax("0.00");
			 * order.setDistQty("0"); order.setTaxQty("0");
			 */

			// add order to db
			ord.getSetData("ordprod_qty", false, "1");
			ord.getSetData("ordprod_name", false, c.getString(c.getColumnIndex("prod_name")));
			ord.getSetData("ordprod_desc", false, c.getString(c.getColumnIndex("prod_desc")));
			ord.getSetData("prod_id", false, c.getString(c.getColumnIndex("_id")));

			String tempPrice = c.getString(c.getColumnIndex("volume_price"));
			if (tempPrice == null || tempPrice.isEmpty()) {
				tempPrice = c.getString(c.getColumnIndex("pricelevel_price"));
				if (tempPrice == null || tempPrice.isEmpty()) {
					tempPrice = c.getString(c.getColumnIndex("chain_price"));
					if (tempPrice == null || tempPrice.isEmpty())
						tempPrice = c.getString(c.getColumnIndex("master_price"));
				}
			}
			if (tempPrice == null || tempPrice.isEmpty() || (!isEditAddon&&!isAdded))
				tempPrice = "0";

			if (isAdded)
			{
				Global.addonTotalAmount += Double.parseDouble(tempPrice);
				addedAddon = addedAddon.add(Global.getBigDecimalNum(tempPrice));
			}
			else
			{
				Global.addonTotalAmount -= Double.parseDouble(tempPrice);
				removedAddon = removedAddon.add(Global.getBigDecimalNum(tempPrice));
			}

			ord.getSetData("overwrite_price", false, tempPrice);
			ord.getSetData("onHand", false, c.getString(c.getColumnIndex("master_prod_onhand")));
			ord.getSetData("imgURL", false, c.getString(c.getColumnIndex("prod_img_name")));

			// Still need to do add the appropriate tax/discount value
			if (ord.getSetData("prod_istaxable", true, null).equals("1")) {
				BigDecimal temp1 = Global.taxAmount.divide(new BigDecimal("100"));
				BigDecimal temp2 = temp1.multiply(Global.getBigDecimalNum(tempPrice)).setScale(2, RoundingMode.HALF_UP);
				// float temp =
				// (float)(Double.parseDouble(tempPrice)*(Global.taxAmount/100));
				ord.getSetData("prod_taxValue", false, temp2.toString());
				ord.getSetData("prod_taxId", false, Global.taxID);
			}
			// ord.getSetData("discount_id", false, discount_id);
			ord.getSetData("taxAmount", false, "");
			ord.getSetData("taxTotal", false, "");
			// ord.getSetData("disAmount", false, disAmount);
			// ord.getSetData("disTotal", false, disTotal);

			// ord.getSetData("pricelevel_id", false, priceLevelID);
			// ord.getSetData("priceLevelName", false, priceLevelName);

			ord.getSetData("prod_price", false, tempPrice);
			// ord.getSetData("tax_position", false,
			// Integer.toString(tax_position));
			// ord.getSetData("discount_position", false,
			// Integer.toString(discount_position));
			// ord.getSetData("pricelevel_position", false,
			// Integer.toString(pricelevel_position));
			// ord.getSetData("uom_position", false,
			// Integer.toString(uom_position));

			ord.getSetData("prod_type", false, c.getString(c.getColumnIndex("prod_type")));

			ord.getSetData("addon", false, "1");
			if (isAdded)
				ord.getSetData("isAdded", false, "1");
			else
				ord.getSetData("isAdded", false, "0");

			_ord_desc.append("<br/>");
			if(!isAdded)//Not added
				_ord_desc.append("[NO ").append(c.getString(c.getColumnIndex("prod_name"))).append("]");
			else
				_ord_desc.append("[").append(c.getString(c.getColumnIndex("prod_name"))).append("]");

			ord.getSetData("itemTotal", false, tempPrice);
			ord.getSetData("itemSubtotal", false, tempPrice);

			OrdersHandler handler = new OrdersHandler(activity);

			GenerateNewID generator = new GenerateNewID(activity);

			if (!Global.isFromOnHold) {
				if (handler.getDBSize() == 0)
					Global.lastOrdID = generator.generate("", 0);
				else
					Global.lastOrdID = generator.generate(handler.getLastOrdID(), 0);

			}

			ord.getSetData("ord_id", false, Global.lastOrdID);

			if (global.orderProductsAddons == null) {
				global.orderProductsAddons = new ArrayList<OrderProducts>();
			}

			UUID uuid = UUID.randomUUID();
			String randomUUIDString = uuid.toString();

			ord.getSetData("ordprod_id", false, randomUUIDString);
			global.orderProductsAddons.add(ord);

		}
	}
	
	private void updateLineItem(int position)
	{
		OrderProducts ordProd = global.orderProducts.get(position);
		
		//BigDecimal temp = new BigDecimal(ordProd.getSetData("overwrite_price", true, null));
		
		ProductsHandler prodHandler = new ProductsHandler(activity);
		String [] itemData = prodHandler.getProductDetails(_prod_id);
		
		BigDecimal temp = new BigDecimal(itemData[2]);
		
		temp = temp.add(addedAddon);
		if(temp.compareTo(new BigDecimal("0"))==-1)
			temp = new BigDecimal("0");
		ordProd.getSetData("overwrite_price", false, Global.getRoundBigDecimal(temp));
		ordProd.getSetData("itemSubtotal", false, Global.getRoundBigDecimal(temp));
		ordProd.getSetData("itemTotal", false, Global.getRoundBigDecimal(temp));
		ordProd.getSetData("ordprod_desc", false, itemData[3]+_ord_desc.toString());
		global.orderProducts.set(position, ordProd);
	}
}
