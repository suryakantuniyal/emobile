package com.android.support;

import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Guarionex on 12/3/2015.
 */
public class GiftCardTextWatcher implements android.text.TextWatcher {
    boolean doneScanning = false;
    private EditText hiddenEditText;
    private CreditCardInfo creditCardInfo;
    private Context context;
    private EditText cardEditText;
    private boolean encryptCardNumber;

    public GiftCardTextWatcher(Context context, EditText hiddenEditText, EditText cardEditText, CreditCardInfo creditCardInfo, boolean encryptCardNumber) {
        this.hiddenEditText = hiddenEditText;
        this.context = context;
        this.cardEditText = cardEditText;
        this.creditCardInfo = creditCardInfo;
        this.encryptCardNumber = encryptCardNumber;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().contains("\n") || s.toString().contains("\r") || s.toString().contains("?"))
            doneScanning = true;
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.d("Read:", s.toString());
        if (doneScanning) {
            doneScanning = false;
            String data = hiddenEditText.getText().toString().trim().replace("\n", "");
            creditCardInfo = Global.parseSimpleMSR(context, data);
            if (encryptCardNumber) {
                cardEditText.setText(creditCardInfo.getCardNumAESEncrypted());
            } else {
                cardEditText.setText(creditCardInfo.getCardNumUnencrypted());
            }
            creditCardInfo.setCardType("GiftCard");
            hiddenEditText.setText("");
            SimpleDateFormat dt = new SimpleDateFormat("yyyy", Locale.getDefault());
            SimpleDateFormat dt2 = new SimpleDateFormat("yy", Locale.getDefault());
            String formatedYear = "";
            try {
                Date date = dt2.parse(creditCardInfo.getCardExpYear());
                formatedYear = dt.format(date);
            } catch (ParseException e) {

            }
            creditCardInfo.setCardExpYear(formatedYear);
            creditCardInfo.setWasSwiped(true);

        }
    }
}
