package com.android.dao;

import com.android.emobilepos.models.realms.Device;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import util.json.JsonUtils;

/**
 * Created by Guarionex on 4/12/2016.
 */
public class DeviceTableDAO {
    public static void insert(String json) {
        Gson gson = JsonUtils.getInstance();

        Type listType = new com.google.gson.reflect.TypeToken<List<Device>>() {
        }.getType();
        try {
            List<Device> devices = gson.fromJson(json, listType);
            DeviceTableDAO.insert(devices);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert(List<Device> devices) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(Device.class);
            realm.copyToRealm(devices);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static List<Device> getAll() {
        Realm realm = Realm.getDefaultInstance();
        List<Device> devices = realm.copyFromRealm(realm.where(Device.class).findAll());
        realm.close();
        return devices;
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            if (realm.where(Device.class).isValid()) {
                realm.delete(Device.class);
            }
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static Device getByEmpId(int id) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<Device> where = realm.where(Device.class);
        Device device = realm.copyFromRealm(where.equalTo("id", id).findFirst());
        realm.close();
        return device;
    }
}