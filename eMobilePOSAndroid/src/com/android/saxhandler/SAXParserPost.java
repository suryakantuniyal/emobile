package com.android.saxhandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXParserPost extends DefaultHandler{
	
	private boolean isStatus, isOrderID,isShiftID,isPayID;

	private static List<String[]> post_data;
	private String[] data = new String[2];
	// private static List<String> list_data;

	// private HashMap<String,Integer> temp_data;
	private StringBuilder sb;

	public List<String[]> getData() {
		return post_data;
	}

	public SAXParserPost() {
		sb = new StringBuilder();
	}

	public enum Limiters {
		status,ord_id,shift_id,pay_id;
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
		Limiters type = Limiters.toLimit(localName);
		if(type!=null)
		{
			switch(type)
			{
			case status:
				isStatus = true;
				break;
			case ord_id:
				isOrderID = true;
				break;
			case pay_id:
				isPayID = true;
				break;
			case shift_id:
				isShiftID = true;
				break;
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		Limiters type = Limiters.toLimit(localName);
		if(type!=null)
		{
			switch(type)
			{
			case status:
				isStatus = false;
				data[0] = sb.toString();
				sb.setLength(0);
				break;
			case ord_id:
			case pay_id:
			case shift_id:
				isOrderID = false;
				isShiftID = false;
				isPayID = false;
				
				data[1] = sb.toString();
				post_data.add(data);
				sb.setLength(0);
				data = new String[2];
				
				break;
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String chars = new String(ch, start, length);
		//chars = chars.trim();

		if (isStatus||isOrderID||isShiftID||isPayID)
			sb.append(chars);

	}

}
