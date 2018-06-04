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
import android.util.Log;
import android.widget.Toast;

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

    private static final int PRINTER_ID = 0;
    private static final String DEBUG_TAG = "EMSGPrinterPT380";
    private static final int MAIN_QUERY_PRINTER_STATUS = 0xfe;

    private PService mPService = null;
    private PrinterServiceConnection conn = null;

    private EMSDeviceManager edm;

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
            Log.e(DEBUG_TAG, "result :" + String.valueOf(r));
            if (r != PrinterCom.ERROR_CODE.SUCCESS) {
                if (r == PrinterCom.ERROR_CODE.DEVICE_ALREADY_OPEN) {
                    Toast.makeText(activity, "Printer already connected!",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, PrinterCom.getErrorText(r),
                            Toast.LENGTH_LONG).show();
                }
            }

//            try {
//                mPService.queryPrinterStatus(PRINTER_ID, 500, MAIN_QUERY_PRINTER_STATUS);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
        }
    }

    @Override
    public void connect(final Context activity, int paperSize, boolean isPOSPrinter,
                        EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
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

                Log.d("TAG", action);
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
                if (!str.isEmpty()) Toast.makeText(activity, str, Toast.LENGTH_LONG).show();
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
        return false;
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes,
                                    boolean isFromHistory, boolean fromOnHold,
                                    EMVContainer emvContainer) {
        return false;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes,
                                    boolean isFromHistory, boolean fromOnHold) {
        return false;
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes,
                                    boolean isFromHistory, boolean fromOnHold) {
        return false;
    }

    @Override
    public boolean printPaymentDetails(String payID, int type, boolean isReprint, EMVContainer emvContainer) {
        setPaperWidth(42);
        printPaymentDetailsReceipt(payID, type, isReprint, 42, emvContainer);
        return true;
    }

    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        return false;
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment,
                                    String encodedSignature) {
        return false;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment,
                                          String encodedSignature) {
        return false;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c,
                                           boolean isPickup) {
        return false;
    }

    @Override
    public String printStationPrinter(List<Orders> orderProducts, String ordID,
                                      boolean cutPaper, boolean printHeader) {
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
    public void turnOnBCR() {

    }

    @Override
    public void turnOffBCR() {

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
        return false;
    }

    @Override
    public void printClockInOut(List<ClockInOut> clockInOuts, String clerkID) {

    }

    @Override
    public void printExpenseReceipt(ShiftExpense expense) {

    }
}