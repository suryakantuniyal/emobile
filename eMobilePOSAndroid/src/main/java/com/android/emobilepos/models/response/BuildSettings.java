package com.android.emobilepos.models.response;

import com.android.emobilepos.models.response.restoresettings.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BuildSettings {

    @SerializedName("regid")
    private String regid;
    @SerializedName("empid")
    private int empid;

    //GENERAL SETTINGS
    @SerializedName("generalSettings")
    private generalsetting generalSettings = new generalsetting();

    //CUSTOMER SETTINGS
    @SerializedName("customerSettings")
    private customersetting customerSettings = new customersetting();

    //RESTAURANT SETTINGS
    @SerializedName("restaurantSettings")
    private restaurantsetting restaurantSettings = new restaurantsetting();

    //GIFTCARD SETTINGS
    @SerializedName("giftCardSettings")
    private giftcardsetting giftCardSettings = new giftcardsetting();

    //PAYMENT METHOD SETTINGS
    @SerializedName("paymentMethodSettings")
    private paymentmethodsetting paymentMethodSettings = new paymentmethodsetting();

    //PAYMENT PROCESSING SETTINGS
    @SerializedName("paymentProcessingSettings")
    private paymentprocessingsetting paymentProcessingSettings = new paymentprocessingsetting();

    //PRINTING SETTINGS
    @SerializedName("printingSettings")
    private printingsetting printingSettings = new printingsetting();

    //PRODUCT SETTINGS
    @SerializedName("productSettings")
    private productsettings productSettings = new productsettings();

    //SESSION SETTINGS
    @SerializedName("sessionSettings")
    private sessionSettings sessionSettings = new sessionSettings();

    //KIOSK SETTINGS
    @SerializedName("kioskSettings")
    private kioskSettings kioskSettings = new kioskSettings();

    //SHIPPING CALCULATION
    @SerializedName("shippingCalculation")
    private shippingCalculation shippingCalculation = new shippingCalculation();

    //TRANSACTION SETTINGS
    @SerializedName("transactionSettings")
    private transactionSettings transactionSettings = new transactionSettings();

    //OTHER SETTINGS
    @SerializedName("otherSettings")
    private otherSettings otherSettings = new otherSettings();

    //SYNC PLUS SERVICES
    @SerializedName("syncPlusServices")
    private syncPlusServices syncPlusServices = new syncPlusServices();


    public String getRegid() {
        return regid;
    }

    public void setRegid(String regid) {
        this.regid = regid;
    }

    public int getEmpid() {
        return empid;
    }

    public void setEmpid(int empid) {
        this.empid = empid;
    }

    public generalsetting getGeneralSettings() {
        return generalSettings;
    }

    public void setGeneralSettings(generalsetting generalSettings) {
        this.generalSettings = generalSettings;
    }

    public customersetting getCustomerSettings() {
        return customerSettings;
    }

    public void setCustomerSettings(customersetting customerSettings) {
        this.customerSettings = customerSettings;
    }

    public restaurantsetting getRestaurantSettings() {
        return restaurantSettings;
    }

    public void setRestaurantSettings(restaurantsetting restaurantSettings) {
        this.restaurantSettings = restaurantSettings;
    }

    public giftcardsetting getGiftCardSettings() {
        return giftCardSettings;
    }

    public void setGiftCardSettings(giftcardsetting giftCardSettings) {
        this.giftCardSettings = giftCardSettings;
    }

    public paymentmethodsetting getPaymentMethodSettings() {
        return paymentMethodSettings;
    }

    public void setPaymentMethodSettings(paymentmethodsetting paymentMethodSettings) {
        this.paymentMethodSettings = paymentMethodSettings;
    }

    public paymentprocessingsetting getPaymentProcessingSettings() {
        return paymentProcessingSettings;
    }

    public void setPaymentProcessingSettings(paymentprocessingsetting paymentProcessingSettings) {
        this.paymentProcessingSettings = paymentProcessingSettings;
    }

    public printingsetting getPrintingSettings() {
        return printingSettings;
    }

    public void setPrintingSettings(printingsetting printingSettings) {
        this.printingSettings = printingSettings;
    }

    public productsettings getProductSettings() {
        return productSettings;
    }

    public void setProductSettings(productsettings productSettings) {
        this.productSettings = productSettings;
    }

    public sessionSettings getSessionSettings() {
        return sessionSettings;
    }

    public void setSessionSettings(sessionSettings sessionSettings) {
        this.sessionSettings = sessionSettings;
    }

    public kioskSettings getKioskSettings() {
        return kioskSettings;
    }

    public void setKioskSettings(kioskSettings kioskSettings) {
        this.kioskSettings = kioskSettings;
    }

    public shippingCalculation getShippingCalculation() {
        return shippingCalculation;
    }

    public void setShippingCalculation(shippingCalculation shippingCalculation) {
        this.shippingCalculation = shippingCalculation;
    }

    public transactionSettings getTransactionSettings() {
        return transactionSettings;
    }

    public void setTransactionSettings(transactionSettings transactionSettings) {
        this.transactionSettings = transactionSettings;
    }

    public otherSettings getOtherSettings() {
        return otherSettings;
    }

    public void setOtherSettings(otherSettings otherSettings) {
        this.otherSettings = otherSettings;
    }

    public syncPlusServices getSyncPlusServices() {
        return syncPlusServices;
    }

    public void setSyncPlusServices(syncPlusServices syncPlusServices) {
        this.syncPlusServices = syncPlusServices;
    }
}
