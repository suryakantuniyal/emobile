package com.android.emobilepos.payment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.genius.GeniusResponse;
import com.android.emobilepos.models.genius.GeniusTransportToken;
import com.android.emobilepos.models.realms.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessGeniusHandler;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;
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
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import util.json.JsonUtils;

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
        if (extras.containsKey("LocalGeniusResponse")) {
            String response = extras.getString("LocalGeniusResponse");
            Gson gson = JsonUtils.getInstance();
            GeniusResponse geniusResponse = gson.fromJson(response, GeniusResponse.class);
//            Payment payment = gson.fromJson(extras.getString("Payment"), Payment.class);
            showResponse(geniusResponse);
        }

    }

    private void showResponse(GeniusResponse response) {
        Intent result = new Intent();
        result.putExtras(getIntent());
        if (response.getStatus().equalsIgnoreCase("APPROVED")) {
            setResult(-2, result);
        } else {
            setResult(0, result);
        }
        finish();
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
        super.onResume();
        boolean skipLogin = extras.containsKey("LocalGeniusResponse");
        if (global.isApplicationSentToBackground(this) && !skipLogin)
            Global.loggedIn = false;
        else {
            Global.loggedIn = true;
        }
        global.stopActivityTransitionTimer();
        if (hasBeenCreated && !Global.loggedIn) {
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
            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }


    private void processPayment() {
        payment = new Payment(activity);

        if (!this.extras.getBoolean("histinvoices"))
            payment.setJob_id(invJobView.getText().toString());
        else
            payment.setInv_id(invJobView.getText().toString());

        if (!myPref.getShiftIsOpen())
            payment.setClerk_id(myPref.getShiftClerkID());
        else if (myPref.isUseClerks())
            payment.setClerk_id(myPref.getClerkID());

        payment.setPay_id(extras.getString("pay_id"));
        payment.setPaymethod_id(paymethod_id);
        payment.setPay_expmonth("0");// dummy
        payment.setPay_expyear("2000");// dummy
        payment.setPay_tip("0.00");
        payment.setPay_dueamount(NumberUtils.cleanCurrencyFormatedNumber(amountView.getText().toString()));
        payment.setPay_amount(NumberUtils.cleanCurrencyFormatedNumber(amountView.getText().toString()));
        payment.setOriginalTotalAmount("0");


        EMSPayGate_Default payGate = new EMSPayGate_Default(activity, payment);
        String generatedURL;

        if (isRefund) {
            payment.setIs_refund("1");
            payment.setPay_type("2");
            generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ReturnGeniusAction, false, "", null);
        } else
            generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeGeniusAction, false, "", null);

        new processLivePaymentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, generatedURL);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.processGeniusButton:
                Toast.makeText(activity, "Processing Genius", Toast.LENGTH_LONG).show();
                processPayment();
                break;
            case R.id.btnExact:
                break;
        }
    }

    public enum Limiters {
        VISA, MASTERCARD, AMEX, DISCOVER, DEBIT, GIFT;

        public static Limiters toLimit(String str) {
            try {
                return valueOf(str);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                return null;
            }
        }
    }

    private class processLivePaymentAsync extends AsyncTask<String, String, GeniusResponse> {

        private boolean boProcessed = false;
        private boolean geniusConnected = false;


        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(getString(R.string.processing_payment_msg));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.manualEntry), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            myProgressDialog.show();
            myProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Post post = new Post();
                            MyPreferences myPref = new MyPreferences(activity);
                            String json = post.postData(11, activity, "http://" + myPref.getGeniusIP() + ":8080/v1/pos?Action=InitiateKeyedSale&Format=XML");
                        }
                    }).start();
                }
            });
        }

        @Override
        protected GeniusResponse doInBackground(String... params) {
            Gson gson = JsonUtils.getInstance();
            GeniusResponse geniusResponse = null;
//            if (pingGeniusDevice()) {
            geniusConnected = true;
            Post post = new Post();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXProcessGeniusHandler handler = new SAXProcessGeniusHandler(activity);

            try {
                String xml = post.postData(13, activity, params[0]);
                InputSource inSource = new InputSource(new StringReader(xml));

                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();
                xr.setContentHandler(handler);
                xr.parse(inSource);
                GeniusTransportToken geniusTransportToken = handler.getGeniusTransportToken();

                if (geniusTransportToken != null && geniusTransportToken.getStatusCode().equalsIgnoreCase("APPROVED")) {// && getData("statusCode", 0, 0).equals("APPROVED")) {
                    boProcessed = true;
                    MyPreferences myPref = new MyPreferences(activity);
                    String json = post.postData(11, activity, "http://" + myPref.getGeniusIP() + ":8080/v2/pos?TransportKey=" + geniusTransportToken.getTransportkey() + "&Format=JSON");
                    geniusResponse = gson.fromJson(json, GeniusResponse.class);
                } else {
                    geniusResponse = new GeniusResponse();
                    geniusResponse.setErrorMessage(geniusTransportToken.getStatusMessage() + "\r\n" + geniusTransportToken.getEpayStatusCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                if (geniusResponse == null)
                    geniusResponse = new GeniusResponse();
                geniusResponse.setErrorMessage(e.getMessage());
            }

//            }
            return geniusResponse;
        }

        @Override
        protected void onPostExecute(GeniusResponse response) {
            if (myProgressDialog != null && myProgressDialog.isShowing()) {
                myProgressDialog.dismiss();
            }

            if (!geniusConnected) {
                Global.showPrompt(activity, R.string.dlog_title_error, activity.getString(R.string.failed_genius_connectivity));
            } else if (!boProcessed) {
                Global.showPrompt(activity, R.string.dlog_title_error, response.getErrorMessage());
            } else if (response != null && (response.getStatus().equalsIgnoreCase("APPROVED") ||
                    response.getStatus().equalsIgnoreCase("DECLINED"))) {
                payment.setPay_transid(response.getToken());
                BigDecimal tip = new BigDecimal(0.00);
                BigDecimal cashBack = new BigDecimal(0.00);
                String signa = "";
                if (response.getAdditionalParameters() != null) {
                    if (response.getAdditionalParameters().getAmountDetails() != null) {
                        payment.setTipAmount(response.getAdditionalParameters().getAmountDetails().getUserTip());
                        payment.setPay_tip(response.getAdditionalParameters().getAmountDetails().getUserTip());
                        tip = new BigDecimal(response.getAdditionalParameters().getAmountDetails().getUserTip());
                        cashBack = new BigDecimal(response.getAdditionalParameters().getAmountDetails().getCashback());
                    } else {
                        payment.setTipAmount("0.00");
                        payment.setPay_tip("0.00");
                        tip = new BigDecimal(0.00);
                        cashBack = new BigDecimal(0.00);
                    }
                    signa = response.getAdditionalParameters().getSignatureData();
                }

                BigDecimal aprovedAmount = new BigDecimal(response.getAmountApproved());
                BigDecimal payAmount = aprovedAmount.subtract(tip).subtract(cashBack);
                payment.setPay_amount(Global.getRoundBigDecimal(payAmount));
                Global.amountPaid = payment.getPay_amount();
                payment.setAuthcode(response.getAuthorizationCode());
                payment.setCcnum_last4(response.getAccountNumber());
                payment.setPay_name(response.getCardholder());
                payment.setPay_date(DateUtils.getDateStringAsString(response.getTransactionDate(), "MM/dd/yyyy HH:mm:ss a"));
                if (signa.contains("^"))
                    parseSignature(signa);
                payment.setCard_type(payMethodDictionary(response.getPaymentType()));
                payment.setProcessed("1");
                payment.setPaymethod_id(PayMethodsHandler.getPayMethodID(payMethodDictionary(response.getPaymentType())));
                payment.setEmvContainer(new EMVContainer(response));
                PaymentsHandler payHandler = new PaymentsHandler(activity);
                if (response.getStatus().equalsIgnoreCase("APPROVED")) {
                    payHandler.insert(payment);
                } else {
                    payHandler.insertDeclined(payment);
                }
                EMVContainer emvContainer = new EMVContainer(response);

                String paid_amount = NumberUtils.cleanCurrencyFormatedNumber(amountView.getText().toString());//Double.toString(Global.formatNumFromLocale(amountView.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim()));
                Intent result = new Intent();
                result.putExtra("total_amount", paid_amount);

                result.putExtra("emvcontainer", new Gson().toJson(emvContainer, EMVContainer.class));
                if (response.getStatus().equalsIgnoreCase("APPROVED")) {
                    setResult(-2, result);
                } else {
                    setResult(0, result);
                }
                if (myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
                    showPrintDlg(false);
                else {
                    if (myPref.getGeniusIP().equalsIgnoreCase("127.0.0.1")) {
                        Intent i = new Intent(ProcessGenius_FA.this, ProcessGenius_FA.class);
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                i.setAction("com.emobilepos.app.VIEW_RESPONSE");
                        Gson gson = JsonUtils.getInstance();
                        String json = gson.toJson(response);
//            ComponentName.createRelative(ProcessGenius_FA.this,".payment.ProcessGenius_FA");
//                i.setComponent(new ComponentName(ProcessGenius_FA.this, ProcessGenius_FA.class));
//            i.setComponent(ComponentName.unflattenFromString("com.emobilepos.app/com.android.emobilepos.payment.ProcessGenius_FA"));
                        i.putExtras(extras);
                        i.putExtra("LocalGeniusResponse", json);
//                        i.putExtra("Payment", gson.toJson(payment));
                        result.putExtra("LocalGeniusResponse", json);
                        finish();
                        startActivity(i);
                    } else {
                        finish();
                    }
                }
            } else {
                Global.showPrompt(activity, R.string.dlog_title_error, response != null ? response.getStatus() : getString(R.string.failed_genius_connectivity));
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
                    dlog.dismiss();
                    new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
            btnNo.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dlog.dismiss();
                    finish();
                }
            });
            dlog.show();
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
                e.printStackTrace();
                Crashlytics.logException(e);
                isReachable = false;
            }
            return isReachable;
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

        private void parseSignature(String signatureVector) {
            String[] splitFirstSentinel = signatureVector.split(Pattern.quote("^"));
            int size = splitFirstSentinel.length;

            if (size > 0) {
                String[] pairs;
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

                OutputStream outStream;
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
                    payment.setPay_signature(global.encodedImage);


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
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

                if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                    printSuccessful = Global.mainPrinterManager.getCurrentDevice().printPaymentDetails(payment.getPay_id(), 1, true, payment.getEmvContainer());
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
    }

}
