package com.android.database;

import android.database.Cursor;

import com.android.emobilepos.models.DataTaxes;

import net.sqlcipher.database.SQLiteStatement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OrderTaxes_DB {

    public final static String ord_tax_id = "ord_tax_id";
    public final static String ord_id = "ord_id";
    public final static String tax_name = "tax_name";
    public final static String tax_amount = "tax_amount";
    public final static String tax_rate = "tax_rate";
    private static final String TABLE_NAME = "OrderTaxes";
    private final List<String> attr = Arrays.asList(ord_tax_id, ord_id, tax_name, tax_amount, tax_rate);
    private HashMap<String, Integer> attrHash;
    private StringBuilder mainSB1, mainSB2;

    public OrderTaxes_DB() {
        attrHash = new HashMap<>();
        mainSB1 = new StringBuilder();
        mainSB2 = new StringBuilder();
        initDictionary();
    }

    private void initDictionary() {
        int size = attr.size();
        for (int i = 0; i < size; i++) {
            attrHash.put(attr.get(i), i + 1);
            if ((i + 1) < size) {
                mainSB1.append(attr.get(i)).append(",");
                mainSB2.append("?").append(",");
            } else {
                mainSB1.append(attr.get(i));
                mainSB2.append("?");
            }
        }
    }

    private int index(String tag) {
        return attrHash.get(tag);
    }

    public void insert(List<DataTaxes> dataTaxes, String _ord_id) {
        DBManager.getDatabase().beginTransaction();
        SQLiteStatement insert = null;
        try {
            delete(_ord_id);
            insert = DBManager.getDatabase().compileStatement("INSERT OR REPLACE INTO " + TABLE_NAME + " (" + mainSB1.toString() + ") " + "VALUES (" + mainSB2.toString() + ")");
            int size = dataTaxes.size();
            for (int j = 0; j < size; j++) {
                insert.bindString(index(ord_tax_id), dataTaxes.get(j).getOrd_tax_id());
                insert.bindString(index(ord_id), _ord_id);
                insert.bindString(index(tax_name), dataTaxes.get(j).getTax_name());
                insert.bindString(index(tax_amount), dataTaxes.get(j).getTax_amount());
                insert.bindString(index(tax_rate), dataTaxes.get(j).getTax_rate());
                insert.execute();
                insert.clearBindings();
            }
            insert.close();
            DBManager.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(Log.getStackTraceString(e), false).build());
        } finally {
            if (insert != null) {
                insert.close();
            }
            DBManager.getDatabase().endTransaction();
        }
    }

    public void emptyTable() {
        DBManager.getDatabase().execSQL("DELETE FROM " + TABLE_NAME);
    }

    public List<DataTaxes> getOrderTaxes(String _ord_id) {
        Cursor c = null;
        try {
            List<DataTaxes> list = new ArrayList<>();
            DataTaxes dataTaxes;

            c = DBManager.getDatabase().rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE ord_id = ?", new String[]{_ord_id});

            if (c.moveToFirst()) {
                int i_tax_name = c.getColumnIndex(tax_name);
                int i_tax_rate = c.getColumnIndex(tax_rate);
                int i_tax_amount = c.getColumnIndex(tax_amount);
                do {
                    dataTaxes = new DataTaxes();
                    dataTaxes.setTax_name(c.getString(i_tax_name));
                    dataTaxes.setTax_rate(c.getString(i_tax_rate));
                    dataTaxes.setTax_amount(new BigDecimal(c.getString(i_tax_amount)).setScale(2, RoundingMode.HALF_UP).toString());
                    list.add(dataTaxes);
                } while (c.moveToNext());
            }
            c.close();
            return list;
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
    }

    public void delete(String _ord_id) {
        DBManager.getDatabase().execSQL("DELETE FROM " + TABLE_NAME +
                " WHERE ord_id = ?", new String[]{_ord_id});
    }
}