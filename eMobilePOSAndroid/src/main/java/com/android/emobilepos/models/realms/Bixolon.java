package com.android.emobilepos.models.realms;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by guarionex on 5/22/17.
 */

public class Bixolon extends RealmObject {
    @PrimaryKey
    private int pkid = 1;
    private String ruc;
    private RealmList<BixolonTax> bixolontaxes = new RealmList<>();
    private RealmList<BixolonPaymentMethod> paymentMethods = new RealmList<>();

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRuc() {
        return ruc;
    }

    public List<BixolonTax> getBixolontaxes() {
        return bixolontaxes;
    }

    public void setBixolontaxes(RealmList<BixolonTax> bixolontaxes) {
        this.bixolontaxes = bixolontaxes;
    }

    public List<BixolonPaymentMethod> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(RealmList<BixolonPaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }
}
