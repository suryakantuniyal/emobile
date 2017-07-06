package com.android.emobilepos.payment;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.ShiftDAO;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.crashlytics.android.Crashlytics;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ProcessTupyx_FA extends FragmentActivity implements OnClickListener {

    private final int REQUEST_CAMERA = 0;
    private EditText fieldAmountToPay, fieldTotalAmount;
    private Activity activity;
    private ProgressDialog myProgressDialog;
    private boolean isRefund = false;
    private String qrCodeData = "";

    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    private IntentFilter[] mNdefExchangeFilters;
    private Payment payment;
    private Global global;
    private boolean hasBeenCreated = false;
    private NumberUtils numberUtils = new NumberUtils();
    private AssignEmployee assignEmployee;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        global = (Global) getApplication();
        assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tupyx_main_layout);


        Button processButton = (Button) findViewById(R.id.processButton);
        Button readQRButton = (Button) findViewById(R.id.readQRButton);
        Button btnExact = (Button) findViewById(R.id.btnExact);

        btnExact.setOnClickListener(this);
        processButton.setOnClickListener(this);
        readQRButton.setOnClickListener(this);

        fieldAmountToPay = (EditText) findViewById(R.id.fieldAmountToPay);
        fieldTotalAmount = (EditText) findViewById(R.id.fieldTotalAmount);

        fieldAmountToPay.addTextChangedListener(getTextWatcher(fieldAmountToPay));
        fieldTotalAmount.addTextChangedListener(getTextWatcher(fieldTotalAmount));
        fieldTotalAmount.setText(Global.formatDoubleToCurrency(0.00));
        fieldTotalAmount.setSelection(fieldTotalAmount.getText().toString().length());


        Bundle extras = this.getIntent().getExtras();
        String amount = extras.getString("amount") != null ? extras.getString("amount") : "0.00";
        fieldAmountToPay.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(amount))));

        boolean isFromMainMenu = extras.getBoolean("isFromMainMenu");
        if (!isFromMainMenu) {
            fieldAmountToPay.setEnabled(false);
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Handle all of our received NFC intents in this activity.
        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        hasBeenCreated = true;
    }

    private TextWatcher getTextWatcher(final EditText editText) {

        return new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NumberUtils.parseInputedCurrency(s, editText);
            }
        };
    }

    @Override
    protected void onResume() {

        if (global.isApplicationSentToBackground())
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !Global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
        }

        if (mNfcAdapter != null)
            enableNdefExchangeMode();

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // NDEF exchange mode
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage[] msgs = getNdefMessages(intent);
            String body = new String(msgs[0].getRecords()[0].getPayload());
            qrCodeData = body;
            Global.showPrompt(activity, R.string.dlog_title_confirm, getString(R.string.dlog_msg_nfc_scanned));
        }
    }

    private NdefMessage[] getNdefMessages(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{
                        record
                });
                msgs = new NdefMessage[]{
                        msg
                };
            }
        } else {
            finish();
        }
        return msgs;
    }

    private void enableNdefExchangeMode() {
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        switch (v.getId()) {
            case R.id.processButton:
                if (!qrCodeData.isEmpty())
                    processPayment();
                break;
            case R.id.readQRButton:
                Intent intent = new Intent(this, TupyxCamera_FA.class);
                startActivityForResult(intent, REQUEST_CAMERA);
                break;
            case R.id.btnExact:
                fieldTotalAmount.setText(fieldAmountToPay.getText());
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == 0 && intent != null)
                qrCodeData = intent.getStringExtra("result");
        }
    }

    private void processPayment() {
        payment = new Payment(activity);
        Bundle extras = activity.getIntent().getExtras();
        MyPreferences myPref = new MyPreferences(activity);

        payment = new Payment(activity);

        payment.setPay_id(extras.getString("pay_id"));

        payment.setEmp_id(String.valueOf(assignEmployee.getEmpId()));

        if (!extras.getBoolean("histinvoices")) {
            payment.setJob_id(extras.getString("job_id"));
        } else {
            payment.setInv_id("");
        }

        if (myPref.isUseClerks()) {
            payment.setClerk_id(myPref.getClerkID());
        } else if (ShiftDAO.isShiftOpen()) {
            payment.setClerk_id(String.valueOf(ShiftDAO.getOpenShift().getClerkId()));
        }

        payment.setCust_id(extras.getString("cust_id") != null ? extras.getString("cust_id") : "");
        payment.setCustidkey(extras.getString("custidkey") != null ? extras.getString("custidkey") : "");
        payment.setPaymethod_id(extras.getString("paymethod_id"));

//		String amountToBePaid = Double.toString(Global.formatNumFromLocale(fieldAmountToPay.getText().toString()
//				.replaceAll("[^\\d\\,\\.]", "").trim()));
//		String totalAmount = Double.toString(Global.formatNumFromLocale(fieldTotalAmount.getText().toString()
//				.replaceAll("[^\\d\\,\\.]", "").trim()));
//
//		Global.amountPaid = amountToBePaid;
//		payment.pay_amount = Global.amountPaid);
//		payment.pay_dueamount = Global.amountPaid);


        double _amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(fieldAmountToPay));
        double _actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(fieldTotalAmount));


        payment.setPay_dueamount(Double.toString(_amountToBePaid));

        if (_amountToBePaid > _actualAmount)
            payment.setPay_amount(Double.toString(_actualAmount));
        else
            payment.setPay_amount(Double.toString(_amountToBePaid));


        EMSPayGate_Default payGate = new EMSPayGate_Default(activity, payment);
        String generatedURL = new String();

        if (!isRefund) {

            payment.setPay_type("0");
            generatedURL = payGate.paymentWithTupyx(qrCodeData, payment.getPay_amount());
            /*
			 * if(wasReadFromReader) generatedURL =
			 * payGate.defaultPaymentWithAction("ChargeCreditCardAction","1");
			 * else generatedURL =
			 * payGate.defaultPaymentWithAction("ChargeCreditCardAction","0");
			 */
        } else {
            // payment.getSetData("is_refund", false, "1");
            // payment.getSetData("pay_type", false, "2");
            // payment.getSetData("pay_transid",false,
            // authIDField.getText().toString());
            // payment.getSetData("authcode", false,
            // transIDField.getText().toString());
            // generatedURL =
            // payGate.paymentWithAction("ReturnCreditCardAction",
            // false,creditCardType,cardInfoManager);

			/*
			 * if(wasReadFromReader) generatedURL =
			 * payGate.defaultPaymentWithAction("ReturnCreditCardAction","1");
			 * else generatedURL =
			 * payGate.defaultPaymentWithAction("ReturnCreditCardAction","0");
			 */
        }

        new processLivePaymentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, generatedURL);

    }

    private class processLivePaymentAsync extends AsyncTask<String, String, String> {

        private HashMap<String, String> parsedMap = new HashMap<String, String>();
        private String urlToPost;
        private boolean wasProcessed = false;
        private String errorMsg = "Could not process the payment.";


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
            Post httpClient = new Post(activity);

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler();
            urlToPost = params[0];


            try {
                String xml = httpClient.postData(Global.S_SUBMIT_TUPYX,  urlToPost);

                if (xml.equals(Global.TIME_OUT)) {
                    errorMsg = "TIME OUT, would you like to try again?";
                } else if (xml.equals(Global.NOT_VALID_URL)) {
                    errorMsg = "Can not proceed...";
                } else {
                    InputSource inSource = new InputSource(new StringReader(xml));

                    SAXParser sp = spf.newSAXParser();
                    XMLReader xr = sp.getXMLReader();
                    xr.setContentHandler(handler);
                    xr.parse(inSource);
                    parsedMap = handler.getData();

                    if (parsedMap != null && parsedMap.size() > 0 && parsedMap.get("epayStatusCode").equals("APPROVED"))
                        wasProcessed = true;
                    else if (parsedMap != null && parsedMap.size() > 0) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("statusCode = ").append(parsedMap.get("statusCode")).append("\n");
                        sb.append(parsedMap.get("statusMessage"));
                        errorMsg = sb.toString();
                    } else
                        errorMsg = xml;
                }

            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();

            if (wasProcessed)        //payment processing succeeded
            {
                PaymentsHandler payHandler = new PaymentsHandler(activity);
                payment.setPay_resultcode(parsedMap.get("statusCode"));
                payment.setPay_transid(parsedMap.get("CreditCardTransID"));
                payment.setAuthcode(parsedMap.get("AuthorizationCode"));
                payment.setCard_type(parsedMap.get("CCCardType"));
                payment.setCcnum_last4(parsedMap.get("last4digits"));
                payment.setPay_tip(parsedMap.get("tip"));
                payment.setPay_expmonth(parsedMap.get("pay_expmonth"));
                payment.setPay_expyear(parsedMap.get("payYear"));
                payment.setTupyx_user_id(parsedMap.get("tupyxUser"));

                Global.amountPaid = payment.getPay_amount();
                payHandler.insert(payment);
                //Global.amountPaid = payHandler.updateAfterCardPayment(extras.getString("pay_id"));


//				if(!myPref.getLastPayID().isEmpty())
//					myPref.setLastPayID("0");
//							
//				
//				global.encodedImage = new String();
//				if(requestCode == Global.FROM_JOB_INVOICE||requestCode == Global.FROM_OPEN_INVOICES||requestCode == Global.FROM_JOB_SALES_RECEIPT)
//					setResult(-2);
//				else
//				{
//					Intent result = new Intent();
//					result.putExtra("total_amount",  Double.toString(Global.formatNumFromLocale(this.amountField.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim())));
//					setResult(-2,result);
//				}
                setResult(-2);
                finish();

            } else                                                                                        //payment processing failed
            {
                Global.showPrompt(activity, R.string.dlog_title_error, errorMsg);
            }
        }
    }
}
