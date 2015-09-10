package com.android.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.emobilepos.ordering.OrdProdAttrHolder;
import com.android.support.DBManager;
import com.android.support.Global;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

public class OrdProdAttrList_DB {

	public final static String Prod_id = "Prod_id";
	public final static String Attrid = "Attrid";
	public final static String ordprod_attr_name = "ordprod_attr_name";
	public final static String required = "required";
	
	
	private final List<String> attr = Arrays.asList(new String[] { Prod_id,Attrid,ordprod_attr_name,required});
	private HashMap<String, Integer> attrHash;
	private List<HashMap<String,Integer>>dictionaryListMap;
	private List<String[]>data;
	private StringBuilder mainSB1,mainSB2;
	private final String empStr = "";
	private static final String TABLE_NAME = "OrdProdAttrList";
	private Activity activity;
	private Global global;
	
	public OrdProdAttrList_DB(Activity activity)
	{
		this.activity = activity;
		attrHash = new HashMap<String,Integer>();
		
		global = (Global)activity.getApplication();
		mainSB1 = new StringBuilder();
		mainSB2 = new StringBuilder();
		
		initDictionary();
	}
	private void initDictionary() {
		int size = attr.size();
		for (int i = 0; i < size; i++) {
			attrHash.put(attr.get(i), i + 1);
			if ((i + 1) < size) {
				mainSB1.append(attr.get(i)).append(",");
				mainSB2.append("?").append(",");
			} else {
				mainSB1.append(attr.get(i));
				mainSB2.append("?");
			}
		}
	}

	private String getData(String tag, int record) {
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return data.get(record)[i];
		}
		return empStr;
	}
	
	private int index(String tag) {
		return attrHash.get(tag);
	}
	

	public void insert(List<String[]> _data, List<HashMap<String, Integer>> dictionary) {
		DBManager._db.beginTransaction();

		try {
			data = _data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(mainSB1.toString()).append(") ").append("VALUES (").append(mainSB2.toString())
					.append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = data.size();

			for (int j = 0; j < size; j++) {
				insert.bindString(index(Prod_id), getData(Prod_id, j));
				insert.bindString(index(Attrid), getData(Attrid, j));
				insert.bindString(index(ordprod_attr_name), getData(ordprod_attr_name, j));
				insert.bindString(index(required), getData(required, j));

				insert.execute();
				insert.clearBindings();
			}
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.OrdProdAttrList_DB (at Class.insert)]");

			Tracker tracker = EasyTracker.getInstance(activity);
			tracker.send(MapBuilder.createException(sb.toString(), false).build());
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
	
	
	public String getRequiredOrdAttr(String prodID)
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();
		
		global.ordProdAttrPending = new HashMap<String,String>();
		sb.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE Prod_id = ? AND required = 'true'");
		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[]{prodID});
		
		sb.setLength(0);
		if(c.moveToFirst())
		{
			int i_ordprodattr_id = c.getColumnIndex("ordprodattr_id");
			int i_ordprod_attr_name = c.getColumnIndex(ordprod_attr_name);
			do
			{
				global.ordProdAttrPending.put(c.getString(i_ordprodattr_id), c.getString(i_ordprod_attr_name));
				sb.append(c.getString(i_ordprod_attr_name)).append("\n");
			}while(c.moveToNext());
		}
		
		c.close();
		//db.close();
		return sb.toString();
	}
	
	public ArrayList<OrdProdAttrHolder>getRequiredOrdAttrList(String prodID)
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();
		ArrayList<OrdProdAttrHolder>listAttr = new ArrayList<OrdProdAttrHolder>();
		
		sb.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE Prod_id = ? ORDER BY required");
		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[]{prodID});
		
		if(c.moveToFirst())
		{
			OrdProdAttrHolder tempHolder = new OrdProdAttrHolder();
			int i_ordprodattr_id = c.getColumnIndex("ordprodattr_id");
			int i_attrid = c.getColumnIndex(Attrid);
			int i_attr_name = c.getColumnIndex(ordprod_attr_name);
			int i_required = c.getColumnIndex(required);
			do
			{
				tempHolder.ordprodattr_id = c.getString(i_ordprodattr_id);
				tempHolder.Attrid = c.getString(i_attrid);
				tempHolder.ordprod_attr_name = c.getString(i_attr_name);
				tempHolder.required = Boolean.parseBoolean(c.getString(i_required));
				listAttr.add(tempHolder);
				tempHolder = new OrdProdAttrHolder();
			}while(c.moveToNext());
		}
		
		c.close();
		//db.close();
		return listAttr;
	}
	
	
}
