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
import com.android.emobilepos.models.Report;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.emobilepos.print.ReceiptBuilder;
import com.android.emobilepos.print.ReportBuilder;
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
import com.epson.eposprint.Print;

import java.util.HashMap;
import java.util.List;

import drivers.epson.ShowMsg;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

public class EMSEpson extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, ReceiveListener {

    private int LINE_WIDTH = 42;
    private EMSDeviceManager edm;
    private EMSDeviceDriver thisInstance;
    private Context activity;
    private Boolean isPOSPrinter;
    boolean isEpsonConnected;
    private FilterOption mFilterOption = null;
    PrinterStatusInfo statusInfo;


    public boolean FindPrinter(Context activity) {
        mFilterOption = new FilterOption();
        mFilterOption.setPortType(Discovery.PORTTYPE_USB);
        mFilterOption.setDeviceType(Discovery.TYPE_PRINTER);
        mFilterOption.setEpsonFilter(Discovery.FILTER_NAME);

        try {
            Discovery.start(activity, mFilterOption, mDiscoveryListener);
            return true;
        } catch (Epos2Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("PrinterName", deviceInfo.getDeviceName());
            item.put("Target", deviceInfo.getTarget());
            Global.epson_device_list.add(item);
        }
    };

    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.isPOSPrinter = isPOSPrinter;
        this.edm = edm;
        thisInstance = this;

        setupPrinter();
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.isPOSPrinter = isPOSPrinter;
        this.edm = edm;
        thisInstance = this;

