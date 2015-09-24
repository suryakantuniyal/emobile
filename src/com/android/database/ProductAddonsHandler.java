package com.android.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.android.support.DBManager;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteStatement;

public class ProductAddonsHandler {

	private final String rest_addons = "rest_addons";
	private final String prod_id = "prod_id";
	private final String cat_id = "cat_id";
	private final String _update = "_update";
	private final String isactive = "isactive";

	private final List<String> attr = Arrays.asList(new String[] { rest_addons, prod_id, cat_id, isactive, _update });
	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	private List<String[]> addrData;
	private Activity activity;
	private List<HashMap<String,Integer>>dictionaryListMap;
	private MyPreferences myPref;

	private static final String table_name = "Product_addons";

	public ProductAddonsHandler(Activity activity) {
		this.activity = activity;
		myPref = new MyPreferences(activity);
		attrHash = new HashMap<String, Integer>();
		addrData = new ArrayList<String[]>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		
		initDictionary();
	}

	private void initDictionary() {
		int size = attr.size();
		for (int i = 0; i < size; i++) {
			attrHash.put(attr.get(i), i + 1);
			if ((i + 1) < size) {
				sb1.append(attr.get(i)).append(",");
				sb2.append("?").append(",");
			} else {
				sb1.append(attr.get(i));
				sb2.append("?");
			}
		}
	}

