package com.android.emobilepos.payment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.ShiftDAO;
import com.android.database.CustomersHandler;
import com.android.database.InvoicePaymentsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.GroupTax;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Device;
import com.android.emobilepos.models.realms.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCheckHandler;
import com.android.support.DeviceUtils;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.crashlytics.android.Crashlytics;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import main.EMSDeviceManager;
import util.json.UIUtils;

public class ProcessCheck_FA extends AbstractPaymentFA implements OnCheckedChangeListener, OnClickListener {

    private Global global;
    private Activity activity;
    private boolean hasBeenCreated = false;
    private final int CHECK_NAME = 0, CHECK_EMAIL = 1, CHECK_PHONE = 2, CHECK_AMOUNT = 3, CHECK_AMOUNT_PAID = 4, CHECK_REFERENCE = 5, CHECK_ACCOUNT = 6,
            CHECK_ROUTING = 7, CHECK_NUMBER = 8, CHECK_CITY = 9, CHECK_STATE = 10, CHECK_ZIPCODE = 11, COMMENTS = 12, CHECK_ADDRESS = 13, CHECK_DL_NUMBER = 14, CHECK_DL_STATE = 15, CHECK_DL_DOB = 16;

    private boolean timedOut = false;
    private boolean isFromSalesReceipt = false;
    private EditText[] field;

    private boolean isMultiInvoice = false, isOpenInvoice = false;
    private String accountType = "Savings";
    private String checkType = "Personal";
    private String inv_id;
    private MyPreferences myPref;

    private ProgressDialog myProgressDialog;
    private Payment payment;
    private PaymentsHandler payHandler;
    private InvoicePaymentsHandler invPayHandler;

    private String[] inv_id_array, txnID_array;
    private double[] balance_array;
    private List<String[]> invPaymentList;

    private boolean isRefund = false;
    private boolean isLivePayment = false;
    private String custidkey = "";
    private HashMap<String, String> customerInfo;
    private RadioGroup radioGroupCheckType, radioGroupAddressType;
    private final int INTENT_CAPTURE_CHECK = 100;
    private Bundle extras;
    private boolean checkWasCapture = false;
    private double amountTender = 0, actualAmount = 0;
    private Button btnProcess;
    private TextView tvCheckChange;
    private EditText subtotal, tax1, tax2, amountField;//,tipAmount,promptTipField
    private List<GroupTax> groupTaxRate;
    private AssignEmployee assignEmployee;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extras = this.getIntent().getExtras();
        activity = this;
        assignEmployee = AssignEmployeeDAO.getAssignEmployee();

        myPref = new MyPreferences(activity);
        String custTaxCode;

        if (myPref.isCustSelected()) {
            custTaxCode = myPref.getCustTaxCode();
        } else {
            custTaxCode = assignEmployee.getTaxDefault();
        }

        if (myPref.getPreferences(MyPreferences.pref_process_check_online)) {
            isLivePayment = true;
            setContentView(R.layout.process_live_check_layout);
        } else {
            setContentView(R.layout.process_local_check_layout);
        }

        subtotal = findViewById(R.id.subtotalCashEdit);
        tax1 = findViewById(R.id.tax1CashEdit);
        tax2 = findViewById(R.id.tax2CashEdit);
        TextView tax1Lbl = findViewById(R.id.tax1CashLbl);
        TextView tax2Lbl = findViewById(R.id.tax2CashLbl);
        groupTaxRate = new TaxesHandler(this).getGroupTaxRate(custTaxCode);
        ProcessCash_FA.setTaxLabels(groupTaxRate, tax1Lbl, tax2Lbl);
        this.amountField = findViewById(R.id.checkAmount);

