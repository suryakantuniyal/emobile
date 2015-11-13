package com.android.saxhandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXSendInventoryTransfer extends DefaultHandler {

	private boolean isStatus, isTransID;

	private static List<String[]> post_data;
	private String[] data = new String[2];
	// private static List<String> list_data;

	// private HashMap<String,Integer> temp_data;
	private StringBuilder sb;

	public List<String[]> getEmpData() {
		return post_data;
	}

	public SAXSendInventoryTransfer() {
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
		else if (localName.equals("trans_id"))
			isTransID = true;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (localName.equals("trans_id")) {
			isStatus = false;
			data[0] = sb.toString().trim();

			sb = new StringBuilder();
		} else if (localName.equals("status")) {
			isTransID = false;
			data[1] = sb.toString().trim();

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
		else if (isTransID)
			sb.append(chars);

	}
}
