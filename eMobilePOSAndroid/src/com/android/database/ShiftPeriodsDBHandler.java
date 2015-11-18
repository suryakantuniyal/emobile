package com.android.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.emobilepos.models.ShiftPeriods;
import com.android.support.DBManager;
import com.android.support.Global;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.SparseArray;
import net.sqlcipher.database.SQLiteStatement;

public class ShiftPeriodsDBHandler {
	private final String shift_id = "shift_id";
	private final String assignee_id = "assignee_id";
	private final String assignee_name = "assignee_name";
	private final String creationDate = "creationDate";
	private final String creationDateLocal = "creationDateLocal";
	private final String startTime = "startTime";
	private final String startTimeLocal = "startTimeLocal";
	private final String endTime = "endTime";
	private final String endTimeLocal = "endTimeLocal";
	private final String beginning_petty_cash = "beginning_petty_cash";
	private final String ending_petty_cash = "ending_petty_cash";
	private final String entered_close_amount = "entered_close_amount";
	private final String total_transaction_cash = "total_transaction_cash";
	private final String shift_issync = "shift_issync";

	public final List<String> attr = Arrays.asList(new String[] { shift_id, assignee_id, assignee_name, creationDate,
			creationDateLocal, startTime, startTimeLocal, endTime, endTimeLocal, beginning_petty_cash,
			ending_petty_cash, entered_close_amount, total_transaction_cash, shift_issync });

	public StringBuilder sb1, sb2;
	public final String empStr = "";
	public HashMap<String, Integer> attrHash;
	public Global global;

	private Activity activity;

	public static final String table_name = "ShiftPeriods";

