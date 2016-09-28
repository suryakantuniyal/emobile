package com.android.emobilepos.models.realms;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Guarionex on 6/15/2016.
 */
public class ProductAttribute extends RealmObject {
    @PrimaryKey
    private int id;
    @Index
    @SerializedName("Prod_id")
    private String productId;
    @SerializedName("Attrid")
    private String attributeId;
    @SerializedName("ordprod_attr_name")
    private String attributeName;
    @SerializedName("required")
    private boolean required;
    private String value;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ProductAttribute) {
            return ((ProductAttribute) o).getId() == getId();
        }
        return false;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
