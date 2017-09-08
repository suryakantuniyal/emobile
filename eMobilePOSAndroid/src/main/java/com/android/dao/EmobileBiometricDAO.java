package com.android.dao;

import com.android.emobilepos.customer.ViewCustomerDetails_FA;
import com.android.emobilepos.models.realms.CustomerFid;
import com.android.emobilepos.models.realms.EmobileBiometric;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Guarionex on 4/12/2016.
 */
public class EmobileBiometricDAO {

    public static void upsert(EmobileBiometric biometric) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.insertOrUpdate(biometric);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static void delete(String id, EmobileBiometric.UserType userType) {
        Realm realm = Realm.getDefaultInstance();
        try{
            realm.beginTransaction();
            EmobileBiometric biometric = realm.where(EmobileBiometric.class)
                    .equalTo("id", id, Case.INSENSITIVE)
                    .equalTo("usereTypeCode", userType.getCode())
                    .findFirst();
            if(biometric!=null && biometric.isValid()) {
                biometric.getFids().clear();
            }
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static EmobileBiometric getBiometrics(String id, EmobileBiometric.UserType userType) {
        Realm realm = Realm.getDefaultInstance();
        EmobileBiometric biometric;
        try {
            biometric = realm.where(EmobileBiometric.class)
                    .equalTo("id", id, Case.INSENSITIVE)
                    .findFirst();
            if (biometric != null) {
                biometric = realm.copyFromRealm(biometric);
            } else {
                biometric = new EmobileBiometric();
                biometric.setId(id);
                biometric.setUserType(userType);
            }
        } finally {
            realm.close();
        }
        return biometric;
    }

    public static void deleteFinger(String customerId, EmobileBiometric.UserType userType, ViewCustomerDetails_FA.Finger finger) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            EmobileBiometric biometric = realm.where(EmobileBiometric.class)
                    .equalTo("id", customerId, Case.INSENSITIVE)
                    .equalTo("userTypeCode", userType.getCode())
                    .findFirst();
            if (biometric != null) {
                RealmResults<CustomerFid> fids = biometric.getFids().where().equalTo("fingerCode", finger.getCode()).findAll();
                if (fids != null) {
                    fids.deleteAllFromRealm();
                }
            }
        }finally {
            realm.commitTransaction();
            realm.close();
        }
    }
}