package com.android.dao;

import com.android.emobilepos.models.realms.AssignEmployee;

import java.util.List;

import io.realm.Realm;

/**
 * Created by guarionex on 12/8/16.
 */

public class AssignEmployeeDAO {

    public static AssignEmployee getAssignEmployee() {
        Realm r = Realm.getDefaultInstance();
        return r.where(AssignEmployee.class).findFirst();
    }

    public static void insertAssignEmployee(List<AssignEmployee> assignEmployees) throws Exception {
        if (assignEmployees == null) {
            throw new Exception("Invalid Assign Employee realm object");
        }
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();
        r.copyToRealmOrUpdate(assignEmployees);
        r.commitTransaction();
    }

    public static void updateLastOrderId(String ord_id) {
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();
        AssignEmployee assignEmployee = getAssignEmployee();
        assignEmployee.setMSLastOrderID(ord_id);
        r.commitTransaction();
    }

    public static void updateLastTransferId(String transferId) {
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();
        AssignEmployee assignEmployee = getAssignEmployee();
        assignEmployee.setMSLastTransferID(transferId);
        r.commitTransaction();
    }
}
