package com.android.dao;

import com.android.emobilepos.models.realms.AssignEmployee;

import java.util.List;

import io.realm.Realm;

/**
 * Created by guarionex on 12/8/16.
 */

public class AssignEmployeeDAO {
    private static AssignEmployee assignEmployee;

    public static AssignEmployee getAssignEmployee() {
        if (assignEmployee == null) {
            Realm r = Realm.getDefaultInstance();
            try {
                assignEmployee = r.where(AssignEmployee.class).findFirst();
                if (assignEmployee != null) {
                    assignEmployee = r.copyFromRealm(assignEmployee);
                }
            } finally {
                r.close();
            }
        }
        return assignEmployee;
    }


    public static void insertAssignEmployee(List<AssignEmployee> assignEmployees) throws Exception {
        if (assignEmployees == null) {
            throw new Exception("Invalid Assign Employee realm object");
        }
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            r.where(AssignEmployee.class).findAll().deleteAllFromRealm();
            r.copyToRealmOrUpdate(assignEmployees);
        } finally {
            r.commitTransaction();
            r.close();
        }
    }

    public static void updateLastOrderId(String ord_id) {
        assignEmployee = getAssignEmployee();
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            assignEmployee.setMSLastOrderID(ord_id);
            r.insertOrUpdate(assignEmployee);
        } finally {
            r.commitTransaction();
            r.close();
        }
    }

    public static void updateLastTransferId(String transferId) {
        assignEmployee = getAssignEmployee();
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            assignEmployee.setMSLastTransferID(transferId);
            r.insertOrUpdate(assignEmployee);
        } finally {
            r.commitTransaction();
            r.close();
        }
    }
}
