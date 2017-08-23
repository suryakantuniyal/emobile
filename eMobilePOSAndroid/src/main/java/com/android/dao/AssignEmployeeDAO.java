package com.android.dao;

import com.android.emobilepos.models.realms.AssignEmployee;

import java.util.List;

import io.realm.Realm;

/**
 * Created by guarionex on 12/8/16.
 */

public class AssignEmployeeDAO {

//    public static AssignEmployee getAssignEmployee(boolean returnManaged) {
//        Realm r = Realm.getDefaultInstance();
//        AssignEmployee employee;
//        try {
//            r.beginTransaction();
//            employee = r.where(AssignEmployee.class).findFirst();
//        } finally {
//            r.commitTransaction();
//        }
//        if (returnManaged || employee == null) {
//            return employee;
//        } else {
//            return r.copyFromRealm(employee);
//        }
//    }


    public static AssignEmployee getAssignEmployee(boolean returnManaged) {
        Realm r = Realm.getDefaultInstance();
        AssignEmployee employee;
        try {
//            r.beginTransaction();
            employee = r.where(AssignEmployee.class).findFirst();

            if (!returnManaged && employee != null) {
                employee = r.copyFromRealm(employee);
            }
        } finally {
//            r.commitTransaction();
            r.close();
        }
        return employee;
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
        Realm r = Realm.getDefaultInstance();
        try {
            AssignEmployee assignEmployee = getAssignEmployee(false);
            r.beginTransaction();
            assignEmployee.setMSLastOrderID(ord_id);
            r.insertOrUpdate(assignEmployee);
        } finally {
            r.commitTransaction();
            r.close();
        }
    }

    public static void updateLastTransferId(String transferId) {
        Realm r = Realm.getDefaultInstance();
        try {
            AssignEmployee assignEmployee = getAssignEmployee(false);
            r.beginTransaction();
            assignEmployee.setMSLastTransferID(transferId);
            r.insertOrUpdate(assignEmployee);
        } finally {
            r.commitTransaction();
            r.close();
        }
    }
}
