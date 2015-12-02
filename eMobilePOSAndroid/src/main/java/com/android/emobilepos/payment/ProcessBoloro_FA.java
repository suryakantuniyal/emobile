package com.android.emobilepos.payment;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXBoloroManual;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.BoloroCarrier;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Post;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import IDTech.MSR.uniMag.Common;

public class ProcessBoloro_FA extends FragmentActivity implements OnClickListener,OnItemSelectedListener{
	
	private Activity activity;
	private ProgressDialog progressDlog;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    private IntentFilter[] mNdefExchangeFilters;
    private boolean isManual = false;
    private Spinner carrierSpinner,accountSpinner;
    private List<BoloroCarrier>manualBoloroData;
    private String boloroTagID = "";
    private Payment payment;
    private EditText fieldPhone;
    private boolean isPolling = true;
    private EditText fieldAmountDue,fieldAmountPaid;
    private final int POLLING_SLEEP_TIME = 5000;
    
	private Global global;
	private boolean hasBeenCreated = false;
    
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Bundle extras = this.getIntent().getExtras();
        global = (Global)getApplication();
        if(extras.getBoolean("isNFC"))
        	setContentView(R.layout.boloro_nfc_layout);
        else {
        	setContentView(R.layout.boloro_manual_layout);
        	isManual = true;
        }
        
      
        Button processButton = (Button)findViewById(R.id.processButton);
        Button btnExact = (Button)findViewById(R.id.btnExact);
        processButton.setOnClickListener(this);
        btnExact.setOnClickListener(this);
        fieldPhone = (EditText)findViewById(R.id.fieldPhone);
        fieldAmountDue = (EditText)findViewById(R.id.fieldAmountDue);
        fieldAmountPaid = (EditText)findViewById(R.id.fieldAmountPaid);
        fieldAmountDue.setText(extras.getString("amount"));
        
        buildPayment();
        
        if (!isManual) {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        } else {
        	carrierSpinner = (Spinner)findViewById(R.id.spinnerBoloroCarrier);
        	carrierSpinner.setOnItemSelectedListener(this);
        	accountSpinner = (Spinner)findViewById(R.id.spinnerBoloroAccount);
        	new LoadBoloroDataAsync().execute();
        }
        
