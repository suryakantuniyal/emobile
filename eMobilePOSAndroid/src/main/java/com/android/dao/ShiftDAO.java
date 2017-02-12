package com.android.dao;

import com.android.emobilepos.models.realms.Shift;

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
                .equalTo("shiftStatusCode", 1)
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
}
