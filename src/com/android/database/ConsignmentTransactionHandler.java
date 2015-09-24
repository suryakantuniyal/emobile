package com.android.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.support.ConsignmentTransaction;
import com.android.support.DBManager;
import com.android.support.MyPreferences;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteStatement;

public class ConsignmentTransactionHandler {
	private final String Cons_ID = "Cons_ID";
	private final String ConsTrans_ID = "ConsTrans_ID";
	private final String ConsEmp_ID = "ConsEmp_ID";
	private final String ConsCust_ID = "ConsCust_ID";
	private final String ConsInvoice_ID = "ConsInvoice_ID";
	private final String ConsReturn_ID = "ConsReturn_ID";
	private final String ConsPickup_ID = "ConsPickup_ID";
	private final String ConsDispatch_ID = "ConsDispatch_ID";
	private final String ConsProd_ID = "ConsProd_ID";
	private final String ConsOriginal_Qty = "ConsOriginal_Qty";
	private final String ConsStock_Qty = "ConsStock_Qty";
	private final String ConsInvoice_Qty = "ConsInvoice_Qty";
	private final String ConsReturn_Qty = "ConsReturn_Qty";
	private final String ConsDispatch_Qty = "ConsDispatch_Qty";
	private final String ConsPickup_Qty = "ConsPickup_Qty";
	private final String ConsNew_Qty = "ConsNew_Qty";
	private final String Cons_timecreated = "Cons_timecreated";
	private final String is_synched = "is_synched";

	private final List<String> attr = Arrays.asList(new String[] { Cons_ID, ConsTrans_ID, ConsEmp_ID, ConsCust_ID,
			ConsInvoice_ID, ConsReturn_ID, ConsPickup_ID, ConsDispatch_ID, ConsProd_ID, ConsOriginal_Qty, ConsStock_Qty,
			ConsInvoice_Qty, ConsReturn_Qty, ConsDispatch_Qty, ConsPickup_Qty, ConsNew_Qty, Cons_timecreated });

	private StringBuilder sb1, sb2;
	private final HashMap<String, Integer> attrHash;

	private MyPreferences myPref;

	private final String TABLE_NAME = "ConsignmentTransaction";

