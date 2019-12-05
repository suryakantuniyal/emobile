package com.android.database;

import android.database.Cursor;

import com.android.emobilepos.models.PriceLevel;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PriceLevelHandler {

    public static final List<String> attr = Arrays.asList("pricelevel_id", "pricelevel_name", "pricelevel_type",
            "pricelevel_fixedpct", "pricelevel_update", "isactive");
    public static final String table_name = "PriceLevel";
    private final String pricelevel_id = "pricelevel_id";
    private final String pricelevel_name = "pricelevel_name";
    private final String pricelevel_type = "pricelevel_type";
    private final String pricelevel_fixedpct = "pricelevel_fixedpct";
    private final String pricelevel_update = "pricelevel_update";
    private final String isactive = "isactive";

    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;
    private List<String[]> addrData;
    private List<HashMap<String, Integer>> dictionaryListMap;

    public PriceLevelHandler() {
        attrHash = new HashMap<String, Integer>();
        addrData = new ArrayList<String[]>();
        sb1 = new StringBuilder();
        sb2 = new StringBuilder();
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


//    public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
//        DBManager.database.beginTransaction();
//        try {
//
//            addrData = data;
//            dictionaryListMap = dictionary;
//            SQLiteStatement insert;
//            insert = DBManager.database.compileStatement("INSERT INTO " + table_name + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() + ")");
//
//            int size = addrData.size();
//
//            for (int j = 0; j < size; j++) {
//
//                insert.bindString(index(pricelevel_id), getData(pricelevel_id, j)); // pricelevel_id
//                insert.bindString(index(pricelevel_name), getData(pricelevel_name, j)); // pricelevel_name
//                insert.bindString(index(pricelevel_type), getData(pricelevel_type, j)); // pricelevel_type
//                insert.bindString(index(pricelevel_fixedpct), getData(pricelevel_fixedpct, j)); // pricelevel_fixedpct
//                insert.bindString(index(pricelevel_update), getData(pricelevel_update, j)); // pricelevel_update
//                insert.bindString(index(isactive), getData(isactive, j)); // isactive
//
//                insert.execute();
//                insert.clearBindings();
//            }
//            insert.close();
//            DBManager.database.setTransactionSuccessful();
//        } catch (Exception e) {
//
//        } finally {
//            DBManager.database.endTransaction();
//        }
//    }


    public void insert(List<PriceLevel> priceLevels) {

        DBManager.getDatabase().beginTransaction();
        SQLiteStatement insert=null;
        try {
            insert = DBManager.getDatabase().compileStatement("INSERT INTO " + table_name + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() + ")");

            int size = addrData.size();

            for (PriceLevel priceLevel : priceLevels) {
                insert.bindString(index(pricelevel_id), priceLevel.getPricelevelId()); // pricelevel_id
                insert.bindString(index(pricelevel_name), priceLevel.getPricelevelName()); // pricelevel_name
                insert.bindString(index(pricelevel_type), priceLevel.getPricelevelType()); // pricelevel_type
                insert.bindDouble(index(pricelevel_fixedpct), priceLevel.getPricelevelFixedpct()); // pricelevel_fixedpct
                insert.bindString(index(pricelevel_update), priceLevel.getPricelevelUpdate()); // pricelevel_update
                insert.bindLong(index(isactive), priceLevel.getIsactive()); // isactive

                insert.execute();
                insert.clearBindings();
            }
            //  insert.close();
            DBManager.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {

        } finally {
            if (insert != null) {
                insert.close();
            }
            DBManager.getDatabase().endTransaction();

        }
    }

    public void emptyTable() {
        DBManager.getDatabase().execSQL("DELETE FROM " + table_name);
    }

    public List<PriceLevel> getFixedPriceLevel(String prod_id) {
        Cursor cursor = null;
        try {
            List<PriceLevel> list = new ArrayList<PriceLevel>();

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT pl.pricelevel_name,pl.pricelevel_id,ROUND(((p.prod_price)+(p.prod_price*pl.pricelevel_fixedpct/100)),2) as result FROM Products p,PriceLevel pl WHERE  pl.pricelevel_type = 'FixedPercentage' AND p.prod_id = '");
            sb.append(prod_id).append("'");

            cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);
            PriceLevel data;
            if (cursor.moveToFirst()) {
                do {
                    data = new PriceLevel();
                    data.setPricelevelName(cursor.getString(cursor.getColumnIndex(pricelevel_name)));
                    data.setPricelevelId(cursor.getString(cursor.getColumnIndex(pricelevel_id)));
                    data.setCalcResult(cursor.getString(cursor.getColumnIndex("result")));
                    list.add(data);
                } while (cursor.moveToNext());
            }
            cursor.close();

//		if(myPref.isCustSelected())
//		{
            sb.setLength(0);
            sb.append("SELECT pl.pricelevel_name,pl.pricelevel_id,pli.pricelevel_price as result FROM PriceLevel pl LEFT OUTER JOIN ");
            sb.append("PriceLevelItems pli ON  pli.pricelevel_id = pl.pricelevel_id LEFT OUTER JOIN Products p ON  pli.pricelevel_prod_id = p.prod_id ");
            sb.append("WHERE  p.prod_id = '").append(prod_id).append("'");

            cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);
            if (cursor.moveToFirst()) {
                do {
                    data = new PriceLevel();
                    data.setPricelevelName(cursor.getString(cursor.getColumnIndex(pricelevel_name)));
                    data.setPricelevelId(cursor.getString(cursor.getColumnIndex(pricelevel_id)));
                    data.setCalcResult(cursor.getString(cursor.getColumnIndex("result")));
                    list.add(data);
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

    public List<String[]> getPriceLevel() {
        Cursor cursor=null;
        try {
            List<String[]> list = new ArrayList<String[]>();
            String[] data = new String[3];
            String[] fields = new String[]{pricelevel_name, pricelevel_id, pricelevel_fixedpct};

             cursor = DBManager.getDatabase().query(true, table_name, fields, null, null, null, null, pricelevel_name, null);

            if (cursor.moveToFirst()) {
                do {

                    data[0] = cursor.getString(cursor.getColumnIndex(pricelevel_name));
                    data[1] = cursor.getString(cursor.getColumnIndex(pricelevel_id));
                    data[2] = cursor.getString(cursor.getColumnIndex(pricelevel_fixedpct));
                    list.add(data);
                    data = new String[3];
                } while (cursor.moveToNext());
            }

            cursor.close();
            return list;
        }finally {
            if(cursor!=null && !cursor.isClosed())
            {
                cursor.close();
            }
        }
    }
}
