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
import android.support.v4.app.FragmentActivity;
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
import com.android.database.DrawInfoHandler;
import com.android.database.InvoicePaymentsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ShiftPeriodsDBHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.GroupTax;
import com.android.emobilepos.models.GroupTaxRate;
import com.android.emobilepos.models.Payment;
import com.android.ivu.MersenneTwisterFast;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProcessCash_FA extends FragmentActivity implements OnClickListener {
    private ProgressDialog myProgressDialog;
    private AlertDialog.Builder dialog;
    private Context thisContext = this;
    private Activity activity = this;

    private Payment payment;
    private Global global;
    private boolean hasBeenCreated = false;
    private String inv_id;
    private boolean isFromSalesReceipt = false;
    private boolean isFromMainMenu = false;
    private EditText paid, amount, reference, tipAmount, promptTipField, subtotal, tax1, tax2;//,tipAmount,promptTipField
    private EditText customerNameField, customerEmailField, phoneNumberField;
    private TextView change, tax1Lbl, tax2Lbl;
    private boolean isMultiInvoice = false;


    private String[] inv_id_array, txnID_array;
    private double[] balance_array;
    private boolean isInvoice = false;

    private boolean showTipField = true;
    private String custidkey = "";

    private double amountToTip = 0;
    private double amountToBePaid = 0, grandTotalAmount = 0, actualAmount = 0;
    private boolean isRefund = false;

    private MyPreferences myPref;
    private TextView dlogGrandTotal;
    private Bundle extras;
    private Button btnProcess;
    private List<GroupTax> groupTaxRate;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.process_cash_layout);
        global = (Global) this.getApplication();
        myPref = new MyPreferences(activity);
        groupTaxRate = TaxesHandler.getGroupTaxRate(myPref.getEmployeeDefaultTax());
        if (!myPref.getPreferences(MyPreferences.pref_show_tips_for_cash)) {
            showTipField = false;
            LinearLayout layout = (LinearLayout) findViewById(R.id.tipFieldMainHolder);
            layout.setVisibility(View.GONE);
        }
        if (!Global.isIvuLoto) {
            findViewById(R.id.ivuposRow1).setVisibility(View.GONE);
            findViewById(R.id.ivuposRow2).setVisibility(View.GONE);
            findViewById(R.id.ivuposRow3).setVisibility(View.GONE);
        }
        TextView headerTitle = (TextView) findViewById(R.id.HeaderTitle);
        extras = this.getIntent().getExtras();

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

        amount = (EditText) findViewById(R.id.amountCashEdit);
        reference = (EditText) findViewById(R.id.referenceNumber);
        tipAmount = (EditText) findViewById(R.id.tipAmountField);
        subtotal = (EditText) findViewById(R.id.subtotalCashEdit);
        tax1 = (EditText) findViewById(R.id.tax1CashEdit);
        tax2 = (EditText) findViewById(R.id.tax2CashEdit);
        tax1Lbl = (TextView) findViewById(R.id.tax1CashLbl);
        tax2Lbl = (TextView) findViewById(R.id.tax2CashLbl);
        setTaxLabels();
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

        amount.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));


        isFromSalesReceipt = extras.getBoolean("isFromSalesReceipt");
        isFromMainMenu = extras.getBoolean("isFromMainMenu");
        custidkey = extras.getString("custidkey");
        if (custidkey == null)
            custidkey = "";

        if (!isFromMainMenu || Global.isIvuLoto) {
            amount.setEnabled(false);
        }

        this.paid = (EditText) findViewById(R.id.paidCashEdit);

        subtotal.setText(Global.formatDoubleToCurrency(0.00));
        tax1.setText(Global.formatDoubleToCurrency(0.00));
        tax2.setText(Global.formatDoubleToCurrency(0.00));
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
        this.amount.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (v.hasFocus()) {
                    Selection.setSelection(amount.getText(), amount.getText().length());
                }

            }
        });
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
                double enteredAmount = Global.formatNumFromLocale(paid.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
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
                amountToBePaid = Global.formatNumFromLocale(amount.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
                grandTotalAmount = amountToBePaid + amountToTip;
                paid.setText(amount.getText().toString());

            }

        });


        this.amount.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                recalculateChange();
            }


            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                parseInputedCurrency(s, amount);
            }
        });
        subtotal.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                calculateTaxes(groupTaxRate, subtotal, tax1, tax2);
                calculateAmountDue(subtotal,tax1,tax2,amount);
                recalculateChange();

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                parseInputedCurrency(s, subtotal);
            }
        });
        tax1.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                calculateAmountDue(subtotal,tax1,tax2,amount);
                recalculateChange();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                parseInputedCurrency(s, tax1);
            }
        });
        tax2.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                calculateAmountDue(subtotal,tax1,tax2,amount);
                recalculateChange();

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                parseInputedCurrency(s, tax2);
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

    public static void calculateAmountDue(EditText subtotal, EditText tax1, EditText tax2, EditText amount) {
        double subtotalDbl = Global.formatNumFromLocale(subtotal.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
        double tax1Dbl = Global.formatNumFromLocale(tax1.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
        double tax2Dbl = Global.formatNumFromLocale(tax2.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
        double amountDueDbl = subtotalDbl + tax1Dbl + tax2Dbl;
        amount.setText(Global.getCurrencyFormat(Global.formatNumToLocale(amountDueDbl)));
    }

    private void setTaxLabels() {
        if (groupTaxRate.size() > 0)
            tax1Lbl.setText(groupTaxRate.get(0).getTaxName());
        if (groupTaxRate.size() > 1)
            tax2Lbl.setText(groupTaxRate.get(1).getTaxName());
    }


    private void recalculateChange() {

        double totAmount = Global.formatNumFromLocale(amount.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
        double totalPaid = Global.formatNumFromLocale(paid.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());

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

        amountToBePaid = Global.formatNumFromLocale(paid.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
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
                // TODO Auto-generated method stub
                amountToTip = (float) (amountToBePaid * (0.1));
                grandTotalAmount = amountToBePaid + amountToTip;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                promptTipField.setText("");
            }
        });

        fifteenPercent.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                amountToTip = (float) (amountToBePaid * (0.15));
                grandTotalAmount = amountToBePaid + amountToTip;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                promptTipField.setText("");
            }
        });

        twentyPercent.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                amountToTip = (float) (amountToBePaid * (0.2));
                grandTotalAmount = amountToBePaid + amountToTip;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                promptTipField.setText("");
            }
        });


        noneButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                amountToTip = 0;
                grandTotalAmount = amountToBePaid;
                dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
                //dialog.dismiss();
            }
        });


        cancelTip.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
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
//            switch (id) {
//                case R.id.subtotalCashEdit:
//                    subtotal.setText(cashAmountBuilder.toString());
//                    break;
//                case R.id.tax1CashEdit:
//                    tax1.setText(cashAmountBuilder.toString());
//                    break;
//                case R.id.tax2CashEdit:
//                    tax2.setText(cashAmountBuilder.toString());
//                    break;
//                case R.id.paidCashEdit:
//                    this.paid.setText(cashAmountBuilder.toString());
////                    amountToBePaid = Global.formatNumFromLocale(cashAmountBuilder.toString().replaceAll("[^\\d\\,\\.]", "").trim());
////                    grandTotalAmount = amountToBePaid + amountToTip;
//                    break;
//                case R.id.amountCashEdit:
//                    this.amount.setText(cashAmountBuilder.toString());
////                    actualAmount = Global.formatNumFromLocale(cashAmountBuilder.toString().replaceAll("[^\\d\\,\\.]", "").trim());
//                    //amountToBePaid = (float)(Global.formatNumFromLocale(cashAmountBuilder.toString().replaceAll("[^\\d\\,\\.]", "").trim()));
//                    //grandTotalAmount = amountToBePaid + amountToTip;
//                    break;
//                case R.id.tipAmountField:
//                    this.promptTipField.setText(cashAmountBuilder);
////                    double amountToTipFromField = Global.formatNumFromLocale(cashAmountBuilder.toString().replaceAll("[^\\d\\,\\.]", "").trim());
////                    if (amountToTipFromField > 0) {
////                        amountToTip = amountToTipFromField;
////                        grandTotalAmount = amountToBePaid + amountToTip;
////                        dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
////                    }
//                    break;
//            }
        }
        Selection.setSelection(editText.getText(), editText.getText().length());
