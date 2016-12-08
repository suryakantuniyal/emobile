package com.android.emobilepos.holders;

import android.app.Activity;

import com.android.dao.AssignEmployeeDAO;
import com.android.support.DateUtils;
import com.android.support.MyPreferences;

import java.util.Date;

public class TransferLocations_Holder {
	private String trans_id = "";
	private String loc_key_from = "";
	private String loc_key_to = "";
	private String emp_id = "";
	private String trans_timecreated = "";
	
	public TransferLocations_Holder(Activity activity)
	{
//		MyPreferences myPref = new MyPreferences(activity);
		setTrans_timecreated(DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss));
		setEmp_id(String.valueOf(AssignEmployeeDAO.getAssignEmployee().getEmpId()));
	}

	public String getTrans_id() {
		return trans_id;
	}

	public void setTrans_id(String trans_id) {
		this.trans_id = trans_id;
	}

	public String getLoc_key_from() {
		return loc_key_from;
	}

	public void setLoc_key_from(String loc_key_from) {
		this.loc_key_from = loc_key_from;
	}

	public String getLoc_key_to() {
		return loc_key_to;
	}

	public void setLoc_key_to(String loc_key_to) {
		this.loc_key_to = loc_key_to;
	}

	public String getEmp_id() {
		return emp_id;
	}

	public void setEmp_id(String emp_id) {
		this.emp_id = emp_id;
	}

	public String getTrans_timecreated() {
		return trans_timecreated;
	}

	public void setTrans_timecreated(String trans_timecreated) {
		this.trans_timecreated = trans_timecreated;
	}
}
