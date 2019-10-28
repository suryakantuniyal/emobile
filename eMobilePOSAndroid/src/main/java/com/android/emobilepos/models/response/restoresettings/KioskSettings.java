package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class KioskSettings {
    @SerializedName("customerDisplayTerminal")
    private CustomerDisplayTerminal CustomerDisplayTerminal = new CustomerDisplayTerminal();


    public CustomerDisplayTerminal getCustomerDisplayTerminal() {
        return CustomerDisplayTerminal;
    }

    public void setCustomerDisplayTerminal(CustomerDisplayTerminal CustomerDisplayTerminal) {
        this.CustomerDisplayTerminal = CustomerDisplayTerminal;
    }
}
