package drivers;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplitedOrder;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.miurasystems.miuralibrary.MPIConnectionDelegate;
import com.miurasystems.miuralibrary.api.executor.MiuraManager;
import com.miurasystems.miuralibrary.enums.BatteryData;
import com.miurasystems.miuralibrary.enums.DeviceStatus;
import com.miurasystems.miuralibrary.enums.M012Printer;
import com.miurasystems.miuralibrary.tlv.CardData;

import java.util.HashMap;
import java.util.List;

import drivers.miurasample.module.bluetooth.BluetoothConnectionListener;
import drivers.miurasample.module.bluetooth.BluetoothModule;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

/**
 * Created by guarionex on 1/17/17.
 */

public class EMSMiura extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, MPIConnectionDelegate {

    private int LINE_WIDTH = 32;
    private int PAPER_WIDTH;
    private EMSCallBack callBack;
    private Handler handler;
    private EMSDeviceManager edm;

    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, final EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.isPOSPrinter = isPOSPrinter;
        this.edm = edm;
        LINE_WIDTH = paperSize;
        switch (LINE_WIDTH) {
            case 32:
                PAPER_WIDTH = 408;
                break;
            case 48:
                PAPER_WIDTH = 576;
                break;
            case 69:
                PAPER_WIDTH = 832;// 5400
                break;
        }
        BluetoothModule.getInstance().openSession(myPref.getPrinterMACAddress(), new BluetoothConnectionListener() {
            @Override
            public void onConnected() {
                edm.driverDidConnectToDevice(EMSMiura.this, true);
            }

            @Override
            public void onDisconnected() {
                edm.driverDidNotConnectToDevice(EMSMiura.this, getString(R.string.fail_to_connect), true);
            }
        });
        MiuraManager.getInstance().setConnectionDelegate(this);
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        boolean didConnect = false;
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.isPOSPrinter = isPOSPrinter;
        this.edm = edm;
        LINE_WIDTH = paperSize;

        switch (LINE_WIDTH) {
            case 32:
                PAPER_WIDTH = 420;
                break;
            case 48:
                PAPER_WIDTH = 1600;
                break;
            case 69:
                PAPER_WIDTH = 300;// 5400
                break;
        }

        if (didConnect) {
            this.edm.driverDidConnectToDevice(this, false);
        } else {
            this.edm.driverDidNotConnectToDevice(this, null, false);
        }

        return didConnect;
    }

    @Override
    public void registerAll() {
        this.registerPrinter();
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
        return null;
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
    public void registerPrinter() {

    }

    @Override
    public void unregisterPrinter() {

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
    public void printReceiptPreview(SplitedOrder splitedOrder) {

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
    public void connected() {
        Log.d("Miura ", "connected");
    }

    @Override
    public void disconnected() {
        Log.d("Miura ", "disconnected");
    }

    @Override
    public void connectionState(boolean b) {

    }

    @Override
    public void onKeyPressed(int i) {

    }

    @Override
    public void onCardStatusChange(CardData cardData) {

    }

    @Override
    public void onDeviceStatusChange(DeviceStatus deviceStatus, String s) {

    }

    @Override
    public void onBatteryStatusChange(BatteryData batteryData) {

    }

    @Override
    public void onBarcodeScan(String s) {

    }

    @Override
    public void onPrintSledStatus(M012Printer m012Printer) {

    }
}
