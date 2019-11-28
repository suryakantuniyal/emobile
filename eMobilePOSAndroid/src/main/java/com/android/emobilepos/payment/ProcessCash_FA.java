package com.android.emobilepos.payment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.ShiftDAO;
import com.android.database.CustomersHandler;
import com.android.database.InvoicePaymentsHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.GroupTax;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Device;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.DeviceUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.TaxesCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import main.EMSDeviceManager;
import util.json.UIUtils;

public class ProcessCash_FA extends AbstractPaymentFA implements OnClickListener {
    String orderSubTotal;
    //    private ProgressDialog myProgressDialog;
    private AlertDialog.Builder dialog;
    private Context thisContext = this;
    private Activity activity = this;
    //    private Payment payment;
    private Global global;
    private boolean hasBeenCreated = false;
    private String inv_id;
    private boolean isFromSalesReceipt = false;
    private boolean isFromMainMenu = false;
    private EditText paid, amountDue, reference, tipAmount, promptTipField, subtotal, tax1, tax2, tax3;
    private EditText customerNameField, customerEmailField, phoneNumberField, commentsField;
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
    private TextView tax1Lbl;
    private TextView tax2Lbl;
    private TextView tax3Lbl;


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
            AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
            custTaxCode = assignEmployee.getTaxDefault();
        }
        groupTaxRate = new TaxesHandler(this).getGroupTaxRate(custTaxCode);
        if (!myPref.getPreferences(MyPreferences.pref_show_tips_for_cash)) {
            showTipField = false;
            LinearLayout layout = findViewById(R.id.tipFieldMainHolder);
            layout.setVisibility(View.GONE);
        }
        TextView headerTitle = findViewById(R.id.HeaderTitle);
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
        orderSubTotal = extras.getString("ord_subtotal", "0");
        amountDue = findViewById(R.id.amountDueCashEdit);
        reference = findViewById(R.id.referenceNumber);
        tipAmount = findViewById(R.id.tipAmountField);
        subtotal = findViewById(R.id.subtotalCashEdit);
        tax1 = findViewById(R.id.tax1CashEdit);
        tax2 = findViewById(R.id.tax2CashEdit);
        tax3 = findViewById(R.id.tax3CashEdit);
        tax1Lbl = findViewById(R.id.tax1CashLbl);
        tax2Lbl = findViewById(R.id.tax2CashLbl);
        tax3Lbl = findViewById(R.id.tax3CashLbl);
        TaxesCalculator.setIvuTaxesLabels(groupTaxRate, tax1Lbl, tax2Lbl, tax3Lbl);
        customerNameField = findViewById(R.id.processCashName);
        customerEmailField = findViewById(R.id.processCashEmail);
        phoneNumberField = findViewById(R.id.processCashPhone);
        commentsField = findViewById(R.id.commentsCashEdit);

        Button btnFive = findViewById(R.id.btnFive);
        Button btnTen = findViewById(R.id.btnTen);
        Button btnTwenty = findViewById(R.id.btnTwenty);
        Button btnFifty = findViewById(R.id.btnFifty);
        btnFive.setOnClickListener(this);
        btnTen.setOnClickListener(this);
        btnTwenty.setOnClickListener(this);
        btnFifty.setOnClickListener(this);

        if (showTipField)
            this.tipAmount.setText(Global.formatDoubleToCurrency(0.00));

        amountDue.setText(Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));

        if (!isFromMainMenu && global.order != null) {
            TaxesCalculator.setIvuTaxesFields(global.order, tax1, tax2, tax3);
        }
        custidkey = extras.getString("custidkey");
        if (custidkey == null)
            custidkey = "";

        if (!isFromMainMenu || Global.isIvuLoto) {
            amountDue.setEnabled(false);
        }

        this.paid = findViewById(R.id.paidCashEdit);

        this.paid.setText(Global.formatDoubleToCurrency(0.00));
        this.paid.setSelection(5);


        this.paid.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.hasFocus()) {
                    int lent = paid.getText().toString().length();
                    Selection.setSelection(paid.getText(), lent);
                }
            }
        });
        this.amountDue.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.hasFocus()) {
                    Selection.setSelection(amountDue.getText(), amountDue.getText().toString().length());
                }

            }
        });

        change = findViewById(R.id.changeCashText);

        Button exactBut = findViewById(R.id.exactAmountBut);
        btnProcess = findViewById(R.id.processCashBut);

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
                if (UIUtils.singleOnClick(v)) {
                    btnProcess.setEnabled(false);
                    double enteredAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
                    double amountDueDbl = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));
                    if (enteredAmount <= 0 && (amountDueDbl > 0 || isFromMainMenu)) {
                        paid.setBackgroundResource(R.drawable.edittext_wrong_input);
                        Global.showPrompt(activity, R.string.validation_failed, activity.getString(R.string.error_wrong_amount));
                    } else {
                        paid.setBackgroundResource(R.drawable.edittext_border);

                        if (Global.mainPrinterManager != null && Global.mainPrinterManager.getCurrentDevice() != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Global.mainPrinterManager.getCurrentDevice().openCashDrawer();
                                }
                            }).start();
                        }

                        if (!isInvoice || (isInvoice && !isMultiInvoice))
                            new processPaymentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
                        else {
                            new processPaymentAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
                        }
                    }
                    btnProcess.setEnabled(true);
                }
            }
        });

        exactBut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
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
                NumberUtils.parseInputedCurrency(s, amountDue);
            }
        });

        this.tipAmount.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                recalculateChange();
            }


            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NumberUtils.parseInputedCurrency(s, tipAmount);
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
                NumberUtils.parseInputedCurrency(s, paid);
            }
        });


        if (!Global.isIvuLoto || isFromSalesReceipt) {
            findViewById(R.id.ivuposRow1).setVisibility(View.GONE);
            findViewById(R.id.ivuposRow2).setVisibility(View.GONE);
            findViewById(R.id.ivuposRow3).setVisibility(View.GONE);
            findViewById(R.id.ivuposRow4).setVisibility(View.GONE);
        } else {
            setIVUPOSFieldListeners();
        }
        subtotal.setText(Global.getCurrencyFormat(orderSubTotal));
        if (showTipField) {
            Button tipButton = findViewById(R.id.tipAmountBut);
            tipButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    promptTipConfirmation();
                }
            });
        }


        if (!Global.getValidString(extras.getString("cust_id")).isEmpty()) {

            CustomersHandler handler2 = new CustomersHandler(activity);
            HashMap<String, String> customerInfo = handler2.getCustomerMap(extras.getString("cust_id"));

            if (customerInfo != null) {
                if (customerInfo.containsKey("cust_name") &&
                        !customerInfo.get("cust_name").isEmpty())
                    customerNameField.setText(customerInfo.get("cust_name"));
                if (customerInfo.containsKey("cust_phone") &&
                        !customerInfo.get("cust_phone").isEmpty())
                    phoneNumberField.setText(customerInfo.get("cust_phone"));
                if (customerInfo.containsKey("cust_email") &&
                        !customerInfo.get("cust_email").isEmpty())
                    customerEmailField.setText(customerInfo.get("cust_email"));
            }
        }

        if (!myPref.isSkipEmailPhone() && isFromSalesReceipt) {
            customerEmailField.setText(extras.getString("order_email", ""));
            phoneNumberField.setText(extras.getString("order_phone", ""));
        }

        hasBeenCreated = true;
    }

    private void setIVUPOSFieldListeners() {
        subtotal.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.hasFocus()) {
                    Selection.setSelection(subtotal.getText(), subtotal.getText().toString().length());
                }

            }
        });
        tax1.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.hasFocus()) {
                    Selection.setSelection(tax1.getText(), tax1.getText().toString().length());
                }

            }
        });
        tax2.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.hasFocus()) {
                    NumberUtils.parseInputedCurrency(tax2.getText().toString(), tax2);
                }

            }
        });
        tax3.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.hasFocus()) {
                    NumberUtils.parseInputedCurrency(tax3.getText().toString(), tax3);
                }

            }
        });
        subtotal.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
