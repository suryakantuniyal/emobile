package oauthclient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Guarionex on 4/21/2016.
 */
public class HttpClient {
    public enum HTTPMethod {GET, PUT, POST, DELETE}

    public static String getString(String urlAddress, OAuthClient authClient) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        InputStream inputStream = get(urlAddress, authClient);
        return convertStreamToString(inputStream);
    }

    public static String getString(String urlAddress, String json, OAuthClient authClient) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        InputStream inputStream = get(urlAddress, json, authClient);
        return convertStreamToString(inputStream);
    }

    public static HttpsURLConnection getHttpsURLConnection(String urlAddress, HTTPMethod method) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        URL url = new URL(urlAddress);
//        HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();
//        httpURLConnection.setRequestMethod(method.name());
//        httpURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        return getHttpsURLConnection(url, method);
    }

    public static HttpsURLConnection getHttpsURLConnection(URL url, HTTPMethod method) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod(method.name());
        httpURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        return httpURLConnection;
    }

    public static InputStream get(String urlAddress, OAuthClient authClient) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpsURLConnection httpURLConnection = getHttpsURLConnection(urlAddress, HTTPMethod.GET);//(HttpsURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        if (authClient != null) {
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + authClient.getAccessToken());
        }
        return httpURLConnection.getInputStream();
    }

    public static InputStream get(String urlAddress, String json, OAuthClient authClient) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpsURLConnection httpURLConnection = getHttpsURLConnection(urlAddress, HTTPMethod.GET);//(HttpsURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
        out.write(json.getBytes());
        out.flush();
        out.close();
        if (authClient != null) {
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + authClient.getAccessToken());
        }
        return httpURLConnection.getInputStream();
    }
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
//        Response = client.execute(post);
//        entity = Response.getEntity();
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
//        Response = client.execute(post);
//        entity = Response.getEntity();
//        if (entity != null) {
//            String convertStreamToString = convertStreamToString(entity
//                    .getContent());
//            return convertStreamToString;
//        }
//        return null;
//    }

    public String post(String urlAddress, String rawData) throws Exception {
        return post(urlAddress, rawData, null);
    }

    public static String post(String urlAddress, String rawData, OAuthClient authClient)
            throws Exception {
//        URL url = new URL(urlAddress);
        HttpsURLConnection httpURLConnection = getHttpsURLConnection(urlAddress, HTTPMethod.POST);//(HttpsURLConnection) url.openConnection();
//        httpURLConnection.setRequestMethod("POST");
        if (authClient != null) {
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + authClient.getAccessToken());
        }
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
        out.write(rawData.getBytes());
        out.flush();
        out.close();
        int responseCode = httpURLConnection.getResponseCode();
        return convertStreamToString(httpURLConnection.getInputStream());

    }

    public String postAuthorizationHeader(String urlAddress, String rawData, String authorization)
            throws Exception {
//        URL url = new URL(urlAddress);
        HttpURLConnection httpURLConnection = getHttpsURLConnection(urlAddress, HTTPMethod.POST);//(HttpURLConnection) url.openConnection();
//        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        if (authorization != null) {
            httpURLConnection.setRequestProperty("Authorization", authorization);
        }
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
//        httpURLConnection.setUseCaches(false);

        DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
        out.write(rawData.getBytes());
        out.flush();
        out.close();
        int responseCode = httpURLConnection.getResponseCode();
        return convertStreamToString(httpURLConnection.getInputStream());

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

