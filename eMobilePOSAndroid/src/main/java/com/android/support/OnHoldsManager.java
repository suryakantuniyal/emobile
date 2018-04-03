package com.android.support;

import android.app.Activity;

import com.android.emobilepos.service.SyncConfigServerService;
import com.android.saxhandler.SAXdownloadHandler;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by guarionex on 9/21/16.
 */
public class OnHoldsManager {

    public static String checkOnHoldStatus(String orderId, Activity activity) {
        return new Post(activity).postData(Global.S_CHECK_STATUS_ON_HOLD, orderId);
    }

    public static boolean isOnHoldAdminClaimRequired(String orderId, Activity activity) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        boolean requiredClaim = true;
        boolean timedOut = false;
        GenerateXML generateXML = new GenerateXML(activity);
        MyPreferences preferences = new MyPreferences(activity);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXdownloadHandler handler = new SAXdownloadHandler(activity);
        String xml;
        if (preferences.isUse_syncplus_services()) {
            String baseUrl = generateXML.getOnHold(Global.S_CHECK_STATUS_ON_HOLD, orderId);
//            baseUrl = activity.getString(R.string.sync_enablermobile_local_checkstatusholds) + orderId;
            String url = SyncConfigServerService.getUrl(baseUrl, activity);
            xml = oauthclient.HttpClient.getString(url, null, false);
        } else {
            xml = new Post(activity).postData(Global.S_CHECK_STATUS_ON_HOLD, orderId);
        }

        switch (xml) {
            case Global.TIME_OUT:
                timedOut = true;
                break;
            case Global.NOT_VALID_URL:
                break;
            default:
                try {
                    InputSource inSource = new InputSource(new StringReader(xml));
                    SAXParser sp = spf.newSAXParser();
                    XMLReader xr = sp.getXMLReader();
                    xr.setContentHandler(handler);
                    xr.parse(inSource);
                    List<String[]> temp = handler.getEmpData();

                    String[] returnedPost = new String[0];
                    if (temp != null && temp.size() > 0) {

                        returnedPost = handler.getEmpData().get(0);
                    }
                    if (returnedPost != null && returnedPost.length > 0 && returnedPost[1].equals("0")) {
                        requiredClaim = false;
                    }
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        return requiredClaim;
    }

    public static String updateStatusOnHold(String orderId, Activity activity) throws Exception {
        MyPreferences preferences = new MyPreferences(activity);
        GenerateXML generateXML = new GenerateXML(activity);
        String xml;
        if (preferences.isUse_syncplus_services()) {
            String baseUrl = generateXML.getOnHold(Global.S_UPDATE_STATUS_ON_HOLD, orderId);
//            baseUrl = activity.getString(R.string.sync_enablermobile_local_checkstatusholds) + orderId;
            String url = SyncConfigServerService.getUrl(baseUrl, activity);
            xml = oauthclient.HttpClient.put(url, null, null, false);
        } else {
            xml = new Post(activity).postData(Global.S_UPDATE_STATUS_ON_HOLD, orderId);
        }
        return xml;
    }

    public static String checkoutOnHold(String orderId, Activity activity) throws Exception {
        MyPreferences preferences = new MyPreferences(activity);
        GenerateXML generateXML = new GenerateXML(activity);
        String xml;
        if (preferences.isUse_syncplus_services()) {
            String baseUrl = generateXML.getOnHold(Global.S_CHECKOUT_ON_HOLD, orderId);
            String url = SyncConfigServerService.getUrl(baseUrl, activity);
            xml = oauthclient.HttpClient.delete(url, null, false);
        } else {
            xml = new Post(activity).postData(Global.S_CHECKOUT_ON_HOLD, orderId);
        }
        return xml;
    }

    public static void synchOrdersOnHoldDetails(Activity activity, String orderId) throws IOException, SAXException {
//        SynchMethods.synchOrdersOnHoldDetails(activity, orderId);
    }

}
