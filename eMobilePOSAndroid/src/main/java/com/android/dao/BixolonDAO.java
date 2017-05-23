package com.android.dao;

import com.android.emobilepos.models.realms.Bixolon;
import com.android.emobilepos.models.realms.BixolonPaymentMethod;
import com.android.emobilepos.models.realms.BixolonTax;
import com.android.emobilepos.models.realms.PaymentMethod;

import io.realm.Realm;

/**
 * Created by guarionex on 5/23/17.
 */

public class BixolonDAO {

    public static Bixolon getBixolon() {
        Realm realm = Realm.getDefaultInstance();
        Bixolon first;
        try {
            first = realm.where(Bixolon.class).findFirst();
            if (first != null) {
                first = realm.copyFromRealm(first);
            }
        } finally {
            realm.close();
        }
        return first;
    }

    public static void save(Bixolon bixolon) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(bixolon);
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    public static void addTax(String taxId, String taxCodeId, String bixolonTaxChar) {
        Bixolon bixolon = getBixolon();
        if (bixolon == null) {
            bixolon = new Bixolon();
        }
        BixolonTax tax = new BixolonTax();
        tax.setBixolonChar(bixolonTaxChar);
        tax.setTaxCode(taxCodeId);
        tax.setTaxId(taxId);
        bixolon.getBixolontaxes().add(tax);
        save(bixolon);
    }

    public static BixolonTax getTax(String prod_taxId, String prod_taxcode) {
        Realm realm = Realm.getDefaultInstance();
        try {
            BixolonTax first = realm.where(BixolonTax.class)
                    .equalTo("taxId", prod_taxId)
                    .equalTo("taxCode", prod_taxcode)
                    .findFirst();
        } finally {
            realm.close();
        }
        return null;
    }

    public static void clearTaxes() {
        Bixolon bixolon = getBixolon();
        if (bixolon != null) {
            bixolon.getBixolontaxes().clear();
            save(bixolon);
        }
    }

    public static void clearPaymentMethods() {
        Bixolon bixolon = getBixolon();
        if (bixolon != null) {
            bixolon.getPaymentMethods().clear();
            save(bixolon);
        }
    }

    public static void addPaymentMethod(int id, PaymentMethod paymentMethod) {
        Bixolon bixolon = getBixolon();
        if (bixolon == null) {
            bixolon = new Bixolon();
        }
        BixolonPaymentMethod bixolonPaymentMethod = new BixolonPaymentMethod();
        bixolonPaymentMethod.setId(id);
        bixolonPaymentMethod.setPaymentMethod(paymentMethod);
        bixolon.getPaymentMethods().add(bixolonPaymentMethod);
        save(bixolon);
    }
}
