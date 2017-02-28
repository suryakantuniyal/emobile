package com.android.dao;

import com.android.emobilepos.models.realms.Clerk;
import com.android.emobilepos.models.realms.DinningTable;
import com.android.support.MyPreferences;
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
public class ClerkDAO {
    public static void insert(String json) {
        Gson gson = JsonUtils.getInstance();
        Type listType = new com.google.gson.reflect.TypeToken<List<Clerk>>() {
        }.getType();
        try {
            List<Clerk> clerks = gson.fromJson(json, listType);
            ClerkDAO.insert(clerks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert(List<Clerk> clerks) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(Clerk.class);
            realm.copyToRealm(clerks);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static RealmResults<Clerk> getAll() {
        RealmResults<Clerk> all;
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            all = realm.where(Clerk.class).findAll();
        } finally {
            realm.commitTransaction();
            realm.close();
        }
        return all;
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(Clerk.class);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static Clerk getByEmpId(int empId) {
        Realm realm = Realm.getDefaultInstance();
        Clerk clerk;
        try {
            RealmQuery<Clerk> where = realm.where(Clerk.class);
            clerk = where.equalTo("empId", empId).findFirst();
            if(clerk!=null)
                clerk=realm.copyFromRealm(clerk);
        } finally {
            realm.close();
        }
        return clerk;
    }

    public static void removeAssignedTable(Clerk selectedClerk, DinningTable table) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            selectedClerk.getAssignedDinningTables().remove(table);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static void addAssignedTable(Clerk selectedClerk, DinningTable table) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            getByEmpId(selectedClerk.getEmpId()).getAssignedDinningTables().add(table);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static void clearAllAssignedTable(Clerk associate) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            getByEmpId(associate.getEmpId()).getAssignedDinningTables().clear();
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static HashMap<String, List<Clerk>> getSalesAssociatesByLocation() {
        Realm realm = Realm.getDefaultInstance();
        String defaultLocation = AssignEmployeeDAO.getAssignEmployee().getDefaultLocation();
        RealmResults<DinningTable> tables = realm.where(DinningTable.class)
                .equalTo("locationId", defaultLocation)
                .findAll();
        Set<String> locations = new HashSet<>();
        HashMap<String, List<Clerk>> locationHashMap = new HashMap<>();
        for (DinningTable table : tables) {
            if (!locations.contains(table.getLocationId())) {
                locations.add(table.getLocationId());
            }
        }
        for (String locId : locations) {
            List<Clerk> associates = new ArrayList<>();
            for (Clerk associate : ClerkDAO.getAll()) {
                RealmResults<DinningTable> tbls = associate.getAssignedDinningTables()
                        .where().equalTo("locationId", locId).findAll();
                RealmList<DinningTable> list = new RealmList<>();
                list.addAll(tbls.subList(0, tbls.size()));
                Clerk associateNoRelm = realm.copyFromRealm(associate);
                associateNoRelm.setAssignedDinningTables(list);
                associates.add(associateNoRelm);
            }
            locationHashMap.put(locId, associates);

        }
        realm.close();
        return locationHashMap;
    }

    public static Clerk login(String password, MyPreferences preferences) {
        Clerk clerk = null;
        Realm r = Realm.getDefaultInstance();
        try {

            Clerk associate = r.where(Clerk.class)
                    .equalTo("empPwd", password)
                    .equalTo("isactive", 1)
                    .findFirst();

            if (associate != null) {
                preferences.setClerkID(String.valueOf(associate.getEmpId()));
                clerk = r.copyFromRealm(associate);
            }
        } finally {
            r.close();
        }
        return clerk;
    }
}
