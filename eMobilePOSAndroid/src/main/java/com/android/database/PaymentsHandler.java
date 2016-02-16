package com.android.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.PaymentDetails;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.google.gson.Gson;
import com.android.support.NumberUtils;

import net.sqlcipher.database.SQLiteStatement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PaymentsHandler {

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
    private final String amount_tender = "amount_tender";
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
    private final String EMVJson = "EMV_JSON";

    private static Payment lastPaymentInserted;

    public static Payment getLastPaymentInserted() {
        return lastPaymentInserted;
    }

    public final List<String> attr = Arrays.asList(pay_id, group_pay_id, cust_id, tupyx_user_id, emp_id,
            inv_id, paymethod_id, pay_check, pay_receipt, pay_amount, pay_dueamount, pay_comment, pay_timecreated,
            pay_timesync, account_id, processed, pay_issync, pay_transid, pay_refnum, pay_name, pay_addr, pay_poscode,
            pay_seccode, pay_maccount, pay_groupcode, pay_stamp, pay_resultcode, pay_resultmessage, pay_ccnum,
            pay_expmonth, pay_expyear, pay_expdate, pay_result, pay_date, recordnumber, pay_signature, authcode, status,
            job_id, user_ID, pay_type, pay_tip, ccnum_last4, pay_phone, pay_email, isVoid, pay_latitude, pay_longitude,
            tipAmount, clerk_id, is_refund, ref_num, IvuLottoDrawDate, IvuLottoNumber, IvuLottoQR, card_type,
            Tax1_amount, Tax1_name, Tax2_amount, Tax2_name, custidkey, original_pay_id, EMVJson, amount_tender);

    private StringBuilder sb1, sb2;
    private HashMap<String, Integer> attrHash;
    private Global global;
    private MyPreferences myPref;
    private static final String table_name = "Payments";
    private static final String table_name_declined = "PaymentsDeclined";
    private Activity activity;

    public PaymentsHandler(Activity activity) {
        global = (Global) activity.getApplication();
        myPref = new MyPreferences(activity);
        this.activity = activity;
        attrHash = new HashMap<String, Integer>();
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
            insert = DBManager._db.compileStatement("INSERT INTO " + table_name + " (" + sb1.toString() + ")" +
                    "VALUES (" + sb2.toString() + ")");
            insert.bindString(index(pay_id), payment.pay_id == null ? "" : payment.pay_id); // pay_id
            insert.bindString(index(group_pay_id), payment.group_pay_id == null ? "" : payment.group_pay_id); // group_pay_id
            insert.bindString(index(original_pay_id), payment.original_pay_id == null ? "" : payment.original_pay_id); // group_pay_id
            insert.bindString(index(cust_id), payment.cust_id == null ? "" : payment.cust_id); // cust_id
            insert.bindString(index(tupyx_user_id), payment.tupyx_user_id == null ? "" : payment.tupyx_user_id);
            insert.bindString(index(custidkey), payment.custidkey == null ? "" : payment.custidkey); // custidkey
            insert.bindString(index(emp_id), payment.emp_id == null ? "" : payment.emp_id); // emp_id
            insert.bindString(index(inv_id), payment.inv_id == null ? "" : payment.inv_id); // inv_id
            insert.bindString(index(paymethod_id), payment.paymethod_id == null ? "" : payment.paymethod_id); // paymethod_id
            insert.bindString(index(pay_check), payment.pay_check == null ? "" : payment.pay_check); // pay_check
            insert.bindString(index(pay_receipt), payment.pay_receipt == null ? "" : payment.pay_receipt); // pay_receipt
            insert.bindString(index(pay_amount), TextUtils.isEmpty(payment.pay_amount) ? "0" : payment.pay_amount); // pay_amount
            insert.bindString(index(pay_dueamount),
                    TextUtils.isEmpty(payment.pay_dueamount) ? "0" : payment.pay_dueamount); // pay_dueamount;
            insert.bindString(index(pay_comment), payment.pay_comment == null ? "" : payment.pay_comment); // pay_comment
            insert.bindString(index(pay_timecreated), payment.pay_timecreated == null ? "" : payment.pay_timecreated); // pay_timecreated
            insert.bindString(index(pay_timesync), payment.pay_timesync == null ? "" : payment.pay_timesync); // pay_timesync
            insert.bindString(index(account_id), payment.account_id == null ? "" : payment.account_id); // account_id
            insert.bindString(index(processed), TextUtils.isEmpty(payment.processed) ? "0" : payment.processed); // processed
            insert.bindString(index(pay_issync), TextUtils.isEmpty(payment.pay_issync) ? "0" : payment.pay_issync); // pay_issync
            insert.bindString(index(pay_transid), payment.pay_transid == null ? "" : payment.pay_transid); // pay_transid
            insert.bindString(index(pay_refnum), payment.pay_refnum == null ? "" : payment.pay_refnum); // pay_refnum
            insert.bindString(index(pay_name), payment.pay_name == null ? "" : payment.pay_name); // pay_name
            insert.bindString(index(pay_addr), payment.pay_addr == null ? "" : payment.pay_addr); // pay_addr
            insert.bindString(index(pay_poscode), payment.pay_poscode == null ? "" : payment.pay_poscode); // pay_poscode
            insert.bindString(index(pay_seccode), payment.pay_seccode == null ? "" : payment.pay_seccode); // pay_seccode
            insert.bindString(index(pay_maccount), payment.pay_maccount == null ? "" : payment.pay_maccount); // pay_maccount
            insert.bindString(index(pay_groupcode), payment.pay_groupcode == null ? "" : payment.pay_groupcode); // pay_groupcode
            insert.bindString(index(pay_stamp), payment.pay_stamp == null ? "" : payment.pay_stamp); // pay_stamp
            insert.bindString(index(pay_resultcode), payment.pay_resultcode == null ? "" : payment.pay_resultcode); // pay_resultcode
            insert.bindString(index(pay_resultmessage),
                    payment.pay_resultmessage == null ? "" : payment.pay_resultmessage); // pay_resultmessage
            insert.bindString(index(pay_ccnum), payment.pay_ccnum == null ? "" : payment.pay_ccnum); // pay_ccnum
            insert.bindString(index(pay_expmonth), payment.pay_expmonth == null ? "" : payment.pay_expmonth); // pay_expMonth
            insert.bindString(index(pay_expyear), payment.pay_expyear == null ? "" : payment.pay_expyear); // pay_expyear
            insert.bindString(index(pay_expdate), payment.pay_expdate == null ? "" : payment.pay_expdate); // pay_expdate
            insert.bindString(index(pay_result), payment.pay_result == null ? "" : payment.pay_result); // pay_result
            insert.bindString(index(pay_date), payment.pay_date == null ? "" : payment.pay_date); // pay_date
            insert.bindString(index(recordnumber), payment.recordnumber == null ? "" : payment.recordnumber); // recordnumber
            insert.bindString(index(pay_signature), payment.pay_signature == null ? "" : payment.pay_signature); // pay_signaute
            insert.bindString(index(authcode), payment.authcode == null ? "" : payment.authcode); // authcode
            insert.bindString(index(status), payment.status == null ? "" : payment.status); // status
            insert.bindString(index(job_id), payment.job_id == null ? "" : payment.job_id); // job_id
            insert.bindDouble(index(amount_tender), payment.amountTender == null ? 0 : payment.amountTender); // user_ID

            insert.bindString(index(user_ID), payment.user_ID == null ? "" : payment.user_ID); // user_ID
            insert.bindString(index(pay_type), payment.pay_type == null ? "" : payment.pay_type); // pay_type
            insert.bindString(index(pay_tip), TextUtils.isEmpty(payment.pay_tip) ? "0" : payment.pay_tip); // pay_tip
            insert.bindString(index(ccnum_last4), payment.ccnum_last4 == null ? "" : payment.ccnum_last4); // ccnum_last4
            insert.bindString(index(pay_phone), payment.pay_phone == null ? "" : payment.pay_phone); // pay_phone
            insert.bindString(index(pay_email), payment.pay_email == null ? "" : payment.pay_email); // pay_email
            insert.bindString(index(isVoid), TextUtils.isEmpty(payment.isVoid) ? "0" : payment.isVoid); // isVoid
            insert.bindString(index(pay_latitude), payment.pay_latitude == null ? "" : payment.pay_latitude); // pay_latitude
            insert.bindString(index(pay_longitude), payment.pay_longitude == null ? "" : payment.pay_longitude); // pay_longitude
            insert.bindString(index(tipAmount), TextUtils.isEmpty(payment.tipAmount) ? "0" : payment.tipAmount); // tipAmount
            insert.bindString(index(clerk_id), payment.clerk_id == null ? "" : payment.clerk_id); // clerk_id

            insert.bindString(index(is_refund), TextUtils.isEmpty(payment.is_refund) ? "0" : payment.is_refund); // is_refund
            insert.bindString(index(ref_num), payment.ref_num == null ? "" : payment.ref_num); // ref_num
            insert.bindString(index(card_type), payment.card_type == null ? "" : payment.card_type); // card_type

            insert.bindString(index(IvuLottoDrawDate),
                    payment.IvuLottoDrawDate == null ? "" : payment.IvuLottoDrawDate); // IvuLottoDrawData
            insert.bindString(index(IvuLottoNumber), payment.IvuLottoNumber == null ? "" : payment.IvuLottoNumber); // IvuLottoNumber
            insert.bindString(index(IvuLottoQR), payment.IvuLottoQR == null ? "" : payment.IvuLottoQR); // IvuLottoQR

            insert.bindString(index(Tax1_amount), TextUtils.isEmpty(payment.Tax1_amount) ? "0" : payment.Tax1_amount);
            insert.bindString(index(Tax1_name), payment.Tax1_name == null ? "" : payment.Tax1_name);
            insert.bindString(index(Tax2_amount), TextUtils.isEmpty(payment.Tax2_amount) ? "0" : payment.Tax2_amount);
            insert.bindString(index(Tax2_name), payment.Tax2_name == null ? "" : payment.Tax2_name);
            insert.bindString(index(EMVJson), payment.emvContainer == null ? "" : new Gson().toJson(payment.emvContainer, EMVContainer.class));


            insert.execute();
            insert.clearBindings();
            insert.close();
            DBManager._db.setTransactionSuccessful();

        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.getMessage()).append(" [com.android.emobilepos.PaymentsHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
        } finally {
            myPref.setLastPayID(payment.pay_id);
            DBManager._db.endTransaction();
            lastPaymentInserted = payment;
        }
    }

    public void emptyTable() {
        DBManager._db.execSQL("DELETE FROM " + table_name);
    }


    public long getDBSize() {


        SQLiteStatement stmt = DBManager._db.compileStatement("SELECT Count(*) FROM " + table_name);
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public Cursor getUnsyncPayments() // Will return Cursor to all
    // unsynchronized payments (used in
    // generation of XML for post)
    {

        return DBManager._db.rawQuery("SELECT " + sb1.toString() + " FROM " + table_name + " WHERE pay_issync = '0'", null);
    }

    public String getTotalPayAmount(String paymethod_id, String pay_date) {

        StringBuilder sb = new StringBuilder();
        if (paymethod_id.equals("Genius")) {
            sb.append(
                    "SELECT ROUND(SUM(pay_amount),2) AS 'total',date(pay_timecreated,'localtime') as 'date' FROM Payments ");
            sb.append("WHERE  paymethod_id = '' OR paymethod_id = '").append(paymethod_id);
        } else
            sb.append(
                    "SELECT ROUND(SUM(pay_amount),2) AS 'total',date(pay_timecreated,'localtime') as 'date' FROM Payments WHERE  paymethod_id = '")
                    .append(paymethod_id);

        sb.append("' AND date = '").append(pay_date).append("' AND is_refund='0' AND isVoid != '1'");

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
        String total = "0.00";
        if (cursor.moveToFirst()) {
            total = cursor.getString(cursor.getColumnIndex("total"));
            if (total == null)
                total = "0.00";
        }
        cursor.close();
        return total;
    }

    public String getTotalRefundAmount(String paymethod_id, String pay_date) {


        Cursor cursor = DBManager._db.rawQuery("SELECT ROUND(SUM(pay_amount),2) AS 'total',date(pay_timecreated,'localtime') as 'date' FROM Payments WHERE  paymethod_id = '" + paymethod_id + "' AND date = '" + pay_date + "' AND is_refund = '1' AND isVoid != '1'", null);
        String total = "0.00";
        if (cursor.moveToFirst()) {
            total = cursor.getString(cursor.getColumnIndex("total"));
            if (total == null)
                total = "0.00";
        }
        cursor.close();
        return total;
    }

    public long getNumUnsyncPayments() {


        SQLiteStatement stmt = DBManager._db.compileStatement("SELECT Count(*) FROM " + table_name + " WHERE pay_issync = '0'");
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public boolean unsyncPaymentsLeft() {

        SQLiteStatement stmt = DBManager._db.compileStatement("SELECT Count(*) FROM " + table_name + " WHERE pay_issync = '0'");
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count != 0;
    }

    public long paymentExist(String pay_id) {
        return paymentExist(pay_id, false);

    }

    public long paymentExist(String pay_id, boolean includeDeclined) {

        String sql = "select count(*) from %s WHERE pay_id = '%s'";
        if (includeDeclined) {
            sql = "select count(*) from (SELECT pay_id FROM %s" +
                    " union " +
                    "SELECT pay_id from %s) WHERE pay_id = '%s'";
            sql = String.format(sql, table_name, table_name_declined, pay_id);
        } else {
            sql = String.format(sql, table_name, pay_id);
        }
        SQLiteStatement stmt = DBManager._db.compileStatement(sql);
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public void updateIsSync(List<String[]> list) {

        StringBuilder sb = new StringBuilder();
        sb.append(pay_id).append(" = ?");

        ContentValues args = new ContentValues();

        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i)[0].equals("0"))
                args.put(pay_issync, "1");
            else
                args.put(pay_issync, "0");
            DBManager._db.update(table_name, args, sb.toString(), new String[]{list.get(i)[1]});
        }
    }

    public String updateSignaturePayment(String payID) {

        StringBuilder sb = new StringBuilder();
        sb.append(pay_id).append(" = ?");

        ContentValues args = new ContentValues();

        args.put(pay_signature, global.encodedImage);

        DBManager._db.update(table_name, args, sb.toString(), new String[]{payID});
        sb.setLength(0);
        sb.append("SELECT pay_amount FROM Payments WHERE pay_id = '").append(payID).append("'");
        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
        String returningVal = "";
        if (cursor.moveToFirst()) {
            returningVal = cursor.getString(cursor.getColumnIndex("pay_amount"));
        }

        cursor.close();
        return returningVal;
    }

    public void updateIsVoid(String param) {

        ContentValues args = new ContentValues();

        args.put(isVoid, "1");
        DBManager._db.update(table_name, args, pay_id + " = ?", new String[]{param});
    }

    public void createVoidPayment(Payment payment, boolean onlineVoid, HashMap<String, String> response) {
        GenerateNewID idGenerator = new GenerateNewID(activity);
        this.updateIsVoid(payment.pay_id);
        String _ord_id = payment.job_id;
        String _orig_pay_id = payment.pay_id;


        payment.pay_id = idGenerator.getNextID(IdType.PAYMENT_ID);
        payment.pay_type = "1";
        payment.isVoid = "1";
        payment.pay_issync = "0";

        payment.original_pay_id = _orig_pay_id;
        payment.pay_timecreated = Global.getCurrentDate();
        payment.pay_date = Global.getCurrentDate();

        if (onlineVoid) {
            payment.pay_resultcode = response.get(pay_resultcode);
            payment.pay_resultmessage = response.get(pay_resultmessage);
            payment.pay_transid = response.get("CreditCardTransID");
            payment.authcode = response.get("AuthorizationCode");
        }

        this.insert(payment);

        if (_ord_id != null && !_ord_id.isEmpty()) {
            OrdersHandler tempHandler = new OrdersHandler(activity);
            tempHandler.updateIsVoid(_ord_id);
        }
    }

    public PaymentDetails getPaymentDetails(String payID) {
        return getPaymentDetails(payID, false);
    }

    public PaymentDetails getPaymentDetails(String payID, boolean isDeclined) {
        String tableName = table_name;
        if (isDeclined)
            tableName = table_name_declined;
        String sql = "SELECT pay_date,pay_comment, amount_tender, job_id, inv_id,group_pay_id,pay_signature,ccnum_last4," +
                " amount_tender, pay_latitude,pay_longitude,isVoid,pay_transid," + "authcode,clerk_id FROM " + tableName +
                " WHERE pay_id = ?";

        PaymentDetails paymentDetails = new PaymentDetails();
        Cursor cursor = DBManager._db.rawQuery(sql, new String[]{payID});
        if (cursor.moveToFirst()) {
            do {
                paymentDetails.setPay_date(Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(pay_date)), activity,
                        0));
                paymentDetails.setPay_comment(cursor.getString(cursor.getColumnIndex(pay_comment)));
                paymentDetails.setInv_id(cursor.getString(cursor.getColumnIndex(inv_id)));// is
                // actually
                // job_id
                // as
                // 'inv_id'
                paymentDetails.setGroup_pay_id(cursor.getString(cursor.getColumnIndex(group_pay_id)));
                paymentDetails.setPay_signature(cursor.getString(cursor.getColumnIndex(pay_signature)));
                paymentDetails.setCcnum_last4(cursor.getString(cursor.getColumnIndex(ccnum_last4)));
                paymentDetails.setPay_latitude(cursor.getString(cursor.getColumnIndex(pay_latitude)));
                paymentDetails.setPay_longitude(cursor.getString(cursor.getColumnIndex(pay_longitude)));
                paymentDetails.setJob_id(cursor.getString(cursor.getColumnIndex(job_id)));
                paymentDetails.setIsVoid(cursor.getString(cursor.getColumnIndex(isVoid)));
                paymentDetails.setAuthcode(cursor.getString(cursor.getColumnIndex(authcode)));
                paymentDetails.setAmountTender(cursor.getDouble(cursor.getColumnIndex(amount_tender)));
                paymentDetails.setPay_transid(cursor.getString(cursor.getColumnIndex(pay_transid)));
                paymentDetails.setClerk_id(cursor.getString(cursor.getColumnIndex(clerk_id)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return paymentDetails;
    }

    public Payment getPaymentForVoid(String payID) {
        Payment payment = new Payment(this.activity);

        Cursor cursor = DBManager._db.rawQuery("SELECT * FROM Payments WHERE pay_id = '" + payID + "'", null);

        if (cursor.moveToFirst()) {
            cursorToPayment(cursor, payment);

        }

        cursor.close();
        return payment;
    }

    public List<Payment> getOrderPayments(String _ordID) {
        Payment payment = new Payment(this.activity);

        Cursor c = DBManager._db.rawQuery("SELECT * FROM Payments WHERE job_id = '" + _ordID + "'", null);

        List<Payment> listPayment = new ArrayList<Payment>();
        if (c.moveToFirst()) {
            do {
                cursorToPayment(c, payment);
                listPayment.add(payment);
                payment = new Payment(activity);
            } while (c.moveToNext());
        }

        c.close();
        return listPayment;
    }

    private void cursorToPayment(Cursor c, Payment payment) {
        payment.pay_id = c.getString(c.getColumnIndex(pay_id));
        payment.group_pay_id = c.getString(c.getColumnIndex(group_pay_id));
        payment.cust_id = c.getString(c.getColumnIndex(cust_id));
        payment.tupyx_user_id = c.getString(c.getColumnIndex(tupyx_user_id));
        payment.emp_id = c.getString(c.getColumnIndex(emp_id));
        payment.inv_id = c.getString(c.getColumnIndex(inv_id));
        payment.paymethod_id = c.getString(c.getColumnIndex(paymethod_id));
        payment.pay_check = c.getString(c.getColumnIndex(pay_check));
        payment.pay_receipt = c.getString(c.getColumnIndex(pay_receipt));
        payment.pay_amount = c.getString(c.getColumnIndex(pay_amount));
        payment.pay_dueamount = c.getString(c.getColumnIndex(pay_dueamount));
        payment.pay_comment = c.getString(c.getColumnIndex(pay_comment));
        payment.pay_timecreated = c.getString(c.getColumnIndex(pay_timecreated));
        payment.pay_timesync = c.getString(c.getColumnIndex(pay_timesync));
        payment.account_id = c.getString(c.getColumnIndex(account_id));
        payment.processed = c.getString(c.getColumnIndex(processed));
        payment.pay_issync = c.getString(c.getColumnIndex(pay_issync));
        payment.pay_transid = c.getString(c.getColumnIndex(pay_transid));
        payment.pay_refnum = c.getString(c.getColumnIndex(pay_refnum));
        payment.pay_name = c.getString(c.getColumnIndex(pay_name));
        payment.pay_addr = c.getString(c.getColumnIndex(pay_addr));
        payment.pay_poscode = c.getString(c.getColumnIndex(pay_poscode));
        payment.pay_seccode = c.getString(c.getColumnIndex(pay_seccode));
        payment.pay_maccount = c.getString(c.getColumnIndex(pay_maccount));
        payment.pay_groupcode = c.getString(c.getColumnIndex(pay_groupcode));
        payment.pay_stamp = c.getString(c.getColumnIndex(pay_stamp));
        payment.pay_resultcode = c.getString(c.getColumnIndex(pay_resultcode));
        payment.pay_resultmessage = c.getString(c.getColumnIndex(pay_resultmessage));
        payment.pay_ccnum = c.getString(c.getColumnIndex(pay_ccnum));
        payment.pay_expmonth = c.getString(c.getColumnIndex(pay_expmonth));
        payment.pay_expyear = c.getString(c.getColumnIndex(pay_expyear));
        payment.pay_expdate = c.getString(c.getColumnIndex(pay_expdate));
        payment.recordnumber = c.getString(c.getColumnIndex(recordnumber));
        payment.pay_signature = c.getString(c.getColumnIndex(pay_signature));
        payment.authcode = c.getString(c.getColumnIndex(authcode));
        payment.status = c.getString(c.getColumnIndex(status));
        payment.job_id = c.getString(c.getColumnIndex(job_id));
        payment.user_ID = c.getString(c.getColumnIndex(user_ID));
        payment.pay_type = c.getString(c.getColumnIndex(pay_type));
        payment.pay_tip = c.getString(c.getColumnIndex(pay_tip));
        payment.ccnum_last4 = c.getString(c.getColumnIndex(ccnum_last4));
        payment.pay_phone = c.getString(c.getColumnIndex(pay_phone));
        payment.pay_email = c.getString(c.getColumnIndex(pay_email));
        payment.isVoid = c.getString(c.getColumnIndex(isVoid));
        payment.pay_latitude = c.getString(c.getColumnIndex(pay_latitude));
        payment.pay_longitude = c.getString(c.getColumnIndex(pay_longitude));
        payment.tipAmount = c.getString(c.getColumnIndex(tipAmount));
        payment.clerk_id = c.getString(c.getColumnIndex(clerk_id));
        payment.is_refund = c.getString(c.getColumnIndex(is_refund));
        payment.ref_num = c.getString(c.getColumnIndex(ref_num));
        payment.IvuLottoDrawDate = c.getString(c.getColumnIndex(IvuLottoDrawDate));
        payment.IvuLottoNumber = c.getString(c.getColumnIndex(IvuLottoNumber));
        payment.IvuLottoQR = c.getString(c.getColumnIndex(IvuLottoQR));
        payment.card_type = c.getString(c.getColumnIndex(card_type));
        payment.custidkey = c.getString(c.getColumnIndex(custidkey));
        payment.original_pay_id = c.getString(c.getColumnIndex(original_pay_id));
        payment.amountTender = c.getDouble(c.getColumnIndex(amount_tender));
    }

    public List<HashMap<String, String>> getPaymentDetailsForTransactions(String jobID) {

        List<HashMap<String, String>> mapList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();

        Cursor cursor = DBManager._db.rawQuery("SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip'," +
                "pm.paymentmethod_type AS 'paymethod_name', amount_tender,p.pay_id AS 'pay_id' FROM Payments p," + "PayMethods pm " +
                "WHERE p.paymethod_id = pm.paymethod_id AND p.job_id = '" + jobID + "' " +
                "UNION " + "SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip','Wallet' AS  'paymethod_name', amount_tender," +
                "p.pay_id AS 'pay_id' FROM Payments p WHERE p.paymethod_id = 'Wallet' " +
                "AND p.job_id = '" + jobID + "' UNION " +
                "SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip','LoyaltyCard' AS  'paymethod_name', amount_tender," +
                "p.pay_id AS 'pay_id' FROM Payments p WHERE p.paymethod_id = 'LoyaltyCard' " + "AND p.job_id = '" +
                jobID + "' UNION " + "SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip'," +
                "'Reward' AS  'paymethod_name', amount_tender,p.pay_id AS 'pay_id' FROM Payments p WHERE p.paymethod_id = 'Reward' " +
                "AND p.job_id = '" + jobID + "' UNION " + "SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip'," +
                "'GiftCard' AS  'paymethod_name', amount_tender,p.pay_id AS 'pay_id' FROM Payments p " +
                "WHERE p.paymethod_id = 'GiftCard' " + "AND p.job_id = '" + jobID + "' UNION " +
                "SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip','Genius' AS  'paymethod_name', amount_tender," +
                "p.pay_id AS 'pay_id' FROM Payments p WHERE p.paymethod_id = 'Genius' " + "AND p.job_id = '" +
                jobID + "' UNION " + "SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip'," +
                "'Genius' AS  'paymethod_name', amount_tender, p.pay_id AS 'pay_id' FROM Payments p WHERE p.paymethod_id = '' " +
                "AND p.job_id = '" + jobID + "'", null);
        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(cursor.getColumnIndex(pay_amount));
                map.put(pay_amount, data);

                data = cursor.getString(cursor.getColumnIndex(pay_tip));
                map.put(pay_tip, data);

                data = cursor.getString(cursor.getColumnIndex("paymethod_name"));
                map.put("paymethod_name", data);

                data = cursor.getString(cursor.getColumnIndex(pay_id));
                map.put(pay_id, data);

                mapList.add(map);
                map = new HashMap<String, String>();
            } while (cursor.moveToNext());
        }
        cursor.close();
        return mapList;
    }

    public Cursor getCashCheckGiftPayment(String type, boolean isRefund) {

        StringBuilder sb = new StringBuilder();

        sb.append("SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM ")
                .append(table_name)
                .append(" p, PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = m.paymethod_id "
                        + "AND m.paymentmethod_type = '")
                .append(type).append("' AND pay_type !='1' AND is_refund ='");
        if (isRefund)
            sb.append("1' ORDER BY p.pay_id DESC");
        else
            sb.append("0' ORDER BY p.pay_id DESC");

        return DBManager._db.rawQuery(sb.toString(), null);
    }

    public Cursor searchCashCheckGift(String type, String search) {

        String subquery1 = "SELECT p.pay_id as _id, p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = m.paymethod_id AND m.paymentmethod_type = '";// cust_name
        String subquery2 = "' AND pay_type !='1' AND c.cust_name LIKE ? ORDER BY p.pay_id DESC";

        Cursor cursor = DBManager._db.rawQuery(subquery1 + type + subquery2, new String[]{"%" + search + "%"});
        cursor.moveToFirst();

        return cursor;
    }

    public Cursor getCardPayments(boolean isRefund) {
        String is_refund = "0";
        if (isRefund)
            is_refund = "1";
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT 'FALSE' as DECLINED,p.pay_id as _id,p.pay_amount as pay_amount,c.cust_name as cust_name," +
                "p.job_id as job_id,p.isVoid as isVoid,p.pay_issync as pay_issync,p.pay_tip as pay_tip " +
                "FROM " + table_name +
                " p, PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id " +
                "WHERE p.paymethod_id = m.paymethod_id AND " +
                "(m.paymentmethod_type = 'AmericanExpress' OR m.paymentmethod_type = 'Discover' OR " +
                "m.paymentmethod_type = 'MasterCard' OR m.paymentmethod_type = 'Visa') " +
                "AND pay_type !='1'  AND is_refund='" + is_refund + "' " +
                " UNION " +
                "SELECT 'FALSE' as DECLINED,p.pay_id as _id,p.pay_amount as pay_amount,c.cust_name as cust_name," +
                "p.job_id as job_id,p.isVoid as isVoid,p.pay_issync as pay_issync,p.pay_tip as pay_tip " + "FROM " + table_name_declined +
                " p, PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id " +
                "WHERE p.paymethod_id = m.paymethod_id AND " +
                "(m.paymentmethod_type = 'AmericanExpress' OR m.paymentmethod_type = 'Discover' OR " +
                "m.paymentmethod_type = 'MasterCard' OR m.paymentmethod_type = 'Visa') " +
                "AND pay_type !='1'  AND is_refund='" + is_refund + "' ");
