package com.android.support;

import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class Parse {
	public boolean validate(String xml, int i) throws XmlPullParserException, IOException {
		boolean response = false;
		String[] limiter = new String[] { "Auth", "deviceID", "Disabled" };
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(new StringReader(xml));
		int eventType = xpp.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String name = xpp.getName();
				if (name.equals(limiter[i])) {
					response = Boolean.parseBoolean(xpp.nextText().toLowerCase());
					// response = xpp.nextText();
					return response;
				}
			}
			eventType = xpp.next();
		}
		return response;
	}

}
