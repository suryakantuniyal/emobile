package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class printprefs {
    @SerializedName("Header")
    private boolean Header= false;
    @SerializedName("ShipToInfo")
    private boolean ShipToInfo= false;
    @SerializedName("Terms")
    private boolean Terms= false;
    @SerializedName("CustomerAccNumber")
    private boolean CustomerAccNumber= false;
    @SerializedName("OrderComments")
    private boolean OrderComments= false;
    @SerializedName("Addons")
    private boolean Addons= false;
    @SerializedName("ProductTaxDetails")
    private boolean ProductTaxDetails= false;
    @SerializedName("ProductDiscountDetails")
    private boolean ProductDiscountDetails= false;
    @SerializedName("ProductDescriptions")
    private boolean ProductDescriptions= false;
    @SerializedName("ProductComments")
    private boolean ProductComments= false;
    @SerializedName("SaleAttributes")
    private boolean SaleAttributes= false;
    @SerializedName("PaymentComments")
    private boolean PaymentComments= false;
    @SerializedName("IVULotoQRCode")
    private boolean IVULotoQRCode= false;
    @SerializedName("Footer")
    private boolean Footer= false;
    @SerializedName("TermsAndConditions")
    private boolean TermsAndConditions= false;
    @SerializedName("EMSWebsiteFooter")
    private boolean EMSWebsiteFooter= false;

    public boolean isHeader() {
        return Header;
    }

    public void setHeader(boolean header) {
        Header = header;
    }

    public boolean isShipToInfo() {
        return ShipToInfo;
    }

    public void setShipToInfo(boolean shipToInfo) {
        ShipToInfo = shipToInfo;
    }

    public boolean isTerms() {
        return Terms;
    }

    public void setTerms(boolean terms) {
        Terms = terms;
    }

    public boolean isCustomerAccNumber() {
        return CustomerAccNumber;
    }

    public void setCustomerAccNumber(boolean customerAccNumber) {
        CustomerAccNumber = customerAccNumber;
    }

    public boolean isOrderComments() {
        return OrderComments;
    }

    public void setOrderComments(boolean orderComments) {
        OrderComments = orderComments;
    }

    public boolean isAddons() {
        return Addons;
    }

    public void setAddons(boolean addons) {
        Addons = addons;
    }

    public boolean isProductTaxDetails() {
        return ProductTaxDetails;
    }

    public void setProductTaxDetails(boolean productTaxDetails) {
        ProductTaxDetails = productTaxDetails;
    }

    public boolean isProductDiscountDetails() {
        return ProductDiscountDetails;
    }

    public void setProductDiscountDetails(boolean productDiscountDetails) {
        ProductDiscountDetails = productDiscountDetails;
    }

    public boolean isProductDescriptions() {
        return ProductDescriptions;
    }

    public void setProductDescriptions(boolean productDescriptions) {
        ProductDescriptions = productDescriptions;
    }

    public boolean isProductComments() {
        return ProductComments;
    }

    public void setProductComments(boolean productComments) {
        ProductComments = productComments;
    }

    public boolean isSaleAttributes() {
        return SaleAttributes;
    }

    public void setSaleAttributes(boolean saleAttributes) {
        SaleAttributes = saleAttributes;
    }

    public boolean isPaymentComments() {
        return PaymentComments;
    }

    public void setPaymentComments(boolean paymentComments) {
        PaymentComments = paymentComments;
    }

    public boolean isIVULotoQRCode() {
        return IVULotoQRCode;
    }

    public void setIVULotoQRCode(boolean IVULotoQRCode) {
        this.IVULotoQRCode = IVULotoQRCode;
    }

    public boolean isFooter() {
        return Footer;
    }

    public void setFooter(boolean footer) {
        Footer = footer;
    }

    public boolean isTermsAndConditions() {
        return TermsAndConditions;
    }

    public void setTermsAndConditions(boolean termsAndConditions) {
        TermsAndConditions = termsAndConditions;
    }

    public boolean isEMSWebsiteFooter() {
        return EMSWebsiteFooter;
    }

    public void setEMSWebsiteFooter(boolean EMSWebsiteFooter) {
        this.EMSWebsiteFooter = EMSWebsiteFooter;
    }
}
