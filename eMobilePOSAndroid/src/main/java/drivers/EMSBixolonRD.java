package drivers;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;

import com.android.database.MemoTextHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplitedOrder;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.thefactoryhka.android.rd.TfhkaAndroid;

import org.springframework.util.StringUtils;

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
    private static final int HEADER_LENGTH = 8;
    private static final int TAX_LENGTH = 5;
    private static TfhkaAndroid printerTFHKA;
    private EMSDeviceManager edm;
    String msg = "Failed to connect";

    @Override
    public void connect(final Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        this.edm = edm;
        if (printerTFHKA == null) {
            printerTFHKA = new TfhkaAndroid();
        }
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
        if (printerTFHKA == null) {
            printerTFHKA = new TfhkaAndroid();
        }
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

    private boolean setPrintrConfiguration() {
        boolean cmd = true;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            cmd = cmd && printerTFHKA.SendCmd("PJ3201");
            cmd = cmd && printerTFHKA.SendCmd("PG" + DateUtils.getDateAsString(new Date(), "ddMMyy"));
            cmd = cmd && printerTFHKA.SendCmd("PF" + DateUtils.getDateAsString(new Date(), "HH:mm:ss"));

            cmd = cmd && printerTFHKA.SendCmd("I0Z0");
            TaxesHandler taxesHandler = new TaxesHandler(activity);
            List<Tax> taxes = taxesHandler.getTaxes();
            String taxCmd = "PT";
            for (Tax tax : taxes) {
                taxCmd += "2" + Global.getRoundBigDecimal(Global.getBigDecimalNum(tax.getTaxRate()), 2).toString();
            }
            taxCmd = StringUtils.deleteAny(taxCmd, ".");
//            cmd = cmd && printerTFHKA.SendCmd(taxCmd);
            //PT command apply the taxes rates. Can be executed 64 times max.
            //cmd = cmd && printerTFHKA.SendCmd("Pt");


            MemoTextHandler handler = new MemoTextHandler(activity);
            String[] header = handler.getHeader();
            if (header[0] != null && !header[0].isEmpty())
                cmd = cmd && printerTFHKA.SendCmd("PH01" + header[0]);
            if (header[1] != null && !header[1].isEmpty())
                cmd = cmd && printerTFHKA.SendCmd("PH02" + header[1]);
            if (header[2] != null && !header[2].isEmpty())
                cmd = cmd && printerTFHKA.SendCmd("PH03" + header[2]);

            String[] footer = handler.getFooter();
            if (footer[0] != null && !footer[0].isEmpty())
                cmd = cmd && printerTFHKA.SendCmd("PH91" + footer[0]);
            if (footer[1] != null && !footer[1].isEmpty())
                cmd = printerTFHKA.SendCmd("PH92" + footer[1]);
            if (footer[2] != null && !footer[2].isEmpty())
                cmd = cmd && printerTFHKA.SendCmd("PH93" + footer[2]);

            return cmd;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cmd;
    }

    public TfhkaAndroid getPrinterTFHKA() {
        return printerTFHKA;
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
    public void registerAll() {
        this.registerPrinter();
    }


    public void registerPrinter() {
        edm.setCurrentDevice(this);
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

    public boolean sendDateTimeCommand(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        boolean cmd = printerTFHKA.SendCmd("PJ3201"); //set print for retail mode
        cmd = cmd && printerTFHKA.SendCmd("PG" + DateUtils.getDateAsString(new Date(), "ddMMyy"));
        cmd = cmd && printerTFHKA.SendCmd("PF" + DateUtils.getDateAsString(new Date(), "HH:mm:ss"));
        return cmd;
    }

    public boolean sendHeaders(String[] headers) {
        printerTFHKA.SendCmd("I0Z0");
        boolean cmd = true;
        for (int i = 0; i < HEADER_LENGTH; i++) {
            if (!cmd) {
                return false;
            }
            if (i < headers.length) {
                cmd = printerTFHKA.SendCmd("PH0" + (i + 1) + headers[i]);
            } else {
                cmd = printerTFHKA.SendCmd("PH0" + (i + 1) + "");
            }
        }
        return cmd;
    }

    public boolean sendFooters(String[] footers) {
        printerTFHKA.SendCmd("I0Z0");
        boolean cmd = true;
        for (int i = 0; i < HEADER_LENGTH; i++) {
            if (!cmd) {
                return false;
            }
            if (i < footers.length) {
                cmd = printerTFHKA.SendCmd("PH9" + (i + 1) + footers[i]);
            } else {
                cmd = printerTFHKA.SendCmd("PH9" + (i + 1) + "");
            }
        }
        return cmd;
    }

    public boolean sendTaxes(List<Tax> taxes) {
        String taxCmd = "PT";
        for (int i = 0; i < TAX_LENGTH; i++) {
            if (i < taxes.size()) {
                taxCmd += "2" + String.format("%05.02f", Global.getRoundBigDecimal(Global.getBigDecimalNum(taxes.get(i).getTaxRate()), 2));
            }else{
                taxCmd += "20000";
            }
        }
        taxCmd = StringUtils.deleteAny(taxCmd, ".");
        boolean cmd = printerTFHKA.SendCmd(taxCmd);
        //PT command apply the taxes rates. Can be executed 64 times max.
        cmd = cmd && printerTFHKA.SendCmd("Pt");
        return cmd;
    }
}
