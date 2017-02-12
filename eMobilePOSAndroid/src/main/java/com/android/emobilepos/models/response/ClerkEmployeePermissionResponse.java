package com.android.emobilepos.models.response;

import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.EmployeePersmission;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by guarionex on 02-12-17.
 */

public class ClerkEmployeePermissionResponse {
    @SerializedName("PDA_DownloadClerks")
    @Expose
    private List<Clerk> clerks = null;
    @SerializedName("PDA_GetEmployeePermissions")
    @Expose
    private List<EmployeePersmission> employeePersmissions = null;

    public List<Clerk> getClerks() {
        return clerks;
    }

    public void setClerks(List<Clerk> clerks) {
        this.clerks = clerks;
    }

    public List<EmployeePersmission> getEmployeePersmissions() {
        return employeePersmissions;
    }

    public void setEmployeePersmissions(List<EmployeePersmission> employeePersmissions) {
        this.employeePersmissions = employeePersmissions;
    }
}
