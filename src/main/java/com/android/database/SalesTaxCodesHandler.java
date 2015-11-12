package com.android.database;

import android.app.Activity;
import android.database.Cursor;

import com.android.support.DBManager;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class SalesTaxCodesHandler {

	private final String taxcode_id = "taxcode_id";
	private final String taxcode_name = "taxcode_name";
	private final String taxcode_desc = "taxcode_desc";
	private final String taxcode_istaxable = "taxcode_istaxable";
	private final String isactive = "isactive";
	private final String taxcode_update = "taxcode_update";

	private final List<String> attr = Arrays
			.asList(new String[] { taxcode_id, taxcode_name, taxcode_desc, taxcode_istaxable, isactive, taxcode_update });

	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	
	private List<String[]> addrData;
	private Activity activity;
	private List<HashMap<String,Integer>>dictionaryListMap;
	
	private static final String table_name = "SalesTaxCodes";

	public SalesTaxCodesHandler(Activity activity) {
		this.activity = activity;
		attrHash = new HashMap<String, Integer>();
		addrData = new ArrayList<String[]>();
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
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = addrData.size();

			for (int j = 0; j < size; j++) {
				insert.bindString(index(taxcode_id), getData(taxcode_id, j)); // taxcode_id
				insert.bindString(index(taxcode_name), getData(taxcode_name, j)); // taxcode_name
				insert.bindString(index(taxcode_desc), getData(taxcode_desc, j)); // taxcode_desc
				insert.bindString(index(taxcode_istaxable), getData(taxcode_istaxable, j)); // taxcode_istaxable
				insert.bindString(index(isactive), getData(isactive, j)); // isactive
				insert.bindString(index(taxcode_update), getData(taxcode_update, j)); // taxcode_update

				insert.execute();
				insert.clearBindings();

			}
			insert.close();
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.SalesTaxCodesHandler (at Class.insert)]");

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

	
	public String getTaxableTaxCode()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM SalesTaxCodes WHERE taxcode_istaxable = '1' LIMIT 1");
		Cursor c = DBManager._db.rawQuery(sb.toString(), null);
		String taxcode_id = "";
		
		if(c.moveToFirst()){
			taxcode_id = c.getString(c.getColumnIndex("taxcode_id"));
		}
		
		return taxcode_id;
	}
	
	
	public boolean checkIfCustTaxable(String cust_taxable)
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		boolean isTaxable = false;

		String subquery1 = "SELECT taxcode_istaxable FROM ";
		String subquery2 = " WHERE taxcode_id = '";
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(subquery1).append(table_name).append(subquery2).append(cust_taxable).append("'");
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		
		if(cursor.moveToFirst())
		{
			if(cursor.getString(cursor.getColumnIndex(taxcode_istaxable)).equals("1"))
				isTaxable = true;
		}
		
		cursor.close();
		//db.close();
		
		return isTaxable;
		
	}
}
