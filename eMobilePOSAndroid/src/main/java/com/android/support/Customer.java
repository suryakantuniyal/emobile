package com.android.support;

import com.android.emobilepos.models.Address;

public class Customer 
{
	private String cust_id = "";
	private String cust_id_ref = "";
	private String qb_sync = "";
	private String zone_id = "";
	private String CompanyName = "";
	private String Salutation = "";
	private String cust_contact = "";
	private String cust_name = "";
	private String cust_chain = "";
	private String cust_balance = "";
	private String cust_limit = "";
	private String cust_firstName = "";
	private String cust_middleName = "";
	private String cust_lastName = "";
	private String cust_phone = "";
	private String cust_email = "";
	private String cust_fax = "";
	private String cust_update = "";
	private String isactive = "";
	private String cust_ordertype = "";
	private String cust_taxable = "";
	private String cust_salestaxcode = "";
	private String pricelevel_id = "";
	private String cust_terms = "";
	private String cust_pwd = "";
	private String cust_securityquestion = "";
	private String cust_securityanswer = "";
	private String cust_points = "";
	private String cust_dob = "";
	private Address shippingAddress;
	private Address billingAddress;
	private String custIdKey;
	private String custIdNumeric;
	private String custAccountNumber;

	public String getCust_id() {
		return cust_id;
	}

	public void setCust_id(String cust_id) {
		this.cust_id = cust_id;
	}

	public String getCust_id_ref() {
		return cust_id_ref;
	}

	public void setCust_id_ref(String cust_id_ref) {
		this.cust_id_ref = cust_id_ref;
	}

	public String getQb_sync() {
		return qb_sync;
	}

	public void setQb_sync(String qb_sync) {
		this.qb_sync = qb_sync;
	}

	public String getZone_id() {
		return zone_id;
	}

	public void setZone_id(String zone_id) {
		this.zone_id = zone_id;
	}

	public String getCompanyName() {
		return CompanyName;
	}

	public void setCompanyName(String companyName) {
		CompanyName = companyName;
	}

	public String getSalutation() {
		return Salutation;
	}

	public void setSalutation(String salutation) {
		Salutation = salutation;
	}

	public String getCust_contact() {
		return cust_contact;
	}

	public void setCust_contact(String cust_contact) {
		this.cust_contact = cust_contact;
	}

	public String getCust_name() {
		return cust_name;
	}

	public void setCust_name(String cust_name) {
		this.cust_name = cust_name;
	}

	public String getCust_chain() {
		return cust_chain;
	}

	public void setCust_chain(String cust_chain) {
		this.cust_chain = cust_chain;
	}

	public String getCust_balance() {
		return cust_balance;
	}

	public void setCust_balance(String cust_balance) {
		this.cust_balance = cust_balance;
	}

	public String getCust_limit() {
		return cust_limit;
	}

	public void setCust_limit(String cust_limit) {
		this.cust_limit = cust_limit;
	}

	public String getCust_firstName() {
		return cust_firstName;
	}

	public void setCust_firstName(String cust_firstName) {
		this.cust_firstName = cust_firstName;
	}

	public String getCust_middleName() {
		return cust_middleName;
	}

	public void setCust_middleName(String cust_middleName) {
		this.cust_middleName = cust_middleName;
	}

	public String getCust_lastName() {
		return cust_lastName;
	}

	public void setCust_lastName(String cust_lastName) {
		this.cust_lastName = cust_lastName;
	}

	public String getCust_phone() {
		return cust_phone;
	}

	public void setCust_phone(String cust_phone) {
		this.cust_phone = cust_phone;
	}

	public String getCust_email() {
		return cust_email;
	}

	public void setCust_email(String cust_email) {
		this.cust_email = cust_email;
	}

	public String getCust_fax() {
		return cust_fax;
	}

	public void setCust_fax(String cust_fax) {
		this.cust_fax = cust_fax;
	}

	public String getCust_update() {
		return cust_update;
	}

	public void setCust_update(String cust_update) {
		this.cust_update = cust_update;
	}

	public String getIsactive() {
		return isactive;
	}

	public void setIsactive(String isactive) {
		this.isactive = isactive;
	}

	public String getCust_ordertype() {
		return cust_ordertype;
	}

	public void setCust_ordertype(String cust_ordertype) {
		this.cust_ordertype = cust_ordertype;
	}

	public String getCust_taxable() {
		return cust_taxable;
	}

	public void setCust_taxable(String cust_taxable) {
		this.cust_taxable = cust_taxable;
	}

	public String getCust_salestaxcode() {
		return cust_salestaxcode;
	}

	public void setCust_salestaxcode(String cust_salestaxcode) {
		this.cust_salestaxcode = cust_salestaxcode;
	}

	public String getPricelevel_id() {
		return pricelevel_id;
	}

	public void setPricelevel_id(String pricelevel_id) {
		this.pricelevel_id = pricelevel_id;
	}

	public String getCust_terms() {
		return cust_terms;
	}

	public void setCust_terms(String cust_terms) {
		this.cust_terms = cust_terms;
	}

	public String getCust_pwd() {
		return cust_pwd;
	}

	public void setCust_pwd(String cust_pwd) {
		this.cust_pwd = cust_pwd;
	}

	public String getCust_securityquestion() {
		return cust_securityquestion;
	}

	public void setCust_securityquestion(String cust_securityquestion) {
		this.cust_securityquestion = cust_securityquestion;
	}

	public String getCust_securityanswer() {
		return cust_securityanswer;
	}

	public void setCust_securityanswer(String cust_securityanswer) {
		this.cust_securityanswer = cust_securityanswer;
	}

	public String getCust_points() {
		return cust_points;
	}

	public void setCust_points(String cust_points) {
		this.cust_points = cust_points;
	}

	public String getCust_dob() {
		return cust_dob;
	}

	public void setCust_dob(String cust_dob) {
		this.cust_dob = cust_dob;
	}

	public Address getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(Address shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public Address getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(Address billingAddress) {
		this.billingAddress = billingAddress;
	}

	public String getCustIdKey() {
		return custIdKey;
	}

	public void setCustIdKey(String custIdKey) {
		this.custIdKey = custIdKey;
	}


	public String getCustIdNumeric() {
		return custIdNumeric;
	}

	public void setCustIdNumeric(String custIdNumeric) {
		this.custIdNumeric = custIdNumeric;
	}

	public String getCustAccountNumber() {
		return custAccountNumber;
	}

	public void setCustAccountNumber(String custAccountNumber) {
		this.custAccountNumber = custAccountNumber;
	}
}