        hasBeenCreated = true;
	}
	
	
	@Override
    protected void onResume() {
        super.onResume();
        if(global.isApplicationSentToBackground(this))
			global.loggedIn = false;
		global.stopActivityTransitionTimer();
		
        if (hasBeenCreated && !global.loggedIn) {
			if(global.getGlobalDlog()!=null)
				global.getGlobalDlog().dismiss();
			global.promptForMandatoryLogin(this);
		}
        if(mNfcAdapter!=null)
        	enableNdefExchangeMode();
    }
	
	
    @Override
    protected void onPause() {
        super.onPause();
        if(mNfcAdapter!=null)
        	mNfcAdapter.disableForegroundDispatch(this);
        PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
		boolean isScreenOn = powerManager.isScreenOn();
		if(!isScreenOn)
			global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // NDEF exchange mode
    	if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
    		new NdefReaderTask().execute(intent); 
    }
    
    
    private void buildPayment() {
    	Bundle extras = activity.getIntent().getExtras();
    	payment = new Payment(activity);
    	MyPreferences myPref = new MyPreferences(activity);
    	payment.pay_id = extras.getString("pay_id");
		payment.emp_id = myPref.getEmpID();
		
        if (!extras.getBoolean("histinvoices")) {
			payment.job_id = extras.getString("job_id");
        } else {
			payment.inv_id = "";
		}
		
		
		if(!myPref.getShiftIsOpen())
			payment.clerk_id = myPref.getShiftClerkID();
		else if(myPref.getPreferences(MyPreferences.pref_use_clerks))
			payment.clerk_id = myPref.getClerkID();
		
		payment.cust_id = extras.getString("cust_id");
		payment.custidkey = extras.getString("custidkey","");
		
		
		payment.paymethod_id = extras.getString("paymethod_id");
		
		Global.amountPaid= fieldAmountPaid.getText().toString();
        if (!Global.amountPaid.isEmpty()) {
			payment.pay_amount = Global.amountPaid;
			payment.pay_dueamount  = Global.amountPaid;
		}
		
        if (isManual) {
			String val = fieldPhone.getText().toString().trim();
			payment.pay_phone = val;
		}
		
		
		String[] location = Global.getCurrLocation(activity);
		payment.pay_latitude = location[0];
		payment.pay_longitude = location[1];
		
		
        if (Global.isIvuLoto) {
			payment.IvuLottoNumber = extras.getString("IvuLottoNumber");
			payment.IvuLottoDrawDate = extras.getString("IvuLottoDrawDate");
			payment.IvuLottoQR = Global.base64QRCode(extras.getString("IvuLottoNumber"),extras.getString("IvuLottoDrawDate"));
			
			
            if (!extras.getString("Tax1_amount").isEmpty()) {
				payment.Tax1_amount = extras.getString("Tax1_amount");
				payment.Tax1_name = extras.getString("Tax1_name");
				
				payment.Tax2_amount = extras.getString("Tax2_amount");
				payment.Tax2_name = extras.getString("Tax2_name");
            } else {
				BigDecimal tempRate;
				double tempPayAmount = Global.formatNumFromLocale(Global.amountPaid);
				tempRate = new BigDecimal(tempPayAmount*0.06).setScale(2, BigDecimal.ROUND_UP);
				payment.Tax1_amount = tempRate.toPlainString();
				payment.Tax1_name = "Estatal";
				
				tempRate = new BigDecimal(tempPayAmount*0.01).setScale(2, BigDecimal.ROUND_UP);
				payment.Tax2_amount = tempRate.toPlainString();
				payment.Tax2_name = "Municipal";
			}
		}	
    }
    
    
    private void checkoutPayment() {
        if (isManual) {
	    	int telcoPos = carrierSpinner.getSelectedItemPosition();
			int transmodePos = accountSpinner.getSelectedItemPosition();
			payment.telcoid = manualBoloroData.get(telcoPos).getTelcoID();
			payment.transmode = manualBoloroData.get(telcoPos).getCarrierAccounts().get(transmodePos).get("payment_mode_id");
			 new ManualCheckoutAsync().execute();
        } else {
    		payment.tagid = boloroTagID;
    		new NFCCheckoutAsync().execute();
    	}
    }
    
    private boolean areValidFields() {
    	String val1 = fieldAmountDue.getText().toString();
    	String val2 = fieldAmountPaid.getText().toString();
    	
        if (!isManual) {
    		if(val1.isEmpty()||val2.isEmpty())
    			Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.card_validation_error));
    		else
    			return true;
        } else {
    		String val = fieldPhone.getText().toString().trim();
        	if(val.isEmpty()||val1.isEmpty()||val2.isEmpty())
        		Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.card_validation_error));
        	else
        		return true;
    	}
    	return false;
    }
    
    private class NdefReaderTask extends AsyncTask<Intent, Void, String> {

        @Override
        protected String doInBackground(Intent... params) {

        	Tag tagFromIntent = params[0].getParcelableExtra(NfcAdapter.EXTRA_TAG);
 	        String uid = Common.getHexStringFromBytes(tagFromIntent.getId());
 	        return uid;
        }

        @Override
        protected void onPostExecute(String result) {
        	if (result != null) {
                boloroTagID = result;
                Global.showPrompt(activity, R.string.dlog_title_confirm, activity.getString(R.string.dlog_msg_nfc_scanned));
            }
        }
    }
    
    
    private class LoadBoloroDataAsync extends AsyncTask<Void, Void, Void> {
    	private List<String> listAccounts;
    	private List<String>listCarriers = new ArrayList<String>();
    	private boolean failed = false;
		
    	@Override
		protected void onPreExecute() {
			progressDlog = new ProgressDialog(activity);
			progressDlog.setMessage(activity.getString(R.string.loading_boloro_data));
			progressDlog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDlog.setCancelable(false);
			progressDlog.show();
		}

        @Override
        protected Void doInBackground(Void... params) {

        	SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXBoloroManual myParser = new SAXBoloroManual(activity);

			try {
				
				EMSPayGate_Default payGate = new EMSPayGate_Default(activity,payment);
				String generatedURL = new String();
				generatedURL = payGate.paymentWithAction("GetMarketTelcos", false,null,null);
				
				Post httpClient = new Post();
				String xml = httpClient.postData(13, activity,generatedURL);

				InputSource inSource = new InputSource(new StringReader(xml));

				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				xr.setContentHandler(myParser);
				xr.parse(inSource);
				manualBoloroData = myParser.getData();
				int size = manualBoloroData.size();
                if (size > 0) {
					for (int i = 0; i < size; i++) {
						listCarriers.add(manualBoloroData.get(i).getTelcoName());
					}
                } else {
					failed = true;	
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
//				Tracker tracker = EasyTracker.getInstance(activity);
//				tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
			}
 	        
 	        return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        	progressDlog.dismiss();
            if (!failed) {
	        	SpinnerAdapter carrierAdapter = new SpinnerAdapter(activity, android.R.layout.simple_spinner_item,listCarriers.toArray(new String[listCarriers.size()]));
	        	listAccounts = manualBoloroData.get(0).getCarrierAccountsName();
	        	SpinnerAdapter accountAdapter = new SpinnerAdapter(activity, android.R.layout.simple_spinner_item,listAccounts.toArray(new String[listAccounts.size()]));
	        	
	        	carrierSpinner.setAdapter(carrierAdapter);
	        	accountSpinner.setAdapter(accountAdapter);
            } else {
        		
        	}
        		
        }
    }

	
    private class ManualCheckoutAsync extends AsyncTask<Void, Void, Void> {
    	HashMap<String,String> response;
    	private boolean failed = false;
    	
		@Override
		protected void onPreExecute() {
			progressDlog = new ProgressDialog(activity);
			progressDlog.setMessage(activity.getString(R.string.sending_boloro_payment));
			progressDlog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDlog.setCancelable(false);
			progressDlog.show();

		}

        @Override
        protected Void doInBackground(Void... params) {

        	SAXParserFactory spf = SAXParserFactory.newInstance();
        	SAXProcessCardPayHandler myParser = new SAXProcessCardPayHandler(activity);

			try {
				
				EMSPayGate_Default payGate = new EMSPayGate_Default(activity,payment);
				String generatedURL = new String();
				generatedURL = payGate.paymentWithAction("ProcessBoloroCheckout", false,null,null);
				
				Post httpClient = new Post();
				String xml = httpClient.postData(13, activity,generatedURL);

				InputSource inSource = new InputSource(new StringReader(xml));

				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				xr.setContentHandler(myParser);
				xr.parse(inSource);
				response = myParser.getData();
				
                if (response != null && !response.isEmpty() && Boolean.parseBoolean(response.get("success"))) {
					payment.pay_transid = response.get("transaction_id");
                } else {
				failed = true;	
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
//				Tracker tracker = EasyTracker.getInstance(activity);
//				tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
			}
 	        
 	        return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        	progressDlog.dismiss();
            if (!failed) {
        		new  BoloroPollingAsync().execute();
            } else if (response.containsKey("error_message")) {
        		Global.showPrompt(activity, R.string.dlog_title_error, response.get("error_message"));
            } else if (response.containsKey("epayStatusCode")) {
        		StringBuilder sb = new StringBuilder();
        		sb.append("Code:").append(response.get("statusCode")).append("\n");
        		sb.append("Msg:").append(response.get("statusMessage"));
        		Global.showPrompt(activity, R.string.dlog_title_error, sb.toString());
        	}
        }
    }
    
    
    private class NFCCheckoutAsync extends AsyncTask<Void, Void, Void> {
    	HashMap<String,String> response;
    	private boolean failed = false;
    	
		@Override
		protected void onPreExecute() {
			progressDlog = new ProgressDialog(activity);
			progressDlog.setMessage(activity.getString(R.string.sending_boloro_payment));
			progressDlog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDlog.setCancelable(false);
			progressDlog.show();

		}

        @Override
        protected Void doInBackground(Void... params) {

        	SAXParserFactory spf = SAXParserFactory.newInstance();
        	SAXProcessCardPayHandler myParser = new SAXProcessCardPayHandler(activity);

			try {
				
				EMSPayGate_Default payGate = new EMSPayGate_Default(activity,payment);
				String generatedURL = new String();
				generatedURL = payGate.paymentWithAction("GetTelcoInfoByTag", false,null,null);
				
				Post httpClient = new Post();
				String xml = httpClient.postData(13, activity,generatedURL);

				InputSource inSource = new InputSource(new StringReader(xml));

				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				xr.setContentHandler(myParser);
				xr.parse(inSource);
				response = myParser.getData();
				
                if (response != null && !response.isEmpty() && Boolean.parseBoolean(response.get("success"))) {
					payment.pay_transid = response.get("transaction_id");
                } else {
				failed = true;	
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
//				Tracker tracker = EasyTracker.getInstance(activity);
//				tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
			}
 	        
 	        return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        	progressDlog.dismiss();
            if (!failed) {
        		new  BoloroPollingAsync().execute();
        		//Global.showPrompt(activity, R.string.dlog_title_confirm, response.get("addnote"));
            } else if (response.containsKey("error_message")) {
        		Global.showPrompt(activity, R.string.dlog_title_error, response.get("error_message"));
            } else if (response.containsKey("epayStatusCode")) {
        		StringBuilder sb = new StringBuilder();
        		sb.append("Code:").append(response.get("statusCode")).append("\n");
        		sb.append("Msg:").append(response.get("statusMessage"));
        		Global.showPrompt(activity, R.string.dlog_title_error, sb.toString());
        	}
        }
    }
    
    
    private class BoloroPollingAsync extends AsyncTask<Void, Void, Void> {
    	HashMap<String,String> response;
    	private boolean failed = false;
    	private boolean transCompleted = false;
    	BoloroPollingAsync myTask;
    	
		@Override
		protected void onPreExecute() {
			myTask = this;
			if (progressDlog == null || !progressDlog.isShowing()) {
				progressDlog = new ProgressDialog(activity);
				
				
				progressDlog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDlog.setCancelable(false);
				if (isPolling) {
					progressDlog.setMessage(activity.getString(R.string.waiting_for_pin));
					progressDlog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getString(R.string.button_cancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									myTask.cancel(true);
									
					        		isPolling = false;
					        		progressDlog.dismiss();
					        		//myTask.doInBackground();
					        		new  BoloroPollingAsync().execute();
								}
							});
                } else {
					progressDlog.setMessage(activity.getString(R.string.cancelling_transaction));
				}
				progressDlog.show();
			}

		}

        @Override
        protected Void doInBackground(Void... params) {

        	SAXParserFactory spf = SAXParserFactory.newInstance();
        	SAXProcessCardPayHandler myParser = new SAXProcessCardPayHandler(activity);

			try {
				
				EMSPayGate_Default payGate = new EMSPayGate_Default(activity,payment);
				String generatedURL = new String();
				if(isPolling)//is Polling
				{
					isPolling = true;
					generatedURL = payGate.paymentWithAction("BoloroPolling", false,null,null);
                } else    //is Cancel
				{
					generatedURL = payGate.paymentWithAction("CancelBoloroTransaction", false,null,null);
				}
				
				
				InputSource inSource = null;
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				Post httpClient = new Post();
				String xml = "";
				do{
					xml = httpClient.postData(13, activity,generatedURL);
					inSource = new InputSource(new StringReader(xml));

//					SAXParser sp = spf.newSAXParser();
//					XMLReader xr = sp.getXMLReader();
					xr.setContentHandler(myParser);
					xr.parse(inSource);
					response = myParser.getData();
					
                    if (response != null && !response.isEmpty() && Boolean.parseBoolean(response.get("success"))) {
                        if (isPolling && response.containsKey("next_action") && response.get("next_action").equals("POLL")) {
							isPolling = true;
                            try {
                                Thread.sleep(POLLING_SLEEP_TIME);
                            } catch (InterruptedException e) {
						}
                        } else if (response.containsKey("next_action") && response.get("next_action").equals("SUCCESS")) {
							
							PaymentsHandler payHandler = new PaymentsHandler(activity);
							payment.processed = "1";
							BigDecimal bg = new BigDecimal(Global.amountPaid);
							Global.amountPaid = bg.setScale(2,RoundingMode.HALF_UP).toString();
							payment.pay_dueamount = Global.amountPaid;
							payment.pay_amount = Global.amountPaid;
		            		payHandler.insert(payment);
		            		isPolling = false;
							transCompleted  = true;
                        } else if (response.containsKey("next_action") && response.get("next_action").equals("FAILED"))
							failed = true;
                    } else {
						failed = true;	
					}
				}while(!failed&&isPolling&&!transCompleted);
				

			} catch (Exception e) {
				// TODO Auto-generated catch block
//				Tracker tracker = EasyTracker.getInstance(activity);
//				tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
			}
 	        
 	        return null;
        }

        @Override
        protected void onPostExecute(Void result) {
			progressDlog.dismiss();
            if (!failed) {
                if (transCompleted) {

            		showFinishDlog(response.get("short_message"));
                } else {
            		Global.showPrompt(activity, R.string.dlog_title_confirm, response.get("short_message"));
            	}
            } else if (response.containsKey("error_message") && !response.get("error_message").isEmpty()) {
        		Global.showPrompt(activity, R.string.dlog_title_error, response.get("error_message"));
            } else if (response.containsKey("short_message")) {
        		Global.showPrompt(activity, R.string.dlog_title_error, response.get("short_message"));
            } else if (response.containsKey("epayStatusCode")) {
        		StringBuilder sb = new StringBuilder();
        		sb.append("Code:").append(response.get("statusCode")).append("\n");
        		sb.append("Msg:").append(response.get("statusMessage"));
        		Global.showPrompt(activity, R.string.dlog_title_error, sb.toString());
        	}
        }
    }
    
    
	private void enableNdefExchangeMode() {
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
    }


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.processButton:
			
                if (areValidFields()) {
				 buildPayment();
				isPolling = true;
				checkoutPayment();
			}
			break;
		case R.id.btnExact:
			fieldAmountPaid.setText(fieldAmountDue.getText().toString());
			break;
		}
	}
	
	
	private class SpinnerAdapter extends ArrayAdapter<String> {
	    Context context;
	    String[] items = new String[] {};

	    public SpinnerAdapter(final Context context,
	            final int textViewResourceId, final String[] objects) {
	        super(context, textViewResourceId, objects);
	        this.items = objects;
	        this.context = context;
	    }

	    @Override
	    public View getDropDownView(int position, View convertView,
	            ViewGroup parent) {

	        if (convertView == null) {
	            LayoutInflater inflater = LayoutInflater.from(context);
	            convertView = inflater.inflate(
	                    android.R.layout.simple_spinner_item, parent, false);
	        }

	        TextView tv = (TextView) convertView
	                .findViewById(android.R.id.text1);
	        tv.setText(items[position]);
	        tv.setTextColor(Color.BLACK);
	        tv.setTextSize(18);
	        tv.setPadding(0,20, 0, 20);
	        return convertView;
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            LayoutInflater inflater = LayoutInflater.from(context);
	            convertView = inflater.inflate(
	                    android.R.layout.simple_spinner_item, parent, false);
	        }
	        TextView tv = (TextView) convertView
	                .findViewById(android.R.id.text1);
	        tv.setText(items[position]);
	        tv.setTextColor(Color.BLACK);
	        tv.setPadding(0, 10, 0, 10);
	        tv.setTextSize(18);
	        return convertView;
	    }
	}


	@Override
	public void onItemSelected(AdapterView<?> arg0, View v, int pos, long arg3) {
		// TODO Auto-generated method stub
		List<String>listAccounts = manualBoloroData.get(pos).getCarrierAccountsName();
    	SpinnerAdapter accountAdapter = new SpinnerAdapter(activity, android.R.layout.simple_spinner_item,listAccounts.toArray(new String[listAccounts.size()]));
    	accountSpinner.setAdapter(accountAdapter);
	}


	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	private void showFinishDlog(String msg) {
		final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
		dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlog.setCancelable(true);
		dlog.setCanceledOnTouchOutside(true);
		dlog.setContentView(R.layout.dlog_btn_single_layout);

		TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
		TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
		viewTitle.setText(R.string.dlog_title_confirm);
		viewMsg.setText(msg);
		Button btnOk = (Button) dlog.findViewById(R.id.btnDlogSingle);
		btnOk.setText(R.string.button_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dlog.dismiss();

				MyPreferences myPref = new MyPreferences(activity);
				if (!myPref.getLastPayID().isEmpty())
					myPref.setLastPayID("0");

				Intent result = new Intent();
				result.putExtra(
						"total_amount",
						Double.toString(Global.formatNumFromLocale(fieldAmountDue.getText().toString().replaceAll("[^\\d\\,\\.]", "")
								.trim())));
				setResult(-2, result);

				finish();
			}
		});
		dlog.show();
	}
	
}
