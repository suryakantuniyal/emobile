package drivers;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;

import com.StarMicronics.jasura.JAException;
import com.android.emobilepos.R;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.miurasystems.miuralibrary.BaseBluetooth;
import com.miurasystems.miuralibrary.MPIConnectionDelegate;
import com.miurasystems.miuralibrary.api.executor.MiuraManager;
import com.miurasystems.miuralibrary.enums.BatteryData;
import com.miurasystems.miuralibrary.enums.DeviceStatus;
import com.miurasystems.miuralibrary.enums.M012Printer;
import com.miurasystems.miuralibrary.tlv.CardData;
import com.starmicronics.stario.StarIOPortException;
import com.uniquesecure.meposconnect.MePOSException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import drivers.miurasample.module.bluetooth.BluetoothConnectionListener;
import drivers.miurasample.module.bluetooth.BluetoothModule;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

/**
 * Created by guarionex on 1/17/17.
 */

public class EMSMiura extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, MPIConnectionDelegate {

    BluetoothModule miuraPrinter = BluetoothModule.getInstance();
    private int LINE_WIDTH = 32;
    private EMSDeviceManager edm;

    @Override
    public void connect(final Context activity, int paperSize, boolean isPOSPrinter, final EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.isPOSPrinter = isPOSPrinter;
        this.edm = edm;
        miuraPrinter.setDefaultDevice(activity, getMiuraDevice());
        miuraPrinter.setSelectedBluetoothDevice(getMiuraDevice());
        MiuraManager.getInstance().setDeviceType(MiuraManager.DeviceType.POS);
        miuraPrinter.setTimeoutEnable(true);
        miuraPrinter.openSessionDefaultDevice(new BluetoothConnectionListener() {
            @Override
            public void onConnected() {
                edm.driverDidConnectToDevice(EMSMiura.this, true, activity);
            }

            @Override
            public void onDisconnected() {
                edm.driverDidNotConnectToDevice(EMSMiura.this, getString(R.string.fail_to_connect), true, activity);
            }
        });
    }

    private BluetoothDevice getMiuraDevice() {
        ArrayList<BluetoothDevice> devices = BaseBluetooth.getInstance().getDevice();
        for (BluetoothDevice btd : devices) {
            if (btd.getAddress().equalsIgnoreCase(myPref.getPrinterMACAddress().substring(3))) {
                return btd;
            }
        }
        return null;
    }

    @Override
    public boolean autoConnect(final Activity activity, final EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        final boolean[] didConnect = {false};
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.isPOSPrinter = isPOSPrinter;
        this.edm = edm;
        if (getMiuraDevice() == null) {
            didConnect[0] = false;
            edm.driverDidNotConnectToDevice(EMSMiura.this, getString(R.string.fail_to_connect), false, activity);
        } else {
            miuraPrinter.setDefaultDevice(activity, getMiuraDevice());
            miuraPrinter.setSelectedBluetoothDevice(getMiuraDevice());
            MiuraManager.getInstance().setDeviceType(MiuraManager.DeviceType.POS);
            miuraPrinter.setTimeoutEnable(true);

            miuraPrinter.openSessionDefaultDevice(new BluetoothConnectionListener() {
                @Override
                public void onConnected() {
                    didConnect[0] = true;
                    edm.driverDidConnectToDevice(EMSMiura.this, false, activity);
                }

                @Override
                public void onDisconnected() {
                    didConnect[0] = false;
                    edm.driverDidNotConnectToDevice(EMSMiura.this, getString(R.string.fail_to_connect), false, activity);
                }
            });
        }
        return didConnect[0];
    }

    @Override
    public void registerAll() {
        this.registerPrinter();
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        setPaperWidth(LINE_WIDTH);
        printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory, boolean fromOnHold) {
        setPaperWidth(LINE_WIDTH);
        printTransaction(ordID, type, isFromHistory, fromOnHold, null);
        return true;
    }

    @Override
    public boolean printPaymentDetails(String payID, int type, boolean isReprint, EMVContainer emvContainer) {
        setPaperWidth(LINE_WIDTH);
        printPaymentDetailsReceipt(payID, type, isReprint, LINE_WIDTH, emvContainer);
        return true;
    }

    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        setPaperWidth(LINE_WIDTH);
        return printBalanceInquiry(values, LINE_WIDTH);
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        setPaperWidth(LINE_WIDTH);
        printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        setPaperWidth(LINE_WIDTH);
        printConsignmentPickupReceipt(myConsignment, encodedSig, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        setPaperWidth(LINE_WIDTH);
        printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);
        return true;
    }

    @Override
    public String printStationPrinter(List<Orders> orders, String ordID, boolean cutPaper, boolean printHeader) {
        return printStationPrinterReceipt(orders, ordID, LINE_WIDTH, cutPaper, printHeader);
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        setPaperWidth(LINE_WIDTH);
        printOpenInvoicesReceipt(invID, LINE_WIDTH);
        return true;
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
        setPaperWidth(LINE_WIDTH);
        printReportReceipt(curDate, LINE_WIDTH);
        return true;
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
        setPaperWidth(LINE_WIDTH);
        printShiftDetailsReceipt(LINE_WIDTH, shiftID);
    }

    @Override
    public void printEndOfDayReport(String curDate, String clerk_id, boolean printDetails) {
        setPaperWidth(LINE_WIDTH);
        printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);
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

        new Thread(new Runnable() {
            public void run() {
                if (mePOS != null) {
                    try {
                        mePOS.openCashDrawer();
                    } catch (MePOSException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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
        return true;
    }

    @Override
    public void printClockInOut(List<ClockInOut> timeClocks, String clerkID) {
        super.printClockInOut(timeClocks, LINE_WIDTH, clerkID);
    }

    @Override
    public void connected() {

    }

    @Override
    public void disconnected() {

    }

    @Override
    public void connectionState(boolean b) {

    }

    @Override
    public void onKeyPressed(int i) {

    }

    @Override
    public void onCardStatusChange(CardData cardData) {

    }

    @Override
    public void onDeviceStatusChange(DeviceStatus deviceStatus, String s) {

    }

    @Override
    public void onBatteryStatusChange(BatteryData batteryData) {

    }

    @Override
    public void onBarcodeScan(String s) {

    }

    @Override
    public void onPrintSledStatus(M012Printer m012Printer) {

    }
}
