package com.android.database;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;

import com.android.dao.AssignEmployeeDAO;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import util.StringUtil;

public class ProductsAttrHandler
{
	private final String prodAttrKey = "prodAttrKey";
	private final String prod_id = "prod_id";
	private final String attr_id = "attr_id";
	private final String attr_name = "attr_name";
	private final String attr_desc = "attr_desc";
	private final String attr_group = "attr_group";
	private final String attr_group_id = "attr_group_id";
	
	private final List<String> attr = Arrays.asList(prodAttrKey,prod_id,attr_id,attr_name,attr_desc,attr_group,attr_group_id);
	
	private StringBuilder sb1, sb2;
	private HashMap<String,Integer>attrHash;
	private List<String[]>data;
	private List<HashMap<String,Integer>>dictionaryListMap;
	private MyPreferences myPref;
	private final String TABLE_NAME = "ProductsAttr";

	public ProductsAttrHandler(Context activity)
	{
		myPref = new MyPreferences(activity);
		attrHash = new HashMap<>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		new DBManager(activity);
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

	private int index(String tag) {
		return attrHash.get(tag);
	}
	
	
	private String getData(String tag, int record) {
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return data.get(record)[i];
		}
		return "";
	}
	
	
	public void emptyTable() 
	{
		DBManager.getDatabase().execSQL("DELETE FROM " + TABLE_NAME);
	}
	
	
	public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager.getDatabase().beginTransaction();
		try {

			this.data = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert;
            insert = DBManager.getDatabase().compileStatement("INSERT INTO " + TABLE_NAME + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() + ")");

			int size = this.data.size();

			for (int i = 0; i < size; i++) {
				insert.bindString(index(prodAttrKey), getData(prodAttrKey, i));// prodAttrKey
				insert.bindString(index(prod_id), getData(prod_id, i)); // prod_id
				insert.bindString(index(attr_id), getData(attr_id, i)); // attr_id
				insert.bindString(index(attr_name), getData(attr_name, i)); // attr_name
				insert.bindString(index(attr_desc), getData(attr_desc, i)); // attr_desc
				insert.bindString(index(attr_group), getData(attr_group, i)); // attr_group
				insert.bindString(index(attr_group_id), getData(attr_group_id, i)); // attr_group_id
				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager.getDatabase().setTransactionSuccessful();
		} catch (Exception e) {
		} finally {

			DBManager.getDatabase().endTransaction();
		}
	}
	
	
	public LinkedHashMap<String,List<String>> getAttributesMap(String prodName)
	{
		StringBuilder sb = new StringBuilder();
		LinkedHashMap<String,List<String>> linkedMap = new LinkedHashMap<>();
		List<String>tempList = new ArrayList<>();
		sb.setLength(0);
		sb.append("SELECT pa.attr_id as '_id',pa.attr_name,pa.attr_desc " +
				"FROM ProductsAttr pa LEFT OUTER JOIN Products p ON pa.prod_id = p.prod_id ");
		sb.append("WHERE p.prod_name = ? GROUP BY attr_desc ORDER BY pa.attr_name, attr_desc");
		Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), new String[]{prodName});
		if(cursor.moveToFirst())
		{
			int i_attr_desc = cursor.getColumnIndex(attr_desc);
			int i_attr_name = cursor.getColumnIndex(attr_name);
			do
				if (linkedMap.containsKey(cursor.getString(i_attr_name))) {
					tempList = linkedMap.get(cursor.getString(i_attr_name));
					tempList.add(cursor.getString(i_attr_desc));
					linkedMap.put(cursor.getString(i_attr_name), tempList);
					tempList = new ArrayList<>();
				} else {
					tempList.add(cursor.getString(i_attr_desc));
					linkedMap.put(cursor.getString(i_attr_name), tempList);
					tempList = new ArrayList<>();
				} while(cursor.moveToNext());
		}
		cursor.close();
		return linkedMap;
	}
	
	public LinkedHashMap<String,String>getDefaultAttributes(String prod_id)
	{
		StringBuilder sb = new StringBuilder();
		LinkedHashMap<String,String> linkedMap = new LinkedHashMap<>();
		sb.setLength(0);
		sb.append("SELECT pa.prod_id as '_id', pa.prodAttrKey,pa.attr_id,pa.attr_name,pa.attr_desc FROM ProductsAttr pa ");
		sb.append("LEFT OUTER JOIN Products p ON pa.prod_id = p.prod_id WHERE p.prod_id = '").append(prod_id).append("' ORDER BY pa.attr_name, attr_desc");
		
		Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);
		if(cursor.moveToFirst())
		{
			int i_attr_desc = cursor.getColumnIndex(attr_desc);
			int i_attr_name = cursor.getColumnIndex(attr_name);
			do
			{
				linkedMap.put(cursor.getString(i_attr_name), cursor.getString(i_attr_desc));
			}while(cursor.moveToNext());
		}
		
		cursor.close();
		//db.close();
		
		return linkedMap;
	}
	
	
	
	public Cursor getNewAttributeProduct(String prod_name,String[] attributesKey, LinkedHashMap<String,String>attributesMap)
	{
		Cursor cursor;
		StringBuilder sb_1 = new StringBuilder();
		sb_1.append("SELECT pa.prod_id, pa.prodAttrKey,pa.attr_id,pa.attr_name,pa.attr_desc,Count(*) as 'count' ");
		sb_1.append("FROM ProductsAttr pa LEFT OUTER JOIN Products p ON pa.prod_id = p.prod_id WHERE p.prod_name = ? AND pa.attr_desc IN (");
		
		int size = attributesKey.length;
		String[] param = new String[size+1];
		param[0] = prod_name;
		for(int i = 0 ; i < size;i++)
		{
			if(i+1>=size)
				sb_1.append("?)");
			else
				sb_1.append("?,");
			
			param[i+1] = attributesMap.get(attributesKey[i]);
		}
		
		sb_1.append(" GROUP BY pa.prod_id ORDER BY count DESC");
		
		cursor = DBManager.getDatabase().rawQuery(sb_1.toString(), param);
		
		if(cursor.moveToFirst())
		{
			String prodID = cursor.getString(cursor.getColumnIndex("prod_id"));
			sb_1.setLength(0);
			
			String priceLevelID;
			if(myPref.isCustSelected())
				priceLevelID = myPref.getCustPriceLevel();
			else {
				AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
				priceLevelID = StringUtil.nullStringToEmpty(assignEmployee.getPricelevelId());
			}

			
			sb_1.append("SELECT  p.prod_id as '_id',p.prod_price as 'master_price',vp.price as 'volume_price', ch.over_price_net as 'chain_price',");
			sb_1.append("pl.pricelevel_price,p.prod_name,p.prod_desc,p.prod_onhand as 'master_prod_onhand',ei.prod_onhand as 'local_prod_onhand',i.prod_img_name, IFNULL(s.taxcode_istaxable,'1') as 'prod_istaxable' ");
			sb_1.append(",p.prod_type,p.prod_price_points,p.prod_value_points FROM Products p  LEFT OUTER JOIN EmpInv ei ON ei.prod_id = p.prod_id LEFT OUTER JOIN VolumePrices vp ON p.prod_id = vp.prod_id AND '1' BETWEEN vp.minQty AND vp.maxQty  AND ");
			sb_1.append("vp.pricelevel_id = ? LEFT OUTER JOIN PriceLevelItems pl ON p.prod_id = pl.pricelevel_prod_id ");
			sb_1.append("AND pl.pricelevel_id = ? LEFT OUTER JOIN Products_Images i ON p.prod_id = i.prod_id AND i.type = 'I' ");
			sb_1.append("LEFT OUTER JOIN SalesTaxCodes s ON p.prod_taxcode = s.taxcode_id LEFT OUTER JOIN ProductChainXRef ch ON ch.prod_id = p.prod_id ");
			sb_1.append("AND ch.cust_chain = ? WHERE p.prod_type != 'Discount' AND p.prod_id = '");
			sb_1.append(prodID).append("'");
			
			cursor.close();
			cursor = DBManager.getDatabase().rawQuery(sb_1.toString(), new String[]{priceLevelID,priceLevelID,myPref.getCustID()});
			cursor.moveToFirst();
		}
		
		//db.close();
		
		return cursor;
	}
	
}
