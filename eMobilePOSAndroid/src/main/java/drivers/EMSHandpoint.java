package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.handpoint.api.ConnectionMethod;
import com.handpoint.api.ConnectionStatus;
import com.handpoint.api.Currency;
import com.handpoint.api.Device;
import com.handpoint.api.Events;
import com.handpoint.api.FinancialStatus;
import com.handpoint.api.Hapi;
import com.handpoint.api.HapiFactory;
import com.handpoint.api.SignatureRequest;
import com.handpoint.api.StatusInfo;
import com.handpoint.api.TransactionResult;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import main.EMSDeviceManager;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;

/**
 * Created by Guarionex on 3/10/2016.
 */
public class EMSHandpoint extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, Events.Required, Events.Status {

    private CreditCardInfo cardManager;
    private Encrypt encrypt;
    private EMSDeviceManager edm;
    static Hapi hapi;
    String sharedSecret = "A110AEBBF5E0160A6F4427E052584C95CAD0C14072225CDD8B6E439FF0B976C1";
    protected static Device device;
    private Handler handler;
    private EMSCallBack msrCallBack;
    String msg = "Failed to connect";
    static boolean connected = false;
    private ProgressDialog myProgressDialog;


    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);
        this.edm = edm;
        if (hapi == null) {
            hapi = HapiFactory.getAsyncInterface(this, activity).defaultSharedSecret(sharedSecret);
        }
//        new ProcessConnectionAsync().execute();
        showDialog(R.string.connecting_handpoint);

