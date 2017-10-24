package com.android.dao;

import com.android.emobilepos.customer.ViewCustomerDetails_FA;
import com.android.emobilepos.models.realms.BiometricFid;
import com.android.emobilepos.models.realms.EmobileBiometric;
import com.digitalpersona.uareu.Fmd;

import java.util.ArrayList;
import java.util.List;

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
                    .equalTo("entityid", id, Case.INSENSITIVE)
                    .equalTo("userTypeCode", userType.getCode())
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
                    .equalTo("entityid", id, Case.INSENSITIVE)
                    .findFirst();
            if (biometric != null) {
                biometric = realm.copyFromRealm(biometric);
            } else {
                biometric = new EmobileBiometric();
                biometric.setEntityid(id);
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
                    .equalTo("entityid", customerId, Case.INSENSITIVE)
                    .equalTo("userTypeCode", userType.getCode())
                    .findFirst();
            if (biometric != null) {
                RealmResults<BiometricFid> fids = biometric.getFids().where().equalTo("fingerCode", finger.getCode()).findAll();
                if (fids != null) {
                    fids.deleteAllFromRealm();
                }
            }
        }finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static Fmd[] getFmds(EmobileBiometric.UserType userType) {
        Realm realm = Realm.getDefaultInstance();
        List<Fmd> fmds = new ArrayList<>();
        try {
            RealmResults<EmobileBiometric> all = realm.where(EmobileBiometric.class)
                    .equalTo("userTypeCode", userType.getCode())
                    .findAll();
            if (all != null) {
                for (EmobileBiometric biometric : all) {
                    for (BiometricFid biometricFid : biometric.getFids()) {
//                        Fid fidEntity = biometricFid.getFidEntity();
                        Fmd fmd = biometricFid.getFmdEntity(); //engine.CreateFmd(fidEntity, Fmd.Format.ANSI_378_2004);
                        fmds.add(fmd);
                    }
                }
            }
        } finally {
            realm.close();
        }
        return fmds.toArray(new Fmd[fmds.size()]);
    }

    public static EmobileBiometric getBiometrics(Fmd fmd) {
        Realm realm = Realm.getDefaultInstance();
        String uuid = BiometricFid.md5(fmd.getData());
        EmobileBiometric biometric = null;
        try {
            biometric = realm.where(EmobileBiometric.class)
                    .equalTo("fids.id", uuid)
                    .findFirst();
            if (biometric != null) {
                biometric = realm.copyFromRealm(biometric);
            }
        } finally {
            realm.close();
        }
        return biometric;
    }

    public static List<EmobileBiometric> getBiometrics() {
        Realm realm = Realm.getDefaultInstance();
        List<EmobileBiometric> all = null;
        try {
            all = realm.where(EmobileBiometric.class).findAll();
            if (all != null) {
                all = realm.copyFromRealm(all);
            }
        } finally {
            realm.close();
            return all;
        }
    }
public static void truncate(){
    Realm realm = Realm.getDefaultInstance();
    try {
        realm.beginTransaction();
        RealmResults<EmobileBiometric> all = realm.where(EmobileBiometric.class).findAll();
        all.deleteAllFromRealm();
        realm.commitTransaction();
    } finally {
        realm.close();
    }
}
    public static void upsert(List<EmobileBiometric> emobileBiometrics) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.insertOrUpdate(emobileBiometrics);
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }
}