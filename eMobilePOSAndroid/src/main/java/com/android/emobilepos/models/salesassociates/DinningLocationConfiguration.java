package com.android.emobilepos.models.salesassociates;

import com.android.emobilepos.models.realms.Clerk;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by guarionex on 9/20/16.
 */
public class DinningLocationConfiguration {
    @SerializedName("loc_id")
    private String locationId;
    @SerializedName("salesAssociates")
    private List<Clerk> clerks;

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public List<Clerk> getClerks() {
        return clerks;
    }

    public void setClerks(List<Clerk> clerks) {
        this.clerks = clerks;
    }

}
