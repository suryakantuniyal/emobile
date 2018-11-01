package com.android.emobilepos.models;

import java.util.UUID;

public class DataTaxes {

    private String ord_tax_id = "";
    private String ord_id = "";
    private String tax_name = "";
    private String tax_amount = "";
    private String tax_rate = "";

    public DataTaxes() {
        setOrd_tax_id(UUID.randomUUID().toString());
    }

    public String getOrd_tax_id() {
        return ord_tax_id;
    }

    public void setOrd_tax_id(String ord_tax_id) {
        this.ord_tax_id = ord_tax_id;
    }

    public String getOrd_id() {
        return ord_id;
    }

    public void setOrd_id(String ord_id) {
        this.ord_id = ord_id;
    }

    public String getTax_name() {
        return tax_name;
    }

    public void setTax_name(String tax_name) {
        this.tax_name = tax_name;
    }

    public String getTax_amount() {
        return tax_amount;
    }

    public void setTax_amount(String tax_amount) {
        this.tax_amount = tax_amount;
    }

    public String getTax_rate() {
        return tax_rate;
    }

    public void setTax_rate(String tax_rate) {
        this.tax_rate = tax_rate;
    }
}