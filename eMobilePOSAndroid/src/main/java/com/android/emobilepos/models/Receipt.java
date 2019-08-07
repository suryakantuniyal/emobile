package com.android.emobilepos.models;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luis Camayd on 8/6/2019.
 */
public class Receipt {
    private Bitmap merchantLogo;
    private String merchantHeader;
    private String specialHeader;
    private String header;
    private List<String> items = new ArrayList<>();
    private String separator;
    private String totals;
    private String taxes;
    private String totalItems;
    private String grandTotal;
    private String paymentsDetails;
    private String youSave;
    private String ivuLoto;
    private String merchantFooter;
    private String loyaltyDetails;
    private String rewardsDetails;
    private String balanceDetails;
    private Bitmap signatureImage;
    private String signature;
    private String specialFooter;
    private String termsAndConditions;
    private String eNablerWebsite;


    public Bitmap getMerchantLogo() {
        return merchantLogo;
    }

    public void setMerchantLogo(Bitmap merchantLogo) {
        this.merchantLogo = merchantLogo;
    }

    public String getMerchantHeader() {
        return merchantHeader;
    }

    public void setMerchantHeader(String merchantHeader) {
        this.merchantHeader = merchantHeader;
    }

    public String getSpecialHeader() {
        return specialHeader;
    }

    public void setSpecialHeader(String specialHeader) {
        this.specialHeader = specialHeader;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getTotals() {
        return totals;
    }

    public void setTotals(String totals) {
        this.totals = totals;
    }

    public String getTaxes() {
        return taxes;
    }

    public void setTaxes(String taxes) {
        this.taxes = taxes;
    }

    public String getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(String totalItems) {
        this.totalItems = totalItems;
    }

    public String getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(String grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getPaymentsDetails() {
        return paymentsDetails;
    }

    public void setPaymentsDetails(String paymentsDetails) {
        this.paymentsDetails = paymentsDetails;
    }

    public String getYouSave() {
        return youSave;
    }

    public void setYouSave(String youSave) {
        this.youSave = youSave;
    }

    public String getIvuLoto() {
        return ivuLoto;
    }

    public void setIvuLoto(String ivuLoto) {
        this.ivuLoto = ivuLoto;
    }

    public String getMerchantFooter() {
        return merchantFooter;
    }

    public void setMerchantFooter(String merchantFooter) {
        this.merchantFooter = merchantFooter;
    }

    public String getLoyaltyDetails() {
        return loyaltyDetails;
    }

    public void setLoyaltyDetails(String loyaltyDetails) {
        this.loyaltyDetails = loyaltyDetails;
    }

    public String getRewardsDetails() {
        return rewardsDetails;
    }

    public void setRewardsDetails(String rewardsDetails) {
        this.rewardsDetails = rewardsDetails;
    }

    public String getBalanceDetails() {
        return balanceDetails;
    }

    public void setBalanceDetails(String balanceDetails) {
        this.balanceDetails = balanceDetails;
    }

    public Bitmap getSignatureImage() {
        return signatureImage;
    }

    public void setSignatureImage(Bitmap signatureImage) {
        this.signatureImage = signatureImage;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSpecialFooter() {
        return specialFooter;
    }

    public void setSpecialFooter(String specialFooter) {
        this.specialFooter = specialFooter;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public String getEnablerWebsite() {
        return eNablerWebsite;
    }

    public void setEnablerWebsite(String eNablerWebsite) {
        this.eNablerWebsite = eNablerWebsite;
    }
}
