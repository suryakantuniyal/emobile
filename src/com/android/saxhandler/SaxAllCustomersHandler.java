package com.android.saxhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;

import com.android.support.Global;

public class SaxAllCustomersHandler extends DefaultHandler {
	private boolean isTable;
	private boolean isAttribute = false;
	private boolean isEndTag = true;
	private static List<String[]> cust_data;
	private static List<String> list_data;
	private static final String empStr = "";
	private static String start_tag;
	StringBuilder data;
	private HashMap<String, Integer> temp_data;
	Global global;
	private int counter = 0;

	public enum Limiters {
		Table;
		public int size() {
			return 16;
		}

		public static Limiters toLimit(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return null;
			}
		}
	}

	public List<String[]> getEmpData() {
		return cust_data;
	}

	public SaxAllCustomersHandler(Activity activity) {
		this.global = (Global) activity.getApplication();
		global.dictionary = new ArrayList<HashMap<String, Integer>>();
		temp_data = new HashMap<String, Integer>();
		list_data = new ArrayList<String>();

		data = new StringBuilder();
	}

	@Override
	public void startDocument() throws SAXException {
		cust_data = new ArrayList<String[]>();
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
			/*
			 * case custidkey: { bools[0] = true; break; } case cust_id: {
			 * bools[1] = true; break; }
			 * 
			 * case qb_sync: { bools[2] = true; break; } case zone_id: {
			 * bools[3] = true; break; } case CompanyName: { bools[4] = true;
			 * break; } case cust_name: { bools[5] = true; break; } case
			 * cust_balance: { bools[6] = true; break; } case cust_limit: {
			 * bools[7] = true; break; } case cust_contact: { bools[8] = true;
			 * break; } case cust_phone: { bools[9] = true; break; } case
			 * cust_email: { bools[10] = true; break; } case cust_update: {
			 * bools[11] = true; break; } case isactive: { bools[12] = true;
			 * break; } case cust_taxable: { bools[13] = true; break; } case
			 * cust_salestaxcode: { bools[14] = true; break; } case
			 * pricelevel_id: { bools[15] = true; break; }
			 */
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
				if (cust_data.size() == 5) {
					String k = "hello";
				}
				counter = 0;
				global.dictionary.add(temp_data);
				isTable = false;
				isAttribute = false;
				cust_data.add(list_data.toArray(new String[list_data.size()]));
				list_data = new ArrayList<String>();
				break;
			}
			}

			/*
			 * switch(test) { case custidkey: { bools[0] = false; break; } case
			 * cust_id: { bools[1] = false; break; }
			 * 
			 * case qb_sync: { bools[2] = false; break; } case zone_id: {
			 * bools[3] = false; break; } case CompanyName: { bools[4] = false;
			 * break; } case cust_name: { bools[5] = false; break; } case
			 * cust_balance: { bools[6] = false; break; } case cust_limit: {
			 * bools[7] = false; break; } case cust_contact: { bools[8] = false;
			 * break; } case cust_phone: { bools[9] = false; break; } case
			 * cust_email: { bools[10] = false; break; } case cust_update: {
			 * bools[11] = false; break; } case isactive: { bools[12] = false;
			 * break; } case cust_taxable: { bools[13] = false; break; } case
			 * cust_salestaxcode: { bools[14] = false; break; } case
			 * pricelevel_id: { bools[15] = false;
			 * 
			 * break; }
			 */

		} else if (isTable) {

			temp_data.put(start_tag, counter);
			list_data.add(data.toString());

			counter++;
			int size = list_data.size();

			if (size != counter) {
				temp_data.put(localName, counter - 1);
				// global.dictionary.add(temp_data);
				list_data.add(empStr);
			}
			isAttribute = false;
			data = new StringBuilder();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {

		String tag = new String(ch, start, length);

		if (isTable && isAttribute) {
			// global.dictionary.put(start_tag,counter);
			data.append(tag.trim());

			/*
			 * temp_data.put(start_tag, counter);
			 * list_data.add(data.toString());
			 */

		}

	}
}
