package com.android.emobilepos.payment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.CustomerCustomFieldsDAO;
import com.android.dao.ShiftDAO;
import com.android.dao.StoredPaymentsDAO;
import com.android.database.DrawInfoHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.DrawReceiptActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.models.GroupTax;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.CustomerCustomField;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.StoreAndForward;
import com.android.emobilepos.ordering.BBPosShelpaDeviceDriver;
import com.android.ivu.MersenneTwisterFast;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.android.support.textwatcher.GiftCardTextWatcher;
import com.bbpos.bbdevice.BBDeviceController;
import com.crashlytics.android.Crashlytics;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import drivers.EMSIDTechUSB;
import drivers.EMSMagtekAudioCardReader;
import drivers.EMSNomad;
import drivers.EMSRover;
import drivers.EMSUniMagDriver;
import interfaces.EMSCallBack;
import util.json.UIUtils;

public class ProcessGiftCard_FA extends BaseFragmentActivityActionBar implements EMSCallBack,
        OnClickListener {

    private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
    private CheckBox cardSwipe, redeemAll;
    private static boolean cardReaderConnected = false;
    private static String ourIntentAction = "";
    private EditText fieldAmountDue, fieldAmountTendered, fieldCardNum, fieldHidden;
    private Global global;
    private MyPreferences myPref;
    private CreditCardInfo cardInfoManager;
    private boolean isFromMainMenu = false;
    private boolean isRefund = false;
    private Activity activity;
    private EMSUniMagDriver uniMagReader;
    private EMSMagtekAudioCardReader magtekReader;
    private EditText subtotal, tax1, tax2;
    private List<GroupTax> groupTaxRate;
    private ProgressDialog myProgressDialog;
    private String inv_id, custidkey;
    private Payment payment;
    private PaymentsHandler payHandler;
    private boolean hasBeenCreated = false;
    private EMSCallBack callBack;
    private EMSIDTechUSB _msrUsbSams;
    private GiftCardTextWatcher msrTextWatcher;
    private AssignEmployee assignEmployee;

    private BBDeviceController bbDeviceController;
    private BBPosShelpaDeviceDriver listener;
    private boolean bcrScanning;
    private String email;
    private String phone;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global = (Global) getApplication();
        activity = this;
        callBack = this;
        assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
        Bundle extras = getIntent().getExtras();
        boolean isFromSalesReceipt = extras.getBoolean("isFromSalesReceipt");

        Global.isEncryptSwipe = true;
        myPref = new MyPreferences(activity);
        setContentView(R.layout.process_giftcard_layout);
        groupTaxRate = new TaxesHandler(this).getGroupTaxRate(assignEmployee.getTaxDefault());
        cardInfoManager = new CreditCardInfo();
        cardSwipe = findViewById(R.id.checkboxCardSwipe);
        redeemAll = findViewById(R.id.checkboxRedeemAll);
        fieldAmountDue = findViewById(R.id.amountDueGiftCard);
        fieldAmountDue.addTextChangedListener(getTextWatcher(fieldAmountDue));
        fieldAmountDue.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        fieldAmountTendered = findViewById(R.id.amountTendered);
        fieldAmountTendered.addTextChangedListener(getTextWatcher(fieldAmountTendered));
        fieldCardNum = findViewById(R.id.cardNumEdit);
        fieldHidden = findViewById(R.id.hiddenField);
        subtotal = findViewById(R.id.subtotalGiftAmount);
        tax1 = findViewById(R.id.tax1GiftAmount);
        tax2 = findViewById(R.id.tax2GiftAmount);
        TextView tax1Lbl = findViewById(R.id.tax1GiftCardLbl);
        TextView tax2Lbl = findViewById(R.id.tax2GiftCardLbl);
        msrTextWatcher = new GiftCardTextWatcher(activity, fieldHidden, fieldCardNum, cardInfoManager, Global.isEncryptSwipe);
        if (!Global.isIvuLoto || isFromSalesReceipt) {
            findViewById(R.id.row1Gift).setVisibility(View.GONE);
            findViewById(R.id.row2Gift).setVisibility(View.GONE);
            findViewById(R.id.row3Gift).setVisibility(View.GONE);

        } else {
            subtotal.addTextChangedListener(getTextWatcher(subtotal));
            tax1.addTextChangedListener(getTextWatcher(tax1));
            tax2.addTextChangedListener(getTextWatcher(tax2));
            ProcessCash_FA.setTaxLabels(groupTaxRate, tax1Lbl, tax2Lbl);

        }
        if (myPref.isPrefillTotalAmount()) {
            this.fieldAmountTendered.setText(
                    Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));
        } else {
            this.fieldAmountTendered.setText("");
        }
        if (myPref.isCustSelected()) {
            if (!TextUtils.isEmpty(myPref.getCustID())) {
                CustomerCustomField customField = CustomerCustomFieldsDAO.findEMWSCardIdByCustomerId(myPref.getCustID());
                if (customField != null) {
                    fieldCardNum.setText(customField.getCustValue());
                }
            }
        }
        email = extras.getString("ord_email", "");
        phone = extras.getString("ord_phone", "");
        subtotal.setText(
                Global.formatDoubleToCurrency(0.00));
        tax1.setText(
                Global.formatDoubleToCurrency(0.00));
        tax2.setText(
                Global.getCurrencyFormat(Global.formatNumToLocale(0.00)));
        fieldHidden.addTextChangedListener(msrTextWatcher);
        Button btnExact = findViewById(R.id.exactAmountBut);
        Button btnProcess = findViewById(R.id.processButton);
        btnExact.setOnClickListener(this);
        btnProcess.setOnClickListener(this);

        setupHeaderTitle();
        setUpCardReader();
        hasBeenCreated = true;
        fieldHidden.requestFocus();


        listener = new BBPosShelpaDeviceDriver(this, ProcessGiftCard_FA.this);
        bbDeviceController = BBDeviceController.getInstance(
                getApplicationContext(), listener);
        bbDeviceController.startBarcodeReader();
