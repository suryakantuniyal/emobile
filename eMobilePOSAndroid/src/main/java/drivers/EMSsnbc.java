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
import com.android.database.MemoTextHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.starmicronics.stario.StarIOPortException;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import POSAPI.POSInterfaceAPI;
import POSAPI.POSUSBAPI;
import POSSDK.POSSDK;
import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;

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


    public static POSSDK pos_usb = null;
    private POSInterfaceAPI interface_usb = null;
    private int error_code = 0;

    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        thisInstance = this;
        this.edm = edm;
        interface_usb = new POSUSBAPI(activity);
        new processConnectionAsync().execute(0);
    }


    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter, String _portName, String _portNumber) {
        boolean didConnect = false;
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        thisInstance = this;
        //isUSBConnected();
        interface_usb = new POSUSBAPI(activity);


        error_code = interface_usb.OpenDevice();
        if (error_code == POS_SUCCESS) {
            pos_usb = new POSSDK(interface_usb);
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
            // TODO Auto-generated method stub


            error_code = interface_usb.OpenDevice();
            if (error_code == POS_SUCCESS) {
                pos_usb = new POSSDK(interface_usb);
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


    private void printString(String s) {
        byte[] send_buf = null;
        try {
            send_buf = s.getBytes("GB18030");
            error_code = pos_sdk.textPrint(send_buf, send_buf.length);
            send_buf = null;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

//	private String getString(int id)
//	{
//		return(activity.getResources().getString(id));
//	}


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
        printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
        printShiftDetailsReceipt(LINE_WIDTH, shiftID);
    }


    @Override
    public boolean printReport(String curDate) {
        printReportReceipt(curDate, LINE_WIDTH);
        return true;
    }


    @Override
    public void registerPrinter() {
        edm.currentDevice = this;
    }


    @Override
    public void unregisterPrinter() {
        edm.currentDevice = null;
    }


    public void printHeader() {

        EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
        StringBuilder sb = new StringBuilder();

        MemoTextHandler handler = new MemoTextHandler(activity);
        String[] header = handler.getHeader();

        if (header[0] != null && !header[0].isEmpty())
            sb.append(textHandler.formatLongString(header[0], LINE_WIDTH)).append("\n");
        if (header[1] != null && !header[1].isEmpty())
            sb.append(textHandler.formatLongString(header[1], LINE_WIDTH)).append("\n");
        if (header[2] != null && !header[2].isEmpty())
            sb.append(textHandler.formatLongString(header[2], LINE_WIDTH)).append("\n");


        if (!sb.toString().isEmpty()) {
            sb.append(textHandler.newLines(2));
            pos_sdk.textStandardModeAlignment(ALIGN_CENTER);
            this.printString(sb.toString());
            pos_sdk.textStandardModeAlignment(ALIGN_LEFT);
        }
    }


    public void printFooter() {

        EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
        StringBuilder sb = new StringBuilder();
        MemoTextHandler handler = new MemoTextHandler(activity);
        String[] footer = handler.getFooter();

        if (footer[0] != null && !footer[0].isEmpty())
            sb.append(textHandler.formatLongString(footer[0], LINE_WIDTH)).append("\n");
        if (footer[1] != null && !footer[1].isEmpty())
            sb.append(textHandler.formatLongString(footer[1], LINE_WIDTH)).append("\n");
        if (footer[2] != null && !footer[2].isEmpty())
            sb.append(textHandler.formatLongString(footer[2], LINE_WIDTH)).append("\n");


        if (!sb.toString().isEmpty()) {
            sb.append(textHandler.newLines(2));
            pos_sdk.textStandardModeAlignment(ALIGN_CENTER);
            this.printString(sb.toString());
            pos_sdk.textStandardModeAlignment(ALIGN_LEFT);
        }

    }

    public void cutPaper() {

        // ******************************************************************************************
        // print in page mode
        error_code = pos_sdk.pageModePrint();

        error_code = pos_sdk.systemCutPaper(66, 0);

        // *****************************************************************************************
        // clear buffer in page mode
        error_code = pos_sdk.pageModeClearBuffer();
    }


    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {

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

        printConsignmentPickupReceipt(myConsignment, encodedSig, LINE_WIDTH);

        return true;
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        printOpenInvoicesReceipt(invID, LINE_WIDTH);

        return true;
    }


    @Override
    public void printStationPrinter(List<Orders> orders, String ordID) {
        printStationPrinterReceipt(orders, ordID, LINE_WIDTH);

    }

    @Override
    public void openCashDrawer() {


        new Thread(new Runnable() {
            public void run() {
                pos_sdk.cashdrawerOpen(0, 100, 100);
            }
        }).start();

    }


    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {

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
    public void refundReversal(Payment payment) {

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
