package com.android.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;

import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.android.emobilepos.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;


import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
 


public class MyHttpClient extends DefaultHttpClient{
	
	private Context context;
	
	
	public StringEntity entity = null;
	private Activity activity;
	
	
	public MyHttpClient(Context context)
	{
		this.context = context;
		
	}
	
	
	@Override
	protected ClientConnectionManager createClientConnectionManager()
	{
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http",PlainSocketFactory.getSocketFactory(),80));
		registry.register(new Scheme("https", newSSLSocketFactory(), 443));
		
		return new SingleClientConnManager(getParams(),registry);
	}
	
	
	private SSLSocketFactory newSSLSocketFactory()
	{
		try{
		KeyStore trusted = KeyStore.getInstance("BKS");
		
		InputStream in = context.getResources().openRawResource(R.raw.test2);
		try
		{
			trusted.load(in,"mysecret".toCharArray());
		}finally
		{
			in.close();
		}
		SSLSocketFactory ssf = new SSLSocketFactory(trusted);
		ssf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
		
		return ssf;
		}
		
		catch(Exception e)
		{
			throw new AssertionError(e);
		}
		
	}
	
	private HttpClient sslClient()
	{
		try
		{
			X509TrustManager tm = new X509TrustManager()
			{

				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					// TODO Auto-generated method stub
					
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					// TODO Auto-generated method stub
					return null;
				}
				
			};
			HttpClient client = new DefaultHttpClient();
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new TrustManager[]{tm},null);
			SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = client.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https",ssf,443));
			return new DefaultHttpClient(ccm,client.getParams());
		}catch(Exception ex)
		{
			return null;
		}
	}
	
	
	private HttpClient createClient(HttpClient httpclient)
	{
	
			//KeyStore trusted = KeyStore.getInstance("BKS");
			
			//InputStream in = context.getResources().openRawResource(R.raw.google_test);
			
				//trusted.load(in,"mysecret".toCharArray());
				
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("http",PlainSocketFactory.getSocketFactory(),80));
				//SSLSocketFactory sslSocketFactory = new SSLSocketFactory(trusted);
				schemeRegistry.register(new Scheme("https",createAdditionalCertsSSLSocketFactory(),443));
				HttpParams httpParams = new BasicHttpParams();
				
				int timeoutConnection = 15*1000;
				HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
				int timeoutSocket = 15*1000;
				HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
				
				ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams,schemeRegistry);
				httpclient= new DefaultHttpClient(cm,httpParams);
				return httpclient;
				
	}
	
	
	
	protected org.apache.http.conn.ssl.SSLSocketFactory createAdditionalCertsSSLSocketFactory() {
	    try {
	        final KeyStore ks = KeyStore.getInstance("BKS");

	        // the bks file we generated above
	        final InputStream in = context.getResources().openRawResource( R.raw.test2);  
	        try {
	            // don't forget to put the password used above in strings.xml/mystore_password
	            ks.load(in, "mysecret".toCharArray());
	        } finally {
	            in.close();
	        }
	        org.apache.http.conn.ssl.SSLSocketFactory sslSocket = new AdditionalKeyStoresSSLSocketFactory(ks);
	        sslSocket.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
	        return sslSocket;

	    } catch( Exception e ) {
	        throw new RuntimeException(e);
	    }
	}
	
	
	
	
	public class AdditionalKeyStoresSSLSocketFactory extends SSLSocketFactory {
	    protected SSLContext sslContext = SSLContext.getInstance("TLS");
	    
	    public AdditionalKeyStoresSSLSocketFactory(KeyStore keyStore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	        super(null, null, null, null, null, null);
	        
	        sslContext.init(null, new TrustManager[]{new AdditionalKeyStoresTrustManager(keyStore)}, null);
	    }

	    @Override
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	    }

	    @Override
	    public Socket createSocket() throws IOException {
	        return sslContext.getSocketFactory().createSocket();
	        
	    }



	    /**
	     * Based on http://download.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#X509TrustManager
	     */
	    public  class AdditionalKeyStoresTrustManager implements X509TrustManager {

	        protected ArrayList<X509TrustManager> x509TrustManagers = new ArrayList<X509TrustManager>();


	        protected AdditionalKeyStoresTrustManager(KeyStore... additionalkeyStores) {
	            final ArrayList<TrustManagerFactory> factories = new ArrayList<TrustManagerFactory>();

	            try {
	                // The default Trustmanager with default keystore
	               
	            	final TrustManagerFactory original = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	                original.init((KeyStore) null);
	                factories.add(original);
	            	

	                for( KeyStore keyStore : additionalkeyStores ) {
	                    final TrustManagerFactory additionalCerts = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	                    additionalCerts.init(keyStore);
	                    factories.add(additionalCerts);
	                }

	            } catch (Exception e) {
	                throw new RuntimeException(e);
	            }



	            /*
	             * Iterate over the returned trustmanagers, and hold on
	             * to any that are X509TrustManagers
	             */
	            for (TrustManagerFactory tmf : factories)
	                for( TrustManager tm : tmf.getTrustManagers() )
	                    if (tm instanceof X509TrustManager)
	                        x509TrustManagers.add( (X509TrustManager)tm );


	            if( x509TrustManagers.size()==0 )
	                throw new RuntimeException("Couldn't find any X509TrustManagers");

	        }

	        /*
	         * Delegate to the default trust manager.
	         */
	        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            final X509TrustManager defaultX509TrustManager = x509TrustManagers.get(0);
	            defaultX509TrustManager.checkClientTrusted(chain, authType);
	        }

	        /*
	         * Loop over the trustmanagers until we find one that accepts our server
	         */
	        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            for( X509TrustManager tm : x509TrustManagers ) {
	                try {
	                    tm.checkServerTrusted(chain,authType);
	                    return;
	                } catch( CertificateException e ) {
	                	String t = "";
	                    // ignore
	                }
	            }
	            throw new CertificateException();
	        }

	        public X509Certificate[] getAcceptedIssuers() {
	            final ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();
	            for( X509TrustManager tm : x509TrustManagers )
	                list.addAll(Arrays.asList(tm.getAcceptedIssuers()));
	            return list.toArray(new X509Certificate[list.size()]);
	        }
	    }

	}
	
	
	
	
	
	private String makeRequest(URL url) {
		//AssetManager assetManager = context.getAssets();
		InputStream keyStoreInputStream;
		KeyStore trustStore;
		TrustManagerFactory tmf;
		SSLContext sslContext;
		HttpsURLConnection urlConnection;
		try {
			keyStoreInputStream = activity.getResources().openRawResource(R.raw.test2);//= assetManager.open("test2.bks");
			trustStore = KeyStore.getInstance("BKS");
			trustStore.load(keyStoreInputStream, "mysecret".toCharArray());
			tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(trustStore);
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);
			urlConnection = (HttpsURLConnection) url.openConnection();
			urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
			BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String textLine;
			while((textLine=r.readLine())!=null)
			{
				sb.append(textLine);
			}
			return sb.toString();
			//return urlConnection.getInputStream();

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
		return null;

	}
	
	
	
	private String testMakeRequest(URL url)
	{
		InputStream keyStoreInputStream;
		KeyStore trustStore;
		TrustManagerFactory tmf;
		SSLContext sslContext;
		HttpsURLConnection urlConnection;
		try {
			keyStoreInputStream = activity.getResources().openRawResource(R.raw.azureinterroot);//= assetManager.open("test2.bks");
			trustStore = KeyStore.getInstance("BKS");
			trustStore.load(keyStoreInputStream, "mysecret".toCharArray());
			
			tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			//tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(trustStore);
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);
			
			
			urlConnection = (HttpsURLConnection) url.openConnection();
			urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
			BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String textLine;
			while((textLine=r.readLine())!=null)
			{
				sb.append(textLine);
			}
			return sb.toString();
			//return urlConnection.getInputStream();

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
		return null;

	}
	
	
	
	
	public String postData(int type, Activity activity, String varyingVariable) {
		
		
		/*HttpParams httpParams = new BasicHttpParams();
		int timeoutConnection = 20*1000;
		HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
		int timeoutSocket = 20*1000;
		HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);*/

		
		
		/*SSLContext sslContext = null;
		try {
			KeyStore trustStore = KeyStore.getInstance("BKS");
			InputStream in = context.getResources().openRawResource(R.raw.test2);
			trustStore.load(in,"mysecret".toCharArray());
			in.close();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
			tmf.init(trustStore);
			sslContext= SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);
			
			
			
			
		} catch (KeyStoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//DefaultHttpClient httpclient = new MyHttpClient(this.context);		//Enable this for SSL pinning (currently working for Jelly Bean)
		//HttpClient httpclient = new DefaultHttpClient(httpParams);
		HttpClient httpclient = null;
		httpclient = createClient(httpclient);
		
		httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "android");
		
		GenerateXML xml = new GenerateXML(activity);
		this.activity = activity;
		
		StringBuilder baseURL = new StringBuilder();
		baseURL.append("https://sync.enablermobile.com/deviceASXMLTrans/");
		HttpGet httppost = new HttpGet();
		StringBuilder url = new StringBuilder();

		String postLink = "";
		String response = "";

		switch (type) {
		case 0: {
			url = baseURL.append(xml.getAuth());

			break;
		}
		case 1: {
			url = baseURL.append(xml.getDeviceID());
			break;
		}
		case 2: {
			url = baseURL.append(xml.getFirstAvailLic());
			break;
		}
		case 3: {
			url = baseURL.append(xml.getEmployees());
			break;
		}
		case 4: {
			url = baseURL.append(xml.assignEmployees());
			break;
		}
		case 5: {
			url = baseURL.append(xml.disableEmployee());
			break;
		}
		case 6: {
			url = baseURL.append(xml.downloadPayments());
			break;
		}
		case 7: {
			url = baseURL.append(xml.downloadAll(varyingVariable));			//varyingVariable will contain the table name
			break;
		}

		case 8: {
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/getXMLOrders.aspx";
			try {
				entity = new StringEntity(xml.synchOrders().toString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at case 8)]");
				handleGoogleAnalytic (sb.toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at case 8)]");
				handleGoogleAnalytic (sb.toString());
			}
			break;
		}
		case 9: {
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/submitPayments.aspx";
			try {
				entity = new StringEntity(xml.synchPayments().toString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at case 9)]");
				handleGoogleAnalytic (sb.toString());
			}
			break;
		}
		case 10:{
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/submitVoidTrans.aspx";
			try{
				entity = new StringEntity (xml.syncVoidTransactions(),"UTF-8");
			}catch(UnsupportedEncodingException e)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at case 10)]");
				handleGoogleAnalytic (sb.toString());
			}
			break;
		}
		case 11:
		{
			url.append(varyingVariable);		
			break;
		}
		case 12:
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/submitCustomer.aspx";
			try
			{
				entity = new StringEntity(xml.synchNewCustomer(),"UTF-8");
			}
			catch(UnsupportedEncodingException e)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at case 12)]");
				handleGoogleAnalytic (sb.toString());
			}
			break;
		case 13:
			postLink = "https://epay.enablermobile.com/index.ashx";
			//postLink = "http://62504d30c93c4a6087193a87f34daa08.cloudapp.net/index.ashx";
			try
			{
				entity = new StringEntity(varyingVariable,"UTF-8");
			}
			catch(UnsupportedEncodingException e)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at case 13)]");
				handleGoogleAnalytic (sb.toString());
			}
			break;
		case 14:
			postLink = "https://sync.enablermobile.com/deviceASXMLTrans/subitTemplates.aspx";
			try
			{
				entity = new StringEntity(xml.synchTemplates(),"UTF-8");
			}
			catch(UnsupportedEncodingException e)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at case 12)]");
				handleGoogleAnalytic (sb.toString());
			}
			break;
		}
		
		
		if (type != 8 && type != 9 && type!=10&&type!=11&&type!=12&&type!=13&&type!=14) {

			ResponseHandler<String> handler = new BasicResponseHandler();

			try {
				
				/*URL request = new URL(url.toString());
				HttpsURLConnection urlConnection = (HttpsURLConnection)request.openConnection();
				urlConnection.setHostnameVerifier(new StrictHostnameVerifier());
				urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
				urlConnection.setConnectTimeout(20000);
				InputStream in = urlConnection.getInputStream();
				response = urlConnection.getResponseMessage();
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				StringBuilder sb = new StringBuilder();
				String input;
				while((input=br.readLine())!=null)
					sb.append(input);
				response = sb.toString();
				response = "";*/
				
				
				
				/*String test = testMakeRequest(new URL("https://www.godaddy.com/"));
				test = "";*/
				
				httppost.setURI(new URI(url.toString()));
				//httppost.setURI(new URI("https://www.google.com"));
				response = httpclient.execute(httppost, handler);
				

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at 1st if)]");
				handleGoogleAnalytic (sb.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at 1st if)]");
				handleGoogleAnalytic (sb.toString());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at 1st if)]");
				handleGoogleAnalytic (sb.toString());
			}
		}
		else if(type==11)
		{
			ResponseHandler<String> handler = new BasicResponseHandler();

			try {
				/*URL request = new URL(url.toString());
				HttpsURLConnection urlConnection = (HttpsURLConnection)request.openConnection();
				urlConnection.setHostnameVerifier(new StrictHostnameVerifier());
				urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
				urlConnection.setConnectTimeout(20000);
				InputStream in = urlConnection.getInputStream();
				response = in.toString();*/
				
				httppost.setURI(new URI(url.toString()));
				response = httpclient.execute(httppost, handler);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at if==11)]");
				handleGoogleAnalytic (sb.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at if==11)]");
				handleGoogleAnalytic (sb.toString());
				if(e.getMessage().contains("timed out"))
					response = Global.TIME_OUT;
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at if==11)]");
				handleGoogleAnalytic (sb.toString());
			}
		}
		else {
			HttpClient httpclient2 = new DefaultHttpClient();
			// HttpPost httppost2 = new
			// HttpPost("https://sync.enablermobile.com/deviceASXMLTrans/getXMLOrders.aspx");
			HttpPost httppost2 = new HttpPost(postLink);
			httppost2.setEntity(entity);
			httppost2.addHeader("Accept", "application/xml");
			httppost2.addHeader("Content-Type", "application/xml");

			HttpResponse resp;
			try {
				resp = httpclient2.execute(httppost2);
				HttpEntity respEntity = resp.getEntity();
				response = EntityUtils.toString(respEntity);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at else)]");
				handleGoogleAnalytic (sb.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				StringBuilder sb = new StringBuilder();
				sb.append(e.getMessage()).append(" [com.android.support.Post (at else)]");
				handleGoogleAnalytic (sb.toString());
			}
		}

		return response;
	}
	
	
	private void handleGoogleAnalytic (String stack)
	{
		EasyTracker.getInstance().setContext(activity);
		Tracker myTracker = EasyTracker.getTracker(); // Get a reference to tracker.
		myTracker.sendException(stack, false); // false indicates non-fatal exception.
	}

}
