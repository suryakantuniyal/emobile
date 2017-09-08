package com.android.emobilepos.models.realms;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
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
    private String realmId;
    @Index
    private String id;
    @Index
    private int userTypeCode;
    @Ignore
    private UserType userType;
    private RealmList<CustomerFid> fids;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
        realmId = this.id + userTypeCode;
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

    public UserType getUserType() {
        this.userType = UserType.getByCode(userTypeCode);
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userTypeCode = userType.getCode();
        realmId = this.id + userTypeCode;
        this.userType = userType;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

}
