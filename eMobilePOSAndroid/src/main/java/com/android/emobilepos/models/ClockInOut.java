package com.android.emobilepos.models;

import android.text.TextUtils;

import com.android.support.DateUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by guarionex on 7/19/17.
 */

public class ClockInOut {
    private String employeeId;
    private String clockIn;
    private String clockOut;
    private int minutesPeriod;

    public static ClockInOut getList(List<TimeClock> timeClocks) {
        ClockInOut clockInOut = new ClockInOut();
        for (TimeClock clock : timeClocks) {
            clockInOut.setEmployeeId(clock.emp_id);
            if (clock.status.equalsIgnoreCase("IN")) {
                clockInOut.setClockIn(clock.punchtime);
            } else {
                clockInOut.setClockOut(clock.punchtime);
            }
        }
        Date in = null;
        if (!TextUtils.isEmpty(clockInOut.getClockIn())) {
            in = DateUtils.getDateStringAsDate(clockInOut.getClockIn(), DateUtils.DATE_PATTERN);
        }
        Date out = null;
        if (!TextUtils.isEmpty(clockInOut.getClockOut())) {
            out = DateUtils.getDateStringAsDate(clockInOut.getClockOut(), DateUtils.DATE_PATTERN);
        }
        if (in != null && out != null && in.compareTo(out) == -1) {
            Map<TimeUnit, Long> timeMap = DateUtils.computeDiff(in, out);
            Long days = timeMap.get(TimeUnit.DAYS) * 60 * 24;
            Long hours = timeMap.get(TimeUnit.HOURS) * 60;
            Long mins = timeMap.get(TimeUnit.MINUTES);
            clockInOut.setMinutesPeriod((int) (days + hours + mins));
        } else {
            clockInOut.setMinutesPeriod(0);
            clockInOut.setClockOut(null);
        }
        return clockInOut;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getClockIn() {
        return clockIn;
    }

    public void setClockIn(String clockIn) {
        this.clockIn = clockIn;
    }

    public String getClockOut() {
        return clockOut;
    }

    public void setClockOut(String clockOut) {
        this.clockOut = clockOut;
    }

    public int getMinutesPeriod() {
        return minutesPeriod;
    }

    public void setMinutesPeriod(int minutesPeriod) {
        this.minutesPeriod = minutesPeriod;
    }
}
