package com.android.dao;

import com.android.emobilepos.models.realms.Clerk;

import io.realm.DynamicRealm;
import io.realm.RealmSchema;

/**
 * Created by Guarionex on 4/13/2016.
 */
public class EmobilePOSRealmMigration implements io.realm.RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        if (oldVersion != newVersion) {
            RealmSchema schema = realm.getSchema();
            if (oldVersion == 2) {
                schema.get(Clerk.class.getSimpleName()).
                        addField("tempid", int.class);
                oldVersion++;
            }
        }
    }
}
