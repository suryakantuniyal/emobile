package com.android.database;

import android.content.Context;
import android.database.Cursor;

import com.android.dao.AssignEmployeeDAO;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MemoTextHandler {

	private static final String memo_id = "memo_id";
	private static final String memo_headerLine1 = "memo_headerLine1";
	private static final String memo_headerLine2 = "memo_headerLine2";
	private static final String memo_headerLine3 = "memo_headerLine3";
	private static final String memo_footerLine1 = "memo_footerLine1";
	private static final String memo_footerLine2 = "memo_footerLine2";
	private static final String memo_footerLine3 = "memo_footerLine3";
	private static final String store_name = "store_name";
	private static final String store_email = "store_email";
	private static final String isactive = "isactive";

	private static final List<String> attr = Arrays.asList(memo_id, memo_headerLine1, memo_headerLine2, memo_headerLine3,
			memo_footerLine1, memo_footerLine2, memo_footerLine3, store_name, store_email, isactive);

	private StringBuilder sb1, sb2;
	private HashMap<String, Integer> attrHash;
	private List<String[]> addrData;
	private static final String table_name = "memotext";
	private MyPreferences myPref;
	private List<HashMap<String,Integer>>dictionaryListMap;
	

	public MemoTextHandler(Context activity) {
		attrHash = new HashMap<>();
		addrData = new ArrayList<>();
		myPref = new MyPreferences(activity);
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		new DBManager(activity);
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

	private String getData(String tag, int record) {
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return addrData.get(record)[i];
		}
		return "";
	}
	
	private int index(String tag) {
		return attrHash.get(tag);
	}

	
	public void insert(List<String[]> data, List<HashMap<String, Integer>> dictionary) {
		DBManager.getDatabase().beginTransaction();

		try {

			addrData = data;
			dictionaryListMap = dictionary;
			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString()).append(")");
			insert = DBManager.getDatabase().compileStatement(sb.toString());

			int size = addrData.size();

			for (int j = 0; j < size; j++) {

				insert.bindString(index(memo_id), getData(memo_id, j)); // memo_id
				insert.bindString(index(memo_headerLine1), getData(memo_headerLine1, j)); // memo_headerLine1
				insert.bindString(index(memo_headerLine2), getData(memo_headerLine2, j)); // memo_headerLine2
				insert.bindString(index(memo_headerLine3), getData(memo_headerLine3, j)); // memo_headerLine3
				insert.bindString(index(memo_footerLine1), getData(memo_footerLine1, j)); // memo_footerLine1
				insert.bindString(index(memo_footerLine2), getData(memo_footerLine2, j)); // memo_footerLine2
				insert.bindString(index(memo_footerLine3), getData(memo_footerLine3, j)); // memo_footerLine3
				insert.bindString(index(isactive), getData(isactive, j)); // isactive
				insert.bindString(index(store_name), getData(store_name, j)); // store_name
				insert.bindString(index(store_email), getData(store_email, j)); // store_email

				insert.execute();
				insert.clearBindings();
			}
			insert.close();
			DBManager.getDatabase().setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.MemoTextHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());
		} finally {
			DBManager.getDatabase().endTransaction();
		}
	}

	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_name);
		DBManager.getDatabase().execSQL(sb.toString());
	}
	
	public String[] getHeader()
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		String [] header = new String[3];
		String query = "SELECT memo_headerLine1,memo_headerLine2,memo_headerLine3 FROM memotext";
		
		Cursor cursor = DBManager.getDatabase().rawQuery(query, null);
		
		if(cursor.moveToFirst())
		{
			do{
				header[0] = cursor.getString(cursor.getColumnIndex(memo_headerLine1));
				header[1] = cursor.getString(cursor.getColumnIndex(memo_headerLine2));
				header[2] = cursor.getString(cursor.getColumnIndex(memo_headerLine3));
			}while(cursor.moveToNext());
		}
		cursor.close();
		//db.close();
		return header;
		
	}
	
	public HashMap<String,String>getOrderInfo()
	{
		HashMap<String,String>map = new HashMap<String,String>();
		AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
		String query = "SELECT * FROM memotext";
		
		Cursor c = DBManager.getDatabase().rawQuery(query, null);
		
		if(c.moveToFirst())
		{
			do{
				map.put("MERCHANT_NAME", c.getString(c.getColumnIndex(store_name)));
				map.put("MERCHANT_EMAIL", c.getString(c.getColumnIndex(store_email)));
				map.put("header1", c.getString(c.getColumnIndex(memo_headerLine1)));
				map.put("header2", c.getString(c.getColumnIndex(memo_headerLine2)));
				map.put("header3", c.getString(c.getColumnIndex(memo_headerLine3)));
				map.put("footer1", c.getString(c.getColumnIndex(memo_footerLine1)));
				map.put("footer2", c.getString(c.getColumnIndex(memo_footerLine2)));
				map.put("footer3", c.getString(c.getColumnIndex(memo_footerLine3)));
				map.put("EMPLOYEE_NAME", assignEmployee.getEmpName());
				map.put("CLERK_NAME", myPref.getClerkName());
			}while(c.moveToNext());
		}
		c.close();
		
		return map;
	}
	
	public String[] getFooter()
	{
		//SQLiteDatabase db =dbManager.openReadableDB();
		String [] footer = new String[3];
		String query = "SELECT memo_footerLine1,memo_footerLine2,memo_footerLine3 FROM memotext";
		
		Cursor cursor = DBManager.getDatabase().rawQuery(query, null);
		
		if(cursor.moveToFirst())
		{
			do{
				footer[0] = cursor.getString(cursor.getColumnIndex(memo_footerLine1));
				footer[1] = cursor.getString(cursor.getColumnIndex(memo_footerLine2));
				footer[2] = cursor.getString(cursor.getColumnIndex(memo_footerLine3));
			}while(cursor.moveToNext());
		}
		cursor.close();
		//db.close();
		return footer;
	}
}
