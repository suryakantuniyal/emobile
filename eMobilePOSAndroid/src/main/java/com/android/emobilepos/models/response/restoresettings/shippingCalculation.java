package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class shippingCalculation {
    @SerializedName("UseNexternal")
    private boolean UseNexternal= false;

    public boolean isUseNexternal() {
        return UseNexternal;
    }

    public void setUseNexternal(boolean useNexternal) {
        UseNexternal = useNexternal;
    }
}
