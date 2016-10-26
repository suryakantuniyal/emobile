package com.android.payments;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Xml;

import com.android.database.CustomersHandler;
import com.android.database.OrdersHandler;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.Payment;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.GenerateXML;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.NumberUtils;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.UUID;

public class EMSPayGate_Default {

    private Activity activity;
    private MyPreferences myPref;
    private Payment payment;

    private XmlSerializer serializer;
    private StringWriter writer;
    private String empstr = "";
    private String totalAmount = "0.00";
    private boolean isTupyx = false;
    private CreditCardInfo cardManager;

//    private static final int CHARGE_CREDIT_CARD = 1010;
//    private static final int CHARGE_DEBIT_CARD = 1014;
//    private static final int CHARGE_CHECK = 1012;

    private static final int REVERSE_CREDIT_CARD = 9993;
    private static final int REVERSE_DEBIT_CARD = 9994;
    private static final int REVERSE_CHECK = 9992;

    public EMSPayGate_Default(Activity activity, Payment payment) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.payment = payment;
        serializer = Xml.newSerializer();
        writer = new StringWriter();
    }

    public enum EAction {
        ChargeCreditCardAction(1010), ChargeTupixAction(1010), ChargeCheckAction(1012), ChargeCashAction(1013),
        ChargeDebitAction(1014), ChargeGeniusAction(1017), ChargeGiftCardAction(1018),
        ChargeLoyaltyCardAction(1019), CreditCardAuthAction(1020), ChargeRewardAction(1021),
        CreditCardAdjustTipAmountAction(1011), VoidCreditCardAction(2010), VoidCheckAction(2012),
        ReturnCreditCardAction(3010), ReturnDebitAction(3014), ReturnGeniusAction(3017), VoidGiftCardAction(2018),
        ReturnGiftCardAction(3018), ActivateGiftCardAction(5000), VoidRewardCardAction(2021),
        //        DeactivateGiftCardAction(5010),
        AddValueGiftCardAction(5010), BalanceGiftCardAction(5020), ActivateLoyaltyCardAction(6000),
        AddValueLoyaltyCardAction(6010), BalanceLoyaltyCardAction(6020), ActivateRewardAction(6100),
        AddValueRewardAction(6110), BalanceRewardAction(6120), CheckTransactionStatus(7003),
        GiftCardReverseAction(9995), LoyaltyCardReverseAction(9996), RewardCardReverseAction(9997),
        ReverseCheckAction(9992), ReverseCreditCardAction(9993), ReverseDebitCardAction(9994),
        ProcessBoloroCheckout(10000), CancelBoloroTransaction(10001), GetTelcoInfoByTag(10002),
        GetMarketTelcos(10003), BoloroPolling(10004), HandpointWorkingKey(7001);

        private int code;

        EAction(int val) {
            this.code = val;
        }

        public static EAction toAction(int code) {
            switch (code) {
                case 1010:
                    return ChargeCreditCardAction;
                case 6100:
                    return ActivateRewardAction;
                case 6110:
                    return AddValueRewardAction;
                case 6120:
                    return BalanceRewardAction;
                case 7003:
                    return CheckTransactionStatus;
                case 9995:
                    return GiftCardReverseAction;
                case 9996:
                    return LoyaltyCardReverseAction;
                case 9997:
                    return RewardCardReverseAction;
                case 9992:
                    return ReverseCheckAction;
                case 9993:
                    return ReverseCreditCardAction;
                case 9994:
                    return ReverseDebitCardAction;
                case 10000:
                    return ProcessBoloroCheckout;
                case 10001:
                    return CancelBoloroTransaction;
                case 10002:
                    return GetTelcoInfoByTag;
                case 10003:
                    return GetMarketTelcos;
                case 10004:
                    return BoloroPolling;
                case 1012:
                    return ChargeCheckAction;
                case 1013:
                    return ChargeCashAction;
                case 1014:
                    return ChargeDebitAction;
                case 1017:
                    return ChargeGeniusAction;
                case 1018:
                    return ChargeGiftCardAction;
                case 1019:
                    return ChargeLoyaltyCardAction;
                case 1020:
                    return CreditCardAuthAction;
                case 1021:
                    return ChargeRewardAction;
                case 1011:
                    return CreditCardAdjustTipAmountAction;
                case 2012:
                    return VoidCheckAction;
                case 3010:
                    return ReturnCreditCardAction;
                case 3014:
                    return ReturnDebitAction;
                case 3017:
                    return ReturnGeniusAction;
                case 2018:
                    return VoidGiftCardAction;
                case 3018:
                    return ReturnGiftCardAction;
                case 5000:
                    return ActivateGiftCardAction;
//                case 5010: return DeactivateGiftCardAction;
                case 5010:
                    return AddValueGiftCardAction;
                case 5020:
                    return BalanceGiftCardAction;
                case 6000:
                    return ActivateLoyaltyCardAction;
                case 6010:
                    return AddValueLoyaltyCardAction;
                case 6020:
                    return BalanceLoyaltyCardAction;
                case 7001:
                    return HandpointWorkingKey;

            }
            return null;
        }

        public static EAction toAction(String str) {
            try {
                return valueOf(str);
            } catch (Exception ex) {
                return null;
            }
        }

        public int getCode() {
            return this.code;
        }

    }

    public static String getPaymentAction(String _action_name) {
        EAction action = EAction.toAction(_action_name);
        switch (action) {
            case CheckTransactionStatus:
                return "7003";
            default:
                return "";
        }
    }

    public String paymentWithAction(EAction actionType, boolean isSwipe, String data, CreditCardInfo _cardManager) {
//        EAction actions = EAction.toAction(actionType);
        this.cardManager = _cardManager;
        try {
            generateAccountInfo();

            serializer.startTag(empstr, "epay");
            serializer.startTag(empstr, "action");
            serializer.text(Integer.toString(actionType.getCode()));
            serializer.endTag(empstr, "action");
            serializer.startTag(empstr, "app_id");
            serializer.text(UUID.randomUUID().toString());
            serializer.endTag(empstr, "app_id");

            switch (actionType) {
                case ChargeTupixAction:
                    serializer.startTag(empstr, "wToken");
                    serializer.text(data);
                    serializer.endTag(empstr, "wToken");
                    generateERP();
                    generateAmountBlock();
                    generateOrderBlock(payment.getJob_id());


                    break;
                case ChargeGeniusAction:
                case ReturnGeniusAction:
                    generateERP();
                    generateAmountBlock();

                    generateContactInfoBlock(payment.getCust_id());

                    if (Global.isIvuLoto)
                        generateEvertec();
                    generateOrderBlock(payment.getJob_id());

                    break;
                case ActivateGiftCardAction:
                case ActivateLoyaltyCardAction:
                case ActivateRewardAction:
                    generateCardBlock(data, isSwipe);

                    if (isSwipe)
                        generateTrackData();

                    if (actionType == EAction.ChargeDebitAction)
                        generatePinBlock();

                    generateERP();
                    generateAmountBlock();

                    generateContactInfoBlock(payment.getCust_id());

                    if (isSwipe)
                        generateEncryptedBlock();

//				if (Global.isIvuLoto )
//					generateEvertec();


                    break;
                case CreditCardAdjustTipAmountAction:
                    generateERP();
                    generateAmountBlock();
                    generateContactInfoBlock(payment.getCust_id());

                    if (isSwipe)
                        generateEncryptedBlock();

                    if (Global.isIvuLoto)
                        generateEvertec();
                    generateOrderBlock(payment.getJob_id());
                    generateAdjustAmountBlock();

                    break;
                case CreditCardAuthAction:
                case ChargeCreditCardAction:
                case ChargeDebitAction:
                case ChargeRewardAction:
                case ChargeGiftCardAction:
                case ChargeLoyaltyCardAction:
                case ReverseCreditCardAction:
                    generateCardBlock(data, isSwipe);

                    if (isSwipe)
                        generateTrackData();

                    if (actionType == EAction.ChargeDebitAction)
                        generatePinBlock();

                    generateERP();
                    generateAmountBlock();

                    generateContactInfoBlock(payment.getCust_id());

                    if (isSwipe)
                        generateEncryptedBlock();

                    if (Global.isIvuLoto)
                        generateEvertec();
                    generateOrderBlock(payment.getJob_id());

                    break;

                case AddValueGiftCardAction:
                case AddValueLoyaltyCardAction:
                case AddValueRewardAction:
                    generateCardBlock(data, isSwipe);

                    if (isSwipe)
                        generateTrackData();

                    if (actionType == EAction.ChargeDebitAction)
                        generatePinBlock();

                    generateERP();
                    generateAmountBlock();

                    generateContactInfoBlock(payment.getCust_id());

                    if (isSwipe)
                        generateEncryptedBlock();

                    if (Global.isIvuLoto)
                        generateEvertec();


                    break;
                case BalanceGiftCardAction:
                case BalanceLoyaltyCardAction:
                case BalanceRewardAction:
                    generateCardBlock(data, isSwipe);

                    if (isSwipe)
                        generateTrackData();
                    generateERP();
                    // generateContactInfoBlock(payment.cust_id);
                    if (isSwipe)
                        generateEncryptedBlock();
                    //

                    break;
                // case AddValueGiftCardAction:
                //
                // break;
                case ReturnCreditCardAction:
                case ReturnDebitAction:
                case ReturnGiftCardAction: {
                    generateCardBlock(data, isSwipe);

                    if (isSwipe)
                        generateTrackData();

                    if (actionType == EAction.ReturnDebitAction)
                        generatePinBlock();

                    generateERP();
                    generateAmountBlock();

                    // generateOrderBlock();
                    generateContactInfoBlock(payment.getCust_id());

                    if (isSwipe)
                        generateEncryptedBlock();

                    generateVoidBlock();

                    generateOrderBlock(payment.getJob_id());


                    break;
                }
                case VoidGiftCardAction:
                case VoidRewardCardAction:
                case VoidCreditCardAction:
                case VoidCheckAction: {
                    if (isSwipe)
                        generateTrackData();

                    generateERP();
                    generateAmountBlock();

                    generateContactInfoBlock(payment.getCust_id());
                    generateVoidBlock();

                    generateOrderBlock(payment.getJob_id());

                    break;
                }
                case ChargeCheckAction:
                    generateCheckBlock();

                    generateERP();
                    generateAmountBlock();

                    generateContactInfoBlock(payment.getCust_id());
                    generateOrderBlock(payment.getJob_id());

                    break;
                case GetMarketTelcos:
                    generateERP();


                    break;
                case CancelBoloroTransaction:
                case BoloroPolling:
                    generateERP();
                    generateVoidBlock();


                    break;
                case GetTelcoInfoByTag:
                    generateERP();
                    generateAmountBlock();

                    generateBoloroBlock();
                    generateOrderBlock(payment.getJob_id());

                    break;
                case ProcessBoloroCheckout:
                    generateERP();
                    generateAmountBlock();

                    generateContactInfoBlock(payment.getCust_id());
                    generateBoloroBlock();
                    generateOrderBlock(payment.getJob_id());

                    break;
                default:
                    break;
            }
            serializer.endTag(empstr, "epay");
            serializer.endDocument();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return writer.toString();
    }

    public void generateOrderBlock(String orderId) {
        try {
            OrdersHandler handler = new OrdersHandler(this.activity);
            GenerateXML xmlGen = new GenerateXML(this.activity);
            Order order = handler.getOrder(orderId);
            if (order != null && !TextUtils.isEmpty(order.ord_id))
                xmlGen.buildOrder(serializer, false, order);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getReverseAction(EMSPayGate_Default.EAction originalAction) {
        switch (originalAction) {
            case CreditCardAdjustTipAmountAction:
            case ChargeCreditCardAction:
                return Integer.toString(REVERSE_CREDIT_CARD);
            case ChargeDebitAction:
                return Integer.toString(REVERSE_DEBIT_CARD);
            case ChargeCheckAction:
                return Integer.toString(REVERSE_CHECK);
//            case CHARGE_CREDIT_CARD:
//                return Integer.toString(REVERSE_CREDIT_CARD);
//            case CHARGE_DEBIT_CARD:
//                return Integer.toString(REVERSE_DEBIT_CARD);
//            case CHARGE_CHECK:
//                return Integer.toString(REVERSE_CHECK);
        }
        return "0";
    }

    public String paymentWithTupyx(String qrData, String _totalAmount) {

        isTupyx = true;
        totalAmount = _totalAmount;

        try {
            generateAccountInfo();

            serializer.startTag(empstr, "epay");
            serializer.startTag(empstr, "action");
            serializer.text("1010");
            serializer.endTag(empstr, "action");
            serializer.startTag(empstr, "app_id");
            serializer.text(UUID.randomUUID().toString());
            serializer.endTag(empstr, "app_id");
            serializer.startTag(empstr, "wToken");
            serializer.text(qrData);
            serializer.endTag(empstr, "wToken");
            generateERP();
            generateAmountBlock();
            serializer.endTag(empstr, "epay");
            serializer.endDocument();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return writer.toString();
    }

    private void generateAccountInfo() throws IllegalArgumentException, IllegalStateException, IOException {
        Encrypt encrypt = new Encrypt(activity);
        serializer.setOutput(writer);
        serializer.startDocument("UTF-8", true);
        serializer.startTag(empstr, "EXML");
        serializer.startTag(empstr, "AccountInformation");

        serializer.startTag(empstr, "DeviceID");
        serializer.text(myPref.getDeviceID());
        serializer.endTag(empstr, "DeviceID");

        serializer.startTag(empstr, "EmployeeID");
        serializer.text(myPref.getEmpID());
        serializer.endTag(empstr, "EmployeeID");

        serializer.startTag(empstr, "Account");
        serializer.text(myPref.getAcctNumber());
        serializer.endTag(empstr, "Account");

        serializer.startTag(empstr, "ActivationKey");
        serializer.text(myPref.getActivKey());
        serializer.endTag(empstr, "ActivationKey");

        serializer.startTag(empstr, "Password");
        serializer.text(encrypt.encryptWithAES(myPref.getAcctPassword()));
        serializer.endTag(empstr, "Password");

        serializer.startTag(empstr, "BundleVersion");
        serializer.text(myPref.getBundleVersion());
        serializer.endTag(empstr, "BundleVersion");

        serializer.endTag(empstr, "AccountInformation");
    }

    private void generateCardBlock(String card_name, boolean isSwipe)
            throws IllegalArgumentException, IllegalStateException, IOException {
        String value;
        serializer.startTag(empstr, "CCardBlock");

        value = payment.getPay_name();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "CCName");
            serializer.text(value);
            serializer.endTag(empstr, "CCName");
        }

        serializer.startTag(empstr, "CCNumber");
        serializer.text(payment.getPay_ccnum());
        serializer.endTag(empstr, "CCNumber");

        value = payment.getPay_seccode();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "CCSecCode");
            serializer.text(value);
            serializer.endTag(empstr, "CCSecCode");
        }

        serializer.startTag(empstr, "CCExpMonth");
        serializer.text(payment.getPay_expmonth());
        serializer.endTag(empstr, "CCExpMonth");

        serializer.startTag(empstr, "CCExpYear");
        serializer.text(payment.getPay_expyear());
        serializer.endTag(empstr, "CCExpYear");

        value = payment.getPay_addr();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "CCAddr");
            serializer.text(value);
            serializer.endTag(empstr, "CCAddr");
        }

        value = payment.getPay_poscode();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "CCPosCode");
            serializer.text(value);
            serializer.endTag(empstr, "CCPosCode");
        }

        value = card_name;
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "CCCardType");
            serializer.text(value);
            serializer.endTag(empstr, "CCCardType");
        }

        serializer.startTag(empstr, "CCEntryMode");
        if (!isSwipe)
            serializer.text("manual");
        else
            serializer.text("swipe");
        serializer.endTag(empstr, "CCEntryMode");

        serializer.endTag(empstr, "CCardBlock");
    }

    private void generateTrackData() throws IllegalArgumentException, IllegalStateException, IOException {
        String tr1;
        String tr2;
        // TrackData
        tr1 = payment.getTrack_one();
        tr2 = payment.getTrack_two();
        if ((tr1 != null && !tr1.isEmpty()) || (tr2 != null && !tr2.isEmpty())) {
            serializer.startTag(empstr, "TrackData");

            if (tr1 != null && !tr1.isEmpty()) {
                serializer.startTag(empstr, "track1");
                serializer.text(tr1);
                serializer.endTag(empstr, "track1");
            }

            if (tr2 != null && !tr2.isEmpty()) {
                serializer.startTag(empstr, "track2");
                serializer.text(tr2);
                serializer.endTag(empstr, "track2");
            }

            serializer.endTag(empstr, "TrackData");
        }
    }

    private void generateEncryptedBlock() throws IllegalArgumentException, IllegalStateException, IOException {
        if (!cardManager.getTrackDataKSN().isEmpty()) {
            serializer.startTag(empstr, "encryptedHW");

            serializer.startTag(empstr, "encryptedBlock");
            serializer.text(cardManager.getEncryptedBlock());
            serializer.endTag(empstr, "encryptedBlock");

            serializer.startTag(empstr, "ksn");
            serializer.text(cardManager.getTrackDataKSN());
            serializer.endTag(empstr, "ksn");

            if (!cardManager.getMagnePrint().isEmpty() && !cardManager.getMagnePrintStatus().isEmpty()) {

                serializer.startTag(empstr, "magnePrint");
                serializer.text(cardManager.getMagnePrint());
                serializer.endTag(empstr, "magnePrint");

                serializer.startTag(empstr, "magnePrintStatus");
                serializer.text(cardManager.getMagnePrintStatus());
                serializer.endTag(empstr, "magnePrintStatus");

                serializer.startTag(empstr, "deviceSerialNumber");
                serializer.text(cardManager.getDeviceSerialNumber());
                serializer.endTag(empstr, "deviceSerialNumber");
            }

            if (!cardManager.getEncryptedTrack1().isEmpty()) {
                serializer.startTag(empstr, "encryptedTrack1");
                serializer.text(cardManager.getEncryptedTrack1());
                serializer.endTag(empstr, "encryptedTrack1");
            }

            if (!cardManager.getEncryptedTrack2().isEmpty()) {
                serializer.startTag(empstr, "encryptedTrack2");
                serializer.text(cardManager.getEncryptedTrack2());
                serializer.endTag(empstr, "encryptedTrack2");
            }

            serializer.endTag(empstr, "encryptedHW");
        }
    }

    private void generatePinBlock() throws IllegalArgumentException, IllegalStateException, IOException {
        // PinBlock
        serializer.startTag(empstr, "PinBlock");

        serializer.startTag(empstr, "pin");
        serializer.text(cardManager.getDebitPinBlock());
        serializer.endTag(empstr, "pin");

        serializer.startTag(empstr, "sn");
        serializer.text(cardManager.getDebitPinSerialNum());
        serializer.endTag(empstr, "sn");

        serializer.endTag(empstr, "PinBlock");
    }

    private void generateCheckBlock() throws IllegalArgumentException, IllegalStateException, IOException {
        String value;
        // ChecksBlock
        serializer.startTag(empstr, "ChecksBlock");

        value = payment.getCheck_account_number();
        serializer.startTag(empstr, "CCBankAccountNumber");
        serializer.text(value);
        serializer.endTag(empstr, "CCBankAccountNumber");

        value = payment.getCheck_routing_number();
        serializer.startTag(empstr, "CCRouting");
        serializer.text(value);
        serializer.endTag(empstr, "CCRouting");

        value = payment.getCheck_check_number();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "CCCheckNum");
            serializer.text(value);
            serializer.endTag(empstr, "CCCheckNum");
        }

        value = payment.getCheck_check_type();
        serializer.startTag(empstr, "CCheckType");
        serializer.text(value);
        serializer.endTag(empstr, "CCheckType");

        value = payment.getCheck_account_type();
        serializer.startTag(empstr, "CCAcctType");
        serializer.text(value);
        serializer.endTag(empstr, "CCAcctType");

        value = payment.getPay_name();
        serializer.startTag(empstr, "CCName");
        serializer.text(value);
        serializer.endTag(empstr, "CCName");

        value = payment.getPay_addr();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "CCAddr");
            serializer.text(value);
            serializer.endTag(empstr, "CCAddr");
        }

        value = payment.getCheck_city();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "CCCity");
            serializer.text(value);
            serializer.endTag(empstr, "CCCity");
        }

        value = payment.getCheck_state();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "CCState");
            serializer.text(value);
            serializer.endTag(empstr, "CCState");
        }

        value = payment.getPay_poscode();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "CCPosCode");
            serializer.text(value);
            serializer.endTag(empstr, "CCPosCode");
        }

        value = payment.getFrontImage();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "frontImage");
            serializer.text(value);
            serializer.endTag(empstr, "frontImage");
        }

        value = payment.getBackImage();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "backImage");
            serializer.text(value);
            serializer.endTag(empstr, "backImage");
        }

        serializer.startTag(empstr, "micrData");
        serializer.text(payment.getMicrData());
        serializer.endTag(empstr, "micrData");

        value = payment.getDl_state();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "DLstate");
            serializer.text(value);
            serializer.endTag(empstr, "DLstate");
        }

        value = payment.getDl_number();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "DLnumber");
            serializer.text(value);
            serializer.endTag(empstr, "DLnumber");
        }

        value = payment.getDl_dob();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "DOByear");
            serializer.text(value);
            serializer.endTag(empstr, "DOByear");
        }

        serializer.endTag(empstr, "ChecksBlock");
    }

    private void generateERP() throws IllegalArgumentException, IllegalStateException, IOException {
        String value;
        // ERP block

        serializer.startTag(empstr, "ERP");

        serializer.startTag(empstr, "PaymentMethodID");
        serializer.text(payment.getPaymethod_id());
        serializer.endTag(empstr, "PaymentMethodID");

        if (!isTupyx) {
            value = "0";
            if (!value.isEmpty()) {
                serializer.startTag(empstr, "CCAccount");
                serializer.text(value);
                serializer.endTag(empstr, "CCAccount");
            }

            value = payment.getCust_id();
            if (value != null && !value.isEmpty()) {
                serializer.startTag(empstr, "CustomerID");
                serializer.text(value);
                serializer.endTag(empstr, "CustomerID");
            }

        }
        value = payment.getJob_id();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "JobID");
            serializer.text(value);
            serializer.endTag(empstr, "JobID");
        }

        value = "";
        if (!value.isEmpty()) {
            serializer.startTag(empstr, "Comment");
            serializer.text(value);
            serializer.endTag(empstr, "Comment");
        }

        value = myPref.getClerkID();
        if (value != null && !value.isEmpty()
                && (!myPref.getShiftIsOpen() || myPref.getPreferences(MyPreferences.pref_use_clerks))) {
            serializer.startTag(empstr, "clerkID");
            serializer.text(value);
            serializer.endTag(empstr, "clerkID");
        }

        value = payment.getRef_num();
        if (value != null && !value.isEmpty()) {
            serializer.startTag(empstr, "CCRef");
            serializer.text(value);
            serializer.endTag(empstr, "CCRef");
        }

        // dateandtime (payment dateandtime) mandatory
        value = payment.getPay_date();
        serializer.startTag(empstr, "dateandtime");
        serializer.text(value);
        serializer.endTag(empstr, "dateandtime");

        serializer.endTag(empstr, "ERP");
    }

    private void generateAdjustAmountBlock() throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(empstr, "AdjustAmountBlock");
        serializer.startTag(empstr, "TransID");
        serializer.text(payment.getPay_transid());
        serializer.endTag(empstr, "TransID");
        serializer.startTag(empstr, "CCCardType");
        serializer.text(payment.getCard_type());
        serializer.endTag(empstr, "CCCardType");
        serializer.endTag(empstr, "AdjustAmountBlock");
    }

    private void generateAmountBlock() throws IllegalArgumentException, IllegalStateException, IOException {

        // Amounts block
        serializer.startTag(empstr, "Amounts");

        serializer.startTag(empstr, "CCAmt");
        String temp = NumberUtils.cleanCurrencyFormatedNumber(payment.getPay_amount());
        try {
            temp = Global.getRoundBigDecimal(new BigDecimal(temp), 2);
        } catch (Exception e) {
            temp = payment.getPay_amount();
        }
        serializer.text(temp);
        serializer.endTag(empstr, "CCAmt");

        serializer.startTag(empstr, "tipAmount");
        serializer.text(payment.getPay_tip());
        serializer.endTag(empstr, "tipAmount");
        if (isTupyx) {
            serializer.startTag(empstr, "originalTotalAmount");
            serializer.text(NumberUtils.cleanCurrencyFormatedNumber(totalAmount));
            serializer.endTag(empstr, "originalTotalAmount");
        } else if (!payment.getOriginalTotalAmount().isEmpty()) {
            serializer.startTag(empstr, "originalTotalAmount");
            serializer.text(NumberUtils.cleanCurrencyFormatedNumber(payment.getOriginalTotalAmount()));
            serializer.endTag(empstr, "originalTotalAmount");
        }

        if (cardManager != null) {
            String val = cardManager.getRedeemAll();
            if (!val.isEmpty()) {
                serializer.startTag(empstr, "redeemAll");
                serializer.text(val);
                serializer.endTag(empstr, "redeemAll");

                serializer.startTag(empstr, "redeem_type");
                serializer.text(cardManager.getRedeemType());
                serializer.endTag(empstr, "redeem_type");
            }
        }
        serializer.endTag(empstr, "Amounts");
    }

    private void generateContactInfoBlock(String cust_id)
            throws IllegalArgumentException, IllegalStateException, IOException {
        if (cust_id != null && !cust_id.isEmpty()) {
            CustomersHandler custHandler = new CustomersHandler(activity);
            String[] data = custHandler.getContactInfoBlock(cust_id);
            // ContactInfo block
            serializer.startTag(empstr, "ContactInfo");

            if (data[0] != null && !data[0].isEmpty()) {
                serializer.startTag(empstr, "phone");
                serializer.text(data[0]);
                serializer.endTag(empstr, "phone");
            }

            if (data[1] != null && !data[1].isEmpty()) {
                serializer.startTag(empstr, "email");
                serializer.text(data[1]);
                serializer.endTag(empstr, "email");
            }

            serializer.endTag(empstr, "ContactInfo");
        } else if (!payment.getPay_phone().isEmpty()) {
            serializer.startTag(empstr, "ContactInfo");
            serializer.startTag(empstr, "phone");
            serializer.text(payment.getPay_phone());
            serializer.endTag(empstr, "phone");
            serializer.endTag(empstr, "ContactInfo");
        }
    }

    private void generateEvertec() throws IllegalArgumentException, IllegalStateException, IOException {
        // EvertecTaxes block
        serializer.startTag(empstr, "EvertecTaxes");

        serializer.startTag(empstr, "Tax1"); // Estatal
        serializer.text(TextUtils.isEmpty(payment.getTax1_amount()) ? "0" : payment.getTax1_amount());
        serializer.endTag(empstr, "Tax1");

        serializer.startTag(empstr, "Tax2"); // Municipal
        serializer.text(TextUtils.isEmpty(payment.getTax2_amount()) ? "0" : payment.getTax2_amount());
        serializer.endTag(empstr, "Tax2");

        serializer.endTag(empstr, "EvertecTaxes");
    }

    private void generateVoidBlock() throws IllegalArgumentException, IllegalStateException, IOException {
        // VoidBlock

        serializer.startTag(empstr, "VoidBlock");

        serializer.startTag(empstr, "TransID");
        serializer.text(payment.getPay_transid());
        serializer.endTag(empstr, "TransID");

        if (!payment.getAuthcode().isEmpty()) {
            serializer.startTag(empstr, "AuthCode");
            serializer.text(payment.getAuthcode());
            serializer.endTag(empstr, "AuthCode");
        }

        // CCCardType
        if (!payment.getCard_type().isEmpty()) {
            serializer.startTag(empstr, "CCCardType");
            serializer.text(payment.getCard_type());
            serializer.endTag(empstr, "CCCardType");
        }

        serializer.endTag(empstr, "VoidBlock");
    }

    private void generateBoloroBlock() throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag(empstr, "Boloro");

        if (!payment.getTagid().isEmpty()) {
            serializer.startTag(empstr, "tagid");
            serializer.text(payment.getTagid());
            serializer.endTag(empstr, "tagid");
        } else {
            serializer.startTag(empstr, "telcoid");
            serializer.text(payment.getTelcoid());
            serializer.endTag(empstr, "telcoid");

            serializer.startTag(empstr, "transmode");
            serializer.text(payment.getTransmode());
            serializer.endTag(empstr, "transmode");
        }

        serializer.endTag(empstr, "Boloro");
    }
}
