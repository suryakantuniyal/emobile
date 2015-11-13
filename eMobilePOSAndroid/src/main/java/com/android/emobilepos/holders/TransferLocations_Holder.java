package com.android.emobilepos.holders;

import android.app.Activity;

import com.android.support.Global;
import com.android.support.MyPreferences;

public class TransferLocations_Holder {
	private String empStr = "";
	private String trans_id = empStr;
	private String loc_key_from = empStr;
	private String loc_key_to = empStr;
	private String emp_id = empStr;
	private String trans_timecreated = empStr;
	
	public TransferLocations_Holder(Activity activity)
	{
		MyPreferences myPref = new MyPreferences(activity);
		trans_timecreated = Global.getCurrentDate();
		emp_id = myPref.getEmpID();
	}
	
	public enum Limiters {
		trans_id,loc_key_from,loc_key_to,emp_id,trans_timecreated;

		public static Limiters toLimit(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return null;
			}
		}
	}

	public String get(String key) {
		Limiters type = Limiters.toLimit(key);

		String returningVal = empStr;
		if (type != null) {
			switch (type) {
			case trans_id:
				returningVal = trans_id;
				break;
			case loc_key_from:
				returningVal = loc_key_from;
				break;
			case loc_key_to:
				returningVal = loc_key_to;
				break;
			case emp_id:
				returningVal = emp_id;
				break;
			case trans_timecreated:
				returningVal = trans_timecreated;
				break;
			}
		}
		else
			return returningVal;
		
		return returningVal==null?empStr:returningVal;
	}
	
	public void set(String key,String value)
	{
		Limiters type = Limiters.toLimit(key);

		String _value = value==null?empStr:value;
		if (type != null) {
			switch (type) {
			case trans_id:
				trans_id = _value;
				break;
			case loc_key_from:
				loc_key_from = _value;
				break;
			case loc_key_to:
				loc_key_to = _value;
				break;
			case emp_id:
				emp_id = _value;
				break;
			case trans_timecreated:
				trans_timecreated = _value;
				break;
			}
		}
	}
}