        field = new EditText[]{findViewById(R.id.checkName), findViewById(R.id.checkEmail),
                findViewById(R.id.checkPhone), findViewById(R.id.checkAmount),
                findViewById(R.id.checkAmountPaid), findViewById(R.id.checkInvoice),
                findViewById(R.id.checkAccount), findViewById(R.id.checkRouting),
                findViewById(R.id.checkNumber), findViewById(R.id.checkCity), findViewById(R.id.checkState),
                findViewById(R.id.checkZipcode), findViewById(R.id.checkComment), findViewById(R.id.checkAddress),
                findViewById(R.id.checkDLNumber), findViewById(R.id.checkDLState), findViewById(R.id.checkDOBYear)};


        if (isLivePayment) {
            ImageButton btnCaptureCheck = findViewById(R.id.btnCheckCapture);
            btnCaptureCheck.setOnClickListener(this);
            btnCaptureCheck.setOnTouchListener(Global.opaqueImageOnClick());
            radioGroupCheckType = findViewById(R.id.radioGroupCheckType);
            radioGroupAddressType = findViewById(R.id.radioGroupAddressType);
            radioGroupCheckType.setOnCheckedChangeListener(this);
            radioGroupAddressType.setOnCheckedChangeListener(this);
        }


        global = (Global) getApplication();

        hasBeenCreated = true;

        TextView headerTitle = findViewById(R.id.HeaderTitle);
        tvCheckChange = findViewById(R.id.changeCheckText);


        if (!Global.getValidString(extras.getString("cust_id")).isEmpty()) {
            CustomersHandler handler2 = new CustomersHandler(activity);
            customerInfo = handler2.getCustomerMap(extras.getString("cust_id"));
            if (customerInfo != null) {
                if (!customerInfo.get("cust_name").isEmpty())
                    field[CHECK_NAME].setText(customerInfo.get("cust_name"));
                if (!customerInfo.get("cust_phone").isEmpty())
                    field[CHECK_PHONE].setText(customerInfo.get("cust_phone"));
                if (!customerInfo.get("cust_email").isEmpty())
                    field[CHECK_EMAIL].setText(customerInfo.get("cust_email"));
            }

        }

        if (!myPref.isSkipEmailPhone()) {
            field[CHECK_EMAIL].setText(extras.getString("order_email", ""));
            field[CHECK_PHONE].setText(extras.getString("order_phone", ""));
        }


        if (extras.getBoolean("salespayment") || extras.getBoolean("salesreceipt")) {
            headerTitle.setText(getString(R.string.check_payment_title));
        } else if (extras.getBoolean("salesrefund")) {
            headerTitle.setText(getString(R.string.check_refund_title));
            isRefund = true;
        } else if (extras.getBoolean("histinvoices")) {
            headerTitle.setText(getString(R.string.check_payment_title));
        } else if (extras.getBoolean("salesinvoice")) {
            headerTitle.setText("Check Invoice");
        }

