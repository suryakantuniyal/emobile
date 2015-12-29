package com.android.database;

import android.app.Activity;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UOMHandler 
{
	private final String TABLE_NAME = "UOM";
	private String uomitem_id = "uomitem_id";
	private String uom_id = "uom_id";
	private String uom_name = "uom_name";
	private String prod_id = "prod_id";
	private String uom_conversion = "uom_conversion";
	private String uom_update = "uom_update";
	private String isactive = "isactive";
	
	
	private final List<String> attr = Arrays.asList(uomitem_id,uom_id,uom_name,prod_id,uom_conversion,uom_update,isactive);

	private StringBuilder sb1, sb2;
	private HashMap<String, Integer> attrHash;
	private List<String[]> uomData;
	private List<HashMap<String,Integer>>dictionaryListMap;
	
	
	public UOMHandler(Activity activity)
	{
		attrHash = new HashMap<String, Integer>();
		uomData = new ArrayList<String[]>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
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
	
	private String getData(String tag, int record) {
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return uomData.get(record)[i];
		}
		return "";
	}

	private int index(String tag) {
		return attrHash.get(tag);
	}
	
	
	public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager._db.beginTransaction();
		try {

			uomData = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = uomData.size();

			for (int j = 0; j < size; j++) {
				insert.bindString(index(uomitem_id), getData(uomitem_id, j)); // uomtime_id
				insert.bindString(index(uom_id), getData(uom_id, j)); // uom_id
				insert.bindString(index(uom_name), getData(uom_name, j)); // uom_name
				insert.bindString(index(prod_id), getData(prod_id, j)); // prod_id
				insert.bindString(index(uom_conversion), getData(uom_conversion, j)); // uom_conversion
				insert.bindString(index(uom_update), getData(uom_update, j)); // uom_update
				insert.bindString(index(isactive), getData(isactive, j)); // isactive

				insert.execute();
				insert.clearBindings();

			}
			insert.close();
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.UOMHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {

			DBManager._db.endTransaction();
		}
	}

	public List<String[]> getUOMList(String prodID) {
		//SQLiteDatabase db = dbManager.openReadableDB();
		List<String[]> list = new ArrayList<String[]>();
		String[] data = new String[3];
		String[] fields = new String[] { uom_name, uom_id, uom_conversion };
		String[] arguments = new String[] { prodID };
		Cursor cursor = DBManager._db.query(true, TABLE_NAME, fields, "prod_id=?",arguments, null, null, uom_name, null);

		if (cursor.moveToFirst()) {
			do {

				data[0] = cursor.getString(cursor.getColumnIndex(uom_name));
				data[1] = cursor.getString(cursor.getColumnIndex(uom_id));
				data[2] = cursor.getString(cursor.getColumnIndex(uom_conversion));
				list.add(data);
				data = new String[3];
			} while (cursor.moveToNext());
		}

		cursor.close();
		//db.close();
		return list;
	}
	
	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME);
		DBManager._db.execSQL(sb.toString());
	}
}
