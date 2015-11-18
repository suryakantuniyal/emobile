package com.android.database;

import android.app.Activity;
import android.database.Cursor;

import com.android.support.DBManager;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ClerksHandler 
{
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
	private final String pricelevel_id = "pricelevel_id";

	private final List<String> attr = Arrays.asList(new String[] { emp_id, zone_id, emp_name, emp_init, emp_pcs, emp_carrier,
			emp_lastlogin, emp_cleanup, emp_pos, qb_emp_id, qb_salesrep_id, quota_month_goal, quota_month, quota_year_goal,
			quota_year, emp_pwd, isactive, email, classid, tax_default, pricelevel_id });

	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String,Integer>attrHash;
	private List<String[]>data;
	private List<HashMap<String,Integer>>dictionaryListMap;
	private MyPreferences myPref;
	private final String TABLE_NAME = "Clerks";
	private Activity activity;
	
	public ClerksHandler(Activity activity)
	{
		this.activity = activity;
		myPref = new MyPreferences(this.activity);
		attrHash = new HashMap<String,Integer>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		initDictionary();
	}
	
	private void initDictionary() {
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

	private int index(String tag) {
		return attrHash.get(tag);
	}
	
	
	private String getData(String tag, int record) {
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return data.get(record)[i];
		}
		return empStr;
	}
	
	public void emptyTable() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(TABLE_NAME);
		DBManager._db.execSQL(sb.toString());
	}
	
	
	public void insert(List<String[]>data, List<HashMap<String,Integer>>dictionary)
	{
		DBManager._db.beginTransaction();
		try {

			this.data = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(TABLE_NAME).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString())
					.append(")");
			insert = DBManager._db.compileStatement(sb.toString());

			int size = this.data.size();
			
			for(int i = 0 ; i < size; i++)
			{
				insert.bindString(index(emp_id), getData(emp_id, i));	//emp_id
				insert.bindString(index(zone_id), getData(zone_id, i));	//zone_id
				insert.bindString(index(emp_name), getData(emp_name, i));	//emp_name
				insert.bindString(index(emp_init), getData(emp_init, i));	//emp_init
				insert.bindString(index(emp_pcs), getData(emp_pcs, i));	//emp_pcs
				insert.bindString(index(emp_carrier), getData(emp_carrier, i));	//emp_carrier
				insert.bindString(index(emp_lastlogin), getData(emp_lastlogin, i));	//emp_lastlogin
				insert.bindString(index(emp_cleanup), getData(emp_cleanup, i));	//emp_cleanup
				insert.bindString(index(emp_pos), getData(emp_pos, i));	//emp_pos
				insert.bindString(index(qb_emp_id), getData(qb_emp_id, i));	//qb_emp_id
				insert.bindString(index(qb_salesrep_id), getData(qb_salesrep_id, i));	//qb_salesrep_id
				insert.bindString(index(quota_month_goal), getData(quota_month_goal, i));	//quota_month_goal
				insert.bindString(index(quota_month), getData(quota_month, i));	//quota_month
				insert.bindString(index(quota_year_goal), getData(quota_year_goal, i));	//quota_year_goal
				insert.bindString(index(quota_year), getData(quota_year, i));	//quota_year
				insert.bindString(index(emp_pwd), getData(emp_pwd, i));	//emp_pwd
				insert.bindString(index(isactive), getData(isactive, i));	//isactive
				insert.bindString(index(email), getData(email, i));	//email
				insert.bindString(index(classid), getData(classid, i));	//classid
				insert.bindString(index(tax_default), getData(tax_default, i));	//tax_default
				insert.bindString(index(pricelevel_id), getData(pricelevel_id, i));	//pricelevel_id
	
				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager._db.setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.ClerksHandler (at Class.insert)]");
			
//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
			DBManager._db.endTransaction();
		}
	}
	
	public String[] getClerkID(String pwd)
	{
		
		//SQLiteDatabase db = dbManager.openReadableDB();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT emp_id,emp_name FROM ").append(TABLE_NAME).append(" WHERE emp_pwd = ?");
		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[]{pwd});
		
		String[] data = null;
		
		if(c.moveToFirst())
		{
			data  = new String[2];
			if(c.getString(c.getColumnIndex(emp_id))==null)
			{
				
				data[0] = "";
				data[1] = "";
			}
			else
			{
				myPref.setClerkName(c.getString(c.getColumnIndex(emp_name)));
				data[0] = c.getString(c.getColumnIndex(emp_id));
				data[1] = c.getString(c.getColumnIndex(emp_name));
			}
		}
		
		c.close();
		//db.close();
		
		return data;
	}
	
	
	public Cursor getAllClerks()
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT emp_id AS '_id',* FROM ").append(TABLE_NAME).append(" ORDER BY emp_name");
		Cursor c = DBManager._db.rawQuery(sb.toString(), null);
		
		c.moveToFirst();
		//db.close();
		return c;
	}
	
	public String getClerkName(String _emp_id)
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT emp_name FROM ").append(TABLE_NAME).append(" WHERE emp_id = ?");
		
		Cursor c = DBManager._db.rawQuery(sb.toString(), new String[]{_emp_id});
		
		c.moveToFirst();
		//db.close();
		String clerk_name = "";
		if(c.moveToFirst())
		{
			clerk_name = c.getString(c.getColumnIndex(emp_name));
		}
		return clerk_name;
	}
}
