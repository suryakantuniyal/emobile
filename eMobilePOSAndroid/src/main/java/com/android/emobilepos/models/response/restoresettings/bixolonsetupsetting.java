package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class bixolonsetupsetting {
    @SerializedName("NCF")
    private String NCF;
    @SerializedName("RUC")
    private String RUC;
    @SerializedName("MerchantName")
    private String MerchantName;
    @SerializedName("Date")
    private String Date;
    @SerializedName("Header")
    private String Header;
    @SerializedName("Footer")
    private String Footer;
    @SerializedName("Taxes")
    private String Taxes;
    @SerializedName("PaymentMethod")
    private String PaymentMethod;

    public String getNCF() {
        return NCF;
    }

    public void setNCF(String NCF) {
        this.NCF = NCF;
    }

    public String getRUC() {
        return RUC;
    }

    public void setRUC(String RUC) {
        this.RUC = RUC;
    }

    public String getMerchantName() {
        return MerchantName;
    }

    public void setMerchantName(String merchantName) {
        MerchantName = merchantName;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getHeader() {
        return Header;
    }

    public void setHeader(String header) {
        Header = header;
    }

    public String getFooter() {
        return Footer;
    }

    public void setFooter(String footer) {
        Footer = footer;
    }

    public String getTaxes() {
        return Taxes;
    }

    public void setTaxes(String taxes) {
        Taxes = taxes;
    }

    public String getPaymentMethod() {
        return PaymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        PaymentMethod = paymentMethod;
    }
}
