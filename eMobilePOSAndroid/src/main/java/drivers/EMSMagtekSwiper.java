package drivers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.emobilepos.R;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.emobilepos.payment.ProcessCreditCard_FA;
import com.android.support.CardParser;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.magtek.mobile.android.libDynamag.MagTeklibDynamag;
import com.magtek.mobile.android.mtlib.MTConnectionType;
import com.magtek.mobile.android.mtlib.MTEMVEvent;
import com.magtek.mobile.android.mtlib.MTSCRA;
import com.magtek.mobile.android.mtlib.MTSCRAEvent;

import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

/**
 * Created by Guarionex on 12/3/2015.
 */
public class EMSMagtekSwiper extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    private Encrypt encrypt;
    private EMSDeviceManager edm;
    private EMSMagtekSwiper thisInstance;
    private boolean didConnect;
    private Handler m_scraHandler;
    private EMSCallBack msrCallBack;
    private MagTeklibDynamag dynamag;

    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        encrypt = new Encrypt(activity);
        this.edm = edm;
        thisInstance = this;
        playSound();
        edm.driverDidConnectToDevice(thisInstance, false, activity);
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        encrypt = new Encrypt(activity);
        this.edm = edm;
        thisInstance = this;
        playSound();
        edm.driverDidConnectToDevice(thisInstance, false, activity);
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory, boolean fromOnHold) {
        return true;
    }

    @Override
    public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint, EMVContainer emvContainer) {
        return true;
    }

    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        return true;
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        return true;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        return true;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        return true;
    }

    @Override
    public String printStationPrinter(List<Orders> orderProducts, String ordID, boolean cutPaper, boolean printHeader) {
        return "";
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        return true;
    }

    @Override
    public boolean printOnHold(Object onHold) {
        return false;
    }

    @Override
    public void setBitmap(Bitmap bmp) {

    }

    @Override
    public void playSound() {
        try {
            MediaPlayer mPlayer = MediaPlayer.create(activity, R.raw.beep);
            mPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void turnOnBCR() {

    }

    @Override
    public void turnOffBCR() {

    }

    @Override
    public boolean printReport(String curDate) {
        return true;
    }

    @Override
    public void printEndOfDayReport(String date, String clerk_id, boolean printDetails) {
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
    }

    @Override
    public void registerAll() {
        this.registerPrinter();
    }

    public void registerPrinter() {
        edm.setCurrentDevice(this);
    }

    public void unregisterPrinter() {
        edm.setCurrentDevice(null);
    }

    @Override
    public void loadCardReader(final EMSCallBack callBack, boolean isDebitCard) {
        msrCallBack = callBack;
        if (dynamag == null) {
            m_scraHandler = new Handler(new SCRAHandlerCallback());
            dynamag = new MagTeklibDynamag(activity,m_scraHandler);
            dynamag.clearCardData();
            dynamag.openDevice();
//            m_scraHandler = new Handler(new SCRAHandlerCallback());
//            m_scra = new MTSCRA(activity, m_scraHandler);
//            m_scra.setConnectionType(MTConnectionType.USB);
//            m_scra.setAddress(null);
//            m_scra.setConnectionRetry(true);
//            m_scra.openDevice();
        }
//        eloCardSwiper = new MagStripDriver(activity);
//        eloCardSwiper.startDevice();
//        eloCardSwiper.registerMagStripeListener(new MagStripDriver.MagStripeListener() { //MageStripe Reader's Listener for notifying various events.
//
//            @Override
//            public void OnDeviceDisconnected() { //Fired when the Device has been Disconnected.
//                Toast.makeText(activity, "Magnetic-Stripe Device Disconnected !", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void OnDeviceConnected() { //Fired when the Device has been Connected.
//                Toast.makeText(activity, "Magnetic-Stripe Device Connected !", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void OnCardSwiped(MagTeklibDynamag cardData) { //Fired when a card has been swiped on the device.
//                Log.d("Card Data", cardData.toString());
//                CreditCardInfo creditCardInfo = new CreditCardInfo();
//                boolean parsed = CardParser.parseCreditCard(activity, cardData.getCardData(), creditCardInfo);
//                callBack.cardWasReadSuccessfully(parsed, creditCardInfo);
//            }
//        });
    }

    @Override
    public void loadScanner(EMSCallBack callBack) {
    }

    @Override
    public void releaseCardReader() {
    }

    @Override
    public void openCashDrawer() {

    }

    @Override
    public void printHeader() {

    }

    @Override
    public void printFooter() {

    }

    @Override
    public boolean isUSBConnected() {
        return false;
    }

    @Override
    public void toggleBarcodeReader() {
    }

    @Override
    public void printReceiptPreview(SplittedOrder splitedOrder) {

    }

    @Override
    public void salePayment(Payment payment, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void saleReversal(Payment payment, String originalTransactionId, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void refund(Payment payment, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void refundReversal(Payment payment, String originalTransactionId, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void printEMVReceipt(String text) {

    }

    @Override
    public void sendEmailLog() {

    }

    @Override
    public void updateFirmware() {

    }

    @Override
    public void submitSignature() {

    }

    @Override
    public boolean isConnected() {
        return dynamag.isDeviceConnected();
    }

    @Override
    public void printClockInOut(List<ClockInOut> timeClocks, String clerkID) {
    }

    @Override
    public void printExpenseReceipt(ShiftExpense expense) {

    }

    private class SCRAHandlerCallback implements Handler.Callback {
        private static final String TAG = "Magtek";

        public boolean handleMessage(Message msg) {
            try {
                Log.i(TAG, "*** Callback " + msg.what);
                switch (msg.what) {
                    case MTSCRAEvent.OnDeviceConnectionStateChanged:
//                        OnDeviceStateChanged((MTConnectionState) msg.obj);
                        break;
                    case MTSCRAEvent.OnCardDataStateChanged:
//                        OnCardDataStateChanged((MTCardDataState) msg.obj);
                        break;
                    case MTSCRAEvent.OnDataReceived:
                        if (dynamag.getCardData() != null) {
                            CreditCardInfo cardInfo = new CreditCardInfo();
                            if (dynamag.getKSN().equals("00000000000000000000")) {
                                CardParser.parseCreditCard(activity, dynamag.getMaskedTracks(), cardInfo);
                            } else {
//                                cardInfo.setCardOwnerName(dynamag.getCardName());
//                                if (dynamag.getCardExpDate() != null && !dynamag.getCardExpDate().isEmpty()) {
//                                    String year = m_scra.getCardExpDate().substring(0, 2);
//                                    String month = m_scra.getCardExpDate().substring(2, 4);
//                                    cardInfo.setCardExpYear(year);
//                                    cardInfo.setCardExpMonth(month);
//                                }
//                                cardInfo.setCardType(ProcessCreditCard_FA.getCardType(dynamag.getCardIIN()));
//                                cardInfo.setCardLast4(dynamag.getCardLast4());
                                cardInfo.setEncryptedTrack1(dynamag.getTrack1());
                                cardInfo.setEncryptedTrack2(dynamag.getTrack2());
//                                cardInfo.setCardNumAESEncrypted(encrypt.encryptWithAES(dynamag.getCardPAN()));
                                if (dynamag.getTrack1Masked() != null && !dynamag.getTrack1Masked().isEmpty())
                                    cardInfo.setEncryptedAESTrack1(encrypt.encryptWithAES(dynamag.getTrack1Masked()));
                                if (dynamag.getTrack2Masked() != null && !dynamag.getTrack2Masked().isEmpty())
                                    cardInfo.setEncryptedAESTrack2(encrypt.encryptWithAES(dynamag.getTrack2Masked()));
                                cardInfo.setDeviceSerialNumber(dynamag.getDeviceSerial());
                                cardInfo.setMagnePrint(dynamag.getMagnePrint());
//                                cardInfo.setCardNumUnencrypted(dynamag.getCardPAN());
                                cardInfo.setMagnePrintStatus(dynamag.getMagnePrintStatus());
                                cardInfo.setTrackDataKSN(dynamag.getKSN());
                            }
                            msrCallBack.cardWasReadSuccessfully(true, cardInfo);
                        }
                        break;
                    case MTSCRAEvent.OnDeviceResponse:
//                        OnDeviceResponse((String) msg.obj);
                        break;
                    case MTEMVEvent.OnTransactionStatus:
//                        OnTransactionStatus((byte[]) msg.obj);
                        break;
                    case MTEMVEvent.OnDisplayMessageRequest:
//                        OnDisplayMessageRequest((byte[]) msg.obj);
                        break;
                    case MTEMVEvent.OnUserSelectionRequest:
//                        OnUserSelectionRequest((byte[]) msg.obj);
                        break;
                    case MTEMVEvent.OnARQCReceived:
//                        OnARQCReceived((byte[]) msg.obj);
                        break;
                    case MTEMVEvent.OnTransactionResult:
//                        OnTransactionResult((byte[]) msg.obj);
                        break;

                    case MTEMVEvent.OnEMVCommandResult:
//                        OnEMVCommandResult((byte[]) msg.obj);
                        break;

                    case MTEMVEvent.OnDeviceExtendedResponse:
//                        OnDeviceExtendedResponse((String) msg.obj);
                        break;
                }
            } catch (Exception ex) {

            }

            return true;
        }
    }


}
