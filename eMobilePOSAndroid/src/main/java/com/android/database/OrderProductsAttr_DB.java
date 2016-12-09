package com.android.database;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;

import com.android.emobilepos.models.realms.ProductAttribute;

import net.sqlcipher.database.SQLiteStatement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OrderProductsAttr_DB {

	public final static String ordprod_id = "ordprod_id";
	public final static String attribute_id = "attribute_id";
	public final static String name = "name";
	public final static String value = "value";
	private final List<String> attr = Arrays.asList(ordprod_id,attribute_id,name,value);
	
	private HashMap<String, Integer> attrHash;
	private StringBuilder mainSB1,mainSB2;
	private static final String TABLE_NAME = "OrderProductsAttr";

	public OrderProductsAttr_DB(Context activity)
	{
		attrHash = new HashMap<>();
		
		mainSB1 = new StringBuilder();
		mainSB2 = new StringBuilder();
		new DBManager(activity);
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
	
	public void insert(List<ProductAttribute> data) {

		// SQLiteDatabase db = dbManager.openWritableDB();
		DBManager.getDatabase().beginTransaction();
		try {

			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT OR REPLACE INTO ").append(TABLE_NAME).append(" (").append(mainSB1.toString()).append(") ").append("VALUES (")
					.append(mainSB2.toString()).append(")");
			insert = DBManager.getDatabase().compileStatement(sb.toString());

			int size = data.size();

			for (int i = 0; i < size; i++) {
				insert.bindString(index(ordprod_id), String.valueOf(data.get(i).getProductId())); // addon
				insert.bindString(index(attribute_id), data.get(i).getAttributeId());
				insert.bindString(index(name), data.get(i).getAttributeName());
				insert.bindString(index(value), data.get(i).getValue());

				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager.getDatabase().setTransactionSuccessful();

		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.OrderProductsAttr (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
			DBManager.getDatabase().endTransaction();
		}
		// db.close();
	}
	
	
	public Cursor getOrdProdAttr(String ordprodID)
	{
//		if(db==null||!db.isOpen())
//			db = dbManager.openWritableDB();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE ordprod_id = ?");
		Cursor c = DBManager.getDatabase().rawQuery(sb.toString(),new String[]{ordprodID});
		return c;
	}
	
	public void deleteOrderProduct(String _ordprod_id)
	{
		DBManager.getDatabase().delete(TABLE_NAME, "ordprod_id = ?", new String[]{_ordprod_id});
	}
}
