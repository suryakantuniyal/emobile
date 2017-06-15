package com.android.dao;

import com.android.emobilepos.models.realms.EmployeePersmission;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by guarionex on 02-12-17.
 */

public class EmployeePermissionDAO {
    public static void insertOrUpdate(List<EmployeePersmission> persmissions) {
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            r.insertOrUpdate(persmissions);
        } finally {
            r.commitTransaction();
            r.close();
        }
    }


    public static void truncate() {
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            r.where(EmployeePersmission.class).findAll().deleteAllFromRealm();
        } finally {
            r.commitTransaction();
            r.close();
        }
    }

    public static List<EmployeePersmission> getEmployeePersmissions(int employeeId, int code) {
        Realm r = Realm.getDefaultInstance();
        try {
            RealmResults<EmployeePersmission> persmissions = r.where(EmployeePersmission.class)
                    .equalTo("empId", employeeId)
                    .equalTo("pId", code)
                    .findAll();
            if (persmissions != null) {
                return r.copyFromRealm(persmissions);
            } else {
                return null;
            }
        }finally {
            r.close();
        }
    }
}
