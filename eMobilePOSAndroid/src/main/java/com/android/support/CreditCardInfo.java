package com.android.support;

import com.android.emobilepos.models.EMVContainer;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CreditCardInfo {
    private String cardType = "";
    private String cardOwnerName = "";
    private String cardExpMonth = "";
    private String cardExpYear = "";
    private String cardLast4 = "";
    private String cardNumAESEncrypted = "";
    private String cardNumUnencrypted = "";
    private String encryptedBlock = "";
    private String trackDataKSN = "";
    private String magnePrint = "";
    private String magnePrintStatus = "";
    private String deviceSerialNumber = "";
    private String encryptedAESTrack1 = "";
    private String encryptedAESTrack2 = "";
    private String encryptedTrack1 = "";
    private String encryptedTack2 = "";
    private String cardEncryptedSecCode = "";
    private String redeem_type = "";
    private String redeemAll = "";
    private String originalTotalAmount = "0";
    private String debitPinBlock = "";
    private String debitPinSerialNum = "";
    public String authcode;
    public String transid;
    public BigDecimal dueAmount = new BigDecimal("0");
    private boolean wasSwiped = false;
    private String resultMessage;
    private EMVContainer emvContainer;


    public void setWasSwiped(boolean value) {
        this.wasSwiped = value;
    }

    public boolean getWasSwiped() {
        return this.wasSwiped;
    }

    public void setRedeemType(String value) {
        this.redeem_type = value;
    }

    public void setRedeemAll(String value) {
        this.redeemAll = value;
    }

    public void setCardType(String value) {
        this.cardType = value;
    }

    public void setCardOwnerName(String value) {
        this.cardOwnerName = value;
    }

    public void setCardExpMonth(String value) {
        this.cardExpMonth = value;
    }

    public void setCardExpYear(String value) {
        this.cardExpYear = value;
    }

    public void setCardLast4(String value) {
        this.cardLast4 = value;
    }

    public void setCardNumAESEncrypted(String value) {
        this.cardNumAESEncrypted = value;
    }

    public void setCardNumUnencrypted(String value) {
        this.cardNumUnencrypted = value;
    }

    public void setEncryptedBlock(String value) {
        this.encryptedBlock = value;
    }

    public void setTrackDataKSN(String value) {
        this.trackDataKSN = value;
    }

    public void setMagnePrint(String value) {
        this.magnePrint = value;
    }

    public void setMagnePrintStatus(String value) {
        this.magnePrintStatus = value;
    }

    public void setDeviceSerialNumber(String value) {
        this.deviceSerialNumber = value;
    }

    public void setEncryptedTrack1(String value) {
        this.encryptedTrack1 = value;
    }

    public void setEncryptedTrack2(String value) {
        this.encryptedTack2 = value;
    }

    public void setCardEncryptedSecCode(String value) {
        this.cardEncryptedSecCode = value;
    }

    public void setEncryptedAESTrack1(String value) {
        this.encryptedAESTrack1 = value;
    }

    public void setEncryptedAESTrack2(String value) {
        this.encryptedAESTrack2 = value;
    }

    public void setOriginalTotalAmount(String value) {
        this.originalTotalAmount = value;
    }

    //	public void setDebitPinNum(String value)
//	{
//		this.debitPinNum = value;
//	}
    public void setDebitPinBlock(String value) {
        this.debitPinBlock = value;
    }

    public void setDebitPinSerialNum(String value) {
        this.debitPinSerialNum = value;
    }


    //	public String getDebitPinNum()
//	{
//		return this.debitPinNum;
//	}
    public String getDebitPinBlock() {
        return this.debitPinBlock;
    }

    public String getDebitPinSerialNum() {
        return this.debitPinSerialNum;
    }

    public String getOriginalTotalAmount() {
        return this.originalTotalAmount;
    }

    public String getRedeemType() {
        return this.redeem_type;
    }

    public String getRedeemAll() {
        return this.redeemAll;
    }

    public String getEncryptedAESTrack1() {
        return this.encryptedAESTrack1;
    }

    public String getEncryptedAESTrack2() {
        return this.encryptedAESTrack2;
    }

    public String getCardEncryptedSecCode() {
        return this.cardEncryptedSecCode;
    }

    public String getCardType() {
        return this.cardType;
    }

    public String getCardOwnerName() {
        return this.cardOwnerName;
    }

    public String getCardExpMonth() {
        if (this.cardExpMonth.isEmpty())
            return "01";
        else {
            try {
                Integer.parseInt(this.cardExpMonth);
            } catch (Exception e) {
                this.cardExpMonth = String.valueOf(new Date().getMonth() + 1);
            }
        }
        return this.cardExpMonth;
    }

    public String getCardExpYear() {
        if (this.cardExpYear.isEmpty()) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy", Locale.getDefault());
            Date date1;
            try {
                String now = df.format(new Date());
                date1 = df.parse(now);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date1);
                cal.add(Calendar.YEAR, 1); // add 28 days

                String t = Integer.toString(cal.get(Calendar.YEAR));
                return t;
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                return "";
            }
        } else if (this.cardExpYear.length() == 2) {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy", Locale.getDefault());
            SimpleDateFormat dt2 = new SimpleDateFormat("yy", Locale.getDefault());
            dt2.set2DigitYearStart(new Date());
            Calendar cal = dt2.getCalendar();
            cal.setTime(new Date());
            cal.setTime(new Date());
            String formatedYear;
            try {
                String milenium = String.valueOf(cal.get(Calendar.YEAR)).substring(0, 2);
                Date date = dt2.parse(milenium + this.cardExpYear);
                formatedYear = dt.format(date);
                return formatedYear;
            } catch (ParseException e) {
                return "";
            }
        }
        return this.cardExpYear;
    }

    public String getCardLast4() {
        return this.cardLast4;
    }

    public String getCardNumAESEncrypted() {
        return this.cardNumAESEncrypted;
    }

    public String getCardNumUnencrypted() {
        return this.cardNumUnencrypted;
    }

    public String getEncryptedBlock() {
        return this.encryptedBlock;
    }

    public String getTrackDataKSN() {
        return this.trackDataKSN;
    }

    public String getMagnePrint() {
        return this.magnePrint;
    }

    public String getMagnePrintStatus() {
        return this.magnePrintStatus;
    }

    public String getDeviceSerialNumber() {
        return this.deviceSerialNumber;
    }

    public String getEncryptedTrack1() {
        return this.encryptedTrack1;
    }

    public String getEncryptedTrack2() {
        return this.encryptedTack2;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public EMVContainer getEmvContainer() {
        return emvContainer;
    }

    public void setEmvContainer(EMVContainer emvContainer) {
        this.emvContainer = emvContainer;
    }
}