//        myProgressDialog = new ProgressDialog(activity);
//        myProgressDialog.setMessage(activity.getString(R.string.connecting_handpoint));
//        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        myProgressDialog.setCancelable(false);
//        myProgressDialog.show();

        discoverDevices(myPref.getPrinterName(), myPref.getPrinterMACAddress());

    }


    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);
        this.edm = edm;
        if (hapi == null) {
            hapi = HapiFactory.getAsyncInterface(this, activity).defaultSharedSecret(sharedSecret);
        }
        synchronized (hapi) {
            discoverDevices(myPref.getPrinterName(), myPref.getPrinterMACAddress());
            try {
                hapi.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return connected;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        return false;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
        return false;
    }

    @Override
    public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint, EMVContainer emvContainer) {
        return false;
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
    public void printStationPrinter(List<Orders> orderProducts, String ordID) {

    }

    @Override
    public boolean printOpenInvoices(String invID) {
        return false;
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

    }

    @Override
    public boolean printReport(String curDate) {
        return false;
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {

    }

    @Override
    public void printEndOfDayReport(String date, String clerk_id, boolean printDetails) {

    }


    @Override
    public void registerAll() {
        this.registerPrinter();
    }

    @Override
    public void registerPrinter() {
        edm.currentDevice = this;
    }

    @Override
    public void unregisterPrinter() {
        hapi.disconnect();
    }


    private Runnable doUpdateDidConnect = new Runnable() {
        public void run() {
            try {
                if (msrCallBack != null)
                    msrCallBack.readerConnectedSuccessfully(true);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    @Override
    public void loadCardReader(EMSCallBack callBack, boolean isDebitCard) {
        if (handler == null)
            handler = new Handler();
        msrCallBack = callBack;
        if (!connected) {
            synchronized (hapi) {
                discoverDevices(myPref.getPrinterName(), myPref.getPrinterMACAddress());
                try {
                    hapi.wait();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        handler.post(doUpdateDidConnect);
    }

    @Override
    public void loadScanner(EMSCallBack _callBack) {

    }

    @Override
    public void releaseCardReader() {

    }

    @Override
    public void openCashDrawer() {

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

    }

    @Override
    public void printReceiptPreview(View view) {

    }


    public void discoverDevices(String deviceName, String macAddress) {
//        device = new Device(deviceName, macAddress, "", ConnectionMethod.BLUETOOTH);
        hapi.listDevices(ConnectionMethod.BLUETOOTH);
        // This triggers the search for all the bluetooth devices around.
    }


    //******************** Handpoint callbacks
    @Override
    public void signatureRequired(SignatureRequest signatureRequest, Device device) {
        hapi.signatureResult(true);
    }

    @Override
    public void endOfTransaction(TransactionResult transactionResult, Device device) {
        Looper.prepare();

        CreditCardInfo creditCardInfo = new CreditCardInfo();
        BigDecimal totalDec = new BigDecimal(transactionResult.getTotalAmount().intValue()).multiply(new BigDecimal(".01"));
        creditCardInfo.setOriginalTotalAmount(totalDec.toString());
        creditCardInfo.setWasSwiped(true);
        creditCardInfo.authcode = transactionResult.getAuthorisationCode();
        creditCardInfo.transid = transactionResult.getTransactionID();
        msrCallBack.cardWasReadSuccessfully(transactionResult.getFinStatus() == FinancialStatus.AUTHORISED, creditCardInfo);
        Looper.loop();
    }

    @Override
    public void connectionStatusChanged(ConnectionStatus connectionStatus, Device device) {
        Log.d("Handpoint connection:", connectionStatus.name());
        switch (connectionStatus) {
            case Connected: {
                connected = true;
                break;
            }
            case Disconnecting:
            case Disconnected: {
                connected = false;
                break;
            }
        }

    }

    @Override
    public void currentTransactionStatus(StatusInfo statusInfo, Device device) {
        StatusInfo.Status status = statusInfo.getStatus();

    }

    @Override
    public void deviceDiscoveryFinished(List<Device> devices) {
        for (Device device : devices) {
            if (device.getName() != null) {
                if (device.getName().equals(myPref.getPrinterName())) {
                    // Put the name of your device, find it by doing C then up arrow on your card reader keypad
                    EMSHandpoint.device = device;
                    hapi.useDevice(EMSHandpoint.device);
                    connected = true;
//                    boolean sale = hapi.sale(new BigInteger("123"), Currency.USD);
                }
            }
        }

        if (myProgressDialog != null && myProgressDialog.isShowing()) {
            dismissDialog();
            Looper.prepare();
            if (connected) {
                this.edm.driverDidConnectToDevice(this, true);
            } else {
                this.edm.driverDidNotConnectToDevice(this, msg, true);
            }
            Looper.loop();
        } else {
            synchronized (hapi) {
                hapi.notify();
                if (connected) {
                    this.edm.driverDidConnectToDevice(this, false);
                } else {
                    this.edm.driverDidNotConnectToDevice(this, msg, false);
                }
            }
        }
    }


    @Override
    public void salePayment(BigInteger amount) {
//        hapi.useDevice(device);
        boolean succeed = hapi.sale(amount, Currency.USD);
        if (!succeed) {
            Global.showPrompt(activity, R.string.payment, activity.getString(R.string.handpoint_payment_error));
        }
    }

    @Override
    public void saleReversal(Payment payment) {

    }

    @Override
    public void refund(BigInteger amount) {
        boolean succeed = hapi.refund(amount, Currency.USD);
        if (!succeed) {
            Global.showPrompt(activity, R.string.payment, activity.getString(R.string.handpoint_payment_error));
        }
    }

    @Override
    public void refundReversal(Payment payment) {

    }

    private void showDialog(int messageRsId) {
        myProgressDialog = new ProgressDialog(activity);
        myProgressDialog.setMessage(activity.getString(messageRsId));
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        myProgressDialog.setCancelable(false);
        myProgressDialog.show();
    }

    private void dismissDialog() {
        if (myProgressDialog != null && myProgressDialog.isShowing()) {
            myProgressDialog.dismiss();
        }
    }

    @Override
    public void printEMVReceipt(String html) {
        Global.showPrompt(activity, R.string.printing_message, html);
    }

}