package com.android.emobilepos.models;

import java.util.UUID;


public class DataTaxes {
	

	private String ord_tax_id = "";
	private String ord_id = "";
	private String tax_name = "";
	private String tax_amount = "";
	private String tax_rate = "";
	
	
	public DataTaxes ()
	{
		setOrd_tax_id(UUID.randomUUID().toString());
	}

	public String getOrd_tax_id() {
		return ord_tax_id;
	}

	public void setOrd_tax_id(String ord_tax_id) {
		this.ord_tax_id = ord_tax_id;
	}

	public String getOrd_id() {
		return ord_id;
	}

	public void setOrd_id(String ord_id) {
		this.ord_id = ord_id;
	}

	public String getTax_name() {
		return tax_name;
	}

	public void setTax_name(String tax_name) {
		this.tax_name = tax_name;
	}

	public String getTax_amount() {
		return tax_amount;
	}

	public void setTax_amount(String tax_amount) {
		this.tax_amount = tax_amount;
	}

	public String getTax_rate() {
		return tax_rate;
	}

	public void setTax_rate(String tax_rate) {
		this.tax_rate = tax_rate;
	}

//	public enum Limiters
//	{
//		ord_tax_id,ord_id,tax_name,tax_amount,tax_rate;
//
//		public static Limiters getValue(String str) {
//			try {
//				return valueOf(str);
//			} catch (Exception ex) {
//				return null;
//			}
//		}
//	}
//
//	public void set(String attr, String value) {
//		Limiters _type = Limiters.getValue(attr);
//
//		if (_type != null)
//		{
//			switch (_type)
//			{
//			case ord_tax_id:
//				this.setOrd_tax_id(value);
//				break;
//			case ord_id:
//				this.setOrd_id(value);
//				break;
//			case tax_name:
//				this.setTax_name(value);
//				break;
//			case tax_amount:
//				if(value==null||value.isEmpty())
//					value = "0";
//				this.setTax_amount(value);
//				break;
//			case tax_rate:
//				if(value==null||value.isEmpty())
//					value = "0";
//				this.setTax_rate(value);
//				break;
//			}
//		}
//	}
//
//	public String get(String attr)
//	{
//		Limiters _type = Limiters.getValue(attr);
//		String value = empStr;
//		if(_type != null)
//		{
//			switch(_type)
//			{
//			case ord_tax_id:
//				value = this.getOrd_tax_id();
//				break;
//			case ord_id:
//				value = this.getOrd_id();
//				break;
//			case tax_name:
//				value = this.getTax_name();
//				break;
//			case tax_amount:
//				value = this.getTax_amount();
//				break;
//			case tax_rate:
//				value = this.getTax_rate();
//				break;
//			}
//		}
//		return value;
//	}
}
