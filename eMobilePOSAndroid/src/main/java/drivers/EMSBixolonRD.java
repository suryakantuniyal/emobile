package drivers;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.BixolonDAO;
import com.android.database.OrdersHandler;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplitedOrder;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.Bixolon;
import com.android.emobilepos.models.realms.BixolonTax;
import com.android.emobilepos.models.realms.BixolonTransaction;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.PaymentMethod;
import com.android.support.ConsignmentTransaction;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.crashlytics.android.Crashlytics;
import com.thefactoryhka.android.controls.PrinterException;
import com.thefactoryhka.android.pa.S1PrinterData;
import com.thefactoryhka.android.pa.TfhkaAndroid;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

/**
 * Created by guarionex on 5/12/17.
 */

public class EMSBixolonRD extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {
    private static final int HEADER_LENGTH = 8;
    private static final int TAX_LENGTH_PANAMA = 3;
    private static final int TAX_LENGTH_DOMINICAN = 5;
    private static final int LINE_WIDTH = 48;
    String msg = "Failed to connect";
    private EMSDeviceManager edm;
    private BixolonCountry country;

    public EMSBixolonRD(BixolonCountry country) {

        this.country = country;
    }

    public void connect(final Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        this.edm = edm;
        if (printerTFHKA == null) {
            printerTFHKA = getInstanceOfTfhka(); //new TfhkaAndroid();
        }
        myPref = new MyPreferences(activity);
        boolean connect = connectTFHKA();
        if (connect) {
            setSendCommandRetry(printerTFHKA);
//            printerTFHKA.setSendCmdRetryAttempts(5);
//            printerTFHKA.setSendCmdRetryInterval(1000);
            edm.driverDidConnectToDevice(this, true);
        } else {
            edm.driverDidNotConnectToDevice(this, msg, true);
        }
    }

    private Object getInstanceOfTfhka() {
        if (country == BixolonCountry.PANAMA)
            return new TfhkaAndroid();
        else
            return new com.thefactoryhka.android.rd.TfhkaAndroid();
    }

    private void setSendCommandRetry(Object printer) {
        if (printer instanceof TfhkaAndroid) {
            ((TfhkaAndroid) printer).setSendCmdRetryAttempts(5);
            ((TfhkaAndroid) printer).setSendCmdRetryInterval(1000);
        } else {
            ((com.thefactoryhka.android.rd.TfhkaAndroid) printer).setSendCmdRetryAttempts(5);
            ((com.thefactoryhka.android.rd.TfhkaAndroid) printer).setSendCmdRetryInterval(1000);
        }
    }

    @Override
    public boolean autoConnect(final Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter, String portName, String portNumber) {
        this.activity = activity;
        this.edm = edm;
        if (printerTFHKA == null) {
            printerTFHKA = getInstanceOfTfhka();//new TfhkaAndroid();
        }
        myPref = new MyPreferences(activity);
        boolean connect = connectTFHKA();
        if (connect) {
            setSendCommandRetry(printerTFHKA);
//            printerTFHKA.setSendCmdRetryAttempts(5);
//            printerTFHKA.setSendCmdRetryInterval(1000);
            edm.driverDidConnectToDevice(this, false);
        } else {
            edm.driverDidNotConnectToDevice(this, msg, false);
        }
        return true;
    }

    private boolean connectTFHKA() {
        if (printerTFHKA instanceof TfhkaAndroid) {
            return ((TfhkaAndroid) printerTFHKA).estado || ((TfhkaAndroid) printerTFHKA).OpenBTPrinter(myPref.getPrinterMACAddress());
        } else {
            return ((com.thefactoryhka.android.rd.TfhkaAndroid) printerTFHKA).estado
                    || ((com.thefactoryhka.android.rd.TfhkaAndroid) printerTFHKA).OpenBTPrinter(myPref.getPrinterMACAddress());
        }

    }

//    public Object getPrinterTFHKA() {
//        return printerTFHKA;
//    }

