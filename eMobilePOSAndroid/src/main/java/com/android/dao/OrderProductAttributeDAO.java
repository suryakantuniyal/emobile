package com.android.dao;

import com.android.emobilepos.models.realms.ProductAttribute;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
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
        try {
            realm.beginTransaction();
            realm.delete(ProductAttribute.class);
            realm.copyToRealm(attributes);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    private static void setPKId(List<ProductAttribute> attributes) {
        int pk = 1;
        for (ProductAttribute a : attributes) {
            a.setId(pk++);
        }
    }

    public static List<ProductAttribute> getAll() {
        Realm realm = Realm.getDefaultInstance();
        try {
            List<ProductAttribute> attributes = realm.where(ProductAttribute.class).findAll();
            if (attributes != null) {
                attributes = realm.copyFromRealm(attributes);
            }
            return attributes;
        } finally {
            realm.close();
        }
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(ProductAttribute.class);
        } finally {
            realm.commitTransaction();
            realm.close();
        }
    }

    public static List<ProductAttribute> getByProdId(String prodId) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmQuery<ProductAttribute> where = realm.where(ProductAttribute.class);
            List<ProductAttribute> attributes = where.equalTo("productId", prodId).findAll();
            if (attributes != null) {
                attributes = realm.copyFromRealm(attributes);
            }
            return attributes;
        } finally {
            realm.close();
        }
    }

    public static List<ProductAttribute> getByProdId(String prodId, boolean required) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmQuery<ProductAttribute> where = realm.where(ProductAttribute.class);
            List<ProductAttribute> attributes = where.equalTo("productId", prodId)
                    .equalTo("required", required).findAll();
            if (attributes != null) {
                attributes = realm.copyFromRealm(attributes);
            }
            return attributes;
        } finally {
            realm.close();
        }
    }

    public static ProductAttribute getById(int id) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmQuery<ProductAttribute> where = realm.where(ProductAttribute.class);
            ProductAttribute attribute = where.equalTo("id", id).findFirst();
            if (attribute != null) {
                attribute = realm.copyFromRealm(attribute);
            }
            return attribute;
        } finally {
            realm.close();
        }
    }
}