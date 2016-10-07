package drivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Toast;

import com.StarMicronics.jasura.JAException;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.handpoint.api.Device;
import com.starmicronics.stario.StarIOPortException;
import com.uniquesecure.meposconnect.MePOS;
import com.uniquesecure.meposconnect.MePOSConnectionManager;
import com.uniquesecure.meposconnect.MePOSConnectionType;
import com.uniquesecure.meposconnect.MePOSException;

import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

/**
 * Created by guarionex on 10/6/16.
 */

public class EMSmePOS extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    private static final int LINE_WIDTH = 48;
    private EMSDeviceManager edm;
    protected static Device device;
    String msg = "Failed to connect";
    private static boolean connected = false;

    @Override
    public void connect(final Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        this.edm = edm;
        myPref = new MyPreferences(activity);
        init();
        if (mePOS.getConnectionManager().getConnectionStatus() == MePOSConnectionManager.STATUS_CONNECTED_USB) {
            edm.driverDidConnectToDevice(this, true);
        } else {
            edm.driverDidNotConnectToDevice(this, msg, true);
        }
    }


    @Override
    public boolean autoConnect(final Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter, String portName, String portNumber) {
        this.activity = activity;
        this.edm = edm;
        myPref = new MyPreferences(activity);
        init();
        if (mePOS.getConnectionManager().getConnectionStatus() == MePOSConnectionManager.STATUS_CONNECTED_USB) {
            edm.driverDidConnectToDevice(this, false);
        } else {
            edm.driverDidNotConnectToDevice(this, msg, false);
        }
        return true;
    }

    private void init() {
        mePOS = new MePOS(activity, MePOSConnectionType.USB);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().contains("ATTACHED")) {
                    mePOS = new MePOS(context, MePOSConnectionType.USB);
                    Toast.makeText(activity, "MePOS connected", Toast.LENGTH_SHORT).show();
                    connected = true;
                } else if (intent.getAction().contains("DETACHED")) {
                    mePOS.getConnectionManager().disconnect();
                    Toast.makeText(activity, "MePOS disconnected", Toast.LENGTH_SHORT).show();
                    connected = false;
                }
            }
        };
        activity.getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory, boolean fromOnHold) {
        printTransaction(ordID, type, isFromHistory, fromOnHold, null);
        return true;
    }


    @Override
    public boolean printPaymentDetails(String payID, int type, boolean isReprint, EMVContainer emvContainer) {
        printPaymentDetailsReceipt(payID, type, isReprint, LINE_WIDTH, emvContainer);
        return true;
    }

    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        return printBalanceInquiry(values, LINE_WIDTH);
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        printConsignmentPickupReceipt(myConsignment, encodedSig, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);
        return true;
    }

    @Override
    public String printStationPrinter(List<Orders> orders, String ordID, boolean cutPaper, boolean printHeader) {
        return printStationPrinterReceipt(orders, ordID, LINE_WIDTH, cutPaper, printHeader);
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        printOpenInvoicesReceipt(invID, LINE_WIDTH);
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

    }

    @Override
    public boolean printReport(String curDate) {
        printReportReceipt(curDate, LINE_WIDTH);
        return true;
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
        printShiftDetailsReceipt(LINE_WIDTH, shiftID);
    }

    @Override
    public void printEndOfDayReport(String curDate, String clerk_id, boolean printDetails) {
        printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);
    }

    @Override
    public void registerAll() {
        this.registerPrinter();
    }


    @Override
    public void registerPrinter() {
        edm.currentDevice = this;
    }

    @Override
    public void unregisterPrinter() {
        edm.currentDevice = null;
    }

    @Override
    public void loadCardReader(EMSCallBack callBack, boolean isDebitCard) {

    }

    @Override
    public void loadScanner(EMSCallBack _callBack) {

    }

    @Override
    public void releaseCardReader() {

    }

    @Override
    public void openCashDrawer() {

        new Thread(new Runnable() {
            public void run() {
                if (mePOS != null) {
                    try {
                        mePOS.openCashDrawer();
                    } catch (MePOSException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void printHeader() {

    }

    @Override
    public void printFooter() {

    }

    @Override
    public boolean isUSBConnected() {
        return connected;
    }

    @Override
    public void toggleBarcodeReader() {

    }

    @Override
    public void printReceiptPreview(View view) {
        try {
            Bitmap bitmap = loadBitmapFromView(view);
            super.printReceiptPreview(bitmap, LINE_WIDTH);
        } catch (JAException e) {
            e.printStackTrace();
        } catch (StarIOPortException e) {
            e.printStackTrace();
        }
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
}
