package com.android.emobilepos.models;

import com.android.emobilepos.models.orders.OrderProduct;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guarionex on 5/17/2016.
 */
public class MixMatchXYZProduct {
    private String productId;
    private List<OrderProduct> orderProducts = new ArrayList<OrderProduct>();
    private int quantity;
    private BigDecimal price;


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object prodId) {
        if (prodId instanceof String) {
            this.getProductId().equalsIgnoreCase((String) prodId);
        }
        return super.equals(prodId);
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public List<OrderProduct> getOrderProducts() {
        return orderProducts;
    }

    public void setOrderProducts(List<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }
}
