package com.android.dao;

import com.android.emobilepos.models.DinningTableOrder;
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
public class DinningTableOrderDAO {

    public static void insert(DinningTableOrder dinningTableOrder) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(dinningTableOrder);
        realm.commitTransaction();
    }

    public static RealmResults<DinningTableOrder> getAll() {
        RealmResults<DinningTableOrder> dinningTableOrders = Realm.getDefaultInstance().where(DinningTableOrder.class).findAll();
        return dinningTableOrders;
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(DinningTableOrder.class);
        realm.commitTransaction();
    }

    public static void deleteByNumber(String number) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<DinningTableOrder> where = realm.where(DinningTableOrder.class);
        RealmResults<DinningTableOrder> results = where.equalTo("dinningTable.number", number).findAll();
        realm.beginTransaction();
        results.clear();
        realm.commitTransaction();
    }

    public static DinningTableOrder getByNumber(String number) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<DinningTableOrder> where = realm.where(DinningTableOrder.class);
        DinningTableOrder table = where.equalTo("dinningTable.number", number).findFirst();
        return table;
    }
}
