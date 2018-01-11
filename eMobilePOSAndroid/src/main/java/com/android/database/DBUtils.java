package com.android.database;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.sqlcipher.database.SQLiteStatement;

import java.util.HashMap;
import java.util.Map;

public class DBUtils {
    DatabaseReference dbAuditing;
    DBChild dbChild;
    Map<String, Object> sparseArray = new HashMap<>();
    private String account;
    private String sql;
    private SQLiteStatement statement;

    public static DBUtils getInstance(String account, SQLiteStatement statement, String sql, DBChild dbChild) {
        DBUtils dbUtils = new DBUtils();
        dbUtils.account = account;
        dbUtils.sql = sql;
        dbUtils.dbChild = dbChild;
        dbUtils.statement = statement;
        FirebaseDatabase.getInstance().goOnline();
        dbUtils.dbAuditing = FirebaseDatabase.getInstance().getReference(dbChild.name()).push();
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
            AuditingRecord record = new AuditingRecord();
            record.setIndex(account);
            record.setSql(sql);
            record.setData(sparseArray);
            dbAuditing.setValue(record);
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
