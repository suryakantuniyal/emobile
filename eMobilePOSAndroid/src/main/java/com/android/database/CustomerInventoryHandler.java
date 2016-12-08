package com.android.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;

import com.android.dao.AssignEmployeeDAO;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.support.CustomerInventory;
import com.android.support.Global;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CustomerInventoryHandler {
	private final String consignment_id = "consignment_id";
	private final String cust_id = "cust_id";
	private final String prod_id = "prod_id";
	private final String qty = "qty";
	private final String price = "price";
	private final String cust_update = "cust_update";

	private final String prod_name = "prod_name";

	private final String is_synched = "is_synched";

	private final List<String> attr = Arrays
			.asList(cust_id, prod_id, qty, price, prod_name, cust_update);

	private StringBuilder sb1, sb2;
	private final HashMap<String, Integer> attrHash;
	private List<String[]> data;
	private MyPreferences myPref;
	private List<HashMap<String, Integer>> dictionaryListMap;
	private final String TABLE_NAME = "CustomerInventory";

	public CustomerInventoryHandler(Activity activity) {
		attrHash = new HashMap<String, Integer>();
		data = new ArrayList<String[]>();
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

	private int index(String tag) {
		return attrHash.get(tag);
	}

	private String getData(String tag, int record) {
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return data.get(record)[i];
		}
		return "";
	}

	public void insertUpdate(List<CustomerInventory> list) {
		// final SQLiteDatabase db = dbManager.openWritableDB();

		Cursor cursor = null;
		StringBuilder sb = new StringBuilder();

		String consignmentID = null;
		ContentValues values = new ContentValues();
		int size = list.size();

		for (int i = 0; i < size; i++) {
			values.clear();
			sb.setLength(0);
			CustomerInventory inv = list.get(i);

			sb.append("SELECT consignment_id FROM ").append(TABLE_NAME).append(" WHERE ");
			sb.append(cust_id).append(" = '").append(inv.cust_id).append("' AND ");
			sb.append(prod_id).append(" = '").append(inv.prod_id).append("'");
			cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);
			if (cursor.moveToFirst())
				consignmentID = cursor.getString(0);

			if (consignmentID != null) {
				sb.setLength(0);
				sb.append(consignment_id).append(" = ?");

				values.put(qty, inv.qty);
				values.put(price, inv.price);
				values.put(cust_update, inv.cust_update);
				values.put(prod_name, inv.prod_name);
				values.put(is_synched, inv.is_synched);

				DBManager.getDatabase().update(TABLE_NAME, values, sb.toString(), new String[] { consignmentID });

				consignmentID = null;
			} else {
				values.put(cust_id, inv.cust_id);
				values.put(prod_id, inv.prod_id);
				values.put(qty, inv.qty);
				values.put(price, inv.price);
				values.put(prod_name, inv.prod_name);
				values.put(cust_update, inv.cust_update);
				values.put(is_synched, inv.is_synched);

				DBManager.getDatabase().insert(TABLE_NAME, null, values);
			}
			cursor.close();
		}
		// db.close();
	}

	public void insert(List<String[]> Data, List<HashMap<String, Integer>> dictionary) {
		DBManager.getDatabase().beginTransaction();
		try {
			data = Data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(sb1.toString()).append(",is_synched) ")
					.append("VALUES (").append(sb2.toString()).append(",?)");
			insert = DBManager.getDatabase().compileStatement(sb.toString());

			int size = data.size();
			int offsetIndex = attr.size() + 1;
			for (int i = 0; i < size; i++) {
				// tempMap = prodDBHandler.getProductMap(db,
				// getData(prod_id,i));

				insert.bindString(index(prod_id), getData(prod_id, i)); // cust_id
				insert.bindString(index(cust_id), getData(cust_id, i));
				insert.bindString(index(qty), getData(qty, i));
				insert.bindString(index(price), getData(price, i));
				insert.bindString(index(cust_update), getData(cust_update, i));
				insert.bindString(offsetIndex, "1");

				// if(tempMap.get("prod_name")!=null)
				// insert.bindString(index(prod_name),
				// tempMap.get("prod_name"));

				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager.getDatabase().setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBManager.getDatabase().endTransaction();
		}
	}

	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME);
		DBManager.getDatabase().execSQL(sb.toString());
	}

	public Cursor getCustomerInventoryCursor() {
		// SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();

		/*
		 * sb.append(
		 * "SELECT prod_id as '_id', prod_name,qty,price,cust_update,ROUND(qty*price,2) as 'total' FROM "
		 * ).append(TABLE_NAME).append(" WHERE cust_id = '");
		 * sb.append(myPref.getCustID()).append("'");
		 */

		/*
		 * sb.append(
		 * "SELECT ci.prod_id as '_id', p.prod_name,ci.qty,ci.price,ci.cust_update,ROUND(ci.qty*ci.price,2) as 'total' FROM CustomerInventory "
		 * ); sb.append(
		 * "ci LEFT OUTER JOIN Products p ON ci.prod_id = p.prod_id WHERE ci.cust_id = '"
		 * ).append(myPref.getCustID()).append("'");
		 */

		String priceLevelID = new String();
		if (myPref.isCustSelected())
			priceLevelID = myPref.getCustPriceLevel();
		else {
			AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();

			priceLevelID = assignEmployee.getPricelevelId();
		}

		sb.append(
				"SELECT ci.prod_id as '_id', p.prod_name,ci.qty,ci.cust_update,ci.price as 'cust_price',p.prod_price as 'master_price',vp.price as 'volume_price', ch.over_price_net as 'chain_price',");
		sb.append(
				"pl.pricelevel_price FROM CustomerInventory ci LEFT OUTER JOIN Products p ON ci.prod_id = p.prod_id LEFT OUTER JOIN VolumePrices vp ON ");
		sb.append("ci.prod_id = vp.prod_id AND '1' BETWEEN vp.minQty AND vp.maxQty AND vp.pricelevel_id = ? ");
		sb.append(
				" LEFT OUTER JOIN PriceLevelItems pl ON ci.prod_id = pl.pricelevel_prod_id AND pl.pricelevel_id = ? ");
		sb.append(
				" LEFT OUTER JOIN ProductChainXRef ch ON ci.prod_id = ch.prod_id AND ch.cust_chain = ci.cust_id WHERE ci.cust_id = ?");

		Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(),
				new String[] { priceLevelID, priceLevelID, myPref.getCustID() });
		cursor.moveToFirst();
		// db.close();
		return cursor;
	}

	public void getCustomerInventory() {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		/*
		 * sb.append("SELECT * FROM ").append(TABLE_NAME).append(
		 * " WHERE cust_id = '"); sb.append(myPref.getCustID()).append("'");
		 */

		/*
		 * sb.append(
		 * "SELECT ci.prod_id, p.prod_name,ci.qty,ci.price FROM CustomerInventory ci LEFT OUTER JOIN Products p ON ci.prod_id = p.prod_id "
		 * ); sb.append("WHERE cust_id = '"
		 * ).append(myPref.getCustID()).append("'");
		 */

		String priceLevelID = new String();
		if (myPref.isCustSelected())
			priceLevelID = myPref.getCustPriceLevel();
		else {
			AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
			priceLevelID = assignEmployee.getPricelevelId();
		}

		sb.append(
				"SELECT ci.prod_id, p.prod_name,ci.qty,p.prod_price as 'master_price',vp.price as 'volume_price', ch.over_price_net as 'chain_price'");
		sb.append(
				",pl.pricelevel_price FROM CustomerInventory ci LEFT OUTER JOIN Products p ON ci.prod_id = p.prod_id LEFT OUTER JOIN ");
		sb.append(
				"VolumePrices vp ON ci.prod_id = vp.prod_id AND '1' BETWEEN vp.minQty AND vp.maxQty AND vp.pricelevel_id = ?");
		sb.append(" LEFT OUTER JOIN PriceLevelItems pl ON ci.prod_id = pl.pricelevel_prod_id AND pl.pricelevel_id = ?");
		sb.append(
				" LEFT OUTER JOIN ProductChainXRef ch ON ci.prod_id = ch.prod_id AND ch.cust_chain = ci.cust_id WHERE ci.cust_id = ? AND qty != '0'");

		Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(),
				new String[] { priceLevelID, priceLevelID, myPref.getCustID() });
		HashMap<String, String[]> tempMap = new HashMap<String, String[]>();
		List<String> keys = new ArrayList<String>();
		if (cursor.moveToFirst()) {
			int i_prod_id = cursor.getColumnIndex(prod_id);
			int i_qty = cursor.getColumnIndex(qty);
			int i_prod_name = cursor.getColumnIndex(prod_name);
			int i_volume_price = cursor.getColumnIndex("volume_price");
			int i_pricelevel_price = cursor.getColumnIndex("pricelevel_price");
			int i_chain_price = cursor.getColumnIndex("chain_price");
			int i_master_price = cursor.getColumnIndex("master_price");
			String[] values = new String[4];

			String tempPrice;
			do {
				values[0] = cursor.getString(i_prod_id);
				values[1] = cursor.getString(i_prod_name);
				values[2] = cursor.getString(i_qty);

				tempPrice = cursor.getString(i_volume_price);
				if (tempPrice == null || tempPrice.isEmpty()) {
					tempPrice = cursor.getString(i_pricelevel_price);
					if (tempPrice == null || tempPrice.isEmpty()) {
						tempPrice = cursor.getString(i_chain_price);

						if (tempPrice == null || tempPrice.isEmpty()) {
							tempPrice = cursor.getString(i_master_price);
							if (tempPrice == null || tempPrice.isEmpty())
								tempPrice = "0";
						}
					}
				}
				values[3] = tempPrice;

				tempMap.put(values[0], values);
				keys.add(values[0]);
				values = new String[4];
			} while (cursor.moveToNext());
		}

		Global.custInventoryMap = tempMap;
		Global.custInventoryKey = keys;
		cursor.close();
		// db.close();
	}

	public double getProdQty(String prodID) {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ").append(qty).append(" FROM ").append(TABLE_NAME)
				.append(" WHERE prod_id = ? AND cust_id = ?");

		Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), new String[] { prod_id, myPref.getCustID() });
		double value = 0.0;

		if (cursor.moveToFirst()) {
			value = cursor.getDouble(0);
		}

		cursor.close();
		// db.close();
		return value;
	}

	public Cursor getUnsychedItems() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ").append(sb1.toString()).append(" FROM ").append(TABLE_NAME)
				.append(" WHERE is_synched = '0'");
		Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);

		return cursor;
	}

	public long getNumUnsyncItems() {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);
		// SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(TABLE_NAME).append(" WHERE is_synched = '0'");

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
		sb.append(cust_id).append(" = ?").append(" AND ").append(prod_id).append(" = ?");

		ContentValues args = new ContentValues();

		int size = list.size();
		for (int i = 0; i < size; i++) {
			if (list.get(i)[0].equals("0"))
				args.put(is_synched, "1");
			else
				args.put(is_synched, "0");
			DBManager.getDatabase().update(TABLE_NAME, args, sb.toString(), new String[] { list.get(i)[1], list.get(i)[2] });
		}
		// db.close();
	}

}
