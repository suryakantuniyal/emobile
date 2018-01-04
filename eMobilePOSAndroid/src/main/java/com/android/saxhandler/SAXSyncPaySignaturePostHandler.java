package com.android.saxhandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class SAXSyncPaySignaturePostHandler extends DefaultHandler {

    private static List<Response> post_data;
    private boolean isStatus, isTransId;
    private String[] data = new String[2];

    private StringBuilder sb;

    public SAXSyncPaySignaturePostHandler() {
        sb = new StringBuilder();
    }

    public List<Response> getResposeData() {
        return post_data;
    }

    @Override
    public void startDocument() throws SAXException {
        post_data = new ArrayList<>();
    }

    @Override
    public void endDocument() throws SAXException {

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equals("status"))
            isStatus = true;
        else if (localName.equals("pay_transid"))
            isTransId = true;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals("status")) {
            isStatus = false;
            data[0] = sb.toString();
            sb = new StringBuilder();
        } else if (localName.equals("pay_transid")) {
            isTransId = false;
            data[1] = sb.toString();
            post_data.add(getResponse());
            sb = new StringBuilder();
            data = new String[2];
        }
    }

    private Response getResponse() {
        Response Response = new Response();
        Response.setStatus(data[0]);
        Response.setTransId(data[1]);
        return Response;
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String chars = new String(ch, start, length);
        if (isStatus)
            sb.append(chars);
        else if (isTransId)
            sb.append(chars);

    }

    public class Response {
        private String status;
        private String transId;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTransId() {
            return transId;
        }

        public void setTransId(String transId) {
            this.transId = transId;
        }
    }
}