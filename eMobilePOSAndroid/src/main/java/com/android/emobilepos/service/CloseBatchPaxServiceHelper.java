package com.android.emobilepos.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import drivers.pax.utils.BatchPaxContentProvider;

public class CloseBatchPaxServiceHelper {

    public void insertIntoResults(ContentResolver contentResolver,String result){
        int maxRecordsCount = 5;
        ContentValues values = new ContentValues();
        values.put(BatchPaxContentProvider.RESULT, result);
        Uri uri = contentResolver.insert( BatchPaxContentProvider.CONTENT_URI, values);
        List<CloseBatchPaxResult> results = getResults(contentResolver,"DESC");
        int i = 0;
        for(CloseBatchPaxResult res: results){
            i++;
            if(i > maxRecordsCount){
                delete(contentResolver,res.getId());
            }
        }
    }
    public List<CloseBatchPaxResult> getResults(ContentResolver contentResolver, String order){
        ArrayList<CloseBatchPaxResult> results = new ArrayList<CloseBatchPaxResult>();
        CloseBatchPaxResult closeBatchPaxResult = null;
        Uri resultsUri = BatchPaxContentProvider.CONTENT_URI;
        Cursor c = contentResolver.query(resultsUri, null, null, null, BatchPaxContentProvider._ID+ " "+order);
        if (c.moveToFirst()) {
            do{
                closeBatchPaxResult = new CloseBatchPaxResult();
                closeBatchPaxResult.setId(Long.parseLong(c.getString(c.getColumnIndex(BatchPaxContentProvider._ID))));
                closeBatchPaxResult.setResult(c.getString(c.getColumnIndex(BatchPaxContentProvider.RESULT)));
                closeBatchPaxResult.setResultDate(c.getString(c.getColumnIndex(BatchPaxContentProvider.RESULT_DATE)));
                results.add(closeBatchPaxResult);
            } while (c.moveToNext());
        }
        c.close();
        return results;
    }
    public void delete(ContentResolver contentResolver,long id){
        try{
            Uri uri = BatchPaxContentProvider.CONTENT_URI;
            contentResolver.delete(uri, BatchPaxContentProvider._ID ,new String[]{""+id});
        }catch (Exception x){
            x.printStackTrace();
        }
    }
}
