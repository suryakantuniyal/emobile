package com.android.saxhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;


import com.android.database.VolumePricesHandler;
import com.android.support.Global;

public class SAXSyncVolumePrices extends DefaultHandler {

	private boolean isTable;
	private boolean isAttribute = false;
	//private static List<String[]> prod_data;
	private static List<String> list_data;
	private static final String empStr = "";
	private static String start_tag;
	private HashMap<String, Integer> temp_data;
	private StringBuilder data;
	private Global global;
	private int counter = 0;
	private VolumePricesHandler volPriceHandler;
	private SQLiteDatabase db;

	public enum Limiters {
		Table;
		public static Limiters toLimit(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return null;
			}
		}
	}

	/*public List<String[]> getEmpData() {
		//temp_data.clear();
		list_data.clear();
		return prod_data;
	}*/

	public SAXSyncVolumePrices(Activity activity,SQLiteDatabase db) {
		this.global = (Global) activity.getApplication();
		this.db = db;
		global.dictionary = new ArrayList<HashMap<String, Integer>>();
		temp_data = new HashMap<String, Integer>();
		list_data = new ArrayList<String>();
		data = new StringBuilder();
		volPriceHandler = new VolumePricesHandler(activity);
		volPriceHandler.emptyTable(this.db);
	}

	@Override
	public void startDocument() throws SAXException {
		//prod_data = new ArrayList<String[]>();
		list_data = new ArrayList<String>();

	}

	@Override
	public void endDocument() throws SAXException {

	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		Limiters test = Limiters.toLimit(localName);

		if (test != null) {
			switch (test) {
			case Table: {
				isTable = true;
				temp_data = new HashMap<String, Integer>();
				break;
			}
			}

		} else if (isTable) {
			start_tag = localName;
			isAttribute = true;
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		Limiters test = Limiters.toLimit(localName);
		if (test != null) {
			switch (test) {
			case Table: {
				counter = 0;
				//global.dictionary.add(temp_data);
				isTable = false;
				isAttribute = false;
				
				
				volPriceHandler.insert(this.db, list_data, temp_data);
				
				
				
				//prod_data.add(list_data.toArray(new String[list_data.size()]));
				list_data = new ArrayList<String>();
				break;
			}
			}
		} else if (isTable) {
			temp_data.put(start_tag, counter);
			list_data.add(data.toString());

			counter++;
			int size = list_data.size();

			if (size != counter) {
				temp_data.put(localName, counter - 1);
				list_data.add(empStr);
			}
			isAttribute = false;

			data = new StringBuilder();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String tag = new String(ch, start, length);
		tag = tag.trim();
		if (isTable && isAttribute) {
			data.append(tag.trim());

			/*
			 * temp_data.put(start_tag, counter); list_data.add(tag);
			 */

		}

	}
}
