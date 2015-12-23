package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

import com.StarMicronics.jasura.JAException;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ProductsHandler;
import com.android.database.StoredPayments_DB;
import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.PaymentDetails;
import com.android.emobilepos.payment.ProcessCreditCard_FA;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.starmicronics.stario.StarIOPortException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;

import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;
import protocols.EMSPrintingDelegate;
import util.NumberUtil;
import util.PocketPos;
import util.StringUtil;

public class EMSBlueBambooP25 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    public EMSPrintingDelegate printingDelegate;
    private BluetoothAdapter mBtAdapter;


    private final int LINE_WIDTH = 32;

    public static BluetoothSocket socket;
    public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public static final String LOAD_CARD_P25 = "C0 48 32 30 30 30 32 20 20 20 20 C1"; // 20
    // second
    // time-out
    public static final int START_FRAME = -64;
    public static final int END_FRAME = -63;

    private String[] cardValues = new String[3];

    public byte[] btBuf;
    public Vector<Byte> packdata = new Vector<Byte>(2048);
    public static boolean isreceive = false;
    public ReceiveThread receivethread;

    private Bitmap bitmap;

    private Handler handler;// = new Handler();
    private ProgressDialog myProgressDialog;
    private EMSDeviceDriver thisInstance;
    private EMSDeviceManager edm;
    private CreditCardInfo cardManager;
    private Encrypt encrypt;
    private BambooHandlerCallback bambooHandler;


    private EMSCallBack callBack;

    public EMSBlueBambooP25() {

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        cardManager = new CreditCardInfo();

    }

    public void registerAll() {
        this.registerPrinter();
    }

    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);

        thisInstance = this;
        this.edm = edm;
        encrypt = new Encrypt(activity);
        new processConnectionAsync().execute(0);
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

        String macAddress = myPref.printerMACAddress(true, null);
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
        }

        if (didConnect) {
            edm.driverDidConnectToDevice(thisInstance, false);
        } else {

            edm.driverDidNotConnectToDevice(thisInstance, "", false);
        }

        return didConnect;
    }

    public class processConnectionAsync extends AsyncTask<Integer, String, String> {

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
        protected String doInBackground(Integer... params) {
            // TODO Auto-generated method stub

            String macAddress = myPref.printerMACAddress(true, null);
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
                edm.driverDidConnectToDevice(thisInstance, true);
            } else {

                edm.driverDidNotConnectToDevice(thisInstance, msg, true);
            }

        }
    }

//	public void printString(String theString) {
//		byte[] header = { 0x1B, 0x21, 0x01 };
//		byte[] lang = new byte[] { (byte) 0x1B, (byte) 0x4B, (byte) 0x31, (byte) 0x1B, (byte) 0x52, 48 };
//
//		try {
//			this.outputStream.write(header);
//			this.outputStream.write(lang);
//			this.outputStream.write(theString.getBytes("UTF-8"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

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
            // TODO Auto-generated catch block
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
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);

        return false;
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.printByteArray(byteArray);
        }

        return true;
    }

    @Override
    public void printEndOfDayReport(String date, String clerk_id) {

    }

    @Override
    public boolean printReport(String curDate) {

        printReportReceipt(curDate, LINE_WIDTH);

        return true;
    }

    public void registerPrinter() {
        // TODO Auto-generated method stub
        edm.currentDevice = this;
        this.printingDelegate = edm;
    }

    public void unregisterPrinter() {
        // TODO Auto-generated method stub
        edm.currentDevice = null;
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
                                                // TODO Auto-generated catch block
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
                                                // TODO Auto-generated catch block
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
                                                // TODO Auto-generated catch block
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
                                    // String[] secondTrack =
                                    // cardValues[1].split("=");
                                    // String[] cardNumber =
                                    // secondTrack[0].split(";"); // cardNumber[1]
                                    // // contains
                                    // // card
                                    // // number

                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 0; i < firstTrack.length; i++)
                                        sb.append(firstTrack[i].trim()).append(" ");

                                    cardManager.setCardOwnerName(sb.toString());
                                    if (cardNumber.length() > 1) {
                                        int temp = cardNumber.length();
                                        String last4Digits = (String) cardNumber.subSequence(temp - 4, temp);
                                        cardManager.setCardLast4(last4Digits);

                                        cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(cardNumber));
                                        cardManager.setCardType(ProcessCreditCard_FA.cardType(cardNumber));

                                        if (!Global.isEncryptSwipe)
                                            cardManager.setCardNumUnencrypted(cardNumber);
                                        // if(Global.isEncryptSwipe)
                                        // cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(cardNumber));
                                        // else
                                        // {
                                        // cardManager.setCardNumUnencrypted(cardNumber);
                                        // }
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

                            // Log.i("receive message", btBuf[0] + "");
                            //
                            // Log.i("receive message package data",
                            // btBuf.length + "");
                            // for (int i = 0; i < btBuf.length; i++) {
                            // Log.i("receive message package data log",
                            // btBuf[i] + "" + "_" + i);
                            // }

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

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {

        printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);


        return true;
    }

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

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        // TODO Auto-generated method stub
        printConsignmentPickupReceipt(myConsignment, encodedSig, LINE_WIDTH);

        return true;
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        // TODO Auto-generated method stub
        printOpenInvoicesReceipt(invID, LINE_WIDTH);

        return true;
    }

    @Override
    public void printStationPrinter(List<Orders> orders, String ordID) {
        // TODO Auto-generated method stub
        printStationPrinterReceipt(orders, ordID, LINE_WIDTH);

    }

    @Override
    public void openCashDrawer() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        // TODO Auto-generated method stub
        printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);

        return true;
    }

    @Override
    public void loadScanner(EMSCallBack _callBack) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isUSBConnected() {
        return false;
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
