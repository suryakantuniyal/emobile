package com.android.emobilepos.models.genius;

/**
 * Created by Guarionex on 12/22/2015.
 */
public class GeniusTransportToken {
    private String epayStatusCode;
    private String statusCode;
    private String statusMessage;
    private String Transportkey;
    private String ValidationKey;
    private String UnexpectedEMVField;
    private String UnexpectedEmptyField1;
    private String UnexpectedEmptyField2;
    private String UnexpectedEmptyField3;

    public String getEpayStatusCode() {
        return epayStatusCode;
    }

    public void setEpayStatusCode(String epayStatusCode) {
        this.epayStatusCode = epayStatusCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getTransportkey() {
        return Transportkey;
    }

    public void setTransportkey(String transportkey) {
        Transportkey = transportkey;
    }

    public String getValidationKey() {
        return ValidationKey;
    }

    public void setValidationKey(String validationKey) {
        ValidationKey = validationKey;
    }

    public String getUnexpectedEMVField() {
        return UnexpectedEMVField;
    }

    public void setUnexpectedEMVField(String unexpectedEMVField) {
        UnexpectedEMVField = unexpectedEMVField;
    }

    public String getUnexpectedEmptyField1() {
        return UnexpectedEmptyField1;
    }

    public void setUnexpectedEmptyField1(String unexpectedEmptyField1) {
        UnexpectedEmptyField1 = unexpectedEmptyField1;
    }

    public String getUnexpectedEmptyField2() {
        return UnexpectedEmptyField2;
    }

    public void setUnexpectedEmptyField2(String unexpectedEmptyField2) {
        UnexpectedEmptyField2 = unexpectedEmptyField2;
    }

    public String getUnexpectedEmptyField3() {
        return UnexpectedEmptyField3;
    }

    public void setUnexpectedEmptyField3(String unexpectedEmptyField3) {
        UnexpectedEmptyField3 = unexpectedEmptyField3;
    }
}
