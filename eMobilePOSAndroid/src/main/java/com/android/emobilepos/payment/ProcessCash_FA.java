package com.android.emobilepos.payment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.database.CustomersHandler;
import com.android.database.InvoicePaymentsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.GroupTax;
import com.android.emobilepos.models.Payment;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.android.support.NumberUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProcessCash_FA extends BaseFragmentActivityActionBar implements OnClickListener {
    private ProgressDialog myProgressDialog;
    private AlertDialog.Builder dialog;
    private Context thisContext = this;
    private Activity activity = this;

    //    private Payment payment;
    private Global global;
    private boolean hasBeenCreated = false;
    private String inv_id;
    private boolean isFromSalesReceipt = false;
    private boolean isFromMainMenu = false;
    private EditText paid, amountDue, reference, tipAmount, promptTipField, subtotal, tax1, tax2;//,tipAmount,promptTipField
    private EditText customerNameField, customerEmailField, phoneNumberField;
    private TextView change;
    private boolean isMultiInvoice = false;


    private String[] inv_id_array, txnID_array;
    private double[] balance_array;
    private boolean isInvoice = false;

    private boolean showTipField = true;
    private String custidkey = "";

    private double amountToTip = 0;
    private double grandTotalAmount = 0;
    private boolean isRefund = false;

    private MyPreferences myPref;
    private TextView dlogGrandTotal;
    private Bundle extras;
    private Button btnProcess;
    private List<GroupTax> groupTaxRate;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_cash_layout);
        global = (Global) this.getApplication();
        myPref = new MyPreferences(activity);
        String custTaxCode;

        if (myPref.isCustSelected()) {
            custTaxCode = myPref.getCustTaxCode();
        } else {
            custTaxCode = myPref.getEmployeeDefaultTax();
        }
        groupTaxRate = TaxesHandler.getGroupTaxRate(custTaxCode);
        if (!myPref.getPreferences(MyPreferences.pref_show_tips_for_cash)) {
            showTipField = false;
            LinearLayout layout = (LinearLayout) findViewById(R.id.tipFieldMainHolder);
            layout.setVisibility(View.GONE);
        }
        TextView headerTitle = (TextView) findViewById(R.id.HeaderTitle);
        extras = this.getIntent().getExtras();
        isFromSalesReceipt = extras.getBoolean("isFromSalesReceipt");
        isFromMainMenu = extras.getBoolean("isFromMainMenu");

        if (extras.getBoolean("salespayment") || extras.getBoolean("salesreceipt")) {
            headerTitle.setText(getString(R.string.cash_payment_title));
        } else if (extras.getBoolean("salesrefund")) {
            headerTitle.setText(getString(R.string.cash_refund_title));
            isRefund = true;
        } else if (extras.getBoolean("histinvoices")) {
            headerTitle.setText(getString(R.string.cash_payment_title));
            isInvoice = true;
        } else if (extras.getBoolean("salesinvoice")) {
            headerTitle.setText(R.string.cash_invoice_lbl);
        }

        amountDue = (EditText) findViewById(R.id.amountDueCashEdit);
        reference = (EditText) findViewById(R.id.referenceNumber);
        tipAmount = (EditText) findViewById(R.id.tipAmountField);
        subtotal = (EditText) findViewById(R.id.subtotalCashEdit);
        tax1 = (EditText) findViewById(R.id.tax1CashEdit);
        tax2 = (EditText) findViewById(R.id.tax2CashEdit);
        TextView tax1Lbl = (TextView) findViewById(R.id.tax1CashLbl);
        TextView tax2Lbl = (TextView) findViewById(R.id.tax2CashLbl);
        setTaxLabels(groupTaxRate, tax1Lbl, tax2Lbl);
        customerNameField = (EditText) findViewById(R.id.processCashName);
        customerEmailField = (EditText) findViewById(R.id.processCashEmail);
        phoneNumberField = (EditText) findViewById(R.id.processCashPhone);


        Button btnFive = (Button) findViewById(R.id.btnFive);
        Button btnTen = (Button) findViewById(R.id.btnTen);
        Button btnTwenty = (Button) findViewById(R.id.btnTwenty);
        Button btnFifty = (Button) findViewById(R.id.btnFifty);
        btnFive.setOnClickListener(this);
        btnTen.setOnClickListener(this);
        btnTwenty.setOnClickListener(this);
        btnFifty.setOnClickListener(this);

        if (showTipField)
            this.tipAmount.setText(Global.formatDoubleToCurrency(0.00));

        amountDue.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));
        tax1.setText(Global.formatDoubleStrToCurrency(extras.getString("Tax1_amount")));
        tax2.setText(Global.formatDoubleStrToCurrency(extras.getString("Tax2_amount")));


        custidkey = extras.getString("custidkey");
        if (custidkey == null)
            custidkey = "";

        if (!isFromMainMenu || Global.isIvuLoto) {
            amountDue.setEnabled(false);
        }

        this.paid = (EditText) findViewById(R.id.paidCashEdit);

        this.paid.setText(Global.formatDoubleToCurrency(0.00));
        this.paid.setSelection(5);


        this.paid.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (v.hasFocus()) {
                    int lent = paid.getText().length();
                    Selection.setSelection(paid.getText(), lent);
                }
            }
        });
        this.amountDue.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (v.hasFocus()) {
                    Selection.setSelection(amountDue.getText(), amountDue.getText().length());
                }

            }
        });

        change = (TextView) findViewById(R.id.changeCashText);

        Button exactBut = (Button) findViewById(R.id.exactAmountBut);
        btnProcess = (Button) findViewById(R.id.processCashBut);

        if (extras.getBoolean("histinvoices")) {
            isMultiInvoice = extras.getBoolean("isMultipleInvoice");
            if (!isMultiInvoice)
                inv_id = extras.getString("inv_id");
            else {
                inv_id_array = extras.getStringArray("inv_id_array");
                balance_array = extras.getDoubleArray("balance_array");
                txnID_array = extras.getStringArray("txnID_array");
            }

        } else
            inv_id = extras.getString("job_id");

        btnProcess.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                btnProcess.setEnabled(false);
                double enteredAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
                if (enteredAmount < 0) {
                    paid.setBackgroundResource(R.drawable.edittext_wrong_input);
                    Global.showPrompt(activity, R.string.validation_failed, activity.getString(R.string.error_wrong_amount));
                } else {
                    paid.setBackgroundResource(R.drawable.edittext_border);

                    if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null) {
                        Global.mainPrinterManager.currentDevice.openCashDrawer();
                    }

                    if (!isInvoice || (isInvoice && !isMultiInvoice))
                        new processPaymentAsync().execute(false);
                    else {
                        new processPaymentAsync().execute(true);
                    }
                }
                btnProcess.setEnabled(true);

            }
        });

        exactBut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));
                grandTotalAmount = amountToBePaid + amountToTip;
                paid.setText(amountDue.getText().toString());

            }

        });


        this.amountDue.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                recalculateChange();
            }


            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                parseInputedCurrency(s, amountDue);
            }
        });

        this.paid.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (!paid.getText().toString().isEmpty())
                    recalculateChange();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                parseInputedCurrency(s, paid);
            }
        });

