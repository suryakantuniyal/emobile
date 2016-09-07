package com.android.emobilepos.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Guarionex on 5/6/2016.
 */
public class PriceLevel {
    @SerializedName("pricelevel_id")
    private String pricelevelId;
    @SerializedName("pricelevel_name")
    private String pricelevelName;
    @SerializedName("pricelevel_type")
    private String pricelevelType;
    @SerializedName("pricelevel_fixedpct")
    private int pricelevelFixedpct;
    @SerializedName("pricelevel_update")
    private String pricelevelUpdate;
    @SerializedName("isactive")
    private int isactive;
    @SerializedName("_rowversion")
    private String rowversion;
    private String calcResult;

    public String getPricelevelId() {
        return pricelevelId;
    }

    public void setPricelevelId(String pricelevelId) {
        this.pricelevelId = pricelevelId;
    }

    public String getPricelevelName() {
        return pricelevelName;
    }

    public void setPricelevelName(String pricelevelName) {
        this.pricelevelName = pricelevelName;
    }

    public String getPricelevelType() {
        return pricelevelType;
    }

    public void setPricelevelType(String pricelevelType) {
        this.pricelevelType = pricelevelType;
    }

    public int getPricelevelFixedpct() {
        return pricelevelFixedpct;
    }

    public void setPricelevelFixedpct(int pricelevelFixedpct) {
        this.pricelevelFixedpct = pricelevelFixedpct;
    }

    public String getPricelevelUpdate() {
        return pricelevelUpdate;
    }

    public void setPricelevelUpdate(String pricelevelUpdate) {
        this.pricelevelUpdate = pricelevelUpdate;
    }

    public int getIsactive() {
        return isactive;
    }

    public void setIsactive(int isactive) {
        this.isactive = isactive;
    }

    public String getRowversion() {
        return rowversion;
    }

    public void setRowversion(String rowversion) {
        this.rowversion = rowversion;
    }


    public String getCalcResult() {
        return calcResult;
    }

    public void setCalcResult(String calcResult) {
        this.calcResult = calcResult;
    }
}
