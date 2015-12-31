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

import com.android.emobilepos.R;
import com.android.emobilepos.mainmenu.SalesTab_FR;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.PaymentDetails;
import com.android.support.ConsignmentTransaction;
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
import com.mpowa.android.sdk.powapos.core.PowaPOSEnums.BootloaderUpdateError;
import com.mpowa.android.sdk.powapos.core.PowaPOSEnums.PowaUSBCOMPort;
import com.mpowa.android.sdk.powapos.core.callbacks.PowaPOSCallback;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import main.EMSDeviceManager;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;

//com.mpowa.android.sdk.powapos.core.callbacks.PowaPOSCallbackBas
public class EMSPowaPOS extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {
    private int LINE_WIDTH = 48;

    private Handler handler;
    private ProgressDialog myProgressDialog;
    private EMSDeviceDriver thisInstance;
    private EMSDeviceManager edm;
    private EMSCallBack callBack, scannerCallBack;

    private boolean isAutoConnect = false;
    private Global global;

    private PowaHidScanner powaHidDecoderScanner;

    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
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
        } else {
            global.promptForMandatoryLogin(activity);
        }

        return true;
    }

    @Override
    public boolean isUSBConnected() {
        UsbManager manager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        return !deviceList.isEmpty();

    }

    public class processConnectionAsync extends AsyncTask<Integer, String, String> {

        String msg = new String();
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
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {

        printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);


        return true;
    }

    @Override
    public void releaseCardReader() {
        // TODO Auto-generated method stu
        callBack = null;
    }

    @Override
    public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {
        // TODO Auto-generated method stub
        callBack = _callBack;
    }

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

        new Thread(new Runnable() {
            public void run() {
                powaPOS.openCashDrawer();
            }
        }).start();
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        // TODO Auto-generated method stub
        printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);

        return true;
    }

    private PowaPOSCallback peripheralCallback = new PowaPOSCallback() {
        @Override
        public void onMCUInitialized(PowaPOSEnums.InitializedResult result) {

            if (myProgressDialog != null)
                myProgressDialog.dismiss();

            if (isAutoConnect && !global.loggedIn) {
                if (global.getGlobalDlog() != null)
                    global.getGlobalDlog().dismiss();
                global.promptForMandatoryLogin(activity);
                SalesTab_FR.startDefault(activity, myPref.getPreferencesValue(MyPreferences.pref_default_transaction));
            }

            if (result.equals(PowaPOSEnums.InitializedResult.SUCCESSFUL))
                edm.driverDidConnectToDevice(thisInstance, !isAutoConnect);
            else
                edm.driverDidNotConnectToDevice(thisInstance, "Failed to connect to MCU", !isAutoConnect);
        }

        @Override
        public void onMCUFirmwareUpdateStarted() {

        }

        @Override
        public void onScannerConnectionStateChanged(PowaEnums.ConnectionState newState) {
            if (newState.equals(PowaEnums.ConnectionState.CONNECTED)) {
                powaPOS.getScanner().scannerBeep(PowaPOSEnums.PowaScannerBeep.SHORT_2_BEEP_HIGH);
            }
        }

        @Override
        public void onScannerInitialized(final PowaPOSEnums.InitializedResult result) {
            if (result.equals(PowaPOSEnums.InitializedResult.SUCCESSFUL)) {

            } else {
                // scannerReconnect();
            }

        }

        @Override
        public void onMCUFirmwareUpdateProgress(int progress) {

        }

        @Override
        public void onMCUFirmwareUpdateFinished() {

        }

        @Override
        public void onCashDrawerStatus(PowaPOSEnums.CashDrawerStatus status) {
            // Dialog dialog = createAlertDialog("The cash drawer status is: " +
            // status.toString());
            // dialog.show();
            // Toast.makeText(activity, "cash drawer status",
            // Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRotationSensorStatus(PowaPOSEnums.RotationSensorStatus status) {
            // Dialog dialog = createAlertDialog("The rotation sensor status is:
            // " + status.toString());
            // dialog.show();
            // Toast.makeText(activity, "rotation sensor status",
            // Toast.LENGTH_LONG).show();
        }

        // @Override
        // public void onPrintJobCompleted(PowaPOSEnums.PrintJobResult result) {
        // // Toast.makeText(activity, "print job completed",
        // // Toast.LENGTH_LONG).show();
        // }

        @Override
        public void onScannerRead(String data) {
            // if(scannerFragment != null){
            // scannerFragment.scannerRead(data);
            // }
            // Toast.makeText(activity, "scanner read",
            // Toast.LENGTH_LONG).show();
            // Toast.makeText(activity, "Data: "+data,
            // Toast.LENGTH_LONG).show();
            // if(_scannerCallBack!=null)
            // {
            // Toast.makeText(activity, "Callback: "+data,
            // Toast.LENGTH_LONG).show();
            // _scannerCallBack.scannerWasRead(data);
            // }

            scannedData = data;
            handler.post(runnableScannedData);
        }

        @Override
        public void onMCUSystemConfiguration(Map<String, String> configuration) {
            // String str = "";
            // for(Map.Entry<String, String> data: configuration.entrySet()){
            // str += data.getKey() + ": " + data.getValue() + "\n";
            // }
            // Dialog dialog = createAlertDialog(str);
            // dialog.show();
            // Toast.makeText(activity, "MCU Sys Config",
            // Toast.LENGTH_LONG).show();
        }

        // @Override
        // public void onMCUDeviceAttached(PowaMsgHeader.DeviceType type) {
        //
        // //Toast.makeText(activity, "MCU Device Attached",
        // Toast.LENGTH_LONG).show();
        // }
        //
        // @Override
        // public void onMCUDeviceDetached(PowaMsgHeader.DeviceType type) {
        // //Toast.makeText(activity, "MCU Device Detached",
        // Toast.LENGTH_LONG).show();
        // }
        //
        // @Override
        // public void onMCUBootloaderUpdateFailed(BootloaderUpdateError arg0) {
        // // TODO Auto-generated method stub
        //
        // }

        @Override
        public void onMCUBootloaderUpdateFinished() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onMCUBootloaderUpdateProgress(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onMCUBootloaderUpdateStarted() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onUSBDeviceAttached(PowaUSBCOMPort arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onUSBDeviceDetached(PowaUSBCOMPort arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onUSBReceivedData(PowaUSBCOMPort arg0, byte[] arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onMCUBootloaderUpdateFailed(BootloaderUpdateError arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onHIDDeviceAttached(PowaPOSEnums.PowaHIDPort port, PowaPOSEnums.PowaHIDType type) {
            // Not used in this sample.
        }

        @Override
        public void onHIDDeviceDetached(PowaPOSEnums.PowaHIDPort port, PowaPOSEnums.PowaHIDType type) {
            // Not used in this sample.
        }

        @Override
        public void onHIDReceivedData(PowaPOSEnums.PowaHIDPort port, PowaPOSEnums.PowaHIDType type, byte[] data) {
            powaHidDecoderScanner.decode(port, type, data);
        }

    };

    @Override
    public void loadScanner(EMSCallBack _callBack) {
        // TODO Auto-generated method stub
        scannerCallBack = _callBack;
        if (handler == null)
            handler = new Handler();
        if (_callBack != null) {

            List<PowaDeviceObject> availScanners = powaPOS.getAvailableScanners();
            if (availScanners.size() > 0) {
                powaPOS.getScanner().selectScanner(availScanners.get(0));
                // powaPOS.selectScanner(availScanners.get(0));
            }
        }
    }

    String scannedData = "";

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

    @Override
    public void printHeader() {
        super.printHeader(LINE_WIDTH);
    }

    @Override
    public void printFooter() {
        super.printFooter(LINE_WIDTH);
    }

    PowaPOSCallback mPowaPOSCallback = new PowaPOSCallback() {
        @Override
        public void onMCUInitialized(PowaPOSEnums.InitializedResult initializedResult) {

            if (myProgressDialog != null)
                myProgressDialog.dismiss();

            if (isAutoConnect && !global.loggedIn) {
                if (global.getGlobalDlog() != null)
                    global.getGlobalDlog().dismiss();
                global.promptForMandatoryLogin(activity);
                SalesTab_FR.startDefault(activity, myPref.getPreferencesValue(MyPreferences.pref_default_transaction));
            }

            if (initializedResult.equals(PowaPOSEnums.InitializedResult.SUCCESSFUL))
                edm.driverDidConnectToDevice(thisInstance, !isAutoConnect);
            else
                edm.driverDidNotConnectToDevice(thisInstance, "Failed to connect to MCU", !isAutoConnect);
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

    };

    // ===================================== SCANNER CALLBACK
    // =======================================
    PowaHidScanner.Callback hidScannerCB = new PowaHidScanner.Callback() {
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
            // Some scanners send CONTROL KEYS at the start/end of the data,
            // this way users may identify
            // different events happening. Check if your scanner brand send any
            // of them by overriding this
            // method.
        }

        @Override
        public void onScanStartDecoding() {
            Log.d("", "onScanStartDecoding()");
            // This event is only useful for providing animation while the
            // decoding is in process.
            // Usually this process take about 1.5 second, so developers can
            // start playing a nice animation
            // at this point.
        }

        @Override
        public void onScanFinishedDecoding(byte[] data) {
            try {
                scannedData = new String(data, "UTF-8");
                handler.post(runnableScannedData);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };
}
