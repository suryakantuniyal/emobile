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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.CustomerCustomFieldsDAO;
import com.android.dao.PaymentMethodDAO;
import com.android.database.OrderProductsHandler;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.PaymentDetails;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.CustomerCustomField;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.PaymentMethod;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.GenerateNewID;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.android.support.textwatcher.GiftCardTextWatcher;
import com.crashlytics.android.Crashlytics;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import drivers.EMSIDTechUSB;
import drivers.EMSMagtekAudioCardReader;
import drivers.EMSRover;
import drivers.EMSUniMagDriver;
import interfaces.EMSCallBack;

public class CardManager_FA extends BaseFragmentActivityActionBar implements EMSCallBack, OnClickListener {

    public static final int CASE_GIFT = 0, CASE_LOYALTY = 1, CASE_REWARD = 2;
    private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
    private static CheckBox cardSwipe;
    private static boolean cardReaderConnected = false;
    private static String ourIntentAction = "";
    EMSPayGate_Default.EAction PAYMENT_ACTION;
    private EditText hiddenField;
    private String finalMessage;
    private String amountAdded;
    private EMSRover roverReader;
    private int cardTypeCase;
    private GiftCardActions giftCardActions;
    private String LOADING_MSG;
    private EMSCallBack msrCallBack;
    private Global global;
    private boolean hasBeenCreated = false, cardWasFound = false;
    private CreditCardInfo cardInfoManager;
    private EMSUniMagDriver uniMagReader;
    private EMSMagtekAudioCardReader magtekReader;
    private MyPreferences myPref;
    private EditText fieldCardNum, fieldAmountToAdd;
    private boolean wasReadFromReader = false;
    private Activity activity;
    private ProgressDialog myProgressDialog;
    private OrderProductsHandler ordProdDB;
    private HashMap<String, String> giftCardMap = new HashMap<String, String>();
    private PaymentsHandler paymentHandlerDB;
    private EMSIDTechUSB _msrUsbSams;
    private Payment payment;
    private boolean resetCustomerOnFinish = false;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // special case for "quit the app"
        if (event.getDevice() == null) {
            return super.dispatchKeyEvent(event);
        }
        // retrieve the input device of current event
        String device_desc = event.getDevice().getName();
        if (device_desc.equals("Sam4s SPT-4000 USB MCR")
                || device_desc.equals("ID TECH TM3 Magstripe USB-HID Keyboard Reader")) {
            // if(device_desc.equals("Sam4s SPT-4000 USB MCR")){
            if (getCurrentFocus() != hiddenField) {
                if (device_desc.equals("ID TECH TM3 Magstripe USB-HID Keyboard Reader"))
                    Global.isEncryptSwipe = true;

                if (getCurrentFocus() instanceof EditText) {
                    EditText text = (EditText) getCurrentFocus();
                    String value = text.getText().toString();
                    value = value.substring(0, value.length() - 1);
                    text.setText(value);
                }

                hiddenField.setText("");
                hiddenField.setFocusable(true);
                hiddenField.requestFocus();
            }

        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        cardTypeCase = extras.getInt("CARD_TYPE");
        giftCardActions = GiftCardActions.getByCode(extras.getInt("PROCESS_TYPE"));

        paymentHandlerDB = new PaymentsHandler(this);
        ordProdDB = new OrderProductsHandler(this);
        activity = this;
        msrCallBack = this;
        global = (Global) getApplication();
        myPref = new MyPreferences(this);
        Global.isEncryptSwipe = false;

        setContentView(R.layout.activate_card_layout);
        hiddenField = (EditText) findViewById(R.id.hiddenFieldGiftCardNumber);
        Button btnProcess = (Button) findViewById(R.id.processButton);
        btnProcess.setOnClickListener(this);
        fieldCardNum = (EditText) findViewById(R.id.fieldCardNumber);
        if (extras.containsKey("cardNumber")) {
            fieldCardNum.setText(extras.getString("cardNumber", ""));
            resetCustomerOnFinish = true;
        }
        cardSwipe = (CheckBox) findViewById(R.id.checkboxCardSwipe);
        CustomerCustomField customField = CustomerCustomFieldsDAO.findEMWSCardIdByCustomerId(myPref.getCustID());
        fieldCardNum.setText(customField == null ? "" : customField.getCustValue());
        TextView headerTitle = (TextView) findViewById(R.id.HeaderTitle);
        LinearLayout ll = (LinearLayout) findViewById(R.id.placeHolderInfo);

        switch (giftCardActions) {
            case CASE_ACTIVATE:
                headerTitle.setText(getString(R.string.activate));
                switch (cardTypeCase) {
                    case CASE_GIFT:
                        PAYMENT_ACTION = EMSPayGate_Default.EAction.ActivateGiftCardAction;
                        break;
                    case CASE_LOYALTY:
                        PAYMENT_ACTION = EMSPayGate_Default.EAction.ActivateLoyaltyCardAction;
                        break;
                    case CASE_REWARD:
                        PAYMENT_ACTION = EMSPayGate_Default.EAction.ActivateRewardAction;
                        break;
                }

                LOADING_MSG = getString(R.string.activating);
                if (cardTypeCase == CASE_GIFT)
                    ll.setVisibility(View.VISIBLE);
                break;
            case CASE_ADD_BALANCE:
                headerTitle.setText(getString(R.string.add_balance));
                PAYMENT_ACTION = EMSPayGate_Default.EAction.AddValueGiftCardAction;
                switch (cardTypeCase) {
                    case CASE_GIFT:
                        PAYMENT_ACTION = EMSPayGate_Default.EAction.AddValueGiftCardAction;
                        break;
                    case CASE_LOYALTY:
                        PAYMENT_ACTION = EMSPayGate_Default.EAction.AddValueLoyaltyCardAction;
                        break;
                    case CASE_REWARD:
                        PAYMENT_ACTION = EMSPayGate_Default.EAction.AddValueRewardAction;
                        break;
                }
                LOADING_MSG = getString(R.string.adding_balance);
                if (cardTypeCase == CASE_GIFT)
                    ll.setVisibility(View.VISIBLE);
                break;
            case CASE_BALANCE_INQUIRY:
                headerTitle.setText(getString(R.string.balance_inquiry));

                switch (cardTypeCase) {
                    case CASE_GIFT:
                        PAYMENT_ACTION = EMSPayGate_Default.EAction.BalanceGiftCardAction;
                        break;
                    case CASE_LOYALTY:
                        PAYMENT_ACTION = EMSPayGate_Default.EAction.BalanceLoyaltyCardAction;
                        break;
                    case CASE_REWARD:
                        PAYMENT_ACTION = EMSPayGate_Default.EAction.BalanceRewardAction;
                        break;
                }
                LOADING_MSG = getString(R.string.retrieving_balance);
                break;
            case CASE_MANUAL_ADD:
                headerTitle.setText(getString(R.string.add_balance));
                switch (cardTypeCase) {
                    case CASE_GIFT:
                        PAYMENT_ACTION = EMSPayGate_Default.EAction.AddValueGiftCardAction;
                        break;
                    case CASE_LOYALTY:
                        PAYMENT_ACTION = EMSPayGate_Default.EAction.AddValueLoyaltyCardAction;
                        break;
                    case CASE_REWARD:
                        PAYMENT_ACTION = EMSPayGate_Default.EAction.AddValueRewardAction;
                        break;
                }

                LOADING_MSG = getString(R.string.adding_balance_message);
                fieldAmountToAdd = (EditText) findViewById(R.id.fieldAmountToAdd);
                fieldAmountToAdd.setVisibility(View.VISIBLE);

                // Loyalty is based on points (whole numbers only) so we adjust the input for that case
                if (cardTypeCase == CASE_LOYALTY) {
                    fieldAmountToAdd.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else {
                    fieldAmountToAdd.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    fieldAmountToAdd.addTextChangedListener(getTextWatcher(fieldAmountToAdd));
                }
                if (extras.containsKey("amount")) {
                    fieldAmountToAdd.setText(extras.getString("amount", "0.00"));
                }
                break;
        }
        cardInfoManager = new CreditCardInfo();
        hiddenField.addTextChangedListener(new GiftCardTextWatcher(activity, hiddenField, fieldCardNum, cardInfoManager, Global.isEncryptSwipe));

        setUpCardReader();
        hasBeenCreated = true;
    }
//
//    private TextWatcher hiddenTxtWatcher(final EditText hiddenField) {
//
//        return new TextWatcher() {
//            @Override
//            public void afterTextChanged(Editable s) {
//                String value = s.toString();
//                if (value.contains("\n") && value.split("\n").length >= 2
//                        && value.substring(value.length() - 1).contains("\n")) {
//                    String data = hiddenField.getText().toString().replace("\n", "");
//                    hiddenField.setText("");
//
//                    cardInfoManager = Global.parseSimpleMSR(activity, data);
//                    updateViewAfterSwipe();
//                }
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//        };
//    }

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

        if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen()) {
            _msrUsbSams.CloseTheDevice();
        }
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    private void setUpCardReader() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        String _audio_reader_type = myPref.getPreferencesValue(MyPreferences.pref_audio_card_reader);
        if (audioManager.isWiredHeadsetOn()) {
            if (_audio_reader_type != null && !_audio_reader_type.isEmpty() && !_audio_reader_type.equals("-1")) {
                switch (_audio_reader_type) {
                    case Global.AUDIO_MSR_UNIMAG:
                        uniMagReader = new EMSUniMagDriver();
                        uniMagReader.initializeReader(activity);
                        break;
                    case Global.AUDIO_MSR_MAGTEK:
                        magtekReader = new EMSMagtekAudioCardReader(activity);
                        new Thread(new Runnable() {
                            public void run() {
                                magtekReader.connectMagtek(true, msrCallBack);
                            }
                        }).start();
                        break;
                    case Global.AUDIO_MSR_ROVER:
                        roverReader = new EMSRover();
                        roverReader.initializeReader(activity, false);
                        break;
                }
            }
        } else {
            int _swiper_type = myPref.getSwiperType();
            int _printer_type = myPref.getPrinterType();
            int _sled_type = myPref.sledType(true, -2);
            if (_swiper_type != -1 && Global.btSwiper != null && Global.btSwiper.getCurrentDevice() != null
                    && !cardReaderConnected) {
                Global.btSwiper.getCurrentDevice().loadCardReader(msrCallBack, false);
            }
            if (_sled_type != -1 && Global.btSled != null && Global.btSled.getCurrentDevice() != null
                    && !cardReaderConnected) {
                Global.btSled.getCurrentDevice().loadCardReader(msrCallBack, false);
            }
            if (_printer_type != -1 && Global.deviceHasMSR(_printer_type)) {
                if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null
                        && !cardReaderConnected)
                    Global.mainPrinterManager.getCurrentDevice().loadCardReader(msrCallBack, false);
            }
        }

        if (myPref.isET1(true, false) || myPref.isMC40(true, false)) {
            ourIntentAction = getString(R.string.intentAction3);
            Intent i = getIntent();
            handleDecodeData(i);
            cardSwipe.setChecked(true);
        } else if (myPref.isSam4s() || myPref.isPAT100() || EMSIDTechUSB.isUSBConnected(this)) {
            cardSwipe.setChecked(true);
            _msrUsbSams = new EMSIDTechUSB(activity, msrCallBack);
            if (_msrUsbSams.OpenDevice())
                _msrUsbSams.StartReadingThread();
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
        switch (v.getId()) {
            case R.id.processButton:
                if (giftCardActions == GiftCardActions.CASE_MANUAL_ADD) {
                    if (isValidAmount())
                        processInquiry();
                    else
                        Global.showPrompt(this, R.string.dlog_title_error, getString(R.string.error_wrong_amount));
                } else
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

    private boolean isValidAmount() {
        BigDecimal bd = Global.getBigDecimalNum(fieldAmountToAdd.getText().toString().trim());
        return bd.compareTo(Global.getBigDecimalNum("0")) == 1;
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

            switch (giftCardActions) {
                case CASE_ACTIVATE:
                case CASE_ADD_BALANCE:
                    giftCardMap = ordProdDB.getOrdProdGiftCard(cardInfoManager.getCardNumUnencrypted());
                    if (giftCardMap.isEmpty())
                        return false;
                    else {
                        TextView labelAmount = (TextView) findViewById(R.id.labelAmount);
                        String temp = giftCardMap.get("overwrite_price") == null ? "-1"
                                : giftCardMap.get("overwrite_price");
                        giftCardMap.put("overwrite_price", temp);
                        labelAmount.setText(Global.formatDoubleStrToCurrency(temp));
                        return true;
                    }
                case CASE_BALANCE_INQUIRY:
                case CASE_MANUAL_ADD:
                    return true;
            }
        } else if ((giftCardActions == GiftCardActions.CASE_ACTIVATE
                || giftCardActions == GiftCardActions.CASE_ADD_BALANCE) && (giftCardMap.isEmpty() || !cardWasFound))
            return false;
        return true;
    }

    private void processInquiry() {
        String cardType = "GiftCard";
        if (cardTypeCase == CASE_LOYALTY)
            cardType = "LoyaltyCard";
        else if (cardTypeCase == CASE_REWARD)
            cardType = "Reward";
        PaymentMethod paymentMethod = PaymentMethodDAO.getPaymentMethodByType(cardType);
        if (populateCardInfo() && paymentMethod != null) {
            AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
            payment = new Payment(this);
            GenerateNewID generator = new GenerateNewID(this);
            String tempPay_id;
            tempPay_id = generator.getNextID(GenerateNewID.IdType.PAYMENT_ID);
            payment.setPay_id(tempPay_id);
            payment.setCust_id(myPref.getCustID());
            payment.setCustidkey(myPref.getCustIDKey());
            payment.setEmp_id(String.valueOf(assignEmployee.getEmpId()));

            payment.setPay_name(cardInfoManager.getCardOwnerName());
            payment.setPay_ccnum(cardInfoManager.getCardNumAESEncrypted());
            payment.setCcnum_last4(cardInfoManager.getCardLast4());
            payment.setPay_expmonth(cardInfoManager.getCardExpMonth());
            payment.setPay_expyear(cardInfoManager.getCardExpYear());
            payment.setPay_seccode(cardInfoManager.getCardEncryptedSecCode());
            payment.setTrack_one(cardInfoManager.getEncryptedAESTrack1());
            payment.setTrack_two(cardInfoManager.getEncryptedAESTrack2());
            payment.setPaymethod_id(paymentMethod.getPaymethod_id());
            payment.setCard_type(cardType);
            payment.setPay_type("0");
            switch (giftCardActions) {
                case CASE_ACTIVATE:
                case CASE_ADD_BALANCE:
                    payment.setPay_amount(giftCardMap.get("overwrite_price"));
                    payment.setPaymethod_id(cardType + "Balance");
                    break;
                case CASE_MANUAL_ADD:
                    BigDecimal bd = Global.getBigDecimalNum(fieldAmountToAdd.getText().toString());
                    amountAdded = fieldAmountToAdd.getText().toString();
                    payment.setPaymethod_id(cardType + "Balance");
                    payment.setPay_amount(bd.toString());
                    break;
            }
            EMSPayGate_Default payGate = new EMSPayGate_Default(this, payment);
            String generatedURL;
            generatedURL = payGate.paymentWithAction(PAYMENT_ACTION, wasReadFromReader, cardType, cardInfoManager);
            new ProcessAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, generatedURL);
        } else {
            if (paymentMethod == null) {
                Global.showPrompt(activity, R.string.dlog_title_error, getString(R.string.invalid_payment_type));
            } else {
                Global.showPrompt(activity, R.string.dlog_title_error, getString(R.string.card_already_processed));
            }
        }
    }

    private void showPrintDlg(final HashMap<String, String> parsedMap) {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(R.string.dlog_msg_print_cust_copy);

        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, parsedMap);

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                showBalancePrompt(finalMessage);
            }
        });
        dlog.show();
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

                dlog.dismiss();
                if(resetCustomerOnFinish) {
                    myPref.resetCustInfo(getString(R.string.no_customer));
                }
                finish();
            }
        });
        dlog.show();
    }

    private void updateViewAfterSwipe() {
        SimpleDateFormat dt = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat dt2 = new SimpleDateFormat("yy", Locale.getDefault());
        String formatedYear = "";
        try {
            Date date = dt2.parse(cardInfoManager.getCardExpYear());
            formatedYear = dt.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            Crashlytics.logException(e);

        }

        cardInfoManager.setCardExpYear(formatedYear);
        fieldCardNum.setText(cardInfoManager.getCardNumUnencrypted());

        switch (giftCardActions) {
            case CASE_ACTIVATE:
            case CASE_ADD_BALANCE:
                giftCardMap = ordProdDB.getOrdProdGiftCard(cardInfoManager.getCardNumUnencrypted());
                if (!giftCardMap.isEmpty())
                    cardWasFound = true;
                TextView labelAmount = (TextView) findViewById(R.id.labelAmount);
                String temp = giftCardMap.get("overwrite_price") == null ? "-1" : giftCardMap.get("overwrite_price");
                giftCardMap.put("overwrite_price", temp);
                labelAmount.setText(Global.formatDoubleStrToCurrency(temp));
                break;
        }

        wasReadFromReader = true;
    }

    @Override
    public void cardWasReadSuccessfully(boolean read, CreditCardInfo cardManager) {
        this.cardInfoManager = cardManager;
        updateViewAfterSwipe();
        if (uniMagReader != null && uniMagReader.readerIsConnected()) {
            uniMagReader.startReading();
        } else if (magtekReader == null && Global.btSwiper == null && _msrUsbSams == null
                && Global.mainPrinterManager != null)
            Global.mainPrinterManager.getCurrentDevice().loadCardReader(msrCallBack, false);
    }

    @Override
    public void readerConnectedSuccessfully(boolean didConnect) {
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

    }

    @Override
    public void startSignature() {

    }

    @Override
    public void nfcWasRead(String nfcUID) {

    }

    public enum GiftCardActions {
        CASE_ACTIVATE(0), CASE_ADD_BALANCE(1), CASE_BALANCE_INQUIRY(2), CASE_MANUAL_ADD(3), CASE_DEACTIVATE(4);

        private int code;

        GiftCardActions(int code) {
            this.code = code;
        }

        public static GiftCardActions getByCode(int code) {
            switch (code) {
                case 0:
                    return CASE_ACTIVATE;
                case 1:
                    return CASE_ADD_BALANCE;
                case 2:
                    return CASE_BALANCE_INQUIRY;
                case 3:
                    return CASE_MANUAL_ADD;
                case 4:
                    return CASE_DEACTIVATE;
            }
            return null;
        }

        public int getCode() {
            return this.code;
        }

        public String getLabelByCode(Context context) {
            switch (code) {
                case 0:
                    return context.getResources().getString(R.string.activate);
                case 1:
                    return context.getResources().getString(R.string.add_balance);
                case 2:
                    return context.getResources().getString(R.string.balance_inquiry);
                case 3:
                    return context.getResources().getString(R.string.manually_add_balance);
                case 4:
                    return context.getResources().getString(R.string.deactivate);
            }
            return null;
        }
    }

    private class ProcessAsync extends AsyncTask<String, String, String> {

        private HashMap<String, String> parsedMap = new HashMap<>();
        private String urlToPost;
        private boolean wasProcessed = false;
        private String errorMsg = "Request could not be processed.";

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(LOADING_MSG);
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

            if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen()) {
                _msrUsbSams.CloseTheDevice();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            Post httpClient = new Post(activity);
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler();
            urlToPost = params[0];
            try {
                String xml = httpClient.postData(13, urlToPost);
                switch (xml) {
                    case Global.TIME_OUT:
                        errorMsg = "TIME OUT, would you like to try again?";
                        break;
                    case Global.NOT_VALID_URL:
                        errorMsg = "Can not proceed...";
                        break;
                    default:
                        InputSource inSource = new InputSource(new StringReader(xml));

                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        xr.setContentHandler(handler);
                        xr.parse(inSource);
                        parsedMap = handler.getData();
                        if (fieldAmountToAdd != null) {
                            parsedMap.put("amountAdded", amountAdded);
                        }

                        if (parsedMap != null && parsedMap.size() > 0 && parsedMap.get("epayStatusCode").equals("APPROVED"))
                            wasProcessed = true;
                        else if (parsedMap != null && parsedMap.size() > 0) {
                            errorMsg = "statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage");
                        } else
                            errorMsg = xml;
                        break;
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
                switch (giftCardActions) {
                    case CASE_ACTIVATE:
                    case CASE_ADD_BALANCE:
                        ordProdDB.updateOrdProdCardActivated(giftCardMap.get("ordprod_id"));
                        break;
                }

                if (giftCardActions == GiftCardActions.CASE_ADD_BALANCE
                        || giftCardActions == GiftCardActions.CASE_MANUAL_ADD || giftCardActions == GiftCardActions.CASE_ACTIVATE) {
                    payment.setPay_resultcode(parsedMap.get("pay_resultcode"));
                    payment.setPay_resultmessage(parsedMap.get("pay_resultmessage"));
                    payment.setPay_transid(parsedMap.get("CreditCardTransID"));
                    payment.setAuthcode(parsedMap.get("AuthorizationCode"));
                    payment.setPay_issync("1");
                    paymentHandlerDB.insert(payment);
                }

                String balance = (parsedMap.get("CardBalance") == null ? "0.0" : parsedMap.get("CardBalance"));

                if (cardTypeCase == CASE_LOYALTY)
                    sb.append("Card Balance: ").append(balance);
                else
                    sb.append("Card Balance: ").append(Global.getCurrencyFrmt(balance));

                finalMessage = sb.toString();
                PaymentDetails details = new PaymentDetails();
                details.setPay_amount(balance);
                details.setPaymethod_name("");
                showPrintDlg(parsedMap);

            } else // payment processing failed
            {
                Global.showPrompt(activity, R.string.dlog_title_error, errorMsg);
            }
        }
    }

    private class PrintAsync extends AsyncTask<HashMap<String, String>, String, HashMap<String, String>> {
        private boolean printSuccessful = true;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Printing...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            if (myProgressDialog.isShowing())
                myProgressDialog.dismiss();
            myProgressDialog.show();

        }

        @Override
        protected HashMap<String, String> doInBackground(HashMap<String, String>... params) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                HashMap<String, String> map = params[0];
                printSuccessful = Global.mainPrinterManager.getCurrentDevice().printBalanceInquiry(map);
                map.put("printSuccessful", String.valueOf(printSuccessful));
            }

            return params[0];
        }

        @Override
        protected void onPostExecute(HashMap<String, String> result) {
            myProgressDialog.dismiss();
            Boolean printSuccessful = Boolean.valueOf(result.get("printSuccessful"));
            if (!printSuccessful) {
                showPrintDlg(result);
            } else {
                if(resetCustomerOnFinish) {
                    myPref.resetCustInfo(getString(R.string.no_customer));
                }
                finish();
            }

        }
    }
}
