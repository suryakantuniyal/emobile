package com.android.support;

import android.app.Activity;

import com.android.emobilepos.models.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.Bidi;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Guarionex on 6/6/2016.
 */
public class PaymentTask {

    public static boolean processRewardPayment(Activity activity, BigDecimal chargeAmount, CreditCardInfo cardInfoManager, Payment rewardPayment) {
        Post httpClient = new Post();

        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);

        boolean wasProcessed = false;
        try {
            EMSPayGate_Default payGate = new EMSPayGate_Default(activity, rewardPayment);
            String reqChargeLoyaltyReward = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeRewardAction, cardInfoManager.getWasSwiped(), cardInfoManager.getCardType(),
                    cardInfoManager);
            String xml = httpClient.postData(13, activity,reqChargeLoyaltyReward);
            Global.generateDebugFile(String.valueOf(chargeAmount));
            String errorMsg;
            if (xml.equals(Global.TIME_OUT)) {
                errorMsg = "TIME OUT, would you like to try again?";
            } else if (xml.equals(Global.NOT_VALID_URL)) {
                errorMsg = "Loyalty could not be processed....";
            } else {
                InputSource inSource = new InputSource(new StringReader(xml));

                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();
                xr.setContentHandler(handler);
                xr.parse(inSource);
                HashMap<String, String> parsedMap = handler.getData();

                if (parsedMap != null && parsedMap.size() > 0
                        && parsedMap.get("epayStatusCode").equals("APPROVED")) {
                    wasProcessed = true;
                } else if (parsedMap != null && parsedMap.size() > 0) {
                    errorMsg = "statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage");
                } else
                    errorMsg = xml;
            }

        } catch (Exception e) {

        }
        return wasProcessed;
    }


}
