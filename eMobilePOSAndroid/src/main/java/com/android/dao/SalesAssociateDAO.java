package com.android.dao;

import com.android.emobilepos.models.SalesAssociate;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import util.JsonUtils;

/**
 * Created by Guarionex on 4/12/2016.
 */
public class SalesAssociateDAO {
    public static void insert(String json) {
        Gson gson = JsonUtils.getInstance();

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
        realm.delete(SalesAssociate.class);
        realm.copyToRealm(salesAssociates);
        realm.commitTransaction();
    }

    public static RealmResults<SalesAssociate> getAll() {
        return Realm.getDefaultInstance().where(SalesAssociate.class).findAll();
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(SalesAssociate.class);
        realm.commitTransaction();
    }

    public static SalesAssociate getByEmpId(int empId) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<SalesAssociate> where = realm.where(SalesAssociate.class);
        return where.equalTo("emp_id", empId).findFirst();
    }
}