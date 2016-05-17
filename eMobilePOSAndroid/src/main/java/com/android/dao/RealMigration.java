package com.android.dao;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmMigration;

/**
 * Created by Guarionex on 4/13/2016.
 */
public class RealMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        Realm.deleteRealm(realm.getConfiguration());
    }
}
