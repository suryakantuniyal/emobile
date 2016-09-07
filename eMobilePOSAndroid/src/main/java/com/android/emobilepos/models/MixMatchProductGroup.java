package com.android.emobilepos.models;

import java.util.List;

/**
 * Created by Guarionex on 5/17/2016.
 */
public class MixMatchProductGroup {
    private String groupId;
    private String priceLevelId;
    private List<OrderProduct> orderProducts;
    private int quantity;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getPriceLevelId() {
        return priceLevelId;
    }

    public void setPriceLevelId(String priceLevelId) {
        this.priceLevelId = priceLevelId;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<OrderProduct> getOrderProducts() {
        return orderProducts;
    }

    public void setOrderProducts(List<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }
}
