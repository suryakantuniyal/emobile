package com.android.emobilepos.payment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.ShiftDAO;
import com.android.database.DrawInfoHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.DrawReceiptActivity;
import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.GroupTax;
import com.android.emobilepos.models.genius.AdditionalParameters;
import com.android.emobilepos.models.genius.ApplicationInformation;
import com.android.emobilepos.models.genius.EMV;
import com.android.emobilepos.models.genius.GeniusResponse;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Device;
import com.android.emobilepos.models.realms.Payment;
import com.android.ivu.MersenneTwisterFast;
import com.android.support.DateUtils;
import com.android.support.DeviceUtils;
import com.android.support.Global;
import com.android.support.MyEditText;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.TaxesCalculator;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.google.gson.Gson;
import com.pax.poslink.POSLinkAndroid;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.ProcessTransResult.ProcessTransResultCode;
import com.pax.poslink.poslink.POSLinkCreator;

import org.kxml2.kdom.Document;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import drivers.pax.utils.PosLinkHelper;
import interfaces.TipsCallback;
import main.EMSDeviceManager;
import util.MoneyUtils;
import util.NumberUtil;
import util.XmlUtils;

import static drivers.pax.utils.Constant.CARD_EXPIRED;
import static drivers.pax.utils.Constant.REQUEST_TENDER_TYPE_CREDIT;
import static drivers.pax.utils.Constant.REQUEST_TENDER_TYPE_DEBIT;
import static drivers.pax.utils.Constant.TRANSACTION_CANCELED;
import static drivers.pax.utils.Constant.TRANSACTION_DECLINED;
import static drivers.pax.utils.Constant.TRANSACTION_SUCCESS;
import static drivers.pax.utils.Constant.TRANSACTION_TIMEOUT;
import static drivers.pax.utils.Constant.REQUEST_TRANSACTION_TYPE_RETURN;
import static drivers.pax.utils.Constant.REQUEST_TRANSACTION_TYPE_SALE;

/**
 * Created by Luis Camayd on 10/11/2018.
 */
public class ProcessPax_FA extends AbstractPaymentFA implements View.OnClickListener, TipsCallback {
    private String orderSubTotal;
    private static final String APPROVED = "APPROVED";
    private Global global;
    private Bundle extras;
    private EditText invoiceJobIdTextView, amountTextView;
    private RadioButton creditRadioButton;
    private ProgressDialog myProgressDialog;
    private Payment payment;
    private boolean hasBeenCreated = false;
    private boolean isRefund = false;
    private MyPreferences myPref;
    private PosLink poslink;
    private static ProcessTransResult ptr;
    private Button btnProcess;
    private TextView dlogGrandTotal;
    private String paid_amount;
    double grandTotalAmount;
    double amountToTip;

    private List<GroupTax> groupTaxRate;
    private TextView tax1Lbl;
    private TextView tax2Lbl;
    private TextView tax3Lbl;
    private boolean isFromSalesReceipt  = false;
    private boolean isFromMainMenu      = false;
    private EditText paid, amountDue, reference, tipAmount, promptTipField, subtotal, tax1, tax2, tax3;
    private TextView change;
    private String custidkey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_pax_layout);
        global = (Global) getApplication();
        myPref = new MyPreferences(this);
        extras = this.getIntent().getExtras();
        String custTaxCode;

        if (myPref.isCustSelected()) {
            custTaxCode = myPref.getCustTaxCode();
        } else {
            AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
            custTaxCode = assignEmployee.getTaxDefault();
        }
        groupTaxRate = new TaxesHandler(this).getGroupTaxRate(custTaxCode);
        isFromSalesReceipt = extras.getBoolean("isFromSalesReceipt");
        isFromMainMenu = extras.getBoolean("isFromMainMenu");
        orderSubTotal = extras.getString("ord_subtotal", "0");
        amountDue = findViewById(R.id.amountDueCashEditPax);
        subtotal = findViewById(R.id.subtotalCashEditPax);
        tax1 = findViewById(R.id.tax1CashEditPax);
        tax2 = findViewById(R.id.tax2CashEditPax);
        tax3 = findViewById(R.id.tax3CashEditPax);
        tax1Lbl = findViewById(R.id.tax1LblPax);
        tax2Lbl = findViewById(R.id.tax2LblPax);
        tax3Lbl = findViewById(R.id.tax3LblPax);
        TaxesCalculator.setIvuTaxesLabels(groupTaxRate, tax1Lbl, tax2Lbl, tax3Lbl);
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

        this.paid = findViewById(R.id.amountTextView);
        this.paid.setText(Global.formatDoubleToCurrency(0.00));
        this.paid.setSelection(5);
