package com.android.saxhandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;


public class SAXProcessCheckHandler extends DefaultHandler
{
	private boolean isepayStatusCode,isStatusCode,isStatusMsg,isCCTransID,isAuthCode,isASXMLIvuLottoNumber,isASXMLIvuLottoDrawDate,
	isMerchantNumber,isBatchNumber,isRetrievalRefNum,isTID,is_pay_receipt,is_pay_refnum,is_pay_maccount,is_pay_groupcode,is_pay_stamp,
	is_pay_resultcode,is_pay_resultmessage,is_pay_expdate,is_pay_result,is_recordnumber;
	
	private HashMap<String,String> responseMap;
	private String[] post_data;


	private StringBuilder sb;

	public String[] getEmpData() {
		return post_data;
	}
	
	public HashMap<String,String>getResponseMap()
	{
		return responseMap;
	}

	public SAXProcessCheckHandler() {
		sb = new StringBuilder();
		responseMap = new HashMap<String,String>();
	}

	@Override
	public void startDocument() throws SAXException {
		post_data = new String[]{};
	}

	@Override
	public void endDocument() throws SAXException {

	}

	
	
	public enum Limiters {
		epayStatusCode,statusCode,statusMessage,CreditCardTransID,AuthorizationCode,ASXMLIvuLottoNumber,ASXMLIvuLottoDrawDate,
		MerchantNumber,BatchNumber,RetrievalReferenceNumber,TID,
		pay_receipt,pay_refnum,pay_maccount,pay_groupcode,pay_stamp,pay_resultcode,pay_resultmessage,pay_expdate,
		pay_result,recordnumber;
		public int size() {
			return 11;
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
				isepayStatusCode = true;
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
			case ASXMLIvuLottoNumber:
				isASXMLIvuLottoNumber = true;
				break;
			case ASXMLIvuLottoDrawDate:
				isASXMLIvuLottoDrawDate = true;
				break;
			case MerchantNumber:
				isMerchantNumber = true;
				break;
			case BatchNumber:
				isBatchNumber = true;
				break;
			case RetrievalReferenceNumber:
				isRetrievalRefNum = true;
				break;
			case TID:
				isTID = true;
				break;
			case pay_receipt:
				is_pay_receipt = true;
				break;
			case pay_refnum:
				is_pay_refnum = true;
				break;
			case pay_maccount:
				is_pay_maccount = true;
				break;
			case pay_groupcode:
				is_pay_groupcode = true;
				break;
			case pay_stamp:
				is_pay_stamp = true;
				break;
			case pay_resultcode:
				is_pay_resultcode = true;
				break;
			case pay_resultmessage:
				is_pay_resultmessage = true;
				break;
			case pay_expdate:
				is_pay_expdate = true;
				break;
			case pay_result:
				is_pay_result = true;
				break;
			case recordnumber:
				is_recordnumber = true;
				break;
				
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		boolean isValid = false;
		Limiters myLimiter = Limiters.toLimit(localName);
		if(myLimiter!=null)
		{
			/*switch(myLimiter)
			{
			case epayStatusCode:
				isepayStatusCode = false;
				data[0] = sb.toString();
				responseMap.put(localName, sb.toString());
				sb = new StringBuilder();
				break;
			case statusCode:
				isStatusCode = false;
				data[1] = sb.toString();
				responseMap.put(localName, sb.toString());
				sb = new StringBuilder();
				break;
			case statusMessage:
				isStatusMsg = false;
				data[2] = sb.toString();
				responseMap.put(localName, sb.toString());
				sb = new StringBuilder();
				break;
			case CreditCardTransID:
				isCCTransID = false;
				data[3] = sb.toString();
				responseMap.put(localName, sb.toString());
				sb = new StringBuilder();
				break;
			case AuthorizationCode:
				isAuthCode = false;
				data[4] = sb.toString();
				responseMap.put(localName, sb.toString());
				sb = new StringBuilder();
				break;
			case ASXMLIvuLottoNumber:
				isASXMLIvuLottoNumber = false;
				data[5] = sb.toString();
				responseMap.put(localName, sb.toString());
				sb = new StringBuilder();
				break;
			case ASXMLIvuLottoDrawDate:
				isASXMLIvuLottoDrawDate = false;
				data[6] = sb.toString();
				responseMap.put(localName, sb.toString());
				sb = new StringBuilder();
				break;
			case MerchantNumber:
				isMerchantNumber = false;
				data[7] = sb.toString();
				responseMap.put(localName, sb.toString());
				sb = new StringBuilder();
				break;
			case BatchNumber:
				isBatchNumber = false;
				data[8] = sb.toString();
				responseMap.put(localName, sb.toString());
				sb = new StringBuilder();
				break;
			case RetrievalReferenceNumber:
				isRetrievalRefNum = false;
				data[9] = sb.toString();
				responseMap.put(localName, sb.toString());
				sb = new StringBuilder();
				break;
			case TID:
				isTID = false;
				data[10] = sb.toString();
				responseMap.put(localName, sb.toString());
				sb = new StringBuilder();
				
				post_data = data ;

				sb = new StringBuilder();
				data = new String[11];
				break;
			case pay_receipt:
				is_pay_receipt = false;
				break;
			case pay_refnum:
				is_pay_refnum = false;
				break;
			case pay_maccount:
				is_pay_maccount = false;
				break;
			case pay_groupcode:
				is_pay_groupcode = false;
				break;
			case pay_stamp:
				is_pay_stamp = false;
				break;
			case pay_resultcode:
				is_pay_resultcode = false;
				break;
			case pay_resultmessage:
				is_pay_resultmessage = false;
				break;
			case pay_expdate:
				is_pay_expdate = false;
				break;
			case pay_result:
				is_pay_result = false;
				break;
			case recordnumber:
				is_recordnumber = false;
				break;
			}*/
			
			
			switch(myLimiter)
			{
			case epayStatusCode:
				isepayStatusCode = false;
				isValid = true;
				break;
			case statusCode:
				isStatusCode = false;
				isValid = true;
				break;
			case statusMessage:
				isStatusMsg = false;
				isValid = true;
				break;
			case CreditCardTransID:
				isCCTransID = false;
				isValid = true;
				break;
			case AuthorizationCode:
				isAuthCode = false;
				isValid = true;
				break;
			case ASXMLIvuLottoNumber:
				isASXMLIvuLottoNumber = false;
				isValid = true;
				break;
			case ASXMLIvuLottoDrawDate:
				isASXMLIvuLottoDrawDate = false;
				isValid = true;
				break;
			case MerchantNumber:
				isMerchantNumber = false;
				isValid = true;
				break;
			case BatchNumber:
				isBatchNumber = false;
				isValid = true;
				break;
			case RetrievalReferenceNumber:
				isRetrievalRefNum = false;
				isValid = true;
				break;
			case TID:
				isTID = false;
				isValid = true;
				break;
			case pay_receipt:
				is_pay_receipt = false;
				isValid = true;
				break;
			case pay_refnum:
				is_pay_refnum = false;
				isValid = true;
				break;
			case pay_maccount:
				is_pay_maccount = false;
				isValid = true;
				break;
			case pay_groupcode:
				is_pay_groupcode = false;
				isValid = true;
				break;
			case pay_stamp:
				is_pay_stamp = false;
				isValid = true;
				break;
			case pay_resultcode:
				is_pay_resultcode = false;
				isValid = true;
				break;
			case pay_resultmessage:
				is_pay_resultmessage = false;
				isValid = true;
				break;
			case pay_expdate:
				is_pay_expdate = false;
				isValid = true;
				break;
			case pay_result:
				is_pay_result = false;
				isValid = true;
				break;
			case recordnumber:
				is_recordnumber = false;
				isValid = true;
				break;
			}
			if(isValid)
			{
				responseMap.put(localName, sb.toString());
				sb = new StringBuilder();
			}
			}
			
		

	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String chars = new String(ch, start, length);
		//chars = chars.trim();

		if (isepayStatusCode||isStatusCode||isStatusMsg||isCCTransID||isAuthCode||isASXMLIvuLottoNumber||isASXMLIvuLottoDrawDate||
				isMerchantNumber||isBatchNumber||isRetrievalRefNum||isTID||is_pay_receipt||is_pay_refnum||is_pay_maccount||
				is_pay_groupcode||is_pay_stamp||is_pay_resultcode||is_pay_resultmessage||is_pay_expdate||is_pay_result||is_recordnumber)
			sb.append(chars);

	}
	
}
