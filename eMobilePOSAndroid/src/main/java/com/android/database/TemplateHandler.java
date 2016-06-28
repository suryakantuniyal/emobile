package com.android.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;

import com.android.emobilepos.models.OrderProduct;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TemplateHandler {

	private final String _id = "_id";
	private final String cust_id = "cust_id";
	private final String product_id = "product_id";
	private final String prod_sku = "prod_sku";
	private final String prod_upc = "prod_upc";
	private final String quantity = "quantity";

	private final String price_level_id = "price_level_id";
	private final String price_level = "price_level";

	private final String name = "name";
	private final String price = "price";
	private final String overwrite_price = "overwrite_price";
	private final String _update = "_update";
	private final String isactive = "isactive";
	private final String isSync = "isSync";

	// private final String itemTotal = "itemTotal";

	private final List<String> attr = Arrays.asList(_id, cust_id, product_id, name, overwrite_price,
			quantity, price, price_level_id, price_level, _update, isactive, isSync, prod_sku, prod_upc);

	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	private List<String[]> addrData;
	private List<HashMap<String, Integer>> dictionaryListMap;
	private static final String table_name = "Templates";
	private Activity activity;
	private Global global;

	public TemplateHandler(Activity activity) {
		// global = (Global) activity.getApplication();
		global = (Global) activity.getApplication();
		attrHash = new HashMap<String, Integer>();
		this.activity = activity;
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
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

	private int index(String tag) {
		return attrHash.get(tag);
	}

	private String getData(String tag, int record) {
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return addrData.get(record)[i];
		}
		return empStr;
	}

	public void insert(String custID) {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);

		// SQLiteDatabase db = dbManager.openWritableDB();
		DBManager._db.beginTransaction();
		try {

			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ")
					.append("VALUES (").append(sb2.toString()).append(")");

			DBManager._db.execSQL("DELETE FROM Templates WHERE cust_id='" + custID + "'");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = global.orderProducts.size();
			for (int i = 0; i < size; i++) {
				OrderProduct prod = global.orderProducts.get(i);
				insert.bindString(index(_id), empStr); // _id
				insert.bindString(index(cust_id), custID); // cust_id
                insert.bindString(index(product_id), prod.getProd_id() == null ? "" : prod.getProd_id()); // product_id
                insert.bindString(index(prod_sku), prod.getProd_sku() == null ? "" : prod.getProd_sku()); // product_sku
                insert.bindString(index(prod_upc), prod.getProd_upc() == null ? "" : prod.getProd_upc()); // product_upc
				insert.bindString(index(quantity), prod.getOrdprod_qty() == null ? "0" : prod.getOrdprod_qty()); // quantity
				insert.bindString(index(price_level_id), prod.getPricelevel_id() == null ? "" : prod.getPricelevel_id()); // price_level_id
				insert.bindString(index(price_level), prod.getPriceLevelName() == null ? "" : prod.getPriceLevelName()); // price_level

				insert.bindString(index(name), prod.getOrdprod_name() == null ? "" : prod.getOrdprod_name()); // name
				// insert.bindString(index(price),
				// global.cur_orders.get(i).getValue());
				insert.bindString(index(overwrite_price), prod.getOverwrite_price() == null ? "0" : prod.getOverwrite_price()); // cust_id
				// insert.bindString(index(itemTotal),
				// global.orderProducts.get(i).getSetData(itemTotal, true,
				// empStr));
				insert.bindString(index(_update), "");
				insert.bindString(index(isactive), "true");
				insert.bindString(index(isSync), "0");

				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager._db.setTransactionSuccessful();

		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.OrdersHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
			DBManager._db.endTransaction();
		}
		// db.close();
	}

	public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager._db.beginTransaction();
		try {

			addrData = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ")
					.append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = addrData.size();

			for (int i = 0; i < size; i++) {
				insert.bindString(index(_id), getData(_id, i)); // _id
				insert.bindString(index(cust_id), getData(cust_id, i)); // cust_id
                insert.bindString(index(product_id), getData(product_id, i)); // product_id
                insert.bindString(index(prod_sku), getData(prod_sku, i));
                insert.bindString(index(prod_upc), getData(prod_upc, i));
				insert.bindString(index(quantity), getData(quantity, i)); // quantity
				insert.bindString(index(price_level_id), getData(price_level_id, i)); // price_level_id
				insert.bindString(index(price_level), getData(price_level, i));// price_level

				insert.bindString(index(name), getData(name, i)); // name
				insert.bindString(index(price), getData(price, i)); // price
				insert.bindString(index(overwrite_price), getData(overwrite_price, i)); // overwrite_price
				// insert.bindString(index(itemTotal),
				// global.orderProducts.get(i).getSetData(itemTotal, true,
				// empStr));
				insert.bindString(index(_update), getData(_update, i)); // _update
				insert.bindString(index(isactive), getData(isactive, i));// isactive
				insert.bindString(index(isSync), "1");// isSync

				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.ProductsHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
			DBManager._db.endTransaction();
		}
	}

	public List<HashMap<String, String>> getTemplate(String custID) {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS |
		// SQLiteDatabase.OPEN_READWRITE);

		// SQLiteDatabase db = dbManager.openReadableDB();

		List<HashMap<String, String>> orderList = new ArrayList<HashMap<String, String>>();
		// OrderProduct anOrder = new OrderProduct();
		HashMap<String, String> map = new HashMap<String, String>();

		GenerateNewID generator = new GenerateNewID(activity);
		StringBuilder sb = new StringBuilder();

		Global.lastOrdID = generator.getNextID(IdType.ORDER_ID);

		/*
		 * sb.append(
		 * "SELECT ord_id,ord_timecreated,ord_total,ord_subtotal,ord_discount,ord_taxamount, (ord_subtotal+ord_taxamount-ord_discount) AS 'gran_total',tipAmount FROM Orders WHERE ord_id = '"
		 * ); sb.append(ordID).append("'");
		 */

		sb.append(
				"SELECT t.product_id,t.name,t.overwrite_price,t.price,t.quantity,p.prod_desc, p.prod_sku, p.prod_upc,IFNULL(s.taxcode_istaxable,'1') as 'prod_istaxable'  FROM Templates t ");
		sb.append(
				"LEFT JOIN Products p ON t.product_id = p.prod_id LEFT JOIN SalesTaxCodes s ON p.prod_taxcode = s.taxcode_id  WHERE cust_id = ?");

		// SELECT
		// o.ord_id,o.ord_timecreated,o.ord_total,o.ord_subtotal,o.ord_discount,o.ord_taxamount,c.cust_name,
		// (o.ord_subtotal+o.ord_taxamount-o.ord_discount) AS 'gran_total' FROM
		// Orders o LEFT OUTER JOIN Customers c ON o.cust_id = c.cust_id WHERE
		// o.ord_id = '50-00000-2012'

		Cursor cursor = DBManager._db.rawQuery(sb.toString(), new String[] { custID });

		if (cursor.moveToFirst()) {
			int i_prod_id = cursor.getColumnIndex(product_id);
			int i_prod_sku = cursor.getColumnIndex(prod_sku);
			int i_prod_upc = cursor.getColumnIndex(prod_upc);

			int i_prod_name = cursor.getColumnIndex(name);
			int i_overwrite_price = cursor.getColumnIndex(overwrite_price);
			int i_ordprod_qty = cursor.getColumnIndex(quantity);
			int i_prod_price = cursor.getColumnIndex(price);
			int i_prod_desc = cursor.getColumnIndex("prod_desc");
			int i_prod_istaxable = cursor.getColumnIndex("prod_istaxable");
			do {
				map.put("prod_id", cursor.getString(i_prod_id));
				map.put("prod_sku", cursor.getString(i_prod_sku));
				map.put("prod_upc", cursor.getString(i_prod_upc));
				map.put("prod_name", cursor.getString(i_prod_name));
				map.put(overwrite_price, cursor.getString(i_overwrite_price));
				map.put("ordprod_qty", cursor.getString(i_ordprod_qty));
				map.put("prod_price", cursor.getString(i_prod_price));
				map.put("prod_istaxable", cursor.getString(i_prod_istaxable));
				map.put("ord_id", Global.lastOrdID);
				map.put("ordprod_id", UUID.randomUUID().toString());

				double total = Double.parseDouble(map.get("ordprod_qty"))
						* Double.parseDouble(map.get(overwrite_price));
				map.put("itemTotal", Double.toString(total));
				map.put("itemSubtotal", Double.toString(total));
				map.put("ordprod_desc", cursor.getString(i_prod_desc));

				orderList.add(map);
				map = new HashMap<String, String>();

			} while (cursor.moveToNext());
		}
		cursor.close();
		// db.close();
		return orderList;
	}

	public Cursor getUnsyncTemplates() {
		// SQLiteDatabase db = myDB;
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM Templates WHERE isSync = '0'");

		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		return cursor;

	}

	public long getNumUnsyncTemplates() {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS |
		// SQLiteDatabase.OPEN_READWRITE);

		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE isSync = '0'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		// db.close();
		return count;
	}

	public boolean unsyncTemplatesLeft() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE isSync = '0'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		return count != 0;
	}

	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_name);
		DBManager._db.execSQL(sb.toString());
	}

	public void updateIsSync(List<String[]> list) {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openWritableDB();

		StringBuilder sb = new StringBuilder();
		sb.append(cust_id).append(" = ? AND ").append(product_id).append(" = ?");

		ContentValues args = new ContentValues();

		int size = list.size();
		for (int i = 0; i < size; i++) {
			args.put("isSync", list.get(i)[0]);
			DBManager._db.update(table_name, args, sb.toString(), new String[] { list.get(i)[1], list.get(i)[2] });
		}
		// db.close();
	}
}
