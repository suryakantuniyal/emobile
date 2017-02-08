package com.android.dao;

import com.android.emobilepos.models.realms.OrderAttributes;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Created by guarionex on 2/8/17.
 */

public class OrderAttributesDAO {

    public static void insert(List<OrderAttributes> orderAttributes) {
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            r.insertOrUpdate(orderAttributes);
        } finally {
            r.commitTransaction();
        }
    }

    public static List<OrderAttributes> getOrderAttributes() {
        Realm r = Realm.getDefaultInstance();
        return r.where(OrderAttributes.class).findAll();
    }

    public static List<String> getOrderAttributeNames() {
        List<OrderAttributes> orderAttributes = getOrderAttributes();
        List<String> names = new ArrayList<>();
        for (OrderAttributes attributes : orderAttributes) {
            names.add(attributes.getOrdAttrName());
        }
        return names;
    }
}
