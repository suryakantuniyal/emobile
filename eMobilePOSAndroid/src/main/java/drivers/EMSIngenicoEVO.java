package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
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
import com.evosnap.sdk.api.transaction.BankCardCapture;
import com.evosnap.sdk.api.transaction.CustomerPresence;
import com.evosnap.sdk.api.transaction.TransactionData;
import com.evosnap.sdk.api.transaction.TransactionStatus;
import com.evosnap.sdk.api.transaction.auth.BankCardTransactionResponse;
import com.evosnap.sdk.api.transaction.management.CancelTransactionRequest;
import com.evosnap.sdk.api.user.SignOnRequest;
import com.evosnap.sdk.api.user.SignOnResponse;
import com.evosnap.sdk.swiper.enums.CaptureMode;
import com.evosnap.sdk.swiper.enums.CaptureType;
import com.evosnap.sdk.swiper.enums.CurrencyCode;
import com.evosnap.sdk.swiper.enums.TransactionResult;
import com.handpoint.api.Device;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

public class EMSIngenicoEVO extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, TransactionCallbacks {

    private EMSDeviceManager edm;
    protected static Device device;
    private Handler handler;
    private EMSCallBack msrCallBack;
    String msg = "Failed to connect";
    static boolean connected = false;
    private ProgressDialog myProgressDialog;
    private ApiConfiguration apiConfig;
    private boolean deviceFound;


    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        showDialog(R.string.connecting_bluetooth_device);
        apiConfig = new ApiConfiguration(false);
        apiConfig.setApplicationProfileId("6883");
        apiConfig.setServiceKey("1F8BA60D09400001");
        apiConfig.setServiceId("39C6700001");
        new EVOConnectAsync().execute(true);
    }


    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        apiConfig = new ApiConfiguration(false);
        apiConfig.setApplicationProfileId("6883");
        apiConfig.setServiceKey("1F8BA60D09400001");
        apiConfig.setServiceId("39C6700001");
        new EVOConnectAsync().execute(false);
        return true;
    }

    private class EVOConnectAsync extends AsyncTask<Boolean, Void, SignOnResponse> {

        private boolean showMsg;

        @Override
        protected SignOnResponse doInBackground(Boolean... params) {
            showMsg = params[0];
            deviceFound = true;
            EvoSnapApi.init(activity, apiConfig);
            SignOnRequest request = new SignOnRequest("enabler1", "Testing!2$");
            return EvoSnapApi.signOn(request);
        }

        @Override
        protected void onPostExecute(SignOnResponse signOnResponse) {
            dismissDialog();
            connected = signOnResponse.isSuccessful() && deviceFound;
            if (connected) {
                edm.driverDidConnectToDevice(EMSIngenicoEVO.this, showMsg);
            } else {
                edm.driverDidNotConnectToDevice(EMSIngenicoEVO.this, msg, showMsg);
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
    public void printStationPrinter(List<Orders> orderProducts, String ordID, boolean cutPaper) {

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
    public void salePayment(Payment payment) {
        TransactionData transactionData = new TransactionData();
        transactionData.setCustomerPresence(CustomerPresence.PRESENT);
        transactionData.setOrderNumber("");
        transactionData.setWasSignatureCaptured(true);
        transactionData.setTipAmount(new BigDecimal(0.00));
        transactionData.setAmount(new BigDecimal(payment.pay_amount));

        ApplicationConfigurationData configurationData = new ApplicationConfigurationData();
        configurationData.setApplicationAttended(true);
        configurationData.setApplicationLocation(ApplicationLocation.ON_PREMISES);
        configurationData.setHardwareType(HardwareType.PC);
        configurationData.setPinCapability(PinCapability.PIN_VERIFIED_BY_DEVICE);
        configurationData.setReadCapability(ReadCapability.MSREMVICC);
        configurationData.setEncryptionType(EncryptionType.NOT_SET);
        EvoSnapApi.startTransaction(transactionData, CaptureMode.SWIPE_OR_INSERT, CaptureType.AUTH_AND_CAPTURE,
                CurrencyCode.USD, configurationData, this);
    }

    @Override
    public void saleReversal(Payment payment, String originalTransactionId) {
        new EVOCancelTransaction().execute(payment);
    }

    private class EVOCancelTransaction extends AsyncTask<Payment, Void, BankCardTransactionResponse> {

        @Override
        protected void onPreExecute() {
//            showDialog(R.string.voiding_payments);
        }

        @Override
        protected BankCardTransactionResponse doInBackground(Payment... params) {
            BankCardCapture bankCardCapture = new BankCardCapture();
            bankCardCapture.setTransactionId(params[0].pay_transid);
            bankCardCapture.setAmount(Global.getBigDecimalNum(params[0].pay_amount));
            bankCardCapture.setType("Undo");
            CancelTransactionRequest cancelRequest = new CancelTransactionRequest();
            cancelRequest.setDifferenceData(bankCardCapture);
            BankCardTransactionResponse response = EvoSnapApi.cancelTransaction(cancelRequest);
            return response;
        }

        @Override
        protected void onPostExecute(BankCardTransactionResponse bankCardTransactionResponse) {
            msrCallBack.cardWasReadSuccessfully(bankCardTransactionResponse.getStatus() == TransactionStatus.SUCCESSFUL,
                    new CreditCardInfo());
        }
    }


    @Override
    public void refund(Payment payment) {
        TransactionData transactionData = new TransactionData();
        transactionData.setCustomerPresence(CustomerPresence.PRESENT);
        transactionData.setOrderNumber(payment.pay_id);
        transactionData.setWasSignatureCaptured(true);
        transactionData.setTipAmount(new BigDecimal(0.00));
        transactionData.setAmount(new BigDecimal(payment.pay_amount));
        ApplicationConfigurationData configurationData = new ApplicationConfigurationData();
        configurationData.setApplicationAttended(true);
        configurationData.setApplicationLocation(ApplicationLocation.ON_PREMISES);
        configurationData.setHardwareType(HardwareType.PC);
        configurationData.setPinCapability(PinCapability.PIN_VERIFIED_BY_DEVICE);
        configurationData.setReadCapability(ReadCapability.MSREMVICC);
        configurationData.setEncryptionType(EncryptionType.NOT_SET);

        EvoSnapApi.startTransaction(transactionData, CaptureMode.SWIPE_OR_INSERT, CaptureType.RETURN_UNLINKED,
                CurrencyCode.USD, configurationData, this);
    }

    @Override
    public void refundReversal(Payment payment, String originalTransactionId) {

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
    public void sendEmailLog() {

    }

    @Override
    public void updateFirmware() {

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
        deviceFound = false;
        connected = false;
    }

    @Override
    public void onWaitingForCard() {
        Looper.prepare();
        Toast.makeText(activity, "onWaitingForCard", Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    @Override
    public void onCardInserted() {
        Looper.prepare();
        Toast.makeText(activity, "onCardInserted", Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    @Override
    public void onCardSwiped(String s, String s1, String s2, String s3) {
        Looper.prepare();
        CreditCardInfo creditCardInfo = new CreditCardInfo();
        creditCardInfo.setWasSwiped(true);
        creditCardInfo.setCardOwnerName(s1);
        creditCardInfo.setCardExpMonth(s2);
        creditCardInfo.setCardExpYear(s3);
        creditCardInfo.setCardNumUnencrypted(s);
        msrCallBack.cardWasReadSuccessfully(true, creditCardInfo);
        Toast.makeText(activity, "onCardSwiped", Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    @Override
    public void onRequestSetAmount() {
        Looper.prepare();
        Toast.makeText(activity, "onRequestSetAmount", Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    @Override
    public void onRequestApplicationSelection(ArrayList<String> arrayList) {
        Looper.prepare();
        Toast.makeText(activity, "onRequestApplicationSelection", Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    @Override
    public void onRequestAmountConfirmation(BigDecimal bigDecimal) {
        Looper.prepare();
        Toast.makeText(activity, "onRequestAmountConfirmation", Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    @Override
    public void onRequestPinEntry() {
        Looper.prepare();
        Toast.makeText(activity, "onRequestPinEntry", Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    @Override
    public void onRequestVerifyId() {
        Looper.prepare();
        Toast.makeText(activity, "onRequestVerifyId", Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    @Override
    public void onTransactionCompleted(TransactionResult transactionResult, BankCardTransactionResponse bankCardTransactionResponse) {
        Looper.prepare();
        CreditCardInfo creditCardInfo = new CreditCardInfo();
        creditCardInfo.setOriginalTotalAmount(bankCardTransactionResponse == null ? "0" : bankCardTransactionResponse.getAmount().toString());
        creditCardInfo.setWasSwiped(true);
        creditCardInfo.authcode = bankCardTransactionResponse.getApprovalCode();
        creditCardInfo.transid = bankCardTransactionResponse.getTransactionId();
        creditCardInfo.setResultMessage(bankCardTransactionResponse.getStatusMessage());
        msrCallBack.cardWasReadSuccessfully(transactionResult == TransactionResult.APPROVED, creditCardInfo);
        Looper.loop();
    }

    @Override
    public void onTransactionError(EvoSnapApi.TransactionError transactionError) {
        Looper.prepare();
        msrCallBack.cardWasReadSuccessfully(false, null);
        Looper.prepare();
    }

    @Override
    public void onRequestOverrideConfirmation() {
        Looper.prepare();
        Toast.makeText(activity, "onRequestOverrideConfirmation", Toast.LENGTH_LONG).show();
        Looper.loop();
    }
}
