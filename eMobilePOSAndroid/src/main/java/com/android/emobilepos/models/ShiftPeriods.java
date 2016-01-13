package com.android.emobilepos.models;

import com.android.support.Global;

import java.util.UUID;


public class ShiftPeriods 
{
	private String empStr = "";
	
	public String shift_id = empStr;
	public String assignee_id = empStr;
	public String assignee_name = empStr;
	public String creationDate = empStr;
	public String creationDateLocal = empStr;
	public String startTime = empStr;
	public String startTimeLocal = empStr;
	public String endTime = empStr;
	public String endTimeLocal = empStr;
	public String beginning_petty_cash = "0";
	public String ending_petty_cash = "0";
	public String entered_close_amount = "0";
	public String total_transaction_cash = "0";
	public String shift_issync = "0";
	public String total_expenses = "0";
	public String total_ending_cash = "0";
	public String over_short = "0";
	
	public ShiftPeriods(boolean isOpen)
	{
		shift_id = UUID.randomUUID().toString();
		if(isOpen)
		{
			startTime = Global.getCurrentDate();
			startTimeLocal = startTime;
			creationDate = startTime;
			creationDateLocal = startTime;
		}
		else
		{
			endTime = Global.getCurrentDate();
			endTimeLocal = endTime;
		}
	}
}
