package com.android.database;

import android.content.Context;
import android.database.Cursor;

import com.android.emobilepos.models.GroupTax;
import com.android.emobilepos.models.Tax;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import util.StringUtil;

public class TaxesHandler {

    private static final String tax_id_key = "tax_id_key";
    private static final String tax_id = "tax_id";
    private static final String tax_name = "tax_name";
    private static final String tax_code_id = "tax_code_id";
    private static final String tax_code_name = "tax_code_name";
    private static final String tax_rate = "tax_rate";
    private static final String tax_type = "tax_type";
    private static final String isactive = "isactive";
    private static final String tax_update = "tax_update";
    private static final String prTax = "prTax";
    private static final String tax_default = "tax_default";
    private static final String tax_account = "tax_account";

    private final List<String> attr = Arrays.asList(tax_id_key, tax_id, tax_name, tax_code_id, tax_code_name, tax_rate, tax_type,
            isactive, tax_update, prTax, tax_default, tax_account);

    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;
    private List<String[]> addrData;
    private MyPreferences myPref;
    private List<HashMap<String, Integer>> dictionaryListMap;

    private static final String table_name = "Taxes";

    public TaxesHandler(Context activity) {
        attrHash = new HashMap<>();
        addrData = new ArrayList<>();
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
                insert.bindString(index(tax_id_key), getData(tax_id_key, j)); // tax_id_key
                insert.bindString(index(tax_id), getData(tax_id, j)); // tax_id
                insert.bindString(index(tax_name), getData(tax_name, j)); // tax_name
                insert.bindString(index(tax_code_id), getData(tax_code_id, j)); // tax_code_id
                insert.bindString(index(tax_code_name), getData(tax_code_name, j)); // tax_code_name
                insert.bindString(index(tax_rate), getData(tax_rate, j)); // tax_rate
                insert.bindString(index(tax_type), getData(tax_type, j)); // tax_type
                insert.bindString(index(isactive), getData(isactive, j)); // isactive
                insert.bindString(index(tax_update), getData(tax_update, j)); // tax_update
                insert.bindString(index(prTax), getData(prTax, j)); // prTax
                insert.bindString(index(tax_default), getData(tax_default, j)); // tax_default
                insert.bindString(index(tax_account), getData(tax_account, j)); // tax_account

                insert.execute();
                insert.clearBindings();

            }
            insert.close();
            DBManager.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.getMessage()).append(" [com.android.emobilepos.TaxesHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
        } finally {
            DBManager.getDatabase().endTransaction();
        }
    }


    public void emptyTable() {
        DBManager.getDatabase().execSQL("DELETE FROM " + table_name);
    }


    public List<Tax> getTaxes(boolean onlyGroupTaxes) {

        List<Tax> list = new ArrayList<>();
        Tax data = new Tax();
        String[] fields = new String[]{tax_name, tax_id, tax_code_id, tax_rate, tax_type};

        Cursor cursor;

        if (onlyGroupTaxes)
            cursor = DBManager.getDatabase().query(false, table_name, fields, "tax_type = ?", new String[]{"G"}, null, null, tax_name, null);
        else
            cursor = DBManager.getDatabase().query(false, table_name, fields, null, null, null, null, tax_name, null);

        if (cursor.moveToFirst()) {
            do {

                data.setTaxName(cursor.getString(cursor.getColumnIndex(tax_name)));
                data.setTaxId(cursor.getString(cursor.getColumnIndex(tax_id)));
                data.setTaxRate(cursor.getString(cursor.getColumnIndex(tax_rate)));
                data.setTaxType(cursor.getString(cursor.getColumnIndex(tax_type)));
                data.setTaxCodeId(cursor.getString(cursor.getColumnIndex(tax_code_id)));
                list.add(data);
                data = new Tax();
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public List<String[]> getTaxes(String taxId) {
        //SQLiteDatabase db = dbManager.openReadableDB();

        List<String[]> list = new ArrayList<String[]>();
        String[] data = new String[4];
        String[] fields = new String[]{tax_name, tax_id, tax_rate, tax_type};

        Cursor cursor;

        if (myPref.getPreferences(MyPreferences.pref_show_only_group_taxes))
            cursor = DBManager.getDatabase().query(false, table_name, fields, "tax_type = ? AND tax_id = ?", new String[]{"G", taxId}, null, null, tax_name, null);
        else
            cursor = DBManager.getDatabase().query(false, table_name, fields, "tax_id = ?", new String[]{taxId}, null, null, tax_name, null);

        if (cursor.moveToFirst()) {
            do {

                data[0] = cursor.getString(cursor.getColumnIndex(tax_name));
                data[1] = cursor.getString(cursor.getColumnIndex(tax_id));
                data[2] = cursor.getString(cursor.getColumnIndex(tax_rate));
                data[3] = cursor.getString(cursor.getColumnIndex(tax_type));
                list.add(data);
                data = new String[4];
            } while (cursor.moveToNext());
        }

        cursor.close();
        //db.close();
        return list;
    }

    public List<GroupTax> getGroupTaxRate(String taxGroupId) {
        List<GroupTax> list = new ArrayList<GroupTax>();
        GroupTax data = new GroupTax();
        Cursor cursor = DBManager.getDatabase().rawQuery("SELECT t.tax_name,t.tax_rate/100 as 'tax_rate',t.prTax " +
                "FROM Taxes t INNER JOIN Taxes_Group tg ON t.tax_id = tg.taxId " +
                "WHERE tg.taxGroupId ='" + StringUtil.nullStringToEmpty(taxGroupId) + "' ORDER BY t.tax_name ASC", null);
        if (cursor.moveToFirst()) {
            do {
                data.setTaxName(cursor.getString(cursor.getColumnIndex(tax_name)));
                data.setTaxRate(cursor.getString(cursor.getColumnIndex(tax_rate)));
                data.setPrTax(cursor.getString(cursor.getColumnIndex(prTax)));
                list.add(data);
                data = new GroupTax();
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Tax getTax(String taxID, String taxType, double prodPrice) {
        Tax tax = new Tax(taxID);
        tax.setTaxType(taxType);
        String taxRate;

        String subquery1 = "SELECT tax_rate,tax_name, tax_type FROM ";
        String subquery2 = " WHERE tax_id = '";

        StringBuilder sb = new StringBuilder();

        sb.append(subquery1).append(table_name).append(subquery2).append(taxID).append("'");

        if (myPref.isRetailTaxes()) {
            sb.append(" AND tax_code_id IS NOT NULL AND tax_code_id != '' AND tax_code_id = '").append(taxType).append("'");
        }

        Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);
        boolean isGroupTax = false;
        if (cursor.moveToFirst()) {
            taxRate = cursor.getString(cursor.getColumnIndex("tax_rate"));
            tax.setTaxRate(taxRate);
            tax.setTaxName(cursor.getString(cursor.getColumnIndex("tax_name")));
            if (cursor.getString(cursor.getColumnIndex("tax_type")).equals("G"))
                isGroupTax = true;
        }

        cursor.close();

        if (isGroupTax && myPref.isRetailTaxes() && !taxType.isEmpty()) {
            sb.setLength(0);
            sb.append("SELECT tax_rate,taxLowRange,taxHighRange FROM Taxes_Group WHERE taxgroupid= ? AND taxcode_id = ?");
            cursor = DBManager.getDatabase().rawQuery(sb.toString(), new String[]{taxID, taxType});
            if (cursor.moveToFirst()) {
                int i_tax_rate = cursor.getColumnIndex("tax_rate");
                int i_taxLowRange = cursor.getColumnIndex("taxLowRange");
                int i_taxHighRange = cursor.getColumnIndex("taxHighRange");

                double total_tax_rate = 0;
                do {
                    double lowRange = cursor.getDouble(i_taxLowRange);
                    double highRange = cursor.getDouble(i_taxHighRange);

                    if (prodPrice >= lowRange && prodPrice <= highRange)
                        total_tax_rate += cursor.getDouble(i_tax_rate);
                } while (cursor.moveToNext());

                taxRate = Double.toString(total_tax_rate);
                tax.setTaxRate(taxRate);
            }
        }
        cursor.close();
        return tax;
    }


    public String getTaxRate(String taxID, String taxType, double prodPrice) {

        return getTax(taxID, taxType, prodPrice).getTaxRate();
//        //SQLiteDatabase db = dbManager.openReadableDB();
//        String taxRate = "0.0";
//
//        String subquery1 = "SELECT tax_rate, tax_type FROM ";
//        String subquery2 = " WHERE tax_id = '";
//
//        StringBuilder sb = new StringBuilder();
//
//        sb.append(subquery1).append(table_name).append(subquery2).append(taxID).append("'");
//
//        if (myPref.getPreferences(MyPreferences.pref_retail_taxes)) {
//            sb.append(" AND tax_code_id = '").append(taxType).append("'");
//        }
//
//        Cursor cursor = DBManager.database.rawQuery(sb.toString(), null);
//        boolean isGroupTax = false;
//        if (cursor.moveToFirst()) {
//            taxRate = cursor.getString(cursor.getColumnIndex("tax_rate"));
//            if (cursor.getString(cursor.getColumnIndex("tax_type")).equals("G"))
//                isGroupTax = true;
//        }
//
//        cursor.close();
//
//        if (isGroupTax && myPref.getPreferences(MyPreferences.pref_retail_taxes) && !taxType.isEmpty()) {
//            sb.setLength(0);
//            sb.append("SELECT tax_rate,taxLowRange,taxHighRange FROM Taxes_Group WHERE taxgroupid= ? AND taxcode_id = ?");
//            cursor = DBManager.database.rawQuery(sb.toString(), new String[]{taxID, taxType});
//            if (cursor.moveToFirst()) {
//                int i_tax_rate = cursor.getColumnIndex("tax_rate");
//                int i_taxLowRange = cursor.getColumnIndex("taxLowRange");
//                int i_taxHighRange = cursor.getColumnIndex("taxHighRange");
//
//                double total_tax_rate = 0;
//                do {
//                    double lowRange = cursor.getDouble(i_taxLowRange);
//                    double highRange = cursor.getDouble(i_taxHighRange);
//
//                    if (prodPrice >= lowRange && prodPrice <= highRange)
//                        total_tax_rate += cursor.getDouble(i_tax_rate);
//                } while (cursor.moveToNext());
//
//                taxRate = Double.toString(total_tax_rate);
//            }
//        }
//        //db.close();
//        cursor.close();
//        return taxRate;
    }

    public List<HashMap<String, String>> getTaxDetails(String taxID, String taxType) {
        //SQLiteDatabase db = dbManager.openReadableDB();

        String subquery1 = "SELECT tax_id,tax_name,tax_code_id,tax_rate,tax_type FROM ";
        String subquery2 = " WHERE tax_id = '";

        StringBuilder sb = new StringBuilder();

        sb.append(subquery1).append(table_name).append(subquery2).append(taxID).append("'");

        if (myPref.isRetailTaxes()) {
            sb.append(" AND tax_code_id = '").append(taxType).append("'");
        }

        Cursor c = DBManager.getDatabase().rawQuery(sb.toString(), null);
        List<HashMap<String, String>> listMap = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> tempMap = new HashMap<String, String>();
        if (c.moveToFirst()) {
            tempMap.put(tax_id, c.getString(c.getColumnIndex(tax_id)));
            tempMap.put(tax_name, c.getString(c.getColumnIndex(tax_name)));
            tempMap.put(tax_code_id, c.getString(c.getColumnIndex(tax_code_id)));
            tempMap.put(tax_rate, c.getString(c.getColumnIndex(tax_rate)));
            tempMap.put(tax_type, c.getString(c.getColumnIndex(tax_type)));

            listMap.add(tempMap);
        }

        c.close();
        //db.close();

        return listMap;
    }
}
