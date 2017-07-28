package com.android.dao;

import com.android.emobilepos.models.realms.CustomerCustomField;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by Guarionex on 4/13/2016.
 */
public class EmobilePOSRealmMigration implements io.realm.RealmMigration {
    public static int REALM_SCHEMA_VERSION = 5;

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        if (oldVersion != newVersion) {
            RealmSchema schema = realm.getSchema();
            if (oldVersion < 4) {
                if (schema.contains(CustomerCustomField.class.getSimpleName())) {
                    RealmObjectSchema custCustField = schema.get(CustomerCustomField.class.getSimpleName());
                    if (custCustField.hasPrimaryKey()) {
                        custCustField.removePrimaryKey();
                    }
                    if (custCustField.hasIndex("custId")) {
                        custCustField.removeIndex("custId");
                    }
                    if (custCustField.hasIndex("custFieldId")) {
                        custCustField.removeIndex("custFieldId");
                    }

                    schema.get(CustomerCustomField.class.getSimpleName()).
                            addIndex("custId")
                            .addIndex("custFieldId");
                } else {
                    schema.create(CustomerCustomField.class.getSimpleName()).
                            addField("custId", String.class, FieldAttribute.INDEXED)
                            .addField("custFieldId", String.class, FieldAttribute.INDEXED)
                            .addField("custFieldName", String.class)
                            .addField("custValue", String.class);
                }
                oldVersion++;
            }
            if (oldVersion == 4) {
                schema.remove(CustomerCustomField.class.getSimpleName());
                schema.create(CustomerCustomField.class.getSimpleName()).
                        addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("custId", String.class, FieldAttribute.INDEXED)
                        .addField("custFieldId", String.class, FieldAttribute.INDEXED)
                        .addField("custFieldName", String.class)
                        .addField("custValue", String.class);
            }
        }
    }
}
