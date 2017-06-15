package com.android.dao;

import com.android.emobilepos.models.realms.PaymentMethod;

import java.util.List;

import io.realm.Realm;

/**
 * Created by guarionex on 9/28/16.
 */

public class PaymentMethodDAO {
    public static PaymentMethod getPaymentMethodByType(String paymentmethodType) {
        Realm realm = Realm.getDefaultInstance();
        try {
            PaymentMethod type = realm.where(PaymentMethod.class).equalTo("paymentmethod_type", paymentmethodType).findFirst();
            if (type != null) {
                type = realm.copyFromRealm(type);
            }
            return type;
        } finally {
            realm.close();
        }
    }

    public static PaymentMethod getPaymentMethodById(String paymethodId) {
        Realm realm = Realm.getDefaultInstance();
        try {
            PaymentMethod method = realm.where(PaymentMethod.class)
                    .equalTo("paymethod_id", paymethodId).findFirst();
            if (method != null) {
                method = realm.copyFromRealm(method);
            }
            return method;
        } finally {
            realm.close();
        }
    }

    public static void incrementPriority(PaymentMethod paymentMethod) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            paymentMethod.incrementPriority();
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static void insert(List<PaymentMethod> paymentMethods) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.insert(paymentMethods);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(PaymentMethod.class);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }
}
