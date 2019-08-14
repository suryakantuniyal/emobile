package drivers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.util.Log;

import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Receipt;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.emobilepos.print.ReceiptBuilder;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;

import com.crashlytics.android.Crashlytics;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

public class EMSEpson extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, ReceiveListener {

    private int LINE_WIDTH = 30;
    private EMSDeviceManager edm;
    private EMSDeviceDriver thisInstance;
    private Context activity;
    private Boolean isPOSPrinter;

    private FilterOption mFilterOption = null;
    private ArrayList<HashMap<String, String>> mPrinterList = null;


    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.isPOSPrinter = isPOSPrinter;
        this.edm = edm;
        thisInstance = this;
        LINE_WIDTH = paperSize;

        setupPrinter();
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        boolean didConnect = false;

        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.isPOSPrinter = isPOSPrinter;
        this.edm = edm;
        thisInstance = this;
        LINE_WIDTH = paperSize;

        setupPrinter();

        return didConnect;
    }

    private void setupPrinter() {
        //FOR NOW, ONLY USB CONNECTIONS ARE PERMITED TO EPSON PRINTERS
        mFilterOption = new FilterOption();
        mFilterOption.setPortType(Discovery.PORTTYPE_USB);
        mFilterOption.setDeviceType(Discovery.TYPE_PRINTER);
        mFilterOption.setEpsonFilter(Discovery.FILTER_NAME);
        try {
            Discovery.start(activity, mFilterOption, mDiscoveryListener);
        } catch (Exception e) {
            Log.e("EMSEpson", e.toString());
        }
    }

    private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            if (!initPrinter())
                Log.e("EMSEpson", "***Could not Initialize to Epson Printer***");
            else if (!connectPrinter(deviceInfo.getTarget())) {
                Log.e("EMSEpson", "***Could not Connect to Epson Printer***");
            }
        }
    };

    private boolean connectPrinter(String target) {
        boolean isBeginTransaction = false;

        if (epsonPrinter == null) {
            return false;
        }

        try {
            epsonPrinter.connect(target, Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            Log.e("EMSEpson", e.toString());
            return false;
        }

        try {
            epsonPrinter.beginTransaction();
            isBeginTransaction = true;
        } catch (Exception e) {
            Log.e("EMSEpson", e.toString());
        }

        if (isBeginTransaction == false) {
            try {
                epsonPrinter.disconnect();
            } catch (Epos2Exception e) {
                // Do nothing
                return false;
            }
        }

        return true;
    }

    private void disconnectPrinter() {
        if (epsonPrinter == null)
            return;

        try {
            epsonPrinter.endTransaction();
        } catch (final Exception e) {
        }

        try {
            epsonPrinter.disconnect();
        } catch (final Exception e) {
        }
        finalizeObject();
    }

    private void finalizeObject() {
        if (epsonPrinter == null) {
            return;
        }
        epsonPrinter.clearCommandBuffer();
        epsonPrinter.setReceiveEventListener(null);
        epsonPrinter = null;
    }

    private boolean initPrinter() {
        try {
            //MUST EDIT THIS LINE TO SUPPORT OTHER PRINTERS
            epsonPrinter = new Printer(12, 0, activity);
            epsonPrinter.setReceiveEventListener(this);
        } catch (Exception e) {
            Log.e("EMSEpson", e.toString());
            return false;
        }

        return true;
    }

    private void printReceipt(Receipt receipt) {
        if (!myPref.isRasterModePrint()) {
            printNormalReceipt(receipt);
        } else {
//            printRasterReceipt(receipt);
        }
    }

    private void printNormalReceipt(Receipt receipt) {
        if (epsonPrinter != null) {
            StringBuilder textData = new StringBuilder();
            Bitmap logoData = null;
            final int barcodeWidth = 2;
            final int barcodeHeight = 100;

            try {
                epsonPrinter.addTextAlign(Printer.ALIGN_CENTER);

                if (receipt.getMerchantLogo() != null)
                    logoData = receipt.getMerchantLogo();
                    epsonPrinter.addImage(logoData, 0, 0,
                            logoData.getWidth(),
                            logoData.getHeight(),
                            Printer.COLOR_1,
                            Printer.MODE_MONO,
                            Printer.HALFTONE_DITHER,
                            Printer.PARAM_DEFAULT,
                            Printer.COMPRESS_AUTO);

                if (receipt.getMerchantHeader() != null)
                    textData.append(receipt.getMerchantHeader());
                if (receipt.getSpecialHeader() != null)
                    textData.append(receipt.getSpecialHeader());
                if (receipt.getHeader() != null)
                    textData.append(receipt.getHeader());
                if (receipt.getEmvDetails() != null)
                    textData.append(receipt.getEmvDetails());
                if (receipt.getSeparator() != null)
                    textData.append(receipt.getSeparator());
                for (String s : receipt.getItems()) {
                    if (s != null)
                        textData.append(s);
                }
                if (receipt.getSeparator() != null)
                    textData.append(receipt.getSeparator());
                if (receipt.getTotals() != null)
                    textData.append(receipt.getTotals());
                if (receipt.getTaxes() != null)
                    textData.append(receipt.getTaxes());
                if (receipt.getTotalItems() != null)
                    textData.append(receipt.getTotalItems());
                if (receipt.getGrandTotal() != null)
                    textData.append(receipt.getGrandTotal());
                if (receipt.getPaymentsDetails() != null)
                    textData.append(receipt.getPaymentsDetails());
                if (receipt.getYouSave() != null)
                    textData.append(receipt.getYouSave());
                if (receipt.getIvuLoto() != null)
                    textData.append(receipt.getIvuLoto());
                if (receipt.getLoyaltyDetails() != null)
                    textData.append(receipt.getLoyaltyDetails());
                if (receipt.getRewardsDetails() != null)
                    textData.append(receipt.getRewardsDetails());
                epsonPrinter.addText(textData.toString());
                textData.delete(0,textData.length());

                if (receipt.getSignatureImage() != null)
                    logoData = receipt.getSignatureImage();
                epsonPrinter.addImage(logoData, 0, 0,
                        logoData.getWidth(),
                        logoData.getHeight(),
                        Printer.COLOR_1,
                        Printer.MODE_MONO,
                        Printer.HALFTONE_DITHER,
                        Printer.PARAM_DEFAULT,
                        Printer.COMPRESS_AUTO);

                if (receipt.getSignature() != null)
                    textData.append(receipt.getSignature());

                if (receipt.getMerchantFooter() != null)
                    textData.append(receipt.getMerchantFooter());

                if (receipt.getSpecialFooter() != null)
                    textData.append(receipt.getSpecialFooter());

                if (receipt.getTermsAndConditions() != null)
                    textData.append(receipt.getTermsAndConditions());

                if (receipt.getEnablerWebsite() != null)
                    textData.append(receipt.getEnablerWebsite());
                epsonPrinter.addText(textData.toString());
                textData.delete(0,textData.length());
                logoData = null;

                epsonPrinter.addCut(Printer.CUT_FEED);
            } catch (Epos2Exception e) {
                e.printStackTrace();
            }
        }
    }

//    private void printRasterReceipt(Receipt receipt) {
//        Bitmap bitmapFromText;
//        ICommandBuilder builder = StarIoExt.createCommandBuilder(StarIoExt.Emulation.StarGraphic);
//        builder.beginDocument();
//
//        if (receipt.getMerchantLogo() != null) {
//            int logoPosition = (PAPER_WIDTH / 3 - receipt.getMerchantLogo().getWidth()) / 2 + 30;
//            builder.appendBitmapWithAbsolutePosition(receipt.getMerchantLogo(),
//                    false, logoPosition);
//        }
//
//        if (receipt.getMerchantHeader() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getMerchantHeader(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getSpecialHeader() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getSpecialHeader(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getHeader() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getHeader(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getEmvDetails() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getEmvDetails(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getSeparator() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getSeparator(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        for (String s : receipt.getItems()) {
//            if (s != null) {
//                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//                bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                        s, FONT_SIZE, PAPER_WIDTH, typeface);
//                builder.appendBitmap(bitmapFromText, false);
//            }
//        }
//
//        if (receipt.getSeparator() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getSeparator(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getTotals() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getTotals(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getTaxes() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getTaxes(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getTotalItems() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getTotalItems(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getGrandTotal() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getGrandTotal(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getPaymentsDetails() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getPaymentsDetails(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getYouSave() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getYouSave(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getIvuLoto() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getIvuLoto(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getLoyaltyDetails() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getLoyaltyDetails(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getRewardsDetails() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getRewardsDetails(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getSignatureImage() != null) {
//            int logoPosition = (PAPER_WIDTH / 3 - receipt.getSignatureImage().getWidth()) / 2 + 30;
//            builder.appendBitmapWithAbsolutePosition(receipt.getSignatureImage(),
//                    false, logoPosition);
//        }
//
//        if (receipt.getSignature() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getSignature(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getMerchantFooter() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getMerchantFooter(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getSpecialFooter() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getSpecialFooter(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getTermsAndConditions() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getTermsAndConditions(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getEnablerWebsite() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getEnablerWebsite(), FONT_SIZE, PAPER_WIDTH, typeface);
//            builder.appendBitmap(bitmapFromText, false);
//        }
//
//        builder.appendCutPaper(ICommandBuilder.CutPaperAction.PartialCutWithFeed);
//        builder.endDocument();
//        byte[] commands;
//        commands = builder.getCommands();
//
//        try {
//            port.writePort(commands, 0, commands.length);
//        } catch (StarIOPortException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold, EMVContainer emvContainer) {
        boolean result = false;
        try {
            setPaperWidth(LINE_WIDTH);

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getTransaction(
                    order, saleTypes, isFromHistory, fromOnHold);
            printReceipt(receipt);
//            printReceipt(order, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);

            releasePrinter();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return result;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes,
                                    boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        boolean result = false;
        try {
            setPaperWidth(LINE_WIDTH);

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getTransaction(
                    ordID, saleTypes, isFromHistory, fromOnHold);
            printReceipt(receipt);
//            printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);

            releasePrinter();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return result;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
        return false;
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
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
    public void printReceiptPreview(SplittedOrder splitedOrder) {

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
        return false;
    }

    @Override
    public void printClockInOut(List<ClockInOut> clockInOuts, String clerkID) {

    }

    @Override
    public void printExpenseReceipt(ShiftExpense expense) {

    }

    @Override
    public void onPtrReceive(Printer printer, int i, PrinterStatusInfo printerStatusInfo, String s) {
        //warnings for running out of paper, low battery, etc. goes here...
    }
}
