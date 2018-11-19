package com.android.emobilepos.payment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dao.ShiftDAO;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.pax.SoundPaymentsResponse;
import com.android.emobilepos.models.realms.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.math.BigDecimal;

import util.XmlUtils;

import static com.android.payments.EMSPayGate_Default.EAction.SoundPaymentsCharge;
import static com.android.payments.EMSPayGate_Default.EAction.SoundPaymentsRefund;

/**
 * Created by Luis Camayd on 10/11/2018.
 */
public class ProcessPax_FA extends BaseFragmentActivityActionBar implements View.OnClickListener {

    private static final String APPROVED = "APPROVED";
    private Global global;
    private String paymethod_id;
    private Bundle extras;
    private EditText invoiceJobIdTextView, amountTextView;
    private ProgressDialog myProgressDialog;
    private Payment payment;
    private boolean hasBeenCreated = false;
    private boolean isRefund = false;
    private boolean isMultiInvoice = false;
    private MyPreferences myPref;

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
                Toast.makeText(this, getString(R.string.processing_payment_msg),
                        Toast.LENGTH_LONG).show();
                processPayment();
                break;
        }
    }

    private void processPayment() {
        paymethod_id = extras.getString("paymethod_id");

        if (extras.getBoolean("salesrefund"))
            isRefund = true;

        payment = new Payment(this);

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

        EMSPayGate_Default payGate = new EMSPayGate_Default(this, payment);
        String requestXml;

        if (isRefund) {
            payment.setIs_refund("1");
            payment.setPay_type("2");
            requestXml = payGate.paymentWithAction(
                    SoundPaymentsRefund, false, "", null);
        } else {
            requestXml = payGate.paymentWithAction(
                    SoundPaymentsCharge, false, "", null);
        }

        sendRequest(requestXml);
    }

    private void processResponse(String response) {
        SoundPaymentsResponse spResponse = XmlUtils.getSoundPaymentsResponse(response);
        Intent result = new Intent();
        PaymentsHandler payHandler = new PaymentsHandler(ProcessPax_FA.this);

        payment.setPay_transid(spResponse.getCreditCardTransID());
        payment.setTipAmount("0.00");
        payment.setPay_tip("0.00");

        BigDecimal payAmount = new BigDecimal(spResponse.getAuthorizedAmount());
        payment.setPay_amount(String.valueOf(Global.getRoundBigDecimal(payAmount)));
        Global.amountPaid = payment.getPay_amount();

        payment.setAuthcode(spResponse.getAuthorizationCode());
        payment.setCcnum_last4(spResponse.getCCLast4());
        payment.setPay_resultcode(spResponse.getPay_resultcode());
        payment.setPay_resultmessage(spResponse.getPay_resultmessage());
        payment.setPay_name("");
        payment.setCard_type(spResponse.getCardType());
        payment.setProcessed("9");
        payment.setPaymethod_id(PayMethodsHandler.getPayMethodID(spResponse.getCardType()));

        Global.dismissDialog(ProcessPax_FA.this, myProgressDialog);

        if (spResponse.getEpayStatusCode().equalsIgnoreCase(APPROVED)) {


//            payment.setEmvContainer(new EMVContainer(response));


            payHandler.insert(payment);


//            EMVContainer emvContainer = new EMVContainer(response);


            String paid_amount = NumberUtils.cleanCurrencyFormatedNumber(
                    amountTextView.getText().toString());

            result.putExtra("total_amount", paid_amount);

//            result.putExtra("emvcontainer", new Gson().toJson(emvContainer, EMVContainer.class));

            setResult(-2, result);

            if (myPref.getPreferences(MyPreferences.pref_prompt_customer_copy))
                showPrintDlg();
            else {
                finish();
            }
        } else {
            payHandler.insertDeclined(payment);
            setResult(0, result);
            String errorMsg = String.format(getString(R.string.error_status_code),
                    spResponse.getStatusCode(), spResponse.getStatusMessage());
            showErrorDlog(errorMsg);
        }
    }

    private void sendRequest(final String request) {
        myProgressDialog = new ProgressDialog(this);
        myProgressDialog.setMessage(getString(R.string.processing_payment_msg));
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        myProgressDialog.setCancelable(false);
        myProgressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Post post = new Post(ProcessPax_FA.this);
                final String response = post.postData(Global.S_SUBMIT_SOUNDPAYMENTS, request);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processResponse(response);
                    }
                });

            }
        }).start();
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
//                printAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (dialog != null) {
//            dialog.create().dismiss();
//        }
//    }
}