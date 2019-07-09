package com.android.saxhandler;

import com.android.emobilepos.models.response.ProcessCardResponse;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SAXProcessCardPayHandler extends DefaultHandler {
    private boolean isTable;
    private boolean isAttribute = false;
    private String start_tag;
    private HashMap<String, String> tempMap = new HashMap<>();
    private StringBuilder data;
    private ProcessCardResponse processCardResponse;
    private List<ProcessCardResponse> processCardResponses;

    public List<ProcessCardResponse> getProcessCardResponses() {
        return processCardResponses;
    }

    public ProcessCardResponse getProcessCardResponse() {
        return processCardResponse;
    }

    public enum Limiters {
        ASXMLCCRs, root;

        public static Limiters toLimit(String str) {
            try {
                return valueOf(str);
            } catch (Exception ex) {
                return null;
            }
        }
    }


    public SAXProcessCardPayHandler() {
        data = new StringBuilder();
    }


    public HashMap<String, String> getData() {
        return tempMap;
    }

    @Override
    public void startDocument() throws SAXException {
        processCardResponses=new ArrayList<>();
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
                    tempMap = new HashMap<>();
                    processCardResponse = new ProcessCardResponse();
                    processCardResponses.add(getProcessCardResponse());
                    break;

            }
        } else if (isTable) {
            start_tag = localName;
            isAttribute = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (localName) {
            case "epayStatusCode":
                getProcessCardResponse().setEpayStatusCode(data.toString());
                break;
            case "statusCode":
                getProcessCardResponse().setStatusCode(data.toString());
                break;
            case "statusMessage":
                getProcessCardResponse().setStatusMessage(data.toString());
                break;
            case "CreditCardTransID":
                getProcessCardResponse().setCreditCardTransID(data.toString());
                break;
            case "AuthorizationCode":
                getProcessCardResponse().setAuthorizationCode(data.toString());
                break;
            case "pay_receipt":
                getProcessCardResponse().setPay_receipt(data.toString());
                break;
            case "pay_refnum":
                getProcessCardResponse().setPay_refnum(data.toString());
                break;
            case "pay_maccount":
                getProcessCardResponse().setPay_maccount(data.toString());
                break;
            case "pay_groupcode":
                getProcessCardResponse().setPay_groupcode(data.toString());
                break;
            case "pay_stamp":
                getProcessCardResponse().setPay_stamp(data.toString());
                break;
            case "pay_resultcode":
                getProcessCardResponse().setPay_resultcode(data.toString());
                break;
            case "pay_resultmessage":
                getProcessCardResponse().setPay_resultmessage(data.toString());
                break;
            case "pay_expdate":
                getProcessCardResponse().setPay_expdate(data.toString());
                break;
            case "recordnumber":
                getProcessCardResponse().setRecordnumber(data.toString());
                break;
            case "AVSZip":
                getProcessCardResponse().setAvsZip(data.toString());
                break;
            case "CardSecurityCodeMatch":
                getProcessCardResponse().setCardSecurityCodeMatch(data.toString());
                break;
            case "CardBalance":
                getProcessCardResponse().setCardBalance(data.toString());
                break;
            case "AuthorizedAmount":
                getProcessCardResponse().setAuthorizedAmount(data.toString());
                break;
            case "CardType":
                getProcessCardResponse().setCardType(data.toString());
                break;
            case "pay_result":
                getProcessCardResponse().setPay_result(data.toString());
                break;
            case "StadisTenderID":
                getProcessCardResponse().setStadisTenderId(data.toString());
                break;
            case "WorkingKey":
                getProcessCardResponse().setWorkingKey(data.toString());
                break;
            case "Secret":
                getProcessCardResponse().setSecret(data.toString());
                break;


        }
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
        if (isTable && isAttribute) {
            data.append(tag);
        }
    }
}
