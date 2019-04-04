package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.StarMicronics.jasura.JAException;
import com.android.emobilepos.R;
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

import com.hp.android.possdk.IJPOSInitCompleteCallBack;
import com.starmicronics.stario.StarIOPortException;

import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;

import jpos.JPOSApp;
import jpos.JposException;
import jpos.POSPrinter;
import main.EMSDeviceManager;

import static jpos.JposConst.JPOS_PN_ENABLED;

public class EMSHPEngageOnePrimePrinter extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, IJPOSInitCompleteCallBack {

    private int LINE_WIDTH = 32;
    private EMSDeviceManager edm;
    private boolean isConnected = false;
    private ProgressDialog myProgressDialog;
    private EMSDeviceDriver thisInstance;

    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        this.edm = edm;
        thisInstance = this;
        myPref = new MyPreferences(this.activity);
        JPOSApp.start(activity, (IJPOSInitCompleteCallBack) this);
        edm.driverDidConnectToDevice(thisInstance, false, activity);
//        new processConnectionAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        thisInstance = this;
        this.activity = activity;
        this.edm = edm;
        myPref = new MyPreferences(this.activity);
        JPOSApp.start(activity, (IJPOSInitCompleteCallBack) this);
        edm.driverDidConnectToDevice(thisInstance, false, activity);
        return true;//initializePrinter();
    }

    public boolean initializePrinter() {
       try{
           JPOSApp.start(activity, (IJPOSInitCompleteCallBack) this);
           if (hpPrinter == null) {
               hpPrinter = new POSPrinter();
           }
           isConnected = true;
       }catch(Exception e){Log.e("HPPrinter",e.toString());}

        if (isConnected) {
            edm.driverDidConnectToDevice(thisInstance, false, activity);
        } else {
            edm.driverDidNotConnectToDevice(thisInstance, "HP did not initialize", true, activity);
        }

        return isConnected;
    }


    @Override
    public void onComplete() {
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        hpPrinter = new POSPrinter();
        printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        hpPrinter = new POSPrinter();
        printReceipt(order, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
        hpPrinter = new POSPrinter();
        return printTransaction(ordID, saleTypes, isFromHistory, fromOnHold, null);
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
        hpPrinter = new POSPrinter();
        printReceipt(order, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, null);
        return true;
    }

    @Override
    public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint, EMVContainer emvContainer) {
        hpPrinter = new POSPrinter();
        printPaymentDetailsReceipt(payID, isFromMainMenu, isReprint, LINE_WIDTH, emvContainer);
        return true;
    }

    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        hpPrinter = new POSPrinter();
        return printBalanceInquiry(values, LINE_WIDTH);
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        hpPrinter = new POSPrinter();
        printConsignmentReceipt(myConsignment, encodedSignature, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        hpPrinter = new POSPrinter();
        printConsignmentPickupReceipt(myConsignment, encodedSignature, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        hpPrinter = new POSPrinter();
        printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);
        return true;
    }

    @Override
    public String printStationPrinter(List<Orders> orderProducts, String ordID, boolean cutPaper, boolean printHeader) {
        hpPrinter = new POSPrinter();
        return printStationPrinterReceipt(orderProducts, ordID, LINE_WIDTH, cutPaper, printHeader);
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        hpPrinter = new POSPrinter();
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
        hpPrinter = new POSPrinter();
        printReportReceipt(curDate, LINE_WIDTH);
        return true;
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
        hpPrinter = new POSPrinter();
        printShiftDetailsReceipt(LINE_WIDTH, shiftID);
    }

    @Override
    public void printEndOfDayReport(String date, String clerk_id, boolean printDetails) {
        hpPrinter = new POSPrinter();
        printEndOfDayReportReceipt(date, LINE_WIDTH, printDetails);
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

    @Override
    public void toggleBarcodeReader() {

    }

    @Override
    public void printReceiptPreview(SplittedOrder splitedOrder) {
        hpPrinter = new POSPrinter();
        try {
            setPaperWidth(LINE_WIDTH);
            super.printReceiptPreview(splitedOrder, LINE_WIDTH);
        } catch (JAException e) {
            e.printStackTrace();
        } catch (StarIOPortException e) {
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
        hpPrinter = new POSPrinter();
        super.printClockInOut(clockInOuts, LINE_WIDTH, clerkID);
    }

    @Override
    public void printExpenseReceipt(ShiftExpense expense) {
        hpPrinter = new POSPrinter();
        printExpenseReceipt(LINE_WIDTH, expense);
    }


    public class processConnectionAsync extends
            AsyncTask<Integer, String, String> {

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
        protected String doInBackground(Integer... params) {
            JPOSApp.start(activity, (IJPOSInitCompleteCallBack) this);
            if (hpPrinter == null) {
                hpPrinter = new POSPrinter();
            }
            try {
                hpPrinter.close();
                didConnect = true;
            } catch (JposException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
            if (didConnect) {
                edm.driverDidConnectToDevice(thisInstance, false, activity);
            } else {
                edm.driverDidNotConnectToDevice(thisInstance, msg, false, activity);
            }

        }
    }

}
