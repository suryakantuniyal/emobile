package com.android.emobilepos.payment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.android.database.PaymentsHandler;
import com.android.database.PaymentsXML_DB;
import com.android.emobilepos.R;
import com.android.emobilepos.models.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.saxhandler.SAXProcessCardPayHandler;
import com.android.support.Global;
import com.android.support.NumberUtils;
import com.android.support.Post;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import util.StringUtil;

public class TipAdjustmentFA extends BaseFragmentActivityActionBar implements View.OnClickListener {

    private EditText transactionId;
    private EditText tipAmount;
    private Button submitTipAmountBtn;
    private TextView messageText;
    Spinner cardTypesSpinner;
    private HashMap<String, String> reverseXMLMap;
    private Global global;
    private boolean hasBeenCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip_adjustment_fa);
        messageText = (TextView) findViewById(R.id.messageTexttextView);
        transactionId = (EditText) findViewById(R.id.transactionIdEditText);
        tipAmount = (EditText) findViewById(R.id.tipAmountEditText);
        submitTipAmountBtn = (Button) findViewById(R.id.submitTipButton);
        cardTypesSpinner = (Spinner) findViewById(R.id.cardTypesspinner);
        String[] cardTypes = getResources().getStringArray(R.array.cardTypes);
        tipAmount.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NumberUtils.parseInputedCurrency(s, tipAmount);
            }
        });
        global = (Global) getApplication();
        hasBeenCreated = true;
        submitTipAmountBtn.setOnClickListener(this);
        messageText.setText("");
        tipAmount.setText("0.00");
        setSpinnerAdapter();
    }

    private void setSpinnerAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_layout, getResources().getStringArray(R.array.cardTypes));
        cardTypesSpinner.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if (transactionId != null && !transactionId.getText().toString().isEmpty() &&
                tipAmount != null && !tipAmount.getText().toString().isEmpty()) {
            BigDecimal tipAmountDec = Global.getBigDecimalNum(NumberUtils.cleanCurrencyFormatedNumber(tipAmount));
            if (tipAmountDec.compareTo(BigDecimal.ZERO) > 0) {
                messageText.setText("");
                new AdjustTipTask().execute(getCreditCardType(), transactionId.getText().toString(), tipAmountDec.toString());
            } else {
                setMessage(R.string.adjust_tip_required_fields);
            }
        } else {
            setMessage(R.string.adjust_tip_required_fields);
        }
    }

    private String getCreditCardType() {
        String cardName = StringUtil.trimSpace(cardTypesSpinner.getSelectedItem().toString());
        if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_AMEX)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_AMEX;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_DISCOVER)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_DISCOVER;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_MASTERCARD)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_MASTERCARD;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_VISA)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_VISA;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_JCB)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_JCB;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_CUP)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_CUP;
        } else if (cardName.trim().equalsIgnoreCase(ProcessCreditCard_FA.CREDITCARD_TYPE_DINERS)) {
            return ProcessCreditCard_FA.CREDITCARD_TYPE_DINERS;
        } else {
            return "";
        }
    }

    private void setMessage(int resId) {
        messageText.setText(resId);
    }

    private void showErrorDlog(final boolean reverse, String msg, final Payment payment) {
        final Dialog dlog = new Dialog(TipAdjustmentFA.this, R.style.Theme_TransparentTest);
        dlog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlog.setCancelable(false);
        dlog.setContentView(R.layout.dlog_btn_single_layout);

        TextView viewTitle = (TextView) dlog.findViewById(R.id.dlogTitle);
        TextView viewMsg = (TextView) dlog.findViewById(R.id.dlogMessage);
        viewTitle.setText(R.string.dlog_title_error);
        viewMsg.setText(msg);

        Button btnOk = (Button) dlog.findViewById(R.id.btnDlogSingle);
        btnOk.setText(R.string.button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         dlog.dismiss();
                                         if (reverse) {
                                             new processReverseAsync().execute(payment);
                                         }
                                     }
                                 }
        );

        dlog.show();
    }

    private class AdjustTipTask extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog;
        private Payment payment;
        private String paymentWithAction;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(TipAdjustmentFA.this);
            dialog.setMessage(getString(R.string.processing_tipamount));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String cardType = params[0];
            String transactionId = params[1];
            String amount = params[2];

            payment = new Payment(TipAdjustmentFA.this);
            payment.pay_transid = transactionId;
            payment.pay_tip = amount;
            payment.card_type = cardType;
            EMSPayGate_Default payGate = new EMSPayGate_Default(TipAdjustmentFA.this, payment);
            paymentWithAction = payGate.paymentWithAction(EMSPayGate_Default.EAction.CreditCardAdjustTipAmountAction, false, null,
                    null);

            Post httpClient = new Post();
            String xml = httpClient.postData(Global.S_SUBMIT_TIP_ADJUSTMENT, TipAdjustmentFA.this, paymentWithAction);

            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(TipAdjustmentFA.this);
            dialog.dismiss();
            if (xml.equals(Global.TIME_OUT) || xml.equals(Global.NOT_VALID_URL) || xml.isEmpty()) {
                String errorMsg = getString(R.string.dlog_msg_established_connection_failed);
                reverseXMLMap = ProcessCreditCard_FA.generateReverseXML(TipAdjustmentFA.this, paymentWithAction);
                reverseXMLMap.put(PaymentsXML_DB.charge_xml, paymentWithAction);
                showErrorDlog(true, errorMsg, payment);
                Global.showPrompt(TipAdjustmentFA.this, R.string.fail_to_connect, errorMsg);
            } else {
                InputSource inSource = new InputSource(new StringReader(xml));

                SAXParser sp = null;
                try {
                    sp = spf.newSAXParser();
                    XMLReader xr = sp.getXMLReader();
                    xr.setContentHandler(handler);
                    xr.parse(inSource);
                    HashMap<String, String> parsedMap = handler.getData();

                    String errorMsg;
                    if (parsedMap != null && parsedMap.size() > 0
                            && parsedMap.get("epayStatusCode").equals("APPROVED")) {
                        saveApprovedPayment(parsedMap, payment);
                        Global.showPrompt(TipAdjustmentFA.this, R.string.card_payment_title, getString(R.string.tippayment_saved_successfully));
                        resetFields();
                    } else if (parsedMap != null && parsedMap.size() > 0) {
                        errorMsg = "statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage");
                        reverseXMLMap = ProcessCreditCard_FA.generateReverseXML(TipAdjustmentFA.this, paymentWithAction);
                        reverseXMLMap.put(PaymentsXML_DB.charge_xml, paymentWithAction);
                        showErrorDlog(false, errorMsg, payment);
                    } else {
                        errorMsg = xml;
                        reverseXMLMap = ProcessCreditCard_FA.generateReverseXML(TipAdjustmentFA.this, paymentWithAction);
                        reverseXMLMap.put(PaymentsXML_DB.charge_xml, paymentWithAction);
                        showErrorDlog(false, errorMsg, payment);
                    }
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void resetFields() {
        reverseXMLMap = new HashMap<String, String>();
        transactionId.setText("");
        tipAmount.setText("");
    }

    private void saveApprovedPayment(HashMap<String, String> parsedMap, Payment payment) {
        PaymentsHandler paymentsHandler = new PaymentsHandler(TipAdjustmentFA.this);
        payment.pay_resultcode = parsedMap.get("pay_resultcode");
        payment.pay_resultmessage = parsedMap.get("pay_resultmessage");
        payment.pay_transid = parsedMap.get("CreditCardTransID");
        payment.authcode = parsedMap.get("AuthorizationCode");
        payment.processed = "9";
        paymentsHandler.insert(payment);

    }

    private class processReverseAsync extends AsyncTask<Payment, Void, Payment> {

        private HashMap<String, String> parsedMap = new HashMap<String, String>();

        private boolean reverseWasProcessed = false;
        private boolean paymentWasApproved = false;
        private String errorMsg = getString(R.string.dlog_msg_no_internet_access);
        private boolean paymentWasDecline = false;
        private ProgressDialog myProgressDialog;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(TipAdjustmentFA.this);
            myProgressDialog.setMessage(getString(R.string.please_wait_message));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected Payment doInBackground(Payment... params) {

            if (Global.isConnectedToInternet(TipAdjustmentFA.this)) {
                Post httpClient = new Post();

                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(TipAdjustmentFA.this);
                String reverseXml = reverseXMLMap.get(PaymentsXML_DB.payment_xml);
                String chargeXML = reverseXMLMap.get(PaymentsXML_DB.charge_xml);
                try {
                    String xml = httpClient.postData(Global.S_SUBMIT_TIP_ADJUSTMENT, TipAdjustmentFA.this, reverseXml);

                    if (xml.equals(Global.TIME_OUT) || xml.equals(Global.NOT_VALID_URL) || xml.isEmpty()) {
                        errorMsg = getString(R.string.dlog_msg_established_connection_failed);
                        reverseWasProcessed = true;
                        String _verify_payment_xml = chargeXML.replaceAll("<action>.*?</action>", "<action>"
                                + EMSPayGate_Default.getPaymentAction("CheckTransactionStatus") + "</action>");
                        xml = httpClient.postData(Global.S_SUBMIT_TIP_ADJUSTMENT, TipAdjustmentFA.this, _verify_payment_xml);
                        if (xml.equals(Global.TIME_OUT) || xml.equals(Global.NOT_VALID_URL)) {
                            errorMsg = getString(R.string.dlog_msg_established_connection_failed);
                        } else {
                            InputSource inSource = new InputSource(new StringReader(xml));
                            SAXParser sp = spf.newSAXParser();
                            XMLReader xr = sp.getXMLReader();
                            xr.setContentHandler(handler);
                            xr.parse(inSource);
                            parsedMap = handler.getData();
                            inSource = new InputSource(new StringReader(xml));
                            xr.parse(inSource);
                            parsedMap = handler.getData();
                            if (parsedMap != null) {
                                if (parsedMap.get("epayStatusCode").equals("APPROVED")) {
                                    paymentWasApproved = true;
                                } else if (parsedMap.get("epayStatusCode").equals("DECLINE")) {
                                    paymentWasDecline = true;
                                    errorMsg = "statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage");
                                } else
                                    errorMsg = xml;
                            }
                        }
                    } else {


                        if (parsedMap != null && parsedMap.size() > 0
                                && (parsedMap.get("epayStatusCode").equals("APPROVED")))
                            reverseWasProcessed = true;
                        else if (parsedMap != null && parsedMap.get("epayStatusCode").equals("DECLINE")) {
//                            reverseWasProcessed = true;
//                            String _verify_payment_xml = chargeXML.replaceAll("<action>.*?</action>", "<action>"
//                                    + EMSPayGate_Default.getPaymentAction("CheckTransactionStatus") + "</action>");
//                            xml = httpClient.postData(Global.S_SUBMIT_TIP_ADJUSTMENT, TipAdjustmentFA.this, _verify_payment_xml);
//                            if (xml.equals(Global.TIME_OUT) || xml.equals(Global.NOT_VALID_URL)) {
//                                errorMsg = getString(R.string.dlog_msg_established_connection_failed);
//                            } else {
//                                inSource = new InputSource(new StringReader(xml));
//                                xr.parse(inSource);
//                                parsedMap = handler.getData();
//                                if (parsedMap != null) {
//                                    if (parsedMap.get("epayStatusCode").equals("APPROVED")) {
//                                        paymentWasApproved = true;
//                                    } else if (parsedMap.get("epayStatusCode").equals("DECLINE")) {
//                                        paymentWasDecline = true;
//                                        errorMsg = "statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage");
//                                    } else
//                                        errorMsg = xml;
//                                }
//                            }
                        }
                    }

                } catch (Exception e) {

                    errorMsg = e.getMessage();
                }
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(Payment payment) {
            myProgressDialog.dismiss();
            String xmlAppId = reverseXMLMap.get(PaymentsXML_DB.app_id);
            if (reverseWasProcessed) {
                PaymentsXML_DB _paymentXml_DB = new PaymentsXML_DB(TipAdjustmentFA.this);
                _paymentXml_DB.deleteRow(xmlAppId);
                if (paymentWasApproved) {
                    saveApprovedPayment(parsedMap, payment);
                } else {
                    if (paymentWasDecline) {
                        showErrorDlog(true, errorMsg, payment);
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        if (global.isApplicationSentToBackground(this))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (hasBeenCreated && !global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            global.loggedIn = false;
        global.startActivityTransitionTimer();
    }
}
