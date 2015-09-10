package com.android.emobilepos;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXGetGeniusHandler;
import com.android.saxhandler.SAXProcessGeniusHandler;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Payment;
import com.android.support.Post;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ProcessGeniusActivity extends Activity {
	private String inv_id, paymethod_id;
	private Activity activity;
	private Bundle extras;

	private EditText invJobView, amountView;
	private ProgressDialog myProgressDialog;
	private Payment payment;
	private boolean isFromMainMenu;
	private String geniusIP;
	private Global global;
	private MyPreferences myPref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.process_genius_layout);
		activity = this;
		global = (Global)this.getApplication();
		extras = this.getIntent().getExtras();
		Button processPayment = (Button) findViewById(R.id.processGeniusButton);
		invJobView = (EditText) findViewById(R.id.geniusJobIDView);
		amountView = (EditText) findViewById(R.id.geniusAmountView);
		
		myPref = new MyPreferences(activity);
		geniusIP = myPref.getGeniusIP();
		
		isFromMainMenu = extras.getBoolean("isFromMainMenu");
		if(!isFromMainMenu)
		{
			invJobView.setEnabled(false);
			amountView.setEnabled(false);
		}
		

		paymethod_id = extras.getString("paymethod_id");

		if (extras.getBoolean("histinvoices"))
			inv_id = extras.getString("inv_id");
		else
			inv_id = extras.getString("job_id");

		invJobView.setText(inv_id);
		amountView.setText(extras.getString("amount"));

		processPayment.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(activity, "Processing Genius", Toast.LENGTH_LONG).show();
				processPayment();
			}
		});

	}

	private void processPayment() {
		payment = new Payment(activity);

		if (!this.extras.getBoolean("histinvoices"))
			payment.getSetData("job_id", false, invJobView.getText().toString());
		else
			payment.getSetData("inv_id", false, invJobView.getText().toString());
		payment.getSetData("pay_id", false, extras.getString("pay_id"));
		payment.getSetData("paymethod_id", false, paymethod_id);
		payment.getSetData("pay_expmonth", false, "0");// dummy
		payment.getSetData("pay_expyear", false, "2000");// dummy
		payment.getSetData("pay_tip", false, "0.00");
		payment.getSetData("pay_dueamount", false, amountView.getText().toString());
		payment.getSetData("pay_amount", false, amountView.getText().toString());
		payment.getSetData("originalTotalAmount", false, "0");
		

		EMSPayGate_Default payGate = new EMSPayGate_Default(activity, payment);
		String generatedURL = new String();
		generatedURL= payGate.paymentWithAction("ChargeGeniusAction", false, "", null);
		//generatedURL = payGate.defaultPaymentWithAction("ChargeGeniusAction", "0");

		new processLivePaymentAsync().execute(generatedURL);
	}

	private class processLivePaymentAsync extends AsyncTask<String, String, String> {

		private List<String[]> returnedPost;
		private List<String[]> returnedGenius;
		private boolean boProcessed = false;
		private boolean geniusConnected = false;
		private String temp = "",temp2="";

		@Override
		protected void onPreExecute() {
			myProgressDialog = new ProgressDialog(activity);
			myProgressDialog.setMessage("Processing Payment...");
			myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			myProgressDialog.setCancelable(false);
			myProgressDialog.show();

		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			if(pingGeniusDevice())
			{
				geniusConnected = true;
			Post post = new Post();
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXProcessGeniusHandler handler = new SAXProcessGeniusHandler(activity);

			try {
				String xml = post.postData(13, activity, params[0]);
				temp = xml.toString();
				InputSource inSource = new InputSource(new StringReader(xml));

				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				xr.setContentHandler(handler);
				xr.parse(inSource);
				returnedPost = handler.getEmpData();

				if (returnedPost != null && getData("statusCode", 0, 0).equals("APPROVED")) {

					boProcessed = true;
					MyPreferences myPref = new MyPreferences(activity);
					StringBuilder sb = new StringBuilder();
					sb.append("http://").append(myPref.getGeniusIP()).append(":8080/pos?TransportKey=").append(getData("TransportKey", 0, 0));
					sb.append("&Format=XML");
					xml = post.postData(11, activity, sb.toString());
					temp2 = xml;
					inSource = new InputSource(new StringReader(xml));
					SAXGetGeniusHandler getGenius = new SAXGetGeniusHandler(activity);
					sp = spf.newSAXParser();
					xr = sp.getXMLReader();
					xr.setContentHandler(getGenius);
					xr.parse(inSource);

					returnedGenius = getGenius.getEmpData();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Tracker tracker = EasyTracker.getInstance(activity);
				tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
				}
			
			}
			return null;
		}

		@Override
		protected void onPostExecute(String unused) {
			myProgressDialog.dismiss();
			
			if(!geniusConnected)
			{
				Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.failed_genius_connectivity));
			}
			else if(!boProcessed)
			{
				Global.showPrompt(activity, R.string.dlog_title_error, temp);
			}
			else if (returnedGenius != null && returnedGenius.size() > 0 && getData("Status", 0, 1).equals("APPROVED")) {
				payment.getSetData("pay_transid", false, getData("Token", 0, 1));
				payment.getSetData("authcode", false, getData("AuthorizationCode", 0, 1));
				payment.getSetData("ccnum_last4", false, getData("AccountNumber", 0, 1).replace("*", "").trim());
				payment.getSetData("pay_name", false, getData("Cardholder", 0, 1));
				payment.getSetData("pay_date", false, getData("TransactionDate", 0, 1));
				String signa = getData("SignatureData", 0, 1);
				parseSignature(signa);
				String paymethodType = payMethodDictionary(getData("PaymentType",0,1));
				PayMethodsHandler payMethodsHandler = new PayMethodsHandler(activity);
				payment.getSetData("paymethod_id", false,payMethodsHandler.getPayMethodID(paymethodType) );
				
				PaymentsHandler payHandler = new PaymentsHandler(activity);
				payHandler.insert(payment);
				//setResult(-1);
				Intent result = new Intent();
				result.putExtra("total_amount",Double.toString(Global.formatNumFromLocale(amountView.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim())));
				Global.amountPaid = Double.toString(Global.formatNumFromLocale(amountView.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim()));
				setResult(-2,result);
				
				if(myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
					showPrintDlg(false);
				else
					finish();
			}
			else
			{
				Global.showPrompt(activity, R.string.dlog_title_error, temp2);
			}


		}
		
		
		private void showPrintDlg(boolean isRetry) {
			final Dialog dlog = new Dialog(activity,R.style.Theme_TransparentTest);
			dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dlog.setCancelable(false);
			dlog.setContentView(R.layout.dlog_btn_left_right_layout);
			
			TextView viewTitle = (TextView)dlog.findViewById(R.id.dlogTitle);
			TextView viewMsg = (TextView)dlog.findViewById(R.id.dlogMessage);
			viewTitle.setText(R.string.dlog_title_confirm);
			
			if(isRetry)
			{
				viewTitle.setText(R.string.dlog_title_error);
				viewMsg.setText(R.string.dlog_msg_failed_print);
			}
			else
			{
				viewMsg.setText(R.string.dlog_msg_print_cust_copy);
			}
			
			Button btnYes = (Button)dlog.findViewById(R.id.btnDlogLeft);
			Button btnNo = (Button)dlog.findViewById(R.id.btnDlogRight);
			btnYes.setText(R.string.button_yes);
			btnNo.setText(R.string.button_no);
			
			btnYes.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dlog.dismiss();
					new printAsync().execute();
				}
			});
			btnNo.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dlog.dismiss();
					finish();
				}
			});
			dlog.show();
		}
		
		
		private class printAsync extends AsyncTask<Void, Void, Void> 
		{
			private boolean printSuccessful = true;
			@Override
			protected void onPreExecute() {
				myProgressDialog = new ProgressDialog(activity);
				myProgressDialog.setMessage("Printing...");
				myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				myProgressDialog.setCancelable(false);
				myProgressDialog.show();

			}

			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub

				if(Global.mainPrinterManager!=null&&Global.mainPrinterManager.currentDevice!=null)
				{
					printSuccessful = Global.mainPrinterManager.currentDevice.printPaymentDetails(payment.getSetData("pay_id", true, null),1,true);
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void unused) {
				myProgressDialog.dismiss();
				if(printSuccessful)
					finish();
				else
				{
					showPrintDlg(true);
				}
			}
		}

		private boolean pingGeniusDevice() {

			boolean isReachable = true;
			try {
				String pingCmd = "ping -c 5 " + geniusIP;
				String pingResult = "";
				Runtime r = Runtime.getRuntime();
				Process p = r.exec(pingCmd);
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					System.out.println(inputLine);
					pingResult += inputLine;
				}
				in.close();
				if (pingResult.contains("Destination Host Unreachable"))
					isReachable = false;
			}
			catch (IOException e) {
				Tracker tracker = EasyTracker.getInstance(activity);
				tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
			}

			return isReachable;

		}
		
		
		private String getData(String tag, int record, int type) {
			Global global = (Global) getApplication();
			Integer i = global.dictionary.get(record).get(tag);
			if (i != null) {
				switch (type) {
				case 0:
					return returnedPost.get(record)[i];
				case 1: {
					if (i > 13)
						i = i - 1;
					return returnedGenius.get(record)[i];
				}
				}
			}
			return "";
		}

		private String payMethodDictionary(String value)
		{
			Limiters test = Limiters.toLimit(value);

			if (test != null) {
				switch (test) {
				case VISA:
					return "Visa";
				case MASTERCARD:
					return "MasterCard";
				case AMEX:
					return "AmericanExpress";
				case DISCOVER:
					return "Discover";
				case DEBIT:
					return "DebitCard";
				case GIFT:
					return "GiftCard";
				}
			}
			return "";
		}
		
		private void parseSignature(String signatureVector) {
			String[] splitFirstSentinel = signatureVector.split(Pattern.quote("^"));
			int size = splitFirstSentinel.length;

			if (size > 0) {
				String[] pairs = new String[2];
				Bitmap myBitmap = Bitmap.createBitmap(150, 80, Config.ARGB_8888);
				Canvas newCanvas = new Canvas();
				newCanvas.setBitmap(myBitmap);
				Paint t = new Paint();
				t.setStrokeWidth(2);
				t.setColor(Color.BLACK);

				for (int i = 0; i < size; i++) {
					pairs = splitFirstSentinel[i].split(Pattern.quote(","));
					if (!pairs[0].equals("~"))
						newCanvas.drawPoint((float) Integer.parseInt(pairs[0]), (float) Integer.parseInt(pairs[1]), t);
				}

				OutputStream outStream = null;
				MyPreferences myPref = new MyPreferences(activity);
				File file = new File(myPref.getCacheDir(), "test.png");

				try {
					outStream = new FileOutputStream(file);
					myBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);

					outStream.flush();
					outStream.close();
					
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					myBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
					byte[] b = baos.toByteArray();
					global.encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
					payment.getSetData("pay_signature", false,global.encodedImage);
					

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					Tracker tracker = EasyTracker.getInstance(activity);
					tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Tracker tracker = EasyTracker.getInstance(activity);
					tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
				}
			}
		}
	}
	
	private enum Limiters {
		VISA,MASTERCARD,AMEX,DISCOVER,DEBIT,GIFT;
		public static Limiters toLimit(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return null;
			}
		}
	}

}
