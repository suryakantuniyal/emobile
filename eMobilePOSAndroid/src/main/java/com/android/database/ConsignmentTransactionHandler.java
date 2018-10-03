package com.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.dao.AssignEmployeeDAO;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.support.ConsignmentTransaction;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConsignmentTransactionHandler {
    private final String Cons_ID = "Cons_ID";
    private final String ConsTrans_ID = "ConsTrans_ID";
    private final String ConsEmp_ID = "ConsEmp_ID";
    private final String ConsCust_ID = "ConsCust_ID";
    private final String ConsInvoice_ID = "ConsInvoice_ID";
    private final String ConsReturn_ID = "ConsReturn_ID";
    private final String ConsPickup_ID = "ConsPickup_ID";
    private final String ConsDispatch_ID = "ConsDispatch_ID";
    private final String ConsProd_ID = "ConsProd_ID";
    private final String ConsOriginal_Qty = "ConsOriginal_Qty";
    private final String ConsStock_Qty = "ConsStock_Qty";
    private final String ConsInvoice_Qty = "ConsInvoice_Qty";
    private final String ConsReturn_Qty = "ConsReturn_Qty";
    private final String ConsDispatch_Qty = "ConsDispatch_Qty";
    private final String ConsPickup_Qty = "ConsPickup_Qty";
    private final String ConsNew_Qty = "ConsNew_Qty";
    private final String Cons_timecreated = "Cons_timecreated";
    private final String is_synched = "is_synched";

    private final List<String> attr = Arrays.asList(Cons_ID, ConsTrans_ID, ConsEmp_ID, ConsCust_ID,
            ConsInvoice_ID, ConsReturn_ID, ConsPickup_ID, ConsDispatch_ID, ConsProd_ID, ConsOriginal_Qty, ConsStock_Qty,
            ConsInvoice_Qty, ConsReturn_Qty, ConsDispatch_Qty, ConsPickup_Qty, ConsNew_Qty, Cons_timecreated);
    private final HashMap<String, Integer> attrHash;
    private final String TABLE_NAME = "ConsignmentTransaction";
    private StringBuilder sb1, sb2;
    private MyPreferences myPref;

    public ConsignmentTransactionHandler(Context activity) {
        attrHash = new HashMap<>();
        sb1 = new StringBuilder();
        sb2 = new StringBuilder();
        myPref = new MyPreferences(activity);
        new DBManager(activity);
        initDictionary();
    }

    public static ConsignmentTransactionHandler getInstance(Context activity) {
        return new ConsignmentTransactionHandler(activity);
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

    public void insert(List<ConsignmentTransaction> list) {
        ContentValues values = new ContentValues();
        int size = list.size();

        for (int i = 0; i < size; i++) {
            values.clear();

            ConsignmentTransaction trans = list.get(i);
            values.put(ConsTrans_ID, trans.ConsTrans_ID);
            values.put(ConsEmp_ID, trans.ConsEmp_ID);
            values.put(ConsCust_ID, trans.ConsCust_ID);
            values.put(ConsInvoice_ID, trans.ConsInvoice_ID);
            values.put(ConsReturn_ID, trans.ConsReturn_ID);
            values.put(ConsDispatch_ID, trans.ConsDispatch_ID);
            values.put(ConsPickup_ID, trans.ConsPickup_ID);
            values.put(ConsProd_ID, trans.ConsProd_ID);
            values.put(ConsOriginal_Qty, trans.ConsOriginal_Qty);
            values.put(ConsStock_Qty, trans.ConsStock_Qty);
            values.put(ConsInvoice_Qty, trans.ConsInvoice_Qty);
            values.put(ConsReturn_Qty, trans.ConsReturn_Qty);
            values.put(ConsDispatch_Qty, trans.ConsDispatch_Qty);
            values.put(ConsPickup_Qty, trans.ConsPickup_Qty);
            values.put(ConsNew_Qty, trans.ConsNew_Qty);
            values.put(Cons_timecreated, trans.Cons_timecreated);

            DBManager.getDatabase().insert(TABLE_NAME, null, values);

        }
        if (!list.isEmpty()) {
            myPref.setLastConsTransID(list.get(0).ConsTrans_ID);
        }
    }

    public void emptyTable() {
        DBManager.getDatabase().execSQL("DELETE FROM " + TABLE_NAME);
    }

    public long getDBSize() {
        SQLiteStatement stmt = DBManager.getDatabase().compileStatement("SELECT Count(*) FROM " + TABLE_NAME);
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public Cursor getUnsychedItems() {
        String sb = "SELECT " + sb1.toString() + " FROM " + TABLE_NAME +
                " WHERE is_synched = '1'";

        return DBManager.getDatabase().rawQuery(sb, null);
    }

    public long getNumUnsyncItems() {
        SQLiteStatement stmt = null;
        try {
            stmt = DBManager.getDatabase().compileStatement("SELECT Count(DISTINCT ConsTrans_ID) FROM " + TABLE_NAME + " WHERE is_synched = '1'");
            long count = stmt.simpleQueryForLong();
            stmt.close();
            return count;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public void updateIsSync(List<String[]> list) {
        StringBuilder sb = new StringBuilder();
        sb.append(ConsTrans_ID).append(" = ?");

        ContentValues args = new ContentValues();

        int size = list.size();
        for (int i = 0; i < size; i++) {
            args.put(is_synched, list.get(i)[0]);
            DBManager.getDatabase().update(TABLE_NAME, args, sb.toString(), new String[]{list.get(i)[1]});
        }
    }

    public boolean unsyncConsignmentsLeft() {
        SQLiteStatement stmt = null;
        try {
            stmt = DBManager.getDatabase().compileStatement("SELECT Count(DISTINCT ConsTrans_ID) FROM " + TABLE_NAME + " WHERE is_synched = '1'");
            long count = stmt.simpleQueryForLong();
            stmt.close();
            return count != 0;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public Cursor getConsignmentCursor(boolean typePickup) {

        StringBuilder sb = new StringBuilder();

        sb.append(
                "SELECT ConsTrans_ID as '_id', c.cust_name,ct.is_synched,Cons_timecreated FROM ConsignmentTransaction ct ");
        sb.append("LEFT OUTER JOIN Customers c ON ct.ConsCust_ID = c.cust_id ");

        if (typePickup) {
            sb.append("WHERE ConsPickup_ID != '' ");
        } else {
            sb.append(
                    "WHERE (ConsInvoice_ID != '' OR ConsReturn_ID != '' OR ConsDispatch_ID != '' OR (ConsInvoice_ID = '' AND ConsReturn_ID = '' AND ConsDispatch_ID = '' AND ConsPickup_ID = '')) ");
        }
        sb.append("GROUP BY ConsTrans_ID ORDER BY Cons_timecreated DESC");

        Cursor c = DBManager.getDatabase().rawQuery(sb.toString(), null);
        c.moveToFirst();
        return c;
    }

    public HashMap<String, String> getConsignmentSummaryDetails(String _ConsTrans_ID, boolean isPickup) {
        Cursor c = null;
        try {
            String sb = "SELECT sum(ConsInvoice_Qty) as 'total_items_sold',sum(ConsReturn_Qty) as 'total_items_returned', " +
                    "sum(ConsDispatch_Qty) as 'total_items_dispatched',count(*) as 'total_line_items'," +
                    "sum((((ConsOriginal_Qty-ConsStock_Qty)*p.prod_price)-(ConsReturn_Qty*p.prod_price))) as 'total_grand_total', " +
                    "cs.encoded_signature FROM ConsignmentTransaction ct LEFT OUTER JOIN Products p ON ct.ConsProd_ID = p.prod_id " +
                    "LEFT OUTER JOIN ConsignmentSignatures cs ON cs.ConsTrans_ID = ct.ConsTrans_ID WHERE ct.ConsTrans_ID = ?";
            HashMap<String, String> map = new HashMap<>();

            c = DBManager.getDatabase().rawQuery(sb, new String[]{_ConsTrans_ID});

            if (c.moveToFirst()) {
                map.put("ConsTrans_ID", _ConsTrans_ID);
                map.put("total_items_sold", c.getString(c.getColumnIndex("total_items_sold")));
                map.put("total_items_returned", c.getString(c.getColumnIndex("total_items_returned")));
                map.put("total_items_dispatched", c.getString(c.getColumnIndex("total_items_dispatched")));
                map.put("total_line_items", c.getString(c.getColumnIndex("total_line_items")));
                if (!isPickup)
                    map.put("total_grand_total", c.getString(c.getColumnIndex("total_grand_total")));
                else
                    map.put("total_grand_total", "0");

                map.put("encoded_signature", c.getString(c.getColumnIndex("encoded_signature")));
            }
            c.close();
            return map;
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
    }

    public Cursor getConsignmentItemDetails(String _ConsTrans_ID) {
        // SQLiteDatabase db = dbManager.openReadableDB();

        String sb = "SELECT ConsOriginal_Qty,ConsStock_Qty,ConsReturn_Qty,ConsInvoice_Qty,ConsDispatch_Qty,ConsNew_Qty,ConsPickup_Qty,ci.price as 'price', " +
                "((ConsOriginal_Qty-ConsStock_Qty)*ci.price) as 'item_subtotal', (ConsReturn_Qty*ci.price) as 'credit_memo', " +
                "(((ConsOriginal_Qty-ConsStock_Qty)*ci.price)-(ConsReturn_Qty*ci.price)) as 'item_total',pi.prod_img_name,p.prod_name,p.prod_desc " +
                "FROM ConsignmentTransaction ct  LEFT OUTER JOIN CustomerInventory ci ON ct.ConsProd_ID = ci.prod_id AND ct.ConsCust_ID = " +
                "ci.cust_id LEFT OUTER JOIN Products_Images pi ON ct.ConsProd_ID = pi.prod_id AND pi.type = 'I' LEFT OUTER JOIN Products p ON " +
                "ct.ConsProd_ID = p.prod_id WHERE ConsTrans_ID = ? ORDER BY p.prod_name ASC";

        Cursor c = DBManager.getDatabase().rawQuery(sb, new String[]{_ConsTrans_ID});
        c.moveToFirst();
        return c;
    }

    public String getLastConsignmentId(int deviceId, int year) {
        String lastID = myPref.getLastConsTransID();
        boolean getIdFromDB = false;
        StringBuilder sb = new StringBuilder();
        if (TextUtils.isEmpty(lastID) || lastID.length() <= 4) {
            getIdFromDB = true;
        } else {
            String[] tokens = myPref.getLastConsTransID().split("-");
            if (!tokens[2].equalsIgnoreCase(String.valueOf(year))) {
                getIdFromDB = true;
            }
        }

        if (getIdFromDB) {
            sb.append("select max(ConsTrans_ID) from ").append(TABLE_NAME).append(" WHERE ConsTrans_ID like '").append(deviceId)
                    .append("-%-").append(year).append("'");

            SQLiteStatement stmt = DBManager.getDatabase().compileStatement(sb.toString());
            Cursor cursor = null;
            try {
                cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);
                cursor.moveToFirst();
                lastID = cursor.getString(0);
                cursor.close();
                stmt.close();
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            }
            if (TextUtils.isEmpty(lastID)) {
                AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
                lastID = assignEmployee.getEmpId() + "-" + "00001" + "-" + year;
            }
            myPref.setLastConsTransID(lastID);
        }
        return lastID;
    }
}
