package com.android.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.support.Global;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class EmployeesHandler {

	private final String emp_id = "emp_id";
	private final String zone_id = "zone_id";
	private final String emp_name = "emp_name";
	private final String emp_init = "emp_init";
	private final String emp_pcs = "emp_pcs";
	private final String emp_carrier = "emp_carrier";
	private final String emp_lastlogin = "emp_lastlogin";
	private final String emp_cleanup = "emp_pos";
	private final String qb_emp_id = "qb_emp_id";
	private final String qb_salesrep_id = "qb_salesrep_id";
	private final String quota_month_goal = "quota_month_goal";
	private final String quota_month = "quota_month";
	private final String quota_year_goal = "quota_year_goal";
	private final String quota_year = "quota_year";
	private final String emp_pwd = "emp_pwd";
	private final String isactive = "isactive";
	private final String email = "email";
	private final String classid = "classid";
	private final String tax_default = "tax_default";
	private final String emp_pos = "emp_pos";
	private final String pricelevel_id = "priceleve_id";

	public final List<String> attr = Arrays.asList(new String[] { "emp_id", "zone_id", "emp_name", "emp_init", "emp_pcs", "emp_carrier",
			"emp_lastlogin", "emp_cleanup", "emp_pos", "qb_emp_id", "qb_salesrep_id", "quota_month_goal", "quota_month", "quota_year_goal",
			"quota_year", "emp_pwd", "isactive", "email", "classid", "tax_default", "pricelevel_id" });

	public StringBuilder sb1, sb2;
	private List<String[]> custData;
	public final String empStr = "";
	public HashMap<String, Integer> attrHash;
	public Global global;
	private Activity activity;

	public static final String table_name = "Employees";

	public EmployeesHandler(Activity activity) {
		global = (Global) activity.getApplication();
		this.activity = activity;
		attrHash = new HashMap<String, Integer>();
		custData = new ArrayList<String[]>();
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

	public String getData(String tag, int record) {
		Integer i = global.dictionary.get(record).get(tag);
		if (i != null) {
			return custData.get(record)[i];
		}
		return empStr;
	}

	public int index(String tag) {
		return attrHash.get(tag);
	}

	public void insert(SQLiteDatabase db, List<String[]> data) {
		db.beginTransaction();
		int counter = 0;
		int i = 0;

		try {

			custData = data;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString())
					.append(")");
			insert = db.compileStatement(sb.toString());

			int size = custData.size();

			do {
				for (int j = 0; j < 1000 && j < size; j++) {
					insert.bindString(index(emp_id), getData(emp_id, i)); // emp_id
					insert.bindString(index(zone_id), getData(zone_id, i)); // zone_id
					insert.bindString(index(emp_name), getData(emp_name, i)); // emp_name
					insert.bindString(index(emp_init), getData(emp_init, i)); // emp_init
					insert.bindString(index(emp_pcs), getData(emp_pcs, i)); // emp_pcs
					insert.bindString(index(emp_carrier), getData(emp_carrier, i)); // emp_carrier
					insert.bindString(index(emp_lastlogin), getData(emp_lastlogin, i)); // emp_lastlogin
					insert.bindString(index(emp_cleanup), getData(emp_cleanup, i)); // emp_cleanup
					insert.bindString(index(emp_pos), getData(emp_pos, i)); // emp_pos
					insert.bindString(index(qb_emp_id), getData(qb_emp_id, i)); // qb_emp_id
					insert.bindString(index(qb_salesrep_id), getData(qb_salesrep_id, i)); // qb_salesrep_id
					insert.bindString(index(quota_month_goal), getData(quota_month_goal, i)); // quota_month_goald
					insert.bindString(index(quota_month), getData(quota_month, i)); // quota_month
					insert.bindString(index(quota_year_goal), getData(quota_year_goal, i)); // quota_year_goal
					insert.bindString(index(quota_year), getData(quota_year, i)); // quota_year
					insert.bindString(index(emp_pwd), getData(emp_pwd, i)); // emp_pwd
					insert.bindString(index(isactive), getData(isactive, i)); // isactive
					insert.bindString(index(email), getData(email, i)); // email
					insert.bindString(index(classid), getData(classid, i)); // classid
					insert.bindString(index(tax_default), getData(tax_default, i)); // tax_default
					insert.bindString(index(pricelevel_id), getData(pricelevel_id, i)); // pricelevel_id

					insert.execute();
					insert.clearBindings();

					if (j == Global.sqlLimitTransaction) {
						db.setTransactionSuccessful();
						db.endTransaction();
						db.beginTransaction();
					}

					counter++;
					i++;
				}
				// db.setTransactionSuccessful();
			} while (counter < size);

		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.EmployeesHandler (at Class.insert)]");
			
			EasyTracker.getInstance().setContext(activity);
			Tracker myTracker = EasyTracker.getTracker(); // Get a reference to tracker.
			myTracker.sendException(sb.toString(), false); // false indicates non-fatal exception.
		} finally {
			db.setTransactionSuccessful();
			db.endTransaction();

			global.dictionary.clear();
			custData.clear();
		}
	}

	public void emptyTable(SQLiteDatabase db) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_name);
		db.execSQL(sb.toString());
	}

}