package com.android.database;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.support.DBManager;
import com.android.support.MyPreferences;

import android.app.Activity;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteStatement;

public class VolumePricesHandler {

	private final String id_key = "id_key";
	private final String prod_id = "prod_id";
	private final String minQty = "minQty";
	private final String maxQty = "maxQty";
	private final String pricelevel_id = "pricelevel_id";
	private final String price = "price";
	private final String isactive = "isactive";

	private final List<String> attr = Arrays
			.asList(new String[] { id_key, prod_id, minQty, maxQty, price, isactive, pricelevel_id });

	private static final String table_name = "VolumePrices";
	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;

	private List<String[]> addrData;
	private List<HashMap<String, Integer>> dictionaryListMap;
	private MyPreferences myPref;
	private Activity activity;

	public VolumePricesHandler(Activity activity) {
		attrHash = new HashMap<String, Integer>();
		this.activity = activity;
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		myPref = new MyPreferences(activity);

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
		return empStr;
	}

	private int index(String tag) {
		return attrHash.get(tag);
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

				insert.bindString(index(id_key), getData(id_key, i)); // id_key
				insert.bindString(index(prod_id), getData(prod_id, i)); // prod_id
				insert.bindString(index(minQty), getData(minQty, i)); // minQty
				insert.bindString(index(maxQty), getData(maxQty, i)); // maxQty
				insert.bindString(index(price), getData(price, i)); // price
				insert.bindString(index(isactive), getData(isactive, i)); // isactive
				insert.bindString(index(pricelevel_id), getData(pricelevel_id, i)); // pricelevel_id

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

	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_name);
		DBManager._db.execSQL(sb.toString());
	}

	public String[] getVolumePrice(String qty, String prod_id) {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		String priceLevelID = new String();

		double tempQty = Double.parseDouble(qty);
		if (tempQty <= 1) {
			qty = "1";
			tempQty = 1;
		}

		if (myPref.isCustSelected())
			priceLevelID = myPref.getCustPriceLevel();
		else
			priceLevelID = myPref.getEmployeePriceLevel();

		sb.append("SELECT * From VolumePrices WHERE prod_id = '");
		sb.append(prod_id).append("' and pricelevel_id = '");
		sb.append(priceLevelID).append("' ORDER BY minQty");

		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		String[] values = new String[2];
		if (cursor.moveToFirst()) {
			int size = cursor.getCount();

			if (size == 1) {
				values[0] = cursor.getString(cursor.getColumnIndex(id_key));
				values[1] = cursor.getString(cursor.getColumnIndex(price));
			} else {
				int i_minQty = cursor.getColumnIndex("minQty");

				double minQTY1 = Double.parseDouble(cursor.getString(i_minQty));
				cursor.moveToNext();
				double minQTY2 = 0.0;
				do {
					minQTY2 = Double.parseDouble(cursor.getString(i_minQty));
					if (tempQty >= minQTY1 && tempQty < minQTY2) {
						cursor.moveToPrevious();
						values[0] = cursor.getString(cursor.getColumnIndex(id_key));
						values[1] = cursor.getString(cursor.getColumnIndex(price));
						break;
					} else if (cursor.isLast()) {
						values[0] = cursor.getString(cursor.getColumnIndex(id_key));
						values[1] = cursor.getString(cursor.getColumnIndex(price));
					}
					minQTY1 = minQTY2;
				} while (cursor.moveToNext());
			}
		}

		cursor.close();
		// db.close();
		return values;

	}
}
