package com.android.dao;

import com.android.emobilepos.models.realms.Bixolon;
import com.android.emobilepos.models.realms.BixolonPaymentMethod;
import com.android.emobilepos.models.realms.BixolonTax;
import com.android.emobilepos.models.realms.BixolonTransaction;
import com.android.emobilepos.models.realms.PaymentMethod;

import java.util.List;

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

    public static BixolonTax getTax(String prod_taxcode, String prod_taxId) {
        Realm realm = Realm.getDefaultInstance();
        BixolonTax bixolonTax;
        try {
            bixolonTax = realm.where(BixolonTax.class)
                    .equalTo("taxId", prod_taxcode)
//                    .equalTo("taxCode", prod_taxcode)
                    .findFirst();
            if (bixolonTax != null) {
                bixolonTax = realm.copyFromRealm(bixolonTax);
            }
        } finally {
            realm.close();
        }
        return bixolonTax;
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

    public static int getPaymentMethodId(PaymentMethod paymentMethod) {
        Realm realm = Realm.getDefaultInstance();
        try {
            BixolonPaymentMethod method = realm.where(BixolonPaymentMethod.class)
                    .equalTo("paymentMethod.paymethod_id", paymentMethod.getPaymethod_id())
                    .findFirst();
            if (method != null) {
                return method.getId();
            }
        } finally {
            realm.close();
        }
        return -1;
    }

    public static void insertFailedOrder(BixolonTransaction bixolonTransaction) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.insertOrUpdate(bixolonTransaction);
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    public static List<BixolonTransaction> getFailedTransactions() {
        Realm realm = Realm.getDefaultInstance();
        List<BixolonTransaction> all;
        try {
            all = realm.where(BixolonTransaction.class).findAll();
            if (all != null) {
                all = realm.copyFromRealm(all);
            }
        } finally {
            realm.close();
        }
        return all;
    }

    public static void removeFailedOrder(String ord_id) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            BixolonTransaction transaction = realm.where(BixolonTransaction.class).equalTo("orderId", ord_id).findFirst();
            if (transaction != null) {
                transaction.deleteFromRealm();
            }
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }
}
