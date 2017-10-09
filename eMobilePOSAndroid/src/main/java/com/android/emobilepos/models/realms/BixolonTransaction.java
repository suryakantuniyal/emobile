package com.android.emobilepos.models.realms;

import com.android.emobilepos.models.orders.Order;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by guarionex on 5/25/17.
 */

public class BixolonTransaction extends RealmObject {
    @PrimaryKey
    private String orderId;
    @Index
    private String bixolonTransactionId;
    private Date transactionDate;

    public BixolonTransaction() {

    }

    public BixolonTransaction(Order order) {
        this.orderId = order.ord_id;
        this.bixolonTransactionId = order.getBixolonTransactionId();
        this.transactionDate = new Date();

    }

    public String getOrderId() {
        return orderId;
    }

    public String getBixolonTransactionId() {
        return bixolonTransactionId;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }
}
