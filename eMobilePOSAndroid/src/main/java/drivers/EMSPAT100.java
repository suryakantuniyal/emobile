package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.StarMicronics.jasura.JAException;
import com.android.dao.StoredPaymentsDAO;
import com.android.database.PayMethodsHandler;
import com.android.database.PaymentsHandler;
import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.PaymentDetails;
import com.android.emobilepos.models.SplitedOrder;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.partner.pt100.cashdrawer.CashDrawerApiContext;
import com.partner.pt100.cashdrawer.CashDrawerManage;
import com.partner.pt100.display.DisplayLineApiContext;
import com.partner.pt100.display.DisplayManager;
import com.partner.pt100.printer.PrinterManage;
import com.starmicronics.stario.StarIOPortException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;
import plaintext.EMSPlainTextHelper;

//import com.partner.pt100.cashdrawer.CashDrawerApiContext;
//import com.partner.pt100.cashdrawer.CashDrawerManage;

public class EMSPAT100 extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    CashDrawerApiContext cashDrawerApiContext = new CashDrawerManage();

    private List<String> printPref;
    private CreditCardInfo cardManager;
    private Encrypt encrypt;
    private EMSDeviceManager edm;
    private EMSDeviceDriver thisInstance;
    private ProgressDialog myProgressDialog;

    private final String FORMAT = "windows-1252";
    private final int LINE_WIDTH = 32;

    public static DisplayLineApiContext terminalDisp;
    // private CashDrawerApiContext terminalCashDrawer;

    public static DisplayLineApiContext getTerminalDisp() {
        if (terminalDisp == null) {
            terminalDisp = new DisplayManager();
            terminalDisp.open();
            terminalDisp.initialize();
        }
        return terminalDisp;
    }

    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);

        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);

        this.edm = edm;
        thisInstance = this;

        new processConnectionAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
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

        terminalDisp = new DisplayManager();
        terminalDisp.open();
        terminalDisp.initialize();

        // terminalCashDrawer = new CashDrawerManage();

        printerApi = new PrinterManage();
        printerApi.open();
        int res = printerApi.initPrinter();
        if (res == 0) {
            this.edm.driverDidConnectToDevice(thisInstance, false);
            return true;
        } else
            this.edm.driverDidNotConnectToDevice(thisInstance, null, false);

        return false;
    }

    public class processConnectionAsync extends AsyncTask<Integer, String, String> {

        String msg = new String();
        boolean didConnect = false;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage(activity.getString(R.string.progress_connecting_printer));
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected String doInBackground(Integer... params) {
            if (myPref.isPAT100()) {
                printerApi = new PrinterManage();
                printerApi.open();
                int res = printerApi.initPrinter();
                if (res == 0) {
                    didConnect = true;
                }
                terminalDisp = new DisplayManager();
                terminalDisp.open();
                terminalDisp.initialize();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            myProgressDialog.dismiss();

            if (didConnect) {
                edm.driverDidConnectToDevice(thisInstance, true);
            } else {

                edm.driverDidNotConnectToDevice(thisInstance, msg, true);
            }
        }
    }

    @Override
    public void registerAll() {
        this.registerPrinter();
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory, boolean fromOnHold) {
        printTransaction(ordID, type, isFromHistory, fromOnHold, null);
        return true;
    }


    @Override
    public boolean printPaymentDetails(String payID, int type, boolean isReprint, EMVContainer emvContainer) {
        EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
        printPref = myPref.getPrintingPreferences();

        PaymentsHandler payHandler = new PaymentsHandler(activity);
        PaymentDetails paymentDetails;
        boolean isStoredFwd = false;
        long pay_count = payHandler.paymentExist(payID);
        if (pay_count == 0) {
            isStoredFwd = true;
            StoredPaymentsDAO dbStoredPay = new StoredPaymentsDAO(activity);
            paymentDetails = dbStoredPay.getPrintingForPaymentDetails(payID, type);
        } else {
            paymentDetails = payHandler.getPrintingForPaymentDetails(payID, type);
        }
        StringBuilder sb = new StringBuilder();
        boolean isCashPayment = false;
        boolean isCheckPayment = false;
        String constantValue = null;
        String creditCardFooting = "";

        if (paymentDetails.getPaymethod_name().toUpperCase(Locale.getDefault()).trim().equals("CASH"))
            isCashPayment = true;
        else if (paymentDetails.getPaymethod_name().toUpperCase(Locale.getDefault()).trim().equals("CHECK"))
            isCheckPayment = true;
        else {
            constantValue = getString(R.string.receipt_included_tip);
            creditCardFooting = getString(R.string.receipt_creditcard_terms);
        }

        try {
            printImage(0);
        } catch (StarIOPortException e) {
            e.printStackTrace();
        } catch (JAException e) {
            e.printStackTrace();
        }

        if (printPref.contains(MyPreferences.print_header))
            this.printHeader();

        // port.writePort(enableCenter, 0, enableCenter.length); //enable center

        sb.append("* ").append(paymentDetails.getPaymethod_name());
        if (paymentDetails.getIs_refund().equals("1"))
            sb.append(" Refund *\n\n\n");
        else
            sb.append(" Sale *\n\n\n");

        printerApi.printData(textHandler.centeredString(sb.toString(), LINE_WIDTH));
        // port.writePort(sb.toString().getBytes(FORMAT), 0, sb.length());
        // port.writePort(disableCenter, 0, disableCenter.length); //disable
        // center
        sb.setLength(0);
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_date),
                getString(R.string.receipt_time), LINE_WIDTH, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(paymentDetails.getPay_date(), paymentDetails.getPay_timecreated(), LINE_WIDTH, 0)).append("\n\n");

        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_customer), paymentDetails.getCust_name(),
                LINE_WIDTH, 0));

        if (paymentDetails.getJob_id() != null && !paymentDetails.getJob_id().isEmpty())
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_order_id), paymentDetails.getJob_id(),
                    LINE_WIDTH, 0));
        else if (paymentDetails.getInv_id() != null && !paymentDetails.getInv_id().isEmpty()) // invoice
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_invoice_ref),
                    paymentDetails.getInv_id(), LINE_WIDTH, 0));

        if (!isStoredFwd)
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_idnum), payID, LINE_WIDTH,
                    0));

        if (!isCashPayment && !isCheckPayment) {
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_cardnum),
                    "*" + paymentDetails.getCcnum_last4(), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText("TransID:", paymentDetails.getPay_transid(), LINE_WIDTH, 0));
        } else if (isCheckPayment) {
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_checknum), paymentDetails.getPay_check(),
                    LINE_WIDTH, 0));
        }

        sb.append(textHandler.newLines(1));
        if (Global.isIvuLoto) {
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.paymentmethod_subtotal_amount),
                    Global.formatDoubleToCurrency(Global.subtotalAmount), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(paymentDetails.getTax1_name(),
                    Global.formatDoubleStrToCurrency(paymentDetails.getTax1_amount()), LINE_WIDTH, 0));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(paymentDetails.getTax2_name(),
                    Global.formatDoubleStrToCurrency(paymentDetails.getTax2_amount()), LINE_WIDTH, 0));
        }

        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                Global.formatDoubleStrToCurrency(paymentDetails.getOrd_total()), LINE_WIDTH, 0));
        sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_paid),
                Global.formatDoubleStrToCurrency(Global.amountPaid), LINE_WIDTH, 0));

        String change = paymentDetails.getChange();

        if (isCashPayment && isCheckPayment && !change.isEmpty() && change.contains(".")
                && Double.parseDouble(change) > 0)
            change = "";

        if (constantValue != null)
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(constantValue,
                    Global.formatDoubleStrToCurrency(change), LINE_WIDTH, 0));
        else if (Double.parseDouble(paymentDetails.getPay_dueamount()) > Double.parseDouble(paymentDetails.getOrd_total())) {
            double chg = Double.parseDouble(paymentDetails.getPay_dueamount()) - Double.parseDouble(paymentDetails.getOrd_total());
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.changeLbl),
                    Global.formatDoubleToCurrency(chg), LINE_WIDTH, 0));
        }

        printerApi.printData(sb.toString());

        sb.setLength(0);
        printerApi.printData(textHandler.newLines(1));


        if (!isCashPayment && !isCheckPayment) {
            if (myPref.getPreferences(MyPreferences.pref_handwritten_signature)) {
                sb.append(textHandler.newLines(1));
            } else if (!paymentDetails.getPay_signature().isEmpty()) {
                encodedSignature = paymentDetails.getPay_signature();
                try {
                    this.printImage(1);
                } catch (StarIOPortException e) {
                    e.printStackTrace();
                } catch (JAException e) {
                    e.printStackTrace();
                }
            }
            sb.append("x").append(textHandler.lines(LINE_WIDTH / 2)).append("\n");
            sb.append(getString(R.string.receipt_signature)).append(textHandler.newLines(1));
            printerApi.printData(sb.toString());
            sb.setLength(0);
        }

        if (Global.isIvuLoto && paymentDetails != null) {
            sb = new StringBuilder();
            // port.writePort(enableCenter, 0, enableCenter.length); //enable
            // center

            if (!printPref.contains(MyPreferences.print_ivuloto_qr)) {
                sb.append("\n");
                sb.append(textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH));
                sb.append(textHandler.centeredString("CONTROL: " + paymentDetails.getIvuLottoNumber(), LINE_WIDTH));
                sb.append(textHandler.centeredString(textHandler.ivuLines(2 * LINE_WIDTH / 3), LINE_WIDTH));
                sb.append("\n");

                printerApi.printData(sb.toString());
            } else {
                sb.append(textHandler.ivuLines(2 * LINE_WIDTH / 3)).append("\n");
                sb.append("\t").append("CONTROL: ").append(paymentDetails.getIvuLottoNumber()).append("\n");
                sb.append(textHandler.ivuLines(2 * LINE_WIDTH / 3)).append("\n");
                printerApi.printData(sb.toString());
            }
            sb.setLength(0);
        }

        this.printFooter();
        // port.writePort(enableCenter, 0, enableCenter.length); // center
        String temp;
        if (!isCashPayment && !isCheckPayment) {

            printerApi.printData(creditCardFooting);
            temp = textHandler.newLines(1);
            printerApi.printData(temp);
        }

        sb.setLength(0);
        if (isReprint) {
            sb.append(textHandler.centeredString("*** Copy ***", LINE_WIDTH));
            printerApi.printData(sb.toString());
        }
        printerApi.printData("\n\n\n");
        return true;
    }


    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        return printBalanceInquiry(values, LINE_WIDTH);
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
    public boolean printOpenInvoices(String invID) {
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
        try {

            PaymentsHandler paymentHandler = new PaymentsHandler(activity);
            PayMethodsHandler payMethodHandler = new PayMethodsHandler(activity);
            EMSPlainTextHelper textHandler = new EMSPlainTextHelper();
            StringBuilder sb = new StringBuilder();
            StringBuilder sb_refunds = new StringBuilder();
            printerApi.printData(textHandler.newLines(1));

            sb.append(textHandler.centeredString("REPORT", LINE_WIDTH));
            sb.append(textHandler.centeredString(Global.formatToDisplayDate(curDate, activity, 0), LINE_WIDTH));
            sb.append(textHandler.newLines(1));
            sb.append(textHandler.oneColumnLineWithLeftAlignedText(getString(R.string.receipt_pay_summary), LINE_WIDTH,
                    0));
            sb_refunds.append(textHandler.oneColumnLineWithLeftAlignedText(getString(R.string.receipt_refund_summmary),
                    LINE_WIDTH, 0));

            HashMap<String, String> paymentMap = paymentHandler
                    .getPaymentsRefundsForReportPrinting(Global.formatToDisplayDate(curDate, activity, 4), 0);
            HashMap<String, String> refundMap = paymentHandler
                    .getPaymentsRefundsForReportPrinting(Global.formatToDisplayDate(curDate, activity, 4), 1);
            List<String[]> payMethodsNames = payMethodHandler.getPayMethodsName();
            int size = payMethodsNames.size();
            double payGranTotal = 0.00;
            double refundGranTotal = 0.00;
            printerApi.printData(sb.toString());
            sb.setLength(0);

            for (int i = 0; i < size; i++) {
                if (paymentMap.containsKey(payMethodsNames.get(i)[0])) {
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                            Global.formatDoubleStrToCurrency(paymentMap.get(payMethodsNames.get(i)[0])), LINE_WIDTH,
                            3));

                    payGranTotal += Double.parseDouble(paymentMap.get(payMethodsNames.get(i)[0]));
                } else
                    sb.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                            Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 3));

                if (refundMap.containsKey(payMethodsNames.get(i)[0])) {
                    sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                            Global.formatDoubleStrToCurrency(refundMap.get(payMethodsNames.get(i)[0])), LINE_WIDTH, 3));
                    refundGranTotal += Double.parseDouble(refundMap.get(payMethodsNames.get(i)[0]));
                } else
                    sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(payMethodsNames.get(i)[1],
                            Global.formatDoubleToCurrency(0.00), LINE_WIDTH, 3));
            }

            sb.append(textHandler.newLines(1));
            sb.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                    Global.formatDoubleStrToCurrency(Double.toString(payGranTotal)), LINE_WIDTH, 4));
            sb.append(textHandler.newLines(1));

            sb_refunds.append(textHandler.newLines(1));
            sb_refunds.append(textHandler.twoColumnLineWithLeftAlignedText(getString(R.string.receipt_total),
                    Global.formatDoubleStrToCurrency(Double.toString(refundGranTotal)), LINE_WIDTH, 4));

            printerApi.printData(sb.toString());
            printerApi.printData(sb_refunds.toString());
            printerApi.printData(textHandler.newLines(1));
            // terminal.printData("".getBytes(FORMAT), "".length());

        } catch (Exception e) {

        }

        return true;
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
        cashDrawerApiContext.OpenCashDrawerA();
    }


    @Override
    public boolean isUSBConnected() {
        return false;
    }

    @Override
    public void toggleBarcodeReader() {

    }

