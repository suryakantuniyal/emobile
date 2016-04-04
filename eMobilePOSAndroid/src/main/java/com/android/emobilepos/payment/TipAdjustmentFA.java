package com.android.emobilepos.payment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.emobilepos.models.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.support.Global;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.math.BigDecimal;

import util.StringUtil;

public class TipAdjustmentFA extends BaseFragmentActivityActionBar implements View.OnClickListener {

    private EditText transactionId;
    private EditText tipAmount;
    private Button submitTipAmountBtn;
    private TextView messageText;
    Spinner cardTypesSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip_adjustment_fa);
        messageText = (TextView) findViewById(R.id.messageTexttextView);
        transactionId = (EditText) findViewById(R.id.transactionIdEditText);
        tipAmount = (EditText) findViewById(R.id.tipAmountEditText);
        submitTipAmountBtn = (Button) findViewById(R.id.submitTipButton);
        cardTypesSpinner = (Spinner) findViewById(R.id.cardTypesspinner);
        String[] cardTypes = getResources().getStringArray(R.array.cardTypes);
        tipAmount.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NumberUtils.parseInputedCurrency(s, tipAmount);
            }
        });
        submitTipAmountBtn.setOnClickListener(this);
        messageText.setText("");
        tipAmount.setText("0.00");
    }

    @Override
    public void onClick(View v) {
        if (transactionId != null && !transactionId.getText().toString().isEmpty() &&
                tipAmount != null && !tipAmount.getText().toString().isEmpty()) {
            BigDecimal tipAmountDec = Global.getBigDecimalNum(NumberUtils.cleanCurrencyFormatedNumber(tipAmount));
            if (tipAmountDec.compareTo(BigDecimal.ZERO) > 0) {
                messageText.setText("");
                new AdjustTipTask().execute(getCreditCardType(), transactionId.getText().toString(), tipAmountDec.toString());
            } else {
                setMessage(R.string.adjust_tip_required_fields);
            }
        } else {
            setMessage(R.string.adjust_tip_required_fields);
        }
    }

    private String getCreditCardType() {
        String cardName = StringUtil.trimSpace(cardTypesSpinner.getSelectedItem().toString());
        if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_AMEX)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_AMEX;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_DISCOVER)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_DISCOVER;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_MASTERCARD)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_MASTERCARD;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_VISA)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_VISA;
        } else {
            return "";
        }
    }

    private void setMessage(int resId) {
        messageText.setText(resId);
    }

    private class AdjustTipTask extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(TipAdjustmentFA.this);
            dialog.setMessage(getString(R.string.processing_tipamount));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String cardType = params[0];
            String transactionId = params[1];
            String amount = params[2];

            Payment payment = new Payment(TipAdjustmentFA.this);
            payment.pay_transid = transactionId;
            payment.pay_tip = amount;
            payment.card_type = cardType;
            EMSPayGate_Default payGate = new EMSPayGate_Default(TipAdjustmentFA.this, payment);
            String paymentWithAction = payGate.paymentWithAction(EMSPayGate_Default.EAction.CreditCardAdjustTipAmountAction, false, null,
                    null);

            Post httpClient = new Post();
//            String xml = httpClient.postData(Global.S_SUBMIT_TIP_ADJUSTMENT, TipAdjustmentFA.this, paymentWithAction);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
        }
    }
}
