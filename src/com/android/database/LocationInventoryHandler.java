package com.android.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;

import android.app.Activity;

import com.android.support.MyPreferences;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;

public class LocationInventoryHandler 
{
	private final String TABLE_NAME = "LocationInventory";
	
	private final String emp_inv_id = "emp_inv_id";
	private final String emp_id = "emp_id";
	private final String loc_id = "loc_id";
	private final String prod_id = "prod_id";
	private final String prod_onhand = "prod_onhand";
	private final String emp_update = "emp_update";
	private final String issync = "issync";
	
	private final List<String>attr = Arrays.asList(new String[]{emp_inv_id,emp_id,loc_id,prod_id,prod_onhand,
			emp_update,issync});
	
	private StringBuilder sb1,sb2;
	private HashMap<String,Integer>attrHash;
	private MyPreferences myPref;
	private Activity activity;
	private List<String[]>inventoryData;
	private List<HashMap<String,Integer>>dictionaryListMap;
	
	public LocationInventoryHandler(Activity activity)
	{
		myPref = new MyPreferences(activity);
		attrHash = new HashMap<String,Integer>();
		inventoryData = new ArrayList<String[]>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		this.activity = activity;
		
		initDictionary();
	}
	
	private void initDictionary()
	{
		int size = attr.size();
		
		for(int i = 0; i < size; i++)
		{
			attrHash.put(attr.get(i), i+1);
			if((i+1)<size)
			{
				sb1.append(attr.get(i)).append(",");
				sb2.append("?").append(",");
			}
			else
			{
				sb1.append(attr.get(i));
				sb2.append("?");
			}
		}
	}
	
	private String getData(String tag, int record)
	{
		Integer i = dictionaryListMap.get(record).get(tag);
		
		if(i != null)
			return inventoryData.get(record)[i];
		return "";
	}
	
	private int index(String tag)
	{
		return attrHash.get(tag);
	}
	
	public void insert(SQLiteDatabase db, List<String[]>data,List<HashMap<String,Integer>>dictionary)
	{
		db.beginTransaction();
		try
		{
			inventoryData = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(sb1.toString()).append(") ");
			sb.append("VALUES (").append(sb2.toString()).append(")");
			int size = inventoryData.size();
			
			for(int i = 0 ; i < size; i++)
			{
				insert.bindString(index(emp_inv_id), getData(emp_inv_id,i));
				insert.bindString(index(emp_id), getData(emp_id,i));
				insert.bindString(index(loc_id), getData(loc_id,i));
				insert.bindString(index(prod_id), getData(prod_id,i));
				insert.bindString(index(prod_onhand), getData(prod_onhand,i));
				insert.bindString(index(emp_update), getData(emp_update,i));
				insert.bindString(index(issync), getData(issync,i));
				
				insert.execute();
				insert.clearBindings();
			}
			
		}
		catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.UOMHandler (at Class.insert)]");
			
			EasyTracker.getInstance().setContext(activity);
			Tracker myTracker = EasyTracker.getTracker(); // Get a reference to tracker.
			myTracker.sendException(sb.toString(), false); // false indicates non-fatal exception.
		} finally {
			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}
	
	public void emptyTable(SQLiteDatabase db) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME);
		db.execSQL(sb.toString());
	}
}
