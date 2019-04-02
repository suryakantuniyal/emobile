package com.android.dao;

import com.android.emobilepos.models.realms.PaymentMethod;

import java.util.List;

import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by guarionex on 10/24/16.
 */

public class PayMethodsDAO {
    public static void insert(PaymentMethod paymentMethod) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            if (paymentMethod != null) {
                realm.copyToRealm(paymentMethod);
            }
        } finally {
            realm.commitTransaction();
            if(realm!=null) {
                realm.close();
            }
        }
    }

    public static void delete(String payId) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            if (!android.text.TextUtils.isEmpty(payId)) {
                PaymentMethod method = realm.where(PaymentMethod.class).equalTo("paymethod_id", payId).findFirst();
                if (method != null && method.isValid()) {
                    method.deleteFromRealm();
                }
            }
        } finally {
            realm.commitTransaction();
            if (realm != null) {
                realm.close();
            }
        }
    }

    public static List<PaymentMethod> getAllSortByName() {
        Realm realm = Realm.getDefaultInstance();
        try {
            List<PaymentMethod> paymentMethods = realm.where(PaymentMethod.class).findAll().sort("paymethod_name", Sort.ASCENDING);
            if (paymentMethods != null) {
                paymentMethods = realm.copyFromRealm(paymentMethods);
            }
            return paymentMethods;
        } finally {
            if(realm!=null) {
                realm.close();
            }
        }
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(PaymentMethod.class);
        } finally {
            realm.commitTransaction();
            if(realm!=null) {
                realm.close();
            }
        }
    }
}
