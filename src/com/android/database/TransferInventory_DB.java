package com.android.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.emobilepos.holders.TransferInventory_Holder;
import com.android.support.DBManager;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import net.sqlcipher.database.SQLiteStatement;

public class TransferInventory_DB {
	public static final String trans_id = "trans_id";
	public static final String prod_id = "prod_id";
	public static final String prod_qty = "prod_qty";
	
	
	private static final List<String> attr = Arrays.asList(new String[] {trans_id,prod_id,prod_qty});

	private static final String TABLE_NAME = "TransferInventory";
	private StringBuilder sb1, sb2;
	private HashMap<String, Integer> attrHash;
	private Activity activity;
	
	
	public TransferInventory_DB(Activity activity) {
		attrHash = new HashMap<String, Integer>();
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
	
	
	private int index(String tag) 
	{
		return attrHash.get(tag);
	}


	public void insert(List<TransferInventory_Holder> inventory) {
		//SQLiteDatabase db = dbManager.openWritableDB();
		DBManager._db.beginTransaction();
		try {

			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString())
					.append(")");
			insert = DBManager._db.compileStatement(sb.toString());
			
			int size = inventory.size();

			for (int i = 0; i < size; i++) {
				insert.bindString(index(trans_id), inventory.get(i).get(trans_id));
				insert.bindString(index(prod_id),inventory.get(i).get(prod_id));
				insert.bindString(index(prod_qty), inventory.get(i).get(prod_qty));
				
				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager._db.setTransactionSuccessful();

		} catch (Exception e) {
			Tracker tracker = EasyTracker.getInstance(activity);
			tracker.send(MapBuilder.createException(Log.getStackTraceString(e), false).build());
		} finally {
			DBManager._db.endTransaction();
		}
		//db.close();
	}

	
	
	public void emptyTable() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME);
		DBManager._db.execSQL(sb.toString());
	}
	
	
	public Cursor getInventoryTransactions(String _trans_id)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE trans_id = ?");
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), new String[]{_trans_id});
		return cursor;
	}
	
	public List<HashMap<String,String>>getInventoryTransactionMap(String _trans_id)
	{
		StringBuilder sb = new StringBuilder();
		//SQLiteDatabase db = dbManager.openReadableDB();
		sb.append("SELECT p.prod_id, p.prod_name,ti.prod_qty FROM ").append(TABLE_NAME).append(" ti LEFT JOIN Products p ON ti.prod_id = p.prod_id WHERE ");
		sb.append("trans_id = ?");
		
		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[]{_trans_id});
		
		List<HashMap<String,String>>listMap = new ArrayList<HashMap<String,String>>();
		if(c.moveToFirst())
		{
			int i_prod_name = c.getColumnIndex("prod_name");
			int i_prod_qty = c.getColumnIndex("prod_qty");
			int i_prod_id = c.getColumnIndex("prod_id");
			HashMap<String,String>tempMap;
			do
			{
				tempMap = new HashMap<String,String>();
				tempMap.put(prod_id, c.getString(i_prod_id));
				tempMap.put("prod_name", c.getString(i_prod_name));
				tempMap.put("prod_qty", c.getString(i_prod_qty));
				listMap.add(tempMap);
			}while(c.moveToNext());
		}
		
		c.close();
		//db.close();
		return listMap;
		
	}
	
}
