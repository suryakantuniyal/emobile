package com.android.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.support.DBManager;
import com.android.support.Global;

import android.app.Activity;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteStatement;

public class ShipMethodHandler {

	private final String shipmethod_id = "shipmethod_id";
	private final String shipmethod_name = "shipmethod_name";
	private final String isactive = "isactive";
	private final String shipmethod_update = "shipmethod_update";

	public final List<String> attr = Arrays.asList(new String[] { shipmethod_id, shipmethod_name, isactive, shipmethod_update });

	public StringBuilder sb1, sb2;
	public final String empStr = "";
	public HashMap<String, Integer> attrHash;
	public Global global;
	private List<String[]> addrData;

	private Activity activity;
	
	private List<String>dataList;
	private HashMap<String,Integer>dictionaryMap;
	private List<HashMap<String,Integer>>dictionaryListMap;
	
	public static final String table_name = "ShipMethod";

	public ShipMethodHandler(Activity activity) {
		global = (Global) activity.getApplication();
		this.activity = activity;
		attrHash = new HashMap<String, Integer>();
		addrData = new ArrayList<String[]>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		initDictionary();
	}

	public void initDictionary() {
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

	public String getData(String tag, int record) {
		/*Integer i = global.dictionary.get(record).get(tag);
		if (i != null) {
			return addrData.get(record)[i];
		}
		return empStr;
		*/
		
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return addrData.get(record)[i];
		}
		return empStr;
	}
	
	public String getData(String tag)
	{
		Integer i = dictionaryMap.get(tag);
		if(i!=null)
			return dataList.get(i);
		return empStr;
	}

	public int index(String tag) {
		return attrHash.get(tag);
	}

	public void insert(List<String[]> data) {
		DBManager._db.beginTransaction();
		try {

			addrData = data;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = addrData.size();

			for (int i = 0; i < size; i++) {
				insert.bindString(index(shipmethod_id), getData(shipmethod_id, i)); // shipmethod_id
				insert.bindString(index(shipmethod_name), getData(shipmethod_name, i)); // shipmethod_name
				insert.bindString(index(shipmethod_update), getData(shipmethod_update, i)); // shipmethod_update
				insert.bindString(index(isactive), getData(isactive, i)); // isactive

				insert.execute();
				insert.clearBindings();

			}
			insert.close();
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.ShipMethodHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
			DBManager._db.endTransaction();
			global.dictionary.clear();
			addrData.clear();
		}
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
				insert.bindString(index(shipmethod_id), getData(shipmethod_id, j)); // cust_id
				insert.bindString(index(shipmethod_name), getData(shipmethod_name, j)); // cust_id_ref
				insert.bindString(index(shipmethod_update), getData(shipmethod_update, j)); // qb_sync
				insert.bindString(index(isactive), getData(isactive, j)); // CompanyName

				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.ShipMethodHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
			DBManager._db.endTransaction();
		}
	}
	
	
	public void insert(List<String> data, HashMap<String, Integer> dictionary) {
		DBManager._db.beginTransaction();
		try {

			dataList = data;
			dictionaryMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			insert.bindString(index(shipmethod_id), getData(shipmethod_id)); // cust_id
			insert.bindString(index(shipmethod_name), getData(shipmethod_name)); // cust_id_ref
			insert.bindString(index(shipmethod_update), getData(shipmethod_update)); // qb_sync
			insert.bindString(index(isactive), getData(isactive)); // CompanyName

			insert.execute();
			insert.clearBindings();
			insert.close();
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.ShipMethodHandler (at Class.insert)]");

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
	
	
	public List<String[]> getShipmentMethods()
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		
		String query = "SELECT shipmethod_name,shipmethod_id FROM ShipMethod WHERE isactive='1' ORDER BY shipmethod_name";
		Cursor cursor = DBManager._db.rawQuery(query, null);
		List<String[]> arrayList = new ArrayList<String[]>();
		String[] arrayValues = new String[2];
		//int i = 0;
		if(cursor.moveToFirst())
		{
			int nameColumnIndex = cursor.getColumnIndex(shipmethod_name);
			int idColumnIndex = cursor.getColumnIndex(shipmethod_id);
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
