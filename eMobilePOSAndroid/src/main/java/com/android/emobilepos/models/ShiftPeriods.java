package com.android.emobilepos.models;

import com.android.support.DateUtils;

import java.util.Date;
import java.util.UUID;


public class ShiftPeriods {


    public enum ShiftStatus {
        OPEN(0), PENDING(1), CLOSED(2);

        public int code;

        ShiftStatus(int code) {
            this.code = code;
        }

        public static ShiftStatus valueOf(int code) {
            switch (code) {
                case 0:
                    return OPEN;
                case 1:
                    return PENDING;
                case 2:
                    return CLOSED;
                default:
                    return CLOSED;
            }
        }
    }

    private String empStr = "";

    public String shift_id = empStr;
    public String assignee_id = empStr;
    public String assignee_name = empStr;
    public String creationDate = empStr;
    public String creationDateLocal = empStr;
    public String startTime = empStr;
    public String startTimeLocal = empStr;
    public String endTime = empStr;
    public String endTimeLocal = empStr;
    public String beginning_petty_cash = "0";
    public String ending_petty_cash = "0";
    public String entered_close_amount = "0";
    public String total_transaction_cash = "0";
    public String shift_issync = "0";
    public String total_expenses = "0";
    public String total_ending_cash = "0";
    public String over_short = "0";
    private ShiftStatus shiftStatus;
    private int shiftStatusCode;


    public ShiftStatus getShiftStatus() {
        return shiftStatus;
    }

    public void setShiftStatus(ShiftStatus shiftStatus) {
        setShiftStatusCode(shiftStatus.code);
        this.shiftStatus = shiftStatus;
    }

    public int getShiftStatusCode() {
        return shiftStatusCode;
    }

    public void setShiftStatusCode(int shiftStatusCode) {
        setShiftStatus(ShiftStatus.valueOf(shiftStatusCode));
        this.shiftStatusCode = shiftStatusCode;
    }

    public ShiftPeriods(boolean isOpen) {
        shift_id = UUID.randomUUID().toString();
        if (isOpen) {
            startTime = DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss);
            startTimeLocal = startTime;
            creationDate = startTime;
            creationDateLocal = startTime;
        } else {
            endTime = DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss);
            endTimeLocal = endTime;
        }
    }
}
