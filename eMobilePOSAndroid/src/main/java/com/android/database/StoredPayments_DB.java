package com.android.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;

import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.PaymentDetails;
import com.android.support.Global;
import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class StoredPayments_DB {

    private final String pay_id = "pay_id";
    private final String group_pay_id = "group_pay_id";
    private final String custidkey = "custidkey";
    private final String tupyx_user_id = "tupyx_user_id";
    private final String cust_id = "cust_id";
    private final String emp_id = "emp_id";
    private final String inv_id = "inv_id";
    private final String paymethod_id = "paymethod_id";
    private final String pay_check = "pay_check";
    private final String pay_receipt = "pay_receipt";
    private final String pay_amount = "pay_amount";
    private final String pay_dueamount = "pay_dueamount";
    private final String pay_comment = "pay_comment";
    private final String pay_timecreated = "pay_timecreated";
    private final String pay_timesync = "pay_timesync";
    private final String account_id = "account_id";
    private final String processed = "processed";
    private final String pay_issync = "pay_issync";
    private final String pay_transid = "pay_transid";
    private final String pay_refnum = "pay_refnum";
    private final String pay_name = "pay_name";
    private final String pay_addr = "pay_addr";
    private final String pay_poscode = "pay_poscode";
    private final String pay_seccode = "pay_seccode";
    private final String pay_maccount = "pay_maccount";
    private final String pay_groupcode = "pay_groupcode";
    private final String pay_stamp = "pay_stamp";
    private final String pay_resultcode = "pay_resultcode";
    private final String pay_resultmessage = "pay_resultmessage";
    private final String pay_ccnum = "pay_ccnum";
    private final String pay_expmonth = "pay_expmonth";
    private final String pay_expyear = "pay_expyear";
    private final String pay_expdate = "pay_expdate";
    private final String pay_result = "pay_result";
    private final String pay_date = "pay_date";
    private final String recordnumber = "recordnumber";
    private final String pay_signature = "pay_signature";
    private final String authcode = "authcode";
    private final String status = "status";

    // added
    private final String job_id = "job_id";
    private final String user_ID = "user_ID";
    private final String pay_type = "pay_type";
    private final String pay_tip = "pay_tip";
    private final String ccnum_last4 = "ccnum_last4";
    private final String pay_phone = "pay_phone";
    private final String pay_email = "pay_email";
    private final String card_type = "card_type";


    private final String isVoid = "isVoid";
    private final String pay_latitude = "pay_latitude";
    private final String pay_longitude = "pay_longitude";
    private final String tipAmount = "tipAmount";
    private final String clerk_id = "clerk_id";
    private final String is_refund = "is_refund";
    private final String ref_num = "ref_num";
    private final String original_pay_id = "original_pay_id";

    private final String IvuLottoDrawDate = "IvuLottoDrawDate";
    private final String IvuLottoNumber = "IvuLottoNumber";
    private final String IvuLottoQR = "IvuLottoQR";

    private final String Tax1_amount = "Tax1_amount";
    private final String Tax1_name = "Tax1_name";
    private final String Tax2_amount = "Tax2_amount";
    private final String Tax2_name = "Tax2_name";

    // Store and Forward Data
    private final String payment_xml = "payment_xml";
    private final String is_retry = "is_retry";
    private final String pay_uuid = "pay_uuid";
    private final String EMVJson = "EMV_JSON";


    public final List<String> attr = Arrays.asList(pay_id, group_pay_id, cust_id, tupyx_user_id, emp_id,
            inv_id, paymethod_id, pay_check, pay_receipt, pay_amount, pay_dueamount, pay_comment, pay_timecreated,
            pay_timesync, account_id, processed, pay_issync, pay_transid, pay_refnum, pay_name, pay_addr, pay_poscode,
            pay_seccode, pay_maccount, pay_groupcode, pay_stamp, pay_resultcode, pay_resultmessage, pay_ccnum,
            pay_expmonth, pay_expyear, pay_expdate, pay_result, pay_date, recordnumber, pay_signature, authcode, status,
            job_id, user_ID, pay_type, pay_tip, ccnum_last4, pay_phone, pay_email, isVoid, pay_latitude, pay_longitude,
            tipAmount, clerk_id, is_refund, ref_num, IvuLottoDrawDate, IvuLottoNumber, IvuLottoQR, card_type,
            Tax1_amount, Tax1_name, Tax2_amount, Tax2_name, custidkey, original_pay_id, pay_uuid, is_retry,
            payment_xml,EMVJson);

    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;
    private Global global;
    private static final String table_name = "StoredPayments";
    private Activity activity;

    public StoredPayments_DB(Activity activity) {
        global = (Global) activity.getApplication();
        this.activity = activity;
        attrHash = new HashMap<>();
        sb1 = new StringBuilder();
        sb2 = new StringBuilder();

        initDictionary();
    }

    public void initDictionary() {
        int size = attr.size();
        for (int i = 0; i < size; i++) {
            attrHash.put(attr.get(i), i + 1);
            if ((i + 1) < size) {
                sb1.append(attr.get(i)).append(",");
                sb2.append("?").append(",");
            } else {
                sb1.append(attr.get(i));
                sb2.append("?");
            }
        }
    }

    public int index(String tag) {
        return attrHash.get(tag);
    }

    public void insert(Payment payment) {

        DBManager._db.beginTransaction();
        try {

            SQLiteStatement insert;
            String sb = "INSERT INTO " + table_name + " (" + sb1.toString() + ") " +
                    "VALUES (" + sb2.toString() + ")";
            insert = DBManager._db.compileStatement(sb);

            insert.bindString(index(pay_id), payment.getPay_id()); // pay_id
            insert.bindString(index(group_pay_id), payment.getGroup_pay_id()); // group_pay_id
            insert.bindString(index(original_pay_id), payment.getOriginal_pay_id()); // group_pay_id
            insert.bindString(index(cust_id), payment.getCust_id()); // cust_id
            insert.bindString(index(tupyx_user_id), payment.getTupyx_user_id());
            insert.bindString(index(custidkey), payment.getCustidkey()); // custidkey
            insert.bindString(index(emp_id), payment.getEmp_id()); // emp_id
            insert.bindString(index(inv_id), payment.getInv_id() == null ? "" : payment.getInv_id()); // inv_id
            insert.bindString(index(paymethod_id), payment.getPaymethod_id()); // paymethod_id
            insert.bindString(index(pay_check), payment.getPay_check()); // pay_check
            insert.bindString(index(pay_receipt), payment.getPay_receipt()); // pay_receipt
            insert.bindString(index(pay_amount), payment.getPay_amount()); // pay_amount
            insert.bindString(index(pay_dueamount), payment.getPay_dueamount()); // pay_dueamount;
            insert.bindString(index(pay_comment), payment.getPay_comment()); // pay_comment
            insert.bindString(index(pay_timecreated), payment.getPay_timecreated()); // pay_timecreated
            insert.bindString(index(pay_timesync), payment.getPay_timesync()); // pay_timesync
            insert.bindString(index(account_id), payment.getAccount_id()); // account_id
            insert.bindString(index(processed), payment.getProcessed()); // processed
            insert.bindString(index(pay_issync), payment.getPay_issync()); // pay_issync
            insert.bindString(index(pay_transid), payment.getPay_transid() == null ? "" : payment.getPay_transid()); // pay_transid
            insert.bindString(index(pay_refnum), payment.getPay_refnum()); // pay_refnum
            insert.bindString(index(pay_name), payment.getPay_name()); // pay_name
            insert.bindString(index(pay_addr), payment.getPay_addr()); // pay_addr
            insert.bindString(index(pay_poscode), payment.getPay_poscode()); // pay_poscode
            insert.bindString(index(pay_seccode), payment.getPay_seccode()); // pay_seccode
            insert.bindString(index(pay_maccount), payment.getPay_maccount()); // pay_maccount
            insert.bindString(index(pay_groupcode), payment.getPay_groupcode()); // pay_groupcode
            insert.bindString(index(pay_stamp), payment.getPay_stamp()); // pay_stamp
            insert.bindString(index(pay_resultcode), payment.getPay_resultcode()); // pay_resultcode
            insert.bindString(index(pay_resultmessage), payment.getPay_resultmessage()); // pay_resultmessage
            insert.bindString(index(pay_ccnum), payment.getPay_ccnum()); // pay_ccnum
            insert.bindString(index(pay_expmonth), payment.getPay_expmonth()); // pay_expMonth
            insert.bindString(index(pay_expyear), payment.getPay_expyear()); // pay_expyear
            insert.bindString(index(pay_expdate), payment.getPay_expdate()); // pay_expdate
            insert.bindString(index(pay_result), payment.getPay_result()); // pay_result
            insert.bindString(index(pay_date), payment.getPay_date()); // pay_date
            insert.bindString(index(recordnumber), payment.getRecordnumber()); // recordnumber
            insert.bindString(index(pay_signature), payment.getPay_signature()); // pay_signaute
            insert.bindString(index(authcode), payment.getAuthcode() == null ? "" : payment.getAuthcode()); // authcode
            insert.bindString(index(status), payment.getStatus()); // status
            insert.bindString(index(job_id), payment.getJob_id()); // job_id

            insert.bindString(index(user_ID), payment.getUser_ID()); // user_ID
            insert.bindString(index(pay_type), payment.getPay_type()); // pay_type
            insert.bindString(index(pay_tip), payment.getPay_tip()); // pay_tip
            insert.bindString(index(ccnum_last4), payment.getCcnum_last4()); // ccnum_last4
            insert.bindString(index(pay_phone), payment.getPay_phone()); // pay_phone
            insert.bindString(index(pay_email), payment.getPay_email()); // pay_email
            insert.bindString(index(isVoid), payment.getIsVoid()); // isVoid
            insert.bindString(index(pay_latitude), payment.getPay_latitude()); // pay_latitude
            insert.bindString(index(pay_longitude), payment.getPay_longitude()); // pay_longitude
            insert.bindString(index(tipAmount), payment.getTipAmount()); // tipAmount
            insert.bindString(index(clerk_id), payment.getClerk_id() ==null?"": payment.getClerk_id()); // clerk_id

            insert.bindString(index(is_refund), payment.getIs_refund() ==null?"": payment.getIs_refund()); // is_refund
            insert.bindString(index(ref_num), payment.getRef_num()); // ref_num
            insert.bindString(index(card_type), payment.getCard_type()); // card_type

            insert.bindString(index(IvuLottoDrawDate), payment.getIvuLottoDrawDate()); // IvuLottoDrawData
            insert.bindString(index(IvuLottoNumber), payment.getIvuLottoNumber()); // IvuLottoNumber
            insert.bindString(index(IvuLottoQR), payment.getIvuLottoQR()); // IvuLottoQR

            insert.bindString(index(Tax1_amount), payment.getTax1_amount());
            insert.bindString(index(Tax1_name), payment.getTax1_name());
            insert.bindString(index(Tax2_amount), payment.getTax2_amount());
            insert.bindString(index(Tax2_name), payment.getTax2_name());

            insert.bindString(index(pay_uuid), payment.getPay_uuid());
            insert.bindString(index(is_retry), payment.getIs_retry());
            insert.bindString(index(payment_xml), payment.getPayment_xml());
            insert.bindString(index(EMVJson), payment.getEmvContainer() == null ? "" : new Gson().toJson(payment.getEmvContainer(), EMVContainer.class));

            insert.execute();
            insert.clearBindings();
            insert.close();
            DBManager._db.setTransactionSuccessful();

        } catch (Exception e) {

        } finally {
            PaymentsHandler.setLastPaymentInserted(payment);
            DBManager._db.endTransaction();
        }
    }

    public void emptyTable() {
        DBManager._db.execSQL("DELETE FROM " + table_name);
    }

    public String updateSignaturePayment(String _pay_uuid) {

        StringBuilder sb = new StringBuilder();
        sb.append(pay_id).append(" = ?");

        ContentValues args = new ContentValues();

        args.put(pay_signature, global.encodedImage);

        DBManager._db.update(table_name, args, sb.toString(), new String[]{_pay_uuid});
        sb.setLength(0);
        sb.append("SELECT pay_amount FROM ").append(table_name).append(" WHERE pay_uuid = '").append(_pay_uuid)
                .append("'");
        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
        String returningVal = "";

        if (cursor.moveToFirst()) {
            returningVal = cursor.getString(cursor.getColumnIndex(pay_amount));
        }

        cursor.close();
        return returningVal;
    }

    public Cursor getStoredPayments() {
        Cursor c = DBManager._db.rawQuery("SELECT pay_uuid as '_id', * FROM " + table_name, null);
        c.moveToFirst();
        return c;
    }

    public long getRetryTransCount(String _job_id) {
        String sb = "SELECT Count(*) FROM " + table_name + " WHERE job_id = '" + _job_id +
                "' AND is_retry = '1'";
        SQLiteStatement stmt = DBManager._db.compileStatement(sb);
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public long getCountPendingStoredPayments(String _job_id) {
        SQLiteStatement stmt = DBManager._db.compileStatement("SELECT Count(*) FROM " + table_name + " WHERE job_id = '" + _job_id + "'");
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public void deletePaymentFromJob(String _job_id) {
        DBManager._db.delete(table_name, "job_id = ?", new String[]{_job_id});
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

        String sb = "SELECT p.pay_amount AS 'pay_amount',pm.paymethod_name AS 'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM StoredPayments p," +
                "PayMethods pm WHERE p.paymethod_id = pm.paymethod_id AND p.job_id = '" + jobID +
                "' UNION " +
                "SELECT p.pay_amount AS 'pay_amount','Wallet' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM StoredPayments p WHERE p.paymethod_id = 'Wallet' " +
                "AND p.job_id = '" + jobID + "' UNION " +
                "SELECT p.pay_amount AS 'pay_amount','LoyaltyCard' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM StoredPayments p WHERE p.paymethod_id = 'LoyaltyCard' " +
                "AND p.job_id = '" + jobID + "' UNION " +
                "SELECT p.pay_amount AS 'pay_amount','Reward' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM StoredPayments p WHERE p.paymethod_id = 'Reward' " +
                "AND p.job_id = '" + jobID + "' UNION " +
                "SELECT p.pay_amount AS 'pay_amount','GiftCard' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM StoredPayments p WHERE p.paymethod_id = 'GiftCard' " +
                "AND p.job_id = '" + jobID + "'";
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

    public void deleteStoredPaymentRow(String _pay_uuid) {
        DBManager._db.delete(table_name, "pay_uuid = ?", new String[]{_pay_uuid});
    }

    public void updateStoredPaymentForRetry(String _pay_uuid) {
        ContentValues args = new ContentValues();
        args.put(is_retry, "1");
        DBManager._db.update(table_name, args, "pay_uuid = ?", new String[]{_pay_uuid});
    }

    public boolean unsyncStoredPaymentsLeft() {

        SQLiteStatement stmt = DBManager._db.compileStatement("SELECT Count(*) FROM " + table_name);
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count != 0;
    }
}
