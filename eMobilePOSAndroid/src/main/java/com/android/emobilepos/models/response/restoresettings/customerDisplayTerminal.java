package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class customerDisplayTerminal {
    @SerializedName("DisplayLine1")
    private String DisplayLine1="";
    @SerializedName("DisplayLine2")
    private String DisplayLine2="";

    public String getDisplayLine1() {
        return DisplayLine1;
    }

    public void setDisplayLine1(String displayLine1) {
        DisplayLine1 = displayLine1;
    }

    public String getDisplayLine2() {
        return DisplayLine2;
    }

    public void setDisplayLine2(String displayLine2) {
        DisplayLine2 = displayLine2;
    }
}
