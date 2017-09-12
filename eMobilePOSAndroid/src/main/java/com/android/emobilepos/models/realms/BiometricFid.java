package com.android.emobilepos.models.realms;

import android.util.Base64;

import com.android.emobilepos.customer.ViewCustomerDetails_FA;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Guarionex on 5/24/2016.
 */
public class BiometricFid extends RealmObject {
    @PrimaryKey
    private long id;
    private String fid;
    @Index
    private int fingerCode;

    public BiometricFid() {

    }

    public BiometricFid(Fid fid, ViewCustomerDetails_FA.Finger finger) {
        this.id = System.currentTimeMillis();
        String encode = Base64.encodeToString(fid.getData(), Base64.DEFAULT);
        this.fingerCode = finger.getCode();
        this.fid = encode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public int getFingerCode() {
        return fingerCode;
    }

    public void setFingerCode(int fingerCode) {
        this.fingerCode = fingerCode;
    }

    public Fid getFidEntity() {
        byte[] fidData = Base64.decode(getFid(), Base64.DEFAULT);
        Fid importFid = null;
        try {
            importFid = UareUGlobal.GetImporter().ImportFid(fidData, Fid.Format.ANSI_381_2004);
        } catch (UareUException e) {
            e.printStackTrace();
        }
        return importFid;
    }

}