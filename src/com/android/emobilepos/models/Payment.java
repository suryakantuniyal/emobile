package com.android.emobilepos.models;

import java.io.Serializable;

import com.android.support.Global;
import com.android.support.MyPreferences;
import com.google.gson.Gson;
import com.google.gson.annotations.JsonAdapter;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;

public class Payment {

	/**
	 * 
	 */
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

	public String originalTotalAmount = empstr;
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
	
	
	public MyPreferences myPref;

	public Payment(Activity activity) {
		myPref = new MyPreferences(activity);

		pay_issync = "0";
		isVoid = "0";
		status = "1";

		String date = Global.getCurrentDate();
		pay_timecreated = date;
		pay_date = date;

		emp_id = myPref.getEmpID();

	}
}
