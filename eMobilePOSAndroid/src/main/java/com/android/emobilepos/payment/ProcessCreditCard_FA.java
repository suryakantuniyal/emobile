package com.android.emobilepos.payment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.ShiftDAO;
import com.android.dao.StoredPaymentsDAO;
import com.android.database.CustomersHandler;
import com.android.database.InvoicePaymentsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.PaymentsXML_DB;
import com.android.database.TaxesHandler;
import com.android.emobilepos.DrawReceiptActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.GroupTax;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.StoreAndForward;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NetworkUtils;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.android.support.textwatcher.CreditCardTextWatcher;
import com.android.support.textwatcher.TextWatcherCallback;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import drivers.EMSIDTechUSB;
import drivers.EMSMagtekAudioCardReader;
import drivers.EMSRover;
import drivers.EMSUniMagDriver;
import interfaces.EMSCallBack;
import main.EMSDeviceManager;
import util.json.UIUtils;

public class ProcessCreditCard_FA extends BaseFragmentActivityActionBar implements EMSCallBack, OnClickListener, TextWatcherCallback, CompoundButton.OnCheckedChangeListener {

    public static final String CREDITCARD_TYPE_JCB = "JCB", CREDITCARD_TYPE_CUP = "CUP",
            CREDITCARD_TYPE_DISCOVER = "Discover", CREDITCARD_TYPE_VISA = "Visa", CREDITCARD_TYPE_DINERS = "DinersClub",
            CREDITCARD_TYPE_MASTERCARD = "MasterCard", CREDITCARD_TYPE_AMEX = "AmericanExpress";
    private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
    private static ProgressDialog myProgressDialog;
    private String ourIntentAction = "";
    private CheckBox cardSwipe = null;
    private boolean cardReaderConnected = false;
    private EditText month, year, cardNum, ownersName, secCode, zipCode;
    private HashMap<String, String> reverseXMLMap;
    private String creditCardType = "";
    private MyPreferences myPref;
    private EditText hiddenField;
    private Global global;
    private Activity activity;
    private boolean hasBeenCreated = false;
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
    private EditText subtotal, tax1, tax2;
    private List<GroupTax> groupTaxRate;
    private boolean isMultiInvoice = false, isOpenInvoice = false;
    private String[] inv_id_array, txnID_array;
    private double[] balance_array;
    private List<String[]> invPaymentList;
    private EMSUniMagDriver uniMagReader;
    private EMSMagtekAudioCardReader magtekReader;
    private EMSRover roverReader;
    private String custidkey = "";
    private TextView tvStatusMSR;
    private double amountToTip = 0;
    private double grandTotalAmount = 0;
    private TextView dlogGrandTotal;
    private EMSCallBack callBack;
    private CreditCardInfo cardInfoManager;
    private Bundle extras;
    private boolean isDebit = false;
    private Button btnProcess;
    private ScrollView scrollView;
    private EMSIDTechUSB _msrUsbSams;
    private boolean isEverpay = false;
    private String _charge_xml;
    private boolean livePaymentRunning = false;
    private String orderSubTotal;

