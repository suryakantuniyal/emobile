package com.android.emobilepos.models.realms;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Guarionex on 5/24/2016.
 */
public class CustomerBiometric extends RealmObject {
    @PrimaryKey
    private String customerId;
    private RealmList<CustomerFid> fids;
    public String getCustomerId() {
        return customerId;
    }
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    public RealmList<CustomerFid> getFids() {
        if (null == fids) {
            fids = new RealmList<>();
        }
        return fids;
    }

    public void setFids(RealmList<CustomerFid> fids) {
        this.fids = fids;
    }


}
