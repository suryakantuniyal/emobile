package com.android.emobilepos.models;


public class Country {
    private String isoCode;
    private String name;
    private boolean defaultCountry;

    public Country(String isoCode, String name, boolean defaultCountry) {
        this.isoCode = isoCode;
        this.name = name;
        this.defaultCountry = defaultCountry;
    }

    public Country() {

    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefaultCountry() {
        return defaultCountry;
    }

    public void setDefaultCountry(boolean defaultCountry) {
        this.defaultCountry = defaultCountry;
    }
}