    public static String getCardType(String number) {
        String ccType = "";
        boolean isMasked;
        try {
            if (TextUtils.isEmpty(number) || number.length() < 4) {
                return "";
            } else {
                if (!TextUtils.isDigitsOnly(number.substring(0, 4))) {
                    return "";
                }
                isMasked = true;
            }
        } catch (NumberFormatException ex) {
            return "";
        }
        if (!isMasked && Integer.parseInt(number.substring(0, 6)) >= 622126
                && Integer.parseInt(number.substring(0, 6)) <= 622925) {
            ccType = CREDITCARD_TYPE_CUP;
        } else if (!isMasked && (Integer.parseInt(number.substring(0, 6)) == 564182
                || Integer.parseInt(number.substring(0, 6)) == 633110)) {
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
                                case 22:
                                case 23:
                                case 24:
                                case 25:
                                case 26:
                                case 27:
                                case 51:
                                case 52:
                                case 53:
                                case 54:
                                case 55:
                                    if ((Integer.parseInt(number.substring(0, 4)) >= 2221 &&
                                            Integer.parseInt(number.substring(0, 4)) <= 2720) ||
                                            (Integer.parseInt(number.substring(0, 4)) >= 5100 &&
                                                    Integer.parseInt(number.substring(0, 4)) <= 5599)) {
                                        ccType = CREDITCARD_TYPE_MASTERCARD;
                                    }
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

    public static HashMap<String, String> generateReverseXML(Activity activity, String chargeXml) {

        int action = 0;

        String xmlAppId = "";
        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xmlFactoryObject.newPullParser();
            parser.setInput(new StringReader(chargeXml));

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
                                xmlAppId = parser.getText();
                                found = true;
                            } else if (tag.equals("action"))
                                action = Integer.parseInt(parser.getText());
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

        String reverseXml = chargeXml.replaceAll("<action>.*?</action>",
                "<action>" + EMSPayGate_Default.getReverseAction(EMSPayGate_Default.EAction.toAction(action)) + "</action>");

        PaymentsXML_DB _payment_xml = new PaymentsXML_DB(activity);
        HashMap<String, String> map = new HashMap<String, String>();

        map.put(PaymentsXML_DB.app_id, xmlAppId);
        map.put(PaymentsXML_DB.payment_xml, reverseXml);

        _payment_xml.insert(map);
        return map;
    }

    public static int getCreditLogo(String cardName) {
        if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_AMEX)
                || cardName.trim().contains("amex") || cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_AMEX)) {
            return R.drawable.americanexpress;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_DISCOVER)
                || cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_DISCOVER)) {
            return R.drawable.discover;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_MASTERCARD) ||
                cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_MASTERCARD)) {
            return R.drawable.mastercard;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_VISA) ||
                cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_VISA)) {
            return R.drawable.visa;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_JCB) ||
                cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_JCB)) {
            return R.drawable.debitcard;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_CUP) ||
                cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_CUP)) {
            return R.drawable.debitcard;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_DINERS) ||
                cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_DINERS)) {
            return R.drawable.debitcard;
        } else {
            return R.drawable.debitcard;
        }
    }

    public static String getCreditName(String cardName) {
        if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_AMEX)
                || cardName.trim().contains("amex") || cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_AMEX)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_AMEX;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_DISCOVER)
                || cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_DISCOVER)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_DISCOVER;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_MASTERCARD) ||
                cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_MASTERCARD)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_MASTERCARD;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_VISA) ||
                cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_VISA)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_VISA;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_JCB) ||
                cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_JCB)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_JCB;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_CUP) ||
                cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_CUP)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_CUP;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_DINERS) ||
                cardName.trim().contains(ProcessCreditCard_FA.CREDITCARD_TYPE_DINERS)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_DINERS;
        } else {
            return "";
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callBack = this;
        setContentView(R.layout.procress_card_layout);
        activity = this;
        global = (Global) getApplication();
        myPref = new MyPreferences(activity);
        AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
        groupTaxRate = new TaxesHandler(this).getGroupTaxRate(assignEmployee.getTaxDefault());

        Global.isEncryptSwipe = true;
        cardInfoManager = new CreditCardInfo();
        scrollView = findViewById(R.id.scrollView);
        reference = findViewById(R.id.referenceNumber);
        TextView headerTitle = findViewById(R.id.HeaderTitle);
        tvStatusMSR = findViewById(R.id.tvStatusMSR);
        cardSwipe = findViewById(R.id.checkBox1);
        extras = this.getIntent().getExtras();
        String paymentMethodType = extras.getString("paymentmethod_type");
        if (paymentMethodType == null) {
            paymentMethodType = CREDITCARD_TYPE_VISA;
        }
        isEverpay = (paymentMethodType.equalsIgnoreCase("Everpay"));

        isDebit = extras.getBoolean("isDebit", false);
        requireTransID = extras.getBoolean("requireTransID", false);

        orderSubTotal = extras.getString("subTotal", "0");
        if (extras.getBoolean("salespayment", false)) {
            headerTitle.setText(getString(R.string.card_payment_title));
            isFromMainMenu = true;
        } else if (extras.getBoolean("salesreceipt", false)) {
            headerTitle.setText(getString(R.string.card_payment_title));
            requestCode = Global.FROM_JOB_SALES_RECEIPT;
        } else if (extras.getBoolean("salesrefund", false)) {
            isRefund = true;
            isFromMainMenu = TextUtils.isEmpty(extras.getString("amount"))
                    || Double.parseDouble(extras.getString("amount")) == 0;
            headerTitle.setText(getString(R.string.card_refund_title));
        } else if (extras.getBoolean("histinvoices", false)) {
            headerTitle.setText(getString(R.string.card_payment_title));
            requestCode = Global.FROM_OPEN_INVOICES;
        } else if (extras.getBoolean("salesinvoice", false)) {
            headerTitle.setText(R.string.card_invoice);
        }

        custidkey = extras.getString("custidkey");
        if (custidkey == null)
            custidkey = "";

        hiddenField = findViewById(R.id.hiddenField);
        zipCode = findViewById(R.id.processCardZipCode);
        zipCode.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        month = findViewById(R.id.monthEdit);
        month.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        listener(0, month);

        year = findViewById(R.id.yearEdit);
        year.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        listener(0, year);

        authIDField = findViewById(R.id.cardAuthIDField);
        transIDField = findViewById(R.id.cardTransIDField);
        subtotal = findViewById(R.id.subtotalCardAmount);
        tax1 = findViewById(R.id.tax1CardAmount);
        tax2 = findViewById(R.id.tax2CardAmount);
        TextView tax1Lbl = findViewById(R.id.tax1CreditCardLbl);
        TextView tax2Lbl = findViewById(R.id.tax2CreditCardLbl);

        tax1.setText(Global.getCurrencyFormat(extras.getString("Tax1_amount")));
        tax2.setText(Global.getCurrencyFormat(extras.getString("Tax2_amount")));
        List<OrderProduct> orderProducts = global.order == null
                ? new ArrayList<OrderProduct>() : global.order.getOrderProducts();
        double subtotalDbl = 0;
        for (OrderProduct products : orderProducts) {
            subtotalDbl += products.getItemSubtotalCalculated().doubleValue();
        }
        subtotal.setText(Global.formatDoubleToCurrency(subtotalDbl));
        this.amountDueField = findViewById(R.id.processCardAmount);
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
        this.amountPaidField = findViewById(R.id.processCardAmountPaid);
        this.amountPaidField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        this.amountPaidField.addTextChangedListener(getTextWatcher(amountPaidField));

        this.amountPaidField.setOnFocusChangeListener(getFocusListener(this.amountPaidField));
        if (myPref.isPrefillTotalAmount())
            this.amountPaidField.setText(
                    Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));
        else
            this.amountPaidField.setText("");

        Button exactBut = findViewById(R.id.exactAmountBut);
        exactBut.setOnClickListener(this);

        cardNum = findViewById(R.id.cardNumEdit);
        cardNum.setInputType(InputType.TYPE_CLASS_NUMBER);
        cardNum.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        cardNum.setTransformationMethod(PasswordTransformationMethod.getInstance());

        ownersName = findViewById(R.id.nameOnCardEdit);
        ownersName.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        if (extras.getBoolean("histinvoices", false)) {
            isMultiInvoice = extras.getBoolean("isMultipleInvoice", false);
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

        secCode = findViewById(R.id.processCardSeccode);
        secCode.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        if (isDebit) {
            zipCode.setVisibility(View.GONE);
            secCode.setVisibility(View.GONE);

        }

        btnProcess = findViewById(R.id.processButton);
        btnProcess.setOnClickListener(this);

        Button tipButton = findViewById(R.id.tipAmountBut);
        tipButton.setOnClickListener(this);

        this.tipAmount = findViewById(R.id.processCardTip);
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

        phoneNumberField = findViewById(R.id.processCardPhone);
        customerEmailField = findViewById(R.id.processCardEmail);

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
        if (myPref.getSwiperType() == Global.NOMAD || myPref.getSwiperType() == Global.HANDPOINT || myPref.getSwiperType() == Global.ICMPEVO || isEverpay) {
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
        findViewById(R.id.accountInformationTextView).setVisibility(View.GONE);
        findViewById(R.id.expirationDateTextView).setVisibility(View.GONE);
        CheckBox swiperCheckBox = findViewById(R.id.checkBox1);
        swiperCheckBox.setClickable(true);
        swiperCheckBox.setOnCheckedChangeListener(this);
    }

    private void enableManualCreditCard() {
        boolean allow = myPref.getPreferences(MyPreferences.pref_allow_manual_credit_card, true);
        cardNum.setEnabled(allow);
        secCode.setEnabled(allow);
        zipCode.setEnabled(allow);
        month.setEnabled(allow);
        year.setEnabled(allow);
        cardNum.setVisibility(View.VISIBLE);
        secCode.setVisibility(View.VISIBLE);
        zipCode.setVisibility(View.VISIBLE);
        month.setVisibility(View.VISIBLE);
        year.setVisibility(View.VISIBLE);
        authIDField.setVisibility(View.VISIBLE);
        transIDField.setVisibility(View.VISIBLE);
        findViewById(R.id.accountInformationTextView).setVisibility(View.VISIBLE);
        findViewById(R.id.expirationDateTextView).setVisibility(View.VISIBLE);
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
            if (!TextUtils.isEmpty(customerInfo.get("cust_name")))
                ownersName.setText(customerInfo.get("cust_name"));
            if (!TextUtils.isEmpty(customerInfo.get("cust_phone")))
                phoneNumberField.setText(customerInfo.get("cust_phone"));
            if (!TextUtils.isEmpty(customerInfo.get("cust_email")))
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
                NumberUtils.parseInputedCurrency(s, editText);
                //parseInputedCurrency(s, type_id);
            }
        };
    }

    private OnFocusChangeListener getFocusListener(final EditText field) {

        return new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
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
                switch (_audio_reader_type) {
                    case Global.AUDIO_MSR_UNIMAG:
                        uniMagReader = new EMSUniMagDriver();
                        uniMagReader.initializeReader(activity);
                        break;
                    case Global.AUDIO_MSR_MAGTEK:
                        magtekReader = new EMSMagtekAudioCardReader(activity);
                        new Thread(new Runnable() {
                            public void run() {
                                magtekReader.connectMagtek(true, callBack);
                            }
                        }).start();
                        break;
                    case Global.AUDIO_MSR_ROVER:
                        roverReader = new EMSRover();
                        roverReader.initializeReader(activity, isDebit);
                        break;
                }
            }
        } else {
            int _swiper_type = myPref.getSwiperType();
            int _printer_type = myPref.getPrinterType();
            int _sled_type = myPref.sledType(true, -2);
            if (_swiper_type != -1 && Global.btSwiper != null && Global.btSwiper.getCurrentDevice() != null
                    && !cardReaderConnected) {
                Global.btSwiper.getCurrentDevice().loadCardReader(callBack, isDebit);
            } else if (_sled_type != -1 && Global.btSled != null && Global.btSled.getCurrentDevice() != null
                    && !cardReaderConnected) {
                Global.btSled.getCurrentDevice().loadCardReader(callBack, isDebit);
            } else if (_printer_type != -1 && Global.deviceHasMSR(_printer_type)) {
                if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null
                        && !cardReaderConnected)
                    Global.mainPrinterManager.getCurrentDevice().loadCardReader(callBack, isDebit);
            }
        }

        if (myPref.isET1(true, false) || myPref.isMC40(true, false)) {
            ourIntentAction = getString(R.string.intentAction3);
            Intent i = getIntent();
            handleDecodeData(i);
            cardSwipe.setChecked(true);
        } else if (myPref.isSam4s() || myPref.isPAT100() || EMSIDTechUSB.isUSBConnected(this)) {
            cardSwipe.setChecked(true);
            _msrUsbSams = new EMSIDTechUSB(activity, callBack);
            if (_msrUsbSams.OpenDevice())
                _msrUsbSams.StartReadingThread();
        } else if ((myPref.isESY13P1() || MyPreferences.isTeamSable()) && Global.btSwiper == null) {
            if (Global.embededMSR != null && Global.embededMSR.getCurrentDevice() != null) {
                Global.embededMSR.getCurrentDevice().loadCardReader(callBack, isDebit);
                cardSwipe.setChecked(true);
            }
        } else if (myPref.isEM100() || myPref.isEM70() || myPref.isOT310() || myPref.isKDC425()) {
            cardSwipe.setChecked(true);
        } else if (myPref.isPAT215() && Global.btSwiper == null) {
            if (Global.embededMSR != null && Global.embededMSR.getCurrentDevice() != null) {
                Global.embededMSR.getCurrentDevice().loadCardReader(callBack, isDebit);
                cardSwipe.setChecked(false);
            }
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
            cardInfoManager.setCardUnEncryptedSecCode(secCode.getText().toString());
            cardInfoManager.setCardNumUnencrypted(cardNum.getText().toString());
        }
    }

    private void processPayment() {

        if (myPref.getSwiperType() != Global.NOMAD && myPref.getSwiperType() != Global.HANDPOINT && myPref.getSwiperType() != Global.ICMPEVO && !isEverpay)
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

        if (myPref.isUseClerks()) {
            clerkId = myPref.getClerkID();
        } else if (ShiftDAO.isShiftOpen()) {
            clerkId = String.valueOf(ShiftDAO.getOpenShift().getClerkId());
        }

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
        amountToTip = Global.getBigDecimalNum(String.valueOf(amountToTip), 2).doubleValue();
        Payment payment = new Payment(activity, extras.getString("pay_id"), extras.getString("cust_id"), invoiceId, jobId, clerkId, custidkey, extras.getString("paymethod_id"),
                actualAmount, amountTender,
                cardInfoManager.getCardOwnerName(), reference.getText().toString(), phoneNumberField.getText().toString(),
                customerEmailField.getText().toString(), amountToTip, taxAmnt1, taxAmnt2, taxName1, taxName2,
                isRef, paymentType, creditCardType, cardInfoManager.getCardNumAESEncrypted(), cardInfoManager.getCardLast4(),
                cardInfoManager.getCardExpMonth(), cardInfoManager.getCardExpYear(),
                zipCode.getText().toString(), cardInfoManager.getCardEncryptedSecCode(), cardInfoManager.getEncryptedAESTrack1(),
                cardInfoManager.getEncryptedAESTrack2(), transactionId, authcode);
        if (cardInfoManager.getEmvContainer() != null && cardInfoManager.getEmvContainer().getHandpointResponse() != null) {
            payment.setCard_type(getCreditName(cardInfoManager.getEmvContainer().getHandpointResponse().getCardSchemeName()));
        }

        payment.setEmvContainer(cardInfoManager.getEmvContainer());
        if (myPref.getSwiperType() != Global.NOMAD && myPref.getSwiperType() != Global.HANDPOINT && myPref.getSwiperType() != Global.ICMPEVO && !isEverpay) {
            EMSPayGate_Default payGate = new EMSPayGate_Default(activity, payment);
            String generatedURL;

            if (!isRefund) {
                paymentType = "0";
                payment.setPay_type(paymentType);
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
                payment.setIs_refund(isRef);
                payment.setPay_type(paymentType);
                payment.setPay_transid(transactionId);
                payment.setAuthcode(authcode);
                if (isDebit)
                    generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ReturnDebitAction, wasReadFromReader, creditCardType,
                            cardInfoManager);
                else
                    generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ReturnCreditCardAction, wasReadFromReader,
                            creditCardType, cardInfoManager);

            }
            if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
                processStoreForward(generatedURL, payment);
            } else {
                new processLivePaymentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, generatedURL, payment);
            }
        } else {
            if (!isRefund) {
                payment.setPay_type("0");
            } else {
                isRef = "1";
                transactionId = transIDField.getText().toString();
                authcode = authIDField.getText().toString();
                payment.setIs_refund(isRef);
                payment.setPay_type("2");
                payment.setPay_transid(transactionId);
                payment.setAuthcode(authcode);
            }
            saveApprovedPayment(null, payment);
        }
    }

    private void processMultiInvoicePayment() {
        populateCardInfo();
        invPayHandler = new InvoicePaymentsHandler(activity);
        invPaymentList = new ArrayList<>();
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
        payHandler = new PaymentsHandler(activity);

        String clerkId = null;
        if (myPref.isUseClerks()) {
            clerkId = myPref.getClerkID();
        } else if (ShiftDAO.isShiftOpen()) {
            clerkId = String.valueOf(ShiftDAO.getOpenShift().getClerkId());
        }

        double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
        double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDueField));
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
            payment.setPay_type("0");
            if (isDebit)
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeDebitAction, wasReadFromReader, creditCardType,
                        cardInfoManager);
            else
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeCreditCardAction, wasReadFromReader, creditCardType,
                        cardInfoManager);
        } else {
            payment.setIs_refund("1");
            payment.setPay_type("2");
            payment.setPay_transid(authIDField.getText().toString());
            payment.setAuthcode(transIDField.getText().toString());
            if (isDebit)
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ReturnDebitAction, wasReadFromReader, creditCardType,
                        cardInfoManager);
            else
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ReturnCreditCardAction, wasReadFromReader, creditCardType,
                        cardInfoManager);
        }

        new processLivePaymentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, generatedURL, payment);

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
        else if (Global.btSwiper != null && Global.btSwiper.getCurrentDevice() != null)
            Global.btSwiper.getCurrentDevice().releaseCardReader();
        else if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null)
            Global.mainPrinterManager.getCurrentDevice().releaseCardReader();
        if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen()) {
            _msrUsbSams.CloseTheDevice();
        }
        if (myPref.getSwiperType() == Global.NOMAD) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                Global.mainPrinterManager.getCurrentDevice().releaseCardReader();
            }
        }
    }

    private void promptTipConfirmation() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogLayout = inflater.inflate(R.layout.tip_dialog_layout, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.DialogLargeArea);
        final AlertDialog dialog = builder.create();
        dialog.setView(dialogLayout, 0, 0, 0, 0);
        dialog.setInverseBackgroundForced(true);
        dialog.setCancelable(false);
        final double subTotal;
        if (isFromMainMenu) {
            subTotal = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDueField));
        } else {
            if (!TextUtils.isEmpty(orderSubTotal) && Double.parseDouble(orderSubTotal) > 0) {
                subTotal = Double.parseDouble(orderSubTotal);
            } else {
                subTotal = Math.abs(Double.parseDouble(global.order.ord_subtotal));
            }
        }

        double amountToBePaid = Global
                .formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
        grandTotalAmount = amountToBePaid + amountToTip;
        final TextView totalAmountView = dialogLayout.findViewById(R.id.totalAmountView);
        totalAmountView.setText(String.format(Locale.getDefault(), getString(R.string.total_plus_tip),
                Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(0)));
        Button tenPercent = dialogLayout.findViewById(R.id.tenPercent);
        Button fifteenPercent = dialogLayout.findViewById(R.id.fifteenPercent);
        Button twentyPercent = dialogLayout.findViewById(R.id.twentyPercent);
        dlogGrandTotal = dialogLayout.findViewById(R.id.grandTotalView);

        dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));

        promptTipField = dialogLayout.findViewById(R.id.otherTipAmountField);
        promptTipField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        promptTipField.clearFocus();
        promptTipField.setText("");

        Button cancelTip = dialogLayout.findViewById(R.id.cancelTipButton);
        Button saveTip = dialogLayout.findViewById(R.id.acceptTipButton);
        Button noneButton = dialogLayout.findViewById(R.id.noneButton);

        promptTipField.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(s.toString())) > 0) {
                    double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
                    amountToTip = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(s.toString()));
                    grandTotalAmount = subTotal + amountToTip;
                    dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                    totalAmountView.setText(String.format(Locale.getDefault(), getString(R.string.total_plus_tip),
                            Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
                }
                NumberUtils.parseInputedCurrency(s, promptTipField);
            }
        });

        promptTipField.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
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
                amountToTip = (float) (subTotal * 0.1);
                grandTotalAmount = subTotal + amountToTip;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                promptTipField.setText("");
                totalAmountView.setText(String.format(Locale.getDefault(), getString(R.string.total_plus_tip),
                        Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
            }
        });

        fifteenPercent.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                double amountToBePaid = Global
                        .formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
                amountToTip = (float) (subTotal * 0.15);
                grandTotalAmount = subTotal + amountToTip;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                promptTipField.setText("");
                totalAmountView.setText(String.format(Locale.getDefault(), getString(R.string.total_plus_tip),
                        Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
            }
        });

        twentyPercent.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                double amountToBePaid = Global
                        .formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
                amountToTip = (float) (subTotal * 0.2);
                grandTotalAmount = subTotal + amountToTip;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                promptTipField.setText("");
                totalAmountView.setText(String.format(Locale.getDefault(), getString(R.string.total_plus_tip),
                        Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
            }
        });

        noneButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
                amountToTip = 0;
                grandTotalAmount = subTotal;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                totalAmountView.setText(String.format(Locale.getDefault(), getString(R.string.total_plus_tip),
                        Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
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

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog dialog = builder.create();
        dialog.setView(dialogLayout, 0, 0, 0, 0);
        dialog.setInverseBackgroundForced(true);
        dialog.setCancelable(false);
        dlogGrandTotal = dialogLayout.findViewById(R.id.confirmTotalView);
        TextView dlogCardType = dialogLayout.findViewById(R.id.confirmCardType);
        TextView dlogCardExpDate = dialogLayout.findViewById(R.id.confirmExpDate);
        TextView dlogCardNum = dialogLayout.findViewById(R.id.confirmCardNumber);

        grandTotalAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
        dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
        dlogCardType.setText(creditCardType);
        int size = cardNum.getText().toString().length();
        String last4Digits = "";
        if (size > 0)
            last4Digits = (String) cardNum.getText().toString().subSequence(size - 4, size);
        dlogCardNum.setText(String.format("*%s", last4Digits));
        dlogCardExpDate.setText(month.getText().toString() + "/" + year.getText().toString());

        Button cancelButton = dialogLayout.findViewById(R.id.cancelButton);
        Button nextButton = dialogLayout.findViewById(R.id.nextButton);

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
        creditCardType = getCardType(number);
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

    private void processStoreForward(String payment_xml, Payment payment) {
        if (_msrUsbSams != null && _msrUsbSams.isDeviceOpen()) {
            _msrUsbSams.CloseTheDevice();
        }
        payment.setPayment_xml(payment_xml);
        payment.setPay_uuid(getXmlValue(payment_xml, "app_id"));

        orientation = getResources().getConfiguration().orientation;
        global.orientation = orientation;

        if (isOpenInvoice && isMultiInvoice) {
            if (invPaymentList.size() > 0) {
                payment.setInv_id("");
                invPayHandler.insert(invPaymentList);
            }
        }

        StoredPaymentsDAO.insert(activity, payment, StoreAndForward.PaymentType.CREDIT_CARD);
        // payHandler.insert(payment);

        if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
            if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
                OrdersHandler dbOrders = new OrdersHandler(this);
                dbOrders.updateOrderStoredFwd(payment.getJob_id(), "1");
            }
            new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false, payment);
        } else if (!isDebit) {
            Intent intent = new Intent(activity, DrawReceiptActivity.class);
            intent.putExtra("isFromPayment", true);
            intent.putExtra("card_type", payment.getCard_type());
            intent.putExtra("pay_amount", payment.getPay_amount());

            startActivityForResult(intent, requestCode);
        } else {
            finishPaymentTransaction(payment);
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

    private void openEverpayApp(Payment payment) {
//        evertec.mobileapp
//        Intent intent = getPackageManager().getLaunchIntentForPackage("evertec.mobileapp");
        Intent intent = new Intent("evertec.mobileapp.action.PERFORM_TRANSACTION");
//        Intent intent = new Intent();
//        intent.setComponent(new ComponentName("evertec.mobileapp", "com.evertec.base.action.PERFORM_TRANSACTION"));

        intent.putExtra("transactionType", isRefund ? 2 : 1);
        intent.putExtra("invoiceNumber", "invoice");
        intent.putExtra("clerkNumber", "clerk");
        intent.putExtra("customerNumber", "customer");
        intent.putExtra("saleAmount", Global.formatNumToLocale(grandTotalAmount));
        startActivityForResult(intent, 202);
    }

    private void saveApprovedPayment(HashMap<String, String> parsedMap, Payment payment) {
        if (myPref.getSwiperType() != Global.NOMAD && myPref.getSwiperType() != Global.HANDPOINT
                && myPref.getSwiperType() != Global.ICMPEVO && !isEverpay) {
            payment.setPay_resultcode(parsedMap.get("pay_resultcode"));
            payment.setPay_resultmessage(parsedMap.get("pay_resultmessage"));
            payment.setPay_transid(parsedMap.get("CreditCardTransID"));
            payment.setAuthcode(parsedMap.get("AuthorizationCode"));
            if (parsedMap.containsKey("AuthorizedAmount")) {
                payment.setPay_amount(parsedMap.get("AuthorizedAmount"));
            }
            payment.setProcessed("9");
        } else {
            if (isRefund) {
                payment.setIs_refund("1");
                payment.setPay_type("2");
            }
            payment.setProcessed("1");
            payment.setPay_transid(cardInfoManager.transid);
            payment.setAuthcode(cardInfoManager.authcode);
        }
        orientation = getResources().getConfiguration().orientation;
        global.orientation = orientation;

        if (isOpenInvoice && isMultiInvoice) {
            if (invPaymentList.size() > 0) {
                payment.setInv_id("");
                invPayHandler.insert(invPaymentList);
            }
        }
        payHandler.insert(payment);
        if (myPref.getSwiperType() != Global.NOMAD && myPref.getSwiperType() != Global.HANDPOINT
                && myPref.getSwiperType() != Global.ICMPEVO && !isEverpay) {
            if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
                new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false, payment);
            } else if (!isDebit) {

                Intent intent = new Intent(activity, DrawReceiptActivity.class);
                intent.putExtra("isFromPayment", true);
                intent.putExtra("card_type", payment.getCard_type());
                intent.putExtra("pay_amount", payment.getPay_amount());
                startActivityForResult(intent, requestCode);
            } else {
                finishPaymentTransaction(payment);
            }
        } else {
            if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
                Global.amountPaid = StoredPaymentsDAO.getStoreAndForward(payment.getPay_uuid()).getPayment().getPay_amount();
                Message msg = Global.handler.obtainMessage();
                msg.what = 0;
                msg.obj = PaymentsHandler.getLastPaymentInserted();
                Global.handler.sendMessage(msg);
                OrdersHandler dbOrders = new OrdersHandler(this);
                dbOrders.updateOrderStoredFwd(payment.getJob_id(), "1");
            } else {
                PaymentsHandler payHandler = new PaymentsHandler(this);
                Global.amountPaid = payHandler.updateSignaturePayment(extras.getString("pay_id"), global.encodedImage);
            }

            if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
                if (myPref.getPreferences(MyPreferences.pref_automatic_printing))
                    new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false, payment);
                else
                    showPrintDlg(false, false, payment);
            } else
                finishPaymentTransaction(payment);
        }
    }

    private void redetectMiuraPrinter() {
        EMSDeviceManager edm = new EMSDeviceManager();
        Global.mainPrinterManager = edm.getManager();
        Global.mainPrinterManager.loadMultiDriver(activity, myPref.getPrinterType(), 0, true, "", "");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (global.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        if (requestCode == 202) {
            redetectMiuraPrinter();
            final String cardNumber = data == null ? "" : data.getStringExtra("cardNumber");
            final String cardHolderName = data == null ? "" : data.getStringExtra("cardHolderName");
            final String responseCode = data == null ? "" : data.getStringExtra("responseCode");
            final String processorResponseCode = data == null ? "" : data.getStringExtra("processorResponseCode");
            final String responseText = data == null ? "" : data.getStringExtra("responseText");
            final String avsResult = data == null ? "" : data.getStringExtra("avsResult");
            final String cvvResult = data == null ? "" : data.getStringExtra("cvvResult");
            final String approvalCode = data == null ? "" : data.getStringExtra("approvalCode");
            final String transactionId = data == null ? "" : data.getStringExtra("transactionId");
            final String gatewayReferenceNumber = data == null ? "" : data.getStringExtra("gatewayReferenceNumber");
            final String processorReferenceNumber = data == null ? "" : data.getStringExtra("processorReferenceNumber");
            final String authorizedAmount = data == null ? "" : data.getStringExtra("authorizedAmount");
            final String tipAmount = data == null ? "" : data.getStringExtra("tipAmount");
            final String cashBackAmount = data == null ? "" : data.getStringExtra("cashBackAmount");
            String result = "cardNumber: " + cardNumber + "\r\n" +
                    "cardHolderName: " + cardHolderName + "\r\n" +
                    "responseCode: " + responseCode + "\r\n" +
                    "processorResponseCode: " + processorResponseCode + "\r\n" +
                    "responseText: " + responseText + "\r\n" +
                    "avsResult: " + avsResult + "\r\n" +
                    "cvvResult: " + cvvResult + "\r\n" +
                    "transactionId: " + transactionId + "\r\n" +
                    "approvalCode: " + approvalCode + "\r\n" +
                    "gatewayReferenceNumber: " + gatewayReferenceNumber + "\r\n" +
                    "processorReferenceNumber: " + processorReferenceNumber + "\r\n" +
                    "authorizedAmount: " + authorizedAmount + "\r\n" +
                    "tipAmount: " + tipAmount + "\r\n" +
                    "cashBackAmount: " + cashBackAmount;

            if (resultCode == RESULT_OK) {
                CreditCardInfo creditCardInfo = new CreditCardInfo();
                BigDecimal totalDec = new BigDecimal("0.00");

                if (!authorizedAmount.isEmpty()) {
                    totalDec = new BigDecimal(authorizedAmount);
                }

                creditCardInfo.setOriginalTotalAmount(totalDec.toString());
                creditCardInfo.setWasSwiped(true);
                creditCardInfo.authcode = approvalCode;
                creditCardInfo.transid = transactionId;
                cardWasReadSuccessfully(responseText.equalsIgnoreCase("APPROVED"), creditCardInfo);

//                new AlertDialog.Builder(this)
//                        .setTitle("Transaction Response")
//                        .setMessage(result)
//                        .setPositiveButton("OK", null)
//                        .show();
            } else if (resultCode == RESULT_CANCELED) {
                btnProcess.setEnabled(true);
                new AlertDialog.Builder(this)
                        .setTitle("EVERPay")
                        .setMessage("The transaction was cancelled.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        } else if (resultCode == -1) {
            if (myPref.getSwiperType() != Global.NOMAD) {
                if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
//                    Global.amountPaid = StoredPaymentsDAO.updateSignaturePayment(PaymentsHandler.getLastPaymentInserted().getPay_uuid(), global.encodedImage);
                    Payment payment = StoredPaymentsDAO.getStoreAndForward(PaymentsHandler.getLastPaymentInserted().getPay_uuid()).getPayment();
                    Global.amountPaid = payment.getPay_amount();
                    PaymentsHandler.getLastPaymentInserted().setPay_signature(global.encodedImage);
                    Message msg = Global.handler.obtainMessage();
                    msg.what = 0;
                    msg.obj = PaymentsHandler.getLastPaymentInserted();
                    Global.handler.sendMessage(msg);
                    OrdersHandler dbOrders = new OrdersHandler(this);
                    dbOrders.updateOrderStoredFwd(PaymentsHandler.getLastPaymentInserted().getJob_id(), "1");
                } else {
                    PaymentsHandler payHandler = new PaymentsHandler(this);
                    Global.amountPaid = payHandler.updateSignaturePayment(extras.getString("pay_id"), global.encodedImage);
                }

                if (myPref.getPreferences(MyPreferences.pref_enable_printing)) {
                    if (myPref.getPreferences(MyPreferences.pref_automatic_printing))
                        new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false, PaymentsHandler.getLastPaymentInserted());
                    else
                        showPrintDlg(false, false, PaymentsHandler.getLastPaymentInserted());
                } else
                    finishPaymentTransaction(PaymentsHandler.getLastPaymentInserted());
            } else {
                PaymentsHandler payHandler = new PaymentsHandler(this);
                Global.amountPaid = payHandler.updateSignaturePayment(extras.getString("pay_id"), global.encodedImage);
                Global.btSwiper.getCurrentDevice().submitSignature();
            }
        }
    }

    private void finishPaymentTransaction(Payment payment) {
        // if(!myPref.getLastPayID().isEmpty())
        // myPref.setLastPayID("0");

        global.encodedImage = "";
        if (requestCode == Global.FROM_JOB_INVOICE || requestCode == Global.FROM_OPEN_INVOICES
                || requestCode == Global.FROM_JOB_SALES_RECEIPT)
            setResult(-2);
        else {
            Intent result = new Intent();
            result.putExtra("emvcontainer", new Gson().toJson(payment.getEmvContainer(), EMVContainer.class));
            result.putExtra("total_amount", Double.toString(Global
                    .formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(this.amountDueField))));
            setResult(-2, result);
        }

        finish();
    }

    private void showPrintDlg(final boolean isReprint, boolean isRetry, final Payment payment) {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
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

        Button btnYes = dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = dlog.findViewById(R.id.btnDlogRight);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                new PrintAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, isReprint, payment);
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                finishPaymentTransaction(payment);
            }
        });
        dlog.show();
    }

    private void showErrorDlog(final boolean isFromReverse, final boolean _connectionFailed, String msg, final Payment payment) {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_single_layout);

        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_error);
        viewMsg.setText(msg);

        Button btnOk = dlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (isFromReverse) {
                    new processReverseAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, payment);
                } else {
                    if (_connectionFailed)
                        new processReverseAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, payment);
                    else
                        finish();
                }
            }
        });

        dlog.show();
    }

    @Override
    public void cardWasReadSuccessfully(boolean read, CreditCardInfo cardManager) {
        if (isDebit) {
            cardManager.setCardType("DebitCard");
        }
        this.cardInfoManager = cardManager;
        if (myPref.getSwiperType() != Global.NOMAD && myPref.getSwiperType() != Global.HANDPOINT && myPref.getSwiperType() != Global.ICMPEVO && !isEverpay) {
            updateViewAfterSwipe(cardManager);
            if (uniMagReader != null && uniMagReader.readerIsConnected()) {
                uniMagReader.startReading();
//            } else if (myPref.getSwiperType() == Global.NOMAD) {
//                processPayment();
            } else if (magtekReader == null && Global.btSwiper == null && _msrUsbSams == null
                    && Global.mainPrinterManager != null)
                Global.mainPrinterManager.getCurrentDevice().loadCardReader(callBack, isDebit);
        } else {
            if (myProgressDialog != null) {
                myProgressDialog.dismiss();
            }
            if (read) {
                processPayment();
            } else {
                String errorMsg = getString(R.string.coundnot_proceess_payment);
                if (cardManager.getResultMessage() != null && !cardManager.getResultMessage().isEmpty()) {
                    errorMsg += "\n\r" + cardManager.getResultMessage();
                }
                if (!Global.isActivityDestroyed(activity)) {
                    Global.showPrompt(activity, R.string.payment, errorMsg);
                }
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
        if (!TextUtils.isEmpty(cardInfoManager.getCardNumAESEncrypted()))
            cardNum.setText(cardInfoManager.getCardNumAESEncrypted());
        else if (!TextUtils.isEmpty(cardInfoManager.getEncryptedBlock())) {
            cardNum.setText(cardInfoManager.getEncryptedBlock());
        }
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
        if (UIUtils.singleOnClick(v)) {
            switch (v.getId()) {
                case R.id.exactAmountBut:
                    double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDueField));
                    grandTotalAmount = amountToBePaid + amountToTip;
                    amountPaidField.setText(amountDueField.getText().toString());
                    break;
                case R.id.processButton:
                    if (myPref.getSwiperType() == Global.NOMAD || myPref.getSwiperType() == Global.HANDPOINT
                            || myPref.getSwiperType() == Global.ICMPEVO || isEverpay) {
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
                                p.setPay_amount(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));

                                if (isEverpay)
                                    openEverpayApp(p);
                                else {
                                    populateCardInfo();
                                    Global.btSwiper.getCurrentDevice().loadCardReader(callBack, false);
                                    Global.btSwiper.getCurrentDevice().refund(p, cardInfoManager);
                                }
                            } else {
                                Payment p = new Payment(activity);
                                p.setPay_amount(NumberUtils.cleanCurrencyFormatedNumber(amountPaidField));
                                p.setTipAmount(String.valueOf(Global.getBigDecimalNum(NumberUtils.cleanCurrencyFormatedNumber(tipAmount), 2)));
                                if (Global.btSwiper != null && Global.btSwiper.getCurrentDevice() != null) {
                                    populateCardInfo();
                                    Global.btSwiper.getCurrentDevice().loadCardReader(callBack, false);
                                    Global.btSwiper.getCurrentDevice().salePayment(p, cardInfoManager);
                                } else if (isEverpay) {
                                    openEverpayApp(p);
                                }
                            }
                        }
                    } else {
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
                    }
