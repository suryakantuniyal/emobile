package drivers;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.android.emobilepos.models.Orders;
import com.android.soundmanager.SoundManager;
import com.android.support.CardParser;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.elotouch.paypoint.register.barcodereader.BarcodeReader;
import com.elotouch.paypoint.register.cd.CashDrawer;
import com.elotouch.paypoint.register.printer.SerialPort;
import com.magtek.mobile.android.libDynamag.MagTeklibDynamag;

import org.bouncycastle.crypto.digests.LongDigest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import drivers.elo.utils.MagStripDriver;
import drivers.elo.utils.MagStripeCardParser;
import drivers.elo.utils.PrinterAPI;
import main.EMSDeviceManager;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;

/**
 * Created by Guarionex on 12/3/2015.
 */
public class EMSELO extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {
    //Load JNI from the library project. Refer MainActivity.java from library project elotouchBarcodeReader.
    // In constructor we are loading .so file for Barcode Reader.

    //Load JNI from the library project. Refer MainActivity.java from library project elotouchCashDrawer.
    // In constructor we are loading .so file for Cash Drawer.
    static {
        System.loadLibrary("cashdrawerjni");
        System.loadLibrary("cfdjni");
        System.loadLibrary("barcodereaderjni");
        System.loadLibrary("serial_port");
    }

