package com.android.emobilepos.models.realms;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import main.EMSDeviceManager;

/**
 * Created by Guarionex on 6/14/2016.
 */
public class Device extends RealmObject {


    public EMSDeviceManager getEmsDeviceManager() {
        return emsDeviceManager;
    }

    public void setEmsDeviceManager(EMSDeviceManager emsDeviceManager) {
        this.emsDeviceManager = emsDeviceManager;
    }

    public enum Printables {
        PAYMENT_RECEIPT, PAYMENT_RECEIPT_REPRINT, TRANSACTION_RECEIPT, TRANSACTION_RECEIPT_REPRINT, REPORTS;
    }

    @SerializedName("printer_id")
    @PrimaryKey
    private String id;
    @SerializedName("printer_name")
    @Index
    private String name;
    @SerializedName("printer_ip")
    private String ipAddress;
    @SerializedName("printer_port")
    private String tcpPort;
    @SerializedName("printer_type")
    private String type;
    @SerializedName("cat_name")
    private String categoryName;
    @SerializedName("cat_id")
    private String categoryId;
    private boolean isRemoteDevice = true;
    private RealmList<String> selectedPritables;
    @Ignore
    private EMSDeviceManager emsDeviceManager;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(String tcpPort) {
        this.tcpPort = tcpPort;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isRemoteDevice() {
        return isRemoteDevice;
    }

    public void setRemoteDevice(boolean remoteDevice) {
        isRemoteDevice = remoteDevice;
    }

    public RealmList<String> getSelectedPritables() {
        return selectedPritables;
    }

    public void setSelectedPritables(RealmList<String> selectedPritables) {
        this.selectedPritables = selectedPritables;
    }

    @Override
    public boolean equals(Object obj) {
        return ((Device) obj).getId().equalsIgnoreCase(getId());
    }
}
