package com.android.dao;

import com.android.emobilepos.models.realms.CustomerCustomField;

import java.util.List;

import io.realm.Realm;

/**
 * Created by guarionex on 7/10/17.
 */

public class CustomerCustomFieldsDAO {
    public static void emptyTable() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.where(CustomerCustomField.class).findAll().deleteAllFromRealm();
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    public static void insert(List<CustomerCustomField> customFields) {
        Realm realm = Realm.getDefaultInstance();
        try {
            for (CustomerCustomField customField : customFields) {
                customField.generatePrimaryKey();
            }
            realm.beginTransaction();
            realm.insert(customFields);
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    public static CustomerCustomField findEMWSCardIdByCustomerId(String custID) {
        Realm realm = Realm.getDefaultInstance();
        CustomerCustomField customField;
        try {
            customField = realm.where(CustomerCustomField.class)
                    .equalTo("id", String.format("%sEMS_CARD_ID_NUM", custID))
                    .findFirst();
            if (customField != null) {
                customField = realm.copyFromRealm(customField);
            }
        } finally {
            realm.close();
        }
        return customField;
    }

    public static void upsert(CustomerCustomField customField) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            customField.generatePrimaryKey();
            realm.insertOrUpdate(customField);
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }
}
