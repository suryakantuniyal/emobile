package drivers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;

import com.android.emobilepos.R;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Receipt;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.emobilepos.print.ReceiptBuilder;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.crashlytics.android.Crashlytics;
import com.pax.poslink.peripheries.POSLinkPrinter;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import com.starmicronics.starioextension.ICommandBuilder;
import com.starmicronics.starioextension.StarIoExt;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;

/**
 * Created by Luis Camayd on 3/1/2019.
 */
public class EMSPaxA920 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    private int LINE_WIDTH = 28;
    private EMSDeviceManager edm;

    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        this.edm = edm;
        myPref = new MyPreferences(activity);

        POSLinkPrinter.getInstance(activity).setGray(POSLinkPrinter.GreyLevel.DEFAULT);
        printDataFormatter = new POSLinkPrinter.PrintDataFormatter();
        this.edm.driverDidConnectToDevice(this, true, activity);
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize,
                               boolean isPOSPrinter, String portName, String portNumber) {
        this.activity = activity;
        this.edm = edm;
        myPref = new MyPreferences(activity);

        POSLinkPrinter.getInstance(activity).setGray(POSLinkPrinter.GreyLevel.DEFAULT);
        printDataFormatter = new POSLinkPrinter.PrintDataFormatter();
        this.edm.driverDidConnectToDevice(this, false, activity);
        return true;
    }

    @Override
    public void registerAll() {
        this.registerPrinter();
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold, EMVContainer emvContainer) {
        printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold, EMVContainer emvContainer) {
        printReceipt(order, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold) {
        return printTransaction(ordID, saleTypes, isFromHistory, fromOnHold, null);
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold) {
        return printTransaction(order, saleTypes, isFromHistory, fromOnHold, null);
    }

    @Override
    public boolean printPaymentDetails(String payID, int isFromMainMenu,
                                       boolean isReprint, EMVContainer emvContainer) {
        printPaymentDetailsReceipt(payID, isFromMainMenu, isReprint, LINE_WIDTH, emvContainer);
        return true;
    }

    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        return printBalanceInquiry(values, LINE_WIDTH);
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment,
                                    String encodedSignature) {
        printConsignmentReceipt(myConsignment, encodedSignature, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment,
                                          String encodedSignature) {
        printConsignmentPickupReceipt(myConsignment, encodedSignature, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c,
                                           boolean isPickup) {
        printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);
        return true;
    }

    @Override
    public boolean printRemoteStation(List<Orders> orders, String ordID) {
//        return printStationPrinterReceipt(orderProducts, ordID, LINE_WIDTH, cutPaper, printHeader);
        return false;
    }

    @Override
    public boolean printOpenInvoices(String invID) {
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
    public void turnOnBCR() {

    }

    @Override
    public void turnOffBCR() {

    }

    @Override
    public boolean printReport(String curDate) {
        printReportReceipt(curDate, LINE_WIDTH);
        return true;
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
        printShiftDetailsReceipt(LINE_WIDTH, shiftID);
    }

    @Override
    public void printEndOfDayReport(String date, String clerk_id, boolean printDetails) {
        printEndOfDayReportReceipt(date, LINE_WIDTH, printDetails);
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
            super.printReceiptPreview(splitedOrder, LINE_WIDTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void salePayment(Payment payment, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void saleReversal(Payment payment, String originalTransactionId, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void refund(Payment payment, CreditCardInfo creditCardInfo) {

    }

    @Override
    public void refundReversal(Payment payment, String originalTransactionId, CreditCardInfo creditCardInfo) {

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
    public void printClockInOut(List<ClockInOut> clockInOuts, String clerkID) {
        printClockInOut(clockInOuts, LINE_WIDTH, clerkID);
    }

    @Override
    public void printExpenseReceipt(ShiftExpense expense) {
        printExpenseReceipt(LINE_WIDTH, expense);
    }
    @Override
    public boolean printGiftReceipt(OrderProduct orderProduct,
                                    Order order,
                                    Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold) {


        boolean result = false;
        try {
            setPaperWidth(LINE_WIDTH);

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getReceipt(orderProduct,order, saleTypes, isFromHistory,fromOnHold);
            printTextReceipt(receipt);
            releasePrinter();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return result;
    }
    private void printTextReceipt(Receipt receipt) {
        ICommandBuilder builder = StarIoExt.createCommandBuilder(StarIoExt.Emulation.StarPRNT);
        builder.beginDocument();
        Charset encoding = Charset.forName("UTF-8");
        builder.appendCodePage(ICommandBuilder.CodePageType.UTF8);

        if (receipt.getMerchantLogo() != null)
            builder.appendBitmapWithAlignment(receipt.getMerchantLogo(), false,
                    ICommandBuilder.AlignmentPosition.Center);
        if (receipt.getMerchantHeader() != null)
            builder.appendEmphasis((receipt.getMerchantHeader()).getBytes(encoding));
        if (receipt.getSpecialHeader() != null)
            builder.appendInvert((receipt.getSpecialHeader()).getBytes(encoding));
        if (receipt.getRemoteStationHeader() != null)
            builder.appendMultiple((receipt.getRemoteStationHeader()).getBytes(encoding), 2, 2);
        if (receipt.getHeader() != null)
            builder.append((receipt.getHeader()).getBytes(encoding));
        if (receipt.getEmvDetails() != null)
            builder.append((receipt.getEmvDetails()).getBytes(encoding));
        if (receipt.getSeparator() != null)
            builder.append((receipt.getSeparator()).getBytes(encoding));
        for (String s : receipt.getItems()) {
            if (s != null)
                builder.append((s).getBytes(encoding));
        }
        for (String s : receipt.getRemoteStationItems()) {
            if (s != null)
                builder.appendMultiple((s).getBytes(encoding), 2, 2);
        }
        if (receipt.getSeparator() != null)
            builder.append((receipt.getSeparator()).getBytes(encoding));
        if (receipt.getTotals() != null)
            builder.append((receipt.getTotals()).getBytes(encoding));
        if (receipt.getTaxes() != null)
            builder.append((receipt.getTaxes()).getBytes(encoding));
        if (receipt.getTotalItems() != null)
            builder.append((receipt.getTotalItems()).getBytes(encoding));
        if (receipt.getGrandTotal() != null)
            builder.appendMultipleWidth((receipt.getGrandTotal()).getBytes(encoding), 2);
        if (receipt.getPaymentsDetails() != null)
            builder.append((receipt.getPaymentsDetails()).getBytes(encoding));
        // Gratuities line
        if (myPref.isGratuitySelected() && myPref.getGratuityOne() != null
                && myPref.getGratuityTwo() != null
                && myPref.getGratuityThree() != null) {
            // Gratuity title
            EMSPlainTextHelper emsPlainTextHelper = new EMSPlainTextHelper();

            if(receipt.getSubTotal()!= null) {
                String title = getString(R.string.suggested_gratuity_title);
                title = emsPlainTextHelper.centeredString(title,LINE_WIDTH);
                if (title != null) {
                    builder.append(title.getBytes(encoding));
                }
            }
        }
        if (receipt.getYouSave() != null)
            builder.append((receipt.getYouSave()).getBytes(encoding));
        if (receipt.getIvuLoto() != null)
            builder.append((receipt.getIvuLoto()).getBytes(encoding));
        if (receipt.getLoyaltyDetails() != null)
            builder.append((receipt.getLoyaltyDetails()).getBytes(encoding));
        if (receipt.getRewardsDetails() != null)
            builder.append((receipt.getRewardsDetails()).getBytes(encoding));
        if (receipt.getSignatureImage() != null)
            builder.appendBitmapWithAlignment(receipt.getSignatureImage(), false,
                    ICommandBuilder.AlignmentPosition.Center);
        if (receipt.getSignature() != null)
            builder.append((receipt.getSignature()).getBytes(encoding));
        if (receipt.getMerchantFooter() != null)
            builder.appendEmphasis((receipt.getMerchantFooter()).getBytes(encoding));
        if (receipt.getSpecialFooter() != null)
            builder.appendInvert((receipt.getSpecialFooter()).getBytes(encoding));
        if (receipt.getTermsAndConditions() != null)
            builder.append((receipt.getTermsAndConditions()).getBytes(encoding));
        if (receipt.getEnablerWebsite() != null)
            builder.appendEmphasis((receipt.getEnablerWebsite()).getBytes(encoding));

        builder.appendCutPaper(ICommandBuilder.CutPaperAction.PartialCutWithFeed);
        builder.endDocument();
        byte[] commands;
        commands = builder.getCommands();

        try {
            port.beginCheckedBlock();
            port.writePort(commands, 0, commands.length);
            port.setEndCheckedBlockTimeoutMillis(30000);
            StarPrinterStatus status = port.endCheckedBlock();
            if(status.offline) {
//                Log.e("Star","offline");
            }
//            StarPrinterStatus status = port.endCheckedBlock();
            // todo: implement status
        } catch (StarIOPortException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }
    private BigDecimal getGratuity(BigDecimal gratuity, BigDecimal subTotal){
        BigDecimal oneHundred = new BigDecimal(100);
        return subTotal.multiply((gratuity.divide(oneHundred).setScale(2,BigDecimal.ROUND_DOWN))).setScale(2, BigDecimal.ROUND_DOWN);
    }
}