//        List<OrderProducts> orderProducts = global.orderProducts;
//        double subtotalDbl = 0;
//        for (OrderProducts products : orderProducts) {
//            subtotalDbl += Double.parseDouble(products.itemSubtotal);
//        }
//        subtotal.setText(Global.formatDoubleToCurrency(subtotalDbl));
        subtotal.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));
        if (!Global.isIvuLoto || isFromSalesReceipt) {
            findViewById(R.id.ivuposRow1).setVisibility(View.GONE);
            findViewById(R.id.ivuposRow2).setVisibility(View.GONE);
            findViewById(R.id.ivuposRow3).setVisibility(View.GONE);
        } else {
            setIVUPOSFieldListeners();
        }

        if (showTipField) {
            Button tipButton = (Button) findViewById(R.id.tipAmountBut);
            tipButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    promptTipConfirmation();
                }
            });
        }


        if (!Global.getValidString(extras.getString("cust_id")).isEmpty()) {

            CustomersHandler handler2 = new CustomersHandler(activity);
            HashMap<String, String> customerInfo = handler2.getCustomerMap(extras.getString("cust_id"));


            if (customerInfo != null) {
                if (!customerInfo.get("cust_name").isEmpty())
                    customerNameField.setText(customerInfo.get("cust_name"));
                if (!customerInfo.get("cust_phone").isEmpty())
                    phoneNumberField.setText(customerInfo.get("cust_phone"));
                if (!customerInfo.get("cust_email").isEmpty())
                    customerEmailField.setText(customerInfo.get("cust_email"));
            }
        } else if (!extras.getString("order_email", "").isEmpty()) {
            customerEmailField.setText(extras.getString("order_email"));
        }

        hasBeenCreated = true;
    }

    private void setIVUPOSFieldListeners() {
        subtotal.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (v.hasFocus()) {
                    Selection.setSelection(subtotal.getText(), subtotal.getText().length());
                }

            }
        });
        tax1.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (v.hasFocus()) {
                    Selection.setSelection(tax1.getText(), tax1.getText().length());
                }

            }
        });
        tax2.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (v.hasFocus()) {
                    Selection.setSelection(tax2.getText(), tax2.getText().length());
                }

            }
        });
        subtotal.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                parseInputedCurrency(s, subtotal);
                if (!isFromSalesReceipt) {
                    calculateTaxes(groupTaxRate, subtotal, tax1, tax2);
                    calculateAmountDue(subtotal, tax1, tax2, amountDue);
                }
                recalculateChange();

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                parseInputedCurrency(s, subtotal);
            }
        });
        tax1.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                calculateAmountDue(subtotal, tax1, tax2, amountDue);
                recalculateChange();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                parseInputedCurrency(s, tax1);
            }
        });
        tax2.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                calculateAmountDue(subtotal, tax1, tax2, amountDue);
                recalculateChange();

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                parseInputedCurrency(s, tax2);
            }
        });
    }

    public static void calculateAmountDue(EditText subtotal, EditText tax1, EditText tax2, EditText amount) {
        double subtotalDbl = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(subtotal));
        double tax1Dbl = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(tax1));
        double tax2Dbl = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(tax2));
        double amountDueDbl = subtotalDbl + tax1Dbl + tax2Dbl;

        amount.setText(Global.getCurrencyFormat(Global.formatNumToLocale(amountDueDbl)));
    }

    public static void setTaxLabels(List<GroupTax> groupTaxRate, TextView tax1Lbl, TextView tax2Lbl) {
        if (groupTaxRate.size() > 0)
            tax1Lbl.setText(groupTaxRate.get(0).getTaxName());
        if (groupTaxRate.size() > 1)
            tax2Lbl.setText(groupTaxRate.get(1).getTaxName());
    }


    private void recalculateChange() {

        double totAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));
        double totalPaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));

        if (totalPaid > totAmount) {
            double tempTotal = Math.abs(totAmount - totalPaid);
            change.setText(Global.getCurrencyFormat(Global.formatNumToLocale(tempTotal)));
        } else {
            change.setText(Global.formatDoubleToCurrency(0.00));
        }

    }


    private void promptTipConfirmation() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogLayout = inflater.inflate(R.layout.tip_dialog_layout, null);


        //****Method that works with both jelly bean/gingerbread
        //AlertDialog.Builder dialog = new AlertDialog.Builder(this,R.style.TransparentDialog);

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog dialog = builder.create();
        dialog.setView(dialogLayout, 0, 0, 0, 0);
        dialog.setInverseBackgroundForced(true);

        //*****Method that works only with gingerbread and removes background
        /*final Dialog dialog = new Dialog(activity,R.style.TransparentDialog);
        dialog.setContentView(dialogLayout);*/

        double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
        grandTotalAmount = amountToBePaid + amountToTip;

        Button tenPercent = (Button) dialogLayout.findViewById(R.id.tenPercent);
        Button fifteenPercent = (Button) dialogLayout.findViewById(R.id.fifteenPercent);
        Button twentyPercent = (Button) dialogLayout.findViewById(R.id.twentyPercent);
        dlogGrandTotal = (TextView) dialogLayout.findViewById(R.id.grandTotalView);
        dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));


        promptTipField = (EditText) dialogLayout.findViewById(R.id.otherTipAmountField);
        promptTipField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
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
                parseInputedCurrency(s, promptTipField);
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
                double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
                amountToTip = (float) (amountToBePaid * (0.1));
                grandTotalAmount = amountToBePaid + amountToTip;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                promptTipField.setText("");
            }
        });

        fifteenPercent.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
                amountToTip = (float) (amountToBePaid * (0.15));
                grandTotalAmount = amountToBePaid + amountToTip;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                promptTipField.setText("");
            }
        });

        twentyPercent.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
                amountToTip = (float) (amountToBePaid * (0.2));
                grandTotalAmount = amountToBePaid + amountToTip;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                promptTipField.setText("");
            }
        });


        noneButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
                amountToTip = 0;
                grandTotalAmount = amountToBePaid;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                //dialog.dismiss();
            }
        });


        cancelTip.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
                amountToTip = 0;
                grandTotalAmount = amountToBePaid;
                dialog.dismiss();
            }
        });

        saveTip.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (tipAmount != null)
                    tipAmount.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(Double.toString(amountToTip)))));
                dialog.dismiss();

            }
        });
        dialog.show();
    }


    public static void parseInputedCurrency(CharSequence s, EditText editText) {
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
        DecimalFormatSymbols sym = format.getDecimalFormatSymbols();
        StringBuilder sb = new StringBuilder();
        sb.append("^\\").append(sym.getCurrencySymbol()).append("\\s(\\d{1,3}(\\").append(sym.getGroupingSeparator()).append("\\d{3})*|(\\d+))(");
        sb.append(sym.getDecimalSeparator()).append("\\d{2})?$");

        if (!s.toString().matches(sb.toString())) {
            String userInput = "" + s.toString().replaceAll("[^\\d]", "");
            StringBuilder cashAmountBuilder = new StringBuilder(userInput);

            while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0') {
                cashAmountBuilder.deleteCharAt(0);
            }
            while (cashAmountBuilder.length() < 3) {
                cashAmountBuilder.insert(0, '0');
            }

            cashAmountBuilder.insert(cashAmountBuilder.length() - 2, sym.getDecimalSeparator());
            cashAmountBuilder.insert(0, sym.getCurrencySymbol() + " ");
            editText.setText(cashAmountBuilder.toString());
        }
        Selection.setSelection(editText.getText(), editText.getText().length());
    }

    public static void calculateTaxes(List<GroupTax> groupTaxRate, EditText subtotal, EditText tax1, EditText tax2) {
        double subtotalDbl = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(subtotal));
        //set default taxes values to zero
        BigDecimal tax1Rate = new BigDecimal(0.00);
        BigDecimal tax2Rate = new BigDecimal(0.00);

        //if we have taxes then
        if (groupTaxRate.size() > 0) {
            tax1Rate = new BigDecimal(Double.parseDouble(groupTaxRate.get(0).getTaxRate()));
            tax2Rate = new BigDecimal(Double.parseDouble(groupTaxRate.get(1).getTaxRate()));
        }

        BigDecimal tax1Dbl = new BigDecimal(subtotalDbl).multiply(tax1Rate);
        BigDecimal tax2Dbl = new BigDecimal(subtotalDbl).multiply(tax2Rate);

