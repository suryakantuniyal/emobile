package com.android.emobilepos.storedforward;

import android.app.Activity;
import android.os.AsyncTask;

import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.payment.ProcessBoloro_FA;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.Global;
import com.android.support.Post;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by guarionex on 8/24/16.
 */
public class BoloroPayment {
    private static boolean isPolling = true;

    public static void stopPolling() {
        isPolling = false;
    }

    public static boolean executeNFCCheckout(Activity activity, String xml, Payment payment) throws ParserConfigurationException, SAXException, IOException {
        boolean result = true;
        isPolling = true;
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXProcessCardPayHandler myParser = new SAXProcessCardPayHandler(activity);
        Post httpClient = new Post();
        String xmlResponse = httpClient.postData(13, activity, xml);

        InputSource inSource = new InputSource(new StringReader(xmlResponse));

        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        xr.setContentHandler(myParser);
        xr.parse(inSource);
        HashMap<String, String> response = myParser.getData();

        if (response != null && !response.isEmpty() && Boolean.parseBoolean(response.get("success"))) {
            payment.setPay_transid(response.get("transaction_id"));
            executeBoloroPolling(activity, payment, isPolling);
        } else {
            result = false;
        }
        return result;
    }

    private static void executeBoloroPolling(Activity activity, Payment payment, boolean isPolling) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXProcessCardPayHandler myParser = new SAXProcessCardPayHandler(activity);
        try {
            EMSPayGate_Default payGate = new EMSPayGate_Default(activity, payment);
            String generatedURL;
            if (isPolling)//is Polling
            {
                isPolling = true;
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.BoloroPolling, false, null, null);
            } else    //is Cancel
            {
                generatedURL = payGate.paymentWithAction(EMSPayGate_Default.EAction.CancelBoloroTransaction, false, null, null);
            }
            InputSource inSource;
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            Post httpClient = new Post();
            String xml;
            boolean transCompleted = false;
            boolean failed = false;
            do {
                xml = httpClient.postData(13, activity, generatedURL);
                inSource = new InputSource(new StringReader(xml));
                xr.setContentHandler(myParser);
                xr.parse(inSource);
                HashMap<String, String> response = myParser.getData();

                if (response != null && !response.isEmpty() && Boolean.parseBoolean(response.get("success"))) {
                    if (isPolling && response.containsKey("next_action") && response.get("next_action").equals("POLL")) {
                        isPolling = true;
                        try {
                            Thread.sleep(ProcessBoloro_FA.POLLING_SLEEP_TIME);
                        } catch (InterruptedException e) {
                        }
                    } else if (response.containsKey("next_action") && response.get("next_action").equals("SUCCESS")) {

                        PaymentsHandler payHandler = new PaymentsHandler(activity);
                        payment.setProcessed("1");
                        BigDecimal bg = new BigDecimal(Global.amountPaid);
                        Global.amountPaid = bg.setScale(2, RoundingMode.HALF_UP).toString();
                        payment.setPay_dueamount(Global.amountPaid);
                        payment.setPay_amount(Global.amountPaid);
                        payHandler.insert(payment);
                        isPolling = false;
                        transCompleted = true;
                    } else if (response.containsKey("next_action") && response.get("next_action").equals("FAILED"))
                        failed = true;
                } else {
                    failed = true;
                }
            } while (!failed && isPolling && !transCompleted);
        } catch (Exception e) {
        }
    }
}
