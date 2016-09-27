package com.android.dao;

import com.android.emobilepos.models.EMSDevice;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import util.json.JsonUtils;

/**
 * Created by Guarionex on 4/12/2016.
 */
public class DeviceTableDAO {
    public static void insert(String json) {
        Gson gson = JsonUtils.getInstance();

        Type listType = new com.google.gson.reflect.TypeToken<List<EMSDevice>>() {
        }.getType();
        try {
            List<EMSDevice> devices = gson.fromJson(json, listType);
            DeviceTableDAO.insert(devices);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert(List<EMSDevice> devices) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(EMSDevice.class);
        realm.copyToRealm(devices);
        realm.commitTransaction();
    }

    public static RealmResults<EMSDevice> getAll() {
        if(Realm.getDefaultInstance().where(EMSDevice.class).isValid()) {
            return Realm.getDefaultInstance().where(EMSDevice.class).findAll();
        }
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.where(EMSDevice.class).findAll().deleteAllFromRealm();
        }finally {
            realm.commitTransaction();
        }
    }

    public static EMSDevice getByEmpId(int id) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<EMSDevice> where = realm.where(EMSDevice.class);
        return where.equalTo("id", id).findFirst();
    }
}