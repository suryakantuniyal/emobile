package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.StarMicronics.jasura.JAException;
import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.SalesTab_FR;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.mpowa.android.powapos.accessory.abstraction.PowaHidKeyDecoder;
import com.mpowa.android.powapos.accessory.hid.PowaHidScanner;
import com.mpowa.android.sdk.powapos.PowaPOS;
import com.mpowa.android.sdk.powapos.common.base.PowaEnums;
import com.mpowa.android.sdk.powapos.common.base.PowaLog;
import com.mpowa.android.sdk.powapos.common.dataobjects.PowaDeviceObject;
import com.mpowa.android.sdk.powapos.common.utils.ByteUtils;
import com.mpowa.android.sdk.powapos.core.PowaPOSEnums;
import com.mpowa.android.sdk.powapos.core.callbacks.PowaPOSCallback;
import com.starmicronics.stario.StarIOPortException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

public class EMSPowaPOS extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {
    private String scannedData = "";
    private int LINE_WIDTH = 48;
    private Handler handler;
    private ProgressDialog myProgressDialog;
    private EMSDeviceDriver thisInstance;
    private EMSDeviceManager edm;
    private EMSCallBack scannerCallBack;
    private boolean isAutoConnect = false;
    private Global global;
    private PowaHidScanner powaHidDecoderScanner;
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
    private PowaPOSCallback mPowaPOSCallback = new PowaPOSCallback() {
        @Override
        public void onMCUInitialized(PowaPOSEnums.InitializedResult initializedResult) {

            if (myProgressDialog != null)
                myProgressDialog.dismiss();

            if (isAutoConnect && !Global.loggedIn) {
                if (global.getGlobalDlog() != null)
                    global.getGlobalDlog().dismiss();
                global.promptForMandatoryLogin(activity);
                String value = myPref.getPreferencesValue(MyPreferences.pref_default_transaction);
                Global.TransactionType type = Global.TransactionType.getByCode(Integer.parseInt(value));
                SalesTab_FR.startDefault((Activity) activity, type);
            }

            if (initializedResult.equals(PowaPOSEnums.InitializedResult.SUCCESSFUL))
                edm.driverDidConnectToDevice(thisInstance, !isAutoConnect, activity);
            else
                edm.driverDidNotConnectToDevice(thisInstance, "Failed to connect to MCU", !isAutoConnect, activity);
        }

        @Override
        public void onScannerInitialized(PowaPOSEnums.InitializedResult initializedResult) {
            Log.d("", "onScannerInitialized()");
        }

        @Override
        public void onHIDDeviceAttached(PowaPOSEnums.PowaHIDPort port, PowaPOSEnums.PowaHIDType type) {
            Log.d("", "onHIDDeviceAttached()");
        }

        @Override
        public void onHIDDeviceDetached(PowaPOSEnums.PowaHIDPort port, PowaPOSEnums.PowaHIDType type) {
            Log.d("", "onHIDDeviceDetached()");
        }

        @Override
        public void onHIDReceivedData(PowaPOSEnums.PowaHIDPort port, PowaPOSEnums.PowaHIDType type, byte[] data) {
            PowaLog.getInstance().logInternal("TAG", ByteUtils.byteArrayToHexStringPretty(data));
            powaHidDecoderScanner.decode(port, type, data);

        }

        @Override
        public void onScannerRead(String data) {
            scannedData = data;
            handler.post(runnableScannedData);
        }

        @Override
        public void onScannerConnectionStateChanged(PowaEnums.ConnectionState newState) {
            if (newState.equals(PowaEnums.ConnectionState.CONNECTED)) {
                powaPOS.getScanner().scannerBeep(PowaPOSEnums.PowaScannerBeep.SHORT_2_BEEP_HIGH);
                powaPOS.getScanner().setScannerAutoScan(true);
            }
        }

    };
    // ===================================== SCANNER CALLBACK
    // =======================================
    private PowaHidScanner.Callback hidScannerCB = new PowaHidScanner.Callback() {
        @Override
        public void onScannerReady() {
            Log.d("", "onScannerReady()");
        }

        @Override
        public void onScannerDetached() {
            Log.d("", "onScannerDetached()");
        }

        @Override
        public void onControlKeyScanned(PowaHidKeyDecoder.CONTROL_KEY controlKey) {
            Toast.makeText(EMSPowaPOS.this.activity, controlKey.name() + " has been received", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onScanStartDecoding() {
            Log.d("", "onScanStartDecoding()");
        }

        @Override
        public void onScanFinishedDecoding(byte[] data) {
            try {
                if (handler != null) {
                    scannedData = new String(data, "UTF-8");
                    handler.post(runnableScannedData);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        thisInstance = this;

        if (isUSBConnected()) {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(activity.getString(R.string.progress_connecting_printer));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
            try {
                powaPOS = new PowaPOS(this.activity, mPowaPOSCallback);
                powaPOS.initializeMCU(true);
                powaPOS.initializeScanner();
                powaHidDecoderScanner = new PowaHidScanner(hidScannerCB);

            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        thisInstance = this;
        isAutoConnect = true;
        global = (Global) activity.getApplication();

        if (isUSBConnected()) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(activity.getString(R.string.progress_connecting_printer));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        powaPOS = new PowaPOS(EMSPowaPOS.this.activity, mPowaPOSCallback);
                        powaPOS.initializeMCU(true);
                        powaPOS.initializeScanner();
                        powaHidDecoderScanner = new PowaHidScanner(hidScannerCB);

                        myProgressDialog.dismiss();
                        Looper.loop();
                    }
                }).start();

            } catch (Exception e) {
            }
            this.edm.driverDidConnectToDevice(thisInstance, false, activity);
        } else {
            global.promptForMandatoryLogin(activity);
        }
        return true;
    }

    @Override
    public boolean isUSBConnected() {
        UsbManager manager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        return !deviceList.isEmpty();
    }

    @Override
    public void toggleBarcodeReader() {
    }

    @Override
    public void printReceiptPreview(SplittedOrder splitedOrder) {
        try {
            setPaperWidth(LINE_WIDTH);
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
    public void printExpenseReceipt(ShiftExpense expense) {
        printExpenseReceipt(LINE_WIDTH, expense);
    }

    @Override
    public void registerAll() {
        this.registerPrinter();
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
    public void turnOnBCR() {

    }

    @Override
    public void turnOffBCR() {

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
        edm.setCurrentDevice(this);
    }

    @Override
    public void unregisterPrinter() {
        edm.setCurrentDevice(null);
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

        new Thread(new Runnable() {
            public void run() {
                if (powaPOS != null) {
                    powaPOS.openCashDrawer();
                }
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
        scannerCallBack = _callBack;
        if (handler == null)
            handler = new Handler();
        if (_callBack != null && powaPOS != null) {
            List<PowaDeviceObject> availScanners = powaPOS.getAvailableScanners();
            if (availScanners.size() > 0) {
                powaPOS.getScanner().selectScanner(availScanners.get(0));
            }
        } else if (powaPOS != null) {
            List<PowaDeviceObject> availScanners = powaPOS.getAvailableScanners();
            if (availScanners.size() > 0) {
                powaPOS.getScanner().selectScanner(availScanners.get(0));
                powaPOS.getScanner().setScannerAutoScan(false);
                powaPOS.getScanner().scannerBeep(PowaPOSEnums.PowaScannerBeep.FAST_WARBLE_BEEP_4_HI_LO_HI_LO);
            }
        }
    }

    @Override
    public void printHeader() {
        super.printHeader(LINE_WIDTH);
    }

    @Override
    public void printFooter() {
        super.printFooter(LINE_WIDTH);
    }

    public class processConnectionAsync extends AsyncTask<Integer, String, String> {

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

            try {
                powaPOS = new PowaPOS(EMSPowaPOS.this.activity, mPowaPOSCallback);
                powaPOS.initializeMCU(true);
                powaPOS.initializeScanner();
                powaHidDecoderScanner = new PowaHidScanner(hidScannerCB);
                didConnect = true;
            } catch (Exception e) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();

            Toast.makeText(activity, "finished connection", Toast.LENGTH_LONG).show();
        }
    }
}
