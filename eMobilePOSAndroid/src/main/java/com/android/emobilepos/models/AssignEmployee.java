package com.android.emobilepos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by guarionex on 12/29/16.
 */

public class AssignEmployee extends RealmObject{

    @SerializedName("emp_id")
    @Expose
    @PrimaryKey
    private int empId;
    @SerializedName("emp_name")
    @Expose
    private String empName;
    @SerializedName("emp_lastlogin")
    @Expose
    private String empLastlogin;
    @SerializedName("emp_cleanup")
    @Expose
    private int empCleanup;
    @SerializedName("emp_pos")
    @Expose
    private int empPos;
    @SerializedName("qb_emp_id")
    @Expose
    private String qbEmpId;
    @SerializedName("MSLastOrderID")
    @Expose
    private String mSLastOrderID;
    @SerializedName("MSOrderEntry")
    @Expose
    private String mSOrderEntry;
    @SerializedName("MSCardProcessor")
    @Expose
    private String mSCardProcessor;
    @SerializedName("GatewayURL")
    @Expose
    private String gatewayURL;
    @SerializedName("approveCode")
    @Expose
    private String approveCode;
    @SerializedName("tax_default")
    @Expose
    private String taxDefault;
    @SerializedName("zone_id")
    @Expose
    private String zoneId;
    @SerializedName("MSLastTransferID")
    @Expose
    private String MSLastTransferID;
    @SerializedName("pricelevel_id")
    @Expose
    private String pricelevelId;
    private boolean VAT;
    @SerializedName("DefaultLocation")
    @Expose
    @Index
    private String defaultLocation;

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getEmpLastlogin() {
        return empLastlogin;
    }

    public void setEmpLastlogin(String empLastlogin) {
        this.empLastlogin = empLastlogin;
    }

    public int getEmpCleanup() {
        return empCleanup;
    }

    public void setEmpCleanup(int empCleanup) {
        this.empCleanup = empCleanup;
    }

    public int getEmpPos() {
        return empPos;
    }

    public void setEmpPos(int empPos) {
        this.empPos = empPos;
    }

    public String getQbEmpId() {
        return qbEmpId;
    }

    public void setQbEmpId(String qbEmpId) {
        this.qbEmpId = qbEmpId;
    }

    public String getMSLastOrderID() {
        return mSLastOrderID;
    }

    public void setMSLastOrderID(String mSLastOrderID) {
        this.mSLastOrderID = mSLastOrderID;
    }

    public String getMSOrderEntry() {
        return mSOrderEntry;
    }

    public void setMSOrderEntry(String mSOrderEntry) {
        this.mSOrderEntry = mSOrderEntry;
    }

    public String getMSCardProcessor() {
        return mSCardProcessor;
    }

    public void setMSCardProcessor(String mSCardProcessor) {
        this.mSCardProcessor = mSCardProcessor;
    }

    public String getGatewayURL() {
        return gatewayURL;
    }

    public void setGatewayURL(String gatewayURL) {
        this.gatewayURL = gatewayURL;
    }

    public String getApproveCode() {
        return approveCode;
    }

    public void setApproveCode(String approveCode) {
        this.approveCode = approveCode;
    }

    public String getTaxDefault() {
        return taxDefault;
    }

    public void setTaxDefault(String taxDefault) {
        this.taxDefault = taxDefault;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getMSLastTransferID() {
        return MSLastTransferID;
    }

    public void setMSLastTransferID(String MSLastTransferID) {
        this.MSLastTransferID = MSLastTransferID;
    }

    public String getPricelevelId() {
        return pricelevelId;
    }

    public void setPricelevelId(String pricelevelId) {
        this.pricelevelId = pricelevelId;
    }

    public boolean isVAT() {
        return VAT;
    }

    public void setVAT(boolean VAT) {
        this.VAT = VAT;
    }

    public String getDefaultLocation() {
        return defaultLocation;
    }

    public void setDefaultLocation(String defaultLocation) {
        this.defaultLocation = defaultLocation;
    }
}
