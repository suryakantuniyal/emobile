package com.android.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ";
    public static final String DATE_yyyy_MM_ddTHH_mm_ss = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_yyyy_MM_dd = "yyyy-MM-dd";
    public static final String DATE_yyyy_MM_dd_h_mm_a = "yyyy-MM-dd, h:mm:a";
    public static final String DATE_MMM_dd_yyyy_h_mm_a = "MMM dd,yyyy h:mm a";
    public static final String DATE_MM_DD = "MM-dd";
    public static final String DATE_h_mm_a = "h:mm a";

    public static String getDateAsString(Date date, String pattern) {
        if (date == null) {
            return "";
        }
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

    public static double computeDiffInSeconds(Date firstDate, Date secondDate) {
        double timeDiffInSeconds = 0;
        if (firstDate != null && secondDate != null) {
            timeDiffInSeconds = (secondDate.getTime() - firstDate.getTime()) / 1000;
        }
        return timeDiffInSeconds;
    }

    public static double computeDiffInHours(Date firstDate, Date secondDate) {
        return convertSecondsToHours(computeDiffInSeconds(firstDate, secondDate));
    }

    public static double convertSecondsToHours(double seconds) {
        return seconds / 60 / 60;
    }

    public static Map<TimeUnit, Long> computeDiff(Date date1, Date date2) {
        Map<TimeUnit, Long> result = new LinkedHashMap<>();
        List<TimeUnit> units = new ArrayList<>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);
        if (date1 == null || date2 == null) {
            for (TimeUnit unit : units) {
                result.put(unit, (long) 0);
            }
            return result;
        }
        long milliSecondsRest = date2.getTime() - date1.getTime();
        for (TimeUnit unit : units) {
            long diff = unit.convert(milliSecondsRest, TimeUnit.MILLISECONDS);
            long diffInMilliSecondsForUnit = unit.toMillis(diff);
            milliSecondsRest = milliSecondsRest - diffInMilliSecondsForUnit;
            result.put(unit, diff);
        }
        return result;
    }

    public static String getYearAdd(int value) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy", Locale.getDefault());
        String now = df.format(new Date());
        Date date;
        try {
            date = df.parse(now);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, value); // add 28 days

        return Integer.toString(cal.get(Calendar.YEAR));
    }

    public static String getEpochTime() {
        return String.valueOf(System.currentTimeMillis());
    }
}