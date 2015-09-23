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
import android.database.Cursor;
import net.sqlcipher.database.SQLiteStatement;

public class ProductsImagesHandler {

	private static final String img_id = "img_id";
	private static final String prod_id = "prod_id";
	private static final String prod_img_name = "prod_img_name";
	private static final String prod_default = "prod_default";
	private static final String type = "type";

	private static final List<String> attr = Arrays.asList(new String[] { img_id, prod_id, prod_img_name, prod_default, type });

	private final String table_name = "Products_Images";
	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	private List<String[]> prodData;
	private Activity activity;
	private List<HashMap<String,Integer>>dictionaryListMap;
	
	public ProductsImagesHandler(Activity activity) {
		this.activity = activity;
		attrHash = new HashMap<String, Integer>();
		prodData = new ArrayList<String[]>();
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
			return prodData.get(record)[i];
		}
		return empStr;
	}

	
	private int index(String tag) {
		return attrHash.get(tag);
	}

	
	public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager._db.beginTransaction();

		try {

			prodData = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = prodData.size();

			for (int j = 0; j < size; j++) {
				insert.bindString(index(prod_id), getData(prod_id, j)); // prod_id
				insert.bindString(index(prod_img_name), getData(prod_img_name, j)); // prod_img_name
				insert.bindString(index(prod_default), getData(prod_default, j)); // prod_default
				insert.bindString(index(type), getData(type, j)); // type
				insert.bindString(index(img_id), getData(img_id, j)); // img_id

				insert.execute();
				insert.clearBindings();

			}
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.ProductsImagesHandler (at Class.insert)]");

			Tracker tracker = EasyTracker.getInstance(activity);
			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
			DBManager._db.endTransaction();
		}
	}
	
	
	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_name);
		DBManager._db.execSQL(sb.toString());
	}

	public List<String> getColumn(String tag) {
		//SQLiteDatabase db = dbManager.openReadableDB();
		List<String> list = new ArrayList<String>();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ").append(tag).append(" FROM ").append(table_name);

		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

		if (cursor.moveToFirst()) {
			do {

				String data = cursor.getString(cursor.getColumnIndex(tag));
				list.add(data);
			} while (cursor.moveToNext());
		}
		// db.close();
		cursor.close();
		//db.close();
		return list;
	}

	public HashMap<String, String> getLinks(String type) {
		//SQLiteDatabase db = dbManager.openReadableDB();
		HashMap<String, String> hash = new HashMap<String, String>();
		String[] fields = new String[] { prod_id, prod_img_name };
		String[] arguments = new String[] { type };

		Cursor cursor = DBManager._db.query(true, table_name, fields, "type=?", arguments, null, null, null, null);

		if (cursor.moveToFirst()) {
			do {
				String key = cursor.getString(cursor.getColumnIndex(prod_id));
				String value = cursor.getString(cursor.getColumnIndex(prod_img_name));
				hash.put(key, value);

			} while (cursor.moveToNext());
		}
		cursor.close();
		//db.close();
		return hash;
	}

	public String getSpecificLink(String type, String prodID) {
		//SQLiteDatabase db = dbManager.openReadableDB();
		String link = empStr;

		String[] fields = new String[] { prod_img_name };
		String[] arguments = new String[] { type, prodID };

		Cursor cursor = DBManager._db.query(true, table_name, fields, "type=? AND prod_id=?", arguments, null, null, null, null);

		if (cursor.moveToFirst()) {
			do {

				link = cursor.getString(cursor.getColumnIndex(prod_img_name));
			} while (cursor.moveToNext());
		}

		cursor.close();
		//db.close();
		return link;
	}
	
	public void getAllImages()
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		
		

		Cursor cursor = DBManager._db.rawQuery("SELECT prod_img_name FROM Products_Images WHERE type='I'", null);
		
		String[] links = new String[cursor.getCount()];
		int i = 0;

		if (cursor.moveToFirst()) {
			do {

				links[i] = "\""+cursor.getString(cursor.getColumnIndex("prod_img_name"))+"\"";
				i++;
			} while (cursor.moveToNext());
		}

		cursor.close();
		//db.close();
	}
}
