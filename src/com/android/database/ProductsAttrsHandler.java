package com.android.database;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class ProductsAttrsHandler {
	public static final String attributes = "prodAttrKey,prod_id,attr_id,attr_name,attr_desc,attr_group,attr_group_id";

	public static final String table_name = "products_attrs";

	public void insert(SQLiteDatabase db, List<String[]> data) {
		db.beginTransaction();
		try {
			SQLiteStatement insert = null;
			insert = db.compileStatement("INSERT INTO " + table_name + " (" + attributes + ") " + "VALUES (?,?,?,?,?,?,?)");

			for (int i = 0; i < 1000 && i < data.size(); i++) {
				insert.bindString(1, data.get(i)[0]);
				insert.bindString(2, data.get(i)[1]);
				insert.bindString(3, data.get(i)[2]);
				insert.bindString(4, data.get(i)[3]);
				insert.bindString(5, data.get(i)[4]);
				insert.bindString(6, data.get(i)[5]);
				insert.bindString(7, data.get(i)[6]);

				insert.execute();
				insert.clearBindings();
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			String err = "error";
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
