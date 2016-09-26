package com.android.emobilepos.models.salesassociates;

import com.android.emobilepos.models.SalesAssociate;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by guarionex on 9/20/16.
 */
public class SalesAssociatesConfiguration {
    @SerializedName("loc_id")
    private String locationId;
    private int emp_id;
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

    public SalesAssociatesConfiguration minify() {
        return this;
    }

    public int getEmp_id() {
        return emp_id;
    }

    public void setEmp_id(int emp_id) {
        this.emp_id = emp_id;
    }
}