//        double tax1Dbl = subtotalDbl * tax1Rate;
//        double tax2Dbl = subtotalDbl * tax2Rate;

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);
        tax1.setText(df.format(tax1Dbl.doubleValue()));
        tax2.setText(df.format(tax2Dbl.doubleValue()));
    }


    private Payment processPayment() {
        PaymentsHandler payHandler = new PaymentsHandler(activity);
        double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));
        double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));

        if (Global.isIvuLoto) {
            Global.subtotalAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(subtotal));
        }

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


        if (showTipField) {
            Global.tipPaid = Double.toString(amountToTip);
        }


        String taxName2 = null;
        String taxAmnt1 = null;
        String taxName1 = null;
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

        String isRef = null;
        String paymentType;
        if (extras.getBoolean("salesrefund", false)) {
            isRef = "1";
            paymentType = "2";
        } else
            paymentType = "0";

        Payment payment = new Payment(activity, extras.getString("pay_id"), extras.getString("cust_id"), invoiceId, jobId, clerkId, custidkey, extras.getString("paymethod_id"),
                actualAmount, amountToBePaid,
                customerNameField.getText().toString(), reference.getText().toString(), phoneNumberField.getText().toString(),
                customerEmailField.getText().toString(), amountToTip, taxAmnt1, taxAmnt2, taxName1, taxName2,
                isRef, paymentType, "Cash", null, null,
                null, null,
                null, null, null,
                null, null, null);


