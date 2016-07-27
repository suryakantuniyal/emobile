package com.android.support;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.android.emobilepos.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Guarionex on 4/21/2016.
 */
public class HttpClient {
    org.apache.http.client.HttpClient client = new DefaultHttpClient();
    HttpPost post;
    StringEntity stringEntity;
    JSONObject jsono;
    HttpResponse response;
    HttpEntity entity;
    Handler handler;
    ProgressDialog progressDialog;
    private DownloadFileCallBack callback;
    public static int incr;
    protected static final int ID = 0;
    private static final int COPY_STARTED = 0;
    private static final int UPDATE_PROGRESS = 1;
    private Context context;

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

    public InputStream httpInputStreamRequest(String url) throws ClientProtocolException,
            IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-Type", "application/json");
        response = client.execute(httpGet);
        entity = response.getEntity();
        if (entity != null) {
            return entity.getContent();
        }
        return null;
    }


    /**
     * @param url
     * @return
     * @throws org.apache.http.client.ClientProtocolException
     * @throws java.io.IOException
     */
    public String httpJsonRequest(String url) throws ClientProtocolException,
            IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-Type", "application/json");
        response = client.execute(httpGet);
        entity = response.getEntity();
        if (entity != null) {
            String convertStreamToString = convertStreamToString(entity.getContent());
            return convertStreamToString;
        }
        return null;
    }

    /**
     * @param url
     * @param jsonObject
     * @return
     * @throws Exception
     */
    public String httpJsonRequest(String url, JSONArray jsonObject)
            throws Exception {

        post = new HttpPost(url);

//		String encrypt = ciphers.encrypt(Ciphers.DEFAULT_SEED,
//				jsonObject.toString());
//		String decrypt = ciphers.decrypt(Ciphers.DEFAULT_SEED, encrypt);
        stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        post.setHeader("Content-Type", "application/json");
        post.setEntity(stringEntity);
        response = client.execute(post);
        entity = response.getEntity();
        if (entity != null) {
            String convertStreamToString = convertStreamToString(entity
                    .getContent());
            return convertStreamToString;
        }
        return null;
    }

    public String httpJsonRequest(String url, JSONObject jsonObject)
            throws Exception {

        post = new HttpPost(url);
//		String encrypt = ciphers.encrypt(Ciphers.DEFAULT_SEED,
//				jsonObject.toString());
//		String decrypt = ciphers.decrypt(Ciphers.DEFAULT_SEED, encrypt);
        stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        post.setHeader("Content-Type", "application/json");
        post.setEntity(stringEntity);
        response = client.execute(post);
        entity = response.getEntity();
        if (entity != null) {
            String convertStreamToString = convertStreamToString(entity
                    .getContent());
            return convertStreamToString;
        }
        return null;
    }

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
                sb.append(line + "\n");
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

    public void downloadFileAsync(String urlAddress, String path, DownloadFileCallBack callBack, Context context) {
        this.callback = callBack;
        this.context = context;
        setHandler();
        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(false);
        progressDialog.setTitle(R.string.downloadig_update);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
//        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        mBuilder = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.drawable.ic_file_download_black_18dp)
//                .setContentTitle(context.getString(R.string.app_name))
//                .setContentText(context.getString(R.string.downloadig_update));
        Message handlerMsg = handler.obtainMessage();
        handlerMsg.what = COPY_STARTED;
        handlerMsg.arg1 = 1;
        handler.sendMessage(handlerMsg);
        new DownloadFileTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urlAddress, path);
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

    public interface DownloadFileCallBack {
        public void downloadCompleted(String path);

        public void downloadFail();
    }

    private void setHandler() {
        handler = new Handler() {

            public void handleMessage(android.os.Message msg) {
                incr = msg.arg1;

                if (incr < 100) {
                    // Sets the progress indicator to a max value, the
                    // current completion percentage, and "determinate"
                    // state
                    progressDialog.setProgress(incr);

//                    mBuilder.setProgress(100, incr, false);
                    // Displays the progress bar for the first time.
//                    mNotificationManager.notify(0, mBuilder.build());
                    // Sleeps the thread, simulating an operation
                    // that takes time

                } else {
                    if (incr == 999) {
                        // When the loop is finished, updates the notification
//                        mBuilder.setContentText("Download fail.")
//                                // Removes the progress bar
//                                .setProgress(0, 0, false);
//                        mNotificationManager.notify(ID, mBuilder.build());
                    } else {
                        // When the loop is finished, updates the notification
                        progressDialog.setProgress(100);
                        progressDialog.dismiss();
//                        mBuilder.setContentText(
//                                "Download completed.")
//                                // Removes the progress bar
//                                .setProgress(0, 0, false);
//                        mNotificationManager.notify(ID, mBuilder.build());
                    }
                }
            }
        };
    }
}