//                NumberUtils.parseInputedCurrency(s, subtotal);
                if (isFromMainMenu) {
                    TaxesCalculator.setIvuTaxesFields(groupTaxRate, subtotal, tax1, tax2, tax3);
                    calculateAmountDue(subtotal, tax1, tax2, tax3, amountDue);
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
                calculateAmountDue(subtotal, tax1, tax2, tax3, amountDue);
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
                calculateAmountDue(subtotal, tax1, tax2, tax3, amountDue);
                recalculateChange();

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NumberUtils.parseInputedCurrency(s, tax2);
            }
        });
        tax3.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                calculateAmountDue(subtotal, tax1, tax2, tax3, amountDue);
                recalculateChange();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NumberUtils.parseInputedCurrency(s, tax3);
            }
        });
    }

    private void recalculateChange() {
        amountToTip = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(tipAmount));
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
        SelectPayMethod_FA.GratuityManager gm = new SelectPayMethod_FA.GratuityManager(activity,myPref,global,isFromMainMenu);
        gm.showTipsForCashPayments(amountDue, promptTipField, paid, orderSubTotal);
    }

    private Payment processPayment() {
        PaymentsHandler payHandler = new PaymentsHandler(activity);
        double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));
        double amountTender = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));

        if (Global.isIvuLoto) {
            Global.subtotalAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(subtotal));
        }
        String clerkId = null;
        String jobId = null;
        String invoiceId = null;
        if (!extras.getBoolean("histinvoices")) {
            jobId = inv_id;
        } else {
            invoiceId = inv_id;
        }

        if (myPref.isUseClerks()) {
            clerkId = myPref.getClerkID();
        } else if (ShiftDAO.isShiftOpen()) {
            clerkId = String.valueOf(ShiftDAO.getOpenShift().getClerkId());
        }


        if (showTipField) {
            Global.tipPaid = Double.toString(amountToTip);
        }

        String taxAmnt1 = null;
        String taxName1 = null;
        String taxAmnt2 = null;
        String taxName2 = null;
        String taxAmnt3 = null;
        String taxName3 = null;
        if (Global.isIvuLoto) {
            if (extras.getBoolean("isFromSalesReceipt")) {
                int counter = 0;
                OrdersHandler orderHandler = new OrdersHandler(activity);
                Order mOrder = orderHandler.getPrintedOrder(extras.getString("job_id"));
                List<DataTaxes> taxesList = mOrder.getListOrderTaxes();
                HashMap<String, String[]> arr = TaxesCalculator.getOrderTaxes(thisContext,taxesList, mOrder);
                Iterator it = arr.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String[]> pair = (Map.Entry<String, String[]>) it.next();
                    if(counter==0) {
                        taxName1 = pair.getValue()[0];
                        taxAmnt1 = String.valueOf(pair.getValue()[1]);
                    }else if(counter==1){
                        taxName2 = pair.getValue()[0];
                        taxAmnt2 = String.valueOf(pair.getValue()[1]);
                    }
                    else {
                        taxName3 = pair.getValue()[0];
                        taxAmnt3 = String.valueOf(pair.getValue()[1]);
                    }
                    counter++;
                    it.remove();
                }
            } else {
                taxAmnt1 = NumberUtils.cleanCurrencyFormatedNumber(tax1);
                taxName1 = tax1Lbl.getText().toString();
                taxAmnt2 = NumberUtils.cleanCurrencyFormatedNumber(tax2);
                taxName2 = tax2Lbl.getText().toString();
                taxAmnt3 = NumberUtils.cleanCurrencyFormatedNumber(tax3);
                taxName3 = tax3Lbl.getText().toString();
            }
        }

        String isRef = null;
        String paymentType;
        if (extras.getBoolean("salesrefund", false)) {
            isRef = "1";
            paymentType = "2";
        } else
            paymentType = "0";

        Payment payment = new Payment(activity,
                extras.getString("pay_id"),
                extras.getString("cust_id"),
                invoiceId,
                jobId,
                clerkId,
                custidkey,
                extras.getString("paymethod_id"),
                actualAmount,
                amountTender,
                customerNameField.getText().toString(),
                reference.getText().toString(),
                phoneNumberField.getText().toString(),
                customerEmailField.getText().toString(),
                commentsField.getText().toString(),
                amountToTip,
                taxAmnt1,
                taxAmnt2,
                taxAmnt3,
                taxName1,
                taxName2,
                taxName3,
                isRef,
                paymentType,
                "Cash",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        Global.amountPaid = Double.toString(amountTender);

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
        List<Double> appliedAmount = new ArrayList<>();
        List<String[]> contentList = new ArrayList<>();
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
        if (myPref.isUseClerks()) {
            clerkId = myPref.getClerkID();
        } else if (ShiftDAO.isShiftOpen()) {
            clerkId = String.valueOf(ShiftDAO.getOpenShift().getClerkId());
        }
        String invoiceId = "";

        String paymentType = "0";
        Payment payment = new Payment(activity,
                extras.getString("pay_id"),
                extras.getString("cust_id"),
                invoiceId,
                null,
                clerkId,
                custidkey,
                extras.getString("paymethod_id"),
                actualAmount,
                amountToBePaid,
                customerNameField.getText().toString(),
                reference.getText().toString(),
                phoneNumberField.getText().toString(),
                customerEmailField.getText().toString(),
                commentsField.getText().toString(),
                amountToTip,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                paymentType,
                "Cash",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        payHandler.insert(payment);
        if (!myPref.getLastPayID().isEmpty())
            myPref.setLastPayID("0");


        updateShiftAmount();

        setResult(-2);
        return payment;
    }


    private void updateShiftAmount() {
        if (ShiftDAO.isShiftOpen()) {
            double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));
            double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
            double amountToApply;
            boolean isReturn = false;
            if (Global.ord_type == Global.OrderType.RETURN || isRefund)
                isReturn = true;
            if (amountToBePaid <= actualAmount) {
                amountToApply = amountToBePaid;
            } else {
                amountToApply = actualAmount;
            }
            ShiftDAO.updateShiftAmounts(amountToApply, isReturn);
        }

    }

    private void showPrintDlg(boolean isRetry, final Payment payment) {
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
                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, payment);

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
        if (myPref.isShowCashChangeAmount()) {
            final Dialog dlog = new Dialog(activity, R.style.Theme_TransparentTest);
            dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlog.setCancelable(false);
            dlog.setContentView(R.layout.dlog_btn_single_layout);

            TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
            TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
            viewTitle.setText(R.string.dlog_title_confirm);
            //sb.append(getString(R.string.dlog_msg_print_cust_copy)).append("\n\n");
            viewMsg.setText(String.format(Locale.getDefault(), "%s%s", getString(R.string.changeLbl), change.getText().toString()));
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

        } else {
            Toast.makeText(this, getString(R.string.changeLbl) + change.getText().toString(), Toast.LENGTH_LONG).show();
            finish();
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
//        DeviceUtils.registerFingerPrintReader(this);
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
//        DeviceUtils.unregisterFingerPrintReader(this);
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

    private class processPaymentAsync extends AsyncTask<Boolean, String, Payment> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(thisContext);
            progressDialog.setMessage(getString(R.string.processing_payment_msg));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            if (activity != null && !activity.isFinishing() && progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.show();
            }
        }


        @Override
        protected Payment doInBackground(Boolean... params) {
            Payment payment;
            boolean isMultiPayment = params[0];
            if (isMultiPayment)
                payment = processMultiInvoicePayment();
            else
                payment = processPayment();

            return payment;
        }

        @Override
        protected void onPostExecute(Payment payment) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
            double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));

            if (myPref.getPreferences(MyPreferences.pref_print_receipt_transaction_payment) && !isFromMainMenu) {

                new printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, payment);

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
        private ProgressDialog myProgressDialog;

        @Override
        protected void onPreExecute() {
            Global.lockOrientation(activity);
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(getString(R.string.printing_message));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            if (myProgressDialog.isShowing())
                myProgressDialog.dismiss();
            if (activity != null && !activity.isFinishing() && myProgressDialog != null && !myProgressDialog.isShowing()) {
                myProgressDialog.show();
            }


        }

        @Override
        protected Payment doInBackground(Payment... params) {
            EMSDeviceManager emsDeviceManager = DeviceUtils.getEmsDeviceManager(Device.Printables.PAYMENT_RECEIPT, Global.printerDevices);
            if (emsDeviceManager != null && emsDeviceManager.getCurrentDevice() != null) {
                printSuccessful = emsDeviceManager.getCurrentDevice().printPaymentDetails(params[0].getPay_id(), 1, false, null);
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(Payment payment) {
            Global.dismissDialog(activity, myProgressDialog);
            Global.releaseOrientation(activity);
            double actualAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));
            double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));

            if (printSuccessful) {
                if (amountToBePaid <= actualAmount)
                    finish();
            } else {
                showPrintDlg(true, payment);
            }
        }
    }

}
