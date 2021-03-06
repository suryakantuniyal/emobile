package com.android.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.android.emobilepos.models.orders.Order;

import net.sqlcipher.database.SQLiteStatement;

import java.util.HashMap;
import java.util.List;

public class VoidTransactionsHandler {

	private final String ord_id = "ord_id";
	private final String ord_type = "ord_type";
	private final String processed = "processed";
	private final String ord_timesync = "ord_timesync";
	private final String qb_synctime = "qb_synctime";
	private final String is_sync = "is_sync";

	private final String table_name = "VoidTransactions";

	private HashMap<String, String> hashedValues;

	public VoidTransactionsHandler() {
	}

	public String getValue(String key) {
		String value = hashedValues.get(key);
		if (value != null)
			return value;
		return "";
	}

	public void insert(Order values) {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openWritableDB();

		ContentValues insertValues = new ContentValues();
		// hashedValues = values;

		insertValues.put(ord_id, values.ord_id == null ? "" : values.ord_id); // ord_id
		insertValues.put(ord_type, values.ord_type == null ? "" : values.ord_type); // ord_type
		insertValues.put(processed, values.processed == null ? "1" : values.processed); // processed
		insertValues.put(ord_timesync, values.ord_timesync == null ? "" : values.ord_timesync); // ord_timesync
		insertValues.put(qb_synctime, values.qb_synctime == null ? "" : values.qb_synctime); // qb_synctime

		DBManager.getDatabase().insert(table_name, null, insertValues);
		// db.close();
	}

	public Cursor getUnsyncVoids() // Will populate all
	// unsynchronized orders
	// for XML post
	{

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ord_id,ord_type FROM ").append(table_name).append(" WHERE is_sync = '1'");
		Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);

		return cursor;
	}

	public long getNumUnsyncVoids() {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE is_sync = '1'");

		SQLiteStatement stmt = DBManager.getDatabase().compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		// db.close();
		return count;
	}

	public void updateIsSync(List<String[]> list) {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openWritableDB();

		StringBuilder sb = new StringBuilder();
		sb.append(ord_id).append(" = ?");

		ContentValues args = new ContentValues();

		int size = list.size();
		for (int i = 0; i < size; i++) {
			args.put(is_sync, list.get(i)[0]);
			DBManager.getDatabase().update(table_name, args, sb.toString(), new String[] { list.get(i)[1] });
		}
		// db.close();
	}
}
