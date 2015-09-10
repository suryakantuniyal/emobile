package com.android.support;


public class CheckHolder 
{
	private String empStr = "";
	private String check_name = empStr;
	private String check_email = empStr;
	private String check_phone = empStr;
	private String check_amount = empStr;
	private String check_amount_paid = empStr;
	private String check_invoice = empStr;
	private String check_acct = empStr;
	private String check_routing = empStr;
	private String check_number = empStr;
	private String check_comment = empStr;
	
	private enum Limiters {
		check_name,check_email,check_phone,check_amount,check_amount_paid,check_invoice,check_acct,check_routing,check_numer,check_comment;

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
		String returnedVal = "";
		if (test != null) {
			switch (test) {
			case check_name:
				if (get)
					returnedVal = this.check_name;
				else
					this.check_name = value;
				break;
			case check_email:
				if(get)
					returnedVal = this.check_email;
				else
					this.check_email = value;
				break;
			case check_phone:
				if(get)
					returnedVal = this.check_phone;
				else
					this.check_phone = value;
				break;
			case check_amount:
				if(get)
					returnedVal = this.check_amount;
				else
					this.check_amount = value;
				break;
			case check_amount_paid:
				if(get)
					returnedVal = this.check_amount_paid;
				else
					this.check_amount_paid = value;
				break;
			case check_invoice:
				if(get)
					returnedVal = this.check_invoice;
				else
					this.check_invoice = value;
				break;
			case check_acct:
				if(get)
					returnedVal = this.check_acct;
				else
					this.check_acct = value;
				break;
			case check_routing:
				if(get)
					returnedVal = this.check_routing;
				else
					this.check_routing = value;
				break;
			case check_numer:
				if(get)
					returnedVal = this.check_number;
				else
					this.check_number = value;
				break;
			case check_comment:
				if(get)
					returnedVal = this.check_comment;
				else
					this.check_comment = value;
				break;
			}
		}
		if(returnedVal==null)
			returnedVal = "";
		return returnedVal;
	}
	
	
	
	
}
