package com.android.dao;

import com.android.emobilepos.models.realms.Clerk;

import java.util.List;

import io.realm.Realm;

/**
 * Created by guarionex on 02-12-17.
 */

public class ClerkDAO {
    public static void inserOrUpdate(List<Clerk> clerks) {
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            r.insertOrUpdate(clerks);
        } finally {
            r.commitTransaction();
        }
    }

    public static void truncate() {
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            r.where(Clerk.class).findAll().deleteAllFromRealm();
        } finally {
            r.commitTransaction();
        }
    }

    public static Clerk login(int clerkID, String password) {
        Realm r = Realm.getDefaultInstance();
        Clerk clerk = r.where(Clerk.class)
                .equalTo("empId", clerkID)
                .equalTo("empPwd", password)
                .findFirst();
        if (clerk != null)
            return r.copyFromRealm(clerk);
        else
            return null;
    }
}
