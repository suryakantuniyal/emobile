package com.android.emobilepos.holders;


public class Locations_Holder {
	
	private String empStr = "";
	private String loc_key = empStr;
	private String loc_id = empStr;
	private String loc_name = empStr;
	
	public enum Limiters {
		loc_key,loc_id,loc_name;

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
			case loc_key:
				returningVal = loc_key;
				break;
			case loc_id:
				returningVal = loc_id;
				break;
			case loc_name:
				returningVal = loc_name;
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
			case loc_key:
				loc_key = _value;
				break;
			case loc_id:
				loc_id = _value;
				break;
			case loc_name:
				loc_name = _value;
				break;
			}
		}
	}
}
