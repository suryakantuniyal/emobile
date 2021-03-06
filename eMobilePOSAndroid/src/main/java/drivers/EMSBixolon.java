package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.android.emobilepos.R;
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
import com.bxl.BXLConst;
import com.bxl.config.editor.BXLConfigLoader;
import com.starmicronics.stario.StarIOPortException;

import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import jpos.JposConst;
import jpos.JposException;
import jpos.POSPrinter;
import jpos.config.JposEntry;
import jpos.events.DirectIOEvent;
import jpos.events.DirectIOListener;
import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;
import jpos.events.OutputCompleteEvent;
import jpos.events.OutputCompleteListener;
import jpos.events.StatusUpdateEvent;
import jpos.events.StatusUpdateListener;
import main.EMSDeviceManager;

/**
 * Created by Guarionex on 5/3/2016.
 */
public class EMSBixolon extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate,
        ErrorListener, OutputCompleteListener, StatusUpdateListener, DirectIOListener {

    private int LINE_WIDTH = 32;
    private BXLConfigLoader bxlConfigLoader;
    private ProgressDialog myProgressDialog;
    private EMSDeviceDriver thisInstance;
    private EMSDeviceManager edm;
    private String logicalName;

    static String getPowerStateString(int powerState) {
        switch (powerState) {
            case JposConst.JPOS_PS_OFF_OFFLINE:
                return "OFFLINE";

            case JposConst.JPOS_PS_ONLINE:
                return "ONLINE";

            default:
                return "Unknown";
        }
    }

    static String getStatusString(int state) {
        switch (state) {
            case JposConst.JPOS_S_BUSY:
                return "JPOS_S_BUSY";

            case JposConst.JPOS_S_CLOSED:
                return "JPOS_S_CLOSED";

            case JposConst.JPOS_S_ERROR:
                return "JPOS_S_ERROR";

            case JposConst.JPOS_S_IDLE:
                return "JPOS_S_IDLE";

            default:
                return "Unknown State";
        }
    }

    private void init(Context activity, int paperSize, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        thisInstance = this;
        LINE_WIDTH = paperSize;

        bxlConfigLoader = new BXLConfigLoader(activity);
        try {
            bxlConfigLoader.openFile();
        } catch (Exception e) {
            e.printStackTrace();
            bxlConfigLoader.newFile();
        }
        bixolonPrinter = new POSPrinter();
        bixolonPrinter.addErrorListener(this);
        bixolonPrinter.addOutputCompleteListener(this);
        bixolonPrinter.addStatusUpdateListener(this);
        bixolonPrinter.addDirectIOListener(this);
        if (myPref.getPrinterName().contains("SPP-R2")) {
            LINE_WIDTH = 32;
        } else {
            LINE_WIDTH = 48;
        }
    }

    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        init(activity, paperSize, edm);
        new processConnectionAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        boolean didConnect;
        init(activity, paperSize, edm);
        didConnect = connectPrinter();
        if (didConnect) {
            this.edm.driverDidConnectToDevice(thisInstance, false, activity);
        } else {

            this.edm.driverDidNotConnectToDevice(thisInstance, null, false, activity);
        }
        return didConnect;
    }

    private boolean connectPrinter() {
        try {
            for (Object entry : bxlConfigLoader.getEntries()) {
                JposEntry jposEntry = (JposEntry) entry;
                bxlConfigLoader.removeEntry(jposEntry.getLogicalName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logicalName = myPref.getPrinterName();
        String strProduce;
        if (getProductName().length() == 0) {
            strProduce = logicalName;
        } else {
            strProduce = getProductName();
        }

        try {

            bxlConfigLoader.addEntry(myPref.getPrinterName(),
                    BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                    strProduce,
                    BXLConfigLoader.DEVICE_BUS_BLUETOOTH, myPref.getPrinterMACAddress().substring(3));

            bxlConfigLoader.saveFile();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        try {
            bixolonPrinter.open(myPref.getPrinterName());
            bixolonPrinter.claim(0);
            bixolonPrinter.setDeviceEnabled(true);
            bixolonPrinter.close();
        } catch (JposException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void directIOOccurred(DirectIOEvent directIOEvent) {

    }

    private String getProductName() {
        String productName = null;

        if ((logicalName.contains("SPP-R200II"))) {
            if (logicalName.substring(10, 11).equals("I")) {
                productName = BXLConst.SPP_R200III;
            } else {
                productName = BXLConst.SPP_R200II;
            }
        } else if ((logicalName.contains("SPP-R210"))) {
            productName = BXLConst.SPP_R210;
        } else if ((logicalName.contains("SPP-R310"))) {
            productName = BXLConst.SPP_R310;
        } else if ((logicalName.contains("SPP-R300"))) {
            productName = BXLConst.SPP_R300;
        } else if ((logicalName.contains("SPP-R400"))) {
            productName = BXLConst.SPP_R400;
        } else if ((logicalName.contains("SPP-R200"))) {
            productName = BXLConst.SPP_R300;
        }

        return productName;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
//        setPaperWidth(LINE_WIDTH);
        printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        printReceipt(order, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
//        setPaperWidth(LINE_WIDTH);
        return printTransaction(ordID, saleTypes, isFromHistory, fromOnHold, null);
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
        printReceipt(order, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, null);
        return true;
    }

    @Override
    public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint, EMVContainer emvContainer) {
//        setPaperWidth(LINE_WIDTH);
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
    public void registerAll() {
        this.registerPrinter();
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
        super.printHeader(LINE_WIDTH);
    }

    @Override
    public void printFooter() {
        super.printFooter(LINE_WIDTH);

    }

    @Override
    public boolean isUSBConnected() {
        return false;
    }

//    @Override
//    public void printReceiptPreview(View view) {
////        setPaperWidth(LINE_WIDTH);
//        Bitmap bitmap = loadBitmapFromView(view);
//        try {
//            super.printReceiptPreview(bitmap, LINE_WIDTH);
//        } catch (JAException e) {
//            e.printStackTrace();
//        } catch (StarIOPortException e) {
//            e.printStackTrace();
//        }
//
//    }

    @Override
    public void toggleBarcodeReader() {

    }

    @Override
    public void printReceiptPreview(SplittedOrder splitedOrder) {
        try {
            setPaperWidth(LINE_WIDTH);
//            Bitmap bitmap = loadBitmapFromView(view);
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

    @Override
    public void errorOccurred(ErrorEvent errorEvent) {
        Log.d("errorOccurred", errorEvent.toString());

    }

    @Override
    public void outputCompleteOccurred(OutputCompleteEvent outputCompleteEvent) {
        Log.d("outputCompleteOccurred", outputCompleteEvent.toString());
    }

    @Override
    public void statusUpdateOccurred(StatusUpdateEvent statusUpdateEvent) {
        Log.d("statusUpdateOccurred", statusUpdateEvent.toString());

    }

    public class processConnectionAsync extends AsyncTask<Integer, String, Boolean> {
        String msg = "";
        boolean didConnect = false;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(activity.getString(R.string.progress_connecting_printer));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            return connectPrinter();
//            try {
//                for (Object entry : bxlConfigLoader.getEntries()) {
//                    JposEntry jposEntry = (JposEntry) entry;
//                    bxlConfigLoader.removeEntry(jposEntry.getLogicalName());
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            logicalName = myPref.getPrinterName();
//            String strProduce;
//            if (setProductName(logicalName).length() == 0) {
//                strProduce = logicalName;
//            } else {
//                strProduce = setProductName(logicalName);
//            }
//
//            try {
//                bxlConfigLoader.addEntry(myPref.getPrinterName(),
//                        BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
//                        strProduce,
//                        BXLConfigLoader.DEVICE_BUS_BLUETOOTH, myPref.getPrinterMACAddress());
//
//                bxlConfigLoader.saveFile();
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//            return true;
        }

        @Override
        protected void onPostExecute(Boolean didConnect) {
            myProgressDialog.dismiss();
            if (didConnect) {
                edm.driverDidConnectToDevice(thisInstance, true, activity);
            } else {
                edm.driverDidNotConnectToDevice(thisInstance, msg, true, activity);
            }
        }
    }
    @Override
    public boolean printGiftReceipt(OrderProduct orderProduct,
                                    Order order,
                                    Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold) {
        return false;
    }
}
