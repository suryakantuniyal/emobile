package com.android.saxhandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXSendConsignmentTransaction extends DefaultHandler {

	private boolean isStatus, isConsTransID;

	private static List<String[]> post_data;
	private String[] data = new String[2];
	// private static List<String> list_data;

	// private HashMap<String,Integer> temp_data;
	private StringBuilder sb;

	public List<String[]> getData() {
		return post_data;
	}

	public SAXSendConsignmentTransaction() {
		sb = new StringBuilder();
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
		else if (localName.equals("ConsTrans_ID"))
			isConsTransID = true;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (localName.equals("status")) {
			isStatus = false;
			data[0] = sb.toString();

			sb = new StringBuilder();
		} else if (localName.equals("ConsTrans_ID")) {
			isConsTransID = false;
			data[1] = sb.toString();

			post_data.add(data);

			sb = new StringBuilder();
			data = new String[2];

		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String chars = new String(ch, start, length);
		//chars = chars.trim();

		if (isStatus)
			sb.append(chars);
		else if (isConsTransID)
			sb.append(chars);

	}
}
