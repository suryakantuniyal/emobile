package drivers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.android.emobilepos.BuildConfig;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.ingenico.mpos.sdk.Ingenico;
import com.ingenico.mpos.sdk.callbacks.LoginCallback;
import com.ingenico.mpos.sdk.constants.ResponseCode;
import com.ingenico.mpos.sdk.data.UserProfile;
import com.roam.roamreaderunifiedapi.callback.DeviceStatusHandler;
import com.roam.roamreaderunifiedapi.constants.CommunicationType;
import com.roam.roamreaderunifiedapi.constants.DeviceType;
import com.roam.roamreaderunifiedapi.data.Device;

import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

/**
 * Created by Luis Camayd on 12/4/2018.
 */
public class EMSIngenicoMoby85
        extends EMSDeviceDriver
        implements EMSDeviceManagerPrinterDelegate, DeviceStatusHandler {

    private final static String API_KEY = "CAT6-64a80ac1-0ff3-4d32-ac92-5558a6870a88";
    private final static String BASE_URL = "https://uatmcm.roamdata.com/";
    private final static String CLIENT_VERSION = "0.1";
    private final static String DEVICE_NAME = "MOBY8500";
    private final static String USERNAME = "enablercorptest1";
    private final static String PASSWORD = "ForIngenico100";
    private final static DeviceType DEVICE_TYPE = DeviceType.MOBY8500;
    private final static CommunicationType COMMUNICATION_TYPE = CommunicationType.Bluetooth;

    private EMSDeviceManager edm;
    private boolean isAutoConnect = false;


    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter,
                        EMSDeviceManager edm) {
        this.activity = activity;
        this.edm = edm;
        myPref = new MyPreferences(this.activity);
        isAutoConnect = false;

        if (myPref.getSwiperMACAddress() != null) {
            initializeIngenicoSDK();
        }
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize,
                               boolean isPOSPrinter, String portName, String portNumber) {
        this.activity = activity;
        this.edm = edm;
        myPref = new MyPreferences(this.activity);
        isAutoConnect = true;

        if (myPref.getSwiperMACAddress() != null) {
            initializeIngenicoSDK();
            return true;
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    private void initializeIngenicoSDK() {
        Device device = new Device(
                DEVICE_TYPE,
                COMMUNICATION_TYPE,
                DEVICE_NAME,
                myPref.getSwiperMACAddress()
        );

        Ingenico ingenico = Ingenico.getInstance();
        ingenico.initialize(activity.getApplicationContext(), BASE_URL, API_KEY, CLIENT_VERSION);
        ingenico.setLogging(BuildConfig.DEBUG);
        ingenico.device().setDeviceType(DEVICE_TYPE);
        ingenico.device().select(device);
        ingenico.device().initialize(activity.getApplicationContext());
        ingenico.device().registerConnectionStatusUpdates(EMSIngenicoMoby85.this);
        ingenico.user().login(USERNAME, PASSWORD, new LoginCallbackImpl());
    }


    // region Device Driver Unused Methods

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes,
                                    boolean isFromHistory, boolean fromOnHold,
                                    EMVContainer emvContainer) {
        return false;
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes,
                                    boolean isFromHistory, boolean fromOnHold,
                                    EMVContainer emvContainer) {
        return false;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes,
                                    boolean isFromHistory, boolean fromOnHold) {
        return false;
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes,
                                    boolean isFromHistory, boolean fromOnHold) {
        return false;
    }

    @Override
    public boolean printPaymentDetails(String payID, int isFromMainMenu,
                                       boolean isReprint, EMVContainer emvContainer) {
        return false;
    }

    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        return false;
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment,
                                    String encodedSignature) {
        return false;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment,
                                          String encodedSignature) {
        return false;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c,
                                           boolean isPickup) {
        return false;
    }

    @Override
    public String printStationPrinter(List<Orders> orderProducts, String ordID,
                                      boolean cutPaper, boolean printHeader) {
        return null;
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
    public void turnOnBCR() {

    }

    @Override
    public void turnOffBCR() {

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
    public void registerPrinter() {

    }

    @Override
    public void unregisterPrinter() {

    }

    @Override
    public void loadCardReader(EMSCallBack callBack, boolean isDebitCard) {

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
    public void printReceiptPreview(SplittedOrder splitedOrder) {

    }

    @Override
    public void salePayment(Payment payment, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void saleReversal(Payment payment, String originalTransactionId,
                             CreditCardInfo creditCardInfo) {

    }

    @Override
    public void refund(Payment payment, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void refundReversal(Payment payment, String originalTransactionId,
                               CreditCardInfo creditCardInfo) {

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
        return false;
    }

    @Override
    public void printClockInOut(List<ClockInOut> clockInOuts, String clerkID) {

    }

    @Override
    public void printExpenseReceipt(ShiftExpense expense) {

    }

    // endregion


    @Override
    public void onConnected() {
        if (isAutoConnect) {
            edm.driverDidConnectToDevice(EMSIngenicoMoby85.this,
                    false, activity);
        } else {
            edm.driverDidConnectToDevice(EMSIngenicoMoby85.this,
                    true, activity);
        }
    }

    @Override
    public void onDisconnected() {
        ((Activity) activity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "Payment Device Disconnected!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onError(String s) {
        if (isAutoConnect) {
            edm.driverDidNotConnectToDevice(EMSIngenicoMoby85.this,
                    null, false, activity);
        } else {
            edm.driverDidNotConnectToDevice(EMSIngenicoMoby85.this,
                    "Please turn the payment device on and try again.",
                    true, activity);
        }
    }

    private class LoginCallbackImpl implements LoginCallback {
        @Override
        public void done(Integer responseCode, UserProfile user) {
            if (ResponseCode.Success != responseCode) {
                edm.driverDidNotConnectToDevice(EMSIngenicoMoby85.this,
                        "Ingenico's user login failed. Please check credentials and try again.",
                        true, activity);
            }
        }
    }
}