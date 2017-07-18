package com.android.dao;

import com.android.emobilepos.models.realms.CustomerCustomField;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmSchema;

/**
 * Created by Guarionex on 4/13/2016.
 */
public class EmobilePOSRealmMigration implements io.realm.RealmMigration {
    public static int REALM_SCHEMA_VERSION = 4;

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        if (oldVersion != newVersion) {
            RealmSchema schema = realm.getSchema();
            if (oldVersion < 4) {
                schema.create(CustomerCustomField.class.getSimpleName()).
                        addField("custId", String.class, FieldAttribute.INDEXED)
                        .addField("custFieldId", String.class, FieldAttribute.INDEXED)
                        .addField("custFieldName", String.class)
                        .addField("custValue", String.class);
                oldVersion++;
            }
        }
    }
}
