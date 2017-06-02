package com.android.emobilepos.security;

import android.content.Context;

import com.android.dao.EmployeePermissionDAO;
import com.android.dao.ShiftDAO;
import com.android.emobilepos.models.realms.EmployeePersmission;
import com.android.emobilepos.models.realms.Shift;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.util.List;

/**
 * Created by guarionex on 02-12-17.
 */

public class SecurityManager {


    public static boolean hasPermissions(Context context, SecurityAction action) {
        MyPreferences preferences = new MyPreferences(context);
        if (preferences.isUseClerks()) {
            List<EmployeePersmission> persmissions = EmployeePermissionDAO.getEmployeePersmissions(Integer.parseInt(preferences.getClerkID()), action.code);
            return persmissions != null && !persmissions.isEmpty();
        } else
            return true;
    }

    public static SecurityResponse validateClerkShift(Context context, Global.TransactionType transactionType) {
        MyPreferences preferences = new MyPreferences(context);
//        if (preferences.isShiftOpenRequired() && !ShiftDAO.isShiftOpen()) {
//            return SecurityResponse.CHECK_USER_CLERK_REQUIRED_SETTING;
//        }
//        if (preferences.isShiftOpenRequired() && preferences.isUseClerks()) {
        boolean shiftOpen = ShiftDAO.isShiftOpen();
        if (!shiftOpen &&  (transactionType != Global.TransactionType.SHIFT_EXPENSES &&
                transactionType != Global.TransactionType.SHIFTS && preferences.isShiftOpenRequired())
                || transactionType == Global.TransactionType.SHIFT_EXPENSES)
            return SecurityResponse.OPEN_SHIFT_REQUIRED;
        else {
            Shift openShift = ShiftDAO.getOpenShift();
            if (openShift != null && openShift.getClerkId() != Integer.parseInt(preferences.getClerkID())) {
                Shift shift = ShiftDAO.getShiftByClerkId(Integer.parseInt(preferences.getClerkID()));
                if (shift != null && shift.getShiftStatus() == Shift.ShiftStatus.PENDING) {
                    return SecurityResponse.CLERK_PENDING_SHIFT_AVAILABLE;
                } else {
                    if (transactionType == Global.TransactionType.SHIFTS) {
                        return SecurityResponse.SHIFT_ALREADY_OPEN;
                    }
                }
            }
        }

//        }
        return SecurityResponse.OK;
    }

    public enum SecurityResponse {
        OPEN_SHIFT_REQUIRED, CHECK_OPEN_SHIFT_REQUIRED_SETTING, CHECK_USER_CLERK_REQUIRED_SETTING, OK, SHIFT_ALREADY_OPEN,
        CLERK_PENDING_SHIFT_AVAILABLE
    }

    public enum SecurityAction {
        TIME_CLOCK(1), MANAGE_TIME_CLOCK(2), SPLIT_ORDER(5), CHANGE_PRICE(13), SYSTEM_SETTINGS(14), PRINT_REPORTS(15), CREATE_CUSTOMERS(16),
        MANUAL_ADD_BALANCE_LOYALTY(17), REPRINT_ORDER(9), VOID_ORDER(10), VOID_PAYMENT(11), NO_SALE(12),
        REMOVE_ITEM(3), OPEN_ORDER(4), TAKE_PAYMENT(6), SHIFT_CLERK(18), TIP_ADJUSTMENT(22), MANAGE_SHIFT(19), NONE(99);
        private int code;

        SecurityAction(int code) {
            this.code = code;
        }

    }
}