	public ShiftPeriodsDBHandler(Activity activity) {
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

	public void insert(ShiftPeriods periods) {
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

			insert.bindString(index(shift_id), periods.shift_id == null ? "" : periods.shift_id); // shift_id
			insert.bindString(index(assignee_id), periods.assignee_id == null ? "" : periods.assignee_id); // assignee_id
			insert.bindString(index(assignee_name), periods.assignee_name == null ? "" : periods.assignee_name); // assignee_name
			insert.bindString(index(creationDate), periods.creationDate == null ? "" : periods.creationDate); // creationDate
			insert.bindString(index(creationDateLocal),
					periods.creationDateLocal == null ? "" : periods.creationDateLocal); // creatingDateLocal
			insert.bindString(index(startTime), periods.startTime == null ? "" : periods.startTime); // startTime
			insert.bindString(index(startTimeLocal), periods.startTimeLocal == null ? "" : periods.startTimeLocal); // startTimeLocal
			insert.bindString(index(endTime), periods.endTime == null ? "" : periods.endTime); // endTime
			insert.bindString(index(endTimeLocal), periods.endTimeLocal == null ? "" : periods.endTimeLocal); // endTimeLocal
			insert.bindString(index(beginning_petty_cash),
					periods.beginning_petty_cash == null ? "0" : periods.beginning_petty_cash); // beginning_petty_cash
			insert.bindString(index(ending_petty_cash),
					periods.ending_petty_cash == null ? "0" : periods.ending_petty_cash); // ending_petty_cash
			insert.bindString(index(entered_close_amount),
					periods.entered_close_amount == null ? "0" : periods.entered_close_amount); // entered_close_amount
			insert.bindString(index(total_transaction_cash),
					periods.total_transaction_cash == null ? "0" : periods.total_transaction_cash); // total_transaction_cash
			insert.bindString(index(shift_issync), periods.shift_issync == null ? "" : periods.shift_issync);

			insert.execute();
			insert.clearBindings();
			insert.close();
			DBManager._db.setTransactionSuccessful();

		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.ShiftPeriodsDBHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
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

	public void updateShiftAmounts(String shiftID, double amount, boolean isReturn) {
		// SQLiteDatabase db = dbManager.openWritableDB();
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT total_transaction_cash,ending_petty_cash FROM ").append(table_name)
				.append(" WHERE shift_id=?");
		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[] { shiftID });

		if (c.moveToFirst()) {
			double totalTransactionCash = c.getDouble(c.getColumnIndex("total_transaction_cash"));

			if (!isReturn)
				totalTransactionCash += amount;
			else
				totalTransactionCash -= amount;

			sb.setLength(0);
			sb.append(shift_id).append(" = ?");
			ContentValues args = new ContentValues();
			args.put(total_transaction_cash, Double.toString(totalTransactionCash));
			DBManager._db.update(table_name, args, sb.toString(), new String[] { shiftID });
			c.close();
		}
	}

	public void updateShift(String shiftID, String attr, String val) {
		// SQLiteDatabase db = dbManager.openWritableDB();
		StringBuilder sb = new StringBuilder();

		sb.append(shift_id).append(" = ?");

		ContentValues args = new ContentValues();

		args.put(attr, val);
		DBManager._db.update(table_name, args, sb.toString(), new String[] { shiftID });

		// db.close();
	}

	public SparseArray<String> getShiftDetails(String shiftID) {
		// SQLiteDatabase db = dbManager.openReadableDB();
		SparseArray<String> map = new SparseArray<String>();
		StringBuilder sb = new StringBuilder();

		sb.append(
				"SELECT assignee_name,beginning_petty_cash,ending_petty_cash,total_transaction_cash,ROUND(ending_petty_cash+total_transaction_cash,2) as 'total_ending_cash',entered_close_amount, CASE WHEN endTime != '' THEN endTime ELSE 'Open' END AS 'end_type' FROM ")
				.append(table_name).append(" WHERE shift_id = ?");

		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[] { shiftID });

		if (c.moveToFirst()) {

			map.put(0, c.getString(c.getColumnIndex(assignee_name)));
			map.put(1, Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex(beginning_petty_cash))));
			map.put(2, Global.formatDoubleStrToCurrency("0"));
			map.put(3, Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex(ending_petty_cash))));
			map.put(4, Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex(total_transaction_cash))));
			map.put(5, Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex("total_ending_cash"))));

			if (!c.getString(c.getColumnIndex("end_type")).equals("Open")) {
				sb.setLength(0);

				double temp1 = Double.parseDouble(c.getString(c.getColumnIndex("total_ending_cash")));
				double temp2 = Double.parseDouble(c.getString(c.getColumnIndex(entered_close_amount)));
				if (temp1 == temp2)
					map.put(6, Global.formatDoubleToCurrency(temp2));
				else if (temp2 < temp1) {
					sb.append(Global.formatDoubleToCurrency(temp2)).append(" ").append("(");
					sb.append(Global.formatDoubleToCurrency(temp1 - temp2)).append(" is Short)");
					map.put(6, sb.toString());
				} else {
					sb.append(Global.formatDoubleToCurrency(temp2)).append(" ").append("(");
					sb.append(Global.formatDoubleToCurrency(temp2 - temp1)).append(" is Over)");
					map.put(6, sb.toString());
				}

			} else {
				map.put(6, Global.formatDoubleStrToCurrency(c.getString(c.getColumnIndex(entered_close_amount))));
			}

		}
		c.close();
		// db.close();
		return map;
	}

	public Cursor getAllShiftsReport(String date) {
		// SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();

		sb.append(
				"SELECT shift_id,startTime,date(creationDate,'localtime') as 'date', CASE WHEN endTime != '' THEN endTime ELSE 'Open' END AS 'end_type',"
						+ "assignee_name,beginning_petty_cash FROM ")
				.append(table_name).append(" WHERE date = ? ORDER BY startTime DESC");

		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[] { date });

		c.moveToFirst();
		// db.close();
		return c;
	}

	public long getNumUnsyncShifts() {
		// SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE shift_issync = '0'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		// db.close();
		return count;
	}

	public void updateIsSync(List<String[]> list) {
		// SQLiteDatabase db = dbManager.openWritableDB();

		StringBuilder sb = new StringBuilder();
		sb.append(shift_id).append(" = ?");

		ContentValues args = new ContentValues();

		int size = list.size();
		for (int i = 0; i < size; i++) {
			if (isShiftFinished(list.get(i)[1]) == 1) {
				if (list.get(i)[0].equals("0"))
					args.put(shift_issync, "1");
				else
					args.put(shift_issync, "0");
				DBManager._db.update(table_name, args, sb.toString(), new String[] { list.get(i)[1] });
			}
		}
		// db.close();
	}

	private long isShiftFinished(String _shiftID) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE shift_id = '");
		sb.append(_shiftID).append("' AND endTime != ''");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();
		return count;
	}

	public Cursor getUnsyncShifts() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT *,ROUND(ending_petty_cash+total_transaction_cash,2) as 'total_ending_cash' FROM ")
				.append(table_name).append(" WHERE shift_issync = '0'");
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);
		return cursor;
	}

	public List<ShiftPeriods> getShiftDayReport(String clerk_id, String date) {
		StringBuilder query = new StringBuilder();
		List<ShiftPeriods> listShifts = new ArrayList<ShiftPeriods>();

		query.append(
				"SELECT assignee_name,beginning_petty_cash,'0' as 'total_expenses',ending_petty_cash,total_transaction_cash,");
		query.append(
				"ROUND(ending_petty_cash+total_transaction_cash,2) as 'total_ending_cash',entered_close_amount, startTime,");
		query.append(
				"CASE WHEN endTime != '' THEN endTime ELSE 'Open' END AS 'end_type',date(startTime,'localtime') as 'date' FROM ShiftPeriods ");

		String[] where_values = null;
		if (clerk_id != null && !clerk_id.isEmpty()) {
			query.append("WHERE assignee_id = ? ");
			where_values = new String[] { clerk_id };

			if (date != null && !date.isEmpty()) {
				query.append(" AND date = ? ");
				where_values = new String[] { clerk_id, date };
			}
		} else if (date != null && !date.isEmpty()) {
			query.append(" WHERE date = ? ");
			where_values = new String[] { date };
		}

		Cursor c = DBManager._db.rawQuery(query.toString(), where_values);

		if (c.moveToFirst()) {
			int i_assignee_name = c.getColumnIndex(assignee_name);
			int i_beginning_petty_cash = c.getColumnIndex(beginning_petty_cash);
			int i_total_expenses = c.getColumnIndex("total_expenses");
			int i_ending_petty_cash = c.getColumnIndex(ending_petty_cash);
			int i_total_transaction_cash = c.getColumnIndex(total_transaction_cash);
			int i_total_ending_cash = c.getColumnIndex("total_ending_cash");
			int i_entered_close_amount = c.getColumnIndex(entered_close_amount);
			int i_start_time = c.getColumnIndex(startTime);
			int i_end_type = c.getColumnIndex("end_type");

			do {
				ShiftPeriods shift = new ShiftPeriods(true);

				shift.assignee_name = c.getString(i_assignee_name);
				shift.startTime = c.getString(i_start_time);
				shift.endTime = c.getString(i_end_type);
				shift.beginning_petty_cash = c.getString(i_beginning_petty_cash);
				shift.total_expenses = c.getString(i_total_expenses);
				shift.ending_petty_cash = c.getString(i_ending_petty_cash);
				shift.total_transaction_cash = c.getString(i_total_transaction_cash);
				shift.total_ending_cash = c.getString(i_total_ending_cash);
				shift.entered_close_amount = c.getString(i_entered_close_amount);

				if (!shift.endTime.equals("Open")) {
					query.setLength(0);

					double temp1 = Double.parseDouble(shift.total_ending_cash);
					double temp2 = Double.parseDouble(shift.entered_close_amount);
					if (temp2 < temp1) {
						query.append(Global.formatDoubleToCurrency(temp2)).append("(");
						query.append(Global.formatDoubleToCurrency(temp1 - temp2)).append(" Short)");
						shift.entered_close_amount = query.toString();
					} else {
						query.append(Global.formatDoubleToCurrency(temp2)).append("(");
						query.append(Global.formatDoubleToCurrency(temp2 - temp1)).append(" Over)");
						shift.entered_close_amount = query.toString();
					}

				}

				listShifts.add(shift);
			} while (c.moveToNext());

		}

		c.close();
		return listShifts;
	}

}
