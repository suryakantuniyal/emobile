package drivers;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;

import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import main.EMSDeviceManager;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;

/**
 * Created by Guarionex on 12/3/2015.
 */
public class EMSEM100 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    private EMSCallBack scannerCallBack;
    private Encrypt encrypt;
    private CreditCardInfo cardManager;
    private EMSDeviceManager edm;
    private EMSEM100 thisInstance;
    private Handler handler;
    String scannedData = "";

    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);
        this.edm = edm;
        thisInstance = this;
        this.edm.driverDidConnectToDevice(thisInstance, false);
    }


    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        return true;
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
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String
            encodedSignature) {
        return false;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String
            encodedSignature) {
        return false;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c,
                                           boolean isPickup) {
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
    public void printEndOfDayReport(String date, String clerk_id, boolean printDetails) {
    }


    @Override
    public void printShiftDetailsReport(String shiftID) {
    }


    @Override
    public void registerAll() {
        this.registerPrinter();
    }


    public void registerPrinter() {
        // TODO Auto-generated method stub
        edm.currentDevice = this;
    }

    public void unregisterPrinter() {
        // TODO Auto-generated method stub
        edm.currentDevice = null;
    }

    @Override
    public void loadCardReader(EMSCallBack callBack, boolean isDebitCard) {

    }

    @Override
    public void loadScanner(EMSCallBack callBack) {

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

    }

    @Override
    public void saleReversal(Payment payment) {

    }

    @Override
    public void refund(Payment payment) {

    }

    @Override
    public void refundReversal(Payment payment) {

    }

    @Override
    public void printEMVReceipt(String text) {

    }


}
