package com.android.emobilepos.security;

import android.content.Context;

import com.android.dao.EmployeePermissionDAO;
import com.android.emobilepos.models.realms.EmployeePersmission;
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

    public enum SecurityAction {
        TIME_CLOCK(1), MANAGE_TIME_CLOCK(2), SPLIT_ORDER(5), CHANGE_PRICE(13), SYSTEM_SETTINGS(14), PRINT_REPORTS(15), CREATE_CUSTOMERS(16),
        MANUAL_ADD_BALANCE_LOYALTY(17), REPRINT_ORDER(9), VOID_ORDER(10), VOID_PAYMENT(11),
        REMOVE_ITEM(3), OPEN_ORDER(4), TAKE_PAYMENT(6), SHIFT_CLERK(18), TIP_ADJUSTMENT(22), NONE(99);
        private int code;

        SecurityAction(int code) {
            this.code = code;
        }

    }
}
