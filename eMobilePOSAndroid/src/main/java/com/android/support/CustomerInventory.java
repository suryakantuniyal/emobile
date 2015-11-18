package com.android.support;



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
		cust_update = Global.getCurrentDate();
	}
}
