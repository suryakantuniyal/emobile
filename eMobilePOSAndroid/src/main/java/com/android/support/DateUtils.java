package com.android.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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


    public static Map<TimeUnit,Long> computeDiff(Date date1, Date date2) {
        long diffInMilliSeconds = date2.getTime() - date1.getTime();
        List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);
        Map<TimeUnit,Long> result = new LinkedHashMap<TimeUnit, Long>();
        long milliSecondsRest = diffInMilliSeconds;
        for (TimeUnit unit : units) {
            long diff = unit.convert(milliSecondsRest,TimeUnit.MILLISECONDS);
            long diffInMilliSecondsForUnit = unit.toMillis(diff);
            milliSecondsRest = milliSecondsRest - diffInMilliSecondsForUnit;
            result.put(unit,diff);
        }
        return result;
    }
}
