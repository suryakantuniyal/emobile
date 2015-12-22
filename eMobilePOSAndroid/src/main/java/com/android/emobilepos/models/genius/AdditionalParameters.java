package com.android.emobilepos.models.genius;

/**
 * Created by Guarionex on 12/18/2015.
 */
public class AdditionalParameters {
    private String SignatureData;
    private AmountDetails AmountDetails;
    private EMV EMV;


    public String getSignatureData() {
        return SignatureData;
    }

    public void setSignatureData(String signatureData) {
        SignatureData = signatureData;
    }

    public com.android.emobilepos.models.genius.AmountDetails getAmountDetails() {
        return AmountDetails;
    }

    public void setAmountDetails(com.android.emobilepos.models.genius.AmountDetails amountDetails) {
        AmountDetails = amountDetails;
    }

    public com.android.emobilepos.models.genius.EMV getEMV() {
        return EMV;
    }

    public void setEMV(com.android.emobilepos.models.genius.EMV EMV) {
        this.EMV = EMV;
    }
}
