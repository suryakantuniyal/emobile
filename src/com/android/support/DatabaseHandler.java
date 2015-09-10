package com.android.support;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class DatabaseHandler extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1.1;
	private static final String DATABASE_NAME = "emobilepos";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		onCreate(db);
		
	}

	public void insert(SQLiteDatabase db, String table_name, List<String> attributes, List<String[]> data) {
		ContentValues values = new ContentValues();
		int pk = getTableSize(db, table_name) + 1;
		int size = data.size();
		int size2 = attributes.size();
		if (table_name.equals("PriceLevelItems") || table_name.equals("Address")) {
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size2; j++) {
					if (j == 0 || j == 1) {
						values.put(attributes.get(j), pk);
					} else {
						values.put(attributes.get(j), data.get(i)[j - 2]);
					}
				}
				db.insert(table_name, null, values);
				pk++;
			}
		} else {
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size2; j++) {
					if (j == 0) {
						values.put(attributes.get(j), pk);
					} else {
						values.put(attributes.get(j), data.get(i)[j - 1]);
					}
				}
				db.insert(table_name, null, values);
				pk++;
			}
		}
	}

	public List<String[]> getTableRow(SQLiteDatabase db, String[] values) {
		List<String[]> list = new ArrayList<String[]>();
		String dbQuery = "SELECT * FROM " + values[0] + " WHERE " + values[1] + "=" + "'" + values[2] + "'";

		Cursor cursor = db.rawQuery(dbQuery, null);
		int size = getAttributesSize(db, values[0]);
		if (cursor.moveToFirst()) {
			do {
				String[] temp = new String[size];

				for (int i = 0; i < temp.length; i++) {
					temp[i] = cursor.getString(i);
				}
				list.add(temp);
			} while (cursor.moveToNext());
		}
		return list;
	}

	public int getTableSize(SQLiteDatabase db, String table_name) {
		String dbQuery = "SELECT Count(*) FROM " + table_name;
		Cursor cursor = db.rawQuery(dbQuery, null);
		if (cursor.moveToFirst())
			return cursor.getInt(0);
		return 0;
	}

	public List<String> getAttributes(SQLiteDatabase db, String table_name) {
		List<String> attributes = new ArrayList<String>();
		Cursor attr = db.rawQuery("PRAGMA table_info(" + table_name + ")", null);
		if (attr.moveToFirst()) {
			do {
				attributes.add(attr.getString(1));
			} while (attr.moveToNext());
		}
		return attributes;
	}

	public int getAttributesSize(SQLiteDatabase db, String table_name) {
		Cursor attr = db.rawQuery("PRAGMA table_info(" + table_name + ")", null);
		int size = 0;
		if (attr.moveToFirst()) {
			do {
				size++;
			} while (attr.moveToNext());
		}
		return size;
	}
}
