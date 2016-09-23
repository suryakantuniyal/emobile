package com.enablercorp.oauthclient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Guarionex on 4/21/2016.
 */
public class HttpClient {
//    org.apache.http.client.HttpClient client = new DefaultHttpClient();
//    HttpPost post;
//    StringEntity stringEntity;
//    HttpResponse response;
//    HttpEntity entity;


//    public InputStream httpInputStreamRequest(String url) throws ClientProtocolException,
//            IOException {
//        HttpGet httpGet = new HttpGet(url);
//        httpGet.setHeader("Content-Type", "application/json");
//        response = client.execute(httpGet);
//        entity = response.getEntity();
//        if (entity != null) {
//            return entity.getContent();
//        }
//        return null;
//    }


//    /**
//     * @param url
//     * @return
//     * @throws org.apache.http.client.ClientProtocolException
//     * @throws IOException
//     */
//    public String httpJsonRequest(String url) throws ClientProtocolException,
//            IOException {
//        HttpGet httpGet = new HttpGet(url);
//        httpGet.setHeader("Content-Type", "application/json");
//        response = client.execute(httpGet);
//        entity = response.getEntity();
//        if (entity != null) {
//            String convertStreamToString = convertStreamToString(entity.getContent());
//            return convertStreamToString;
//        }
//        return null;
//    }

//    /**
//     * @param url
//     * @param jsonObject
//     * @return
//     * @throws Exception
//     */
//    public String httpJsonRequest(String url, JSONArray jsonObject)
//            throws Exception {
//
//        post = new HttpPost(url);
//        stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
//        post.setHeader("Content-Type", "application/json");
//        post.setEntity(stringEntity);
//        response = client.execute(post);
//        entity = response.getEntity();
//        if (entity != null) {
//            String convertStreamToString = convertStreamToString(entity
//                    .getContent());
//            return convertStreamToString;
//        }
//        return null;
//    }
//
//    public String httpJsonRequest(String url, JSONObject jsonObject)
//            throws Exception {
//        post = new HttpPost(url);
//        stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
//        post.setHeader("Content-Type", "application/json");
//        post.setEntity(stringEntity);
//        response = client.execute(post);
//        entity = response.getEntity();
//        if (entity != null) {
//            String convertStreamToString = convertStreamToString(entity
//                    .getContent());
//            return convertStreamToString;
//        }
//        return null;
//    }
    public String httpJsonRequest(String urlAddress, String rawData)
            throws Exception {
        URL url = new URL(urlAddress);
        HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
        out.write(rawData.getBytes());
        out.flush();
        out.close();
        return convertStreamToString(httpURLConnection.getInputStream());


//        post = new HttpPost(url);
//        stringEntity = new StringEntity(rawData, "UTF-8");
//        post.setHeader("Content-Type", "application/json");
//        post.setEntity(stringEntity);
//        response = client.execute(post);
//        entity = response.getEntity();
//        if (entity != null) {
//            String convertStreamToString = convertStreamToString(entity
//                    .getContent());
//            return convertStreamToString;
//        }
//        return null;
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

}