//        Payment payment = new Payment(activity, extras.getString("pay_id"), extras.getString("cust_id"), invoiceId, jobId, clerkId, custidkey,
//                extras.getString("paymethod_id"), actualAmount, amountToBePaid,
//                customerNameField.getText().toString(), reference.getText().toString(), phoneNumberField.getText().toString(), customerEmailField.getText().toString(),
//                amountToTip, taxAmnt1, taxAmnt2, taxName1, taxName2,
//                isRef, paymentType, "Cash");


        Global.amountPaid = Double.toString(amountToBePaid);

        payHandler.insert(payment);

        if (!myPref.getLastPayID().isEmpty())
            myPref.setLastPayID("0");

        updateShiftAmount();

        if (extras.getBoolean("histinvoices") || extras.getBoolean("salesinvoice") || isFromSalesReceipt) {
            setResult(-2);
        } else if (extras.getBoolean("salespayment") || extras.getBoolean("salesrefund")) {
            Intent result = new Intent();
            result.putExtra("total_amount",
                    Double.toString(Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(this.amountDue))));
            setResult(-2, result);
        } else
            setResult(-1);
        return payment;
    }


    private Payment processMultiInvoicePayment() {
        InvoicePaymentsHandler invHandler = new InvoicePaymentsHandler(activity);
        List<Double> appliedAmount = new ArrayList<Double>();
        List<String[]> contentList = new ArrayList<String[]>();
        String[] content = new String[4];

        int size = inv_id_array.length;
        String payID = extras.getString("pay_id");

        double value;

        for (int i = 0; i < size; i++) {
            value = invHandler.getTotalPaidAmount(inv_id_array[i]);
            if (value != -1) {
                if (balance_array[i] >= value)
                    balance_array[i] -= value;
                else
                    balance_array[i] = 0.0;
            }

        }

        double tempPaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(this.paid));
        Global.amountPaid = Double.toString(grandTotalAmount);
        boolean endBreak = false;
        for (int i = 0; i < size; i++) {
            if (balance_array[i] > 0) {
                if (tempPaid >= balance_array[i]) {
                    content[2] = Double.toString(balance_array[i]);
                    appliedAmount.add(balance_array[i]);
                    tempPaid -= balance_array[i];
                } else {
                    content[2] = Double.toString(tempPaid);
                    endBreak = true;
                }

                content[0] = payID;
                content[1] = inv_id_array[i];
                content[3] = txnID_array[i];
                contentList.add(content);
                content = new String[4];
                if (endBreak)
                    break;
            }
        }

        if (contentList.size() > 0)
            invHandler.insert(contentList);


        PaymentsHandler payHandler = new PaymentsHandler(activity);
        double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));
        double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
        String clerkId = null;
        if (!myPref.getShiftIsOpen())
            clerkId = myPref.getShiftClerkID();
        else if (myPref.getPreferences(MyPreferences.pref_use_clerks))
            clerkId = myPref.getClerkID();
        String invoiceId = "";

        String paymentType = "0";
        Payment payment = new Payment(activity, extras.getString("pay_id"), extras.getString("cust_id"), invoiceId, null,
                clerkId, custidkey, extras.getString("paymethod_id"),
                actualAmount, amountToBePaid,
                customerNameField.getText().toString(), reference.getText().toString(), phoneNumberField.getText().toString(),
                customerEmailField.getText().toString(), amountToTip, null, null, null, null,
                null, paymentType, "Cash", null, null,
                null, null,
                null, null, null,
                null, null, null);

