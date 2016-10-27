package com.android.dao;

import com.android.emobilepos.models.PaymentMethod;

import java.util.List;

import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by guarionex on 10/24/16.
 */


public class PayMethodsDAO {
    public static void insert(PaymentMethod paymentMethod) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        if (paymentMethod != null) {
            realm.copyToRealm(paymentMethod);
        }
        realm.commitTransaction();
    }

    public static void delete(String payId) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        if (!android.text.TextUtils.isEmpty(payId)) {
            PaymentMethod method = realm.where(PaymentMethod.class).equalTo("paymethod_id", payId).findFirst();
            if (method != null && method.isValid()) {
                method.deleteFromRealm();
            }
        }
        realm.commitTransaction();
    }

    public static List<PaymentMethod> getAllSortByName() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(PaymentMethod.class).findAll().sort("paymethod_name", Sort.ASCENDING);
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(PaymentMethod.class);
        realm.commitTransaction();
    }
}
