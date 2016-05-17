package com.android.dao;

import com.android.emobilepos.models.DinningTable;
import com.android.emobilepos.models.MixMatch;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Guarionex on 4/12/2016.
 */
public class MixMatchDAO {
    public static void insert(String json) {
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();

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
        realm.beginTransaction();
        realm.clear(MixMatch.class);
        realm.copyToRealm(mixMatches);
        realm.commitTransaction();
    }

    public static RealmResults<MixMatch> getAll() {
        RealmResults<MixMatch> mixMatches = Realm.getDefaultInstance().allObjects(MixMatch.class);
        return mixMatches;
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.clear(MixMatch.class);
        realm.commitTransaction();
    }

}