//        this.paid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (v.hasFocus()) {
//                    int lent = paid.getText().toString().length();
//                    Selection.setSelection(paid.getText(), lent);
//                }
//            }
//        });
//        this.paid = findViewById(R.id.paidCashEditPax);
//
//        this.paid.setText(Global.formatDoubleToCurrency(0.00));
//        this.paid.setSelection(5);
//
//
//        this.paid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (v.hasFocus()) {
//                    int lent = paid.getText().toString().length();
//                    Selection.setSelection(paid.getText(), lent);
//                }
//            }
//        });
        this.amountDue.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (v.hasFocus()) {
                    Selection.setSelection(amountDue.getText(), amountDue.getText().toString().length());
                }

            }
        });

//        change = findViewById(R.id.changeEditPax);
        invoiceJobIdTextView = findViewById(R.id.invoiceJobIdTextView);
        amountTextView = findViewById(R.id.amountTextView);
        creditRadioButton = findViewById(R.id.creditRadioButton);

        btnProcess = findViewById(R.id.processButton);
        btnProcess.setOnClickListener(this);

        String inv_id;
        if (extras.getBoolean("histinvoices"))
            inv_id = extras.getString("inv_id");
        else
            inv_id = extras.getString("job_id");

        // listeners
        this.amountDue.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                //recalculateChange();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NumberUtils.parseInputedCurrency(s, amountDue);
            }
        });

//        this.tipAmount.addTextChangedListener(new TextWatcher() {
//            public void afterTextChanged(Editable s) {
//                recalculateChange();
//            }
//
//
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                NumberUtils.parseInputedCurrency(s, tipAmount);
//            }
//        });

//        this.paid.addTextChangedListener(new TextWatcher() {
//            public void afterTextChanged(Editable s) {
//                if (!paid.getText().toString().isEmpty())
//                    recalculateChange();
//            }
//
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                NumberUtils.parseInputedCurrency(s, paid);
//            }
//        });
        // end of listeners

        if (!Global.isIvuLoto || isFromSalesReceipt) {
            findViewById(R.id.ivuposRow1).setVisibility(View.GONE);
            findViewById(R.id.ivuposRow2).setVisibility(View.GONE);
            findViewById(R.id.ivuposRow3).setVisibility(View.GONE);
            findViewById(R.id.ivuposRow4).setVisibility(View.GONE);
        } else {
            setIVUPOSFieldListeners();
        }
        subtotal.setText(Global.getCurrencyFormat(orderSubTotal));

        invoiceJobIdTextView.setEnabled(extras.getBoolean("isFromMainMenu", false));
        invoiceJobIdTextView.setText(inv_id);

        amountTextView.setText(
                Global.getCurrencyFormat(
                        Global.formatNumToLocale(
                                Double.parseDouble(extras.getString("amount")))));
        amountTextView.addTextChangedListener(getTextWatcher(amountTextView));
        amountTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Selection.setSelection(
                            amountTextView.getText(), amountTextView.getText().length());
                }
            }
        });

        hasBeenCreated = true;

        if (extras.containsKey("isReopen")) {
            finish();
        }

        isRefund = extras.getBoolean("salesrefund", false);

        if (isRefund) {
            CheckBox refundCheckBox = findViewById(R.id.refundCheckBox);
            refundCheckBox.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.processButton:
                if(myPref.getPreferences("pref_show_confirmation_screen")){
                    btnProcess.setEnabled(false);
                    promptAmountConfirmation(this);
                }
                else{
                    processPayment(view.getContext());
                }
                break;
        }
    }

    private void processPayment(Context context) {
        String paymethod_id = extras.getString("paymethod_id");
        payment = new Payment(this);

        if (Global.isIvuLoto) {
            DrawInfoHandler drawDateInfo = new DrawInfoHandler(this);
            MersenneTwisterFast mersenneTwister = new MersenneTwisterFast();
            payment.setIvuLottoDrawDate(drawDateInfo.getDrawDate());
            payment.setIvuLottoNumber(mersenneTwister.generateIVULoto());
            Global.subtotalAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(subtotal));
        }

        if (!this.extras.getBoolean("histinvoices"))
            payment.setJob_id(invoiceJobIdTextView.getText().toString());
        else
            payment.setInv_id(invoiceJobIdTextView.getText().toString());

        if (myPref.isUseClerks()) {
            payment.setClerk_id(myPref.getClerkID());
        } else {
            if (ShiftDAO.isShiftOpen() && ShiftDAO.getOpenShift() != null) {
                payment.setClerk_id(String.valueOf(ShiftDAO.getOpenShift().getClerkId()));
            }
        }

        if (myPref.isCustSelected()) {
            payment.setCust_id(myPref.getCustID());
        }

        payment.setPay_id(extras.getString("pay_id"));
        payment.setPaymethod_id(paymethod_id);
        payment.setPay_expmonth("0");// dummy
        payment.setPay_expyear("2000");// dummy
        payment.setOriginalTotalAmount("0");
        payment.setPay_type("0");
        if(myPref.getPreferences(MyPreferences.pref_show_confirmation_screen)) {
            payment.setPay_tip(NumberUtils.cleanCurrencyFormatedNumber(Global.formatDoubleToCurrency(amountToTip)));
        }
        else {
            payment.setPay_tip("0.00");
        }
