package drivers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.Toast;

import com.android.emobilepos.R;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import drivers.epson.ShowMsg;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;
import util.BitmapUtils;

public class EMSEpson extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, ReceiveListener {

    private int LINE_WIDTH = 42;
    private int PAPER_WIDTH = 576;
    private EMSDeviceManager edm;
    private EMSDeviceDriver thisInstance;
    private Context activity;
    private Boolean isPOSPrinter;
    boolean isEpsonConnected;
    private FilterOption mFilterOption = null;
    PrinterStatusInfo statusInfo;

    private Typeface typefaceNormal = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
    private Typeface typefaceBold = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
    private Typeface typefaceBoldItalic = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);
    private int FONT_SIZE_NORMAL = 20;
    private int FONT_SIZE_LARGE = 30;


    public boolean FindPrinter(Context activity, boolean isRestart) {
        mFilterOption = new FilterOption();
        mFilterOption.setPortType(Discovery.PORTTYPE_USB);
        mFilterOption.setDeviceType(Discovery.TYPE_PRINTER);
        mFilterOption.setEpsonFilter(Discovery.FILTER_NAME);
        try {
            if (!isRestart) {
                Discovery.start(activity, mFilterOption, mDiscoveryListener);
            } else {
                while (true) {
                    try {
                        Discovery.stop();
                        break;
                    } catch (Epos2Exception e) {
                        if (e.getErrorStatus() != Epos2Exception.ERR_PROCESSING) {
                            ShowMsg.showException(e, "stop", activity);
                        }
                    }
                }
                Global.epson_device_list.clear();
                Discovery.start(activity, mFilterOption, mDiscoveryListener);
            }
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

        setupPrinter(null);
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.isPOSPrinter = isPOSPrinter;
        this.edm = edm;
        thisInstance = this;

        return setupPrinter(_portName);
    }

    private boolean setupPrinter(String kitchenPrinterIP) {
        if (edm.getCurrentDevice() == null || (statusInfo != null && statusInfo.getConnection() == Printer.FALSE)) {
            if (!initPrinter(kitchenPrinterIP)) {
                Log.e("EMSEpson", "***Could not Initialize to Epson Printer***");
                isEpsonConnected = false;
                return false;
            } else if (!connectPrinter(kitchenPrinterIP)) {
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

    private boolean connectPrinter(String kitchenPrinterIP) {
        boolean isBeginTransaction = false;
        String target = null;

        if (epsonPrinter == null) {
            return false;
        }

        target = (kitchenPrinterIP != null) ? kitchenPrinterIP : myPref.getEpsonTarget();
        if (!target.isEmpty()) {
            try {
                epsonPrinter.connect(target, Printer.PARAM_DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
            }

//            if (!target.equals(kitchenPrinterIP)) {
            PrinterStatusInfo status = epsonPrinter.getStatus();
            if (!isPrintable(status)) {
                try {
                    edm.driverDidNotConnectToDevice(thisInstance, null, false, activity);
                    //UNCOMMENT ONE OF THESE LINES TO DISPLAY EPSON PRINTER STATUS WARNINGS.(Low on paper, Low battery, Paper jam, etc.)
//                    Toast.makeText(activity, makeErrorMessage(status), Toast.LENGTH_SHORT).show();
//                    ShowMsg.showMsg(makeErrorMessage(status), activity);
                    epsonPrinter.disconnect();
                } catch (Exception ex) {
                }
                return false;
            } else {
                edm.driverDidConnectToDevice(thisInstance, false, activity);
                //UNCOMMENT ONE OF THESE LINES TO DISPLAY EPSON PRINTER STATUS WARNINGS.(Low on paper, Low battery, Paper jam, etc.)
//                Toast.makeText(activity, dispPrinterWarnings(status), Toast.LENGTH_SHORT).show();
//                ShowMsg.showMsg(dispPrinterWarnings(status),activity);
            }
//            }
            try {
                epsonPrinter.beginTransaction();
                isBeginTransaction = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!isBeginTransaction) {
                try {
                    epsonPrinter.disconnect();
                } catch (Epos2Exception e) {
                    // Do nothing
                }
                return false;
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

    private boolean initPrinter(String kitchenPrinterIP) {
        if (kitchenPrinterIP != null) {
            try {
                //THIS DRIVER ONLY SUPPORTS REMOTE STATION PRINTER SERIES TM-U220 FOR NOW...
                epsonPrinter = new Printer(Printer.TM_U220, 0, activity);
            } catch (Exception e) {
                Log.e("EMSEpson", e.toString());
                e.printStackTrace();
                return false;
            }
            epsonPrinter.setReceiveEventListener(this);
            return true;
        } else if (myPref.getEpsonModel() != -1) {
            try {
                epsonPrinter = new Printer(myPref.getEpsonModel(), 0, activity);

            } catch (Exception e) {
                Log.e("EMSEpson", e.toString());
                e.printStackTrace();
                return false;
            }
            epsonPrinter.setReceiveEventListener(this);
            return true;
        }
        return false;
    }

    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        } else if (status.getOnline() == Printer.FALSE) {
            return false;
        }

        return true;
    }

    private String makeErrorMessage(PrinterStatusInfo status) {
        String msg = "";

        if (status.getOnline() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_offline);
        }
        if (status.getConnection() == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_no_response);
        }
        if (status.getCoverOpen() == Printer.TRUE) {
            msg += getString(R.string.handlingmsg_err_cover_open);
        }
        if (status.getPaper() == Printer.PAPER_EMPTY) {
            msg += getString(R.string.handlingmsg_err_receipt_end);
        }
        if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
            msg += getString(R.string.handlingmsg_err_paper_feed);
        }
        if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
            msg += getString(R.string.handlingmsg_err_autocutter);
            msg += getString(R.string.handlingmsg_err_need_recover);
        }
        if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
            msg += getString(R.string.handlingmsg_err_unrecover);
        }
        if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
            if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_head);
            }
            if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_motor);
            }
            if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat);
                msg += getString(R.string.handlingmsg_err_battery);
            }
            if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
                msg += getString(R.string.handlingmsg_err_wrong_paper);
            }
        }
        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
            msg += getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }

    private String dispPrinterWarnings(PrinterStatusInfo status) {
        String warningsMsg = "";

        if (status == null) {
            return null;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += getString(R.string.handlingmsg_warn_battery_near_end);
        }
        return warningsMsg;
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
        try {
            setPaperWidth(LINE_WIDTH);

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getPayment(
                    payID, isFromMainMenu, isReprint, emvContainer);
            printReceipt(receipt);
//            printPaymentDetailsReceipt(payID, type, isReprint, LINE_WIDTH, emvContainer);

            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        boolean printed = false;
        try {
            setPaperWidth(LINE_WIDTH);

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getBalanceInquiry(values);
            printReceipt(receipt);
            printed = true;
//            printed = printBalanceInquiry(values, LINE_WIDTH);

            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return printed;
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        boolean printed = false;
        try {
            setPaperWidth(LINE_WIDTH);

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getConsignment(myConsignment, encodedSignature);
            printReceipt(receipt);
            printed = true;
//            printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);

            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return printed;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        boolean printed = false;
        try {
            setPaperWidth(LINE_WIDTH);

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getConsignmentPickup(myConsignment, encodedSignature);
            printReceipt(receipt);
            printed = true;
//            printConsignmentPickupReceipt(myConsignment, encodedSig, LINE_WIDTH);

            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return printed;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        boolean printed = false;
        try {
            setPaperWidth(LINE_WIDTH);

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getConsignmentHistory(map, c, isPickup);
            printReceipt(receipt);
            printed = true;
//            printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);

            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return printed;
    }

    @Override
    public boolean printRemoteStation(List<Orders> orderProducts, String ordID) {
        boolean result = false;
        try {
            setPaperWidth(LINE_WIDTH);

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getRemoteStation(orderProducts, ordID);
            printReceipt(receipt);
//            printStationPrinterReceipt(orders, ordID, 42, cutPaper, printHeader);

            releasePrinter();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return result;
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        boolean printed = false;
        try {
            setPaperWidth(LINE_WIDTH);

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getOpenInvoice(invID);
            printReceipt(receipt);
            printed = true;
//            printOpenInvoicesReceipt(invID, LINE_WIDTH);

            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return printed;
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
        boolean printed = false;
        try {
            setPaperWidth(LINE_WIDTH);

            ReportBuilder reportBuilder = new ReportBuilder(activity, LINE_WIDTH);
            Report report = reportBuilder.getDaySummary(curDate);
            printReport(report);
            printed = true;
//            printReportReceipt(curDate, LINE_WIDTH);

            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return printed;
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
        try {
            setPaperWidth(LINE_WIDTH);

            ReportBuilder reportBuilder = new ReportBuilder(activity, LINE_WIDTH);
            Report report = reportBuilder.getShift(shiftID);
            printReport(report);
//            printShiftDetailsReceipt(LINE_WIDTH, shiftID);

            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
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
        try {
            setPaperWidth(LINE_WIDTH);

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getSplitOrderPreview(splitedOrder);
            printReceipt(receipt);
//            super.printReceiptPreview(splitedOrder, LINE_WIDTH);

            releasePrinter();
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
        return false;
    }

    @Override
    public void printClockInOut(List<ClockInOut> clockInOuts, String clerkID) {
        try {
            setPaperWidth(LINE_WIDTH);

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getClockInOut(clockInOuts, clerkID);
            printReceipt(receipt);
//            printClockInOut(timeClocks, LINE_WIDTH, clerkID);

            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    @Override
    public void printExpenseReceipt(ShiftExpense expense) {
        try {
            setPaperWidth(LINE_WIDTH);

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getExpense(expense);
            printReceipt(receipt);
//            printExpenseReceipt(LINE_WIDTH, expense);

            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
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
            printRasterReceipt(receipt);
        }
    }

    private void printNormalReceipt(Receipt receipt) {
        if (epsonPrinter != null) {
            StringBuilder textData = new StringBuilder();
            try {
                epsonPrinter.clearCommandBuffer();
                if (receipt.getMerchantLogo() != null) {
                    epsonPrinter.addTextAlign(Printer.ALIGN_CENTER);
                    epsonPrinter.addImage(receipt.getMerchantLogo(), 0, 0,
                            receipt.getMerchantLogo().getWidth(),
                            receipt.getMerchantLogo().getHeight(),
                            Printer.COLOR_1,
                            Printer.MODE_MONO_HIGH_DENSITY,
                            Printer.HALFTONE_DITHER,
                            Printer.PARAM_DEFAULT,
                            Printer.COMPRESS_AUTO);
                    epsonPrinter.addTextAlign(Printer.PARAM_DEFAULT);
                }
                if (receipt.getMerchantHeader() != null)
                    textData.append(receipt.getMerchantHeader());
                if (receipt.getSpecialHeader() != null)
                    textData.append(receipt.getSpecialHeader());
                if (receipt.getHeader() != null)
                    textData.append(receipt.getHeader());
                epsonPrinter.addText(textData.toString());
                textData.delete(0, textData.length());

                if (receipt.getRemoteStationHeader() != null) {
                    epsonPrinter.addTextSize(8, 8);
                    textData.append(receipt.getRemoteStationHeader());
                    epsonPrinter.addTextSize(Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT);
                }
                if (receipt.getEmvDetails() != null)
                    textData.append(receipt.getEmvDetails());
                if (receipt.getSeparator() != null)
                    textData.append(receipt.getSeparator());
                for (String s : receipt.getItems()) {
                    if (s != null)
                        textData.append(s);
                }
                for (String s : receipt.getRemoteStationItems()) {
                    if (s != null) {
                        epsonPrinter.addTextSize(8, 8);
                        textData.append(s);
                        epsonPrinter.addTextSize(Printer.PARAM_DEFAULT, Printer.PARAM_DEFAULT);
                    }
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

    private void printRasterReceipt(Receipt receipt) {
        Bitmap bitmapFromText;
        setPaperWidth(LINE_WIDTH);
        ArrayList<Bitmap> rasterImages = new ArrayList<Bitmap>();
        try {
            epsonPrinter.clearCommandBuffer();
            if (receipt.getMerchantLogo() != null) {
//            int logoPosition = (PAPER_WIDTH / 3 - receipt.getMerchantLogo().getWidth()) / 2 + 30;
                epsonPrinter.addTextAlign(Printer.ALIGN_CENTER);
                epsonPrinter.addImage(receipt.getMerchantLogo(), 0, 0,
                        receipt.getMerchantLogo().getWidth(),
                        receipt.getMerchantLogo().getHeight(),
                        Printer.COLOR_1,
                        Printer.MODE_MONO_HIGH_DENSITY,
                        Printer.HALFTONE_DITHER,
                        Printer.PARAM_DEFAULT,
                        Printer.COMPRESS_AUTO);
                epsonPrinter.addTextAlign(Printer.PARAM_DEFAULT);
            }

            if (receipt.getMerchantHeader() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getMerchantHeader(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getSpecialHeader() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getSpecialHeader(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getHeader() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getHeader(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getEmvDetails() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getEmvDetails(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getSeparator() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getSeparator(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            for (String s : receipt.getItems()) {
                if (s != null) {
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                    bitmapFromText = BitmapUtils.createBitmapFromText(
                            s, FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                    rasterImages.add(bitmapFromText);
                }
            }

            if (receipt.getSeparator() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getSeparator(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getTotals() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getTotals(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getTaxes() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getTaxes(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getTotalItems() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getTotalItems(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getGrandTotal() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getGrandTotal(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getPaymentsDetails() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getPaymentsDetails(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getYouSave() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getYouSave(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getIvuLoto() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getIvuLoto(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getLoyaltyDetails() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getLoyaltyDetails(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getRewardsDetails() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getRewardsDetails(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getSignatureImage() != null) {
//                int logoPosition = (PAPER_WIDTH / 3 - receipt.getSignatureImage().getWidth()) / 2 + 30;
//                textData.appendBitmapWithAbsolutePosition(receipt.getSignatureImage(),
//                        false, logoPosition);
                rasterImages.add(receipt.getSignatureImage());
            }

            if (receipt.getSignature() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getSignature(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getMerchantFooter() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getMerchantFooter(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getSpecialFooter() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getSpecialFooter(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getTermsAndConditions() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getTermsAndConditions(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }

            if (receipt.getEnablerWebsite() != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        receipt.getEnablerWebsite(), FONT_SIZE_NORMAL, PAPER_WIDTH, typeface);
                rasterImages.add(bitmapFromText);
            }
            if (receipt.getRemoteStationHeader() != null) {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        removeNewLine(receipt.getRemoteStationHeader()), FONT_SIZE_LARGE, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }
            for (String s : receipt.getRemoteStationItems()) {
                if (s != null) {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
                    bitmapFromText = BitmapUtils.createBitmapFromText(
                            s, FONT_SIZE_LARGE, PAPER_WIDTH, typefaceNormal);
                    rasterImages.add(bitmapFromText);
                }
            }
            for (Bitmap img : rasterImages) {
                epsonPrinter.addImage(img, 0, 0,
                        img.getWidth(),
                        img.getHeight(),
                        Printer.COLOR_1,
                        Printer.MODE_MONO_HIGH_DENSITY,
                        Printer.HALFTONE_DITHER,
                        Printer.PARAM_DEFAULT,
                        Printer.COMPRESS_AUTO);
            }

            epsonPrinter.addCut(Printer.CUT_FEED);
            epsonPrinter.sendData(Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printReport(Report report) {
        if (myPref.isRasterModePrint()) {
            printRasterReport(report);
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

    private void printRasterReport(Report report) {
        Bitmap bitmapFromText;
        setPaperWidth(LINE_WIDTH);
        ArrayList<Bitmap> rasterImages = new ArrayList<Bitmap>();
        try {
            epsonPrinter.clearCommandBuffer();

            if (report.getSpecialHeader() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getSpecialHeader(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceBold);
                rasterImages.add(bitmapFromText);
            }

            if (report.getHeader() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getHeader(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getSummary() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getSummary(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getArTransactions() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getArTransactions(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getSalesByClerk() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getSalesByClerk(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getTotalsByShifts() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getTotalsByShifts(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getTotalsByTypes() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getTotalsByTypes(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getItemsSold() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getItemsSold(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getDepartmentSales() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getDepartmentSales(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getDepartmentReturns() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getDepartmentReturns(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getPayments() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getPayments(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getVoids() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getVoids(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getRefunds() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getRefunds(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getItemsReturned() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getItemsReturned(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getFooter() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getFooter(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceNormal);
                rasterImages.add(bitmapFromText);
            }

            if (report.getSpecialFooter() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getSpecialFooter(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceBold);
                rasterImages.add(bitmapFromText);
            }

            if (report.getEnablerWebsite() != null) {
                bitmapFromText = BitmapUtils.createBitmapFromText(
                        report.getEnablerWebsite(), FONT_SIZE_NORMAL, PAPER_WIDTH, typefaceBold);
                rasterImages.add(bitmapFromText);
            }

            for (Bitmap img : rasterImages) {
                epsonPrinter.addImage(img, 0, 0,
                        img.getWidth(),
                        img.getHeight(),
                        Printer.COLOR_1,
                        Printer.MODE_MONO_HIGH_DENSITY,
                        Printer.HALFTONE_DITHER,
                        Printer.PARAM_DEFAULT,
                        Printer.COMPRESS_AUTO);
            }

            epsonPrinter.addCut(Printer.CUT_FEED);
            epsonPrinter.sendData(Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String removeNewLine(String str){
        return str.substring(0,str.length()-2);
    }
}
