package com.android.database;

import android.app.Activity;
import android.content.Context;

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

    public ProductAliases_DB(Context activity) {
        attrHash = new HashMap<>();
        addrData = new ArrayList<>();
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


    public void insert(List<ProductAlias> aliases) {
        SQLiteStatement insert=null;
        DBManager.getDatabase().beginTransaction();
        try {
            insert = DBManager.getDatabase().compileStatement("INSERT INTO " + table_name + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() + ")");

            int size = addrData.size();

            for (ProductAlias alias : aliases) {
                insert.bindString(index(prod_id), alias.getProd_id());
                insert.bindString(index(prod_alias), alias.getProd_alias());
                insert.execute();
                insert.clearBindings();
            }

            DBManager.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            if(insert!=null) {
                insert.close();
            }
            DBManager.getDatabase().endTransaction();
        }

    }

    public void emptyTable() {
        DBManager.getDatabase().execSQL("DELETE FROM " + table_name);
    }
}
