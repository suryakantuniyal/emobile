package com.android.saxhandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxLoginHandler extends DefaultHandler {
	private boolean _inauth, _indeviceid, _inlic, _indisabled, _inpayid;
	private static final String[] delimiter = new String[] { "Auth", "deviceID", "Lic", "Disabled", "pay_id" };
	private String data;

	public String getData() {
		return data;
	}

	@Override
	public void startDocument() throws SAXException {
		data = "";
	}

	@Override
	public void endDocument() throws SAXException {

	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (localName.equals(delimiter[0])) {
			_inauth = true;
		} else if (localName.equals(delimiter[1])) {
			_indeviceid = true;
		} else if (localName.equals(delimiter[2])) {
			_inlic = true;
		} else if (localName.equals(delimiter[3])) {
			_indisabled = true;
		} else if (localName.equals(delimiter[4])) {
			_inpayid = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals(delimiter[0])) {
			_inauth = false;
		} else if (localName.equals(delimiter[1])) {
			_indeviceid = false;
		} else if (localName.equals(delimiter[2])) {
			_inlic = false;
		} else if (localName.equals(delimiter[3])) {
			_indisabled = false;
		} else if (localName.equals(delimiter[4])) {
			_inpayid = false;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String tag = new String(ch, start, length);
		//tag = tag.trim();

		if (_inauth) {
			data = tag;
		} else if (_indeviceid) {
			data = tag;
		} else if (_inlic) {
			data = tag;
		} else if (_indisabled) {
			data = tag;
		} else if (_inpayid) {
			data = tag;
		}
	}
}
