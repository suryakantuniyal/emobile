package com.android.emobilepos.storedforward;

import android.app.Activity;
import android.os.AsyncTask;

import com.android.dao.StoredPaymentsDAO;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.storedAndForward.StoreAndForward;
import com.android.emobilepos.payment.ProcessBoloro_FA;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.GenerateNewID;
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

import io.realm.Realm;

/**
 * Created by guarionex on 8/24/16.
 */
public class BoloroPayment {
    private static boolean isPolling = true;

    public static void stopPolling() {
        isPolling = false;
    }

    public static HashMap<String, String> executeNFCCheckout(Activity activity, String xml, Payment payment) throws ParserConfigurationException, SAXException, IOException {
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
            Realm.getDefaultInstance().beginTransaction();
            payment.setPay_transid(response.get("transaction_id"));
            Realm.getDefaultInstance().commitTransaction();
            return executeBoloroPolling(activity, payment, isPolling);
        }
        return response;
    }

    private static HashMap<String, String> executeBoloroPolling(Activity activity, Payment payment, boolean isPolling) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXProcessCardPayHandler myParser = new SAXProcessCardPayHandler(activity);
        HashMap<String, String> response = null;
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
                response = myParser.getData();

                if (response != null && !response.isEmpty() && Boolean.parseBoolean(response.get("success"))) {
                    if (isPolling && response.containsKey("next_action") && response.get("next_action").equals("POLL")) {
                        isPolling = true;
                        try {
                            Thread.sleep(ProcessBoloro_FA.POLLING_SLEEP_TIME);
                        } catch (InterruptedException e) {
                        }
                    } else if (response.containsKey("next_action") && response.get("next_action").equals("SUCCESS")) {
                        Realm.getDefaultInstance().beginTransaction();
                        PaymentsHandler payHandler = new PaymentsHandler(activity);
                        payment.setProcessed("1");
                        GenerateNewID newID = new GenerateNewID(activity);
                        String nextID = newID.getNextID(GenerateNewID.IdType.PAYMENT_ID);
                        payment.setPay_id(nextID);
//                        BigDecimal bg = new BigDecimal(Global.amountPaid);
//                        Global.amountPaid = bg.setScale(2, RoundingMode.HALF_UP).toString();
//                        payment.setPay_dueamount(Global.amountPaid);
//                        payment.setPay_amount(Global.amountPaid);
                        payHandler.insert(payment);
                        Realm.getDefaultInstance().commitTransaction();
                        isPolling = false;
                        transCompleted = true;
                    } else if (response.containsKey("next_action") && response.get("next_action").equals("FAILED"))
                        failed = true;
                } else {
                    failed = true;
                }
            } while (!failed && isPolling && !transCompleted);
        } catch (Exception e) {
            e.printStackTrace();
            if (Realm.getDefaultInstance().isInTransaction()) {
                Realm.getDefaultInstance().cancelTransaction();
            }
        }
        return response;
    }

    public static void seveBoloroAsInvoice(Activity activity, StoreAndForward storeAndForward, HashMap<String, String> parsedMap) {
        OrdersHandler dbOrdHandler = new OrdersHandler(activity);
        StoredPaymentsDAO dbStoredPay = new StoredPaymentsDAO(activity);
        //remove from StoredPayment and change order to Invoice
        StringBuilder sb = new StringBuilder();
        String job_id = storeAndForward.getPayment().getJob_id();
        sb.append(dbOrdHandler.getColumnValue("ord_comment", job_id)).append("  ");
        sb.append("(Card Holder: ").append(storeAndForward.getPayment().getPay_name());//myCursor.getString(myCursor.getColumnIndex("pay_name")));
        sb.append("; Last 4: ").append(storeAndForward.getPayment().getCcnum_last4());//myCursor.getString(myCursor.getColumnIndex("ccnum_last4")));
        sb.append("; Exp date: ").append(storeAndForward.getPayment().getPay_expmonth());//myCursor.getString(myCursor.getColumnIndex("pay_expmonth")));
        sb.append("/").append(storeAndForward.getPayment().getPay_expyear());//myCursor.getString(myCursor.getColumnIndex("pay_expyear")));
        if (parsedMap.containsKey("statusMessage")) {
            sb.append("; Status Msg: ").append(parsedMap.get("statusMessage"));
            sb.append("; Status Code: ").append(parsedMap.get("statusCode"));
            sb.append("; TransID: ").append(parsedMap.get("CreditCardTransID"));
            sb.append("; Auth Code: ").append(parsedMap.get("AuthorizationCode")).append(")");
        } else if (parsedMap.containsKey("error_message")) {
            sb.append("; Status Msg: ").append(parsedMap.get("error_message"));
            sb.append("; Status Code: ").append(parsedMap.get("error_code"));
        }
        String pay_uuid = storeAndForward.getPayment().getPay_uuid();
        dbStoredPay.deleteStoredPaymentRow(pay_uuid);
        if (dbOrdHandler.getColumnValue("ord_type", job_id).equals(Global.OrderType.SALES_RECEIPT.getCodeString()))
            dbOrdHandler.updateOrderTypeToInvoice(job_id);
        dbOrdHandler.updateOrderComment(job_id, sb.toString());

        //Remove as pending stored & forward if no more payments are pending to be processed.
        if (dbStoredPay.getCountPendingStoredPayments(job_id) <= 0)
            dbOrdHandler.updateOrderStoredFwd(job_id, "0");

    }
}