//        Toast.makeText(ProcessGiftCard_FA.this, "startBarcodeReader", Toast.LENGTH_LONG).show();

    }

//    @Override
//    public void onDataScanned(String data) {
//        fieldCardNum.setText(data);
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
        if (bbDeviceController != null) {
            bbDeviceController.stopBarcodeReader();
        }
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

        bbDeviceController.stopBarcodeReader();
//        Toast.makeText(ProcessGiftCard_FA.this, "stopBarcodeReader", Toast.LENGTH_LONG).show();

        bbDeviceController.releaseBBDeviceController();
        bbDeviceController = null;
        listener = null;
//        Toast.makeText(ProcessGiftCard_FA.this, "releaseBBDeviceController", Toast.LENGTH_LONG).show();
    }

    private TextWatcher getTextWatcher(final EditText editText) {

        return new TextWatcher() {
            public void afterTextChanged(Editable s) {
                switch (editText.getId()) {
                    case R.id.subtotalGiftAmount: {
                        ProcessCash_FA.calculateTaxes(groupTaxRate, editText, tax1, tax2);
                        ProcessCash_FA.calculateAmountDue(subtotal, tax1, tax2, fieldAmountDue);
                        break;
                    }
                    case R.id.tax2GiftAmount:
                    case R.id.tax1GiftAmount: {
                        ProcessCash_FA.calculateAmountDue(subtotal, tax1, tax2, fieldAmountDue);
                        break;
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NumberUtils.parseInputedCurrency(s, editText);
                //parseInputedCurrency(s, type_id);
            }
        };
    }

    private void setupHeaderTitle() {
        TextView headerTitle = findViewById(R.id.HeaderTitle);
        Bundle extras = getIntent().getExtras();
        if (extras.getBoolean("salespayment")) {
            headerTitle.setText(getString(R.string.card_payment_title));
            isFromMainMenu = TextUtils.isEmpty(extras.getString("amount"))
                    || Double.parseDouble(extras.getString("amount")) == 0;
        } else if (extras.getBoolean("salesreceipt")) {
            headerTitle.setText(getString(R.string.card_payment_title));
        } else if (extras.getBoolean("salesrefund")) {
            isRefund = true;
            isFromMainMenu = TextUtils.isEmpty(extras.getString("amount"))
                    || Double.parseDouble(extras.getString("amount")) == 0;
            headerTitle.setText(getString(R.string.card_refund_title));
        } else if (extras.getBoolean("histinvoices")) {
            headerTitle.setText(getString(R.string.card_payment_title));
        } else if (extras.getBoolean("salesinvoice")) {
            headerTitle.setText(getString(R.string.card_invoice));
        }

        custidkey = extras.getString("custidkey");
        if (custidkey == null)
            custidkey = "";
        inv_id = extras.getString("job_id");

        fieldAmountDue.setText(
                Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));
        if (!isFromMainMenu) {
            fieldAmountDue.setEnabled(false);
        }
    }

    @SuppressWarnings("deprecation")
    private void setUpCardReader() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        String _audio_reader_type = myPref.getPreferencesValue(MyPreferences.pref_audio_card_reader);
        EMSNomad walkerReader;
        if (audioManager.isWiredHeadsetOn()) {
            if (_audio_reader_type != null && !_audio_reader_type.isEmpty() && !_audio_reader_type.equals("-1")) {
                if (_audio_reader_type.equals(Global.AUDIO_MSR_UNIMAG)) {
                    uniMagReader = new EMSUniMagDriver();
                    uniMagReader.initializeReader(activity);
                } else if (_audio_reader_type.equals(Global.AUDIO_MSR_MAGTEK)) {
                    magtekReader = new EMSMagtekAudioCardReader(activity);
                    new Thread(new Runnable() {
                        public void run() {
                            magtekReader.connectMagtek(true, callBack);
                        }
                    }).start();
                } else if (_audio_reader_type.equals(Global.AUDIO_MSR_ROVER)) {
                    EMSRover roverReader = new EMSRover();
                    roverReader.initializeReader(activity, false);
                } else if (_audio_reader_type.equals(Global.AUDIO_MSR_WALKER)) {
                    walkerReader = new EMSNomad();
                    myPref.setSwiperType(Global.NOMAD);
                }
            }

        } else if (_audio_reader_type.equals(Global.AUDIO_MSR_WALKER)) {
            walkerReader = new EMSNomad();
            myPref.setSwiperType(Global.NOMAD);

        } else {
            int _swiper_type = myPref.getSwiperType();
            int _printer_type = myPref.getPrinterType();
            int _sled_type = myPref.sledType(true, -2);
            if (_swiper_type != -1 && Global.btSwiper != null && Global.btSwiper.getCurrentDevice() != null
                    && !cardReaderConnected) {
                Global.btSwiper.getCurrentDevice().loadCardReader(callBack, false);
            }
            if (_sled_type != -1 && Global.btSled != null && Global.btSled.getCurrentDevice() != null
                    && !cardReaderConnected) {
                Global.btSled.getCurrentDevice().loadCardReader(callBack, false);
            }
            if (_printer_type != -1 && Global.deviceHasMSR(_printer_type)) {
                if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null
                        && !cardReaderConnected)
                    Global.mainPrinterManager.getCurrentDevice().loadCardReader(callBack, false);
            }
        }

        if (myPref.isET1(true, false) || myPref.isMC40(true, false)) {
            ourIntentAction = getString(R.string.intentAction3);
            Intent i = getIntent();
            handleDecodeData(i);
            cardSwipe.setChecked(true);
        } else if (myPref.isSam4s() || myPref.isPAT100()) {
            cardSwipe.setChecked(true);
            _msrUsbSams = new EMSIDTechUSB(activity, callBack);
            if (_msrUsbSams.OpenDevice())
                _msrUsbSams.StartReadingThread();
        } else if (myPref.isESY13P1()) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                Global.mainPrinterManager.getCurrentDevice().loadCardReader(callBack, false);
                cardSwipe.setChecked(true);
            }
        } else if (myPref.isEM100() || myPref.isEM70() || myPref.isOT310() || myPref.isKDC425()) {
            cardSwipe.setChecked(true);
        } else if (myPref.isPAT215() && Global.btSwiper == null) {
            if (Global.embededMSR != null && Global.embededMSR.getCurrentDevice() != null) {
                Global.embededMSR.getCurrentDevice().loadCardReader(callBack, false);
                cardSwipe.setChecked(false);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // special case for "quit the app"
        if (event.getDevice() == null) {
            return super.dispatchKeyEvent(event);
        }

        // retrieve the input device of current event
        String device_desc = event.getDevice().getName();
        if (device_desc.equals("Sam4s SPT-4000 USB MCR")) {
            if (getCurrentFocus() != fieldHidden) {

                if (getCurrentFocus() instanceof EditText) {
                    EditText text = (EditText) getCurrentFocus();
                    String value = text.getText().toString();
                    value = value.substring(0, value.length() - 1);
                    text.setText(value);
                }
                fieldHidden.setText("");
                fieldHidden.setFocusable(true);
                fieldHidden.requestFocus();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void populateCardInfo() {
        if (!cardInfoManager.getWasSwiped()) {
            Encrypt encrypt = new Encrypt(activity);
            int size = fieldCardNum.getText().toString().length();
            if (size > 4) {
                String last4Digits = (String) fieldCardNum.getText().toString().subSequence(size - 4, size);
                cardInfoManager.setCardLast4(last4Digits);
            }
            cardInfoManager.setCardNumAESEncrypted(encrypt.encryptWithAES(fieldCardNum.getText().toString()));
            cardInfoManager.setCardNumUnencrypted(fieldCardNum.getText().toString());

        }
    }

    private void processPayment() {
        double amountTendered = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(fieldAmountTendered));
        double totalAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(fieldAmountDue));
        if (Global.isIvuLoto) {
            Global.subtotalAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(subtotal));
        }
        populateCardInfo();

        Bundle extras = activity.getIntent().getExtras();
        payHandler = new PaymentsHandler(activity);
        payment = new Payment(activity);
        String cardType = extras.getString("paymentmethod_type");
        payment.setPay_id(extras.getString("pay_id"));

        payment.setEmp_id(String.valueOf(assignEmployee.getEmpId()));
        payment.setPay_email(email);
        payment.setPay_phone(phone);
        if (!extras.getBoolean("histinvoices")) {
            payment.setJob_id(inv_id);
        } else {
            payment.setInv_id("");
        }

        if (myPref.isUseClerks()) {
            payment.setClerk_id(myPref.getClerkID());
        } else if (ShiftDAO.isShiftOpen()) {
            payment.setClerk_id(String.valueOf(ShiftDAO.getOpenShift().getClerkId()));
        }

        payment.setCust_id(extras.getString("cust_id"));
        payment.setCustidkey(custidkey);


        Global.amountPaid = Double.toString(amountTendered);
        payment.setPay_dueamount(Double.toString(totalAmount - amountTendered));
        payment.setAmountTender(amountTendered);
        payment.setPay_amount(Double.toString(amountTendered));
        payment.setOriginalTotalAmount(Double.toString(totalAmount));

        payment.setPaymethod_id(extras.getString("paymethod_id"));

        payment.setPay_name(cardInfoManager.getCardOwnerName());

        payment.setPay_ccnum(cardInfoManager.getCardNumAESEncrypted());

        payment.setCcnum_last4(cardInfoManager.getCardLast4());
        payment.setPay_expmonth(cardInfoManager.getCardExpMonth());
        payment.setPay_expyear(cardInfoManager.getCardExpYear());
        payment.setPay_seccode(cardInfoManager.getCardEncryptedSecCode());

        payment.setTrack_one(cardInfoManager.getEncryptedAESTrack1());
        payment.setTrack_two(cardInfoManager.getEncryptedAESTrack2());

        Location location = Global.getCurrLocation(activity, false);
        payment.setPay_latitude(String.valueOf(location.getLatitude()));
        payment.setPay_longitude(String.valueOf(location.getLongitude()));

        payment.setCard_type(cardType);

        if (Global.isIvuLoto) {
            DrawInfoHandler drawDateInfo = new DrawInfoHandler(activity);
            MersenneTwisterFast mersenneTwister = new MersenneTwisterFast();
            String drawDate = drawDateInfo.getDrawDate();
            String ivuLottoNum = mersenneTwister.generateIVULoto();

            payment.setIvuLottoNumber(ivuLottoNum);
            payment.setIvuLottoDrawDate(drawDate);
            payment.setIvuLottoQR(Global.base64QRCode(ivuLottoNum, drawDate));
            if (!TextUtils.isEmpty(extras.getString("Tax1_amount"))) {
                payment.setTax1_amount(extras.getString("Tax1_amount"));
                payment.setTax1_name(extras.getString("Tax1_name"));

                payment.setTax2_amount(extras.getString("Tax2_amount"));
                payment.setTax2_name(extras.getString("Tax2_name"));
            } else {
                payment.setTax1_amount(Double.toString(Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(tax1))));
                if (groupTaxRate.size() > 0)
                    payment.setTax1_name(groupTaxRate.get(0).getTaxName());
                payment.setTax2_amount(Double.toString(Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(tax2))));
                if (groupTaxRate.size() > 1)
                    payment.setTax2_name(groupTaxRate.get(1).getTaxName());
            }
        }


        if (redeemAll.isChecked() && !(myPref.getPreferences(MyPreferences.pref_use_store_and_forward) && cardType.equalsIgnoreCase("GIFTCARD")))
            cardInfoManager.setRedeemAll("1");
        else
            cardInfoManager.setRedeemAll("0");
        cardInfoManager.setRedeemType("Only");

        EMSPayGate_Default payGate = new EMSPayGate_Default(activity, payment);
        String generatedURL = null;

        if (!isRefund) {
            payment.setPay_type("0");
            if (cardType.equalsIgnoreCase("GIFTCARD")) {
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeGiftCardAction, cardInfoManager.getWasSwiped(), cardType,
                        cardInfoManager);
            } else if (cardType.equalsIgnoreCase("REWARD")) {
                Global.rewardCardInfo = cardInfoManager;
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeRewardAction, cardInfoManager.getWasSwiped(), cardType,
                        cardInfoManager);
            }

        } else {
            payment.setIs_refund("1");
            payment.setPay_type("2");
            generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ReturnGiftCardAction, cardInfoManager.getWasSwiped(), cardType,
                    cardInfoManager);
        }

        if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward) && cardType.equalsIgnoreCase("GIFTCARD")) {
            processStoreForward(generatedURL, payment);
        } else {
            new processLivePaymentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, generatedURL);
        }

    }

    private void processStoreForward(String payment_xml, Payment payment) {
        if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen()) {
            _msrUsbSams.CloseTheDevice();
        }
        payment.setPayment_xml(payment_xml);
        payment.setPay_uuid(getXmlValue(payment_xml, "app_id"));

        StoredPaymentsDAO.insert(activity, payment, StoreAndForward.PaymentType.GIFT_CARD);
        Global.amountPaid = payment.getPay_amount();

        OrdersHandler dbOrders = new OrdersHandler(this);
        dbOrders.updateOrderStoredFwd(PaymentsHandler.getLastPaymentInserted().getJob_id(), "1");

        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("originalTotalAmount", payment.getOriginalTotalAmount());
        bundle.putString("total_amount", Double.toString(Global
                .formatNumFromLocale(payment.getOriginalTotalAmount())));
        bundle.putString("pay_dueamount", payment.getPay_dueamount());
        bundle.putString("pay_amount", payment.getPay_amount());
        data.putExtras(bundle);
        setResult(-2, data);
        finish();
    }

    private String getXmlValue(String _xml, String attribute) {
        String value = "";
        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xmlFactoryObject.newPullParser();
            parser.setInput(new StringReader(_xml));

            int event = parser.getEventType();
            String tag = "";

            boolean found = false;
            while (event != XmlPullParser.END_DOCUMENT && !found) {

                switch (event) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        if (tag != null) {
                            if (tag.equals(attribute)) {
                                value = parser.getText();
                                found = true;
                            }
                        }
                        break;
                }
                event = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return value;
    }

    public void showBalancePrompt(String msg) {
        if (myPref.isShowGiftCardBalanceAfterPayments()) {
            final Dialog dlog = new Dialog(this, R.style.Theme_TransparentTest);
            dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlog.setCancelable(false);
            dlog.setContentView(R.layout.dlog_btn_single_layout);

            TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
            TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
            viewTitle.setText(R.string.dlog_title_confirm);
            viewMsg.setText(msg);
            Button btnOk = dlog.findViewById(R.id.btnDlogSingle);
            btnOk.setText(R.string.button_ok);
            btnOk.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dlog.dismiss();
                    closeActivity();
                }
            });
            dlog.show();
        } else {
            Toast.makeText(ProcessGiftCard_FA.this, msg, Toast.LENGTH_LONG).show();
            closeActivity();
        }
    }

    public void closeActivity() {
        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("originalTotalAmount", payment.getOriginalTotalAmount());
        bundle.putString("total_amount", Double.toString(Global
                .formatNumFromLocale(payment.getOriginalTotalAmount())));
        bundle.putString("pay_dueamount", payment.getPay_dueamount());
        bundle.putString("pay_amount", payment.getPay_amount());
        bundle.putString("pay_amount", payment.getPay_amount());
        Global.amountPaid = payment.getPay_amount();
        data.putExtras(bundle);
        setResult(-2, data);
        finish();
    }

    private void updateViewAfterSwipe() {
        SimpleDateFormat dt = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat dt2 = new SimpleDateFormat("yy", Locale.getDefault());
        String formatedYear = "";
        try {
            Date date = dt2.parse(cardInfoManager.getCardExpYear());
            formatedYear = dt.format(date);
        } catch (ParseException e) {
            Crashlytics.logException(e);
        }

        cardInfoManager.setCardExpYear(formatedYear);
        fieldCardNum.setText(cardInfoManager.getCardNumAESEncrypted());
        cardInfoManager.setWasSwiped(true);
    }

    @Override
    public void cardWasReadSuccessfully(boolean read, CreditCardInfo cardManager) {
        if (read) {
            this.cardInfoManager = cardManager;
            updateViewAfterSwipe();
            if (uniMagReader != null && uniMagReader.readerIsConnected()) {
                uniMagReader.startReading();
            } else if (magtekReader == null && Global.btSwiper == null && _msrUsbSams == null
                    && Global.mainPrinterManager != null)
                Global.mainPrinterManager.getCurrentDevice().loadCardReader(callBack, false);
        } else {
            Global.showPrompt(activity, R.string.card_card_swipe, getString(R.string.error_reading_card));
        }

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
        if (myPref.isRemoveLeadingZerosFromUPC()) {
            data = NumberUtils.removeLeadingZeros(data);
        }
        fieldCardNum.setText(data);
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
            this.cardInfoManager = Global.parseSimpleMSR(activity, data);
            updateViewAfterSwipe();
        }
    }

    @Override
    public void onClick(View v) {
        if (UIUtils.singleOnClick(v)) {
            switch (v.getId()) {
                case R.id.exactAmountBut:
                    fieldAmountTendered.setText(fieldAmountDue.getText().toString());
                    break;
                case R.id.processButton:
                    v.setEnabled(false);
                    if (validatePaymentData()) {
                        processPayment();
                    } else {
                        v.setEnabled(true);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 138) {
            if (bcrScanning) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        bbDeviceController.stopBarcodeReader();
                        bbDeviceController.startBarcodeReader();
                    }
                }).start();
                bcrScanning = false;

            } else {
                bcrScanning = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        bbDeviceController.startBarcodeReader();
                        bbDeviceController.getBarcode();
                    }
                }).start();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean validatePaymentData() {
        String errorMsg = activity.getString(R.string.card_validation_error);
        fieldAmountDue.setBackgroundResource(android.R.drawable.edit_text);
        fieldAmountTendered.setBackgroundResource(android.R.drawable.edit_text);
        fieldCardNum.setBackgroundResource(android.R.drawable.edit_text);
        boolean isValid = true;
        if (TextUtils.isEmpty(fieldAmountDue.getText().toString())
                || Double.parseDouble(NumberUtils.cleanCurrencyFormatedNumber(fieldAmountDue)) <= 0) {
            isValid = false;
            fieldAmountDue.setBackgroundResource(R.drawable.edittext_wrong_input);
        }

        if (TextUtils.isEmpty(fieldAmountTendered.getText().toString())
                || Double.parseDouble(NumberUtils.cleanCurrencyFormatedNumber(fieldAmountTendered)) <= 0) {
            isValid = false;
            fieldAmountTendered.setBackgroundResource(R.drawable.edittext_wrong_input);
            errorMsg = activity.getString(R.string.error_wrong_amount);
        }
        if (TextUtils.isEmpty(fieldCardNum.getText().toString())) {
            isValid = false;
            fieldCardNum.setBackgroundResource(R.drawable.edittext_wrong_input);
        }

        double amountDue = TextUtils.isEmpty(fieldAmountDue.getText().toString()) ? 0
                : Double.parseDouble(NumberUtils.cleanCurrencyFormatedNumber(fieldAmountDue));
        double amountPaid = TextUtils.isEmpty(fieldAmountTendered.getText().toString()) ? 0
                : Double.parseDouble(NumberUtils.cleanCurrencyFormatedNumber(fieldAmountTendered));

        if (amountPaid > amountDue) {
            isValid = false;
            fieldAmountTendered.setBackgroundResource(R.drawable.edittext_wrong_input);
            errorMsg = activity.getString(R.string.card_overpaid_error);
        }
        if (!isValid) {
            Global.showPrompt(activity, R.string.validation_failed, errorMsg);
        }
        return isValid;
    }

    @Override
    public void startSignature() {
    }

    @Override
    public void nfcWasRead(String nfcUID) {

    }

    private class processLivePaymentAsync extends AsyncTask<String, String, String> {

        private HashMap<String, String> parsedMap = new HashMap<String, String>();
        private String urlToPost;
        private boolean wasProcessed = false;
        private String errorMsg = getString(R.string.coundnot_proceess_payment);

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(getString(R.string.processing_payment_msg));
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
                        errorMsg = getString(R.string.timeout_tryagain);
                        break;
                    case Global.NOT_VALID_URL:
                        errorMsg = getString(R.string.cannot_proceed);
                        break;
                    default:
                        InputSource inSource = new InputSource(new StringReader(xml));

                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        xr.setContentHandler(handler);
                        xr.parse(inSource);
                        parsedMap = handler.getData();
                        payment.setPay_amount(parsedMap.get("AuthorizedAmount"));
                        double due = Double.parseDouble(payment.getOriginalTotalAmount() == null ? "0" : payment.getOriginalTotalAmount())
                                - Double.parseDouble(payment.getPay_amount() == null ? "0" : payment.getPay_amount());
                        payment.setPay_dueamount(String.valueOf(due));
                        Global.amountPaid = payment.getPay_amount();
                        if (parsedMap != null && parsedMap.size() > 0 && parsedMap.get("epayStatusCode").equals("APPROVED"))
                            wasProcessed = true;
                        else if (parsedMap != null && parsedMap.size() > 0) {
                            errorMsg = "statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage");
                        } else
                            errorMsg = xml;
                        break;
                }

            } catch (SAXException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            } catch (IOException e) {
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
                payment.setPay_resultcode(parsedMap.get("pay_resultcode"));
                payment.setPay_resultmessage(parsedMap.get("pay_resultmessage"));
                payment.setPay_transid(parsedMap.get("CreditCardTransID"));
                payment.setAuthcode(parsedMap.get("AuthorizationCode"));
                payment.setProcessed("9");
                Intent intent = new Intent(activity, DrawReceiptActivity.class);
                intent.putExtra("isFromPayment", true);

                payHandler.insert(payment);

                showBalancePrompt("Status: " + parsedMap.get("epayStatusCode") + "\n" + "Auth. Amount: " + (parsedMap.get("AuthorizedAmount") == null ? "0.00" : parsedMap.get("AuthorizedAmount")) + "\n" + "Card Balance: " + Global.getCurrencyFrmt(parsedMap.get("CardBalance")) + "\n");
            } else // payment processing failed
            {
                findViewById(R.id.processButton).setEnabled(true);
                Global.showPrompt(activity, R.string.dlog_title_error, errorMsg);
            }
        }
    }
}
