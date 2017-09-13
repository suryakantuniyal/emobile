package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.CardParser;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
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
import koamtac.kdc.sdk.KPOSData;
import main.EMSDeviceManager;

/**
 * Created by Guarionex on 12/8/2015.
 */
public class EMSKDC425 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate,
        KDCDataReceivedListener, // required for KDC Barcode Solution models
        KDCBarcodeDataReceivedListener, // required for KDC Barcode Solution models
        KDCGPSDataReceivedListener, // required for KDC Barcode Solution models
        KDCMSRDataReceivedListener, // required for KDC Barcode Solution models
        KDCNFCDataReceivedListener, // required for KDC Barcode Solution models
        KDCConnectionListener // required for all

{

    private static final String AES_KEY = "ThisIsMyTestKeyForAESEncryption.";
    private static EMSCallBack scannerCallBack;
    private static KDCReader kdc425Reader;
    private static Handler handler;
    String msg = "Failed to connect";
    private EMSDeviceManager edm;
    private EMSKDC425 thisInstance;
    private String scannedData = "";
    private BluetoothDevice btDev;
    private boolean isAutoConect = false;
    private CreditCardInfo cardInfo;
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
    private Runnable connectionCallBack = new Runnable() {
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            showConnectionMessage();
        }
    };
    private Runnable cardReadCallBack = new Runnable() {
        public void run() {
            scannerCallBack.cardWasReadSuccessfully(true, cardInfo);
        }
    };
    private Runnable runnableScannedData = new Runnable() {
        public void run() {
            try {
                if (scannerCallBack != null)
                    scannerCallBack.scannerWasRead(scannedData);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        thisInstance = this;
        isAutoConect = false;
        if (handler == null) {
            handler = new Handler();
        }
        if (kdc425Reader == null || !kdc425Reader.IsConnected()) {
            new processConnectionAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            showConnectionMessage();
        }
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        isAutoConect = true;
        thisInstance = this;

        boolean connected = connectKDC425();
        if (connected) {
            this.edm.driverDidConnectToDevice(thisInstance, false, activity);
        } else {
            this.edm.driverDidNotConnectToDevice(thisInstance, msg, false, activity);
        }

        return true;
    }

    private boolean connectKDC425() {
        if (kdc425Reader != null) {
            kdc425Reader.Disconnect();
            kdc425Reader.Dispose();
        }
        kdc425Reader = new KDCReader(this, null, null, this, null, this, false);
        if (!kdc425Reader.IsConnected() && KDCReader.GetAvailableDeviceList() != null && KDCReader.GetAvailableDeviceList().size() > 0) {
            btDev = KDCReader.GetAvailableDeviceList().get(0);
            if (kdc425Reader != null && btDev != null) {
                kdc425Reader.Connect(btDev);
            } else {
                return false;

            }
        }
        return kdc425Reader.IsConnected();
    }

    @Override
    public void NFCDataReceived(KDCData kdcData) {

    }

    private void HandleNFCCardReadEvent(KPOSData pData) {
        String nfcUID = pData.GetNFCUID();
        scannerCallBack.nfcWasRead(nfcUID);
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
        edm.setCurrentDevice(thisInstance);
        Global.btSwiper.setCurrentDevice(this);
    }

    public void unregisterPrinter() {
        if (kdc425Reader != null && kdc425Reader.IsConnected()) {
//            kdc425Reader.DisableNFC_POS();
//            kdc425Reader.DisableMSR_POS();
//            kdc425Reader.DisableCardReader_POS((short) (KPOSConstants.CARD_TYPE_MAGNETIC | KPOSConstants.CARD_TYPE_EMV_CONTACT));
        }
        edm.setCurrentDevice(null);
    }

    @Override
    public void loadCardReader(EMSCallBack callBack, boolean isDebitCard) {
        if (handler == null) {
            handler = new Handler();
        }
        if (kdc425Reader != null && kdc425Reader.IsConnected()) {
            scannerCallBack = callBack;
//            kdc425Reader.EnableMSR_POS();
//            kdc425Reader.EnableNFC_POS();
//            enableAES();
//            String SAMPLE_AES128_KEY = "1AAEAF7E7ABE338A942844F7F189BD49";
//            kdc425Reader.SetMSRDataEncryption(KDCConstants.MSRDataEncryption.AES);
//            kdc425Reader.SetAESKeyLength(KDCConstants.AESBitLengths.AES_128_BITS);
//            kdc425Reader.SetAESKey(SAMPLE_AES128_KEY);
//            kdc425Reader.EnableCardReader_POS((short) (KPOSConstants.CARD_TYPE_MAGNETIC | KPOSConstants.CARD_TYPE_EMV_CONTACT));
        }
        handler.post(doUpdateDidConnect);
    }

    private void enableAES() {
        if (kdc425Reader == null || !kdc425Reader.IsConnected()) {
            return;
        }

        // Change encryption key in KDC device - Optional
        kdc425Reader.SetAESKey(AES_KEY);
        kdc425Reader.SetAESKeyLength(KDCConstants.AESBitLengths.AES_128_BITS);

        // Enable KDC device to encrypt MSR data by AES algorithms. - Optional
        kdc425Reader.SetMSRDataEncryption(KDCConstants.MSRDataEncryption.AES);

        // Get AES Key and Length from connected kdc device
        String key = kdc425Reader.GetAESKey();
        KDCConstants.AESBitLengths length = kdc425Reader.GetAESKeyLength();

        // Enable KDCReader to decrypt MSR data using AES Key from KDC Device.
        if (key != null && kdc425Reader.EnableDecryptMSRData(true, key, length)) {
            ((Activity)activity).runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(activity, "Encrypted MSR Data from KDC device will be decrypted from now.", Toast.LENGTH_LONG).show();
                }
            });
        } else {
//            Log.d(TAG, "Error");
        }

    }

    private void showConnectionMessage() {
        if (kdc425Reader != null && kdc425Reader.IsConnected()) {
            edm.driverDidConnectToDevice(thisInstance, !isAutoConect, activity);
        } else {
            edm.driverDidNotConnectToDevice(thisInstance, msg, !isAutoConect, activity);
        }
    }

    @Override
    public void loadScanner(EMSCallBack callBack) {
        scannerCallBack = callBack;
//        kdc425Reader.EnableMSR_POS();
//        kdc425Reader.EnableNFC_POS();
//        kdc425Reader.EnableCardReader_POS((short) (KPOSConstants.CARD_TYPE_MAGNETIC | KPOSConstants.CARD_TYPE_EMV_CONTACT));
        if (handler == null)
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
//
//    @Override
//    public void printReceiptPreview(View view) {
//
//    }

    @Override
    public void printReceiptPreview(SplittedOrder splitedOrder) {

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
    public void submitSignature() {

    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void printClockInOut(List<ClockInOut> timeClocks, String clerkID) {

    }


    @Override
    public void BarcodeDataReceived(KDCData kdcData) {
        String data = kdcData.GetData();
    }

    @Override
    public void ConnectionChanged(BluetoothDevice bluetoothDevice, int state) {
        switch (state) {
            case KDCConstants.CONNECTION_STATE_CONNECTED:
                Log.d("KDCReader", "Connected");
                enableAES();
                if (!isAutoConect) {
                    handler.post(connectionCallBack);
                } else {
                    showConnectionMessage();
                }
                break;

            case KDCConstants.CONNECTION_STATE_CONNECTING:
            case KDCConstants.CONNECTION_STATE_NONE:
                Log.d("KDCReader", "Connection Closed");
                break;
            case KDCConstants.CONNECTION_STATE_LOST:
                Log.d("KDCReader", "Connection Lost");
                break;
            case KDCConstants.CONNECTION_STATE_FAILED:
                if (!isAutoConect) {
                    handler.post(connectionCallBack);
                } else {
                    showConnectionMessage();
                }
                break;
            case KDCConstants.CONNECTION_STATE_LISTEN:
                break;
        }
    }

    @Override
    public void DataReceived(KDCData kdcData) {
        if (kdcData.GetDataType() == KDCConstants.DataType.BARCODE) {
            scannedData = kdcData.GetData();
            if (handler != null) {
                handler.post(runnableScannedData);
            }
//            scannerCallBack.scannerWasRead(kdcData.GetData());
        } else {
            if (!TextUtils.isEmpty(kdcData.GetData())) {
                cardInfo = new CreditCardInfo();
                CardParser.parseCreditCard(activity, kdcData.GetData(), cardInfo);
                handler.post(cardReadCallBack);
            }
//            String SAMPLE_AES128_KEY = "1AAEAF7E7ABE338A942844F7F189BD49";
//            String data = "";
//            data = kdcData.GetData();
//            byte[] IV = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//
//            String iv = String.valueOf(IV);
//            byte[] decryptedData_hex = new byte[0];
//            try {
//                decryptedData_hex = KPOSHSM.decryptWithAES(kdcData.GetDataBytes(), this.hexStringToByteArray(SAMPLE_AES128_KEY));
//                data = new String(decryptedData_hex);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        byte[] decryptedTrack2Data = new byte[unencryptedTrack2Length];
//        System.arraycopy(decryptedData_hex, 4, decryptedTrack2Data, 0, unencryptedTrack2Length); // first 4 bytes are random data
//        DisplayString(I, "decrypted track2 data [ " + new String(decryptedTrack2Data, "UTF-8") + " ]");
        }
    }

    public byte[] hexStringToByteArray(String s) {

        if (s == null) return null;

        byte[] result = new byte[s.length() / 2];
        for (int i = 0; i < result.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            result[i] = (byte) v;
        }
        return result;
    }

    @Override
    public void GPSDataReceived(KDCData kdcData) {

    }

    @Override
    public void MSRDataReceived(KDCData kdcData) {
        cardInfo = new CreditCardInfo();
        CardParser.parseCreditCard(activity, kdcData.GetMSRData(), cardInfo);
        if (handler != null && cardReadCallBack != null) {
            handler.post(cardReadCallBack);
        }
    }


//    @Override
//    public void POSDataReceived(final KPOSData pData) {
//        if (pData != null) {
//            switch (pData.GetEventCode()) {
//                case KPOSConstants.EVT_NFC_CARD_TAPPED:
//                    Looper.prepare();
//                    HandleNFCCardReadEvent(pData);
//                    Looper.loop();
//                    break;
//                case KPOSConstants.EVT_BARCODE_SCANNED:
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            HandleBarcodeScannedEvent(pData);
//                        }
//                    });
//                    break;
//                case KPOSConstants.EVT_CARD_SWIPED: // an user swiped a card, and EMSKDC425 read it successfully
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            HandleCardSwipedEvent(pData);
//                        }
//                    });
//                    break;
//                case KPOSConstants.EVT_CARD_SWIPED_ENCRYPTED: // an user swiped a card, and EMSKDC425 read it successfully and encrypt
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            HandleCardSwipedEncryptedEvent(pData);
//                        }
//                    });
//                    break;
////                case KPOSConstants.EVT_VALUE_ENTERED:
////                    HandleValueEnteredEvent(pData);
////                    break;
////                case KPOSConstants.EVT_CARD_READ_FAILED: // an user swiped a card, but EMSKDC425 could not read it successfully
////                    HandleCardReadFailedEvent();
////                    break;
////                case KPOSConstants.EVT_CANCELLED_CARD_READ: // an user pressed CANCEL button on EMSKDC425 during the card read mode
////                    HandleCardReadCancelledEvent();
////                    break;
////                case KPOSConstants.EVT_TIMEOUT_CARD_READ: // an user did not swipe a card before time-out occurred
////                    HandleCardReadTimeoutEvent();
////                    break;
////                case KPOSConstants.EVT_CANCELLED:
////                    HandleCancelledEvent();
////                    break;
////                case KPOSConstants.EVT_TIMEOUT:
////                    HandleTimeoutEvent();
////                    break;
////                case KPOSConstants.EVT_PINBLOCK_GENERATED: // an user entered PIN, and EMSKDC425 generated PIN Block successfully
////                    HandlePinblockGeneratedEvent(pData);
////                    break;
////                case KPOSConstants.EVT_PINBLOCK_GENERATION_FAILED: // an user entered PIN, but EMSKDC425 could not generate PIN Block successfully
////                    HandlePinblockGenerationFailedEvent();
////                    break;
////                case KPOSConstants.EVT_CANCELLED_PIN_ENTRY: // an user pressed CANCEL button on EMSKDC425 during the pin entry mode
////                    HandlePinEntryCancelledEvent();
////                    break;
////                case KPOSConstants.EVT_TIMEOUT_PIN_ENTRY: // an user did not enter PIN completely before time-out occurred
////                    HandlePinEntryTimeoutEvent();
////                    break;
////
////                case KPOSConstants.EVT_TRANSACTION_STATE_ENTERED:
////                case KPOSConstants.EVT_TRANSACTION_STATE_EXITED:
////                    HandleTransactionStateChangeEvent(pData);
////                    break;
////
////                case KPOSConstants.EVT_EMV_CARD_INSERTED:
////                    HandleCardInsertedEvent(pData);
////                    break;
////                case KPOSConstants.EVT_EMV_TRANSACTION_REQUESTED:
////                    HandleEMVTransactionRequestedEvent(pData);
////                    break;
////                case KPOSConstants.EVT_EMV_TRANSACTION_REVERSED:
////                    HandleEMVTransactionReversedEvent(pData);
////                    break;
////                case KPOSConstants.EVT_EMV_TRANSACTION_CONFIRMED:
////                    HandleEMVTransactionConfirmedEvent(pData);
////                    break;
////                case KPOSConstants.EVT_EMV_TRANSACTION_ENDED:
////                    HandleEMVTransactionEndedEvent(pData);
////                    break;
//
//                default:
//                    break;
//            }
//        }
//    }

//    private void HandleBarcodeScannedEvent(KPOSData pData) {
//        try {
//            scannedData = new String(pData.GetBarcodeBytes());
//            scannerCallBack.scannerWasRead(scannedData);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

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
            return connectKDC425();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            boolean isDestroyed = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (((Activity)activity).isDestroyed()) {
                    isDestroyed = true;
                }
            }
            if (!((Activity)activity).isFinishing() && !isDestroyed && myProgressDialog.isShowing()) {
                myProgressDialog.dismiss();
            }
        }
    }

