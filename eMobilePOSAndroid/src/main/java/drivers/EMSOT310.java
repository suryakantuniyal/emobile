package drivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.android.emobilepos.models.Orders;
import com.android.internal.misccomm.misccommManager;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.oem.barcode.BCRIntents;
import com.oem.barcode.BCRManager;

import java.util.HashMap;
import java.util.List;

import drivers.OT310.utils.BCRAppBroadcastReceiver;
import main.EMSDeviceManager;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;

/**
 * Created by Guarionex on 12/8/2015.
 */
public class EMSOT310 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    private EMSCallBack scannerCallBack;
    private Encrypt encrypt;
    private CreditCardInfo cardManager;
    private EMSDeviceManager edm;
    private EMSOT310 thisInstance;
    private Handler handler;
    String scannedData = "";
    private BCRAppBroadcastReceiver mBroadcastReceiver = new BCRAppBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra(BCRIntents.EXTRA_BCR_TYPE, -1);
            byte[] data = intent.getByteArrayExtra(BCRIntents.EXTRA_BCR_DATA);
            scannedData = new String(data);
            scannerCallBack.scannerWasRead(scannedData);

        }
    };
    private EMSCallBack swiperBack;


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
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);
        this.edm = edm;
        thisInstance = this;
        this.edm.driverDidConnectToDevice(thisInstance, false);
        return true;
    }

    private boolean openUSBPort() {
        misccommManager.getDefault().setMsrDefault();
//        int ret = misccommManager.getDefault().setMsrEnable();
        return 0 == 0;

    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
        return false;
    }

    @Override
    public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint) {
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
    public void printEndOfDayReport(String date, String clerk_id, boolean printDetails) {

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
        openUSBPort();
        this.swiperBack = callBack;
    }

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
    public void loadScanner(EMSCallBack callBack) {

        scannerCallBack = callBack;
        if (handler == null)
            handler = new Handler();
        if (callBack != null) {
            BCRManager.getDefault().BCREnable();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BCRIntents.ACTION_NEW_DATA);
            activity.getApplicationContext().registerReceiver(mBroadcastReceiver, filter);
        }
    }


    @Override
    public void releaseCardReader() {
        try {
//            misccommManager.getDefault().setMsrDisable();
            activity.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
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


}
