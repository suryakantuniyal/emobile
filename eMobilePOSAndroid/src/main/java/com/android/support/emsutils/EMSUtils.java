package com.android.support.emsutils;

import android.app.Activity;
import android.text.TextUtils;

import com.android.emobilepos.models.EMSEpayLoginInfo;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.Global;
import com.android.support.Post;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by guarionex on 12/12/16.
 */

public class EMSUtils {
    public enum EPayStatusCode {DECLINE, APPROVED}

    public static EMSEpayLoginInfo getEmsEpayLoginInfo(Activity activity) {
        EMSEpayLoginInfo loginInfo = new EMSEpayLoginInfo();
        EMSPayGate_Default payGate = new EMSPayGate_Default(activity, null);
        String request = payGate.paymentWithAction(EMSPayGate_Default.EAction.HandpointWorkingKey, false, null,
                null);
        Post httpClient = new Post(activity);
        String xml = httpClient.postData(Global.S_SUBMIT_WORKINGKEY_REQUEST,  request);

        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler();
        InputSource inSource = new InputSource(new StringReader(xml));
        String workingKey = null;
        SAXParser sp;
        try {
            sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(handler);
            xr.parse(inSource);
            HashMap<String, String> parsedMap = handler.getData();
            if(TextUtils.isEmpty(parsedMap.get("epayStatusCode"))){
                loginInfo.setEpayStatusCode(EPayStatusCode.DECLINE);
            }else {
                loginInfo.setEpayStatusCode(EPayStatusCode.valueOf(parsedMap.get("epayStatusCode").toUpperCase()));
            }
            if (parsedMap != null && parsedMap.size() > 0
                    && parsedMap.get("epayStatusCode").equals("APPROVED")) {
                if (parsedMap.containsKey("WorkingKey")) {
                    workingKey = parsedMap.get("WorkingKey");
                } else if (parsedMap.containsKey("Secret")) {
                    workingKey = parsedMap.get("Secret");
                }
                if (parsedMap.containsKey("statusMessage")) {
                    loginInfo.setEpayStatusMessasge(parsedMap.get("statusMessage"));
                }
                if (parsedMap.containsKey("TerminalID")) {
                    loginInfo.setTerminalId(parsedMap.get("TerminalID"));
                }
                loginInfo.setSecret(workingKey);
            } else if (parsedMap != null && parsedMap.size() > 0) {
                loginInfo.setEpayStatusMessasge("statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage"));
            } else {
                loginInfo.setEpayStatusMessasge(xml);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loginInfo;
    }

}