//        switch (id) {
//            case R.id.subtotalCashEdit:
//                Selection.setSelection(subtotal.getText(), subtotal.getText().length());
//                break;
//            case R.id.tax1CashEdit:
//                Selection.setSelection(tax1.getText(), tax1.getText().length());
//                break;
//            case R.id.tax2CashEdit:
//                Selection.setSelection(tax2.getText(), tax2.getText().length());
//                break;
//            case R.id.paidCashEdit:
//                Selection.setSelection(paid.getText(), this.paid.getText().length());
//                break;
//            case R.id.amountCashEdit:
//                Selection.setSelection(this.amount.getText(), this.amount.getText().length());
//                break;
//            case R.id.tipAmountField:
//                Selection.setSelection(this.promptTipField.getText(), this.promptTipField.getText().length());
//                break;
//        }
    }

    public static void calculateTaxes(List<GroupTax> groupTaxRate, EditText subtotal, EditText tax1, EditText tax2) {
        double subtotalDbl = Global.formatNumFromLocale(subtotal.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
        double tax1Rate = Double.parseDouble(groupTaxRate.get(0).getTaxRate());
        double tax2Rate = Double.parseDouble(groupTaxRate.get(1).getTaxRate());
        double tax1Dbl = new BigDecimal(subtotalDbl * tax1Rate).setScale(2, BigDecimal.ROUND_UP).doubleValue();
        double tax2Dbl = new BigDecimal(subtotalDbl * tax2Rate).setScale(2, BigDecimal.ROUND_UP).doubleValue();
        tax1.setText(Global.formatDoubleToCurrency(tax1Dbl));
        tax2.setText(Global.formatDoubleToCurrency(tax2Dbl));
    }

    private void processPayment() {
        PaymentsHandler payHandler = new PaymentsHandler(activity);
        actualAmount = Global.formatNumFromLocale(amount.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());


        payment = new Payment(activity);

        payment.pay_id = extras.getString("pay_id");

        payment.emp_id = myPref.getEmpID();
        payment.cust_id = extras.getString("cust_id");
        if (!extras.getBoolean("histinvoices")) {
            payment.job_id = inv_id;
        } else {
            payment.inv_id = inv_id;
        }

        if (!myPref.getShiftIsOpen())
            payment.clerk_id = myPref.getShiftClerkID();
        else if (myPref.getPreferences(MyPreferences.pref_use_clerks))
            payment.clerk_id = myPref.getClerkID();

        payment.custidkey = custidkey;

        // String tempPaid = Double.toString(grandTotalAmount);

        payment.paymethod_id = extras.getString("paymethod_id");


        payment.pay_dueamount = Double.toString(amountToBePaid);

        if (amountToBePaid > actualAmount)
            payment.pay_amount = Double.toString(actualAmount);
        else
            payment.pay_amount = Double.toString(amountToBePaid);


        payment.pay_name = customerNameField.getText().toString();
        payment.processed = "1";
        payment.ref_num = reference.getText().toString();

        payment.pay_phone = phoneNumberField.getText().toString();
        payment.pay_email = customerEmailField.getText().toString();

        if (showTipField) {
            Global.tipPaid = Double.toString(amountToTip);
            payment.pay_tip = Global.tipPaid;
        }

        if (Global.isIvuLoto) {
            DrawInfoHandler drawDateInfo = new DrawInfoHandler(activity);
            MersenneTwisterFast mersenneTwister = new MersenneTwisterFast();
            String drawDate = drawDateInfo.getDrawDate();
            String ivuLottoNum = mersenneTwister.generateIVULoto();

            payment.IvuLottoNumber = ivuLottoNum;
            payment.IvuLottoDrawDate = drawDate;
            payment.IvuLottoQR =
                    Global.base64QRCode(ivuLottoNum, drawDate);

            if (!extras.getString("Tax1_amount").isEmpty()) {
                payment.Tax1_amount = extras.getString("Tax1_amount");
                payment.Tax1_name = extras.getString("Tax1_name");

                payment.Tax2_amount = extras.getString("Tax2_amount");
                payment.Tax2_name = extras.getString("Tax2_name");
            } else {
                payment.Tax1_amount = String.valueOf(Global.formatNumFromLocale(tax1.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim()));
                if (groupTaxRate.size() > 0)
                    payment.Tax1_name = groupTaxRate.get(0).getTaxName();
                payment.Tax2_amount = String.valueOf(Global.formatNumFromLocale(tax2.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim()));
                if (groupTaxRate.size() > 1)
                    payment.Tax2_name = groupTaxRate.get(1).getTaxName();
            }
        }

        String[] location = Global.getCurrLocation(activity);
        payment.pay_latitude = location[0];
        payment.pay_longitude = location[1];
        payment.card_type = "Cash";

        if (extras.getBoolean("salesrefund", false)) {
            payment.is_refund = "1";
            payment.pay_type = "2";
        } else
            payment.pay_type = "0";

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
                    Double.toString(Global.formatNumFromLocale(this.amount.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim())));
            setResult(-2, result);
        } else
            setResult(-1);
    }


    private void processMultiInvoicePayment() {
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

        double tempPaid = Global.formatNumFromLocale(this.paid.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());
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
        payment = new Payment(activity);
        actualAmount = Global.formatNumFromLocale(amount.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim());

        payment.pay_id = extras.getString("pay_id");
        payment.cust_id = extras.getString("cust_id");
        payment.custidkey = custidkey;
        payment.emp_id = myPref.getEmpID();


        if (!myPref.getShiftIsOpen())
            payment.clerk_id = myPref.getShiftClerkID();
        else if (myPref.getPreferences(MyPreferences.pref_use_clerks))
            payment.clerk_id = myPref.getClerkID();


        payment.paymethod_id = extras.getString("paymethod_id");
        payment.pay_dueamount = Double.toString(amountToBePaid);

        if (amountToBePaid > actualAmount)
            payment.pay_amount = Double.toString(actualAmount);
        else
            payment.pay_amount = Double.toString(amountToBePaid);
        //payment.pay_amount =Double.toString(amountToBePaid));
        payment.pay_name = customerNameField.getText().toString();
        payment.pay_phone = phoneNumberField.getText().toString();
        payment.pay_email = customerEmailField.getText().toString();
        payment.processed = "1";
        payment.ref_num = reference.getText().toString();
        payment.inv_id = "";

        String[] location = Global.getCurrLocation(activity);
        payment.pay_latitude = location[0];
        payment.pay_longitude = location[1];
        payment.pay_type = "0";
        payment.card_type = "Cash";

        payHandler.insert(payment);
        if (!myPref.getLastPayID().isEmpty())
            myPref.setLastPayID("0");


        updateShiftAmount();

        setResult(-2);
    }


    private void updateShiftAmount() {

        if (!myPref.getShiftIsOpen()) {
            boolean isReturn = false;
            if (Global.ord_type.equals(Global.IS_RETURN) || isRefund)
                isReturn = true;
            ShiftPeriodsDBHandler handler = new ShiftPeriodsDBHandler(activity);
            if (amountToBePaid <= actualAmount) {
                handler.updateShiftAmounts(myPref.getShiftID(), amountToBePaid, isReturn);
                //handler.updateShift(myPref.getShiftID(), "total_transaction_cash", Double.toString(amountToBePaid));
            } else {
                handler.updateShiftAmounts(myPref.getShiftID(), actualAmount, isReturn);
                //handler.updateShift(myPref.getShiftID(), "total_transaction_cash", Double.toString(actualAmount));
            }
        }

    }


    private class processPaymentAsync extends AsyncTask<Boolean, String, String> {

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(thisContext);
            myProgressDialog.setMessage(getString(R.string.processing_payment_msg));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            // myProgressDialog.setMax(100);
            myProgressDialog.show();
        }


        @Override
        protected String doInBackground(Boolean... params) {
            // TODO Auto-generated method stub
            boolean isMultiPayment = params[0];
            try {
                if (isMultiPayment)
                    processMultiInvoicePayment();
                else
                    processPayment();
            } catch (Exception e) {
                // TODO Auto-generated catch block
//				Tracker tracker = EasyTracker.getInstance(activity);
//				tracker.send(MapBuilder.createException(e.getStackTrace().toString(), false).build());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
//			if(myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
//				showPrintDlg();
//			else
//				finish();

            if (myPref.getPreferences(MyPreferences.pref_print_receipt_transaction_payment) && !isFromMainMenu) {

                new printAsync().execute();

                if (amountToBePaid > actualAmount)
                    showChangeDlg();
            } else if (amountToBePaid > actualAmount)
                showChangeDlg();
            else
                finish();

        }
    }


    private class printAsync extends AsyncTask<Void, Void, Void> {
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
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub

//			if(isFromMainMenu||isInvoice)
//				Global.mainPrinterManager.currentDevice.printPaymentDetails(payment.getSetData("pay_id", true, null),1,false);
//			else
//				Global.mainPrinterManager.currentDevice.printPaymentDetails(payment.getSetData("job_id", true, null),0,false);
            if (Global.mainPrinterManager != null && Global.mainPrinterManager.currentDevice != null) {
                printSuccessful = Global.mainPrinterManager.currentDevice.printPaymentDetails(payment.pay_id, 1, false);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            myProgressDialog.dismiss();
            if (printSuccessful) {
                if (amountToBePaid <= actualAmount)
                    finish();
            } else {
                showPrintDlg(true);
            }

//			if(amountToBePaid<=actualAmount)
//				finish();
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

        amountToBePaid += temp;
        grandTotalAmount = amountToBePaid + amountToTip;
        paid.setText(Global.formatDoubleToCurrency(amountToBePaid));
    }

}
