package com.android.database;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.android.emobilepos.holders.TransferLocations_Holder;
import com.android.support.DBManager;
import com.android.support.MyPreferences;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class TransferLocations_DB {
	public static final String trans_id = "trans_id";
	public static final String loc_key_from = "loc_key_from";
	public static final String loc_key_to = "loc_key_to";
	public static final String emp_id = "emp_id";
	public static final String trans_timecreated = "trans_timecreated";
	public static final String issync = "issync";
	
	private static final List<String> attr = Arrays.asList(new String[] {trans_id, loc_key_from,loc_key_to,emp_id,trans_timecreated});

	private static final String TABLE_NAME = "TransferLocations";
	private StringBuilder sb1, sb2;
	private HashMap<String, Integer> attrHash;
	private MyPreferences myPref;
	private Activity activity;
	
	
	public TransferLocations_DB(Activity activity) {
		attrHash = new HashMap<String, Integer>();
		this.activity = activity;
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		myPref = new MyPreferences(activity);
		initDictionary();
	}

	
	private void initDictionary() 
	{
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
	
	
	private int index(String tag) 
	{
		return attrHash.get(tag);
	}


	public void insert(TransferLocations_Holder location) {
		//SQLiteDatabase db = dbManager.openWritableDB();
		DBManager._db.beginTransaction();
		try {

			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString())
					.append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			insert.bindString(index(trans_id), location.get(trans_id));
			insert.bindString(index(loc_key_from),location.get(loc_key_from));
			insert.bindString(index(loc_key_to), location.get(loc_key_to));
			insert.bindString(index(emp_id), location.get(emp_id));
			insert.bindString(index(trans_timecreated), location.get(trans_timecreated));

			insert.execute();
			insert.clearBindings();

			DBManager._db.setTransactionSuccessful();

		} catch (Exception e) {
			Tracker tracker = EasyTracker.getInstance(activity);
			tracker.send(MapBuilder.createException(Log.getStackTraceString(e), false).build());
		} finally {
			myPref.setLastTransferID(location.get(trans_id));
			DBManager._db.endTransaction();
		}
		//db.close();
	}

	
	
	public void emptyTable() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME);
		DBManager._db.execSQL(sb.toString());
	}
	
//	public void emptyTable() 
//	{
//		StringBuilder sb = new StringBuilder();
//		SQLiteDatabase db = dbManager.openWritableDB();
//		sb.append("DELETE FROM ").append(TABLE_NAME);
//		db.execSQL(sb.toString());
//		db.close();
//	}
	
	
	public String getLastTransferID() {
		//NOTE: Any update here should be a similar update done to Load Template
		
		//SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		//sb.append("SELECT ord_id FROM ").append(table_name).append(" WHERE ord_id = (select max(ord_id) FROM ").append(table_name).append(")");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy",Locale.getDefault());
		String currYear = sdf.format(new Date());
		sb.append("SELECT max(trans_id) FROM TransferLocations WHERE trans_id LIKE \"").append(myPref.getEmpID()).append("-%-").append(currYear).append("\"");
		
		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());

		String val = stmt.simpleQueryForString();
		//db.close();
		return val;
	}
	
	
	public Cursor getUnsyncTransfers()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE issync = '0'");
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		return cursor;
	}
	
	public long getNumUnsyncTransfers() {
		//SQLiteDatabase db = SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS| SQLiteDatabase.OPEN_READWRITE);
		//SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(TABLE_NAME).append(" WHERE issync = '0'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();

		//db.close();
		return count;
	}
	
	public void updateIsSync(List<String[]> list) {
		//SQLiteDatabase db = dbManager.openWritableDB();
		StringBuilder sb = new StringBuilder();
		sb.append(trans_id).append(" = ?");

		ContentValues args = new ContentValues();

		int size = list.size();
		for (int i = 0; i < size; i++) {
			if(list.get(i)[1].equals("0"))
				args.put(issync, "1");
			else
				args.put(issync, "0");
			DBManager._db.update(TABLE_NAME, args, sb.toString(), new String[] { list.get(i)[0] });
		}
		//db.close();
	}
	
	public Cursor getAllTransactions()
	{
		StringBuilder sb = new StringBuilder();
		//SQLiteDatabase db = dbManager.openReadableDB();
		
		sb.append("SELECT trans_id as '_id', * FROM ").append(TABLE_NAME).append(" ORDER BY trans_id DESC");
		
		Cursor c = DBManager._db.rawQuery(sb.toString(), null);
		
		c.moveToFirst();
		//db.close();
		return c;
	}
	
}
