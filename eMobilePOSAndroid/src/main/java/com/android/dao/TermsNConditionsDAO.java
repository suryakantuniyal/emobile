package com.android.dao;

import com.android.emobilepos.models.realms.TermsNConditions;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import util.json.JsonUtils;

/**
 * Created by guarionex on 5/22/17.
 */

public class TermsNConditionsDAO {
    public static void insert(String json) {
        Gson gson = JsonUtils.getInstance();
        Type listType = new com.google.gson.reflect.TypeToken<List<TermsNConditions>>() {
        }.getType();
        try {
            List<TermsNConditions> terms = gson.fromJson(json, listType);
            Realm realm = Realm.getDefaultInstance();
            try {
                realm.beginTransaction();
                realm.delete(TermsNConditions.class);
                realm.copyToRealm(terms);
            } finally {
                realm.commitTransaction();
                if(realm!=null) {
                    realm.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<TermsNConditions> getTermsNConds() {
        Realm realm = Realm.getDefaultInstance();
        try {
            List<TermsNConditions> all = realm.where(TermsNConditions.class).findAll();
            if (all != null) {
                all = realm.copyFromRealm(all);
                return all;
            }
            return new ArrayList<>();
        }finally {
            if(realm!=null) {
                realm.close();
            }
        }
    }
}
