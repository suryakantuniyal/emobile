package com.android.dao;

import com.android.emobilepos.models.realms.BiometricFid;
import com.android.emobilepos.models.realms.Bixolon;
import com.android.emobilepos.models.realms.BixolonPaymentMethod;
import com.android.emobilepos.models.realms.BixolonTax;
import com.android.emobilepos.models.realms.BixolonTransaction;
import com.android.emobilepos.models.realms.CustomerCustomField;
import com.android.emobilepos.models.realms.EmobileBiometric;
import com.android.emobilepos.models.realms.PaymentMethod;
import com.crashlytics.android.Crashlytics;

import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by Guarionex on 4/13/2016.
 */
public class EmobilePOSRealmMigration implements io.realm.RealmMigration {
    public static int REALM_SCHEMA_VERSION = 9;

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        try {
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
                    oldVersion = 4;
                }

                if (oldVersion == 4) {
                    schema.remove(CustomerCustomField.class.getSimpleName());
                    schema.create(CustomerCustomField.class.getSimpleName()).
                            addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("custId", String.class, FieldAttribute.INDEXED)
                            .addField("custFieldId", String.class, FieldAttribute.INDEXED)
                            .addField("custFieldName", String.class)
                            .addField("custValue", String.class);
                    oldVersion++;
                }
                if (oldVersion == 5) {
                    schema.create(BiometricFid.class.getSimpleName()).
                            addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("fid", String.class)
                            .addField("fingerCode", int.class, FieldAttribute.INDEXED);

                    schema.create(EmobileBiometric.class.getSimpleName()).
                            addField("customerId", String.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmListField("fids", schema.get(BiometricFid.class.getSimpleName()));
                    oldVersion++;
                }
                if (oldVersion == 6) {
                    if (schema.contains(Bixolon.class.getSimpleName())) {
                        schema.remove(Bixolon.class.getSimpleName());
                    }
                    if (schema.contains(BixolonTransaction.class.getSimpleName())) {
                        schema.remove(BixolonTransaction.class.getSimpleName());
                    }
                    if (schema.contains(BixolonTax.class.getSimpleName())) {
                        schema.remove(BixolonTax.class.getSimpleName());
                    }
                    if (schema.contains(BixolonPaymentMethod.class.getSimpleName())) {
                        schema.remove(BixolonPaymentMethod.class.getSimpleName());
                    }

                    schema.create(BixolonTransaction.class.getSimpleName()).
                            addField("orderId", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("bixolonTransactionId", String.class, FieldAttribute.INDEXED)
                            .addField("transactionDate", Date.class);


                    schema.create(BixolonTax.class.getSimpleName()).
                            addField("taxId", String.class)
                            .addField("taxCode", String.class)
                            .addField("bixolonChar", String.class, FieldAttribute.PRIMARY_KEY);

                    schema.create(BixolonPaymentMethod.class.getSimpleName()).
                            addField("id", int.class, FieldAttribute.PRIMARY_KEY)
                            .addRealmObjectField("paymentMethod", schema.get(PaymentMethod.class.getSimpleName()));

                    schema.create(Bixolon.class.getSimpleName()).
                            addField("pkid", int.class, FieldAttribute.PRIMARY_KEY)
                            .addField("ruc", String.class)
                            .addField("ncf", String.class)
                            .addRealmListField("bixolontaxes", schema.get(BixolonTax.class.getSimpleName()))
                            .addRealmListField("paymentMethods", schema.get(BixolonPaymentMethod.class.getSimpleName()))
                            .addField("merchantName", String.class);
                    oldVersion++;
                }
                if (oldVersion == 7) {
                    if (schema.contains(EmobileBiometric.class.getSimpleName())) {
                        schema.remove(EmobileBiometric.class.getSimpleName());
                    }
                    if (schema.contains(BiometricFid.class.getSimpleName())) {
                        schema.remove(BiometricFid.class.getSimpleName());
                    }
                    schema.create(BiometricFid.class.getSimpleName()).
                            addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("fid", String.class)
                            .addField("fmdData", byte[].class)
                            .addField("fmdBase64", String.class)
                            .addField("fingerCode", int.class, FieldAttribute.INDEXED);


                    schema.create(EmobileBiometric.class.getSimpleName()).
                            addField("realmId", String.class, FieldAttribute.PRIMARY_KEY)
                            .addField("entityid", String.class, FieldAttribute.INDEXED)
                            .addField("userTypeCode", int.class, FieldAttribute.INDEXED)
                            .addRealmListField("fids", schema.get(BiometricFid.class.getSimpleName()))
                            .addField("regid", String.class, FieldAttribute.INDEXED);
                    oldVersion++;
                }
                if (oldVersion == 8) {
                    if (!schema.get(BiometricFid.class.getSimpleName()).hasField("fmdBase64")) {
                        schema.get(BiometricFid.class.getSimpleName())
                                .addField("fmdBase64", String.class);
                    }
                    oldVersion++;
                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }
}
