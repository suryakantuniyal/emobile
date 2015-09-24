package com.android.database;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class ReasonsHandler {
	public static final String attributes = "reason_id,reason_name,reason_date,isactive";

	public static final String table_name = "Reasons";

	public void insert(SQLiteDatabase db, List<String[]> data) {
		db.beginTransaction();
		try {
			SQLiteStatement insert = null;
			insert = db.compileStatement("INSERT INTO " + table_name + " (" + attributes + ") " + "VALUES (?,?,?,?)");

			for (int i = 0; i < 1000 && i < data.size(); i++) {
				insert.bindString(1, data.get(i)[0]);
				insert.bindString(2, data.get(i)[1]);
				insert.bindString(3, data.get(i)[2]);
				insert.bindString(4, data.get(i)[3]);

				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			db.setTransactionSuccessful();
		} catch (Exception e) {
			
		} finally {
			db.endTransaction();
		}
	}

	public void emptyTable(SQLiteDatabase db) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_name);
		db.execSQL(sb.toString());
	}
}