package com.android.emobilepos.models.realms;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.text.TextUtils;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.PaymentMethodDAO;
import com.android.database.DrawInfoHandler;
import com.android.emobilepos.models.EMVContainer;
import com.android.ivu.MersenneTwisterFast;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Payment extends RealmObject {


    private static final long serialVersionUID = 1L;
    private AssignEmployee assignEmployee;
    @PrimaryKey
    private String pay_id = "";
    private String group_pay_id = "";
    private String original_pay_id = "";
    private String tupyx_user_id = "";
    private String cust_id = "";
    private String custidkey = "";
    private String emp_id = "";
    private String inv_id = "";
    private String paymethod_id = "";
    @Ignore
    private PaymentMethod paymentMethod;
    private String pay_check = "";
    private String pay_receipt = "";
    private String pay_amount = "0.00";
    private String pay_dueamount = "0.00";
    private Double amountTender = 0.00;

    private String originalTotalAmount = "";
    private String pay_comment = "";
    private String pay_timecreated = "";
    private String pay_timesync = "";
    private String account_id = "";
    private String processed = "";
    private String pay_issync = "";
    private String pay_transid = "";
    private String pay_refnum = "";
    private String pay_name = "";
    private String pay_addr = "";
    private String pay_poscode = "";
    private String pay_seccode = "";
    private String pay_maccount = "";
    private String pay_groupcode = "";
    private String pay_stamp = "";
    private String pay_resultcode = "";
    private String pay_resultmessage = "";
    private String pay_ccnum = "";
    private String pay_expmonth = "";
    private String pay_expyear = "";
    private String pay_expdate = "";
    private String pay_result = "";
    private String pay_date = "";
    private String recordnumber = "";
    private String pay_signature = "";
    private String authcode = "";
    private String status = "";
    @Index
    private String job_id = "";
    private String user_ID = "";
    private String pay_type = "";
    private String pay_tip = "0.00";
    private String ccnum_last4 = "";
    private String pay_phone = "";
    private String pay_email = "";
    @Index
    private String isVoid = "";

    private String tipAmount = "";
    private String clerk_id = "";
    private String pay_latitude = "";
    private String pay_longitude = "";
    private String IvuLottoDrawDate = "";
    private String IvuLottoNumber = "";
    private String IvuLottoQR = "";

    private String Tax1_amount = "";
    private String Tax1_name = "";
    private String Tax2_amount = "";
    private String Tax2_name = "";

    private String track_one = "";
    private String track_two = "";
    private String is_refund = "0";
    private String ref_num = "";
    private String card_type = "";

    private String check_account_number = "";
    private String check_routing_number = "";
    private String check_check_number = "";
    private String check_check_type = "";
    private String check_account_type = "";
    private String check_name = "";
    private String check_city = "";
    private String check_state = "";
    private String dl_number = "";
    private String dl_state = "";
    private String dl_dob = "";

    // Check Capture
    private String frontImage = "";
    private String backImage = "";
    private String micrData = "";

    // For Boloro
    private String telcoid = "";
    private String transmode = "";
    private String tagid = "";

    // Store & Forward
    @Index
    private String pay_uuid = "";
    private String is_retry = "0";
    private String payment_xml = "";
    @Ignore
    private EMVContainer emvContainer;

    public enum PaymentType {
        PAYMENT(0), VOID(1), REFUND(2);
        private int code;

        PaymentType(int code) {
            this.code = code;
        }

        public static PaymentType getPaymentTypeByCode(String payType) {
            switch (payType) {
                case "0":
                    return PAYMENT;
                case "1":
                    return VOID;
                case "2":
                    return REFUND;
                default:
                    return PAYMENT;
            }
        }
    }

    public Payment() {
        assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
    }

    public Payment(Context activity) {

        setPay_issync("0");
        setIsVoid("0");
        setStatus("1");

        String date = DateUtils.getDateAsString(new Date(), DateUtils.DATE_PATTERN);
        setPay_timecreated(date);
        setPay_date(date);
        assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);

        setEmp_id(String.valueOf(assignEmployee.getEmpId()));
        Location currLocation = Global.getCurrLocation(activity, false);
        setPay_latitude(String.valueOf(currLocation.getLatitude()));
        setPay_longitude(String.valueOf(currLocation.getLongitude()));

    }

    public Payment(Activity activity, String paymentId, String customerId, String invoiceId, String jobId, String clerkId,
                   String custidkey, String paymentMethod, Double actualAmount, Double amountTender,
                   String customerName, String referenceNumber, String phoneNumber, String email, Double tipAmount,
                   String taxAmount1, String taxAmount2, String taxName1, String taxName2,
                   String isRefund, String paymentType, String creditCardType, String cardNumberEnc, String cardNumberLast4,
                   String cardExpMonth, String cardExpYear, String cardPostalCode, String cardSecurityCode, String trackOne,
                   String trackTwo, String transactionId, String authcode) {
        assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
        setPay_issync("0");
        setIsVoid("0");
        setStatus("1");
        setPay_transid(transactionId);
        this.setAuthcode(authcode);
        setPay_ccnum(cardNumberEnc);
        setCcnum_last4(cardNumberLast4);
        setPay_expmonth(cardExpMonth);
        setPay_expyear(cardExpYear);
        setPay_poscode(cardPostalCode);
        setPay_seccode(cardSecurityCode);
        this.setAmountTender(amountTender);
        this.setCard_type(creditCardType);
        this.setTrack_one(trackOne);
        this.setTrack_two(trackTwo);

        String date = DateUtils.getDateAsString(new Date(), DateUtils.DATE_PATTERN);
        setPay_timecreated(date);
        setPay_date(date);

        setEmp_id(String.valueOf(assignEmployee.getEmpId()));

        setPay_id(paymentId);
        setEmp_id(String.valueOf(assignEmployee.getEmpId()));
        setCust_id(customerId);
        setJob_id(jobId);
        setInv_id(invoiceId);
        setClerk_id(clerkId);
        custidkey = custidkey;
        setPaymethod_id(paymentMethod);
        setPay_dueamount(Double.toString(actualAmount - amountTender));
        if (amountTender > actualAmount)
            setPay_amount(Double.toString(actualAmount));
        else
            setPay_amount(Double.toString(amountTender));
        setPay_name(customerName);
        setProcessed("1");
        setRef_num(referenceNumber);
        setPay_phone(phoneNumber);
        setPay_email(email);
        setPay_tip(String.valueOf(tipAmount));
        if (Global.isIvuLoto) {
            DrawInfoHandler drawDateInfo = new DrawInfoHandler(activity);
            MersenneTwisterFast mersenneTwister = new MersenneTwisterFast();
            String drawDate = drawDateInfo.getDrawDate();
            String ivuLottoNum = mersenneTwister.generateIVULoto();
            setIvuLottoNumber(ivuLottoNum);
            setIvuLottoDrawDate(drawDate);
            setIvuLottoQR(Global.base64QRCode(ivuLottoNum, drawDate));
            if (!TextUtils.isEmpty(taxAmount1)) {
                setTax1_amount(taxAmount1);
                setTax1_name(taxName1);
                setTax2_amount(taxAmount2);
                setTax2_name(taxName2);
            }
        }
        Location currLocation = Global.getCurrLocation(activity, false);
        setPay_latitude(String.valueOf(currLocation.getLatitude()));
        setPay_longitude(String.valueOf(currLocation.getLongitude()));
        setIs_refund(isRefund);
        setPay_type(paymentType);

    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getPay_id() {
        return pay_id;
    }

    public void setPay_id(String pay_id) {
        this.pay_id = pay_id;
    }

    public String getGroup_pay_id() {
        return group_pay_id;
    }

    public void setGroup_pay_id(String group_pay_id) {
        this.group_pay_id = group_pay_id;
    }

    public String getOriginal_pay_id() {
        return original_pay_id;
    }

    public void setOriginal_pay_id(String original_pay_id) {
        this.original_pay_id = original_pay_id;
    }

    public String getTupyx_user_id() {
        return tupyx_user_id;
    }

    public void setTupyx_user_id(String tupyx_user_id) {
        this.tupyx_user_id = tupyx_user_id;
    }

    public String getCust_id() {
        return cust_id;
    }

    public void setCust_id(String cust_id) {
        this.cust_id = cust_id;
    }

    public String getCustidkey() {
        return custidkey;
    }

    public void setCustidkey(String custidkey) {
        this.custidkey = custidkey;
    }

    public String getEmp_id() {
        return emp_id;
    }

    public void setEmp_id(String emp_id) {
        this.emp_id = emp_id;
    }

    public String getInv_id() {
        return inv_id;
    }

    public void setInv_id(String inv_id) {
        this.inv_id = inv_id;
    }

    public String getPaymethod_id() {
        return paymethod_id;
    }

    private void setPaymentMethod(String paymethod_id) {
        this.paymentMethod = PaymentMethodDAO.getPaymentMethodById(paymethod_id);
//                Realm.getDefaultInstance()
//                .where(PaymentMethod.class)
//                .equalTo("paymethod_id", paymethod_id).findFirst();
        if (!this.isValid() && getPaymentMethod() != null) {
            Realm realm = Realm.getDefaultInstance();
            this.paymentMethod = realm.copyFromRealm(this.getPaymentMethod());
            realm.close();
        }
    }

    public void setPaymethod_id(String paymethod_id) {
        setPaymentMethod(paymethod_id);
        this.paymethod_id = paymethod_id;
    }

    public String getPay_check() {
        return pay_check;
    }

    public void setPay_check(String pay_check) {
        this.pay_check = pay_check;
    }

    public String getPay_receipt() {
        return pay_receipt;
    }

    public void setPay_receipt(String pay_receipt) {
        this.pay_receipt = pay_receipt;
    }

    public String getPay_amount() {
        return pay_amount;
    }

    public void setPay_amount(String pay_amount) {
        this.pay_amount = pay_amount;
    }

    public String getPay_dueamount() {
        return pay_dueamount;
    }

    public void setPay_dueamount(String pay_dueamount) {
        this.pay_dueamount = pay_dueamount;
    }

    public Double getAmountTender() {
        return amountTender;
    }

    public void setAmountTender(Double amountTender) {
        this.amountTender = amountTender;
    }

    public String getOriginalTotalAmount() {
        return originalTotalAmount;
    }

    public void setOriginalTotalAmount(String originalTotalAmount) {
        this.originalTotalAmount = originalTotalAmount;
    }

    public String getPay_comment() {
        return pay_comment;
    }

    public void setPay_comment(String pay_comment) {
        this.pay_comment = pay_comment;
    }

    public String getPay_timecreated() {
        return pay_timecreated;
    }

    public void setPay_timecreated(String pay_timecreated) {
        this.pay_timecreated = pay_timecreated;
    }

    public String getPay_timesync() {
        return pay_timesync;
    }

    public void setPay_timesync(String pay_timesync) {
        this.pay_timesync = pay_timesync;
    }

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public String getProcessed() {
        return processed;
    }

    public void setProcessed(String processed) {
        this.processed = processed;
    }

    public String getPay_issync() {
        return pay_issync;
    }

    public void setPay_issync(String pay_issync) {
        this.pay_issync = pay_issync;
    }

    public String getPay_transid() {
        return pay_transid;
    }

    public void setPay_transid(String pay_transid) {
        this.pay_transid = pay_transid;
    }

    public String getPay_refnum() {
        return pay_refnum;
    }

    public void setPay_refnum(String pay_refnum) {
        this.pay_refnum = pay_refnum;
    }

    public String getPay_name() {
        return pay_name;
    }

    public void setPay_name(String pay_name) {
        this.pay_name = pay_name;
    }

    public String getPay_addr() {
        return pay_addr;
    }

    public void setPay_addr(String pay_addr) {
        this.pay_addr = pay_addr;
    }

    public String getPay_poscode() {
        return pay_poscode;
    }

    public void setPay_poscode(String pay_poscode) {
        this.pay_poscode = pay_poscode;
    }

    public String getPay_seccode() {
        return pay_seccode;
    }

    public void setPay_seccode(String pay_seccode) {
        this.pay_seccode = pay_seccode;
    }

    public String getPay_maccount() {
        return pay_maccount;
    }

    public void setPay_maccount(String pay_maccount) {
        this.pay_maccount = pay_maccount;
    }

    public String getPay_groupcode() {
        return pay_groupcode;
    }

    public void setPay_groupcode(String pay_groupcode) {
        this.pay_groupcode = pay_groupcode;
    }

    public String getPay_stamp() {
        return pay_stamp;
    }

    public void setPay_stamp(String pay_stamp) {
        this.pay_stamp = pay_stamp;
    }

    public String getPay_resultcode() {
        return pay_resultcode;
    }

    public void setPay_resultcode(String pay_resultcode) {
        this.pay_resultcode = pay_resultcode;
    }

    public String getPay_resultmessage() {
        return pay_resultmessage;
    }

    public void setPay_resultmessage(String pay_resultmessage) {
        this.pay_resultmessage = pay_resultmessage;
    }

    public String getPay_ccnum() {
        return pay_ccnum;
    }

    public void setPay_ccnum(String pay_ccnum) {
        this.pay_ccnum = pay_ccnum;
    }

    public String getPay_expmonth() {
        return pay_expmonth;
    }

    public void setPay_expmonth(String pay_expmonth) {
        this.pay_expmonth = pay_expmonth;
    }

    public String getPay_expyear() {
        return pay_expyear;
    }

    public void setPay_expyear(String pay_expyear) {
        this.pay_expyear = pay_expyear;
    }

    public String getPay_expdate() {
        return pay_expdate;
    }

    public void setPay_expdate(String pay_expdate) {
        this.pay_expdate = pay_expdate;
    }

    public String getPay_result() {
        return pay_result;
    }

    public void setPay_result(String pay_result) {
        this.pay_result = pay_result;
    }

    public String getPay_date() {
        return pay_date;
    }

    public void setPay_date(String pay_date) {
        this.pay_date = pay_date;
    }

    public String getRecordnumber() {
        return recordnumber;
    }

    public void setRecordnumber(String recordnumber) {
        this.recordnumber = recordnumber;
    }

    public String getPay_signature() {
        return pay_signature;
    }

    public void setPay_signature(String pay_signature) {
        this.pay_signature = pay_signature;
    }

    public String getAuthcode() {
        return authcode;
    }

    public void setAuthcode(String authcode) {
        this.authcode = authcode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public String getUser_ID() {
        return user_ID;
    }

    public void setUser_ID(String user_ID) {
        this.user_ID = user_ID;
    }

    public String getPay_type() {
        return pay_type;
    }

    public void setPay_type(String pay_type) {
        this.pay_type = pay_type;
    }

    public boolean isVoidPaymentType() {
        return PaymentType.getPaymentTypeByCode(pay_type) == PaymentType.VOID;
    }

    public boolean isRefundPaymentType() {
        return PaymentType.getPaymentTypeByCode(pay_type) == PaymentType.REFUND;
    }

    public boolean isPaymentPaymentType() {
        return PaymentType.getPaymentTypeByCode(pay_type) == PaymentType.PAYMENT;
    }

    public String getPay_tip() {
        return pay_tip;
    }

    public void setPay_tip(String pay_tip) {
        this.pay_tip = pay_tip;
    }

    public String getCcnum_last4() {
        return ccnum_last4;
    }

    public void setCcnum_last4(String ccnum_last4) {
        this.ccnum_last4 = ccnum_last4;
    }

    public String getPay_phone() {
        return pay_phone;
    }

    public void setPay_phone(String pay_phone) {
        this.pay_phone = pay_phone;
    }

    public String getPay_email() {
        return pay_email;
    }

    public void setPay_email(String pay_email) {
        this.pay_email = pay_email;
    }

    public String getIsVoid() {
        return isVoid;
    }

    public void setIsVoid(String isVoid) {
        this.isVoid = isVoid;
    }

    public String getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(String tipAmount) {
        this.tipAmount = tipAmount;
    }

    public String getClerk_id() {
        return clerk_id;
    }

    public void setClerk_id(String clerk_id) {
        this.clerk_id = clerk_id;
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

    public String getTax1_amount() {
        return Tax1_amount;
    }

    public void setTax1_amount(String tax1_amount) {
        Tax1_amount = tax1_amount;
    }

    public String getTax1_name() {
        return Tax1_name;
    }

    public void setTax1_name(String tax1_name) {
        Tax1_name = tax1_name;
    }

    public String getTax2_amount() {
        return Tax2_amount;
    }

    public void setTax2_amount(String tax2_amount) {
        Tax2_amount = tax2_amount;
    }

    public String getTax2_name() {
        return Tax2_name;
    }

    public void setTax2_name(String tax2_name) {
        Tax2_name = tax2_name;
    }

    public String getTrack_one() {
        return track_one;
    }

    public void setTrack_one(String track_one) {
        this.track_one = track_one;
    }

    public String getTrack_two() {
        return track_two;
    }

    public void setTrack_two(String track_two) {
        this.track_two = track_two;
    }

    public String getIs_refund() {
        return is_refund;
    }

    public void setIs_refund(String is_refund) {
        this.is_refund = is_refund;
    }

    public String getRef_num() {
        return ref_num;
    }

    public void setRef_num(String ref_num) {
        this.ref_num = ref_num;
    }

    public String getCard_type() {
        return card_type;
    }

    public void setCard_type(String card_type) {
        this.card_type = card_type;
    }

    public String getCheck_account_number() {
        return check_account_number;
    }

    public void setCheck_account_number(String check_account_number) {
        this.check_account_number = check_account_number;
    }

    public String getCheck_routing_number() {
        return check_routing_number;
    }

    public void setCheck_routing_number(String check_routing_number) {
        this.check_routing_number = check_routing_number;
    }

    public String getCheck_check_number() {
        return check_check_number;
    }

    public void setCheck_check_number(String check_check_number) {
        this.check_check_number = check_check_number;
    }

    public String getCheck_check_type() {
        return check_check_type;
    }

    public void setCheck_check_type(String check_check_type) {
        this.check_check_type = check_check_type;
    }

    public String getCheck_account_type() {
        return check_account_type;
    }

    public void setCheck_account_type(String check_account_type) {
        this.check_account_type = check_account_type;
    }

    public String getCheck_name() {
        return check_name;
    }

    public void setCheck_name(String check_name) {
        this.check_name = check_name;
    }

    public String getCheck_city() {
        return check_city;
    }

    public void setCheck_city(String check_city) {
        this.check_city = check_city;
    }

    public String getCheck_state() {
        return check_state;
    }

    public void setCheck_state(String check_state) {
        this.check_state = check_state;
    }

    public String getDl_number() {
        return dl_number;
    }

    public void setDl_number(String dl_number) {
        this.dl_number = dl_number;
    }

    public String getDl_state() {
        return dl_state;
    }

    public void setDl_state(String dl_state) {
        this.dl_state = dl_state;
    }

    public String getDl_dob() {
        return dl_dob;
    }

    public void setDl_dob(String dl_dob) {
        this.dl_dob = dl_dob;
    }

    public String getFrontImage() {
        return frontImage;
    }

    public void setFrontImage(String frontImage) {
        this.frontImage = frontImage;
    }

    public String getBackImage() {
        return backImage;
    }

    public void setBackImage(String backImage) {
        this.backImage = backImage;
    }

    public String getMicrData() {
        return micrData;
    }

    public void setMicrData(String micrData) {
        this.micrData = micrData;
    }

    public String getTelcoid() {
        return telcoid;
    }

    public void setTelcoid(String telcoid) {
        this.telcoid = telcoid;
    }

    public String getTransmode() {
        return transmode;
    }

    public void setTransmode(String transmode) {
        this.transmode = transmode;
    }

    public String getTagid() {
        return tagid;
    }

    public void setTagid(String tagid) {
        this.tagid = tagid;
    }

    public String getPay_uuid() {
        return pay_uuid;
    }

    public void setPay_uuid(String pay_uuid) {
        this.pay_uuid = pay_uuid;
    }

    public String getIs_retry() {
        return is_retry;
    }

    public void setIs_retry(String is_retry) {
        this.is_retry = is_retry;
    }

    public String getPayment_xml() {
        return payment_xml;
    }

    public void setPayment_xml(String payment_xml) {
        this.payment_xml = payment_xml;
    }

    public EMVContainer getEmvContainer() {
        return emvContainer;
    }

    public void setEmvContainer(EMVContainer emvContainer) {
        this.emvContainer = emvContainer;
    }

    public PaymentMethod getPaymentMethod() {
        if (this.paymentMethod == null && !TextUtils.isEmpty(getPaymethod_id())) {
            setPaymentMethod(getPaymethod_id());
        }
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
