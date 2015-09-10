package com.android.support;

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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.emobilepos.app.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.os.Debug;
import android.os.Environment;

public class Post {

	private String entity = new String();
	private Activity activity;
	private boolean isShortResponse = false;
	private boolean isPost = false;

	public String postData(int type, Activity activity, String varyingVariable) {
		GenerateXML xml = new GenerateXML(activity);
		this.activity = activity;
		StringBuilder baseURL = new StringBuilder();
		baseURL.append("https://sync.enablermobile.com/deviceASXMLTrans/");

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

		case Global.S_GET_XML_ORDERS: {
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/getXMLOrders.aspx";
			entity = xml.synchOrders(false).toString();
			isPost = true;

			break;
		}
		case Global.S_SUBMIT_ON_HOLD: {
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/submitOrdersOnHold.ashx";
			entity = xml.synchOrders(true).toString();
			isPost = true;

			break;
		}
		case Global.S_SUBMIT_PAYMENTS: {
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/submitPayments.aspx";
			entity = xml.synchPayments().toString();
			isPost = true;
			break;
		}
		case Global.S_SUBMIT_TIME_CLOCK: {
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/submitTimeClock.ashx";
			entity = xml.synchTimeClock();
			isPost = true;
			break;
		}
		case Global.S_SUBMIT_VOID_TRANSACTION: {
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/submitVoidTrans.aspx";
			entity = xml.syncVoidTransactions();
			isPost = true;

			break;
		}
		case 11: {
			url.append(varyingVariable).toString();
			isPost = false;
			break;
		}
		case Global.S_SUBMIT_CUSTOMER:
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/submitCustomer.aspx";
			entity = xml.synchNewCustomer();
			isPost = true;
			break;
		case 13:
			postLink = "https://epay.enablermobile.com/index.ashx";
			entity = varyingVariable;
			isPost = true;
			break;
		case Global.S_SUBMIT_TUPYX:
			postLink = "https://epay.enablermobile.com/tupyx.ashx";
			entity = varyingVariable;
			isPost = true;
			break;
		case Global.S_SUBMIT_TEMPLATES:
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/subitTemplates.aspx";
			entity = xml.synchTemplates();
			isPost = true;
			break;
		case Global.S_SUBMIT_CONSIGNMENT_TRANSACTION:
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/submitConsignmentTransaction.aspx";
			entity = xml.synchConsignmentTransaction();
			isPost = true;
			break;
		case Global.S_SUBMIT_CUSTOMER_INVENTORY:
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/submitCustomerInventory.aspx";
			entity = xml.synchCustomerInventory();
			isPost = true;
			break;
		case Global.S_SUBMIT_SHIFT:
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/submitShiftPeriods.aspx";
			entity = xml.synchShift();
			isPost = true;
			break;
		case Global.S_SUBMIT_LOCATIONS_INVENTORY:
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/submitLocationInventory.aspx";
			entity = xml.synchInventoryTransfer();
			isPost = true;
			break;
		case Global.S_SUBMIT_WALLET_RECEIPTS:
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/submitwalletReceipt.ashx";
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
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		if (!isPost) {
			try {
				if (type != 11)
					response = this.getRequest(new URL(url.toString()));
				else
					response = getRequestUnsecure(new URI(url.toString()));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			response = this.postRequest(postLink, entity.toString());
		}

		return response;
	}

	private String getRequest(URL url) {
		System.setProperty("http.keepAlive", "false");
		InputStream keyStoreInputStream;
		KeyStore trustStore;
		TrustManagerFactory tmf;
		SSLContext sslContext;
		HttpsURLConnection urlConnection;
		try {
			double apiVersion = Double.valueOf(android.os.Build.VERSION.SDK_INT);

			if (apiVersion >= 14) // ICS+
				keyStoreInputStream = activity.getResources().openRawResource(R.raw.azure);
			else // GingerBread
				keyStoreInputStream = activity.getResources().openRawResource(R.raw.azure_godaddyroot);

			trustStore = KeyStore.getInstance("BKS");
			trustStore.load(keyStoreInputStream, "mysecret".toCharArray());
			tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(trustStore);
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);
			HttpsURLConnection.setFollowRedirects(false);
			urlConnection = (HttpsURLConnection) url.openConnection();
			urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
			// urlConnection.setRequestMethod("GET");
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
				int read = 0;
				byte[] bytes = new byte[1024];

				InputStream inStream = urlConnection.getInputStream();
				while ((read = inStream.read(bytes)) != -1) {
					outStream.write(bytes, 0, read);
				}
				inStream.close();
				outStream.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
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
		}
		return response.toString();
	}

	private String postRequest(String Url, String data) {
		System.setProperty("http.keepAlive", "false");
		InputStream keyStoreInputStream;
		KeyStore trustStore;
		TrustManagerFactory tmf;
		SSLContext sslContext;
		HttpsURLConnection urlConnection;
		try {
			URL url = new URL(Url);
			double apiVersion = Double.valueOf(android.os.Build.VERSION.SDK_INT);
			if (apiVersion >= 14) // ICS+
				keyStoreInputStream = activity.getResources().openRawResource(R.raw.azure);
			else // GingerBread
				keyStoreInputStream = activity.getResources().openRawResource(R.raw.azure_godaddyroot);

			trustStore = KeyStore.getInstance("BKS");
			trustStore.load(keyStoreInputStream, "mysecret".toCharArray());
			tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(trustStore);
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);

			try {
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
			// TODO Auto-generated catch block
			return Global.NOT_VALID_URL;
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	private void handleGoogleAnalytic(String stack) {
		Tracker tracker = EasyTracker.getInstance(activity);
		tracker.send(MapBuilder.createException(stack, false).build());
	}
}
