package com.android.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static String getDateAsString(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    public static String getDateStringAsString(String date, String inPattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(inPattern);
        SimpleDateFormat output = new SimpleDateFormat(DATE_PATTERN);
        Date d;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        String formattedDate = output.format(d);
        return formattedDate;
    }

    public static Date getDateStringAsDate(String date, String inPattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(inPattern);
        SimpleDateFormat output = new SimpleDateFormat(DATE_PATTERN);
        Date d;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        return d;
    }

    public static String getDateAsString(Date date) {

        return getDateAsString(date, DATE_PATTERN);
    }
}
