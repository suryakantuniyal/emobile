package com.android.emobilepos.models.realms;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by guarionex on 5/23/17.
 */

public class BixolonPaymentMethod extends RealmObject {
    @PrimaryKey
    private int id;
    private PaymentMethod paymentMethod;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
