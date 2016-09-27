package com.android.dao;

import com.android.emobilepos.models.DinningTable;
import com.android.emobilepos.models.DinningTableOrder;
import com.android.emobilepos.models.Order;
import com.android.support.DateUtils;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Guarionex on 4/12/2016.
 */
public class DinningTableOrderDAO {

    public static void insert(DinningTableOrder dinningTableOrder) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(dinningTableOrder);
        realm.commitTransaction();
    }

    public static RealmResults<DinningTableOrder> getAll() {
        return Realm.getDefaultInstance().where(DinningTableOrder.class).findAll();
    }

    public static void truncate() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(DinningTableOrder.class);
        realm.commitTransaction();
    }

    public static void deleteByNumber(String number) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<DinningTableOrder> where = realm.where(DinningTableOrder.class);
        RealmResults<DinningTableOrder> results = where.equalTo("dinningTable.number", number).findAll();
        realm.beginTransaction();
        results.deleteAllFromRealm();
        realm.commitTransaction();
    }

    public static DinningTableOrder getByNumber(String number) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<DinningTableOrder> where = realm.where(DinningTableOrder.class);
        return where.equalTo("dinningTable.number", number).findFirst();
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
        RealmQuery<DinningTableOrder> where = realm.where(DinningTableOrder.class);
        RealmResults<DinningTableOrder> results = where.equalTo("currentOrderId", ord_id).findAll();
        realm.beginTransaction();
        results.deleteAllFromRealm();
        realm.commitTransaction();
    }
}
