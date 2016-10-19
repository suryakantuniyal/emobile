package com.android.emobilepos.models.genius;

/**
 * Created by Guarionex on 12/18/2015.
 */
public class ApplicationCryptogram {
    private String CryptogramType;
    private String Cryptogram;

    public String getCryptogramType() {
        return CryptogramType;
    }

    public void setCryptogramType(String cryptogramType) {
        CryptogramType = cryptogramType;
    }

    public String getCryptogram() {
        return Cryptogram;
    }

    public void setCryptogram(String cryptogram) {
        Cryptogram = cryptogram;
    }
}
