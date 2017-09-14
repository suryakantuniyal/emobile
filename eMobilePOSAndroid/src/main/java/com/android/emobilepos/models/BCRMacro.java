package com.android.emobilepos.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class BCRMacro {
    @SerializedName("START_ORDER")
    @Expose
    private BCRMacroParams bcrMacroParams;

    public BCRMacroParams getBcrMacroParams() {
        return bcrMacroParams;
    }

    public void setBcrMacroParams(BCRMacroParams bcrMacroParams) {
        this.bcrMacroParams = bcrMacroParams;
    }

    public class BCRMacroParams {
        @SerializedName("cust_id")
        @Expose
        private String custId;
        @SerializedName("LOADTEMPLATE")
        @Expose
        private boolean loadTemplate;

        public String getCustId() {
            return custId;
        }

        public void setCustId(String custId) {
            this.custId = custId;
        }

        public boolean isLoadTemplate() {
            return loadTemplate;
        }

        public void setLoadTemplate(boolean loadTemplate) {
            this.loadTemplate = loadTemplate;
        }
    }
}


