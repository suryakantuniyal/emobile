package com.android.database;

import android.content.Context;
import android.database.Cursor;

import com.android.emobilepos.models.Address;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import util.StringUtil;

public class AddressHandler {

    private static final String table_name = "Address";
    private final String addr_id = "addr_id";
    private final String cust_id = "cust_id";
    private final String addr_b_str1 = "addr_b_str1";
    private final String addr_b_str2 = "addr_b_str2";
    private final String addr_b_str3 = "addr_b_str3";
    private final String addr_b_city = "addr_b_city";
    private final String addr_b_state = "addr_b_state";
    private final String addr_b_country = "addr_b_country";
    private final String addr_b_zipcode = "addr_b_zipcode";
    private final String addr_s_name = "addr_s_name";
    private final String addr_s_str1 = "addr_s_str1";
    private final String addr_s_str2 = "addr_s_str2";
    private final String addr_s_str3 = "addr_s_str3";
    private final String addr_s_city = "addr_s_city";
    private final String addr_s_state = "addr_s_state";
    private final String addr_s_country = "addr_s_country";
    private final String addr_s_zipcode = "addr_s_zipcode";
    private final String qb_cust_id = "qb_cust_id";
    private final String addr_b_type = "addr_b_type";
    private final String addr_s_type = "addr_s_type";
    private final List<String> attr = Arrays.asList(addr_id, cust_id, addr_b_str1, addr_b_str2, addr_b_str3,
            addr_b_city, addr_b_state, addr_b_country, addr_b_zipcode, addr_s_name, addr_s_str1, addr_s_str2, addr_s_str3, addr_s_city, addr_s_state,
            addr_s_country, addr_s_zipcode, qb_cust_id, addr_b_type, addr_s_type);
    private final String empStr = "";
    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;

    private List<String[]> addrData;
    private List<HashMap<String, Integer>> dictionaryListMap;
    private MyPreferences myPref;


    public AddressHandler(Context activity) {
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
        return empStr;
    }

    private int index(String tag) {
        return attrHash.get(tag);
    }


    public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
        DBManager.getDatabase().beginTransaction();
        SQLiteStatement insert = null;
        try {

            addrData = data;
            dictionaryListMap = dictionary;

            insert = DBManager.getDatabase().compileStatement("INSERT INTO " + table_name + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() + ")");

            int size = addrData.size();

            for (int j = 0; j < size; j++) {
                insert.bindString(index(cust_id), getData(cust_id, j)); // cust_id
                insert.bindString(index(addr_id), getData(addr_id, j)); // addr_id
                insert.bindString(index(addr_b_str1), getData(addr_b_str1, j)); // addr_b_str1
                insert.bindString(index(addr_b_str2), getData(addr_b_str2, j)); // addr_b_str2
                insert.bindString(index(addr_b_str3), getData(addr_b_str3, j)); // addr_b_str3
                insert.bindString(index(addr_b_city), getData(addr_b_city, j)); // addr_b_city
                insert.bindString(index(addr_b_state), getData(addr_b_state, j)); // addr_b_state
                insert.bindString(index(addr_b_country), getData(addr_b_country, j)); // addr_b_country
                insert.bindString(index(addr_b_zipcode), getData(addr_b_zipcode, j)); // addr_b_zipcode
                insert.bindString(index(addr_s_name), getData(addr_s_name, j)); // addr_s_name
                insert.bindString(index(addr_s_str1), getData(addr_s_str1, j)); // addr_s_str1
                insert.bindString(index(addr_s_str2), getData(addr_s_str2, j)); // addr_s_str2
                insert.bindString(index(addr_s_str3), getData(addr_s_str3, j)); // addr_s_str3
                insert.bindString(index(addr_s_city), getData(addr_s_city, j)); // addr_s_city
                insert.bindString(index(addr_s_state), getData(addr_s_state, j)); // addr_s_state
                insert.bindString(index(addr_s_country), getData(addr_s_country, j)); // addr_s_country
                insert.bindString(index(addr_s_zipcode), getData(addr_s_zipcode, j)); // addr_s_zipcode
                insert.bindString(index(qb_cust_id), getData(qb_cust_id, j)); // qb_cust_id
                insert.bindString(index(addr_b_type), getData(addr_b_type, j)); // addr_b_type
                insert.bindString(index(addr_s_type), getData(addr_s_type, j)); // addr_s_type

                insert.execute();
                insert.clearBindings();

            }

            DBManager.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {

        } finally {
            if (insert != null) {
                insert.close();
            }
            DBManager.getDatabase().endTransaction();

        }
    }


