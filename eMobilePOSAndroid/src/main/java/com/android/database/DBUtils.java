package com.android.database;

import com.android.support.HttpClient;
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

public class DBUtils {
    public static final String storageConnectionString = "DefaultEndpointsProtocol=https;"
            + "AccountName=enablerbackup;"
            + "AccountKey=qSBKH0hNLkqdC4hgfNfE88xAeVpUnA4h2ZCX+P4EhKsrNYlLMYN+Jq4U/Ylyhdy1ctK89Bk4LIPZz5Nh4K8pSg==;" +
            "EndpointSuffix=core.windows.net";

    DBChild dbChild;
    Map<String, Object> sparseArray = new HashMap<>();
    HttpClient httpClient;
//    String url = "https://emobilepos-53888.firebaseio.com/";
    private SQLiteStatement statement;

    public static DBUtils getInstance(SQLiteStatement statement, DBChild dbChild) {
        DBUtils dbUtils = new DBUtils();
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


    public void bindString(int index, String value) {
        statement.bindString(index, value);
        sparseArray.put(String.valueOf(index), value);
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

}
