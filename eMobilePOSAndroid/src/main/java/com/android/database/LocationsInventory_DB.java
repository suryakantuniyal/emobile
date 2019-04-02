package com.android.database;

import android.app.Activity;
import android.content.Context;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LocationsInventory_DB {
	public static final String loc_id = "loc_id";
	public static final String prod_id = "prod_id";
	public static final String prod_onhand = "prod_onhand";
	
	private static final List<String> attr = Arrays.asList(loc_id,prod_id,prod_onhand);

	private static final String TABLE_NAME = "LocationsInventory";
	private StringBuilder sb1, sb2;
	private HashMap<String, Integer> attrHash;
	private List<String[]> prodData;
	private List<HashMap<String,Integer>>dictionaryListMap;
	
	
	public LocationsInventory_DB(Context activity) {
		attrHash = new HashMap<>();
		prodData = new ArrayList<>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		new DBManager(activity);
		initDictionary();
	}

	
	private void initDictionary() 
	{
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
	
	

	private String getData(String tag, int record) 
	{
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return prodData.get(record)[i];
		}
		return "";
	}

	
	
	private int index(String tag) 
	{
		return attrHash.get(tag);
	}


	public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager.getDatabase().beginTransaction();
		SQLiteStatement insert = null;
		try {
			prodData = data;
			dictionaryListMap = dictionary;

			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager.getDatabase().compileStatement(sb.toString());

			int size = prodData.size();

			for (int j = 0; j < size; j++) {
				insert.bindString(index(loc_id), getData(loc_id, j));
				insert.bindString(index(prod_id), getData(prod_id, j));
				insert.bindString(index(prod_onhand), getData(prod_onhand, j).isEmpty() ? "0" : getData(prod_onhand, j));

				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager.getDatabase().setTransactionSuccessful();
		} catch (Exception e) {
//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(Log.getStackTraceString(new Exception()), false).build());
		} finally {
			if(insert!=null)
			{
			insert.close();
			}
			DBManager.getDatabase().endTransaction();
		}
	}

	
	
	public void emptyTable() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME);
		DBManager.getDatabase().execSQL(sb.toString());
	}
	
}
