package com.android.database;

import android.app.Activity;
import android.database.Cursor;

import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PayMethodsHandler {


    private final static String paymethod_id = "paymethod_id";
    private final String paymethod_name = "paymethod_name";
    private final String paymentmethod_type = "paymentmethod_type";
    private final String paymethod_update = "paymethod_update";
    private final String isactive = "isactive";
    private final String paymethod_showOnline = "paymethod_showOnline";
    private final String image_url = "image_url";
    private final String OriginalTransid = "OriginalTransid";

    private final List<String> attr = Arrays.asList(paymethod_id, paymethod_name, paymentmethod_type, paymethod_update,
            isactive, paymethod_showOnline, image_url, OriginalTransid);
    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;
    private List<String[]> addrData;
    private MyPreferences myPref;
    private List<HashMap<String, Integer>> dictionaryListMap;

    private static final String table_name = "PayMethods";

    public PayMethodsHandler(Activity activity) {
        attrHash = new HashMap<String, Integer>();
        addrData = new ArrayList<String[]>();
        sb1 = new StringBuilder();
        sb2 = new StringBuilder();
        myPref = new MyPreferences(activity);
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

        DBManager._db.beginTransaction();
        try {

            addrData = data;
            dictionaryListMap = dictionary;
            SQLiteStatement insert;
            insert = DBManager._db.compileStatement("INSERT INTO " + table_name + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() + ")");

            int size = addrData.size();

            for (int j = 0; j < size; j++) {
                insert.bindString(index(paymethod_id), getData(paymethod_id, j));
                insert.bindString(index(paymethod_name), getData(paymethod_name, j));
                insert.bindString(index(paymentmethod_type), getData(paymentmethod_type, j));
                insert.bindString(index(paymethod_update), getData(paymethod_update, j));
                insert.bindString(index(isactive), getData(isactive, j));
                insert.bindString(index(paymethod_showOnline), getData(paymethod_showOnline, j));
                insert.bindString(index(image_url), getData(image_url, j));
                insert.bindString(index(OriginalTransid), Boolean.parseBoolean(getData(OriginalTransid, j)) ? "1" : "0");

                insert.execute();
                insert.clearBindings();
            }
            insert.close();
            DBManager._db.setTransactionSuccessful();
        } catch (Exception ignored) {

        } finally {
            DBManager._db.endTransaction();
        }
    }


    public void emptyTable() {
        DBManager._db.execSQL("DELETE FROM " + table_name);
    }

    public List<String[]> getPayMethod() {
        //SQLiteDatabase db = dbManager.openReadableDB();

        List<String[]> list = new ArrayList<String[]>();

        String[] fields = new String[]{paymethod_id, paymethod_name, paymentmethod_type, image_url, OriginalTransid};

        Cursor cursor = DBManager._db.query(true, table_name, fields, "paymethod_id!=''", null, null, null, paymethod_name + " ASC", null);
        String[] data = new String[5];


        //--------------- add additional payment methods ----------------
        if (myPref.getPreferences(MyPreferences.pref_mw_with_genius)) {
            String[] extraMethods = new String[]{"Genius", "Genius", "Genius", "", "0"};
            list.add(extraMethods);
        }
        if (myPref.getPreferences(MyPreferences.pref_pay_with_tupyx)) {
            String[] extraMethods = new String[]{"Wallet", "Tupyx", "Wallet", "", "0"};
            list.add(extraMethods);
        }


        if (cursor.moveToFirst()) {
            do {

                data[0] = cursor.getString(cursor.getColumnIndex(paymethod_id));
                data[1] = cursor.getString(cursor.getColumnIndex(paymethod_name));
                data[2] = cursor.getString(cursor.getColumnIndex(paymentmethod_type));
                data[3] = cursor.getString(cursor.getColumnIndex(image_url));
                data[4] = cursor.getString(cursor.getColumnIndex(OriginalTransid));
                list.add(data);

                data = new String[5];

            } while (cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return list;
    }


    public List<String[]> getPayMethodsName() {
        //SQLiteDatabase db =dbManager.openReadableDB();

        List<String[]> list = new ArrayList<String[]>();

        String[] fields = new String[]{paymethod_id, paymethod_name};

        Cursor cursor = DBManager._db.query(true, table_name, fields, "paymethod_id!=''", null, null, null, paymethod_name + " ASC", null);
        //String[] data = new String[2];

        //--------------- add additional payment methods ----------------
        if (myPref.getPreferences(MyPreferences.pref_mw_with_genius)) {
            String[] extraMethods = new String[]{"Genius", "Genius", "Genius", "", "0"};
            list.add(extraMethods);
        }
        if (myPref.getPreferences(MyPreferences.pref_pay_with_tupyx)) {
            String[] extraMethods = new String[]{"Wallet", "Tupyx", "Wallet", "", "0"};
            list.add(extraMethods);
        }

        if (cursor.moveToFirst()) {
            String[] values = new String[2];
            int i_paymethod_id = cursor.getColumnIndex(paymethod_id);
            int i_paymethod_name = cursor.getColumnIndex(paymethod_name);
            do {

                values[0] = cursor.getString(i_paymethod_id);
                values[1] = cursor.getString(i_paymethod_name);
                list.add(values);
                values = new String[2];

            } while (cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return list;
    }


    public static String getPayMethodID(String methodType) {
        //SQLiteDatabase db = dbManager.openReadableDB();

        String[] fields = new String[]{paymethod_id};

        Cursor cursor = DBManager._db.query(true, table_name, fields, "paymentmethod_type= '" + methodType + "'", null, null, null, null, null);
        String data = "";
        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(cursor.getColumnIndex(paymethod_id));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return data;
    }


    public String getSpecificPayMethod(String methodID) {
        String[] fields = new String[]{paymethod_name};
        Cursor cursor = DBManager._db.query(true, table_name, fields, "paymethod_id = '" + methodID + "'", null, null, null, null, null);
        String data = "";
        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(cursor.getColumnIndex(paymethod_name));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return data;
    }

    public String getSpecificPayMethodId(String methodName) {
        String[] fields = new String[]{paymethod_id};
        Cursor cursor = DBManager._db.query(true, table_name, fields, "paymethod_name = '" + methodName + "'", null, null, null, null, null);
        String data = "";
        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(cursor.getColumnIndex(paymethod_id));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return data;
    }

}
