package com.android.database;

import android.content.Context;

import com.android.support.Encrypt;
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
    private Context context;

    public DeviceDefaultValuesHandler(Context context) {
        this.context = context;
        attrHash = new HashMap<>();
        sb1 = new StringBuilder();
        sb2 = new StringBuilder();
        myPref = new MyPreferences(context);
        new DBManager(context);
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
            SQLiteStatement insert;
            String sb = "INSERT INTO " + table_name + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() +
                    ")";
            insert = DBManager.getDatabase().compileStatement(sb);
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

            DBManager.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.getDatabase().endTransaction();
        }
    }

    public void emptyTable() {
        DBManager.getDatabase().execSQL("DELETE FROM " + table_name);
    }
}