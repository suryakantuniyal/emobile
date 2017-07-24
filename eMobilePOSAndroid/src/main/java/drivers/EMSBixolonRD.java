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
import com.thefactoryhka.android.controls.PrinterException;
import com.thefactoryhka.android.pa.ReportData;
import com.thefactoryhka.android.pa.S1PrinterData;
import com.thefactoryhka.android.pa.TfhkaAndroid;

import org.springframework.util.StringUtils;

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
    String msg = "Failed to connectTFHKA";
    private EMSDeviceManager edm;
    private BixolonCountry country;

    public EMSBixolonRD(BixolonCountry country) {

        this.country = country;
    }

    public void connectTFHKA(final Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        this.edm = edm;
        if (printerTFHKA == null) {
            printerTFHKA = new TfhkaAndroid();
        }
        myPref = new MyPreferences(activity);
        boolean connect = connectTFHKA();
        if (connect) {
            printerTFHKA.setSendCmdRetryAttempts(5);
            printerTFHKA.setSendCmdRetryInterval(1000);
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
        boolean connect = connectTFHKA();
        if (connect) {
            printerTFHKA.setSendCmdRetryAttempts(5);
            printerTFHKA.setSendCmdRetryInterval(1000);
            edm.driverDidConnectToDevice(this, false);
        } else {
            edm.driverDidNotConnectToDevice(this, msg, false);
        }
        return true;
    }

    private boolean connectTFHKA() {
        return printerTFHKA.estado || printerTFHKA.OpenBTPrinter(myPref.getPrinterMACAddress());
    }

    public TfhkaAndroid getPrinterTFHKA() {
        return printerTFHKA;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        OrdersHandler ordersHandler = new OrdersHandler(activity);
        Order order = ordersHandler.getOrder(ordID);
        Bixolon bixolon = BixolonDAO.getBixolon();
        boolean cmd = printOrderId(order);
        if (cmd) {
            cmd = printRUC(bixolon.getRuc());
            if (cmd) {
                cmd = printMerchantName(bixolon.getMerchantName());
                if (cmd) {
                    Global.OrderType type = Global.OrderType.getByCode(Integer.parseInt(order.ord_type));
                    String typeDetails = getOrderTypeDetails(fromOnHold, order, LINE_WIDTH, type).replace(" ", "");
                    cmd = printerTFHKA.SendCmd(String.format("%s%s", getCommentsChar(saleTypes), typeDetails));
                    if (cmd) {
                        List<OrderProduct> orderProducts = order.getOrderProducts();
                        for (OrderProduct product : orderProducts) {
                            cmd = printItemComments(product.getOrdprod_desc());
                            if (cmd) {
                                cmd = printItem(product, saleTypes == Global.OrderType.RETURN);
                                if (!cmd) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        if (cmd) {
                            cmd = printSubTotal();
                            if (cmd) {
                                cmd = printTotal(order);
                            }
                        }
                    }
                }
            }
        }

        try {
            ReportData xReport = printerTFHKA.getXReport();
            S1PrinterData printerData = printerTFHKA.getS1PrinterData();
            String machineNumber = printerData.getRegisteredMachineNumber();
            int lastInvoice = cmd ? xReport.getNumberOfLastInvoice() : 0;
            order.setBixolonTransactionId(String.format(Locale.getDefault(), "%s-%08d", machineNumber, lastInvoice));
            ordersHandler.updateBixolonTransactionId(order);
        } catch (PrinterException e) {
            e.printStackTrace();
        }
        if (!cmd) {
            saveFailedTransaction(order);
        } else {
            BixolonDAO.removeFailedOrder(order.ord_id);
        }
        return cmd;
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
        int taxLength = country == BixolonCountry.PANAMA ? TAX_LENGTH_PANAMA : TAX_LENGTH_DOMINICAN;
        String taxChar = country == BixolonCountry.PANAMA ? "1" : "2";
        BixolonDAO.clearTaxes();
        for (int i = 0; i < taxLength; i++) {
            if (i < taxes.size()) {
                taxCmd += taxChar + String.format(Locale.getDefault(), "%05.02f",
                        Global.getRoundBigDecimal(Global.getBigDecimalNum(taxes.get(i).getTaxRate()), 2));
            } else {
                taxCmd += "20000";
            }
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
            }
        }
        boolean cmd = printerTFHKA.SendCmd("I0Z0");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        taxCmd = StringUtils.deleteAny(taxCmd, ".");
        if (cmd) {
            cmd = printerTFHKA.SendCmd(taxCmd);
            if (!cmd) {//retries if fails
                cmd = printerTFHKA.SendCmd(taxCmd);
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
            cmd = printerTFHKA.SendCmd("Pt");
        }
        return cmd;
    }

    public boolean sendPaymentMethods(List<PaymentMethod> paymentMethods) {
        boolean cmd = true;
        int i = 1;
        BixolonDAO.clearPaymentMethods();
        for (PaymentMethod paymentMethod : paymentMethods) {
            BixolonDAO.addPaymentMethod(i, paymentMethod);
            cmd = printerTFHKA.SendCmd("PE" + String.format(Locale.getDefault(), "%02d", i) + paymentMethod.getPaymethod_name());
            i++;
            if (!cmd) {
                break;
            }
        }
        return cmd;
    }

    public boolean printSetupInfo() {
        return printerTFHKA.SendCmd("D");
    }

    private boolean printRUC(String ruc) {
        if (TextUtils.isEmpty(ruc)) {
            return true;
        } else {
            return printerTFHKA.SendCmd(String.format("jR%s", ruc));
        }
    }

    private boolean printMerchantName(String merchantName) {
        if (TextUtils.isEmpty(merchantName)) {
            return true;
        } else {
            return printerTFHKA.SendCmd(String.format("jS%s", merchantName));
        }
    }

    private boolean printItemComments(String comments) {
        if (TextUtils.isEmpty(comments)) {
            return true;
        } else {
            return printerTFHKA.SendCmd(String.format("B%s", comments));
        }
    }

    private boolean printCustomerInfo(String data) {
        return printerTFHKA.SendCmd(String.format("j2%s", data));
    }

    private boolean printSubTotal() {
        return printerTFHKA.SendCmd("3");
    }

    private boolean printSubTotalDiscount(Order order) {
        String command;
        if (order.ord_discount_id.equalsIgnoreCase("")) {
            command = String.format("p-%04d", order.ord_globalDiscount);
        } else {
            command = String.format("q-%04d", order.ord_discount);
        }
        return printerTFHKA.SendCmd(command);
    }

    private boolean printItem(OrderProduct product, boolean isCredit) {
        AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
        BixolonTax tax = BixolonDAO.getTax(product.getProd_taxId(), assignEmployee.getTaxDefault());
        String cmnd;
        if (isCredit) {
            cmnd = String.format(Locale.getDefault(), "d%s%s%s",
                    String.format("%0" + (10 - Global.getBigDecimalNum(product.getFinalPrice(), 2).toString().replace(".", "").length()) + "d%s", 0, Global.getBigDecimalNum(product.getFinalPrice(), 2).toString().replace(".", "")),
                    String.format("%0" + (8 - Global.getBigDecimalNum(product.getOrdprod_qty(), 3).abs().toString().replace(".", "").length()) + "d%s", 0, Global.getBigDecimalNum(product.getOrdprod_qty(), 3).abs().toString().replace(".", "")),
                    product.getOrdprod_name());
        } else {
            cmnd = String.format(Locale.getDefault(), "%s%s%s%s",
                    tax == null ? " " : tax.getBixolonChar(),
                    String.format("%0" + (10 - Global.getBigDecimalNum(product.getFinalPrice(), 2).toString().replace(".", "").length()) + "d%s", 0, Global.getBigDecimalNum(product.getFinalPrice(), 2).toString().replace(".", "")),
                    String.format("%0" + (8 - Global.getBigDecimalNum(product.getOrdprod_qty(), 3).toString().replace(".", "").length()) + "d%s", 0, Global.getBigDecimalNum(product.getOrdprod_qty(), 3).toString().replace(".", "")),
                    product.getOrdprod_name());
        }
        return printerTFHKA.SendCmd(cmnd);
    }

    private boolean printTotal(Order order) {
        PaymentsHandler paymentsHandler = new PaymentsHandler(activity);
        List<Payment> orderPayments = paymentsHandler.getOrderPayments(order.ord_id);
        String command = null;
        if (Global.OrderType.getByCode(Integer.parseInt(order.ord_type)) == Global.OrderType.RETURN) {
            int paymentId = BixolonDAO.getPaymentmetodId(orderPayments.get(0).getPaymentMethod());
            command = String.format(Locale.getDefault(), "1%02d", paymentId);
        } else {
            if (orderPayments.size() == 1) {
                int paymentId = BixolonDAO.getPaymentmetodId(orderPayments.get(0).getPaymentMethod());
                command = String.format(Locale.getDefault(), "1%02d", paymentId);
            } else {
                for (Payment payment : orderPayments) {
                    int paymentId = BixolonDAO.getPaymentmetodId(payment.getPaymentMethod());
                    command = String.format(Locale.getDefault(), "2%012d", payment.getPay_amount());
                }
            }
        }
        return printerTFHKA.SendCmd(command);
    }

    private boolean voidLastTransaction() {
        return printerTFHKA.SendCmd("7");
    }

    private boolean printOrderId(Order order) {
        return printerTFHKA.SendCmd("jF" + String.format("%0" + (22 - order.ord_id.length()) + "d%s", 0, order.ord_id));
    }

    public enum BixolonCountry {
        PANAMA, DOMINICAN_REPUBLIC
    }

}