        return setupPrinter();
    }

    private boolean setupPrinter() {
        if (edm.getCurrentDevice() == null || (statusInfo != null && statusInfo.getConnection() == Printer.FALSE)) {
            if (!initPrinter()) {
                Log.e("EMSEpson", "***Could not Initialize to Epson Printer***");
                isEpsonConnected = false;
                return false;
            } else if (!connectPrinter()) {
                Log.e("EMSEpson", "***Could not Connect to Epson Printer***");
                isEpsonConnected = false;
                return false;
            }
            isEpsonConnected = true;
        }

        if (isEpsonConnected) {
            statusInfo = epsonPrinter.getStatus();
        }
        return true;

    }

    private boolean connectPrinter() {
        boolean isBeginTransaction = false;

        if (epsonPrinter == null) {
            return false;
        }

        if (!myPref.getEpsonTarget().isEmpty()) {
            try {
                epsonPrinter.connect(myPref.getEpsonTarget(), Printer.PARAM_DEFAULT);
                edm.driverDidConnectToDevice(thisInstance, false, activity);
            } catch (Exception e) {
                edm.driverDidNotConnectToDevice(thisInstance, null, false, activity);
                ShowMsg.showException(e, "connect", activity);
                return false;
            }

            try {
                epsonPrinter.beginTransaction();
                isBeginTransaction = true;
            } catch (Exception e) {
                ShowMsg.showException(e, "beginTransaction", activity);
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

        return false;
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
            epsonPrinter = new Printer(myPref.getEpsonModel(), 0, activity);
            epsonPrinter.setReceiveEventListener(this);
        } catch (Exception e) {
            Log.e("EMSEpson", e.toString());
            ShowMsg.showException(e, "Printer", activity);
            return false;
        }

        return true;
    }


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
        return printTransaction(ordID, saleTypes, isFromHistory, fromOnHold, null);
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
        return printTransaction(order, saleTypes, isFromHistory, fromOnHold, null);
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
    public boolean printRemoteStation(List<Orders> orderProducts, String ordID) {
        return false;
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
        try {
            setPaperWidth(LINE_WIDTH);

            ReportBuilder reportBuilder = new ReportBuilder(activity, LINE_WIDTH);
            Report report = reportBuilder.getEndOfDay(date, printDetails);
            printReport(report);
//            printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);

            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
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
    public void registerAll() {
        this.registerPrinter();
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


    //////////////Printing Process Methods////////////
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
                if (receipt.getMerchantLogo() != null) {
                    epsonPrinter.addImage(receipt.getMerchantLogo(), 0, 0,
                            receipt.getMerchantLogo().getWidth(),
                            receipt.getMerchantLogo().getHeight(),
                            Printer.COLOR_1,
                            Printer.MODE_MONO_HIGH_DENSITY,
                            Printer.HALFTONE_DITHER,
                            Printer.PARAM_DEFAULT,
                            Printer.COMPRESS_AUTO);
                }
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
                textData.delete(0, textData.length());

                if (receipt.getSignatureImage() != null)
                    epsonPrinter.addImage(receipt.getSignatureImage(), 0, 0,
                            receipt.getSignatureImage().getWidth(),
                            receipt.getSignatureImage().getHeight(),
                            Printer.COLOR_1,
                            Printer.MODE_MONO_HIGH_DENSITY,
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
                textData.delete(0, textData.length());

                epsonPrinter.addCut(Printer.CUT_FEED);
                epsonPrinter.sendData(Printer.PARAM_DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    private void printRasterReceipt(Receipt receipt) {
//        Bitmap bitmapFromText;
//        ICommandBuilder textData = StarIoExt.createCommandBuilder(StarIoExt.Emulation.StarGraphic);
//        textData.beginDocument();
//
//        if (receipt.getMerchantLogo() != null) {
//            int logoPosition = (PAPER_WIDTH / 3 - receipt.getMerchantLogo().getWidth()) / 2 + 30;
//            textData.appendBitmapWithAbsolutePosition(receipt.getMerchantLogo(),
//                    false, logoPosition);
//        }
//
//        if (receipt.getMerchantHeader() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getMerchantHeader(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getSpecialHeader() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getSpecialHeader(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getHeader() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getHeader(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getEmvDetails() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getEmvDetails(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getSeparator() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getSeparator(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        for (String s : receipt.getItems()) {
//            if (s != null) {
//                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//                bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                        s, FONT_SIZE, PAPER_WIDTH, typeface);
//                textData.appendBitmap(bitmapFromText, false);
//            }
//        }
//
//        if (receipt.getSeparator() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getSeparator(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getTotals() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getTotals(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getTaxes() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getTaxes(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getTotalItems() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getTotalItems(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getGrandTotal() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getGrandTotal(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getPaymentsDetails() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getPaymentsDetails(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getYouSave() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getYouSave(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getIvuLoto() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getIvuLoto(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getLoyaltyDetails() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getLoyaltyDetails(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getRewardsDetails() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getRewardsDetails(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getSignatureImage() != null) {
//            int logoPosition = (PAPER_WIDTH / 3 - receipt.getSignatureImage().getWidth()) / 2 + 30;
//            textData.appendBitmapWithAbsolutePosition(receipt.getSignatureImage(),
//                    false, logoPosition);
//        }
//
//        if (receipt.getSignature() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getSignature(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getMerchantFooter() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getMerchantFooter(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getSpecialFooter() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getSpecialFooter(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getTermsAndConditions() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getTermsAndConditions(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (receipt.getEnablerWebsite() != null) {
//            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
//            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
//                    receipt.getEnablerWebsite(), FONT_SIZE, PAPER_WIDTH, typeface);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        textData.appendCutPaper(ICommandBuilder.CutPaperAction.PartialCutWithFeed);
//        textData.endDocument();
//        byte[] commands;
//        commands = textData.getCommands();
//
//        try {
//            port.writePort(commands, 0, commands.length);
//        } catch (StarIOPortException e) {
//            e.printStackTrace();
//        }
//    }

    private void printReport(Report report) {
        if (myPref.isRasterModePrint()) {
//            printRasterReport(report);
        } else {
            printTextReport(report);
        }
    }

    private void printTextReport(Report report) {
        if (epsonPrinter != null) {
            StringBuilder textData = new StringBuilder();

            try {
                epsonPrinter.clearCommandBuffer();

                if (report.getSpecialHeader() != null)
                    textData.append(report.getSpecialHeader());
                if (report.getHeader() != null)
                    textData.append(report.getHeader());
                epsonPrinter.addText(textData.toString());
                textData.delete(0, textData.length());

                if (report.getSummary() != null)
                    textData.append(report.getSummary());
                if (report.getArTransactions() != null)
                    textData.append(report.getArTransactions());
                if (report.getSalesByClerk() != null)
                    textData.append(report.getSalesByClerk());
                if (report.getTotalsByShifts() != null)
                    textData.append(report.getTotalsByShifts());
                if (report.getTotalsByTypes() != null)
                    textData.append(report.getTotalsByTypes());
                if (report.getItemsSold() != null)
                    textData.append(report.getItemsSold());
                if (report.getDepartmentSales() != null)
                    textData.append(report.getDepartmentSales());
                if (report.getDepartmentReturns() != null)
                    textData.append(report.getDepartmentReturns());
                if (report.getPayments() != null)
                    textData.append(report.getPayments());
                if (report.getVoids() != null)
                    textData.append(report.getVoids());
                if (report.getRefunds() != null)
                    textData.append(report.getRefunds());
                if (report.getItemsReturned() != null)
                    textData.append(report.getItemsReturned());
                epsonPrinter.addText(textData.toString());
                textData.delete(0, textData.length());

                if (report.getFooter() != null)
                    textData.append(report.getFooter());
                if (report.getSpecialFooter() != null)
                    textData.append(report.getSpecialFooter());
                if (report.getEnablerWebsite() != null)
                    textData.append(report.getEnablerWebsite());
                epsonPrinter.addText(textData.toString());
                textData.delete(0, textData.length());

                epsonPrinter.addCut(Printer.CUT_FEED);
                epsonPrinter.sendData(Printer.PARAM_DEFAULT);
            } catch (Epos2Exception e) {
                e.printStackTrace();
            }
        }
    }

//    private void printRasterReport(Report report) {
//        Bitmap bitmapFromText;
//
//        ICommandBuilder textData = StarIoExt.createCommandBuilder(StarIoExt.Emulation.StarGraphic);
//        textData.beginDocument();
//
//        if (report.getSpecialHeader() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getSpecialHeader(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceBold);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getHeader() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getHeader(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getSummary() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getSummary(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getArTransactions() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getArTransactions(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getSalesByClerk() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getSalesByClerk(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getTotalsByShifts() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getTotalsByShifts(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getTotalsByTypes() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getTotalsByTypes(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getItemsSold() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getItemsSold(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getDepartmentSales() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getDepartmentSales(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getDepartmentReturns() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getDepartmentReturns(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getPayments() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getPayments(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getVoids() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getVoids(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getRefunds() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getRefunds(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getItemsReturned() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getItemsReturned(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getFooter() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getFooter(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getSpecialFooter() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getSpecialFooter(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceBold);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        if (report.getEnablerWebsite() != null) {
//            bitmapFromText = BitmapUtils.createBitmapFromText(
//                    report.getEnablerWebsite(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceBold);
//            textData.appendBitmap(bitmapFromText, false);
//        }
//
//        textData.appendCutPaper(ICommandBuilder.CutPaperAction.PartialCutWithFeed);
//        textData.endDocument();
//        byte[] commands;
//        commands = textData.getCommands();
//
//        try {
//            port.writePort(commands, 0, commands.length);
//        } catch (StarIOPortException e) {
//            e.printStackTrace();
//            Crashlytics.logException(e);
//        }
//    }

}
