package com.android.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.emobilepos.models.Payment;
import com.android.support.DBManager;
import com.android.support.Global;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteStatement;

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

	public final List<String> attr = Arrays.asList(new String[] { pay_id, group_pay_id, cust_id, tupyx_user_id, emp_id,
			inv_id, paymethod_id, pay_check, pay_receipt, pay_amount, pay_dueamount, pay_comment, pay_timecreated,
			pay_timesync, account_id, processed, pay_issync, pay_transid, pay_refnum, pay_name, pay_addr, pay_poscode,
			pay_seccode, pay_maccount, pay_groupcode, pay_stamp, pay_resultcode, pay_resultmessage, pay_ccnum,
			pay_expmonth, pay_expyear, pay_expdate, pay_result, pay_date, recordnumber, pay_signature, authcode, status,
			job_id, user_ID, pay_type, pay_tip, ccnum_last4, pay_phone, pay_email, isVoid, pay_latitude, pay_longitude,
			tipAmount, clerk_id, is_refund, ref_num, IvuLottoDrawDate, IvuLottoNumber, IvuLottoQR, card_type,
			Tax1_amount, Tax1_name, Tax2_amount, Tax2_name, custidkey, original_pay_id, pay_uuid, is_retry,
			payment_xml });

	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	private Global global;
	private static final String table_name = "StoredPayments";
	private Activity activity;

	public StoredPayments_DB(Activity activity) {
		global = (Global) activity.getApplication();
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
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ")
					.append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			insert.bindString(index(pay_id), payment.pay_id); // pay_id
			insert.bindString(index(group_pay_id), payment.group_pay_id); // group_pay_id
			insert.bindString(index(original_pay_id), payment.original_pay_id); // group_pay_id
			insert.bindString(index(cust_id), payment.cust_id); // cust_id
			insert.bindString(index(tupyx_user_id), payment.tupyx_user_id);
			insert.bindString(index(custidkey), payment.custidkey); // custidkey
			insert.bindString(index(emp_id), payment.emp_id); // emp_id
			insert.bindString(index(inv_id), payment.inv_id); // inv_id
			insert.bindString(index(paymethod_id), payment.paymethod_id); // paymethod_id
			insert.bindString(index(pay_check), payment.pay_check); // pay_check
			insert.bindString(index(pay_receipt), payment.pay_receipt); // pay_receipt
			insert.bindString(index(pay_amount), payment.pay_amount); // pay_amount
			insert.bindString(index(pay_dueamount), payment.pay_dueamount); // pay_dueamount;
			insert.bindString(index(pay_comment), payment.pay_comment); // pay_comment
			insert.bindString(index(pay_timecreated), payment.pay_timecreated); // pay_timecreated
			insert.bindString(index(pay_timesync), payment.pay_timesync); // pay_timesync
			insert.bindString(index(account_id), payment.account_id); // account_id
			insert.bindString(index(processed), payment.processed); // processed
			insert.bindString(index(pay_issync), payment.pay_issync); // pay_issync
			insert.bindString(index(pay_transid), payment.pay_transid); // pay_transid
			insert.bindString(index(pay_refnum), payment.pay_refnum); // pay_refnum
			insert.bindString(index(pay_name), payment.pay_name); // pay_name
			insert.bindString(index(pay_addr), payment.pay_addr); // pay_addr
			insert.bindString(index(pay_poscode), payment.pay_poscode); // pay_poscode
			insert.bindString(index(pay_seccode), payment.pay_seccode); // pay_seccode
			insert.bindString(index(pay_maccount), payment.pay_maccount); // pay_maccount
			insert.bindString(index(pay_groupcode), payment.pay_groupcode); // pay_groupcode
			insert.bindString(index(pay_stamp), payment.pay_stamp); // pay_stamp
			insert.bindString(index(pay_resultcode), payment.pay_resultcode); // pay_resultcode
			insert.bindString(index(pay_resultmessage), payment.pay_resultmessage); // pay_resultmessage
			insert.bindString(index(pay_ccnum), payment.pay_ccnum); // pay_ccnum
			insert.bindString(index(pay_expmonth), payment.pay_expmonth); // pay_expMonth
			insert.bindString(index(pay_expyear), payment.pay_expyear); // pay_expyear
			insert.bindString(index(pay_expdate), payment.pay_expdate); // pay_expdate
			insert.bindString(index(pay_result), payment.pay_result); // pay_result
			insert.bindString(index(pay_date), payment.pay_date); // pay_date
			insert.bindString(index(recordnumber), payment.recordnumber); // recordnumber
			insert.bindString(index(pay_signature), payment.pay_signature); // pay_signaute
			insert.bindString(index(authcode), payment.authcode); // authcode
			insert.bindString(index(status), payment.status); // status
			insert.bindString(index(job_id), payment.job_id); // job_id

			insert.bindString(index(user_ID), payment.user_ID); // user_ID
			insert.bindString(index(pay_type), payment.pay_type); // pay_type
			insert.bindString(index(pay_tip), payment.pay_tip); // pay_tip
			insert.bindString(index(ccnum_last4), payment.ccnum_last4); // ccnum_last4
			insert.bindString(index(pay_phone), payment.pay_phone); // pay_phone
			insert.bindString(index(pay_email), payment.pay_email); // pay_email
			insert.bindString(index(isVoid), payment.isVoid); // isVoid
			insert.bindString(index(pay_latitude), payment.pay_latitude); // pay_latitude
			insert.bindString(index(pay_longitude), payment.pay_longitude); // pay_longitude
			insert.bindString(index(tipAmount), payment.tipAmount); // tipAmount
			insert.bindString(index(clerk_id), payment.clerk_id); // clerk_id

			insert.bindString(index(is_refund), payment.is_refund); // is_refund
			insert.bindString(index(ref_num), payment.ref_num); // ref_num
			insert.bindString(index(card_type), payment.card_type); // card_type

			insert.bindString(index(IvuLottoDrawDate), payment.IvuLottoDrawDate); // IvuLottoDrawData
			insert.bindString(index(IvuLottoNumber), payment.IvuLottoNumber); // IvuLottoNumber
			insert.bindString(index(IvuLottoQR), payment.IvuLottoQR); // IvuLottoQR

			insert.bindString(index(Tax1_amount), payment.Tax1_amount);
			insert.bindString(index(Tax1_name), payment.Tax1_name);
			insert.bindString(index(Tax2_amount), payment.Tax2_amount);
			insert.bindString(index(Tax2_name), payment.Tax2_name);

			insert.bindString(index(pay_uuid), payment.pay_uuid);
			insert.bindString(index(is_retry), payment.is_retry);
			insert.bindString(index(payment_xml), payment.payment_xml);

			insert.execute();
			insert.clearBindings();
			insert.close();
			DBManager._db.setTransactionSuccessful();

		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.StoredPayments (at Class.insert)]");

			Tracker tracker = EasyTracker.getInstance(activity);
			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
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

	public String updateSignaturePayment(String _pay_uuid) {
		// SQLiteDatabase db = dbManager.openWritableDB();

		StringBuilder sb = new StringBuilder();
		sb.append(pay_id).append(" = ?");

		ContentValues args = new ContentValues();

		args.put(pay_signature, global.encodedImage);

		DBManager._db.update(table_name, args, sb.toString(), new String[] { _pay_uuid });
		sb.setLength(0);
		sb.append("SELECT pay_amount FROM ").append(table_name).append(" WHERE pay_uuid = '").append(_pay_uuid)
				.append("'");
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		String returningVal = "";

		if (cursor.moveToFirst()) {
			returningVal = cursor.getString(cursor.getColumnIndex(pay_amount));
		}

		cursor.close();
		// db.close();
		return returningVal;
	}

	public Cursor getStoredPayments() {
		// if(!db.isOpen())
		// db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT pay_uuid as '_id', * FROM ").append(table_name);
		Cursor c = DBManager._db.rawQuery(sb.toString(), null);

		c.moveToFirst();

		return c;
	}

	public long getRetryTransCount(String _job_id) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE job_id = '").append(_job_id)
				.append("' AND is_retry = '1'");
		// SQLiteDatabase db = dbManager.openReadableDB();
		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		// db.close();
		return count;
	}

	public long getCountPendingStoredPayments(String _job_id) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE job_id = '").append(_job_id).append("'");
		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		return count;
	}

	public void deletePaymentFromJob(String _job_id) {
		// SQLiteDatabase db = dbManager.openWritableDB();
		DBManager._db.delete(table_name, "job_id = ?", new String[] { _job_id });
		// db.close();
	}

	public String[] getPrintingForPaymentDetails(String payID, int type) {
		// SQLiteDatabase db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();

		if (type == 0) // May come from History>Payment>Details
		{

			sb.append(
					"SELECT p.inv_id,p.job_id, CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name',p.pay_date,p.pay_timecreated,IFNULL(c.cust_name,'Unknown') as 'cust_name', o.ord_total,p.pay_amount,p.pay_dueamount,"
							+ "CASE WHEN (m.paymethod_name = 'Cash') THEN (o.ord_total-p.pay_amount)  ELSE p.pay_tip END as 'change' ,p.pay_signature, "
							+ "p.pay_transid,p.ccnum_last4,p.pay_check,p.is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR' "
							+ "FROM StoredPayments p,Orders o LEFT OUTER JOIN Customers c  ON c.cust_id = p.cust_id  "
							+ "LEFT OUTER JOIN PayMethods m ON m.paymethod_id = p.paymethod_id WHERE o.ord_id = p.job_id AND p.job_id ='");
		}

		else if (type == 1) // Straight from main menu 'Payment'
		{
			sb.append(
					"SELECT p.inv_id,p.job_id,CASE WHEN p.paymethod_id IN ('Genius','') THEN p.card_type ELSE m.paymethod_name END AS 'paymethod_name',p.pay_date,p.pay_timecreated, IFNULL(c.cust_name,'Unknown') as 'cust_name',p.pay_amount AS 'ord_total',p.pay_amount,p.pay_dueamount,"
							+ "CASE WHEN (m.paymethod_name = 'Cash') THEN SUM(p.pay_amount-p.pay_amount) ELSE p.pay_tip END AS 'change', p.pay_signature,  "
							+ "p.pay_transid,p.ccnum_last4,p.pay_check,p.is_refund,p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR' "
							+ "FROM StoredPayments p LEFT OUTER JOIN Customers c ON c.cust_id =p.cust_id LEFT OUTER JOIN "
							+ "PayMethods m ON p.paymethod_id = m.paymethod_id  WHERE p.pay_id = '");
		}

		sb.append(payID).append("'");

		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		String[] arrayVal = new String[18];

		if (cursor.moveToFirst()) {

			do {
				arrayVal[0] = cursor.getString(cursor.getColumnIndex("paymethod_name"));
				arrayVal[1] = Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(pay_date)), activity,
						0);
				arrayVal[2] = Global.formatToDisplayDate(cursor.getString(cursor.getColumnIndex(pay_timecreated)),
						activity, 2);
				arrayVal[3] = cursor.getString(cursor.getColumnIndex("cust_name"));
				arrayVal[4] = cursor.getString(cursor.getColumnIndex("ord_total"));
				arrayVal[5] = cursor.getString(cursor.getColumnIndex(pay_amount));
				arrayVal[6] = cursor.getString(cursor.getColumnIndex("change"));
				arrayVal[7] = cursor.getString(cursor.getColumnIndex(pay_signature));
				arrayVal[8] = cursor.getString(cursor.getColumnIndex(pay_transid));
				arrayVal[9] = cursor.getString(cursor.getColumnIndex(ccnum_last4));
				arrayVal[10] = cursor.getString(cursor.getColumnIndex(pay_check));
				arrayVal[11] = cursor.getString(cursor.getColumnIndex(is_refund));
				arrayVal[12] = cursor.getString(cursor.getColumnIndex(IvuLottoDrawDate));
				arrayVal[13] = cursor.getString(cursor.getColumnIndex(IvuLottoNumber));
				arrayVal[14] = cursor.getString(cursor.getColumnIndex(IvuLottoQR));
				arrayVal[15] = cursor.getString(cursor.getColumnIndex(pay_dueamount));
				arrayVal[16] = cursor.getString(cursor.getColumnIndex(inv_id));
				arrayVal[17] = cursor.getString(cursor.getColumnIndex(job_id));

			} while (cursor.moveToNext());
		}

		cursor.close();
		// db.close();
		return arrayVal;
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
				"SELECT p.pay_amount AS 'pay_amount',pm.paymethod_name AS 'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM StoredPayments p,");
		sb.append("PayMethods pm WHERE p.paymethod_id = pm.paymethod_id AND p.job_id = '").append(jobID)
				.append("' UNION ");

		sb.append(
				"SELECT p.pay_amount AS 'pay_amount','Wallet' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM StoredPayments p WHERE p.paymethod_id = 'Wallet' ");
		sb.append("AND p.job_id = '").append(jobID).append("' UNION ");

		sb.append(
				"SELECT p.pay_amount AS 'pay_amount','LoyaltyCard' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM StoredPayments p WHERE p.paymethod_id = 'LoyaltyCard' ");
		sb.append("AND p.job_id = '").append(jobID).append("' UNION ");

		sb.append(
				"SELECT p.pay_amount AS 'pay_amount','Reward' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM StoredPayments p WHERE p.paymethod_id = 'Reward' ");
		sb.append("AND p.job_id = '").append(jobID).append("' UNION ");

		sb.append(
				"SELECT p.pay_amount AS 'pay_amount','GiftCard' AS  'paymethod_name',p.pay_tip AS 'pay_tip',p.pay_signature AS 'pay_signature',p.pay_transid AS 'pay_transid',p.ccnum_last4 AS 'ccnum_last4',p.IvuLottoDrawDate AS 'IvuLottoDrawDate',p.IvuLottoNumber AS 'IvuLottoNumber',p.IvuLottoQR AS 'IvuLottoQR',p.pay_dueamount AS 'pay_dueamount' FROM StoredPayments p WHERE p.paymethod_id = 'GiftCard' ");
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

	public void deleteStoredPaymentRow(String _pay_uuid) {
		DBManager._db.delete(table_name, "pay_uuid = ?", new String[] { _pay_uuid });
	}

	public void updateStoredPaymentForRetry(String _pay_uuid) {
		ContentValues args = new ContentValues();
		args.put(is_retry, "1");
		DBManager._db.update(table_name, args, "pay_uuid = ?", new String[] { _pay_uuid });
	}

	public boolean unsyncStoredPaymentsLeft() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name);

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		if (count == 0)
			return false;
		return true;
	}
}
