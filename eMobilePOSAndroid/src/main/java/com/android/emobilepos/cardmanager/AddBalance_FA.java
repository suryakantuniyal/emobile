package com.android.emobilepos.cardmanager;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.dao.CustomerCustomFieldsDAO;
import com.android.database.OrderProductsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.CustomerCustomField;
import com.android.emobilepos.models.realms.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.crashlytics.android.Crashlytics;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import drivers.EMSMagtekAudioCardReader;
import drivers.EMSRover;
import drivers.EMSUniMagDriver;
import interfaces.EMSCallBack;

public class AddBalance_FA extends BaseFragmentActivityActionBar implements EMSCallBack, OnClickListener {

    public static final int CASE_GIFT = 0, CASE_LOYALTY = 1, CASE_REWARD = 2;
    private int typeCase;

    private EMSCallBack msrCallBack;
    private Global global;
    private boolean hasBeenCreated = false;
    private CheckBox cardSwipe;
    private CreditCardInfo cardInfoManager;
    private EMSUniMagDriver uniMagReader;
    private EMSMagtekAudioCardReader magtekReader;
    private EMSRover roverReader;
    private static boolean cardReaderConnected = false;
    private MyPreferences myPref;
    private EditText fieldCardNum;
    private boolean wasReadFromReader = false;
    private static String ourIntentAction = "";
    private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
    private Activity activity;
    private ProgressDialog myProgressDialog;
    private OrderProductsHandler ordProdDB;
    private HashMap<String, String> giftCardMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activate_card_layout);
        Bundle extras = getIntent().getExtras();
        typeCase = extras.getInt("case");

        activity = this;
        ordProdDB = new OrderProductsHandler(this);
        msrCallBack = this;
        global = (Global) getApplication();
        myPref = new MyPreferences(this);
        Global.isEncryptSwipe = true;

        Button btnProcess = (Button) findViewById(R.id.processButton);
        btnProcess.setOnClickListener(this);
        fieldCardNum = (EditText) findViewById(R.id.fieldCardNumber);
        cardSwipe = (CheckBox) findViewById(R.id.checkboxCardSwipe);
        CustomerCustomField customField = CustomerCustomFieldsDAO.findEMWSCardIdByCustomerId(myPref.getCustID());
        fieldCardNum.setText(customField == null ? "" : customField.getCustValue());


        TextView headerTitle = (TextView) findViewById(R.id.HeaderTitle);
        headerTitle.setText(getString(R.string.add_balance));
        LinearLayout ll = (LinearLayout) findViewById(R.id.placeHolderInfo);

        if (typeCase == CASE_GIFT)
            ll.setVisibility(View.VISIBLE);

        setUpCardReader();
        hasBeenCreated = true;
    }


    @Override
    public void onResume() {

        if (global.isApplicationSentToBackground())
            Global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !Global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }


    @Override
    protected void onDestroy() {
        cardReaderConnected = false;


        if (uniMagReader != null)
            uniMagReader.release();
        else if (magtekReader != null)
            magtekReader.closeDevice();
        else if (Global.btSwiper != null && Global.btSwiper.getCurrentDevice() != null)
            Global.btSwiper.getCurrentDevice().releaseCardReader();
        else if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null)
            Global.mainPrinterManager.getCurrentDevice().releaseCardReader();


        super.onDestroy();
    }


    @SuppressWarnings("deprecation")
    private void setUpCardReader() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isWiredHeadsetOn()) {
            String _audio_reader_type = myPref.getPreferencesValue(MyPreferences.pref_audio_card_reader);
            if (_audio_reader_type != null && !_audio_reader_type.isEmpty() && !_audio_reader_type.equals("-1")) {
                if (_audio_reader_type.equals(Global.AUDIO_MSR_UNIMAG)) {
                    uniMagReader = new EMSUniMagDriver();
                    uniMagReader.initializeReader(activity);
                } else if (_audio_reader_type.equals(Global.AUDIO_MSR_MAGTEK)) {
                    magtekReader = new EMSMagtekAudioCardReader(activity);
                    new Thread(new Runnable() {
                        public void run() {
                            magtekReader.connectMagtek(true, msrCallBack);
                        }
                    }).start();
                } else if (_audio_reader_type.equals(Global.AUDIO_MSR_ROVER)) {
                    roverReader = new EMSRover();
                    roverReader.initializeReader(activity, false);
                }
            }

        } else {
            int _swiper_type = myPref.getSwiperType();
            int _printer_type = myPref.getPrinterType();
            if (_swiper_type != -1 && Global.btSwiper != null && Global.btSwiper.getCurrentDevice() != null && !cardReaderConnected) {
                Global.btSwiper.getCurrentDevice().loadCardReader(msrCallBack, false);
            } else if (_printer_type != -1
                    && (_printer_type == Global.STAR || _printer_type == Global.BAMBOO || _printer_type == Global.ZEBRA)) {
                if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null && !cardReaderConnected)
                    Global.mainPrinterManager.getCurrentDevice().loadCardReader(msrCallBack, false);
            }
        }
        // }
        if (myPref.isET1(true, false) || myPref.isMC40(true, false)) {
            ourIntentAction = getString(R.string.intentAction3);
            Intent i = getIntent();
            handleDecodeData(i);
            cardSwipe.setChecked(true);
        } else if (myPref.isSam4s() || myPref.isPAT100()) {
            cardSwipe.setChecked(true);
        } else if (myPref.isESY13P1()) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                Global.mainPrinterManager.getCurrentDevice().loadCardReader(msrCallBack, false);
                cardSwipe.setChecked(true);
            }
        } else if (myPref.isEM100() || myPref.isEM70() || myPref.isOT310() || myPref.isKDC425()) {
            cardSwipe.setChecked(true);
        } else if (myPref.isPAT215() && Global.btSwiper == null) {
            if (Global.embededMSR != null && Global.embededMSR.getCurrentDevice() != null) {
                Global.embededMSR.getCurrentDevice().loadCardReader(msrCallBack, false);
                cardSwipe.setChecked(false);
            }
        }
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.processButton:
                processInquiry();
                break;
        }
    }

    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        handleDecodeData(i);
    }

    private void handleDecodeData(Intent i) {
        // check the intent action is for us
        if (i.getAction() != null && i.getAction().contentEquals(ourIntentAction)) {

            // get the data from the intent
            String data = i.getStringExtra(DATA_STRING_TAG);
            this.cardInfoManager = Global.parseSimpleMSR(this, data);
            updateViewAfterSwipe();
        }
    }


    private boolean populateCardInfo() {
        if (!wasReadFromReader) {
            Encrypt encrypt = new Encrypt(activity);
            cardInfoManager = new CreditCardInfo();
            int size = fieldCardNum.getText().toString().length();
            if (size > 4) {
                String last4Digits = (String) fieldCardNum.getText().toString().subSequence(size - 4, size);
                cardInfoManager.setCardLast4(last4Digits);
            }
            cardInfoManager.setCardNumAESEncrypted(encrypt.encryptWithAES(fieldCardNum.getText().toString()));
            cardInfoManager.setCardNumUnencrypted(fieldCardNum.getText().toString());

            giftCardMap = ordProdDB.getOrdProdGiftCard(cardInfoManager.getCardNumUnencrypted());
            if (giftCardMap.isEmpty())
                return false;
            else {
                TextView labelAmount = (TextView) findViewById(R.id.labelAmount);
                String temp = giftCardMap.get("overwrite_price") == null ? "-1" : giftCardMap.get("overwrite_price");
                giftCardMap.put("overwrite_price", temp);
                labelAmount.setText(Global.getCurrencyFormat(temp));
                return true;
            }
        } else if (giftCardMap.isEmpty())
            return false;
        return true;
    }


    private void processInquiry() {

        if (populateCardInfo()) {

            Payment payment = new Payment(this);
            String cardType = "GiftCard";
            Bundle extras = getIntent().getExtras();

            payment.setPaymethod_id(extras.getString("paymethod_id"));

            payment.setPay_name(cardInfoManager.getCardOwnerName());
            payment.setPay_ccnum(cardInfoManager.getCardNumAESEncrypted());

            payment.setCcnum_last4(cardInfoManager.getCardLast4());
            payment.setPay_expmonth(cardInfoManager.getCardExpMonth());
            payment.setPay_expyear(cardInfoManager.getCardExpYear());
            payment.setPay_seccode(cardInfoManager.getCardEncryptedSecCode());

            payment.setTrack_one(cardInfoManager.getEncryptedAESTrack1());
            payment.setTrack_two(cardInfoManager.getEncryptedAESTrack2());

            payment.setPay_amount(giftCardMap.get("overwrite_price"));
            payment.setCard_type(cardType);

            EMSPayGate_Default payGate = new EMSPayGate_Default(this, payment);
            String generatedURL = new String();

            payment.setPay_type("0");
            if (typeCase == CASE_GIFT)
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.AddValueGiftCardAction, wasReadFromReader, cardType, cardInfoManager);

            new processAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, generatedURL);
        } else {
            Global.showPrompt(activity, R.string.dlog_title_error, "Card has already been processed");
        }
    }

    private class processAsync extends AsyncTask<String, String, String> {

        private HashMap<String, String> parsedMap = new HashMap<String, String>();
        private String urlToPost;
        private boolean wasProcessed = false;
        private String errorMsg = "Request could not be processed.";

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Adding Balance...");
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
                String xml = httpClient.postData(13,  urlToPost);

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

            if (wasProcessed) // payment processing succeeded
            {
                StringBuilder sb = new StringBuilder();
                //sb.append("Status: ").append(parsedMap.get("epayStatusCode")).append("\n");
                // sb.append("Auth. Amount: ").append(parsedMap.get("AuthorizedAmount")==null?
                // "0.00":parsedMap.get("AuthorizedAmount")).append("\n");
                ordProdDB.updateOrdProdCardActivated(giftCardMap.get("ordprod_id"));
                String temp = (parsedMap.get("CardBalance") == null ? "0.0" : parsedMap.get("CardBalance"));
                sb.append("Card Balance: ").append(Global.getCurrencyFrmt(temp));

                showBalancePrompt(sb.toString());

            } else // payment processing failed
            {
                Global.showPrompt(activity, R.string.dlog_title_error, errorMsg);
            }
        }
    }

    public void showBalancePrompt(String msg) {
        final Dialog dlog = new Dialog(this, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setContentView(R.layout.dlog_btn_single_layout);
        dlog.setCancelable(false);
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
                finish();
            }
        });
        dlog.show();
    }


    private void updateViewAfterSwipe() {
        //month.setText(cardInfoManager.getCardExpMonth());
        SimpleDateFormat dt = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat dt2 = new SimpleDateFormat("yy", Locale.getDefault());
        String formatedYear = new String();
        try {
            Date date = dt2.parse(cardInfoManager.getCardExpYear());
            formatedYear = dt.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        cardInfoManager.setCardExpYear(formatedYear);
        fieldCardNum.setText(cardInfoManager.getCardNumUnencrypted());

        giftCardMap = ordProdDB.getOrdProdGiftCard(cardInfoManager.getCardNumUnencrypted());

        TextView labelAmount = (TextView) findViewById(R.id.labelAmount);
        String temp = giftCardMap.get("overwrite_price") == null ? "-1" : giftCardMap.get("overwrite_price");
        giftCardMap.put("overwrite_price", temp);
        labelAmount.setText(Global.getCurrencyFormat(temp));

        wasReadFromReader = true;
    }


    @Override
    public void cardWasReadSuccessfully(boolean read, CreditCardInfo cardManager) {
        // TODO Auto-generated method stub
        this.cardInfoManager = cardManager;
        updateViewAfterSwipe();
        if (uniMagReader != null && uniMagReader.readerIsConnected()) {
            uniMagReader.startReading();
        } else if (magtekReader == null && Global.btSwiper == null && Global.mainPrinterManager != null)
            Global.mainPrinterManager.getCurrentDevice().loadCardReader(msrCallBack, false);
    }

    @Override
    public void readerConnectedSuccessfully(boolean didConnect) {
        // TODO Auto-generated method stub
        if (didConnect) {
            cardReaderConnected = true;
            if (uniMagReader != null && uniMagReader.readerIsConnected())
                uniMagReader.startReading();
            if (!cardSwipe.isChecked())
                cardSwipe.setChecked(true);
        } else {
            cardReaderConnected = false;
            if (cardSwipe.isChecked())
                cardSwipe.setChecked(false);
        }
    }

    @Override
    public void scannerWasRead(String data) {
        // TODO Auto-generated method stub

    }


    @Override
    public void startSignature() {
        // TODO Auto-generated method stub

    }

    @Override
    public void nfcWasRead(String nfcUID) {

    }
}
