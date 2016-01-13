package com.android.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;

import com.android.emobilepos.models.TimeClock;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TimeClockHandler

{
	private final String timeclockid = "timeclockid";
	private final String emp_id = "emp_id";
	private final String status = "status";
	private final String punchtime = "punchtime";
	private final String updated = "updated";
	private final String issync = "issync";

	public final List<String> attr = Arrays
			.asList(timeclockid, emp_id, status, punchtime, updated, issync);

	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	private static final String table_name = "TimeClock";

	public TimeClockHandler(Activity activity) {

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

	public void insert(List<TimeClock> timeClock, boolean deleteAll) {
		// SQLiteDatabase db = dbManager.openWritableDB();
		DBManager._db.beginTransaction();
		try {

			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ")
					.append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = timeClock.size();

			for (int i = 0; i < size; i++) {
				if (deleteAll)
					DBManager._db.delete(table_name, "emp_id = ? AND status = ?",
							new String[] { timeClock.get(i).emp_id, timeClock.get(i).status });
				insert.bindString(index(timeclockid), timeClock.get(i).timeclockid); // timeclockid
				insert.bindString(index(emp_id), timeClock.get(i).emp_id); // emp_id
				insert.bindString(index(status), timeClock.get(i).status); // status
				insert.bindString(index(punchtime), timeClock.get(i).punchtime); // punchtime
				insert.bindString(index(updated), timeClock.get(i).updated); // updated
				insert.bindString(index(issync), timeClock.get(i).issync); // issync

				insert.execute();
				insert.clearBindings();

			}
			insert.close();
			DBManager._db.setTransactionSuccessful();

		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.TimeClockHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
			DBManager._db.endTransaction();
		}
		// db.close();
	}

	public List<TimeClock> getEmployeeTimeClock(String empID) {
		TimeClock tempTC = new TimeClock();
		List<TimeClock> listTC = new ArrayList<TimeClock>();
		StringBuilder sb = new StringBuilder();
		// SQLiteDatabase db = dbManager.openReadableDB();

		sb.append("SELECT * FROM ").append(table_name).append(" WHERE emp_id = ? ORDER BY punchtime DESC LIMIT 2");

		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[] { empID });

		if (c.moveToFirst()) {
			int i_timeclockid = c.getColumnIndex(timeclockid);
			int i_emp_id = c.getColumnIndex(emp_id);
			int i_status = c.getColumnIndex(status);
			int i_punchtime = c.getColumnIndex(punchtime);

			do {
				tempTC.timeclockid = c.getString(i_timeclockid);
				tempTC.emp_id = c.getString(i_emp_id);
				tempTC.status = c.getString(i_status);
				tempTC.punchtime = c.getString(i_punchtime);

				listTC.add(tempTC);
				tempTC = new TimeClock();

			} while (c.moveToNext());
		}
		c.close();
		// db.close();
		return listTC;

	}

	public Cursor getAllUnsync() // Will populate all unsynchronized orders for
									// XML post
	{
		// if(db==null||!db.isOpen())
		// db = dbManager.openReadableDB();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ").append(sb1.toString()).append(" FROM ").append(table_name)
				.append(" WHERE issync = '0' and emp_id NOT NULL and trim(emp_id) <> ''");
		Cursor cursor = DBManager._db.rawQuery(sb.toString(), null);

		return cursor;
	}

	public void updateIsSync(String timeclockID, String status) {
		// SQLiteDatabase db =
		// SQLiteDatabase.openDatabase(myPref.getDBpath(),Global.dbPass, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS|
		// SQLiteDatabase.OPEN_READWRITE);

		// if(db==null||!db.isOpen())
		// db = dbManager.openWritableDB();
		StringBuilder sb = new StringBuilder();
		sb.append(this.timeclockid).append(" = ?");

		ContentValues args = new ContentValues();

		if (status.equals("0"))
			args.put(issync, "1");
		else
			args.put(issync, "0");
		DBManager._db.update(table_name, args, sb.toString(), new String[] { timeclockID });
	}

	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_name);
		DBManager._db.execSQL(sb.toString());
	}

	public long getNumUnsyncTimeClock() {

		// if(db==null||!db.isOpen())
		// db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT Count(*) FROM ").append(table_name).append(" WHERE issync = '0'");

		SQLiteStatement stmt = DBManager._db.compileStatement(sb.toString());
		long count = stmt.simpleQueryForLong();
		stmt.close();

		return count;
	}

}
