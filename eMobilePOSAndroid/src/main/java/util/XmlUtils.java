package util;

import com.android.emobilepos.models.ingenico.CredentialsResponse;
import com.android.emobilepos.models.pax.SoundPaymentsResponse;
import com.android.emobilepos.models.xml.EMSPayment;
import com.crashlytics.android.Crashlytics;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Luis Camayd on 7/24/2018.
 */
public class XmlUtils {

    public static EMSPayment getEMSPayment(String xml) {
        EMSPayment emsPayment = new EMSPayment();
        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xmlFactoryObject.newPullParser();
            parser.setInput(new StringReader(xml));
            int event = parser.getEventType();
            String tag = "";
            boolean found = false;
            while (event != XmlPullParser.END_DOCUMENT && !found) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        if (tag != null) {
                            if (tag.equalsIgnoreCase("app_id")) {
                                emsPayment.setAppId(parser.getText());
                            } else if (tag.equalsIgnoreCase("action")) {
                                emsPayment.setAction(Integer.parseInt(parser.getText()));
                            } else if (tag.equalsIgnoreCase("JobID")) {
                                emsPayment.setJobId(parser.getText());
                                found = true;
                            }
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            Crashlytics.logException(e);
        } catch (UnsupportedEncodingException e) {
            Crashlytics.logException(e);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

        return emsPayment;
    }

    public static SoundPaymentsResponse getSoundPaymentsResponse(String xml) {
        SoundPaymentsResponse soundPaymentsResponse = new SoundPaymentsResponse();
        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xmlFactoryObject.newPullParser();
            parser.setInput(new StringReader(xml));
            int event = parser.getEventType();
            String name;
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name != null) {
                            if (name.equalsIgnoreCase("epayStatusCode")) {
                                soundPaymentsResponse.setEpayStatusCode(parser.nextText());
                            } else if (name.equalsIgnoreCase("statusCode")) {
                                soundPaymentsResponse.setStatusCode(parser.nextText());
                            } else if (name.equalsIgnoreCase("statusMessage")) {
                                soundPaymentsResponse.setStatusMessage(parser.nextText());
                            } else if (name.equalsIgnoreCase("CreditCardTransID")) {
                                soundPaymentsResponse.setCreditCardTransID(parser.nextText());
                            } else if (name.equalsIgnoreCase("AuthorizationCode")) {
                                soundPaymentsResponse.setAuthorizationCode(parser.nextText());
                            } else if (name.equalsIgnoreCase("pay_receipt")) {
                                soundPaymentsResponse.setPay_receipt(parser.nextText());
                            } else if (name.equalsIgnoreCase("pay_refnum")) {
                                soundPaymentsResponse.setPay_refnum(parser.nextText());
                            } else if (name.equalsIgnoreCase("pay_maccount")) {
                                soundPaymentsResponse.setPay_maccount(parser.nextText());
                            } else if (name.equalsIgnoreCase("pay_groupcode")) {
                                soundPaymentsResponse.setPay_groupcode(parser.nextText());
                            } else if (name.equalsIgnoreCase("pay_stamp")) {
                                soundPaymentsResponse.setPay_stamp(parser.nextText());
                            } else if (name.equalsIgnoreCase("pay_resultcode")) {
                                soundPaymentsResponse.setPay_resultcode(parser.nextText());
                            } else if (name.equalsIgnoreCase("pay_resultmessage")) {
                                soundPaymentsResponse.setPay_resultmessage(parser.nextText());
                            } else if (name.equalsIgnoreCase("pay_expdate")) {
                                soundPaymentsResponse.setPay_expdate(parser.nextText());
                            } else if (name.equalsIgnoreCase("pay_result")) {
                                soundPaymentsResponse.setPay_result(parser.nextText());
                            } else if (name.equalsIgnoreCase("recordnumber")) {
                                soundPaymentsResponse.setRecordnumber(parser.nextText());
                            } else if (name.equalsIgnoreCase("AVSZip")) {
                                soundPaymentsResponse.setAVSZip(parser.nextText());
                            } else if (name.equalsIgnoreCase("CardSecurityCodeMatch")) {
                                soundPaymentsResponse.setCardSecurityCodeMatch(parser.nextText());
                            } else if (name.equalsIgnoreCase("CardBalance")) {
                                soundPaymentsResponse.setCardBalance(parser.nextText());
                            } else if (name.equalsIgnoreCase("AuthorizedAmount")) {
                                soundPaymentsResponse.setAuthorizedAmount(parser.nextText());
                            } else if (name.equalsIgnoreCase("CardType")) {
                                soundPaymentsResponse.setCardType(parser.nextText());
                            } else if (name.equalsIgnoreCase("CCLast4")) {
                                soundPaymentsResponse.setCCLast4(parser.nextText());
                            } else if (name.equalsIgnoreCase("CCexpDate")) {
                                soundPaymentsResponse.setCCexpDate(parser.nextText());
                            } else if (name.equalsIgnoreCase("CCName")) {
                                soundPaymentsResponse.setCCName(parser.nextText());
                            } else if (name.equalsIgnoreCase("AID")) {
                                soundPaymentsResponse.setAID(parser.nextText());
                            } else if (name.equalsIgnoreCase("APPLAB")) {
                                soundPaymentsResponse.setAPPLAB(parser.nextText());
                            } else if (name.equalsIgnoreCase("ATC")) {
                                soundPaymentsResponse.setATC(parser.nextText());
                            } else if (name.equalsIgnoreCase("CVM")) {
                                soundPaymentsResponse.setCVM(parser.nextText());
                            } else if (name.equalsIgnoreCase("CVMMSG")) {
                                soundPaymentsResponse.setCVMMSG(parser.nextText());
                            } else if (name.equalsIgnoreCase("IAD")) {
                                soundPaymentsResponse.setIAD(parser.nextText());
                            } else if (name.equalsIgnoreCase("AC")) {
                                soundPaymentsResponse.setAC(parser.nextText());
                            } else if (name.equalsIgnoreCase("TVR")) {
                                soundPaymentsResponse.setTVR(parser.nextText());
                            } else if (name.equalsIgnoreCase("EntryMode")) {
                                soundPaymentsResponse.setEntryMode(parser.nextText());
                            } else if (name.equalsIgnoreCase("EntryModeMsg")) {
                                soundPaymentsResponse.setEntryModeMsg(parser.nextText());
                            }
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            Crashlytics.logException(e);
        } catch (UnsupportedEncodingException e) {
            Crashlytics.logException(e);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

        return soundPaymentsResponse;
    }

    public static CredentialsResponse getCredentialsResponse(String xml) {
        CredentialsResponse credentialsResponse = new CredentialsResponse();
        credentialsResponse.setApiKey("");
        credentialsResponse.setUrl("");
        credentialsResponse.setUsername("");
        credentialsResponse.setPassword("");
        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xmlFactoryObject.newPullParser();
            parser.setInput(new StringReader(xml));
            int event = parser.getEventType();
            String name;
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name != null) {
                            if (name.equalsIgnoreCase("APIKey")) {
                                credentialsResponse.setApiKey(parser.nextText());
                            } else if (name.equalsIgnoreCase("URL")) {
                                credentialsResponse.setUrl(parser.nextText());
                            } else if (name.equalsIgnoreCase("Username")) {
                                credentialsResponse.setUsername(parser.nextText());
                            } else if (name.equalsIgnoreCase("Password")) {
                                credentialsResponse.setPassword(parser.nextText());
                            }
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            Crashlytics.logException(e);
        } catch (UnsupportedEncodingException e) {
            Crashlytics.logException(e);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

        return credentialsResponse;
    }

    public static String replaceAction(String xml, String newAction) {
        return xml.replaceAll("<action>.*?</action>",
                "<action>" + newAction + "</action>");
    }

    public static String findXMl(String data, String node) {
        String extData = "<root>" + data + "</root>";
        ByteArrayInputStream input;
        input = new ByteArrayInputStream(extData.getBytes());

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(input);

            Element rootElement = document.getDocumentElement();
            NodeList items = rootElement.getChildNodes();
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                if (item.getNodeName().equals(node))
                    return item.getFirstChild().getNodeValue();
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return "";
    }
}