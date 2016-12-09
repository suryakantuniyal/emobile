package com.android.support;

import android.app.Activity;
import android.content.Context;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.StoredPaymentsDAO;
import com.android.database.ConsignmentTransactionHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.database.TransferLocations_DB;
import com.android.emobilepos.models.realms.AssignEmployee;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GenerateNewID {
    private MyPreferences myPref;
    private Context activity;

    public enum IdType {
        ORDER_ID, PAYMENT_ID, CONSIGNMENT_ID, INVENTORY_TRANSFER_ID
    }

    private static String delims = "[\\-]";

    public GenerateNewID(Context activity) {
        this.activity = activity;
        myPref = new MyPreferences(activity);
    }

    public String getNextID(String currentId) {
        String[] tokens = currentId.split(delims);
        int seq = Integer.parseInt(tokens[1]);
        seq++;

        return tokens[0] + "-" + String.format("%05d", seq) + "-" + tokens[2];
    }

    public String getNextID(IdType idType) {
        AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        String year = sdf.format(new Date());
        String lastID = null;

        switch (idType) {
            case ORDER_ID:
                lastID = OrdersHandler.getInstance(activity).getLastOrderId(Integer.parseInt(String.valueOf(assignEmployee.getEmpId())), Integer.parseInt(year));
                break;
            case PAYMENT_ID:
                if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
                    lastID = StoredPaymentsDAO.getLastPaymentId(activity, Integer.parseInt(String.valueOf(assignEmployee.getEmpId())), Integer.parseInt(year));
                } else {
                    lastID = PaymentsHandler.getInstance(activity).getLastPaymentId(Integer.parseInt(String.valueOf(assignEmployee.getEmpId())), Integer.parseInt(year));
                }
                break;
            case CONSIGNMENT_ID:
                lastID = ConsignmentTransactionHandler.getInstance(activity).getLastConsignmentId(Integer.parseInt(String.valueOf(assignEmployee.getEmpId())), Integer.parseInt(year));
                break;
            case INVENTORY_TRANSFER_ID:
                lastID = TransferLocations_DB.getLastTransferID(Integer.parseInt(String.valueOf(assignEmployee.getEmpId())), Integer.parseInt(year));
                break;
        }

        if (lastID == null)
            lastID = "";

        if (lastID.isEmpty() || lastID.length() <= 4) {
            sb.append(assignEmployee.getEmpId()).append("-").append("00001").append("-").append(year);
        } else {

            String[] tokens = lastID.split(delims);

            if (tokens[2].equals(year)) {
                int seq = Integer.parseInt(tokens[1]);
                sb.append(assignEmployee.getEmpId()).append("-").append(String.format("%05d", (seq + 1))).append("-")
                        .append(year);
            } else {
                sb.append(assignEmployee.getEmpId()).append("-").append("00001").append("-").append(year);
            }
        }

        return sb.toString();
    }

    public static String getQBOrderId(String orderId) {
        String qbOrderId = orderId.replace("-", "");
        qbOrderId = qbOrderId.substring(0, qbOrderId.length() - 4) + qbOrderId.substring(qbOrderId.length() - 2);
        return qbOrderId;
    }
}
