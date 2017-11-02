package drivers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.StarMicronics.jasura.JAException;
import com.android.emobilepos.R;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.payment.ProcessCreditCard_FA;
import com.android.support.CardParser;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.elo.device.DeviceManager;
import com.elo.device.ProductInfo;
import com.elo.device.enums.EloPlatform;
import com.elo.device.exceptions.UnsupportedEloPlatform;
import com.elotouch.paypoint.register.barcodereader.BarcodeReader;
import com.elotouch.paypoint.register.cd.CashDrawer;
import com.elotouch.paypoint.register.cfd.CFD;
import com.elotouch.paypoint.register.printer.SerialPort;
import com.magtek.mobile.android.mtlib.MTConnectionType;
import com.magtek.mobile.android.mtlib.MTEMVEvent;
import com.magtek.mobile.android.mtlib.MTSCRA;
import com.magtek.mobile.android.mtlib.MTSCRAEvent;
import com.starmicronics.stario.StarIOPortException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import drivers.elo.utils.PrinterAPI;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

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
    private Encrypt encrypt;
    private EMSDeviceManager edm;
    private EMSELO thisInstance;
    private Handler handler;
    String scannedData = "";
    private final int LINE_WIDTH = 32;
    private BarcodeReader barcodereader = new BarcodeReader();
    private boolean didConnect;
    private static CFD customerFacingDisplay;
    private MTSCRA m_scra;
    private Handler m_scraHandler;

    private class SCRAHandlerCallback implements Handler.Callback {
        private static final String TAG = "Magtek";

        public boolean handleMessage(Message msg) {
            try {
                Log.i(TAG, "*** Callback " + msg.what);
                switch (msg.what) {
                    case MTSCRAEvent.OnDeviceConnectionStateChanged:
//                        OnDeviceStateChanged((MTConnectionState) msg.obj);
                        break;
                    case MTSCRAEvent.OnCardDataStateChanged:
//                        OnCardDataStateChanged((MTCardDataState) msg.obj);
                        break;
                    case MTSCRAEvent.OnDataReceived:
                        if (m_scra.getResponseData() != null) {
                            CreditCardInfo cardInfo = new CreditCardInfo();
                            if (m_scra.getKSN().equals("00000000000000000000")) {
                                CardParser.parseCreditCard(activity, m_scra.getMaskedTracks(), cardInfo);
                            } else {
                                cardInfo.setCardOwnerName(m_scra.getCardName());
                                if (m_scra.getCardExpDate() != null && !m_scra.getCardExpDate().isEmpty()) {
                                    String year = m_scra.getCardExpDate().substring(0, 2);
                                    String month = m_scra.getCardExpDate().substring(2, 4);
                                    cardInfo.setCardExpYear(year);
                                    cardInfo.setCardExpMonth(month);
                                }
                                cardInfo.setCardType(ProcessCreditCard_FA.getCardType(m_scra.getCardIIN()));
                                cardInfo.setCardLast4(m_scra.getCardLast4());
                                cardInfo.setEncryptedTrack1(m_scra.getTrack1());
                                cardInfo.setEncryptedTrack2(m_scra.getTrack2());
                                cardInfo.setCardNumAESEncrypted(encrypt.encryptWithAES(m_scra.getCardPAN()));
                                if (m_scra.getTrack1Masked() != null && !m_scra.getTrack1Masked().isEmpty())
                                    cardInfo.setEncryptedAESTrack1(encrypt.encryptWithAES(m_scra.getTrack1Masked()));
                                if (m_scra.getTrack2Masked() != null && !m_scra.getTrack2Masked().isEmpty())
                                    cardInfo.setEncryptedAESTrack2(encrypt.encryptWithAES(m_scra.getTrack2Masked()));
                                cardInfo.setDeviceSerialNumber(m_scra.getDeviceSerial());
                                cardInfo.setMagnePrint(m_scra.getMagnePrint());
                                cardInfo.setCardNumUnencrypted(m_scra.getCardPAN());
                                cardInfo.setMagnePrintStatus(m_scra.getMagnePrintStatus());
                                cardInfo.setTrackDataKSN(m_scra.getKSN());
                            }
                            scannerCallBack.cardWasReadSuccessfully(true, cardInfo);
                        }
                        break;
                    case MTSCRAEvent.OnDeviceResponse:
//                        OnDeviceResponse((String) msg.obj);
                        break;
                    case MTEMVEvent.OnTransactionStatus:
//                        OnTransactionStatus((byte[]) msg.obj);
                        break;
                    case MTEMVEvent.OnDisplayMessageRequest:
//                        OnDisplayMessageRequest((byte[]) msg.obj);
                        break;
                    case MTEMVEvent.OnUserSelectionRequest:
//                        OnUserSelectionRequest((byte[]) msg.obj);
                        break;
                    case MTEMVEvent.OnARQCReceived:
//                        OnARQCReceived((byte[]) msg.obj);
                        break;
                    case MTEMVEvent.OnTransactionResult:
//                        OnTransactionResult((byte[]) msg.obj);
                        break;

                    case MTEMVEvent.OnEMVCommandResult:
//                        OnEMVCommandResult((byte[]) msg.obj);
                        break;

                    case MTEMVEvent.OnDeviceExtendedResponse:
//                        OnDeviceExtendedResponse((String) msg.obj);
                        break;
                }
            } catch (Exception ex) {

            }

            return true;
        }
    }

    public static CFD getTerminalDisp() {
        if (customerFacingDisplay == null) {
            customerFacingDisplay = new CFD();
        }
        return customerFacingDisplay;
    }

    /*
 *
 * Prints/Displays Text on Customer Facing Display.
 *
 * */
    public static void printTextOnCFD(String Line1, String Line2, Context context) {
        DeviceManager deviceManager;
        try {
            deviceManager = DeviceManager.getInstance(EloPlatform.PAYPOINT_2, context);
            if (deviceManager != null) {
                com.elo.device.peripherals.CFD cfd = deviceManager.getCfd();
                if (cfd != null) {
                    cfd.setBacklight(true);
                    cfd.clear();
                    cfd.setLine(1, Line1);
                    cfd.setLine(2, Line2);
                }
            }
        } catch (UnsupportedEloPlatform unsupportedEloPlatform) {

        }
//        getTerminalDisp().setBacklight(true);
//        getTerminalDisp().clearDisplay();
//        getTerminalDisp().setLine1(Line1);
//        getTerminalDisp().setLine2(Line2);
    }

    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        encrypt = new Encrypt(activity);
        this.edm = edm;
        thisInstance = this;
        playSound();
        ProductInfo platformInfo = DeviceManager.getPlatformInfo();
        if (platformInfo.eloPlatform != EloPlatform.PAYPOINT_2 && isPOSPrinter) {
            if (Global.mainPrinterManager == null || Global.mainPrinterManager.getCurrentDevice() == null) {
                new processConnectionAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
            }
        } else {
            edm.driverDidConnectToDevice(thisInstance, false, activity);
        }
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        encrypt = new Encrypt(activity);
        this.edm = edm;
        thisInstance = this;
        playSound();
        ProductInfo platformInfo = DeviceManager.getPlatformInfo();
        if (platformInfo.eloPlatform != EloPlatform.PAYPOINT_2 && isPOSPrinter) {
            if (Global.mainPrinterManager == null || Global.mainPrinterManager.getCurrentDevice() == null) {
                new processConnectionAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
            }
        } else {
            edm.driverDidConnectToDevice(thisInstance, false, activity);
        }
        return true;
    }

    public class processConnectionAsync extends AsyncTask<Boolean, String, Boolean> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            SerialPort port;
            try {
                port = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
                OutputStream stream = port.getOutputStream();
                InputStream iStream = port.getInputStream();
                SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
                eloPrinterApi = new PrinterAPI(eloPrinterPort);
                if (!eloPrinterApi.isPaperAvailable()) {
//                    Toast.makeText(activity, "Printer out of paper!", Toast.LENGTH_LONG).show();
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
            if (didConnect) {
                playSound();
                edm.driverDidConnectToDevice(thisInstance, showAlert, activity);
            } else {
                edm.driverDidNotConnectToDevice(thisInstance, "", showAlert, activity);
            }
        }
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        try {
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
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
    public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory, boolean fromOnHold) {
        printTransaction(ordID, type, isFromHistory, fromOnHold, null);
        return true;
    }

    @Override
    public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint, EMVContainer emvContainer) {
        try {
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            super.printPaymentDetailsReceipt(payID, isFromMainMenu, isReprint, LINE_WIDTH, emvContainer);
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
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        return printBalanceInquiry(values, LINE_WIDTH);
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
    public String printStationPrinter(List<Orders> orderProducts, String ordID, boolean cutPaper, boolean printHeader) {
        try {
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            String receipt = super.printStationPrinterReceipt(orderProducts, ordID, LINE_WIDTH, cutPaper, printHeader);
            eloPrinterPort.getInputStream().close();
            eloPrinterPort.getOutputStream().close();
            eloPrinterPort.close();
            return receipt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
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
            MediaPlayer mPlayer = MediaPlayer.create(activity, R.raw.beep);
            mPlayer.start();
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
    public void printEndOfDayReport(String date, String clerk_id, boolean printDetails) {
        try {
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            super.printEndOfDayReportReceipt(date, LINE_WIDTH, printDetails);
            eloPrinterPort.getInputStream().close();
            eloPrinterPort.getOutputStream().close();
            eloPrinterPort.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
        try {
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            super.printShiftDetailsReceipt(LINE_WIDTH, shiftID);
            eloPrinterPort.getInputStream().close();
            eloPrinterPort.getOutputStream().close();
            eloPrinterPort.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerAll() {
        this.registerPrinter();
    }

    public void registerPrinter() {
        edm.setCurrentDevice(this);
    }

    public void unregisterPrinter() {
        edm.setCurrentDevice(null);
        turnOffBCR();
    }

    @Override
    public void loadCardReader(final EMSCallBack callBack, boolean isDebitCard) {
        this.scannerCallBack = callBack;
        if (m_scra == null) {
            m_scraHandler = new Handler(new SCRAHandlerCallback());
            m_scra = new MTSCRA(activity, m_scraHandler);
            m_scra.setConnectionType(MTConnectionType.USB);
            m_scra.setAddress(null);
            m_scra.setConnectionRetry(true);
            m_scra.openDevice();
        }
//        eloCardSwiper = new MagStripDriver(activity);
//        eloCardSwiper.startDevice();
//        eloCardSwiper.registerMagStripeListener(new MagStripDriver.MagStripeListener() { //MageStripe Reader's Listener for notifying various events.
//
//            @Override
//            public void OnDeviceDisconnected() { //Fired when the Device has been Disconnected.
//                Toast.makeText(activity, "Magnetic-Stripe Device Disconnected !", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void OnDeviceConnected() { //Fired when the Device has been Connected.
//                Toast.makeText(activity, "Magnetic-Stripe Device Connected !", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void OnCardSwiped(MagTeklibDynamag cardData) { //Fired when a card has been swiped on the device.
//                Log.d("Card Data", cardData.toString());
//                CreditCardInfo creditCardInfo = new CreditCardInfo();
//                boolean parsed = CardParser.parseCreditCard(activity, cardData.getCardData(), creditCardInfo);
//                callBack.cardWasReadSuccessfully(parsed, creditCardInfo);
//            }
//        });
    }

    @Override
    public void loadScanner(EMSCallBack callBack) {

        scannerCallBack = callBack;
        if (handler == null)
            handler = new Handler();
        if (callBack != null) {
            turnOnBCR();
        } else {
            turnOffBCR();
        }
    }

    @Override
    public void releaseCardReader() {
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

    @Override
    public void toggleBarcodeReader() {
        if (barcodereader != null) {
            barcodereader.turnOnLaser();
        }
    }

//    @Override
//    public void printReceiptPreview(View view) {
//
//        try {
//            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
//            eloPrinterApi = new PrinterAPI(eloPrinterPort);
//            setPaperWidth(LINE_WIDTH);
//            Bitmap bitmap = loadBitmapFromView(view);
//            super.printReceiptPreview(bitmap, LINE_WIDTH);
//            eloPrinterPort.getInputStream().close();
//            eloPrinterPort.getOutputStream().close();
//            eloPrinterPort.close();
//        } catch (JAException e) {
//            e.printStackTrace();
//        } catch (StarIOPortException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void printReceiptPreview(SplittedOrder splitedOrder) {
        try {
            SerialPort eloPrinterPort = new SerialPort(new File("/dev/ttymxc1"), 9600, 0);
            eloPrinterApi = new PrinterAPI(eloPrinterPort);
            setPaperWidth(LINE_WIDTH);
            super.printReceiptPreview(splitedOrder, LINE_WIDTH);
            eloPrinterPort.getInputStream().close();
            eloPrinterPort.getOutputStream().close();
            eloPrinterPort.close();
        } catch (JAException e) {
            e.printStackTrace();
        } catch (StarIOPortException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        turnOnBCR();
    }

    public void turnOnBCR() {
        if (!barcodereader.isBcrOn()) {
            barcodereader.turnOnLaser();
        }
    }

    public void turnOffBCR() {
        if (barcodereader.isBcrOn()) {
            barcodereader.turnOnLaser();
        }
    }
}
