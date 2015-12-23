package com.android.emobilepos.payment;

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
import android.os.PowerManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.genius.GeniusResponse;
import com.android.emobilepos.models.genius.GeniusTransportToken;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXGetGeniusHandler;
import com.android.saxhandler.SAXProcessGeniusHandler;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.google.gson.Gson;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ProcessGenius_FA extends BaseFragmentActivityActionBar implements OnClickListener {
    private String paymethod_id;
    private Activity activity;
    private Bundle extras;

    private EditText invJobView, amountView;
    private ProgressDialog myProgressDialog;
    private Payment payment;
    private String geniusIP;
    private Global global;
    private MyPreferences myPref;
    private boolean hasBeenCreated = false;
    private boolean isRefund = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_genius_layout);
        activity = this;
        global = (Global) this.getApplication();
        extras = this.getIntent().getExtras();

        invJobView = (EditText) findViewById(R.id.geniusJobIDView);
        amountView = (EditText) findViewById(R.id.geniusAmountView);

        Button btnProcess = (Button) findViewById(R.id.processGeniusButton);
        btnProcess.setOnClickListener(this);

//		Button btnExact = (Button)findViewById(R.id.btnExact);
//		btnExact.setOnClickListener(this);

        myPref = new MyPreferences(activity);
        geniusIP = myPref.getGeniusIP();

        boolean isFromMainMenu = extras.getBoolean("isFromMainMenu");
        if (!isFromMainMenu) {
            invJobView.setEnabled(false);
        }


        paymethod_id = extras.getString("paymethod_id");

        String inv_id;
        if (extras.getBoolean("histinvoices"))
            inv_id = extras.getString("inv_id");
        else
            inv_id = extras.getString("job_id");

        if (extras.getBoolean("salesrefund"))
            isRefund = true;

        invJobView.setText(inv_id);
        amountView.setText(
                Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));
        amountView.addTextChangedListener(getTextWatcher(amountView));
        amountView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Selection.setSelection(amountView.getText(), amountView.getText().length());
                }
            }
        });
        hasBeenCreated = true;

    }

    private TextWatcher getTextWatcher(final EditText editText) {

        return new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ProcessCash_FA.parseInputedCurrency(s, editText);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (global.isApplicationSentToBackground(this))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            global.loggedIn = false;
        global.startActivityTransitionTimer();
    }


    private void processPayment() {
        payment = new Payment(activity);

        if (!this.extras.getBoolean("histinvoices"))
            payment.job_id = invJobView.getText().toString();
        else
            payment.inv_id = invJobView.getText().toString();

        if (!myPref.getShiftIsOpen())
            payment.clerk_id = myPref.getShiftClerkID();
        else if (myPref.getPreferences(MyPreferences.pref_use_clerks))
            payment.clerk_id = myPref.getClerkID();

        payment.pay_id = extras.getString("pay_id");
        payment.paymethod_id = paymethod_id;
        payment.pay_expmonth = "0";// dummy
        payment.pay_expyear = "2000";// dummy
        payment.pay_tip = "0.00";
        payment.pay_dueamount = NumberUtils.cleanCurrencyFormatedNumber(amountView.getText().toString());
        payment.pay_amount = NumberUtils.cleanCurrencyFormatedNumber(amountView.getText().toString());
        payment.originalTotalAmount = "0";


        EMSPayGate_Default payGate = new EMSPayGate_Default(activity, payment);
        String generatedURL;

        if (isRefund) {
            payment.is_refund = "1";
            payment.pay_type = "2";
            generatedURL = payGate.paymentWithAction("ReturnGeniusAction", false, "", null);
        } else
            generatedURL = payGate.paymentWithAction("ChargeGeniusAction", false, "", null);
        //generatedURL = payGate.defaultPaymentWithAction("ChargeGeniusAction", "0");

        new processLivePaymentAsync().execute(generatedURL);
    }

    private class processLivePaymentAsync extends AsyncTask<String, String, GeniusResponse> {

        private boolean boProcessed = false;
        private boolean geniusConnected = false;
        private String temp = "";

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Processing Payment...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected GeniusResponse doInBackground(String... params) {
            // TODO Auto-generated method stub
            Gson gson = new Gson();
            GeniusResponse geniusResponse = null;
            if (pingGeniusDevice()) {
                geniusConnected = true;
                Post post = new Post();
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXProcessGeniusHandler handler = new SAXProcessGeniusHandler(activity);

                try {
                    String xml = post.postData(13, activity, params[0]);
                    temp = xml;
                    InputSource inSource = new InputSource(new StringReader(xml));

                    SAXParser sp = spf.newSAXParser();
                    XMLReader xr = sp.getXMLReader();
                    xr.setContentHandler(handler);
                    xr.parse(inSource);
                    GeniusTransportToken geniusTransportToken = handler.getGeniusTransportToken();

                    if (geniusTransportToken != null && geniusTransportToken.getStatusCode().equalsIgnoreCase("APPROVED")) {// && getData("statusCode", 0, 0).equals("APPROVED")) {

                        boProcessed = true;
                        MyPreferences myPref = new MyPreferences(activity);
                        StringBuilder sb = new StringBuilder();
                        sb.append("http://").append(myPref.getGeniusIP()).append(":8080/v2/pos?TransportKey=").append(geniusTransportToken.getTransportkey());
                        sb.append("&Format=JSON");
                        String json = post.postData(11, activity, sb.toString());
                        geniusResponse = gson.fromJson(json, GeniusResponse.class);

                    }

                } catch (Exception e) {

                }

            }
            return geniusResponse;
        }

        @Override
        protected void onPostExecute(GeniusResponse response) {
            myProgressDialog.dismiss();

            if (!geniusConnected) {
                Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.failed_genius_connectivity));
            } else if (!boProcessed) {
                Global.showPrompt(activity, R.string.dlog_title_error, temp);
            } else if (response != null && (response.getStatus().equalsIgnoreCase("APPROVED") ||
                    response.getStatus().equalsIgnoreCase("DECLINED"))) {
                payment.pay_transid = response.getToken();
                payment.authcode = response.getAuthorizationCode();
                payment.ccnum_last4 = response.getAccountNumber();
                payment.pay_name = response.getCardholder();
                payment.pay_date = DateUtils.getDateStringAsString(response.getTransactionDate(), "MM/dd/yyyy HH:mm:ss a");
                String signa = response.getAdditionalParameters().getSignatureData();
                if (signa.contains("^"))
                    parseSignature(signa);
                payment.card_type = payMethodDictionary(response.getPaymentType());
                payment.processed = "1";
                payment.paymethod_id = "Genius";
                payment.emvContainer = new EMVContainer(response);
                PaymentsHandler payHandler = new PaymentsHandler(activity);
                if(response.getStatus().equalsIgnoreCase("APPROVED")) {
                    payHandler.insert(payment);
                }else{
                    payHandler.insertDeclined(payment);

                }
                EMVContainer emvContainer = new EMVContainer(response);

                String paid_amount = NumberUtils.cleanCurrencyFormatedNumber(amountView.getText().toString());//Double.toString(Global.formatNumFromLocale(amountView.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim()));
                Intent result = new Intent();
                result.putExtra("total_amount", paid_amount);

                result.putExtra("emvcontainer", new Gson().toJson(emvContainer, EMVContainer.class));
                Global.amountPaid = paid_amount;
                setResult(-2, result);

                if (myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
                    showPrintDlg(false);
                else
                    finish();
            } else {
                Global.showPrompt(activity, R.string.dlog_title_error, response.getStatus());
            }


        }


        private void showPrintDlg(boolean isRetry) {
            final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
            dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlog.setCancelable(false);
            dlog.setContentView(R.layout.dlog_btn_left_right_layout);

            TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
            TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
            viewTitle.setText(R.string.dlog_title_confirm);

            if (isRetry) {
                viewTitle.setText(R.string.dlog_title_error);
                viewMsg.setText(R.string.dlog_msg_failed_print);
            } else {
                viewMsg.setText(R.string.dlog_msg_print_cust_copy);
            }
            dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

            Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
            Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
            Button btnCancel = (Button) dlog.findViewById(R.id.btnDlogCancel);
            btnCancel.setVisibility(View.GONE);
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


        private class printAsync extends AsyncTask<Void, Void, Void> {
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

                if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null) {
                    printSuccessful = Global.mainPrinterManager.currentDevice.printPaymentDetails(payment.pay_id, 1, true, payment.emvContainer);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                myProgressDialog.dismiss();
                if (printSuccessful)
                    finish();
                else {
                    showPrintDlg(true);
                }
            }
        }

        private boolean pingGeniusDevice() {
            boolean isReachable = true;
            try {
                URL url = new URL("http://" + geniusIP + ":8080");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(2000);
                int code = connection.getResponseCode();

                isReachable = code == 200 || code == 400;
            } catch (IOException e) {

            }
            return isReachable;
        }


