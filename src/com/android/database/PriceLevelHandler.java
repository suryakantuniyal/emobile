package com.android.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.support.DBManager;
import com.android.support.MyPreferences;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import net.sqlcipher.database.SQLiteStatement;

public class PriceLevelHandler {

	public static final List<String> attr = Arrays.asList(new String[] { "pricelevel_id", "pricelevel_name", "pricelevel_type",
			"pricelevel_fixedpct", "pricelevel_update", "isactive" });

	private final String pricelevel_id = "pricelevel_id";
	private final String pricelevel_name = "pricelevel_name";
	private final String pricelevel_type = "pricelevel_type";
	private final String pricelevel_fixedpct = "pricelevel_fixedpct";
	private final String pricelevel_update = "pricelevel_update";
	private final String isactive = "isactive";

	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	private List<String[]> addrData;
	private Activity activity;
	private List<HashMap<String,Integer>>dictionaryListMap;
	private MyPreferences myPref;
	
	public static final String table_name = "PriceLevel";

	public PriceLevelHandler(Activity activity) {
		this.activity = activity;
		attrHash = new HashMap<String, Integer>();
		addrData = new ArrayList<String[]>();
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

				insert.bindString(index(pricelevel_id), getData(pricelevel_id, j)); // pricelevel_id
				insert.bindString(index(pricelevel_name), getData(pricelevel_name, j)); // pricelevel_name
				insert.bindString(index(pricelevel_type), getData(pricelevel_type, j)); // pricelevel_type
				insert.bindString(index(pricelevel_fixedpct), getData(pricelevel_fixedpct, j)); // pricelevel_fixedpct
				insert.bindString(index(pricelevel_update), getData(pricelevel_update, j)); // pricelevel_update
				insert.bindString(index(isactive), getData(isactive, j)); // isactive

				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(Log.getStackTraceString(e));

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

	public List<String[]> getFixedPriceLevel(String prod_id) {
		//SQLiteDatabase db = dbManager.openReadableDB();

		List<String[]> list = new ArrayList<String[]>();
		String[] data = new String[3];

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT pl.pricelevel_name,pl.pricelevel_id,ROUND(((p.prod_price)+(p.prod_price*pl.pricelevel_fixedpct/100)),2) as result FROM Products p,PriceLevel pl WHERE  pl.pricelevel_type = 'FixedPercentage' AND p.prod_id = '");
		sb.append(prod_id).append("'");

		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

		if (cursor.moveToFirst()) {
			do {
				data[0] = cursor.getString(cursor.getColumnIndex(pricelevel_name));
				data[1] = cursor.getString(cursor.getColumnIndex(pricelevel_id));
				data[2] = cursor.getString(cursor.getColumnIndex("result"));
				list.add(data);
				data = new String[3];
			} while (cursor.moveToNext());
		}
		cursor.close();
		
		if(myPref.isCustSelected())
		{
			sb.setLength(0);
			sb.append("SELECT pl.pricelevel_name,pl.pricelevel_id,pli.pricelevel_price as result FROM PriceLevel pl LEFT OUTER JOIN ");
			sb.append("PriceLevelItems pli ON  pli.pricelevel_id = pl.pricelevel_id LEFT OUTER JOIN Products p ON  pli.pricelevel_prod_id = p.prod_id ");
			sb.append("WHERE  p.prod_id = '").append(prod_id).append("'");
			
			cursor = DBManager._db.rawQuery(sb.toString(), null);
			if(cursor.moveToFirst())
			{
				do
				{
					data[0] = cursor.getString(cursor.getColumnIndex(pricelevel_name));
					data[1] = cursor.getString(cursor.getColumnIndex(pricelevel_id));
					data[2] = cursor.getString(cursor.getColumnIndexOrThrow("result"));
					list.add(data);
					data = new String[3];
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		
		
		//db.close();
		return list;
	}

	public List<String[]> getPriceLevel() {

		//SQLiteDatabase db = dbManager.openReadableDB();

		List<String[]> list = new ArrayList<String[]>();
		String[] data = new String[3];
		String[] fields = new String[] { pricelevel_name, pricelevel_id, pricelevel_fixedpct };

		Cursor cursor = DBManager._db.query(true, table_name, fields, null, null, null, null, pricelevel_name, null);

		if (cursor.moveToFirst()) {
			do {

				data[0] = cursor.getString(cursor.getColumnIndex(pricelevel_name));
				data[1] = cursor.getString(cursor.getColumnIndex(pricelevel_id));
				data[2] = cursor.getString(cursor.getColumnIndex(pricelevel_fixedpct));
				list.add(data);
				data = new String[3];
			} while (cursor.moveToNext());
		}

		cursor.close();
		//db.close();
		return list;
	}
}
