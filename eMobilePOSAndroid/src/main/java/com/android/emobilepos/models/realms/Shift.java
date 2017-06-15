package com.android.emobilepos.models.realms;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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

    @SerializedName("shift_id")
    @Expose
    @PrimaryKey
    private String shiftId;
    @SerializedName("assignee_id")
    @Expose
    @Index
    private int assigneeId;
    @SerializedName("assignee_name")
    @Expose
    private String assigneeName;
    @SerializedName("creationDate")
    @Expose
    private Date creationDate;
    @SerializedName("creationDateLocal")
    @Expose
    private Date creationDateLocal;
    @SerializedName("startTime")
    @Expose
    private Date startTime;
    @SerializedName("startTimeLocal")
    @Expose
    private Date startTimeLocal;
    @SerializedName("endTime")
    @Expose
    private Date endTime;
    @SerializedName("endTimeLocal")
    @Expose
    private Date endTimeLocal;
    @SerializedName("beginning_petty_cash")
    @Expose
    private String beginningPettyCash = "0";
    @SerializedName("total_expenses")
    @Expose
    private String totalExpenses = "0";
    @SerializedName("ending_petty_cash")
    @Expose
    private String endingPettyCash = "0";
    //    @SerializedName("ending_cash")
//    @Expose
//    private String endingCash;
    @SerializedName("entered_close_amount")
    @Expose
    private String enteredCloseAmount = "0";
    @SerializedName("total_transactions_cash")
    @Expose
    private String totalTransactionsCash = "0";
    @SerializedName("emp_id")
    @Expose
    private int clerkId;
    @SerializedName("Shift_status")
    @Expose
    @Index
    private int shiftStatusCode;
    @SerializedName("ending_cash")
    @Expose
    private String total_ending_cash = "0";
    private String over_short = "0";
    private boolean sync;
    @Ignore
    private ShiftStatus shiftStatus;

    public Shift() {
        setShiftId(UUID.randomUUID().toString());
        setShiftStatus(ShiftStatus.CLOSED);
    }

    public ShiftStatus getShiftStatus() {
        shiftStatus = ShiftStatus.valueOf(getShiftStatusCode());
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

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public int getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(int assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
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

    public String getBeginningPettyCash() {
        return beginningPettyCash;
    }

    public void setBeginningPettyCash(String beginningPettyCash) {
        this.beginningPettyCash = beginningPettyCash;
    }

    public String getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(String totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public String getEndingPettyCash() {
        return endingPettyCash;
    }

    public void setEndingPettyCash(String endingPettyCash) {
        this.endingPettyCash = endingPettyCash;
    }

    public String getEnteredCloseAmount() {
        return enteredCloseAmount;
    }

    public void setEnteredCloseAmount(String enteredCloseAmount) {
        this.enteredCloseAmount = enteredCloseAmount;
    }

    public String getTotalTransactionsCash() {
        return totalTransactionsCash;
    }

    public void setTotalTransactionsCash(String totalTransactionsCash) {
        this.totalTransactionsCash = totalTransactionsCash;
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

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public int getClerkId() {
        return clerkId;
    }

    public void setClerkId(int clerkId) {
        this.clerkId = clerkId;
    }

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
}
