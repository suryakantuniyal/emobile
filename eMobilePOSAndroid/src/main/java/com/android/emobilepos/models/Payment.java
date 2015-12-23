package com.android.emobilepos.models;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.android.database.DrawInfoHandler;
import com.android.emobilepos.models.genius.GeniusResponse;
import com.android.ivu.MersenneTwisterFast;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.google.gson.annotations.Expose;

public class Payment {


    private static final long serialVersionUID = 1L;

    private String empstr = "";

    public String pay_id = empstr;
    public String group_pay_id = empstr;
    public String original_pay_id = empstr;
    public String tupyx_user_id = empstr;
    public String cust_id = empstr;
    public String custidkey = empstr;
    public String emp_id = empstr;
    public String inv_id = empstr;
    public String paymethod_id = empstr;
    public String pay_check = empstr;
    public String pay_receipt = empstr;
    public String pay_amount = "0.00";
    public String pay_dueamount = "0.00";
    public String originalTotalAmount = empstr;
    public String pay_comment = empstr;
    public String pay_timecreated = empstr;
    public String pay_timesync = empstr;
    public String account_id = empstr;
    public String processed = empstr;
    public String pay_issync = empstr;
    public String pay_transid = empstr;
    public String pay_refnum = empstr;
    public String pay_name = empstr;
    public String pay_addr = empstr;
    public String pay_poscode = empstr;
    public String pay_seccode = empstr;
    public String pay_maccount = empstr;
    public String pay_groupcode = empstr;
    public String pay_stamp = empstr;
    public String pay_resultcode = empstr;
    public String pay_resultmessage = empstr;
    public String pay_ccnum = empstr;
    public String pay_expmonth = empstr;
    public String pay_expyear = empstr;
    public String pay_expdate = empstr;
    public String pay_result = empstr;
    public String pay_date = empstr;
    public String recordnumber = empstr;
    public String pay_signature = empstr;
    public String authcode = empstr;
    public String status = empstr;
    public String job_id = empstr;
    public String user_ID = empstr;
    public String pay_type = empstr;
    public String pay_tip = "0.00";
    public String ccnum_last4 = empstr;
    public String pay_phone = empstr;
    public String pay_email = empstr;
    public String isVoid = empstr;

    public String tipAmount = empstr;
    public String clerk_id = empstr;
    public String pay_latitude = empstr;
    public String pay_longitude = empstr;
    public String IvuLottoDrawDate = empstr;
    public String IvuLottoNumber = empstr;
    public String IvuLottoQR = empstr;

    public String Tax1_amount = empstr;
    public String Tax1_name = empstr;
    public String Tax2_amount = empstr;
    public String Tax2_name = empstr;

    public String track_one = empstr;
    public String track_two = empstr;
    public String is_refund = "0";
    public String ref_num = empstr;
    public String card_type = empstr;

    public String check_account_number = empstr;
    public String check_routing_number = empstr;
    public String check_check_number = empstr;
    public String check_check_type = empstr;
    public String check_account_type = empstr;
    public String check_name = empstr;
    public String check_city = empstr;
    public String check_state = empstr;
    public String dl_number = empstr;
    public String dl_state = empstr;
    public String dl_dob = empstr;

    // Check Capture
    public String frontImage = empstr;
    public String backImage = empstr;
    public String micrData = empstr;

    // For Boloro
    public String telcoid = empstr;
    public String transmode = empstr;
    public String tagid = empstr;

    // Store & Forward
    public String pay_uuid = empstr;
    public String is_retry = "0";
    public String payment_xml = "";


    public EMVContainer emvContainer;

    public Payment(Activity activity) {
        MyPreferences myPref = new MyPreferences(activity);

        pay_issync = "0";
        isVoid = "0";
        status = "1";

        String date = Global.getCurrentDate();
        pay_timecreated = date;
        pay_date = date;

        emp_id = myPref.getEmpID();

    }

    public Payment(Activity activity, String paymentId, String customerId, String invoiceId, String jobId, String clerkId,
                   String custidkey, String paymentMethod, Double actualAmount, Double amountToBePaid,
                   String customerName, String referenceNumber, String phoneNumber, String email, Double tipAmount,
                   String taxAmount1, String taxAmount2, String taxName1, String taxName2,
                   String isRefund, String paymentType, String creditCardType, String cardNumberEnc, String cardNumberLast4,
                   String cardExpMonth, String cardExpYear, String cardPostalCode, String cardSecurityCode, String trackOne,
                   String trackTwo, String transactionId, String authcode) {

        MyPreferences myPref = new MyPreferences(activity);
        pay_issync = "0";
        isVoid = "0";
        status = "1";
        pay_transid = transactionId;
        this.authcode = authcode;
        pay_ccnum = cardNumberEnc;
        ccnum_last4 = cardNumberLast4;
        pay_expmonth = cardExpMonth;
        pay_expyear = cardExpYear;
        pay_poscode = cardPostalCode;
        pay_seccode = cardSecurityCode;

        this.card_type = creditCardType;
        this.track_one = trackOne;
        this.track_two = trackTwo;

        String date = Global.getCurrentDate();
        pay_timecreated = date;
        pay_date = date;

        emp_id = myPref.getEmpID();

        pay_id = paymentId;
        emp_id = myPref.getEmpID();
        cust_id = customerId;
        job_id = jobId;
        inv_id = invoiceId;
        clerk_id = clerkId;
        custidkey = custidkey;
        paymethod_id = paymentMethod;
        pay_dueamount = Double.toString(actualAmount - amountToBePaid);
        if (amountToBePaid > actualAmount)
            pay_amount = Double.toString(actualAmount);
        else
            pay_amount = Double.toString(amountToBePaid);
        pay_name = customerName;
        processed = "1";
        ref_num = referenceNumber;
        pay_phone = phoneNumber;
        pay_email = email;
        pay_tip = String.valueOf(tipAmount);
        if (Global.isIvuLoto) {
            DrawInfoHandler drawDateInfo = new DrawInfoHandler(activity);
            MersenneTwisterFast mersenneTwister = new MersenneTwisterFast();
            String drawDate = drawDateInfo.getDrawDate();
            String ivuLottoNum = mersenneTwister.generateIVULoto();
            IvuLottoNumber = ivuLottoNum;
            IvuLottoDrawDate = drawDate;
            IvuLottoQR =
                    Global.base64QRCode(ivuLottoNum, drawDate);
            if (!TextUtils.isEmpty(taxAmount1)) {
                Tax1_amount = taxAmount1;
                Tax1_name = taxName1;
                Tax2_amount = taxAmount2;
                Tax2_name = taxName2;
            }
        }
        String[] location = Global.getCurrLocation(activity);
        pay_latitude = location[0];
        pay_longitude = location[1];
        is_refund = isRefund;
        pay_type = paymentType;

    }

}
