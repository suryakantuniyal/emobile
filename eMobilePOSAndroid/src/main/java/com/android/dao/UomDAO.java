package com.android.dao;

import com.android.emobilepos.models.realms.UOM;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
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
        try {
            realm.beginTransaction();
            realm.delete(UOM.class);
            realm.copyToRealm(uoms);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static List<UOM> getAll() {
        Realm realm = Realm.getDefaultInstance();
        try {
            List<UOM> uoms = realm.where(UOM.class).findAll();
            if (uoms != null) {
                uoms = realm.copyFromRealm(uoms);
            }
            return uoms;
        } finally {
            realm.close();
        }
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(UOM.class);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static List<UOM> getByProdId(String prodId) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmQuery<UOM> where = realm.where(UOM.class);
            List<UOM> uoms = where.equalTo("prodId", prodId).findAll();
            if (uoms != null) {
                uoms = realm.copyFromRealm(uoms);
            }
            return uoms;
        } finally {
            realm.close();
        }
    }
}