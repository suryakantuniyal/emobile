package com.android.support;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.android.emobilepos.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Created by Guarionex on 4/21/2016.
 */
public class HttpClient {
    protected static final int ID = 0;
    private static final int COPY_STARTED = 0;
    private static final int UPDATE_PROGRESS = 1;
    private static int incr;
    private Handler handler;
    private ProgressDialog progressDialog;
    private DownloadFileCallBack callback;

    private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static void uploadCloudFile(String bucketName, Uri file, OnFailureListener failCallback, OnSuccessListener successCallback) {
        String date = DateUtils.getDateAsString(new Date(), "yyyyMMdd_hhmm");
        FirebaseStorage storage = FirebaseStorage.getInstance();//("gs://emobilepos-53888.appspot.com");
        StorageReference fileReference = storage.getReference().child("emobilepos/" + bucketName + "_" + date + "_" + file.getLastPathSegment());
        UploadTask uploadTask = fileReference.putFile(file);
        if (failCallback != null) {
            uploadTask.addOnFailureListener(failCallback);
        }
        if (successCallback != null) {
            uploadTask.addOnSuccessListener(successCallback);
        }
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(context, R.string.backup_data_fail, Toast.LENGTH_LONG).show();
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                Toast.makeText(context, R.string.backup_data_success, Toast.LENGTH_LONG).show();
//            }
//        });
    }

    public String downloadFile(String urlAddress, String path) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {

            URL url = new URL(urlAddress);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
//            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                return "Server returned HTTP " + connection.getResponseCode()
//                        + " " + connection.getResponseMessage();
//            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(path);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
//                if (isCancelled()) {
//                    input.close();
//                    return null;
//                }
                total += count;
                // publishing the progress....
//                if (fileLength > 0) // only if total length is known
//                    publishProgress((int) (total * 100 / fileLength));
                if (fileLength > 0) {
                    if (handler != null) {
                        Message handlerMsg = handler.obtainMessage();
                        handlerMsg.what = UPDATE_PROGRESS;
                        handlerMsg.arg1 = (int) (total * 100 / fileLength);
                        handler.sendMessage(handlerMsg);
                    }
                    incr = (int) (total * 100 / fileLength);
                }
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                incr = 100;
                if (handler != null) {
                    Message handlerMsg = handler.obtainMessage();
                    handlerMsg.what = UPDATE_PROGRESS;
                    handlerMsg.arg1 = 100;
                    handler.sendMessage(handlerMsg);
                }

                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return path;
    }

    public void downloadFileAsync(String urlAddress, String path, DownloadFileCallBack callBack, Context context) {
        this.callback = callBack;
        setHandler();
        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(false);
        progressDialog.setTitle(R.string.downloadig_update);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        Message handlerMsg = handler.obtainMessage();
        handlerMsg.what = COPY_STARTED;
        handlerMsg.arg1 = 1;
        handler.sendMessage(handlerMsg);
        new DownloadFileTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urlAddress, path);
    }

    private void setHandler() {
        handler = new Handler() {

            public void handleMessage(android.os.Message msg) {
                incr = msg.arg1;

                if (incr < 100) {
                    progressDialog.setProgress(incr);
                } else {
                    if (incr != 999) {
                        // When the loop is finished, updates the notification
                        progressDialog.setProgress(100);
                        progressDialog.dismiss();
                    }
                }
            }
        };
    }

    public interface DownloadFileCallBack {
        void downloadCompleted(String path);
        void downloadFail();
    }

    private class DownloadFileTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            return downloadFile((String) params[0], (String) params[1]);
        }

        @Override
        protected void onPostExecute(String s) {
            if (TextUtils.isEmpty(s))
                callback.downloadFail();
            else
                callback.downloadCompleted(s);
        }
    }
}

