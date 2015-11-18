package com.android.emobilepos.models;

import java.util.UUID;


public class DataTaxes {
	
	private String empStr = "";
	
	private String ord_tax_id = empStr;
	private String ord_id = empStr;
	private String tax_name = empStr;
	private String tax_amount = empStr;
	private String tax_rate = empStr;
	
	
	public DataTaxes ()
	{
		ord_tax_id = UUID.randomUUID().toString();
	}
	
	public enum Limiters 
	{
		ord_tax_id,ord_id,tax_name,tax_amount,tax_rate;

		public static Limiters getValue(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return null;
			}
		}
	}
	
	public void set(String attr, String value) {
		Limiters _type = Limiters.getValue(attr);

		if (_type != null) 
		{
			switch (_type) 
			{
			case ord_tax_id:
				this.ord_tax_id = value;
				break;
			case ord_id:
				this.ord_id = value;
				break;
			case tax_name:
				this.tax_name = value;
				break;
			case tax_amount:
				if(value==null||value.isEmpty())
					value = "0";
				this.tax_amount = value;
				break;
			case tax_rate:
				if(value==null||value.isEmpty())
					value = "0";
				this.tax_rate = value;
				break;
			}
		}
	}
	
	public String get(String attr)
	{
		Limiters _type = Limiters.getValue(attr);
		String value = empStr;
		if(_type != null)
		{
			switch(_type)
			{
			case ord_tax_id:
				value = this.ord_tax_id;
				break;
			case ord_id:
				value = this.ord_id;
				break;
			case tax_name:
				value = this.tax_name;
				break;
			case tax_amount:
				value = this.tax_amount;
				break;
			case tax_rate:
				value = this.tax_rate;
				break;
			}
		}		
		return value;
	}
}
