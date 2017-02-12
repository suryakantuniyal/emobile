package com.android.emobilepos.models.realms;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.Index;

/**
 * Created by guarionex on 02-12-17.
 */

public class EmployeePersmission extends RealmObject {

    @SerializedName("p_id")
    @Expose
    @Index
    private int pId;
    @SerializedName("p_name")
    @Expose
    private String pName;
    @SerializedName("R_ID")
    @Expose
    @Index
    private int rID;
    @SerializedName("emp_id")
    @Expose
    @Index
    private int empId;

    public int getPId() {
        return pId;
    }

    public void setPId(int pId) {
        this.pId = pId;
    }

    public String getPName() {
        return pName;
    }

    public void setPName(String pName) {
        this.pName = pName;
    }

    public int getRID() {
        return rID;
    }

    public void setRID(int rID) {
        this.rID = rID;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }
}
