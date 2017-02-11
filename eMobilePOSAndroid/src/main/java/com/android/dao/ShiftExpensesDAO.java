package com.android.dao;

import com.android.emobilepos.models.realms.ShiftExpense;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by guarionex on 02-11-17.
 */

public class ShiftExpensesDAO {

    public static void insertOrUpdate(ShiftExpense expense) {
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            r.insertOrUpdate(expense);
        } finally {
            r.commitTransaction();
        }
    }

    public static List<ShiftExpense> getShiftExpenses(String shiftId) {
        Realm r = Realm.getDefaultInstance();
        RealmResults<ShiftExpense> expenses = r.where(ShiftExpense.class).equalTo("shiftId", shiftId).findAll();
        if (expenses != null)
            return r.copyFromRealm(expenses);
        else
            return null;
    }
}
