package com.android.dao;

import com.android.emobilepos.firebase.NotificationSettings;

import io.realm.Realm;

/**
 * Created by guarionex on 12/6/16.
 */

public class FirebaseDAO {
    public static NotificationSettings getFirebaseSettings() {
        Realm r = Realm.getDefaultInstance();
        return r.where(NotificationSettings.class).findFirst();
    }

    public static void saveFirebaseSettings(NotificationSettings settings) {
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();
        r.copyToRealmOrUpdate(settings);
        r.commitTransaction();
    }
}
