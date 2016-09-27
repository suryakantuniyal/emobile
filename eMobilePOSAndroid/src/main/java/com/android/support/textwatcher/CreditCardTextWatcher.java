package com.android.support.textwatcher;

import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import com.android.support.CreditCardInfo;
import com.android.support.Global;

/**
 * Created by Guarionex on 12/3/2015.
 */
public class CreditCardTextWatcher implements android.text.TextWatcher {
    boolean doneScanning = false;

    private EditText hiddenEditText;
    private CreditCardInfo creditCardInfo;
    private Context context;
    private TextWatcherCallback callback;

    public CreditCardTextWatcher(Context context, EditText hiddenEditText, EditText cardEditText, CreditCardInfo creditCardInfo, boolean encryptCardNumber, TextWatcherCallback callback) {
        this.hiddenEditText = hiddenEditText;
        this.context = context;
        this.creditCardInfo = creditCardInfo;
        this.callback = callback;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().contains(";") && s.toString().endsWith("?")){
            doneScanning = true;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.d("Read:", s.toString());
        if (doneScanning) {
            doneScanning = false;
            String data = hiddenEditText.getText().toString().replace("\n", "").replace("\r", "");
            hiddenEditText.setText("");
            creditCardInfo = Global.parseSimpleMSR(context, data);
            callback.updateViewAfterSwipe(creditCardInfo);
        }
    }
}
