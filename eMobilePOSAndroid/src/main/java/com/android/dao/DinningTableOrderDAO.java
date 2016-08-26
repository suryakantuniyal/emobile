package com.android.dao;

import com.android.emobilepos.models.DinningTableOrder;

import io.realm.Realm;
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
        return Realm.getDefaultInstance().where(DinningTableOrder.class).findAll();
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
        results.deleteAllFromRealm();
        realm.commitTransaction();
    }

    public static DinningTableOrder getByNumber(String number) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<DinningTableOrder> where = realm.where(DinningTableOrder.class);
        return where.equalTo("dinningTable.number", number).findFirst();
    }
}
