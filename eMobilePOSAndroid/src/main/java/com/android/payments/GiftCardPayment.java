package com.android.payments;

import android.content.Context;
import android.content.res.Resources;

import com.android.emobilepos.models.response.ProcessCardResponse;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.Global;
import com.android.support.Post;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Copied from NewUI branch. LC 07-01-19.
 */

public class GiftCardPayment {

    public static List<ProcessCardResponse> getProcessCardResponse(Context context, String urlToPost) throws TimeoutException, ParserConfigurationException, SAXException, IOException {
        List<ProcessCardResponse> cardResponses;
        Post httpClient = new Post(context);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler();
        String xml = httpClient.postData(13, urlToPost);
        switch (xml) {
            case Global.TIME_OUT:
                throw new TimeoutException(xml);
            case Global.NOT_VALID_URL:
                throw new Resources.NotFoundException(xml);
            default:
                InputSource inSource = new InputSource(new StringReader(xml));
                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();
                xr.setContentHandler(handler);
                xr.parse(inSource);
                cardResponses = handler.getProcessCardResponses();
                break;
        }
        return cardResponses;
    }
}
