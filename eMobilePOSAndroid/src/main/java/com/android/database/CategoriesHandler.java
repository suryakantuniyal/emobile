package com.android.database;

import android.app.Activity;
import android.database.Cursor;

import com.android.support.Global;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CategoriesHandler {

	private static final String cat_id = "cat_id";
	private static final String cat_name = "cat_name";
	private static final String cat_update = "cat_update";
	private static final String isactive = "isactive";
	private static final String parentID = "parentID";
	private static final String url_icon = "url_icon";

	private final List<String> attr = Arrays.asList(new String[] { cat_id, cat_name, cat_update, isactive, parentID ,url_icon});
	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	
	private List<String[]> catData;
	private MyPreferences myPref;
	private Activity activity;
	private List<HashMap<String,Integer>>dictionaryListMap;
	private static final String table_name = "Categories";

	public CategoriesHandler(Activity activity) {
		this.activity = activity;
		attrHash = new HashMap<String, Integer>();
		catData = new ArrayList<String[]>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		myPref = new MyPreferences(activity);
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

	private String getData(String tag, int record) 
	{
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return catData.get(record)[i];
		}
		return empStr;
	}
	

	private int index(String tag) {
		return attrHash.get(tag);
	}
	
	
	public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager._db.beginTransaction();
		try {

			catData = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = catData.size();
			for (int j = 0; j < size; j++) {

				insert.bindString(index(cat_id), getData(cat_id, j)); // cat_id
				insert.bindString(index(cat_name), getData(cat_name, j)); // cat_name
				insert.bindString(index(cat_update), getData(cat_update, j)); // cat_update
				insert.bindString(index(isactive), getData(isactive, j)); // isactive
				insert.bindString(index(parentID), getData(parentID, j)); // parentID
				insert.bindString(index(url_icon), getData(url_icon, j)); // url_icon

				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.CategoriesHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
			DBManager._db.endTransaction();
		}
	}
	
	

	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_name);
		DBManager._db.execSQL(sb.toString());
	}
	

	public List<String[]> getSubcategories(String name) 
	{
		
		//SQLiteDatabase db = dbManager.openReadableDB();
		
		List<String[]> list = new ArrayList<String[]>();
		String[] data = null;
		String[] fields = new String[] { cat_name, cat_id };
		Cursor cursor = null, cursor2 = null;
		StringBuilder sb = new StringBuilder();

		cursor = DBManager._db.query(true, table_name, fields, "parentID=?", new String[]{name}, null, null, cat_name, null);
		
		
		if (!myPref.getPreferences(MyPreferences.pref_enable_multi_category)) {
			data = new String[2];
		} else {
			data = new String[3];
		}

		if (cursor.moveToFirst()) {
			do {

				data[0] = cursor.getString(cursor.getColumnIndex(cat_name));
				data[1] = cursor.getString(cursor.getColumnIndex(cat_id));
				list.add(data);
				if (!myPref.getPreferences(MyPreferences.pref_enable_multi_category))
					data = new String[2];
				else {

					sb.append("SELECT Count(*) AS count FROM Categories WHERE parentID='").append(data[1]).append("'");
					cursor2 = DBManager._db.rawQuery(sb.toString(), null);
					cursor2.moveToFirst();
					data[2] = cursor2.getString(cursor2.getColumnIndex("count"));
					data = new String[3];
					sb.setLength(0);
					cursor2.close();
				}
			} while (cursor.moveToNext());
		}

		cursor.close();

		//db.close();
		return list;
	}
	
	

	
	public List<String[]> getCategories() {
		//SQLiteDatabase db = dbManager.openReadableDB();

		List<String[]> list = new ArrayList<String[]>();
		String[] data = null;
		String[] fields = new String[] { cat_name, cat_id };
		Cursor cursor = null, cursor2 = null;
		StringBuilder sb = new StringBuilder();

		if(myPref.getPreferences(MyPreferences.pref_enable_multi_category))
			cursor = DBManager._db.query(true, table_name, fields, "parentID='' AND cat_id!=''", null, null, null, cat_name, null);
		else
			cursor = DBManager._db.query(true, table_name, fields, null, null, null, null, cat_name, null);
		
		
		if (!myPref.getPreferences(MyPreferences.pref_enable_multi_category)) {
			data = new String[2];
		} else {
			data = new String[3];
		}

		if (cursor.moveToFirst()) {
			do {

				data[0] = cursor.getString(cursor.getColumnIndex(cat_name));
				data[1] = cursor.getString(cursor.getColumnIndex(cat_id));
				list.add(data);
				if (!myPref.getPreferences(MyPreferences.pref_enable_multi_category))
					data = new String[2];
				else {

					sb.append("SELECT Count(*) AS count FROM Categories WHERE parentID='").append(data[1]).append("'");
					cursor2 = DBManager._db.rawQuery(sb.toString(), null);
					cursor2.moveToFirst();
					data[2] = cursor2.getString(cursor2.getColumnIndex("count"));
					data = new String[3];
					sb.setLength(0);
					cursor2.close();
				}
			} while (cursor.moveToNext());
		}

		cursor.close();

		//db.close();
		return list;
	}

	

	public Cursor getCategoriesCursor()
	{

//		if(!db.isOpen())
//			db = dbManager.openReadableDB();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT cat_id as '_id',cat_name,url_icon,(SELECT Count(*)  FROM Categories c2 WHERE c2.parentID = c1.cat_id) AS num_subcategories FROM Categories c1 ");
		
		if(!Global.cat_id.equals("0"))
			sb.append("  WHERE c1.cat_id ='").append(Global.cat_id).append("' ");
		else if(myPref.getPreferences(MyPreferences.pref_enable_multi_category))
			sb.append("  WHERE c1.parentID='' AND c1.cat_id !='' ");
		
		sb.append(" ORDER BY c1.cat_name");
		
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		cursor.moveToFirst();
		return cursor;
	}
	
	public Cursor getSubcategoriesCursor(String name) 
	{
//		if(!db.isOpen())
//			db = dbManager.openReadableDB();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT cat_id as '_id',cat_name,url_icon,(SELECT Count(*)  FROM Categories c2 WHERE c2.parentID = c1.cat_id) AS num_subcategories FROM Categories c1 ");
		sb.append("  WHERE c1.parentID=? ORDER BY c1.cat_name");
		
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), new String[]{name});
		cursor.moveToFirst();
		return cursor;
	}
	
	
}