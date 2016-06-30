package com.android.dao;

import com.android.emobilepos.models.Device;
import com.android.emobilepos.models.DinningTable;
import com.android.emobilepos.models.SalesAssociate;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Guarionex on 4/12/2016.
 */
public class DeviceTableDAO {
    public static void insert(String json) {
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();

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
        realm.beginTransaction();
        realm.clear(Device.class);
        realm.copyToRealm(devices);
        realm.commitTransaction();
    }

    public static RealmResults<Device> getAll() {
        RealmResults<Device> devices = Realm.getDefaultInstance().allObjects(Device.class);
        return devices;
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.clear(Device.class);
        realm.commitTransaction();
    }

    public static Device getByEmpId(int id) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<Device> where = realm.where(Device.class);
        Device device = where.equalTo("id", id).findFirst();
        return device;
    }
}