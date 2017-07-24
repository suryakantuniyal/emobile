package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Base64;

import com.StarMicronics.jasura.JAException;
import com.android.database.MemoTextHandler;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplitedOrder;
import com.android.emobilepos.models.TimeClock;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.starmicronics.stario.StarIOPortException;
import com.zebra.printer.MobilePrinter;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;

public class EMSZebraEM220ii extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {
    static final int MESSAGE_START_WORK = Integer.MAX_VALUE - 4;
    static final int MESSAGE_END_WORK = Integer.MAX_VALUE - 5;
    private int LINE_WIDTH = 32;
    private final int ALIGN_CENTER = MobilePrinter.ALIGNMENT_CENTER;
    private final int ALIGN_LEFT = MobilePrinter.ALIGNMENT_LEFT;
    private final int TEXT_SIZE = MobilePrinter.TEXT_SIZE_HORIZONTAL1 | MobilePrinter.TEXT_SIZE_VERTICAL1;
    private MobilePrinter myPrinter;
    private EMSDeviceDriver thisInstance;
    private ProgressDialog myProgressDialog;
    // private int didConnect = -1;
    private int stateCount = 0;
    private EMSPlainTextHelper textHandler;
    private String encodedSignature;
    private String encodedQRCode = "";

    private Handler zebraHandler;
    private int readerCount = 0;
    private EMSDeviceManager edm;
    private CreditCardInfo cardManager;
    private Encrypt encrypt;
    private boolean isConnected = false;
    private boolean isAutoConnect = false;
    private EMSCallBack callBack;

    public EMSZebraEM220ii() {
        thisInstance = this;
        textHandler = new EMSPlainTextHelper();
        cardManager = new CreditCardInfo();
    }

    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {

        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        zebraHandler = new Handler(new ZebraHandlerCallback());
        myPrinter = new MobilePrinter(this.activity, zebraHandler, null);
        this.edm = edm;
        encrypt = new Encrypt(activity);
        myProgressDialog = new ProgressDialog(activity);
        myProgressDialog.setMessage("Connecting Printer...");
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        myProgressDialog.setCancelable(false);
        myProgressDialog.show();

        myPrinter.connect(myPref.getPrinterMACAddress());
    }

