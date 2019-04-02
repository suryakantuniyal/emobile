package com.android.database;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PaymentsXML_DB {

	public static final String app_id = "app_id";
	public static final String payment_xml = "payment_xml";
	public static final String charge_xml = "chargeXml";
	
	private final List<String> attr = Arrays.asList(app_id,payment_xml);
	private HashMap<String, Integer> attrHash;
	private StringBuilder mainSB1,mainSB2;
	private static final String TABLE_NAME = "PaymentsXML";

	public PaymentsXML_DB(Context activity)
	{
		attrHash = new HashMap<>();
		mainSB1 = new StringBuilder();
		mainSB2 = new StringBuilder();
		new DBManager(activity);
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
		SQLiteStatement insert = null;
		DBManager.getDatabase().beginTransaction();

		try {

			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(mainSB1.toString()).append(") ").append("VALUES (")
					.append(mainSB2.toString()).append(")");
			insert = DBManager.getDatabase().compileStatement(sb.toString());
			
			insert.bindString(index(app_id), _data.get(app_id));
			insert.bindString(index(payment_xml), _data.get(payment_xml));
			
			insert.execute();
			insert.clearBindings();
			//insert.close();
			DBManager.getDatabase().setTransactionSuccessful();
		} catch (Exception e) {
//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(Log.getStackTraceString(e), false).build());
		} finally {
			if(insert!=null) {
				insert.close();
			}
			DBManager.getDatabase().endTransaction();

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
		
		DBManager.getDatabase().execSQL(sb.toString(),new String[]{_app_id});
		//db.close();
	}
	
	public Cursor getReversePayments()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(TABLE_NAME);
		
		Cursor c = DBManager.getDatabase().rawQuery(sb.toString(), null);
		c.moveToFirst();
		return c;
	}
}
