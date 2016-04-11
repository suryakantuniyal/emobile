package com.android.database;

import android.app.Activity;
import android.database.Cursor;

import com.android.emobilepos.models.SalesAssociate;
import com.android.support.DateUtils;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SalesAssociateHandler {

    public static String emp_id = "emp_id";
    public static String zone_id = "zone_id";
    public static String emp_name = "emp_name";
    public static String emp_init = "emp_init";
    public static String emp_pcs = "emp_pcs";
    public static String emp_lastlogin = "emp_lastlogin";
    public static String emp_pos = "emp_pos";
    public static String qb_emp_id = "qb_emp_id";
    public static String qb_salesrep_id = "qb_salesrep_id";
    public static String isactive = "isactive";
    public static String tax_default = "tax_default";
    public static String loc_items = "loc_items";
    public static String _rowversion = "_rowversion";
    public static String lastSync = "lastSync";
    public static String TupyWalletDevice = "TupyWalletDevice";
    public static String VAT = "VAT";
    public static String[] columns = {emp_id, zone_id, emp_name, emp_init, emp_pcs, emp_lastlogin, emp_pos, qb_emp_id, qb_salesrep_id,
            isactive, tax_default, loc_items, _rowversion, lastSync, TupyWalletDevice, VAT};
    private static final String TABLE_NAME = "salesassociate";


    public static void insert(List<SalesAssociate> associates) {
        DBManager._db.beginTransaction();
        try {
            SQLiteStatement insert;
            insert = DBManager._db.compileStatement("INSERT OR REPLACE INTO " + TABLE_NAME + " ( " +
                    emp_id + "," + zone_id + "," + emp_name + "," + emp_init + "," + emp_pcs + "," +
                    emp_lastlogin + "," + emp_pos + "," + qb_emp_id + "," + qb_salesrep_id + "," +
                    isactive + "," + tax_default + "," + loc_items + "," + _rowversion + "," + lastSync + "," +
                    TupyWalletDevice + "," + VAT + ") " +
                    "VALUES ( " +
                    "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            for (SalesAssociate associate : associates) {
                insert.bindLong(1, associate.getEmp_id());
                insert.bindString(2, associate.getZone_id() == null ? "" : associate.getZone_id());
                insert.bindString(3, associate.getEmp_name() == null ? "" : associate.getEmp_name());
                insert.bindString(4, associate.getEmp_init() == null ? "" : associate.getEmp_init());
                insert.bindString(5, associate.getEmp_pcs() == null ? "" : associate.getEmp_pcs());
                insert.bindString(6, associate.getEmp_lastlogin() == null ? "" : associate.getEmp_lastlogin());
                insert.bindLong(7, associate.getEmp_pos());
                insert.bindString(8, associate.getQb_emp_id() == null ? "" : associate.getQb_emp_id());
                insert.bindString(9, associate.getQb_salesrep_id() == null ? "" : associate.getQb_salesrep_id());
                insert.bindString(10, String.valueOf(associate.isactive()));
                insert.bindString(11, associate.getTax_default() == null ? "" : associate.getTax_default());
                insert.bindString(12, String.valueOf(associate.isLoc_items()));
                insert.bindString(13, associate.get_rowversion() == null ? "" : associate.get_rowversion());
                insert.bindString(14, associate.getLastSync() == null ? "" : associate.getLastSync());
                insert.bindString(15, String.valueOf(associate.isTupyWalletDevice()));
                insert.bindString(16, String.valueOf(associate.isVAT()));
                insert.execute();
                insert.clearBindings();
            }
            insert.close();
            DBManager._db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager._db.endTransaction();
        }
    }

    public static void emptyTable() {
        DBManager._db.execSQL("DELETE FROM " + TABLE_NAME);
    }


    public static List<SalesAssociate> getAllSalesAssociates() {
//        String query = "SELECT * FROM " + TABLE_NAME;
//        Cursor cursor = DBManager._db.rawQuery(query, null);
        Cursor cursor = DBManager._db.query(TABLE_NAME, columns, null, null, null, null, null);
        List<SalesAssociate> associates = new ArrayList<SalesAssociate>();
        while (cursor.moveToNext()) {
            SalesAssociate associate = new SalesAssociate();
            associate.set_rowversion(cursor.getString(cursor.getColumnIndex("_rowversion")));
            associate.setEmp_id(Integer.parseInt(cursor.getString(cursor.getColumnIndex("emp_id"))));
            associate.setZone_id(cursor.getString(cursor.getColumnIndex("zone_id")));
            associate.setEmp_name(cursor.getString(cursor.getColumnIndex("emp_name")));
            associate.setEmp_init(cursor.getString(cursor.getColumnIndex("emp_init")));
            associate.setEmp_pcs(cursor.getString(cursor.getColumnIndex("emp_pcs")));
            associate.setEmp_lastlogin(cursor.getString(cursor.getColumnIndex("emp_lastlogin")));
            associate.setEmp_pos(Integer.parseInt(cursor.getString(cursor.getColumnIndex("emp_pos"))));
            associate.setQb_emp_id(cursor.getString(cursor.getColumnIndex("qb_emp_id")));
            associate.setQb_salesrep_id(cursor.getString(cursor.getColumnIndex("qb_salesrep_id")));
            associate.setTax_default(cursor.getString(cursor.getColumnIndex("tax_default")));
            associate.setLoc_items(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("loc_items"))));
            associate.setLastSync(cursor.getString(cursor.getColumnIndex("lastSync")));
            associate.setTupyWalletDevice(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("TupyWalletDevice"))));
            associate.setVAT(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("VAT"))));
            associates.add(associate);
        }
        cursor.close();
        return associates;
    }

    public static SalesAssociate getSalesAssociate(String empId) {
        Cursor cursor = DBManager._db.query(TABLE_NAME, columns, emp_id + " = ?", new String[]{empId},
                null, null, null);
        SalesAssociate associate = null;
        if (cursor.moveToFirst()) {
            associate = new SalesAssociate();
            associate.set_rowversion(cursor.getString(cursor.getColumnIndex("_rowversion")));
            associate.setEmp_id(Integer.parseInt(cursor.getString(cursor.getColumnIndex("emp_id"))));
            associate.setZone_id(cursor.getString(cursor.getColumnIndex("zone_id")));
            associate.setEmp_name(cursor.getString(cursor.getColumnIndex("emp_name")));
            associate.setEmp_init(cursor.getString(cursor.getColumnIndex("emp_init")));
            associate.setEmp_pcs(cursor.getString(cursor.getColumnIndex("emp_pcs")));
            associate.setEmp_lastlogin(cursor.getString(cursor.getColumnIndex("emp_lastlogin")));
            associate.setEmp_pos(Integer.parseInt(cursor.getString(cursor.getColumnIndex("emp_pos"))));
            associate.setQb_emp_id(cursor.getString(cursor.getColumnIndex("qb_emp_id")));
            associate.setQb_salesrep_id(cursor.getString(cursor.getColumnIndex("qb_salesrep_id")));
            associate.setTax_default(cursor.getString(cursor.getColumnIndex("tax_default")));
            associate.setLoc_items(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("loc_items"))));
            associate.setLastSync(cursor.getString(cursor.getColumnIndex("lastSync")));
            associate.setTupyWalletDevice(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("TupyWalletDevice"))));
            associate.setVAT(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("VAT"))));
        }
        cursor.close();
        return associate;
    }
}
