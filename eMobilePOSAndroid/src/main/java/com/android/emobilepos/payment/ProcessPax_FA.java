package com.android.emobilepos.payment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.Global;
import com.android.support.NumberUtils;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

/**
 * Created by Luis Camayd on 10/11/2018.
 */
public class ProcessPax_FA extends BaseFragmentActivityActionBar implements View.OnClickListener {
    private String paymethod_id;
    private Bundle extras;

    private EditText invoiceJobIdTextView, amountTextView;
    private ProgressDialog myProgressDialog;
    private Payment payment;
    private boolean hasBeenCreated = false;
    private boolean isRefund = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_pax_layout);
        extras = this.getIntent().getExtras();
        invoiceJobIdTextView = findViewById(R.id.invoiceJobIdTextView);
        amountTextView = findViewById(R.id.amountTextView);

        Button btnProcess = findViewById(R.id.processButton);
        btnProcess.setOnClickListener(this);

        boolean isFromMainMenu = extras.getBoolean("isFromMainMenu");
        if (!isFromMainMenu) {
            invoiceJobIdTextView.setEnabled(false);
        }

        paymethod_id = extras.getString("paymethod_id");

        String inv_id;
        if (extras.getBoolean("histinvoices"))
            inv_id = extras.getString("inv_id");
        else
            inv_id = extras.getString("job_id");

        if (extras.getBoolean("salesrefund"))
            isRefund = true;

        invoiceJobIdTextView.setText(inv_id);
        amountTextView.setText(
                Global.getCurrencyFormat(Global.formatNumToLocale(Double.parseDouble(extras.getString("amount")))));
        amountTextView.addTextChangedListener(getTextWatcher(amountTextView));
        amountTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Selection.setSelection(amountTextView.getText(), amountTextView.getText().length());
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
                Toast.makeText(this, getString(R.string.processing_payment_msg), Toast.LENGTH_LONG).show();
                processPayment();
                break;
        }
    }

    private void processPayment() {

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
}