    private EMSCallBack scannerCallBack;
    MagStripDriver eloCardSwiper;
    private Encrypt encrypt;
    private CreditCardInfo cardManager;
    private EMSDeviceManager edm;
    private EMSELO thisInstance;
    private Handler handler;
    String scannedData = "";
    private final int LINE_WIDTH = 32;
    private BarcodeReader barcodereader = new BarcodeReader();
    private boolean didConnect;


    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);
        this.edm = edm;
        thisInstance = this;
        playSound();
        new processConnectionAsync().execute(true);
    }


    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);
        this.edm = edm;
        thisInstance = this;
        playSound();
        new processConnectionAsync().execute(false);
        return true;
    }


    public class processConnectionAsync extends AsyncTask<Boolean, String, Boolean> {

//        private ProgressDialog myProgressDialog;

        @Override
        protected void onPreExecute() {

//            myProgressDialog = new ProgressDialog(activity);
//            myProgressDialog.setMessage(activity.getString(R.string.progress_connecting_printer));
//            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            myProgressDialog.setCancelable(false);
//            myProgressDialog.show();

        }

        @Override
        protected Boolean doInBackground(Boolean... params) {

            String Text = "\n\n\nYour Elo Touch Solutions\nPayPoint receipt printer is\nworking properly.";
            SerialPort port = null;
            try {

                port = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
                OutputStream stream = port.getOutputStream();
                InputStream iStream = port.getInputStream();
                SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
                eloPrinterApi = new PrinterAPI(eloPrinterPort);
                if (!eloPrinterApi.isPaperAvailable()) {
                    Toast.makeText(activity, "Printer out of paper!", Toast.LENGTH_LONG).show();
                }
                eloPrinterPort.getInputStream().close();
                eloPrinterPort.getOutputStream().close();
                eloPrinterPort.close();

                didConnect = true;
            } catch (IOException e) {
                didConnect = false;
                e.printStackTrace();
            }

            return params[0];
        }

        @Override
        protected void onPostExecute(Boolean showAlert) {
//            myProgressDialog.dismiss();

            if (didConnect) {
                playSound();
                edm.driverDidConnectToDevice(thisInstance, showAlert);
            } else {
                edm.driverDidNotConnectToDevice(thisInstance, "", showAlert);
            }
        }
    }


    @Override
    public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory, boolean fromOnHold) {
        try {
//            String Text = "\n\n\nYour Elo Touch Solutions\nPayPoint receipt printer is\nworking properly.";
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            printReceipt(ordID, LINE_WIDTH, fromOnHold, type, isFromHistory);
            eloPrinterPort.getInputStream().close();
            eloPrinterPort.getOutputStream().close();
            eloPrinterPort.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    @Override
    public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint) {
        try {
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            super.printPaymentDetailsReceipt(payID, isFromMainMenu, isReprint, LINE_WIDTH);
            eloPrinterPort.getInputStream().close();
            eloPrinterPort.getOutputStream().close();
            eloPrinterPort.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        try {
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            super.printConsignmentReceipt(myConsignment, encodedSignature, LINE_WIDTH);
            eloPrinterPort.getInputStream().close();
            eloPrinterPort.getOutputStream().close();
            eloPrinterPort.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        try {
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            super.printConsignmentPickupReceipt(myConsignment, encodedSignature, LINE_WIDTH);
            eloPrinterPort.getInputStream().close();
            eloPrinterPort.getOutputStream().close();
            eloPrinterPort.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        try {
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            super.printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);
            eloPrinterPort.getInputStream().close();
            eloPrinterPort.getOutputStream().close();
            eloPrinterPort.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void printStationPrinter(List<Orders> orderProducts, String ordID) {
        try {
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            super.printStationPrinterReceipt(orderProducts, ordID, LINE_WIDTH);
            eloPrinterPort.getInputStream().close();
            eloPrinterPort.getOutputStream().close();
            eloPrinterPort.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        try {
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            super.printOpenInvoicesReceipt(invID, LINE_WIDTH);
            eloPrinterPort.getInputStream().close();
            eloPrinterPort.getOutputStream().close();
            eloPrinterPort.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            SoundManager.getInstance();
            SoundManager.initSounds(activity);
            SoundManager.loadSounds();
            SoundManager.playSound(1, 1);
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean printReport(String curDate) {
        try {
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            super.printReportReceipt(curDate, LINE_WIDTH);
            eloPrinterPort.getInputStream().close();
            eloPrinterPort.getOutputStream().close();
            eloPrinterPort.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void printEndOfDayReport(String date, String clerk_id) {

    }


    @Override
    public void registerAll() {
        this.registerPrinter();
    }


    public void registerPrinter() {
        // TODO Auto-generated method stub
        edm.currentDevice = this;
    }

    public void unregisterPrinter() {
        // TODO Auto-generated method stub
        edm.currentDevice = null;
        TurnOffBCR();
    }

    @Override
    public void loadCardReader(final EMSCallBack callBack, boolean isDebitCard) {
        eloCardSwiper = new MagStripDriver(activity);
        eloCardSwiper.startDevice();
        eloCardSwiper.registerMagStripeListener(new MagStripDriver.MagStripeListener() { //MageStripe Reader's Listener for notifying various events.

            @Override
            public void OnDeviceDisconnected() { //Fired when the Device has been Disconnected.
                Toast.makeText(activity, "Magnetic-Stripe Device Disconnected !", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnDeviceConnected() { //Fired when the Device has been Connected.
                Toast.makeText(activity, "Magnetic-Stripe Device Connected !", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnCardSwiped(MagTeklibDynamag cardData) { //Fired when a card has been swiped on the device.
                Log.d("Card Data", cardData.toString());
                CreditCardInfo creditCardInfo = new CreditCardInfo();
                CardParser.parseCreditCard(activity, cardData.getCardData(), creditCardInfo);
                callBack.cardWasReadSuccessfully(true, creditCardInfo);
            }
        });
    }

    @Override
    public void loadScanner(EMSCallBack callBack) {

        scannerCallBack = callBack;
        if (handler == null)
            handler = new Handler();
        if (callBack != null) {
            if (!barcodereader.isBcrOn()) {
                readBarcode();
            }
        } else {
            TurnOffBCR();
        }
    }


    @Override
    public void releaseCardReader() {
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openCashDrawer() {

        CashDrawer cash_drawer = new CashDrawer();
        if (cash_drawer.isDrawerOpen()) {
            Toast.makeText(activity, "The Cash Drawer is already open !", Toast.LENGTH_SHORT).show();
        } else {
            cash_drawer.openCashDrawer();
        }
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

    private Runnable runnableScannedData = new Runnable() {
        public void run() {
            try {
                if (scannerCallBack != null)
                    scannerCallBack.scannerWasRead(scannedData);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    /*
         *
         * Code to Read Barcode through Barcode Reader.
         * BarcodeReader automatically populates the widget that is currently in focus.
         * Here the "bar_code" edittext widget has focus.
         *
         * */
    private void readBarcode() {
        barcodereader.turnOnLaser();
    }

    public void TurnOnBCR() {
        barcodereader.turnOnLaser();
    }

    public void TurnOffBCR() {

        barcodereader.turnOnLaser();
    }


}
