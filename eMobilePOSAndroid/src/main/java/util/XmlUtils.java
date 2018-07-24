package util;

import com.android.emobilepos.models.xml.EMSPayment;
import com.crashlytics.android.Crashlytics;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

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

    public static String replaceAction(String xml, String newAction) {
        return xml.replaceAll("<action>.*?</action>",
                "<action>" + newAction + "</action>");
    }
}