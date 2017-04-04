package com.android.support;

import android.app.Activity;

import com.android.dao.AssignEmployeeDAO;
import com.android.emobilepos.models.realms.AssignEmployee;

import java.text.SimpleDateFormat;
import java.util.Date;


public class Invoice {
	private String empstr = "";

	private String inv_id = empstr;
	private String cust_id = empstr;
	private String emp_id = empstr;
	private String inv_timecreated = empstr;
	private String inv_ispending = empstr;
	private String inv_ponumber = empstr;
	private String inv_terms = empstr;
	private String inv_duedate = empstr;
	private String inv_shipdate = empstr;
	private String inv_shipmethod = empstr;
	private String inv_total = empstr;
	private String inv_apptotal = empstr;
	private String inv_balance = empstr;
	private String inv_custmsg = empstr;
	private String inv_ispaid = empstr;
	private String inv_paiddate = empstr;
	private String mod_date = empstr;
	private String txnID = empstr;
	private String inv_update = empstr;

	public Invoice(Activity activity) {
		AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm a");
		inv_timecreated = sdf.format(new Date());

		emp_id = String.valueOf(assignEmployee.getEmpId());
	}

	public enum Limiters {
		inv_id, cust_id, emp_id, inv_timecreated, inv_ispending, inv_ponumber, inv_terms, inv_duedate, inv_shipdate, inv_shipmethod, inv_total, inv_apptotal, inv_balance, inv_custmsg, inv_ispaid, inv_paiddate, mod_date, txnID, inv_update;

		public static Limiters toLimit(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return null;
			}
		}
	}

	public String getSetData(String attribute, boolean get, String value) {
		Limiters test = Limiters.toLimit(attribute);
		String returnedVal = empstr;
		if(value == null)
			value = empstr;
		if (test != null) {
			switch (test) {
			case inv_id: {
				if (get)
					returnedVal = this.inv_id;
				else
					this.inv_id = value;
				break;
			}
			case cust_id: {
				if (get)
					returnedVal = this.cust_id;
				else
					this.cust_id = value;
				break;
			}
			case emp_id: {
				if (get)
					returnedVal = this.emp_id;
				else
					this.emp_id = value;
				break;
			}
			case inv_timecreated: {
				if (get)
					returnedVal = this.inv_timecreated;
				else
					this.inv_timecreated = value;
				break;
			}
			case inv_ispending: {
				if (get)
					returnedVal = this.inv_ispending;
				else
					this.inv_ispending = value;
				break;
			}
			case inv_ponumber: {
				if (get)
					returnedVal = this.inv_ponumber;
				else
					this.inv_ponumber = value;
				break;
			}
			case inv_terms: {
				if (get)
					returnedVal = this.inv_terms;
				else
					this.inv_terms = value;
				break;
			}
			case inv_duedate: {
				if (get)
					returnedVal = this.inv_duedate;
				else
					this.inv_duedate = value;
				break;
			}
			case inv_shipdate: {
				if (get)
					returnedVal = this.inv_shipdate;
				else
					this.inv_shipdate = value;
				break;
			}
			case inv_shipmethod: {
				if (get)
					returnedVal = this.inv_shipmethod;
				else
					this.inv_shipmethod = value;
				break;
			}
			case inv_total: {
				if (get)
					returnedVal = this.inv_total;
				else
					this.inv_total = value;
				break;
			}
			case inv_apptotal: {
				if (get)
					returnedVal = this.inv_apptotal;
				else
					this.inv_apptotal = value;
				break;
			}
			case inv_balance: {
				if (get)
					returnedVal = this.inv_balance;
				else
					this.inv_balance = value;
				break;
			}
			case inv_custmsg: {
				if (get)
					returnedVal = this.inv_custmsg;
				else
					this.inv_custmsg = value;
				break;
			}
			case inv_ispaid: {
				if (get)
					returnedVal = this.inv_ispaid;
				else
					this.inv_ispaid = value;
				break;
			}
			case inv_paiddate: {
				if (get)
					returnedVal = this.inv_paiddate;
				else
					this.inv_paiddate = value;
				break;
			}
			case mod_date: {
				if (get)
					returnedVal = this.mod_date;
				else
					this.mod_date = value;
				break;
			}
			case txnID: {
				if (get)
					returnedVal = this.txnID;
				else
					this.txnID = value;
				break;
			}
			case inv_update: {
				if (get)
					returnedVal = this.inv_update;
				else
					this.inv_update = value;
				break;
			}

			}
		}
		return returnedVal;
	}
}
