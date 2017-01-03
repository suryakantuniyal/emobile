package com.android.emobilepos.models.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by guarionex on 1/3/17.
 */

public class HubRegistrationPNS {
    @SerializedName("empid")
    @Expose
    private int employeeId;
    @SerializedName("DeviceID")
    @Expose
    private String deviceID;
    @SerializedName("PNS")
    @Expose
    private String pns;
    @SerializedName("OS")
    @Expose
    private String os;
    @SerializedName("BundleVersion")
    @Expose
    private String bundleVersion;
    @SerializedName("ActivationKey")
    @Expose
    private String activationKey;

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getPns() {
        return pns;
    }

    public void setPns(String pns) {
        this.pns = pns;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getBundleVersion() {
        return bundleVersion;
    }

    public void setBundleVersion(String bundleVersion) {
        this.bundleVersion = bundleVersion;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }
}
