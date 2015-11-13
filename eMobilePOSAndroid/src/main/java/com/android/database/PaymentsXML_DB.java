package com.android.database;

import android.app.Activity;
import android.database.Cursor;

import com.android.support.DBManager;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PaymentsXML_DB {

	public static final String app_id = "app_id";
	public static final String payment_xml = "payment_xml";
	
	private final List<String> attr = Arrays.asList(new String[] {app_id,payment_xml});
	private HashMap<String, Integer> attrHash;
	private StringBuilder mainSB1,mainSB2;
	private static final String TABLE_NAME = "PaymentsXML";
	private Activity activity;
	
	public PaymentsXML_DB(Activity activity)
	{
		this.activity = activity;
		attrHash = new HashMap<String,Integer>();
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
	
	private int index(String tag) {
		return attrHash.get(tag);
	}
	

	public void insert(HashMap<String,String> _data) {
		//SQLiteDatabase db = dbManager.openWritableDB();

		DBManager._db.beginTransaction();

		try {
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(mainSB1.toString()).append(") ").append("VALUES (")
					.append(mainSB2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());
			
			insert.bindString(index(app_id), _data.get(app_id));
			insert.bindString(index(payment_xml), _data.get(payment_xml));
			
			insert.execute();
			insert.clearBindings();
			insert.close();
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(Log.getStackTraceString(e), false).build());
		} finally {
			DBManager._db.endTransaction();
		}
	}
	
	public void emptyTable(SQLiteDatabase db) 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME);
		db.execSQL(sb.toString());
	}
	
	public void deleteRow(String _app_id)
	{
		//SQLiteDatabase db = dbManager.openWritableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME).append(" WHERE app_id = ?");
		
		DBManager._db.execSQL(sb.toString(),new String[]{_app_id});
		//db.close();
	}
	
	public Cursor getReversePayments()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(TABLE_NAME);
		
		Cursor c = DBManager._db.rawQuery(sb.toString(), null);
		c.moveToFirst();
		return c;
	}
}
