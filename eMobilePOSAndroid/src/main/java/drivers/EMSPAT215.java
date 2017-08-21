package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.android.emobilepos.R;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.realms.Payment;
import com.android.internal.misccomm.MsrApiContext;
import com.android.internal.misccomm.MsrManager;
import com.android.support.CardParser;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.partner.pt215.cashdrawer.CashDrawerApiContext;
import com.partner.pt215.cashdrawer.CashDrawerManage;
import com.partner.pt215.display.DisplayLineApiContext;
import com.partner.pt215.display.DisplayManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;


public class EMSPAT215 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    private CashDrawerApiContext cashDrawerApiContext = new CashDrawerManage();
    private EMSDeviceManager edm;
    private EMSDeviceDriver thisInstance;
    private ProgressDialog myProgressDialog;
    private MsrApiContext msrApiContext;
    private final int LINE_WIDTH = 32;
    private byte[] cardReadBuffer;
    public static DisplayLineApiContext terminalDisp;
    private CreditCardInfo creditCardInfo;

    private static volatile boolean runReader;
    private EMSCallBack callback;
    Handler handler;

    public static DisplayLineApiContext getTerminalDisp() {
        if (terminalDisp == null) {
            terminalDisp = new DisplayManager();
            terminalDisp.open();
            terminalDisp.initialize();
        }
        return terminalDisp;
    }

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
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        thisInstance = this;
//        Looper.prepare();
        new processConnectionAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
//        Looper.loop();
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        thisInstance = this;

        terminalDisp = new DisplayManager();
        terminalDisp.open();
        int res = terminalDisp.initialize();
        msrApiContext = MsrManager.getDefault(activity);

        int reader = msrApiContext.getMsrReading();
        initMSR(false);
        if (res == 0) {
            this.edm.driverDidConnectToDevice(thisInstance, false, activity);
            return true;
        } else
            this.edm.driverDidNotConnectToDevice(thisInstance, null, false, activity);

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
            terminalDisp = new DisplayManager();
            terminalDisp.open();
            int res = terminalDisp.initialize();

            initMSR(true);
            if (res == 0) {
                didConnect = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();
            if (didConnect) {
                edm.driverDidConnectToDevice(thisInstance, true, activity);
            } else {

                edm.driverDidNotConnectToDevice(thisInstance, msg, true, activity);
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
        edm.setCurrentDevice(this);
    }

    @Override
    public void unregisterPrinter() {
        msrApiContext.msrClose();
    }

    @Override
    public void loadCardReader(EMSCallBack callBack, boolean isDebitCard) {
        this.callback = callBack;
        if (handler == null)
            handler = new Handler();

        new Thread() {
            public void run() {
                initMSR(false);
                Thread thread = new Thread(setViewThread);
                thread.start();
            }
        }.start();
    }

    private void fullReload() {
        int msrDefault = msrApiContext.setMsrDefault();
        msrApiContext.setMsrOutputMode(0);
    }

    private void initMSR(boolean fullReload) {
        if (msrApiContext == null)
            msrApiContext = MsrManager.getDefault((Activity) activity);
        releaseCardReader();
        EMSPAT215.runReader = true;
        if (fullReload) {
            fullReload();
        }
        int reader = msrApiContext.getMsrReading();
        int setMsrReading;
        if (reader == 0) {
            setMsrReading = msrApiContext.setMsrReading(1);
        }
        int outMode = msrApiContext.getMsrOutputMode();
        if (outMode == 0) {
            int setMsrOutputMode = msrApiContext.setMsrOutputMode(1);
            if (setMsrOutputMode == 1)
                setMsrOutputMode = msrApiContext.setMsrOutputMode(1);
        }

        int setMsrEnable = msrApiContext.setMsrEnable();
        int startReadData = msrApiContext.startReadData();
        ((Activity)activity).runOnUiThread(doUpdateDidConnect);
    }

    private Runnable doUpdateDidConnect = new Runnable() {
        public void run() {
            try {
                if (callback != null)
                    callback.readerConnectedSuccessfully(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };
    //
    private Runnable setViewThread = new Runnable() {
        @Override
        public void run() {
            boolean startReadBuffer = false;

            while (EMSPAT215.runReader) {
                if (msrApiContext == null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    byte[] data = msrApiContext.readBufferData();
                    if (data != null && data.length > 0) {
                        startReadBuffer = true;
                        byte[] cardDataLen = {data[1], data[2]};
                        ByteBuffer byteBuffer = ByteBuffer.wrap(cardDataLen);
                        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                        short l = byteBuffer.getShort();
                        String s = new String(data, 9, data.length - 9);
                        cardReadBuffer = Arrays.copyOfRange(data, 0, l + 6);
                    }
                    if (startReadBuffer && data == null) {
                        EMSPAT215.runReader = false;

                        creditCardInfo = CardParser.parseIDTechOriginal(activity, cardReadBuffer);
                        handler.post(doUpdateViews);

                    }
                }
            }
        }
    };

    private Runnable doUpdateViews = new Runnable() {
        public void run() {
            try {
                if (callback != null)
                    callback.cardWasReadSuccessfully(true, creditCardInfo);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

//
//    private String convertByteToString(byte[] byteData) {
//        String str = null;
//        StringBuffer hexString = new StringBuffer();
//        for (int i = 0; i < byteData.length; i++) {
//            str = Integer.toHexString(0xFF & byteData[i]);
//            if (str.length() == 1)
//                str = "0" + str;
//            hexString.append(str);
//        }
//        return hexString.toString();
//    }
//
//    private String convertHexToAscii(String hexString) {
//        StringBuffer asciiString = new StringBuffer();
//        for (int i = 0; i < hexString.length(); i += 2) {
//            String subs = hexString.substring(i, i + 2);
//            asciiString.append((char) Integer.parseInt(subs, 16));
//        }
//        return asciiString.toString();
//    }
//
//    private short getByteShort(byte[] bytes) {
//        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
//        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
//        short l = byteBuffer.getShort();
//        return l;
//    }

    @Override
    public void loadScanner(EMSCallBack _callBack) {
    }

    @Override
    public void releaseCardReader() {
        EMSPAT215.runReader = false;
        msrApiContext.readBufferData();
        msrApiContext.stopReadData();
//        msrApiContext.setMsrDisable();
//        msrApiContext.setMsrReading(0);
//        msrApiContext.msrClose();
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

//    @Override
//    public void printReceiptPreview(View view) {
//
//    }

    @Override
    public void printReceiptPreview(SplittedOrder splitedOrder) {

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

    @Override
    public String printStationPrinter(List<Orders> orderProducts, String ordID, boolean cutPaper, boolean printHeader) {
        return "";
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