    public Date getCurrentPrinterDateTime() {
        if (printerTFHKA instanceof TfhkaAndroid) {
            try {
                return ((TfhkaAndroid) printerTFHKA).getS1PrinterData().getCurrentPrinterDateTime();
            } catch (PrinterException e) {
                Crashlytics.logException(e);
            }
        } else {
            try {
                return ((com.thefactoryhka.android.rd.TfhkaAndroid) printerTFHKA).getS1PrinterData().getCurrentPrinterDate();
            } catch (PrinterException e) {
                Crashlytics.logException(e);
            }
        }
        return null;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        OrdersHandler ordersHandler = new OrdersHandler(activity);
        Order order = ordersHandler.getOrder(ordID);
        Bixolon bixolon = BixolonDAO.getBixolon();
        Global.OrderType type = Global.OrderType.getByCode(Integer.parseInt(order.ord_type));
        boolean cmd = printBixolonMerchantName(bixolon.getMerchantName());
        cmd = printRUC(bixolon.getRuc());
        cmd = sendNFC(bixolon.getNcf());
        cmd = openDocument(type);
        if (cmd) {
            List<OrderProduct> orderProducts = order.getOrderProducts();
            for (OrderProduct product : orderProducts) {
                cmd = printBixolonItem(product, saleTypes == Global.OrderType.RETURN);
                if (!cmd) {
                    break;
                }
            }
            if (cmd) {
                cmd = printBixolonSubTotal();
                if (cmd) {
                    cmd = printBixolonTotal(order);
                    if (cmd) {
                        cmd = closeDocument();
                    }
                }
            }
        }
        String machineNumber = getRegisteredMachineNumber();
        int lastInvoice = cmd ? getNumberOfLastInvoice() : 0;
        order.setBixolonTransactionId(String.format(Locale.getDefault(), "%s-%08d", machineNumber, lastInvoice));
        ordersHandler.updateBixolonTransactionId(order);
        if (!cmd) {
            saveFailedTransaction(order);
        } else {
            BixolonDAO.removeFailedOrder(order.ord_id);
        }
        return cmd;
    }


    private String getRegisteredMachineNumber() {
        if (printerTFHKA instanceof TfhkaAndroid) {
            try {
                return ((TfhkaAndroid) printerTFHKA).getS1PrinterData().getRegisteredMachineNumber();
            } catch (PrinterException e) {
                Crashlytics.logException(e);
            }
        } else {
            try {
                return ((com.thefactoryhka.android.rd.TfhkaAndroid) printerTFHKA).getS1PrinterData().getRegisteredMachineNumber();
            } catch (PrinterException e) {
                Crashlytics.logException(e);
            }
        }
        return "";
    }

    private int getNumberOfLastInvoice() {
        if (printerTFHKA instanceof TfhkaAndroid) {
            try {
                return ((TfhkaAndroid) printerTFHKA).getXReport().getNumberOfLastInvoice();
            } catch (PrinterException e) {
                Crashlytics.logException(e);
            }
        } else {
//            return ((com.thefactoryhka.android.rd.TfhkaAndroid)printerTFHKA).getS1PrinterData().getNumberOfLastInvoice();
        }
        return 0;
    }

    private boolean closeDocument() {
        return SendCmd("199");
    }

    private boolean openDocument(Global.OrderType orderType) {
        switch (country) {
            case DOMINICAN_REPUBLIC: {
                if (orderType == Global.OrderType.RETURN) {
                    return SendCmd("/1");
                } else {
                    return SendCmd("/0");
                }
            }
        }
        return true;
    }

    private String getCommentsChar(Global.OrderType saleTypes) {
        switch (saleTypes) {
            case RETURN:
                return "A";
            default:
                return "@";
        }
    }

