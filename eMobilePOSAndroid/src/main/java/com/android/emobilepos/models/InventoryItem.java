package com.android.emobilepos.models;

public class InventoryItem {

    private String name;
    private String id;
    private Double qty;

    public InventoryItem(String name, Double qty) {
        this.name = name;
        this.qty = qty;
    }

    public InventoryItem() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
