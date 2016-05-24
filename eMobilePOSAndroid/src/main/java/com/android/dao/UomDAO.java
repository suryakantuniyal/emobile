package com.android.dao;

import com.android.emobilepos.models.UOM;
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
public class UomDAO {
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

        Type listType = new com.google.gson.reflect.TypeToken<List<UOM>>() {
        }.getType();
        try {
            List<UOM> uoms = gson.fromJson(json, listType);

            UomDAO.insert(uoms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert(List<UOM> uoms) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.clear(UOM.class);
        realm.copyToRealm(uoms);
        realm.commitTransaction();
    }

    public static RealmResults<UOM> getAll() {
        RealmResults<UOM> uoms = Realm.getDefaultInstance().allObjects(UOM.class);
        return uoms;
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.clear(UOM.class);
        realm.commitTransaction();
    }

    public static RealmResults<UOM> getByProdId(String prodId) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<UOM> where = realm.where(UOM.class);
        RealmResults<UOM> uoms = where.equalTo("prodId", prodId).findAll();
        return uoms;
    }
}