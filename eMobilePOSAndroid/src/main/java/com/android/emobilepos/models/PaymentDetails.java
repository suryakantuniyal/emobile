package com.android.emobilepos.models;

/**
 * Created by Guarionex on 11/24/2015.
 */
public class PaymentDetails {
    private String paymethod_name;
    private String pay_date;
    private String pay_timecreated;
    private String cust_name;
    private String ord_total;
    private String pay_amount;
    private String change;
    private String pay_signature;
    private String pay_transid;
    private String ccnum_last4;
    private String pay_check;
    private String is_refund;
    private String IvuLottoDrawDate;
    private String IvuLottoNumber;
    private String IvuLottoQR;
    private String pay_dueamount;
    private double amountTender;
    private String inv_id;
    private String job_id;
    private String Tax1_amount;
    private String Tax2_amount;
    private String Tax1_name;
    private String Tax2_name;
    private String pay_tip;
    private String pay_comment;
    private String group_pay_id;
    private String pay_latitude;
    private String pay_longitude;
    private String isVoid;
    private String authcode;
    private String clerk_id;

    private EMVContainer emvContainer;


    public String getPaymethod_name() {
        return paymethod_name;
    }

    public void setPaymethod_name(String paymethod_name) {
        this.paymethod_name = paymethod_name;
    }

    public String getPay_date() {
        return pay_date;
    }

    public void setPay_date(String pay_date) {
        this.pay_date = pay_date;
    }

    public String getPay_timecreated() {
        return pay_timecreated;
    }

    public void setPay_timecreated(String pay_timecreated) {
        this.pay_timecreated = pay_timecreated;
    }

    public String getCust_name() {
        return cust_name;
    }

    public void setCust_name(String cust_name) {
        this.cust_name = cust_name;
    }

    public String getOrd_total() {
        return ord_total;
    }

    public void setOrd_total(String ord_total) {
        this.ord_total = ord_total;
    }

    public String getPay_amount() {
        return pay_amount;
    }

    public void setPay_amount(String pay_amount) {
        this.pay_amount = pay_amount;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public String getPay_signature() {
        return pay_signature;
    }

    public void setPay_signature(String pay_signature) {
        this.pay_signature = pay_signature;
    }

    public String getPay_transid() {
        return pay_transid;
    }

    public void setPay_transid(String pay_transid) {
        this.pay_transid = pay_transid;
    }

    public String getCcnum_last4() {
        return ccnum_last4;
    }

    public void setCcnum_last4(String ccnum_last4) {
        this.ccnum_last4 = ccnum_last4;
    }

    public String getPay_check() {
        return pay_check;
    }

    public void setPay_check(String pay_check) {
        this.pay_check = pay_check;
    }

    public String getIs_refund() {
        return is_refund;
    }

    public void setIs_refund(String is_refund) {
        this.is_refund = is_refund;
    }

    public String getIvuLottoDrawDate() {
        return IvuLottoDrawDate;
    }

    public void setIvuLottoDrawDate(String ivuLottoDrawDate) {
        IvuLottoDrawDate = ivuLottoDrawDate;
    }

    public String getIvuLottoNumber() {
        return IvuLottoNumber;
    }

    public void setIvuLottoNumber(String ivuLottoNumber) {
        IvuLottoNumber = ivuLottoNumber;
    }

    public String getIvuLottoQR() {
        return IvuLottoQR;
    }

    public void setIvuLottoQR(String ivuLottoQR) {
        IvuLottoQR = ivuLottoQR;
    }

    public String getPay_dueamount() {
        return pay_dueamount;
    }

    public void setPay_dueamount(String pay_dueamount) {
        this.pay_dueamount = pay_dueamount;
    }

    public String getInv_id() {
        return inv_id;
    }

    public void setInv_id(String inv_id) {
        this.inv_id = inv_id;
    }

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public String getTax1_amount() {
        return Tax1_amount;
    }

    public void setTax1_amount(String tax1_amount) {
        Tax1_amount = tax1_amount;
    }

    public String getTax2_amount() {
        return Tax2_amount;
    }

    public void setTax2_amount(String tax2_amount) {
        Tax2_amount = tax2_amount;
    }

    public String getTax1_name() {
        return Tax1_name;
    }

    public void setTax1_name(String tax1_name) {
        Tax1_name = tax1_name;
    }

    public String getTax2_name() {
        return Tax2_name;
    }

    public void setTax2_name(String tax2_name) {
        Tax2_name = tax2_name;
    }

    public String getPay_tip() {
        return pay_tip;
    }

    public void setPay_tip(String pay_tip) {
        this.pay_tip = pay_tip;
    }

    public EMVContainer getEmvContainer() {
        return emvContainer;
    }

    public void setEmvContainer(EMVContainer emvContainer) {
        this.emvContainer = emvContainer;
    }

    public String getPay_comment() {
        return pay_comment;
    }

    public void setPay_comment(String pay_comment) {
        this.pay_comment = pay_comment;
    }

    public String getGroup_pay_id() {
        return group_pay_id;
    }

    public void setGroup_pay_id(String group_pay_id) {
        this.group_pay_id = group_pay_id;
    }

    public String getPay_latitude() {
        return pay_latitude;
    }

    public void setPay_latitude(String pay_latitude) {
        this.pay_latitude = pay_latitude;
    }

    public String getPay_longitude() {
        return pay_longitude;
    }

    public void setPay_longitude(String pay_longitude) {
        this.pay_longitude = pay_longitude;
    }

    public String getIsVoid() {
        return isVoid;
    }

    public void setIsVoid(String isVoid) {
        this.isVoid = isVoid;
    }

    public String getAuthcode() {
        return authcode;
    }

    public void setAuthcode(String authcode) {
        this.authcode = authcode;
    }

    public String getClerk_id() {
        return clerk_id;
    }

    public void setClerk_id(String clerk_id) {
        this.clerk_id = clerk_id;
    }

    public double getAmountTender() {
        return amountTender;
    }

    public void setAmountTender(double amountTender) {
        this.amountTender = amountTender;
    }

    public boolean isVoid() {
        return getIsVoid().equals("1");
    }
}
