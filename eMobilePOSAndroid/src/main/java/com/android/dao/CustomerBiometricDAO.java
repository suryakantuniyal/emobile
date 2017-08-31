package com.android.dao;

import com.android.emobilepos.models.realms.CustomerBiometric;

import io.realm.Case;
import io.realm.Realm;

/**
 * Created by Guarionex on 4/12/2016.
 */
public class CustomerBiometricDAO {

    public static void upsert(CustomerBiometric biometric) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.insertOrUpdate(biometric);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static void delete(String customerId) {
        Realm realm = Realm.getDefaultInstance();
        try{
            realm.beginTransaction();
            CustomerBiometric biometric = realm.where(CustomerBiometric.class)
                    .equalTo("customerId", customerId, Case.INSENSITIVE)
                    .findFirst();
            if(biometric!=null && biometric.isValid()) {
                biometric.getFids().clear();
            }
        }finally {
            realm.commitTransaction();
            realm.close();
        }
    }
}