package com.android.emobilepos.models.realms;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Guarionex on 3/8/2016.
 */
public class Clerk extends RealmObject {
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
    @SerializedName("emp_pwd")
    @Expose
    @Index
    private String empPwd;
    @SerializedName("isactive")
    @Expose
    @Index
    private int isactive;
    @SerializedName("loc_items")
    @Expose
    private boolean locItems;
    @SerializedName("_rowversion")
    @Expose
    private String rowversion;
    @SerializedName("lastSync")
    @Expose
    private String lastSync;
    @SerializedName("TupyWalletDevice")
    @Expose
    private boolean tupyWalletDevice;
    @SerializedName("VAT")
    @Expose
    private boolean vAT;
    @SerializedName("isUnenryptedSwipeAllowed")
    @Expose
    private boolean isUnenryptedSwipeAllowed;
    @SerializedName("role_id")
    @Expose
    private int roleId;
    @SerializedName("qb_salesrep_id")
    @Expose
    private String qbSalesrepId;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("zone_id")
    @Expose
    private String zoneId;
    @SerializedName("tax_default")
    @Expose
    private String taxDefault;
    @SerializedName("pricelevel_id")
    @Expose
    private String pricelevelId;
    private RealmList<DinningTable> assignedDinningTables;


    @Override
    public String toString() {
        return String.format("%s (%s)", getEmpName(), String.valueOf(getEmpId()));
    }

    public RealmList<DinningTable> getAssignedDinningTables() {
        if (assignedDinningTables == null) {
            assignedDinningTables = new RealmList<>();
        }
        return assignedDinningTables;
    }

    public void setAssignedDinningTables(RealmList<DinningTable> assignedDinningTables) {
        this.assignedDinningTables = assignedDinningTables;
    }

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

    public String getEmpPwd() {
        return empPwd;
    }

    public void setEmpPwd(String empPwd) {
        this.empPwd = empPwd;
    }

    public int getIsactive() {
        return isactive;
    }

    public void setIsactive(int isactive) {
        this.isactive = isactive;
    }

    public boolean isLocItems() {
        return locItems;
    }

    public void setLocItems(boolean locItems) {
        this.locItems = locItems;
    }

    public String getRowversion() {
        return rowversion;
    }

    public void setRowversion(String rowversion) {
        this.rowversion = rowversion;
    }

    public String getLastSync() {
        return lastSync;
    }

    public void setLastSync(String lastSync) {
        this.lastSync = lastSync;
    }

    public boolean isTupyWalletDevice() {
        return tupyWalletDevice;
    }

    public void setTupyWalletDevice(boolean tupyWalletDevice) {
        this.tupyWalletDevice = tupyWalletDevice;
    }

    public boolean isvAT() {
        return vAT;
    }

    public void setvAT(boolean vAT) {
        this.vAT = vAT;
    }

    public boolean isUnenryptedSwipeAllowed() {
        return isUnenryptedSwipeAllowed;
    }

    public void setUnenryptedSwipeAllowed(boolean unenryptedSwipeAllowed) {
        isUnenryptedSwipeAllowed = unenryptedSwipeAllowed;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getQbSalesrepId() {
        return qbSalesrepId;
    }

    public void setQbSalesrepId(String qbSalesrepId) {
        this.qbSalesrepId = qbSalesrepId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getTaxDefault() {
        return taxDefault;
    }

    public void setTaxDefault(String taxDefault) {
        this.taxDefault = taxDefault;
    }

    public String getPricelevelId() {
        return pricelevelId;
    }

    public void setPricelevelId(String pricelevelId) {
        this.pricelevelId = pricelevelId;
    }
}
