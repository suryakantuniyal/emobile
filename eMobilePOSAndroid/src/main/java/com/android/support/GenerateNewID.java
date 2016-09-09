package com.android.support;

import android.app.Activity;

import com.android.dao.StoredPaymentsDAO;
import com.android.database.ConsignmentTransactionHandler;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GenerateNewID {
    private MyPreferences myPref;
    private Activity activity;

    public enum IdType {
        ORDER_ID, PAYMENT_ID, CONSIGNMENT_ID
    }

    // public static final int ORDER_ID = 0, PAYMENT_ID = 1;
    private static String delims = "[\\-]";

    public GenerateNewID(Activity activity) {
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
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
        String year = sdf.format(new Date());
        String lastID = null;

        switch (idType) {
            case ORDER_ID:
                lastID = OrdersHandler.getInstance(activity).getLastOrderId(Integer.parseInt(myPref.getEmpID()), Integer.parseInt(year));
                break;
            case PAYMENT_ID:
                if (myPref.getPreferences(MyPreferences.pref_use_store_and_forward)) {
                    lastID = StoredPaymentsDAO.getLastPaymentId(activity, Integer.parseInt(myPref.getEmpID()), Integer.parseInt(year));
                } else {
                    lastID = PaymentsHandler.getInstance(activity).getLastPaymentId(Integer.parseInt(myPref.getEmpID()), Integer.parseInt(year));
                }
                break;
            case CONSIGNMENT_ID:
                lastID = ConsignmentTransactionHandler.getInstance(activity).getLastConsignmentId(Integer.parseInt(myPref.getEmpID()), Integer.parseInt(year));
                break;
        }

        if (lastID == null)
            lastID = "";

        if (lastID.isEmpty() || lastID.length() <= 4) {
            sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
        } else {

            String[] tokens = lastID.split(delims);

            if (tokens[2].equals(year)) {
                int seq = Integer.parseInt(tokens[1]);
                sb.append(myPref.getEmpID()).append("-").append(String.format("%05d", (seq + 1))).append("-")
                        .append(year);
            } else {
                sb.append(myPref.getEmpID()).append("-").append("00001").append("-").append(year);
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