//    @Override
//    public void printReceiptPreview(View view) {
//        try {
//            Bitmap bitmap = loadBitmapFromView(view);
//            super.printReceiptPreview(bitmap, LINE_WIDTH);
//        } catch (JAException e) {
//            e.printStackTrace();
//        } catch (StarIOPortException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void printReceiptPreview(SplitedOrder splitedOrder) {
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
    //
    // protected void printImage(int type) {
    // Bitmap myBitmap = null;
    // switch (type) {
    // case 0: // Logo
    // {
    // File imgFile = new File(myPref.getAccountLogoPath());
    // if (imgFile.exists()) {
    // myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
    // // int res = printerApi.downLoadImage(myBitmap);
    // // if (res == 0) {
    // // res = printerApi.printDownLoadImage(0);
    // // } else {
    // // }
    // }
    // break;
    // }
    // case 1: // signature
    // {
    // if (!encodedSignature.isEmpty()) {
    // byte[] img = Base64.decode(encodedSignature, Base64.DEFAULT);
    // myBitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
    // }
    // break;
    // }
    // case 2: {
    // // if (!encodedQRCode.isEmpty()) {
    // // byte[] img = Base64.decode(encodedQRCode, Base64.DEFAULT);
    // // myBitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
    // // }
    // break;
    // }
    // }
    //
    // if (myBitmap != null) {
    // printerApi.printImage(myBitmap, 0);
    // }
    // }

    // private double formatStrToDouble(String val) {
    // if (val == null || val.isEmpty())
    // return 0.00;
    // return Double.parseDouble(val);
    // }

    @Override
    public String printStationPrinter(List<Orders> orderProducts, String ordID, boolean cutPaper, boolean printHeader) {
        return "";
    }

    @Override
    public boolean printOnHold(Object onHold) {
        return false;
    }

    @Override
    public void printEndOfDayReport(String curDate, String clerk_id, boolean printDetails) {
//        printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
        //       printShiftDetailsReceipt(LINE_WIDTH, shiftID);
    }


    @Override
    public void printHeader() {
        super.printHeader(LINE_WIDTH);
    }

    @Override
    public void printFooter() {
        super.printFooter(LINE_WIDTH);
    }


}