    @Override
    public boolean autoConnect(Activity _activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter, String _portName, String _portNumber) {
        this.activity = _activity;
        myPref = new MyPreferences(this.activity);
        isAutoConnect = true;
        this.edm = edm;
        encrypt = new Encrypt(activity);


        ((Activity)activity).runOnUiThread(new Runnable() {
            public void run() {
                zebraHandler = new Handler(new ZebraHandlerCallback());
                myPrinter = new MobilePrinter(activity, zebraHandler, null);

            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        if (myPrinter != null)
            myPrinter.connect(myPref.getPrinterMACAddress());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        return isConnected;

    }


    public void printHeader() {
        StringBuilder sb = new StringBuilder();

        MemoTextHandler handler = new MemoTextHandler(activity);
        String[] header = handler.getHeader();

        if (header[0] != null && !header[0].isEmpty())
            sb.append(header[0]).append("\n");
        if (header[0] != null && !header[1].isEmpty())
            sb.append(header[1]).append("\n");
        if (header[0] != null && !header[2].isEmpty())
            sb.append(header[2]).append("\n");

        if (!sb.toString().isEmpty()) {
            sb.append(textHandler.newLines(2));
            myPrinter.printText(sb.toString(), ALIGN_CENTER, 0, TEXT_SIZE, false);
        }
    }

    public void printFooter() {
        StringBuilder sb = new StringBuilder();
        MemoTextHandler handler = new MemoTextHandler(activity);
        String[] footer = handler.getFooter();

        if (footer[0] != null && !footer[0].isEmpty())
            sb.append(footer[0]).append("\n");
        if (footer[0] != null && !footer[1].isEmpty())
            sb.append(footer[1]).append("\n");
        if (footer[0] != null && !footer[2].isEmpty())
            sb.append(footer[2]).append("\n");

        if (!sb.toString().isEmpty()) {
            sb.append(textHandler.newLines(2));
            myPrinter.printText(sb.toString(), ALIGN_CENTER, 0, TEXT_SIZE, false);
        }
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
    public boolean printPaymentDetails(String payID, int type, boolean isReprint, EMVContainer emvContainer) {

        printPaymentDetailsReceipt(payID, type, isReprint, LINE_WIDTH, emvContainer);

        return true;
    }


    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        return printBalanceInquiry(values, LINE_WIDTH);
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {

        printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);


        return true;
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
//        printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
        //      printShiftDetailsReceipt(LINE_WIDTH, shiftID);
    }

    @Override
    public boolean printReport(String curDate) {
        printReportReceipt(curDate, LINE_WIDTH);

        return true;
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
    public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {
        callBack = _callBack;
        myPrinter.getMsrMode();
        myPrinter.setMsrReaderMode();
    }


    @Override
    public void registerAll() {
        this.registerPrinter();
    }

    protected void printImage(int type) {
        Bitmap myBitmap = null;
        switch (type) {
            case 0: // Logo
            {
                File imgFile = new File(myPref.getAccountLogoPath());
                if (imgFile.exists()) {
                    myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                }
                break;
            }
            case 1: // signature
            {
                if (!encodedSignature.isEmpty()) {
                    byte[] img = Base64.decode(encodedSignature, Base64.DEFAULT);
                    myBitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                }
                break;
            }
            case 2: {
                if (!encodedQRCode.isEmpty()) {
                    byte[] img = Base64.decode(encodedQRCode, Base64.DEFAULT);
                    myBitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                }
                break;
            }
        }

        if (myBitmap != null) {
            myPrinter.printBitmap(myBitmap, ALIGN_CENTER, MobilePrinter.BITMAP_WIDTH_FULL, 50, false);

        }
    }


    private class ZebraHandlerCallback implements Callback {
        @Override
        public boolean handleMessage(Message msg) {
            stateCount++;
            switch (msg.what) {
                case MobilePrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case MobilePrinter.STATE_CONNECTED:
                            stateCount = 0;
                            isConnected = true;
                            if (!isAutoConnect) {
                                myProgressDialog.dismiss();
                                edm.driverDidConnectToDevice(thisInstance, true);
                            } else {
                                edm.driverDidConnectToDevice(thisInstance, false);
                            }

                            break;

                        case MobilePrinter.STATE_CONNECTING:
                            // setStatus(R.string.title_connecting);

                            break;

                        case MobilePrinter.STATE_NONE:
                            if (stateCount > 3) {
                                isConnected = false;
                                stateCount = 0;
                                if (!isAutoConnect) {
                                    myProgressDialog.dismiss();
                                    edm.driverDidNotConnectToDevice(thisInstance, "Couldn't Connect...", true);
                                } else
                                    edm.driverDidNotConnectToDevice(thisInstance, "", false);

                            }
                            break;
                    }
                    break;

                case MobilePrinter.MESSAGE_WRITE:
                    switch (msg.arg1) {
                        case MobilePrinter.PROCESS_DEFINE_NV_IMAGE:
                            myPrinter.getDefinedNvImageKeyCodes();
                            // Toast.makeText(activity, "Complete to define NV image",
                            // Toast.LENGTH_LONG).show();
                            break;

                        case MobilePrinter.PROCESS_REMOVE_NV_IMAGE:
                            myPrinter.getDefinedNvImageKeyCodes();
                            // Toast.makeText(getApplicationContext(),
                            // "Complete to remove NV image", Toast.LENGTH_LONG).show();
                            break;

                        case MobilePrinter.PROCESS_UPDATE_FIRMWARE:
                            myPrinter.disconnect();
                            // Toast.makeText(getApplicationContext(),
                            // "Complete to download firmware.\nPlease reboot the printer.",
                            // Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;

                case MobilePrinter.MESSAGE_READ:
                    if (readerCount == 0) {
                        //handler.post(doUpdateDidConnect);
                        zebraHandler.post(doUpdateDidConnect);

                        readerCount++;
                    } else {
                        Bundle bundle = msg.getData();
                        byte[] tr1 = bundle.getByteArray(MobilePrinter.MSR_TRACK1);
                        byte[] tr2 = bundle.getByteArray(MobilePrinter.MSR_TRACK2);


                        if (tr1 != null) {
                            String mTrack1Data = new String(tr1);


                            mTrack1Data.trim();
                            String[] firstTrack = mTrack1Data.split("\\^");
                            if (firstTrack.length > 1)
                                firstTrack = firstTrack[1].split("/");

                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < firstTrack.length; i++)
                                sb.append(firstTrack[i].trim()).append(" ");

                            cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(mTrack1Data));
                            cardManager.setCardOwnerName(sb.toString());
                            // Global.cardName = sb.toString();

                            //handler.post(doUpdateViews);
                            if (tr2 == null)
                                zebraHandler.post(doUpdateViews);
                        }
                        if (tr2 != null) {
                            String mTrack2Data = new String(tr2);
                            String[] secondTrack = mTrack2Data.split("=");

                            String expYear = "";
                            String expDate = "";

                            if (secondTrack.length > 1 && secondTrack[1].length() > 4) {
                                expYear = secondTrack[1].substring(0, 2);
                                expDate = secondTrack[1].substring(2, 4);

                                cardManager.setCardExpMonth(expDate);
                                cardManager.setCardExpYear(expYear);
                            }
                            //StringBuilder sb = new StringBuilder();
                            cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(mTrack2Data));
                            //cardManager.setCardOwnerName(sb.toString());
                            // Global.cardName = sb.toString();

                            //Toast.makeText(activity, secondTrack[0], Toast.LENGTH_LONG).show();
                            if (!secondTrack[0].isEmpty()) {
                                if (secondTrack[0].length() > 4) {
                                    int temp = secondTrack[0].length();
                                    String last4Digits = (String) secondTrack[0].subSequence(temp - 4, temp);
                                    cardManager.setCardLast4(last4Digits);
                                }


                                cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(secondTrack[0]));
                                if (!Global.isEncryptSwipe)
                                    cardManager.setCardNumUnencrypted(secondTrack[0]);

                            }


                            zebraHandler.post(doUpdateViews);
                        }

                        readerCount = 0;
                    }
                    break;

                case MobilePrinter.MESSAGE_DEVICE_NAME:


                    break;

                case MobilePrinter.MESSAGE_TOAST:


                    break;

                case MobilePrinter.MESSAGE_DEVICE_SET:

                    break;

                case MobilePrinter.MESSAGE_PRINT_COMPLETE:


                    break;

                case MESSAGE_START_WORK:


                    break;

                case MESSAGE_END_WORK:


                    break;
            }
            return false;
        }
    }

    @Override
    public void releaseCardReader() {
        myPrinter.cancelMsrReaderMode();
        readerCount = 0;
        callBack = null;
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
//
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
    public void printReceiptPreview(SplitedOrder splitedOrder) {
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

}
