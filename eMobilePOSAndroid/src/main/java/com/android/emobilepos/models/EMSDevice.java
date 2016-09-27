package com.android.emobilepos.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by Guarionex on 6/14/2016.
 */
public class EMSDevice extends RealmObject {
    @SerializedName("printer_id")
    private String id;
    @SerializedName("printer_name")
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
}
