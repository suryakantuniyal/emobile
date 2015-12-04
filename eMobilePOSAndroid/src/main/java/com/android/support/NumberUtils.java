package com.android.support;

/**
 * Created by tirizar on 12/3/2015.
 */
public class NumberUtils {

    public static String cleanCurrencyFormatedNumber(String s){
        return s.replaceAll("[^\\d\\,\\.]", "").trim();
    }
}
