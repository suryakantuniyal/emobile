package com.android.database;

import com.android.support.HttpClient;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import net.sqlcipher.database.SQLiteStatement;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;

import util.json.JsonUtils;

public class DBUtils {
    public static final String storageConnectionString = "DefaultEndpointsProtocol=https;"
            + "AccountName=enablerbackup;"
            + "AccountKey=qSBKH0hNLkqdC4hgfNfE88xAeVpUnA4h2ZCX+P4EhKsrNYlLMYN+Jq4U/Ylyhdy1ctK89Bk4LIPZz5Nh4K8pSg==;" +
            "EndpointSuffix=core.windows.net";

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
        return dbUtils;
    }

    public static void uploadDatabaseBackup(InputStream dbFileInputStream, String uploadFileName) throws URISyntaxException, InvalidKeyException, StorageException, IOException {
        CloudStorageAccount storageAccount = CloudStorageAccount
                .parse(storageConnectionString);
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference("emsbackupfiles");
        container.createIfNotExists();
        CloudBlockBlob imageBlob = container.getBlockBlobReference(uploadFileName);
        imageBlob.upload(dbFileInputStream, dbFileInputStream.available());
    }

    public static void release() {
        try {
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String response = oauthclient.HttpClient.getString(url + dbChild.name() + ".json", record.toJson(), null);
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
