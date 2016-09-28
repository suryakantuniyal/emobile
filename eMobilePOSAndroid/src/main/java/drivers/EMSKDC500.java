package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.CardParser;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import koamtac.kdc.sdk.KDCBarcodeDataReceivedListener;
import koamtac.kdc.sdk.KDCConnectionListener;
import koamtac.kdc.sdk.KDCConstants;
import koamtac.kdc.sdk.KDCData;
import koamtac.kdc.sdk.KDCDataReceivedListener;
import koamtac.kdc.sdk.KDCGPSDataReceivedListener;
import koamtac.kdc.sdk.KDCMSRDataReceivedListener;
import koamtac.kdc.sdk.KDCNFCDataReceivedListener;
import koamtac.kdc.sdk.KDCReader;
import koamtac.kdc.sdk.KPOSConstants;
import koamtac.kdc.sdk.KPOSData;
import koamtac.kdc.sdk.KPOSDataReceivedListener;
import main.EMSDeviceManager;

/**
 * Created by Guarionex on 12/8/2015.
 */
public class EMSKDC500 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate,
        KDCDataReceivedListener,        // required for KDC Barcode Solution models
        KDCBarcodeDataReceivedListener,  // required for KDC Barcode Solution models
        KDCGPSDataReceivedListener,        // required for KDC Barcode Solution models
        KDCMSRDataReceivedListener,        // required for KDC Barcode Solution models
        KDCNFCDataReceivedListener,        // required for KDC Barcode Solution models
        KPOSDataReceivedListener,        // required for KDC Payment Solution models
        KDCConnectionListener            // required for all

