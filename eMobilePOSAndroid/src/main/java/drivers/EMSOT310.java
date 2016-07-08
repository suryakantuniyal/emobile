package drivers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.internal.misccomm.misccommManager;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.magtek.mobile.android.libDynamag.MagTeklibDynamag;
import com.oem.barcode.BCRIntents;
import com.oem.barcode.BCRManager;

import java.util.HashMap;
import java.util.List;

import drivers.OT310.utils.BCRAppBroadcastReceiver;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

/**
 * Created by Guarionex on 12/8/2015.
 */
public class EMSOT310 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    private EMSCallBack scannerCallBack;
    private Encrypt encrypt;
    private CreditCardInfo cardManager;
    private EMSDeviceManager edm;
    private EMSOT310 thisInstance;
    private MagTeklibDynamag magTeklibDynamag;
    private Handler mReaderDataHandler;

    public static final int DEVICE_MESSAGE_CARDDATA_CHANGE = 3;
    public static final int DEVICE_STATUS_CONNECTED = 4;
    public static final int DEVICE_STATUS_DISCONNECTED = 5;
    public static final int DEVICE_STATUS_CONNECTED_SUCCESS = 0;
    public static final int DEVICE_STATUS_CONNECTED_FAIL = 1;
    public static final int DEVICE_STATUS_CONNECTED_PERMISSION_DENIED = 2;

    private Handler handler;
    String scannedData = "";
    private BCRAppBroadcastReceiver mBroadcastReceiver = new BCRAppBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra(BCRIntents.EXTRA_BCR_TYPE, -1);
            byte[] data = intent.getByteArrayExtra(BCRIntents.EXTRA_BCR_DATA);
            scannedData = new String(data);
            scannerCallBack.scannerWasRead(scannedData);

        }
    };


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

    private void initializeData() {
        magTeklibDynamag.clearCardData();
        scannedData = "";
    }

    private boolean openUSBPort() {
//        misccommManager.setMsrDefault();
        int ret = misccommManager.getDefault().setMsrEnable();
        return 0 == 0;

    }

    private boolean closeUSBPort() {
//        misccommManager.setMsrDefault();
        int ret = misccommManager.getDefault().setMsrDisable();
        return 0 == 0;

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
    public void printStationPrinter(List<Orders> orderProducts, String ordID, boolean cutPaper, boolean printHeader) {

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
    public void printShiftDetailsReport(String shiftID) {
     }


    @Override
    public void registerAll() {
        this.registerPrinter();
    }


    public void registerPrinter() {
        edm.currentDevice = this;
    }

    public void unregisterPrinter() {
        closeUSBPort();
        edm.currentDevice = null;

    }

    @Override
    public void loadCardReader(EMSCallBack callBack, boolean isDebitCard) {

        if (mReaderDataHandler == null) {
            mReaderDataHandler = new Handler(new MtHandlerCallback());
        }
        if (magTeklibDynamag == null) {
            magTeklibDynamag = new MagTeklibDynamag(activity, mReaderDataHandler);
        }
        initializeData();
        if (!magTeklibDynamag.isDeviceConnected()) {
            magTeklibDynamag.openDevice();
        }
        openUSBPort();

    }


    @Override
    public void loadScanner(EMSCallBack callBack) {

        scannerCallBack = callBack;
        if (handler == null)
            handler = new Handler();
        if (callBack != null) {
            BCRManager.getDefault().BCREnable();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BCRIntents.ACTION_NEW_DATA);
            activity.registerReceiver(mBroadcastReceiver, filter);
        }
    }


    @Override
    public void releaseCardReader() {
        try {
            if (magTeklibDynamag.isDeviceConnected()) {
                magTeklibDynamag.closeDevice();
            }
            misccommManager.getDefault().setMsrDisable();
            activity.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        return true;
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

    private class MtHandlerCallback implements Handler.Callback {
        public boolean handleMessage(Message msg) {

            boolean ret = false;
            Log.d("MSR Handler:" + msg.what, msg.obj.toString());
            switch (msg.what) {
                case DEVICE_MESSAGE_CARDDATA_CHANGE:
                    scannedData = (String) msg.obj;
                    if (scannerCallBack != null)
                        scannerCallBack.scannerWasRead(scannedData);
                    ret = true;
                    break;

                case DEVICE_STATUS_CONNECTED:
                    if (((Number) msg.obj).intValue() == DEVICE_STATUS_CONNECTED_SUCCESS) {
                        EMSOT310.this.edm.driverDidConnectToDevice(thisInstance, false);
                    } else if (((Number) msg.obj).intValue() == DEVICE_STATUS_CONNECTED_FAIL) {
                        EMSOT310.this.edm.driverDidNotConnectToDevice(thisInstance, getString(R.string.error_reading_card), true);
                    } else if (((Number) msg.obj).intValue() == DEVICE_STATUS_CONNECTED_PERMISSION_DENIED) {
                        EMSOT310.this.edm.driverDidNotConnectToDevice(thisInstance, getString(R.string.error_reading_card), true);
                    }

                    break;

                case DEVICE_STATUS_DISCONNECTED:
                    EMSOT310.this.edm.driverDidDisconnectFromDevice(thisInstance, true);
                    break;

                default:
                    ret = false;
                    break;

            }

            return ret;
        }
    }
}
