package com.android.saxhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;

import com.android.support.Global;

public class SaxSelectedEmpHandler extends DefaultHandler {

	private boolean isTable;
	private boolean isAttribute = false;
	private static List<String[]> empl_data;
	private static List<String> list_data;
	private static final String empStr = "";
	private static String start_tag;
	private HashMap<String, Integer> temp_data;
	Global global;
	private int counter = 0;

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

	public List<String[]> getEmpData() {
		return empl_data;
	}

	public SaxSelectedEmpHandler(Activity activity) {
		this.global = (Global) activity.getApplication();
		global.dictionary = new ArrayList<HashMap<String, Integer>>();
		temp_data = new HashMap<String, Integer>();
		list_data = new ArrayList<String>();
	}

	@Override
	public void startDocument() throws SAXException {
		empl_data = new ArrayList<String[]>();
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
				global.dictionary.add(temp_data);
				// temp_data.clear();
				isTable = false;
				isAttribute = false;
				empl_data.add(list_data.toArray(new String[list_data.size()]));
				list_data = new ArrayList<String>();
				break;
			}
			}
		} else if (isTable) {
			counter++;
			int size = list_data.size();

			if (size != counter) {
				temp_data.put(localName, counter - 1);
				list_data.add(empStr);
			}
			isAttribute = false;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String tag = new String(ch, start, length);
		//tag = tag.trim();
		if (isTable && isAttribute) {
			temp_data.put(start_tag, counter);
			list_data.add(tag);

		}

	}
	/*
	 * private static final String[] limiter = new
	 * String[]{"emp_id","emp_name","emp_lastlogin","emp_cleanup","emp_pos",
	 * "MSOrderEntry","MSCardProcessor","GatewayURL","approveCode","zone_id"};
	 * private List<String> emp_data;
	 * 
	 * private boolean [] bools = new boolean[10];
	 * 
	 * public List<String> getEmpData() { return emp_data; }
	 * 
	 * public enum Limiters { emp_id, zone_id,// emp_name, emp_lastlogin,
	 * emp_pos, MSOrderEntry, MSCardProcessor, GatewayURL, approveCode;
	 * 
	 * public static Limiters toLimit(String str) { try { return valueOf(str); }
	 * catch (Exception ex) { return null; } } }
	 * 
	 * @Override public void startDocument() throws SAXException { emp_data =
	 * new ArrayList<String>(); }
	 * 
	 * @Override public void endDocument() throws SAXException {
	 * 
	 * }
	 * 
	 * @Override public void startElement(String uri, String localName, String
	 * qName, Attributes attributes) throws SAXException { Limiters test =
	 * Limiters.toLimit(localName);
	 * 
	 * if(test!=null) { //Limiters test = Limiters.valueOf(localName);
	 * switch(test) { case emp_id: { bools[0] = true; break; } case emp_name: {
	 * bools[1] = true; break; }
	 * 
	 * case emp_lastlogin: { bools[2] = true; break; } case emp_pos: { bools[3]
	 * = true; break; } case MSOrderEntry: { bools[4] = true; break; } case
	 * MSCardProcessor: { bools[5] = true; break; } case GatewayURL: { bools[6]
	 * = true; break; } case approveCode: { bools[7] = true; break; } case
	 * zone_id: { bools[8] = true; break; } } } }
	 * 
	 * @Override public void endElement(String uri, String localName, String
	 * qName) throws SAXException {
	 * 
	 * Limiters test = Limiters.toLimit(localName);
	 * 
	 * if(test!=null) { switch(test) { case emp_id: { bools[0] = false; break; }
	 * case emp_name: { bools[1] = false; break; }
	 * 
	 * case emp_lastlogin: { bools[2] = false; break; } case emp_pos: { bools[3]
	 * = false; break; } case MSOrderEntry: { bools[4] = false; break; } case
	 * MSCardProcessor: { bools[5] = false; break; } case GatewayURL: { bools[6]
	 * = false; break; } case approveCode: { bools[7] = false; break; } case
	 * zone_id: { bools[8] = false; break; } }
	 * 
	 * } }
	 * 
	 * @Override public void characters(char[] ch, int start, int length) {
	 * String tag = new String(ch,start,length); tag = tag.trim(); for(int i =
	 * 0;i<bools.length;i++) { if(bools[i]) { emp_data.add(tag); break; } }
	 * 
	 * }
	 */
}
