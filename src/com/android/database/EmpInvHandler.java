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
import net.sqlcipher.database.SQLiteStatement;



public class EmpInvHandler {
	

	private final String emp_inv_id = "emp_inv_id";
	private final String emp_id = "emp_id";
	private final String prod_id = "prod_id";
	private final String prod_onhand = "prod_onhand";
	private final String emp_update = "emp_update";
	private final String issync = "issync";
	private final String loc_id = "loc_id";
	
	private final List<String> attr = Arrays.asList(new String[] {emp_inv_id,emp_id,prod_id,prod_onhand,emp_update,issync,loc_id});
	
	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	
	private List<String[]> data;
	private Activity activity;
	private List<HashMap<String,Integer>>dictionaryListMap;
	private static final String TABLE_NAME = "EmpInv";

	public EmpInvHandler(Activity activity) {
		this.activity = activity;
		attrHash = new HashMap<String, Integer>();
		data = new ArrayList<String[]>();
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
			return data.get(record)[i];
		}
		return empStr;
	}
	

	private int index(String tag) 
	{
		return attrHash.get(tag);
	}
	
	
	
	public void insert(List<String[]> insertData, List<HashMap<String, Integer>> dictionary) {
		DBManager._db.beginTransaction();
		try {

			data = insertData;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = data.size();

			for (int j = 0; j < size; j++) {
				insert.bindString(index(emp_inv_id), getData(emp_inv_id, j)); // emp_inv_id
				insert.bindString(index(emp_id), getData(emp_id, j)); // emp_id
				insert.bindString(index(prod_id), getData(prod_id, j)); // prod_id
				insert.bindString(index(emp_update), getData(emp_update, j)); // emp_update
				insert.bindString(index(prod_onhand), getData(prod_onhand, j)); // prod_onhand
				insert.bindString(index(issync), getData(issync, j)); // issync
				insert.bindString(index(loc_id), getData(loc_id, j)); // loc_id

				insert.execute();
				insert.clearBindings();

			}
			insert.close();
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.EmpInvHandlerHandler (at Class.insert)]");

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
	
	
	public void updateOnHand(String prodID,String quantity,boolean increment)
	{
		//SQLiteDatabase db = dbManager.openWritableDB();
		StringBuilder sb = new StringBuilder();
		
		sb.append("UPDATE ").append(TABLE_NAME).append(" SET ").append(prod_onhand).append(" = prod_onhand");
		
		if(increment)
			sb.append("+").append(quantity).append(" WHERE ").append(prod_id).append(" = '").append(prodID).append("'");
		else
			sb.append("-").append(quantity).append(" WHERE ").append(prod_id).append(" = '").append(prodID).append("'");
		
		DBManager._db.execSQL(sb.toString());
		//db.close();
	}

}