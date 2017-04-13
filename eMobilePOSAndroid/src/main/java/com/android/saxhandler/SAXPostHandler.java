package com.android.saxhandler;

import android.app.Activity;

import com.android.support.Global;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SAXPostHandler extends DefaultHandler {

	private boolean isTable = false;
	private boolean isTimeClock = false;
	private boolean isAttribute = false;
	private static List<String> list_data;
	private static final String empStr = "";
	private static String start_tag;
	private HashMap<String, Integer> temp_data;
	private List<HashMap<String,Integer>> dictionaryListMap;
	private List<String[]>dataList;
	private int outterCounter = 0;
	private StringBuilder data;

	private int counter = 0;

	
	

	public enum Limiters {
		Table,TimeCLock;
		public static Limiters toLimit(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return null;
			}
		}
	}
	
	public String getData(String tag, int record) {
		Integer i = dictionaryListMap.get(record).get(tag);
		if (i != null) {
			return dataList.get(record)[i];
		}
		return empStr;
	}
	
	public int getSize()
	{
		int size = 0;
		if(dictionaryListMap!=null)
			size = dictionaryListMap.size();
		return size;
	}


	public SAXPostHandler() {
		temp_data = new HashMap<>();
		list_data = new ArrayList<>();
		data = new StringBuilder();
		dictionaryListMap = new ArrayList<>();
		dataList = new ArrayList<>();
	}

	@Override
	public void startDocument() throws SAXException {
		list_data = new ArrayList<String>();

	}
	
	@Override
	public void endDocument() throws SAXException {

	}


	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		Limiters test = Limiters.toLimit(localName);

		if (test != null) {
			switch (test) 
			{
			
			case Table: {
				isTable = true;
				temp_data = new HashMap<String, Integer>();
				break;
			}
			case TimeCLock:
			{
				isTimeClock = true;
				temp_data = new HashMap<String,Integer>();
				break;
			}
			}

		} 
		else if (isTable||isTimeClock) {
			start_tag = localName;
			isAttribute = true;
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		Limiters test = Limiters.toLimit(localName);
		if (test != null) {
			switch (test) {
			case Table:
				isTable = false;
			case TimeCLock:
				isTimeClock = false;
				
				
				counter = 0;
				isAttribute = false;
				outterCounter++;
				
				dictionaryListMap.add(temp_data);
				dataList.add(list_data.toArray(new String[list_data.size()]));
				
				if(outterCounter==Global.sqlLimitTransaction)
				{
					//switchCase(true);
					dictionaryListMap.clear();
					dataList.clear();
					outterCounter = 0;
				}
				
				list_data = new ArrayList<String>();
				break;
			}
		} 
		else if (isTable||isTimeClock) {
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
		//tag = tag.trim();
		if ((isTable||isTimeClock) && isAttribute) {
			data.append(tag);
		}
	}
	
}

