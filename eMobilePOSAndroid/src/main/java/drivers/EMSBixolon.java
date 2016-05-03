package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;

import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.bxl.BXLConst;
import com.bxl.config.editor.BXLConfigLoader;

import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import jpos.POSPrinter;
import jpos.config.JposEntry;
import main.EMSDeviceManager;

/**
 * Created by Guarionex on 5/3/2016.
 */
public class EMSBixolon extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    private int LINE_WIDTH = 32;
    private int PAPER_WIDTH;
    private String portSettings, portName;
    private BXLConfigLoader bxlConfigLoader;
    private POSPrinter posPrinter;
    private EMSCallBack callBack;
    private Handler handler;// = new Handler();
    private ProgressDialog myProgressDialog;
    private EMSDeviceDriver thisInstance;
    private String portNumber = "";
    private EMSDeviceManager edm;
    private String logicalName;

    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        thisInstance = this;
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
        portName = myPref.getPrinterMACAddress();
        portNumber = myPref.getStarPort();
        bxlConfigLoader = new BXLConfigLoader(activity);
        try {
            bxlConfigLoader.openFile();
        } catch (Exception e) {
            e.printStackTrace();
            bxlConfigLoader.newFile();
        }
        posPrinter = new POSPrinter(activity);
        new processConnectionAsync().execute(0);
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        boolean didConnect = false;
        bxlConfigLoader = new BXLConfigLoader(activity);
        try {
            bxlConfigLoader.openFile();
        } catch (Exception e) {
            e.printStackTrace();
            bxlConfigLoader.newFile();
        }
        posPrinter = new POSPrinter(activity);
        return didConnect;
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
            if (setProductName(logicalName).length() == 0) {
                strProduce = logicalName;
            } else {
                strProduce = setProductName(logicalName);
            }

            try {
                bxlConfigLoader.addEntry(myPref.getPrinterName(),
                        BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                        strProduce,
                        BXLConfigLoader.DEVICE_BUS_BLUETOOTH, myPref.getPrinterMACAddress());

                bxlConfigLoader.saveFile();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean didConnect) {
            myProgressDialog.dismiss();
            if (didConnect) {
                edm.driverDidConnectToDevice(thisInstance, true);
            } else {
                edm.driverDidNotConnectToDevice(thisInstance, msg, true);
            }
        }
    }

    private String setProductName(String name) {
        String productName = null;

        if ((logicalName.indexOf("SPP-R200II") >= 0)) {
            if (logicalName.substring(10, 11).equals("I")) {
                productName = BXLConst.SPP_R200III;
            } else {
                productName = BXLConst.SPP_R200II;
            }
        } else if ((logicalName.indexOf("SPP-R210") >= 0)) {
            productName = BXLConst.SPP_R210;
        } else if ((logicalName.indexOf("SPP-R310") >= 0)) {
            productName = BXLConst.SPP_R310;
        } else if ((logicalName.indexOf("SPP-R300") >= 0)) {
            productName = BXLConst.SPP_R300;
        } else if ((logicalName.indexOf("SPP-R400") >= 0)) {
            productName = BXLConst.SPP_R400;
        }

        return productName;
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
}
