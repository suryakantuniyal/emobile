package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class kioskSettings {
    @SerializedName("customerDisplayTerminal")
    private customerDisplayTerminal customerDisplayTerminal;


    public customerDisplayTerminal getCustomerDisplayTerminal() {
        return customerDisplayTerminal;
    }

    public void setCustomerDisplayTerminal(customerDisplayTerminal customerDisplayTerminal) {
        this.customerDisplayTerminal = customerDisplayTerminal;
    }
}
