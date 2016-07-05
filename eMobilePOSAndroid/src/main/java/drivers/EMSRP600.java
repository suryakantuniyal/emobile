package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.partner.pt215.cashdrawer.CashDrawerApiContext;
import com.partner.pt215.cashdrawer.CashDrawerManage;

import java.util.HashMap;
import java.util.List;

import POSAPI.POSUSBAPI;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;


public class EMSRP600 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    private CashDrawerApiContext cashDrawerApiContext = new CashDrawerManage();
    private EMSDeviceManager edm;
    private EMSDeviceDriver thisInstance;
    private ProgressDialog myProgressDialog;
    private final int LINE_WIDTH = 32;
    private EMSCallBack callback;
    Handler handler;
    private POSUSBAPI posInterfaceAPI;


    private void setHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                callback.cardWasReadSuccessfully(true, (CreditCardInfo) msg.obj);
                return true;
            }
        });
    }

    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        posInterfaceAPI = new POSUSBAPI(activity);
        this.edm = edm;
        thisInstance = this;
        new processConnectionAsync().execute(0);
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        posInterfaceAPI = new POSUSBAPI(activity);
        this.edm = edm;
        thisInstance = this;

        int printerErrorCode = posInterfaceAPI.OpenDevice();//
        if (printerErrorCode == 0) {
            this.edm.driverDidConnectToDevice(thisInstance, false);
            return true;
        } else
            this.edm.driverDidNotConnectToDevice(thisInstance, null, false);

        return false;
    }

    public class processConnectionAsync extends AsyncTask<Integer, String, String> {

        String msg = "";
        boolean didConnect = false;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(activity.getString(R.string.connecting_device));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected String doInBackground(Integer... params) {

            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
            if (didConnect) {
                edm.driverDidConnectToDevice(thisInstance, true);
            } else {

                edm.driverDidNotConnectToDevice(thisInstance, msg, true);
            }
        }
    }

    @Override
    public void registerAll() {
        this.registerPrinter();
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory, boolean fromOnHold) {
        return true;
    }


    @Override
    public boolean printPaymentDetails(String payID, int type, boolean isReprint, EMVContainer emvContainer) {
        return true;
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
    public boolean printOpenInvoices(String invID) {
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
        return true;
    }

    @Override
    public void registerPrinter() {
        edm.currentDevice = this;
    }

    @Override
    public void unregisterPrinter() {
    }

    @Override
    public void loadCardReader(EMSCallBack callBack, boolean isDebitCard) {
        this.callback = callBack;
        if (handler == null)
            handler = new Handler();

    }


    @Override
    public void loadScanner(EMSCallBack _callBack) {
    }

    @Override
    public void releaseCardReader() {

    }

    @Override
    public void openCashDrawer() {
        cashDrawerApiContext.OpenCashDrawerA();
    }

    @Override
    public boolean isUSBConnected() {
        return false;
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

    @Override
    public void printStationPrinter(List<Orders> orderProducts, String ordID, boolean cutPaper) {
    }

    @Override
    public boolean printOnHold(Object onHold) {
        return false;
    }

    @Override
    public void printEndOfDayReport(String curDate, String clerk_id, boolean printDetails) {
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
    }

    @Override
    public void printHeader() {
        super.printHeader(LINE_WIDTH);
    }

    @Override
    public void printFooter() {
        super.printFooter(LINE_WIDTH);
    }

}
