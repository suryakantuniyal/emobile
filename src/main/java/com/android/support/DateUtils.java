package com.android.support;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	public static String getDateAsString(Date date, String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(date);
	}

	public static String getDateAsString(Date date) {
		return getDateAsString(date, DATE_PATTERN);
	}
}
