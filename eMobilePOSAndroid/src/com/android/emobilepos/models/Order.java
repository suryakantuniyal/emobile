package com.android.emobilepos.models;

import com.android.support.Customer;
import com.android.support.Global;
import com.android.support.MyPreferences;

import android.app.Activity;

public class Order {
	private String emp = "";
	public String ord_id = emp;
	public String qbord_id = emp;
	public String emp_id = emp;
	public String cust_id = emp;
	public String clerk_id = emp;
	public String c_email = emp;
	public String ord_signature = emp;
	public String ord_po = emp;
	public String total_lines = emp;
	public String total_lines_pay = "0";
	public String ord_total = emp;
	public String ord_comment = emp;
	public String ord_delivery = emp;
	public String ord_timecreated = emp;
	public String ord_timesync = emp;
	public String qb_synctime = emp;
	public String emailed = emp;
	public String processed = emp;
	public String ord_type = emp;
	public String ord_type_name = emp;
	public String ord_claimnumber = emp;
	public String ord_rganumber = emp;
	public String ord_returns_pu = emp;
	public String ord_inventory = emp;
	public String ord_issync = emp;
	public String tax_id = emp;
	public String ord_shipvia = emp;
	public String ord_shipto = emp;
	public String ord_terms = emp;
	public String ord_custmsg = emp;
	public String ord_class = emp;
	public String ord_subtotal = emp;
	public String ord_lineItemDiscount = emp;
	public String ord_globalDiscount = emp;
	public String ord_taxamount = emp;
	public String ord_discount = emp;
	public String ord_discount_id = emp;
	public String ord_latitude = emp;
	public String ord_longitude = emp;
	public String tipAmount = emp;
	public String custidkey = emp;
	public String isOnHold = "0"; // 0 - not on hold, 1 - on hold
	public String ord_HoldName = emp;
	public String is_stored_fwd = "0";
	public String VAT = "0";

	public String isVoid = emp;
	
	public String gran_total = emp;
	public String cust_name = emp;
	public String sync_id = emp;
	public Customer customer;
	

	//private Global global;
	private MyPreferences myPref;

