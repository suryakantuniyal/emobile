package com.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.android.dao.AssignEmployeeDAO;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.PaymentDetails;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.DateUtils;
import com.android.support.GenerateNewID;
import com.android.support.GenerateNewID.IdType;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;
import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;

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

    public static void setLastPaymentInserted(Payment payment) {
        if (payment != null && payment.isManaged()) {
            lastPaymentInserted = Realm.getDefaultInstance().copyFromRealm(payment);
        } else {
            lastPaymentInserted = payment;
        }
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
    //    private Global global;
    private MyPreferences myPref;
    private static final String table_name = "Payments";
    private static final String table_name_declined = "PaymentsDeclined";
    private Context activity;

    public PaymentsHandler(Context context) {
//        global = (Global) activity.getApplication();
        myPref = new MyPreferences(context);
        this.activity = context;
        attrHash = new HashMap<>();
        sb1 = new StringBuilder();
        sb2 = new StringBuilder();
        new DBManager(context);
        initDictionary();
    }

    public SQLiteDatabase getDatabase() {
        if (DBManager.getDatabase() == null || !DBManager.getDatabase().isOpen()) {
            new DBManager(activity);
        }
        return DBManager.getDatabase();
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

        getDatabase().beginTransaction();
        try {
            SQLiteStatement insert;
            insert = getDatabase().compileStatement("INSERT INTO " + table_name + " (" + sb1.toString() + ")" +
                    "VALUES (" + sb2.toString() + ")");
            insert.bindString(index(pay_id), payment.getPay_id() == null ? "" : payment.getPay_id()); // pay_id
            insert.bindString(index(group_pay_id), payment.getGroup_pay_id() == null ? "" : payment.getGroup_pay_id()); // group_pay_id
            insert.bindString(index(original_pay_id), payment.getOriginal_pay_id() == null ? "" : payment.getOriginal_pay_id()); // group_pay_id
            insert.bindString(index(cust_id), payment.getCust_id() == null ? "" : payment.getCust_id()); // cust_id
            insert.bindString(index(tupyx_user_id), payment.getTupyx_user_id() == null ? "" : payment.getTupyx_user_id());
            insert.bindString(index(custidkey), payment.getCustidkey() == null ? "" : payment.getCustidkey()); // custidkey
            insert.bindString(index(emp_id), payment.getEmp_id() == null ? "" : payment.getEmp_id()); // emp_id
            insert.bindString(index(inv_id), payment.getInv_id() == null ? "" : payment.getInv_id()); // inv_id
            insert.bindString(index(paymethod_id), payment.getPaymethod_id() == null ? "" : payment.getPaymethod_id()); // paymethod_id
            insert.bindString(index(pay_check), payment.getPay_check() == null ? "" : payment.getPay_check()); // pay_check
            insert.bindString(index(pay_receipt), payment.getPay_receipt() == null ? "" : payment.getPay_receipt()); // pay_receipt
            insert.bindString(index(pay_amount), TextUtils.isEmpty(payment.getPay_amount()) ? "0" : payment.getPay_amount()); // pay_amount
            insert.bindString(index(pay_dueamount),
                    TextUtils.isEmpty(payment.getPay_dueamount()) ? "0" : payment.getPay_dueamount()); // pay_dueamount;
            insert.bindString(index(pay_comment), payment.getPay_comment() == null ? "" : payment.getPay_comment()); // pay_comment
            insert.bindString(index(pay_timecreated), payment.getPay_timecreated() == null ? "" : payment.getPay_timecreated()); // pay_timecreated
            insert.bindString(index(pay_timesync), payment.getPay_timesync() == null ? "" : payment.getPay_timesync()); // pay_timesync
            insert.bindString(index(account_id), payment.getAccount_id() == null ? "" : payment.getAccount_id()); // account_id
            insert.bindString(index(processed), TextUtils.isEmpty(payment.getProcessed()) ? "0" : payment.getProcessed()); // processed
            insert.bindString(index(pay_issync), TextUtils.isEmpty(payment.getPay_issync()) ? "0" : payment.getPay_issync()); // pay_issync
            insert.bindString(index(pay_transid), payment.getPay_transid() == null ? "" : payment.getPay_transid()); // pay_transid
            insert.bindString(index(pay_refnum), payment.getPay_refnum() == null ? "" : payment.getPay_refnum()); // pay_refnum
            insert.bindString(index(pay_name), payment.getPay_name() == null ? "" : payment.getPay_name()); // pay_name
            insert.bindString(index(pay_addr), payment.getPay_addr() == null ? "" : payment.getPay_addr()); // pay_addr
            insert.bindString(index(pay_poscode), payment.getPay_poscode() == null ? "" : payment.getPay_poscode()); // pay_poscode
            insert.bindString(index(pay_seccode), payment.getPay_seccode() == null ? "" : payment.getPay_seccode()); // pay_seccode
            insert.bindString(index(pay_maccount), payment.getPay_maccount() == null ? "" : payment.getPay_maccount()); // pay_maccount
            insert.bindString(index(pay_groupcode), payment.getPay_groupcode() == null ? "" : payment.getPay_groupcode()); // pay_groupcode
            insert.bindString(index(pay_stamp), payment.getPay_stamp() == null ? "" : payment.getPay_stamp()); // pay_stamp
            insert.bindString(index(pay_resultcode), payment.getPay_resultcode() == null ? "" : payment.getPay_resultcode()); // pay_resultcode
            insert.bindString(index(pay_resultmessage),
                    payment.getPay_resultmessage() == null ? "" : payment.getPay_resultmessage()); // pay_resultmessage
            insert.bindString(index(pay_ccnum), payment.getPay_ccnum() == null ? "" : payment.getPay_ccnum()); // pay_ccnum
            insert.bindString(index(pay_expmonth), payment.getPay_expmonth() == null ? "" : payment.getPay_expmonth()); // pay_expMonth
            insert.bindString(index(pay_expyear), payment.getPay_expyear() == null ? "" : payment.getPay_expyear()); // pay_expyear
            insert.bindString(index(pay_expdate), payment.getPay_expdate() == null ? "" : payment.getPay_expdate()); // pay_expdate
            insert.bindString(index(pay_result), payment.getPay_result() == null ? "" : payment.getPay_result()); // pay_result
            insert.bindString(index(pay_date), payment.getPay_date() == null ? "" : payment.getPay_date()); // pay_date
            insert.bindString(index(recordnumber), payment.getRecordnumber() == null ? "" : payment.getRecordnumber()); // recordnumber
            insert.bindString(index(pay_signature), payment.getPay_signature() == null ? "" : payment.getPay_signature()); // pay_signaute
            insert.bindString(index(authcode), payment.getAuthcode() == null ? "" : payment.getAuthcode()); // authcode
            insert.bindString(index(status), payment.getStatus() == null ? "" : payment.getStatus()); // status
            insert.bindString(index(job_id), payment.getJob_id() == null ? "" : payment.getJob_id()); // job_id
            insert.bindDouble(index(amount_tender), payment.getAmountTender() == null ? 0 : payment.getAmountTender()); // user_ID

            insert.bindString(index(user_ID), payment.getUser_ID() == null ? "" : payment.getUser_ID()); // user_ID
            insert.bindString(index(pay_type), payment.getPay_type() == null ? "" : payment.getPay_type()); // pay_type
            insert.bindString(index(pay_tip), TextUtils.isEmpty(payment.getPay_tip()) ? "0" : payment.getPay_tip()); // pay_tip
            insert.bindString(index(ccnum_last4), payment.getCcnum_last4() == null ? "" : payment.getCcnum_last4()); // ccnum_last4
            insert.bindString(index(pay_phone), payment.getPay_phone() == null ? "" : payment.getPay_phone()); // pay_phone
            insert.bindString(index(pay_email), payment.getPay_email() == null ? "" : payment.getPay_email()); // pay_email
            insert.bindString(index(isVoid), TextUtils.isEmpty(payment.getIsVoid()) ? "0" : payment.getIsVoid()); // isVoid
            insert.bindString(index(pay_latitude), payment.getPay_latitude() == null ? "" : payment.getPay_latitude()); // pay_latitude
            insert.bindString(index(pay_longitude), payment.getPay_longitude() == null ? "" : payment.getPay_longitude()); // pay_longitude
            insert.bindString(index(tipAmount), TextUtils.isEmpty(payment.getTipAmount()) ? "0" : payment.getTipAmount()); // tipAmount
            insert.bindString(index(clerk_id), payment.getClerk_id() == null ? "" : payment.getClerk_id()); // clerk_id

            insert.bindString(index(is_refund), TextUtils.isEmpty(payment.getIs_refund()) ? "0" : payment.getIs_refund()); // is_refund
            insert.bindString(index(ref_num), payment.getRef_num() == null ? "" : payment.getRef_num()); // ref_num
            insert.bindString(index(card_type), payment.getCard_type() == null ? "" : payment.getCard_type()); // card_type

            insert.bindString(index(IvuLottoDrawDate),
                    payment.getIvuLottoDrawDate() == null ? "" : payment.getIvuLottoDrawDate()); // IvuLottoDrawData
            insert.bindString(index(IvuLottoNumber), payment.getIvuLottoNumber() == null ? "" : payment.getIvuLottoNumber()); // IvuLottoNumber
            insert.bindString(index(IvuLottoQR), payment.getIvuLottoQR() == null ? "" : payment.getIvuLottoQR()); // IvuLottoQR

            insert.bindString(index(Tax1_amount), TextUtils.isEmpty(payment.getTax1_amount()) ? "0" : payment.getTax1_amount());
            insert.bindString(index(Tax1_name), payment.getTax1_name() == null ? "" : payment.getTax1_name());
            insert.bindString(index(Tax2_amount), TextUtils.isEmpty(payment.getTax2_amount()) ? "0" : payment.getTax2_amount());
            insert.bindString(index(Tax2_name), payment.getTax2_name() == null ? "" : payment.getTax2_name());
            insert.bindString(index(EMVJson), payment.getEmvContainer() == null ? "" : new Gson().toJson(payment.getEmvContainer(), EMVContainer.class));


            insert.execute();
            insert.clearBindings();
            insert.close();
            getDatabase().setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            myPref.setLastPayID(payment.getPay_id());
            getDatabase().endTransaction();
            lastPaymentInserted = payment;
        }
    }

    public void emptyTable() {
        getDatabase().execSQL("DELETE FROM " + table_name);
    }


    public Cursor getUnsyncPayments() // Will return Cursor to all
    // unsynchronized payments (used in
    // generation of XML for post)
    {
        return getDatabase().rawQuery("SELECT " + sb1.toString() + " FROM " + table_name + " WHERE pay_issync = '0'", null);
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

        Cursor cursor = getDatabase().rawQuery(sb.toString(), null);
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
        Cursor cursor = getDatabase().rawQuery("SELECT ROUND(SUM(pay_amount),2) AS 'total',date(pay_timecreated,'localtime') as 'date' FROM Payments WHERE  paymethod_id = '" + paymethod_id + "' AND date = '" + pay_date + "' AND is_refund = '1' AND isVoid != '1'", null);
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
        SQLiteStatement stmt = getDatabase().compileStatement("SELECT Count(*) FROM " + table_name + " WHERE pay_issync = '0'");
        long count = stmt.simpleQueryForLong();
        stmt.close();
        return count;
    }

    public boolean unsyncPaymentsLeft() {
        SQLiteStatement stmt = getDatabase().compileStatement("SELECT Count(*) FROM " + table_name + " WHERE pay_issync = '0'");
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
        SQLiteStatement stmt = getDatabase().compileStatement(sql);
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
            getDatabase().update(table_name, args, sb.toString(), new String[]{list.get(i)[1]});
        }
    }

    public String updateSignaturePayment(String payID, String encodedImage) {

        StringBuilder sb = new StringBuilder();
        sb.append(pay_id).append(" = ?");

        ContentValues args = new ContentValues();

        args.put(pay_signature, encodedImage);

        getDatabase().update(table_name, args, sb.toString(), new String[]{payID});
        sb.setLength(0);
        sb.append("SELECT pay_amount FROM Payments WHERE pay_id = '").append(payID).append("'");
        Cursor cursor = getDatabase().rawQuery(sb.toString(), null);
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
        getDatabase().update(table_name, args, pay_id + " = ?", new String[]{param});
    }

    public void createVoidPayment(Payment payment, boolean onlineVoid, HashMap<String, String> response) {
        GenerateNewID idGenerator = new GenerateNewID(activity);
        this.updateIsVoid(payment.getPay_id());
        String _ord_id = payment.getJob_id();
        String _orig_pay_id = payment.getPay_id();


        payment.setPay_id(idGenerator.getNextID(IdType.PAYMENT_ID));
        payment.setPay_type("1");
        payment.setIsVoid("1");
        payment.setPay_issync("0");

        payment.setOriginal_pay_id(_orig_pay_id);
        payment.setPay_timecreated(DateUtils.getDateAsString(new Date(), DateUtils.DATE_PATTERN));
        payment.setPay_date(DateUtils.getDateAsString(new Date(), DateUtils.DATE_PATTERN));

        if (onlineVoid) {
            payment.setPay_resultcode(response.get(pay_resultcode));
            payment.setPay_resultmessage(response.get(pay_resultmessage));
            payment.setPay_transid(response.get("CreditCardTransID"));
            payment.setAuthcode(response.get("AuthorizationCode"));
        }

        this.insert(payment);

        if (_ord_id != null && !_ord_id.isEmpty()) {
            OrdersHandler tempHandler = new OrdersHandler(activity);
            tempHandler.updateIsVoid(_ord_id);
        }
    }


    public PaymentDetails getPaymentDetails(String payID, boolean isDeclined) {
        String tableName = table_name;
        if (isDeclined)
            tableName = table_name_declined;
        String sql = "SELECT pay_date,pay_comment, amount_tender, job_id, inv_id,group_pay_id,pay_signature,ccnum_last4," +
                " amount_tender, pay_latitude,pay_longitude,isVoid,pay_transid," + "authcode,clerk_id FROM " + tableName +
                " WHERE pay_id = ?";

        PaymentDetails paymentDetails = new PaymentDetails();
        Cursor cursor = getDatabase().rawQuery(sql, new String[]{payID});
        if (cursor.moveToFirst()) {
            do {
                paymentDetails.setPay_date(Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(pay_date)), 0));
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
        Payment payment = null;
        Cursor cursor = getDatabase().rawQuery("SELECT * FROM Payments WHERE pay_id = '" + payID + "'", null);
        if (cursor.moveToFirst()) {
            payment = getPayment(cursor);

        }
        cursor.close();
        return payment;
    }

    public List<Payment> getOrderPayments(String _ordID) {

        Cursor c = getDatabase().rawQuery("SELECT * FROM Payments WHERE job_id = '" + _ordID + "'", null);

        List<Payment> listPayment = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                listPayment.add(getPayment(c));
            } while (c.moveToNext());
        }

        c.close();
        return listPayment;
    }

    private Payment getPayment(Cursor c) {
        Payment payment = new Payment(activity);
        payment.setPay_id(c.getString(c.getColumnIndex(pay_id)));
        payment.setGroup_pay_id(c.getString(c.getColumnIndex(group_pay_id)));
        payment.setCust_id(c.getString(c.getColumnIndex(cust_id)));
        payment.setTupyx_user_id(c.getString(c.getColumnIndex(tupyx_user_id)));
        payment.setEmp_id(c.getString(c.getColumnIndex(emp_id)));
        payment.setInv_id(c.getString(c.getColumnIndex(inv_id)));
        payment.setPaymethod_id(c.getString(c.getColumnIndex(paymethod_id)));
        payment.setPay_check(c.getString(c.getColumnIndex(pay_check)));
        payment.setPay_receipt(c.getString(c.getColumnIndex(pay_receipt)));
        payment.setPay_amount(c.getString(c.getColumnIndex(pay_amount)));
        payment.setPay_dueamount(c.getString(c.getColumnIndex(pay_dueamount)));
        payment.setPay_comment(c.getString(c.getColumnIndex(pay_comment)));
        payment.setPay_timecreated(c.getString(c.getColumnIndex(pay_timecreated)));
        payment.setPay_timesync(c.getString(c.getColumnIndex(pay_timesync)));
        payment.setAccount_id(c.getString(c.getColumnIndex(account_id)));
        payment.setProcessed(c.getString(c.getColumnIndex(processed)));
        payment.setPay_issync(c.getString(c.getColumnIndex(pay_issync)));
        payment.setPay_transid(c.getString(c.getColumnIndex(pay_transid)));
        payment.setPay_refnum(c.getString(c.getColumnIndex(pay_refnum)));
        payment.setPay_name(c.getString(c.getColumnIndex(pay_name)));
        payment.setPay_addr(c.getString(c.getColumnIndex(pay_addr)));
        payment.setPay_poscode(c.getString(c.getColumnIndex(pay_poscode)));
        payment.setPay_seccode(c.getString(c.getColumnIndex(pay_seccode)));
        payment.setPay_maccount(c.getString(c.getColumnIndex(pay_maccount)));
        payment.setPay_groupcode(c.getString(c.getColumnIndex(pay_groupcode)));
        payment.setPay_stamp(c.getString(c.getColumnIndex(pay_stamp)));
        payment.setPay_resultcode(c.getString(c.getColumnIndex(pay_resultcode)));
        payment.setPay_resultmessage(c.getString(c.getColumnIndex(pay_resultmessage)));
        payment.setPay_ccnum(c.getString(c.getColumnIndex(pay_ccnum)));
        payment.setPay_expmonth(c.getString(c.getColumnIndex(pay_expmonth)));
        payment.setPay_expyear(c.getString(c.getColumnIndex(pay_expyear)));
        payment.setPay_expdate(c.getString(c.getColumnIndex(pay_expdate)));
        payment.setRecordnumber(c.getString(c.getColumnIndex(recordnumber)));
        payment.setPay_signature(c.getString(c.getColumnIndex(pay_signature)));
        payment.setAuthcode(c.getString(c.getColumnIndex(authcode)));
        payment.setStatus(c.getString(c.getColumnIndex(status)));
        payment.setJob_id(c.getString(c.getColumnIndex(job_id)));
        payment.setUser_ID(c.getString(c.getColumnIndex(user_ID)));
        payment.setPay_type(c.getString(c.getColumnIndex(pay_type)));
        payment.setPay_tip(c.getString(c.getColumnIndex(pay_tip)));
        payment.setCcnum_last4(c.getString(c.getColumnIndex(ccnum_last4)));
        payment.setPay_phone(c.getString(c.getColumnIndex(pay_phone)));
        payment.setPay_email(c.getString(c.getColumnIndex(pay_email)));
        payment.setIsVoid(c.getString(c.getColumnIndex(isVoid)));
        payment.setPay_latitude(c.getString(c.getColumnIndex(pay_latitude)));
        payment.setPay_longitude(c.getString(c.getColumnIndex(pay_longitude)));
        payment.setTipAmount(c.getString(c.getColumnIndex(tipAmount)));
        payment.setClerk_id(c.getString(c.getColumnIndex(clerk_id)));
        payment.setIs_refund(c.getString(c.getColumnIndex(is_refund)));
        payment.setRef_num(c.getString(c.getColumnIndex(ref_num)));
        payment.setIvuLottoDrawDate(c.getString(c.getColumnIndex(IvuLottoDrawDate)));
        payment.setIvuLottoNumber(c.getString(c.getColumnIndex(IvuLottoNumber)));
        payment.setIvuLottoQR(c.getString(c.getColumnIndex(IvuLottoQR)));
        payment.setCard_type(c.getString(c.getColumnIndex(card_type)));
        payment.setCustidkey(c.getString(c.getColumnIndex(custidkey)));
        payment.setOriginal_pay_id(c.getString(c.getColumnIndex(original_pay_id)));
        payment.setAmountTender(c.getDouble(c.getColumnIndex(amount_tender)));
        return payment;
    }

    public List<Payment> getPaymentDetailsForTransactions(String jobID) {
        List<Payment> payments = new ArrayList<>();
        Cursor cursor = getDatabase().rawQuery("SELECT p.isVoid as 'isVoid', p.paymethod_id as 'paymethod_id', p.pay_transid as 'pay_transid', p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip'," +
                "pm.paymentmethod_type AS 'paymethod_name', amount_tender,p.pay_id AS 'pay_id', p.pay_type as 'pay_type' FROM Payments p," + "PayMethods pm " +
                "WHERE p.paymethod_id = pm.paymethod_id  AND pay_type != '1' AND p.job_id = '" + jobID + "' " +
                "UNION " + "SELECT p.isVoid as 'isVoid',p.paymethod_id as 'paymethod_id',p.pay_transid as 'pay_transid', p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip','Wallet' AS  'paymethod_name', amount_tender," +
                "p.pay_id AS 'pay_id', p.pay_type as 'pay_type' FROM Payments p WHERE p.paymethod_id = 'Wallet' AND pay_type != '1' " +
                "AND p.job_id = '" + jobID + "' UNION " +
                "SELECT p.isVoid as 'isVoid', p.paymethod_id as 'paymethod_id',p.pay_transid as 'pay_transid', p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip','LoyaltyCard' AS  'paymethod_name', amount_tender," +
                "p.pay_id AS 'pay_id', p.pay_type as 'pay_type' FROM Payments p WHERE p.paymethod_id = 'LoyaltyCard' " + "  AND pay_type != '1' AND p.job_id = '" +
                jobID + "' UNION " + "SELECT p.isVoid as 'isVoid',p.paymethod_id as 'paymethod_id',p.pay_transid as 'pay_transid', p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip'," +
                "'Reward' AS  'paymethod_name', amount_tender,p.pay_id AS 'pay_id', p.pay_type as 'pay_type' FROM Payments p WHERE p.paymethod_id = 'Reward' " +
                " AND pay_type != '1' AND p.job_id = '" + jobID + "' UNION " + "SELECT p.isVoid as 'isVoid',p.paymethod_id as 'paymethod_id',p.pay_transid as 'pay_transid', p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip'," +
                "'GiftCard' AS  'paymethod_name', amount_tender,p.pay_id AS 'pay_id', p.pay_type as 'pay_type' FROM Payments p " +
                "WHERE p.paymethod_id = 'GiftCard' " + " AND pay_type != '1' AND p.job_id = '" + jobID + "' UNION " +
                "SELECT p.isVoid as 'isVoid',p.paymethod_id as 'paymethod_id',p.pay_transid as 'pay_transid', p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip','Genius' AS  'paymethod_name', amount_tender," +
                "p.pay_id AS 'pay_id', p.pay_type as 'pay_type' FROM Payments p WHERE p.paymethod_id = 'Genius' " + " AND pay_type != '1' AND p.job_id = '" +
                jobID + "' UNION " + "SELECT p.isVoid as 'isVoid',p.paymethod_id as 'paymethod_id',p.pay_transid as 'pay_transid', p.pay_amount AS 'pay_amount',p.pay_tip AS 'pay_tip'," +
                "'Genius' AS  'paymethod_name', amount_tender, p.pay_id AS 'pay_id', p.pay_type as 'pay_type' FROM Payments p WHERE p.paymethod_id = '' " +
                " AND pay_type != '1' AND p.job_id = '" + jobID + "'", null);
        if (cursor.moveToFirst()) {
            do {
                Payment p = new Payment();
                p.setPay_amount(cursor.getString(cursor.getColumnIndex(pay_amount)));
                p.setPay_tip(cursor.getString(cursor.getColumnIndex(pay_tip)));
                p.setPaymethod_id(cursor.getString(cursor.getColumnIndex(paymethod_id)));
                p.setPay_transid(cursor.getString(cursor.getColumnIndex(pay_transid)));
                p.setPay_id(cursor.getString(cursor.getColumnIndex(pay_id)));
                p.setIsVoid(cursor.getString(cursor.getColumnIndex(isVoid)));
                p.setPay_type(cursor.getString(cursor.getColumnIndex(pay_type)));
                payments.add(p);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return payments;
    }

    public Cursor getCashCheckGiftPayment(String type, boolean isRefund) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM ")
                .append(table_name)
                .append(" p, PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = m.paymethod_id "
                        + "AND m.paymentmethod_type = '")
                .append(type).append("' AND pay_type !='1' AND is_refund ='");
        if (isRefund)
            sb.append("1' ORDER BY substr(p.pay_id,10,4) desc, p.pay_id DESC");
        else
            sb.append("0' ORDER BY substr(p.pay_id,10,4) desc, p.pay_id DESC");
        return getDatabase().rawQuery(sb.toString(), null);
    }

    public Cursor searchCashCheckGift(String type, String search) {
        String subquery1 = "SELECT p.pay_id as _id, p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = m.paymethod_id AND m.paymentmethod_type = '";// cust_name
        String subquery2 = "' AND pay_type !='1' AND c.cust_name LIKE ? ORDER BY p.pay_id DESC";
        Cursor cursor = getDatabase().rawQuery(subquery1 + type + subquery2, new String[]{"%" + search + "%"});
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getCardPayments(boolean isRefund) {
        String is_refund = "0";
        if (isRefund)
            is_refund = "1";
        String sb = ("SELECT 'FALSE' as DECLINED,p.pay_id as _id,p.pay_amount as pay_amount,c.cust_name as cust_name," +
                "p.job_id as job_id,p.isVoid as isVoid,p.pay_issync as pay_issync,p.pay_tip as pay_tip " +
                "FROM " + table_name +
                " p, PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id " +
                "WHERE p.paymethod_id = m.paymethod_id AND " +
                "(m.paymentmethod_type = 'AmericanExpress' OR m.paymentmethod_type = 'Discover' OR " +
                "m.paymentmethod_type = 'MasterCard' OR m.paymentmethod_type = 'Visa') " +
                "AND pay_type !='1'  AND is_refund='" + is_refund + "' " +
                " UNION " +
                "SELECT 'TRUE' as DECLINED,p.pay_id as _id,p.pay_amount as pay_amount,c.cust_name as cust_name," +
                "p.job_id as job_id,p.isVoid as isVoid,p.pay_issync as pay_issync,p.pay_tip as pay_tip " + "FROM " + table_name_declined +
                " p, PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id " +
                "WHERE p.paymethod_id = m.paymethod_id AND " +
                "(m.paymentmethod_type = 'AmericanExpress' OR m.paymentmethod_type = 'Discover' OR " +
                "m.paymentmethod_type = 'MasterCard' OR m.paymentmethod_type = 'Visa') " +
                "AND pay_type !='1'  AND is_refund='" + is_refund + "' ");

        return getDatabase().rawQuery(sb, null);
    }

    public Cursor searchCards(String search) {

        String subquery1 = "SELECT p.pay_id as _id, p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = m.paymethod_id AND (m.paymentmethod_type = ";// cust_name
        String subquery2 = "'AmericanExpress' OR m.paymentmethod_type = 'Discover' OR m.paymentmethod_type = 'MasterCard' OR m.paymentmethod_type = 'Visa') AND c.cust_name LIKE '%";
        String subquery3 = "%' ORDER BY p.pay_id DESC";

        Cursor cursor = getDatabase().rawQuery(subquery1 + subquery2 + search + subquery3, null);
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

        return getDatabase().rawQuery(sb.toString(), null);
    }

    public Cursor getLoyaltyPayments() {

        return getDatabase().rawQuery("SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, " + "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'LoyaltyCard' " + "AND pay_type!=1 " + " AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ", null);
    }

    public Cursor getLoyaltyAddBalance() {

        return getDatabase().rawQuery("SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, " + "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'LoyaltyCardBalance' " + "AND pay_type!=1 " + " AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ", null);
    }

    public Cursor getGiftCardAddBalance() {


        return getDatabase().rawQuery("SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, " + "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'GiftCardBalance' " + "AND pay_type!=1 " + " AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ", null);
    }

    public Cursor getRewardPayments() {

        return getDatabase().rawQuery("SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, " + "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'Reward' " + "AND pay_type!=1 " + " AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ", null);
    }

    public Cursor getRewardAddBalance() {

        return getDatabase().rawQuery("SELECT p.pay_id as _id,p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, " + "PayMethods m LEFT OUTER JOIN Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = 'RewardBalance' " + "AND pay_type!=1 " + " AND is_refund = '0' GROUP BY p.pay_id ORDER BY p.pay_id DESC ", null);
    }

    public Cursor searchOther(String search) {

        String subquery1 = "SELECT p.pay_id as _id, p.pay_amount,c.cust_name,p.job_id,p.isVoid,p.pay_issync,p.pay_tip FROM Payments p, PayMethods m LEFT OUTER JOIN "
                + "Customers c ON p.cust_id = c.cust_id WHERE p.paymethod_id = m.paymethod_id AND pay_type !='1'  AND (m.paymentmethod_type != ";// cust_name
        String subquery2 = "'AmericanExpress' AND m.paymentmethod_type != 'Discover' AND m.paymentmethod_type != 'MasterCard' "
                + "AND m.paymentmethod_type != 'Visa' AND m.paymentmethod_type != 'Cash' AND m.paymentmethod_type != 'GiftCard') AND c.cust_name LIKE ? ORDER BY p.pay_id DESC";

        Cursor cursor = getDatabase().rawQuery(subquery1 + subquery2, new String[]{"%" + search + "%"});
        cursor.moveToFirst();

        return cursor;
    }

    public List<PaymentDetails> getPaymentForPrintingTransactions(String jobID) {

        List<PaymentDetails> list = new ArrayList<>();

        Cursor cursor = getDatabase().rawQuery("SELECT p.pay_type as 'pay_type', p.is_refund as 'is_refund', " +
                "p.isVoid as 'isVoid', p.pay_id, p.pay_amount AS 'pay_amount', amount_tender,pm.paymethod_name AS 'paymethod_name'," +
                "p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid'," +
                "p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate'," +
                "p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' " +
                "FROM Payments p," + "PayMethods pm WHERE p.paymethod_id = pm.paymethod_id " +
                "AND p.job_id = '" + jobID + "' UNION " + "SELECT p.pay_type as 'pay_type', p.is_refund as 'is_refund', " +
                "p.isVoid as 'isVoid', p.pay_id, p.pay_amount AS 'pay_amount', amount_tender, " +
                "'Wallet' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature'," +
                "p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate'," +
                "p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' " +
                "FROM Payments p WHERE p.paymethod_id = 'Wallet' " + "AND p.job_id = '" + jobID + "' UNION " + "SELECT " +
                "p.pay_type as 'pay_type', p.is_refund as 'is_refund', p.isVoid as 'isVoid', p.pay_id,  " +
                "p.pay_amount AS 'pay_amount', amount_tender,'LoyaltyCard' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature " +
                "AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4'," +
                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber'," +
                "p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p " +
                "WHERE p.paymethod_id = 'LoyaltyCard' " + "AND p.job_id = '" + jobID + "' UNION " + "SELECT p.pay_type as 'pay_type', " +
                "p.is_refund as 'is_refund', p.isVoid as 'isVoid', " +
                "p.pay_id, p.pay_amount AS 'pay_amount', amount_tender,'Reward' AS  'paymethod_name',p.pay_tip AS 'pay_tip'," +
                "p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4'," +
                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber'," +
                "p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p " +
                "WHERE p.paymethod_id = 'Reward' " + "AND p.job_id = '" + jobID + "' UNION " + "SELECT p.pay_type as 'pay_type', " +
                "p.is_refund as 'is_refund', p.isVoid as 'isVoid', " +
                "p.pay_id, p.pay_amount AS 'pay_amount', amount_tender,'GiftCard' AS  'paymethod_name',p.pay_tip AS 'pay_tip'," +
                "p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4'," +
                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber'," +
                "p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p " +
                "WHERE p.paymethod_id = 'GiftCard' " + "AND p.job_id = '" + jobID + "' UNION " + "SELECT p.pay_type as 'pay_type', " +
                "p.is_refund as 'is_refund', p.isVoid as 'isVoid', " +
                "p.pay_id, p.pay_amount AS 'pay_amount', amount_tender,p.card_type AS  'paymethod_name',p.pay_tip AS 'pay_tip'," +
                "p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4'," +
                "p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber'," +
                "p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM Payments p " +
                "WHERE p.paymethod_id = 'Genius' " + "AND p.job_id = '" + jobID + "' UNION " + "SELECT p.pay_type as 'pay_type', " +
                "p.is_refund as 'is_refund', p.isVoid as 'isVoid', " +
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
                details.setIs_refund(cursor.getString(cursor.getColumnIndex(is_refund)));
                details.setIsVoid(cursor.getString(cursor.getColumnIndex(isVoid)));
                details.setPayType(cursor.getString(cursor.getColumnIndex(pay_type)));
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

        Cursor cursor = getDatabase().rawQuery(sb.toString(), null);
        PaymentDetails payDetail = new PaymentDetails();

        if (cursor.moveToFirst()) {

            do {
                payDetail.setPaymethod_name(cursor.getString(cursor.getColumnIndex("paymethod_name")));
                payDetail.setPay_date(Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(pay_date)), 0));
                payDetail.setPay_timecreated(Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(pay_timecreated)), 2));
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

        Cursor cursor = getDatabase().rawQuery(sb.toString(), new String[]{date});
        HashMap<String, String> map = new HashMap<>();
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
        List<Payment> listPayment = new ArrayList<>();

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

        Cursor c = getDatabase().rawQuery(query.toString(), where_values);

        if (c.moveToFirst()) {
            int i_card_type = c.getColumnIndex(card_type);
            int i_pay_amount = c.getColumnIndex(pay_amount);
            int i_pay_tip = c.getColumnIndex(pay_tip);
            int i_pay_id = c.getColumnIndex(pay_id);
            int i_job_id = c.getColumnIndex(job_id);

            do {
                Payment payment = new Payment(activity);

                payment.setCard_type(c.getString(i_card_type));
                payment.setPay_amount(c.getString(i_pay_amount));
                payment.setPay_tip(c.getString(i_pay_tip));
                payment.setPay_id(c.getString(i_pay_id));
                payment.setJob_id(c.getString(i_job_id));

                listPayment.add(payment);

            } while (c.moveToNext());
        }

        c.close();
        return listPayment;

    }

    public List<Payment> getPaymentsGroupDayReport(int type, String clerk_id, String date) {
        StringBuilder query = new StringBuilder();
        List<Payment> listPayment = new ArrayList<>();

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

        Cursor c = getDatabase().rawQuery(query.toString(), where_values);

        if (c.moveToFirst()) {
            int i_card_type = c.getColumnIndex(card_type);
            int i_pay_amount = c.getColumnIndex(pay_amount);
            int i_pay_tip = c.getColumnIndex(pay_tip);
            int i_pay_id = c.getColumnIndex(pay_id);
            int i_job_id = c.getColumnIndex(job_id);
            int i_paymethod_id = c.getColumnIndex(paymethod_id);
            do {
                Payment payment = new Payment(activity);

                payment.setCard_type(c.getString(i_card_type));
                payment.setPay_amount(c.getString(i_pay_amount));
                payment.setPay_tip(c.getString(i_pay_tip));
                payment.setPay_id(c.getString(i_pay_id));
                payment.setJob_id(c.getString(i_job_id));
                payment.setPaymethod_id(c.getString(i_paymethod_id));

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

            SQLiteStatement stmt = getDatabase().compileStatement(sb.toString());
            Cursor cursor = getDatabase().rawQuery(sb.toString(), null);
            cursor.moveToFirst();
            lastPayID = cursor.getString(0);
            cursor.close();
            stmt.close();
            if (TextUtils.isEmpty(lastPayID)) {
                AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
                lastPayID = assignEmployee.getEmpId() + "-" + "00001" + "-" + year;
            }
            myPref.setLastPayID(lastPayID);
        }
        return lastPayID;
    }

    public static PaymentsHandler getInstance(Context activity) {
        return new PaymentsHandler(activity);
    }

    public void insertDeclined(Payment payment) {
        getDatabase().beginTransaction();
        try {
            SQLiteStatement insert;
            insert = getDatabase().compileStatement("INSERT INTO " + table_name_declined + " (" + sb1.toString() + ")" + "VALUES (" + sb2.toString() + ")");
            insert.bindString(index(pay_id), payment.getPay_id() == null ? "" : payment.getPay_id()); // pay_id
            insert.bindString(index(group_pay_id), payment.getGroup_pay_id() == null ? "" : payment.getGroup_pay_id()); // group_pay_id
            insert.bindString(index(original_pay_id), payment.getOriginal_pay_id() == null ? "" : payment.getOriginal_pay_id()); // group_pay_id
            insert.bindString(index(cust_id), payment.getCust_id() == null ? "" : payment.getCust_id()); // cust_id
            insert.bindString(index(tupyx_user_id), payment.getTupyx_user_id() == null ? "" : payment.getTupyx_user_id());
            insert.bindString(index(custidkey), payment.getCustidkey() == null ? "" : payment.getCustidkey()); // custidkey
            insert.bindString(index(emp_id), payment.getEmp_id() == null ? "" : payment.getEmp_id()); // emp_id
            insert.bindString(index(inv_id), payment.getInv_id() == null ? "" : payment.getInv_id()); // inv_id
            insert.bindString(index(paymethod_id), payment.getPaymethod_id() == null ? "" : payment.getPaymethod_id()); // paymethod_id
            insert.bindString(index(pay_check), payment.getPay_check() == null ? "" : payment.getPay_check()); // pay_check
            insert.bindString(index(pay_receipt), payment.getPay_receipt() == null ? "" : payment.getPay_receipt()); // pay_receipt
            insert.bindString(index(pay_amount), TextUtils.isEmpty(payment.getPay_amount()) ? "0" : payment.getPay_amount()); // pay_amount
            insert.bindString(index(pay_dueamount),
                    TextUtils.isEmpty(payment.getPay_dueamount()) ? "0" : payment.getPay_dueamount()); // pay_dueamount;
            insert.bindString(index(pay_comment), payment.getPay_comment() == null ? "" : payment.getPay_comment()); // pay_comment
            insert.bindString(index(pay_timecreated), payment.getPay_timecreated() == null ? "" : payment.getPay_timecreated()); // pay_timecreated
            insert.bindString(index(pay_timesync), payment.getPay_timesync() == null ? "" : payment.getPay_timesync()); // pay_timesync
            insert.bindString(index(account_id), payment.getAccount_id() == null ? "" : payment.getAccount_id()); // account_id
            insert.bindString(index(processed), TextUtils.isEmpty(payment.getProcessed()) ? "0" : payment.getProcessed()); // processed
            insert.bindString(index(pay_issync), TextUtils.isEmpty(payment.getPay_issync()) ? "0" : payment.getPay_issync()); // pay_issync
            insert.bindString(index(pay_transid), payment.getPay_transid() == null ? "" : payment.getPay_transid()); // pay_transid
            insert.bindString(index(pay_refnum), payment.getPay_refnum() == null ? "" : payment.getPay_refnum()); // pay_refnum
            insert.bindString(index(pay_name), payment.getPay_name() == null ? "" : payment.getPay_name()); // pay_name
            insert.bindString(index(pay_addr), payment.getPay_addr() == null ? "" : payment.getPay_addr()); // pay_addr
            insert.bindString(index(pay_poscode), payment.getPay_poscode() == null ? "" : payment.getPay_poscode()); // pay_poscode
            insert.bindString(index(pay_seccode), payment.getPay_seccode() == null ? "" : payment.getPay_seccode()); // pay_seccode
            insert.bindString(index(pay_maccount), payment.getPay_maccount() == null ? "" : payment.getPay_maccount()); // pay_maccount
            insert.bindString(index(pay_groupcode), payment.getPay_groupcode() == null ? "" : payment.getPay_groupcode()); // pay_groupcode
            insert.bindString(index(pay_stamp), payment.getPay_stamp() == null ? "" : payment.getPay_stamp()); // pay_stamp
            insert.bindString(index(pay_resultcode), payment.getPay_resultcode() == null ? "" : payment.getPay_resultcode()); // pay_resultcode
            insert.bindString(index(pay_resultmessage),
                    payment.getPay_resultmessage() == null ? "" : payment.getPay_resultmessage()); // pay_resultmessage
            insert.bindString(index(pay_ccnum), payment.getPay_ccnum() == null ? "" : payment.getPay_ccnum()); // pay_ccnum
            insert.bindString(index(pay_expmonth), payment.getPay_expmonth() == null ? "" : payment.getPay_expmonth()); // pay_expMonth
            insert.bindString(index(pay_expyear), payment.getPay_expyear() == null ? "" : payment.getPay_expyear()); // pay_expyear
            insert.bindString(index(pay_expdate), payment.getPay_expdate() == null ? "" : payment.getPay_expdate()); // pay_expdate
            insert.bindString(index(pay_result), payment.getPay_result() == null ? "" : payment.getPay_result()); // pay_result
            insert.bindString(index(pay_date), payment.getPay_date() == null ? "" : payment.getPay_date()); // pay_date
            insert.bindString(index(recordnumber), payment.getRecordnumber() == null ? "" : payment.getRecordnumber()); // recordnumber
            insert.bindString(index(pay_signature), payment.getPay_signature() == null ? "" : payment.getPay_signature()); // pay_signaute
            insert.bindString(index(authcode), payment.getAuthcode() == null ? "" : payment.getAuthcode()); // authcode
            insert.bindString(index(status), payment.getStatus() == null ? "" : payment.getStatus()); // status
            insert.bindString(index(job_id), payment.getJob_id() == null ? "" : payment.getJob_id()); // job_id

            insert.bindString(index(user_ID), payment.getUser_ID() == null ? "" : payment.getUser_ID()); // user_ID
            insert.bindString(index(pay_type), payment.getPay_type() == null ? "" : payment.getPay_type()); // pay_type
            insert.bindString(index(pay_tip), TextUtils.isEmpty(payment.getPay_tip()) ? "0" : payment.getPay_tip()); // pay_tip
            insert.bindString(index(ccnum_last4), payment.getCcnum_last4() == null ? "" : payment.getCcnum_last4()); // ccnum_last4
            insert.bindString(index(pay_phone), payment.getPay_phone() == null ? "" : payment.getPay_phone()); // pay_phone
            insert.bindString(index(pay_email), payment.getPay_email() == null ? "" : payment.getPay_email()); // pay_email
            insert.bindString(index(isVoid), TextUtils.isEmpty(payment.getIsVoid()) ? "0" : payment.getIsVoid()); // isVoid
            insert.bindString(index(pay_latitude), payment.getPay_latitude() == null ? "" : payment.getPay_latitude()); // pay_latitude
            insert.bindString(index(pay_longitude), payment.getPay_longitude() == null ? "" : payment.getPay_longitude()); // pay_longitude
            insert.bindString(index(tipAmount), TextUtils.isEmpty(payment.getTipAmount()) ? "0" : payment.getTipAmount()); // tipAmount
            insert.bindString(index(clerk_id), payment.getClerk_id() == null ? "" : payment.getClerk_id()); // clerk_id

            insert.bindString(index(is_refund), TextUtils.isEmpty(payment.getIs_refund()) ? "0" : payment.getIs_refund()); // is_refund
            insert.bindString(index(ref_num), payment.getRef_num() == null ? "" : payment.getRef_num()); // ref_num
            insert.bindString(index(card_type), payment.getCard_type() == null ? "" : payment.getCard_type()); // card_type

            insert.bindString(index(IvuLottoDrawDate),
                    payment.getIvuLottoDrawDate() == null ? "" : payment.getIvuLottoDrawDate()); // IvuLottoDrawData
            insert.bindString(index(IvuLottoNumber), payment.getIvuLottoNumber() == null ? "" : payment.getIvuLottoNumber()); // IvuLottoNumber
            insert.bindString(index(IvuLottoQR), payment.getIvuLottoQR() == null ? "" : payment.getIvuLottoQR()); // IvuLottoQR

            insert.bindString(index(Tax1_amount), TextUtils.isEmpty(payment.getTax1_amount()) ? "0" : payment.getTax1_amount());
            insert.bindString(index(Tax1_name), payment.getTax1_name() == null ? "" : payment.getTax1_name());
            insert.bindString(index(Tax2_amount), TextUtils.isEmpty(payment.getTax2_amount()) ? "0" : payment.getTax2_amount());
            insert.bindString(index(Tax2_name), payment.getTax2_name() == null ? "" : payment.getTax2_name());
            insert.bindString(index(EMVJson), payment.getEmvContainer() == null ? "" : new Gson().toJson(payment.getEmvContainer(), EMVContainer.class));

            insert.execute();
            insert.clearBindings();
            insert.close();
            getDatabase().setTransactionSuccessful();

        } catch (Exception e) {
            Log.d("Exception", e.getMessage() + " [com.android.emobilepos.PaymentsHandler (at Class.insertDeclined)]");
        } finally {
            myPref.setLastPayID(payment.getPay_id());
            lastPaymentInserted = payment;
            getDatabase().endTransaction();
        }
    }
}
