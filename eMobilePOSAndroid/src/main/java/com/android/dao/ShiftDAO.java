package com.android.dao;

import com.android.emobilepos.models.realms.Shift;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by guarionex on 2/10/17.
 */

public class ShiftDAO {
    public static Shift getCurrentShift(String clerkId) {
        Realm r = Realm.getDefaultInstance();
        Shift shift = r.where(Shift.class)
                .equalTo("assignee_id", clerkId)
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

    public static void insertOrUpdate(Shift shift) {
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            r.insertOrUpdate(shift);
        } finally {
            r.commitTransaction();
        }
    }

    public static List<Shift> getPendingSyncShifts() {
        Realm r = Realm.getDefaultInstance();
        return r.copyFromRealm(r.where(Shift.class).equalTo("sync", false).findAll());
    }
}
