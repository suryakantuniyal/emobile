package com.android.emobilepos.models;



public class Address 
{
	private String addr_id = "";
	private String cust_id = "";
	private String addr_b_str1 = "";
	private String addr_b_str2 = "";
	private String addr_b_str3 = "";
	private String addr_b_city = "";
	private String addr_b_state = "";
	private String addr_b_country = "";
	private String addr_b_zipcode = "";
	private String addr_s_name = "";
	private String addr_s_str1 = "";
	private String addr_s_str2 = "";
	private String addr_s_str3 = "";
	private String addr_s_city = "";
	private String addr_s_state = "";
	private String addr_s_country = "";
	private String addr_s_zipcode = "";
	private String qb_cust_id = "";
	private String addr_b_type = "";
	private String addr_s_type = "";

	public String getAddr_id() {
		return addr_id;
	}

	public void setAddr_id(String addr_id) {
		this.addr_id = addr_id;
	}

	public String getCust_id() {
		return cust_id;
	}

	public void setCust_id(String cust_id) {
		this.cust_id = cust_id;
	}

	public String getAddr_b_str1() {
		return addr_b_str1;
	}

	public void setAddr_b_str1(String addr_b_str1) {
		this.addr_b_str1 = addr_b_str1;
	}

	public String getAddr_b_str2() {
		return addr_b_str2;
	}

	public void setAddr_b_str2(String addr_b_str2) {
		this.addr_b_str2 = addr_b_str2;
	}

	public String getAddr_b_str3() {
		return addr_b_str3;
	}

	public void setAddr_b_str3(String addr_b_str3) {
		this.addr_b_str3 = addr_b_str3;
	}

	public String getAddr_b_city() {
		return addr_b_city;
	}

	public void setAddr_b_city(String addr_b_city) {
		this.addr_b_city = addr_b_city;
	}

	public String getAddr_b_state() {
		return addr_b_state;
	}

	public void setAddr_b_state(String addr_b_state) {
		this.addr_b_state = addr_b_state;
	}

	public String getAddr_b_country() {
		return addr_b_country;
	}

	public void setAddr_b_country(String addr_b_country) {
		this.addr_b_country = addr_b_country;
	}

	public String getAddr_b_zipcode() {
		return addr_b_zipcode;
	}

	public void setAddr_b_zipcode(String addr_b_zipcode) {
		this.addr_b_zipcode = addr_b_zipcode;
	}

	public String getAddr_s_name() {
		return addr_s_name;
	}

	public void setAddr_s_name(String addr_s_name) {
		this.addr_s_name = addr_s_name;
	}

	public String getAddr_s_str1() {
		return addr_s_str1;
	}

	public void setAddr_s_str1(String addr_s_str1) {
		this.addr_s_str1 = addr_s_str1;
	}

	public String getAddr_s_str2() {
		return addr_s_str2;
	}

	public void setAddr_s_str2(String addr_s_str2) {
		this.addr_s_str2 = addr_s_str2;
	}

	public String getAddr_s_str3() {
		return addr_s_str3;
	}

	public void setAddr_s_str3(String addr_s_str3) {
		this.addr_s_str3 = addr_s_str3;
	}

	public String getAddr_s_city() {
		return addr_s_city;
	}

	public void setAddr_s_city(String addr_s_city) {
		this.addr_s_city = addr_s_city;
	}

	public String getAddr_s_state() {
		return addr_s_state;
	}

	public void setAddr_s_state(String addr_s_state) {
		this.addr_s_state = addr_s_state;
	}

	public String getAddr_s_country() {
		return addr_s_country;
	}

	public void setAddr_s_country(String addr_s_country) {
		this.addr_s_country = addr_s_country;
	}

	public String getAddr_s_zipcode() {
		return addr_s_zipcode;
	}

	public void setAddr_s_zipcode(String addr_s_zipcode) {
		this.addr_s_zipcode = addr_s_zipcode;
	}

	public String getQb_cust_id() {
		return qb_cust_id;
	}

	public void setQb_cust_id(String qb_cust_id) {
		this.qb_cust_id = qb_cust_id;
	}

	public String getAddr_b_type() {
		return addr_b_type;
	}

	public void setAddr_b_type(String addr_b_type) {
		this.addr_b_type = addr_b_type;
	}

	public String getAddr_s_type() {
		return addr_s_type;
	}

	public void setAddr_s_type(String addr_s_type) {
		this.addr_s_type = addr_s_type;
	}

	public String getFullShippingAddress() {
		StringBuilder sb = new StringBuilder();
		if (getAddr_s_str1() != null && !getAddr_s_str1().isEmpty())
		    sb.append(getAddr_s_str1());
		if (getAddr_s_str2() != null && !getAddr_s_str2().isEmpty())
		    sb.append(" ").append(getAddr_s_str2());
		if (getAddr_s_str3() != null && !getAddr_s_str3().isEmpty())
		    sb.append(" ").append(getAddr_s_str3());
		if (getAddr_s_city() != null && !getAddr_s_city().isEmpty())
		    sb.append(" ").append(getAddr_s_city());
		if (getAddr_s_state() != null && !getAddr_s_state().isEmpty())
		    sb.append(" ").append(getAddr_s_state());
		if (getAddr_s_country() != null && !getAddr_s_country().isEmpty())
		    sb.append(" ").append(getAddr_s_country());
		return sb.toString().trim();
	}
}
