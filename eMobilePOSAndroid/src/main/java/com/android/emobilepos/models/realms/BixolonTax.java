package com.android.emobilepos.models.realms;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by guarionex on 5/23/17.
 */

public class BixolonTax extends RealmObject {
    private String taxId;
    private String taxCode;
    @PrimaryKey
    private String bixolonChar;

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getBixolonChar() {
        return bixolonChar;
    }

    public void setBixolonChar(String bixolonChar) {
        this.bixolonChar = bixolonChar;
    }
}
