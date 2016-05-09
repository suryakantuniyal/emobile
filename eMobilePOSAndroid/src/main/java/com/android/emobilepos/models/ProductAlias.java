package com.android.emobilepos.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by guari_000 on 5/9/2016.
 */
public class ProductAlias {
    @SerializedName("prod_id")
    private String prod_id;
    @SerializedName("prod_alias")
    private String prod_alias;

    public String getProd_id() {
        return prod_id;
    }

    public void setProd_id(String prod_id) {
        this.prod_id = prod_id;
    }

    public String getProd_alias() {
        return prod_alias;
    }

    public void setProd_alias(String prod_alias) {
        this.prod_alias = prod_alias;
    }
}
