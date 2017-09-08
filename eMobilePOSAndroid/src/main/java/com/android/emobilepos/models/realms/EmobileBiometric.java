package com.android.emobilepos.models.realms;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Guarionex on 5/24/2016.
 */
public class EmobileBiometric extends RealmObject {
    
    public enum UserType{
        CUSTOMER(0), CLERK(1), SYSTEM(2);

        private int code;

        UserType(int code) {
            this.code = code;
        }
        public int getCode(){
            return code;
        }
        public static UserType getByCode(int code){
            switch (code){
                case 0:
                    return CUSTOMER;
                case 1:
                    return CLERK;
                case 2:
                    return SYSTEM;
            }
            return null;
        }
    }

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
