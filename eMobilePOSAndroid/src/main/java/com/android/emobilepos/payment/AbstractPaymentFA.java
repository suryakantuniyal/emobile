package com.android.emobilepos.payment;

import android.widget.EditText;

import com.android.support.Global;
import com.android.support.NumberUtils;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

/**
 * Created by Guarionex on 5/12/2016.
 */
public abstract class AbstractPaymentFA extends BaseFragmentActivityActionBar {

    public static void calculateAmountDue(EditText subtotal, EditText tax1, EditText tax2,
                                          EditText tax3, EditText amount) {
        double subtotalDbl = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(subtotal));
        double tax1Dbl = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(tax1));
        double tax2Dbl = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(tax2));
        double tax3Dbl = Global.formatNumFromLocale(NumberUtils.cleanCurrencyFormatedNumber(tax3));
        double amountDueDbl = subtotalDbl + tax1Dbl + tax2Dbl + tax3Dbl;

        amount.setText(Global.getCurrencyFormat(Global.formatNumToLocale(amountDueDbl)));
    }
}
