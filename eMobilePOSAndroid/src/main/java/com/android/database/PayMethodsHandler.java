package com.android.database;

import android.app.Activity;
import android.database.Cursor;

import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.PaymentMethod;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.Sort;
import util.StringUtil;

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
    private MyPreferences myPref;

    private static final String table_name = "PayMethods";

    public PayMethodsHandler(Activity activity) {
        attrHash = new HashMap<>();
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


    private int index(String tag) {
        return attrHash.get(tag);
    }


    public void insert(List<PaymentMethod> paymentMethods) {

        DBManager._db.beginTransaction();
        try {
            SQLiteStatement insert;
            insert = DBManager._db.compileStatement("INSERT INTO " + table_name + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() + ")");

            for (PaymentMethod method : paymentMethods) {
                insert.bindString(index(paymethod_id), StringUtil.nullStringToEmpty(method.getPaymethod_id()));
                insert.bindString(index(paymethod_name), StringUtil.nullStringToEmpty(method.getPaymethod_name()));
                insert.bindString(index(paymentmethod_type), StringUtil.nullStringToEmpty(method.getPaymentmethod_type()));
                insert.bindString(index(paymethod_update), StringUtil.nullStringToEmpty(method.getPaymethod_update()));
                insert.bindString(index(isactive), StringUtil.nullStringToEmpty(method.getIsactive()));
                insert.bindString(index(paymethod_showOnline), StringUtil.nullStringToEmpty(method.getPaymethod_showOnline()));
                insert.bindString(index(image_url), StringUtil.nullStringToEmpty(method.getImage_url()));
                insert.bindString(index(OriginalTransid), Boolean.parseBoolean(method.getOriginalTransid()) ? "1" : "0");
                insert.execute();
                insert.clearBindings();
            }
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.insert(paymentMethods);
            realm.commitTransaction();
            insert.close();
            DBManager._db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager._db.endTransaction();
        }
    }


    public void emptyTable() {
        Realm realm = Realm.getDefaultInstance();
        try {
            DBManager._db.execSQL("DELETE FROM " + table_name);
            realm.beginTransaction();
            realm.where(Payment.class).findAll().deleteAllFromRealm();
//            realm.delete(PaymentMethod.class);
        } finally {
            realm.commitTransaction();
        }
    }

    public List<PaymentMethod> getPayMethod() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(PaymentMethod.class).findAll().sort("paymethod_name", Sort.ASCENDING);
    }


    public List<String[]> getPayMethodsName() {
        List<String[]> list = new ArrayList<>();

        String[] fields = new String[]{paymethod_id, paymethod_name};

        Cursor cursor = DBManager._db.query(true, table_name, fields, "paymethod_id!=''", null, null, null, paymethod_name + " ASC", null);

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
