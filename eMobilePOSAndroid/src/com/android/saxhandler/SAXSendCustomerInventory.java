package com.android.saxhandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class SAXSendCustomerInventory extends DefaultHandler {

	private boolean isStatus, isCustID,isProdID;

	private static List<String[]> post_data;
	private String[] data = new String[3];
	private StringBuilder sb;

	public List<String[]> getData() {
		return post_data;
	}

	public SAXSendCustomerInventory() {
		sb = new StringBuilder();
	}
	
	public enum Limiters {
		status,cust_id,prod_id;
		public static Limiters toLimit(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return null;
			}
		}
	}
	

	@Override
	public void startDocument() throws SAXException {
		post_data = new ArrayList<String[]>();
	}

	@Override
	public void endDocument() throws SAXException {

	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (localName.equals("status"))
			isStatus = true;
		else if (localName.equals("cust_id"))
			isCustID = true;
		else if(localName.equals("prod_id"))
			isProdID = true;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (localName.equals("status")) {
			isStatus = false;
			data[0] = sb.toString();
			
			sb = new StringBuilder();
		} else if (localName.equals("cust_id")) {
			isCustID = false;
			data[1] = sb.toString();
			
			sb = new StringBuilder();

		}
		else if(localName.equals("prod_id"))
		{
			isProdID = false;
			data[2] = sb.toString();

			post_data.add(data);

			sb = new StringBuilder();
			data = new String[3];
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String chars = new String(ch, start, length);
		//chars = chars.trim();

		if (isStatus||isCustID||isProdID)
			sb.append(chars);

	}
}
