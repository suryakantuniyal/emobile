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
import android.support.v4.app.FragmentActivity;
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
import com.android.emobilepos.models.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXGetGeniusHandler;
import com.android.saxhandler.SAXProcessGeniusHandler;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ProcessGenius_FA extends BaseFragmentActivityActionBar implements OnClickListener {
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

        isFromMainMenu = extras.getBoolean("isFromMainMenu");
        if (!isFromMainMenu) {
            invJobView.setEnabled(false);
        }


        paymethod_id = extras.getString("paymethod_id");

        if (extras.getBoolean("histinvoices"))
            inv_id = extras.getString("inv_id");
        else
            inv_id = extras.getString("job_id");

        if (extras.getBoolean("salesrefund"))
            isRefund = true;

        invJobView.setText(inv_id);
        amountView.setText(extras.getString("amount"));
        amountView.addTextChangedListener(getTextWatcher(amountView));
//      move the amount cursor to the right of the default value
        Selection.setSelection(amountView.getText(), amountView.getText().length());

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
        payment.pay_dueamount = amountView.getText().toString();
        payment.pay_amount = amountView.getText().toString();
        payment.originalTotalAmount = "0";


        EMSPayGate_Default payGate = new EMSPayGate_Default(activity, payment);
        String generatedURL = new String();

        if (isRefund) {
            payment.is_refund = "1";
            payment.pay_type = "2";
            generatedURL = payGate.paymentWithAction("ReturnGeniusAction", false, "", null);
        } else
            generatedURL = payGate.paymentWithAction("ChargeGeniusAction", false, "", null);
        //generatedURL = payGate.defaultPaymentWithAction("ChargeGeniusAction", "0");

        new processLivePaymentAsync().execute(generatedURL);
    }

    private class processLivePaymentAsync extends AsyncTask<String, String, String> {

        private List<String[]> returnedPost;
        private List<String[]> returnedGenius;
        private boolean boProcessed = false;
        private boolean geniusConnected = false;
        private String temp = "", temp2 = "";

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

            if (pingGeniusDevice()) {
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

                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();

            if (!geniusConnected) {
                Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.failed_genius_connectivity));
            } else if (!boProcessed) {
                Global.showPrompt(activity, R.string.dlog_title_error, temp);
            } else if (returnedGenius != null && returnedGenius.size() > 0 && getData("Status", 0, 1).equals("APPROVED")) {
                payment.pay_transid = getData("Token", 0, 1);
                payment.authcode = getData("AuthorizationCode", 0, 1);
                payment.ccnum_last4 = getData("AccountNumber", 0, 1).replace("*", "").trim();
                payment.pay_name = getData("Cardholder", 0, 1);
                payment.pay_date = getData("TransactionDate", 0, 1);
                String signa = getData("SignatureData", 0, 1);
                if (signa.contains("^"))
                    parseSignature(signa);
                String paymethodType = payMethodDictionary(getData("PaymentType", 0, 1));
                payment.card_type = paymethodType;
                payment.processed = "1";
                //PayMethodsHandler payMethodsHandler = new PayMethodsHandler(activity);
                payment.paymethod_id = "Genius";

                PaymentsHandler payHandler = new PaymentsHandler(activity);
                payHandler.insert(payment);
                //setResult(-1);
                String paid_amount = Double.toString(Global.formatNumFromLocale(amountView.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim()));
                Intent result = new Intent();
                result.putExtra("total_amount", paid_amount);
                Global.amountPaid = paid_amount;
                setResult(-2, result);

                if (myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
                    showPrintDlg(false);
                else
                    finish();
            } else {
                Global.showPrompt(activity, R.string.dlog_title_error,  getData("Status", 0, 1));
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

            Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
            Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
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
                    printSuccessful = Global.mainPrinterManager.currentDevice.printPaymentDetails(payment.pay_id, 1, true);
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
                URL url = new URL("http://" + geniusIP+":8080");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(2000);
                int code = connection.getResponseCode();

                if (code == 200 || code == 400) {
                    isReachable = true;
                } else {
                    isReachable = false;
                }
            } catch (IOException e) {

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

    private enum Limiters {
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
