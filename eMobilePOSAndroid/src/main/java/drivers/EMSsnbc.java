package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.view.View;

import com.StarMicronics.jasura.JAException;
import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.starmicronics.stario.StarIOPortException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import POSAPI.POSInterfaceAPI;
import POSAPI.POSUSBAPI;
import POSSDK.POSSDK;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

public class EMSsnbc extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {
    private final int LINE_WIDTH = 42;
    private String encodedSignature;
    private String encodedQRCode = "";
    private ProgressDialog myProgressDialog;
    private EMSDeviceDriver thisInstance;
    private EMSDeviceManager edm;

    //Returned Value Statement
    private final int POS_SUCCESS = 1000;        //success
    private final int ERR_PROCESSING = 1001;    //processing error
    private final int ERR_PARAM = 1002;        //parameter error


    private static POSSDK pos_usb;
    private static POSInterfaceAPI interface_usb;
    private int error_code = 0;

    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        thisInstance = this;
        this.edm = edm;
        if (interface_usb == null) {
            interface_usb = new POSUSBAPI(activity);
        }
        new processConnectionAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
    }


    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter, String _portName, String _portNumber) {
        boolean didConnect = false;
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        thisInstance = this;
        if (interface_usb == null) {
            interface_usb = new POSUSBAPI(activity);
        } else {
            interface_usb.CloseDevice();
        }

        error_code = interface_usb.OpenDevice();
        if (error_code == POS_SUCCESS) {
            if (pos_usb == null) {
                pos_usb = new POSSDK(interface_usb);
            }
            pos_sdk = pos_usb;
            if (setupPrinter())
                didConnect = true;
        } else if (error_code == ERR_PROCESSING) {
            int _time_out = 0;
            while (error_code == ERR_PROCESSING || _time_out > 10) {
                error_code = interface_usb.OpenDevice();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                _time_out++;
            }

            if (error_code == POS_SUCCESS) {
                pos_usb = new POSSDK(interface_usb);
                pos_sdk = pos_usb;
                if (setupPrinter())
                    didConnect = true;
            }
        }
        if (didConnect) {
            this.edm.driverDidConnectToDevice(thisInstance, false);
        } else {

            this.edm.driverDidNotConnectToDevice(thisInstance, null, false);
        }


        return didConnect;
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


            error_code = interface_usb.OpenDevice();
            if (error_code == POS_SUCCESS) {
                if (pos_usb == null) {
                    pos_usb = new POSSDK(interface_usb);
                }
                pos_sdk = pos_usb;

                if (setupPrinter())
                    didConnect = true;
            }

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


    private boolean setupPrinter() {
        error_code = pos_sdk.textStandardModeAlignment(0);
        if (error_code != POS_SUCCESS)
            return false;
        // set the horizontal and vertical motion units
        pos_sdk.systemSetMotionUnit(100, 100);

        // set line height
        pos_sdk.textSetLineHeight(10);
        int FontStyle = 0;
        int FontType = 0;

        // set character font
        pos_sdk.textSelectFont(FontType, FontStyle);

        // set character size
        pos_sdk.textSelectFontMagnifyTimes(1, 1);

        return true;
    }


    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        openUsbInterface();
        printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory, boolean fromOnHold) {
        openUsbInterface();
        printTransaction(ordID, type, isFromHistory, fromOnHold, null);
        return true;
    }


    @Override
    public boolean printPaymentDetails(String payID, int type, boolean isReprint, EMVContainer emvContainer) {
        openUsbInterface();
        printPaymentDetailsReceipt(payID, type, isReprint, LINE_WIDTH, emvContainer);
        return true;
    }


    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        openUsbInterface();
        return printBalanceInquiry(values, LINE_WIDTH);
    }

    @Override
    public boolean printOnHold(Object onHold) {
        return true;
    }


    @Override
    public void setBitmap(Bitmap bmp) {

    }

    @Override
    public void playSound() {

    }


    @Override
    public void printEndOfDayReport(String curDate, String clerk_id, boolean printDetails) {
        openUsbInterface();
        printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
        openUsbInterface();
        printShiftDetailsReceipt(LINE_WIDTH, shiftID);
    }


    @Override
    public boolean printReport(String curDate) {
        openUsbInterface();
        printReportReceipt(curDate, LINE_WIDTH);
        return true;
    }


    @Override
    public void registerPrinter() {
        edm.currentDevice = this;
    }


    @Override
    public void unregisterPrinter() {
        interface_usb.CloseDevice();
        edm.currentDevice = null;
    }

    public boolean openUsbInterface() {
        boolean didConnect = false;

        if (interface_usb == null) {
            interface_usb = new POSUSBAPI(activity);
        } else {
            interface_usb.CloseDevice();
        }

        error_code = interface_usb.OpenDevice();
        if (error_code == POS_SUCCESS) {
            if (pos_usb == null) {
                pos_usb = new POSSDK(interface_usb);
            }
            pos_sdk = pos_usb;
            if (setupPrinter())
                didConnect = true;
        } else if (error_code == ERR_PROCESSING) {
            int _time_out = 0;
            while (error_code == ERR_PROCESSING || _time_out > 10) {
                error_code = interface_usb.OpenDevice();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                _time_out++;
            }

            if (error_code == POS_SUCCESS) {
                pos_usb = new POSSDK(interface_usb);
                pos_sdk = pos_usb;
                if (setupPrinter())
                    didConnect = true;
            }
        }
        return didConnect;
    }

    public void closeUsbInterface() {
        interface_usb.CloseDevice();
    }


    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        openUsbInterface();
        printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);


        return true;
    }


    @Override
    public void releaseCardReader() {
    }

    @Override
    public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {
    }


    @Override
    public boolean printConsignmentPickup(
            List<ConsignmentTransaction> myConsignment, String encodedSig) {
        openUsbInterface();
        printConsignmentPickupReceipt(myConsignment, encodedSig, LINE_WIDTH);

        return true;
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        openUsbInterface();
        printOpenInvoicesReceipt(invID, LINE_WIDTH);

        return true;
    }


    @Override
    public String printStationPrinter(List<Orders> orders, String ordID, boolean cutPaper, boolean printHeader) {
        openUsbInterface();
        return printStationPrinterReceipt(orders, ordID, LINE_WIDTH, cutPaper, printHeader);

    }

    @Override
    public void openCashDrawer() {
        openUsbInterface();
        new Thread(new Runnable() {
            public void run() {
                pos_sdk.cashdrawerOpen(0, 100, 100);
            }
        }).start();

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
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        openUsbInterface();
        printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);

        return true;
    }

    @Override
    public void loadScanner(EMSCallBack _callBack) {
    }

    @Override
    public boolean isUSBConnected() {
        UsbManager manager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        while (deviceIterator.hasNext()) {

            UsbDevice device = deviceIterator.next();
            if ((device.getVendorId() == 7306 && device.getProductId() == 515))
                return true;

        }
        return false;
    }

    @Override
    public void toggleBarcodeReader() {

    }

    @Override
    public void printReceiptPreview(View view) {
        openUsbInterface();
        try {
            setPaperWidth(LINE_WIDTH);
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
