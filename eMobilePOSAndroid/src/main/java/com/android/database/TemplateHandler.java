package com.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.salesassociates.Template;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;

import net.sqlcipher.database.SQLiteStatement;

import java.math.BigDecimal;
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
	private Context activity;
//	private Global global;

	public TemplateHandler(Context activity) {
		// global = (Global) activity.getApplication();
//		global = (Global) activity.getApplication();
		attrHash = new HashMap<String, Integer>();
		this.activity = activity;
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

	public void insert(String custID, List<OrderProduct> orderProducts) {

		DBManager.getDatabase().beginTransaction();
		try {

            SQLiteStatement insert;
            String sb = "INSERT INTO " + table_name + " (" + sb1.toString() + ") " +
                    "VALUES (" + sb2.toString() + ")";

			DBManager.getDatabase().execSQL("DELETE FROM Templates WHERE cust_id='" + custID + "'");
            insert = DBManager.getDatabase().compileStatement(sb);

			int size = orderProducts.size();
			for (int i = 0; i < size; i++) {
				OrderProduct prod = orderProducts.get(i);
				insert.bindString(index(_id), empStr); // _id
				insert.bindString(index(cust_id), custID); // cust_id
                insert.bindString(index(product_id), prod.getProd_id() == null ? "" : prod.getProd_id());
                insert.bindString(index(prod_sku), prod.getProd_sku() == null ? "" : prod.getProd_sku());
                insert.bindString(index(prod_upc), prod.getProd_upc() == null ? "" : prod.getProd_upc());
                insert.bindString(index(quantity), prod.getOrdprod_qty() == null ? "0" : prod.getOrdprod_qty());
                insert.bindString(index(price_level_id), prod.getPricelevel_id() == null ? "" : prod.getPricelevel_id());
                insert.bindString(index(price_level), prod.getPriceLevelName() == null ? "" : prod.getPriceLevelName());
                insert.bindString(index(name), prod.getOrdprod_name() == null ? "" : prod.getOrdprod_name());
                insert.bindString(index(overwrite_price), prod.getOverwrite_price() == null ? prod.getProd_price() : prod.getOverwrite_price().toString());
                insert.bindString(index(_update), "");
				insert.bindString(index(isactive), "true");
				insert.bindString(index(isSync), "0");
                insert.bindString(index(price),  prod.getProd_price() == null ? "0" : prod.getProd_price());


                insert.execute();
                insert.clearBindings();
			}
			insert.close();
			DBManager.getDatabase().setTransactionSuccessful();

		} catch (Exception e) {

		} finally {
			DBManager.getDatabase().endTransaction();
		}
	}

	public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager.getDatabase().beginTransaction();
		try {

			addrData = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert;
			String sb = "INSERT INTO " + table_name + " (" + sb1.toString() + ") " +
					"VALUES (" + sb2.toString() + ")";
			insert = DBManager.getDatabase().compileStatement(sb);

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
			DBManager.getDatabase().setTransactionSuccessful();
		} catch (Exception e) {
		} finally {
			DBManager.getDatabase().endTransaction();
		}
	}

    public List<Template> getTemplate(String custID) {
        List<Template> orderList = new ArrayList<>();
        GenerateNewID generator = new GenerateNewID(activity);
		StringBuilder sb = new StringBuilder();
		Global.lastOrdID = generator.getNextID(IdType.ORDER_ID);
		sb.append(
                "SELECT t.product_id,t.name,t.overwrite_price,t.price,t.quantity,p.prod_desc, p.prod_sku, " +
                        "p.prod_upc,IFNULL(s.taxcode_istaxable,'1') as 'prod_istaxable'  " +
                        "FROM Templates t ");
        sb.append(
                "LEFT JOIN Products p ON t.product_id = p.prod_id " +
                        "LEFT JOIN SalesTaxCodes s ON p.prod_taxcode = s.taxcode_id  " +
                        "WHERE cust_id = ?");
        Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), new String[] { custID });
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
            Template template;
            do {
                template = new Template();
                template.setProductId(cursor.getString(i_prod_id));
                template.setProductSku(cursor.getString(i_prod_sku));
                template.setProductUpc(cursor.getString(i_prod_upc));
                template.setProductName(cursor.getString(i_prod_name));
                template.setOveritePrice(cursor.getString(i_overwrite_price));
                template.setOrdProductQty(cursor.getString(i_ordprod_qty));
                template.setProductPrice(cursor.getString(i_prod_price));
                template.setProductIsTaxable(cursor.getString(i_prod_istaxable));
                template.setOrderId(Global.lastOrdID);
                template.setOrdProductId(UUID.randomUUID().toString());
                BigDecimal total = Global.getBigDecimalNum(template.getOrdProductQty())
                        .multiply(Global.getBigDecimalNum(template.getOveritePrice()));
                template.setItemTotal(total);
                template.setItemSubtotal(total);
                template.setOrdProductDescription(cursor.getString(i_prod_desc));
                orderList.add(template);
            } while (cursor.moveToNext());
		}
		cursor.close();
		return orderList;
	}

	public Cursor getUnsyncTemplates() {
        return DBManager.getDatabase().rawQuery("SELECT * FROM Templates WHERE isSync = '0'", null);

	}

	public long getNumUnsyncTemplates() {
        SQLiteStatement stmt = DBManager.getDatabase().compileStatement("SELECT Count(*) FROM " + table_name + " WHERE isSync = '0'");
        long count = stmt.simpleQueryForLong();
		stmt.close();
		return count;
	}

	public boolean unsyncTemplatesLeft() {

        SQLiteStatement stmt = DBManager.getDatabase().compileStatement("SELECT Count(*) FROM " + table_name + " WHERE isSync = '0'");
        long count = stmt.simpleQueryForLong();
		stmt.close();
		return count != 0;
	}

	public void emptyTable() {
        DBManager.getDatabase().execSQL("DELETE FROM " + table_name);
    }

	public void updateIsSync(List<String[]> list) {
		StringBuilder sb = new StringBuilder();
		sb.append(cust_id).append(" = ? AND ").append(product_id).append(" = ?");
		ContentValues args = new ContentValues();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			args.put("isSync", list.get(i)[0]);
			DBManager.getDatabase().update(table_name, args, sb.toString(), new String[] { list.get(i)[1], list.get(i)[2] });
		}
	}
}
