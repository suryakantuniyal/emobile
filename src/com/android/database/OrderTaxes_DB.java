package com.android.database;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.emobilepos.models.DataTaxes;
import com.android.support.DBManager;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import net.sqlcipher.database.SQLiteStatement;

public class OrderTaxes_DB {
	
	public final static String ord_tax_id = "ord_tax_id";
	public final static String ord_id = "ord_id";
	public final static String tax_name = "tax_name";
	public final static String tax_amount = "tax_amount";
	public final static String tax_rate = "tax_rate";
	
	private final List<String> attr = Arrays.asList(new String[] {ord_tax_id,ord_id,tax_name,tax_amount,tax_rate});
	private HashMap<String, Integer> attrHash;
	private StringBuilder mainSB1,mainSB2;
	private static final String TABLE_NAME = "OrderTaxes";
	private Activity activity;

	
	public OrderTaxes_DB(Activity activity)
	{
		this.activity = activity;
		attrHash = new HashMap<String,Integer>();
		
		mainSB1 = new StringBuilder();
		mainSB2 = new StringBuilder();
		
		initDictionary();
	}
	private void initDictionary() {
		int size = attr.size();
		for (int i = 0; i < size; i++) {
			attrHash.put(attr.get(i), i + 1);
			if ((i + 1) < size) {
				mainSB1.append(attr.get(i)).append(",");
				mainSB2.append("?").append(",");
			} else {
				mainSB1.append(attr.get(i));
				mainSB2.append("?");
			}
		}
	}
	
	private int index(String tag) {
		return attrHash.get(tag);
	}
	

	public void insert(List<DataTaxes> _data,String _ord_id) {
		//SQLiteDatabase db = dbManager.openWritableDB();

		DBManager._db.beginTransaction();

		try {
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT OR REPLACE INTO ").append(TABLE_NAME).append(" (").append(mainSB1.toString()).append(") ").append("VALUES (")
					.append(mainSB2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = _data.size();

			for (int j = 0; j < size; j++) {
				
				insert.bindString(index(ord_tax_id),_data.get(j).get(ord_tax_id));
				insert.bindString(index(ord_id), _ord_id);
				insert.bindString(index(tax_name), _data.get(j).get(tax_name));
				insert.bindString(index(tax_amount), _data.get(j).get(tax_amount));
				insert.bindString(index(tax_rate), _data.get(j).get(tax_rate));
				
				insert.execute();
				insert.clearBindings();
			}
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			Tracker tracker = EasyTracker.getInstance(activity);
			tracker.send(MapBuilder.createException(Log.getStackTraceString(e), false).build());
		} finally {
			DBManager._db.endTransaction();
		}
	}
	
	public void emptyTable() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME);
		DBManager._db.execSQL(sb.toString());
	}
	
	public List<DataTaxes> getOrderTaxes(String _ord_id)
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		List<DataTaxes>list = new ArrayList<DataTaxes>();
		DataTaxes dataTaxes;
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ord_id = ?");
		
		Cursor c = DBManager._db.rawQuery(query.toString(), new String[]{_ord_id});
		
		if(c.moveToFirst())
		{
			int i_tax_name = c.getColumnIndex(tax_name);
			int i_tax_rate = c.getColumnIndex(tax_rate);
			int i_tax_amount = c.getColumnIndex(tax_amount);
			do
			{
				dataTaxes = new DataTaxes();
				
				dataTaxes.set(tax_name, c.getString(i_tax_name));
				dataTaxes.set(tax_rate, c.getString(i_tax_rate));
				dataTaxes.set(tax_amount, new BigDecimal(c.getString(i_tax_amount)).setScale(2, RoundingMode.HALF_UP).toString());
				
				list.add(dataTaxes);
			}while(c.moveToNext());
		}
		
		c.close();
		//db.close();
		
		return list;
	}
}
