package com.android.database;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class SalesTaxCodesHandler {

    private final String taxcode_id = "taxcode_id";
    private final String taxcode_name = "taxcode_name";
    private final String taxcode_desc = "taxcode_desc";
    private final String taxcode_istaxable = "taxcode_istaxable";
    private final String isactive = "isactive";
    private final String taxcode_update = "taxcode_update";

    private final List<String> attr = Arrays
            .asList(taxcode_id, taxcode_name, taxcode_desc, taxcode_istaxable, isactive, taxcode_update);

    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;

    private List<String[]> addrData;
    private List<HashMap<String, Integer>> dictionaryListMap;

    public enum TaxableCode {TAXABLE, NON_TAXABLE, NONE}

    private static final String table_name = "SalesTaxCodes";

    public SalesTaxCodesHandler(Context activity) {
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

    public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
        DBManager.getDatabase().beginTransaction();
        try {

            addrData = data;
            dictionaryListMap = dictionary;
            SQLiteStatement insert;
            insert = DBManager.getDatabase().compileStatement("INSERT INTO " + table_name + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() + ")");

            int size = addrData.size();

            for (int j = 0; j < size; j++) {
                insert.bindString(index(taxcode_id), getData(taxcode_id, j)); // taxcode_id
                insert.bindString(index(taxcode_name), getData(taxcode_name, j)); // taxcode_name
                insert.bindString(index(taxcode_desc), getData(taxcode_desc, j)); // taxcode_desc
                insert.bindString(index(taxcode_istaxable), getData(taxcode_istaxable, j)); // taxcode_istaxable
                insert.bindString(index(isactive), getData(isactive, j)); // isactive
                insert.bindString(index(taxcode_update), getData(taxcode_update, j)); // taxcode_update

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
        DBManager.getDatabase().execSQL("DELETE FROM " + table_name);
    }


    public String getTaxableTaxCode() {
        Cursor c = DBManager.getDatabase().rawQuery("SELECT * FROM SalesTaxCodes WHERE taxcode_istaxable = '1' LIMIT 1", null);
        String taxcode_id = "";

        if (c.moveToFirst()) {
            taxcode_id = c.getString(c.getColumnIndex("taxcode_id"));
        }

        return taxcode_id;
    }


    public TaxableCode checkIfCustTaxable(String cust_taxable) {
        TaxableCode taxableCode;
        String subquery1 = "SELECT taxcode_istaxable FROM ";
        String subquery2 = " WHERE taxcode_id = '";

        Cursor cursor = DBManager.getDatabase().rawQuery(subquery1 + table_name + subquery2 + cust_taxable + "'", null);

        if (cursor.moveToFirst()) {
            if (cursor.getString(cursor.getColumnIndex(taxcode_istaxable)).equals("1")) {
                taxableCode = TaxableCode.TAXABLE;
            } else {
                taxableCode = TaxableCode.NON_TAXABLE;
            }
        } else {
            taxableCode = TaxableCode.NONE;
        }
        cursor.close();

        return taxableCode;

    }
}
