package com.android.database;

import android.content.Context;

import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DeviceDefaultValuesHandler {

    private final String df_id = "df_id";
    private final String posAdminPassword = "posAdminPassword";
    private final String loyaltyPointFeature = "loyaltyPointFeature";
    private final String pointsType = "pointsType";
    private final String defaultPointsPricePercentage = "defaultPointsPricePercentage";
    private final String globalDiscountID = "globalDiscountID";
    private final String SaFOption = "SaFOption";
    public final String table_name = "deviceDefaultValues";

    private final List<String> attr = Arrays.asList(df_id, posAdminPassword, loyaltyPointFeature,
            pointsType, defaultPointsPricePercentage, globalDiscountID);

    private StringBuilder sb1, sb2;
    private final String empStr = "";
    private MyPreferences myPref;
    private HashMap<String, Integer> attrHash;
    private List<String[]> addrData;
    private List<HashMap<String, Integer>> dictionaryListMap;

    public DeviceDefaultValuesHandler(Context activity) {
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

    private String getData(String tag, int record) {
        Integer i = dictionaryListMap.get(record).get(tag);
        if (i != null) {
            return addrData.get(record)[i];
        }
        return empStr;
    }

    private int index(String tag) {
        return attrHash.get(tag);
    }

    public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
        DBManager.getDatabase().beginTransaction();
        try {

            addrData = data;
            dictionaryListMap = dictionary;
            SQLiteStatement insert = null;
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString())
                    .append(")");
            insert = DBManager.getDatabase().compileStatement(sb.toString());

            int size = addrData.size();

            for (int i = 0; i < size; i++) {
                insert.bindString(index(df_id), getData(df_id, i));    //df_id
                insert.bindString(index(posAdminPassword), getData(posAdminPassword, i));    //posAdminPassword
                myPref.setPOSAdminPass(getData(posAdminPassword, i));
                myPref.setStoredAndForward(getData(SaFOption, i).equals("true") ? true : false);
                insert.bindString(index(loyaltyPointFeature), getData(loyaltyPointFeature, i));    //loyaltyPointFeature
                insert.bindString(index(pointsType), getData(pointsType, i));    //pointsType
                insert.bindString(index(defaultPointsPricePercentage), getData(defaultPointsPricePercentage, i));    //defaultPointsPricePercentage
                insert.bindString(index(globalDiscountID), getData(globalDiscountID, i));    //globalDiscountID
                myPref.setPosManagerPass(getData("posManagerPassword", i));

                insert.execute();
                insert.clearBindings();
            }
            insert.close();
            DBManager.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.getDatabase().endTransaction();
        }
    }

    public void emptyTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(table_name);
        DBManager.getDatabase().execSQL(sb.toString());
    }
}
