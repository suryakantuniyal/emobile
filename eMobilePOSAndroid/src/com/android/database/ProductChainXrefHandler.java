package com.android.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.support.DBManager;

import android.app.Activity;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteStatement;

public class ProductChainXrefHandler {

	private final String chainKey = "chainKey";
	private final String cust_chain = "cust_chain";
	private final String prod_id = "prod_id";
	private final String over_price_gross = "over_price_gross";
	private final String over_price_net = "over_price_net";
	private final String isactive = "isactive";
	private final String productchain_update = "productchain_update";
	private final String customer_item = "customer_item";

	private final List<String> attr = Arrays.asList(new String[] { chainKey, cust_chain, prod_id, over_price_gross, over_price_net, isactive,
			productchain_update, customer_item });

	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	private List<String[]> addrData;
	private Activity activity;
	private List<HashMap<String,Integer>>dictionaryListMap;
	private static final String table_name = "ProductChainXRef";

	public ProductChainXrefHandler(Activity activity) {
		
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
				insert.bindString(index(chainKey), getData(chainKey, j)); // chainKey
				insert.bindString(index(prod_id), getData(prod_id, j)); // prod_id
				insert.bindString(index(cust_chain), getData(cust_chain, j)); // cust_chain
				insert.bindString(index(over_price_net), getData(over_price_net, j)); // over_price_net
				insert.bindString(index(isactive), getData(isactive, j)); // isactive
				insert.bindString(index(over_price_gross), getData(over_price_gross, j)); // over_price_gross
				insert.bindString(index(productchain_update), getData(productchain_update, j)); // productchain_update
				insert.bindString(index(customer_item), getData(customer_item, j)); // customer_item

				insert.execute();
				insert.clearBindings();

			}
			insert.close();
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.ProdcutChainXrefHandler (at Class.insert)]");

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
	
	
	public long getNumOfCustomers(String custID)
	//called from ProductsHandler no need to open/close db
	{
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) as 'count' FROM ").append(table_name).append(" WHERE cust_chain != '' AND cust_chain = ?");

		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[]{custID});
		long count = 0;
		if(c.moveToFirst())
		{
			count = Long.parseLong(c.getString(c.getColumnIndex("count")));
		}
		/*
		SQLiteStatement stmt = db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();*/
		c.close();

		return count;
	}
}