//    private void HandleCardSwipedEvent(KPOSData pData) {
//
//        String track1 = cleanTrack(pData.GetTrack1());
//
//        String track2 = cleanTrack(pData.GetTrack2());
//        String track3 = cleanTrack(pData.GetTrack3());
//        Log.d("TRK1:", "[" + track1 + "]");
//        Log.d("TRK2:", "[" + track2 + "]");
//
//        CreditCardInfo creditCardInfo = new CreditCardInfo();
//        boolean parsed = CardParser.parseCreditCard(activity, track1 + track2 + track3, creditCardInfo);
//        scannerCallBack.cardWasReadSuccessfully(parsed, creditCardInfo);
//    }

//    private void HandleCardSwipedEncryptedEvent(KPOSData pData) {
//        short encryptionType = pData.GetEncryptionType();
//        String deviceSerialNumber = pData.GetDeviceSerialNumber();
//
//        String ksn = pData.GetCardDataKSN();
//
//        short unencryptedTrack1Length = pData.GetUnencryptedTrack1Length();
//        short unencryptedTrack2Length = pData.GetUnencryptedTrack2Length();
//        short unencryptedTrack3Length = pData.GetUnencryptedTrack3Length();
//
//        short encryptedTrack1Length = pData.GetEncryptedTrack1Length();
//        short encryptedTrack2Length = pData.GetEncryptedTrack2Length();
//        short encryptedTrack3Length = pData.GetEncryptedTrack3Length();
//
//        short track1DigestLength = pData.GetTrack1DigestLength();
//        short track2DigestLength = pData.GetTrack2DigestLength();
//        short track3DigestLength = pData.GetTrack3DigestLength();
//
//        byte[] encryptedTrack1Data = pData.GetEncryptedTrack1Bytes();
//        byte[] encryptedTrack2Data = pData.GetEncryptedTrack2Bytes();
//        byte[] encryptedTrack3Data = pData.GetEncryptedTrack3Bytes();
//
//        short digestType = pData.GetDigestType();
//
//        byte[] track1Digest = pData.GetTrack1DigestBytes();
//        byte[] track2Digest = pData.GetTrack2DigestBytes();
//        byte[] track3Digest = pData.GetTrack3DigestBytes();
//
//
//    }
}
