package com.android.saxhandler;

import android.app.Activity;

import com.android.support.BoloroCarrier;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SAXBoloroManual extends DefaultHandler {

	private boolean isCarrier = false,isAccount = false;
	private boolean isAttribute = false;
	
	private List<BoloroCarrier>listBoloro;
	private BoloroCarrier tempBoloroCarrier;
	private HashMap<String, String> temp_data;
	
	private static String start_tag;
	
	private StringBuilder data;


	public enum Limiters {
		telco,telco_payment_modes;
		public static Limiters toLimit(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return null;
			}
		}
	}

	
	public List<BoloroCarrier>getData()
	{
		return listBoloro;
	}
	


	public SAXBoloroManual(Activity activity) {
		
		tempBoloroCarrier = new BoloroCarrier();
		temp_data = new HashMap<String, String>();
		listBoloro = new ArrayList<BoloroCarrier>();
		data = new StringBuilder();
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		Limiters test = Limiters.toLimit(localName);

		if (test != null) {
			switch (test) {
			case telco: 
				isCarrier = true;
				isAccount = false;
				temp_data = new HashMap<String, String>();
				break;
			case telco_payment_modes:
				isAccount = true;
				break;
			}

		} else if (isCarrier) {
			start_tag = localName;
			isAttribute = true;
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		Limiters test = Limiters.toLimit(localName);
		if (test != null) {
			switch (test) {
			case telco:
				//counter = 0;
				isCarrier = false;
				isAttribute = false;
				listBoloro.add(tempBoloroCarrier);
				
				tempBoloroCarrier = new BoloroCarrier();
				
				break;
			case telco_payment_modes:
				tempBoloroCarrier.addCarrierAccounts(temp_data);
				temp_data = new HashMap<String,String>();
				break;
			}
		} else if (isCarrier&&!isAccount) {
			if(start_tag.equals("telco_id"))
				tempBoloroCarrier.setTelcoID(data.toString());
			else if(start_tag.equals("telco_name"))
				tempBoloroCarrier.setTelcoName(data.toString());
			
			isAttribute = false;
			data = new StringBuilder();
		}else if(isCarrier&&isAccount)
		{
			temp_data.put(start_tag, data.toString());
			isAttribute = false;
			data = new StringBuilder();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String tag = new String(ch, start, length);
		//tag = tag.trim();
		if (isCarrier && isAttribute) {
			data.append(tag);
		}

	}
}