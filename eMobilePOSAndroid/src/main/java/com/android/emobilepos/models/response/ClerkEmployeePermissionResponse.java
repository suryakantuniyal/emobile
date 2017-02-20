package com.android.emobilepos.models.response;

import com.android.emobilepos.models.realms.EmployeePersmission;
import com.android.emobilepos.models.realms.SalesAssociate;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by guarionex on 02-12-17.
 */

public class ClerkEmployeePermissionResponse {
    @SerializedName("PDA_DownloadClerks")
    @Expose
    private List<SalesAssociate> clerks = null;
    @SerializedName("PDA_GetEmployeePermissions")
    @Expose
    private List<EmployeePersmission> employeePersmissions = null;

    public List<SalesAssociate> getClerks() {
        return clerks;
    }

    public void setClerks(List<SalesAssociate> clerks) {
        this.clerks = clerks;
    }

    public List<EmployeePersmission> getEmployeePersmissions() {
        return employeePersmissions;
    }

    public void setEmployeePersmissions(List<EmployeePersmission> employeePersmissions) {
        this.employeePersmissions = employeePersmissions;
    }
}
