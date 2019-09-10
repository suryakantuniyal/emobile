package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class paymentprocessingsetting {
    @SerializedName("AllowManualCreditCard")
    private boolean AllowManualCreditCard;
    @SerializedName("ProcessCheckOnline")
    private boolean ProcessCheckOnline;
    @SerializedName("ShowTipsForCash")
    private boolean ShowTipsForCash;
    @SerializedName("AudioCardReader")
    private String AudioCardReader;
    @SerializedName("DefaultPaymentMethod")
    private String DefaultPaymentMethod;
    @SerializedName("ReturnRequireRefund")
    private boolean ReturnRequireRefund;
    @SerializedName("ConvertToReward")
    private boolean ConvertToReward;
    @SerializedName("InvoiceRequirePayment")
    private boolean InvoiceRequirePayment;
    @SerializedName("InvoiceRequirePaymentFull")
    private boolean InvoiceRequirePaymentFull;
    @SerializedName("PreFillTotalAmount")
    private boolean PreFillTotalAmount;
    @SerializedName("UseStoreForward")
    private boolean UseStoreForward;
    @SerializedName("ShowCashChangeAmount")
    private boolean ShowCashChangeAmount;


    public boolean isAllowManualCreditCard() {
        return AllowManualCreditCard;
    }

    public void setAllowManualCreditCard(boolean allowManualCreditCard) {
        AllowManualCreditCard = allowManualCreditCard;
    }

    public boolean isProcessCheckOnline() {
        return ProcessCheckOnline;
    }

    public void setProcessCheckOnline(boolean processCheckOnline) {
        ProcessCheckOnline = processCheckOnline;
    }

    public boolean isShowTipsForCash() {
        return ShowTipsForCash;
    }

    public void setShowTipsForCash(boolean showTipsForCash) {
        ShowTipsForCash = showTipsForCash;
    }

    public String getAudioCardReader() {
        return AudioCardReader;
    }

    public void setAudioCardReader(String audioCardReader) {
        AudioCardReader = audioCardReader;
    }

    public String getDefaultPaymentMethod() {
        return DefaultPaymentMethod;
    }

    public void setDefaultPaymentMethod(String defaultPaymentMethod) {
        DefaultPaymentMethod = defaultPaymentMethod;
    }

    public boolean isReturnRequireRefund() {
        return ReturnRequireRefund;
    }

    public void setReturnRequireRefund(boolean returnRequireRefund) {
        ReturnRequireRefund = returnRequireRefund;
    }

    public boolean isConvertToReward() {
        return ConvertToReward;
    }

    public void setConvertToReward(boolean convertToReward) {
        ConvertToReward = convertToReward;
    }

    public boolean isInvoiceRequirePayment() {
        return InvoiceRequirePayment;
    }

    public void setInvoiceRequirePayment(boolean invoiceRequirePayment) {
        InvoiceRequirePayment = invoiceRequirePayment;
    }

    public boolean isInvoiceRequirePaymentFull() {
        return InvoiceRequirePaymentFull;
    }

    public void setInvoiceRequirePaymentFull(boolean invoiceRequirePaymentFull) {
        InvoiceRequirePaymentFull = invoiceRequirePaymentFull;
    }

    public boolean isPreFillTotalAmount() {
        return PreFillTotalAmount;
    }

    public void setPreFillTotalAmount(boolean preFillTotalAmount) {
        PreFillTotalAmount = preFillTotalAmount;
    }

    public boolean isUseStoreForward() {
        return UseStoreForward;
    }

    public void setUseStoreForward(boolean useStoreForward) {
        UseStoreForward = useStoreForward;
    }

    public boolean isShowCashChangeAmount() {
        return ShowCashChangeAmount;
    }

    public void setShowCashChangeAmount(boolean showCashChangeAmount) {
        ShowCashChangeAmount = showCashChangeAmount;
    }
}
