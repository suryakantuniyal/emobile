package com.android.database;

import android.app.Activity;
import android.database.Cursor;

import com.android.emobilepos.models.Address;
import com.android.support.MyPreferences;

import net.sqlcipher.database.SQLiteStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import util.StringUtil;

public class AddressHandler {

	private final String addr_id = "addr_id";
	private final String cust_id = "cust_id";
	private final String addr_b_str1 = "addr_b_str1";
	private final String addr_b_str2 = "addr_b_str2";
	private final String addr_b_str3 = "addr_b_str3";
	private final String addr_b_city = "addr_b_city";
	private final String addr_b_state = "addr_b_state";
	private final String addr_b_country = "addr_b_country";
	private final String addr_b_zipcode = "addr_b_zipcode";
	private final String addr_s_name = "addr_s_name";
	private final String addr_s_str1 = "addr_s_str1";
	private final String addr_s_str2 = "addr_s_str2";
	private final String addr_s_str3 = "addr_s_str3";
	private final String addr_s_city = "addr_s_city";
	private final String addr_s_state = "addr_s_state";
	private final String addr_s_country = "addr_s_country";
	private final String addr_s_zipcode = "addr_s_zipcode";
	private final String qb_cust_id = "qb_cust_id";
	private final String addr_b_type = "addr_b_type";
	private final String addr_s_type = "addr_s_type";

	private final List<String> attr = Arrays.asList(addr_id, cust_id, addr_b_str1, addr_b_str2, addr_b_str3,
			addr_b_city, addr_b_state, addr_b_country, addr_b_zipcode, addr_s_name, addr_s_str1, addr_s_str2, addr_s_str3, addr_s_city, addr_s_state,
			addr_s_country, addr_s_zipcode, qb_cust_id,addr_b_type,addr_s_type);

	private static final String table_name = "Address";
	private StringBuilder sb1, sb2;
	private final String empStr = "";
	private HashMap<String, Integer> attrHash;
	
	private List<String[]> addrData;
	private List<HashMap<String,Integer>>dictionaryListMap;
	private MyPreferences myPref;
	private Activity activity;
	
	

	public AddressHandler(Activity activity) {
		this.activity = activity;
		attrHash = new HashMap<String, Integer>();
		addrData = new ArrayList<String[]>();
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		myPref = new MyPreferences(activity);
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

	private String getData(String tag, int record) 
	{
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return addrData.get(record)[i];
		}
		return empStr;
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
				insert.bindString(index(cust_id), getData(cust_id, j)); // cust_id
				insert.bindString(index(addr_id), getData(addr_id, j)); // addr_id
				insert.bindString(index(addr_b_str1), getData(addr_b_str1, j)); // addr_b_str1
				insert.bindString(index(addr_b_str2), getData(addr_b_str2, j)); // addr_b_str2
				insert.bindString(index(addr_b_str3), getData(addr_b_str3, j)); // addr_b_str3
				insert.bindString(index(addr_b_city), getData(addr_b_city, j)); // addr_b_city
				insert.bindString(index(addr_b_state), getData(addr_b_state, j)); // addr_b_state
				insert.bindString(index(addr_b_country), getData(addr_b_country, j)); // addr_b_country
				insert.bindString(index(addr_b_zipcode), getData(addr_b_zipcode, j)); // addr_b_zipcode
				insert.bindString(index(addr_s_name), getData(addr_s_name, j)); // addr_s_name
				insert.bindString(index(addr_s_str1), getData(addr_s_str1, j)); // addr_s_str1
				insert.bindString(index(addr_s_str2), getData(addr_s_str2, j)); // addr_s_str2
				insert.bindString(index(addr_s_str3), getData(addr_s_str3, j)); // addr_s_str3
				insert.bindString(index(addr_s_city), getData(addr_s_city, j)); // addr_s_city
				insert.bindString(index(addr_s_state), getData(addr_s_state, j)); // addr_s_state
				insert.bindString(index(addr_s_country), getData(addr_s_country, j)); // addr_s_country
				insert.bindString(index(addr_s_zipcode), getData(addr_s_zipcode, j)); // addr_s_zipcode
				insert.bindString(index(qb_cust_id), getData(qb_cust_id, j)); // qb_cust_id
				insert.bindString(index(addr_b_type), getData(addr_b_type, j)); // addr_b_type
				insert.bindString(index(addr_s_type), getData(addr_s_type, j)); // addr_s_type

				insert.execute();
				insert.clearBindings();

			}
			insert.close();
			DBManager.getDatabase().setTransactionSuccessful();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage()).append(" [com.android.emobilepos.AddressHandler (at Class.insert)]");

