package com.android.emobilepos.models.salesassociates;

import com.android.emobilepos.models.realms.SalesAssociate;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by guarionex on 9/20/16.
 */
public class DinningLocationConfiguration {
    @SerializedName("loc_id")
    private String locationId;
    private List<SalesAssociate> salesAssociates;

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public List<SalesAssociate> getSalesAssociates() {
        return salesAssociates;
    }

    public void setSalesAssociates(List<SalesAssociate> salesAssociates) {
        this.salesAssociates = salesAssociates;
    }

}
