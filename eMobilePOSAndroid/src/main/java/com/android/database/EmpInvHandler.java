package com.android.database;

import android.content.Context;

import com.android.emobilepos.models.orders.OrderProduct;
import com.crashlytics.android.Crashlytics;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class EmpInvHandler {


    private final String emp_inv_id = "emp_inv_id";
    private final String emp_id = "emp_id";
    private final String prod_id = "prod_id";
    private final String prod_onhand = "prod_onhand";
    private final String emp_update = "emp_update";
    private final String issync = "issync";
    private final String loc_id = "loc_id";

    private final List<String> attr = Arrays.asList(emp_inv_id, emp_id, prod_id, prod_onhand, emp_update, issync, loc_id);

    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;

    private List<String[]> data;
    private List<HashMap<String, Integer>> dictionaryListMap;
    private static final String TABLE_NAME = "EmpInv";

    public EmpInvHandler(Context activity) {
        attrHash = new HashMap<>();
        data = new ArrayList<>();
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
            return data.get(record)[i];
        }
        return "";
    }


    private int index(String tag) {
        return attrHash.get(tag);
    }


    public void insert(List<String[]> insertData, List<HashMap<String, Integer>> dictionary) {
        DBManager.getDatabase().beginTransaction();
        try {

            data = insertData;
            dictionaryListMap = dictionary;
            SQLiteStatement insert = null;
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
            insert = DBManager.getDatabase().compileStatement(sb.toString());

            int size = data.size();

            for (int j = 0; j < size; j++) {
                insert.bindString(index(emp_inv_id), getData(emp_inv_id, j)); // emp_inv_id
                insert.bindString(index(emp_id), getData(emp_id, j)); // emp_id
                insert.bindString(index(prod_id), getData(prod_id, j)); // prod_id
                insert.bindString(index(emp_update), getData(emp_update, j)); // emp_update
                insert.bindString(index(prod_onhand), getData(prod_onhand, j)); // prod_onhand
                insert.bindString(index(issync), getData(issync, j)); // issync
                insert.bindString(index(loc_id), getData(loc_id, j)); // loc_id

                insert.execute();
                insert.clearBindings();

            }
            insert.close();
            DBManager.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.getMessage()).append(" [com.android.emobilepos.EmpInvHandlerHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
        } finally {
            DBManager.getDatabase().endTransaction();
        }
    }

    public void emptyTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(TABLE_NAME);
        DBManager.getDatabase().execSQL(sb.toString());
    }


    public void updateOnHand(List<OrderProduct> orderProducts) {
        try {
            DBManager.getDatabase().beginTransaction();
            SQLiteStatement insert;
            String sb = "UPDATE " + TABLE_NAME + " SET " + prod_onhand + " = prod_onhand" +
                    " - " + "?" + " WHERE " + prod_id + " = " + "?";

            insert = DBManager.getDatabase().compileStatement(sb);
            for (OrderProduct product : orderProducts) {
                insert.bindLong(1, Long.parseLong(product.getOrdprod_qty()));
                insert.bindString(2, product.getProd_id());
                insert.execute();
                insert.clearBindings();
            }
            insert.close();
            DBManager.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.logException(e);
        } finally {
            DBManager.getDatabase().endTransaction();
        }
//        DBManager.getDatabase().execSQL(sb.toString());
    }

}