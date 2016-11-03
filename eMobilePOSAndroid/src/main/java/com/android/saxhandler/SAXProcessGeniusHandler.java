package com.android.saxhandler;

import android.app.Activity;

import com.android.emobilepos.models.genius.GeniusTransportToken;
import com.android.support.Global;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXProcessGeniusHandler extends DefaultHandler {
    GeniusTransportToken geniusTransportToken;
    //    private boolean isTable;
//    private boolean isAttribute = false;
//    private static List<String[]> prod_data;
//    private static List<String> list_data;
//    private static final String empStr = "";
//    private static String start_tag;
//    private HashMap<String, Integer> temp_data;
    private StringBuilder data;
    private Global global;
//    private int counter = 0;

//    public enum Limiters {
//        ASXMLCCRs;
//
//        public static Limiters toLimit(String str) {
//            try {
//                return valueOf(str);
//            } catch (Exception ex) {
//                return null;
//            }
//        }
//    }

    public GeniusTransportToken getGeniusTransportToken() {
        //temp_data.clear();
//        list_data.clear();
        return geniusTransportToken;
    }

    public SAXProcessGeniusHandler(Activity activity) {
//        this.global = (Global) activity.getApplication();
//        global.dictionary = new ArrayList<HashMap<String, Integer>>();
//        temp_data = new HashMap<String, Integer>();
//        list_data = new ArrayList<String>();
        data = new StringBuilder();
    }

    @Override
    public void startDocument() throws SAXException {
        geniusTransportToken = new GeniusTransportToken();
//        prod_data = new ArrayList<String[]>();
//        list_data = new ArrayList<String>();

    }

    @Override
    public void endDocument() throws SAXException {

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//        Limiters test = Limiters.toLimit(localName);
//
//        if (test != null) {
//            switch (test) {
//                case ASXMLCCRs: {
//                    isTable = true;
//                    temp_data = new HashMap<String, Integer>();
//                    break;
//                }
//            }
//
//        } else if (isTable) {
//            start_tag = localName;
//            isAttribute = true;
//        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (localName.equalsIgnoreCase("epayStatusCode")) {
            geniusTransportToken.setEpayStatusCode(data.toString());
        } else if (localName.equalsIgnoreCase("statusCode")) {
            geniusTransportToken.setStatusCode(data.toString());
        } else if (localName.equalsIgnoreCase("statusMessage")) {
            geniusTransportToken.setStatusMessage(data.toString());
        } else if (localName.equalsIgnoreCase("Transportkey")) {
            geniusTransportToken.setTransportkey(data.toString());
        } else if (localName.equalsIgnoreCase("ValidationKey")) {
            geniusTransportToken.setValidationKey(data.toString());
        }
//
//        Limiters test = Limiters.toLimit(localName);
//        if (test != null) {
//            switch (test) {
//                case ASXMLCCRs: {
//                    counter = 0;
//                    global.dictionary.add(temp_data);
//                    // temp_data.clear();
//                    isTable = false;
//                    isAttribute = false;
//                    prod_data.add(list_data.toArray(new String[list_data.size()]));
//                    list_data = new ArrayList<String>();
//                    break;
//                }
//            }
//        } else if (isTable) {
//            temp_data.put(start_tag, counter);
//            list_data.add(data.toString());
//
//            counter++;
//            int size = list_data.size();
//
//            if (size != counter) {
//                temp_data.put(localName, counter - 1);
//                list_data.add(empStr);
//            }
//            isAttribute = false;

        data = new StringBuilder();
//    }

    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String tag = new String(ch, start, length);
        //tag = tag.trim();
//        if (isTable && isAttribute) {
        data.append(tag);
//        }

    }

	
	/*

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private boolean isTransportKey, isValidationKey, isStatusCode, isStatusMsg, isSuccess;

	private String[] post_data;
	private String[] data = new String[5];

	private StringBuilder sb;

	public String[] getEmpData() {
		return post_data;
	}

	public SAXProcessGeniusHandler() {
		sb = new StringBuilder();
	}

	@Override
	public void startDocument() throws SAXException {
		post_data = new String[] {};
	}

	@Override
	public void endDocument() throws SAXException {

	}

	public enum Limiters {
		TransportKey, ValidationKey, statusCode, statusMessage,success;
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
		if (myLimiter != null) {
			switch (myLimiter) {
			case statusCode:
				isStatusCode = true;
				break;
			case statusMessage:
				isStatusMsg = true;
				break;
			case TransportKey:
				isTransportKey = true;
				break;
			case ValidationKey:
				isValidationKey = true;
				break;
			case success:
				isSuccess = true;
				break;
			}
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		Limiters myLimiter = Limiters.toLimit(localName);
		if (myLimiter != null) {
			switch (myLimiter) {
			case statusCode:
				isStatusCode = false;
				data[0] = sb.toString();
				sb = new StringBuilder();
				
				break;
			case statusMessage:
				isStatusMsg = false;
				data[1] = sb.toString();
				sb = new StringBuilder();
				
				break;
			case TransportKey:
				isTransportKey = false;
				data[2] = sb.toString();
				sb = new StringBuilder();
				
				break;
			case ValidationKey:
				isValidationKey = false;
				data[3] = sb.toString();
				sb = new StringBuilder();

				
				break;
			case success:
				isSuccess = false;
				data[4] = sb.toString();
				sb = new StringBuilder();
				
				post_data = data;
				data = new String[5];
				break;
			}
		}

	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String chars = new String(ch, start, length);
		chars = chars.trim();

		if (isStatusCode || isStatusMsg || isTransportKey || isValidationKey || isSuccess)
			sb.append(chars);

	}*/
}
