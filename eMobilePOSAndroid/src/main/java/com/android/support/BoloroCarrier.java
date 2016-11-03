package com.android.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BoloroCarrier {

	private String empstr = "";
	private String telco_id = empstr;
	private String telco_name = empstr;
	private List<HashMap<String,String>>carrierAccounts = new ArrayList<HashMap<String,String>>();
	private List<String> listAccountName = new ArrayList<String>();
	
	
	public void setTelcoID(String val)
	{
		this.telco_id = val;
	}
	
	public String getTelcoID()
	{
		return this.telco_id;
	}
	
	public void setTelcoName(String val)
	{
		this.telco_name = val;
	}
	
	public String getTelcoName()
	{
		return this.telco_name;
	}
	
	public void addCarrierAccounts(HashMap<String,String>map)
	{
		if(Boolean.parseBoolean(map.get("is_default")))
		{
			carrierAccounts.add(0,map);
			listAccountName.add(0,map.get("payment_mode"));
		}
		else
		{
			carrierAccounts.add(map);
			listAccountName.add(map.get("payment_mode"));
		}
	}
	public List<HashMap<String,String>> getCarrierAccounts()
	{
		return carrierAccounts;
	}
	public List<String>getCarrierAccountsName()
	{
		return this.listAccountName;
	}
	
	
}
