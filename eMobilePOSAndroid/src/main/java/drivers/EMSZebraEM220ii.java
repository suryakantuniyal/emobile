package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Base64;

import com.android.database.ClerksHandler;
import com.android.database.MemoTextHandler;
import com.android.database.OrderProductsHandler;
import com.android.database.OrderTaxes_DB;
import com.android.database.OrdersHandler;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.database.ProductsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.PaymentDetails;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.zebra.printer.MobilePrinter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;

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
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {

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

        myPrinter.connect(myPref.printerMACAddress(true, null));
    }

    @Override
    public boolean autoConnect(Activity _activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter, String _portName, String _portNumber) {
        this.activity = _activity;
        myPref = new MyPreferences(this.activity);
        isAutoConnect = true;
        this.edm = edm;
        encrypt = new Encrypt(activity);


        activity.runOnUiThread(new Runnable() {
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
            myPrinter.connect(myPref.printerMACAddress(true, null));

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
        // TODO Auto-generated method stub

        printPaymentDetailsReceipt(payID, type, isReprint, LINE_WIDTH, emvContainer);

        return true;
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {

        printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);


        return true;
    }

    /*
        @Override
        public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature) {


            StringBuilder sb = new StringBuilder();
            printPref = myPref.getPrintingPreferences();
            //SQLiteDatabase db = new DBManager(activity).openReadableDB();
            ProductsHandler productDBHandler = new ProductsHandler(activity);
            HashMap<String, String> map = new HashMap<String, String>();
            double ordTotal = 0, totalSold = 0, totalReturned = 0, totalDispached = 0, totalLines = 0;

            int size = myConsignment.size();

            if (printPref.contains(MyPreferences.print_header))
                this.printHeader();

            sb.append(textHandler.centeredString("Consignment Summary", LINE_WIDTH)).append("\n\n");

            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), myPref.getCustName(), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_employee), myPref.getEmpName(), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                    Global.formatToDisplayDate(Global.getCurrentDate(), activity, 3), LINE_WIDTH, 0));
            sb.append(textHandler.newLines(3));

            for (int i = 0; i < size; i++) {
                map = productDBHandler.getProductMap(myConsignment.get(i).ConsProd_ID, true);

                sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_name"), LINE_WIDTH, 0));

                if (printPref.contains(MyPreferences.print_descriptions)) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_description), "", LINE_WIDTH, 3)).append(
                            "\n");
                    sb.append(textHandler.oneColumnLineWithLeftAlignedText(map.get("prod_desc"), LINE_WIDTH, 5)).append("\n");
                } else
                    sb.append(textHandler.newLines(1));

                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Original Qty:",
                        myConsignment.get(i).ConsOriginal_Qty, LINE_WIDTH, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Rack Qty:", myConsignment.get(i)
                        .ConsStock_Qty, LINE_WIDTH, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Returned Qty:",
                        myConsignment.get(i).ConsReturn_Qty, LINE_WIDTH, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Sold Qty:",
                        myConsignment.get(i).ConsInvoice_Qty, LINE_WIDTH, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Dispatched Qty:",
                        myConsignment.get(i).ConsDispatch_Qty, LINE_WIDTH, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("New Qty:", myConsignment.get(i).ConsNew_Qty,
                        LINE_WIDTH, 3));
                sb.append(textHandler.twoColumnLineWithLeftAlignedText("Product Price:",
                        Global.formatDoubleStrToCurrency(map.get("prod_price")), LINE_WIDTH, 5));
                sb.append(
                        textHandler.twoColumnLineWithLeftAlignedText("Total:",
                                Global.formatDoubleStrToCurrency(myConsignment.get(i).invoice_total), LINE_WIDTH, 5))
                        .append(textHandler.newLines(2));

                totalSold += Double.parseDouble(myConsignment.get(i).ConsInvoice_Qty);
                totalReturned += Double.parseDouble(myConsignment.get(i).ConsReturn_Qty);
                totalDispached += Double.parseDouble(myConsignment.get(i).ConsDispatch_Qty);
                totalLines += 1;
                ordTotal += Double.parseDouble(myConsignment.get(i).invoice_total);

                myPrinter.printText(sb.toString(), ALIGN_LEFT, 0, TEXT_SIZE, false);
                sb.setLength(0);
            }

            sb.append(textHandler.lines(LINE_WIDTH));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Sold:", Double.toString(totalSold), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Returned", Double.toString(totalReturned), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Items Dispatched", Double.toString(totalDispached), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Total Line Items", Double.toString(totalLines), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("Grand Total:", Global.formatDoubleToCurrency(ordTotal), LINE_WIDTH, 0));
            sb.append(textHandler.newLines(3));

            myPrinter.printText(sb.toString(), ALIGN_LEFT, 0, TEXT_SIZE, false);

            if (printPref.contains(MyPreferences.print_footer))
                this.printFooter();
            myPrinter.printText(textHandler.newLines(3), ALIGN_LEFT, 0, TEXT_SIZE, false);
            //db.close();

            return true;
        }

    */
    @Override
    public boolean printOnHold(Object onHold) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void setBitmap(Bitmap bmp) {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub
        printReportReceipt(curDate, LINE_WIDTH);

        return true;
    }

    @Override
    public void registerPrinter() {
        // TODO Auto-generated method stub
        edm.currentDevice = this;
    }

    @Override
    public void unregisterPrinter() {
        // TODO Auto-generated method stub
        edm.currentDevice = null;
    }

    @Override
    public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {
        // TODO Auto-generated method stub
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

//						if (secondTrack.length > 1)//#####=### 
//						{
//							int temp = secondTrack[0].length();
//							String last4Digits = (String) secondTrack[0].subSequence(temp - 4, temp);
//							cardManager.setCardLast4(last4Digits);
//							
//							if(Global.isEncryptSwipe)
//								cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(secondTrack[0]));
//							else
//								cardManager.setCardNumAESEncrypted(secondTrack[0]);
//						}
//						else
//						{
//							if(Global.isEncryptSwipe)
//								cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(secondTrack[0]));
//							else
//								cardManager.setCardNumAESEncrypted(secondTrack[0]);
//						}


                            zebraHandler.post(doUpdateViews);
                        }


//					if (tr1 != null && tr2 != null) {
//						String mTrack1Data = new String(tr1);
//						String mTrack2Data = new String(tr2);
//
//						mTrack1Data.trim();
//						String[] firstTrack = mTrack1Data.split("\\^");
//						if (firstTrack.length > 1)
//							firstTrack = firstTrack[1].split("/");
//						String[] secondTrack = mTrack2Data.split("=");
//						// String[] cardNumber = secondTrack[0].split(";");
//
//						String expYear = "";
//						String expDate = "";
//
//						if (secondTrack.length > 1) {
//							expYear = secondTrack[1].substring(0, 2);
//							expDate = secondTrack[1].substring(2, 4);
//						}
//						StringBuilder sb = new StringBuilder();
//						for (int i = 0; i < firstTrack.length; i++)
//							sb.append(firstTrack[i].trim()).append(" ");
//
//						cardManager.setEncryptedAESTrack1(encrypt.encryptWithAES(mTrack1Data));
//						cardManager.setEncryptedAESTrack2(encrypt.encryptWithAES(mTrack2Data));
//						cardManager.setCardOwnerName(sb.toString());
//						// Global.cardName = sb.toString();
//
//						if (secondTrack.length > 1) {
//							int temp = secondTrack[0].length();
//							String last4Digits = (String) secondTrack[0].subSequence(temp - 4, temp);
//							cardManager.setCardLast4(last4Digits);
//							
//							if(Global.isEncryptSwipe)
//								cardManager.setCardNumAESEncrypted(encrypt.encryptWithAES(secondTrack[0]));
//							else
//								cardManager.setCardNumAESEncrypted(secondTrack[0]);
//						}
//						cardManager.setCardExpMonth(expDate);
//						cardManager.setCardExpYear(expYear);
//
//						//handler.post(doUpdateViews);
//						zebraHandler.post(doUpdateViews);
//					}
                        readerCount = 0;
                    }
                    break;

                case MobilePrinter.MESSAGE_DEVICE_NAME:

                    // mConnectedDeviceName =
                    // msg.getData().getString(MobilePrinter.DEVICE_NAME);
                    // Toast.makeText(getApplicationContext(), mConnectedDeviceName,
                    // Toast.LENGTH_LONG).show();
                    break;

                case MobilePrinter.MESSAGE_TOAST:

                    // mListView.setEnabled(false);
                    // Toast.makeText(getApplicationContext(),
                    // msg.getData().getString(MobilePrinter.TOAST),
                    // Toast.LENGTH_SHORT).show();
                    break;

                case MobilePrinter.MESSAGE_DEVICE_SET:

				/*
                 * if (msg.obj == null) {
				 * Toast.makeText(getApplicationContext(), "No paired device",
				 * Toast.LENGTH_SHORT).show(); } else {
				 * DialogManager.showBluetoothDialog(MainActivity.this,
				 * (Set<BluetoothDevice>) msg.obj); }
				 */
                    break;

                case MobilePrinter.MESSAGE_PRINT_COMPLETE:

                    // Toast.makeText(getApplicationContext(), "Complete to print",
                    // Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_START_WORK:

				/*
                 * mListView.setEnabled(false);
				 * mProgressBar.setVisibility(View.VISIBLE);
				 */
                    break;

                case MESSAGE_END_WORK:

				/*
                 * mListView.setEnabled(true);
				 * mProgressBar.setVisibility(View.INVISIBLE);
				 */
                    break;
            }
            return false;
        }
    }

    @Override
    public void releaseCardReader() {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub

        printConsignmentPickupReceipt(myConsignment, encodedSig, LINE_WIDTH);

        return true;
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        // TODO Auto-generated method stub\
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
    public void toggleBarcodeReader() {

    }

}