	private String getData(String tag, int record) {
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return addrData.get(record)[i];
		}
		return empStr;
	}

	private int index(String tag) {
		return attrHash.get(tag);
	}

	
	public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager._db.beginTransaction();

		try {

			addrData = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = addrData.size();

			for (int j = 0; j < size; j++) {
				insert.bindString(index(rest_addons), getData(rest_addons, j)); // rest_addons
				insert.bindString(index(prod_id), getData(prod_id, j)); // prod_id
				insert.bindString(index(cat_id), getData(cat_id, j)); // cat_id
				insert.bindString(index(_update), getData(_update, j)); // _update
				insert.bindString(index(isactive), getData(isactive, j)); // isactive

				insert.execute();
				insert.clearBindings();

			}
			insert.close();
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.ProductAddonsHandler (at Class.insert)]");

			Tracker tracker = EasyTracker.getInstance(activity);
			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
			DBManager._db.endTransaction();
		}
	}
	
	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_name);
		DBManager._db.execSQL(sb.toString());
	}
	
	
	
	
	
	public List<HashMap<String,String>> getParentAddons (String prodID)
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
//		if(db==null||!db.isOpen())
//			db = dbManager.openReadableDB();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT c.cat_id,c.cat_name,c.url_icon as 'url',Count(*) as 'qty' FROM Product_addons pa LEFT OUTER JOIN Products p ");
		sb.append("ON pa.cat_id = p.cat_id LEFT OUTER JOIN Categories c ON pa.cat_id = c.cat_id WHERE pa.prod_id = '");
		sb.append(prodID).append("'  GROUP BY cat_name ORDER BY pa.rest_addons ASC");
		
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		List<HashMap<String,String>> listHashMap = new ArrayList<HashMap<String,String>>();
		HashMap<String,String> hashMap = new HashMap<String,String>();
		Global.productParentAddonsDictionary = new HashMap<String,Integer>();
		if(cursor.moveToFirst())
		{
			int i_cat_id = cursor.getColumnIndex(cat_id);
			int i_cat_name = cursor.getColumnIndex("cat_name");
			int i_url = cursor.getColumnIndex("url");
			int i_qty = cursor.getColumnIndex("qty");
			int i = 0 ;
			int count = 0;
			do
			{
				hashMap.put(cat_id, cursor.getString(i_cat_id));
				hashMap.put("cat_name", cursor.getString(i_cat_name));
				hashMap.put("url", cursor.getString(i_url));
				hashMap.put("qty", cursor.getString(i_qty));
				hashMap.put("pos",Integer.toString(count));
				listHashMap.add(hashMap);
				
				Global.productParentAddonsDictionary.put(cursor.getString(i_cat_id), i);
				hashMap = new HashMap<String,String>();
				
				try
				{
					count+=Integer.parseInt(cursor.getString(i_qty));
				}catch(Exception e)
				{
					
				}
				
				i++;
				
			}while(cursor.moveToNext());
		}
		
		cursor.close();
		//db.close();
		return listHashMap;
	}
	
	
	public LinkedHashMap<String,Cursor>getChildAddons(String prodID,List<HashMap<String,String>> addonParentList)
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		
		StringBuilder sb = new StringBuilder();
		int size = addonParentList.size();
		LinkedHashMap<String,Cursor>linkedHashMap = new LinkedHashMap<String,Cursor>();
		
		
		String priceLevelID = new String();
		
		if(myPref.isCustSelected())
			priceLevelID = myPref.getCustPriceLevel();
		else
			priceLevelID = myPref.getEmployeePriceLevel();
		
	
		sb.append("SELECT p.prod_id as '_id',c.cat_name,p.prod_price as 'master_price',vp.price as 'volume_price', ");
		sb.append("ch.over_price_net as 'chain_price',pl.pricelevel_price,p.prod_name,p.prod_desc,p.prod_onhand as 'master_prod_onhand',");
		sb.append("ei.prod_onhand as 'local_prod_onhand',i.prod_img_name, IFNULL(s.taxcode_istaxable,'1') as 'prod_istaxable',p.prod_taxcode,p.prod_taxtype, p.prod_type ");
		sb.append("FROM Product_addons pa LEFT OUTER JOIN Products p ON pa.cat_id = p.cat_id LEFT OUTER JOIN Categories c ON pa.cat_id = c.cat_id ");
		sb.append("LEFT OUTER JOIN EmpInv ei ON ei.prod_id = p.prod_id LEFT OUTER JOIN VolumePrices vp ON p.prod_id = vp.prod_id AND '1' ");
		sb.append("BETWEEN vp.minQty AND vp.maxQty  AND vp.pricelevel_id = '").append(priceLevelID).append("' LEFT OUTER JOIN PriceLevelItems pl ");
		sb.append("ON p.prod_id = pl.pricelevel_prod_id AND pl.pricelevel_id = '").append(priceLevelID).append("' LEFT OUTER JOIN Products_Images i ");
		sb.append("ON p.prod_id = i.prod_id AND i.type = 'I' LEFT OUTER JOIN SalesTaxCodes s ON p.prod_taxcode = s.taxcode_id ");
		sb.append("LEFT OUTER JOIN ProductChainXRef ch ON ch.prod_id = p.prod_id ");
		
		if(myPref.isCustSelected()&&myPref.getPreferences(MyPreferences.pref_filter_products_by_customer))
		{
			sb.append("WHERE p.prod_type != 'Discount' AND ch.cust_chain = ? AND pa.prod_id = '").append(prodID);
		}
		else
		{
			sb.append("AND ch.cust_chain = ? WHERE pa.prod_id = '").append(prodID);
		}
		
		StringBuilder sb2 = new StringBuilder();
		Cursor cursor;
		
		for(int i = 0 ;i < size;i++)
		{
			sb2.append("' AND c.cat_id = '").append(addonParentList.get(i).get(cat_id)).append("' ORDER BY p.prod_name");
			cursor = DBManager._db.rawQuery(sb.toString()+sb2.toString(), new String[]{myPref.getCustID()});
			if(cursor.moveToFirst())
			{
				Cursor tempCursor = cursor;
				linkedHashMap.put(addonParentList.get(i).get(cat_id), tempCursor);
			}

			sb2.setLength(0);
		}
		
		
		//db.close();
		return linkedHashMap;
	}
	
	
	public Cursor getChildAddons2(String prodID,List<HashMap<String,String>> addonParentList)
	{
//		if(db==null||!db.isOpen())
//			db = dbManager.openReadableDB();
		
		StringBuilder sb = new StringBuilder();
		int size = addonParentList.size();
		
		
		String priceLevelID = new String();
		
		if(myPref.isCustSelected())
			priceLevelID = myPref.getCustPriceLevel();
		else
			priceLevelID = myPref.getEmployeePriceLevel();
		
	
		sb.append("SELECT p.prod_id as '_id',c.cat_name,p.prod_price as 'master_price',vp.price as 'volume_price', ");
		sb.append("ch.over_price_net as 'chain_price',pl.pricelevel_price,p.prod_name,p.prod_desc,p.prod_onhand as 'master_prod_onhand',");
		sb.append("ei.prod_onhand as 'local_prod_onhand',i.prod_img_name, IFNULL(s.taxcode_istaxable,'1') as 'prod_istaxable',p.prod_taxcode,p.prod_taxtype, p.prod_type, ");
		sb.append("c.cat_id FROM Product_addons pa LEFT OUTER JOIN Products p ON pa.cat_id = p.cat_id LEFT OUTER JOIN Categories c ON pa.cat_id = c.cat_id ");
		sb.append("LEFT OUTER JOIN EmpInv ei ON ei.prod_id = p.prod_id LEFT OUTER JOIN VolumePrices vp ON p.prod_id = vp.prod_id AND '1' ");
		sb.append("BETWEEN vp.minQty AND vp.maxQty  AND vp.pricelevel_id = '").append(priceLevelID).append("' LEFT OUTER JOIN PriceLevelItems pl ");
		sb.append("ON p.prod_id = pl.pricelevel_prod_id AND pl.pricelevel_id = '").append(priceLevelID).append("' LEFT OUTER JOIN Products_Images i ");
		sb.append("ON p.prod_id = i.prod_id AND i.type = 'I' LEFT OUTER JOIN SalesTaxCodes s ON p.prod_taxcode = s.taxcode_id ");
		sb.append("LEFT OUTER JOIN ProductChainXRef ch ON ch.prod_id = p.prod_id ");
		
		if(myPref.isCustSelected()&&myPref.getPreferences(MyPreferences.pref_filter_products_by_customer))
		{
			sb.append("WHERE p.prod_type != 'Discount' AND ch.cust_chain = '").append(myPref.getCustID()).append("' AND pa.prod_id = '").append(prodID);
		}
		else
		{
			sb.append("AND ch.cust_chain = '").append(myPref.getCustID()).append("' WHERE pa.prod_id = '").append(prodID);
		}
		
		sb.append("' AND c.cat_id IN (");
		
		StringBuilder sb2 = new StringBuilder();
		
		for(int i = 0 ;i < size;i++)
		{
			sb2.append("'").append(addonParentList.get(i).get(cat_id)).append("'");
			if(i+1<size)
				sb2.append(",");
		}
		
		sb.append(sb2.toString()).append(") ORDER BY pa.rest_addons ASC,p.prod_name");
		
		Cursor c = DBManager._db.rawQuery(sb.toString(), null);
		//db.close();
		return c;
	}
	
	public Cursor getSpecificChildAddons(String prodID,String _parent_cat_id)
	{
//		if(db==null||!db.isOpen())
//			db = dbManager.openReadableDB();
		
		StringBuilder sb = new StringBuilder();
		//int size = addonParentList.size();
		
		
		String priceLevelID = new String();
		
		if(myPref.isCustSelected())
			priceLevelID = myPref.getCustPriceLevel();
		else
			priceLevelID = myPref.getEmployeePriceLevel();
		
	
		sb.append("SELECT p.prod_id as '_id',c.cat_name,p.prod_price as 'master_price',vp.price as 'volume_price', ");
		sb.append("ch.over_price_net as 'chain_price',pl.pricelevel_price,p.prod_name,p.prod_desc,p.prod_onhand as 'master_prod_onhand',");
		sb.append("ei.prod_onhand as 'local_prod_onhand',i.prod_img_name, IFNULL(s.taxcode_istaxable,'1') as 'prod_istaxable',p.prod_taxcode,p.prod_taxtype, p.prod_type, ");
		sb.append("c.cat_id FROM Product_addons pa LEFT OUTER JOIN Products p ON pa.cat_id = p.cat_id LEFT OUTER JOIN Categories c ON pa.cat_id = c.cat_id ");
		sb.append("LEFT OUTER JOIN EmpInv ei ON ei.prod_id = p.prod_id LEFT OUTER JOIN VolumePrices vp ON p.prod_id = vp.prod_id AND '1' ");
		sb.append("BETWEEN vp.minQty AND vp.maxQty  AND vp.pricelevel_id = '").append(priceLevelID).append("' LEFT OUTER JOIN PriceLevelItems pl ");
		sb.append("ON p.prod_id = pl.pricelevel_prod_id AND pl.pricelevel_id = '").append(priceLevelID).append("' LEFT OUTER JOIN Products_Images i ");
		sb.append("ON p.prod_id = i.prod_id AND i.type = 'I' LEFT OUTER JOIN SalesTaxCodes s ON p.prod_taxcode = s.taxcode_id ");
		sb.append("LEFT OUTER JOIN ProductChainXRef ch ON ch.prod_id = p.prod_id ");
		
		if(myPref.isCustSelected()&&myPref.getPreferences(MyPreferences.pref_filter_products_by_customer))
		{
			sb.append("WHERE p.prod_type != 'Discount' AND ch.cust_chain = '").append(myPref.getCustID()).append("' AND pa.prod_id = '").append(prodID);
		}
		else
		{
			sb.append("AND ch.cust_chain = '").append(myPref.getCustID()).append("' WHERE pa.prod_id = '").append(prodID);
		}
		
		sb.append("' AND c.cat_id IN (");
		
		StringBuilder sb2 = new StringBuilder();
		
		sb2.append("'").append(_parent_cat_id).append("'");
		
//		for(int i = 0 ;i < size;i++)
//		{
//			sb2.append("'").append(addonParentList.get(i).get(cat_id)).append("'");
//			if(i+1<size)
//				sb2.append(",");
//		}
		
		sb.append(sb2.toString()).append(") ORDER BY pa.rest_addons ASC,p.prod_name");
		
		Cursor c = DBManager._db.rawQuery(sb.toString(), null);
		//db.close();
		return c;
	}
	
	
	
	public String[] getAddonDetails(String parentProdID,String addonProdID)
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		String[] values = new String[2];
		StringBuilder sb = new StringBuilder();
		
		int i = 0;
		
		sb.append("SELECT cat_id FROM Products WHERE prod_id = '").append(addonProdID).append("'");
		
		Cursor c = DBManager._db.rawQuery(sb.toString(), null);
		
		if(c.moveToFirst())
		{
			values[0] = c.getString(0);
			sb.setLength(0);
			
			sb.append("SELECT p.prod_id FROM Product_addons pa LEFT OUTER JOIN Products p ON pa.cat_id = p.cat_id ");
			sb.append("LEFT OUTER JOIN Categories c ON pa.cat_id = c.cat_id WHERE pa.prod_id = '");
			sb.append(parentProdID).append("' AND c.cat_id = '").append(values[0]).append("' ORDER BY p.prod_name");
			
			c = DBManager._db.rawQuery(sb.toString(), null);
			if(c.moveToFirst())
			{
			do
			{
				if(c.getString(0).equals(addonProdID))
				{
					values[1] = Integer.toString(i);
					break;
				}
				i++;
			}while(c.moveToNext());
			
			}
		}
		
		c.close();
		//db.close();
		
		return values;
	}
		
}

