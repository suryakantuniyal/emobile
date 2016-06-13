package com.android.emobilepos.models;

/**
 * Created by Guarionex on 12/1/2015.
 */
public class Tax {
    private String taxName;
    private String taxId;
    private String taxRate;
    private String taxType;

    public Tax() {
    }

    public Tax(String taxId) {
        this.taxId = taxId;
        this.taxRate = "0.0";
    }

    public String getTaxName() {
        return taxName;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(String taxRate) {
        this.taxRate = taxRate;
    }

    public String getTaxType() {
        return taxType;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Tax) {
            return this.getTaxId().equalsIgnoreCase(((Tax) o).getTaxId());
        }
        return false;
    }
}
