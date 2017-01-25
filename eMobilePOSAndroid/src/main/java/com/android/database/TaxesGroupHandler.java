package com.android.database;

import android.app.Activity;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TaxesGroupHandler {

	private final static String taxGroupKey = "taxGroupKey";
	private final static String taxGroupId = "taxGroupId";
	private final static String taxId = "taxId";
	private final static String taxcode_id = "taxcode_id";
	private final static String tax_rate = "tax_rate";
	private final static String taxLowRange = "taxLowRange";
	private final static String taxHighRange = "taxHighRange";
	private final static String taxgroup_update = "taxgroup_update";
	private final static String isactive = "isactive";

	private static final List<String> attr = Arrays.asList(taxGroupKey, taxGroupId, taxId, taxcode_id, tax_rate, taxLowRange,
			taxHighRange, taxgroup_update, isactive);

	private StringBuilder sb1, sb2;
	private HashMap<String, Integer> attrHash;
	private List<String[]> addrData;
	private List<HashMap<String,Integer>>dictionaryListMap;
	
	private static final String table_name = "Taxes_Group";

	public TaxesGroupHandler(Activity activity) {
		attrHash = new HashMap<String, Integer>();
		addrData = new ArrayList<String[]>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
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
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager.getDatabase().compileStatement(sb.toString());

			int size = addrData.size();

			for (int j = 0; j < size; j++) {
				insert.bindString(index(taxGroupKey), getData(taxGroupKey, j)); // taxGroupKey
				insert.bindString(index(taxGroupId), getData(taxGroupId, j)); // taxGroupId
				insert.bindString(index(taxId), getData(taxId, j)); // taxId
				insert.bindString(index(taxcode_id), getData(taxcode_id, j)); // taxcode_id
				insert.bindString(index(tax_rate), getData(tax_rate, j)); // tax_rate
				insert.bindString(index(isactive), getData(isactive, j)); // isactive
				insert.bindString(index(taxLowRange), getData(taxLowRange, j)); // taxLowRange
				insert.bindString(index(taxHighRange), getData(taxHighRange, j)); // taxHighRange
				insert.bindString(index(taxgroup_update), getData(taxgroup_update, j)); // taxgroup_update

				insert.execute();
				insert.clearBindings();

			}
			insert.close();
			DBManager.getDatabase().setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.TaxesGroupHandler (at Class.insert)]");

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
	
	
	public List<HashMap<String,String>> getIndividualTaxes(String _tax_group_id,String _taxcode_id)
	{
		//SQLiteDatabase db = dbManager.openReadableDB();

		//String query = "SELECT taxId,taxcode_id AS 'tax_name',tax_rate FROM Taxes_Group WHERE taxGroupId = ? AND taxcode_id = ?";
		String query = "SELECT * FROM Taxes WHERE tax_id IN (SELECT taxId FROM TAXES_GROUP WHERE taxGroupId = ? AND taxcode_id = ?);";
		
		Cursor c = DBManager.getDatabase().rawQuery(query, new String[]{_tax_group_id,_taxcode_id});
		
		List<HashMap<String,String>>listMap = new ArrayList<HashMap<String,String>>();
		HashMap<String,String>tempMap;
		if(c.moveToFirst())
		{
			int i_tax_id = c.getColumnIndex("tax_id");
			int i_tax_name = c.getColumnIndex("tax_name");
			int i_tax_rate = c.getColumnIndex("tax_rate");
			tempMap = new HashMap<String,String>();
			do
			{
				tempMap.put(taxId, c.getString(i_tax_id));
				tempMap.put("tax_name", c.getString(i_tax_name));
				tempMap.put(tax_rate, c.getString(i_tax_rate));
				
				listMap.add(tempMap);
				tempMap = new HashMap<String,String>();
			}while(c.moveToNext());
		}
		
		c.close();
		//db.close();
		
		return listMap;
	}
	

}
