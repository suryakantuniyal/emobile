package com.android.database;

import com.android.support.HttpClient;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteStatement;

import java.util.HashMap;
import java.util.Map;

import util.json.JsonUtils;

public class DBUtils {
//    DatabaseReference dbAuditing;
    DBChild dbChild;
    Map<String, Object> sparseArray = new HashMap<>();
    HttpClient httpClient;
    String url = "https://emobilepos-53888.firebaseio.com/";
    private String index;
    private String sql;
    private SQLiteStatement statement;

    public static DBUtils getInstance(String index, SQLiteStatement statement, String sql, DBChild dbChild) {
        DBUtils dbUtils = new DBUtils();
        dbUtils.index = index;
        dbUtils.sql = sql;
        dbUtils.dbChild = dbChild;
        dbUtils.httpClient = new HttpClient();
        dbUtils.statement = statement;
//        FirebaseDatabase.getInstance().goOnline();
//        dbUtils.dbAuditing = FirebaseDatabase.getInstance().getReference(dbChild.name()).push();
        return dbUtils;
    }

    public static void release() {
        try {
//            FirebaseDatabase.getInstance().goOffline();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bindString(int index, String value) {
        statement.bindString(index, value);
        sparseArray.put(String.valueOf(index), value);
    }

    public void executeAuditedDB() {
        statement.execute();
        try {
            final AuditingRecord record = new AuditingRecord();
            record.setIndex(index);
            record.setSql(sql);
            record.setData(sparseArray);
//            dbAuditing.setValue(record);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String response = httpClient.httpJsonRequest(url + dbChild.name() + ".json", record.toJson());
                    } catch (Exception e) {

                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    public void bindDouble(int index, double value) {
        statement.bindDouble(index, value);
        sparseArray.put(String.valueOf(index), value);
    }

    public void bindNull(int index) {
        statement.bindNull(index);
        sparseArray.put(String.valueOf(index), "null");
    }

    public void bindLong(int index, long value) {
        statement.bindLong(index, value);
        sparseArray.put(String.valueOf(index), value);
    }

    public enum DBChild {
        ORDER_PRODUCT, PAYMENTS, ORDERS
    }

    private class AuditingRecord {
        private String sql;
        private String index;
        private Map<String, Object> data;
        private String timestamp = String.valueOf(System.currentTimeMillis());

        public AuditingRecord() {

        }

        public String toJson() {
            Gson gson = JsonUtils.getInstance();
            String toJson = gson.toJson(this);
            toJson = "{\"" + index + "\":" + toJson + "}";
            return toJson;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
