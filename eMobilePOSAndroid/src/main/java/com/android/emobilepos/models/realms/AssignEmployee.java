package com.android.emobilepos.models.realms;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by guarionex on 12/8/16.
 */

public class AssignEmployee extends RealmObject {
    @PrimaryKey
    @SerializedName("emp_id")
    @Expose
    private int empId;
    @SerializedName("zone_id")
    @Expose
    private String zoneId;
    @SerializedName("emp_name")
    @Expose
    private String empName;
    @SerializedName("emp_init")
    @Expose
    private String empInit;
    @SerializedName("emp_pcs")
    @Expose
    private String empPcs;
    @SerializedName("emp_lastlogin")
    @Expose
    private String empLastlogin;
    @SerializedName("emp_pos")
    @Expose
    private int empPos;
    @SerializedName("qb_emp_id")
    @Expose
    private String qbEmpId;
    @SerializedName("qb_salesrep_id")
    @Expose
    private String qbSalesrepId;
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
    @SerializedName("pricelevel_id")
    @Expose
    private String pricelevelId;
    @SerializedName("VAT")
    @Expose
    private boolean vAT;
    @SerializedName("DefaultLocation")
    @Expose
    @Index
    private String defaultLocation;
    private String MSLastTransferID;
    String temp;

    /**
     * @return The empId
     */
    public int getEmpId() {
        return empId;
    }

    /**
     * @param empId The emp_id
     */
    public void setEmpId(int empId) {
        this.empId = empId;
    }

    /**
     * @return The zoneId
     */
    public String getZoneId() {
        return zoneId;
    }

    /**
     * @param zoneId The zone_id
     */
    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    /**
     * @return The empName
     */
    public String getEmpName() {
        return empName;
    }

    /**
     * @param empName The emp_name
     */
    public void setEmpName(String empName) {
        this.empName = empName;
    }

    /**
     * @return The empInit
     */
    public String getEmpInit() {
        return empInit;
    }

    /**
     * @param empInit The emp_init
     */
    public void setEmpInit(String empInit) {
        this.empInit = empInit;
    }

    /**
     * @return The empPcs
     */
    public String getEmpPcs() {
        return empPcs;
    }

    /**
     * @param empPcs The emp_pcs
     */
    public void setEmpPcs(String empPcs) {
        this.empPcs = empPcs;
    }

    /**
     * @return The empLastlogin
     */
    public String getEmpLastlogin() {
        return empLastlogin;
    }

    /**
     * @param empLastlogin The emp_lastlogin
     */
    public void setEmpLastlogin(String empLastlogin) {
        this.empLastlogin = empLastlogin;
    }

    /**
     * @return The empPos
     */
    public int getEmpPos() {
        return empPos;
    }

    /**
     * @param empPos The emp_pos
     */
    public void setEmpPos(int empPos) {
        this.empPos = empPos;
    }

    /**
     * @return The qbEmpId
     */
    public String getQbEmpId() {
        return qbEmpId;
    }

    /**
     * @param qbEmpId The qb_emp_id
     */
    public void setQbEmpId(String qbEmpId) {
        this.qbEmpId = qbEmpId;
    }

    /**
     * @return The qbSalesrepId
     */
    public String getQbSalesrepId() {
        return qbSalesrepId;
    }

    /**
     * @param qbSalesrepId The qb_salesrep_id
     */
    public void setQbSalesrepId(String qbSalesrepId) {
        this.qbSalesrepId = qbSalesrepId;
    }

    /**
     * @return The mSLastOrderID
     */
    public String getMSLastOrderID() {
        return mSLastOrderID;
    }

    /**
     * @param mSLastOrderID The MSLastOrderID
     */
    public void setMSLastOrderID(String mSLastOrderID) {
        this.mSLastOrderID = mSLastOrderID;
    }

    /**
     * @return The mSOrderEntry
     */
    public String getMSOrderEntry() {
        return mSOrderEntry;
    }

    /**
     * @param mSOrderEntry The MSOrderEntry
     */
    public void setMSOrderEntry(String mSOrderEntry) {
        this.mSOrderEntry = mSOrderEntry;
    }

    /**
     * @return The mSCardProcessor
     */
    public String getMSCardProcessor() {
        return mSCardProcessor;
    }

    /**
     * @param mSCardProcessor The MSCardProcessor
     */
    public void setMSCardProcessor(String mSCardProcessor) {
        this.mSCardProcessor = mSCardProcessor;
    }

    /**
     * @return The gatewayURL
     */
    public String getGatewayURL() {
        return gatewayURL;
    }

    /**
     * @param gatewayURL The GatewayURL
     */
    public void setGatewayURL(String gatewayURL) {
        this.gatewayURL = gatewayURL;
    }

    /**
     * @return The approveCode
     */
    public String getApproveCode() {
        return approveCode;
    }

    /**
     * @param approveCode The approveCode
     */
    public void setApproveCode(String approveCode) {
        this.approveCode = approveCode;
    }

    /**
     * @return The taxDefault
     */
    public String getTaxDefault() {
        return taxDefault;
    }

    /**
     * @param taxDefault The tax_default
     */
    public void setTaxDefault(String taxDefault) {
        this.taxDefault = taxDefault;
    }

    /**
     * @return The pricelevelId
     */
    public String getPricelevelId() {
        return pricelevelId;
    }

    /**
     * @param pricelevelId The pricelevel_id
     */
    public void setPricelevelId(String pricelevelId) {
        this.pricelevelId = pricelevelId;
    }

    /**
     * @return The vAT
     */
    public boolean isVAT() {
        return vAT;
    }

    /**
     * @param vAT The VAT
     */
    public void setVAT(boolean vAT) {
        this.vAT = vAT;
    }

    /**
     * @return The defaultLocation
     */
    public String getDefaultLocation() {
        return defaultLocation;
    }

    /**
     * @param defaultLocation The DefaultLocation
     */
    public void setDefaultLocation(String defaultLocation) {
        this.defaultLocation = defaultLocation;
    }

    public void setMSLastTransferID(String MSLastTransferID) {
        this.MSLastTransferID = MSLastTransferID;
    }

    public String getMSLastTransferID() {
        return MSLastTransferID;
    }
}
