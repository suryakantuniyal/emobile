package com.android.emobilepos.models.realms;

import com.google.gson.annotations.Expose;
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


    @SerializedName("printer_id")
    @PrimaryKey
    @Expose(deserialize = true, serialize = true)
    private String id;
    @SerializedName("printer_name")
    @Index
    @Expose(deserialize = true, serialize = true)
    private String name;
    @SerializedName("printer_ip")
    @Expose(deserialize = true, serialize = true)
    private String ipAddress;
    @SerializedName("printer_port")
    @Expose(deserialize = true, serialize = true)
    private String tcpPort;
    @SerializedName("printer_type")
    @Expose(deserialize = true, serialize = true)
    private String type;
    @SerializedName("cat_name")
    @Expose(deserialize = true, serialize = true)
    private String categoryName;
    @SerializedName("cat_id")
    @Expose(deserialize = true, serialize = true)
    private String categoryId;
    @Expose(deserialize = false, serialize = false)
    private boolean isRemoteDevice = true;
    @Expose(deserialize = false, serialize = false)
    private RealmList<RealmString> selectedPritables;
    @Ignore
    @Expose(deserialize = false, serialize = false)
    private transient EMSDeviceManager emsDeviceManager;
    @Expose(deserialize = true, serialize = true)
    private String macAddress;
    @Expose(deserialize = true, serialize = true)
    private int textAreaSize;
    @Expose(deserialize = true, serialize = true)
    private boolean POS;

    public EMSDeviceManager getEmsDeviceManager() {
        return emsDeviceManager;
    }

    public void setEmsDeviceManager(EMSDeviceManager emsDeviceManager) {
        this.emsDeviceManager = emsDeviceManager;
    }

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

    public RealmList<RealmString> getSelectedPritables() {
        return selectedPritables;
    }

    public void setSelectedPritables(RealmList<RealmString> selectedPritables) {
        this.selectedPritables = selectedPritables;
    }

    @Override
    public boolean equals(Object obj) {
        return ((Device) obj).getId().equalsIgnoreCase(getId());
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public int getTextAreaSize() {
        return textAreaSize;
    }

    public void setTextAreaSize(int textAreaSize) {
        this.textAreaSize = textAreaSize;
    }

    public boolean isPOS() {
        return POS;
    }

    public void setPOS(boolean POS) {
        this.POS = POS;
    }

    public enum Printables {
        PAYMENT_RECEIPT, PAYMENT_RECEIPT_REPRINT, TRANSACTION_RECEIPT, TRANSACTION_RECEIPT_REPRINT, REPORTS;

        public RealmString getRealmString() {
            RealmString realmString = new RealmString();
            realmString.setValue(name());
            return realmString;
        }
    }
}
