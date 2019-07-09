package com.android.emobilepos.models.response;

/**
 * Created by guarionex on 02-12-17. Copied from NewUI branch. LC 07-01-19.
 */

public class ProcessCardResponse {
    private String epayStatusCode;
    private String statusCode;
    private String statusMessage;
    private String creditCardTransID;
    private String authorizationCode;
    private String pay_receipt;
    private String pay_refnum;
    private String pay_maccount;
    private String pay_groupcode;
    private String pay_stamp;
    private String pay_resultcode;
    private String pay_resultmessage;
    private String pay_expdate;
    private String pay_result;
    private String recordnumber;
    private String avsZip;
    private String cardSecurityCodeMatch;
    private String cardBalance;
    private String authorizedAmount;
    private String cardType;
    private String stadisTenderId;
    private String workingKey;
    private String secret;
    private String TerminalID;
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

    public String getCreditCardTransID() {
        return creditCardTransID;
    }

    public void setCreditCardTransID(String creditCardTransID) {
        this.creditCardTransID = creditCardTransID;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getPay_receipt() {
        return pay_receipt;
    }

    public void setPay_receipt(String pay_receipt) {
        this.pay_receipt = pay_receipt;
    }

    public String getPay_refnum() {
        return pay_refnum;
    }

    public void setPay_refnum(String pay_refnum) {
        this.pay_refnum = pay_refnum;
    }

    public String getPay_maccount() {
        return pay_maccount;
    }

    public void setPay_maccount(String pay_maccount) {
        this.pay_maccount = pay_maccount;
    }

    public String getPay_groupcode() {
        return pay_groupcode;
    }

    public void setPay_groupcode(String pay_groupcode) {
        this.pay_groupcode = pay_groupcode;
    }

    public String getPay_stamp() {
        return pay_stamp;
    }

    public void setPay_stamp(String pay_stamp) {
        this.pay_stamp = pay_stamp;
    }

    public String getPay_resultcode() {
        return pay_resultcode;
    }

    public void setPay_resultcode(String pay_resultcode) {
        this.pay_resultcode = pay_resultcode;
    }

    public String getPay_resultmessage() {
        return pay_resultmessage;
    }

    public void setPay_resultmessage(String pay_resultmessage) {
        this.pay_resultmessage = pay_resultmessage;
    }

    public String getPay_expdate() {
        return pay_expdate;
    }

    public void setPay_expdate(String pay_expdate) {
        this.pay_expdate = pay_expdate;
    }

    public String getPay_result() {
        return pay_result;
    }

    public void setPay_result(String pay_result) {
        this.pay_result = pay_result;
    }

    public String getRecordnumber() {
        return recordnumber;
    }

    public void setRecordnumber(String recordnumber) {
        this.recordnumber = recordnumber;
    }

    public String getAvsZip() {
        return avsZip;
    }

    public void setAvsZip(String avsZip) {
        this.avsZip = avsZip;
    }

    public String getCardSecurityCodeMatch() {
        return cardSecurityCodeMatch;
    }

    public void setCardSecurityCodeMatch(String cardSecurityCodeMatch) {
        this.cardSecurityCodeMatch = cardSecurityCodeMatch;
    }

    public String getCardBalance() {
        return cardBalance;
    }

    public void setCardBalance(String cardBalance) {
        this.cardBalance = cardBalance;
    }

    public String getAuthorizedAmount() {
        return authorizedAmount;
    }

    public void setAuthorizedAmount(String authorizedAmount) {
        this.authorizedAmount = authorizedAmount;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getStadisTenderId() {
        return stadisTenderId;
    }

    public void setStadisTenderId(String stadisTenderId) {
        this.stadisTenderId = stadisTenderId;
    }

    public String getWorkingKey() {
        return workingKey;
    }

    public void setWorkingKey(String workingKey) {
        this.workingKey = workingKey;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getTerminalID() {
        return TerminalID;
    }

    public void setTerminalID(String terminalID) {
        TerminalID = terminalID;
    }
}
