package com.android.dao;

import com.android.emobilepos.models.realms.DinningTable;
import com.android.emobilepos.models.realms.SalesAssociate;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import util.json.JsonUtils;

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
        try {
            realm.beginTransaction();
            realm.delete(SalesAssociate.class);
            realm.copyToRealm(salesAssociates);
        } finally {
            realm.commitTransaction();
        }
    }

    public static RealmResults<SalesAssociate> getAll() {
        return Realm.getDefaultInstance().where(SalesAssociate.class).findAll();
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(SalesAssociate.class);
        } finally {
            realm.commitTransaction();
        }
    }

    public static SalesAssociate getByEmpId(int empId) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<SalesAssociate> where = realm.where(SalesAssociate.class);
        return where.equalTo("emp_id", empId).findFirst();
    }

    public static void removeAssignedTable(SalesAssociate selectedSalesAssociate, DinningTable table) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            selectedSalesAssociate.getAssignedDinningTables().remove(table);
        } finally {
            realm.commitTransaction();
        }
    }

    public static void addAssignedTable(SalesAssociate selectedSalesAssociate, DinningTable table) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            getByEmpId(selectedSalesAssociate.getEmp_id()).getAssignedDinningTables().add(table);
        } finally {
            realm.commitTransaction();
        }
    }

    public static void clearAllAssignedTable(SalesAssociate associate) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            getByEmpId(associate.getEmp_id()).getAssignedDinningTables().deleteAllFromRealm();
        } finally {
            realm.commitTransaction();
        }
    }

    public static HashMap<String, List<SalesAssociate>> getSalesAssociatesByLocation() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<DinningTable> tables = realm.where(DinningTable.class).findAll();
        Set<String> locations = new HashSet<>();
        HashMap<String, List<SalesAssociate>> locationHashMap = new HashMap<>();
        for (DinningTable table : tables) {
            if (!locations.contains(table.getLocationId())) {
                locations.add(table.getLocationId());
            }
        }
        for (String locId : locations) {
            List<SalesAssociate> associates = new ArrayList<>();
            for (SalesAssociate associate : SalesAssociateDAO.getAll()) {
                RealmResults<DinningTable> tbls = associate.getAssignedDinningTables()
                        .where().equalTo("locationId", locId).findAll();
                RealmList<DinningTable> list = new RealmList<>();
                list.addAll(tbls.subList(0, tbls.size()));
                SalesAssociate associateNoRelm = realm.copyFromRealm(associate);
                associateNoRelm.setAssignedDinningTables(list);
                associates.add(associateNoRelm);
            }
            locationHashMap.put(locId, associates);

        }
        return locationHashMap;
    }
}
