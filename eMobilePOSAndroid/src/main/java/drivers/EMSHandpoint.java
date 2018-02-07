package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.emobilepos.R;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMSEpayLoginInfo;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.emsutils.EMSUtils;
import com.handpoint.api.ConnectionMethod;
import com.handpoint.api.ConnectionStatus;
import com.handpoint.api.Device;
import com.handpoint.api.Events;
import com.handpoint.api.FinancialStatus;
import com.handpoint.api.Hapi;
import com.handpoint.api.HapiFactory;
import com.handpoint.api.LogLevel;
import com.handpoint.api.SignatureRequest;
import com.handpoint.api.StatusInfo;
import com.handpoint.api.TransactionResult;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

/**
 * Created by Guarionex on 3/10/2016.
 */
public class EMSHandpoint extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, Events.Required, Events.Status, Events.Log, Events.PendingResults {

    protected static Device device;
    static Hapi hapi;
    static boolean connected = false;
    private static String sharedSecret = "A110AEBBF5E0160A6F4427E052584C95CAD0C14072225CDD8B6E439FF0B976C1";
    private static ProgressDialog myProgressDialog;
    String msg = "Failed to connect";
    com.handpoint.api.Currency currency = com.handpoint.api.Currency.valueOf(java.util.Currency.getInstance(Locale.getDefault()).getCurrencyCode());
    private EMSDeviceManager edm;
    private Handler handler;
    private EMSCallBack msrCallBack;
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
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        new WorkingKeyRequest().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

//    public static String getWorkingKey(String xml, Activity activity) {
//        SAXParserFactory spf = SAXParserFactory.newInstance();
//        SAXProcessCardPayHandler handler = new SAXProcessCardPayHandler(activity);
//        InputSource inSource = new InputSource(new StringReader(xml));
//        String workingKey = "";
//        SAXParser sp;
//        try {
//            sp = spf.newSAXParser();
//            XMLReader xr = sp.getXMLReader();
//            xr.setContentHandler(handler);
//            xr.parse(inSource);
//            HashMap<String, String> parsedMap = handler.getData();
//
//            String errorMsg;
//            if (parsedMap != null && parsedMap.size() > 0
//                    && parsedMap.get("epayStatusCode").equals("APPROVED")) {
//                workingKey = parsedMap.get("WorkingKey");
//            } else if (parsedMap != null && parsedMap.size() > 0) {
//                errorMsg = "statusCode = " + parsedMap.get("statusCode") + "\n" + parsedMap.get("statusMessage");
//            } else {
//                errorMsg = xml;
//            }
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return workingKey;
//    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        if (hapi == null) {

//            EMSPayGate_Default payGate = new EMSPayGate_Default(activity, null);
//            String request = payGate.paymentWithAction(EMSPayGate_Default.EAction.HandpointWorkingKey, false, null,
//                    null);
//            Post httpClient = new Post();
//            String xml = httpClient.postData(Global.S_SUBMIT_WORKINGKEY_REQUEST, activity, request);
            EMSEpayLoginInfo loginInfo = EMSUtils.getEmsEpayLoginInfo(activity);
            sharedSecret = loginInfo.getSecret();//getWorkingKey(xml, activity);
            if (TextUtils.isEmpty(sharedSecret)) {
                return false;
            }
            hapi = HapiFactory.getAsyncInterface(this, activity).defaultSharedSecret(sharedSecret);
        }
        synchronized (hapi) {
            discoverDevices(myPref.getPrinterName(), myPref.getSwiperMACAddress());
            try {
                hapi.wait(10000);
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
    public String printStationPrinter(List<Orders> orderProducts, String ordID, boolean cutPaper, boolean printHeader) {
        return "";
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
    public void registerAll() {
        this.registerPrinter();
    }

    @Override
    public void registerPrinter() {
        edm.setCurrentDevice(this);
    }

    @Override
    public void unregisterPrinter() {
        hapi.disconnect();
    }

    @Override
    public void loadCardReader(EMSCallBack callBack, boolean isDebitCard) {
        if (handler == null)
            handler = new Handler();
        msrCallBack = callBack;
        if (!connected) {
            synchronized (hapi) {
                discoverDevices(myPref.getPrinterName(), myPref.getSwiperMACAddress());
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
    public void printReceiptPreview(SplittedOrder splitedOrder) {

    }
//
//    @Override
//    public void printReceiptPreview(View view) {
//
//    }

    public void discoverDevices(String deviceName, String macAddress) {
        device = new Device(deviceName, macAddress, "", ConnectionMethod.BLUETOOTH);
        if (!TextUtils.isEmpty(device.getAddress()) && !TextUtils.isEmpty(sharedSecret)) {
            EMSHandpoint.hapi.useDevice(EMSHandpoint.device);
            connected = true;
            if (myProgressDialog != null && myProgressDialog.isShowing()) {
                dismissDialog();
//                Looper.prepare();
                if (connected) {
                    this.edm.driverDidConnectToDevice(this, true, activity);
                } else {
                    this.edm.driverDidNotConnectToDevice(this, msg, true, activity);
                }
//                Looper.loop();
            } else {
                synchronized (hapi) {
                    hapi.notifyAll();
                    if (connected) {
                        this.edm.driverDidConnectToDevice(this, false, activity);
                    } else {
                        this.edm.driverDidNotConnectToDevice(this, msg, false, activity);
                    }
                }
            }
        } else {
            this.edm.driverDidNotConnectToDevice(this, msg, false, activity);
        }
//        hapi.listDevices(ConnectionMethod.BLUETOOTH);
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
        creditCardInfo.transid = transactionResult.geteFTTransactionID();
        creditCardInfo.setEmvContainer(new EMVContainer(transactionResult));
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
                if (device.getName().equals(myPref.getSwiperName())) {
                    // Put the name of your device, find it by doing C then up arrow on your card reader keypad
                    EMSHandpoint.device = device;
                    EMSHandpoint.hapi.useDevice(EMSHandpoint.device);
                    connected = true;
                }
            }
        }

        if (myProgressDialog != null && myProgressDialog.isShowing()) {
            dismissDialog();
            Looper.prepare();
            if (connected) {
                this.edm.driverDidConnectToDevice(this, true, activity);
            } else {
                this.edm.driverDidNotConnectToDevice(this, msg, true, activity);
            }
            Looper.loop();
        } else {
            synchronized (hapi) {
                hapi.notifyAll();
                if (connected) {
                    this.edm.driverDidConnectToDevice(this, false, activity);
                } else {
                    this.edm.driverDidNotConnectToDevice(this, msg, false, activity);
                }
            }
        }
    }

    @Override
    public void salePayment(Payment payment, CreditCardInfo creditCardInfo) {
        hapi.addPendingResultsEventHandler(this);
        hapi.getPendingTransaction();
        boolean succeed = hapi.sale(new BigInteger(payment.getPay_amount().replace(".", "")), currency);
        if (!succeed && activity != null && !((Activity) activity).isFinishing()) {
            Global.showPrompt(activity, R.string.payment, activity.getString(R.string.handpoint_payment_error));
        }
    }

    @Override
    public void saleReversal(Payment payment, String originalTransactionId, CreditCardInfo creditCardInfo) {
        hapi.getPendingTransaction();
        boolean succeed = hapi.saleReversal(new BigInteger(payment.getPay_amount().replace(".", "")), currency, originalTransactionId);
        if (!succeed) {
            Global.showPrompt(activity, R.string.payment, activity.getString(R.string.handpoint_payment_error));
        }
    }

    @Override
    public void refund(Payment payment, CreditCardInfo creditCardInfo) {
        hapi.addPendingResultsEventHandler(this);
        hapi.getPendingTransaction();
        boolean succeed = hapi.refund(new BigInteger(payment.getPay_amount().replace(".", "")), currency);
        if (!succeed) {
            dismissDialog();
            Global.showPrompt(activity, R.string.payment, activity.getString(R.string.handpoint_payment_error));
        }
    }

    @Override
    public void refundReversal(Payment payment, String originalTransactionId, CreditCardInfo creditCardInfo) {
        hapi.addPendingResultsEventHandler(this);
        hapi.getPendingTransaction();
        boolean succeed = hapi.saleReversal(new
                BigInteger(payment.getPay_amount().replace(".", "")), currency, originalTransactionId);
        if (!succeed) {
            Global.showPrompt(activity, R.string.payment, activity.getString(R.string.handpoint_payment_error));
        }
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
        hapi.addLogEventHandler(this);
        hapi.getDeviceLogs();
    }

    @Override
    public void updateFirmware() {
        boolean update = hapi.update();
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

    }

    @Override
    public void printExpenseReceipt(ShiftExpense expense) {

    }

    @Override
    public void deviceLogsReady(String s, Device device) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, activity.getString(R.string.enabler_support_email));
        intent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.handpoint_log_file));
        intent.putExtra(Intent.EXTRA_TEXT, s);
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.send_email)));
        hapi.removeLogEventHandler(this);
    }

    @Override
    public void onMessageLogged(LogLevel logLevel, String s) {
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void pendingTransactionResult(Device device) {
        if (hapi.getPendingTransaction()) {

        }
    }

    @Override
    public void transactionResultReady(TransactionResult transactionResult, Device device) {
        if (hapi.getPendingTransaction()) {
//            Gson gson = new Gson();
//            String s = gson.toJson(transactionResult, TransactionResult.class);
            if (transactionResult.getFinStatus() == FinancialStatus.AUTHORISED) {
                Log.d("TransactionResult", transactionResult.getFinStatus().name());
                Log.d("TransResult Reversed", transactionResult.geteFTTransactionID());
                switch (transactionResult.getType()) {
                    case SALE:
                        boolean succeed = hapi.saleReversal(transactionResult.getRequestedAmount(), currency, transactionResult.geteFTTransactionID());
                        break;
                    case REFUND:
                        succeed = hapi.saleReversal(transactionResult.getRequestedAmount(), currency, transactionResult.geteFTTransactionID());
                        break;
                }
            } else {
                hapi.removePendingResultsEventHandler(this);
            }
        }
    }

    private class WorkingKeyRequest extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            showDialog(R.string.connecting_handpoint);
        }

        @Override
        protected String doInBackground(Void... params) {
//            EMSPayGate_Default payGate = new EMSPayGate_Default(activity, null);
//            String request = payGate.paymentWithAction(EMSPayGate_Default.EAction.HandpointWorkingKey, false, null,
//                    null);
//            Post httpClient = new Post();
//            String xml = httpClient.postData(Global.S_SUBMIT_WORKINGKEY_REQUEST, activity, request);
            EMSEpayLoginInfo loginInfo = EMSUtils.getEmsEpayLoginInfo(activity);
            return loginInfo.getSecret();//getWorkingKey(xml, activity);
        }

        @Override
        protected void onPostExecute(String result) {
            sharedSecret = result;
            if (hapi == null) {
                hapi = HapiFactory.getAsyncInterface(EMSHandpoint.this, activity).defaultSharedSecret(sharedSecret);
            }
            if (!TextUtils.isEmpty(sharedSecret)) {
                discoverDevices(myPref.getPrinterName(), myPref.getSwiperMACAddress());
            } else {
                dismissDialog();
                edm.driverDidNotConnectToDevice(EMSHandpoint.this, msg, true, activity);
            }
        }
    }
}