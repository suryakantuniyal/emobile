package com.android.emobilepos.models;

/**
 * Created by Guarionex on 2/24/2016.
 */
public class Discount {
    private String productName;
    private String productDiscountType;
    private String productPrice;
    private String taxCodeIsTaxable;
    private String productId;

    public enum DiscountType {
        FIXED, PERCENT
    }

    public static Discount getDefaultInstance() {
        Discount discount = new Discount();
        discount.setProductId("");
        discount.setTaxCodeIsTaxable("");
        discount.setProductPrice("0");
        discount.setProductDiscountType("");
        discount.setProductName("");
        return discount;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDiscountType() {
        return productDiscountType;
    }

    public void setProductDiscountType(String productDiscountType) {
        this.productDiscountType = productDiscountType;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getTaxCodeIsTaxable() {
        return taxCodeIsTaxable;
    }

    public void setTaxCodeIsTaxable(String taxCodeIsTaxable) {
        this.taxCodeIsTaxable = taxCodeIsTaxable;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