    public void insertOneAddress(Address address) {
        SQLiteStatement insert = null;
        DBManager.getDatabase().beginTransaction();
        try {


            String sb = "INSERT INTO " + table_name + " (" + sb1.toString() + ") " + "VALUES (" + sb2.toString() +
                    ")";
            insert = DBManager.getDatabase().compileStatement(sb);

            insert.bindString(index(addr_id), address.getAddr_id() == null ? "" : address.getAddr_id());
            insert.bindString(index(cust_id), address.getCust_id() == null ? "" : address.getCust_id());
            insert.bindString(index(addr_b_str1), address.getAddr_b_str1() == null ? "" : address.getAddr_b_str1());
            insert.bindString(index(addr_b_str2), address.getAddr_b_str2() == null ? "" : address.getAddr_b_str2());
            insert.bindString(index(addr_b_str3), address.getAddr_b_str3() == null ? "" : address.getAddr_b_str3());
            insert.bindString(index(addr_b_city), address.getAddr_b_city() == null ? "" : address.getAddr_b_city());
            insert.bindString(index(addr_b_state), address.getAddr_b_state() == null ? "" : address.getAddr_b_state());
            insert.bindString(index(addr_b_country), address.getAddr_b_country() == null ? "" : address.getAddr_b_country());
            insert.bindString(index(addr_b_zipcode), address.getAddr_b_zipcode() == null ? "" : address.getAddr_b_zipcode());
            insert.bindString(index(addr_s_name), address.getAddr_s_name() == null ? "" : address.getAddr_s_name());
            insert.bindString(index(addr_s_str1), address.getAddr_s_str1() == null ? "" : address.getAddr_s_str1());
            insert.bindString(index(addr_s_str2), address.getAddr_s_str2() == null ? "" : address.getAddr_s_str2());
            insert.bindString(index(addr_s_str3), address.getAddr_s_str3() == null ? "" : address.getAddr_s_str3());
            insert.bindString(index(addr_s_city), address.getAddr_s_city() == null ? "" : address.getAddr_s_city());
            insert.bindString(index(addr_s_state), address.getAddr_s_state() == null ? "" : address.getAddr_s_state());
            insert.bindString(index(addr_s_country), address.getAddr_s_country() == null ? "" : address.getAddr_s_country());
            insert.bindString(index(addr_s_zipcode), address.getAddr_s_zipcode() == null ? "" : address.getAddr_s_zipcode());
            insert.bindString(index(qb_cust_id), address.getQb_cust_id() == null ? "" : address.getQb_cust_id());
            insert.bindString(index(addr_b_type), address.getAddr_b_type() == null ? "" : address.getAddr_b_type());
            insert.bindString(index(addr_s_type), address.getAddr_s_type() == null ? "" : address.getAddr_s_type());


            insert.execute();
            insert.clearBindings();

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
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(table_name);
        DBManager.getDatabase().execSQL(sb.toString());
    }


    public Cursor getCursorAddress(String custID) {
        String sb = "SELECT addr_id,addr_b_str1,addr_b_str2,addr_b_str3,addr_b_city,addr_b_state,addr_b_country,addr_b_zipcode," +
                "addr_s_str1,addr_s_str2,addr_s_str3,addr_s_city,addr_s_state,addr_s_country,addr_s_zipcode,addr_b_type,addr_s_type FROM Address WHERE cust_id = ?";

        return DBManager.getDatabase().rawQuery(sb, new String[]{custID});
    }


    public List<Address> getSpecificAddress(String custID) {
        List<Address> addresses = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = DBManager.getDatabase().rawQuery("SELECT " +
                    "addr_b_str1,addr_b_str2,addr_b_str3,addr_b_country,addr_b_city, addr_b_state,addr_b_zipcode, " +
                    "addr_s_str1,addr_s_str2,addr_s_str3,addr_s_country,addr_s_city, addr_s_state,addr_s_zipcode " +
                    "FROM Address WHERE cust_id= ?", new String[]{custID});
            Address address;
            if (cursor.moveToFirst()) {
                address = new Address();
                int addrSTR1Index = cursor.getColumnIndex(addr_b_str1);
                int addrSTR2Index = cursor.getColumnIndex(addr_b_str2);
                int addrSTR3Index = cursor.getColumnIndex(addr_b_str3);
                int addrCountryIndex = cursor.getColumnIndex(addr_b_country);
                int addrCityIndex = cursor.getColumnIndex(addr_b_city);
                int addrStateIndex = cursor.getColumnIndex(addr_b_state);
                int addrZipCodeIndex = cursor.getColumnIndex(addr_b_zipcode);

                int addr2STR1Index = cursor.getColumnIndex(addr_s_str1);
                int addr2STR2Index = cursor.getColumnIndex(addr_s_str2);
                int addr2STR3Index = cursor.getColumnIndex(addr_s_str3);
                int addr2CountryIndex = cursor.getColumnIndex(addr_s_country);
                int addr2CityIndex = cursor.getColumnIndex(addr_s_city);
                int addr2StateIndex = cursor.getColumnIndex(addr_s_state);
                int addr2ZipCodeIndex = cursor.getColumnIndex(addr_s_zipcode);
                do {
                    address.setAddr_b_str1(cursor.getString(addrSTR1Index));
                    address.setAddr_b_str2(cursor.getString(addrSTR2Index));
                    address.setAddr_b_str3(cursor.getString(addrSTR3Index));
                    address.setAddr_b_country(cursor.getString(addrCountryIndex));
                    address.setAddr_b_city(cursor.getString(addrCityIndex));
                    address.setAddr_b_state(cursor.getString(addrStateIndex));
                    address.setAddr_b_zipcode(cursor.getString(addrZipCodeIndex));

                    address.setAddr_s_str1(cursor.getString(addr2STR1Index));
                    address.setAddr_s_str2(cursor.getString(addr2STR2Index));
                    address.setAddr_s_str3(cursor.getString(addr2STR3Index));
                    address.setAddr_s_country(cursor.getString(addr2CountryIndex));
                    address.setAddr_s_city(cursor.getString(addr2CityIndex));
                    address.setAddr_s_state(cursor.getString(addr2StateIndex));
                    address.setAddr_s_zipcode(cursor.getString(addr2ZipCodeIndex));
                    addresses.add(address);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return addresses;
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }


    public List<String[]> getAddress() {
        String sb = "SELECT addr_id,addr_s_str1,addr_s_str2,addr_s_str3,addr_s_country,addr_s_city,addr_s_state,addr_s_zipcode FROM Address WHERE cust_id = ? " +
                " AND cust_id != '' ORDER BY addr_id";


        Cursor cursor = DBManager.getDatabase().rawQuery(sb, new String[]{StringUtil.nullStringToEmpty(myPref.getCustID())});
        List<String[]> arrayList = new ArrayList<>();
        String[] arrayValues = new String[8];

        if (cursor.moveToFirst()) {
            int addrIDIndex = cursor.getColumnIndex(addr_id);
            int addrSTR1Index = cursor.getColumnIndex(addr_s_str1);
            int addrSTR2Index = cursor.getColumnIndex(addr_s_str2);
            int addrSTR3Index = cursor.getColumnIndex(addr_s_str3);
            int addrCountryIndex = cursor.getColumnIndex(addr_s_country);
            int addrCityIndex = cursor.getColumnIndex(addr_s_city);
            int addrStateIndex = cursor.getColumnIndex(addr_s_state);
            int addrZipCodeIndex = cursor.getColumnIndex(addr_s_zipcode);
            do {
                arrayValues[0] = cursor.getString(addrIDIndex);
                arrayValues[1] = cursor.getString(addrSTR1Index);
                arrayValues[2] = cursor.getString(addrSTR2Index);
                arrayValues[3] = cursor.getString(addrSTR3Index);
                arrayValues[4] = cursor.getString(addrCountryIndex);
                arrayValues[5] = cursor.getString(addrCityIndex);
                arrayValues[6] = cursor.getString(addrStateIndex);
                arrayValues[7] = cursor.getString(addrZipCodeIndex);

                arrayList.add(arrayValues);
                arrayValues = new String[8];
            } while (cursor.moveToNext());
        }

        cursor.close();
        return arrayList;

    }
}
