package com.android.support.textwatcher;

import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.NumberUtils;

/**
 * Created by Guarionex on 12/3/2015.
 */
public class CurrencyFieldsTextWatcher implements android.text.TextWatcher{


    private final EditText editText;
    private final Context context;

    public CurrencyFieldsTextWatcher(Context context, EditText editText) {
        this.editText = editText;
        this.context = context;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        NumberUtils.parseInputedCurrency(s, editText);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
