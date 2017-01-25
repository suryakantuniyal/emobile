package com.android.emobilepos.models.realms;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.Index;

/**
 * Created by Guarionex on 5/24/2016.
 */
public class UOM extends RealmObject {
    @SerializedName("uomitem_id")
    @Index
    private String uomItemId;
    @SerializedName("uom_id")
    @Index
    private String uomId;
    @SerializedName("uom_name")
    private String uomName;
    @SerializedName("prod_id")
    @Index
    private String prodId;
    @SerializedName("uom_conversion")
    private String uomConversion;
    @SerializedName("uom_update")
    private String uomUpdate;
    @SerializedName("isactive")
    private String isActive;

    public String getUomItemId() {
        return uomItemId;
    }

    public void setUomItemId(String uomItemId) {
        this.uomItemId = uomItemId;
    }

    public String getUomId() {
        return uomId;
    }

    public void setUomId(String uomId) {
        this.uomId = uomId;
    }

    public String getUomName() {
        return uomName;
    }

    public void setUomName(String uomName) {
        this.uomName = uomName;
    }

    public String getProdId() {
        return prodId;
    }

    public void setProdId(String prodId) {
        this.prodId = prodId;
    }

    public String getUomConversion() {
        return uomConversion;
    }

    public void setUomConversion(String uomConversion) {
        this.uomConversion = uomConversion;
    }

    public String getUomUpdate() {
        return uomUpdate;
    }

    public void setUomUpdate(String uomUpdate) {
        this.uomUpdate = uomUpdate;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }
}
