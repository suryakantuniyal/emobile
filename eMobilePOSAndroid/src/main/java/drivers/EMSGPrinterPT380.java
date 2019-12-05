package drivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.android.emobilepos.BuildConfig;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.printer.aidl.PService;
import com.printer.command.PrinterCom;
import com.printer.io.PortParameters;
import com.printer.io.PrinterDevice;
import com.printer.service.PrinterPrintService;

import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

/**
 * Created by Luis Camayd on 6/1/2018.
 */
public class EMSGPrinterPT380 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    private final int LINE_WIDTH = 48;
    public static final int PRINTER_ID = 0;
    private static final int MAIN_QUERY_PRINTER_STATUS = 0xfe;

    private PrinterServiceConnection conn = null;
    private EMSDeviceManager edm;
    private boolean isAutoConnect;

    class PrinterServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPService = PService.Stub.asInterface(service);

            int rel = 0;
            try {
                rel = mPService.openPort(PRINTER_ID, PortParameters.BLUETOOTH,
                        myPref.getPrinterMACAddress(), 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            PrinterCom.ERROR_CODE r = PrinterCom.ERROR_CODE.values()[rel];
            if (r != PrinterCom.ERROR_CODE.SUCCESS) {
                if (r == PrinterCom.ERROR_CODE.DEVICE_ALREADY_OPEN) {
                    Toast.makeText(activity, "Printer already connected!",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, PrinterCom.getErrorText(r),
                            Toast.LENGTH_LONG).show();
                }
                edm.driverDidNotConnectToDevice(EMSGPrinterPT380.this,
                        "", !isAutoConnect, activity);
            } else {
                edm.driverDidConnectToDevice(EMSGPrinterPT380.this,
                        !isAutoConnect, activity);
            }
        }
    }

    @Override
    public void connect(final Context activity, int paperSize, boolean isPOSPrinter,
                        EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        isAutoConnect = false;
        registerBroadcast();
        conn = new PrinterServiceConnection();
        Intent intent = new Intent(activity, PrinterPrintService.class);
        activity.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize,
                               boolean isPOSPrinter, String portName, String portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        isAutoConnect = true;
        registerBroadcast();
        conn = new PrinterServiceConnection();
        Intent intent = new Intent(activity, PrinterPrintService.class);
        activity.bindService(intent, conn, Context.BIND_AUTO_CREATE);

        return true;
    }

    private void registerBroadcast() {
        final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String str = "";

                // PrinterCom.ACTION_DEVICE_REAL_STATUS "For broadcast" IntentFilter
                if (action.equals(PrinterCom.ACTION_DEVICE_REAL_STATUS)) {
                    // "Business logic request code, where to query what to do"
                    int requestCode = intent.getIntExtra(
                            PrinterCom.EXTRA_PRINTER_REQUEST_CODE, -1);
                    // "Judgment request code, yes business operation"
                    if (requestCode == MAIN_QUERY_PRINTER_STATUS) {

                        int status = intent.getIntExtra(
                                PrinterCom.EXTRA_PRINTER_REAL_STATUS, 16);
                        if (status == PrinterCom.STATE_NO_ERR) {
                            str = "The printer is normal.";
                        } else {
                            str = "Printer: ";
                            if ((byte) (status & PrinterCom.STATE_OFFLINE) > 0) {
                                str += "Offline.";
                            }
                            if ((byte) (status & PrinterCom.STATE_PAPER_ERR) > 0) {
                                str += "Paper out!";
                            }
                            if ((byte) (status & PrinterCom.STATE_COVER_OPEN) > 0) {
                                str += "Printer cover is open!";
                            }
                            if ((byte) (status & PrinterCom.STATE_ERR_OCCURS) > 0) {
                                str += "Printer error!";
                            }
                            if ((byte) (status & PrinterCom.STATE_TIMES_OUT) > 0) {
                                str += "Query timeout!";
                            }
                        }
                    }
                } else if (action.equals(PrinterCom.ACTION_CONNECT_STATUS)) {
                    int type = intent.getIntExtra(PrinterPrintService.CONNECT_STATUS, 0);
                    switch (type) {
                        case PrinterDevice.STATE_CONNECTING:
                            str = "Connecting Printer...";
                            break;
                        case PrinterDevice.STATE_NONE:
                            str = "Printer Disconnected.";
                            break;
                        case PrinterDevice.STATE_VALID_PRINTER:
                            str = "Printer Connected!";
                            break;
                        case PrinterDevice.STATE_INVALID_PRINTER:
                            str = "Printer Invalid!";
                            break;
                    }
                }
                if (BuildConfig.DEBUG && !str.isEmpty())
                    Toast.makeText(activity, str, Toast.LENGTH_LONG).show();
            }
        };

        activity.registerReceiver(mBroadcastReceiver,
                new IntentFilter(PrinterCom.ACTION_CONNECT_STATUS));

        activity.registerReceiver(mBroadcastReceiver,
                new IntentFilter(PrinterCom.ACTION_DEVICE_REAL_STATUS));
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes,
                                    boolean isFromHistory, boolean fromOnHold,
                                    EMVContainer emvContainer) {
        setPaperWidth(LINE_WIDTH);
        printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes,
                                    boolean isFromHistory, boolean fromOnHold,
                                    EMVContainer emvContainer) {
        setPaperWidth(LINE_WIDTH);
        printReceipt(order, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes,
                                    boolean isFromHistory, boolean fromOnHold) {
        setPaperWidth(LINE_WIDTH);
        printTransaction(ordID, saleTypes, isFromHistory, fromOnHold, null);
        return true;
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes,
                                    boolean isFromHistory, boolean fromOnHold) {
        setPaperWidth(LINE_WIDTH);
        printReceipt(order, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, null);
        return true;
    }

    @Override
    public boolean printGiftReceipt(Order order, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
        return false;
    }

    @Override
    public boolean printPaymentDetails(String payID, int type, boolean isReprint, EMVContainer emvContainer) {
        setPaperWidth(LINE_WIDTH);
        printPaymentDetailsReceipt(payID, type, isReprint, LINE_WIDTH, emvContainer);
        return true;
    }

    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        setPaperWidth(LINE_WIDTH);
        return printBalanceInquiry(values, LINE_WIDTH);
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        setPaperWidth(LINE_WIDTH);
        printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        setPaperWidth(LINE_WIDTH);
        printConsignmentPickupReceipt(myConsignment, encodedSig, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        setPaperWidth(LINE_WIDTH);
        printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printRemoteStation(List<Orders> orders, String ordID) {
//        return printStationPrinterReceipt(orders, ordID, LINE_WIDTH, cutPaper, printHeader);
        return false;
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        setPaperWidth(LINE_WIDTH);
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
        setPaperWidth(LINE_WIDTH);
        printReportReceipt(curDate, LINE_WIDTH);
        return true;
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
        setPaperWidth(LINE_WIDTH);
        printShiftDetailsReceipt(LINE_WIDTH, shiftID);
    }

    @Override
    public void printEndOfDayReport(String curDate, String clerk_id, boolean printDetails) {
        setPaperWidth(LINE_WIDTH);
        printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);
    }

    @Override
    public void registerAll() {
        this.registerPrinter();
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
            setPaperWidth(LINE_WIDTH);
            super.printReceiptPreview(splitedOrder, LINE_WIDTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void salePayment(Payment payment, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void saleReversal(Payment payment, String originalTransactionId,
                             CreditCardInfo creditCardInfo) {

    }

    @Override
    public void refund(Payment payment, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void refundReversal(Payment payment, String originalTransactionId,
                               CreditCardInfo creditCardInfo) {

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
        super.printClockInOut(timeClocks, LINE_WIDTH, clerkID);
    }

    @Override
    public void printExpenseReceipt(ShiftExpense expense) {
        printExpenseReceipt(LINE_WIDTH, expense);
    }
}