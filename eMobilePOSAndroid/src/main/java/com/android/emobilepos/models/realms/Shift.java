package com.android.emobilepos.models.realms;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by guarionex on 2/10/17.
 */

public class Shift extends RealmObject {

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

    @PrimaryKey
    private String shift_id;
    @Index
    private String assignee_id;
    private String assignee_name;
    private Date creationDate;
    private Date creationDateLocal;
    private Date startTime;
    private Date startTimeLocal;
    private Date endTime;
    private Date endTimeLocal;
    private String beginning_petty_cash = "0";
    private String ending_petty_cash = "0";
    private String entered_close_amount = "0";
    private String total_transaction_cash = "0";
    private String shift_issync = "0";
    private String total_expenses = "0";
    private String total_ending_cash = "0";
    private String over_short = "0";
    private boolean sync;
    @Ignore
    private ShiftStatus shiftStatus;
    @Index
    private int shiftStatusCode;

    public Shift() {
        shift_id = UUID.randomUUID().toString();
        setShiftStatus(ShiftStatus.CLOSED);
    }

    public String getShift_id() {
        return shift_id;
    }

    public void setShift_id(String shift_id) {
        this.shift_id = shift_id;
    }

    public String getAssignee_id() {
        return assignee_id;
    }

    public void setAssignee_id(String assignee_id) {
        this.assignee_id = assignee_id;
    }

    public String getAssignee_name() {
        return assignee_name;
    }

    public void setAssignee_name(String assignee_name) {
        this.assignee_name = assignee_name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getCreationDateLocal() {
        return creationDateLocal;
    }

    public void setCreationDateLocal(Date creationDateLocal) {
        this.creationDateLocal = creationDateLocal;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStartTimeLocal() {
        return startTimeLocal;
    }

    public void setStartTimeLocal(Date startTimeLocal) {
        this.startTimeLocal = startTimeLocal;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getEndTimeLocal() {
        return endTimeLocal;
    }

    public void setEndTimeLocal(Date endTimeLocal) {
        this.endTimeLocal = endTimeLocal;
    }

    public String getBeginning_petty_cash() {
        return beginning_petty_cash;
    }

    public void setBeginning_petty_cash(String beginning_petty_cash) {
        this.beginning_petty_cash = beginning_petty_cash;
    }

    public String getEnding_petty_cash() {
        return ending_petty_cash;
    }

    public void setEnding_petty_cash(String ending_petty_cash) {
        this.ending_petty_cash = ending_petty_cash;
    }

    public String getEntered_close_amount() {
        return entered_close_amount;
    }

    public void setEntered_close_amount(String entered_close_amount) {
        this.entered_close_amount = entered_close_amount;
    }

    public String getTotal_transaction_cash() {
        return total_transaction_cash;
    }

    public void setTotal_transaction_cash(String total_transaction_cash) {
        this.total_transaction_cash = total_transaction_cash;
    }

    public String getShift_issync() {
        return shift_issync;
    }

    public void setShift_issync(String shift_issync) {
        this.shift_issync = shift_issync;
    }

    public String getTotal_expenses() {
        return total_expenses;
    }

    public void setTotal_expenses(String total_expenses) {
        this.total_expenses = total_expenses;
    }

    public String getTotal_ending_cash() {
        return total_ending_cash;
    }

    public void setTotal_ending_cash(String total_ending_cash) {
        this.total_ending_cash = total_ending_cash;
    }

    public String getOver_short() {
        return over_short;
    }

    public void setOver_short(String over_short) {
        this.over_short = over_short;
    }

    public ShiftStatus getShiftStatus() {
        shiftStatus = ShiftStatus.valueOf(shiftStatusCode);
        return shiftStatus;
    }

    public void setShiftStatus(ShiftStatus shiftStatus) {
        shiftStatusCode = shiftStatus.code;
        this.shiftStatus = shiftStatus;
    }

    public int getShiftStatusCode() {
        return shiftStatusCode;
    }

    public void setShiftStatusCode(int shiftStatusCode) {
        shiftStatus = ShiftStatus.valueOf(shiftStatusCode);
        this.shiftStatusCode = shiftStatusCode;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }
}
