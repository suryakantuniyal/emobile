package com.android.emobilepos.models.genius;

/**
 * Created by Guarionex on 12/18/2015.
 */
public class AmountDetails {
    private String UserTip;
    private String Cashback;
    private String Donation;
    private String Surcharge;
    private Discount Discount;

    public String getUserTip() {
        return UserTip;
    }

    public void setUserTip(String userTip) {
        UserTip = userTip;
    }

    public String getCashback() {
        return Cashback;
    }

    public void setCashback(String cashback) {
        Cashback = cashback;
    }

    public String getDonation() {
        return Donation;
    }

    public void setDonation(String donation) {
        Donation = donation;
    }

    public String getSurcharge() {
        return Surcharge;
    }

    public void setSurcharge(String surcharge) {
        Surcharge = surcharge;
    }

    public com.android.emobilepos.models.genius.Discount getDiscount() {
        return Discount;
    }

    public void setDiscount(com.android.emobilepos.models.genius.Discount discount) {
        Discount = discount;
    }
}
