package com.android.database;

import android.database.Cursor;

import com.android.emobilepos.holders.TransferInventory_Holder;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TransferInventory_DB {
    public static final String trans_id = "trans_id";
    public static final String prod_id = "prod_id";
    public static final String prod_qty = "prod_qty";


    private static final List<String> attr = Arrays.asList(trans_id, prod_id, prod_qty);

    private static final String TABLE_NAME = "TransferInventory";
    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;


    public TransferInventory_DB() {
        attrHash = new HashMap<>();
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


    private int index(String tag) {
        return attrHash.get(tag);
    }


    public void insert(List<TransferInventory_Holder> inventory) {
        DBManager.getDatabase().beginTransaction();
        try {

            SQLiteStatement insert;
            String sb = "INSERT INTO " + TABLE_NAME + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() +
                    ")";
            insert = DBManager.getDatabase().compileStatement(sb);

            int size = inventory.size();

            for (int i = 0; i < size; i++) {
                insert.bindString(index(trans_id), inventory.get(i).getTrans_id());
                insert.bindString(index(prod_id), inventory.get(i).getProd_id());
                insert.bindString(index(prod_qty), inventory.get(i).getProd_qty());

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
        DBManager.getDatabase().execSQL("DELETE FROM " + TABLE_NAME);
    }


    public Cursor getInventoryTransactions(String _trans_id) {
        return DBManager.getDatabase().rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE trans_id = ?", new String[]{_trans_id});
    }

    public List<HashMap<String, String>> getInventoryTransactionMap(String _trans_id) {
        Cursor c=null;
        try {
            String sb = "SELECT p.prod_id, p.prod_name,ti.prod_qty FROM " + TABLE_NAME + " ti LEFT JOIN Products p ON ti.prod_id = p.prod_id WHERE " +
                    "trans_id = ?";

            c = DBManager.getDatabase().rawQuery(sb, new String[]{_trans_id});

            List<HashMap<String, String>> listMap = new ArrayList<>();
            if (c.moveToFirst()) {
                int i_prod_name = c.getColumnIndex("prod_name");
                int i_prod_qty = c.getColumnIndex("prod_qty");
                int i_prod_id = c.getColumnIndex("prod_id");
                HashMap<String, String> tempMap;
                do {
                    tempMap = new HashMap<>();
                    tempMap.put(prod_id, c.getString(i_prod_id));
                    tempMap.put("prod_name", c.getString(i_prod_name));
                    tempMap.put("prod_qty", c.getString(i_prod_qty));
                    listMap.add(tempMap);
                } while (c.moveToNext());
            }

            c.close();
            return listMap;
        }finally {
            if(c!=null && !c.isClosed())
            {
                c.close();
            }
        }

    }

}