{

    private EMSCallBack scannerCallBack;
    private Encrypt encrypt;
    private CreditCardInfo cardManager;
    private EMSDeviceManager edm;
    private EMSKDC500 thisInstance;
    KDCReader kdcReader;
    String msg = "Failed to connect";


    private Handler handler;
    String scannedData = "";
    private BluetoothDevice btDev;


    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);
        this.edm = edm;
        thisInstance = this;
        new processConnectionAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);
        this.edm = edm;
        thisInstance = this;
        if (connectKDC500()) {
            this.edm.driverDidConnectToDevice(thisInstance, false);
            return true;
        } else {
            this.edm.driverDidNotConnectToDevice(thisInstance, msg, false);
            return false;
        }
    }

    private boolean connectKDC500() {
        if (kdcReader == null) {
            kdcReader = new KDCReader(this, null, null, null, null, this, this, false);
        }
        btDev = null;
        if (KDCReader.GetAvailableDeviceList() != null && KDCReader.GetAvailableDeviceList().size() > 0) {
            btDev = KDCReader.GetAvailableDeviceList().get(0);
            kdcReader.Connect(btDev);
        }
        return true;
    }

    @Override
    public void NFCDataReceived(KDCData kdcData) {

    }

    private void HandleNFCCardReadEvent(KPOSData pData) {
        String nfcUID = pData.GetNFCUID();
        scannerCallBack.nfcWasRead(nfcUID);
    }


    public class processConnectionAsync extends AsyncTask<Void, String, Boolean> {

        private ProgressDialog myProgressDialog;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Connecting Printer...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return connectKDC500();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            myProgressDialog.dismiss();

            if (result) {
                edm.driverDidConnectToDevice(thisInstance, true);
            } else {

                edm.driverDidNotConnectToDevice(thisInstance, msg, true);
            }

        }
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        return false;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
        return false;
    }

    @Override
    public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint, EMVContainer emvContainer) {
        return false;
    }


    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        return false;
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        return false;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        return false;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        return false;
    }

    @Override
    public String printStationPrinter(List<Orders> orderProducts, String ordID, boolean cutPaper, boolean printHeader) {
        return "";
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        return false;
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

    }

    @Override
    public boolean printReport(String curDate) {
        return false;
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {

    }

    @Override
    public void printEndOfDayReport(String date, String clerk_id, boolean printDetails) {

    }


    @Override
    public void registerAll() {
        this.registerPrinter();
    }


    public void registerPrinter() {

        edm.currentDevice = thisInstance;
    }

    public void unregisterPrinter() {
        kdcReader.DisableNFC_POS();

        edm.currentDevice = null;
    }

    @Override
    public void loadCardReader(EMSCallBack callBack, boolean isDebitCard) {
        if (handler == null)
            handler = new Handler();
        scannerCallBack = callBack;
        kdcReader.EnableMSR_POS();
        kdcReader.EnableNFC_POS();
        handler.post(doUpdateDidConnect);
    }

    private Runnable doUpdateDidConnect = new Runnable() {
        public void run() {
            try {
                if (scannerCallBack != null)
                    scannerCallBack.readerConnectedSuccessfully(true);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    @Override
    public void loadScanner(EMSCallBack callBack) {
        scannerCallBack = callBack;
        kdcReader.EnableMSR_POS();
        kdcReader.EnableNFC_POS();
        if (handler == null)
            handler = new Handler();

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
    public void printReceiptPreview(View view) {

    }

    @Override
    public void salePayment(Payment payment) {

    }

    @Override
    public void saleReversal(Payment payment, String originalTransactionId) {

    }

    @Override
    public void refund(Payment payment) {

    }

    @Override
    public void refundReversal(Payment payment, String originalTransactionId) {

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
    public void BarcodeDataReceived(KDCData kdcData) {
    }

    @Override
    public void ConnectionChanged(BluetoothDevice bluetoothDevice, int state) {
        switch (state) {
            case KDCConstants.CONNECTION_STATE_CONNECTED:
                Log.d("KDCReader", "Connected");
                break;

            case KDCConstants.CONNECTION_STATE_CONNECTING:
            case KDCConstants.CONNECTION_STATE_NONE:
                Log.d("KDCReader", "Connection Closed");
                break;
            case KDCConstants.CONNECTION_STATE_LOST:
                Log.d("KDCReader", "Connection Lost");
                break;
            case KDCConstants.CONNECTION_STATE_FAILED:
                break;
            case KDCConstants.CONNECTION_STATE_LISTEN:
                break;
        }
    }


    @Override
    public void DataReceived(KDCData kdcData) {

    }

    @Override
    public void GPSDataReceived(KDCData kdcData) {

    }

    @Override
    public void MSRDataReceived(KDCData kdcData) {

    }


    @Override
    public void POSDataReceived(final KPOSData pData) {
        if (pData != null) {
            switch (pData.GetEventCode()) {
                case KPOSConstants.EVT_NFC_CARD_TAPPED:
                    Looper.prepare();
                    HandleNFCCardReadEvent(pData);
                    Looper.loop();
                    break;
                case KPOSConstants.EVT_BARCODE_SCANNED:
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            HandleBarcodeScannedEvent(pData);
                        }
                    });
                    break;
                case KPOSConstants.EVT_CARD_SWIPED: // an user swiped a card, and EMSKDC500 read it successfully
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            HandleCardSwipedEvent(pData);
                        }
                    });
                    break;
                case KPOSConstants.EVT_CARD_SWIPED_ENCRYPTED: // an user swiped a card, and EMSKDC500 read it successfully and encrypt
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            HandleCardSwipedEncryptedEvent(pData);
                        }
                    });
                    break;
