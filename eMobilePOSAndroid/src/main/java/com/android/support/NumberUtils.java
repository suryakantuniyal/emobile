package com.android.support;

import android.widget.EditText;

/**
 * Created by tirizar on 12/3/2015.
 */
public class NumberUtils {

    public static String cleanCurrencyFormatedNumber(String s){
        return s.replaceAll("[^\\d\\,\\.]", "").trim();
    }

    public static String cleanCurrencyFormatedNumber(EditText s){
        return s.getText().toString().replaceAll("[^\\d\\,\\.]", "").trim();
    }

    public static String cleanCurrencyFormatedNumber(StringBuilder s){
        return s.toString().replaceAll("[^\\d\\,\\.]", "").trim();
    }
}
