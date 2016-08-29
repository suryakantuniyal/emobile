package com.android.dao;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;

import com.android.database.DBManager;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.PaymentDetails;
import com.android.emobilepos.models.storedAndForward.StoreAndForward;
import com.android.support.Global;
import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class StoredPaymentsDAO {

    //    private final String pay_id = "pay_id";
//    private final String group_pay_id = "group_pay_id";
//    private final String custidkey = "custidkey";
//    private final String tupyx_user_id = "tupyx_user_id";
//    private final String cust_id = "cust_id";
//    private final String emp_id = "emp_id";
    private final String inv_id = "inv_id";
    //    private final String paymethod_id = "paymethod_id";
    private final String pay_check = "pay_check";
    //    private final String pay_receipt = "pay_receipt";
    private final String pay_amount = "pay_amount";
    private final String pay_dueamount = "pay_dueamount";
    //    private final String pay_comment = "pay_comment";
    private final String pay_timecreated = "pay_timecreated";
    //    private final String pay_timesync = "pay_timesync";
//    private final String account_id = "account_id";
//    private final String processed = "processed";
//    private final String pay_issync = "pay_issync";
    private final String pay_transid = "pay_transid";
    //    private final String pay_refnum = "pay_refnum";
//    private final String pay_name = "pay_name";
//    private final String pay_addr = "pay_addr";
//    private final String pay_poscode = "pay_poscode";
//    private final String pay_seccode = "pay_seccode";
//    private final String pay_maccount = "pay_maccount";
//    private final String pay_groupcode = "pay_groupcode";
//    private final String pay_stamp = "pay_stamp";
//    private final String pay_resultcode = "pay_resultcode";
//    private final String pay_resultmessage = "pay_resultmessage";
//    private final String pay_ccnum = "pay_ccnum";
//    private final String pay_expmonth = "pay_expmonth";
//    private final String pay_expyear = "pay_expyear";
//    private final String pay_expdate = "pay_expdate";
//    private final String pay_result = "pay_result";
    private final String pay_date = "pay_date";
    //    private final String recordnumber = "recordnumber";
    private final String pay_signature = "pay_signature";
//    private final String authcode = "authcode";
//    private final String status = "status";

    // added
    private final String job_id = "job_id";
    //    private final String user_ID = "user_ID";
//    private final String pay_type = "pay_type";
    private final String pay_tip = "pay_tip";
    private final String ccnum_last4 = "ccnum_last4";
    //    private final String pay_phone = "pay_phone";
//    private final String pay_email = "pay_email";
//    private final String card_type = "card_type";
//
//
//    private final String isVoid = "isVoid";
//    private final String pay_latitude = "pay_latitude";
//    private final String pay_longitude = "pay_longitude";
//    private final String tipAmount = "tipAmount";
//    private final String clerk_id = "clerk_id";
    private final String is_refund = "is_refund";
//    private final String ref_num = "ref_num";
//    private final String original_pay_id = "original_pay_id";

    private final String IvuLottoDrawDate = "IvuLottoDrawDate";
    private final String IvuLottoNumber = "IvuLottoNumber";
    private final String IvuLottoQR = "IvuLottoQR";

    private final String Tax1_amount = "Tax1_amount";
    private final String Tax1_name = "Tax1_name";
    private final String Tax2_amount = "Tax2_amount";
    private final String Tax2_name = "Tax2_name";

    // Store and Forward Data
//    private final String payment_xml = "payment_xml";
//    private final String is_retry = "is_retry";
//    private final String pay_uuid = "pay_uuid";
    private final String EMVJson = "EMV_JSON";


//    public final List<String> attr = Arrays.asList(pay_id, group_pay_id, cust_id, tupyx_user_id, emp_id,
//            inv_id, paymethod_id, pay_check, pay_receipt, pay_amount, pay_dueamount, pay_comment, pay_timecreated,
//            pay_timesync, account_id, processed, pay_issync, pay_transid, pay_refnum, pay_name, pay_addr, pay_poscode,
//            pay_seccode, pay_maccount, pay_groupcode, pay_stamp, pay_resultcode, pay_resultmessage, pay_ccnum,
//            pay_expmonth, pay_expyear, pay_expdate, pay_result, pay_date, recordnumber, pay_signature, authcode, status,
//            job_id, user_ID, pay_type, pay_tip, ccnum_last4, pay_phone, pay_email, isVoid, pay_latitude, pay_longitude,
//            tipAmount, clerk_id, is_refund, ref_num, IvuLottoDrawDate, IvuLottoNumber, IvuLottoQR, card_type,
//            Tax1_amount, Tax1_name, Tax2_amount, Tax2_name, custidkey, original_pay_id, pay_uuid, is_retry,
//            payment_xml, EMVJson);

    //    private StringBuilder sb1, sb2;
//    private HashMap<String, Integer> attrHash;
    private Global global;
    //    private static final String table_name = "StoredPayments";
    private Activity activity;

    public StoredPaymentsDAO(Activity activity) {
        global = (Global) activity.getApplication();
        this.activity = activity;
//        attrHash = new HashMap<>();
//        sb1 = new StringBuilder();
//        sb2 = new StringBuilder();
//
//        initDictionary();
    }

//    public void initDictionary() {
//        int size = attr.size();
//        for (int i = 0; i < size; i++) {
//            attrHash.put(attr.get(i), i + 1);
//            if ((i + 1) < size) {
//                sb1.append(attr.get(i)).append(",");
//                sb2.append("?").append(",");
//            } else {
//                sb1.append(attr.get(i));
//                sb2.append("?");
//            }
//        }
//    }

//    public int index(String tag) {
//        return attrHash.get(tag);
//    }


//    public void emptyTable() {
//        DBManager._db.execSQL("DELETE FROM " + table_name);
//    }

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

    public long getCountPendingStoredPayments(String _job_id) {
        return (long) Realm.getDefaultInstance().where(StoreAndForward.class)
                .equalTo("payment.job_id", _job_id).findAll().size();
    }

    public void deletePaymentFromJob(String _job_id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        StoreAndForward first = realm.where(StoreAndForward.class)
                .equalTo("payment.job_id", _job_id).findFirst();
        first.deleteFromRealm();
        realm.commitTransaction();
    }


    public PaymentDetails getPrintingForPaymentDetails(String payID, int type) {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            // May come from History>Payment>Details
            case 0:
                sb.append(
                        "SELECT p.inv_id,p.job_id, CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name',p.pay_date,p.pay_timecreated,IFNULL(c.cust_name,'Unknown') as 'cust_name', o.ord_total,p.pay_amount,p.pay_dueamount,"
                                + "CASE WHEN (m.paymethod_name = 'Cash') THEN (o.ord_total-p.pay_amount)  ELSE p.pay_tip END as 'change' ,p.pay_signature, "
                                + "p.pay_transid,p.ccnum_last4,p.pay_check,p.is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR', "
                                + "p.Tax1_amount, p.Tax2_amount, p.Tax1_name, p.Tax2_name, p.EMV_JSON "
                                + "FROM StoredPayments p,Orders o LEFT OUTER JOIN Customers c  ON c.cust_id = p.cust_id  "
                                + "LEFT OUTER JOIN PayMethods m ON m.paymethod_id = p.paymethod_id WHERE o.ord_id = p.job_id AND p.job_id ='");

                break;
            // Straight from main menu 'Payment'
            case 1:
                sb.append(
                        "SELECT p.inv_id,p.job_id,CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name',p.pay_date,p.pay_timecreated, IFNULL(c.cust_name,'Unknown') as 'cust_name',p.pay_amount AS 'ord_total',p.pay_amount,p.pay_dueamount,"
                                + "CASE WHEN (m.paymethod_name = 'Cash') THEN SUM(p.pay_amount-p.pay_amount) ELSE p.pay_tip END AS 'change', p.pay_signature,  "
                                + "p.pay_transid,p.ccnum_last4,p.pay_check,p.is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR', "
                                + "p.Tax1_amount, p.Tax2_amount, p.Tax1_name, p.Tax2_name, p.EMV_JSON "
                                + "FROM StoredPayments p LEFT OUTER JOIN Customers c ON c.cust_id =p.cust_id LEFT OUTER JOIN "
                                + "PayMethods m ON p.paymethod_id = m.paymethod_id  WHERE p.pay_id = '");

                break;
            // Straight from main menu 'Payment & Declined'
            case 2:
                sb.append(
                        "SELECT p.inv_id,p.job_id,CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name',p.pay_date,p.pay_timecreated, IFNULL(c.cust_name,'Unknown') as 'cust_name',p.pay_amount AS 'ord_total',p.pay_amount,p.pay_dueamount,"
                                + "CASE WHEN (m.paymethod_name = 'Cash') THEN SUM(p.pay_amount-p.pay_amount) ELSE p.pay_tip END AS 'change', p.pay_signature,  "
                                + "p.pay_transid,p.ccnum_last4,p.pay_check,p.is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR', "
                                + "p.Tax1_amount, p.Tax2_amount, p.Tax1_name, p.Tax2_name, p.EMV_JSON "
                                + "FROM PaymentsDeclined p LEFT OUTER JOIN Customers c ON c.cust_id =p.cust_id LEFT OUTER JOIN "
                                + "PayMethods m ON p.paymethod_id = m.paymethod_id  WHERE p.pay_id = '");

                break;
        }

        sb.append(payID).append("'");

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
        PaymentDetails paymentDetails = new PaymentDetails();

        if (cursor.moveToFirst()) {

            do {
                paymentDetails.setPaymethod_name(cursor.getString(cursor.getColumnIndex("paymethod_name")));
                paymentDetails.setPay_date(Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(pay_date)), activity, 0));
                paymentDetails.setPay_timecreated(Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(pay_timecreated)), activity, 2));
                paymentDetails.setCust_name(cursor.getString(cursor.getColumnIndex("cust_name")));
                paymentDetails.setOrd_total(cursor.getString(cursor.getColumnIndex("ord_total")));
                paymentDetails.setPay_amount(cursor.getString(cursor.getColumnIndex(pay_amount)));
                paymentDetails.setChange(cursor.getString(cursor.getColumnIndex("change")));
                paymentDetails.setPay_signature(cursor.getString(cursor.getColumnIndex(pay_signature)));
                paymentDetails.setPay_transid(cursor.getString(cursor.getColumnIndex(pay_transid)));
                paymentDetails.setCcnum_last4(cursor.getString(cursor.getColumnIndex(ccnum_last4)));
                paymentDetails.setPay_check(cursor.getString(cursor.getColumnIndex(pay_check)));
                paymentDetails.setIs_refund(cursor.getString(cursor.getColumnIndex(is_refund)));
                paymentDetails.setIvuLottoDrawDate(cursor.getString(cursor.getColumnIndex(IvuLottoDrawDate)));
                paymentDetails.setIvuLottoNumber(cursor.getString(cursor.getColumnIndex(IvuLottoNumber)));
                paymentDetails.setIvuLottoQR(cursor.getString(cursor.getColumnIndex(IvuLottoQR)));
                paymentDetails.setPay_dueamount(cursor.getString(cursor.getColumnIndex(pay_dueamount)));
                paymentDetails.setInv_id(cursor.getString(cursor.getColumnIndex(inv_id)));
                paymentDetails.setJob_id(cursor.getString(cursor.getColumnIndex(job_id)));
                paymentDetails.setTax1_amount(cursor.getString(cursor.getColumnIndex(Tax1_amount)));
                paymentDetails.setTax2_amount(cursor.getString(cursor.getColumnIndex(Tax2_amount)));
                paymentDetails.setTax1_name(cursor.getString(cursor.getColumnIndex(Tax1_name)));
                paymentDetails.setTax2_name(cursor.getString(cursor.getColumnIndex(Tax2_name)));
                paymentDetails.setEmvContainer(new Gson().fromJson(cursor.getString(cursor.getColumnIndex(EMVJson)), EMVContainer.class));

            } while (cursor.moveToNext());
        }

        cursor.close();
        return paymentDetails;
    }

    public List<PaymentDetails> getPaymentForPrintingTransactions(String jobID) {

        String sb = "SELECT p.pay_amount AS 'pay_amount',pm.paymethod_name AS 'paymethod_name',p.pay_tip AS 'pay_tip'," +
                "p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4'," +
                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR'," +
                "p.pay_dueamount AS 'pay_dueamount' " +
                "FROM StoredPayments p," +
                "PayMethods pm " +
                "WHERE p.paymethod_id = pm.paymethod_id AND p.job_id = '" + jobID +
                "' UNION " +
                "SELECT p.pay_amount AS 'pay_amount','Wallet' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature'," +
                "p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate'," +
                "p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' " +
                "FROM StoredPayments p " +
                "WHERE p.paymethod_id = 'Wallet' AND p.job_id = '" + jobID + "' UNION " +
                "SELECT p.pay_amount AS 'pay_amount','LoyaltyCard' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature'," +
                "p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate'," +
                "p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' " +
                "FROM StoredPayments p " +
                "WHERE p.paymethod_id = 'LoyaltyCard' AND p.job_id = '" + jobID + "' UNION " +
                "SELECT p.pay_amount AS 'pay_amount','Reward' AS  'paymethod_name',p.pay_tip AS 'pay_tip'," +
                "p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4'," +
                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR'," +
                "p.pay_dueamount AS 'pay_dueamount' " +
                "FROM StoredPayments p " +
                "WHERE p.paymethod_id = 'Reward' AND p.job_id = '" + jobID + "' UNION " +
                "SELECT p.pay_amount AS 'pay_amount','GiftCard' AS  'paymethod_name',p.pay_tip AS 'pay_tip'," +
                "p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4'," +
                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR'," +
                "p.pay_dueamount AS 'pay_dueamount' " +
                "FROM StoredPayments p " +
                "WHERE p.paymethod_id = 'GiftCard' AND p.job_id = '" + jobID + "'";
        List<PaymentDetails> list = new ArrayList<>();

        Cursor cursor = DBManager._db.rawQuery(sb, null);
        PaymentDetails details = new PaymentDetails();
        if (cursor.moveToFirst()) {

            do {
                details.setPay_amount(cursor.getString(cursor.getColumnIndex(pay_amount)));
                details.setPaymethod_name(cursor.getString(cursor.getColumnIndex("paymethod_name")));
                details.setPay_tip(cursor.getString(cursor.getColumnIndex(pay_tip)));
                details.setPay_signature(cursor.getString(cursor.getColumnIndex(pay_signature)));
                details.setPay_transid(cursor.getString(cursor.getColumnIndex(pay_transid)));
                details.setCcnum_last4(cursor.getString(cursor.getColumnIndex(ccnum_last4)));
                details.setIvuLottoDrawDate(cursor.getString(cursor.getColumnIndex(IvuLottoDrawDate)));
                details.setIvuLottoNumber(cursor.getString(cursor.getColumnIndex(IvuLottoNumber)));
                details.setIvuLottoQR(cursor.getString(cursor.getColumnIndex(IvuLottoQR)));
                details.setPay_dueamount(cursor.getString(cursor.getColumnIndex(pay_dueamount)));

                list.add(details);
                details = new PaymentDetails();
            } while (cursor.moveToNext());
        }
        cursor.close();
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

    public static void purdeDeletedStoredPayment() {
//        DBManager._db.delete(table_name, "pay_uuid = ?", new String[]{_pay_uuid});
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(StoreAndForward.class).equalTo("status"
                , StoreAndForward.StoreAndForwatdStatus.DELETED.getCode())
                .findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public void updateStoredPaymentForRetry(StoreAndForward storeAndForward) {
//        ContentValues args = new ContentValues();
//        args.put(is_retry, "1");
//        DBManager._db.update(table_name, args, "pay_uuid = ?", new String[]{_pay_uuid});
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        storeAndForward.setRetry(true);
        realm.commitTransaction();
    }


    public void insert(Payment payment, StoreAndForward.PaymentType paymentType) {
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
        realm.commitTransaction();
        PaymentsHandler.setLastPaymentInserted(payment);
    }

    public static void updateStoreForwardPaymentToRetry(StoreAndForward storeAndForward) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        storeAndForward.setRetry(true);
        realm.commitTransaction();
    }
}
