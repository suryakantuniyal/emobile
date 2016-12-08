package com.android.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;

import com.android.dao.AssignEmployeeDAO;
import com.android.emobilepos.holders.TransferLocations_Holder;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TransferLocations_DB {
    public static final String trans_id = "trans_id";
    public static final String loc_key_from = "loc_key_from";
    public static final String loc_key_to = "loc_key_to";
    public static final String emp_id = "emp_id";
    public static final String trans_timecreated = "trans_timecreated";
    public static final String issync = "issync";

    private static final List<String> attr = Arrays
            .asList(trans_id, loc_key_from, loc_key_to, emp_id, trans_timecreated);

    private static final String TABLE_NAME = "TransferLocations";
    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;
    private MyPreferences myPref;

    public TransferLocations_DB(Activity activity) {
        attrHash = new HashMap<>();
        sb1 = new StringBuilder();
        sb2 = new StringBuilder();
        myPref = new MyPreferences(activity);
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

    private int index(String tag) {
        return attrHash.get(tag);
    }

    public void insert(TransferLocations_Holder location) {
        DBManager.getDatabase().beginTransaction();
        try {
            SQLiteStatement insert;
            String sb = "INSERT INTO " + TABLE_NAME + " (" + sb1.toString() + ") " +
                    "VALUES (" + sb2.toString() + ")";
            insert = DBManager.getDatabase().compileStatement(sb);
            insert.bindString(index(trans_id), location.getTrans_id());
            insert.bindString(index(loc_key_from), location.getLoc_key_from());
            insert.bindString(index(loc_key_to), location.getLoc_key_to());
            insert.bindString(index(emp_id), location.getEmp_id());
            insert.bindString(index(trans_timecreated), location.getTrans_timecreated());
            insert.execute();
            insert.clearBindings();
            insert.close();
            DBManager.getDatabase().setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            AssignEmployeeDAO.updateLastTransferId(location.getTrans_id());
//            myPref.setLastTransferID(location.getTrans_id());
            DBManager.getDatabase().endTransaction();
        }
    }

    public void emptyTable() {
        DBManager.getDatabase().execSQL("DELETE FROM " + TABLE_NAME);
    }

    public static String getLastTransferID(int empId, int year) {
        String sb = "SELECT max(trans_id) FROM TransferLocations WHERE trans_id LIKE \"" + empId +
                "-%-" + year + "\"";

        SQLiteStatement stmt = DBManager.getDatabase().compileStatement(sb);
        String val = stmt.simpleQueryForString();
        stmt.close();
        return val;
    }

    public Cursor getUnsyncTransfers() {
        return DBManager.getDatabase().rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE issync = '0'", null);
    }

    public long getNumUnsyncTransfers() {
        SQLiteStatement stmt = DBManager.getDatabase().compileStatement("SELECT Count(*) FROM " + TABLE_NAME + " WHERE issync = '0'");
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public void updateIsSync(List<String[]> list) {
        StringBuilder sb = new StringBuilder();
        sb.append(trans_id).append(" = ?");
        ContentValues args = new ContentValues();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i)[1].equals("0"))
                args.put(issync, "1");
            else
                args.put(issync, "0");
            DBManager.getDatabase().update(TABLE_NAME, args, sb.toString(), new String[]{list.get(i)[0]});
        }
    }

    public Cursor getAllTransactions() {
        Cursor c = DBManager.getDatabase().rawQuery("SELECT trans_id as '_id', * FROM " + TABLE_NAME + " ORDER BY trans_id DESC", null);
        c.moveToFirst();
        return c;
    }

}
