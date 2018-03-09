package com.android.support;

import android.content.Context;

import com.android.emobilepos.R;
import com.android.emobilepos.service.SyncConfigServerService;

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

import oauthclient.HttpClient;

public class Post {

    private String entity = "";
    private Context context;
    private boolean isShortResponse = false;
    private HttpClient.HTTPMethod httpMethod = HttpClient.HTTPMethod.GET;
    private static InputStream keyStoreInputStream;
    private static KeyStore trustStore;
    private static TrustManagerFactory tmf;
    private static SSLContext sslContext;
    private static HttpsURLConnection urlConnection;
    private double apiVersion = (double) android.os.Build.VERSION.SDK_INT;
    private GenerateXML xml;
    private StringBuilder url = new StringBuilder();
    private String baseURL;
    MyPreferences preferences;

    public Post(Context context) {
        xml = new GenerateXML(context);
        this.context = context;
        preferences = new MyPreferences(context);
        baseURL = context.getString(R.string.sync_enablermobile_deviceasxmltrans);
    }

    public void initSSL() {
        System.setProperty("http.keepAlive", "false");
        if (apiVersion >= 14) // ICS+
            keyStoreInputStream = context.getResources().openRawResource(R.raw.azure);
        else // GingerBread
            keyStoreInputStream = context.getResources().openRawResource(R.raw.azure_godaddyroot);

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

    public String postData(int type, String varyingVariable) {
        url.setLength(0);
        String postLink = "";
        String response = "";
        switch (type) {
            case 0: {
                url.append(baseURL).append(xml.getAuth());
                isShortResponse = true;
                break;
            }
            case 1: {
                url.append(baseURL + (xml.getDeviceID()));
                isShortResponse = true;
                break;
            }
            case 2: {
                url.append(baseURL + (xml.getFirstAvailLic()));
                isShortResponse = true;
                break;
            }
            case 3: {
                url.append(baseURL + (xml.getEmployees()));
                isShortResponse = true;
                break;
            }
            case Global.S_GET_ASSIGN_EMPLOYEES: {
                url.append(baseURL + (xml.assignEmployees()));
                isShortResponse = true;
                break;
            }
            case 5: {
                url.append(baseURL + (xml.disableEmployee()));
                isShortResponse = true;
                break;
            }
            case 6: {
                url.append(baseURL + (xml.downloadPayments()));
                isShortResponse = true;
                break;
            }
            case 7: {
                url.append(baseURL + (xml.downloadAll(varyingVariable))); // varyingVariable
                // will
                // contain
                // the table
                // name
                isShortResponse = false;
                httpMethod = HttpClient.HTTPMethod.GET;
                break;
            }
            case Global.S_GET_XML_DINNER_TABLES: {

                url.append(baseURL + (xml.getDinnerTables()));
                isShortResponse = true;
                httpMethod = HttpClient.HTTPMethod.GET;
                break;
            }
            case Global.S_GET_XML_SALES_ASSOCIATE: {
                url.append(baseURL + (xml.getSalesAssociate()));
                isShortResponse = true;
                httpMethod = HttpClient.HTTPMethod.GET;
                break;
            }
            case Global.S_GET_XML_ORDERS: {
                postLink = context.getString(R.string.sync_enablermobile_getxmlorders);
                entity = xml.synchOrders(false);
                httpMethod = HttpClient.HTTPMethod.POST;

                break;
            }
            case Global.S_SUBMIT_ON_HOLD: {
                postLink = context.getString(R.string.sync_enabler_submitordersonhold);
                entity = xml.synchOrders(true);
                httpMethod = HttpClient.HTTPMethod.POST;

                break;
            }
            case Global.S_SUBMIT_PAYMENTS: {
                postLink = context.getString(R.string.sync_enabler_submitpayments);
                entity = xml.synchPayments();
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            }
            case Global.S_SUBMIT_PAYMENT_SIGNATURES: {
                postLink = context.getString(R.string.sync_enabler_submitpaymentsignatures);
                entity = xml.syncPaymentSignatures();
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            }
            case Global.S_SUBMIT_TIME_CLOCK: {
                postLink = context.getString(R.string.sync_enabler_submittimeclock);
                entity = xml.synchTimeClock();
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            }
            case Global.S_SUBMIT_VOID_TRANSACTION: {
                postLink = context.getString(R.string.sync_enabler_submitvoidtrans);
                entity = xml.syncVoidTransactions();
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            }
            case 11: {
                url.append(varyingVariable);
                httpMethod = HttpClient.HTTPMethod.GET;
                break;
            }
            case Global.S_SUBMIT_CUSTOMER:
                postLink = context.getString(R.string.sync_enabler_submitcustomer);
                entity = xml.synchNewCustomer();
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            case 13:
                postLink = context.getString(R.string.genius_token_url);//"https://epay.enablermobile.com/index.ashx";
                entity = varyingVariable;
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            case Global.S_SUBMIT_TUPYX:
                postLink = context.getString(R.string.epay_enablermobile_tupix);
                entity = varyingVariable;
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            case Global.S_SUBMIT_TEMPLATES:
                postLink = context.getString(R.string.sync_enabler_submittempletes);
                entity = xml.synchTemplates();
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            case Global.S_SUBMIT_CONSIGNMENT_TRANSACTION:
                postLink = context.getString(R.string.sync_enablersubmitconsignmenttransaction);
                entity = xml.synchConsignmentTransaction();
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            case Global.S_SUBMIT_CUSTOMER_INVENTORY:
                postLink = context.getString(R.string.sync_enabler_submitcustomerinventory);
                entity = xml.synchCustomerInventory();
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            case Global.S_SUBMIT_SHIFT:
                postLink = context.getString(R.string.sync_enabler_submitshiftperiods);
                entity = xml.synchShift();
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            case Global.S_SUBMIT_LOCATIONS_INVENTORY:
                postLink = context.getString(R.string.sync_enabler_submitlocationinventory);
                entity = xml.synchInventoryTransfer();
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            case Global.S_SUBMIT_WALLET_RECEIPTS:
                postLink = context.getString(R.string.sync_enabler_submitwalletreceipt);
                entity = xml.synchWalletReceipts();
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            case Global.S_ORDERS_ON_HOLD_DETAILS:
                url.append(baseURL + (xml.getOnHold(type, varyingVariable)));
                isShortResponse = false;
                httpMethod = HttpClient.HTTPMethod.GET;
                break;
            case Global.S_GET_TIME_CLOCK:
                url.append(baseURL + (xml.getTimeClock()));
                isShortResponse = true;
                httpMethod = HttpClient.HTTPMethod.GET;
                break;
            case Global.S_CHECK_STATUS_ON_HOLD:
            case Global.S_UPDATE_STATUS_ON_HOLD:
            case Global.S_CHECKOUT_ON_HOLD:
                url.append(baseURL + (xml.getOnHold(type, varyingVariable)));
                isShortResponse = true;
                httpMethod = HttpClient.HTTPMethod.GET;
                break;
            case Global.S_GET_SERVER_TIME:
                url.append(baseURL + (xml.getServerTime()));
                isShortResponse = true;
                httpMethod = HttpClient.HTTPMethod.GET;
                break;
            case Global.S_SUBMIT_TIP_ADJUSTMENT:
                postLink = context.getString(R.string.genius_token_url);//"https://epay.enablermobile.com/index.ashx";
                entity = varyingVariable;
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
            case Global.S_UPDATE_SYNC_TIME:
                url.append(baseURL + (xml.updateSyncTime(varyingVariable)));
                httpMethod = HttpClient.HTTPMethod.GET;
                isShortResponse = true;
                break;
            case Global.S_SUBMIT_WORKINGKEY_REQUEST:
                postLink = context.getString(R.string.genius_token_url);//"https://epay.enablermobile.com/index.ashx";
                entity = varyingVariable;
                httpMethod = HttpClient.HTTPMethod.POST;
                break;
        }
//Testing png download with the 2016 ssl cert
//		try {
//			this.getRequest(
//					new URL("https://bo.enablermobile.com/App_Themes/BONewDesign/images/login/EMobileLogo_login.png"));
//		} catch (MalformedURLException e1) {
//			e1.printStackTrace();
//		}
        if (httpMethod == HttpClient.HTTPMethod.GET) {
            try {
                if (type != 11)
                    if (type == Global.S_GET_XML_SALES_ASSOCIATE
                            || type == Global.S_GET_ASSIGN_EMPLOYEES
                            || type == Global.S_GET_XML_DINNER_TABLES) {
                        response = this.getRequest(new URL(url.toString()), true);
                    } else {
                        response = this.getRequest(new URL(url.toString()), false);
                    }
                else
                    response = getRequestUnsecure(new URI(url.toString()));
            } catch (MalformedURLException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
//            Log.d("Request XMKL: ", entity);
            response = this.postRequest(postLink, entity);
//            Log.d("Request XMKL: ", Response);
        }

        return response;
    }

    private String getRequest(URL url, boolean isJsonContentType) {

        HttpsURLConnection urlConnection;
        try {
            if (sslContext == null) {
//                initSSL();
            }
            HttpsURLConnection.setFollowRedirects(false);
            urlConnection = (HttpsURLConnection) url.openConnection();
//            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
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
                        context.getApplicationContext().getFilesDir().getAbsolutePath() + "/temp.xml");
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
//                    initSSL();
                }
                urlConnection = (HttpsURLConnection) HttpClient.getHttpURLConnection(url, HttpClient.HTTPMethod.POST);
//                urlConnection = (HttpsURLConnection) url.openConnection();
//                urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
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
                e.printStackTrace();
                return Global.NOT_VALID_URL;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return Global.NOT_VALID_URL;
        }
    }

}
