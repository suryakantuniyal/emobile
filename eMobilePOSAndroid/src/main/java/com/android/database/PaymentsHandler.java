package com.android.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.PaymentDetails;
import com.android.support.DBManager;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;
import com.android.support.MyPreferences;

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

    public final List<String> attr = Arrays.asList(new String[]{pay_id, group_pay_id, cust_id, tupyx_user_id, emp_id,
            inv_id, paymethod_id, pay_check, pay_receipt, pay_amount, pay_dueamount, pay_comment, pay_timecreated,
            pay_timesync, account_id, processed, pay_issync, pay_transid, pay_refnum, pay_name, pay_addr, pay_poscode,
            pay_seccode, pay_maccount, pay_groupcode, pay_stamp, pay_resultcode, pay_resultmessage, pay_ccnum,
            pay_expmonth, pay_expyear, pay_expdate, pay_result, pay_date, recordnumber, pay_signature, authcode, status,
            job_id, user_ID, pay_type, pay_tip, ccnum_last4, pay_phone, pay_email, isVoid, pay_latitude, pay_longitude,
            tipAmount, clerk_id, is_refund, ref_num, IvuLottoDrawDate, IvuLottoNumber, IvuLottoQR, card_type,
            Tax1_amount, Tax1_name, Tax2_amount, Tax2_name, custidkey, original_pay_id});

    private StringBuilder sb1, sb2;
    private final String empStr = "";
    private HashMap<String, Integer> attrHash;
    private Global global;
    private MyPreferences myPref;
    private static final String table_name = "Payments";
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
        // SQLiteDatabase db =
        // SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
        // SQLiteDatabase.NO_LOCALIZED_COLLATORS|
        // SQLiteDatabase.OPEN_READWRITE);
        // SQLiteDatabase db = dbManager.openWritableDB();
        DBManager._db.beginTransaction();
        try {
            SQLiteStatement insert = null;
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(")")
                    .append("VALUES (").append(sb2.toString()).append(")");
            insert = DBManager._db.compileStatement(sb.toString());
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
        }
        // db.close();
    }

    public void emptyTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(table_name);
        DBManager._db.execSQL(sb.toString());
    }

    // public void emptyTable() {
    // StringBuilder sb = new StringBuilder();
    // SQLiteDatabase db = dbManager.openWritableDB();
    // sb.append("DELETE FROM ").append(table_name);
    // db.execSQL(sb.toString());
    // db.close();
    // }

    public long getDBSize() {
        // SQLiteDatabase db =
        // SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
        // SQLiteDatabase.NO_LOCALIZED_COLLATORS|
        // SQLiteDatabase.OPEN_READWRITE);
        // SQLiteDatabase db = dbManager.openReadableDB();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT Count(*) FROM ").append(table_name);

        SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
        long count = stmt.simpleQueryForLong();
        stmt.close();
        // db.close();
        return count;
    }

    public Cursor getUnsyncPayments() // Will return Cursor to all
    // unsynchronized payments (used in
    // generation of XML for post)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(sb1.toString()).append(" FROM ").append(table_name)
                .append(" WHERE pay_issync = '0'");
        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

        return cursor;
    }

    public String getTotalPayAmount(String paymethod_id, String pay_date) {
        // SQLiteDatabase db =
        // SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
        // SQLiteDatabase.NO_LOCALIZED_COLLATORS|
        // SQLiteDatabase.OPEN_READWRITE);
        // SQLiteDatabase db = dbManager.openReadableDB();
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
        // db.close();
        return total;
    }

    public String getTotalRefundAmount(String paymethod_id, String pay_date) {

        // SQLiteDatabase db =
        // SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
        // SQLiteDatabase.NO_LOCALIZED_COLLATORS|
        // SQLiteDatabase.OPEN_READWRITE);
        // SQLiteDatabase db = dbManager.openReadableDB();
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT ROUND(SUM(pay_amount),2) AS 'total',date(pay_timecreated,'localtime') as 'date' FROM Payments WHERE  paymethod_id = '")
                .append(paymethod_id);
        sb.append("' AND date = '").append(pay_date).append("' AND is_refund = '1' AND isVoid != '1'");

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
        String total = "0.00";
        if (cursor.moveToFirst()) {
            total = cursor.getString(cursor.getColumnIndex("total"));
            if (total == null)
                total = "0.00";
        }
        cursor.close();
        // db.close();
        return total;
    }

    public long getNumUnsyncPayments() {
        // SQLiteDatabase db =
        // SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
        // SQLiteDatabase.NO_LOCALIZED_COLLATORS|
        // SQLiteDatabase.OPEN_READWRITE);
        // SQLiteDatabase db = dbManager.openReadableDB();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE pay_issync = '0'");

        SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
        long count = stmt.simpleQueryForLong();
        stmt.close();
        // db.close();
        return count;
    }

    public boolean unsyncPaymentsLeft() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE pay_issync = '0'");

        SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
        long count = stmt.simpleQueryForLong();
        stmt.close();
        if (count == 0)
            return false;
        return true;
    }

    public long paymentExist(String _pay_id) {
        // SQLiteDatabase db = dbManager.openReadableDB();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE pay_id = '").append(_pay_id).append("'");

        SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
        long count = stmt.simpleQueryForLong();
        stmt.close();
        // db.close();
        return count;
    }

    public void updateIsSync(List<String[]> list) {
        // SQLiteDatabase db = dbManager.openWritableDB();

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
        // db.close();
    }

    public String updateSignaturePayment(String payID) {
        // SQLiteDatabase db = dbManager.openWritableDB();

        StringBuilder sb = new StringBuilder();
        sb.append(pay_id).append(" = ?");

        ContentValues args = new ContentValues();

        args.put(pay_signature, global.encodedImage);

        DBManager._db.update(table_name, args, sb.toString(), new String[]{payID});
        sb.setLength(0);
        sb.append("SELECT pay_amount FROM Payments WHERE pay_id = '").append(payID).append("'");
        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
        String returningVal = "";
        int count = cursor.getCount();
        if (cursor.moveToFirst()) {
            returningVal = cursor.getString(cursor.getColumnIndex("pay_amount"));
        }

        cursor.close();
        // db.close();
        return returningVal;
    }

    public void updateIsVoid(String param) {
        // SQLiteDatabase db = dbManager.openWritableDB();

        StringBuilder sb = new StringBuilder();
        sb.append(pay_id).append(" = ?");

        ContentValues args = new ContentValues();

        args.put(isVoid, "1");
        DBManager._db.update(table_name, args, sb.toString(), new String[]{param});

        // db.close();
    }

    public void createVoidPayment(Payment payment, boolean onlineVoid, HashMap<String, String> response) {
        GenerateNewID idGenerator = new GenerateNewID(activity);
        this.updateIsVoid(payment.pay_id);
        String _ord_id = payment.job_id;
        String _orig_pay_id = payment.pay_id;// myPref.getLastPayID();
        // String _orig_pay_id = payment.getSetData("pay_id", true, null);
        // myPref.setLastPayID(idGenerator.getNextID(myPref.getLastPayID()));

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

    public String[] getPaymentDetails(String payID) {
        // SQLiteDatabase db =
        // SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
        // SQLiteDatabase.NO_LOCALIZED_COLLATORS|
        // SQLiteDatabase.OPEN_READWRITE);
        // SQLiteDatabase db = dbManager.openReadableDB();

        StringBuilder sb = new StringBuilder();

        sb.append(
                "SELECT pay_date,pay_comment,job_id, inv_id,group_pay_id,pay_signature,ccnum_last4,pay_latitude,pay_longitude,isVoid,pay_transid,");
        sb.append("authcode,clerk_id FROM Payments WHERE pay_id = ?");

        String[] arrayVal = new String[13];

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), new String[]{payID});
        if (cursor.moveToFirst()) {
            do {
                arrayVal[0] = Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(pay_date)), activity,
                        0);
                arrayVal[1] = cursor.getString(cursor.getColumnIndex(pay_comment));
                arrayVal[2] = cursor.getString(cursor.getColumnIndex(inv_id));// is
                // actually
                // job_id
                // as
                // 'inv_id'
                arrayVal[3] = cursor.getString(cursor.getColumnIndex(group_pay_id));
                arrayVal[4] = cursor.getString(cursor.getColumnIndex(pay_signature));
                arrayVal[5] = cursor.getString(cursor.getColumnIndex(ccnum_last4));
                arrayVal[6] = cursor.getString(cursor.getColumnIndex(pay_latitude));
                arrayVal[7] = cursor.getString(cursor.getColumnIndex(pay_longitude));
                arrayVal[8] = cursor.getString(cursor.getColumnIndex(job_id));
                arrayVal[9] = cursor.getString(cursor.getColumnIndex(isVoid));
                arrayVal[10] = cursor.getString(cursor.getColumnIndex(authcode));
                arrayVal[11] = cursor.getString(cursor.getColumnIndex(pay_transid));
                arrayVal[12] = cursor.getString(cursor.getColumnIndex(clerk_id));
            } while (cursor.moveToNext());
        }
        cursor.close();
        // db.close();
        return arrayVal;
    }

    public Payment getPaymentForVoid(String payID) {
        Payment payment = new Payment(this.activity);
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM Payments WHERE pay_id = '");
        sb.append(payID).append("'");

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

        if (cursor.moveToFirst()) {
            cursorToPayment(cursor, payment);

        }

        cursor.close();
        return payment;
    }

    // private void setPayForVoid(Payment payment,String tag,String val)
    // {
    // payment.getSetData(tag, false, val);
    // }
    //

    public List<Payment> getOrderPayments(String _ordID) {
        Payment payment = new Payment(this.activity);
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM Payments WHERE job_id = '");
        sb.append(_ordID).append("'");

        Cursor c = DBManager._db.rawQuery(sb.toString(), null);

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
    }

    public List<HashMap<String, String>> getPaymentDetailsForTransactions(String jobID) {
        // SQLiteDatabase db = dbManager.openReadableDB();

        List<HashMap<String, String>> mapList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();

        StringBuilder sb = new StringBuilder();

        sb.append(
                "SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip',pm.paymentmethod_type AS 'paymethod_name',p.pay_id AS 'pay_id' FROM Payments p,");
        sb.append("PayMethods pm WHERE p.paymethod_id = pm.paymethod_id AND p.job_id = '").append(jobID)
                .append("' UNION ");
        sb.append(
                "SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip','Wallet' AS  'paymethod_name',p.pay_id AS 'pay_id' FROM Payments p WHERE p.paymethod_id = 'Wallet' ");
        sb.append("AND p.job_id = '").append(jobID).append("' UNION ");
        sb.append(
                "SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip','LoyaltyCard' AS  'paymethod_name',p.pay_id AS 'pay_id' FROM Payments p WHERE p.paymethod_id = 'LoyaltyCard' ");
        sb.append("AND p.job_id = '").append(jobID).append("' UNION ");
        sb.append(
                "SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip','Reward' AS  'paymethod_name',p.pay_id AS 'pay_id' FROM Payments p WHERE p.paymethod_id = 'Reward' ");
        sb.append("AND p.job_id = '").append(jobID).append("' UNION ");
        sb.append(
                "SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip','GiftCard' AS  'paymethod_name',p.pay_id AS 'pay_id' FROM Payments p WHERE p.paymethod_id = 'GiftCard' ");
        sb.append("AND p.job_id = '").append(jobID).append("' UNION ");
        sb.append(
                "SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip','Genius' AS  'paymethod_name',p.pay_id AS 'pay_id' FROM Payments p WHERE p.paymethod_id = 'Genius' ");
        sb.append("AND p.job_id = '").append(jobID).append("' UNION ");
        sb.append(
                "SELECT p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip','Genius' AS  'paymethod_name',p.pay_id AS 'pay_id' FROM Payments p WHERE p.paymethod_id = '' ");
        sb.append("AND p.job_id = '").append(jobID).append("'");

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
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
        // db.close();
        return mapList;
    }

    public Cursor getCashCheckGiftPayment(String type, boolean isRefund) {
        // SQLiteDatabase db = dbManager.openReadableDB();

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

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

        return cursor;
    }

    public Cursor searchCashCheckGift(String type, String search) {
        // SQLiteDatabase db = dbManager.openReadableDB();

        String subquery1 = "SELECT p.pay_id as _id, p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = m.paymethod_id AND m.paymentmethod_type = '";// cust_name
        String subquery2 = "' AND pay_type !='1' AND c.cust_name LIKE ? ORDER BY p.pay_id DESC";

        StringBuilder sb = new StringBuilder();
        sb.append(subquery1).append(type).append(subquery2);// .append(search).append(subquery3);

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), new String[]{"%" + search + "%"});
        cursor.moveToFirst();
        // db.close();

        return cursor;
    }

    public Cursor getCardPayments(boolean isRefund) {
        // SQLiteDatabase db = dbManager.openReadableDB();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM ")
                .append(table_name)
                .append(" p, PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = m.paymethod_id AND (m.paymentmethod_type = ");
        sb.append(
                "'AmericanExpress' OR m.paymentmethod_type = 'Discover' OR m.paymentmethod_type = 'MasterCard' OR m.paymentmethod_type = 'Visa') AND pay_type !='1'  AND is_refund='");

        if (isRefund)
            sb.append("1' ORDER BY p.pay_id DESC");
        else
            sb.append("0' ORDER BY p.pay_id DESC");

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

        return cursor;
    }

    public Cursor searchCards(String search) {
        // SQLiteDatabase db = dbManager.openReadableDB();

        String subquery1 = "SELECT p.pay_id as _id, p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = m.paymethod_id AND (m.paymentmethod_type = ";// cust_name
        String subquery2 = "'AmericanExpress' OR m.paymentmethod_type = 'Discover' OR m.paymentmethod_type = 'MasterCard' OR m.paymentmethod_type = 'Visa') AND c.cust_name LIKE '%";
        String subquery3 = "%' ORDER BY p.pay_id DESC";

        StringBuilder sb = new StringBuilder();
        sb.append(subquery1).append(subquery2).append(search).append(subquery3);

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
        cursor.moveToFirst();
        // db.close();

        return cursor;
    }

    public Cursor getOtherPayments(boolean isRefund) {
        // SQLiteDatabase db = dbManager.openReadableDB();
        StringBuilder sb = new StringBuilder();

        sb.append(
                "SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, ");
        sb.append(
                "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE   p.paymethod_id = 'Wallet' ");
        sb.append("AND pay_type!=1 OR (p.paymethod_id = m.paymethod_id AND pay_type !='1'  AND ");
        sb.append("(m.paymentmethod_type != 'AmericanExpress' AND m.paymentmethod_type != 'Discover' AND ");
        sb.append("m.paymentmethod_type != 'MasterCard' AND m.paymentmethod_type != 'Visa' AND ");
        sb.append(
                "m.paymentmethod_type != 'Cash' AND m.paymentmethod_type != 'GiftCard')) OR p.paymethod_id = 'Genius' OR p.paymethod_id = '' ");

        if (isRefund)
            sb.append(" AND is_refund = '1' GROUP BY p.pay_id ORDER BY p.pay_id DESC ");
        else
            sb.append(" AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ");
        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

        return cursor;
    }

    public Cursor getLoyaltyPayments() {
        // SQLiteDatabase db = dbManager.openReadableDB();
        StringBuilder sb = new StringBuilder();

        sb.append(
                "SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, ");
        sb.append(
                "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'LoyaltyCard' ");
        sb.append("AND pay_type!=1 ");

        sb.append(" AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ");
        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

        return cursor;
    }

    public Cursor getLoyaltyAddBalance() {
        // SQLiteDatabase db = dbManager.openReadableDB();
        StringBuilder sb = new StringBuilder();

        sb.append(
                "SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, ");
        sb.append(
                "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'LoyaltyCardBalance' ");
        sb.append("AND pay_type!=1 ");

        sb.append(" AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ");
        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

        return cursor;
    }

    public Cursor getGiftCardAddBalance() {

        StringBuilder sb = new StringBuilder();
        // sb.append(pay_id).append(" = ?");
        //
        // ContentValues args = new ContentValues();
        //
        // args.put(isVoid, "0");
        // int update = DBManager._db.update(table_name, args, sb.toString(),
        // new String[] { "19-00020-2015" });
        //
        // sb.setLength(0);

        sb.append(
                "SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, ");
        sb.append(
                "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'GiftCardBalance' ");
        sb.append("AND pay_type!=1 ");

        sb.append(" AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ");
        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

        return cursor;
    }

    public Cursor getRewardPayments() {
        // SQLiteDatabase db = dbManager.openReadableDB();
        StringBuilder sb = new StringBuilder();

        sb.append(
                "SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, ");
        sb.append("PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'Reward' ");
        sb.append("AND pay_type!=1 ");

        sb.append(" AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ");
        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

        return cursor;
    }

    public Cursor getRewardAddBalance() {
        // SQLiteDatabase db = dbManager.openReadableDB();
        StringBuilder sb = new StringBuilder();

        sb.append(
                "SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, ");
        sb.append(
                "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'RewardBalance' ");
        sb.append("AND pay_type!=1 ");

        sb.append(" AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ");
        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

        return cursor;
    }

    public Cursor searchOther(String search) {
        // SQLiteDatabase db = dbManager.openReadableDB();

        String subquery1 = "SELECT p.pay_id as _id, p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, PayMethods m LEFT OUTER JOIN "
                + "Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = m.paymethod_id AND pay_type !='1'  AND (m.paymentmethod_type != ";// cust_name
        String subquery2 = "'AmericanExpress' AND m.paymentmethod_type != 'Discover' AND m.paymentmethod_type != 'MasterCard' "
                + "AND m.paymentmethod_type != 'Visa' AND m.paymentmethod_type != 'Cash' AND m.paymentmethod_type != 'GiftCard') AND c.cust_name LIKE ? ORDER BY p.pay_id DESC";

        StringBuilder sb = new StringBuilder();
        sb.append(subquery1).append(subquery2);// .append(search).append(subquery3);

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), new String[]{"%" + search + "%"});
        cursor.moveToFirst();
        // db.close();

        return cursor;
    }

    public List<String[]> getPaymentForPrintingTransactions(String jobID) {
        // SQLiteDatabase db = dbManager.openReadableDB();

        StringBuilder sb = new StringBuilder();
        List<String[]> arrayList = new ArrayList<String[]>();

        // sb.append("SELECT
        // p.pay_amount,m.paymethod_name,p.pay_tip,p.pay_signature,p.pay_transid,p.ccnum_last4,p.IvuLottoDrawDate,p.IvuLottoNumber,p.IvuLottoQR
        // FROM Payments p LEFT OUTER JOIN PayMethods m ON p.paymethod_id =
        // m.paymethod_id " +
        // "WHERE job_id = '");
        //
        // sb.append(jobID).append("'");

        sb.append(
                "SELECT p.pay_amount AS 'pay_amount',pm.paymethod_name AS 'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p,");
        sb.append("PayMethods pm WHERE p.paymethod_id = pm.paymethod_id AND p.job_id = '").append(jobID)
                .append("' UNION ");

        sb.append(
                "SELECT p.pay_amount AS 'pay_amount','Wallet' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p WHERE p.paymethod_id = 'Wallet' ");
        sb.append("AND p.job_id = '").append(jobID).append("' UNION ");

        sb.append(
                "SELECT p.pay_amount AS 'pay_amount','LoyaltyCard' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p WHERE p.paymethod_id = 'LoyaltyCard' ");
        sb.append("AND p.job_id = '").append(jobID).append("' UNION ");

        sb.append(
                "SELECT p.pay_amount AS 'pay_amount','Reward' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p WHERE p.paymethod_id = 'Reward' ");
        sb.append("AND p.job_id = '").append(jobID).append("' UNION ");

        sb.append(
                "SELECT p.pay_amount AS 'pay_amount','GiftCard' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p WHERE p.paymethod_id = 'GiftCard' ");
        sb.append("AND p.job_id = '").append(jobID).append("' UNION ");

        sb.append(
                "SELECT p.pay_amount AS 'pay_amount',p.card_type AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p WHERE p.paymethod_id = 'Genius' ");
        sb.append("AND p.job_id = '").append(jobID).append("' UNION ");

        sb.append(
                "SELECT p.pay_amount AS 'pay_amount',p.card_type AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p WHERE p.paymethod_id = '' ");
        sb.append("AND p.job_id = '").append(jobID).append("'");

        Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
        String[] arrayVal = new String[10];
        if (cursor.moveToFirst()) {

            do {
                arrayVal[0] = cursor.getString(cursor.getColumnIndex(pay_amount));
                arrayVal[1] = cursor.getString(cursor.getColumnIndex("paymethod_name"));
                arrayVal[2] = cursor.getString(cursor.getColumnIndex(pay_tip));
                arrayVal[3] = cursor.getString(cursor.getColumnIndex(pay_signature));
                arrayVal[4] = cursor.getString(cursor.getColumnIndex(pay_transid));
                arrayVal[5] = cursor.getString(cursor.getColumnIndex(ccnum_last4));
                arrayVal[6] = cursor.getString(cursor.getColumnIndex(IvuLottoDrawDate));
                arrayVal[7] = cursor.getString(cursor.getColumnIndex(IvuLottoNumber));
                arrayVal[8] = cursor.getString(cursor.getColumnIndex(IvuLottoQR));
                arrayVal[9] = cursor.getString(cursor.getColumnIndex(pay_dueamount));

                arrayList.add(arrayVal);
                arrayVal = new String[10];
            } while (cursor.moveToNext());
        }
        cursor.close();
        // db.close();
        return arrayList;
    }

    public PaymentDetails getPrintingForPaymentDetails(String payID, int type) {
        // SQLiteDatabase db = dbManager.openReadableDB();

        StringBuilder sb = new StringBuilder();

        if (type == 0) // May come from History>Payment>Details
        {

            sb.append(
                    "SELECT p.inv_id,p.job_id,CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name',p.pay_date,p.pay_timecreated,IFNULL(c.cust_name,'Unknown') as 'cust_name', o.ord_total,p.pay_amount,p.pay_dueamount,"
                            + "CASE WHEN (m.paymethod_name = 'Cash') THEN (o.ord_total-p.pay_amount)  ELSE p.pay_tip END as 'change' ,p.pay_signature, "
                            + "p.pay_transid,p.ccnum_last4,p.pay_check,p.is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR' "
                            + "FROM Payments p,Orders o LEFT OUTER JOIN Customers c  ON c.cust_id = p.cust_id  "
                            + "LEFT OUTER JOIN PayMethods m ON m.paymethod_id = p.paymethod_id WHERE o.ord_id = p.job_id AND p.job_id ='");
        } else if (type == 1) // Straight from main menu 'Payment'
        {
            sb.append(
                    "SELECT p.inv_id,p.job_id,CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name',p.pay_date,p.pay_timecreated, IFNULL(c.cust_name,'Unknown') as 'cust_name',p.pay_amount AS 'ord_total',p.pay_amount,p.pay_dueamount,"
                            + "CASE WHEN (m.paymethod_name = 'Cash') THEN SUM(p.pay_amount-p.pay_amount) ELSE p.pay_tip END AS 'change', p.pay_signature,  "
                            + "p.pay_transid,p.ccnum_last4,p.pay_check,p.is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR' "
                            + "FROM Payments p LEFT OUTER JOIN Customers c ON c.cust_id =p.cust_id LEFT OUTER JOIN "
                            + "PayMethods m ON p.paymethod_id = m.paymethod_id  WHERE p.pay_id = '");
        }

        sb.append(payID).append("'");

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

                payDetail.setTax1_name(cursor.getString(cursor.getColumnIndex(Tax1_name)));
                payDetail.setTax2_name(cursor.getString(cursor.getColumnIndex(Tax2_name)));
                payDetail.setTax1_amount(cursor.getString(cursor.getColumnIndex(Tax1_amount)));
                payDetail.setTax2_amount(cursor.getString(cursor.getColumnIndex(Tax2_amount)));

            } while (cursor.moveToNext());
        }

        cursor.close();
        // db.close();
        return payDetail;
    }

    public HashMap<String, String> getPaymentsRefundsForReportPrinting(String date, int type) {
        // SQLiteDatabase db =
        // SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
        // SQLiteDatabase.NO_LOCALIZED_COLLATORS|
        // SQLiteDatabase.OPEN_READWRITE);
        // SQLiteDatabase db = dbManager.openReadableDB();

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
                    bg = bg.add(new BigDecimal(cursor.getString(cursor.getColumnIndex("total")))).setScale(2,
                            RoundingMode.HALF_UP);
                    map.put(cursor.getString(cursor.getColumnIndex("paymethod_id")), bg.toString());
                } else
                    map.put(cursor.getString(cursor.getColumnIndex("paymethod_id")),
                            cursor.getString(cursor.getColumnIndex("total")));

            } while (cursor.moveToNext());
        }

        cursor.close();
        // db.close();
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
                query.append("isVoid = '1' ");
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
                query.append("isVoid = '1' ");
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
}
