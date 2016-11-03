package com.android.emobilepos.models.genius;

/**
 * Created by Guarionex on 12/18/2015.
 */
public class CardInformation {
    private String MaskedPan;
    private String PanSequenceNumber;
    private String CardExpiryDate;

    public String getMaskedPan() {
        return MaskedPan;
    }

    public void setMaskedPan(String maskedPan) {
        MaskedPan = maskedPan;
    }

    public String getPanSequenceNumber() {
        return PanSequenceNumber;
    }

    public void setPanSequenceNumber(String panSequenceNumber) {
        PanSequenceNumber = panSequenceNumber;
    }

    public String getCardExpiryDate() {
        return CardExpiryDate;
    }

    public void setCardExpiryDate(String cardExpiryDate) {
        CardExpiryDate = cardExpiryDate;
    }
}
