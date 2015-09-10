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
import android.database.sqlite.SQLiteStatement;

public class TermsHandler {

	private final String terms_id = "terms_id";
	private final String terms_name = "terms_name";
	private final String terms_stdduedays = "terms_stdduedays";
	private final String terms_stddiscdays = "terms_stddiscdays";
	private final String terms_discpct = "terms_discpct";
	private final String terms_update = "terms_update";
	private final String isactive = "isactive";

	private final List<String> attr = Arrays.asList(new String[] { terms_id, terms_name, terms_stdduedays, terms_stddiscdays, terms_discpct, isactive,
			terms_update });

	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	private List<String[]> addrData;
	private Activity activity;
	private List<HashMap<String,Integer>>dictionaryListMap;
	private static final String table_name = "Terms";

	public TermsHandler(Activity activity) {
		this.activity = activity;
		attrHash = new HashMap<String, Integer>();
		addrData = new ArrayList<String[]>();
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
			return addrData.get(record)[i];
		}
		return empStr;
	}
	
	private int index(String tag) {
		return attrHash.get(tag);
	}

	
	public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager._db.beginTransaction();
		try {

			addrData = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = addrData.size();

			for (int j = 0; j < size; j++) {
				insert.bindString(index(terms_id), getData(terms_id, j)); // terms_id
				insert.bindString(index(terms_name), getData(terms_name, j)); // terms_name
				insert.bindString(index(terms_stdduedays), getData(terms_stdduedays, j)); // terms_stdduedays
				insert.bindString(index(terms_stddiscdays), getData(terms_stddiscdays, j)); // terms_stddiscdays
				insert.bindString(index(terms_discpct), getData(terms_discpct, j)); // terms_discpct
				insert.bindString(index(terms_update), getData(terms_update, j)); // terms_update
				insert.bindString(index(isactive), getData(isactive, j)); // isactive

				insert.execute();
				insert.clearBindings();

			}
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.TermsHandler (at Class.insert)]");

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
	
	
	public List<String[]> getAllTerms()
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		
		String query = "SELECT terms_name,terms_id FROM Terms WHERE isactive='1' ORDER BY terms_name";
		Cursor cursor = DBManager._db.rawQuery(query, null);
		List<String[]> arrayList = new ArrayList<String[]>();
		String[] arrayValues = new String[2];
		//int i = 0;
		if(cursor.moveToFirst())
		{
			int nameColumnIndex = cursor.getColumnIndex(terms_name);
			int idColumnIndex = cursor.getColumnIndex(terms_id);
			do
			{
				arrayValues[0] = cursor.getString(nameColumnIndex);
				arrayValues[1] = cursor.getString(idColumnIndex);
				arrayList.add(arrayValues);
				arrayValues = new String[2];
			}while(cursor.moveToNext());
		}
		
		cursor.close();
		//db.close();
		return arrayList;
	}
}
