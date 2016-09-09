package com.android.emobilepos.models;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by guarionex on 8/25/16.
 */
public class PaymentMethod extends RealmObject {
    @PrimaryKey
    private String paymethod_id;
    @Index
    private String paymethod_name;
    @Index
    private String paymentmethod_type;
    private String paymethod_update;
    private String isactive;
    private String paymethod_showOnline;
    private String image_url;
    private String OriginalTransid;
    @Index
    private long priority;

    public String getPaymethod_id() {
        return paymethod_id;
    }

    public void setPaymethod_id(String paymethod_id) {
        this.paymethod_id = paymethod_id;
    }

    public String getPaymethod_name() {
        return paymethod_name;
    }

    public void setPaymethod_name(String paymethod_name) {
        this.paymethod_name = paymethod_name;
    }

    public String getPaymentmethod_type() {
        return paymentmethod_type;
    }

    public void setPaymentmethod_type(String paymentmethod_type) {
        this.paymentmethod_type = paymentmethod_type;
    }

    public String getPaymethod_update() {
        return paymethod_update;
    }

    public void setPaymethod_update(String paymethod_update) {
        this.paymethod_update = paymethod_update;
    }

    public String getIsactive() {
        return isactive;
    }

    public void setIsactive(String isactive) {
        this.isactive = isactive;
    }

    public String getPaymethod_showOnline() {
        return paymethod_showOnline;
    }

    public void setPaymethod_showOnline(String paymethod_showOnline) {
        this.paymethod_showOnline = paymethod_showOnline;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getOriginalTransid() {
        return OriginalTransid;
    }

    public void setOriginalTransid(String originalTransid) {
        OriginalTransid = originalTransid;
    }

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public void incrementPriority() {
        priority++;
    }
}
