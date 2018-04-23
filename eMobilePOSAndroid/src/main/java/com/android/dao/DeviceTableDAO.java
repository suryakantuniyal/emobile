package com.android.dao;

import com.android.emobilepos.models.realms.Device;
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
            realm.copyToRealmOrUpdate(devices);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static List<Device> getAll() {
        Realm realm = Realm.getDefaultInstance();
        List<Device> all;
        try {
            all = realm.where(Device.class).findAll();
            if (all != null) {
                all = realm.copyFromRealm(all);
            }
        } finally {
            realm.close();
        }
        return all;
    }

    public static void truncateRemoteDevices() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            if (realm.where(Device.class).isValid()) {
                RealmResults<Device> devices = realm.where(Device.class)
                        .equalTo("isRemoteDevice", true)
                        .findAll();
                devices.deleteAllFromRealm();
            }
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static Device getByEmpId(int id) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmQuery<Device> where = realm.where(Device.class);
            Device first = where.equalTo("id", id).findFirst();
            if (first != null) {
                first = realm.copyFromRealm(first);
            }
            return first;
        } finally {
            realm.close();
        }
    }

    public static Device getByIp(String ip) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmQuery<Device> where = realm.where(Device.class);
            Device first = where.equalTo("ipAddress", ip).findFirst();
            if (first != null) {
                first = realm.copyFromRealm(first);
            }
            return first;
        } finally {
            realm.close();
        }
    }

    public static Device getByName(String name) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmQuery<Device> where = realm.where(Device.class);
            Device first = where.equalTo("name", name).findFirst();
            if (first != null) {
                first = realm.copyFromRealm(first);
            }
            return first;
        } finally {
            realm.close();
        }
    }

    public static void upsert(Device device) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.insertOrUpdate(device);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static Device getByPrintable(Device.Printables printable) {
        Realm realm = Realm.getDefaultInstance();
        try {
            List<Device> all = realm.where(Device.class).findAll();
            for (Device d : all) {
                boolean contains = d.getSelectedPritables().contains(printable.name());
                if (contains) {
                    return realm.copyFromRealm(d);
                }
            }
            return null;
        } finally {
            realm.close();
        }
    }
}