//                case KPOSConstants.EVT_VALUE_ENTERED:
//                    HandleValueEnteredEvent(pData);
//                    break;
//                case KPOSConstants.EVT_CARD_READ_FAILED: // an user swiped a card, but EMSKDC500 could not read it successfully
//                    HandleCardReadFailedEvent();
//                    break;
//                case KPOSConstants.EVT_CANCELLED_CARD_READ: // an user pressed CANCEL button on EMSKDC500 during the card read mode
//                    HandleCardReadCancelledEvent();
//                    break;
//                case KPOSConstants.EVT_TIMEOUT_CARD_READ: // an user did not swipe a card before time-out occurred
//                    HandleCardReadTimeoutEvent();
//                    break;
//                case KPOSConstants.EVT_CANCELLED:
//                    HandleCancelledEvent();
//                    break;
//                case KPOSConstants.EVT_TIMEOUT:
//                    HandleTimeoutEvent();
//                    break;
//                case KPOSConstants.EVT_PINBLOCK_GENERATED: // an user entered PIN, and EMSKDC500 generated PIN Block successfully
//                    HandlePinblockGeneratedEvent(pData);
//                    break;
//                case KPOSConstants.EVT_PINBLOCK_GENERATION_FAILED: // an user entered PIN, but EMSKDC500 could not generate PIN Block successfully
//                    HandlePinblockGenerationFailedEvent();
//                    break;
//                case KPOSConstants.EVT_CANCELLED_PIN_ENTRY: // an user pressed CANCEL button on EMSKDC500 during the pin entry mode
//                    HandlePinEntryCancelledEvent();
//                    break;
//                case KPOSConstants.EVT_TIMEOUT_PIN_ENTRY: // an user did not enter PIN completely before time-out occurred
//                    HandlePinEntryTimeoutEvent();
//                    break;
//
//                case KPOSConstants.EVT_TRANSACTION_STATE_ENTERED:
//                case KPOSConstants.EVT_TRANSACTION_STATE_EXITED:
//                    HandleTransactionStateChangeEvent(pData);
//                    break;
//
//                case KPOSConstants.EVT_EMV_CARD_INSERTED:
//                    HandleCardInsertedEvent(pData);
//                    break;
//                case KPOSConstants.EVT_EMV_TRANSACTION_REQUESTED:
//                    HandleEMVTransactionRequestedEvent(pData);
//                    break;
//                case KPOSConstants.EVT_EMV_TRANSACTION_REVERSED:
//                    HandleEMVTransactionReversedEvent(pData);
//                    break;
//                case KPOSConstants.EVT_EMV_TRANSACTION_CONFIRMED:
//                    HandleEMVTransactionConfirmedEvent(pData);
//                    break;
//                case KPOSConstants.EVT_EMV_TRANSACTION_ENDED:
//                    HandleEMVTransactionEndedEvent(pData);
//                    break;

                default:
                    break;
            }
        }
    }

    private void HandleBarcodeScannedEvent(KPOSData pData) {
        try {
            scannedData = new String(pData.GetBarcodeBytes());
            scannerCallBack.scannerWasRead(scannedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String cleanTrack(String track) {
        String c = track;//"hjdg$h&jk8^i0ssh6";
        Pattern pt = Pattern.compile("[^a-zA-Z0-9;/%?-_.,*= ]");
        Matcher match = pt.matcher(c);
        while (match.find()) {
            String s = match.group();
            c = c.replaceAll("\\" + s, "");
        }
        return c;
    }

    private void HandleCardSwipedEvent(KPOSData pData) {

        String track1 = cleanTrack(pData.GetTrack1());

        String track2 = cleanTrack(pData.GetTrack2());
        String track3 = cleanTrack(pData.GetTrack3());
        Log.d("TRK1:", "[" + track1 + "]");
        Log.d("TRK2:", "[" + track2 + "]");

        CreditCardInfo creditCardInfo = new CreditCardInfo();
        boolean parsed = CardParser.parseCreditCard(activity, track1 + track2 + track3, creditCardInfo);
        scannerCallBack.cardWasReadSuccessfully(parsed, creditCardInfo);
    }

    private void HandleCardSwipedEncryptedEvent(KPOSData pData) {
        short encryptionType = pData.GetEncryptionType();
        String deviceSerialNumber = pData.GetDeviceSerialNumber();

        String ksn = pData.GetCardDataKSN();

        short unencryptedTrack1Length = pData.GetUnencryptedTrack1Length();
        short unencryptedTrack2Length = pData.GetUnencryptedTrack2Length();
        short unencryptedTrack3Length = pData.GetUnencryptedTrack3Length();

        short encryptedTrack1Length = pData.GetEncryptedTrack1Length();
        short encryptedTrack2Length = pData.GetEncryptedTrack2Length();
        short encryptedTrack3Length = pData.GetEncryptedTrack3Length();

        short track1DigestLength = pData.GetTrack1DigestLength();
        short track2DigestLength = pData.GetTrack2DigestLength();
        short track3DigestLength = pData.GetTrack3DigestLength();

        byte[] encryptedTrack1Data = pData.GetEncryptedTrack1Bytes();
        byte[] encryptedTrack2Data = pData.GetEncryptedTrack2Bytes();
        byte[] encryptedTrack3Data = pData.GetEncryptedTrack3Bytes();

        short digestType = pData.GetDigestType();

        byte[] track1Digest = pData.GetTrack1DigestBytes();
        byte[] track2Digest = pData.GetTrack2DigestBytes();
        byte[] track3Digest = pData.GetTrack3DigestBytes();


    }
}
