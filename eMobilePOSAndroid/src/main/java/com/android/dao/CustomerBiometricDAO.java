package com.android.dao;

import com.android.emobilepos.customer.ViewCustomerDetails_FA;
import com.android.emobilepos.models.realms.CustomerBiometric;
import com.android.emobilepos.models.realms.CustomerFid;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

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
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static CustomerBiometric getBiometrics(String customerId) {
        Realm realm = Realm.getDefaultInstance();
        CustomerBiometric biometric;
        try {
            biometric = realm.where(CustomerBiometric.class)
                    .equalTo("customerId", customerId, Case.INSENSITIVE)
                    .findFirst();
            if (biometric != null) {
                biometric = realm.copyFromRealm(biometric);
            }
        } finally {
            realm.close();
        }
        return biometric;
    }

    public static void deleteFinger(String customerId, ViewCustomerDetails_FA.Finger finger) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            CustomerBiometric biometric = realm.where(CustomerBiometric.class)
                    .equalTo("customerId", customerId, Case.INSENSITIVE)
                    .findFirst();
            RealmResults<CustomerFid> fids = biometric.getFids().where().equalTo("fingerCode", finger.getCode()).findAll();
            if (fids != null) {
                fids.deleteAllFromRealm();
            }
        }finally {
            realm.commitTransaction();
            realm.close();
        }
    }
}