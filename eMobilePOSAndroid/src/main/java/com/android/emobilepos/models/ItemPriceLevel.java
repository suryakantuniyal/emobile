package com.android.emobilepos.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Guarionex on 5/6/2016.
 */
public class ItemPriceLevel {
    @SerializedName("pricelevel_prod_id")
    private String pricelevelProdId;
    @SerializedName("pricelevel_id")
    private String priceLevelId;
    @SerializedName("pricelevel")
    private String priceLevel;
    @SerializedName("pricelevel_price")
    private String priceLevelPrice;
    @SerializedName("pricelevel_update")
    private String priceLevelUpdate;
    @SerializedName("isactive")
    private String isActive;
    @SerializedName("_rowversion")
    private String rowVersion;


    public String getPricelevelProdId() {
        return pricelevelProdId;
    }

    public void setPricelevelProdId(String pricelevelProdId) {
        this.pricelevelProdId = pricelevelProdId;
    }

    public String getPriceLevelId() {
        return priceLevelId;
    }

    public void setPriceLevelId(String priceLevelId) {
        this.priceLevelId = priceLevelId;
    }

    public String getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(String priceLevel) {
        this.priceLevel = priceLevel;
    }

    public String getPriceLevelPrice() {
        return priceLevelPrice;
    }

    public void setPriceLevelPrice(String priceLevelPrice) {
        this.priceLevelPrice = priceLevelPrice;
    }

    public String getPriceLevelUpdate() {
        return priceLevelUpdate;
    }

    public void setPriceLevelUpdate(String priceLevelUpdate) {
        this.priceLevelUpdate = priceLevelUpdate;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(String rowVersion) {
        this.rowVersion = rowVersion;
    }
}
