package com.android.dao;

import com.android.emobilepos.customer.ViewCustomerDetails_FA;
import com.android.emobilepos.models.realms.BiometricFid;
import com.android.emobilepos.models.realms.EmobileBiometric;
import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUException;

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

    public static Fmd[] getFmds(Engine engine) {
        Realm realm = Realm.getDefaultInstance();
        List<Fmd> fmds = new ArrayList<>();
        try {
            RealmResults<EmobileBiometric> all = realm.where(EmobileBiometric.class).findAll();
            if (all != null) {
                for (EmobileBiometric biometric : all) {
                    for (BiometricFid biometricFid : biometric.getFids()) {
                        Fid fidEntity = biometricFid.getFidEntity();
                        Fmd fmd = engine.CreateFmd(fidEntity, Fmd.Format.ANSI_378_2004);
//                                engine.CreateFmd(fidEntity.getData(), fidEntity.getViews()[0].getWidth(),
//                                fidEntity.getViews()[0].getHeight(), fidEntity.getViews()[0].getQuality(),
//                                fidEntity.getViews()[0].getFingerPosition(), fidEntity.getCbeffId(),
//                                Fmd.Format.ANSI_378_2004);
                        fmds.add(fmd);
                    }
                }
            }
        } catch (UareUException e) {
            e.printStackTrace();
        } finally {
            realm.close();
        }
        return fmds.toArray(new Fmd[fmds.size()]);
    }
}