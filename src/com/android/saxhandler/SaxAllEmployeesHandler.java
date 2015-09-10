package com.android.saxhandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.android.saxhandler.SAXdownloadHandler.Limiters;

public class SaxAllEmployeesHandler extends DefaultHandler {
	private boolean _inname, _inempid;
	private List<String> emp_name;
	private List<String> emp_id;
	private StringBuilder sb;

	public List<String> getEmpName() {
		return emp_name;
	}

	public List<String> getEmpId() {
		return emp_id;
	}
	
	public enum Limiters {
		empid,emp_name;
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

	@Override
	public void startDocument() throws SAXException {
		emp_name = new ArrayList<String>();
		emp_id = new ArrayList<String>();
		sb = new StringBuilder();
	}

	@Override
	public void endDocument() throws SAXException {

	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		/*if (localName.equals(limiter[0])) {
			_inname = true;
		} else if (localName.equals(limiter[1])) {
			_inempid = true;
		}*/
		
		Limiters myLimiter = Limiters.toLimit(localName);
		if(myLimiter!=null)
		{
			switch(myLimiter)
			{
			case empid:
				_inempid = true;
				break;
			case emp_name:
				_inname = true;
				break;
			
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		/*if (localName.equals(limiter[0])) {
			_inname = false;
		} else if (localName.equals(limiter[1])) {
			_inempid = false;
		}*/
		Limiters myLimiter = Limiters.toLimit(localName);
		if(myLimiter!=null)
		{
			switch(myLimiter)
			{
			case empid:
				_inempid = false;
				 emp_id.add(sb.toString());
				
				sb = new StringBuilder();
				break;
			case emp_name:
				_inname = false;
				emp_name.add(sb.toString());
				
				sb = new StringBuilder();
				break;
			}
		}

	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String tag = new String(ch, start, length);
		//tag = tag.trim();
		if (_inname||_inempid)
			sb.append(tag);
	}
}