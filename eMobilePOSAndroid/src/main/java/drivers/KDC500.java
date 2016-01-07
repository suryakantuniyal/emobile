package drivers;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;

import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.util.HashMap;
import java.util.List;

import koamtac.kdc.sdk.KDCBarcodeDataReceivedListener;
import koamtac.kdc.sdk.KDCConnectionListener;
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
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;

/**
 * Created by Guarionex on 12/8/2015.
 */
public class KDC500 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate,
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
    private KDC500 thisInstance;
    KDCReader kdcReader;


    private Handler handler;
    String scannedData = "";


    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);
        this.edm = edm;
        thisInstance = this;

        this.edm.driverDidConnectToDevice(thisInstance, false);
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

        this.edm.driverDidConnectToDevice(thisInstance, false);
        return true;
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
    public void printStationPrinter(List<Orders> orderProducts, String ordID) {

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
    public void printEndOfDayReport(String date, String clerk_id, boolean printDetails) {

    }


    @Override
    public void registerAll() {
        this.registerPrinter();
    }


    public void registerPrinter() {
        edm.currentDevice = this;
    }

    public void unregisterPrinter() {
        edm.currentDevice = null;
    }

    @Override
    public void loadCardReader(EMSCallBack callBack, boolean isDebitCard) {


    }


    @Override
    public void loadScanner(EMSCallBack callBack) {
        scannerCallBack = callBack;
        if (handler == null)
            handler = new Handler();
        if (callBack != null) {
            kdcReader = new KDCReader(this, null, null, null, null, this, this, false);
        }
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
    public void BarcodeDataReceived(KDCData kdcData) {
    }

    @Override
    public void ConnectionChanged(BluetoothDevice bluetoothDevice, int i) {

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
    public void NFCDataReceived(KDCData kdcData) {

    }


    @Override
    public void POSDataReceived(KPOSData pData)
    {
        if (pData != null) {
            switch (pData.GetEventCode()) {
                case KPOSConstants.EVT_BARCODE_SCANNED:
                    HandleBarcodeScannedEvent(pData);
                    break;
//                case KPOSConstants.EVT_NFC_CARD_TAPPED:
//                    HandleNFCCardReadEvent(pData);
//                    break;
//                case KPOSConstants.EVT_CARD_SWIPED: // an user swiped a card, and KDC500 read it successfully
//                    HandleCardSwipedEvent(pData);
//                    break;
//                case KPOSConstants.EVT_CARD_SWIPED_ENCRYPTED: // an user swiped a card, and KDC500 read it successfully and encrypt
//                    HandleCardSwipedEncryptedEvent(pData);
//                    break;
//                case KPOSConstants.EVT_VALUE_ENTERED:
//                    HandleValueEnteredEvent(pData);
//                    break;
//                case KPOSConstants.EVT_CARD_READ_FAILED: // an user swiped a card, but KDC500 could not read it successfully
//                    HandleCardReadFailedEvent();
//                    break;
//                case KPOSConstants.EVT_CANCELLED_CARD_READ: // an user pressed CANCEL button on KDC500 during the card read mode
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
//                case KPOSConstants.EVT_PINBLOCK_GENERATED: // an user entered PIN, and KDC500 generated PIN Block successfully
//                    HandlePinblockGeneratedEvent(pData);
//                    break;
//                case KPOSConstants.EVT_PINBLOCK_GENERATION_FAILED: // an user entered PIN, but KDC500 could not generate PIN Block successfully
//                    HandlePinblockGenerationFailedEvent();
//                    break;
//                case KPOSConstants.EVT_CANCELLED_PIN_ENTRY: // an user pressed CANCEL button on KDC500 during the pin entry mode
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

    private void HandleBarcodeScannedEvent(KPOSData pData)
    {
        try {
            scannedData = new String(pData.GetBarcodeBytes());
            scannerCallBack.scannerWasRead(scannedData);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
