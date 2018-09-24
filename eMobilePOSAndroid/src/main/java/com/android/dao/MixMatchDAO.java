package com.android.dao;

import com.android.emobilepos.models.MixMatchProductGroup;
import com.android.emobilepos.models.realms.MixMatch;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import util.json.JsonUtils;

/**
 * Created by Guarionex on 4/12/2016.
 */
public class MixMatchDAO {
    public static void insert(String json) {
        Gson gson = JsonUtils.getInstance();

        Type listType = new com.google.gson.reflect.TypeToken<List<MixMatch>>() {
        }.getType();
        try {
            List<MixMatch> mixMatches = gson.fromJson(json, listType);
            MixMatchDAO.insert(mixMatches);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert(List<MixMatch> mixMatches) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(MixMatch.class);
            realm.copyToRealm(mixMatches);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static List<MixMatch> getAll() {
        Realm realm = Realm.getDefaultInstance();
        try {
            List<MixMatch> all = realm.where(MixMatch.class).findAll();
            if (all != null) {
                all = realm.copyFromRealm(all);
            }
            return all;
        } finally {
            realm.close();
        }
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(MixMatch.class);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static RealmResults<MixMatch> getDiscountsBygroupId(MixMatchProductGroup group) {
        Date now = new Date();
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<MixMatch> where = realm.where(MixMatch.class);
        RealmResults<MixMatch> realmResults = where
                .equalTo("groupId", group.getGroupId())
                .equalTo("priceLevelId", group.getPriceLevelId())
                .or()
                .equalTo("groupId", group.getGroupId())
                .equalTo("priceLevelId", "")
                .equalTo("mixMatchType", 2)
                .equalTo("xyzSequence", 1)
                .findAll()
                .where()
                .equalTo("isActive", true)
                .lessThanOrEqualTo("startDate", now)
                .greaterThanOrEqualTo("endDate", now)
                .findAll().where()
                .equalTo("mixMatchType", 1)
                .lessThanOrEqualTo("qty", group.getQuantity())
                .or()
                .equalTo("mixMatchType", 2)
                .findAll();
        realm.close();
        return realmResults;
    }
}
