package com.android.emobilepos.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by Guarionex on 5/17/2016.
 */
public class MixMatch extends RealmObject {
    
    @SerializedName("id_key")
    private int idKey;
    @SerializedName("group_id")
    private String groupId;
    @SerializedName("description")
    private String description;
    @SerializedName("pricelevel_id")
    private String priceLevelId;
    @SerializedName("Qty")
    private int qty;
    @SerializedName("discount_type")
    private String discountType;
    @SerializedName("price")
    private double price;
    @SerializedName("isactive")
    private boolean isActive;
    @SerializedName("start_date")
    private String startDate;
    @SerializedName("end_date")
    private String endDate;
    @SerializedName("_rowversion")
    private String rowVersion;
    @SerializedName("MixmatchType")
    private int mixMatchType;
    @SerializedName("DiscountOddItems")
    private boolean discountOddsItems;
    @SerializedName("XYZSeq")
    private int xyzSequence;

    public int getIdKey() {
        return idKey;
    }

    public void setIdKey(int idKey) {
        this.idKey = idKey;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriceLevelId() {
        return priceLevelId;
    }

    public void setPriceLevelId(String priceLevelId) {
        this.priceLevelId = priceLevelId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(String rowVersion) {
        this.rowVersion = rowVersion;
    }

    public int getMixMatchType() {
        return mixMatchType;
    }

    public void setMixMatchType(int mixMatchType) {
        this.mixMatchType = mixMatchType;
    }

    public boolean isDiscountOddsItems() {
        return discountOddsItems;
    }

    public void setDiscountOddsItems(boolean discountOddsItems) {
        this.discountOddsItems = discountOddsItems;
    }

    public int getXyzSequence() {
        return xyzSequence;
    }

    public void setXyzSequence(int xyzSequence) {
        this.xyzSequence = xyzSequence;
    }
}
