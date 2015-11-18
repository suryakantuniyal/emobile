package com.android.emobilepos.holders;

public class TransferInventory_Holder {
	private String empStr = "";
	private String trans_id = empStr;
	private String prod_id = empStr;
	private String prod_qty = empStr;
	
	
	public enum Limiters {
		trans_id,prod_id,prod_qty;

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
			case prod_id:
				returningVal = prod_id;
				break;
			case prod_qty:
				returningVal = prod_qty;
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
			case prod_id:
				prod_id = _value;
				break;
			case prod_qty:
				prod_qty = _value;
				break;
			}
		}
	}
}