//        Payment payment = new Payment(activity, extras.getString("pay_id"), extras.getString("cust_id"), invoiceId, null, clerkId,
//                custidkey, extras.getString("paymethod_id"), actualAmount, amountToBePaid,
//                customerNameField.getText().toString(), reference.getText().toString(), phoneNumberField.getText().toString(),
//                customerEmailField.getText().toString(),
//                amountToTip, null, null, null, null,
//                null, paymentType, "Cash");


        payHandler.insert(payment);
        if (!myPref.getLastPayID().isEmpty())
            myPref.setLastPayID("0");


        updateShiftAmount();

        setResult(-2);
        return payment;
    }


    private void updateShiftAmount() {

        if (!myPref.getShiftIsOpen()) {
            double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));
            double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
            boolean isReturn = false;
            if (Global.ord_type.equals(Global.OrderType.RETURN.getCodeString()) || isRefund)
                isReturn = true;
            ShiftPeriodsDBHandler handler = new ShiftPeriodsDBHandler(activity);
            if (amountToBePaid <= actualAmount) {
                handler.updateShiftAmounts(myPref.getShiftID(), amountToBePaid, isReturn);
            } else {
                handler.updateShiftAmounts(myPref.getShiftID(), actualAmount, isReturn);
            }
        }

    }


    private class processPaymentAsync extends AsyncTask<Boolean, String, Payment> {

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(thisContext);
            myProgressDialog.setMessage(getString(R.string.processing_payment_msg));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }


        @Override
        protected Payment doInBackground(Boolean... params) {
            Payment payment = null;
            boolean isMultiPayment = params[0];
            try {
                if (isMultiPayment)
                    payment = processMultiInvoicePayment();
                else
                    payment = processPayment();
            } catch (Exception e) {
                // TODO Auto-generated catch block
//				Tracker tracker = EasyTracker.getInstance(activity);
//				tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
            }
            return payment;
        }

        @Override
        protected void onPostExecute(Payment payment) {
            myProgressDialog.dismiss();

            double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
            double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));

            if (myPref.getPreferences(MyPreferences.pref_print_receipt_transaction_payment) && !isFromMainMenu) {

                new printAsync().execute(payment);

                if (amountToBePaid > actualAmount)
                    showChangeDlg();
            } else if (amountToBePaid > actualAmount)
                showChangeDlg();
            else
                finish();

        }
    }


    private class printAsync extends AsyncTask<Payment, Void, Payment> {
        private boolean printSuccessful = true;

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
        protected Payment doInBackground(Payment... params) {
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null) {
                printSuccessful = Global.mainPrinterManager.currentDevice.printPaymentDetails(params[0].pay_id, 1, false, null);
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(Payment payment) {
            myProgressDialog.dismiss();
            double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));
            double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));

            if (printSuccessful) {
                if (amountToBePaid <= actualAmount)
                    finish();
            } else {
                showPrintDlg(true, payment);
            }