	public Order(Activity activity) {
		myPref = new MyPreferences(activity);

		ord_issync = "0";
		isVoid = "0";
		processed = "0"; //need to be 1 when order has been processed or 9 if voided
		
		ord_timecreated = Global.getCurrentDate();
		emp_id = myPref.getEmpID();
		custidkey = myPref.getCustIDKey();
	}

//	public enum Limiters {
//		ord_id, qbord_id, emp_id, cust_id, clerk_id, c_email, ord_signature, ord_po, total_lines, total_lines_pay, ord_total, ord_comment, ord_delivery, ord_timecreated, ord_timesync, qb_synctime, emailed, processed, ord_type, ord_claimnumber, ord_rganumber, ord_returns_pu, ord_inventory, ord_issync, tax_id, ord_shipvia, ord_shipto, ord_terms, ord_custmsg, ord_class, ord_subtotal, ord_taxamount, ord_discount, ord_discount_id, ord_latitude, ord_longitude, tipAmount,
//
//		isVoid, gran_total,cust_name,custidkey,isOnHold,ord_HoldName, is_stored_fwd,VAT;
//
//		public static Limiters toLimit(String str) {
//			try {
//				return valueOf(str);
//			} catch (Exception ex) {
//				return null;
//			}
//		}
//	}
//
//	public String getSetData(String attribute, boolean get, String value) {
//		Limiters test = Limiters.toLimit(attribute);
//		String returnedVal = emp;
//		if(value==null)
//			value = emp;
//		
//		if (test != null) {
//			switch (test) {
//			case ord_id: {
//				if (get)
//					returnedVal = this.ord_id;
//				else
//					this.ord_id = value;
//				break;
//			}
//
//			case qbord_id: {
//				if (get)
//					returnedVal = this.qbord_id;
//				else
//					this.qbord_id = value;
//				break;
//			}
//
//			case emp_id: {
//				if (get)
//					returnedVal = this.emp_id;
//				else
//					this.emp_id = value;
//				break;
//			}
//
//			case cust_id: {
//				if (get)
//					returnedVal = this.cust_id;
//				else
//					this.cust_id = value;
//				break;
//			}
//
//			case clerk_id: {
//				if (get)
//					returnedVal = this.clerk_id;
//				else
//					this.clerk_id = value;
//				break;
//			}
//
//			case c_email: {
//				if (get)
//					returnedVal = this.c_email;
//				else
//					this.c_email = value;
//				break;
//			}
//
//			case ord_signature: {
//				if (get)
//					returnedVal = this.ord_signature;
//				else
//					this.ord_signature = value;
//				break;
//			}
//
//			case ord_po: {
//				if (get)
//					returnedVal = this.ord_po;
//				else
//					this.ord_po = value;
//				break;
//			}
//
//			case total_lines: {
//				if (get)
//					returnedVal = this.total_lines;
//				else
//					this.total_lines = value;
//				break;
//			}
//
//			case total_lines_pay: {
//				if (get)
//					returnedVal = this.total_lines_pay;
//				else
//					this.total_lines_pay = value;
//				break;
//			}
//
//			case ord_total: {
//				if (get)
//					returnedVal = this.ord_total;
//				else
//					this.ord_total = value;
//				break;
//			}
//
//			case ord_comment: {
//				if (get)
//					returnedVal = this.ord_comment;
//				else
//					this.ord_comment = value;
//				break;
//			}
//
//			case ord_delivery: {
//				if (get)
//					returnedVal = this.ord_delivery;
//				else
//					this.ord_delivery = value;
//				break;
//			}
//
//			case ord_timecreated: {
//				if (get)
//					returnedVal = this.ord_timecreated;
//				else
//					this.ord_timecreated = value;
//				break;
//			}
//
//			case ord_timesync: {
//				if (get)
//					returnedVal = this.ord_timesync;
//				else
//					this.ord_timesync = value;
//				break;
//			}
//
//			case qb_synctime: {
//				if (get)
//					returnedVal = this.qb_synctime;
//				else
//					this.qb_synctime = value;
//				break;
//			}
//
//			case emailed: {
//				if (get)
//					returnedVal = this.emailed;
//				else
//					this.emailed = value;
//				break;
//			}
//
//			case processed: {
//				if (get)
//					returnedVal = this.processed;
//				else
//					this.processed = value;
//				break;
//			}
//
//			case ord_type: {
//				if (get)
//					returnedVal = this.ord_type;
//				else
//					this.ord_type = value;
//				break;
//			}
//
//			case ord_claimnumber: {
//				if (get)
//					returnedVal = this.ord_claimnumber;
//				else
//					this.ord_claimnumber = value;
//				break;
//			}
//
//			case ord_rganumber: {
//				if (get)
//					returnedVal = this.ord_rganumber;
//				else
//					this.ord_rganumber = value;
//				break;
//			}
//
//			case ord_returns_pu: {
//				if (get)
//					returnedVal = this.ord_returns_pu;
//				else
//					this.ord_returns_pu = value;
//				break;
//			}
//
//			case ord_inventory: {
//				if (get)
//					returnedVal = this.ord_inventory;
//				else
//					this.ord_inventory = value;
//				break;
//			}
//
//			case ord_issync: {
//				if (get)
//					returnedVal = this.ord_issync;
//				else
//					this.ord_issync = value;
//				break;
//			}
//
//			case tax_id: {
//				if (get)
//					returnedVal = this.tax_id;
//				else
//					this.tax_id = value;
//				break;
//			}
//
//			case ord_shipvia: {
//				if (get)
//					returnedVal = this.ord_shipvia;
//				else
//					this.ord_shipvia = value;
//				break;
//			}
//
//			case ord_shipto: {
//				if (get)
//					returnedVal = this.ord_shipto;
//				else
//					this.ord_shipto = value;
//				break;
//			}
//
//			case ord_terms: {
//				if (get)
//					returnedVal = this.ord_terms;
//				else
//					this.ord_terms = value;
//				break;
//			}
//
//			case ord_custmsg: {
//				if (get)
//					returnedVal = this.ord_custmsg;
//				else
//					this.ord_custmsg = value;
//				break;
//			}
//
//			case ord_class: {
//				if (get)
//					returnedVal = this.ord_class;
//				else
//					this.ord_class = value;
//				break;
//			}
//
//			case ord_subtotal: {
//				if (get)
//					returnedVal = this.ord_subtotal;
//				else
//					this.ord_subtotal = value;
//				break;
//			}
//			case ord_taxamount: {
//				if (get)
//					returnedVal = this.ord_taxamount;
//				else
//					this.ord_taxamount = value;
//				break;
//			}
//			case ord_discount: {
//				if (get)
//					returnedVal = this.ord_discount;
//				else
//					this.ord_discount = value;
//				break;
//			}
//			case ord_discount_id: {
//				if (get)
//					returnedVal = this.ord_discount_id;
//				else
//					this.ord_discount_id = value;
//				break;
//			}
//			case ord_latitude: {
//				if (get)
//					returnedVal = this.ord_latitude;
//				else
//					this.ord_latitude = value;
//				break;
//			}
//
//			case ord_longitude: {
//				if (get)
//					returnedVal = this.ord_longitude;
//				else
//					this.ord_longitude = value;
//				break;
//			}
//
//			case tipAmount: 
//			{
//				if (get)
//					returnedVal = this.tipAmount;
//				else
//					this.tipAmount = value;
//				break;
//			}
//
//			case isVoid: 
//			{
//				if (get)
//					returnedVal = this.isVoid;
//				else
//					this.isVoid = value;
//				break;
//			}
//			
//			case gran_total:
//			{
//				if(get)
//					returnedVal = this.gran_total;
//				else
//					this.gran_total = value;
//				break;
//			}
//			
//			case cust_name:
//			{
//				if(get)
//					returnedVal = this.cust_name;
//				else
//					this.cust_name = value;
//				break;
//			}
//			
//			case custidkey:
//				if(get)
//					returnedVal = this.custidkey;
//				else
//					this.custidkey = value;
//				break;
//			case isOnHold:
//				if(get)
//					returnedVal = this.isOnHold;
//				else
//					this.isOnHold = value;
//				break;
//			case ord_HoldName:
//				if(get)
//					returnedVal = this.ord_HoldName;
//				else
//					this.ord_HoldName = value;
//				break;
//			case is_stored_fwd:
//				if(get)
//					returnedVal = this.is_stored_fwd;
//				else
//					this.is_stored_fwd = value;
//				break;
//			case VAT:
//				if(get)
//					returnedVal = this.VAT;
//				else
//					this.VAT = value;
//				break;
//			}			
//		}
//		
//		if(returnedVal==null)
//			returnedVal = "";
//		return returnedVal;
//	}
}
