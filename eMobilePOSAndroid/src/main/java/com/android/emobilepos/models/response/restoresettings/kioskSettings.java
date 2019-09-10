package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class kioskSettings {
    @SerializedName("customerDisplayTerminal")
    private List<customerDisplayTerminal> customerDisplayTerminal;


    public List<customerDisplayTerminal> getCustomerDisplayTerminal() {
        return customerDisplayTerminal;
    }

    public void setCustomerDisplayTerminal(List<customerDisplayTerminal> customerDisplayTerminal) {
        this.customerDisplayTerminal = customerDisplayTerminal;
    }
}