        this.field[this.CHECK_AMOUNT].setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));
        field[CHECK_AMOUNT_PAID].setText(Global.formatDoubleToCurrency(0));

        isFromSalesReceipt = extras.getBoolean("isFromSalesReceipt");
        boolean isFromMainMenu = extras.getBoolean("isFromMainMenu");

        if (!isFromMainMenu || Global.isIvuLoto) {
            this.field[this.CHECK_AMOUNT].setEnabled(false);
        }


        Button exactBut = findViewById(R.id.exactAmountBut);
        btnProcess = findViewById(R.id.processCheckBut);
        exactBut.setOnClickListener(this);
        btnProcess.setOnClickListener(this);

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


        custidkey = extras.getString("custidkey");
        if (custidkey == null)
            custidkey = "";


        field[CHECK_AMOUNT_PAID].setText(Global.formatDoubleToCurrency(0.00));
        field[CHECK_AMOUNT_PAID].setSelection(5);

        field[CHECK_AMOUNT_PAID].setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.hasFocus()) {
                    int lent = field[CHECK_AMOUNT_PAID].getText().length();
                    Selection.setSelection(field[CHECK_AMOUNT_PAID].getText(), lent);
                }
            }
        });

        field[CHECK_AMOUNT_PAID].addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (!field[CHECK_AMOUNT_PAID].getText().toString().isEmpty())
                    recalculateChange();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NumberUtils.parseInputedCurrency(s, field[CHECK_AMOUNT_PAID]);
            }
        });

        if (!Global.isIvuLoto || isFromSalesReceipt) {
            findViewById(R.id.ivuposRow1).setVisibility(View.GONE);
            findViewById(R.id.ivuposRow2).setVisibility(View.GONE);
            findViewById(R.id.ivuposRow3).setVisibility(View.GONE);
        } else {
            setIVUPOSFieldListeners();
        }

        hasBeenCreated = true;
    }

    private void setIVUPOSFieldListeners() {
        subtotal.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.hasFocus()) {
                    Selection.setSelection(subtotal.getText(), subtotal.getText().length());
                }

            }
        });
        tax1.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.hasFocus()) {
                    Selection.setSelection(tax1.getText(), tax1.getText().length());
                }

            }
        });
        tax2.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.hasFocus()) {
                    Selection.setSelection(tax2.getText(), tax2.getText().length());
                }

            }
        });
        subtotal.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                NumberUtils.parseInputedCurrency(s, subtotal);
                if (!isFromSalesReceipt) {
                    ProcessCash_FA.calculateTaxes(groupTaxRate, subtotal, tax1, tax2);
                    ProcessCash_FA.calculateAmountDue(subtotal, tax1, tax2, amountField);
                }
                recalculateChange();

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NumberUtils.parseInputedCurrency(s, subtotal);
            }
        });
        tax1.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                ProcessCash_FA.calculateAmountDue(subtotal, tax1, tax2, amountField);
                recalculateChange();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NumberUtils.parseInputedCurrency(s, tax1);
            }
        });
        tax2.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                ProcessCash_FA.calculateAmountDue(subtotal, tax1, tax2, amountField);
                recalculateChange();

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NumberUtils.parseInputedCurrency(s, tax2);
            }
        });
    }


    private void recalculateChange() {

        double totAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(field[CHECK_AMOUNT]));
        double totalPaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(field[CHECK_AMOUNT_PAID]));

        if (totalPaid > totAmount) {
            double tempTotal = Math.abs(totAmount - totalPaid);
            tvCheckChange.setText(Global.getCurrencyFormat(Global.formatNumToLocale(tempTotal)));
        } else {
            tvCheckChange.setText(Global.formatDoubleToCurrency(0.00));
        }

    }


    private boolean validInput() {
        String check_amount_paid = field[CHECK_AMOUNT_PAID].getText().toString();
        String check_name = field[CHECK_NAME].getText().toString();
        if (isLivePayment) {
            String check_acct_num = field[CHECK_ACCOUNT].getText().toString();
            String check_routing = field[CHECK_ROUTING].getText().toString();

            if (!check_acct_num.isEmpty() && !check_routing.isEmpty() && !check_name.isEmpty() && !check_amount_paid.isEmpty()) {
                field[CHECK_ACCOUNT].setBackgroundResource(android.R.drawable.edit_text);
                field[CHECK_ROUTING].setBackgroundResource(android.R.drawable.edit_text);
                field[CHECK_NAME].setBackgroundResource(android.R.drawable.edit_text);
                field[CHECK_AMOUNT_PAID].setBackgroundResource(android.R.drawable.edit_text);
                return true;
            } else {
                if (check_acct_num.isEmpty())
                    field[CHECK_ACCOUNT].setBackgroundResource(R.drawable.edittext_wrong_input);
                else
                    field[CHECK_ACCOUNT].setBackgroundResource(android.R.drawable.edit_text);
                if (check_routing.isEmpty())
                    field[CHECK_ROUTING].setBackgroundResource(R.drawable.edittext_wrong_input);
                else
                    field[CHECK_ROUTING].setBackgroundResource(android.R.drawable.edit_text);
                if (check_name.isEmpty())
                    field[CHECK_NAME].setBackgroundResource(R.drawable.edittext_wrong_input);
                else
                    field[CHECK_NAME].setBackgroundResource(android.R.drawable.edit_text);
                return false;
            }
        } else {
            String check_check_num = field[CHECK_NUMBER].getText().toString();
            if (!check_check_num.isEmpty() && !check_name.isEmpty() && !check_amount_paid.isEmpty()) {
                field[CHECK_NUMBER].setBackgroundResource(android.R.drawable.edit_text);
                field[CHECK_NAME].setBackgroundResource(android.R.drawable.edit_text);
                field[CHECK_AMOUNT_PAID].setBackgroundResource(android.R.drawable.edit_text);
                return true;
            } else {
                if (check_check_num.isEmpty())
                    field[CHECK_NUMBER].setBackgroundResource(R.drawable.edittext_wrong_input);
                else
                    field[CHECK_NUMBER].setBackgroundResource(android.R.drawable.edit_text);

                if (check_name.isEmpty())
                    field[CHECK_NAME].setBackgroundResource(R.drawable.edittext_wrong_input);
                else
                    field[CHECK_NAME].setBackgroundResource(android.R.drawable.edit_text);

                if (check_amount_paid.isEmpty())
                    field[CHECK_AMOUNT_PAID].setBackgroundResource(R.drawable.edittext_wrong_input);
                else
                    field[CHECK_AMOUNT_PAID].setBackgroundResource(android.R.drawable.edit_text);
                return false;
            }
        }
    }


    private void processPayment() {
        MyPreferences myPref = new MyPreferences(activity);
        actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(field[this.CHECK_AMOUNT]));
        amountTender = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(field[CHECK_AMOUNT_PAID]));
        payHandler = new PaymentsHandler(activity);
        payment = new Payment(activity);
        payment.setAmountTender(amountTender);
        payment.setPay_id(extras.getString("pay_id"));

        payment.setEmp_id(String.valueOf(assignEmployee.getEmpId()));

        if (!extras.getBoolean("histinvoices")) {
            payment.setJob_id(inv_id);
        } else {
            payment.setInv_id(inv_id);
        }

        payment.setCust_id(extras.getString("cust_id"));
        payment.setCustidkey(custidkey);


        if (myPref.isUseClerks()) {
            payment.setClerk_id(myPref.getClerkID());
        } else if (ShiftDAO.isShiftOpen()) {
            payment.setClerk_id(String.valueOf(ShiftDAO.getOpenShift().getClerkId()));
        }


        payment.setRef_num(field[CHECK_REFERENCE].getText().toString());
        payment.setPaymethod_id(extras.getString("paymethod_id"));

        Global.amountPaid = Double.toString(amountTender);

        payment.setPay_dueamount(Double.toString(amountTender));

        if (amountTender > actualAmount)
            payment.setPay_amount(Double.toString(actualAmount));
        else
            payment.setPay_amount(Double.toString(amountTender));


        payment.setPay_name(field[CHECK_NAME].getText().toString());
        payment.setPay_phone(field[CHECK_PHONE].getText().toString());
        payment.setPay_email(field[CHECK_EMAIL].getText().toString());
        payment.setPay_check(this.field[CHECK_NUMBER].getText().toString());
        payment.setPay_comment(field[COMMENTS].getText().toString());

        Location location = Global.getCurrLocation(activity, false);
        payment.setPay_latitude(String.valueOf(location.getLatitude()));
        payment.setPay_longitude(String.valueOf(location.getLongitude()));


        if (Global.isIvuLoto) {
            payment.setIvuLottoNumber(extras.getString("IvuLottoNumber"));
            payment.setIvuLottoDrawDate(extras.getString("IvuLottoDrawDate"));
            payment.setIvuLottoQR(Global.base64QRCode(extras.getString("IvuLottoNumber"), extras.getString("IvuLottoDrawDate")));


            if (!TextUtils.isEmpty(extras.getString("Tax1_amount"))) {
                payment.setTax1_amount(extras.getString("Tax1_amount"));
                payment.setTax1_name(extras.getString("Tax1_name"));

                payment.setTax2_amount(extras.getString("Tax2_amount"));
                payment.setTax2_name(extras.getString("Tax2_name"));
            } else {
                BigDecimal tempRate;
                double tempPayAmount = Global.formatNumFromLocale(Global.amountPaid);
                tempRate = new BigDecimal(tempPayAmount * 0.06).setScale(2, BigDecimal.ROUND_UP);
                payment.setTax1_amount(tempRate.toPlainString());
                payment.setTax1_name("Estatal");

                tempRate = new BigDecimal(tempPayAmount * 0.01).setScale(2, BigDecimal.ROUND_UP);
                payment.setTax2_amount(tempRate.toPlainString());
                payment.setTax2_name("Municipal");
            }
        }

        payment.setCard_type("Check");

        if (extras.getBoolean("salesrefund", false)) {
            payment.setIs_refund("1");
            payment.setPay_type("2");
        } else
            payment.setPay_type("0");

        if (!isLivePayment) {
            payment.setProcessed("1");
            payHandler.insert(payment);

            if (!myPref.getLastPayID().isEmpty())
                myPref.setLastPayID("0");


            if (extras.getBoolean("histinvoices") || extras.getBoolean("salesinvoice") || isFromSalesReceipt)
                setResult(-2);
            else if (extras.getBoolean("salespayment") || extras.getBoolean("salesrefund")) {
                Intent result = new Intent();
                result.putExtra("total_amount", Double.toString(Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(field[CHECK_AMOUNT]))));
                setResult(-2, result);
            } else
                setResult(-1);

            if (myPref.getPreferences(MyPreferences.pref_print_receipt_transaction_payment)) {
                if (amountTender > actualAmount)
                    showChangeDlg();
                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if (amountTender > actualAmount)
                showChangeDlg();
            else
                finish();


        } else {

            if (checkWasCapture) {
                payment.setFrontImage(Global.imgFrontCheck);
                payment.setBackImage(Global.imgBackCheck);

                StringBuilder sb = new StringBuilder();
                sb.append("O").append(field[CHECK_NUMBER].getText().toString()).append("OT");
                sb.append(field[CHECK_ROUTING].getText().toString()).append("T");

                String value = field[CHECK_ACCOUNT].getText().toString();
                String str1 = value.substring(0, 3);
                String str2 = value.substring(3, value.length());

                sb.append(str1).append("-").append(str2).append("O");
                payment.setMicrData(sb.toString());
            }

            payment.setProcessed("9");
            Encrypt encrypt = new Encrypt(activity);


            payment.setCheck_account_number(encrypt.encryptWithAES(field[CHECK_ACCOUNT].getText().toString()));
            payment.setCheck_routing_number(encrypt.encryptWithAES(field[CHECK_ROUTING].getText().toString()));
            payment.setCheck_check_number(field[CHECK_NUMBER].getText().toString());
            payment.setCheck_check_type(checkType);
            payment.setCheck_account_type(accountType);
            payment.setPay_addr(field[CHECK_ADDRESS].getText().toString());
            payment.setCheck_city(field[CHECK_CITY].getText().toString().trim());
            payment.setCheck_state(field[CHECK_STATE].getText().toString().trim());
            payment.setPay_poscode(field[CHECK_ZIPCODE].getText().toString());
            payment.setDl_number(field[CHECK_DL_NUMBER].getText().toString());
            payment.setDl_state(field[CHECK_DL_STATE].getText().toString());
            payment.setDl_dob(field[CHECK_DL_DOB].getText().toString());


            EMSPayGate_Default payGate = new EMSPayGate_Default(activity, payment);
            String generatedURL;

            if (!isRefund) {
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeCheckAction, false, null, null);
            } else {
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ReverseCheckAction, false, null, null);
            }

            new processLivePaymentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, generatedURL);
        }
    }


    private void processMultiInvoicePayment() {
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

        actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(field[this.CHECK_AMOUNT]));
        amountTender = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(field[CHECK_AMOUNT_PAID]));

        Global.amountPaid = Double.toString(amountTender);
        boolean endBreak = false;
        for (int i = 0; i < size; i++) {
            if (balance_array[i] > 0) {
                if (amountTender >= balance_array[i]) {
                    content[2] = Double.toString(balance_array[i]);
                    amountTender -= balance_array[i];
                } else {
                    content[2] = Double.toString(amountTender);
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

        MyPreferences myPref = new MyPreferences(activity);
        Encrypt encrypt = new Encrypt(activity);


        payHandler = new PaymentsHandler(activity);

        payment = new Payment(activity);

        payment.setPay_id(extras.getString("pay_id"));
        payment.setEmp_id(String.valueOf(assignEmployee.getEmpId()));
        payment.setCust_id(extras.getString("cust_id"));
        payment.setCustidkey(custidkey);

        if (myPref.isUseClerks()) {
            payment.setClerk_id(myPref.getClerkID());
        } else if (ShiftDAO.isShiftOpen()) {
            payment.setClerk_id(String.valueOf(ShiftDAO.getOpenShift().getClerkId()));
        }

        payment.setRef_num(field[CHECK_REFERENCE].getText().toString());
        payment.setPaymethod_id(extras.getString("paymethod_id"));

        if ((amountTender - actualAmount) > 0)
            payment.setPay_dueamount(Double.toString(actualAmount));
        else
            payment.setPay_dueamount(Double.toString(amountTender));

        payment.setPay_amount(Global.amountPaid);
        payment.setPay_name(field[CHECK_NAME].getText().toString());
        payment.setPay_phone(field[CHECK_PHONE].getText().toString());
        payment.setPay_email(field[CHECK_EMAIL].getText().toString());
        payment.setProcessed("1");
        payment.setPay_check(this.field[CHECK_NUMBER].getText().toString());
        payment.setPay_comment(field[COMMENTS].getText().toString());

        Location location = Global.getCurrLocation(activity, false);
        payment.setPay_latitude(String.valueOf(location.getLatitude()));
        payment.setPay_longitude(String.valueOf(location.getLongitude()));

        if (Global.isIvuLoto) {
            payment.setIvuLottoNumber(extras.getString("IvuLottoNumber"));
            payment.setIvuLottoDrawDate(extras.getString("IvuLottoDrawDate"));
            payment.setIvuLottoQR(Global.base64QRCode(extras.getString("IvuLottoNumber"), extras.getString("IvuLottoDrawDate")));


            if (!extras.getString("Tax1_amount").isEmpty()) {
                payment.setTax1_amount(extras.getString("Tax1_amount"));
                payment.setTax1_name(extras.getString("Tax1_name"));

                payment.setTax2_amount(extras.getString("Tax2_amount"));
                payment.setTax2_name(extras.getString("Tax2_name"));
            } else {
                BigDecimal tempRate;
                double tempPayAmount = Global.formatNumFromLocale(Global.amountPaid);
                tempRate = new BigDecimal(tempPayAmount * 0.06).setScale(2, BigDecimal.ROUND_UP);
                payment.setTax1_amount(tempRate.toPlainString());
                payment.setTax1_name("Estatal");

                tempRate = new BigDecimal(tempPayAmount * 0.01).setScale(2, BigDecimal.ROUND_UP);
                payment.setTax2_amount(tempRate.toPlainString());
                payment.setTax2_name("Municipal");
            }
        }


        payment.setPay_type("0");
        payment.setCard_type("Check");

        if (!isLivePayment) {
            payment.setProcessed("1");
            if (invPaymentList.size() > 0)
                invPayHandler.insert(invPaymentList);

            payHandler.insert(payment);
            if (!myPref.getLastPayID().isEmpty())
                myPref.setLastPayID("0");

            setResult(-2);

            if (myPref.getPreferences(MyPreferences.pref_print_receipt_transaction_payment)) {
                if (amountTender > actualAmount)
                    showChangeDlg();
                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if (amountTender > actualAmount)
                showChangeDlg();
            else
                finish();

        } else {

            if (checkWasCapture) {
                payment.setFrontImage(Global.imgFrontCheck);
                payment.setBackImage(Global.imgBackCheck);

                StringBuilder sb = new StringBuilder();
                sb.append("O").append(field[CHECK_NUMBER].getText().toString()).append("OT");
                sb.append(field[CHECK_ROUTING].getText().toString()).append("T");

                String tempVal = field[CHECK_ACCOUNT].getText().toString();
                String str1 = tempVal.substring(0, 3);
                String str2 = tempVal.substring(3, tempVal.length());

                sb.append(str1).append("-").append(str2).append("O");
                payment.setMicrData(sb.toString());
            }

            payment.setProcessed("9");

            payment.setCheck_account_number(encrypt.encryptWithAES(field[CHECK_ACCOUNT].getText().toString()));
            payment.setCheck_routing_number(encrypt.encryptWithAES(field[CHECK_ROUTING].getText().toString()));
            payment.setCheck_check_number(this.field[CHECK_NUMBER].getText().toString());
            payment.setCheck_check_type(checkType);
            payment.setCheck_account_type(accountType);
            payment.setPay_addr(field[CHECK_ADDRESS].getText().toString());
            payment.setCheck_city(field[CHECK_CITY].getText().toString().trim());
            payment.setCheck_state(field[CHECK_STATE].getText().toString().trim());
            payment.setPay_poscode(field[CHECK_ZIPCODE].getText().toString());

            EMSPayGate_Default payGate = new EMSPayGate_Default(activity, payment);
            String generatedURL;

            if (!isRefund) {
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeCheckAction, false, null, null);
            } else {
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.ReverseCheckAction, false, null, null);
            }

            new processLivePaymentAsync().execute(generatedURL);
        }

    }


    public class processLivePaymentAsync extends AsyncTask<String, String, String> {

        //private String[]returnedPost;
        private HashMap<String, String> responseMap;
        private String statusCode = "";
        private String urlToPost;

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
            SAXProcessCheckHandler handler = new SAXProcessCheckHandler();
            urlToPost = params[0];
            try {
                String xml = httpClient.postData(13, urlToPost);

                if (xml.equals(Global.TIME_OUT)) {
                    errorMsg = "Could not process the payment, would you like to try again?";
                    timedOut = true;
                } else if (xml.equals(Global.NOT_VALID_URL)) {
                    errorMsg = "Can not proceed...";
                }
                InputSource inSource = new InputSource(new StringReader(xml));

                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();
                xr.setContentHandler(handler);
                xr.parse(inSource);

                responseMap = handler.getResponseMap();
                statusCode = responseMap.get("epayStatusCode");
                if (statusCode != null && !statusCode.equals("APPROVED")) {
                    errorMsg = responseMap.get("epayStatusCode") + "\nstatusCode = " + responseMap.get("statusCode") + "\n" + responseMap.get("statusMessage");
                } else if (statusCode == null) {
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

            if (responseMap != null && statusCode != null && statusCode.equals("APPROVED")) {
                payment.setPay_resultcode(responseMap.get("pay_resultcode"));
                payment.setPay_resultmessage(responseMap.get("pay_resultmessage"));
                payment.setPay_transid(responseMap.get("CreditCardTransID"));
                payment.setAuthcode(responseMap.get("AuthorizationCode"));
                payment.setPay_receipt(responseMap.get("pay_receipt"));
                payment.setPay_refnum(responseMap.get("pay_refnum"));
                payment.setPay_maccount(responseMap.get("pay_maccount"));
                payment.setPay_groupcode(responseMap.get("pay_groupcode"));
                payment.setPay_stamp(responseMap.get("pay_stamp"));
                payment.setPay_expdate(responseMap.get("pay_expdate"));
                payment.setPay_result(responseMap.get("pay_result"));
                payment.setRecordnumber(responseMap.get("recordnumber"));


                Global.imgBackCheck = "";
                Global.imgFrontCheck = "";

                if (isOpenInvoice && isMultiInvoice) {
                    if (invPaymentList.size() > 0) {
                        invPayHandler.insert(invPaymentList);
                    }
                }
                payHandler.insert(payment);
                setResult(-2);

                if (myPref.getPreferences(MyPreferences.pref_print_receipt_transaction_payment)) {
                    if (amountTender > actualAmount)
                        showChangeDlg();
                    new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else if (amountTender > actualAmount)
                    showChangeDlg();
                else
                    finish();

            } else
                showFailedPrompt(errorMsg, urlToPost);

        }
    }


    private void showPrintDlg(boolean isRetry) {
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
                new printAsync().execute();

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (amountTender <= actualAmount)
                    finish();
            }
        });
        dlog.show();
    }


    private void showChangeDlg() {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_single_layout);

        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);

        viewMsg.setText("Change: " + Global.formatDoubleToCurrency(amountTender - actualAmount));
        Button btnOK = dlog.findViewById(R.id.btnDlogSingle);
        btnOK.setText(R.string.button_ok);

        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                finish();
            }
        });
        dlog.show();
    }


    private void showFailedPrompt(String msg, final String urlToPost) {
        final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(true);
        dlog.setCanceledOnTouchOutside(true);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_error);
        viewMsg.setText(msg);
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        Button btnOK = dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = dlog.findViewById(R.id.btnDlogRight);
        btnOK.setText(R.string.button_ok);
        btnNo.setText(R.string.button_no);

        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                if (timedOut)
                    new processLivePaymentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urlToPost);
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlog.dismiss();
                finish();
            }
        });
        if (!timedOut)
            btnNo.setVisibility(View.GONE);
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
            EMSDeviceManager emsDeviceManager = DeviceUtils.getEmsDeviceManager(Device.Printables.PAYMENT_RECEIPT, Global.printerDevices);
            if (emsDeviceManager != null && emsDeviceManager.getCurrentDevice() != null) {
                printSuccessful = emsDeviceManager.getCurrentDevice().printPaymentDetails(payment.getPay_id(), 1, false, null);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            myProgressDialog.dismiss();
            if (printSuccessful) {
                if (amountTender <= actualAmount)
                    finish();
            } else {
                showPrintDlg(true);
            }
        }
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
//        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        boolean isScreenOn = powerManager.isScreenOn();
//        if (!isScreenOn && myPref.isExpireUserSession())
//            Global.loggedIn = false;
        global.startActivityTransitionTimer();
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radioTypeSavings:
                accountType = "Savings";
                break;
            case R.id.radioTypeChecking:
                accountType = "Checking";
                break;
            case R.id.radioTypePersonal:
                checkType = "Personal";
                break;
            case R.id.radioTypeCorporate:
                checkType = "Corporate";
                break;
        }
    }


    @Override
    public void onClick(View v) {
        if (UIUtils.singleOnClick(v)) {
            switch (v.getId()) {
                case R.id.processCheckBut:
                    btnProcess.setEnabled(false);
                    if (!validInput()) {
                        Global.showPrompt(activity, R.string.validation_failed, activity.getString(R.string.card_validation_error));
                    } else {
                        if (!isLivePayment && Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                            Global.mainPrinterManager.getCurrentDevice().openCashDrawer();
                        }
                        if (!isOpenInvoice || (isOpenInvoice && !isMultiInvoice))
                            processPayment();
                        else
                            processMultiInvoicePayment();
                    }
                    btnProcess.setEnabled(true);
                    break;
                case R.id.exactAmountBut:
                    field[CHECK_AMOUNT_PAID].setText(field[CHECK_AMOUNT].getText().toString().replace(",", ""));
                    break;
                case R.id.btnCheckCapture:
                    Intent intent = new Intent(this, CaptureCheck_FA.class);
                    startActivityForResult(intent, INTENT_CAPTURE_CHECK);
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == INTENT_CAPTURE_CHECK && resultCode == RESULT_OK) {
            checkWasCapture = true;
        }
    }
}
