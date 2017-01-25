package com.android.dao;

import com.android.emobilepos.models.AssignEmployee;

import io.realm.Realm;

/**
 * Created by guarionex on 12/29/16.
 */

public class AssignEmployeeDAO {
    public static void insert(AssignEmployee assignEmployee) {
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();
        r.insertOrUpdate(assignEmployee);
        r.commitTransaction();
    }

    public static AssignEmployee getAssignEmployee() {
        Realm r = Realm.getDefaultInstance();
        return r.where(AssignEmployee.class).findFirst();
    }
}
