package com.android.database;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class PublicVariablesHandler {
	public static final String attributes = "MSEmployeeID,MSEmployeeName,MSDeviceID,MSZoneID,MSLastSynch,MSOrderLastSynch,"
			+ "MSActivationKey,MSConnection,MSTicket,MSRegID,MSAccount,MSUser,MSPass,MPUser,"
			+ "MPPass,MSQBMS,MSLastOrderID,MSOrderEntry,MSOrderType,MSCardProcessor,MSPrinter,MSLanguage";

	public static final String table_name = "PublicVariables";

	public void insert(SQLiteDatabase db, List<String[]> data) {
		db.beginTransaction();
		try {
			SQLiteStatement insert = null;
			insert = db.compileStatement("INSERT INTO " + table_name + " (" + attributes + ") "
					+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

			for (int i = 0; i < 1000 && i < data.size(); i++) {
				insert.bindString(1, data.get(i)[0]);
				insert.bindString(2, data.get(i)[1]);
				insert.bindString(3, data.get(i)[2]);
				insert.bindString(4, data.get(i)[3]);
				insert.bindString(5, data.get(i)[4]);
				insert.bindString(6, data.get(i)[5]);
				insert.bindString(7, data.get(i)[6]);
				insert.bindString(8, data.get(i)[7]);
				insert.bindString(9, data.get(i)[8]);
				insert.bindString(10, data.get(i)[9]);
				insert.bindString(11, data.get(i)[10]);
				insert.bindString(12, data.get(i)[11]);
				insert.bindString(13, data.get(i)[12]);
				insert.bindString(14, data.get(i)[13]);
				insert.bindString(15, data.get(i)[14]);
				insert.bindString(16, data.get(i)[15]);
				insert.bindString(17, data.get(i)[16]);
				insert.bindString(18, data.get(i)[17]);
				insert.bindString(19, data.get(i)[18]);
				insert.bindString(20, data.get(i)[19]);
				insert.bindString(21, data.get(i)[20]);
				insert.bindString(22, data.get(i)[21]);

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
