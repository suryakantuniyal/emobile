package com.android.emobilepos.models.realms;

import android.text.TextUtils;
import android.util.Base64;

import com.android.emobilepos.customer.ViewCustomerDetails_FA;
import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.google.gson.annotations.Expose;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Guarionex on 5/24/2016.
 */
public class BiometricFid extends RealmObject {
    @Expose
    @PrimaryKey
    private String id;
    @Expose(serialize = false, deserialize = false)
    transient private String fid;
    @Expose(serialize = false, deserialize = false)
    private byte[] fmdData;
    @Expose
    private String fmdBase64;
    @Expose
    @Index
    private int fingerCode;

    public BiometricFid() {

    }

    public BiometricFid(Engine engine, Fid fid, ViewCustomerDetails_FA.Finger finger) throws UareUException {
        Fmd fmd = engine.CreateFmd(fid, Fmd.Format.ANSI_378_2004);
        this.id = md5(fmd.getData());
        String encode = Base64.encodeToString(fid.getData(), Base64.DEFAULT);
        this.fingerCode = finger.getCode();
        this.fid = encode;
        this.fmdData = fmd.getData();
        this.fmdBase64 = Base64.encodeToString(getFmdData(), Base64.DEFAULT);
    }

    public static String md5(byte[] data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(data);
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getGuidFromByteArray(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        UUID uuid = new UUID(high, low);
        return uuid.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public Fmd getFmdEntity() {
//        byte[] fidData = Base64.decode(getFid(), Base64.DEFAULT);
        Fmd importFmd = null;
        try {
            importFmd = UareUGlobal.GetImporter().ImportFmd(getFmdData(), Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
        } catch (UareUException e) {
            e.printStackTrace();
        }
        return importFmd;
    }

    public byte[] getFmdData() {
        if (fmdData == null && !TextUtils.isEmpty(fmdBase64)) {
            fmdData = Base64.decode(fmdBase64, Base64.DEFAULT);
        }
        return fmdData;
    }

    public void setFmdData(byte[] fidData) {
        this.fmdData = fidData;
        this.fmdBase64 = Base64.encodeToString(getFmdData(), Base64.DEFAULT);
    }

    public void decodeFmdBase64() {
        if (fmdData == null && !TextUtils.isEmpty(fmdBase64)) {
            fmdData = Base64.decode(fmdBase64, Base64.DEFAULT);
        }
    }
}