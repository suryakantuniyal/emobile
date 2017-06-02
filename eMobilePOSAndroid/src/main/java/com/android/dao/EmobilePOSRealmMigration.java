package com.android.dao;

import io.realm.DynamicRealm;
import io.realm.Realm;

/**
 * Created by Guarionex on 4/13/2016.
 */
public class EmobilePOSRealmMigration implements io.realm.RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        if (oldVersion != newVersion) {
//           realm.deleteAll();
        }
    }
}
