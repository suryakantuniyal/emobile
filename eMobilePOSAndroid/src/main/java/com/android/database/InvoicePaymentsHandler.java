package com.android.database;

import android.app.Activity;
import android.database.Cursor;

import com.android.support.DBManager;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InvoicePaymentsHandler {

	private final String pay_id = "pay_id";
	private final String inv_id = "inv_id";
	private final String applied_amount = "applied_amount";
	private final String txnID = "txnID";

	public final List<String> attr = Arrays.asList(new String[] { pay_id, inv_id, txnID, applied_amount });

	private StringBuilder sb1, sb2;
	private HashMap<String, Integer> attrHash;
	private static final String table_name = "InvoicePayments";
	private Activity activity;

	public InvoicePaymentsHandler(Activity activity) {
		this.activity = activity;
		attrHash = new HashMap<String, Integer>();
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
				sb2.append("ROUND(?,2)");
			}
		}
	}

	public int index(String tag) {
		return attrHash.get(tag);
	}

	public void insert(List<String[]> payment) {
		// SQLiteDatabase db = dbManager.openWritableDB();
		DBManager._db.beginTransaction();
		try {

			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ")
					.append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = payment.size();
			for (int i = 0; i < size; i++) {
				insert.bindString(index(pay_id), payment.get(i)[0]); // pay_id
				insert.bindString(index(inv_id), payment.get(i)[1]); // inv_id
				insert.bindString(index(applied_amount), payment.get(i)[2]); // applied_amount
				insert.bindString(index(txnID), payment.get(i)[3]); // txnID

				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager._db.setTransactionSuccessful();

		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.PaymentsHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
			DBManager._db.endTransaction();
		}
		// db.close();
	}

	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_name);
		DBManager._db.execSQL(sb.toString());
	}

	public long getDBSize() {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name);

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		// db.close();
		return count;
	}

	public double getTotalPaidAmount(String invID) {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT  ifnull(ROUND(sum(applied_amount),2),-1) as 'total' FROM InvoicePayments WHERE inv_id = '")
				.append(invID).append("'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		String count = stmt.simpleQueryForString();
		stmt.close();
		// db.close();
		return Double.parseDouble(count);
	}

	public String getInvoicePaymentsID(String payID) {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT txnID FROM InvoicePayments WHERE pay_id = '").append(payID).append("' GROUP BY txnID");

		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		sb.setLength(0);
		if (cursor.moveToFirst()) {
			do {
				sb.append(cursor.getString(cursor.getColumnIndex(txnID))).append("\n");
			} while (cursor.moveToNext());
		}

		cursor.close();
		// db.close();
		return sb.toString();
	}

	public List<String[]> getInvoicesPaymentsList(String payID) {

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT inv_id,applied_amount FROM InvoicePayments WHERE pay_id = '").append(payID).append("'");

		List<String[]> list = new ArrayList<String[]>();
		String[] content = new String[2];
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		sb.setLength(0);
		if (cursor.moveToFirst()) {
			int i_inv_id = cursor.getColumnIndex(inv_id);
			int i_amount = cursor.getColumnIndex(applied_amount);
			do {
				content[0] = cursor.getString(i_inv_id);
				content[1] = cursor.getString(i_amount);

				list.add(content);
				content = new String[2];
			} while (cursor.moveToNext());
		}

		cursor.close();
		return list;
	}
}
