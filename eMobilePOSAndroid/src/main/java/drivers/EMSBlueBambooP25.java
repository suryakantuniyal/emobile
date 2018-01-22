package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

import com.StarMicronics.jasura.JAException;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.payment.ProcessCreditCard_FA;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.crashlytics.android.Crashlytics;
import com.starmicronics.stario.StarIOPortException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import interfaces.EMSPrintingDelegate;
import main.EMSDeviceManager;
import util.NumberUtil;
import util.PocketPos;
import util.StringUtil;

public class EMSBlueBambooP25 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public static final String LOAD_CARD_P25 = "C0 48 32 30 30 30 32 20 20 20 20 C1"; // 20
    // second
    // time-out
    public static final int START_FRAME = -64;
    public static final int END_FRAME = -63;
    public static BluetoothSocket socket;
    public static boolean isreceive = false;
    private final int LINE_WIDTH = 32;
    public EMSPrintingDelegate printingDelegate;
    public byte[] btBuf;
    public Vector<Byte> packdata = new Vector<>(2048);
    public ReceiveThread receivethread;
    private BluetoothAdapter mBtAdapter;
    private String[] cardValues = new String[3];
    private Bitmap bitmap;

    private Handler handler;// = new Handler();
    private ProgressDialog myProgressDialog;
    private EMSDeviceDriver thisInstance;
    private EMSDeviceManager edm;
    private CreditCardInfo cardManager;
    private Encrypt encrypt;
    private BambooHandlerCallback bambooHandler;


    private EMSCallBack callBack;
    // displays data from card swiping
    private Runnable doUpdateViews = new Runnable() {
        public void run() {
            try {
                if (callBack != null)
                    callBack.cardWasReadSuccessfully(true, cardManager);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };
    private Runnable doUpdateDidConnect = new Runnable() {
        public void run() {
            try {
                if (callBack != null)
                    callBack.readerConnectedSuccessfully(true);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    public EMSBlueBambooP25() {

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        cardManager = new CreditCardInfo();

    }

    public void registerAll() {
        this.registerPrinter();
    }

    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);

        thisInstance = this;
        this.edm = edm;
        encrypt = new Encrypt(activity);
        new processConnectionAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean autoConnect(Activity _activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = _activity;
        myPref = new MyPreferences(this.activity);

        thisInstance = this;
        this.edm = edm;
        encrypt = new Encrypt(activity);
        boolean didConnect = false;

        String macAddress = myPref.getPrinterMACAddress();
        try {
            BluetoothDevice btDev = mBtAdapter.getRemoteDevice(macAddress);
            socket = btDev.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
            socket.connect();

            if (socket != null) {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

                didConnect = true;
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        if (didConnect) {
            edm.driverDidConnectToDevice(thisInstance, false, _activity);
        } else {

            edm.driverDidNotConnectToDevice(thisInstance, "", false, _activity);
        }

        return didConnect;
    }

    public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {

        if (handler == null)
            handler = new Handler();

        if (!isreceive) {
            callBack = _callBack;
            startReceiveThread();
        }
        byte[] commandcancel = StringUtil.hexStringToBytes(LOAD_CARD_P25);
        this.printByteArray(commandcancel);
        handler.post(doUpdateDidConnect);
    }

    public void printByteArray(byte[] byteArray) {
        try {
            this.outputStream.write(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBitmap(Bitmap bmp) {
        this.bitmap = bmp;
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
    public void printShiftDetailsReport(String shiftID) {
        printShiftDetailsReceipt(LINE_WIDTH, shiftID);
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

    public boolean printOnHold(Object onHold) {
        EMSBambooImageLoader loader = new EMSBambooImageLoader();
        ArrayList<ArrayList<Byte>> arrayListList = loader.bambooDataWithAlignment(0, bitmap);

        for (ArrayList<Byte> arrayList : arrayListList) {

            byte[] byteArray = new byte[arrayList.size()];
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                byteArray[i] = arrayList.get(i).byteValue();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.printByteArray(byteArray);
        }

        return true;
    }

    @Override
    public boolean printReport(String curDate) {

        printReportReceipt(curDate, LINE_WIDTH);

        return true;
    }

    @Override
    public void printEndOfDayReport(String curDate, String clerk_id, boolean printDetails) {
        printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);
    }

    public void registerPrinter() {
        edm.setCurrentDevice(this);
        this.printingDelegate = edm;
    }

    public void unregisterPrinter() {
        edm.setCurrentDevice(null);
        this.printingDelegate = null;
    }

    private void sendMsg(int flag) {

        Message msg = new Message();
        msg.what = flag;
        if (bambooHandler == null)
            bambooHandler = new BambooHandlerCallback();

        bambooHandler.handleMessage(msg);

        // bambooHandler.sendMessage(msg);
    }

    public void startReceiveThread() {
        isreceive = true;
        if (receivethread == null) {
            receivethread = new ReceiveThread();
            receivethread.start();
        }

    }

    @Override
    public void releaseCardReader() {
        isreceive = false;
        callBack = null;
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {

        printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);


        return true;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        printConsignmentPickupReceipt(myConsignment, encodedSig, LINE_WIDTH);

        return true;
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        printOpenInvoicesReceipt(invID, LINE_WIDTH);

        return true;
    }

    @Override
    public String printStationPrinter(List<Orders> orders, String ordID, boolean cutPaper, boolean printHeader) {
        return printStationPrinterReceipt(orders, ordID, LINE_WIDTH, cutPaper, printHeader);

    }

    @Override
    public void openCashDrawer() {

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
        return false;
    }

    @Override
    public void toggleBarcodeReader() {

    }

    @Override
    public void printReceiptPreview(SplittedOrder splitedOrder) {
        try {
            setPaperWidth(LINE_WIDTH);
//            Bitmap bitmap = loadBitmapFromView(view);
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

//    @Override
//    public void printReceiptPreview(View view) {
//        try {
//            Bitmap bitmap = loadBitmapFromView(view);
//            super.printReceiptPreview(bitmap, LINE_WIDTH);
//        } catch (JAException e) {
//            e.printStackTrace();
//        } catch (StarIOPortException e) {
//            e.printStackTrace();
//        }
//    }

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
    public void printHeader() {
        super.printHeader(LINE_WIDTH);
    }

    @Override
    public void printFooter() {
        super.printFooter(LINE_WIDTH);
    }

    public class processConnectionAsync extends AsyncTask<Void, String, String> {

        String msg = new String("Failed to connect");
        boolean didConnect = false;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Connecting Printer...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected String doInBackground(Void... params) {

            String macAddress = myPref.getPrinterMACAddress();
            BluetoothDevice btDev = mBtAdapter.getRemoteDevice(macAddress);
            try {
                socket = btDev.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                socket.connect();

                if (socket != null) {
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();

                    didConnect = true;
                }
            } catch (IOException e) {

                msg = "Failed to connect: \n" + e.getMessage();
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

    private class BambooHandlerCallback implements Callback {
        @Override
        public boolean handleMessage(Message msg) {
            if (isreceive) {
                switch (msg.what) {
                    case 3:
                        int size = packdata.size();
                        byte[] buffer = null;
                        buffer = new byte[size];

                        for (int i = 0; i < size; i++) {
                            buffer[i] = packdata.get(i);
                        }

                        if (buffer != null) {
                            // handle the message
                            byte[] content = PocketPos.FrameUnpack(buffer, 0, buffer.length);
                            if (content != null) {
                                if (content[0] == PocketPos.FRAME_MSR) {

                                    String trackone = "";
                                    String tracktwo = "";
                                    String trackthree = "";

                                    byte tracknumber;
                                    int offset = 1;
                                    byte[] tracknumberlength = new byte[4];

                                    Log.i("content length = ", content.length + "");

                                    do {

                                        if (offset < content.length - 2) {
                                            tracknumber = content[offset];// track
                                            // number
                                        } else {
                                            return false;
                                        }

                                        if (StringUtil.toHexChar(tracknumber) == (byte) 0x31) // track
                                        // one
                                        {
                                            // Log.i("tracknumber = " ,
                                            // StringUtil.toHexString(tracknumber));
                                            tracknumberlength[0] = content[offset + 1];
                                            tracknumberlength[1] = content[offset + 2];
                                            tracknumberlength[2] = content[offset + 3];
                                            tracknumberlength[3] = content[offset + 4];

                                            offset += 4;
                                            int tracklength = NumberUtil.parseInt(tracknumberlength, 0,
                                                    tracknumberlength.length, 10);
                                            byte[] trackcontent = new byte[tracklength];
                                            System.arraycopy(content, offset + 1, trackcontent, 0, tracklength);

                                            try {
                                                trackone = new String(trackcontent, "utf-8");
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }

                                            // Global.cardTrackOne = trackone;
                                            cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(trackone));
                                            offset += tracklength;
                                            offset++;
                                        }

                                        if (StringUtil.toHexChar(tracknumber) == (byte) 0x32) // track
                                        // two
                                        {
                                            // Log.i("tracknumber = ",
                                            // StringUtil.toHexString(tracknumber));
                                            tracknumberlength[0] = content[offset + 1];
                                            tracknumberlength[1] = content[offset + 2];
                                            tracknumberlength[2] = content[offset + 3];
                                            tracknumberlength[3] = content[offset + 4];

                                            offset += 4;
                                            int tracklength = NumberUtil.parseInt(tracknumberlength, 0,
                                                    tracknumberlength.length, 10);
                                            byte[] trackcontent = new byte[tracklength];
                                            System.arraycopy(content, offset + 1, trackcontent, 0, tracklength);

                                            try {
                                                tracktwo = new String(trackcontent, "utf-8");
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }

                                            // Global.cardTrackTwo = tracktwo;
                                            cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(tracktwo));
                                            offset += tracklength;
                                            offset++;
                                        }

                                        if (StringUtil.toHexChar(tracknumber) == (byte) 0x33) // track
                                        // three
                                        {
                                            // Log.i("tracknumber = ",
                                            // StringUtil.toHexString(tracknumber));
                                            tracknumberlength[0] = content[offset + 1];
                                            tracknumberlength[1] = content[offset + 2];
                                            tracknumberlength[2] = content[offset + 3];
                                            tracknumberlength[3] = content[offset + 4];

                                            offset += 4;
                                            int tracklength = NumberUtil.parseInt(tracknumberlength, 0,
                                                    tracknumberlength.length, 10);
                                            byte[] trackcontent = new byte[tracklength];
                                            System.arraycopy(content, offset + 1, trackcontent, 0, tracklength);

                                            try {
                                                trackthree = new String(trackcontent, "utf-8");
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                            // Log.i("track three content=",
                                            // trackthree);

                                            offset += tracklength;
                                        }

                                        // Log.i("offset = ", offset + "");
                                    } while (offset <= content.length - 2);

                                    cardValues[0] = trackone;
                                    cardValues[1] = tracktwo;
                                    cardValues[2] = trackthree;

                                    // Toast.makeText(activity,
                                    // cardValues[0]+cardValues[1]+cardValues[2],
                                    // Toast.LENGTH_LONG).show();

                                    cardValues[0].trim();
                                    String[] firstTrack = cardValues[0].split("\\^");
                                    if (firstTrack.length > 1)
                                        firstTrack = firstTrack[1].split("/");

                                    boolean isDateAvail = false;
                                    String[] secondTrack;
                                    String expYear = "";
                                    String expDate = "";
                                    String cardNumber = "";
                                    if (cardValues[1].contains("=")) {
                                        secondTrack = cardValues[1].split("=");
                                        // tempCardNum[1] should contain the card
                                        // number
                                        String[] tempCardNum = secondTrack[0].split(";");
                                        if (tempCardNum.length > 1) {
                                            expYear = secondTrack[1].substring(0, 2);
                                            expDate = secondTrack[1].substring(2, 4);
                                        }
                                        cardNumber = tempCardNum[1];

                                    } else {
                                        String[] tempCardNum = cardValues[1].split(";");
                                        cardNumber = tempCardNum[1].replace("?", "");
                                    }


                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 0; i < firstTrack.length; i++)
                                        sb.append(firstTrack[i].trim()).append(" ");

                                    cardManager.setCardOwnerName(sb.toString());
                                    if (cardNumber.length() > 1) {
                                        int temp = cardNumber.length();
                                        String last4Digits = (String) cardNumber.subSequence(temp - 4, temp);
                                        cardManager.setCardLast4(last4Digits);

                                        cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(cardNumber));
                                        cardManager.setCardType(ProcessCreditCard_FA.getCardType(cardNumber));

                                        if (!Global.isEncryptSwipe)
                                            cardManager.setCardNumUnencrypted(cardNumber);

                                    }
                                    cardManager.setCardExpMonth(expDate);
                                    cardManager.setCardExpYear(expYear);

                                    handler.post(doUpdateViews);
                                } else if (content[0] == PocketPos.FRAME_TOF_PWD) {
                                    if (content.length > 1) {
                                    }
                                }
                            }
                        }

                        break;

                    case 4:

                        break;
                    case 5:
                        break;

                    case -1:
                        break;
                }
            }
            return false;
        }
    }

    class ReceiveThread extends Thread {
        public void run() {

            while (isreceive) {
                if (inputStream != null) {
                    try {
                        byte[] temp = new byte[2048];// 2k

                        int len = inputStream.read(temp);
                        if (len > 0) {
                            btBuf = new byte[len];

                            System.arraycopy(temp, 0, btBuf, 0, btBuf.length);


                            if (btBuf[0] == START_FRAME && btBuf[btBuf.length - 1] == END_FRAME) {
                                // Log.i("receive message", "put whole data");
                                packdata.clear();
                                for (int i = 0; i < btBuf.length; i++) {
                                    packdata.add(btBuf[i]);
                                }
                                sendMsg(3);
                            } else if ((btBuf[0] == START_FRAME) && (btBuf[btBuf.length - 1] != END_FRAME)) {
                                // Log.i("receive message package data", "No1");
                                if (packdata != null && packdata.size() > 0) {
                                    packdata.clear();

                                }
                                for (int i = 0; i < btBuf.length; i++) {
                                    packdata.add(btBuf[i]);
                                }
                            } else if ((btBuf[0] != START_FRAME) && (btBuf[btBuf.length - 1] == END_FRAME)) {
                                // Log.i("receive message package data", "No2");
                                if (packdata != null) {
                                    if (packdata.get(0) == START_FRAME) {

                                        if (packdata.size() <= 2048) {
                                            for (int i = 0; i < btBuf.length; i++) {
                                                packdata.add(btBuf[i]);
                                            }
                                            sendMsg(3);
                                        } else {
                                            packdata.clear();
                                            // Log.i("receive message package
                                            // data",
                                            // "Clear 2");
                                        }
                                    }
                                }
                            } else if (btBuf[0] != START_FRAME && btBuf[btBuf.length - 1] != END_FRAME) {
                                if (packdata != null && packdata.size() > 0) {
                                    if (packdata.get(0) == START_FRAME) {
                                        // Log.i("receive message package
                                        // data","No3");
                                        for (int i = 0; i < btBuf.length; i++) {
                                            packdata.add(btBuf[i]);
                                        }
                                    }
                                }
                            }

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        isreceive = false;
                    }
                }
            }
        }
    }
}
