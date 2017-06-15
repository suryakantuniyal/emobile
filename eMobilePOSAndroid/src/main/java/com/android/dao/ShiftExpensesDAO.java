package com.android.dao;

import com.android.emobilepos.models.realms.Shift;
import com.android.emobilepos.models.realms.ShiftExpense;

import java.math.BigDecimal;
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
            Shift shift = r.where(Shift.class).equalTo("shiftId", expense.getShiftId()).findFirst();
            shift.setSync(false);
//            shift.setEndingPettyCash(String.valueOf(Global.getBigDecimalNum(shift.getTotal_ending_cash())
//                    .add(Global.getBigDecimalNum(expense.getCashAmount()))));
            r.insertOrUpdate(expense);
        } finally {
            r.commitTransaction();
            r.close();
        }
    }

    public static List<ShiftExpense> getShiftExpenses(String shiftId) {
        Realm r = Realm.getDefaultInstance();
        try {
            RealmResults<ShiftExpense> expenses = r.where(ShiftExpense.class).equalTo("shiftId", shiftId).findAll();
            if (expenses != null)
                return r.copyFromRealm(expenses);
            else
                return null;
        } finally {
            r.close();
        }
    }


    public static BigDecimal getShiftTotalExpenses(String shiftID) {
        Realm r = Realm.getDefaultInstance();
        try {
            RealmResults<ShiftExpense> expenses = r.where(ShiftExpense.class).equalTo("shiftId", shiftID).findAll();
            BigDecimal total = new BigDecimal(0);
            for (ShiftExpense expense : expenses) {
                total = total.add(new BigDecimal(expense.getCashAmount() == null ? "0" : expense.getCashAmount()));
            }
            return total;
        } finally {
            r.close();
        }
    }

    public static BigDecimal getShiftTotalExpenses(String shiftID, ShiftExpense.ExpenseProductId productId) {
        Realm r = Realm.getDefaultInstance();
        try {
            RealmResults<ShiftExpense> expenses = r.where(ShiftExpense.class)
                    .equalTo("shiftId", shiftID)
                    .equalTo("productId", productId.getCode())
                    .findAll();
            BigDecimal total = new BigDecimal(0);
            for (ShiftExpense expense : expenses) {
                total = total.add(new BigDecimal(expense.getCashAmount() == null ? "0" : expense.getCashAmount()));
            }
            return total;
        } finally {
            r.close();
        }
    }
}
