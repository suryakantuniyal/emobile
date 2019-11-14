package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class SyncPlusServices {

    @SerializedName("UseSyncPlusServices")
    private boolean UseSyncPlusServices= false;
    @SerializedName("ConnectionMode")
    private String ConnectionMode="";
    @SerializedName("IPAddress")
    private String IPAddress="";
    @SerializedName("PortNumber")
    private String PortNumber="";

    public boolean isUseSyncPlusServices() {
        return UseSyncPlusServices;
    }

    public void setUseSyncPlusServices(boolean useSyncPlusServices) {
        UseSyncPlusServices = useSyncPlusServices;
    }

    public String getConnectionMode() {
        return ConnectionMode;
    }

    public void setConnectionMode(String connectionMode) {
        ConnectionMode = connectionMode;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public String getPortNumber() {
        return PortNumber;
    }

    public void setPortNumber(String portNumber) {
        PortNumber = portNumber;
    }
}