//                else {
//                    new ProcessWalkerAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                }
                    break;
                case R.id.tipAmountBut:
                    promptTipConfirmation();
                    break;
            }
        }
    }

    private boolean validateProcessPayment() {
        String errorMsg = getString(R.string.card_validation_error);
        year.setBackgroundResource(android.R.drawable.edit_text);
        cardNum.setBackgroundResource(android.R.drawable.edit_text);
        month.setBackgroundResource(android.R.drawable.edit_text);
        amountPaidField.setBackgroundResource(android.R.drawable.edit_text);
        boolean error = false;
        if (myPref.getSwiperType() != Global.NOMAD && myPref.getSwiperType() != Global.HANDPOINT && myPref.getSwiperType() != Global.ICMPEVO && !isEverpay) {
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
            double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDueField));

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

    @Override
    public void startSignature() {
        if (myProgressDialog != null && myProgressDialog.isShowing()) {
            myProgressDialog.setMessage(activity.getString(R.string.processing_payment_msg));
        }
        Intent intent = new Intent(activity, DrawReceiptActivity.class);
        intent.putExtra("isFromPayment", true);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void nfcWasRead(String nfcUID) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!b) {
            enableManualCreditCard();
        } else {
            setHandopintUIFields();
        }
    }


    public enum PAYMENT_GIFT_CARDS {
        GIFTCARDS, LOYALTYCARD, REWARD
    }

    private class processLivePaymentAsync extends AsyncTask<Object, String, Payment> {

        private HashMap<String, String> parsedMap = new HashMap<>();
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
//            if (true) {
//                openEverpayApp((Payment) params[1]);
//            } else
            if (NetworkUtils.isConnectedToInternet(activity) && !livePaymentRunning) {
                livePaymentRunning = true;

                Post httpClient = new Post(activity);
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler();
                _charge_xml = (String) params[0];

                try {
                    String xml = httpClient.postData(13, _charge_xml);

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
                    Crashlytics.logException(e);
                }
            }

            return (Payment) params[1];
        }

        @Override
        protected void onPostExecute(Payment payment) {
            Global.dismissDialog(ProcessCreditCard_FA.this, myProgressDialog);

            livePaymentRunning = false;
            if (wasProcessed) // payment processing succeeded
            {
                saveApprovedPayment(parsedMap, payment);
            } else // payment processing failed
            {
                if (connectionFailed) {
                    reverseXMLMap = generateReverseXML(activity, _charge_xml);
                }

                btnProcess.setEnabled(true);
                showErrorDlog(false, connectionFailed, errorMsg, payment);
            }
        }
    }

    private class processReverseAsync extends AsyncTask<Payment, Void, Payment> {

        private HashMap<String, String> parsedMap = new HashMap<>();

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

            if (NetworkUtils.isConnectedToInternet(activity)) {
                Post httpClient = new Post(activity);

                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler();

                try {
                    String reverseXml = "";
                    String xml = httpClient.postData(13, reverseXml);

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
                            xml = httpClient.postData(13, _verify_payment_xml);
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
                    Crashlytics.logException(e);
                    errorMsg = e.getMessage();
                }
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(Payment payment) {
            Global.dismissDialog(ProcessCreditCard_FA.this, myProgressDialog);
            String xmlAppId = reverseXMLMap.get(PaymentsXML_DB.app_id);

            if (reverseWasProcessed) {
                PaymentsXML_DB _paymentXml_DB = new PaymentsXML_DB(activity);
                _paymentXml_DB.deleteRow(xmlAppId);
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

    private class PrintAsync extends AsyncTask<Object, String, Payment> {
        private boolean wasReprint = false;
        private boolean printingSuccessful = true;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(getString(R.string.printing_message));
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
            try {
                if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                    printingSuccessful = Global.mainPrinterManager.getCurrentDevice().printPaymentDetails(payment.getPay_id(), 1,
                            wasReprint, payment.getEmvContainer());
                }
            } catch (Exception e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
            return payment;
        }

        @Override
        protected void onPostExecute(Payment payment) {
            Global.dismissDialog(ProcessCreditCard_FA.this, myProgressDialog);
            if (printingSuccessful) {
                if (!wasReprint && myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
                    showPrintDlg(true, false, payment);
                else {
                    finishPaymentTransaction(payment);
                }
            } else {
                showPrintDlg(wasReprint, true, payment);
            }
        }
    }
}
