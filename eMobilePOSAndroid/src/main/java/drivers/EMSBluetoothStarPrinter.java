package drivers;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.android.dao.DeviceTableDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Receipt;
import com.android.emobilepos.models.SplittedOrder;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.realms.Device;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.ShiftExpense;
import com.android.emobilepos.print.ReceiptBuilder;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.crashlytics.android.Crashlytics;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import com.starmicronics.starioextension.ICommandBuilder;
import com.starmicronics.starioextension.IConnectionCallback;
import com.starmicronics.starioextension.StarIoExt;
import com.starmicronics.starioextension.StarIoExtManager;
import com.starmicronics.starioextension.StarIoExtManagerListener;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

import drivers.star.utils.PrinterFunctions;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

public class EMSBluetoothStarPrinter extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, IConnectionCallback {

    private int connectionRetries = 0;
    private boolean isNetworkPrinter = false;
    private int LINE_WIDTH = 32;
    private int PAPER_WIDTH;
    private int FONT_SIZE = 20;
    private String portSettings;
    private String portName;
    private String scannedData = "";
    private EMSCallBack callBack, scannerCallBack;
    private StarIoExtManager mStarIoExtManager;
    private Handler handler;
    private ProgressDialog myProgressDialog;
    private EMSBluetoothStarPrinter thisInstance;
    private boolean stopLoop = false;
    private String portNumber = "";
    private EMSDeviceManager edm;
    private CreditCardInfo cardManager;
    private Runnable doUpdateViews = new Runnable() {
        public void run() {
            try {
                if (callBack != null)
                    callBack.cardWasReadSuccessfully(true, cardManager);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };
    private Runnable doUpdateDidConnect = new Runnable() {
        public void run() {
            try {
                if (callBack != null)
                    callBack.readerConnectedSuccessfully(true);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };
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
    private StarIoExtManagerListener mStarIoExtManagerListener = new StarIoExtManagerListener() {
        @Override
        public void onBarcodeDataReceive(byte[] bytes) {
            String[] barcodeDataArray = new String(bytes).split("\r\n");
            for (String barcodeData : barcodeDataArray) {
                scannedData = barcodeData;
                handler.post(runnableScannedData);
            }
        }
    };

    @Override
    public void connect(Context activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        cardManager = new CreditCardInfo();
        this.isPOSPrinter = isPOSPrinter;
        this.edm = edm;
        thisInstance = this;
        LINE_WIDTH = paperSize;

        switch (LINE_WIDTH) {
            case 32:
                PAPER_WIDTH = 408;
                break;
            case 48:
                PAPER_WIDTH = 576;
                break;
            case 69:
                PAPER_WIDTH = 832;// 5400
                break;
        }

        portName = myPref.getPrinterMACAddress();
        portNumber = myPref.getStarPort();

        new processConnectionAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        boolean didConnect = false;

        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        cardManager = new CreditCardInfo();
        this.isPOSPrinter = isPOSPrinter;
        this.edm = edm;
        thisInstance = this;
        LINE_WIDTH = paperSize;

        switch (LINE_WIDTH) {
            case 32:
                PAPER_WIDTH = 420;
                break;
            case 48:
                PAPER_WIDTH = 1600;
                break;
            case 69:
                PAPER_WIDTH = 300;// 5400
                break;
        }

        if (_portName != null || _portNumber != null) {
            portName = _portName;
            portNumber = _portNumber;
        }

        try {

            if (!isPOSPrinter) {
                portSettings = "mini";
                port = getStarIOPort();
                enableCenter = new byte[]{0x1b, 0x61, 0x01};
                disableCenter = new byte[]{0x1b, 0x61, 0x00};
            } else {
                if (getPortName().contains("TCP") && portNumber != null && portNumber.equals("9100")) {
                    portSettings = portNumber;
                    isNetworkPrinter = true;
                } else {
                    portSettings = "";
                    isNetworkPrinter = false;
                }

                port = getStarIOPort();
                enableCenter = new byte[]{0x1b, 0x1d, 0x61, 0x01};
                disableCenter = new byte[]{0x1b, 0x1d, 0x61, 0x00};
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            StarPrinterStatus status = null;
            try {
                if (!isNetworkPrinter && port != null) {
                    status = port.retreiveStatus();
                }
            } catch (Exception e) {
                try {
                    StarIOPort.releasePort(port);
                    port = getStarIOPort();
                    Thread.sleep(1000);
                    StarIOPort.releasePort(port);
                    Thread.sleep(1000);
                    port = getStarIOPort();
                    Thread.sleep(1000);
                    status = port.retreiveStatus();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (port != null && (isNetworkPrinter || !status.offline)) {
                didConnect = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            releasePrinter();
        }
        if (didConnect) {
            this.edm.driverDidConnectToDevice(thisInstance, false, activity);
        } else {
            this.edm.driverDidNotConnectToDevice(thisInstance, null, false, activity);
        }

        return didConnect;
    }

    public String getPortName() {
        return portName;
    }

    public StarIOPort getPort() {
        return port;
    }

    @Override
    public void registerAll() {
        this.registerPrinter();
    }

    public void verifyConnectivity() throws StarIOPortException, InterruptedException {
        try {
            connectionRetries++;
            if (port == null)
                port = getStarIOPort();
        } catch (StarIOPortException e) {
            releasePrinter();
            Thread.sleep(500);
            if (connectionRetries <= 3) {
                verifyConnectivity();
            } else {
                throw e;
            }
            verifyConnectivity();
        }
    }

    private void printReceipt(Receipt receipt) {
        if (!myPref.isRasterModePrint()) {
            printNormalReceipt(receipt);
        } else {
            printRasterReceipt(receipt);
        }
    }

    private void printNormalReceipt(Receipt receipt) {
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
            builder.appendMultiple((receipt.getGrandTotal()).getBytes(encoding), 2, 2);
        if (receipt.getPaymentsDetails() != null)
            builder.append((receipt.getPaymentsDetails()).getBytes(encoding));
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
            port.writePort(commands, 0, commands.length);
            port.setEndCheckedBlockTimeoutMillis(30000);
            StarPrinterStatus status = port.endCheckedBlock();
            // todo: implement status
        } catch (StarIOPortException e) {
            e.printStackTrace();
        }
    }

    private void printRasterReceipt(Receipt receipt) {
        Bitmap bitmapFromText;
        ICommandBuilder builder = StarIoExt.createCommandBuilder(StarIoExt.Emulation.StarGraphic);
        builder.beginDocument();

        if (receipt.getMerchantLogo() != null) {
            int logoPosition = (PAPER_WIDTH / 3 - receipt.getMerchantLogo().getWidth()) / 2 + 30;
            builder.appendBitmapWithAbsolutePosition(receipt.getMerchantLogo(),
                    false, logoPosition);
        }

        if (receipt.getMerchantHeader() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getMerchantHeader(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getSpecialHeader() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getSpecialHeader(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getHeader() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getHeader(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getRemoteStationHeader() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getRemoteStationHeader(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getEmvDetails() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getEmvDetails(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getSeparator() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getSeparator(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        for (String s : receipt.getItems()) {
            if (s != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
                bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                        s, FONT_SIZE, PAPER_WIDTH, typeface);
                builder.appendBitmap(bitmapFromText, false);
            }
        }

        for (String s : receipt.getRemoteStationItems()) {
            if (s != null) {
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
                bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                        s, FONT_SIZE, PAPER_WIDTH, typeface);
                builder.appendBitmap(bitmapFromText, false);
            }
        }

        if (receipt.getSeparator() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getSeparator(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getTotals() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getTotals(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getTaxes() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getTaxes(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getTotalItems() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getTotalItems(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getGrandTotal() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getGrandTotal(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getPaymentsDetails() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getPaymentsDetails(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getYouSave() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getYouSave(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getIvuLoto() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getIvuLoto(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getLoyaltyDetails() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getLoyaltyDetails(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getRewardsDetails() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getRewardsDetails(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getSignatureImage() != null) {
            int logoPosition = (PAPER_WIDTH / 3 - receipt.getSignatureImage().getWidth()) / 2 + 30;
            builder.appendBitmapWithAbsolutePosition(receipt.getSignatureImage(),
                    false, logoPosition);
        }

        if (receipt.getSignature() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getSignature(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getMerchantFooter() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getMerchantFooter(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getSpecialFooter() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getSpecialFooter(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getTermsAndConditions() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getTermsAndConditions(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        if (receipt.getEnablerWebsite() != null) {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
            bitmapFromText = EMSBluetoothStarPrinter.createBitmapFromText(
                    receipt.getEnablerWebsite(), FONT_SIZE, PAPER_WIDTH, typeface);
            builder.appendBitmap(bitmapFromText, false);
        }

        builder.appendCutPaper(ICommandBuilder.CutPaperAction.PartialCutWithFeed);
        builder.endDocument();
        byte[] commands;
        commands = builder.getCommands();

        try {
            port.writePort(commands, 0, commands.length);
        } catch (StarIOPortException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold, EMVContainer emvContainer) {
        boolean result = false;
        try {
            setPaperWidth(LINE_WIDTH);
            verifyConnectivity();

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
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold, EMVContainer emvContainer) {
        boolean result = false;
        try {
            setPaperWidth(LINE_WIDTH);
            verifyConnectivity();

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
    public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory,
                                    boolean fromOnHold) {
        return printTransaction(ordID, type, isFromHistory, fromOnHold, null);
    }

    @Override
    public boolean printTransaction(Order order, Global.OrderType saleTypes, boolean isFromHistory,
                                    boolean fromOnHold) {
        return printTransaction(order, saleTypes, isFromHistory, fromOnHold, null);
    }

    static public Bitmap createBitmapFromText(String printText, int textSize, int printWidth,
                                              Typeface typeface) {
        Paint paint = new Paint();
        Bitmap bitmap;
        Canvas canvas;

        paint.setTextSize(textSize);
        paint.setTypeface(typeface);

        paint.getTextBounds(printText, 0, printText.length(), new Rect());

        TextPaint textPaint = new TextPaint(paint);
        android.text.StaticLayout staticLayout = new StaticLayout(printText, textPaint,
                printWidth, Layout.Alignment.ALIGN_NORMAL,
                1, 0, false);

        // Create bitmap
        bitmap = Bitmap.createBitmap(staticLayout.getWidth(), staticLayout.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Create canvas
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.translate(0, 0);
        staticLayout.draw(canvas);

        return bitmap;
    }

    @Override
    public boolean printPaymentDetails(String payID, int type, boolean isReprint,
                                       EMVContainer emvContainer) {
        try {
            setPaperWidth(LINE_WIDTH);
            verifyConnectivity();

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getPayment(
                    payID, type, isReprint, emvContainer);
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
            verifyConnectivity();

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getBalanceInquiry(values);
            printReceipt(receipt);
            printed = true;
//            printed = printBalanceInquiry(values, LINE_WIDTH);

            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return printed;
    }

    @Override
    public boolean printOnHold(Object onHold) {
        return true;
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
    public void printEndOfDayReport(String curDate, String clerk_id, boolean printDetails) {
        try {
            setPaperWidth(LINE_WIDTH);
            verifyConnectivity();
            printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);
            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean printReport(String curDate) {
        try {
            setPaperWidth(LINE_WIDTH);
            verifyConnectivity();
            printReportReceipt(curDate, LINE_WIDTH);
            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void registerPrinter() {
        edm.setCurrentDevice(this);
        String ip = "";
        if (portName.contains("TCP:")) {
            ip = portName.substring(portName.indexOf(':') + 1);
        }
        Device kitchenPrinter = DeviceTableDAO.getByIp(ip);
        if (kitchenPrinter == null) {
            Global.mainPrinterManager = edm;
        }
    }

    @Override
    public void unregisterPrinter() {
        edm.setCurrentDevice(null);
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        boolean printed = false;
        try {
            setPaperWidth(LINE_WIDTH);
            verifyConnectivity();

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getConsignment(myConsignment, encodedSig);
            printReceipt(receipt);
            printed = true;
//            printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);

            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return printed;
    }

    @Override
    public void releaseCardReader() {

    }

    @Override
    public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {

    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        try {
            setPaperWidth(LINE_WIDTH);
            verifyConnectivity();
            printConsignmentPickupReceipt(myConsignment, encodedSig, LINE_WIDTH);
            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        try {
            setPaperWidth(LINE_WIDTH);
            verifyConnectivity();
            printOpenInvoicesReceipt(invID, LINE_WIDTH);
            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean printRemoteStation(List<Orders> orders, String ordID) {
        boolean result = false;
        try {
            setPaperWidth(LINE_WIDTH);
            verifyConnectivity();

            ReceiptBuilder receiptBuilder = new ReceiptBuilder(activity, LINE_WIDTH);
            Receipt receipt = receiptBuilder.getRemoteStation(orders, ordID);
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

    public void print(String str, int size, PrinterFunctions.Alignment alignment) {
        super.print(str, FORMAT, size, alignment);
    }

    public void print(String str) {
        super.print(str);
    }

    public void print(String str, String FORMAT) {
        super.print(str, FORMAT);
    }

    @Override
    public void cutPaper() {
        try {
            verifyConnectivity();
            super.cutPaper();
            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openCashDrawer() {
        try {
            verifyConnectivity();
            try {
                byte[] data = PrinterFunctions.createCommandsOpenCashDrawer();
                port.writePort(data, 0, data.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            releasePrinter();
        }
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        try {
            setPaperWidth(LINE_WIDTH);
            verifyConnectivity();
            printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);
            releasePrinter();
        } catch (StarIOPortException e) {
            return false;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void printShiftDetailsReport(String shiftID) {
        try {
            setPaperWidth(LINE_WIDTH);
            verifyConnectivity();
            printShiftDetailsReceipt(LINE_WIDTH, shiftID);
            releasePrinter();
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
    }

    @Override
    public void loadScanner(EMSCallBack callBack) {
        if (myPref.getPrinterName().toUpperCase().contains("MPOP")) {
            scannerCallBack = callBack;
            if (handler == null)
                handler = new Handler();
            if (callBack != null) {
                mStarIoExtManager = new StarIoExtManager(StarIoExtManager.Type.OnlyBarcodeReader, getPortName(), "", 10000,
                        this.activity);
                mStarIoExtManager.setListener(mStarIoExtManagerListener);
                starIoExtManagerConnect();
            } else {
                if (mStarIoExtManager != null) {
                    mStarIoExtManager.disconnect(this);
                    mStarIoExtManager = null;
                }
            }
        } else if (callBack != null && Build.MODEL.toUpperCase().contains("ELO")) {
            // Do nothing, let the scanner do its thing.
            // ELO scanner is ready when the hardware is turned on.
        }
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
            verifyConnectivity();

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
        StarPrinterStatus status = null;
        try {
            if (port == null) {
                return false;
            }
            status = port.retreiveStatus();
        } catch (StarIOPortException e) {
            try {
                StarIOPort.releasePort(port);
                port = getStarIOPort();
                Thread.sleep(1000);
                StarIOPort.releasePort(port);
                Thread.sleep(1000);
                port = getStarIOPort();
                Thread.sleep(1000);
                if (port != null) {
                    status = port.retreiveStatus();
                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                return false;
            } catch (StarIOPortException e1) {
                e1.printStackTrace();
                return false;
            } catch (Exception ex) {
                Crashlytics.logException(ex);
                return false;
            }
        } catch (Exception ex) {
            Crashlytics.logException(ex);
            return false;
        }
        return status != null && !status.offline;
    }

    @Override
    public void printClockInOut(List<ClockInOut> timeClocks, String clerkID) {
        try {
            setPaperWidth(LINE_WIDTH);
            verifyConnectivity();
            printClockInOut(timeClocks, LINE_WIDTH, clerkID);
            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printExpenseReceipt(ShiftExpense expense) {
        try {
            setPaperWidth(LINE_WIDTH);
            verifyConnectivity();
            printExpenseReceipt(LINE_WIDTH, expense);
            releasePrinter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void starIoExtManagerConnect() {
        final Dialog mProgressDialog = new ProgressDialog(EMSBluetoothStarPrinter.this.activity);
        AsyncTask<Void, Void, Boolean> asyncTask = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                if (!((Activity) EMSBluetoothStarPrinter.this.activity).isFinishing())
                    mProgressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                mStarIoExtManager.disconnect(EMSBluetoothStarPrinter.this);
                mStarIoExtManager.connect(EMSBluetoothStarPrinter.this);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!((Activity) EMSBluetoothStarPrinter.this.activity).isFinishing()) {
                    mProgressDialog.dismiss();
                }
            }
        };

        asyncTask.execute();
    }

    @Override
    public void printFooter() {
        super.printFooter(LINE_WIDTH);
    }

    @Override
    public void printHeader() {
        super.printHeader(LINE_WIDTH);
    }

    private StarIOPort getStarIOPort() throws StarIOPortException {
        releasePrinter();
        if (port == null || port.retreiveStatus() == null || port.retreiveStatus().offline) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        port = StarIOPort.getPort(getPortName(), portSettings, 30000, activity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        synchronized (portSettings) {
                            portSettings.notify();
                        }
                    }
                }
            }).start();
            synchronized (portSettings) {
                try {
                    portSettings.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return port;
    }

    @Override
    public void onConnected(ConnectResult connectResult) {

    }

    @Override
    public void onDisconnected() {

    }

    public class processConnectionAsync extends AsyncTask<Integer, String, String> {

        String msg = "";
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
            try {

                if (!isPOSPrinter) {
                    portSettings = "mini";
                    port = getStarIOPort();
                    enableCenter = new byte[]{0x1b, 0x61, 0x01};
                    disableCenter = new byte[]{0x1b, 0x61, 0x00};
                } else {
                    if (getPortName().contains("TCP") && portNumber.equals("9100"))
                        portSettings = portNumber;
                    else
                        portSettings = "";

                    port = getStarIOPort();
                    enableCenter = new byte[]{0x1b, 0x1d, 0x61, 0x01};
                    disableCenter = new byte[]{0x1b, 0x1d, 0x61, 0x00};
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (port != null) {
                    StarPrinterStatus status = null;
                    try {
                        status = port.retreiveStatus();
                    } catch (Exception e) {
                        try {
                            StarIOPort.releasePort(port);
                            port = getStarIOPort();
                            Thread.sleep(1000);
                            StarIOPort.releasePort(port);
                            Thread.sleep(1000);
                            port = getStarIOPort();
                            Thread.sleep(1000);
                            status = port.retreiveStatus();
                        } catch (Exception e1) {
                            Crashlytics.logException(e1);
                        }
                    }
                    if (!status.offline) {
                        didConnect = true;

                    } else {
                        msg = "Printer is offline";
                        if (status.receiptPaperEmpty) {
                            msg += "\nPaper is Empty";
                        }
                        if (status.coverOpen) {
                            msg += "\nCover is Open";
                        }
                    }
                } else {
                    msg = "Printer is offline";
                }
            } catch (Exception e) {
                msg = "Failed: \n" + e.getMessage();
            } finally {
                releasePrinter();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            boolean isDestroyed = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (((Activity) activity).isDestroyed()) {
                    isDestroyed = true;
                }
            }
            if (!((Activity) activity).isFinishing() && !isDestroyed && myProgressDialog.isShowing()) {
                myProgressDialog.dismiss();
            }

            if (didConnect) {
                edm.driverDidConnectToDevice(thisInstance, true, activity);
            } else {

                edm.driverDidNotConnectToDevice(thisInstance, msg, true, activity);
            }

        }
    }
}
