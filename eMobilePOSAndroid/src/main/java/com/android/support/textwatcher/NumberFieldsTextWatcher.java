package com.android.support.textwatcher;

import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.widget.EditText;

import com.android.support.Global;

/**
 * Created by Guarionex on 12/3/2015.
 */
public class NumberFieldsTextWatcher implements android.text.TextWatcher {


    private final EditText editText;
    private boolean formated = false;

    public NumberFieldsTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!formated) {
            formated = true;
            double value = TextUtils.isEmpty(editText.getText().toString()) ? 0 : Double.parseDouble(editText.getText().toString());
            String number = Global.formatNumber(false, value);
            editText.setText(String.valueOf(number));
        }
        Selection.setSelection(editText.getText(), editText.getText().toString().length());
        formated = false;
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
