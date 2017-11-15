package com.android.dao;

import com.android.emobilepos.models.realms.SyncServerConfiguration;

import io.realm.Realm;

/**
 * Created by Guarionex on 4/12/2016.
 */
public class SyncServerConfigurationDAO {
    public static void insert(SyncServerConfiguration configuration) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.insertOrUpdate(configuration);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static SyncServerConfiguration getSyncServerConfiguration() {
        Realm realm = Realm.getDefaultInstance();
        SyncServerConfiguration configuration;
        try {
            configuration = realm.where(SyncServerConfiguration.class).findFirst();
            if (configuration != null) {
                configuration = realm.copyFromRealm(configuration);
            }
        } finally {
            realm.close();
        }
        return configuration;
    }
}
