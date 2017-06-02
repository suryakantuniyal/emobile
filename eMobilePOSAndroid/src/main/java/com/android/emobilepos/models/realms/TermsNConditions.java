package com.android.emobilepos.models.realms;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by guarionex on 5/22/17.
 */

public class TermsNConditions extends RealmObject {

    @SerializedName("tc_id")
    @Expose
    @PrimaryKey
    private String tcId;
    @SerializedName("tc_term")
    @Expose
    private String tcTerm;
    @SerializedName("loc_id")
    @Expose
    private String locId;

    public String getTcId() {
        return tcId;
    }

    public void setTcId(String tcId) {
        this.tcId = tcId;
    }

    public String getTcTerm() {
        return tcTerm;
    }

    public void setTcTerm(String tcTerm) {
        this.tcTerm = tcTerm;
    }

    public String getLocId() {
        return locId;
    }

    public void setLocId(String locId) {
        this.locId = locId;
    }
}
