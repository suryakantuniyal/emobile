package com.android.dao;

import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.DinningTable;
import com.android.emobilepos.models.realms.DinningTableOrder;
import com.android.support.DateUtils;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Guarionex on 4/12/2016.
 */
public class DinningTableOrderDAO {

    public static void insert(DinningTableOrder dinningTableOrder) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(dinningTableOrder);
        } finally {
            realm.commitTransaction();
            if(realm!=null) {
                realm.close();
            }
        }
    }

    public static List<DinningTableOrder> getAll() {
        Realm realm = Realm.getDefaultInstance();
        try {
            List<DinningTableOrder> all = Realm.getDefaultInstance().where(DinningTableOrder.class).findAll();
            if (all != null) {
                all = realm.copyFromRealm(all);
            }
            return all;
        } finally {
            if(realm!=null) {
                realm.close();
            }
        }
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.delete(DinningTableOrder.class);
        } finally {
            realm.commitTransaction();
            if(realm!=null) {
                realm.close();
            }
        }
    }

    public static void deleteByNumber(String number) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmQuery<DinningTableOrder> where = realm.where(DinningTableOrder.class);
            RealmResults<DinningTableOrder> results = where.equalTo("dinningTable.number", number).findAll();
            realm.beginTransaction();
            results.deleteAllFromRealm();
        } finally {
            realm.commitTransaction();
            if(realm!=null) {
                realm.close();
            }
        }
    }

    public static DinningTableOrder getByNumber(String number) {
        Realm realm = Realm.getDefaultInstance();
        DinningTableOrder first;
        try {
            RealmQuery<DinningTableOrder> where = realm.where(DinningTableOrder.class);
            first = where.equalTo("dinningTable.number", number).findFirst();
            if (first != null) {
                first = realm.copyFromRealm(first);
            }
        } finally {
            if(realm!=null) {
                realm.close();
            }
        }
        return first;
    }

    public static void createDinningTableOrder(Order order) {
        DinningTable table = DinningTableDAO.getByNumber(order.assignedTable);
        DinningTableOrder dinningTableOrder = DinningTableOrderDAO.getByNumber(order.assignedTable);
        if (dinningTableOrder == null && table != null && order.numberOfSeats > 0) {
            dinningTableOrder = new DinningTableOrder();
            dinningTableOrder.setDinningTable(table);
            dinningTableOrder.setOrderStartDate(DateUtils.getDateStringAsDate(order.ord_timeStarted, DateUtils.DATE_yyyy_MM_ddTHH_mm_ss));
            dinningTableOrder.setCurrentOrderId(order.ord_id);
            dinningTableOrder.setNumberOfGuest(order.numberOfSeats);
            DinningTableOrderDAO.insert(dinningTableOrder);
        }
    }

    public static void deleteByOrderId(String ord_id) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmQuery<DinningTableOrder> where = realm.where(DinningTableOrder.class);
            RealmResults<DinningTableOrder> results = where.equalTo("currentOrderId", ord_id).findAll();
            realm.beginTransaction();
            results.deleteAllFromRealm();
        } finally {
            realm.commitTransaction();
            if(realm!=null) {
                realm.close();
            }
        }
    }
}
