package com.android.database;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InvoicePaymentsHandler {

    private static final String table_name = "InvoicePayments";
    private final String pay_id = "pay_id";
    private final String inv_id = "inv_id";
    private final String applied_amount = "applied_amount";
    private final String txnID = "txnID";
    public final List<String> attr = Arrays.asList(pay_id, inv_id, txnID, applied_amount);
    SQLiteStatement insert = null;
    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;

    public InvoicePaymentsHandler(Context activity) {
        attrHash = new HashMap<>();
        sb1 = new StringBuilder();
        sb2 = new StringBuilder();
        new DBManager(activity);
        initDictionary();
    }

    public void initDictionary() {
        int size = attr.size();
        for (int i = 0; i < size; i++) {
            attrHash.put(attr.get(i), i + 1);
            if ((i + 1) < size) {
                sb1.append(attr.get(i)).append(",");
                sb2.append("?").append(",");
            } else {
                sb1.append(attr.get(i));
                sb2.append("ROUND(?,2)");
            }
        }
    }

    public int index(String tag) {
        return attrHash.get(tag);
    }

    public void insert(List<String[]> payment) {
        // SQLiteDatabase db = dbManager.openWritableDB();
        DBManager.getDatabase().beginTransaction();
        try {


            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ")
                    .append("VALUES (").append(sb2.toString()).append(")");
            insert = DBManager.getDatabase().compileStatement(sb.toString());

            int size = payment.size();
            for (int i = 0; i < size; i++) {
                insert.bindString(index(pay_id), payment.get(i)[0]); // pay_id
                insert.bindString(index(inv_id), payment.get(i)[1]); // inv_id
                insert.bindString(index(applied_amount), payment.get(i)[2]); // applied_amount
                insert.bindString(index(txnID), payment.get(i)[3]); // txnID

                insert.execute();
                insert.clearBindings();
            }
            //	insert.close();
            DBManager.getDatabase().setTransactionSuccessful();

        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.getMessage()).append(" [com.android.emobilepos.PaymentsHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
        } finally {
            if (insert != null) {
                insert.close();
            }
            DBManager.getDatabase().endTransaction();

        }
        // db.close();
    }

    public void emptyTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(table_name);
        DBManager.getDatabase().execSQL(sb.toString());
    }

    public long getDBSize() {
        // SQLiteDatabase db = dbManager.openReadableDB();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT Count(*) FROM ").append(table_name);
        SQLiteStatement stmt = null;
        try {
            stmt = DBManager.getDatabase().compileStatement(sb.toString());
            long count = stmt.simpleQueryForLong();
            stmt.close();
            // db.close();
            return count;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public double getTotalPaidAmount(String invID) {
        // SQLiteDatabase db = dbManager.openReadableDB();
        SQLiteStatement stmt = null;
        try {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT  ifnull(ROUND(sum(applied_amount),2),-1) as 'total' FROM InvoicePayments WHERE inv_id = '")
                .append(invID).append("'");


            stmt = DBManager.getDatabase().compileStatement(sb.toString());
            String count = stmt.simpleQueryForString();
            stmt.close();
            // db.close();
            return Double.parseDouble(count);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public String getInvoicePaymentsID(String payID) {
        // SQLiteDatabase db = dbManager.openReadableDB();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT txnID FROM InvoicePayments WHERE pay_id = '").append(payID).append("' GROUP BY txnID");
        net.sqlcipher.Cursor cursor = null;
        try {

            cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);
            sb.setLength(0);
            if (cursor.moveToFirst()) {
                do {
                    sb.append(cursor.getString(cursor.getColumnIndex(txnID))).append("\n");
                } while (cursor.moveToNext());
            }

            cursor.close();
            // db.close();
            return sb.toString();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public List<String[]> getInvoicesPaymentsList(String payID) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT inv_id,applied_amount FROM InvoicePayments WHERE pay_id = '").append(payID).append("'");
        net.sqlcipher.Cursor cursor = null;
        try {
            List<String[]> list = new ArrayList<String[]>();
            String[] content = new String[2];
            cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);
            sb.setLength(0);
            if (cursor.moveToFirst()) {
                int i_inv_id = cursor.getColumnIndex(inv_id);
                int i_amount = cursor.getColumnIndex(applied_amount);
                do {
                    content[0] = cursor.getString(i_inv_id);
                    content[1] = cursor.getString(i_amount);

                    list.add(content);
                    content = new String[2];
                } while (cursor.moveToNext());
            }

            cursor.close();
            return list;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }
}
