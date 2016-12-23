package com.android.support;

import android.text.TextUtils;

import com.google.common.base.CaseFormat;

/**
 * Created by guarionex on 12/22/16.
 */

public class StringUtils {
    public static String toTitleCase(String value) {
        String retVal = "";
        value = value.replace('_', ' ');
        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
        splitter.setString(value);
        for (String word : splitter) {
            if (retVal.isEmpty()) {
                retVal += CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, word.toUpperCase());
            } else {
                retVal += " ";
                retVal += CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, word.toUpperCase());
            }
        }
        return retVal;
    }
}
