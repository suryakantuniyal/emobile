package com.android.emobilepos.payment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.dao.ShiftDAO;
import com.android.database.DrawInfoHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.Device;
import com.android.emobilepos.models.realms.Payment;
import com.android.ivu.MersenneTwisterFast;
import com.android.support.DeviceUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.pax.poslink.POSLinkAndroid;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.ProcessTransResult.ProcessTransResultCode;
import com.pax.poslink.poslink.POSLinkCreator;

import java.math.BigDecimal;

import drivers.pax.utils.PosLinkHelper;
import main.EMSDeviceManager;
import util.MoneyUtils;

import static drivers.pax.utils.Constant.CARD_EXPIRED;
import static drivers.pax.utils.Constant.TRANSACTION_CANCELED;
import static drivers.pax.utils.Constant.TRANSACTION_DECLINED;
import static drivers.pax.utils.Constant.TRANSACTION_SUCCESS;
import static drivers.pax.utils.Constant.TRANSACTION_TIMEOUT;

/**
 * Created by Luis Camayd on 10/11/2018.
 */
public class ProcessPax_FA extends BaseFragmentActivityActionBar implements View.OnClickListener {

    private static final String APPROVED = "APPROVED";
    private Global global;
    private Bundle extras;
    private EditText invoiceJobIdTextView, amountTextView;
    private ProgressDialog myProgressDialog;
    private Payment payment;
    private boolean hasBeenCreated = false;
    private boolean isRefund = false;
    private MyPreferences myPref;
    private PosLink poslink;
    private static ProcessTransResult ptr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_pax_layout);
        global = (Global) getApplication();
        myPref = new MyPreferences(this);
        extras = this.getIntent().getExtras();
        invoiceJobIdTextView = findViewById(R.id.invoiceJobIdTextView);
        amountTextView = findViewById(R.id.amountTextView);

        Button btnProcess = findViewById(R.id.processButton);
        btnProcess.setOnClickListener(this);

        String inv_id;
        if (extras.getBoolean("histinvoices"))
            inv_id = extras.getString("inv_id");
        else
            inv_id = extras.getString("job_id");

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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.processButton:
                processPayment();
                break;
        }
    }

    private void processPayment() {
        String paymethod_id = extras.getString("paymethod_id");

        if (extras.getBoolean("salesrefund"))
            isRefund = true;

        payment = new Payment(this);

        if (Global.isIvuLoto) {
            DrawInfoHandler drawDateInfo = new DrawInfoHandler(this);
            MersenneTwisterFast mersenneTwister = new MersenneTwisterFast();
            payment.setIvuLottoDrawDate(drawDateInfo.getDrawDate());
            payment.setIvuLottoNumber(mersenneTwister.generateIVULoto());
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
        payment.setPay_tip("0.00");
        payment.setPay_dueamount(NumberUtils.cleanCurrencyFormatedNumber(
                amountTextView.getText().toString()));
        payment.setPay_amount(NumberUtils.cleanCurrencyFormatedNumber(
                amountTextView.getText().toString()));
        payment.setOriginalTotalAmount("0");
        payment.setPay_type("0");

        if (isRefund) {
            payment.setIs_refund("1");
            payment.setPay_type("2");
        }

        startPaxPayment();
    }

    private void startPaxPayment() {
        myProgressDialog = new ProgressDialog(this);
        myProgressDialog.setMessage(getString(R.string.processing_payment_msg));
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        myProgressDialog.setCancelable(false);
        myProgressDialog.show();

        POSLinkAndroid.init(getApplicationContext(), PosLinkHelper.getCommSetting());
        poslink = POSLinkCreator.createPoslink(getApplicationContext());
        PaymentRequest payrequest = new PaymentRequest();
        payrequest.TenderType = 1;
        payrequest.TransType = 2;
        payrequest.Amount = String.valueOf(
                MoneyUtils.convertDollarsToCents(
                        NumberUtils.cleanCurrencyFormatedNumber(amountTextView)));
        payrequest.ECRRefNum = "1";
        poslink.PaymentRequest = payrequest;
        poslink.SetCommSetting(PosLinkHelper.getCommSetting());

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
                payAmount = new BigDecimal(response.ApprovedAmount);
            }
            Global.amountPaid = String.valueOf(Global.getRoundBigDecimal(payAmount));
            payment.setPay_amount(Global.amountPaid);
            payment.setTipAmount("0.00");
            payment.setPay_tip("0.00");
            payment.setPay_transid("");
            payment.setAuthcode(response.AuthCode);
            payment.setCcnum_last4(response.BogusAccountNum);
            payment.setPay_resultcode(response.ResultCode);
            payment.setPay_resultmessage(response.Message);
            payment.setPay_name("");
            payment.setCard_type(response.CardType);
            payment.setProcessed("9");
            payment.setPaymethod_id(PayMethodsHandler.getPayMethodID(response.CardType));

            switch (response.ResultCode) {
                case TRANSACTION_SUCCESS:
                    showErrorDlog("Approved!");
                    payHandler.insert(payment);
                    String paid_amount = NumberUtils.cleanCurrencyFormatedNumber(
                            amountTextView.getText().toString());
                    result.putExtra("total_amount", paid_amount);
                    setResult(-2, result);

                    if (myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
                        showPrintDlg();
                    else {
                        finish();
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
                    showErrorDlog("Card is expired!");
                    break;
            }
        } else if (ptr.Code == ProcessTransResultCode.TimeOut) {
            showErrorDlog("Transaction TimeOut!\n" + ptr.Msg);
        } else {
            showErrorDlog("Transaction Error!\n" + ptr.Msg);
        }
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
}