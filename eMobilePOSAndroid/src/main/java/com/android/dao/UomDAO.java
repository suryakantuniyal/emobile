package com.android.dao;

import com.android.emobilepos.models.UOM;
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
public class UomDAO {
    public static void insert(String json) {
        Gson gson = JsonUtils.getInstance();

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
        realm.delete(UOM.class);
        realm.copyToRealm(uoms);
        realm.commitTransaction();
    }

    public static RealmResults<UOM> getAll() {
        RealmResults<UOM> uoms = Realm.getDefaultInstance().where(UOM.class).findAll();
        return uoms;
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(UOM.class);
        }finally {
            realm.commitTransaction();
        }
    }

    public static RealmResults<UOM> getByProdId(String prodId) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<UOM> where = realm.where(UOM.class);
        RealmResults<UOM> uoms = where.equalTo("prodId", prodId).findAll();
        return uoms;
    }
}