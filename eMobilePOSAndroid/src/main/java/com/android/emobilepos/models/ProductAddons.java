package com.android.emobilepos.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Guarionex on 5/6/2016.
 */
public class ProductAddons {
    @SerializedName("rest_addons")
    private int restAddons;
    @SerializedName("prod_id")
    private String prodId;
    @SerializedName("cat_id")
    private String catId;
    @SerializedName("_update")
    private String update;
    @SerializedName("isactive")
    private boolean isActive;

    public int getRestAddons() {
        return restAddons;
    }

    public void setRestAddons(int restAddons) {
        this.restAddons = restAddons;
    }

    public String getProdId() {
        return prodId;
    }

    public void setProdId(String prodId) {
        this.prodId = prodId;
    }

    public String getCatId() {
        return catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
