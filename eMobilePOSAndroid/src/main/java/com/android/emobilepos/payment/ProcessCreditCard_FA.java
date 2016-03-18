package com.android.emobilepos.payment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.database.CustomersHandler;
import com.android.database.InvoicePaymentsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.PaymentsXML_DB;
import com.android.database.StoredPayments_DB;
import com.android.database.TaxesHandler;
import com.android.emobilepos.DrawReceiptActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.models.GroupTax;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.android.support.textwatcher.CreditCardTextWatcher;
import com.android.support.textwatcher.TextWatcherCallback;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import drivers.EMSIDTechUSB;
import drivers.EMSMagtekAudioCardReader;
import drivers.EMSRover;
import drivers.EMSUniMagDriver;
import drivers.EMSWalker;
import interfaces.EMSCallBack;

public class ProcessCreditCard_FA extends BaseFragmentActivityActionBar implements EMSCallBack, OnClickListener, TextWatcherCallback {

    private static final String CREDITCARD_TYPE_JCB = "JCB", CREDITCARD_TYPE_CUP = "CUP",
            CREDITCARD_TYPE_DISCOVER = "Discover", CREDITCARD_TYPE_VISA = "Visa", CREDITCARD_TYPE_DINERS = "DinersClub",
            CREDITCARD_TYPE_MASTERCARD = "MasterCard", CREDITCARD_TYPE_AMEX = "AmericanExpress";

    public enum PAYMENT_GIFT_CARDS {
        GIFTCARDS, LOYALTYCARD, REWARD
    }

    private String creditCardType = "";


    private static CheckBox cardSwipe = null;
    private static boolean cardReaderConnected = false;

    private MyPreferences myPref;

    private EditText hiddenField;
    private static EditText month, year, cardNum, ownersName, secCode, zipCode;

    private Global global;
    private Activity activity;
    private boolean hasBeenCreated = false;
    private ProgressDialog myProgressDialog;

    private PaymentsHandler payHandler;
    private InvoicePaymentsHandler invPayHandler;
    private String inv_id;
    private boolean wasReadFromReader = false;
    private boolean requireTransID = false;
    private boolean isFromMainMenu = false;
    private int orientation = 0;
    private int requestCode = 0;
    private boolean isRefund = false;
    private EditText tipAmount, reference, promptTipField;
    private EditText amountDueField;
    private EditText amountPaidField;
    private EditText phoneNumberField, customerEmailField;
    private EditText authIDField, transIDField;
    private TextView tax2Lbl;
    private EditText subtotal, tax1, tax2;
    private List<GroupTax> groupTaxRate;
    // private boolean timedOut = false;

    private boolean isMultiInvoice = false, isOpenInvoice = false;
    private String[] inv_id_array, txnID_array;
    private double[] balance_array;
    private List<String[]> invPaymentList;
    private EMSUniMagDriver uniMagReader;
    private EMSMagtekAudioCardReader magtekReader;
    private EMSRover roverReader;
    private String custidkey = "";
    public static TextView tvStatusMSR;

    private double amountToTip = 0;
    private double grandTotalAmount = 0;

    private TextView dlogGrandTotal;
    private EMSCallBack callBack;
    private CreditCardInfo cardInfoManager;

    private static String ourIntentAction = "";
    private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
    private Bundle extras;
    private boolean isDebit = false;
    private Button btnProcess;
    private ScrollView scrollView;
    private EMSIDTechUSB _msrUsbSams;

    private EMSWalker walkerReader;
    private NumberUtils numberUtils = new NumberUtils();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callBack = this;
        setContentView(R.layout.procress_card_layout);
        activity = this;
        global = (Global) getApplication();
        myPref = new MyPreferences(activity);
        groupTaxRate = TaxesHandler.getGroupTaxRate(myPref.getEmployeeDefaultTax());

        Global.isEncryptSwipe = true;
        cardInfoManager = new CreditCardInfo();
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        reference = (EditText) findViewById(R.id.referenceNumber);
        TextView headerTitle = (TextView) findViewById(R.id.HeaderTitle);
        tvStatusMSR = (TextView) findViewById(R.id.tvStatusMSR);
        cardSwipe = (CheckBox) findViewById(R.id.checkBox1);
        extras = this.getIntent().getExtras();
        String paymentMethodType = extras.getString("paymentmethod_type");
        isDebit = extras.getBoolean("isDebit");
        requireTransID = extras.getBoolean("requireTransID");

        if (extras.getBoolean("salespayment")) {
            headerTitle.setText(getString(R.string.card_payment_title));
            isFromMainMenu = true;
        } else if (extras.getBoolean("salesreceipt")) {
            headerTitle.setText(getString(R.string.card_payment_title));
            requestCode = Global.FROM_JOB_SALES_RECEIPT;
        } else if (extras.getBoolean("salesrefund")) {
            isRefund = true;
            isFromMainMenu = TextUtils.isEmpty(extras.getString("amount"))
                    || Double.parseDouble(extras.getString("amount")) == 0;
            headerTitle.setText(getString(R.string.card_refund_title));
        } else if (extras.getBoolean("histinvoices")) {
            headerTitle.setText(getString(R.string.card_payment_title));
            requestCode = Global.FROM_OPEN_INVOICES;
        } else if (extras.getBoolean("salesinvoice")) {
            headerTitle.setText(R.string.card_invoice);
        }

        custidkey = extras.getString("custidkey");
        if (custidkey == null)
            custidkey = "";

        hiddenField = (EditText) findViewById(R.id.hiddenField);
        zipCode = (EditText) findViewById(R.id.processCardZipCode);
        zipCode.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        month = (EditText) findViewById(R.id.monthEdit);
        month.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        listener(0, month);

        year = (EditText) findViewById(R.id.yearEdit);
        year.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        listener(0, year);

        authIDField = (EditText) findViewById(R.id.cardAuthIDField);
        transIDField = (EditText) findViewById(R.id.cardTransIDField);
        subtotal = (EditText) findViewById(R.id.subtotalCardAmount);
        tax1 = (EditText) findViewById(R.id.tax1CardAmount);
        tax2 = (EditText) findViewById(R.id.tax2CardAmount);
        TextView tax1Lbl = (TextView) findViewById(R.id.tax1CreditCardLbl);
        tax2Lbl = (TextView) findViewById(R.id.tax2CreditCardLbl);

