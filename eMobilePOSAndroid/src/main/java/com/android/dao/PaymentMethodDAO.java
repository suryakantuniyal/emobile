package com.android.dao;

import com.android.emobilepos.models.realms.PaymentMethod;

import java.util.List;

import io.realm.Realm;

/**
 * Created by guarionex on 9/28/16.
 */

public class PaymentMethodDAO {
    public static PaymentMethod getPaymentMethodByType(String paymentmethodType) {
        return Realm.getDefaultInstance().where(PaymentMethod.class).equalTo("paymentmethod_type", paymentmethodType).findFirst();
    }

    public static PaymentMethod getPaymentMethodById(String paymethodId) {
        return Realm.getDefaultInstance()
                .where(PaymentMethod.class)
                .equalTo("paymethod_id", paymethodId).findFirst();
    }

    public static void incrementPriority(PaymentMethod paymentMethod) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            paymentMethod.incrementPriority();
        } finally {
            realm.commitTransaction();
        }
    }

    public static void insert(List<PaymentMethod> paymentMethods) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.insert(paymentMethods);
        } finally {
            realm.commitTransaction();
        }
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(PaymentMethod.class);
        } finally {
            realm.commitTransaction();
        }
    }
}
