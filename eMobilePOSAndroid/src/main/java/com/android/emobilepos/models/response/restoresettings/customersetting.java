package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

public class customersetting {

    @SerializedName("Required")
    private boolean Required= false;
    @SerializedName("ClearAfterTrans")
    private boolean ClearAfterTrans= false;
    @SerializedName("AllowCreation")
    private boolean AllowCreation= false;
    @SerializedName("DirectSelection")
    private boolean DirectSelection= false;
    @SerializedName("DisplayAccountNumber")
    private boolean DisplayAccountNumber= false;
    @SerializedName("SelectTag")
    private String SelectTag="";


    public boolean isRequired() {
        return Required;
    }

    public void setRequired(boolean required) {
        Required = required;
    }

    public boolean isClearAfterTrans() {
        return ClearAfterTrans;
    }

    public void setClearAfterTrans(boolean clearAfterTrans) {
        ClearAfterTrans = clearAfterTrans;
    }

    public boolean isAllowCreation() {
        return AllowCreation;
    }

    public void setAllowCreation(boolean allowCreation) {
        AllowCreation = allowCreation;
    }

    public boolean isDirectSelection() {
        return DirectSelection;
    }

    public void setDirectSelection(boolean directSelection) {
        DirectSelection = directSelection;
    }

    public boolean isDisplayAccountNumber() {
        return DisplayAccountNumber;
    }

    public void setDisplayAccountNumber(boolean displayAccountNumber) {
        DisplayAccountNumber = displayAccountNumber;
    }

    public String getSelectTag() {
        return SelectTag;
    }

    public void setSelectTag(String selectTag) {
        SelectTag = selectTag;
    }
}
