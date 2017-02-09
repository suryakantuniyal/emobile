package com.android.emobilepos.models.realms;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by guarionex on 2/8/17.
 */

public class OrderAttributes extends RealmObject {
    @SerializedName("ID_K")
    @Expose
    @PrimaryKey
    private String iDK;
    @SerializedName("ord_attr_name")
    @Expose
    private String ordAttrName;
    @SerializedName("isrequired")
    @Expose
    private boolean isrequired;
    @SerializedName("ord_attr_group")
    @Expose
    private String ordAttrGroup;
    @SerializedName("ord_attr_group_name")
    @Expose
    private String ordAttrGroupName;
    @SerializedName("ord_attr_type")
    @Expose
    private String ordAttrType;

    @Ignore
    @Expose(deserialize = false, serialize = false)
    private
    String inputValue;

    public OrderAttributes() {

    }

    public OrderAttributes(String attr) {
        this.ordAttrName = attr;
    }

    public String getIDK() {
        return iDK;
    }

    public void setIDK(String iDK) {
        this.iDK = iDK;
    }

    public String getOrdAttrName() {
        return ordAttrName;
    }

    public void setOrdAttrName(String ordAttrName) {
        this.ordAttrName = ordAttrName;
    }

    public boolean isIsrequired() {
        return isrequired;
    }

    public void setIsrequired(boolean isrequired) {
        this.isrequired = isrequired;
    }

    public String getOrdAttrGroup() {
        return ordAttrGroup;
    }

    public void setOrdAttrGroup(String ordAttrGroup) {
        this.ordAttrGroup = ordAttrGroup;
    }

    public String getOrdAttrGroupName() {
        return ordAttrGroupName;
    }

    public void setOrdAttrGroupName(String ordAttrGroupName) {
        this.ordAttrGroupName = ordAttrGroupName;
    }

    public String getOrdAttrType() {
        return ordAttrType;
    }

    public void setOrdAttrType(String ordAttrType) {
        this.ordAttrType = ordAttrType;
    }

    public String getInputValue() {
        return inputValue;
    }

    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }
}
