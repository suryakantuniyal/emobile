package com.android.dao;

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
public class SalesAssociateDAO {
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

        Type listType = new com.google.gson.reflect.TypeToken<List<SalesAssociate>>() {
        }.getType();
        try {
            List<SalesAssociate> salesAssociates = gson.fromJson(json, listType);

            SalesAssociateDAO.insert(salesAssociates);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert(List<SalesAssociate> salesAssociates) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.clear(SalesAssociate.class);
        realm.copyToRealm(salesAssociates);
        realm.commitTransaction();
    }

    public static RealmResults<SalesAssociate> getAll() {
        RealmResults<SalesAssociate> salesAssociates = Realm.getDefaultInstance().allObjects(SalesAssociate.class);
        return salesAssociates;
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.clear(SalesAssociate.class);
        realm.commitTransaction();
    }

    public static SalesAssociate getByEmpId(int empId) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<SalesAssociate> where = realm.where(SalesAssociate.class);
        SalesAssociate salesAssociate = where.equalTo("emp_id", empId).findFirst();
        return salesAssociate;
    }
}