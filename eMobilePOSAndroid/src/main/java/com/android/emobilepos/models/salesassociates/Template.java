package com.android.emobilepos.models.salesassociates;


import java.math.BigDecimal;

public class Template {
	private String productId;
    private String productSku;
    private String productUpc;
    private String productName;
    private String overitePrice;
    private String ordProductQty;
    private String productPrice;
    private String productIsTaxable;
    private String ordProductDescription;
    private String orderId;
    private String ordProductId;
    private BigDecimal itemTotal;
    private BigDecimal itemSubtotal;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductUpc() {
        return productUpc;
    }

    public void setProductUpc(String productUpc) {
        this.productUpc = productUpc;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getOveritePrice() {
        return overitePrice;
    }

    public void setOveritePrice(String overitePrice) {
        this.overitePrice = overitePrice;
    }

    public String getOrdProductQty() {
        return ordProductQty;
    }

    public void setOrdProductQty(String ordProductQty) {
        this.ordProductQty = ordProductQty;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductIsTaxable() {
        return productIsTaxable;
    }

    public void setProductIsTaxable(String productIsTaxable) {
        this.productIsTaxable = productIsTaxable;
    }

    public String getOrdProductDescription() {
        return ordProductDescription;
    }

    public void setOrdProductDescription(String ordProductDescription) {
        this.ordProductDescription = ordProductDescription;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(BigDecimal itemTotal) {
        this.itemTotal = itemTotal;
    }

    public BigDecimal getItemSubtotal() {
        return itemSubtotal;
    }

    public void setItemSubtotal(BigDecimal itemSubtotal) {
        this.itemSubtotal = itemSubtotal;
    }

    public String getOrdProductId() {
        return ordProductId;
    }

    public void setOrdProductId(String ordProductId) {
        this.ordProductId = ordProductId;
    }
}