//			Tracker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(sb.toString(), false).build());

		} finally {
			DBManager.getDatabase().endTransaction();
		}
	}
	
	
	
	public void insertOneAddress(Address address)
	{
		//SQLiteDatabase db = dbManager.openWritableDB();
		DBManager.getDatabase().beginTransaction();
		try {

			SQLiteStatement insert = null;
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ").append(table_name).append(" (").append(sb1.toString()).append(") ").append("VALUES (").append(sb2.toString())
					.append(")");
			insert = DBManager.getDatabase().compileStatement(sb.toString());

			insert.bindString(index(addr_id), address.addr_id==null?"":address.addr_id);
			insert.bindString(index(cust_id), address.cust_id==null?"":address.cust_id);
			insert.bindString(index(addr_b_str1), address.addr_b_str1==null?"":address.addr_b_str1);
			insert.bindString(index(addr_b_str2), address.addr_b_str2==null?"":address.addr_b_str2);
			insert.bindString(index(addr_b_str3), address.addr_b_str3==null?"":address.addr_b_str3);
			insert.bindString(index(addr_b_city), address.addr_b_city==null?"":address.addr_b_city);
			insert.bindString(index(addr_b_state), address.addr_b_state==null?"":address.addr_b_state);
			insert.bindString(index(addr_b_country), address.addr_b_country==null?"":address.addr_b_country);
			insert.bindString(index(addr_b_zipcode), address.addr_b_zipcode==null?"":address.addr_b_zipcode);
			insert.bindString(index(addr_s_name), address.addr_s_name==null?"":address.addr_s_name);
			insert.bindString(index(addr_s_str1), address.addr_s_str1==null?"":address.addr_s_str1);
			insert.bindString(index(addr_s_str2), address.addr_s_str2==null?"":address.addr_s_str2);
			insert.bindString(index(addr_s_str3), address.addr_s_str3==null?"":address.addr_s_str3);
			insert.bindString(index(addr_s_city), address.addr_s_city==null?"":address.addr_s_city);
			insert.bindString(index(addr_s_state), address.addr_s_state==null?"":address.addr_s_state);
			insert.bindString(index(addr_s_country), address.addr_s_country==null?"":address.addr_s_country);
			insert.bindString(index(addr_s_zipcode), address.addr_s_zipcode==null?"":address.addr_s_zipcode);
			insert.bindString(index(qb_cust_id), address.qb_cust_id==null?"":address.qb_cust_id);
			insert.bindString(index(addr_b_type), address.addr_b_type==null?"":address.addr_b_type);
			insert.bindString(index(addr_s_type), address.addr_s_type==null?"":address.addr_s_type);

			

			insert.execute();
			insert.clearBindings();
			insert.close();
			DBManager.getDatabase().setTransactionSuccessful();

		} catch (Exception e) {
			//e.printStackTrace();
//			racker tracker = EasyTracker.getInstance(activity);
//			tracker.send(MapBuilder.createException(e.getMessage(), false).build());
		} finally {
			DBManager.getDatabase().endTransaction();
		}
		//db.close();
	}
	
	
	
	
	public void emptyTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(table_name);
		DBManager.getDatabase().execSQL(sb.toString());
	}
	
	
	
	public String getLastAddressID()
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT addr_id FROM Address WHERE addr_id like '").append(myPref.getEmpID()).append("-_____-____' ORDER BY addr_id");
		Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), null);
		String lastCustID = empStr;
		if(cursor.moveToLast())
		{
			lastCustID = cursor.getString(cursor.getColumnIndex(addr_id));
		}
		
		cursor.close();
		//db.close();
		
		return lastCustID;
	}
	
	public Cursor getCursorAddress(String custID)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT addr_id,addr_b_str1,addr_b_str2,addr_b_str3,addr_b_city,addr_b_state,addr_b_country,addr_b_zipcode,");
		sb.append("addr_s_str1,addr_s_str2,addr_s_str3,addr_s_city,addr_s_state,addr_s_country,addr_s_zipcode,addr_b_type,addr_s_type FROM Address WHERE cust_id = ?");
		
		Cursor cursor = DBManager.getDatabase().rawQuery(sb.toString(), new String[]{custID});
		
		return cursor;
	}
	
	
	
	public List<String[]> getSpecificAddress(String custID,int type)
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		

		StringBuilder sb = new StringBuilder();
		List<String[]>arrayList = new ArrayList<String[]>();
		String[] arrayValues = new String[7];
		Cursor cursor = null;
		switch(type)
		{
		case 0:									//BILLING
			
			sb.append("SELECT addr_b_str1,addr_b_str2,addr_b_str3,addr_b_country,addr_b_city,addr_b_state,addr_b_zipcode FROM Address WHERE cust_id= ?");
			//sb.append(subquery1).append(custID).append("'");
			
			cursor = DBManager.getDatabase().rawQuery(sb.toString(), new String[]{custID});
			if(cursor.moveToFirst())
			{
				int addrSTR1Index = cursor.getColumnIndex(addr_b_str1);
				int addrSTR2Index = cursor.getColumnIndex(addr_b_str2);
				int addrSTR3Index = cursor.getColumnIndex(addr_b_str3);
				int addrCountryIndex = cursor.getColumnIndex(addr_b_country);
				int addrCityIndex = cursor.getColumnIndex(addr_b_city);
				int addrStateIndex = cursor.getColumnIndex(addr_b_state);
				int addrZipCodeIndex = cursor.getColumnIndex(addr_b_zipcode);
				do
				{
					arrayValues[0] = cursor.getString(addrSTR1Index);
					arrayValues[1] = cursor.getString(addrSTR2Index);
					arrayValues[2] = cursor.getString(addrSTR3Index);
					arrayValues[3] = cursor.getString(addrCountryIndex);
					arrayValues[4] = cursor.getString(addrCityIndex);
					arrayValues[5] = cursor.getString(addrStateIndex);
					arrayValues[6] = cursor.getString(addrZipCodeIndex);
					
					arrayList.add(arrayValues);
					arrayValues = new String[7];
				}while(cursor.moveToNext());
			}
			break;
		case 1:									//SHIPPING
			
			sb.append("SELECT addr_s_str1,addr_s_str2,addr_s_str3,addr_s_country,addr_s_city,addr_s_state,addr_s_zipcode FROM Address WHERE cust_id=?");
			//sb.append(subquery1).append(custID).append("'");
			cursor = DBManager.getDatabase().rawQuery(sb.toString(), new String[]{custID});
			if(cursor.moveToFirst())
			{
				int addrSTR1Index = cursor.getColumnIndex(addr_s_str1);
				int addrSTR2Index = cursor.getColumnIndex(addr_s_str2);
				int addrSTR3Index = cursor.getColumnIndex(addr_s_str3);
				int addrCountryIndex = cursor.getColumnIndex(addr_s_country);
				int addrCityIndex = cursor.getColumnIndex(addr_s_city);
				int addrStateIndex = cursor.getColumnIndex(addr_s_state);
				int addrZipCodeIndex = cursor.getColumnIndex(addr_s_zipcode);
				do
				{
					arrayValues[0] = cursor.getString(addrSTR1Index);
					arrayValues[1] = cursor.getString(addrSTR2Index);
					arrayValues[2] = cursor.getString(addrSTR3Index);
					arrayValues[3] = cursor.getString(addrCountryIndex);
					arrayValues[4] = cursor.getString(addrCityIndex);
					arrayValues[5] = cursor.getString(addrStateIndex);
					arrayValues[6] = cursor.getString(addrZipCodeIndex);
					
					arrayList.add(arrayValues);
					arrayValues = new String[7];
				}while(cursor.moveToNext());
			}
			break;
		}
		if (cursor != null) {
			cursor.close();
		}
		//db.close();
		return arrayList;
	}
	
	
	public List<String[]> getAddress()
	{
		//SQLiteDatabase db = dbManager.openReadableDB();
		
//		String subquery1 = "SELECT addr_id,addr_s_str1,addr_s_str2,addr_s_str3,addr_s_country,addr_s_city,addr_s_state,addr_s_zipcode FROM Address WHERE cust_id='";
//		String subquery2 = "' AND cust_id!='' ORDER BY addr_id";

		String sb = "SELECT addr_id,addr_s_str1,addr_s_str2,addr_s_str3,addr_s_country,addr_s_city,addr_s_state,addr_s_zipcode FROM Address WHERE cust_id = ? " +
				" AND cust_id != '' ORDER BY addr_id";

		Cursor cursor = DBManager.getDatabase().rawQuery(sb, new String[]{StringUtil.nullStringToEmpty(myPref.getCustID())});
		List<String[]> arrayList = new ArrayList<>();
		String[] arrayValues = new String[8];
		
		if(cursor.moveToFirst())
		{
			int addrIDIndex = cursor.getColumnIndex(addr_id);
			int addrSTR1Index = cursor.getColumnIndex(addr_s_str1);
			int addrSTR2Index = cursor.getColumnIndex(addr_s_str2);
			int addrSTR3Index = cursor.getColumnIndex(addr_s_str3);
			int addrCountryIndex = cursor.getColumnIndex(addr_s_country);
			int addrCityIndex = cursor.getColumnIndex(addr_s_city);
			int addrStateIndex = cursor.getColumnIndex(addr_s_state);
			int addrZipCodeIndex = cursor.getColumnIndex(addr_s_zipcode);
			do
			{
				arrayValues[0] = cursor.getString(addrIDIndex);
				arrayValues[1] = cursor.getString(addrSTR1Index);
				arrayValues[2] = cursor.getString(addrSTR2Index);
				arrayValues[3] = cursor.getString(addrSTR3Index);
				arrayValues[4] = cursor.getString(addrCountryIndex);
				arrayValues[5] = cursor.getString(addrCityIndex);
				arrayValues[6] = cursor.getString(addrStateIndex);
				arrayValues[7] = cursor.getString(addrZipCodeIndex);
				
				arrayList.add(arrayValues);
				arrayValues = new String[8];
			}while(cursor.moveToNext());
		}
		
		cursor.close();
		//db.close();
		return arrayList;
	}
}
