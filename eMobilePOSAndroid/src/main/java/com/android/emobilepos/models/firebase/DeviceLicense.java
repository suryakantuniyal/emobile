package com.android.emobilepos.models.firebase;

import io.realm.RealmObject;

/**
 * Created by guarionex on 1/3/17.
 */

public class DeviceLicense extends RealmObject {
    private String activationKey;

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }
}