        tax1.setText(Global.formatDoubleStrToCurrency(extras.getString("Tax1_amount")));
        tax2.setText(Global.formatDoubleStrToCurrency(extras.getString("Tax2_amount")));
        List<OrderProduct> orderProducts = global.orderProducts;
        double subtotalDbl = 0;
        for (OrderProduct products : orderProducts) {
            subtotalDbl += Double.parseDouble(products.itemSubtotal);
        }
        subtotal.setText(Global.formatDoubleToCurrency(subtotalDbl));
        this.amountDueField = (EditText) findViewById(R.id.processCardAmount);
        this.amountDueField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        this.amountDueField.setText(
                Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));

        amountDueField.addTextChangedListener(getTextWatcher(amountDueField));
        this.amountDueField.setOnFocusChangeListener(getFocusListener(amountDueField));


        subtotal.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        tax1.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        tax2.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        if (!isFromMainMenu || Global.isIvuLoto) {
            amountDueField.setEnabled(false);
        }
        if (!Global.isIvuLoto || !isFromMainMenu) {
            findViewById(R.id.row1Credit).setVisibility(View.GONE);
            findViewById(R.id.row2Credit).setVisibility(View.GONE);
            findViewById(R.id.row3Credit).setVisibility(View.GONE);

        } else {
            subtotal.setOnFocusChangeListener(getFocusListener(subtotal));
            tax1.setOnFocusChangeListener(getFocusListener(tax1));
            tax2.setOnFocusChangeListener(getFocusListener(tax2));
            subtotal.addTextChangedListener(getTextWatcher(subtotal));
            tax1.addTextChangedListener(getTextWatcher(tax1));
            tax2.addTextChangedListener(getTextWatcher(tax2));
            ProcessCash_FA.setTaxLabels(groupTaxRate, tax1Lbl, tax2Lbl);
        }
        this.amountPaidField = (EditText) findViewById(R.id.processCardAmountPaid);
        this.amountPaidField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        this.amountPaidField.addTextChangedListener(getTextWatcher(amountPaidField));

        this.amountPaidField.setOnFocusChangeListener(getFocusListener(this.amountPaidField));
        if (myPref.getPreferences(MyPreferences.pref_prefill_total_amount))
            this.amountPaidField.setText(
                    Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));
        else
            this.amountPaidField.setText("");

        Button exactBut = (Button) findViewById(R.id.exactAmountBut);
        exactBut.setOnClickListener(this);

        cardNum = (EditText) findViewById(R.id.cardNumEdit);
        cardNum.setInputType(InputType.TYPE_CLASS_NUMBER);
        cardNum.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        cardNum.setTransformationMethod(PasswordTransformationMethod.getInstance());

        ownersName = (EditText) findViewById(R.id.nameOnCardEdit);
        ownersName.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        if (extras.getBoolean("histinvoices")) {
            isMultiInvoice = extras.getBoolean("isMultipleInvoice");
            isOpenInvoice = true;
            if (!isMultiInvoice)
                inv_id = extras.getString("inv_id");
            else {
                inv_id_array = extras.getStringArray("inv_id_array");
                balance_array = extras.getDoubleArray("balance_array");
                txnID_array = extras.getStringArray("txnID_array");
            }
        } else
            inv_id = extras.getString("job_id");

        secCode = (EditText) findViewById(R.id.processCardSeccode);
        secCode.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        if (isDebit) {
            zipCode.setVisibility(View.GONE);
            secCode.setVisibility(View.GONE);

        }

        btnProcess = (Button) findViewById(R.id.processButton);
        btnProcess.setOnClickListener(this);

        Button tipButton = (Button) findViewById(R.id.tipAmountBut);
        tipButton.setOnClickListener(this);

        this.tipAmount = (EditText) findViewById(R.id.processCardTip);
        if (myPref.getPreferences(MyPreferences.pref_show_confirmation_screen)) {
            this.tipAmount.setVisibility(View.GONE);
            tipButton.setVisibility(View.GONE);
        } else {
            this.tipAmount.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            this.tipAmount.setText(Global.formatDoubleToCurrency(0.00));
            this.tipAmount.addTextChangedListener(getTextWatcher(tipAmount
            ));
            this.tipAmount.setOnFocusChangeListener(getFocusListener(this.tipAmount));
        }

        phoneNumberField = (EditText) findViewById(R.id.processCardPhone);
        customerEmailField = (EditText) findViewById(R.id.processCardEmail);

        if (!Global.getValidString(extras.getString("cust_id")).isEmpty())
            prefillCustomerInfo();
        else if (!extras.getString("order_email", "").isEmpty()) {
            customerEmailField.setText(extras.getString("order_email"));
        }

        hasBeenCreated = true;
        if (!paymentMethodType.equalsIgnoreCase(PAYMENT_GIFT_CARDS.GIFTCARDS.name())
                && !paymentMethodType.equalsIgnoreCase(PAYMENT_GIFT_CARDS.REWARD.name())
                && !paymentMethodType.equalsIgnoreCase(PAYMENT_GIFT_CARDS.LOYALTYCARD.name())) {
            enableManualCreditCard();
        }
        hiddenField.addTextChangedListener(new CreditCardTextWatcher(activity, hiddenField, cardNum, cardInfoManager, Global.isEncryptSwipe, this));

        setUpCardReader();
        if (myPref.getPrinterType() == Global.HANDPOINT || myPref.getPrinterType() == Global.ICMPEVO) {
            setHandopintUIFields();
        }
    }


    private void setHandopintUIFields() {
        cardNum.setVisibility(View.GONE);
        secCode.setVisibility(View.GONE);
        zipCode.setVisibility(View.GONE);
        month.setVisibility(View.GONE);
        year.setVisibility(View.GONE);
        authIDField.setVisibility(View.GONE);
        transIDField.setVisibility(View.GONE);
        tipAmount.setVisibility(View.GONE);
        findViewById(R.id.accountInformationTextView).setVisibility(View.GONE);
        findViewById(R.id.tipAmountBut).setVisibility(View.GONE);

    }

    private void enableManualCreditCard() {
        boolean allow = myPref.getPreferences(MyPreferences.pref_allow_manual_credit_card, true);
        cardNum.setEnabled(allow);
        secCode.setEnabled(allow);
        zipCode.setEnabled(allow);
        month.setEnabled(allow);
        year.setEnabled(allow);
    }

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

    private void prefillCustomerInfo() {
        CustomersHandler handler2 = new CustomersHandler(activity);
        HashMap<String, String> customerInfo = handler2.getCustomerMap(extras.getString("cust_id"));
        if (customerInfo != null) {
            if (!customerInfo.get("cust_name").isEmpty())
                ownersName.setText(customerInfo.get("cust_name"));
            if (!customerInfo.get("cust_phone").isEmpty())
                phoneNumberField.setText(customerInfo.get("cust_phone"));
            if (!customerInfo.get("cust_email").isEmpty())
                customerEmailField.setText(customerInfo.get("cust_email"));
        }
    }

    private TextWatcher getTextWatcher(final EditText editText) {

        return new TextWatcher() {
            public void afterTextChanged(Editable s) {
                switch (editText.getId()) {
                    case R.id.subtotalCardAmount: {
                        ProcessCash_FA.calculateTaxes(groupTaxRate, editText, tax1, tax2);
                        ProcessCash_FA.calculateAmountDue(subtotal, tax1, tax2, amountDueField);
                        break;
                    }
                    case R.id.tax2CardAmount:
                    case R.id.tax1CardAmount: {
                        ProcessCash_FA.calculateAmountDue(subtotal, tax1, tax2, amountDueField);
                        break;
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                numberUtils.parseInputedCurrency(s, editText);
                //parseInputedCurrency(s, type_id);
            }
        };
    }

    private OnFocusChangeListener getFocusListener(final EditText field) {

        return new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (v.hasFocus()) {
                    Selection.setSelection(field.getText(), field.getText().length());
                }

            }
        };
    }

    @SuppressWarnings("deprecation")
    private void setUpCardReader() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        String _audio_reader_type = myPref.getPreferencesValue(MyPreferences.pref_audio_card_reader);
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
                    roverReader = new EMSRover();
                    roverReader.initializeReader(activity, isDebit);
                } else if (_audio_reader_type.equals(Global.AUDIO_MSR_WALKER)) {
                    walkerReader = new EMSWalker(activity, true);

                }
            }

        } else if (_audio_reader_type.equals(Global.AUDIO_MSR_WALKER)) {
            walkerReader = new EMSWalker(activity, false);
            // new Thread(new Runnable(){
            // public void run()
            // {
            // walkerReader = new EMSWalker(activity);
            // }
            // }).start();
        } else {
            int _swiper_type = myPref.swiperType(true, -2);
            int _printer_type = myPref.getPrinterType();
            int _sled_type = myPref.sledType(true, -2);
            if (myPref.getPrinterType() == Global.HANDPOINT && Global.btSwiper.currentDevice == null) {
                Global.mainPrinterManager.loadDrivers(activity, Global.HANDPOINT, false);
            }
            if (_swiper_type != -1 && Global.btSwiper != null && Global.btSwiper.currentDevice != null
                    && !cardReaderConnected) {
                Global.btSwiper.currentDevice.loadCardReader(callBack, isDebit);
            } else if (_sled_type != -1 && Global.btSled != null && Global.btSled.currentDevice != null
                    && !cardReaderConnected) {
                Global.btSled.currentDevice.loadCardReader(callBack, isDebit);
            } else if (_printer_type != -1 && Global.deviceHasMSR(_printer_type)) {
                if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null
                        && !cardReaderConnected)
                    Global.mainPrinterManager.currentDevice.loadCardReader(callBack, isDebit);
            }
        }

        if (myPref.isET1(true, false) || myPref.isMC40(true, false)) {
            ourIntentAction = getString(R.string.intentAction3);
            Intent i = getIntent();
            handleDecodeData(i);
            cardSwipe.setChecked(true);
        } else if (myPref.isSam4s(true, false) || myPref.isPAT100()) {
            cardSwipe.setChecked(true);
            _msrUsbSams = new EMSIDTechUSB(activity, callBack);
            if (_msrUsbSams.OpenDevice())
                _msrUsbSams.StartReadingThread();
        } else if (myPref.isESY13P1()) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null) {
                Global.mainPrinterManager.currentDevice.loadCardReader(callBack, isDebit);
                cardSwipe.setChecked(true);
            }
        } else if (myPref.isEM100() || myPref.isEM70() || myPref.isOT310() || myPref.isKDC5000()) {
            cardSwipe.setChecked(true);
        }
    }

    private void populateCardInfo() {
        if (!wasReadFromReader) {
            Encrypt encrypt = new Encrypt(activity);
            int size = cardNum.getText().toString().length();
            String last4Digits = "";
            if (size > 4)
                last4Digits = (String) cardNum.getText().toString().subSequence(size - 4, size);
            cardInfoManager.setCardExpMonth(month.getText().toString());
            cardInfoManager.setCardExpYear(year.getText().toString());
            cardInfoManager.setCardLast4(last4Digits);
            cardInfoManager.setCardOwnerName(ownersName.getText().toString());
            // cardInfoManager.setCardEncryptedNum(encrypt.encryptWithAES(cardNum.getText().toString()));
            cardInfoManager.setCardNumAESEncrypted(encrypt.encryptWithAES(cardNum.getText().toString()));
            cardInfoManager.setCardEncryptedSecCode(encrypt.encryptWithAES(secCode.getText().toString()));

        }
    }

    private void processPayment() {

        if (walkerReader == null && myPref.getPrinterType() != Global.HANDPOINT && myPref.getPrinterType() != Global.ICMPEVO)
            populateCardInfo();
        if (Global.isIvuLoto) {
            Global.subtotalAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(subtotal));
        }
        payHandler = new PaymentsHandler(activity);


        String jobId = null;
        String invoiceId = null;
        if (!extras.getBoolean("histinvoices")) {
            jobId = inv_id;
        } else {
            invoiceId = inv_id;
        }


        String clerkId = null;
        if (!myPref.getShiftIsOpen())
            clerkId = myPref.getShiftClerkID();
        else if (myPref.getPreferences(MyPreferences.pref_use_clerks))
            clerkId = myPref.getClerkID();


        double amountTender = Global
                .formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));


        Global.amountPaid = Double.toString(amountTender);


        String taxAmnt1 = null;
        String taxName1 = null;
        String taxName2 = null;
        String taxAmnt2 = null;
        if (Global.isIvuLoto) {

            if (!extras.getString("Tax1_amount").isEmpty()) {
                taxAmnt1 = extras.getString("Tax1_amount");
                taxName1 = extras.getString("Tax1_name");

                taxAmnt2 = extras.getString("Tax2_amount");
                taxName2 = extras.getString("Tax2_name");
            } else {
                taxAmnt1 = Double.toString(Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(tax1)));
                if (groupTaxRate.size() > 0)
                    taxName1 = groupTaxRate.get(0).getTaxName();
                taxAmnt2 = Double.toString(Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(tax2)));
                if (groupTaxRate.size() > 1)
                    taxName2 = groupTaxRate.get(1).getTaxName();
            }
        }
        double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDueField));

        String isRef = null;
        String paymentType = null;
        String transactionId = null;
        String authcode = null;
        Payment payment = new Payment(activity, extras.getString("pay_id"), extras.getString("cust_id"), invoiceId, jobId, clerkId, custidkey, extras.getString("paymethod_id"),
                actualAmount, amountTender,
                cardInfoManager.getCardOwnerName(), reference.getText().toString(), phoneNumberField.getText().toString(),
                customerEmailField.getText().toString(), amountToTip, taxAmnt1, taxAmnt2, taxName1, taxName2,
                isRef, paymentType, creditCardType, cardInfoManager.getCardNumAESEncrypted(), cardInfoManager.getCardLast4(),
                cardInfoManager.getCardExpMonth(), cardInfoManager.getCardExpYear(),
                zipCode.getText().toString(), cardInfoManager.getCardEncryptedSecCode(), cardInfoManager.getEncryptedAESTrack1(),
                cardInfoManager.getEncryptedAESTrack2(), transactionId, authcode);


        if (walkerReader == null && myPref.getPrinterType() != Global.HANDPOINT && myPref.getPrinterType() != Global.ICMPEVO) {
            EMSPayGate_Default payGate = new EMSPayGate_Default(activity, payment);
            String generatedURL;

            if (!isRefund) {
                paymentType = "0";
                payment.pay_type = paymentType;
                if (isDebit)
                    generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeDebitAction, wasReadFromReader, creditCardType,
                            cardInfoManager);
                else
                    generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeCreditCardAction, wasReadFromReader,
                            creditCardType, cardInfoManager);


            } else {
                isRef = "1";
                paymentType = "2";
                transactionId = transIDField.getText().toString();
                authcode = authIDField.getText().toString();
                payment.is_refund = isRef;
                payment.pay_type = paymentType;
                payment.pay_transid = transactionId;
                payment.authcode = authcode;
                if (isDebit)
                    generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ReturnDebitAction, wasReadFromReader, creditCardType,
                            cardInfoManager);
                else
                    generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ReturnCreditCardAction, wasReadFromReader,
                            creditCardType, cardInfoManager);


            }
            if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) // Perform
                // store
                // and
                // forward
                // procedure

                processStoreForward(generatedURL, payment);
            else
                new processLivePaymentAsync().execute(generatedURL, payment);
        } else {
            if (!isRefund) {
                payment.pay_type = "0";
            } else {
                isRef = "1";
                transactionId = transIDField.getText().toString();
                authcode = authIDField.getText().toString();
                payment.is_refund = isRef;
                payment.pay_type = "2";
                payment.pay_transid = transactionId;
                payment.authcode = authcode;
            }
            saveApprovedPayment(null, payment);
        }
    }

    private void processMultiInvoicePayment() {
        populateCardInfo();
        invPayHandler = new InvoicePaymentsHandler(activity);
        invPaymentList = new ArrayList<String[]>();
        String[] content = new String[4];

        int size = inv_id_array.length;
        String payID = extras.getString("pay_id");

        double value;

        for (int i = 0; i < size; i++) {
            value = invPayHandler.getTotalPaidAmount(inv_id_array[i]);
            if (value != -1) {
                if (balance_array[i] >= value)
                    balance_array[i] -= value;
                else
                    balance_array[i] = 0.0;
            }
        }

        double tempPaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
        Global.amountPaid = Double.toString(tempPaid);
        boolean endBreak = false;
        for (int i = 0; i < size; i++) {
            if (balance_array[i] > 0) {
                if (tempPaid >= balance_array[i]) {
                    content[2] = Double.toString(balance_array[i]);
                    tempPaid -= balance_array[i];
                } else {
                    content[2] = Double.toString(tempPaid);
                    endBreak = true;
                }

                content[0] = payID;
                content[1] = inv_id_array[i];
                content[3] = txnID_array[i];
                invPaymentList.add(content);
                content = new String[4];
                if (endBreak)
                    break;
            }
        }

        // if(contentList.size()>0)
        // invHandler.insert(contentList);

        // MyPreferences myPref = new MyPreferences(activity);

        payHandler = new PaymentsHandler(activity);


        String clerkId = null;
        if (!myPref.getShiftIsOpen())
            clerkId = myPref.getShiftClerkID();
        else if (myPref.getPreferences(MyPreferences.pref_use_clerks))
            clerkId = myPref.getClerkID();


        double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
        double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDueField));

        String pay_dueamount = extras.getString("amount");

        Global.tipPaid = Double.toString(amountToTip);


        String taxName2 = null;
        String taxAmnt2 = null;
        String taxName1 = null;
        String taxAmnt1 = null;
        if (Global.isIvuLoto) {

            if (!extras.getString("Tax1_amount").isEmpty()) {
                taxAmnt1 = extras.getString("Tax1_amount");
                taxName1 = extras.getString("Tax1_name");

                taxAmnt2 = extras.getString("Tax2_amount");
                taxName2 = extras.getString("Tax2_name");
            } else {
                BigDecimal tempRate;
                double tempPayAmount = Global.formatNumFromLocale(Global.amountPaid);
                tempRate = new BigDecimal(tempPayAmount * 0.06).setScale(2, BigDecimal.ROUND_UP);
                taxAmnt1 = tempRate.toPlainString();
                taxName1 = "Estatal";

                tempRate = new BigDecimal(tempPayAmount * 0.01).setScale(2, BigDecimal.ROUND_UP);
                taxAmnt2 = tempRate.toPlainString();
                taxName2 = "Municipal";
            }
        }

        String isRef = null;
        String paymentType = null;
        String transactionId = null;
        String authcode = null;
        String invoiceId = null;
        String jobId = null;
        Payment payment = new Payment(activity, extras.getString("pay_id"), extras.getString("cust_id"), invoiceId, jobId, clerkId, custidkey, extras.getString("paymethod_id"),
                actualAmount, amountToBePaid,
                cardInfoManager.getCardOwnerName(), reference.getText().toString(), phoneNumberField.getText().toString(),
                customerEmailField.getText().toString(), amountToTip, taxAmnt1, taxAmnt2, taxName1, taxName2,
                isRef, paymentType, creditCardType, cardInfoManager.getCardNumAESEncrypted(), cardInfoManager.getCardLast4(),
                cardInfoManager.getCardExpMonth(), cardInfoManager.getCardExpYear(),
                zipCode.getText().toString(), cardInfoManager.getCardEncryptedSecCode(), cardInfoManager.getEncryptedAESTrack1(),
                cardInfoManager.getEncryptedAESTrack2(), transactionId, authcode);

        EMSPayGate_Default payGate = new EMSPayGate_Default(activity, payment);
        String generatedURL;

        if (!isRefund) {
            payment.pay_type = "0";
            if (isDebit)
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeDebitAction, wasReadFromReader, creditCardType,
                        cardInfoManager);
            else
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeCreditCardAction, wasReadFromReader, creditCardType,
                        cardInfoManager);
        } else {
            payment.is_refund = "1";
            payment.pay_type = "2";
            payment.pay_transid = authIDField.getText().toString();
            payment.authcode = transIDField.getText().toString();
            if (isDebit)
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ReturnDebitAction, wasReadFromReader, creditCardType,
                        cardInfoManager);
            else
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ReturnCreditCardAction, wasReadFromReader, creditCardType,
                        cardInfoManager);
        }

        new processLivePaymentAsync().execute(generatedURL, payment);

    }

    @Override
    public void onResume() {

        // if(_msrUsbSams!=null)
        // {
        // _msrUsbSams.registerReceiver();
        // }
        if (global.isApplicationSentToBackground(this))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // if(_msrUsbSams!=null)
        // {
        // _msrUsbSams.unregisterReceiver();
        // }

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        View view = getCurrentFocus();
        if (view != null) {
            view.clearFocus();
        }
        cardReaderConnected = false;

        if (uniMagReader != null)
            uniMagReader.release();
        else if (magtekReader != null)
            magtekReader.closeDevice();
        else if (roverReader != null)
            roverReader.release();
        else if (Global.btSwiper != null && Global.btSwiper.currentDevice != null)
            Global.btSwiper.currentDevice.releaseCardReader();
        else if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null)
            Global.mainPrinterManager.currentDevice.releaseCardReader();
        if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen()) {
            _msrUsbSams.CloseTheDevice();
        }
    }

    private void promptTipConfirmation() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogLayout = inflater.inflate(R.layout.tip_dialog_layout, null);

        // ****Method that works with both jelly bean/gingerbread
        // AlertDialog.Builder dialog = new
        // AlertDialog.Builder(this,R.style.TransparentDialog);

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog dialog = builder.create();
        dialog.setView(dialogLayout, 0, 0, 0, 0);
        dialog.setInverseBackgroundForced(true);
        dialog.setCancelable(false);
        // *****Method that works only with gingerbread and removes background
        /*
         * final Dialog dialog = new Dialog(activity,R.style.TransparentDialog);
		 * dialog.setContentView(dialogLayout);
		 */

        double amountToBePaid = Global
                .formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
        grandTotalAmount = amountToBePaid + amountToTip;

        Button tenPercent = (Button) dialogLayout.findViewById(R.id.tenPercent);
        Button fifteenPercent = (Button) dialogLayout.findViewById(R.id.fifteenPercent);
        Button twentyPercent = (Button) dialogLayout.findViewById(R.id.twentyPercent);
        dlogGrandTotal = (TextView) dialogLayout.findViewById(R.id.grandTotalView);

        dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));

        promptTipField = (EditText) dialogLayout.findViewById(R.id.otherTipAmountField);
        promptTipField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        promptTipField.clearFocus();
        promptTipField.setText("");

        Button cancelTip = (Button) dialogLayout.findViewById(R.id.cancelTipButton);
        Button saveTip = (Button) dialogLayout.findViewById(R.id.acceptTipButton);
        Button noneButton = (Button) dialogLayout.findViewById(R.id.noneButton);

        promptTipField.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                numberUtils.parseInputedCurrency(s, promptTipField);
            }
        });

        promptTipField.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (v.hasFocus()) {
                    Selection.setSelection(promptTipField.getText(), promptTipField.getText().length());
                }

            }
        });

        tenPercent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                double amountToBePaid = Global
                        .formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
                amountToTip = (float) (amountToBePaid * 0.1);
                grandTotalAmount = amountToBePaid + amountToTip;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                promptTipField.setText("");
            }
        });

        fifteenPercent.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                double amountToBePaid = Global
                        .formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
                amountToTip = (float) (amountToBePaid * 0.15);
                grandTotalAmount = amountToBePaid + amountToTip;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                promptTipField.setText("");
            }
        });

        twentyPercent.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                double amountToBePaid = Global
                        .formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
                amountToTip = (float) (amountToBePaid * 0.2);
                grandTotalAmount = amountToBePaid + amountToTip;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                promptTipField.setText("");
            }
        });

        noneButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
                amountToTip = 0;
                grandTotalAmount = amountToBePaid;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                // dialog.dismiss();
            }
        });

        cancelTip.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
                amountToTip = 0;
                grandTotalAmount = amountToBePaid;
                dialog.dismiss();
            }
        });

        saveTip.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (myPref.getPreferences(MyPreferences.pref_show_confirmation_screen)) {
                    dialog.dismiss();

                    if (!extras.getBoolean("histinvoices") || (isOpenInvoice && !isMultiInvoice))
                        processPayment();
                    else
                        processMultiInvoicePayment();
                } else {
                    if (tipAmount != null)
                        tipAmount.setText(Global.getCurrencyFormat(
                                Global.formatNumToLocale(Double.parseDouble(Double.toString(amountToTip)))));
                    dialog.dismiss();
                }

            }
        });
        dialog.show();
    }

    private void promptAmountConfirmation() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogLayout = inflater.inflate(R.layout.confirmation_amount_layout, null);

        // ****Method that works with both jelly bean/gingerbread
        // AlertDialog.Builder dialog = new
        // AlertDialog.Builder(this,R.style.TransparentDialog);

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog dialog = builder.create();
        dialog.setView(dialogLayout, 0, 0, 0, 0);
        dialog.setInverseBackgroundForced(true);
        dialog.setCancelable(false);
        // *****Method that works only with gingerbread and removes background
        /*
         * final Dialog dialog = new Dialog(activity,R.style.TransparentDialog);
		 * dialog.setContentView(dialogLayout);
		 */

        dlogGrandTotal = (TextView) dialogLayout.findViewById(R.id.confirmTotalView);
        TextView dlogCardType = (TextView) dialogLayout.findViewById(R.id.confirmCardType);
        TextView dlogCardExpDate = (TextView) dialogLayout.findViewById(R.id.confirmExpDate);
        TextView dlogCardNum = (TextView) dialogLayout.findViewById(R.id.confirmCardNumber);
        double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));

        grandTotalAmount = amountToBePaid;
        dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
        dlogCardType.setText(creditCardType);
        int size = cardNum.getText().toString().length();
        String last4Digits = "";
        if (size > 0)
            last4Digits = (String) cardNum.getText().toString().subSequence(size - 4, size);
        dlogCardNum.setText("*" + last4Digits);
        dlogCardExpDate.setText(month.getText().toString() + "/" + year.getText().toString());

        Button cancelButton = (Button) dialogLayout.findViewById(R.id.cancelButton);
        Button nextButton = (Button) dialogLayout.findViewById(R.id.nextButton);

        cancelButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        nextButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptTipConfirmation();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void listener(int cases, final EditText x) {

        switch (cases) {
            case 0: {
                x.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            x.setGravity(Gravity.LEFT);
                        } else {
                            x.setGravity(Gravity.CENTER);
                        }

                    }
                });
                break;
            }
        }
    }

    private boolean cardIsValid(String number) {
        creditCardType = cardType(number);
        if (creditCardType.isEmpty())
            return false;
        else if (creditCardType.equals("CUP")) {
            creditCardType = CREDITCARD_TYPE_DISCOVER;
            return true;
        } else {
            return luhnTest(number);
        }
    }

    // For validating the credit card number
    private boolean luhnTest(String number) {
        int s1 = 0, s2 = 0;
        String reverse = new StringBuffer(number).reverse().toString();
        for (int i = 0; i < reverse.length(); i++) {
            int digit = Character.digit(reverse.charAt(i), 10);
            if (i % 2 == 0) {// this is for odd digits, they are 1-indexed in
                // the algorithm
                s1 += digit;
            } else {// add 2 * digit for 0-4, add 2 * digit - 9 for 5-9
                s2 += 2 * digit;
                if (digit >= 5) {
                    s2 -= 9;
                }
            }
        }
        return (s1 + s2) % 10 == 0;

    }

    public static String cardType(String number) {
        String ccType = "";
        try {
            Long.parseLong(number);
        } catch (NumberFormatException e) {
            return "";
        }
        if (Integer.parseInt(number.substring(0, 6)) >= 622126
                && Integer.parseInt(number.substring(0, 6)) <= 622925) {
            ccType = CREDITCARD_TYPE_CUP;
        } else if (Integer.parseInt(number.substring(0, 6)) == 564182
                || Integer.parseInt(number.substring(0, 6)) == 633110) {
            ccType = CREDITCARD_TYPE_DISCOVER;
        } else {
            switch (Integer.parseInt(number.substring(0, 4))) {
                case 2014:
                case 2149:
                    ccType = CREDITCARD_TYPE_DINERS;
                    break;
                case 2131:
                case 1800:
                case 3528:
                case 3529:
                    ccType = CREDITCARD_TYPE_JCB;
                    break;
                case 6011:
                    ccType = CREDITCARD_TYPE_DISCOVER;
                    break;
                case 3095:
                    ccType = CREDITCARD_TYPE_DINERS;
                    break;
                case 6222:
                case 6223:
                case 6224:
                case 6225:
                case 6226:
                case 6227:
                case 6228:
                case 6282:
                case 6283:
                case 6284:
                case 6285:
                case 6286:
                case 6287:
                case 6288:
                    ccType = CREDITCARD_TYPE_CUP;
                    break;
                case 5018:
                case 5020:
                case 5038:
                case 6304:
                case 6759:
                case 6761:
                case 6763:
                    ccType = CREDITCARD_TYPE_MASTERCARD;
                    break;
                case 6333:
                    ccType = CREDITCARD_TYPE_VISA;
                    break;
                default: {
                    switch (Integer.parseInt(number.substring(0, 3))) {
                        case 300:
                        case 301:
                        case 302:
                        case 303:
                        case 304:
                        case 305:
                            ccType = CREDITCARD_TYPE_DINERS;
                            break;
                        case 353:
                        case 354:
                        case 355:
                        case 356:
                        case 357:
                        case 358:
                            ccType = CREDITCARD_TYPE_JCB;
                            break;
                        case 644:
                        case 645:
                        case 646:
                        case 647:
                        case 648:
                        case 649:
                            ccType = CREDITCARD_TYPE_DISCOVER;
                            break;
                        case 624:
                        case 625:
                        case 626:
                            ccType = CREDITCARD_TYPE_CUP;
                            break;
                        default: {
                            switch (Integer.parseInt(number.substring(0, 2))) {
                                case 34:
                                case 37:
                                    ccType = CREDITCARD_TYPE_AMEX;
                                    break;
                                case 36:
                                case 38:
                                case 39:
                                    ccType = CREDITCARD_TYPE_DINERS;
                                    break;
                                case 51:
                                case 52:
                                case 53:
                                case 54:
                                case 55:
                                    ccType = CREDITCARD_TYPE_MASTERCARD;
                                    break;
                                case 65:
                                    ccType = CREDITCARD_TYPE_DISCOVER;
                                    break;
                                default: {

                                    switch (Integer.parseInt(number.substring(0, 1))) {
                                        case 3:
                                            ccType = CREDITCARD_TYPE_JCB;
                                            break;
                                        case 5:
                                        case 6:
                                            ccType = CREDITCARD_TYPE_MASTERCARD;
                                            break;
                                        case 4:
                                        case 9:
                                            ccType = CREDITCARD_TYPE_VISA;
                                            break;
                                        default: {
                                        }
                                        break;
                                    }

                                }
                                break;
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }

        return ccType;
    }

    private void processStoreForward(String payment_xml, Payment payment) {
        if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen()) {
            _msrUsbSams.CloseTheDevice();
        }
        payment.payment_xml = payment_xml;
        payment.pay_uuid = getXmlValue(payment_xml, "app_id");

        // payment.getSetData("pay_resultcode", false,
        // parsedMap.get("pay_resultcode"));
        // payment.getSetData("pay_resultmessage",
        // false,parsedMap.get("pay_resultmessage"));
        // payment.getSetData("pay_transid", false,
        // parsedMap.get("CreditCardTransID"));
        // payment.getSetData("authcode", false,
        // parsedMap.get("AuthorizationCode"));
        // payment.getSetData("processed", false, "9");

        orientation = getResources().getConfiguration().orientation;
        global.orientation = orientation;

        if (isOpenInvoice && isMultiInvoice) {
            if (invPaymentList.size() > 0) {
                payment.inv_id = "";
                invPayHandler.insert(invPaymentList);
            }
        }

        StoredPayments_DB dbStoredPayments = new StoredPayments_DB(this);
        dbStoredPayments.insert(payment);
        // payHandler.insert(payment);

        if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
            if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
                OrdersHandler dbOrders = new OrdersHandler(this);
                dbOrders.updateOrderStoredFwd(payment.job_id, "1");
            }
            new printAsync().execute(false, payment);
        } else if (!isDebit) {
            Intent intent = new Intent(activity, DrawReceiptActivity.class);
            intent.putExtra("isFromPayment", true);
            startActivityForResult(intent, requestCode);
        } else {
            finishPaymentTransaction();
        }
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
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    private String _charge_xml;
    private boolean livePaymentRunning = false;

    private class processLivePaymentAsync extends AsyncTask<Object, String, Payment> {

        private HashMap<String, String> parsedMap = new HashMap<String, String>();
        private boolean wasProcessed = false;
        private boolean connectionFailed = false;
        private String errorMsg = getString(R.string.dlog_msg_no_internet_access);

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(getString(R.string.please_wait_message));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
            if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen()) {
                _msrUsbSams.CloseTheDevice();
            }
        }

        @Override
        protected Payment doInBackground(Object... params) {

            if (Global.isConnectedToInternet(activity) && !livePaymentRunning) {
                livePaymentRunning = true;

                Post httpClient = new Post();
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);
                _charge_xml = (String) params[0];

                try {
                    String xml = httpClient.postData(13, activity, _charge_xml);

                    if (xml.equals(Global.TIME_OUT) || xml.equals(Global.NOT_VALID_URL) || xml.isEmpty()) {
                        connectionFailed = true;
                        errorMsg = getString(R.string.dlog_msg_established_connection_failed);
                    } else {
                        InputSource inSource = new InputSource(new StringReader(xml));

                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        xr.setContentHandler(handler);
                        xr.parse(inSource);
                        parsedMap = handler.getData();
                        parsedMap = handler.getData();

                        if (parsedMap != null && parsedMap.size() > 0
                                && parsedMap.get("epayStatusCode").equals("APPROVED"))
                            wasProcessed = true;
                        else if (parsedMap != null && parsedMap.size() > 0) {
                            errorMsg = "statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage");
                        } else
                            errorMsg = xml;
                    }

                } catch (Exception e) {

                    connectionFailed = true;
                }
            }

            return (Payment) params[1];
        }

        @Override
        protected void onPostExecute(Payment payment) {
            myProgressDialog.dismiss();

            livePaymentRunning = false;
            if (wasProcessed) // payment processing succeeded
            {
                saveApprovedPayment(parsedMap, payment);
            } else // payment processing failed
            {
                if (connectionFailed) {
                    generateReverseXML(_charge_xml);
                }

                btnProcess.setEnabled(true);
                showErrorDlog(false, connectionFailed, errorMsg, payment);
            }
        }
    }

    private String _reverse_xml = "";

    private class processReverseAsync extends AsyncTask<Payment, Void, Payment> {

        private HashMap<String, String> parsedMap = new HashMap<String, String>();

        private boolean reverseWasProcessed = false;
        private boolean paymentWasApproved = false;
        private String errorMsg = getString(R.string.dlog_msg_no_internet_access);
        private boolean paymentWasDecline = false;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(getString(R.string.please_wait_message));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected Payment doInBackground(Payment... params) {

            if (Global.isConnectedToInternet(activity)) {
                Post httpClient = new Post();

                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);

                try {
                    String xml = httpClient.postData(13, activity, _reverse_xml);

                    if (xml.equals(Global.TIME_OUT) || xml.equals(Global.NOT_VALID_URL) || xml.isEmpty()) {
                        errorMsg = getString(R.string.dlog_msg_established_connection_failed);
                    } else {
                        InputSource inSource = new InputSource(new StringReader(xml));

                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        xr.setContentHandler(handler);
                        xr.parse(inSource);
                        parsedMap = handler.getData();

                        if (parsedMap != null && parsedMap.size() > 0
                                && (parsedMap.get("epayStatusCode").equals("APPROVED")))
                            reverseWasProcessed = true;
                        else if (parsedMap != null && parsedMap.get("epayStatusCode").equals("DECLINE")) {
                            reverseWasProcessed = true;
                            String _verify_payment_xml = _charge_xml.replaceAll("<action>.*?</action>", "<action>"
                                    + EMSPayGate_Default.getPaymentAction("CheckTransactionStatus") + "</action>");
                            xml = httpClient.postData(13, activity, _verify_payment_xml);
                            if (xml.equals(Global.TIME_OUT) || xml.equals(Global.NOT_VALID_URL)) {
                                errorMsg = getString(R.string.dlog_msg_established_connection_failed);
                            } else {
                                inSource = new InputSource(new StringReader(xml));
                                xr.parse(inSource);
                                parsedMap = handler.getData();
                                if (parsedMap != null) {
                                    if (parsedMap.get("epayStatusCode").equals("APPROVED")) {
                                        paymentWasApproved = true;
                                    } else if (parsedMap.get("epayStatusCode").equals("DECLINE")) {
                                        paymentWasDecline = true;
                                        errorMsg = "statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage");
                                    } else
                                        errorMsg = xml;
                                }
                            }
                        }
                    }

                } catch (Exception e) {

                    errorMsg = e.getMessage();
                }
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(Payment payment) {
            myProgressDialog.dismiss();
            if (reverseWasProcessed) {
                PaymentsXML_DB _paymentXml_DB = new PaymentsXML_DB(activity);
                _paymentXml_DB.deleteRow(_xml_app_id);
                if (paymentWasApproved) {
                    saveApprovedPayment(parsedMap, payment);
                } else {
                    if (paymentWasDecline) {
                        showErrorDlog(false, false, errorMsg, payment);
                    } else {
                        finish();
                    }
                }
            } else {
                finish();
            }
        }
    }

    private void saveApprovedPayment(HashMap<String, String> parsedMap, Payment payment) {
        if (walkerReader == null && myPref.getPrinterType() != Global.HANDPOINT && myPref.getPrinterType() != Global.ICMPEVO) {
            payment.pay_resultcode = parsedMap.get("pay_resultcode");
            payment.pay_resultmessage = parsedMap.get("pay_resultmessage");
            payment.pay_transid = parsedMap.get("CreditCardTransID");
            payment.authcode = parsedMap.get("AuthorizationCode");
            payment.processed = "9";
        } else {
            if (isRefund) {
                payment.is_refund = "1";
                payment.pay_type = "2";
            }
            payment.processed = "1";
            payment.pay_transid = cardInfoManager.transid;
            payment.authcode = cardInfoManager.authcode;
        }
        orientation = getResources().getConfiguration().orientation;
        global.orientation = orientation;

        if (isOpenInvoice && isMultiInvoice) {
            if (invPaymentList.size() > 0) {
                payment.inv_id = "";
                invPayHandler.insert(invPaymentList);
            }
        }
        payHandler.insert(payment);
        if (walkerReader == null) {
            if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
                new printAsync().execute(false, payment);
            } else if (!isDebit) {
                Intent intent = new Intent(activity, DrawReceiptActivity.class);
                intent.putExtra("isFromPayment", true);
                startActivityForResult(intent, requestCode);
            } else {
                finishPaymentTransaction();
            }
        } else {
            if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
                StoredPayments_DB dbStoredPayments = new StoredPayments_DB(this);
                Global.amountPaid = dbStoredPayments.updateSignaturePayment(payment.pay_uuid);

                OrdersHandler dbOrders = new OrdersHandler(this);
                dbOrders.updateOrderStoredFwd(payment.job_id, "1");
            } else {
                PaymentsHandler payHandler = new PaymentsHandler(this);
                Global.amountPaid = payHandler.updateSignaturePayment(extras.getString("pay_id"));
            }

            if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
                if (myPref.getPreferences(MyPreferences.pref_automatic_printing))
                    new printAsync().execute(false, payment);
                else
                    showPrintDlg(false, false, payment);
            } else
                finishPaymentTransaction();
        }
    }

    private String _xml_app_id = "";

    private void generateReverseXML(String _charge_xml) {

        int _action = 0;

        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xmlFactoryObject.newPullParser();
            parser.setInput(new StringReader(_charge_xml));

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
                            if (tag.equals("app_id")) {
                                _xml_app_id = parser.getText();
                                found = true;
                            } else if (tag.equals("action"))
                                _action = Integer.parseInt(parser.getText());
                        }
                        break;
                }
                event = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        _reverse_xml = _charge_xml.replaceAll("<action>.*?</action>",
                "<action>" + EMSPayGate_Default.getReverseAction(_action) + "</action>");

        PaymentsXML_DB _payment_xml = new PaymentsXML_DB(activity);
        HashMap<String, String> map = new HashMap<String, String>();

        map.put(PaymentsXML_DB.app_id, _xml_app_id);
        map.put(PaymentsXML_DB.payment_xml, _reverse_xml);

        _payment_xml.insert(map);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (global.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        if (resultCode == -1) {
            if (walkerReader == null) {
                if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
                    StoredPayments_DB dbStoredPayments = new StoredPayments_DB(this);
                    Global.amountPaid = dbStoredPayments.updateSignaturePayment(PaymentsHandler.getLastPaymentInserted().pay_uuid);

                    OrdersHandler dbOrders = new OrdersHandler(this);
                    dbOrders.updateOrderStoredFwd(PaymentsHandler.getLastPaymentInserted().job_id, "1");
                } else {
                    PaymentsHandler payHandler = new PaymentsHandler(this);
                    Global.amountPaid = payHandler.updateSignaturePayment(extras.getString("pay_id"));
                }

                if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
                    if (myPref.getPreferences(MyPreferences.pref_automatic_printing))
                        new printAsync().execute(false, PaymentsHandler.getLastPaymentInserted());
                    else
                        showPrintDlg(false, false, PaymentsHandler.getLastPaymentInserted());
                } else
                    finishPaymentTransaction();
            } else {
                PaymentsHandler payHandler = new PaymentsHandler(this);
                Global.amountPaid = payHandler.updateSignaturePayment(extras.getString("pay_id"));
                walkerReader.submitSignature();
            }
        }
    }

    private void finishPaymentTransaction() {
        // if(!myPref.getLastPayID().isEmpty())
        // myPref.setLastPayID("0");

        global.encodedImage = "";
        if (requestCode == Global.FROM_JOB_INVOICE || requestCode == Global.FROM_OPEN_INVOICES
                || requestCode == Global.FROM_JOB_SALES_RECEIPT)
            setResult(-2);
        else {
            Intent result = new Intent();

            result.putExtra("total_amount", Double.toString(Global
                    .formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(this.amountDueField))));
            setResult(-2, result);
        }

        finish();
    }

    private class printAsync extends AsyncTask<Object, String, Payment> {
        private boolean wasReprint = false;
        private boolean printingSuccessful = true;

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
        protected Payment doInBackground(Object... params) {
            Payment payment = (Payment) params[1];
            wasReprint = (Boolean) params[0];
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null) {
                printingSuccessful = Global.mainPrinterManager.currentDevice.printPaymentDetails(payment.pay_id, 1,
                        wasReprint, null);
            }
            return payment;
        }

        @Override
        protected void onPostExecute(Payment payment) {
            if (myProgressDialog.isShowing())
                myProgressDialog.dismiss();
            if (printingSuccessful) {
                if (!wasReprint && myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
                    showPrintDlg(true, false, payment);
                else {
                    finishPaymentTransaction();
                }
            } else {
                showPrintDlg(wasReprint, true, payment);
            }
        }
    }

    private void showPrintDlg(final boolean isReprint, boolean isRetry, final Payment payment) {
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
            if (isReprint)
                viewMsg.setText(R.string.dlog_msg_print_cust_copy);
            else
                viewMsg.setText(R.string.dlog_msg_want_to_print);
        }
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                new printAsync().execute(isReprint, payment);

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                finishPaymentTransaction();
            }
        });
        dlog.show();
    }

    private void showErrorDlog(final boolean isFromReverse, final boolean _connectionFailed, String msg, final Payment payment) {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_single_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_error);
        viewMsg.setText(msg);

        Button btnOk = (Button) dlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (isFromReverse) {
                    new processReverseAsync().execute(payment);
                } else {
                    if (_connectionFailed)
                        new processReverseAsync().execute(payment);
                    else
                        finish();
                }
            }
        });

        dlog.show();
    }

    @Override
    public void cardWasReadSuccessfully(boolean read, CreditCardInfo cardManager) {
        this.cardInfoManager = cardManager;
        if (myPref.getPrinterType() != Global.HANDPOINT && myPref.getPrinterType() != Global.ICMPEVO) {
            updateViewAfterSwipe(cardManager);
            if (uniMagReader != null && uniMagReader.readerIsConnected()) {
                uniMagReader.startReading();
            } else if (walkerReader != null) {
                processPayment();
            } else if (magtekReader == null && Global.btSwiper == null && _msrUsbSams == null
                    && Global.mainPrinterManager != null)
                Global.mainPrinterManager.currentDevice.loadCardReader(callBack, isDebit);
        } else {
            if (myProgressDialog != null && myProgressDialog.isShowing()) {
                myProgressDialog.dismiss();
            }
            if (read) {
                processPayment();
            } else {
                String errorMsg = getString(R.string.coundnot_proceess_payment);
                if (cardManager.getResultMessage() != null && !cardManager.getResultMessage().isEmpty()) {
                    errorMsg += "\n\r" + cardManager.getResultMessage();
                }
                Global.showPrompt(activity, R.string.payment, errorMsg);
            }
        }
    }

    @Override
    public void readerConnectedSuccessfully(boolean didConnect) {
        tvStatusMSR.setText(R.string.status_connected);
        if (didConnect) {
            cardReaderConnected = true;
            if (uniMagReader != null && uniMagReader.readerIsConnected())
                uniMagReader.startReading();
            cardSwipe.setChecked(true);
        } else {
            cardReaderConnected = false;
            if (cardSwipe.isChecked())
                cardSwipe.setChecked(false);
        }
    }

    public void updateViewAfterSwipe(CreditCardInfo creditCardInfo) {
        cardInfoManager = creditCardInfo;
        wasReadFromReader = true;
        month.setText(cardInfoManager.getCardExpMonth());
        String formatedYear = cardInfoManager.getCardExpYear();
        cardInfoManager.setCardExpYear(formatedYear);
        year.setText(formatedYear);
        ownersName.setText(cardInfoManager.getCardOwnerName());
        cardNum.setText(cardInfoManager.getCardNumAESEncrypted());

        creditCardType = cardInfoManager.getCardType();
        scrollView.fullScroll(ScrollView.FOCUS_UP);
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
            updateViewAfterSwipe(this.cardInfoManager);
        }
    }

    @Override
    public void scannerWasRead(String data) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exactAmountBut:
                double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDueField));
                grandTotalAmount = amountToBePaid + amountToTip;
                amountPaidField.setText(amountDueField.getText().toString());
                break;
            case R.id.processButton:
                if (myPref.getPrinterType() == Global.HANDPOINT || myPref.getPrinterType() == Global.ICMPEVO) {
                    boolean valid = validateProcessPayment();
                    if (!valid) {
                        String errorMsg = getString(R.string.card_validation_error);
                        Global.showPrompt(activity, R.string.validation_failed, errorMsg);
                    } else {
                        myProgressDialog = new ProgressDialog(activity);
                        myProgressDialog.setMessage(activity.getString(R.string.swipe_insert_card));
                        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        myProgressDialog.setCancelable(true);
                        myProgressDialog.show();
                        if (isRefund) {
                            Payment p = new Payment(activity);
                            p.pay_amount = NumberUtils.cleanCurrencyFormatedNumber(amountPaidField);
                            Global.mainPrinterManager.currentDevice.refund(p);
                        } else {
                            Payment p = new Payment(activity);
                            p.pay_amount = NumberUtils.cleanCurrencyFormatedNumber(amountPaidField);
                            Global.mainPrinterManager.currentDevice.salePayment(p);
                        }
                    }
                } else if (walkerReader == null) {
                    boolean valid = validateProcessPayment();
                    if (!valid) {
                        String errorMsg = getString(R.string.card_validation_error);
                        Global.showPrompt(activity, R.string.validation_failed, errorMsg);
                    } else {
                        btnProcess.setEnabled(false);
                        if (myPref.getPreferences(MyPreferences.pref_show_confirmation_screen)) {
                            promptAmountConfirmation();
                        } else {
                            if (!extras.getBoolean("histinvoices") || (isOpenInvoice && !isMultiInvoice))
                                processPayment();
                            else
                                processMultiInvoicePayment();
                        }
                    }


                } else {
                    new ProcessWalkerAsync().execute();
                }
                break;
            case R.id.tipAmountBut:
                promptTipConfirmation();
                break;
        }
    }

    private boolean validateProcessPayment() {
        String errorMsg = getString(R.string.card_validation_error);
        year.setBackgroundResource(android.R.drawable.edit_text);
        cardNum.setBackgroundResource(android.R.drawable.edit_text);
        month.setBackgroundResource(android.R.drawable.edit_text);
        amountPaidField.setBackgroundResource(android.R.drawable.edit_text);
        boolean error = false;
        if (myPref.getPrinterType() != Global.HANDPOINT && myPref.getPrinterType() != Global.ICMPEVO) {
            if (cardNum.getText().toString().isEmpty() || cardNum.getText().toString().length() < 14
                    || (!wasReadFromReader && !cardIsValid(cardNum.getText().toString()))) {
                cardNum.setBackgroundResource(R.drawable.edittext_wrong_input);
                error = true;
            } else {
                cardNum.setBackgroundResource(R.drawable.edittext_border);
            }

            int myMonth = -1;
            int myYear = -1;
            if (!month.getText().toString().isEmpty()) {
                myMonth = Integer.parseInt(month.getText().toString());
            }
            if (!year.getText().toString().isEmpty()) {
                myYear = Integer.parseInt(year.getText().toString());
            }

            int curMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int curYear = Calendar.getInstance().get(Calendar.YEAR);
            if (myYear <= curYear) {
                if (myYear < curYear) {
                    year.setBackgroundResource(R.drawable.edittext_wrong_input);
                    error = true;
                } else {
                    year.setBackgroundResource(R.drawable.edittext_border);
                }
                if ((myMonth < curMonth && myYear != -1) || myMonth < 1 || myMonth > 12
                        || (myMonth < curMonth && myYear == curYear)) {
                    month.setBackgroundResource(R.drawable.edittext_wrong_input);
                    error = true;
                } else {
                    month.setBackgroundResource(R.drawable.edittext_border);
                }

            } else {
                if (myMonth <= 0 || myMonth > 12) {
                    month.setBackgroundResource(R.drawable.edittext_wrong_input);
                    error = true;
                } else {
                    month.setBackgroundResource(R.drawable.edittext_border);
                }
            }
        }
        if (!isFromMainMenu) {
            double enteredAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
            double actualAmount = Double.parseDouble(extras.getString("amount"));

            if (enteredAmount > actualAmount) {
                errorMsg = getString(R.string.card_overpaid_error);
                amountPaidField.setBackgroundResource(R.drawable.edittext_wrong_input);
                error = true;
            } else if (enteredAmount <= 0) {
                amountPaidField.setBackgroundResource(R.drawable.edittext_wrong_input);
                errorMsg = getString(R.string.error_wrong_amount);
                error = true;
            }

        } else {
            double enteredAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));

            if (enteredAmount <= 0) {
                errorMsg = getString(R.string.error_wrong_amount);
                error = true;
            }
        }

        if (!error && isRefund && requireTransID) {
            if (transIDField.getText().toString().isEmpty()) {
                error = true;
                transIDField.setBackgroundResource(R.drawable.edittext_wrong_input);
            } else {
                transIDField.setBackgroundResource(R.drawable.edittext_border);
            }
        }


        return !error;
    }


    private class ProcessHanpointAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (Global.mainPrinterManager.currentDevice != null) {
                Payment p = new Payment(activity);
                p.pay_amount = NumberUtils.cleanCurrencyFormatedNumber(amountPaidField);
                Global.mainPrinterManager.currentDevice.salePayment(p);
            }
            return null;
        }
    }

    private class ProcessWalkerAsync extends AsyncTask<Void, Void, Void> {
        private ProcessWalkerAsync myTask;
        private double enteredAmount;

        @Override
        protected void onPreExecute() {
            myTask = this;
            enteredAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));

            myProgressDialog = new ProgressDialog(activity);
            if (walkerReader.deviceConnected())
                myProgressDialog.setMessage(getString(R.string.swipe_insert_card));
            else
                myProgressDialog.setMessage(getString(R.string.please_wait_message));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            if (myProgressDialog.isShowing())
                myProgressDialog.dismiss();

            myProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getString(R.string.button_cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myTask.cancel(true);
                            walkerReader.isReadingCard = false;
                            myProgressDialog.dismiss();
                        }
                    });

            myProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            cardInfoManager.dueAmount = BigDecimal.valueOf(enteredAmount);
            walkerReader.startReading(cardInfoManager, myProgressDialog);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            myProgressDialog.dismiss();
            if (walkerReader.failedProcessing)
                Toast.makeText(activity, "Error", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void startSignature() {
        Intent intent = new Intent(activity, DrawReceiptActivity.class);
        intent.putExtra("isFromPayment", true);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void nfcWasRead(String nfcUID) {

    }

}
