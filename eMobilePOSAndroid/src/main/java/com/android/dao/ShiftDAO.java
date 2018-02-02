package com.android.dao;

import com.android.emobilepos.models.realms.Shift;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.NumberUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by guarionex on 2/10/17.
 */

public class ShiftDAO {

    public static Shift getShiftByEmployeeId(int employeeId) {
        Realm r = Realm.getDefaultInstance();
        try {
            Shift shift = r.where(Shift.class)
                    .equalTo("assigneeId", employeeId)
                    .findAll().where()
                    .equalTo("shiftStatusCode", Shift.ShiftStatus.OPEN.code)
                    .or()
                    .equalTo("shiftStatusCode", Shift.ShiftStatus.PENDING.code)
                    .findFirst();
            if (shift != null) {
                return r.copyFromRealm(shift);
            }
            return null;
        } finally {
            r.close();
        }
    }

    public static Shift getShiftByClerkId(int clerkId) {
        Realm r = Realm.getDefaultInstance();
        try {
            Shift shift = r.where(Shift.class)
                    .equalTo("clerkId", clerkId)
                    .findAll().where()
                    .equalTo("shiftStatusCode", Shift.ShiftStatus.OPEN.code)
                    .or()
                    .equalTo("shiftStatusCode", Shift.ShiftStatus.PENDING.code)
                    .findFirst();
            if (shift != null) {
                return r.copyFromRealm(shift);
            }
            return null;
        } finally {
            r.close();
        }
    }

    public static Shift getOpenShift() {
        int empId = AssignEmployeeDAO.getAssignEmployee(false).getEmpId();
        Realm r = Realm.getDefaultInstance();
        try {
            Shift shift = r.where(Shift.class)
                    .equalTo("assigneeId", empId)
                    .equalTo("shiftStatusCode", Shift.ShiftStatus.OPEN.code)
                    .findFirst();
            if (shift != null) {
                return r.copyFromRealm(shift);
            } else {
                return null;
            }
        } finally {
            r.close();
        }
    }


    public static void insertOrUpdate(Shift shift) {
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            shift.setSync(false);
            r.insertOrUpdate(shift);
        } finally {
            r.commitTransaction();
            r.close();
        }
    }

    public static List<Shift> getPendingSyncShifts() {
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            r.commitTransaction();
            RealmResults<Shift> sync = r.where(Shift.class)
                    .equalTo("sync", false)
                    .findAll().where()
                    .equalTo("shiftStatusCode", Shift.ShiftStatus.CLOSED.code)
                    .or()
                    .equalTo("shiftStatusCode", Shift.ShiftStatus.PENDING.code)
                    .findAll();
            return r.copyFromRealm(sync);
        } finally {
            r.close();
        }
    }

    public static void insertOrUpdate(List<Shift> shifts) {
        Realm r = Realm.getDefaultInstance();
        try {
            for (Shift s : shifts) {
                s.setSync(false);
            }
            r.beginTransaction();
            r.insertOrUpdate(shifts);
        } finally {
            r.commitTransaction();
            r.close();
        }
    }

    public static void updateShiftToSync(List<Shift> shifts) {
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            r.insertOrUpdate(shifts);
        } finally {
            r.commitTransaction();
            r.close();
        }
    }

    public static boolean isShiftOpen() {
        Shift shift = getOpenShift();
        return shift != null;
    }

    public static void updateShiftAmounts(double amountToApply, boolean isReturn) {
        Shift openShift = getOpenShift();
        if (openShift == null) {
            return;
        }

        if (isReturn) {
            openShift.setTotalTransactionsCash(String.valueOf(Global.getBigDecimalNum(openShift.getTotalTransactionsCash())
                    .subtract(BigDecimal.valueOf(amountToApply))));
        } else {
            openShift.setTotalTransactionsCash(String.valueOf(Global.getBigDecimalNum(openShift.getTotalTransactionsCash())
                    .add(BigDecimal.valueOf(amountToApply))));
        }
        BigDecimal beginningPettyCash = new BigDecimal(openShift.getBeginningPettyCash());
        BigDecimal transactionsCash = new BigDecimal(openShift.getTotalTransactionsCash());
        BigDecimal totalExpenses = ShiftExpensesDAO.getShiftTotalExpenses(openShift.getShiftId());
        BigDecimal totalEndingCash = Global.getRoundBigDecimal(beginningPettyCash.add(transactionsCash).add(totalExpenses), 2);
        openShift.setTotal_ending_cash(String.valueOf(totalEndingCash));
        insertOrUpdate(openShift);
    }

    public static Shift getShift(String shiftId) {
        Realm r = Realm.getDefaultInstance();
        try {
            return r.copyFromRealm(r.where(Shift.class).equalTo("shiftId", shiftId).findFirst());
        } finally {
            r.close();
        }
    }

    public static List<Shift> getShift(Date date) {
        List<Shift> list = new ArrayList<>();
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            r.commitTransaction();
            RealmResults<Shift> shifts = r.where(Shift.class).findAll();
            for (Shift shift : shifts) {
                String creationDate = DateUtils.getDateAsString(shift.getCreationDate(), DateUtils.DATE_yyyy_MM_dd);
                String filterDate = DateUtils.getDateAsString(date, DateUtils.DATE_yyyy_MM_dd);
                if (creationDate.equalsIgnoreCase(filterDate)) {
                    list.add(shift);
                }
            }
            return r.copyFromRealm(list);
        } finally {
            r.close();
        }
    }

    public static void insertOrUpdatePendingShift(List<Shift> shifts, int clerkId) {
        Realm r = Realm.getDefaultInstance();
        try {
            for (Shift s : shifts) {
                s.setSync(false);
            }
            r.beginTransaction();
            RealmResults<Shift> all = r.where(Shift.class)
                    .equalTo("clerkId", clerkId)
                    .equalTo("shiftStatusCode", Shift.ShiftStatus.PENDING.code)
                    .findAll();
            if (all != null && all.isValid()) {
                all.deleteAllFromRealm();
            }
            r.insertOrUpdate(shifts);
        } finally {
            r.commitTransaction();
            r.close();
        }
    }
//
//    public static List<Shift> getShift(String clerkId, Date date) {
//        List<Shift> list = new ArrayList<>();
//        Realm r = Realm.getDefaultInstance();
//        RealmResults<Shift> shifts = r.where(Shift.class)
//                .equalTo("assigneeId", clerkId)
//                .findAll();
//        for (Shift shift : shifts) {
//            String creationDate = DateUtils.getDateAsString(shift.getCreationDate(), DateUtils.DATE_yyyy_MM_dd);
//            String filterDate = DateUtils.getDateAsString(date, DateUtils.DATE_yyyy_MM_dd);
//            if (creationDate.equalsIgnoreCase(filterDate)) {
//                list.add(shift);
//            }
//        }
//        return list;
//    }
}