	public ConsignmentTransactionHandler(Activity activity) {
		attrHash = new HashMap<String, Integer>();
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

	public void insert(List<ConsignmentTransaction> list) {
		// final SQLiteDatabase db = dbManager.openWritableDB();

		ContentValues values = new ContentValues();
		int size = list.size();

		for (int i = 0; i < size; i++) {
			values.clear();

			ConsignmentTransaction trans = list.get(i);
			values.put(ConsTrans_ID, trans.ConsTrans_ID);
			values.put(ConsEmp_ID, trans.ConsEmp_ID);
			values.put(ConsCust_ID, trans.ConsCust_ID);
			values.put(ConsInvoice_ID, trans.ConsInvoice_ID);
			values.put(ConsReturn_ID, trans.ConsReturn_ID);
			values.put(ConsDispatch_ID, trans.ConsDispatch_ID);
			values.put(ConsPickup_ID, trans.ConsPickup_ID);
			values.put(ConsProd_ID, trans.ConsProd_ID);
			values.put(ConsOriginal_Qty, trans.ConsOriginal_Qty);
			values.put(ConsStock_Qty, trans.ConsStock_Qty);
			values.put(ConsInvoice_Qty, trans.ConsInvoice_Qty);
			values.put(ConsReturn_Qty, trans.ConsReturn_Qty);
			values.put(ConsDispatch_Qty, trans.ConsDispatch_Qty);
			values.put(ConsPickup_Qty, trans.ConsPickup_Qty);
			values.put(ConsNew_Qty, trans.ConsNew_Qty);
			values.put(Cons_timecreated, trans.Cons_timecreated);
			// values.put(is_synched,list.get(i).getSetData(is_synched, true,
			// null));

			DBManager._db.insert(TABLE_NAME, null, values);

		}

		myPref.setLastConsTransID(list.get(0).ConsTrans_ID);
		// db.close();
	}

	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME);
		DBManager._db.execSQL(sb.toString());
	}

	public List<ConsignmentTransaction> getLastConsTransaction() {
		// SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();
		String priceLevelID = new String();
		if (myPref.isCustSelected())
			priceLevelID = myPref.getCustPriceLevel();
		else
			priceLevelID = myPref.getEmployeePriceLevel();
		/*
		 * sb.append(
		 * "SELECT * FROM CustomerInventory ci LEFT OUTER JOIN ConsignmentTransaction ct ON ci.prod_id = ct.ConsProd_ID  "
		 * ); sb.append(
		 * "AND ci.cust_id = ct.ConsCust_ID WHERE ct.ConsCust_ID = '");
		 * sb.append(myPref.getCustID()).append(
		 * "'  GROUP BY ct.ConsProd_ID ORDER BY ct.Cons_timecreated DESC ");
		 */

		sb.append(
				"SELECT ct.ConsProd_ID,ct.ConsOriginal_Qty,ct.ConsStock_Qty,ct.ConsReturn_Qty,ct.ConsInvoice_Qty,ct.ConsDispatch_Qty,");
		sb.append(
				"ct.ConsNew_Qty, p.prod_price as 'master_price',vp.price as 'volume_price', ch.over_price_net as 'chain_price',");
		sb.append("pl.pricelevel_price FROM CustomerInventory ci LEFT OUTER JOIN ConsignmentTransaction ct ON ");
		sb.append(
				"ci.prod_id = ct.ConsProd_ID  AND ci.cust_id = ct.ConsCust_ID LEFT OUTER JOIN Products p ON p.prod_id = ci.prod_id ");
		sb.append(
				"LEFT OUTER JOIN VolumePrices vp ON ci.prod_id = vp.prod_id AND '1' BETWEEN vp.minQty AND vp.maxQty ");
		sb.append("AND vp.pricelevel_id = '").append(priceLevelID).append("' LEFT OUTER JOIN PriceLevelItems pl ON ");
		sb.append("ci.prod_id = pl.pricelevel_prod_id AND pl.pricelevel_id = '").append(priceLevelID)
				.append("' LEFT OUTER JOIN ProductChainXRef ch ");
		sb.append("ON ci.prod_id = ch.prod_id AND ch.cust_chain = ci.cust_id WHERE ct.ConsCust_ID = ?  ");
		sb.append("GROUP BY ct.ConsProd_ID ORDER BY ct.Cons_timecreated DESC ");

		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[] { myPref.getCustID() });
		List<ConsignmentTransaction> ctList = new ArrayList<ConsignmentTransaction>();
		ConsignmentTransaction ct = new ConsignmentTransaction();
		if (c.moveToFirst()) {
			int i_prod_id = c.getColumnIndex(ConsProd_ID);
			int i_original_qty = c.getColumnIndex(ConsOriginal_Qty);
			int i_stock_qty = c.getColumnIndex(ConsStock_Qty);
			int i_return_qty = c.getColumnIndex(ConsReturn_Qty);
			int i_invoice_qty = c.getColumnIndex(ConsInvoice_Qty);
			int i_dispatch_qty = c.getColumnIndex(ConsDispatch_Qty);
			int i_new_qty = c.getColumnIndex(ConsNew_Qty);
			int i_volume_price = c.getColumnIndex("volume_price");
			int i_pricelevel_price = c.getColumnIndex("pricelevel_price");
			int i_chain_price = c.getColumnIndex("chain_price");
			int i_master_price = c.getColumnIndex("master_price");

			String tempPrice;
			double temp = 0;
			do {
				ct.ConsProd_ID = c.getString(i_prod_id);
				ct.ConsOriginal_Qty = c.getString(i_original_qty);
				ct.ConsStock_Qty = c.getString(i_stock_qty);
				ct.ConsReturn_Qty = c.getString(i_return_qty);
				ct.ConsInvoice_Qty = c.getString(i_invoice_qty);
				ct.ConsDispatch_Qty = c.getString(i_dispatch_qty);
				ct.ConsNew_Qty = c.getString(i_new_qty);

				tempPrice = c.getString(i_volume_price);
				if (tempPrice == null || tempPrice.isEmpty()) {
					tempPrice = c.getString(i_pricelevel_price);
					if (tempPrice == null || tempPrice.isEmpty()) {
						tempPrice = c.getString(i_chain_price);

						if (tempPrice == null || tempPrice.isEmpty()) {
							tempPrice = c.getString(i_master_price);
							if (tempPrice == null || tempPrice.isEmpty())
								tempPrice = "0";
						}
					}
				}

				temp = Double.parseDouble(tempPrice) * Double.parseDouble(c.getString(i_invoice_qty));
				ct.ConsOriginal_Qty = c.getString(i_original_qty);
				ct.invoice_total = Double.toString(temp);
				ctList.add(ct);
				ct = new ConsignmentTransaction();

			} while (c.moveToNext());
		}

		c.close();
		// db.close();
		return ctList;

	}

	public long getDBSize() {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(TABLE_NAME);

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		// db.close();
		return count;
	}

	public Cursor getUnsychedItems() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ").append(sb1.toString()).append(" FROM ").append(TABLE_NAME)
				.append(" WHERE is_synched = '1'");
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

		return cursor;
	}

	public long getNumUnsyncItems() {
		// SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(DISTINCT ConsTrans_ID) FROM ").append(TABLE_NAME).append(" WHERE is_synched = '1'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		// db.close();
		return count;
	}

	public void updateIsSync(List<String[]> list) {
		// SQLiteDatabase db = dbManager.openWritableDB();

		StringBuilder sb = new StringBuilder();
		sb.append(ConsTrans_ID).append(" = ?");

		ContentValues args = new ContentValues();

		int size = list.size();
		for (int i = 0; i < size; i++) {
			args.put(is_synched, list.get(i)[0]);
			DBManager._db.update(TABLE_NAME, args, sb.toString(), new String[] { list.get(i)[1] });
		}
		// db.close();
	}

	public boolean unsyncConsignmentsLeft() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(DISTINCT ConsTrans_ID) FROM ").append(TABLE_NAME).append(" WHERE is_synched = '1'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		if (count == 0)
			return false;
		return true;
	}

	public Cursor getConsignmentCursor(boolean typePickup) {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();

		sb.append(
				"SELECT ConsTrans_ID as '_id', c.cust_name,ct.is_synched,Cons_timecreated FROM ConsignmentTransaction ct ");
		sb.append("LEFT OUTER JOIN Customers c ON ct.ConsCust_ID = c.cust_id ");

		if (typePickup) {
			sb.append("WHERE ConsPickup_ID != '' ");
		} else {
			sb.append(
					"WHERE (ConsInvoice_ID != '' OR ConsReturn_ID != '' OR ConsDispatch_ID != '' OR (ConsInvoice_ID = '' AND ConsReturn_ID = '' AND ConsDispatch_ID = '' AND ConsPickup_ID = '')) ");
		}
		sb.append("GROUP BY ConsTrans_ID ORDER BY Cons_timecreated DESC");

		Cursor c = DBManager._db.rawQuery(sb.toString(), null);
		c.moveToFirst();
		// db.close();
		return c;
	}

	public HashMap<String, String> getConsignmentSummaryDetails(String _ConsTrans_ID, boolean isPickup) {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		HashMap<String, String> map = new HashMap<String, String>();
		sb.append("SELECT sum(ConsInvoice_Qty) as 'total_items_sold',sum(ConsReturn_Qty) as 'total_items_returned', ");
		sb.append("sum(ConsDispatch_Qty) as 'total_items_dispatched',count(*) as 'total_line_items',");
		sb.append(
				"sum((((ConsOriginal_Qty-ConsStock_Qty)*p.prod_price)-(ConsReturn_Qty*p.prod_price))) as 'total_grand_total', ");
		sb.append(
				"cs.encoded_signature FROM ConsignmentTransaction ct LEFT OUTER JOIN Products p ON ct.ConsProd_ID = p.prod_id ");
		sb.append(
				"LEFT OUTER JOIN ConsignmentSignatures cs ON cs.ConsTrans_ID = ct.ConsTrans_ID WHERE ct.ConsTrans_ID = ?");

		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[] { _ConsTrans_ID });

		if (c.moveToFirst()) {
			map.put("ConsTrans_ID", _ConsTrans_ID);
			map.put("total_items_sold", c.getString(c.getColumnIndex("total_items_sold")));
			map.put("total_items_returned", c.getString(c.getColumnIndex("total_items_returned")));
			map.put("total_items_dispatched", c.getString(c.getColumnIndex("total_items_dispatched")));
			map.put("total_line_items", c.getString(c.getColumnIndex("total_line_items")));
			if (!isPickup)
				map.put("total_grand_total", c.getString(c.getColumnIndex("total_grand_total")));
			else
				map.put("total_grand_total", "0");

			map.put("encoded_signature", c.getString(c.getColumnIndex("encoded_signature")));
		}
		c.close();
		// db.close();
		return map;
	}

	public Cursor getConsignmentItemDetails(String _ConsTrans_ID) {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();

		sb.append(
				"SELECT ConsOriginal_Qty,ConsStock_Qty,ConsReturn_Qty,ConsInvoice_Qty,ConsDispatch_Qty,ConsNew_Qty,ConsPickup_Qty,ci.price as 'price', ");
		sb.append(
				"((ConsOriginal_Qty-ConsStock_Qty)*ci.price) as 'item_subtotal', (ConsReturn_Qty*ci.price) as 'credit_memo', ");
		sb.append(
				"(((ConsOriginal_Qty-ConsStock_Qty)*ci.price)-(ConsReturn_Qty*ci.price)) as 'item_total',pi.prod_img_name,p.prod_name,p.prod_desc ");
		sb.append(
				"FROM ConsignmentTransaction ct  LEFT OUTER JOIN CustomerInventory ci ON ct.ConsProd_ID = ci.prod_id AND ct.ConsCust_ID = ");
		sb.append(
				"ci.cust_id LEFT OUTER JOIN Products_Images pi ON ct.ConsProd_ID = pi.prod_id AND pi.type = 'I' LEFT OUTER JOIN Products p ON ");
		sb.append("ct.ConsProd_ID = p.prod_id WHERE ConsTrans_ID = ? ORDER BY p.prod_name ASC");

		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[] { _ConsTrans_ID });
		c.moveToFirst();
		// db.close();
		return c;
	}

}
