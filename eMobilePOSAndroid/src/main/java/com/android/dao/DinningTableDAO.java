package com.android.dao;

import com.android.emobilepos.models.realms.DinningTable;
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
public class DinningTableDAO {
    public static void insert(String json) {
        Gson gson = JsonUtils.getInstance();
        Type listType = new com.google.gson.reflect.TypeToken<List<DinningTable>>() {
        }.getType();
        try {
            List<DinningTable> dinningTables = gson.fromJson(json, listType);
            for (DinningTable t : dinningTables) {
                if (t.getAdditionalInfoJson() != null && !t.getAdditionalInfoJson().isEmpty()) {
                    t.parseAdditionalInfo();
                }
            }
            DinningTableDAO.insert(dinningTables);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert(List<DinningTable> dinningTables) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(DinningTable.class);
            realm.copyToRealm(dinningTables);
        } finally {
            realm.commitTransaction();
            removeInvalidLocations();
        }
    }

    private static void removeInvalidLocations() {
        Realm r = Realm.getDefaultInstance();
        try {
            String defaultLocation = AssignEmployeeDAO.getAssignEmployee().getDefaultLocation();
            r.beginTransaction();
            r.where(DinningTable.class)
                    .notEqualTo("locationId", defaultLocation)
                    .findAll().deleteAllFromRealm();
        } finally {
            r.commitTransaction();
        }
    }

    public static RealmResults<DinningTable> getAll() {
        String defaultLocation = AssignEmployeeDAO.getAssignEmployee().getDefaultLocation();
        return Realm.getDefaultInstance().where(DinningTable.class)
                .equalTo("locationId", defaultLocation)
                .findAll();
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(DinningTable.class);
        } finally {
            realm.commitTransaction();
        }
    }

    public static DinningTable getById(String tableId) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<DinningTable> where = realm.where(DinningTable.class);
        DinningTable table = where.equalTo("id", tableId).findFirst();
        return table;
    }

    public static DinningTable getByNumber(String tableNumber) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<DinningTable> where = realm.where(DinningTable.class);
        DinningTable table = where.equalTo("number", tableNumber).findFirst();
        return table;
    }
}
