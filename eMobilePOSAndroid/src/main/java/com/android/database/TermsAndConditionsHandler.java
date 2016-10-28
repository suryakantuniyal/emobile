package com.android.database;

import android.app.Activity;

import net.sqlcipher.database.SQLiteStatement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TermsAndConditionsHandler 
{
	private final String tc_id = "tc_id";
	private final String tc_term = "tc_term";
	private final String loc_id = "loc_id";
	
	private final List<String> attr = Arrays.asList(tc_id,tc_term,loc_id);
	
	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String,Integer>attrHash;
	private List<String[]>data;
	private List<HashMap<String,Integer>>dictionaryListMap;
	private final String TABLE_NAME = "TermsAndConditions";
	private Activity activity;

	public TermsAndConditionsHandler(Activity activity)
	{
		this.activity = activity;
		attrHash = new HashMap<String,Integer>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		new DBManager(activity);
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

	private int index(String tag) {
		return attrHash.get(tag);
	}
	
	
	private String getData(String tag, int record) {
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return data.get(record)[i];
		}
		return empStr;
	}
	
	public void emptyTable() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME);
		DBManager.getDatabase().execSQL(sb.toString());
	}
	
	
	public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager.getDatabase().beginTransaction();
		try {

			this.data = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager.getDatabase().compileStatement(sb.toString());

			int size = this.data.size();

			for (int i = 0; i < size; i++) {
				insert.bindString(index(tc_id), getData(tc_id, i)); // tc_id
				insert.bindString(index(tc_term), getData(tc_term, i)); // tc_terms
				insert.bindString(index(loc_id), getData(loc_id, i)); // loc_id

				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager.getDatabase().setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.TermsAndConditionsHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
			DBManager.getDatabase().endTransaction();
		}
	}
}
