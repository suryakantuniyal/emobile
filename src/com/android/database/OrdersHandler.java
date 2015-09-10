package com.android.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.emobilepos.models.Order;
import com.android.support.DBManager;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class OrdersHandler {

	private final String ord_id = "ord_id";
	private final String qbord_id = "qbord_id";
	// private final String qbtxid = "qbtxid";
	private final String emp_id = "emp_id";
	private final String cust_id = "cust_id";
	private final String custidkey = "custidkey";
	private final String ord_po = "ord_po";
	private final String total_lines = "total_lines";
	private final String total_lines_pay = "total_lines_pay";
	private final String ord_total = "ord_total";
	private final String ord_signature = "ord_signature";
	private final String ord_comment = "ord_comment";
	private final String ord_delivery = "ord_delivery";
	private final String ord_timecreated = "ord_timecreated";
	private final String ord_timesync = "ord_timesync";
	private final String qb_synctime = "qb_synctime";
	private final String emailed = "emailed";
	private final String processed = "processed";
	private final String ord_type = "ord_type";
	private final String ord_claimnumber = "ord_claimnumber";
	private final String ord_rganumber = "ord_rganumber";
	private final String ord_returns_pu = "ord_returns_pu";
	private final String ord_inventory = "ord_inventory";
	private final String ord_issync = "ord_issync";
	private final String tax_id = "tax_id";
	private final String ord_shipvia = "ord_shipvia";
	private final String ord_shipto = "ord_shipto";
	private final String ord_terms = "ord_terms";
	private final String ord_custmsg = "ord_custmsg";
	private final String ord_class = "ord_class";
	private final String ord_subtotal = "ord_subtotal";
	private final String ord_taxamount = "ord_taxamount";
	private final String ord_discount = "ord_discount";
	private final String c_email = "c_email";
	private final String isOnHold = "isOnHold";
	private final String ord_HoldName = "ord_HoldName";

	// added
	private final String clerk_id = "clerk_id";
	private final String ord_discount_id = "ord_discount_id";
	private final String ord_latitude = "ord_latitude";
	private final String ord_longitude = "ord_longitude";
	private final String tipAmount = "tipAmount";
	private final String isVoid = "isVoid";
	private final String is_stored_fwd = "is_stored_fwd";
	private final String VAT = "VAT";

	private final List<String> attr = Arrays.asList(new String[] { ord_id, qbord_id, emp_id, cust_id, clerk_id, c_email,
			ord_signature, ord_po, total_lines, total_lines_pay, ord_total, ord_comment, ord_delivery, ord_timecreated,
			ord_timesync, qb_synctime, emailed, processed, ord_type, ord_claimnumber, ord_rganumber, ord_returns_pu,
			ord_inventory, ord_issync, tax_id, ord_shipvia, ord_shipto, ord_terms, ord_custmsg, ord_class, ord_subtotal,
			ord_taxamount, ord_discount, ord_discount_id, ord_latitude, ord_longitude, tipAmount, isVoid, custidkey,
			isOnHold, ord_HoldName, is_stored_fwd, VAT });

	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	private List<String[]> data;
	private List<HashMap<String, Integer>> dictionaryListMap;
	private MyPreferences myPref;

	private static final String table_name = "Orders";
	private Activity activity;

	public OrdersHandler(Activity activity) {
		// global = (Global) activity.getApplication();
		myPref = new MyPreferences(activity);
		attrHash = new HashMap<String, Integer>();
		this.activity = activity;
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

	public int index(String tag) {
		return attrHash.get(tag);
	}

	private String getData(String tag, int record) {
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return data.get(record)[i];
		}
		return empStr;
	}

	public void insert(Order order) {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openWritableDB();
		DBManager._db.beginTransaction();
		try {

			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT OR REPLACE INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ")
					.append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			insert.bindString(index(ord_id), order.ord_id == null ? "" : order.ord_id); // cust_id
			insert.bindString(index(qbord_id), order.qbord_id == null ? "" : order.qbord_id); // cust_id
			insert.bindString(index(emp_id), order.emp_id == null ? "" : order.emp_id); // cust_id
			insert.bindString(index(cust_id), order.cust_id == null ? "" : order.cust_id); // cust_id
			insert.bindString(index(clerk_id), order.clerk_id == null ? "" : order.clerk_id); // cust_id
			insert.bindString(index(c_email), order.c_email == null ? "" : order.c_email); // cust_id
			insert.bindString(index(ord_signature), order.ord_signature == null ? "" : order.ord_signature); // cust_id
			insert.bindString(index(ord_po), order.ord_po == null ? "" : order.ord_po); // cust_id
			insert.bindString(index(total_lines), order.total_lines == null ? "0" : order.total_lines); // cust_id
			insert.bindString(index(total_lines_pay), order.total_lines_pay == null ? "0" : order.total_lines_pay); // cust_id
			insert.bindString(index(ord_total), order.ord_total == null ? "0" : order.ord_total); // cust_id
			insert.bindString(index(ord_comment), order.ord_comment == null ? "" : order.ord_comment); // cust_id
			insert.bindString(index(ord_delivery), order.ord_delivery == null ? "" : order.ord_delivery); // cust_id
			insert.bindString(index(ord_timecreated), order.ord_timecreated == null ? "" : order.ord_timecreated); // cust_id
			insert.bindString(index(ord_timesync), order.ord_timesync == null ? "" : order.ord_timesync); // cust_id
			insert.bindString(index(qb_synctime), order.qb_synctime == null ? "" : order.qb_synctime); // cust_id
			insert.bindString(index(emailed), order.emailed == null ? "0" : order.emailed); // cust_id
			insert.bindString(index(processed), order.processed == null ? "1" : order.processed); // cust_id
			insert.bindString(index(ord_type), order.ord_type == null ? "" : order.ord_type); // cust_id
			insert.bindString(index(ord_claimnumber), order.ord_claimnumber == null ? "" : order.ord_claimnumber); // cust_id
			insert.bindString(index(ord_rganumber), order.ord_rganumber == null ? "" : order.ord_rganumber); // cust_id
			insert.bindString(index(ord_returns_pu), order.ord_returns_pu == null ? "" : order.ord_returns_pu); // cust_id
			insert.bindString(index(ord_inventory), order.ord_inventory == null ? "" : order.ord_inventory); // cust_id
			insert.bindString(index(ord_issync), order.ord_issync == null ? "0" : order.ord_issync); // cust_id
			insert.bindString(index(tax_id), order.tax_id == null ? "" : order.tax_id); // cust_id
			insert.bindString(index(ord_shipvia), order.ord_shipvia == null ? "" : order.ord_shipvia); // cust_id
			insert.bindString(index(ord_shipto), order.ord_shipto == null ? "" : order.ord_shipto); // cust_id
			insert.bindString(index(ord_terms), order.ord_terms == null ? "" : order.ord_terms); // cust_id
			insert.bindString(index(ord_custmsg), order.ord_custmsg == null ? "" : order.ord_custmsg); // cust_id
			insert.bindString(index(ord_class), order.ord_class == null ? "" : order.ord_class); // cust_id
			insert.bindString(index(ord_subtotal), order.ord_subtotal == null ? "0" : order.ord_subtotal); // cust_id
			insert.bindString(index(ord_taxamount), order.ord_taxamount == null ? "0" : order.ord_taxamount); // cust_id
			insert.bindString(index(ord_discount), order.ord_discount == null ? "0" : order.ord_discount); // cust_id
			insert.bindString(index(ord_discount_id), order.ord_discount_id == null ? "" : order.ord_discount_id); // cust_id
			insert.bindString(index(ord_latitude), order.ord_latitude == null ? "" : order.ord_latitude); // cust_id
			insert.bindString(index(ord_longitude), order.ord_longitude == null ? "" : order.ord_longitude); // cust_id
			insert.bindString(index(tipAmount), order.tipAmount == null ? "0" : order.tipAmount); // cust_id
			insert.bindString(index(custidkey), order.custidkey == null ? "" : order.custidkey);
			insert.bindString(index(isOnHold), order.isOnHold == null ? "0" : order.isOnHold);
			insert.bindString(index(ord_HoldName), order.ord_HoldName == null ? "" : order.ord_HoldName);
			insert.bindString(index(is_stored_fwd), order.is_stored_fwd == null ? "0" : order.is_stored_fwd);

			insert.bindString(index(isVoid), order.isVoid == null ? "0" : order.isVoid);
			insert.bindString(index(VAT), order.VAT == null ? "0" : order.VAT);

			insert.execute();
			insert.clearBindings();

			DBManager._db.setTransactionSuccessful();

		} catch (Exception e) {
			Tracker tracker = EasyTracker.getInstance(activity);
			tracker.send(MapBuilder.createException(Log.getStackTraceString(e), false).build());
		} finally {
			myPref.setLastOrdID(order.ord_id);
			DBManager._db.endTransaction();
		}
		// db.close();
	}

	public void insertOnHold(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager._db.beginTransaction();
		try {

			this.data = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ")
					.append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = this.data.size();

			for (int i = 0; i < size; i++) {

				if (checkIfExist(getData(ord_id, i))) {
					updateOnHoldSync(getData(ord_id, i));
				} else {
					insert.bindString(index(ord_id), getData(ord_id, i)); // ord_id
					insert.bindString(index(qbord_id), getData(qbord_id, i)); // qbord_id
					insert.bindString(index(emp_id), getData(emp_id, i)); // emp_id
					insert.bindString(index(cust_id), getData(cust_id, i)); // cust_id
					insert.bindString(index(clerk_id), getData(clerk_id, i)); // clerk_id
					insert.bindString(index(c_email), getData(c_email, i)); // c_email
					insert.bindString(index(ord_signature), getData(ord_signature, i)); // ord_signature
					insert.bindString(index(ord_po), getData(ord_po, i)); // ord_po
					insert.bindString(index(total_lines), getData(total_lines, i)); // total_lines
					insert.bindString(index(total_lines_pay), getData(total_lines_pay, i)); // total_lines_pay
					insert.bindString(index(ord_total), getData(ord_total, i)); // ord_total
					insert.bindString(index(ord_comment), getData(ord_comment, i)); // ord_comment
					insert.bindString(index(ord_delivery), getData(ord_delivery, i)); // ord_delivery
					insert.bindString(index(ord_timecreated), getData(ord_timecreated, i)); // ord_timecreated
					insert.bindString(index(ord_timesync), getData(ord_timesync, i)); // ord_timesync
					insert.bindString(index(qb_synctime), getData(qb_synctime, i)); // qb_synctime
					insert.bindString(index(emailed), getData(emailed, i)); // emailed
					insert.bindString(index(processed), getData(processed, i)); // processed
					insert.bindString(index(ord_type), getData(ord_type, i)); // ord_type
					insert.bindString(index(ord_claimnumber), getData(ord_claimnumber, i)); // ord_claimnumber
					insert.bindString(index(ord_rganumber), getData(ord_rganumber, i)); // ord_rganumber
					insert.bindString(index(ord_returns_pu), getData(ord_returns_pu, i)); // ord_returns_pu
					insert.bindString(index(ord_inventory), getData(ord_inventory, i)); // ord_inventory
					insert.bindString(index(ord_issync), "1"); // ord_issync
					insert.bindString(index(tax_id), getData(tax_id, i)); // tax_id
					insert.bindString(index(ord_shipvia), getData(ord_shipvia, i)); // ord_shipvia
					insert.bindString(index(ord_shipto), getData(ord_shipto, i)); // ord_shipto
					insert.bindString(index(ord_terms), getData(ord_terms, i)); // ord_terms
					insert.bindString(index(ord_custmsg), getData(ord_custmsg, i)); // ord_custmsg
					insert.bindString(index(ord_class), getData(ord_class, i)); // ord_class
					insert.bindString(index(ord_subtotal), getData(ord_subtotal, i)); // ord_subtotal
					insert.bindString(index(ord_taxamount), getData(ord_taxamount, i)); // ord_taxamount
					insert.bindString(index(ord_discount), getData(ord_discount, i)); // ord_discount
					insert.bindString(index(ord_discount_id), getData(ord_discount_id, i)); // ord_discount_id
					insert.bindString(index(ord_latitude), getData(ord_latitude, i)); // ord_latitude
					insert.bindString(index(ord_longitude), getData(ord_longitude, i)); // ord_longitude
					insert.bindString(index(tipAmount), getData(tipAmount, i)); // tipAmount
					insert.bindString(index(custidkey), getData(custidkey, i)); // custidkey
					insert.bindString(index(isOnHold), "1"); // isOnHold
					insert.bindString(index(ord_HoldName), getData(ord_HoldName, i)); // ord_HoldName

					insert.bindString(index(VAT), getData(VAT, i));

					insert.execute();
					insert.clearBindings();
				}
			}

		} catch (Exception e) {
			Tracker tracker = EasyTracker.getInstance(activity);
			tracker.send(MapBuilder.createException(Log.getStackTraceString(e), false).build());
		} finally {
			DBManager._db.setTransactionSuccessful();
			DBManager._db.endTransaction();
		}
	}

	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_name);
		DBManager._db.execSQL(sb.toString());
	}

	public void deleteOrder(String _ord_id) {
		DBManager._db.delete(table_name, "ord_id = ?", new String[] { _ord_id });
	}

	// public void emptyTable()
	// {
	// StringBuilder sb = new StringBuilder();
	// SQLiteDatabase db = dbManager.openWritableDB();
	// sb.append("DELETE FROM ").append(table_name);
	// db.execSQL(sb.toString());
	// db.close();
	// }

	public void emptyTableOnHold() {
		StringBuilder sb = new StringBuilder();
		// sb.append("DELETE FROM OrderProducts WHERE OrderProducts.ord_id IN
		// ");
		// sb.append("(SELECT op.ord_id FROM OrderProducts op LEFT JOIN Orders o
		// ON op.ord_id=o.ord_id WHERE o.isOnHold = '1' AND o.emp_id != ?)");
		// DBManager._db.rawQuery(sb.toString(), new
		// String[]{myPref.getEmpID()});
		DBManager._db.delete("OrderProducts",
				"OrderProducts.ord_id IN (SELECT op.ord_id FROM OrderProducts op LEFT JOIN Orders o ON op.ord_id=o.ord_id WHERE o.isOnHold = '1' AND o.emp_id != ?)",
				new String[] { myPref.getEmpID() });
		// sb.setLength(0);
		// sb.append("DELETE FROM ").append(table_name).append(" WHERE isOnHold
		// = '1' AND emp_id != ?");
		// DBManager._db.rawQuery(sb.toString(), new
		// String[]{myPref.getEmpID()});

		DBManager._db.delete(table_name, "isOnHold = '1' AND emp_id != ?", new String[] { myPref.getEmpID() });
	}

	private boolean checkIfExist(String ordID) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT 1 FROM ").append(table_name).append(" WHERE ord_id = '");
		sb.append(ordID).append("'");
		Cursor c = DBManager._db.rawQuery(sb.toString(), null);
		boolean exists = (c.getCount() > 0);
		c.close();

		return exists;
	}

	public Cursor getUnsyncOrders() // Will populate all unsynchronized orders
									// for XML post
	{
		StringBuilder sb = new StringBuilder();
		if (Global.isForceUpload)
			sb.append("SELECT * FROM ").append(table_name).append(" WHERE ord_issync = '0'");
		else
			sb.append("SELECT ").append(sb1.toString()).append(" FROM ").append(table_name)
					.append(" WHERE ord_issync = '0' AND processed != '0' AND is_stored_fwd = '0'");
		// sb.append("SELECT o.*, Count(p.pay_id) AS 'pay_count' FROM
		// ").append(table_name).append(" o LEFT JOIN Payments p ");
		// sb.append("ON p.job_id = o.ord_id AND p.pay_issync = '1' WHERE
		// o.ord_issync = '0' AND o.processed != '0' AND o.is_stored_fwd = '0'
		// GROUP BY o.ord_id ");
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		return cursor;
	}

	public Cursor getTupyxOrders() {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"SELECT * FROM Orders o LEFT OUTER JOIN Payments p ON o.ord_id = p.job_id LEFT OUTER JOIN Customers c ON o.cust_id = c.cust_id WHERE p.paymethod_id = 'Wallet' AND ord_issync = '0'");
		Cursor c = DBManager._db.rawQuery(sb.toString(), null);
		return c;
	}

	public long getNumUnsyncTupyxOrders() {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(
				" o LEFT OUTER JOIN Payments p ON o.ord_id = p.job_id WHERE p.paymethod_id = 'Wallet' AND o.ord_issync = '0'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();

		// db.close();
		return count;
	}

	public Cursor getUnsyncOrdersOnHold() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(table_name).append(" WHERE ord_issync = '0' AND isOnHold = '1'");
		// sb.append("SELECT o.*, Count(p.pay_id) AS 'pay_count' FROM
		// ").append(table_name).append(" o LEFT JOIN Payments p ");
		// sb.append("ON p.job_id = o.ord_id AND p.pay_issync = '1' WHERE
		// o.ord_issync = '0' AND isOnHold = '1' GROUP BY o.ord_id ");
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		return cursor;
	}

	public long getNumUnsyncOrdersOnHold() {

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE ord_issync = '0' AND isOnHold = '1'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();

		return count;
	}

	public Cursor getOrderOnHold() {
		// if(db==null||!db.isOpen())
		// db = dbManager.openReadableDB();

		Cursor c = DBManager._db
				.rawQuery("SELECT ord_id as '_id',* FROM Orders WHERE isOnHold = '1' ORDER BY ord_id ASC", null);
		c.moveToFirst();
		// db.close();
		return c;
	}

	public long getNumUnsyncOrders() {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		/// SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE ord_issync = '0'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();

		// db.close();
		return count;
	}

	public static String getLastOrderId(int deviceId, int year) {
		StringBuilder sb = new StringBuilder();
		sb.append("select max(ord_id) from ").append(table_name)
				.append(" WHERE ord_id like '" + deviceId + "-%-" + year + "'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		cursor.moveToFirst();
		String max = cursor.getString(0);
		return max;
	}

	public long getNumUnsyncProcessedOrders() {
		// SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE ord_issync = '0' AND processed != '0'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();

		// db.close();
		return count;
	}

	public long getNumUnsyncOrdersStoredFwd() {
		// SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE is_stored_fwd = '1'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();

		// db.close();
		return count;
	}

	public boolean unsyncOrdersLeft() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE ord_issync = '0' AND processed != '0'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();

		if (count == 0)
			return false;
		return true;
	}

	public Cursor getReceipts1Data(String type) // Transactions Receipts first
												// listview
	{
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openReadableDB();

		String subquery1 = "SELECT ord_id as _id,ord_total,ord_issync,cust_id,isVoid,ord_type FROM Orders WHERE ord_type IN (";
		String subquery2 = ") AND isOnHold = '0' ORDER BY rowid DESC";
		StringBuilder sb = new StringBuilder();
		sb.append(subquery1).append(type).append(subquery2);
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

		cursor.moveToFirst();
		// db.close();
		return cursor;
	}

	public Cursor getReceipts1CustData(String type, String custID) {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openReadableDB();

		String subquery1 = "SELECT ord_id as _id,ord_total,ord_issync,cust_id,isVoid,ord_type FROM Orders WHERE ord_type IN (";
		String subquery2 = ") AND cust_id = ?";
		String subquery3 = " AND isOnHold = '0' ORDER BY rowid DESC";
		StringBuilder sb = new StringBuilder();
		sb.append(subquery1).append(type).append(subquery2).append(subquery3);
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), new String[] { custID });

		cursor.moveToFirst();
		// db.close();
		return cursor;

	}

	public Cursor getSearchOrder(String type, String search, String customerID) // Transactions
	// Receipts
	// first
	// listview
	{
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openReadableDB();

		String subqueries[] = new String[4];
		StringBuilder sb = new StringBuilder();
		String[] params = null;
		if (customerID == null) {
			subqueries[0] = "SELECT Orders.ord_id as _id,Orders.ord_total,Orders.ord_issync,Customers.cust_id,Orders.isVoid,Orders.ord_type FROM Orders JOIN Customers WHERE Orders.ord_type IN(";
			subqueries[1] = ") AND Orders.cust_id = Customers.cust_id AND Orders.ord_id LIKE ? ORDER BY Orders.rowid DESC";
			sb.append(subqueries[0]).append(type).append(subqueries[1]);// .append(search).append(subqueries[2]);
			params = new String[] { "%" + search + "%" };
		} else {
			subqueries[0] = "SELECT ord_id as _id,ord_total,ord_issync,cust_id,isVoid,ord_type FROM Orders WHERE ord_type IN(";
			subqueries[1] = ") AND cust_id = ?";
			subqueries[2] = " AND Orders.ord_id LIKE ? ORDER BY Orders.rowid DESC";

			sb.append(subqueries[0]).append(type).append(subqueries[1]).append(subqueries[2]);// .append(search).append(subqueries[3]);
			params = new String[] { customerID, "%" + search + "%" };
		}

		/*
		 * String subquery1 =
		 * "SELECT Orders.ord_id as _id,Orders.ord_total,Orders.ord_issync,Customers.cust_id,Orders.isVoid FROM Orders JOIN Customers WHERE Orders.ord_type='"
		 * ; String subquery2 =
		 * "' AND Orders.cust_id = Customers.cust_id AND Orders.ord_id LIKE '%";
		 * String subquery3 = "%' ORDER BY Orders.rowid DESC";
		 */

		Cursor cursor = DBManager._db.rawQuery(sb.toString(), params);
		cursor.moveToFirst();
		// db.close();
		return cursor;
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
			if (list.get(i)[0].equals("0"))
				args.put(ord_issync, "1");
			else
				args.put(ord_issync, "0");
			DBManager._db.update(table_name, args, sb.toString(), new String[] { list.get(i)[1] });
		}
		// db.close();
	}

	private void updateOnHoldSync(String ordID) {
		StringBuilder sb = new StringBuilder();
		sb.append(ord_id).append(" = ?");
		ContentValues args = new ContentValues();

		args.put(ord_issync, "1");
		DBManager._db.update(table_name, args, sb.toString(), new String[] { ordID });
	}

	public String updateFinishOnHold(String ordID) {
		// SQLiteDatabase db = dbManager.openWritableDB();
		StringBuilder sb2 = new StringBuilder();
		StringBuilder sb = new StringBuilder();

		Cursor c = DBManager._db.rawQuery("SELECT ord_timecreated FROM Orders WHERE ord_id = ?",
				new String[] { ordID });
		String dateCreated = Global.getCurrentDate();

		if (c.moveToFirst())
			dateCreated = c.getString(c.getColumnIndex(ord_timecreated));

		sb.append("DELETE FROM ").append(table_name).append(" WHERE ord_id = '").append(ordID).append("'");
		sb2.append("DELETE FROM OrderProducts WHERE ord_id = '").append(ordID).append("'");

		DBManager._db.delete(table_name, "ord_id = ?", new String[] { ordID });
		DBManager._db.delete("OrderProducts", "ord_id = ?", new String[] { ordID });
		// db.rawQuery(sb.toString(), null);
		// db.rawQuery(sb2.toString(), null);
		c.close();
		// db.close();

		return dateCreated;
	}

	public void updateIsProcessed(String orderID, String updateValue) {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openWritableDB();

		StringBuilder sb = new StringBuilder();
		sb.append(ord_id).append(" = ?");

		ContentValues args = new ContentValues();

		args.put(processed, updateValue);

		DBManager._db.update(table_name, args, sb.toString(), new String[] { orderID });

		// db.close();
	}

	public void updateOrderTypeToInvoice(String orderID) {
		StringBuilder sb = new StringBuilder();
		sb.append(ord_id).append(" = ?");

		ContentValues args = new ContentValues();

		args.put(ord_type, Global.IS_INVOICE);

		DBManager._db.update(table_name, args, sb.toString(), new String[] { orderID });
	}

	public void updateOrderComment(String orderID, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append(ord_id).append(" = ?");

		ContentValues args = new ContentValues();

		args.put(ord_comment, value);

		DBManager._db.update(table_name, args, sb.toString(), new String[] { orderID });
	}

	public void updateOrderStoredFwd(String _order_id, String value) {
		// if(db==null)
		// db = dbManager.openWritableDB();

		StringBuilder sb = new StringBuilder();
		sb.append(ord_id).append(" = ?");

		ContentValues args = new ContentValues();

		args.put(is_stored_fwd, value);

		DBManager._db.update(table_name, args, sb.toString(), new String[] { _order_id });
	}

	public void updateIsTotalLinesPay(String orderID, String updateValue) {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openWritableDB();

		StringBuilder sb = new StringBuilder();
		sb.append(ord_id).append(" = ?");

		ContentValues args = new ContentValues();

		args.put(total_lines_pay, updateValue);

		DBManager._db.update(table_name, args, sb.toString(), new String[] { orderID });

		// db.close();
	}

	public void updateIsVoid(String param) {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openWritableDB();

		StringBuilder sb = new StringBuilder();
		sb.append(ord_id).append(" = ?");

		ContentValues args = new ContentValues();

		args.put(isVoid, "1");
		args.put(ord_issync, "0");
		args.put(processed, "9");
		DBManager._db.update(table_name, args, sb.toString(), new String[] { param });

		// db.close();
	}

	public String getColumnValue(String key, String _ord_id) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ").append(key).append(" FROM ").append(table_name).append(" WHERE ord_id = ?");

		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[] { _ord_id });
		String value = "";
		if (c.moveToFirst()) {
			value = c.getString(c.getColumnIndex(key));
		}

		return value;
	}

	public long getDBSize() {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name);

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();

		// db.close();
		return count;
	}

	public boolean isOrderOffline(String ordID) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ord_issync FROM Orders WHERE ord_id = ?");
		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[] { ordID });

		boolean offline = false;
		if (c.moveToFirst()) {
			if (c.getString(c.getColumnIndex("ord_issync")).equals("0"))
				offline = true;
			else
				offline = false;
		}
		c.close();
		return offline;

	}

	// public String getLastOrdID() {
	// //NOTE: Any update here should be a similar update done to Load Template
	// and Payments
	//
	// SQLiteDatabase db = dbManager.openReadableDB();
	//
	// StringBuilder sb = new StringBuilder();
	// //sb.append("SELECT ord_id FROM ").append(table_name).append(" WHERE
	// ord_id = (select max(ord_id) FROM ").append(table_name).append(")");
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy",Locale.getDefault());
	// String currYear = sdf.format(new Date());
	// sb.append("SELECT max(ord_id) FROM Orders WHERE ord_id LIKE
	// \"").append(myPref.getEmpID()).append("-%-").append(currYear).append("\"");
	//
	// SQLiteStatement stmt = db.compileStatement(sb.toString());
	//
	// String val = stmt.simpleQueryForString();
	// db.close();
	// return val;
	// }

	public HashMap<String, String> getOrderDetails(String ordID) {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openReadableDB();
		HashMap<String, String> map = new HashMap<String, String>();

		/*
		 * String subquery1 =
		 * "SELECT o.ord_id as _id,o.ord_total,p.pay_amount,p.pay_tip,o.ord_timecreated,o.clerk_id,"
		 * +
		 * "o.ord_comment,o.ord_shipvia,o.ord_terms,o.ord_delivery,o.c_email,o.cust_id, o.ord_signature,p.paymethod_id,p.pay_id FROM Orders o "
		 * +
		 * "LEFT OUTER JOIN Payments p  ON o.ord_id = p.inv_id OR o.ord_id = p.job_id WHERE o.ord_id = '"
		 * ;
		 */

		String subquery1 = "SELECT o.ord_id as _id,o.ord_total ,o.ord_timecreated,o.ord_type,o.isVoid,o.clerk_id,o.ord_comment,o.ord_shipvia,o.ord_terms,o.ord_delivery,"
				+ "o.c_email,o.cust_id, o.ord_signature,o.ord_po,o.ord_latitude,o.ord_longitude FROM Orders o  WHERE o.ord_id ='";

		// String subquery1 = "SELECT ord_id as
		// _id,ord_total,ord_timecreated,clerk_id,ord_comment,ord_shipvia,ord_terms,ord_delivery,c_email,cust_id,
		// ord_signature FROM Orders WHERE ord_id ='";
		String subquery2 = "'";
		StringBuilder sb = new StringBuilder();
		sb.append(subquery1).append(ordID).append(subquery2);

		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

		if (cursor.moveToFirst()) {
			do {
				String data = cursor.getString(cursor.getColumnIndex(ord_total));
				map.put(ord_total, data);

				data = Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(ord_timecreated)), activity,
						0);
				map.put(ord_timecreated, data);

				data = cursor.getString(cursor.getColumnIndex(clerk_id));
				map.put(clerk_id, data);

				data = cursor.getString(cursor.getColumnIndex(ord_comment));
				map.put(ord_comment, data);

				data = cursor.getString(cursor.getColumnIndex(ord_shipvia));
				map.put(ord_shipvia, data);

				data = cursor.getString(cursor.getColumnIndex(ord_terms));
				map.put(ord_terms, data);

				data = cursor.getString(cursor.getColumnIndex(ord_delivery));
				map.put(ord_delivery, data);

				data = cursor.getString(cursor.getColumnIndex(c_email));
				map.put(c_email, data);

				data = cursor.getString(cursor.getColumnIndex(cust_id));
				map.put(cust_id, data);

				data = cursor.getString(cursor.getColumnIndex(ord_signature));
				map.put(ord_signature, data);

				data = cursor.getString(cursor.getColumnIndex(ord_po));
				map.put(ord_po, data);

				data = cursor.getString(cursor.getColumnIndex(ord_latitude));
				map.put(ord_latitude, data);

				data = cursor.getString(cursor.getColumnIndex(ord_longitude));
				map.put(ord_longitude, data);

				data = cursor.getString(cursor.getColumnIndex(ord_type));
				map.put(ord_type, data);

				data = cursor.getString(cursor.getColumnIndex(isVoid));
				map.put(isVoid, data);

			} while (cursor.moveToNext());
		}

		cursor.close();
		// db.close();
		return map;
	}

	public Order getPrintedOrder(String ordID) {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openReadableDB();
		Order anOrder = new Order(activity);

		StringBuilder sb = new StringBuilder();

		/*
		 * sb.append(
		 * "SELECT ord_id,ord_timecreated,ord_total,ord_subtotal,ord_discount,ord_taxamount, (ord_subtotal+ord_taxamount-ord_discount) AS 'gran_total',tipAmount FROM Orders WHERE ord_id = '"
		 * ); sb.append(ordID).append("'");
		 */

		sb.append(
				"SELECT o.ord_id,o.ord_timecreated,o.ord_total,o.ord_subtotal,o.ord_discount,o.ord_taxamount,c.cust_name,c.AccountNumnber,o.cust_id, "
						+ "(o.ord_subtotal+o.ord_taxamount-o.ord_discount) AS 'gran_total', tipAmount, ord_signature,o.ord_HoldName,o.clerk_id,o.ord_comment,o.isVoid FROM Orders o LEFT OUTER JOIN Customers c ON "
						+ "o.cust_id = c.cust_id WHERE o.ord_id = '");
		sb.append(ordID).append("'");
		// SELECT
		// o.ord_id,o.ord_timecreated,o.ord_total,o.ord_subtotal,o.ord_discount,o.ord_taxamount,c.cust_name,
		// (o.ord_subtotal+o.ord_taxamount-o.ord_discount) AS 'gran_total' FROM
		// Orders o LEFT OUTER JOIN Customers c ON o.cust_id = c.cust_id WHERE
		// o.ord_id = '50-00000-2012'
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

		if (cursor.moveToFirst()) {
			do {
				anOrder.ord_id = getValue(cursor.getString(cursor.getColumnIndex(ord_id)));
				anOrder.ord_timecreated = cursor.getString(cursor.getColumnIndex(ord_timecreated));
				anOrder.ord_total = getValue(cursor.getString(cursor.getColumnIndex(ord_total)));
				anOrder.ord_subtotal = getValue(cursor.getString(cursor.getColumnIndex(ord_subtotal)));
				anOrder.ord_discount = getValue(cursor.getString(cursor.getColumnIndex(ord_discount)));
				anOrder.ord_taxamount = getValue(cursor.getString(cursor.getColumnIndex(ord_taxamount)));
				anOrder.cust_name = getValue(cursor.getString(cursor.getColumnIndex("cust_name")));
				anOrder.gran_total = getValue(cursor.getString(cursor.getColumnIndex("gran_total")));
				anOrder.tipAmount = getValue(cursor.getString(cursor.getColumnIndex(tipAmount)));
				anOrder.ord_signature = getValue(cursor.getString(cursor.getColumnIndex(ord_signature)));
				anOrder.ord_HoldName = getValue(cursor.getString(cursor.getColumnIndex(ord_HoldName)));
				anOrder.clerk_id = getValue(cursor.getString(cursor.getColumnIndex(clerk_id)));
				anOrder.ord_comment = getValue(cursor.getString(cursor.getColumnIndex(ord_comment)));
				anOrder.cust_id = getValue(cursor.getString(cursor.getColumnIndex("AccountNumnber")));
				anOrder.isVoid = getValue(cursor.getString(cursor.getColumnIndex(isVoid)));
			} while (cursor.moveToNext());
		}
		cursor.close();
		// db.close();
		return anOrder;
	}

	private String getValue(String value) {
		if (value == null)
			value = empStr;
		return value;
	}

	public List<Order> getOrderDayReport(String clerk_id, String date) {
		List<Order> listOrder = new ArrayList<Order>();

		StringBuilder query = new StringBuilder();
		query.append(
				"SELECT ord_type,sum(ord_subtotal) as 'ord_subtotal',sum(ord_discount) as 'ord_discount', sum(ord_taxamount) as 'ord_taxamount' ,  ");
		query.append("sum(ord_total) as 'ord_total',date(ord_timecreated,'localtime') as 'date' FROM Orders ");

		String[] where_values = null;
		if (clerk_id != null && !clerk_id.isEmpty()) {
			query.append("WHERE clerk_id = ? ");
			where_values = new String[] { clerk_id };

			if (date != null && !date.isEmpty()) {
				query.append(" AND date = ? ");
				where_values = new String[] { clerk_id, date };
			}
		} else if (date != null && !date.isEmpty()) {
			query.append(" WHERE date = ? ");
			where_values = new String[] { date };
		}

		query.append("GROUP BY ord_type");

		Cursor c = DBManager._db.rawQuery(query.toString(), where_values);
		if (c.moveToFirst()) {
			int i_ord_type = c.getColumnIndex(ord_type);
			int i_ord_subtotal = c.getColumnIndex(ord_subtotal);
			int i_ord_discount = c.getColumnIndex(ord_discount);
			int i_ord_taxamount = c.getColumnIndex(ord_taxamount);
			int i_ord_total = c.getColumnIndex(ord_total);
			do {
				Order ord = new Order(activity);
				ord.ord_type = c.getString(i_ord_type);
				ord.ord_subtotal = c.getString(i_ord_subtotal);
				ord.ord_discount = c.getString(i_ord_discount);
				ord.ord_taxamount = c.getString(i_ord_taxamount);
				ord.ord_total = c.getString(i_ord_total);

				listOrder.add(ord);
			} while (c.moveToNext());
		}

		c.close();
		return listOrder;
	}

	public List<Order> getARTransactionsDayReport(String clerk_id, String date) {
		List<Order> listOrder = new ArrayList<Order>();

		StringBuilder query = new StringBuilder();
		query.append(
				"SELECT o.ord_id, c.cust_name , sum(o.ord_total) as 'ord_total',date(o.ord_timecreated,'localtime') as 'date' FROM Orders o LEFT JOIN Customers c ");
		query.append("ON o.cust_id = c.cust_id WHERE o.ord_type = '2' ");

		String[] where_values = null;
		if (clerk_id != null && !clerk_id.isEmpty()) {
			query.append("AND clerk_id = ? ");
			where_values = new String[] { clerk_id };

			if (date != null && !date.isEmpty()) {
				query.append(" AND date = ? ");
				where_values = new String[] { clerk_id, date };
			}
		} else if (date != null && !date.isEmpty()) {
			query.append(" AND date = ? ");
			where_values = new String[] { date };
		}

		Cursor c = DBManager._db.rawQuery(query.toString(), where_values);
		if (c.moveToFirst()) {
			int i_ord_id = c.getColumnIndex(ord_id);
			int i_cust_name = c.getColumnIndex("cust_name");
			int i_ord_total = c.getColumnIndex(ord_total);
			do {
				if (c.getString(i_ord_id) != null) {
					Order ord = new Order(activity);
					ord.ord_id = c.getString(i_ord_id);
					ord.cust_name = c.getString(i_cust_name);
					ord.ord_total = c.getString(i_ord_total);

					listOrder.add(ord);
				}
			} while (c.moveToNext());
		}

		c.close();
		return listOrder;
	}

}
