package com.android.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.support.DBManager;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.util.Log;
import net.sqlcipher.database.SQLiteStatement;

public class LocationsInventory_DB {
	public static final String loc_id = "loc_id";
	public static final String prod_id = "prod_id";
	public static final String prod_onhand = "prod_onhand";
	
	private static final List<String> attr = Arrays.asList(new String[] {loc_id,prod_id,prod_onhand});

	private static final String TABLE_NAME = "LocationsInventory";
	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	private List<String[]> prodData;
	private Activity activity;
	private List<HashMap<String,Integer>>dictionaryListMap;
	
	
	public LocationsInventory_DB(Activity activity) {
		attrHash = new HashMap<String, Integer>();
		prodData = new ArrayList<String[]>();
		this.activity = activity;
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
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
		return empStr;
	}

	
	
	private int index(String tag) 
	{
		return attrHash.get(tag);
	}


	public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager._db.beginTransaction();

		try {
			prodData = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = prodData.size();

			for (int j = 0; j < size; j++) {
				insert.bindString(index(loc_id), getData(loc_id, j));
				insert.bindString(index(prod_id), getData(prod_id, j));
				insert.bindString(index(prod_onhand), getData(prod_onhand, j).isEmpty() ? "0" : getData(prod_onhand, j));

				insert.execute();
				insert.clearBindings();
			}
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			Tracker tracker = EasyTracker.getInstance(activity);
			tracker.send(MapBuilder.createException(Log.getStackTraceString(new Exception()), false).build());
		} finally {
			DBManager._db.endTransaction();
		}
	}

	
	
	public void emptyTable() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME);
		DBManager._db.execSQL(sb.toString());
	}
	
}
