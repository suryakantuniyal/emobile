package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class transactionSettings {

    @SerializedName("RequireManagerPWToVoid")
    private boolean RequireManagerPWToVoid= false;
    @SerializedName("DefaultCountry")
    private String DefaultCountry="";

    public boolean isRequireManagerPWToVoid() {
        return RequireManagerPWToVoid;
    }

    public void setRequireManagerPWToVoid(boolean requireManagerPWToVoid) {
        RequireManagerPWToVoid = requireManagerPWToVoid;
    }

    public String getDefaultCountry() {
        return DefaultCountry;
    }

    public void setDefaultCountry(String defaultCountry) {
        DefaultCountry = defaultCountry;
    }
}
