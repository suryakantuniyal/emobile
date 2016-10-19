package com.android.support;

import android.app.Activity;

import com.android.emobilepos.models.realms.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Guarionex on 6/6/2016.
 */
public class PaymentTask {

    public static Response processRewardPayment(Activity activity, BigDecimal chargeAmount, CreditCardInfo cardInfoManager, Payment rewardPayment) {
        Post httpClient = new Post();

        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);
        Response response = new Response();
        try {
            EMSPayGate_Default payGate = new EMSPayGate_Default(activity, rewardPayment);
            String reqChargeLoyaltyReward = payGate.paymentWithAction(EMSPayGate_Default.EAction.ChargeRewardAction, cardInfoManager.getWasSwiped(), cardInfoManager.getCardType(),
                    cardInfoManager);
            String xml = httpClient.postData(13, activity, reqChargeLoyaltyReward);
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
                    response.setResponseStatus(Response.ResponseStatus.OK);
                } else if (parsedMap != null && parsedMap.size() > 0) {
                    errorMsg = "statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage");
                } else
                    errorMsg = xml;
                response.setEpayStatusCode(parsedMap.get("epayStatusCode"));
                response.setMessage(parsedMap.get("statusMessage"));
                response.setStatusCode(parsedMap.get("statusCode"));
                response.setApprovedAmount(new BigDecimal(parsedMap.get("AuthorizedAmount")));
                response.setTransactionId(parsedMap.get("CreditCardTransID"));
            }
        } catch (Exception e) {

        }
        return response;
    }

    public static class Response {
        public enum ResponseStatus {
            OK, FAIL
        }

        private ResponseStatus responseStatus = ResponseStatus.FAIL;
        private String statusCode;
        private String message;
        private String epayStatusCode;
        private BigDecimal approvedAmount;
        private String transactionId;

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }


        public String getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(String statusCode) {
            this.statusCode = statusCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getEpayStatusCode() {
            return epayStatusCode;
        }

        public void setEpayStatusCode(String epayStatusCode) {
            this.epayStatusCode = epayStatusCode;
        }

        public ResponseStatus getResponseStatus() {
            return responseStatus;
        }

        public void setResponseStatus(ResponseStatus responseStatus) {
            this.responseStatus = responseStatus;
        }

        public BigDecimal getApprovedAmount() {
            return approvedAmount;
        }

        public void setApprovedAmount(BigDecimal approvedAmount) {
            this.approvedAmount = approvedAmount;
        }

    }
}
