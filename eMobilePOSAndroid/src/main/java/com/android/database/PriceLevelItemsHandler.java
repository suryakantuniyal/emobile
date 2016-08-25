package com.android.database;

import android.app.Activity;

import com.android.emobilepos.models.ItemPriceLevel;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PriceLevelItemsHandler {

    private final String pricelevel_prod_id = "pricelevel_prod_id";
    private final String pricelevel_id = "pricelevel_id";
    private final String pricelevel = "pricelevel";
    private final String pricelevel_price = "pricelevel_price";
    private final String pricelevel_update = "pricelevel_update";
    private final String isactive = "isactive";

    private final List<String> attr = Arrays.asList(pricelevel_prod_id, pricelevel_id, pricelevel, pricelevel_price, pricelevel_update,
            isactive);

    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;
    private List<String[]> addrData;
    private List<HashMap<String, Integer>> dictionaryListMap;
    private static final String table_name = "PriceLevelItems";

    public PriceLevelItemsHandler(Activity activity) {
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

    public void insert(List<ItemPriceLevel> itemPriceLevels) {
        DBManager._db.beginTransaction();
        try {
            SQLiteStatement insert;
            insert = DBManager._db.compileStatement("INSERT INTO " + table_name + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() + ")");

            for (ItemPriceLevel itemPriceLevel : itemPriceLevels) {
                insert.bindString(index(pricelevel_prod_id), itemPriceLevel.getPricelevelProdId()); // pricelevel_prod_id
                insert.bindString(index(pricelevel_id), itemPriceLevel.getPriceLevelId()); // pricelevel_id
                insert.bindString(index(pricelevel), itemPriceLevel.getPriceLevel()); // pricelevel
                insert.bindString(index(pricelevel_price), itemPriceLevel.getPriceLevelPrice()); // pricelevel_price
                insert.bindString(index(pricelevel_update), itemPriceLevel.getPriceLevelUpdate()); // pricelevel_update
                insert.bindString(index(isactive), itemPriceLevel.getIsActive()); // isactive
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

    public void emptyTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(table_name);
        DBManager._db.execSQL(sb.toString());
    }

}
