package com.android.saxhandler;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;


//import com.android.saxhandler.SaxAddressHandler.Limiters;

public class SAXProcessCardPayHandler extends DefaultHandler
{
	/*private boolean isEpayStatusCode,isStatusCode,isStatusMsg,isCCTransID,isAuthCode,isMerchNum;
	
	private String[] post_data;
	private String[] data = new String[6];

	private StringBuilder sb;

	public String[] getEmpData() {
		return post_data;
	}

	public SAXProcessCardPayHandler() {
		sb = new StringBuilder();
	}

	@Override
	public void startDocument() throws SAXException {
		post_data = new String[]{};
	}

	@Override
	public void endDocument() throws SAXException {

	}

	
	
	public enum Limiters {
		epayStatusCode,statusCode,statusMessage,CreditCardTransID,AuthorizationCode,
		MerchantNumber;
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
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		Limiters myLimiter = Limiters.toLimit(localName);
		if(myLimiter!=null)
		{
			switch(myLimiter)
			{
			case epayStatusCode:
				isEpayStatusCode = true;
				break;
			case statusCode:
				isStatusCode = true;
				break;
			case statusMessage:
				isStatusMsg = true;
				break;
			case CreditCardTransID:
				isCCTransID = true;
				break;
			case AuthorizationCode:
				isAuthCode = true;
				break;
			case MerchantNumber:
				isMerchNum = true;
				break;
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		Limiters myLimiter = Limiters.toLimit(localName);
		if(myLimiter!=null)
		{
			switch(myLimiter)
			{
			case epayStatusCode:
				isEpayStatusCode = false;
				data[0] = sb.toString();
				
				sb = new StringBuilder();
				break;
			case statusCode:
				isStatusCode = false;
				data[1] = sb.toString();
				
				sb = new StringBuilder();
				break;
			case statusMessage:
				isStatusMsg = false;
				data[2] = sb.toString();
				
				sb = new StringBuilder();
				break;
			case CreditCardTransID:
				isCCTransID = false;
				data[3] = sb.toString();
				
				sb = new StringBuilder();
				break;
			case AuthorizationCode:
				isAuthCode = false;
				data[4] = sb.toString();
				
				sb = new StringBuilder();
				
				break;
				
			case MerchantNumber:
				isMerchNum = false;
				data[5] = sb.toString();
				
				sb = new StringBuilder();
				post_data = data;
				data = new String[6];
				break;
			}
		}

	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String chars = new String(ch, start, length);
		chars = chars.trim();

		if (isEpayStatusCode||isStatusCode||isStatusMsg||isCCTransID||isAuthCode||isMerchNum)
			sb.append(chars);

	}*/
	
	private Activity activity;
	private boolean isTable;
	private boolean isAttribute = false;
	
	private final String empStr = "";
	private String start_tag;
	
	
	//private HashMap<String,String>mapList = new ArrayList<HashMap<String,String>>();
	private HashMap<String,String>tempMap = new HashMap<String,String>();
	
	private StringBuilder data;

	
	private SQLiteDatabase db;
	
	
	
	public enum Limiters {
		ASXMLCCRs,root;
		public static Limiters toLimit(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return null;
			}
		}
	}


	public SAXProcessCardPayHandler(Activity activity) {
		
		this.activity = activity;
		
		
		data = new StringBuilder();
	}

	
	public HashMap<String,String> getData()
	{
		return tempMap;
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
			case ASXMLCCRs:
			case root:
				isTable = true;
				tempMap = new HashMap<String, String>();
				break;
			
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
			case ASXMLCCRs:
			case root:
				isTable = false;
				isAttribute = false;
				tempMap.put(localName, data.toString());
				break;
			
			}
		} else if (isTable) {
			
			tempMap.put(start_tag, data.toString());
			isAttribute = false;

			data = new StringBuilder();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String tag = new String(ch, start, length);
		//tag = tag.trim();
		if (isTable && isAttribute) {
			data.append(tag);
		}
	}
	
	
}
