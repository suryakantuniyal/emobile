package com.android.support;


import java.util.Date;

public class CustomerInventory
{
	private final String empt = "";
	
	public String consignment_id = empt;
	public String cust_id = empt;
	public String prod_id = empt;
	public String qty = empt;
	public String price = empt;
	public String prod_name = empt;
	public String cust_update;
	public String is_synched = "0";
	
	
	public CustomerInventory()
	{
		cust_update = DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss);
	}
}
