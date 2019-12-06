package drivers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.util.Log;

import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;
import wangpos.sdk4.libbasebinder.Core;
import wangpos.sdk4.libbasebinder.Printer;

public class EMSAPT50 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    private int LINE_WIDTH = 32;
    private EMSDeviceManager edm;
    int[] status = new int[1];
    int ret = -1;

    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        this.edm = edm;
        myPref = new MyPreferences(activity);
        initApt50(false);
    }

    private void initApt50(boolean isAutoConnect) {
        aptPrinter = new Printer(activity);
        aptCore = new Core(activity);
        try {

            ret = aptPrinter.getPrinterStatus(status);
            if (ret == 0) {
                aptPrinter.printInit();
                aptPrinter.clearPrintDataCache();
                if (aptPrinter.printFinish() == 0) {
                    aptCore.led(0,0,0,0,0);
                    aptCore.led(1,0,0,0,1);
                    if (!isAutoConnect) {
                        this.edm.driverDidConnectToDevice(this, true, activity);
                    } else {
                        this.edm.driverDidConnectToDevice(this, false, activity);
                    }
                }
            }
        } catch (Exception e) {
            try {
                aptCore.led(0,0,0,1,1);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
            Log.e("APT50", e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize,
                               boolean isPOSPrinter, String portName, String portNumber) {
        this.activity = activity;
        this.edm = edm;
        myPref = new MyPreferences(activity);
        initApt50(true);
        return true;
    }

    @Override
    public void registerAll() {
        this.registerPrinter();
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold, EMVContainer emvContainer) {
        printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold, EMVContainer emvContainer) {
        printReceipt(order, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold) {
        return printTransaction(ordID, saleTypes, isFromHistory, fromOnHold, null);
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold) {
        return printTransaction(order, saleTypes, isFromHistory, fromOnHold, null);
    }

    @Override
    public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint, EMVContainer emvContainer) {
        printPaymentDetailsReceipt(payID, isFromMainMenu, isReprint, LINE_WIDTH, emvContainer);
        return true;
    }

    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        return printBalanceInquiry(values, LINE_WIDTH);
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        printConsignmentReceipt(myConsignment, encodedSignature, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        printConsignmentPickupReceipt(myConsignment, encodedSignature, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printRemoteStation(List<Orders> orders, String ordID) {
//        return printStationPrinterReceipt(orderProducts, ordID, LINE_WIDTH, cutPaper, printHeader);
        return false;
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
    public void turnOnBCR() {

    }

    @Override
    public void turnOffBCR() {

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
    public void printEndOfDayReport(String date, String clerk_id, boolean printDetails) {
        printEndOfDayReportReceipt(date, LINE_WIDTH, printDetails);

    }

    @Override
    public void registerPrinter() {
        edm.setCurrentDevice(this);
    }

    @Override
    public void unregisterPrinter() {
        edm.setCurrentDevice(null);
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
    public void printReceiptPreview(SplittedOrder splitedOrder) {
        try {
            super.printReceiptPreview(splitedOrder, LINE_WIDTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        return false;
    }

    @Override
    public void printClockInOut(List<ClockInOut> clockInOuts, String clerkID) {
        printClockInOut(clockInOuts, LINE_WIDTH, clerkID);
    }

    @Override
    public void printExpenseReceipt(ShiftExpense expense) {
        printExpenseReceipt(LINE_WIDTH, expense);
    }

//    public void leds(String str) {
//        Core mCore = new Core(activity);
//        try {
//            switch (str) {
//                case "blue":
//                    mCore.led(0, 0, 0, 0, 0);
//                    mCore.led(1, 0, 0, 0, 1);
//                    break;
//                case "red":
//                    mCore.led(0, 0, 0, 0, 0);
//                    mCore.led(0, 0, 0, 1, 1);
//                    break;
//                case "green":
//                    mCore.led(0, 0, 0, 0, 0);
//                    mCore.led(0, 0, 1, 0, 1);
//                    break;
//                case "yellow":
//                    mCore.led(0, 0, 0, 0, 0);
//                    mCore.led(0, 1, 0, 0, 1);
//                    break;
//                case "off":
//                    mCore.led(0, 0, 0, 0, 0);
//                    break;
//                case "on":
//                    mCore.led(1, 1, 1, 1, 1);
//                    break;
//            }
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }
    @Override
    public boolean printGiftReceipt(OrderProduct orderProduct,
                                    Order order,
                                    Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold) {
        return false;
    }
}
