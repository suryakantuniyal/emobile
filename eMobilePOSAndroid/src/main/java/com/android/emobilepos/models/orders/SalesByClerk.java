package com.android.emobilepos.models.orders;

import java.math.BigDecimal;

public class SalesByClerk {
    private String clerkId;
    private String overwritePrice;
    private String ordProdQuantity;

    public String getClerkId() {
        return clerkId;
    }

    public void setClerkId(String clerkId) {
        this.clerkId = clerkId;
    }

    public String getOverwritePrice() {
        return overwritePrice;
    }

    public void setOverwritePrice(String overwritePrice) {
        this.overwritePrice = overwritePrice;
    }

    public String getOrdProdQuantity() {
        return ordProdQuantity;
    }

    public void setOrdProdQuantity(String ordProdQuantity) {
        this.ordProdQuantity = ordProdQuantity;
    }
}
