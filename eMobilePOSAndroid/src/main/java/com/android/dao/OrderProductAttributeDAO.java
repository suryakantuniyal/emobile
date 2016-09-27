package com.android.dao;

import com.android.emobilepos.models.ProductAttribute;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import util.json.JsonUtils;

/**
 * Created by Guarionex on 4/12/2016.
 */
public class OrderProductAttributeDAO {
    public static void insert(String json) {
        Gson gson = JsonUtils.getInstance();

        Type listType = new com.google.gson.reflect.TypeToken<List<ProductAttribute>>() {
        }.getType();
        try {
            List<ProductAttribute> attributes = gson.fromJson(json, listType);
            OrderProductAttributeDAO.insert(attributes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert(List<ProductAttribute> attributes) {
        setPKId(attributes);
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(ProductAttribute.class);
        realm.copyToRealm(attributes);
        realm.commitTransaction();
    }

    private static void setPKId(List<ProductAttribute> attributes) {
        int pk = 1;
        for (ProductAttribute a : attributes) {
            a.setId(pk++);
        }
    }

    public static RealmResults<ProductAttribute> getAll() {
        RealmResults<ProductAttribute> attributes = Realm.getDefaultInstance().where(ProductAttribute.class).findAll();
        return attributes;
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(ProductAttribute.class);
        }finally {
            realm.commitTransaction();
        }
    }

    public static RealmResults<ProductAttribute> getByProdId(String prodId) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<ProductAttribute> where = realm.where(ProductAttribute.class);
        RealmResults<ProductAttribute> attributes = where.equalTo("productId", prodId).findAll();
        return attributes;
    }

    public static RealmResults<ProductAttribute> getByProdId(String prodId, boolean required) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<ProductAttribute> where = realm.where(ProductAttribute.class);
        RealmResults<ProductAttribute> attributes = where.equalTo("productId", prodId)
                .equalTo("required", required).findAll();
        return attributes;
    }

    public static ProductAttribute getById(int id) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<ProductAttribute> where = realm.where(ProductAttribute.class);
        ProductAttribute attribute = where.equalTo("id", id).findFirst();
        return attribute;
    }
}