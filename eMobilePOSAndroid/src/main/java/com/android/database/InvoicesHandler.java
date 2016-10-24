package com.android.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;

import com.android.support.Global;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InvoicesHandler {

	private final static String inv_id = "inv_id";
	private final static String cust_id = "cust_id";
	private final static String emp_id = "emp_id";
	private final static String inv_timecreated = "inv_timecreated";
	private final static String inv_ispending = "inv_ispending";
	private final static String inv_ponumber = "inv_ponumber";
	private final static String inv_terms = "inv_terms";
	private final static String inv_duedate = "inv_duedate";
	private final static String inv_shipdate = "inv_shipdate";
	private final static String inv_shipmethod = "inv_shipmethod";
	private final static String inv_total = "inv_total";
	private final static String inv_apptotal = "inv_apptotal";
	private final static String inv_balance = "inv_balance";
	private final static String inv_custmsg = "inv_custmsg";
	private final static String inv_ispaid = "inv_ispaid";
	private final static String inv_paiddate = "inv_paiddate";
	private final static String mod_date = "mod_date";
	private final static String txnID = "txnID";
	private final static String inv_update = "inv_update";

	private static final List<String> attr = Arrays.asList(inv_id, cust_id, emp_id, inv_timecreated,
			inv_ispending, inv_ponumber, inv_terms, inv_duedate, inv_shipdate, inv_shipmethod, inv_total, inv_apptotal,
			inv_balance, inv_custmsg, inv_ispaid, inv_paiddate, mod_date, txnID, inv_update);

	private StringBuilder sb1, sb2;
	private HashMap<String, Integer> attrHash;
	private List<String[]> addrData;
	private List<HashMap<String, Integer>> dictionaryListMap;
	private MyPreferences myPref;
	private Activity activity;

	private static final String table_name = "Invoices";

	public InvoicesHandler(Activity activity) {
		this.activity = activity;
		attrHash = new HashMap<String, Integer>();
		addrData = new ArrayList<String[]>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		myPref = new MyPreferences(activity);
		new DBManager(activity);
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
		return "";
	}

	private int index(String tag) {
		return attrHash.get(tag);
	}

	public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager.getDatabase().beginTransaction();
		try {

			addrData = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ")
					.append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager.getDatabase().compileStatement(sb.toString());

			int size = addrData.size();

			for (int j = 0; j < size; j++) {
				insert.bindString(index(inv_id), getData(inv_id, j)); // cust_id
				insert.bindString(index(cust_id), getData(cust_id, j)); // cust_id_ref
				insert.bindString(index(emp_id), getData(emp_id, j)); // qb_sync
				insert.bindString(index(inv_timecreated), getData(inv_timecreated, j)); // zone_id
				insert.bindString(index(inv_ispending), getData(inv_ispending, j)); // CompanyName
				insert.bindString(index(inv_ponumber), getData(inv_ponumber, j)); // Salutation
				insert.bindString(index(inv_terms), getData(inv_terms, j)); // cust_name
				insert.bindString(index(inv_duedate), getData(inv_duedate, j)); // cust_chain
				insert.bindString(index(inv_shipdate), getData(inv_shipdate, j)); // cust_balance
				insert.bindString(index(inv_shipmethod), getData(inv_shipmethod, j)); // cust_limit
				insert.bindString(index(inv_total), getData(inv_total, j)); // cust_contact
				insert.bindString(index(inv_apptotal), getData(inv_apptotal, j)); // cust_firstName
				insert.bindString(index(inv_balance), getData(inv_balance, j)); // cust_middleName
				insert.bindString(index(inv_custmsg), getData(inv_custmsg, j)); // cust_lastName
				insert.bindString(index(inv_ispaid), getData(inv_ispaid, j)); // cust_phone
				insert.bindString(index(inv_paiddate), getData(inv_paiddate, j)); // cust_email
				insert.bindString(index(mod_date), getData(mod_date, j)); // cust_fax
				insert.bindString(index(txnID), getData(txnID, j)); // cust_update
				insert.bindString(index(inv_update), getData(inv_update, j)); // isactive

				insert.execute();
				insert.clearBindings();

			}
			insert.close();
			DBManager.getDatabase().setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.InvoicesHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {

			DBManager.getDatabase().endTransaction();
		}
	}

	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_name);
		DBManager.getDatabase().execSQL(sb.toString());
	}

	public long getDBSize() {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name);

		SQLiteStatement stmt = DBManager.getDatabase().compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		// db.close();
		return count;
	}

	// public String getLastInvoiceID() {
	// // SQLiteDatabase db = dbManager.openReadableDB();
	//
	// StringBuilder sb = new StringBuilder();
	// sb.append("SELECT inv_id FROM ").append(table_name).append(" WHERE inv_id
	// = (select max(inv_id) FROM ").append(table_name).append(")");
	//
	// SQLiteStatement stmt = DBManager.database.compileStatement(sb.toString());
	//
	// String val = stmt.simpleQueryForString();
	// // db.close();
	// return val;
	// }

	public void updateIsPaid(boolean isPaid, String param, String remainingBalance) {

		// SQLiteDatabase db = dbManager.openWritableDB();

		StringBuilder sb = new StringBuilder();
		sb.append(inv_id).append(" = ?");

		ContentValues args = new ContentValues();

		if (isPaid) {
			args.put(inv_ispaid, "1");
			args.put(inv_balance, "0.00");
		} else {
			args.put(inv_balance, remainingBalance);
		}
		DBManager.getDatabase().update(table_name, args, sb.toString(), new String[] { param });

		// db.close();

	}

	public Cursor getInvoices(String type) {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ").append(sb1.toString()).append(" FROM ").append(table_name).append(" WHERE inv_ispaid = ?");

		Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), new String[] { type });

		cursor.moveToFirst();
		// db.close();
		return cursor;
	}

	public Cursor getListSpecificInvoice(String id) {

		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append(
				"SELECT i.inv_id as _id, c.cust_name, i.txnID,i.inv_balance,i.inv_ispaid,i.inv_total,i.inv_duedate,i.inv_shipdate,i.inv_timecreated "
						+ ",c.cust_id,c.custidkey FROM Invoices i, Customers c WHERE i.cust_id = c.cust_id AND i.cust_id = ? ORDER BY c.cust_name AND i.inv_ispaid");

		/*
		 * sb.append(
		 * "SELECT i.inv_id as _id, c.cust_name, i.txnID,i.inv_balance,i.inv_ispaid,i.inv_total,i.inv_duedate,i.inv_shipdate,i.inv_timecreated "
		 * +
		 * "FROM Invoices i, Customers c WHERE i.cust_id = c.cust_id ORDER BY c.cust_name AND i.inv_ispaid WHERE i.cust_id = "
		 * );
		 */

		Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), new String[] { id });

		cursor.moveToFirst();
		// db.close();
		return cursor;
	}

	public Cursor getInvoicesList() {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append(
				"SELECT i.inv_id as _id, c.cust_name, i.txnID,i.inv_balance,i.inv_ispaid,i.inv_total,i.inv_duedate,i.inv_shipdate,i.inv_timecreated "
						+ ",c.cust_id,c.custidkey FROM Invoices i, Customers c WHERE i.cust_id = c.cust_id ORDER BY c.cust_name AND i.inv_ispaid");

		Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);

		cursor.moveToFirst();
		// db.close();
		return cursor;
	}

	public Cursor getSearchedInvoicesList(String pattern, boolean isForSpecificCust) {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		Cursor cursor;
		if (!isForSpecificCust) {
			sb.append(
					"SELECT i.inv_id as _id, c.cust_name, i.txnID,i.inv_balance,i.inv_ispaid,i.inv_total,i.inv_duedate,i.inv_shipdate,i.inv_timecreated "
							+ ",c.cust_id,c.custidkey FROM Invoices i, Customers c WHERE i.cust_id = c.cust_id AND i.inv_id LIKE ? ORDER BY c.cust_name");
			cursor = DBManager.getDatabase().rawQuery(sb.toString(), new String[] { "%" + pattern + "%" });
		} else {
			sb.append(
					"SELECT i.inv_id as _id, c.cust_name, i.txnID,i.inv_balance,i.inv_ispaid,i.inv_total,i.inv_duedate,i.inv_shipdate,i.inv_timecreated "
							+ ",c.cust_id,c.custidkey FROM Invoices i, Customers c WHERE i.cust_id = c.cust_id AND i.cust_id = ? AND i.inv_id LIKE ? ORDER BY c.cust_name");
			cursor = DBManager.getDatabase().rawQuery(sb.toString(), new String[] { myPref.getCustID(), "%" + pattern + "%" });
		}

		// Cursor cursor = db.rawQuery(sb.toString(), new String[] {"%" +
		// pattern + "%"});

		cursor.moveToFirst();
		// db.close();
		return cursor;
	}

	public String[] getSpecificInvoice(String invID) {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append(
				"SELECT c.cust_name, i.inv_id , i.txnID,i.inv_total, i.inv_balance,i.inv_timecreated,i.inv_duedate,i.inv_shipdate,i.inv_ispaid "
						+ ",i.inv_terms,i.inv_ponumber, i.inv_total, i.inv_balance FROM Invoices i, Customers c WHERE i.cust_id = c.cust_id AND  i.inv_id = ?");

		Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), new String[] { invID });

		String[] arrayVal = new String[14];

		if (cursor.moveToFirst()) {
			do {
				arrayVal[0] = cursor.getString(cursor.getColumnIndex("cust_name"));
				arrayVal[1] = cursor.getString(cursor.getColumnIndex(inv_id));
				arrayVal[2] = cursor.getString(cursor.getColumnIndex(txnID));
				arrayVal[3] = Global.formatDoubleStrToCurrency(cursor.getString(cursor.getColumnIndex(inv_total)));
				arrayVal[4] = Global.formatDoubleStrToCurrency(cursor.getString(cursor.getColumnIndex(inv_balance)));
				arrayVal[5] = Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(inv_timecreated)),
						activity, 0);
				arrayVal[6] = Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(inv_duedate)), activity,
						0);
				arrayVal[7] = Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(inv_shipdate)),
						activity, 0);
				if (cursor.getString(cursor.getColumnIndex(inv_ispaid)).equals("0"))
					arrayVal[8] = "No";
				else
					arrayVal[8] = "Yes";
				arrayVal[9] = cursor.getString(cursor.getColumnIndex(inv_terms));
				arrayVal[10] = cursor.getString(cursor.getColumnIndex(inv_ponumber));

				arrayVal[11] = cursor.getString(cursor.getColumnIndex(inv_total));
				arrayVal[12] = cursor.getString(cursor.getColumnIndex(inv_balance));
				arrayVal[13] = Double.toString(Double.parseDouble(arrayVal[11]) - Double.parseDouble(arrayVal[12]));

			} while (cursor.moveToNext());
		}

		cursor.close();
		// db.close();

		return arrayVal;
	}

	public long getNumberOpenedInvoices() {
		/// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE inv_ispaid = '0'");

		SQLiteStatement stmt = DBManager.getDatabase().compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		// db.close();

		return count;
	}
}