package com.android.dao;

import com.android.emobilepos.models.realms.Shift;
import com.android.support.DateUtils;
import com.android.support.Global;

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
    public static Shift getCurrentShift(int clerkId) {
        Realm r = Realm.getDefaultInstance();
        Shift shift = r.where(Shift.class)
                .equalTo("assigneeId", clerkId)
                .findAll().where()
                .equalTo("shiftStatusCode", 0)
                .or()
                .equalTo("shiftStatusCode", 1)
                .findFirst();
        if (shift != null) {
            return r.copyFromRealm(shift);
        }
        return null;
    }

    public static Shift getOpenShift(int clerkId) {
        Realm r = Realm.getDefaultInstance();
        Shift shift = r.where(Shift.class)
                .equalTo("assigneeId", clerkId)
                .equalTo("shiftStatusCode", 0)
                .findFirst();
        if (shift != null) {
            return r.copyFromRealm(shift);
        } else {
            return null;
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
        }
    }

    public static List<Shift> getPendingSyncShifts() {
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();
        r.commitTransaction();
        RealmResults<Shift> sync = r.where(Shift.class)
                .equalTo("sync", false)
                .findAll().where()
                .equalTo("shiftStatusCode", 2)
                .or()
                .equalTo("shiftStatusCode", 1)
                .findAll();
        return r.copyFromRealm(sync);
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
        }
    }

    public static void updateShiftToSync(List<Shift> shifts) {
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            r.insertOrUpdate(shifts);
        } finally {
            r.commitTransaction();
        }
    }

    public static boolean isShiftOpen(String clerkID) {
        Shift shift = getOpenShift(Integer.parseInt(clerkID));
        return shift != null;
    }

    public static void updateShiftAmounts(int clerkID, double amountToApply, boolean isReturn) {
        Shift openShift = getOpenShift(clerkID);
        if(openShift==null) {
            return;
        }

        if (isReturn) {
            openShift.setTotalTransactionsCash(String.valueOf(Global.getBigDecimalNum(openShift != null
                    ? openShift.getTotalTransactionsCash() :
                    "0").subtract(BigDecimal.valueOf(amountToApply))));
        } else {
            openShift.setTotalTransactionsCash(String.valueOf(Global.getBigDecimalNum(openShift != null
                    ? openShift.getTotalTransactionsCash()
                    : "0").add(BigDecimal.valueOf(amountToApply))));
        }
        insertOrUpdate(openShift);
    }

    public static Shift getShift(String shiftId) {
        Realm r = Realm.getDefaultInstance();
        return r.copyFromRealm(r.where(Shift.class).equalTo("shiftId", shiftId).findFirst());
    }

    public static List<Shift> getShift(Date date) {
        List<Shift> list = new ArrayList<>();
        Realm r = Realm.getDefaultInstance();
        RealmResults<Shift> shifts = r.where(Shift.class).findAll();
        for (Shift shift : shifts) {
            String creationDate = DateUtils.getDateAsString(shift.getCreationDate(), DateUtils.DATE_yyyy_MM_dd);
            String filterDate = DateUtils.getDateAsString(date, DateUtils.DATE_yyyy_MM_dd);
            if (creationDate.equalsIgnoreCase(filterDate)) {
                list.add(shift);
            }
        }
        return r.copyFromRealm(list);
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
