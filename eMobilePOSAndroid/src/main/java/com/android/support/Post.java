package com.android.support;

import android.app.Activity;
import android.util.Log;

import com.android.emobilepos.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class Post {

    private String entity = new String();
    private Activity activity;
    private boolean isShortResponse = false;
    private boolean isPost = false;
    static InputStream keyStoreInputStream;
    static KeyStore trustStore;
    static TrustManagerFactory tmf;
    static SSLContext sslContext;
    static HttpsURLConnection urlConnection;
    double apiVersion = Double.valueOf(android.os.Build.VERSION.SDK_INT);

    public void initSSL() {
        System.setProperty("http.keepAlive", "false");
        if (apiVersion >= 14) // ICS+
            keyStoreInputStream = activity.getResources().openRawResource(R.raw.azure);
        else // GingerBread
            keyStoreInputStream = activity.getResources().openRawResource(R.raw.azure_godaddyroot);

        try {
            trustStore = KeyStore.getInstance("BKS");
            trustStore.load(keyStoreInputStream, "mysecret".toCharArray());
            tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(trustStore);
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String postData(int type, Activity activity, String varyingVariable) {
        GenerateXML xml = new GenerateXML(activity);
        this.activity = activity;
        StringBuilder baseURL = new StringBuilder();
        baseURL.append(activity.getString(R.string.sync_enablermobile_deviceasxmltrans));

        StringBuilder url = new StringBuilder();

        String postLink = "";
        String response = "";

        switch (type) {
            case 0: {
                url = baseURL.append(xml.getAuth());
                isShortResponse = true;
                break;
            }
            case 1: {
                url = baseURL.append(xml.getDeviceID());
                isShortResponse = true;
                break;
            }
            case 2: {
                url = baseURL.append(xml.getFirstAvailLic());
                isShortResponse = true;
                break;
            }
            case 3: {
                url = baseURL.append(xml.getEmployees());
                isShortResponse = true;
                break;
            }
            case 4: {
                url = baseURL.append(xml.assignEmployees());
                isShortResponse = true;
                break;
            }
            case 5: {
                url = baseURL.append(xml.disableEmployee());
                isShortResponse = true;
                break;
            }
            case 6: {
                url = baseURL.append(xml.downloadPayments());
                isShortResponse = true;
                break;
            }
            case 7: {
                url = baseURL.append(xml.downloadAll(varyingVariable)); // varyingVariable
                // will
                // contain
                // the table
                // name
                isShortResponse = false;
                isPost = false;
                break;
            }
            case Global.S_GET_XML_DINNER_TABLES: {

                url = baseURL.append(xml.getDinnerTables());
                isShortResponse = true;
                isPost = false;
                break;
            }
            case Global.S_GET_XML_SALES_ASSOCIATE: {
                url = baseURL.append(xml.getSalesAssociate());
                isShortResponse = true;
                isPost = false;
                break;
            }
            case Global.S_GET_XML_ORDERS: {
                postLink = activity.getString(R.string.sync_enablermobile_getxmlorders);
                entity = xml.synchOrders(false);
                isPost = true;

                break;
            }
            case Global.S_SUBMIT_ON_HOLD: {
                postLink = activity.getString(R.string.sync_enabler_submitordersonhold);
                entity = xml.synchOrders(true);
                isPost = true;

                break;
            }
            case Global.S_SUBMIT_PAYMENTS: {
                postLink = activity.getString(R.string.sync_enabler_submitpayments);
                entity = xml.synchPayments();
                isPost = true;
                break;
            }
            case Global.S_SUBMIT_TIME_CLOCK: {
                postLink = activity.getString(R.string.sync_enabler_submittimeclock);
                entity = xml.synchTimeClock();
                isPost = true;
                break;
            }
            case Global.S_SUBMIT_VOID_TRANSACTION: {
                postLink = activity.getString(R.string.sync_enabler_submitvoidtrans);
                entity = xml.syncVoidTransactions();
                isPost = true;

                break;
            }
            case 11: {
                url.append(varyingVariable);
                isPost = false;
                break;
            }
            case Global.S_SUBMIT_CUSTOMER:
                postLink = activity.getString(R.string.sync_enabler_submitcustomer);
                entity = xml.synchNewCustomer();
                isPost = true;
                break;
            case 13:
                postLink = activity.getString(R.string.genius_token_url);//"https://epay.enablermobile.com/index.ashx";
                entity = varyingVariable;
                isPost = true;
                break;
            case Global.S_SUBMIT_TUPYX:
                postLink = activity.getString(R.string.epay_enablermobile_tupix);
                entity = varyingVariable;
                isPost = true;
                break;
            case Global.S_SUBMIT_TEMPLATES:
                postLink = activity.getString(R.string.sync_enabler_submittempletes);
                entity = xml.synchTemplates();
                isPost = true;
                break;
            case Global.S_SUBMIT_CONSIGNMENT_TRANSACTION:
                postLink = activity.getString(R.string.sync_enablersubmitconsignmenttransaction);
                entity = xml.synchConsignmentTransaction();
                isPost = true;
                break;
            case Global.S_SUBMIT_CUSTOMER_INVENTORY:
                postLink = activity.getString(R.string.sync_enabler_submitcustomerinventory);
                entity = xml.synchCustomerInventory();
                isPost = true;
                break;
            case Global.S_SUBMIT_SHIFT:
                postLink = activity.getString(R.string.sync_enabler_submitshiftperiods);
                entity = xml.synchShift();
                isPost = true;
                break;
            case Global.S_SUBMIT_LOCATIONS_INVENTORY:
                postLink = activity.getString(R.string.sync_enabler_submitlocationinventory);
                entity = xml.synchInventoryTransfer();
                isPost = true;
                break;
            case Global.S_SUBMIT_WALLET_RECEIPTS:
                postLink = activity.getString(R.string.sync_enabler_submitwalletreceipt);
                entity = xml.synchWalletReceipts();
                isPost = true;
                break;
            case Global.S_ORDERS_ON_HOLD_DETAILS:
                url = baseURL.append(xml.getOnHold(type, varyingVariable));
                isShortResponse = false;
                isPost = false;
                break;
            case Global.S_GET_TIME_CLOCK:
                url = baseURL.append(xml.getTimeClock());
                isShortResponse = true;
                isPost = false;
                break;
            case Global.S_CHECK_STATUS_ON_HOLD:
            case Global.S_UPDATE_STATUS_ON_HOLD:
            case Global.S_CHECKOUT_ON_HOLD:
                url = baseURL.append(xml.getOnHold(type, varyingVariable));
                isShortResponse = true;
                isPost = false;
                break;
            case Global.S_GET_SERVER_TIME:
                url = baseURL.append(xml.getServerTime());
                isShortResponse = true;
                isPost = false;
                break;
            case Global.S_SUBMIT_TIP_ADJUSTMENT:
                postLink = activity.getString(R.string.sync_enabler_submitpayments);
                entity = varyingVariable;
                isPost = true;
                break;
            case Global.S_UPDATE_SYNC_TIME:
                url = baseURL.append(xml.updateSyncTime(varyingVariable));
                isPost = false;
                isShortResponse = true;
                break;
        }
//Testing png download with the 2016 ssl cert
//		try {
//			this.getRequest(
//					new URL("https://bo.enablermobile.com/App_Themes/BONewDesign/images/login/EMobileLogo_login.png"));
//		} catch (MalformedURLException e1) {
//			e1.printStackTrace();
//		}
        if (!isPost) {
            try {
                if (type != 11)
                    if (type == Global.S_GET_XML_SALES_ASSOCIATE) {
                        response = this.getRequest(new URL(url.toString()), true);
                    } else {
                        response = this.getRequest(new URL(url.toString()), false);
                    }
                else
                    response = getRequestUnsecure(new URI(url.toString()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
//            Log.d("Request XMKL: ", entity);
            response = this.postRequest(postLink, entity);
//            Log.d("Request XMKL: ", response);
        }

        return response;
    }

    private String getRequest(URL url, boolean isJsonContentType) {

        HttpsURLConnection urlConnection;
        try {
            if (sslContext == null) {
                initSSL();
            }
            HttpsURLConnection.setFollowRedirects(false);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            urlConnection.setRequestMethod("GET");
            if (isJsonContentType) {
                urlConnection.setRequestProperty("Content-Type", "application/json");
            }
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setUseCaches(false);
            urlConnection.setConnectTimeout(50 * 1000);
            urlConnection.setReadTimeout(50 * 1000);

            if (isShortResponse) {
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String textLine;
                while ((textLine = r.readLine()) != null) {
                    sb.append(textLine);
                }
                return sb.toString();
            } else {
                File tempFile = new File(
                        activity.getApplicationContext().getFilesDir().getAbsolutePath() + "/temp.xml");
//				File tempFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.png");
                if (!tempFile.exists())
                    tempFile.createNewFile();
                OutputStream outStream = new FileOutputStream(tempFile);
                int read;
                byte[] bytes = new byte[1024];

                InputStream inStream = urlConnection.getInputStream();
                while ((read = inStream.read(bytes)) != -1) {
                    outStream.write(bytes, 0, read);
                }
                inStream.close();
                outStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getRequestUnsecure(URI uri) {

        StringBuilder response = new StringBuilder();
        try {
            HttpGet get = new HttpGet();
            get.setURI(uri);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(get);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {

                HttpEntity messageEntity = httpResponse.getEntity();
                InputStream is = messageEntity.getContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    private String postRequest(String Url, String data) {

        try {
            URL url = new URL(Url);

            try {
                if (sslContext == null) {
                    initSSL();
                }
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                urlConnection.setRequestMethod("POST");
                HttpURLConnection.setFollowRedirects(true);

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Connection", "close");
                urlConnection.setUseCaches(false);
                urlConnection.setConnectTimeout(60 * 1000);
                urlConnection.setReadTimeout(60 * 1000);
                urlConnection.setUseCaches(false);

                urlConnection.setRequestProperty("Content-Type", "text/xml");

                DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
                out.write(data.getBytes());
                out.flush();
                out.close();

                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String textLine;

                while ((textLine = r.readLine()) != null) {
                    sb.append(textLine);
                }

                return sb.toString();
            } catch (Exception e) {
                return Global.NOT_VALID_URL;
            }

        } catch (IOException e) {
            return Global.NOT_VALID_URL;
        }
    }

}