//
//        if (isRefund)
//            sb.append("1' ORDER BY p.pay_id DESC");
//        else
//            sb.append("0' ORDER BY p.pay_id DESC");

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

        return cursor;
    }

    public Cursor searchCards(String search) {

        String subquery1 = "SELECT p.pay_id as _id, p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = m.paymethod_id AND (m.paymentmethod_type = ";// cust_name
        String subquery2 = "'AmericanExpress' OR m.paymentmethod_type = 'Discover' OR m.paymentmethod_type = 'MasterCard' OR m.paymentmethod_type = 'Visa') AND c.cust_name LIKE '%";
        String subquery3 = "%' ORDER BY p.pay_id DESC";

        Cursor cursor = DBManager._db.rawQuery(subquery1 + subquery2 + search + subquery3, null);
        cursor.moveToFirst();

        return cursor;
    }

    public Cursor getOtherPayments(boolean isRefund) {
        StringBuilder sb = new StringBuilder();
        String is_refund = "0";
        if (isRefund)
            is_refund = "1";
        sb.append(
                "SELECT 'FALSE' as DECLINED, p.pay_id as _id,p.pay_amount as pay_amount,c.cust_name as cust_name,p.job_id as job_id,p.isVoid as isVoid,p.pay_issync as pay_issync," +
                        "p.pay_tip as pay_tip FROM Payments p, " +
                        "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE   p.paymethod_id = 'Wallet' " +
                        "AND pay_type!=1 OR (p.paymethod_id = m.paymethod_id AND pay_type !='1'  AND " +
                        "(m.paymentmethod_type != 'AmericanExpress' AND m.paymentmethod_type != 'Discover' AND " +
                        "m.paymentmethod_type != 'MasterCard' AND m.paymentmethod_type != 'Visa' AND " +
                        "m.paymentmethod_type != 'Cash' AND m.paymentmethod_type != 'GiftCard')) OR p.paymethod_id = 'Genius' OR p.paymethod_id = '' " +
                        " AND is_refund = '" + is_refund + "' GROUP BY p.pay_id " +
                        "UNION " +
                        "SELECT 'TRUE' as DECLINED, p.pay_id as _id,p.pay_amount as pay_amount,c.cust_name as cust_name,p.job_id as job_id,p.isVoid as isVoid,p.pay_issync as pay_issync," +
                        "p.pay_tip as pay_tip FROM PaymentsDeclined p, " +
                        "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE   p.paymethod_id = 'Wallet' " +
                        "AND pay_type!=1 OR (p.paymethod_id = m.paymethod_id AND pay_type !='1'  AND " +
                        "(m.paymentmethod_type != 'AmericanExpress' AND m.paymentmethod_type != 'Discover' AND " +
                        "m.paymentmethod_type != 'MasterCard' AND m.paymentmethod_type != 'Visa' AND " +
                        "m.paymentmethod_type != 'Cash' AND m.paymentmethod_type != 'GiftCard')) OR p.paymethod_id = 'Genius' OR p.paymethod_id = '' " +
                        " AND is_refund = '" + is_refund + "' GROUP BY p.pay_id "
        );

        return DBManager._db.rawQuery(sb.toString(), null);
    }

    public Cursor getLoyaltyPayments() {

        return DBManager._db.rawQuery("SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, " + "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'LoyaltyCard' " + "AND pay_type!=1 " + " AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ", null);
    }

    public Cursor getLoyaltyAddBalance() {

        return DBManager._db.rawQuery("SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, " + "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'LoyaltyCardBalance' " + "AND pay_type!=1 " + " AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ", null);
    }

    public Cursor getGiftCardAddBalance() {


        return DBManager._db.rawQuery("SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, " + "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'GiftCardBalance' " + "AND pay_type!=1 " + " AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ", null);
    }

    public Cursor getRewardPayments() {

        return DBManager._db.rawQuery("SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, " + "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'Reward' " + "AND pay_type!=1 " + " AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ", null);
    }

    public Cursor getRewardAddBalance() {

        return DBManager._db.rawQuery("SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, " + "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'RewardBalance' " + "AND pay_type!=1 " + " AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ", null);
    }

    public Cursor searchOther(String search) {

        String subquery1 = "SELECT p.pay_id as _id, p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, PayMethods m LEFT OUTER JOIN "
                + "Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = m.paymethod_id AND pay_type !='1'  AND (m.paymentmethod_type != ";// cust_name
        String subquery2 = "'AmericanExpress' AND m.paymentmethod_type != 'Discover' AND m.paymentmethod_type != 'MasterCard' "
                + "AND m.paymentmethod_type != 'Visa' AND m.paymentmethod_type != 'Cash' AND m.paymentmethod_type != 'GiftCard') AND c.cust_name LIKE ? ORDER BY p.pay_id DESC";

        Cursor cursor = DBManager._db.rawQuery(subquery1 + subquery2, new String[]{"%" + search + "%"});
        cursor.moveToFirst();

        return cursor;
    }

    public List<PaymentDetails> getPaymentForPrintingTransactions(String jobID) {

        List<PaymentDetails> list = new ArrayList<PaymentDetails>();

        Cursor cursor = DBManager._db.rawQuery("SELECT p.pay_id, p.pay_amount AS 'pay_amount', amount_tender,pm.paymethod_name AS 'paymethod_name'," +
                "p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid'," +
                "p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate'," +
                "p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' " +
                "FROM Payments p," + "PayMethods pm WHERE p.paymethod_id = pm.paymethod_id " +
                "AND p.job_id = '" + jobID + "' UNION " + "SELECT p.pay_id, p.pay_amount AS 'pay_amount', amount_tender, " +
                "'Wallet' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature'," +
                "p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate'," +
                "p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' " +
                "FROM Payments p WHERE p.paymethod_id = 'Wallet' " + "AND p.job_id = '" + jobID + "' UNION " + "SELECT " +
                "p.pay_id,  p.pay_amount AS 'pay_amount', amount_tender,'LoyaltyCard' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature " +
                "AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4'," +
                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber'," +
                "p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p " +
                "WHERE p.paymethod_id = 'LoyaltyCard' " + "AND p.job_id = '" + jobID + "' UNION " + "SELECT " +
                "p.pay_id, p.pay_amount AS 'pay_amount', amount_tender,'Reward' AS  'paymethod_name',p.pay_tip AS 'pay_tip'," +
                "p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4'," +
                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber'," +
                "p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p " +
                "WHERE p.paymethod_id = 'Reward' " + "AND p.job_id = '" + jobID + "' UNION " + "SELECT " +
                "p.pay_id, p.pay_amount AS 'pay_amount', amount_tender,'GiftCard' AS  'paymethod_name',p.pay_tip AS 'pay_tip'," +
                "p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4'," +
                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber'," +
                "p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p " +
                "WHERE p.paymethod_id = 'GiftCard' " + "AND p.job_id = '" + jobID + "' UNION " + "SELECT " +
                "p.pay_id, p.pay_amount AS 'pay_amount', amount_tender,p.card_type AS  'paymethod_name',p.pay_tip AS 'pay_tip'," +
                "p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4'," +
                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber'," +
                "p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p " +
                "WHERE p.paymethod_id = 'Genius' " + "AND p.job_id = '" + jobID + "' UNION " + "SELECT " +
                "p.pay_id, p.pay_amount AS 'pay_amount', amount_tender,p.card_type AS  'paymethod_name',p.pay_tip AS 'pay_tip'," +
                "p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4'," +
                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber'," +
                "p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p " +
                "WHERE p.paymethod_id = '' " + "AND p.job_id = '" + jobID + "' ORDER BY p.pay_id", null);
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
                details.setAmountTender(cursor.getDouble(cursor.getColumnIndex(amount_tender)));

                list.add(details);
                details = new PaymentDetails();
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public PaymentDetails getPrintingForPaymentDetails(String payID, int type) {

        StringBuilder sb = new StringBuilder();
        switch (type) {
            // May come from History>Payment>Details
            case 0: {
                sb.append(
                        "select * from (SELECT p.pay_id as pay_id, p.inv_id as inv_id,p.job_id as job_id,CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name'," +
                                "p.pay_date as pay_date,p.pay_timecreated as pay_timecreated,IFNULL(c.cust_name,'Unknown') as 'cust_name', o.ord_total as ord_total,p.pay_amount as pay_amount," +
                                "p.pay_dueamount as pay_dueamount, amount_tender,"
                                + "CASE WHEN (m.paymethod_name = 'Cash') THEN (o.ord_total-p.pay_amount)  ELSE p.pay_tip END as 'change' ,p.pay_signature as pay_signature, "
                                + "p.pay_transid as pay_transid,p.ccnum_last4 as ccnum_last4,p.pay_check as pay_check,p.is_refund as is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate'," +
                                "p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR', "
                                + "p.Tax1_amount as Tax1_amount, p.Tax2_amount as Tax2_amount, p.Tax1_name as Tax1_name, p.Tax2_name as Tax2_name, p.EMV_JSON as EMV_JSON "
                                + "FROM Payments p,Orders o LEFT OUTER JOIN Customers c  ON c.cust_id = p.cust_id  "
                                + "LEFT OUTER JOIN PayMethods m ON m.paymethod_id = p.paymethod_id WHERE o.ord_id = p.job_id AND job_id ='" + payID + "' " +
                                " UNION " +
                                "SELECT p.pay_id as pay_id, p.inv_id as inv_id,p.job_id as job_id,CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name'," +
                                "p.pay_date as pay_date,p.pay_timecreated as pay_timecreated,IFNULL(c.cust_name,'Unknown') as 'cust_name', o.ord_total as ord_total,p.pay_amount as pay_amount," +
                                "p.pay_dueamount as pay_dueamount, amount_tender,"
                                + "CASE WHEN (m.paymethod_name = 'Cash') THEN (o.ord_total-p.pay_amount)  ELSE p.pay_tip END as 'change' ,p.pay_signature as pay_signature, "
                                + "p.pay_transid as pay_transid,p.ccnum_last4 as ccnum_last4,p.pay_check as pay_check,p.is_refund as is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate'," +
                                "p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR', "
                                + "p.Tax1_amount as Tax1_amount, p.Tax2_amount as Tax2_amount, p.Tax1_name as Tax1_name, p.Tax2_name as Tax2_name, p.EMV_JSON as EMV_JSON "
                                + "FROM PaymentsDeclined p,Orders o LEFT OUTER JOIN Customers c  ON c.cust_id = p.cust_id  "
                                + "LEFT OUTER JOIN PayMethods m ON m.paymethod_id = p.paymethod_id WHERE o.ord_id = p.job_id  AND job_id ='" + payID + "')" +
                                " WHERE job_id ='" + payID + "' "
                );
                break;
            }
            // Straight from main menu 'Payment'
            case 1: {

                sb.append(
                        "select * from (SELECT p.pay_id as pay_id, p.inv_id as inv_id,p.job_id as job_id,CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name'," +
                                "p.pay_date as pay_date,p.pay_timecreated as pay_timecreated, IFNULL(c.cust_name,'Unknown') as 'cust_name',p.pay_amount AS 'ord_total',p.pay_amount as pay_amount," +
                                "p.pay_dueamount as pay_dueamount,amount_tender,"
                                + "CASE WHEN (m.paymethod_name = 'Cash') THEN SUM(p.pay_amount-p.pay_amount) ELSE p.pay_tip END AS 'change', p.pay_signature as pay_signature,  "
                                + "p.pay_transid as pay_transid,p.ccnum_last4 as ccnum_last4,p.pay_check as pay_check,p.is_refund as is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate'," +
                                "p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR', "
                                + "p.Tax1_amount as Tax1_amount, p.Tax2_amount as Tax2_amount, p.Tax1_name as Tax1_name, p.Tax2_name as Tax2_name, p.EMV_JSON as EMV_JSON  "
                                + "FROM Payments p LEFT OUTER JOIN Customers c ON c.cust_id =p.cust_id LEFT OUTER JOIN "
                                + "PayMethods m ON p.paymethod_id = m.paymethod_id  " +
                                "  WHERE pay_id = '" + payID + "'" +
                                " UNION " +
                                "SELECT p.pay_id as pay_id, p.inv_id as inv_id,p.job_id as job_id,CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name'," +
                                "p.pay_date as pay_date,p.pay_timecreated as pay_timecreated, IFNULL(c.cust_name,'Unknown') as 'cust_name',p.pay_amount AS 'ord_total',p.pay_amount as pay_amount," +
                                "p.pay_dueamount as pay_dueamount, amount_tender,"
                                + "CASE WHEN (m.paymethod_name = 'Cash') THEN SUM(p.pay_amount-p.pay_amount) ELSE p.pay_tip END AS 'change', p.pay_signature as pay_signature,  "
                                + "p.pay_transid as pay_transid,p.ccnum_last4 as ccnum_last4,p.pay_check as pay_check,p.is_refund as is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate'," +
                                "p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR', "
                                + "p.Tax1_amount as Tax1_amount, p.Tax2_amount as Tax2_amount, p.Tax1_name as Tax1_name, p.Tax2_name as Tax2_name, p.EMV_JSON as EMV_JSON  "
                                + "FROM PaymentsDeclined p LEFT OUTER JOIN Customers c ON c.cust_id =p.cust_id LEFT OUTER JOIN "
                                + "PayMethods m ON p.paymethod_id = m.paymethod_id" +
                                "  WHERE pay_id = '" + payID + "'" +
                                ")" +
                                "  WHERE pay_id = '" + payID + "' ");
                break;
            }
            // Straight from main menu 'Payment & Declined'
            case 2: {
                sb.append(
                        "select * from (SELECT p.pay_id as pay_id, p.inv_id as inv_id,p.job_id as job_id,CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name'," +
                                "p.pay_date as pay_date,p.pay_timecreated as pay_timecreated, IFNULL(c.cust_name,'Unknown') as 'cust_name',p.pay_amount AS 'ord_total'," +
                                "p.pay_amount as pay_amount,p.pay_dueamount as pay_dueamount,amount_tender,"
                                + "CASE WHEN (m.paymethod_name = 'Cash') THEN SUM(p.pay_amount-p.pay_amount) ELSE p.pay_tip END AS 'change', p.pay_signature as pay_signature,  "
                                + "p.pay_transid as pay_transid,p.ccnum_last4 as ccnum_last4,p.pay_check as pay_check,p.is_refund as is_refund," +
                                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR', "
                                + "p.Tax1_amount as Tax1_amount, p.Tax2_amount as Tax2_amount, p.Tax1_name as Tax1_name, p.Tax2_name as Tax2_name, p.EMV_JSON as EMV_JSON  "
                                + "FROM PaymentsDeclined p LEFT OUTER JOIN Customers c ON c.cust_id =p.cust_id LEFT OUTER JOIN "
                                + "PayMethods m ON p.paymethod_id = m.paymethod_id " +
                                "  WHERE pay_id = '" + payID + "'" +
                                " UNION " +
                                "SELECT p.pay_id as pay_id, p.inv_id as inv_id,p.job_id as job_id,CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name'," +
                                "p.pay_date as pay_date,p.pay_timecreated as pay_timecreated, IFNULL(c.cust_name,'Unknown') as 'cust_name',p.pay_amount AS 'ord_total'," +
                                "p.pay_amount as pay_amount,p.pay_dueamount as pay_dueamount, amount_tender,"
                                + "CASE WHEN (m.paymethod_name = 'Cash') THEN SUM(p.pay_amount-p.pay_amount) ELSE p.pay_tip END AS 'change', p.pay_signature as pay_signature,  "
                                + "p.pay_transid as pay_transid,p.ccnum_last4 as ccnum_last4,p.pay_check as pay_check,p.is_refund as is_refund," +
                                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR', "
                                + "p.Tax1_amount as Tax1_amount, p.Tax2_amount as Tax2_amount, p.Tax1_name as Tax1_name, p.Tax2_name as Tax2_name, p.EMV_JSON as EMV_JSON  "
                                + "FROM PaymentsDeclined p LEFT OUTER JOIN Customers c ON c.cust_id =p.cust_id LEFT OUTER JOIN "
                                + "PayMethods m ON p.paymethod_id = m.paymethod_id " +
                                "  WHERE pay_id = '" + payID + "'" +
                                ")" +
                                "  WHERE pay_id = '" + payID + "'"
                );
                break;
            }

        }
//        sb.append(payID).append("'");

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
        PaymentDetails payDetail = new PaymentDetails();

        if (cursor.moveToFirst()) {

            do {
                payDetail.setPaymethod_name(cursor.getString(cursor.getColumnIndex("paymethod_name")));
                payDetail.setPay_date(Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(pay_date)), activity,
                        0));
                payDetail.setPay_timecreated(Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(pay_timecreated)),
                        activity, 2));
                payDetail.setCust_name(cursor.getString(cursor.getColumnIndex("cust_name")));
                payDetail.setOrd_total(cursor.getString(cursor.getColumnIndex("ord_total")));
                payDetail.setPay_amount(cursor.getString(cursor.getColumnIndex(pay_amount)));
                payDetail.setChange(cursor.getString(cursor.getColumnIndex("change")));
                payDetail.setPay_signature(cursor.getString(cursor.getColumnIndex(pay_signature)));
                payDetail.setPay_transid(cursor.getString(cursor.getColumnIndex(pay_transid)));
                payDetail.setCcnum_last4(cursor.getString(cursor.getColumnIndex(ccnum_last4)));
                payDetail.setPay_check(cursor.getString(cursor.getColumnIndex(pay_check)));
                payDetail.setIs_refund(cursor.getString(cursor.getColumnIndex(is_refund)));
                payDetail.setIvuLottoDrawDate(cursor.getString(cursor.getColumnIndex(IvuLottoDrawDate)));
                payDetail.setIvuLottoNumber(cursor.getString(cursor.getColumnIndex(IvuLottoNumber)));
                payDetail.setIvuLottoQR(cursor.getString(cursor.getColumnIndex(IvuLottoQR)));
                payDetail.setPay_dueamount(cursor.getString(cursor.getColumnIndex(pay_dueamount)));
                payDetail.setInv_id(cursor.getString(cursor.getColumnIndex(inv_id)));
                payDetail.setJob_id(cursor.getString(cursor.getColumnIndex(job_id)));
                payDetail.setAmountTender(cursor.getDouble(cursor.getColumnIndex(amount_tender)));

                payDetail.setTax1_name(cursor.getString(cursor.getColumnIndex(Tax1_name)));
                payDetail.setTax2_name(cursor.getString(cursor.getColumnIndex(Tax2_name)));
                payDetail.setTax1_amount(cursor.getString(cursor.getColumnIndex(Tax1_amount)));
                payDetail.setTax2_amount(cursor.getString(cursor.getColumnIndex(Tax2_amount)));
                payDetail.setEmvContainer(new Gson().fromJson(cursor.getString(cursor.getColumnIndex(EMVJson)), EMVContainer.class));

            } while (cursor.moveToNext());
        }

        cursor.close();
        // db.close();
        return payDetail;
    }

    public HashMap<String, String> getPaymentsRefundsForReportPrinting(String date, int type) {

        StringBuilder sb = new StringBuilder();

        switch (type) {
            case 0:// search for Payments Summary
                sb.append(
                        "SELECT p.paymethod_id,p.pay_amount as 'total',date(pay_timecreated,'localtime') as 'date' FROM Payments p "
                                + " WHERE is_refund = '0' AND date = ? AND isVoid != '1'");
                break;
            case 1:// search for Refunds Summary
                sb.append(
                        "SELECT p.paymethod_id,p.pay_amount as 'total',date(pay_timecreated,'localtime') as 'date' FROM Payments p  "
                                + " WHERE is_refund = '1' AND date = ? AND isVoid != '1'");
                break;
        }

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), new String[]{date});
        HashMap<String, String> map = new HashMap<String, String>();
        BigDecimal bg;
        if (cursor.moveToFirst()) {
            do {
                if (map.containsKey(cursor.getString(cursor.getColumnIndex("paymethod_id")))) {
                    bg = new BigDecimal(map.get(cursor.getString(cursor.getColumnIndex("paymethod_id"))));
                    bg = bg.add(new BigDecimal(NumberUtils.cleanCurrencyFormatedNumber(cursor.getString(cursor.getColumnIndex("total"))))).setScale(2,
                            RoundingMode.HALF_UP);
                    map.put(cursor.getString(cursor.getColumnIndex("paymethod_id")), bg.toString());
                } else
                    map.put(cursor.getString(cursor.getColumnIndex("paymethod_id")),
                            cursor.getString(cursor.getColumnIndex("total")));

            } while (cursor.moveToNext());
        }

        cursor.close();
        return map;
    }

    public List<Payment> getPaymentsDayReport(int type, String clerk_id, String date) {
        StringBuilder query = new StringBuilder();
        List<Payment> listPayment = new ArrayList<Payment>();

        query.append(
                "SELECT card_type, pay_amount, pay_tip, pay_id, CASE WHEN inv_id ='' THEN job_ID ELSE inv_id END AS 'job_id',date(pay_timecreated,'localtime') as 'date' FROM Payments WHERE ");

        switch (type) {
            case 0:// Payments
                query.append("isVoid = '0' AND is_refund = '0' ");
                break;
            case 1:// Voids
                query.append("isVoid = '1' AND pay_type = '1' ");
                break;
            case 2:// Refunds
                query.append("isVoid = '0' AND is_refund = '1' ");
                break;
        }

        String[] where_values = null;
        if (clerk_id != null && !clerk_id.isEmpty()) {
            query.append("AND clerk_id = ? ");
            where_values = new String[]{clerk_id};

            if (date != null && !date.isEmpty()) {
                query.append(" AND date = ? ");
                where_values = new String[]{clerk_id, date};
            }
        } else if (date != null && !date.isEmpty()) {
            query.append(" AND date = ? ");
            where_values = new String[]{date};
        }

        Cursor c = DBManager._db.rawQuery(query.toString(), where_values);

        if (c.moveToFirst()) {
            int i_card_type = c.getColumnIndex(card_type);
            int i_pay_amount = c.getColumnIndex(pay_amount);
            int i_pay_tip = c.getColumnIndex(pay_tip);
            int i_pay_id = c.getColumnIndex(pay_id);
            int i_job_id = c.getColumnIndex(job_id);

            do {
                Payment payment = new Payment(activity);

                payment.card_type = c.getString(i_card_type);
                payment.pay_amount = c.getString(i_pay_amount);
                payment.pay_tip = c.getString(i_pay_tip);
                payment.pay_id = c.getString(i_pay_id);
                payment.job_id = c.getString(i_job_id);

                listPayment.add(payment);

            } while (c.moveToNext());
        }

        c.close();
        return listPayment;

    }

    public List<Payment> getPaymentsGroupDayReport(int type, String clerk_id, String date) {
        StringBuilder query = new StringBuilder();
        List<Payment> listPayment = new ArrayList<Payment>();

        query.append(
                "SELECT card_type, SUM(pay_amount) AS 'pay_amount', SUM(pay_tip) AS 'pay_tip', pay_id, paymethod_id,");
        query.append(
                "CASE WHEN inv_id ='' THEN job_ID ELSE inv_id END AS 'job_id',date(pay_timecreated,'localtime') as 'date' FROM Payments WHERE ");

        switch (type) {
            case 0:// Payments
                query.append("isVoid = '0' AND is_refund = '0' ");
                break;
            case 1:// Voids
                query.append("isVoid = '1' AND pay_type = '1' ");
                break;
            case 2:// Refunds
                query.append("isVoid = '0' AND is_refund = '1' ");
                break;
        }

        String[] where_values = null;
        if (clerk_id != null && !clerk_id.isEmpty()) {
            query.append("AND clerk_id = ? ");
            where_values = new String[]{clerk_id};

            if (date != null && !date.isEmpty()) {
                query.append(" AND date = ? ");
                where_values = new String[]{clerk_id, date};
            }
        } else if (date != null && !date.isEmpty()) {
            query.append(" AND date = ? ");
            where_values = new String[]{date};
        }

        query.append(" GROUP BY paymethod_id");

        Cursor c = DBManager._db.rawQuery(query.toString(), where_values);

        if (c.moveToFirst()) {
            int i_card_type = c.getColumnIndex(card_type);
            int i_pay_amount = c.getColumnIndex(pay_amount);
            int i_pay_tip = c.getColumnIndex(pay_tip);
            int i_pay_id = c.getColumnIndex(pay_id);
            int i_job_id = c.getColumnIndex(job_id);
            int i_paymethod_id = c.getColumnIndex(paymethod_id);
            do {
                Payment payment = new Payment(activity);

                payment.card_type = c.getString(i_card_type);
                payment.pay_amount = c.getString(i_pay_amount);
                payment.pay_tip = c.getString(i_pay_tip);
                payment.pay_id = c.getString(i_pay_id);
                payment.job_id = c.getString(i_job_id);
                payment.paymethod_id = c.getString(i_paymethod_id);

                listPayment.add(payment);

            } while (c.moveToNext());
        }

        c.close();
        return listPayment;

    }

    public String getLastPaymentId(int deviceId, int year) {
        String lastPayID = myPref.getLastPayID();
        boolean getIdFromDB = false;
        StringBuilder sb = new StringBuilder();
        if (TextUtils.isEmpty(lastPayID) || lastPayID.length() <= 4) {
            getIdFromDB = true;
        } else {
            String[] tokens = myPref.getLastPayID().split("-");
            if (!tokens[2].equalsIgnoreCase(String.valueOf(year))) {
                getIdFromDB = true;
            }
        }

        if (getIdFromDB) {
            sb.append("select max(pay_id) from ").append(table_name).append(" WHERE pay_id like '").append(deviceId)
                    .append("-%-").append(year).append("'");

            SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
            Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
            cursor.moveToFirst();
            lastPayID = cursor.getString(0);
            cursor.close();
            stmt.close();
            if (TextUtils.isEmpty(lastPayID)) {
                lastPayID = myPref.getEmpID() + "-" + "00001" + "-" + year;
            }
            myPref.setLastPayID(lastPayID);
        }
        return lastPayID;
    }

    public static PaymentsHandler getInstance(Activity activity) {
        return new PaymentsHandler(activity);
    }

    public void insertDeclined(Payment payment) {
        DBManager._db.beginTransaction();
        try {
            SQLiteStatement insert;
            insert = DBManager._db.compileStatement("INSERT INTO " + table_name_declined + " (" + sb1.toString() + ")" + "VALUES (" + sb2.toString() + ")");
            insert.bindString(index(pay_id), payment.pay_id == null ? "" : payment.pay_id); // pay_id
            insert.bindString(index(group_pay_id), payment.group_pay_id == null ? "" : payment.group_pay_id); // group_pay_id
            insert.bindString(index(original_pay_id), payment.original_pay_id == null ? "" : payment.original_pay_id); // group_pay_id
            insert.bindString(index(cust_id), payment.cust_id == null ? "" : payment.cust_id); // cust_id
            insert.bindString(index(tupyx_user_id), payment.tupyx_user_id == null ? "" : payment.tupyx_user_id);
            insert.bindString(index(custidkey), payment.custidkey == null ? "" : payment.custidkey); // custidkey
            insert.bindString(index(emp_id), payment.emp_id == null ? "" : payment.emp_id); // emp_id
            insert.bindString(index(inv_id), payment.inv_id == null ? "" : payment.inv_id); // inv_id
            insert.bindString(index(paymethod_id), payment.paymethod_id == null ? "" : payment.paymethod_id); // paymethod_id
            insert.bindString(index(pay_check), payment.pay_check == null ? "" : payment.pay_check); // pay_check
            insert.bindString(index(pay_receipt), payment.pay_receipt == null ? "" : payment.pay_receipt); // pay_receipt
            insert.bindString(index(pay_amount), TextUtils.isEmpty(payment.pay_amount) ? "0" : payment.pay_amount); // pay_amount
            insert.bindString(index(pay_dueamount),
                    TextUtils.isEmpty(payment.pay_dueamount) ? "0" : payment.pay_dueamount); // pay_dueamount;
            insert.bindString(index(pay_comment), payment.pay_comment == null ? "" : payment.pay_comment); // pay_comment
            insert.bindString(index(pay_timecreated), payment.pay_timecreated == null ? "" : payment.pay_timecreated); // pay_timecreated
            insert.bindString(index(pay_timesync), payment.pay_timesync == null ? "" : payment.pay_timesync); // pay_timesync
            insert.bindString(index(account_id), payment.account_id == null ? "" : payment.account_id); // account_id
            insert.bindString(index(processed), TextUtils.isEmpty(payment.processed) ? "0" : payment.processed); // processed
            insert.bindString(index(pay_issync), TextUtils.isEmpty(payment.pay_issync) ? "0" : payment.pay_issync); // pay_issync
            insert.bindString(index(pay_transid), payment.pay_transid == null ? "" : payment.pay_transid); // pay_transid
            insert.bindString(index(pay_refnum), payment.pay_refnum == null ? "" : payment.pay_refnum); // pay_refnum
            insert.bindString(index(pay_name), payment.pay_name == null ? "" : payment.pay_name); // pay_name
            insert.bindString(index(pay_addr), payment.pay_addr == null ? "" : payment.pay_addr); // pay_addr
            insert.bindString(index(pay_poscode), payment.pay_poscode == null ? "" : payment.pay_poscode); // pay_poscode
            insert.bindString(index(pay_seccode), payment.pay_seccode == null ? "" : payment.pay_seccode); // pay_seccode
            insert.bindString(index(pay_maccount), payment.pay_maccount == null ? "" : payment.pay_maccount); // pay_maccount
            insert.bindString(index(pay_groupcode), payment.pay_groupcode == null ? "" : payment.pay_groupcode); // pay_groupcode
            insert.bindString(index(pay_stamp), payment.pay_stamp == null ? "" : payment.pay_stamp); // pay_stamp
            insert.bindString(index(pay_resultcode), payment.pay_resultcode == null ? "" : payment.pay_resultcode); // pay_resultcode
            insert.bindString(index(pay_resultmessage),
                    payment.pay_resultmessage == null ? "" : payment.pay_resultmessage); // pay_resultmessage
            insert.bindString(index(pay_ccnum), payment.pay_ccnum == null ? "" : payment.pay_ccnum); // pay_ccnum
            insert.bindString(index(pay_expmonth), payment.pay_expmonth == null ? "" : payment.pay_expmonth); // pay_expMonth
            insert.bindString(index(pay_expyear), payment.pay_expyear == null ? "" : payment.pay_expyear); // pay_expyear
            insert.bindString(index(pay_expdate), payment.pay_expdate == null ? "" : payment.pay_expdate); // pay_expdate
            insert.bindString(index(pay_result), payment.pay_result == null ? "" : payment.pay_result); // pay_result
            insert.bindString(index(pay_date), payment.pay_date == null ? "" : payment.pay_date); // pay_date
            insert.bindString(index(recordnumber), payment.recordnumber == null ? "" : payment.recordnumber); // recordnumber
            insert.bindString(index(pay_signature), payment.pay_signature == null ? "" : payment.pay_signature); // pay_signaute
            insert.bindString(index(authcode), payment.authcode == null ? "" : payment.authcode); // authcode
            insert.bindString(index(status), payment.status == null ? "" : payment.status); // status
            insert.bindString(index(job_id), payment.job_id == null ? "" : payment.job_id); // job_id

            insert.bindString(index(user_ID), payment.user_ID == null ? "" : payment.user_ID); // user_ID
            insert.bindString(index(pay_type), payment.pay_type == null ? "" : payment.pay_type); // pay_type
            insert.bindString(index(pay_tip), TextUtils.isEmpty(payment.pay_tip) ? "0" : payment.pay_tip); // pay_tip
            insert.bindString(index(ccnum_last4), payment.ccnum_last4 == null ? "" : payment.ccnum_last4); // ccnum_last4
            insert.bindString(index(pay_phone), payment.pay_phone == null ? "" : payment.pay_phone); // pay_phone
            insert.bindString(index(pay_email), payment.pay_email == null ? "" : payment.pay_email); // pay_email
            insert.bindString(index(isVoid), TextUtils.isEmpty(payment.isVoid) ? "0" : payment.isVoid); // isVoid
            insert.bindString(index(pay_latitude), payment.pay_latitude == null ? "" : payment.pay_latitude); // pay_latitude
            insert.bindString(index(pay_longitude), payment.pay_longitude == null ? "" : payment.pay_longitude); // pay_longitude
            insert.bindString(index(tipAmount), TextUtils.isEmpty(payment.tipAmount) ? "0" : payment.tipAmount); // tipAmount
            insert.bindString(index(clerk_id), payment.clerk_id == null ? "" : payment.clerk_id); // clerk_id

            insert.bindString(index(is_refund), TextUtils.isEmpty(payment.is_refund) ? "0" : payment.is_refund); // is_refund
            insert.bindString(index(ref_num), payment.ref_num == null ? "" : payment.ref_num); // ref_num
            insert.bindString(index(card_type), payment.card_type == null ? "" : payment.card_type); // card_type

            insert.bindString(index(IvuLottoDrawDate),
                    payment.IvuLottoDrawDate == null ? "" : payment.IvuLottoDrawDate); // IvuLottoDrawData
            insert.bindString(index(IvuLottoNumber), payment.IvuLottoNumber == null ? "" : payment.IvuLottoNumber); // IvuLottoNumber
            insert.bindString(index(IvuLottoQR), payment.IvuLottoQR == null ? "" : payment.IvuLottoQR); // IvuLottoQR

            insert.bindString(index(Tax1_amount), TextUtils.isEmpty(payment.Tax1_amount) ? "0" : payment.Tax1_amount);
            insert.bindString(index(Tax1_name), payment.Tax1_name == null ? "" : payment.Tax1_name);
            insert.bindString(index(Tax2_amount), TextUtils.isEmpty(payment.Tax2_amount) ? "0" : payment.Tax2_amount);
            insert.bindString(index(Tax2_name), payment.Tax2_name == null ? "" : payment.Tax2_name);
            insert.bindString(index(EMVJson), payment.emvContainer == null ? "" : new Gson().toJson(payment.emvContainer, EMVContainer.class));

            insert.execute();
            insert.clearBindings();
            insert.close();
            DBManager._db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.d("Exception", e.getMessage() + " [com.android.emobilepos.PaymentsHandler (at Class.insertDeclined)]");
        } finally {
            myPref.setLastPayID(payment.pay_id);
            lastPaymentInserted = payment;
            DBManager._db.endTransaction();
        }
    }
}