    private void saveFailedTransaction(Order order) {
        //            voidLastTransaction();
        BixolonTransaction bixolonTransaction = new BixolonTransaction(order);
        BixolonDAO.insertFailedOrder(bixolonTransaction);
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold) {
        setPaperWidth(LINE_WIDTH);
        printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, null);
        //        return printTransaction(ordID, null, false, false, null);
        return true;
    }

    @Override
    public boolean printPaymentDetails(String payID, int isFromMainMenu, boolean isReprint, EMVContainer emvContainer) {
        super.printPaymentDetailsReceipt(payID, isFromMainMenu, isReprint, LINE_WIDTH, emvContainer);
        return true;
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
        printReportReceipt(curDate, LINE_WIDTH);
        return true;
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
        super.printShiftDetailsReceipt(LINE_WIDTH, shiftID);
    }

    @Override
    public void printEndOfDayReport(String date, String clerk_id, boolean printDetails) {
        printEndOfDayReportReceipt(date, LINE_WIDTH, printDetails);
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

    @Override
    public void printClockInOut(List<ClockInOut> clockInOuts, String clerkID) {
        super.printClockInOut(clockInOuts, LINE_WIDTH, clerkID);
    }

    public boolean sendDateTimeCommand(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        boolean cmd = SendCmd("PJ3201"); //set print for retail mode
        cmd = cmd && SendCmd("PG" + DateUtils.getDateAsString(new Date(), "ddMMyy"));
        cmd = cmd && SendCmd("PF" + DateUtils.getDateAsString(new Date(), "HH:mm:ss"));
        return cmd;
    }

    public boolean sendHeaders(String[] headers) {
        SendCmd("I0Z0");
        boolean cmd = true;
        for (int i = 0; i < HEADER_LENGTH; i++) {
            if (!cmd) {
                return false;
            }
            if (i < headers.length) {
                cmd = SendCmd("PH0" + (i + 1) + headers[i]);
            } else {
                cmd = SendCmd("PH0" + (i + 1) + "");
            }
        }
        return cmd;
    }

    public boolean sendFooters(String[] footers) {
        SendCmd("I0Z0");
        boolean cmd = true;
        for (int i = 0; i < HEADER_LENGTH; i++) {
            if (!cmd) {
                return false;
            }
            if (i < footers.length) {
                cmd = SendCmd("PH9" + (i + 1) + footers[i]);
            } else {
                cmd = SendCmd("PH9" + (i + 1) + "");
            }
        }
        return cmd;
    }

    public boolean sendTaxes(List<Tax> taxes) {
        String taxCmd = "PT";
        int taxLength = country == BixolonCountry.PANAMA ? TAX_LENGTH_PANAMA : TAX_LENGTH_DOMINICAN;
        String taxChar = country == BixolonCountry.PANAMA ? "1" : "2";
        BixolonDAO.clearTaxes();
        for (int i = 0; i < taxLength; i++) {
            if (i < taxes.size()) {
                taxCmd += taxChar + String.format(Locale.getDefault(), "%05.02f",
                        Global.getRoundBigDecimal(Global.getBigDecimalNum(taxes.get(i).getTaxRate()), 2));
                switch (i) {
                    case 0:
                        BixolonDAO.addTax(taxes.get(i).getTaxId(), taxes.get(i).getTaxCodeId(), "!");
                        break;
                    case 1:
                        BixolonDAO.addTax(taxes.get(i).getTaxId(), taxes.get(i).getTaxCodeId(), "\"");
                        break;
                    case 2:
                        BixolonDAO.addTax(taxes.get(i).getTaxId(), taxes.get(i).getTaxCodeId(), "#");
                        break;
                    case 3:
                        BixolonDAO.addTax(taxes.get(i).getTaxId(), taxes.get(i).getTaxCodeId(), "$");
                        break;
                    case 4:
                        BixolonDAO.addTax(taxes.get(i).getTaxId(), taxes.get(i).getTaxCodeId(), "%");
                        break;
                }
            } else {
                taxCmd += "20000";
            }
        }
        boolean cmd = SendCmd("I0Z0");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        taxCmd = StringUtils.deleteAny(taxCmd, ".");
        if (cmd) {
            cmd = SendCmd(taxCmd);
            if (!cmd) {//retries if fails
                cmd = SendCmd(taxCmd);
            }
        } else {
            return false;
        }
        //PT command apply the taxes rates. Can be executed 64 times max.
        if (cmd) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cmd = SendCmd("Pt");
        }
        return cmd;
    }

    public boolean sendPaymentMethods(List<PaymentMethod> paymentMethods) {
        boolean cmd = true;
        int i = 1;
        BixolonDAO.clearPaymentMethods();
        for (PaymentMethod paymentMethod : paymentMethods) {
            BixolonDAO.addPaymentMethod(i, paymentMethod);
            cmd = SendCmd("PE" + String.format(Locale.getDefault(), "%02d", i) + paymentMethod.getPaymethod_name());
            i++;
            if (!cmd) {
                break;
            }
        }
        return cmd;
    }

    public boolean printSetupInfo() {
        return SendCmd("D");
    }

    private boolean printRUC(String ruc) {
        if (TextUtils.isEmpty(ruc)) {
            return true;
        }
        switch (country) {
            case PANAMA: {
                return SendCmd(String.format("jR%s", ruc));
            }
            case DOMINICAN_REPUBLIC: {
                return SendCmd(String.format("iR0%s", ruc));
            }
            default:
                return true;
        }
    }

    private boolean sendNFC(String nfc) {
        if (TextUtils.isEmpty(nfc)) {
            return true;
        }
        switch (country) {
            case DOMINICAN_REPUBLIC: {
                return SendCmd(String.format("F%18s", nfc));
            }
            default:
                return true;
        }
    }

    private boolean printBixolonMerchantName(String merchantName) {
        if (TextUtils.isEmpty(merchantName)) {
            return true;
        }
        switch (country) {
            case PANAMA: {
                return SendCmd(String.format("jS%s", merchantName));
            }
            case DOMINICAN_REPUBLIC: {
                return SendCmd(String.format("iS0%s", merchantName));
            }
            default:
                return true;
        }
    }

    private boolean printBixolonItemComments(String comments) {
        if (TextUtils.isEmpty(comments)) {
            return true;
        }
        switch (country) {
            case PANAMA: {
                return SendCmd(String.format("B%s", comments));
            }
            default:
                return true;
        }
    }

    private boolean printCustomerInfo(String data) {
        switch (country) {
            case PANAMA: {
                return SendCmd(String.format("j2%s", data));
            }
            case DOMINICAN_REPUBLIC: {
                return SendCmd(String.format("i00%s", data));
            }
            default:
                return true;
        }
    }

    private boolean printBixolonSubTotal() {
        return SendCmd("3");
    }

    private boolean printSubTotalDiscount(Order order) {
        String command;
        if (order.ord_discount_id.equalsIgnoreCase("")) {
            command = String.format(Locale.getDefault(), "p-%04d", order.ord_globalDiscount);
        } else {
            command = String.format(Locale.getDefault(), "q-%04d", order.ord_discount);
        }
        return SendCmd(command);
    }

    private boolean printBixolonItem(OrderProduct product, boolean isCredit) {
        AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
        BixolonTax tax = BixolonDAO.getTax(product.getProd_taxId(), assignEmployee.getTaxDefault());
        String cmnd;
        BigDecimal totalPrice = Global.getBigDecimalNum(product.getFinalPrice(), 2).add(product.getProd_taxValue());
        int scaleQty = country == BixolonCountry.PANAMA ? 2 : 2;
        if (isCredit) {
            cmnd = String.format(Locale.getDefault(), "d%s%s%s",
                    String.format("%0" + (10 - Global.getRoundBigDecimal(totalPrice, 2).toString().replace(".", "").length()) + "d%s", 0, Global.getRoundBigDecimal(totalPrice, 2).toString().replace(".", "")),
                    String.format("%0" + (8 - Global.getBigDecimalNum(product.getOrdprod_qty(), scaleQty).abs().toString().replace(".", "").length()) + "d%s", 0, Global.getBigDecimalNum(product.getOrdprod_qty(), scaleQty).abs().toString().replace(".", "")),
                    product.getOrdprod_name());
        } else {
            cmnd = String.format(Locale.getDefault(), "%s%s%s%s",
                    tax == null ? " " : tax.getBixolonChar(),
                    String.format("%0" + (10 - Global.getRoundBigDecimal(totalPrice, 2).toString().replace(".", "").length()) + "d%s", 0, Global.getRoundBigDecimal(totalPrice, 2).toString().replace(".", "")),
                    String.format("%0" + (8 - Global.getBigDecimalNum(product.getOrdprod_qty(), scaleQty).toString().replace(".", "").length()) + "d%s", 0, Global.getBigDecimalNum(product.getOrdprod_qty(), scaleQty).toString().replace(".", "")),
                    product.getOrdprod_name());
        }
        boolean cmd = SendCmd(cmnd);
        if (cmd) {
            if (!TextUtils.isEmpty(product.getDiscount_id())) {
                int scale = product.isDiscountFixed() ? 8 : 4;
                String discount = Global.getBigDecimalNum(product.getDisAmount(), 2).toString().replace(".", "");
                if (discount.length() < scale) {
                    discount = String.format(Locale.getDefault(), "%0" + (scale - discount.length()) + "d%s", 0, discount);
                }
                if (product.isDiscountFixed()) {
                    cmnd = "q" + "-" + discount;
                } else {
                    cmnd = "p" + "-" + discount;
                }
                cmd = SendCmd(cmnd);
            }
        }
        return cmd;
    }

    private boolean printBixolonPayments(List<Payment> payments) {
        String command = null;
        if (country == BixolonCountry.PANAMA) {

        } else {
            String payType = payments.size() == 1 ? "1" : "2";
            for (Payment payment : payments) {
                switch (payment.getPaymentMethod().getPaymentmethod_type().toUpperCase()) {
//                    case "CASH":
//                        command = String.format(Locale.getDefault(), "%s01%012d", payType, Integer.parseInt(StringUtils.deleteAny(payment.getPay_amount(), ".")));
//                        break;
                    case "CHECK":
                        command = String.format(Locale.getDefault(), "%s02%012d", payType,
                                Integer.parseInt(StringUtils.deleteAny(Global.getBigDecimalNum(payment.getPay_amount(), 2).toString(), ".")));
                        break;
                    case "DISCOVER":
                    case "MASTERCARD":
                    case "VISA":
                    case "DEBITCARD":
                        command = String.format(Locale.getDefault(), "%s03%012d", payType,
                                Integer.parseInt(StringUtils.deleteAny(Global.getBigDecimalNum(payment.getPay_amount(), 2).toString(), ".")));
                        break;
                    case "LOYALTYCARD":
                    case "GIFTCARD":
                    case "REWARD":
                        command = String.format(Locale.getDefault(), "%s08%012d", payType,
                                Integer.parseInt(StringUtils.deleteAny(Global.getBigDecimalNum(payment.getPay_amount(), 2).toString(), ".")));
                        break;
                }
                if (command != null && !SendCmd(command)) {
                    return false;
                }
            }
        }
        SendCmd("101");
        return true;
    }

    private boolean printBixolonTotal(Order order) {
        PaymentsHandler paymentsHandler = new PaymentsHandler(activity);
        List<Payment> orderPayments = paymentsHandler.getOrderPayments(order.ord_id);
        String command = null;
        if (country == BixolonCountry.PANAMA) {
            if (Global.OrderType.getByCode(Integer.parseInt(order.ord_type)) == Global.OrderType.RETURN) {
                int paymentId = BixolonDAO.getPaymentMethodId(orderPayments.get(0).getPaymentMethod());
                command = String.format(Locale.getDefault(), "1%02d", paymentId);
            } else {
                if (orderPayments.size() == 1) {
                    int paymentId = BixolonDAO.getPaymentMethodId(orderPayments.get(0).getPaymentMethod());
                    command = String.format(Locale.getDefault(), "1%02d", paymentId);
                } else {
                    for (Payment payment : orderPayments) {
                        int paymentId = BixolonDAO.getPaymentMethodId(payment.getPaymentMethod());
                        command = String.format(Locale.getDefault(), "2%012d", payment.getPay_amount());
                    }
                }
            }
            return SendCmd(command);
        } else {
            return printBixolonPayments(orderPayments);
//            String payType = orderPayments.size() == 1 ? "1" : "2";
//            for (Payment payment : orderPayments) {
//                switch (payment.getPaymentMethod().getPaymentmethod_type().toUpperCase()) {
////                    case "CASH":
////                        command = String.format(Locale.getDefault(), "%s01%012d", payType, Integer.parseInt(StringUtils.deleteAny(payment.getPay_amount(), ".")));
////                        break;
//                    case "CHECK":
//                        command = String.format(Locale.getDefault(), "%s02%012d", payType,
//                                Integer.parseInt(StringUtils.deleteAny(Global.getBigDecimalNum(payment.getPay_amount(), 2).toString(), ".")));
//                        break;
//                    case "DISCOVER":
//                    case "MASTERCARD":
//                    case "VISA":
//                    case "DEBITCARD":
//                        command = String.format(Locale.getDefault(), "%s03%012d", payType,
//                                Integer.parseInt(StringUtils.deleteAny(Global.getBigDecimalNum(payment.getPay_amount(), 2).toString(), ".")));
//                        break;
//                    case "LOYALTYCARD":
//                    case "GIFTCARD":
//                    case "REWARD":
//                        command = String.format(Locale.getDefault(), "%s08%012d", payType,
//                                Integer.parseInt(StringUtils.deleteAny(Global.getBigDecimalNum(payment.getPay_amount(), 2).toString(), ".")));
//                        break;
//                }
//                if (command != null && !SendCmd(command)) {
//                    return false;
//                }
//            }
        }
//        SendCmd("101");
//        return true;
    }

    private boolean voidLastTransaction() {
        return SendCmd("7");
    }

    private boolean printOrderId(Order order) {
        if (country == BixolonCountry.PANAMA) {
            return SendCmd("jF" + String.format("%0" + (22 - order.ord_id.length()) + "d%s", 0, order.ord_id));
        } else
            return true;
    }

    public void printZReport() throws PrinterException {
        if (printerTFHKA instanceof TfhkaAndroid) {
            ((TfhkaAndroid) printerTFHKA).printZReport();
        } else {
            ((com.thefactoryhka.android.rd.TfhkaAndroid) printerTFHKA).printZReport();
        }
    }

    public void printXReport() throws PrinterException {
        if (printerTFHKA instanceof TfhkaAndroid) {
            ((TfhkaAndroid) printerTFHKA).printXReport();
        } else {
            ((com.thefactoryhka.android.rd.TfhkaAndroid) printerTFHKA).printXReport();
        }
    }

    public drivers.bixolon.S1PrinterData getS1PrinterData() throws PrinterException {
        drivers.bixolon.S1PrinterData printerData = new drivers.bixolon.S1PrinterData();
        if (printerTFHKA instanceof TfhkaAndroid) {
            S1PrinterData data = ((TfhkaAndroid) printerTFHKA).getS1PrinterData();
            printerData.setAuditReportsCounter(data.getAuditReportsCounter());
            printerData.setCashierNumber(data.getCashierNumber());
            printerData.setCurrentPrinterDateTime(data.getCurrentPrinterDateTime());
            printerData.setDailyClosureCounter(data.getDailyClosureCounter());
            printerData.setDV(data.getDV());
            printerData.setLastCNNumber(data.getLastCNNumber());
            printerData.setLastInvoiceNumber(data.getLastInvoiceNumber());
            printerData.setNumberNonFiscalDocuments(data.getNumberNonFiscalDocuments());
            printerData.setQuantityNonFiscalDocuments(data.getQuantityNonFiscalDocuments());
            printerData.setQuantityOfCNToday(data.getQuantityOfCNsToday());
            printerData.setQuantityOfDebitNoteToday(data.getQuantityOfNDsToday());
            printerData.setRegisteredMachineNumber(data.getRegisteredMachineNumber());
            printerData.setRUC(data.getRUC());
            printerData.setTotalDailySales(data.getTotalDailySales());
        } else {
            com.thefactoryhka.android.rd.S1PrinterData data = ((com.thefactoryhka.android.rd.TfhkaAndroid) printerTFHKA).getS1PrinterData();
            printerData.setAuditReportsCounter(data.getFiscalReportsCounter());
            printerData.setCashierNumber(data.getCashierNumber());
            printerData.setCurrentPrinterDateTime(data.getCurrentPrinterDate());
            printerData.setDailyClosureCounter(data.getDailyClosureCounter());
//            printerData.setDV(data.getDV());
//            printerData.setLastCNNumber(data.getNumLastCNNumber());
//            printerData.setLastInvoiceNumber(data.getLastInvoiceNumber());
            printerData.setNumberNonFiscalDocuments(data.getNumberOfLastNonFiscalDocument());
            printerData.setQuantityNonFiscalDocuments(data.getQuantityNonFiscalDocuments());
            printerData.setQuantityOfCNToday(data.getQuantityTransactionToday());
//            printerData.setQuantityOfDebitNoteToday(data.getQuantityOfNDsToday());
            printerData.setRegisteredMachineNumber(data.getRegisteredMachineNumber());
            printerData.setRUC(data.getRNC());
//            printerData.setTotalDailySales(data.getSaTotalDailySales());
        }
        return printerData;
    }

    public Boolean printBixolonSettings() {
        return SendCmd("D");
    }

    public enum BixolonCountry {
        PANAMA, DOMINICAN_REPUBLIC
    }

}