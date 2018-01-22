package com.android.emobilepos.models.realms;

import com.google.gson.annotations.Expose;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Guarionex on 5/24/2016.
 */
public class EmobileBiometric extends RealmObject {

    @Expose(serialize = false, deserialize = false)
    @PrimaryKey
    private String realmId;
    @Expose
    @Index
    private String entityid;
    @Expose
    @Index
    private int userTypeCode;
    @Ignore
    private UserType userType;
    @Expose
    private RealmList<BiometricFid> fids;
    @Expose
    @Index
    private String regid;

    public String getEntityid() {
        return entityid;
    }

    public void setEntityid(String entityid) {
        this.entityid = entityid;
        realmId = this.entityid + userTypeCode;
    }

    public RealmList<BiometricFid> getFids() {
        if (null == fids) {
            fids = new RealmList<>();
        }
        return fids;
    }

    public void setFids(RealmList<BiometricFid> fids) {
        this.fids = fids;
    }

    public void initRealmId() {
        setEntityid(this.entityid);
    }

    public UserType getUserType() {
        this.userType = UserType.getByCode(userTypeCode);
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userTypeCode = userType.getCode();
        realmId = this.entityid + userTypeCode;
        this.userType = userType;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getRegid() {
        return regid;
    }

    public void setRegid(String regid) {
        this.regid = regid;
    }

    public enum UserType {
        CUSTOMER(0), CLERK(1), SYSTEM(2);

        private int code;

        UserType(int code) {
            this.code = code;
        }

        public static UserType getByCode(int code) {
            switch (code) {
                case 0:
                    return CUSTOMER;
                case 1:
                    return CLERK;
                case 2:
                    return SYSTEM;
            }
            return null;
        }

        public int getCode() {
            return code;
        }
    }

}
