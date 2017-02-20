
package com.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.android.support.Global;

import net.sqlcipher.database.SQLiteStatement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tirizar on 1/5/2016.
 */


public class ShiftExpensesDBHandler {
    private final String expenseID = "expenseID";
    private final String shiftPeriodID = "shiftPeriodID";
    private final String cashAmount = "cashAmount";
    private final String productID = "productID";
    private final String productName = "productName";

    public final List<String> attr = Arrays.asList(new String[]{expenseID,shiftPeriodID,cashAmount,productID,productName});

    public StringBuilder sb1, sb2;
    public final String empStr = "";
    public HashMap<String, Integer> attrHash;

    private Context activity;

    public static final String table_name = "Expenses";

    public ShiftExpensesDBHandler(Context activity) {
//        global = (Global) activity.getApplication();
        this.activity = activity;
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
                sb2.append("?");
            }
        }
    }

    public int index(String tag) {
        return attrHash.get(tag);
    }

    public void insert(String pID, String pName, double cAmount, String spID) {
        //pID the product id, this a product of type expense
        //pName the product name
        //cAmount the cash amount of the expense
        //spID the shift period ID

//insert the expense section
        String expID; //need to create a new expID
        Long milliseconds = (Long) System.currentTimeMillis();
        expID = milliseconds.toString(); //use time stamp as expenseID

        DBManager.getDatabase().beginTransaction();
        try {
            SQLiteStatement insert;
            String sb = "INSERT INTO " + table_name + " (" + sb1.toString() + ") " +
                    "VALUES (" + sb2.toString() + ")";
            insert = DBManager.getDatabase().compileStatement(sb);
            insert.bindString(index(expenseID), expID == null ? "" : expID);
            insert.bindString(index(shiftPeriodID), spID == null ? "" : spID);
            insert.bindDouble(index(cashAmount), cAmount);
            insert.bindString(index(productID), pID == null ? "" : pID);
            insert.bindString(index(productName), pName == null ? "" : pName);
            insert.execute();
            insert.clearBindings();
            insert.close();
            DBManager.getDatabase().setTransactionSuccessful();

        } catch (Exception e) {
        } finally {
            DBManager.getDatabase().endTransaction();
        }

        //update the ending petty cash due to the expense section
        ShiftPeriodsDBHandler shiftPeriodsDBHandler = new ShiftPeriodsDBHandler(activity);
        shiftPeriodsDBHandler.decreaseEndingPettyCash(spID,cAmount);

    }

    public void emptyTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(table_name);
        DBManager.getDatabase().execSQL(sb.toString());
    }

    // public void emptyTable() {
    // StringBuilder sb = new StringBuilder();
    // SQLiteDatabase db = dbManager.openWritableDB();
    // sb.append("DELETE FROM ").append(table_name);
    // db.execSQL(sb.toString());
    // db.close();
    // }


    public void updateShiftExpense(String expenseID, String attr, String val) {
        // SQLiteDatabase db = dbManager.openWritableDB();
        StringBuilder sb = new StringBuilder();

        sb.append(expenseID).append(" = ?");

        ContentValues args = new ContentValues();

        args.put(attr, val);
        DBManager.getDatabase().update(table_name, args, sb.toString(), new String[]{expenseID});

        // db.close();
    }

    public SparseArray<String> getShiftExpenseDetails(String expenseID) {
        // SQLiteDatabase db = dbManager.openReadableDB();
        SparseArray<String> map = new SparseArray<String>();
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM ").append(table_name).append(" WHERE expenseID = ?");

        Cursor c = DBManager.getDatabase().rawQuery(sb.toString(), new String[] { expenseID });

        if (c.moveToFirst()) {

            map.put(0, c.getString(c.getColumnIndex(expenseID)));
            map.put(1, c.getString(c.getColumnIndex(shiftPeriodID)));
            map.put(2, Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex(cashAmount))));
            map.put(3, c.getString(c.getColumnIndex(productID)));
            map.put(4, c.getString(c.getColumnIndex(productName)));
        }
        c.close();
        // db.close();
        return map;
    }

    //get list of expenses for a shift
    public Cursor getShiftExpenses(String spID) {

        String[] parameters = null;
        String query = empStr;
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT expenseID as _id, shiftPeriodID, cashAmount, productID, productName FROM ");
        sb.append(table_name);
        sb.append(" WHERE shiftPeriodID = '");
        sb.append(spID).append("'");

        query = sb.toString();

        Cursor cursor = DBManager.getDatabase().rawQuery(query, parameters);
        cursor.moveToFirst();
        // db.close();

        return cursor;

    }

    //get total expenses for a shift
    public String getShiftTotalExpenses(String spID) {

        String[] parameters = null;
        String query = empStr;
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT total(cashAmount) as total_expenses FROM ");
        sb.append(table_name);
        sb.append(" WHERE shiftPeriodID = '");
        sb.append(spID).append("'");

        query = sb.toString();

        Cursor cursor = DBManager.getDatabase().rawQuery(query, parameters);
        cursor.moveToFirst();

        String theTotalExpenses = cursor.getString(0); //get the computed total
        // db.close();

        return theTotalExpenses;

    }



}
