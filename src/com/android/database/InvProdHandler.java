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

public class InvProdHandler {

	private final String ordprod_id = "ordprod_id";
	private final String prod_id = "prod_id";
	private final String ord_id = "ord_id";
	private final String ordprod_qty = "ordprod_qty";
	private final String overwrite_price = "overwrite_price";
	private final String reason_id = "reason_id";
	private final String ordprod_desc = "ordprod_desc";
	private final String pricelevel_id = "pricelevel_id";
	private final String prod_seq = "prod_seq";
	private final String uom_name = "uom_name";
	private final String uom_conversion = "uom_conversion";

	private final List<String> attr = Arrays.asList(new String[] { ordprod_id, prod_id, ord_id, ordprod_qty, overwrite_price, reason_id, ordprod_desc,
			pricelevel_id, prod_seq, uom_name, uom_conversion });

	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	
	private List<String[]> addrData;
	private Activity activity;
	private List<HashMap<String,Integer>>dictionaryListMap;
	
	public static final String table_name = "InvProducts";

	public InvProdHandler(Activity activity) {
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

	private String getData(String tag, int record) 
	{
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
				insert.bindString(index(ordprod_id), getData(ordprod_id, j)); // ordprod_id
				insert.bindString(index(prod_id), getData(prod_id, j)); // prod_id
				insert.bindString(index(ord_id), getData(ord_id, j)); // ord_id
				insert.bindString(index(ordprod_qty), getData(ordprod_qty, j)); // ordprod_qty
				insert.bindString(index(overwrite_price), getData(overwrite_price, j)); // overwrite_price
				insert.bindString(index(reason_id), getData(reason_id, j)); // reason_id
				insert.bindString(index(ordprod_desc), getData(ordprod_desc, j)); // ordprod_desc
				insert.bindString(index(pricelevel_id), getData(pricelevel_id, j)); // pricelevel_id
				insert.bindString(index(prod_seq), getData(prod_seq, j)); // prod_seq
				insert.bindString(index(uom_name), getData(uom_name, j)); // uom_name
				insert.bindString(index(uom_conversion), getData(uom_conversion, j)); // uom_conversion

				insert.execute();
				insert.clearBindings();
			}
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.InvProdHandler (at Class.insert)]");

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
	
	
	public List<String[]> getInvProd(String invID)
	{
		//SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT p.prod_name,i.ordprod_desc,i.ordprod_qty,i.overwrite_price,ROUND(i.ordprod_qty*i.overwrite_price,2) as 'total', im.prod_img_name FROM InvProducts i,Products p LEFT OUTER JOIN " +
				"Products_Images im ON i.prod_id = im.prod_id WHERE p.prod_id = i.prod_id AND  i.ord_id = ?");
		
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), new String[]{invID});
		
		List<String[]> arrayList = new ArrayList<String[]>();
		
		String[] arrayVal = new String[6];
		if(cursor.moveToFirst())
		{
			do
			{
				arrayVal[0] = cursor.getString(cursor.getColumnIndex("prod_name"));
				arrayVal[1] = cursor.getString(cursor.getColumnIndex(ordprod_desc));
				arrayVal[2] = cursor.getString(cursor.getColumnIndex(ordprod_qty));
				arrayVal[3] = cursor.getString(cursor.getColumnIndex(overwrite_price));
				arrayVal[4] = cursor.getString(cursor.getColumnIndex("prod_img_name"));
				arrayVal[5] = cursor.getString(cursor.getColumnIndex("total"));
				arrayList.add(arrayVal);
				arrayVal = new String[6];
			}while(cursor.moveToNext());
		}
		
		cursor.close();
		//db.close();
		
		return arrayList;

	}
	
	
}
