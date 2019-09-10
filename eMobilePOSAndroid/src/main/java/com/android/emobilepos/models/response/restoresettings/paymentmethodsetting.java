package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class paymentmethodsetting {
    @SerializedName("PayWithTupyx")
    private boolean PayWithTupyx;
    @SerializedName("CayanGenius")
    private boolean CayanGenius;
    @SerializedName("PayWithCardOnFile")
    private boolean PayWithCardOnFile;
    @SerializedName("PAXSecurePay")
    private boolean PAXSecurePay;
    @SerializedName("SPSecurePay")
    private boolean SPSecurePay;
    @SerializedName("ShowAlsoRedeem")
    private boolean ShowAlsoRedeem;
    @SerializedName("GeniusIP")
    private String GeniusIP;

    public boolean isPayWithTupyx() {
        return PayWithTupyx;
    }

    public void setPayWithTupyx(boolean payWithTupyx) {
        PayWithTupyx = payWithTupyx;
    }

    public boolean isCayanGenius() {
        return CayanGenius;
    }

    public void setCayanGenius(boolean cayanGenius) {
        CayanGenius = cayanGenius;
    }

    public boolean isPayWithCardOnFile() {
        return PayWithCardOnFile;
    }

    public void setPayWithCardOnFile(boolean payWithCardOnFile) {
        PayWithCardOnFile = payWithCardOnFile;
    }

    public boolean isPAXSecurePay() {
        return PAXSecurePay;
    }

    public void setPAXSecurePay(boolean PAXSecurePay) {
        this.PAXSecurePay = PAXSecurePay;
    }

    public boolean isSPSecurePay() {
        return SPSecurePay;
    }

    public void setSPSecurePay(boolean SPSecurePay) {
        this.SPSecurePay = SPSecurePay;
    }

    public boolean isShowAlsoRedeem() {
        return ShowAlsoRedeem;
    }

    public void setShowAlsoRedeem(boolean showAlsoRedeem) {
        ShowAlsoRedeem = showAlsoRedeem;
    }

    public String getGeniusIP() {
        return GeniusIP;
    }

    public void setGeniusIP(String geniusIP) {
        GeniusIP = geniusIP;
    }
}
