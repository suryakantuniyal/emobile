package com.android.database;

import android.app.Activity;

import com.android.emobilepos.models.ProductAlias;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ProductAliases_DB {
    public final static String prod_id = "prod_id";
    public final static String prod_alias = "prod_alias";

    private final List<String> attr = Arrays.asList(prod_id, prod_alias);
    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;
    private List<String[]> addrData;
    private List<HashMap<String, Integer>> dictionaryListMap;

    private static final String table_name = "ProductAliases";

    public ProductAliases_DB(Activity activity) {
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
        return "";
    }

    private int index(String tag) {
        return attrHash.get(tag);
    }


    public void insert(List<ProductAlias> aliases) {
        DBManager._db.beginTransaction();
        try {
            SQLiteStatement insert;
            insert = DBManager._db.compileStatement("INSERT INTO " + table_name + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() + ")");

            int size = addrData.size();

            for (ProductAlias alias : aliases) {
                insert.bindString(index(prod_id), alias.getProd_id());
                insert.bindString(index(prod_alias), alias.getProd_alias());
                insert.execute();
                insert.clearBindings();
            }
            insert.close();
            DBManager._db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            DBManager._db.endTransaction();
        }

    }

    public void emptyTable() {
        DBManager._db.execSQL("DELETE FROM " + table_name);
    }
}