//        payment.setPay_dueamount(NumberUtils.cleanCurrencyFormatedNumber(String.valueOf(grandTotalAmount)));
//        payment.setPay_amount(NumberUtils.cleanCurrencyFormatedNumber(String.valueOf(grandTotalAmount)));
        payment.setPay_dueamount(NumberUtils.cleanCurrencyFormatedNumber(amountTextView.getText().toString()));
        payment.setPay_amount(NumberUtils.cleanCurrencyFormatedNumber(amountTextView.getText().toString()));
        // taxes
        payment.setTax1_amount(NumberUtils.cleanCurrencyFormatedNumber(tax1.getText().toString()));
        payment.setTax2_amount(NumberUtils.cleanCurrencyFormatedNumber(tax2.getText().toString()));
        payment.setTax3_amount(NumberUtils.cleanCurrencyFormatedNumber(tax3.getText().toString()));
        payment.setTax1_name(tax1Lbl.getText().toString());
        payment.setTax2_name(tax2Lbl.getText().toString());
        payment.setTax3_name(tax3Lbl.getText().toString());

        if (isRefund) {
            payment.setIs_refund("1");
            payment.setPay_type("2");
        }

        startPaxPayment(context);
    }

    private void startPaxPayment(Context context) {
        myProgressDialog = new ProgressDialog(context);
        myProgressDialog.setMessage(getString(R.string.processing_payment_msg));
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        myProgressDialog.setCancelable(false);
        myProgressDialog.show();

        POSLinkAndroid.init(getApplicationContext(), PosLinkHelper.getCommSetting(myPref.getPaymentDevice(),myPref.getPaymentDeviceIP()));
        poslink = POSLinkCreator.createPoslink(getApplicationContext());
        PaymentRequest payrequest = new PaymentRequest();
        if (creditRadioButton.isChecked()) {
            payrequest.TenderType = REQUEST_TENDER_TYPE_CREDIT;
        } else {
            payrequest.TenderType = REQUEST_TENDER_TYPE_DEBIT;
        }

        if (!isRefund) {
            payrequest.TransType = REQUEST_TRANSACTION_TYPE_SALE;
        } else {
            payrequest.TransType = REQUEST_TRANSACTION_TYPE_RETURN;
        }

        if(myPref.getPreferences(MyPreferences.pref_show_confirmation_screen)){
            payrequest.TipAmt = String.valueOf(MoneyUtils.convertDollarsToCents(
                    NumberUtils.cleanCurrencyFormatedNumber(Global.formatDoubleToCurrency(amountToTip))));
        }
        payrequest.Amount = String.valueOf(MoneyUtils.convertDollarsToCents(
                NumberUtils.cleanCurrencyFormatedNumber(amountTextView)));

        payrequest.ECRRefNum = DateUtils.getEpochTime();
        poslink.PaymentRequest = payrequest;
        poslink.SetCommSetting(PosLinkHelper.getCommSetting(myPref.getPaymentDevice(),myPref.getPaymentDeviceIP()));
        payment.setPay_stamp(String.valueOf(payrequest.TenderType));

        // as processTrans is blocked, we must run it in an async task
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    // ProcessTrans is Blocking call, will return when the transaction is complete.
                    ptr = poslink.ProcessTrans();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processResponse();
                    }
                });
            }
        }).start();
    }

    private void processResponse() {
        Global.dismissDialog(ProcessPax_FA.this, myProgressDialog);

        if (ptr.Code == ProcessTransResultCode.OK) {
            PaymentResponse response = poslink.PaymentResponse;
            Intent result = new Intent();
            PaymentsHandler payHandler = new PaymentsHandler(ProcessPax_FA.this);
            BigDecimal payAmount = BigDecimal.ZERO;
            if (!response.ApprovedAmount.isEmpty()) {
                payAmount = MoneyUtils.convertCentsToDollars(response.ApprovedAmount);
            }
            Global.amountPaid = String.valueOf(Global.getRoundBigDecimal(payAmount));
            payment.setPay_amount(Global.amountPaid);
            payment.setTipAmount("0.00");
            payment.setPay_tip("0.00");
            payment.setPay_transid(response.RefNum);
            payment.setAuthcode(response.AuthCode);
            payment.setCcnum_last4(response.BogusAccountNum);
            payment.setPay_resultcode(response.ResultCode);
            payment.setPay_resultmessage(response.Message);
            payment.setPay_name("");
            payment.setCard_type(PosLinkHelper.payMethodDictionary(response.CardType));
            payment.setProcessed("1");
            payment.setPaymethod_id(PayMethodsHandler.getPayMethodID(response.CardType));

            // Set EMV
            ApplicationInformation applicationInformation = new ApplicationInformation();
            applicationInformation.setAid(
                    XmlUtils.findXMl(poslink.PaymentResponse.ExtData, "AID"));
            applicationInformation.setApplicationLabel(
                    XmlUtils.findXMl(poslink.PaymentResponse.ExtData, "APPLAB"));

            EMV emv = new EMV();
            emv.setApplicationInformation(applicationInformation);
            emv.setPINStatement(
                    PosLinkHelper.getCvmMessage(
                            XmlUtils.findXMl(poslink.PaymentResponse.ExtData, "CVM")));
            emv.setEntryModeMessage(
                    PosLinkHelper.getEntryModeValue(
                            XmlUtils.findXMl(poslink.PaymentResponse.ExtData, "PLEntryMode")));
            emv.setTVR(
                    XmlUtils.findXMl(poslink.PaymentResponse.ExtData, "TVR"));
            emv.setIAD(
                    XmlUtils.findXMl(poslink.PaymentResponse.ExtData, "IAD"));
            emv.setTSI(
                    XmlUtils.findXMl(poslink.PaymentResponse.ExtData, "TSI"));
            emv.setAC(
                    XmlUtils.findXMl(poslink.PaymentResponse.ExtData, "AC"));

            AdditionalParameters additionalParameters = new AdditionalParameters();
            additionalParameters.setEMV(emv);

            GeniusResponse geniusResponse = new GeniusResponse();
            geniusResponse.setStatus("");
            geniusResponse.setPaymentType("");
            geniusResponse.setAdditionalParameters(additionalParameters);

            payment.setEmvContainer(new EMVContainer(geniusResponse));

            switch (response.ResultCode) {
                case TRANSACTION_SUCCESS:
                    payHandler.insert(payment);
                    paid_amount = NumberUtils.cleanCurrencyFormatedNumber(
                            amountTextView.getText().toString());

                    payment.getEmvContainer().getGeniusResponse().setStatus(APPROVED);

                    if(myPref.getPreferences(MyPreferences.pref_use_pax_signature))
                    {
                        Intent intent = new Intent(this, DrawReceiptActivity.class);
                        intent.putExtra("isFromPayment", true);
                        startActivityForResult(intent, 0);
                    }else {
                        result.putExtra("total_amount", paid_amount);
                        result.putExtra("emvcontainer",
                                new Gson().toJson(payment.getEmvContainer(), EMVContainer.class));
                        setResult(-2, result);

                        if (myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
                            showPrintDlg();
                        else {
                            finish();
                        }
                    }
                    break;
                case TRANSACTION_DECLINED:
                    payHandler.insertDeclined(payment);
                    setResult(0, result);
                    showErrorDlog("Transaction Declined!");
                    break;
                case TRANSACTION_TIMEOUT:
                    showErrorDlog("Transaction TimeOut!");
                    break;
                case TRANSACTION_CANCELED:
                    showErrorDlog("Transaction Canceled!");
                    break;
                case CARD_EXPIRED:
                    showErrorDlog("Card is invalid or expired!");
                    break;
            }
        } else if (ptr.Code == ProcessTransResultCode.TimeOut) {
            showErrorDlog("Transaction TimeOut!\n" + ptr.Msg);
        } else {
            showErrorDlog("Transaction Error!\n" + ptr.Msg);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == -1){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            Intent result = new Intent();
            result.putExtra("total_amount", paid_amount);
            result.putExtra("emvcontainer", new Gson().toJson(payment.getEmvContainer(), EMVContainer.class));
            setResult(-2, result);
            if (myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
               showPrintDlg();
            else {
                finish();
            }
        }
    }

    private void promptAmountConfirmation(Activity activity) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogLayout = inflater.inflate(R.layout.confirmation_amount_layout, null);

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog dialog = builder.create();
        dialog.setView(dialogLayout, 0, 0, 0, 0);
        dialog.setInverseBackgroundForced(true);
        dialog.setCancelable(false);
        dlogGrandTotal = dialogLayout.findViewById(R.id.confirmTotalView);
        LinearLayout cardDetails = dialogLayout.findViewById(R.id.cardDetailsLayout);
        cardDetails.setVisibility(View.GONE);

        grandTotalAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountTextView.getText().toString()));
        dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));

        Button cancelButton = dialogLayout.findViewById(R.id.cancelButton);
        Button nextButton = dialogLayout.findViewById(R.id.nextButton);

        cancelButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                btnProcess.setEnabled(true);
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

    private void promptTipConfirmation() {
        SelectPayMethod_FA.GratuityManager gm = new SelectPayMethod_FA.GratuityManager(this,this,myPref,global,extras.getBoolean("isFromMainMenu", false));
        gm.showTipsForPaxPayments(amountTextView);
    }

    private void showPrintDlg() {
        final Dialog dlog = new Dialog(ProcessPax_FA.this, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_left_right_layout);

        TextView viewTitle = dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_confirm);
        viewMsg.setText(R.string.dlog_msg_print_cust_copy);

        dlog.findViewById(R.id.btnDlogCancel).setVisibility(View.GONE);

        Button btnYes = dlog.findViewById(R.id.btnDlogLeft);
        Button btnNo = dlog.findViewById(R.id.btnDlogRight);
        Button btnCancel = dlog.findViewById(R.id.btnDlogCancel);
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

    private void showErrorDlog(String msg) {
        final Dialog dlog = new Dialog(this, R.style.Theme_TransparentTest);
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
                finish();
            }
        });
        dlog.show();
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
        global.startActivityTransitionTimer();
    }

    @Override
    public void noneTipGratuityWasPressed(TextView totalAmountView, TextView dlogGrandTotal, double subTotal) {
        amountToTip = 0;
        grandTotalAmount = subTotal;
        dlogGrandTotal.setText(Global.formatDoubleToCurrency(grandTotalAmount));
        totalAmountView.setText(String.format(Locale.getDefault(), getString(R.string.total_plus_tip),
                Global.formatDoubleToCurrency(subTotal), Global.formatDoubleToCurrency(amountToTip)));
    }

    @Override
    public void cancelTipGratuityWasPressed(AlertDialog dialog) {
        double amountToBePaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountTextView.getText().toString()));
        amountToTip = 0;
        grandTotalAmount = amountToBePaid;
        btnProcess.setEnabled(true);
        dialog.dismiss();
    }

    @Override
    public void saveTipGratuityWasPressed(AlertDialog dialog, double amountToTip) {
        dialog.dismiss();
        processPayment(this);
    }

    private class printAsync extends AsyncTask<Void, Void, Void> {
        private boolean printSuccessful = true;
        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(ProcessPax_FA.this);
            myProgressDialog.setMessage(getString(R.string.printing_message));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            EMSDeviceManager emsDeviceManager = DeviceUtils.getEmsDeviceManager(Device.Printables.PAYMENT_RECEIPT, Global.printerDevices);
            if (emsDeviceManager != null && emsDeviceManager.getCurrentDevice() != null) {
                printSuccessful = emsDeviceManager.getCurrentDevice().printPaymentDetails(payment.getPay_id(), 1, false, payment.getEmvContainer());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void unused) {
            myProgressDialog.dismiss();
            if (printSuccessful)
                finish();
            else {
                showPrintDlg();
            }
        }
    }
//    private void recalculateChange() {
//        //amountToTip = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(tipAmount));
//        double totAmount = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(amountDue));
//        double totalPaid = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(paid));
//
//        if (totalPaid > totAmount) {
//            double tempTotal = Math.abs(totAmount - totalPaid);
//            change.setText(Global.getCurrencyFormat(Global.formatNumToLocale(tempTotal)));
//        } else {
//            change.setText(Global.formatDoubleToCurrency(0.00));
//        }
//
//    }
    private void setIVUPOSFieldListeners() {
        try{
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
                    //recalculateChange();
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
                    //recalculateChange();
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    NumberUtils.parseInputedCurrency(s, tax1);
                }
            });
            tax2.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    System.out.println("13");
                    calculateAmountDue(subtotal, tax1, tax2, tax3, amountDue);
                    System.out.println("14");
                    //recalculateChange();
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
                    //recalculateChange();
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    NumberUtils.parseInputedCurrency(s, tax3);
                }
            });
        }catch (Exception x){
            x.printStackTrace();
        }
    }
}