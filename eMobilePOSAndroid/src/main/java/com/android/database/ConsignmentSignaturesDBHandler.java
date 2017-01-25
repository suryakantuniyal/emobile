package com.android.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConsignmentSignaturesDBHandler {

	private final String ConsTrans_ID = "ConsTrans_ID";
	private final String encoded_signature = "encoded_signature";
	
	private final List<String>attr = Arrays.asList(ConsTrans_ID,encoded_signature);
	
	private StringBuilder sb1,sb2;
	private final HashMap<String,Integer> attrHash;
	private final String TABLE_NAME = "ConsignmentSignatures";

	public ConsignmentSignaturesDBHandler(Activity activity)
	{
		attrHash = new HashMap<String,Integer>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		new DBManager(activity);
		initDictionary();
	}
	
	private void initDictionary()
	{
		int size = attr.size();
		for (int i = 0; i < size; i++) 
		{
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
	
	public void insert(HashMap<String, String> map) {
		//SQLiteDatabase db = dbManager.openWritableDB();

		ContentValues values = new ContentValues();

		values.put(ConsTrans_ID, map.get(ConsTrans_ID));
		values.put(encoded_signature, map.get(encoded_signature));

		DBManager.getDatabase().insert(TABLE_NAME, null, values);

		//db.close();
	}
	
	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME);
		DBManager.getDatabase().execSQL(sb.toString());
	}
	
	public String getSignature(String _ConsTrans_ID)
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT encoded_signature FROM ").append(TABLE_NAME).append(" WHERE ConsTrans_ID = ?");
		
		Cursor c = DBManager.getDatabase().rawQuery(sb.toString(), new String[]{_ConsTrans_ID});
		
		String value = "";
		if(c.moveToFirst())
		{
			value = c.getString(c.getColumnIndex(encoded_signature));
		}
		
		c.close();
		//db.close();
		
		return value;
		
		
	}
}
