package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;

import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.evosnap.sdk.api.ApiConfiguration;
import com.evosnap.sdk.api.EvoSnapApi;
import com.evosnap.sdk.api.TransactionCallbacks;
import com.evosnap.sdk.api.config.ApplicationConfigurationData;
import com.evosnap.sdk.api.config.ApplicationLocation;
import com.evosnap.sdk.api.config.EncryptionType;
import com.evosnap.sdk.api.config.HardwareType;
import com.evosnap.sdk.api.config.PinCapability;
import com.evosnap.sdk.api.config.ReadCapability;
import com.evosnap.sdk.api.transaction.CustomerPresence;
import com.evosnap.sdk.api.transaction.TransactionData;
import com.evosnap.sdk.api.transaction.auth.BankCardTransactionResponse;
import com.evosnap.sdk.api.user.SignOnRequest;
import com.evosnap.sdk.api.user.SignOnResponse;
import com.evosnap.sdk.swiper.enums.CaptureMode;
import com.evosnap.sdk.swiper.enums.CaptureType;
import com.evosnap.sdk.swiper.enums.CurrencyCode;
import com.evosnap.sdk.swiper.enums.TransactionResult;
import com.handpoint.api.Device;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;
import rba_sdk.Comm_Timeout;

public class EMSIngenicoEVO extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, TransactionCallbacks {

    private EMSDeviceManager edm;
    String sharedSecret = "A110AEBBF5E0160A6F4427E052584C95CAD0C14072225CDD8B6E439FF0B976C1";
    protected static Device device;
    private Handler handler;
    private EMSCallBack msrCallBack;
    String msg = "Failed to connect";
    static boolean connected = false;
    private ProgressDialog myProgressDialog;
    private ApiConfiguration apiConfig;


    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
//        showDialog(R.string.connecting_bluetooth_device);
        apiConfig = new ApiConfiguration(false);
        apiConfig.setApplicationProfileId("6883");
        apiConfig.setServiceKey("1F8BA60D09400001");
        apiConfig.setServiceId("39C6700001");
//        setCommTimeOuts();
//        apiConfig.setWorkflowId("A121700011");
        new EVOConnectAsync().execute();
    }


    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        return connected;
    }

    private class EVOConnectAsync extends AsyncTask<Void, Void, SignOnResponse> {

        @Override
        protected SignOnResponse doInBackground(Void... params) {
            EvoSnapApi.init(activity, apiConfig);
            SignOnRequest request = new SignOnRequest("enabler1", "Testing!2$");
            SignOnResponse signOnResponse = EvoSnapApi.signOn(request);
            return signOnResponse;
        }

        @Override
        protected void onPostExecute(SignOnResponse signOnResponse) {
            String sessionToken = signOnResponse.getSessionToken();
            connected = signOnResponse.isSuccessful();
            if (connected) {
                edm.driverDidConnectToDevice(EMSIngenicoEVO.this, true);
                salePayment(new BigInteger("123"));
            } else {
                edm.driverDidNotConnectToDevice(EMSIngenicoEVO.this, msg, true);
            }

        }
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

    @Override
    public void salePayment(BigInteger amount) {
        TransactionData transactionData = new TransactionData();
        transactionData.setCustomerPresence(CustomerPresence.PRESENT);
        transactionData.setOrderNumber("");
        transactionData.setWasSignatureCaptured(true);
        transactionData.setTipAmount(new BigDecimal(1.00));
        transactionData.setAmount(new BigDecimal(amount).multiply(new BigDecimal("0.01")));

        ApplicationConfigurationData configurationData = new ApplicationConfigurationData();
        configurationData.setApplicationAttended(true);
        configurationData.setApplicationLocation(ApplicationLocation.ON_PREMISES);
        configurationData.setHardwareType(HardwareType.PC);
        configurationData.setPinCapability(PinCapability.PIN_VERIFIED_BY_DEVICE);
        configurationData.setReadCapability(ReadCapability.MSREMVICC);
        configurationData.setEncryptionType(EncryptionType.NOT_SET);

        EvoSnapApi.startTransaction(transactionData, CaptureMode.SWIPE_OR_INSERT, CaptureType.AUTH_AND_CAPTURE, CurrencyCode.USD, configurationData, this);
    }

    @Override
    public void saleReversal(BigInteger amount, String originalTransactionId) {

    }

    @Override
    public void refund(BigInteger amount) {

    }

    @Override
    public void refundReversal(Payment payment) {

    }


    private void showDialog(int messageRsId) {
        if (myProgressDialog != null && myProgressDialog.isShowing()) {
            myProgressDialog.dismiss();
        }
        myProgressDialog = new ProgressDialog(activity);
        myProgressDialog.setMessage(activity.getString(messageRsId));
        myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        myProgressDialog.setCancelable(true);
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

    @Override
    public void onDeviceConnected() {
        connected = true;
        if (myProgressDialog != null && myProgressDialog.isShowing()) {
            myProgressDialog.dismiss();
            this.edm.driverDidConnectToDevice(this, true);

        }
    }

    @Override
    public void onDeviceDisconnected() {
        connected = false;
        if (myProgressDialog != null && myProgressDialog.isShowing()) {
            myProgressDialog.dismiss();
            this.edm.driverDidNotConnectToDevice(this, msg, true);
        }
    }

    @Override
    public void onNoDeviceFound() {
        connected = false;
    }

    @Override
    public void onWaitingForCard() {

    }

    @Override
    public void onCardInserted() {

    }

    @Override
    public void onCardSwiped(String s, String s1, String s2, String s3) {

    }

    @Override
    public void onRequestSetAmount() {

    }

    @Override
    public void onRequestApplicationSelection(ArrayList<String> arrayList) {

    }

    @Override
    public void onRequestAmountConfirmation(BigDecimal bigDecimal) {

    }

    @Override
    public void onRequestPinEntry() {

    }

    @Override
    public void onRequestVerifyId() {

    }

    @Override
    public void onTransactionCompleted(TransactionResult transactionResult, BankCardTransactionResponse bankCardTransactionResponse) {

    }

    @Override
    public void onTransactionError(EvoSnapApi.TransactionError transactionError) {

    }

    @Override
    public void onRequestOverrideConfirmation() {

    }
}
