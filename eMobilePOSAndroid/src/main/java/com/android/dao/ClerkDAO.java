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
            if(realm!=null) {
                realm.close();
            }
        }
    }

    public static void insertOrUpdate(Clerk clerk) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.insertOrUpdate(clerk);
        } finally {
            realm.commitTransaction();
            if(realm!=null) {
                realm.close();
            }
        }
    }

    public static List<Clerk> getAll() {
        List<Clerk> all;
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            all = realm.where(Clerk.class).findAll();
            if (all != null) {
                all = realm.copyFromRealm(all);
            }
        } finally {
            realm.commitTransaction();
            if(realm!=null) {
                realm.close();
            }
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
            if(realm!=null) {
                realm.close();
            }
        }
    }

    public static Clerk getByEmpId(int empId) {
        Realm realm = Realm.getDefaultInstance();
        Clerk clerk;
        try {
            RealmQuery<Clerk> where = realm.where(Clerk.class);
            clerk = where.equalTo("empId", empId).findFirst();
            if (clerk != null) {
                clerk = realm.copyFromRealm(clerk);
            }
        } finally {
            if(realm!=null) {
                realm.close();
            }
        }
        return clerk;
    }

    public static void removeAssignedTable(Clerk selectedClerk, DinningTable table) {
        Realm realm = Realm.getDefaultInstance();
        try {
             selectedClerk.getAssignedDinningTables().remove(table);
        }
        finally {
            if(realm!=null) {
                realm.close();
            }
        }

        insertOrUpdate(selectedClerk);
    }

    public static void addAssignedTable(Clerk selectedClerk, DinningTable table) {
        Realm realm = Realm.getDefaultInstance();
        try {
            Clerk clerk = getByEmpId(selectedClerk.getEmpId());
            clerk.getAssignedDinningTables().remove(table);
            clerk.getAssignedDinningTables().add(table);
            insertOrUpdate(clerk);
            selectedClerk = clerk;
        } finally {
            if(realm!=null) {
                realm.close();
            }
        }
    }

    public static void clearAllAssignedTable(Clerk associate) {
        Realm realm = Realm.getDefaultInstance();
        try {
            Clerk clerk = getByEmpId(associate.getEmpId());
            if (clerk != null) {
                clerk.getAssignedDinningTables().clear();
                insertOrUpdate(clerk);
            }
        } finally {
            if(realm!=null) {
                realm.close();
            }
        }
    }

    public static HashMap<String, List<Clerk>> getSalesAssociatesByLocation() {
        String defaultLocation = AssignEmployeeDAO.getAssignEmployee().getDefaultLocation();
        Realm realm = Realm.getDefaultInstance();
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
            RealmResults<Clerk> all = realm.where(Clerk.class).findAll();
            for (Clerk associate : all) {
                RealmResults<DinningTable> tbls = associate.getAssignedDinningTables()
                        .where().equalTo("locationId", locId).findAll();
                RealmList<DinningTable> list = new RealmList<>();
                list.addAll(realm.copyFromRealm(tbls));
                Clerk associateNoRelm = realm.copyFromRealm(associate);
                associateNoRelm.setAssignedDinningTables(list);
                associates.add(associateNoRelm);
            }
            locationHashMap.put(locId, associates);

        }
        if(realm!=null) {
            realm.close();
        }
        return locationHashMap;
    }

    public static Clerk login(String password, MyPreferences preferences, boolean isSystemLogin) {
        Clerk clerk = null;
        Realm r = Realm.getDefaultInstance();
        try {

            Clerk associate = r.where(Clerk.class)
                    .equalTo("empPwd", password)
                    .equalTo("isactive", 1)
                    .findFirst();

            if (associate != null) {
                if (isSystemLogin) {
                    preferences.setClerkID(String.valueOf(associate.getEmpId()));
                    preferences.setClerkName(associate.getEmpName());
                }
                clerk = r.copyFromRealm(associate);
            }
        } finally {
            if(r!=null) {
                r.close();
            }
        }
        return clerk;
    }

    public static boolean hasAssignedDinningTable(int clerkId, String tableNumber) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmQuery<Clerk> where = realm.where(Clerk.class);
            Clerk clerk = where.equalTo("empId", clerkId).findFirst();
            long count = clerk == null ? 0 : clerk.getAssignedDinningTables()
                    .where()
                    .equalTo("number", tableNumber).count();
            return count > 0;
        } finally {
            if(realm!=null) {
                realm.close();
            }
        }
    }
}
