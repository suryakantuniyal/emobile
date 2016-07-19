package com.android.emobilepos.models;

import android.app.Activity;
import android.location.Location;
import android.text.TextUtils;

import com.android.database.DrawInfoHandler;
import com.android.ivu.MersenneTwisterFast;
import com.android.support.Global;
import com.android.support.MyPreferences;

public class Payment {


    private static final long serialVersionUID = 1L;

    public String pay_id = "";
    public String group_pay_id = "";
    public String original_pay_id = "";
    public String tupyx_user_id = "";
    public String cust_id = "";
    public String custidkey = "";
    public String emp_id = "";
    public String inv_id = "";
    public String paymethod_id = "";
    public String pay_check = "";
    public String pay_receipt = "";
    public String pay_amount = "0.00";
    public String pay_dueamount = "0.00";
    public Double amountTender = 0.00;

    public String originalTotalAmount = "";
    public String pay_comment = "";
    public String pay_timecreated = "";
    public String pay_timesync = "";
    public String account_id = "";
    public String processed = "";
    public String pay_issync = "";
    public String pay_transid = "";
    public String pay_refnum = "";
    public String pay_name = "";
    public String pay_addr = "";
    public String pay_poscode = "";
    public String pay_seccode = "";
    public String pay_maccount = "";
    public String pay_groupcode = "";
    public String pay_stamp = "";
    public String pay_resultcode = "";
    public String pay_resultmessage = "";
    public String pay_ccnum = "";
    public String pay_expmonth = "";
    public String pay_expyear = "";
    public String pay_expdate = "";
    public String pay_result = "";
    public String pay_date = "";
    public String recordnumber = "";
    public String pay_signature = "";
    public String authcode = "";
    public String status = "";
    public String job_id = "";
    public String user_ID = "";
    public String pay_type = "";
    public String pay_tip = "0.00";
    public String ccnum_last4 = "";
    public String pay_phone = "";
    public String pay_email = "";
    public String isVoid = "";

    public String tipAmount = "";
    public String clerk_id = "";
    public String pay_latitude = "";
    public String pay_longitude = "";
    public String IvuLottoDrawDate = "";
    public String IvuLottoNumber = "";
    public String IvuLottoQR = "";

    public String Tax1_amount = "";
    public String Tax1_name = "";
    public String Tax2_amount = "";
    public String Tax2_name = "";

    public String track_one = "";
    public String track_two = "";
    public String is_refund = "0";
    public String ref_num = "";
    public String card_type = "";

    public String check_account_number = "";
    public String check_routing_number = "";
    public String check_check_number = "";
    public String check_check_type = "";
    public String check_account_type = "";
    public String check_name = "";
    public String check_city = "";
    public String check_state = "";
    public String dl_number = "";
    public String dl_state = "";
    public String dl_dob = "";

    // Check Capture
    public String frontImage = "";
    public String backImage = "";
    public String micrData = "";

    // For Boloro
    public String telcoid = "";
    public String transmode = "";
    public String tagid = "";

    // Store & Forward
    public String pay_uuid = "";
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
        Location currLocation = Global.getCurrLocation(activity, false);
        pay_latitude = String.valueOf(currLocation.getLatitude());
        pay_longitude = String.valueOf(currLocation.getLongitude());

    }

    public Payment(Activity activity, String paymentId, String customerId, String invoiceId, String jobId, String clerkId,
                   String custidkey, String paymentMethod, Double actualAmount, Double amountTender,
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
        this.amountTender = amountTender;
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
        pay_dueamount = Double.toString(actualAmount - amountTender);
        if (amountTender > actualAmount)
            pay_amount = Double.toString(actualAmount);
        else
            pay_amount = Double.toString(amountTender);
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
        Location currLocation = Global.getCurrLocation(activity, false);
        pay_latitude = String.valueOf(currLocation.getLatitude());
        pay_longitude = String.valueOf(currLocation.getLongitude());
        is_refund = isRefund;
        pay_type = paymentType;

    }

}
