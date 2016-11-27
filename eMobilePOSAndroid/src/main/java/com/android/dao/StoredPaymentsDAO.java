package com.android.dao;

import android.app.Activity;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.database.DBManager;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.PaymentDetails;
import com.android.emobilepos.models.realms.StoreAndForward;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class StoredPaymentsDAO {


    private Global global;
    private Activity activity;

    public StoredPaymentsDAO(Activity activity) {
        global = (Global) activity.getApplication();
        this.activity = activity;

    }

    public String updateSignaturePayment(String pay_uuid) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        StoreAndForward storeAndForward = realm.where(StoreAndForward.class).equalTo("payment.pay_uuid", pay_uuid).findFirst();
        storeAndForward.getPayment().setPay_signature(global.encodedImage);
        realm.commitTransaction();
        return storeAndForward.getPayment().getPay_amount();
    }


    public long getRetryTransCount(String _job_id) {
        return (long) Realm.getDefaultInstance().where(StoreAndForward.class).equalTo("payment.job_id", _job_id)
                .equalTo("payment.is_retry", "1").findAll().size();
    }

    public long getCountPendingStoredPayments(String job_id) {
        return (long) Realm.getDefaultInstance().where(StoreAndForward.class)
                .equalTo("payment.job_id", job_id).findAll().size();
    }

    public void deletePaymentFromJob(String _job_id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        StoreAndForward first = realm.where(StoreAndForward.class)
                .equalTo("payment.job_id", _job_id).findFirst();
        if (first != null && first.isValid()) {
            first.deleteFromRealm();
        }
        realm.commitTransaction();
    }


    public PaymentDetails getPrintingForPaymentDetails(String payID, int type) {
        StringBuilder sb = new StringBuilder();
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Payment payment = realm.where(StoreAndForward.class).equalTo("payment.pay_id", payID).findFirst().getPayment();
        realm.commitTransaction();
        switch (type) {
            // May come from History>Payment>Details
//            case 0:
//                sb.append(
//                        "SELECT p.inv_id,p.job_id, CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name',p.pay_date,p.pay_timecreated,IFNULL(c.cust_name,'Unknown') as 'cust_name', o.ord_total,p.pay_amount,p.pay_dueamount,"
//                                + "CASE WHEN (m.paymethod_name = 'Cash') THEN (o.ord_total-p.pay_amount)  ELSE p.pay_tip END as 'change' ,p.pay_signature, "
//                                + "p.pay_transid,p.ccnum_last4,p.pay_check,p.is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR', "
//                                + "p.Tax1_amount, p.Tax2_amount, p.Tax1_name, p.Tax2_name, p.EMV_JSON "
//                                + "FROM StoredPayments p,Orders o LEFT OUTER JOIN Customers c  ON c.cust_id = p.cust_id  "
//                                + "LEFT OUTER JOIN PayMethods m ON m.paymethod_id = p.paymethod_id WHERE o.ord_id = p.job_id AND p.job_id ='");
//
//                break;
            // Straight from main menu 'Payment'
            case 1:
                sb.append(
                        "SELECT " +
//                                "p.inv_id,p.job_id,CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type " +
//                                "ELSE m.paymethod_name END AS 'paymethod_name',p.pay_date,p.pay_timecreated, " +
                                "IFNULL(c.cust_name,'Unknown') as 'cust_name' "
//                                "p.pay_amount AS 'ord_total',p.pay_amount,p.pay_dueamount,"
//                                + "CASE WHEN (m.paymethod_name = 'Cash') THEN SUM(p.pay_amount-p.pay_amount) " +
//                                "ELSE p.pay_tip END AS 'change', p.pay_signature,  "
//                                + "p.pay_transid,p.ccnum_last4,p.pay_check,p.is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate',"
//                                +  "p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR', "
//                                + "p.Tax1_amount, p.Tax2_amount, p.Tax1_name, p.Tax2_name, p.EMV_JSON "
                                + "FROM Customers c where c.cust_id = '" + payment.getCust_id() + "'");
//                                + "WHERE p.pay_id = '");


                break;
            // Straight from main menu 'Payment & Declined'
//            case 2:
//                sb.append(
//                        "SELECT p.inv_id,p.job_id,CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name',p.pay_date,p.pay_timecreated, IFNULL(c.cust_name,'Unknown') as 'cust_name',p.pay_amount AS 'ord_total',p.pay_amount,p.pay_dueamount,"
//                                + "CASE WHEN (m.paymethod_name = 'Cash') THEN SUM(p.pay_amount-p.pay_amount) ELSE p.pay_tip END AS 'change', p.pay_signature,  "
//                                + "p.pay_transid,p.ccnum_last4,p.pay_check,p.is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR', "
//                                + "p.Tax1_amount, p.Tax2_amount, p.Tax1_name, p.Tax2_name, p.EMV_JSON "
//                                + "FROM PaymentsDeclined p LEFT OUTER JOIN Customers c ON c.cust_id =p.cust_id LEFT OUTER JOIN "
//                                + "PayMethods m ON p.paymethod_id = m.paymethod_id  WHERE p.pay_id = '");
//                sb.append(payID).append("'");
//                break;
        }


        Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);
        PaymentDetails paymentDetails = new PaymentDetails();
        boolean haveCustomer = cursor.moveToFirst();
        if (payment != null) {
            if (TextUtils.isEmpty(payment.getPaymethod_id()) || payment.getPaymethod_id().equalsIgnoreCase("Genius")) {
                paymentDetails.setPaymethod_name(payment.getCard_type());
            } else {
                paymentDetails.setPaymethod_name(payment.getPaymentMethod().getPaymethod_name());
            }
            paymentDetails.setPay_date(Global.formatToDisplayDate(payment.getPay_date(), activity, 0));
            paymentDetails.setPay_timecreated(Global.formatToDisplayDate(payment.getPay_timecreated(), activity, 2));
            paymentDetails.setCust_name(haveCustomer ? cursor.getString(cursor.getColumnIndex("cust_name")) : "Unknown");
            paymentDetails.setOrd_total(payment.getPay_amount());
            paymentDetails.setPay_amount(payment.getPay_amount());
            paymentDetails.setChange(new BigDecimal(payment.getAmountTender()).subtract(new BigDecimal(payment.getPay_amount())).toString());
            paymentDetails.setPay_signature(payment.getPay_signature());
            paymentDetails.setPay_transid(payment.getPay_transid());
            paymentDetails.setCcnum_last4(payment.getCcnum_last4());
            paymentDetails.setPay_check(payment.getPay_check());
            paymentDetails.setIs_refund(payment.getIs_refund());
            paymentDetails.setIvuLottoDrawDate(payment.getIvuLottoDrawDate());
            paymentDetails.setIvuLottoNumber(payment.getIvuLottoNumber());
            paymentDetails.setIvuLottoQR(payment.getIvuLottoQR());
            paymentDetails.setPay_dueamount(payment.getPay_dueamount());
            paymentDetails.setInv_id(payment.getInv_id());
            paymentDetails.setJob_id(payment.getJob_id());
            paymentDetails.setTax1_amount(payment.getTax1_amount());
            paymentDetails.setTax2_amount(payment.getTax2_amount());
            paymentDetails.setTax1_name(payment.getTax1_name());
            paymentDetails.setTax2_name(payment.getTax2_name());
            paymentDetails.setEmvContainer(payment.getEmvContainer());

        }

        cursor.close();
        return paymentDetails;
    }

    public List<PaymentDetails> getPaymentForPrintingTransactions(String jobID) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<StoreAndForward> storeAndForwards = realm.where(StoreAndForward.class).equalTo("payment.job_id", jobID).findAll();
        realm.commitTransaction();
        List<PaymentDetails> list = new ArrayList<>();

        PaymentDetails details = new PaymentDetails();
        for (StoreAndForward sf : storeAndForwards) {
            details.setPay_amount(sf.getPayment().getPay_amount());
            details.setPaymethod_name(sf.getPayment().getPaymentMethod().getPaymethod_name());
            details.setPay_tip(sf.getPayment().getPay_tip());
            details.setPay_signature(sf.getPayment().getPay_signature());
            details.setPay_transid(sf.getPayment().getPay_transid());
            details.setCcnum_last4(sf.getPayment().getCcnum_last4());
            details.setIvuLottoDrawDate(sf.getPayment().getIvuLottoDrawDate());
            details.setIvuLottoNumber(sf.getPayment().getIvuLottoNumber());
            details.setIvuLottoQR(sf.getPayment().getIvuLottoQR());
            details.setPay_dueamount(sf.getPayment().getPay_dueamount());
            list.add(details);
            details = new PaymentDetails();
        }
        return list;
    }

    public static void updateStatusDeleted(StoreAndForward storeAndForward) {
        Realm realm = Realm.getDefaultInstance();
        long id = storeAndForward.getId();
        realm.beginTransaction();
        RealmResults<StoreAndForward> all = realm.where(StoreAndForward.class).equalTo("id", id).findAll();
        if (all.isValid()) {
            all.deleteAllFromRealm();
        }
        realm.commitTransaction();
    }

    public static void purgeDeletedStoredPayment() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(StoreAndForward.class).equalTo("status"
                , StoreAndForward.StoreAndForwatdStatus.DELETED.getCode())
                .findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public void updateStoredPaymentForRetry(StoreAndForward storeAndForward) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        storeAndForward.setRetry(true);
        realm.commitTransaction();
    }


    public void insert(Activity activity, Payment payment, StoreAndForward.PaymentType paymentType) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        StoreAndForward storeAndForward = realm.createObject(StoreAndForward.class);
        storeAndForward.setPaymentType(paymentType);
        storeAndForward.setPaymentXml(payment.getPayment_xml());
        storeAndForward.setPayment(realm.copyToRealm(payment));
        storeAndForward.setStoreAndForwatdStatus(StoreAndForward.StoreAndForwatdStatus.PENDING);
        storeAndForward.setRetry(false);
        storeAndForward.setCreationDate(new Date());
        storeAndForward.setId(System.currentTimeMillis());
        realm.insert(storeAndForward);
        realm.commitTransaction();
        PaymentsHandler.setLastPaymentInserted(payment);
        new MyPreferences(activity).setLastPayID(payment.getPay_id());
    }

    public static void updateStoreForwardPaymentToRetry(StoreAndForward storeAndForward) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        storeAndForward.setRetry(true);
        realm.commitTransaction();
    }

    public static String getLastPaymentId(Activity activity, int deviceId, int year) {
        MyPreferences myPref = new MyPreferences(activity);
        String lastPayID = myPref.getLastPayID();
        boolean getIdFromRealm = false;
        if (TextUtils.isEmpty(lastPayID) || lastPayID.length() <= 4) {
            getIdFromRealm = true;
        } else {
            String[] tokens = myPref.getLastPayID().split("-");
            if (!tokens[2].equalsIgnoreCase(String.valueOf(year))) {
                getIdFromRealm = true;
            }
        }

        if (getIdFromRealm) {
            Realm realm = Realm.getDefaultInstance();
            StoreAndForward storeAndForward = realm.where(StoreAndForward.class)
                    .beginsWith("pay_id", deviceId + "-")
                    .endsWith("pay_id", "-" + year).findFirst();
            lastPayID = storeAndForward.getPayment().getPay_id();
            if (TextUtils.isEmpty(lastPayID)) {
                lastPayID = myPref.getEmpID() + "-" + "00001" + "-" + year;
            }
            myPref.setLastPayID(lastPayID);
        }
        return lastPayID;
    }
}
