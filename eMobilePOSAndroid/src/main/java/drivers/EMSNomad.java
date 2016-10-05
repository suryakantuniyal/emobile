package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.android.emobilepos.BuildConfig;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.payments.EMSPayGate_Default;
import com.android.support.ConsignmentTransaction;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.Post;

import com.payments.core.CoreRefundResponse;
import com.payments.core.CoreResponse;
import com.payments.core.CoreSaleResponse;
import com.payments.core.CoreSettings;
import com.payments.core.CoreSignature;
import com.payments.core.CoreTransactions;
import com.payments.core.admin.AndroidTerminal;
import com.payments.core.common.contracts.CoreAPIListener;
import com.payments.core.common.enums.CoreDeviceError;
import com.payments.core.common.enums.CoreError;
import com.payments.core.common.enums.CoreMessage;
import com.payments.core.common.enums.CoreMode;
import com.payments.core.common.enums.DeviceEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

/**
 * Created by guarionex on 10/4/16.
 */

public class EMSNomad extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, CoreAPIListener {

    private EMSDeviceManager edm;

    private Handler handler;
    private EMSCallBack msrCallBack;
    String msg = "Failed to connect";
    static boolean connected = false;
    private static ProgressDialog myProgressDialog;
    private AndroidTerminal terminal;
    private String terminalSecret = "1007";
    private String terminalId = "secret";

    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        initDevice();

    }

    private void initDevice() {
        terminal = new AndroidTerminal(this);
        terminal.initWithConfiguration(this.activity, terminalId, terminalSecret);
        if (BuildConfig.DEBUG_NOMAD) {
            terminal.setMode(CoreMode.DEMO);
        } else {
            terminal.setMode(CoreMode.LIVE);
        }
        Toast.makeText(this.activity, "Initializing Nomad.", Toast.LENGTH_SHORT).show();
        terminal.initDevice(DeviceEnum.NOMAD);
//        terminal.selectBTDevice(item);
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;


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
    public void printReceiptPreview(View view) {

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
    public void onMessage(CoreMessage coreMessage) {

    }

    @Override
    public void onSaleResponse(CoreSaleResponse coreSaleResponse) {

    }

    @Override
    public void onRefundResponse(CoreRefundResponse coreRefundResponse) {

    }

    @Override
    public void onTransactionListResponse(CoreTransactions coreTransactions) {

    }

    @Override
    public void onLoginUrlRetrieved(String s) {

    }

    @Override
    public void onSignatureRequired(CoreSignature coreSignature) {

    }

    @Override
    public void onError(CoreError coreError, String s) {

    }

    @Override
    public void onDeviceError(CoreDeviceError coreDeviceError, String s) {

    }

    @Override
    public void onSettingsRetrieved(CoreSettings coreSettings) {

    }

    @Override
    public void onDeviceConnected(DeviceEnum deviceEnum, HashMap<String, String> hashMap) {

    }

    @Override
    public void onDeviceDisconnected(DeviceEnum deviceEnum) {

    }

    @Override
    public void onSelectApplication(ArrayList<String> arrayList) {

    }

    @Override
    public void onSelectBTDevice(ArrayList<String> arrayList) {

    }

    @Override
    public void onDeviceConnectionError() {

    }

    @Override
    public void onAutoConfigProgressUpdate(String s) {

    }

    @Override
    public void onReversalRetrieved(CoreResponse coreResponse) {

    }
}
