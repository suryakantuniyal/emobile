package com.android.support;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
//	Ciphers ciphers = new Ciphers();

    /**
     *
     * @param url
     * @throws org.apache.http.client.ClientProtocolException
     * @throws java.io.IOException
     * @return
     */
    public String httpJsonRequest(String url) throws ClientProtocolException,
            IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-Type", "application/json");
        response = client.execute(httpGet);
        entity = response.getEntity();
        if (entity != null) {
            String convertStreamToString = convertStreamToString(entity
                    .getContent());
            return convertStreamToString;
        }
        return null;
    }

    /**
     *
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

        String line = null;
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

}

