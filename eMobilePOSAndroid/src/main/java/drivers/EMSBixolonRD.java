package drivers;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;

import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplitedOrder;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.thefactoryhka.android.rd.TfhkaAndroid;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

/**
 * Created by guarionex on 5/12/17.
 */

public class EMSBixolonRD extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {
    private TfhkaAndroid printerTFHKA;
    private EMSDeviceManager edm;
    String msg = "Failed to connect";

    @Override
    public void connect(final Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        this.edm = edm;
        printerTFHKA = new TfhkaAndroid();
        myPref = new MyPreferences(activity);
        boolean connect = connect();
        if (connect) {
            edm.driverDidConnectToDevice(this, true);
        } else {
            edm.driverDidNotConnectToDevice(this, msg, true);
        }
    }


    @Override
    public boolean autoConnect(final Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter, String portName, String portNumber) {
        this.activity = activity;
        this.edm = edm;
        printerTFHKA = new TfhkaAndroid();
        myPref = new MyPreferences(activity);
        boolean connect = connect();
        if (connect) {
            edm.driverDidConnectToDevice(this, false);
        } else {
            edm.driverDidNotConnectToDevice(this, msg, false);
        }
        return true;
    }

    private boolean connect() {
        return printerTFHKA.estado || printerTFHKA.OpenBTPrinter(myPref.getPrinterMACAddress());
    }

    private void setPrintrConfiguration(){
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            boolean cmd = printerTFHKA.SendCmd("PJ3201");
            cmd = printerTFHKA.SendCmd("PG"+ DateUtils.getDateAsString(new Date(),"ddMMyy"));
            cmd = printerTFHKA.SendCmd("PG"+ DateUtils.getDateAsString(new Date(),"HH:mm:ss"));
            cmd = printerTFHKA.SendCmd("PG"+ DateUtils.getDateAsString(new Date(),"HH:mm:ss"));
        } catch (Exception e) {
            e.printStackTrace();
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
    public void printReceiptPreview(SplitedOrder splitedOrder) {

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
    public boolean isConnected() {
        return false;
    }
}