//			if(amountToBePaid<=actualAmount)
//				finish();
        }
    }


    private void showPrintDlg(boolean isRetry, final Payment payment) {
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
        }
        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        Button btnYes = (Button) dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = (Button) dlog.findViewById(R.id.btnDlogRight);
        btnYes.setText(R.string.button_yes);
        btnNo.setText(R.string.button_no);

        btnYes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                new printAsync().execute(payment);

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));
                double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));

                dlog.dismiss();
                //activity.finish();
                if (amountToBePaid <= actualAmount)
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

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        //sb.append(getString(R.string.dlog_msg_print_cust_copy)).append("\n\n");
        viewMsg.setText(getString(R.string.changeLbl) + change.getText().toString());
        Button btnOK = (Button) dlog.findViewById(R.id.btnDlogSingle);
        btnOK.setText(R.string.button_ok);

        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dlog.dismiss();
                finish();
            }
        });
        dlog.show();
    }

    @Override
    public void onResume() {

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
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            global.loggedIn = false;
        global.startActivityTransitionTimer();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.create().dismiss();
        }
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int temp = 0;
        switch (v.getId()) {
            case R.id.btnFive:
                temp = 5;
                break;
            case R.id.btnTen:
                temp = 10;
                break;
            case R.id.btnTwenty:
                temp = 20;
                break;
            case R.id.btnFifty:
                temp = 50;
                break;
        }
        double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
        amountToBePaid += temp;
        grandTotalAmount = amountToBePaid + amountToTip;
        paid.setText(Global.formatDoubleToCurrency(amountToBePaid));
    }

}
