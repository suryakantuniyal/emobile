package com.android.database;

import android.app.Activity;
import android.content.Context;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ProdCatXrefHandler {

    public static final String table_name = "ProdCatXref";
    private final String idKey = "idKey";
    private final String prod_id = "prod_id";
    private final String cat_id = "cat_id";
    private final String _update = "_update";
    private final String isactive = "isactive";
    public final List<String> attr = Arrays.asList(idKey, prod_id, cat_id, _update, isactive);
    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;
    private List<String[]> addrData;
    private List<HashMap<String, Integer>> dictionaryListMap;

    public ProdCatXrefHandler(Context activity) {
        attrHash = new HashMap<>();
        addrData = new ArrayList<>();
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
        return "";
    }

    private int index(String tag) {
        return attrHash.get(tag);
    }

    public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
        DBManager.getDatabase().beginTransaction();
        SQLiteStatement insert = null;
        try {

            addrData = data;
            dictionaryListMap = dictionary;

            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ")
                    .append("VALUES (").append(sb2.toString()).append(")");
            insert = DBManager.getDatabase().compileStatement(sb.toString());

            int size = addrData.size();
            for (int j = 0; j < size; j++) {

                insert.bindString(index(idKey), getData(idKey, j)); // idKey
                insert.bindString(index(prod_id), getData(prod_id, j)); // prod_id
                insert.bindString(index(cat_id), getData(cat_id, j)); // cat_id
                insert.bindString(index(_update), getData(_update, j)); // _update
                insert.bindString(index(isactive), getData(isactive, j)); // isactive

                insert.execute();
                insert.clearBindings();

            }
            insert.close();
            DBManager.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.getMessage()).append(" [com.android.emobilepos.ProdCatXrefHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
        } finally {
            if (insert != null) {
                insert.close();

            }
            DBManager.getDatabase().endTransaction();
        }
    }

    public long getDBSize() {
        // SQLiteDatabase db = dbManager.openReadableDB();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT Count(*) FROM ").append(table_name);

        SQLiteStatement stmt = DBManager.getDatabase().compileStatement(sb.toString());
        long count = stmt.simpleQueryForLong();
        stmt.close();
        // db.close();
        return count;
    }

    public void emptyTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(table_name);
        DBManager.getDatabase().execSQL(sb.toString());
    }
}
