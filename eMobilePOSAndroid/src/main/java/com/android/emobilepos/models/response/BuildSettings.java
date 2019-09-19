package com.android.emobilepos.models.response;

import com.android.emobilepos.models.response.restoresettings.*;
import com.google.gson.annotations.SerializedName;

public class BuildSettings {

    @SerializedName("regid")
    private String regid;
    @SerializedName("empid")
    private int empid;

    //GENERAL SETTINGS
    @SerializedName("generalSettings")
    private GeneralSetting generalSettings = new GeneralSetting();

    //CUSTOMER SETTINGS
    @SerializedName("customerSettings")
    private CustomerSetting customerSettings = new CustomerSetting();

    //RESTAURANT SETTINGS
    @SerializedName("restaurantSettings")
    private RestaurantSetting restaurantSettings = new RestaurantSetting();

    //GIFTCARD SETTINGS
    @SerializedName("giftCardSettings")
    private GiftCardSetting giftCardSettings = new GiftCardSetting();

    //PAYMENT METHOD SETTINGS
    @SerializedName("paymentMethodSettings")
    private PaymentMethodSetting paymentMethodSettings = new PaymentMethodSetting();

    //PAYMENT PROCESSING SETTINGS
    @SerializedName("paymentProcessingSettings")
    private PaymentProcessingSetting paymentProcessingSettings = new PaymentProcessingSetting();

    //PRINTING SETTINGS
    @SerializedName("printingSettings")
    private PrintingSetting printingSettings = new PrintingSetting();

    //PRODUCT SETTINGS
    @SerializedName("productSettings")
    private ProductSettings productSettings = new ProductSettings();

    //SESSION SETTINGS
    @SerializedName("sessionSettings")
    private SessionSettings SessionSettings = new SessionSettings();

    //KIOSK SETTINGS
    @SerializedName("kioskSettings")
    private KioskSettings KioskSettings = new KioskSettings();

    //SHIPPING CALCULATION
    @SerializedName("shippingCalculation")
    private ShippingCalculation ShippingCalculation = new ShippingCalculation();

    //TRANSACTION SETTINGS
    @SerializedName("transactionSettings")
    private TransactionSettings TransactionSettings = new TransactionSettings();

    //OTHER SETTINGS
    @SerializedName("otherSettings")
    private OtherSettings otherSettings = new OtherSettings();

    //SYNC PLUS SERVICES
    @SerializedName("syncPlusServices")
    private SyncPlusServices SyncPlusServices = new SyncPlusServices();


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

    public GeneralSetting getGeneralSettings() {
        return generalSettings;
    }

    public void setGeneralSettings(GeneralSetting generalSettings) {
        this.generalSettings = generalSettings;
    }

    public CustomerSetting getCustomerSettings() {
        return customerSettings;
    }

    public void setCustomerSettings(CustomerSetting customerSettings) {
        this.customerSettings = customerSettings;
    }

    public RestaurantSetting getRestaurantSettings() {
        return restaurantSettings;
    }

    public void setRestaurantSettings(RestaurantSetting restaurantSettings) {
        this.restaurantSettings = restaurantSettings;
    }

    public GiftCardSetting getGiftCardSettings() {
        return giftCardSettings;
    }

    public void setGiftCardSettings(GiftCardSetting giftCardSettings) {
        this.giftCardSettings = giftCardSettings;
    }

    public PaymentMethodSetting getPaymentMethodSettings() {
        return paymentMethodSettings;
    }

    public void setPaymentMethodSettings(PaymentMethodSetting paymentMethodSettings) {
        this.paymentMethodSettings = paymentMethodSettings;
    }

    public PaymentProcessingSetting getPaymentProcessingSettings() {
        return paymentProcessingSettings;
    }

    public void setPaymentProcessingSettings(PaymentProcessingSetting paymentProcessingSettings) {
        this.paymentProcessingSettings = paymentProcessingSettings;
    }

    public PrintingSetting getPrintingSettings() {
        return printingSettings;
    }

    public void setPrintingSettings(PrintingSetting printingSettings) {
        this.printingSettings = printingSettings;
    }

    public ProductSettings getProductSettings() {
        return productSettings;
    }

    public void setProductSettings(ProductSettings productSettings) {
        this.productSettings = productSettings;
    }

    public SessionSettings getSessionSettings() {
        return SessionSettings;
    }

    public void setSessionSettings(SessionSettings SessionSettings) {
        this.SessionSettings = SessionSettings;
    }

    public KioskSettings getKioskSettings() {
        return KioskSettings;
    }

    public void setKioskSettings(KioskSettings KioskSettings) {
        this.KioskSettings = KioskSettings;
    }

    public ShippingCalculation getShippingCalculation() {
        return ShippingCalculation;
    }

    public void setShippingCalculation(ShippingCalculation ShippingCalculation) {
        this.ShippingCalculation = ShippingCalculation;
    }

    public TransactionSettings getTransactionSettings() {
        return TransactionSettings;
    }

    public void setTransactionSettings(TransactionSettings TransactionSettings) {
        this.TransactionSettings = TransactionSettings;
    }

    public OtherSettings getOtherSettings() {
        return otherSettings;
    }

    public void setOtherSettings(OtherSettings otherSettings) {
        this.otherSettings = otherSettings;
    }

    public SyncPlusServices getSyncPlusServices() {
        return SyncPlusServices;
    }

    public void setSyncPlusServices(SyncPlusServices SyncPlusServices) {
        this.SyncPlusServices = SyncPlusServices;
    }
}
