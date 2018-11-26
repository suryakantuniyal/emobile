package com.android.emobilepos.models.pax;

/**
 * Created by Luis Camayd on 11/12/2018.
 */
public class SoundPaymentsResponse {

    private String epayStatusCode;
    private String statusCode;
    private String statusMessage;
    private String CreditCardTransID;
    private String AuthorizationCode;
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
    private String AVSZip;
    private String CardSecurityCodeMatch;
    private String CardBalance;
    private String AuthorizedAmount;
    private String CardType;
    private String CCLast4;
    private String CCexpDate;
    private String CCName;
    private String AID;
    private String APPLAB;
    private String ATC;
    private String CVM;
    private String CVMMSG;
    private String IAD;
    private String AC;
    private String TVR;
    private String EntryMode;
    private String EntryModeMsg;

    public SoundPaymentsResponse() {
    }


    public String getCCexpDate() {
        return CCexpDate;
    }

    public void setCCexpDate(String CCexpDate) {
        this.CCexpDate = CCexpDate;
    }

    public String getPay_refnum() {
        return pay_refnum;
    }

    public void setPay_refnum(String pay_refnum) {
        this.pay_refnum = pay_refnum;
    }

    public String getEpayStatusCode() {
        return epayStatusCode;
    }

    public void setEpayStatusCode(String epayStatusCode) {
        this.epayStatusCode = epayStatusCode;
    }

    public String getPay_receipt() {
        return pay_receipt;
    }

    public void setPay_receipt(String pay_receipt) {
        this.pay_receipt = pay_receipt;
    }

    public String getCardBalance() {
        return CardBalance;
    }

    public void setCardBalance(String CardBalance) {
        this.CardBalance = CardBalance;
    }

    public String getAVSZip() {
        return AVSZip;
    }

    public void setAVSZip(String AVSZip) {
        this.AVSZip = AVSZip;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getPay_maccount() {
        return pay_maccount;
    }

    public void setPay_maccount(String pay_maccount) {
        this.pay_maccount = pay_maccount;
    }

    public String getCardSecurityCodeMatch() {
        return CardSecurityCodeMatch;
    }

    public void setCardSecurityCodeMatch(String CardSecurityCodeMatch) {
        this.CardSecurityCodeMatch = CardSecurityCodeMatch;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getCCLast4() {
        return CCLast4;
    }

    public void setCCLast4(String CCLast4) {
        this.CCLast4 = CCLast4;
    }

    public String getPay_groupcode() {
        return pay_groupcode;
    }

    public void setPay_groupcode(String pay_groupcode) {
        this.pay_groupcode = pay_groupcode;
    }

    public String getAuthorizationCode() {
        return AuthorizationCode;
    }

    public void setAuthorizationCode(String AuthorizationCode) {
        this.AuthorizationCode = AuthorizationCode;
    }

    public String getRecordnumber() {
        return recordnumber;
    }

    public void setRecordnumber(String recordnumber) {
        this.recordnumber = recordnumber;
    }

    public String getPay_result() {
        return pay_result;
    }

    public void setPay_result(String pay_result) {
        this.pay_result = pay_result;
    }

    public String getAuthorizedAmount() {
        return AuthorizedAmount;
    }

    public void setAuthorizedAmount(String AuthorizedAmount) {
        this.AuthorizedAmount = AuthorizedAmount;
    }

    public String getCreditCardTransID() {
        return CreditCardTransID;
    }

    public void setCreditCardTransID(String CreditCardTransID) {
        this.CreditCardTransID = CreditCardTransID;
    }

    public String getPay_resultcode() {
        return pay_resultcode;
    }

    public void setPay_resultcode(String pay_resultcode) {
        this.pay_resultcode = pay_resultcode;
    }

    public String getPay_stamp() {
        return pay_stamp;
    }

    public void setPay_stamp(String pay_stamp) {
        this.pay_stamp = pay_stamp;
    }

    public String getPay_expdate() {
        return pay_expdate;
    }

    public void setPay_expdate(String pay_expdate) {
        this.pay_expdate = pay_expdate;
    }

    public String getCardType() {
        return CardType;
    }

    public void setCardType(String CardType) {
        this.CardType = CardType;
    }

    public String getPay_resultmessage() {
        return pay_resultmessage;
    }

    public void setPay_resultmessage(String pay_resultmessage) {
        this.pay_resultmessage = pay_resultmessage;
    }

    public String getCCName() {
        return CCName;
    }

    public void setCCName(String CCName) {
        this.CCName = CCName;
    }

    public String getAC() {
        return AC;
    }

    public void setAC(String AC) {
        this.AC = AC;
    }

    public String getEntryMode() {
        return EntryMode;
    }

    public void setEntryMode(String EntryMode) {
        this.EntryMode = EntryMode;
    }

    public String getATC() {
        return ATC;
    }

    public void setATC(String ATC) {
        this.ATC = ATC;
    }

    public String getCVM() {
        return CVM;
    }

    public void setCVM(String CVM) {
        this.CVM = CVM;
    }

    public String getAID() {
        return AID;
    }

    public void setAID(String AID) {
        this.AID = AID;
    }

    public String getCVMMSG() {
        return CVMMSG;
    }

    public void setCVMMSG(String CVMMSG) {
        this.CVMMSG = CVMMSG;
    }

    public String getTVR() {
        return TVR;
    }

    public void setTVR(String TVR) {
        this.TVR = TVR;
    }

    public String getAPPLAB() {
        return APPLAB;
    }

    public void setAPPLAB(String APPLAB) {
        this.APPLAB = APPLAB;
    }

    public String getIAD() {
        return IAD;
    }

    public void setIAD(String IAD) {
        this.IAD = IAD;
    }

    public String getEntryModeMsg() {
        return EntryModeMsg;
    }

    public void setEntryModeMsg(String EntryModeMsg) {
        this.EntryModeMsg = EntryModeMsg;
    }
}