//        private String getData(String tag, int record, int type) {
//            Global global = (Global) getApplication();
//            Integer i = global.dictionary.get(record).get(tag);
//            if (i != null) {
//                switch (type) {
//                    case 0:
//                        return returnedPost.get(record)[i];
//                    case 1: {
//                        if (i > 13)
//                            i = i - 1;
//                        return returnedGenius.get(record)[i];
//                    }
//                }
//            }
//            return "";
//        }

        private String payMethodDictionary(String value) {
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

                for (String aSplitFirstSentinel : splitFirstSentinel) {
                    pairs = aSplitFirstSentinel.split(Pattern.quote(","));
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
                    payment.pay_signature = global.encodedImage;


                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
//					Tracker tracker = EasyTracker.getInstance(activity);
//					tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
//					Tracker tracker = EasyTracker.getInstance(activity);
//					tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
                }
            }
        }
    }

    public enum Limiters {
        VISA, MASTERCARD, AMEX, DISCOVER, DEBIT, GIFT;

        public static Limiters toLimit(String str) {
            try {
                return valueOf(str);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.processGeniusButton:
                Toast.makeText(activity, "Processing Genius", Toast.LENGTH_LONG).show();
                processPayment();
                break;
            case R.id.btnExact:
                break;
        }
    }

}
