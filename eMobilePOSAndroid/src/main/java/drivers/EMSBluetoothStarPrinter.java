package drivers;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import com.StarMicronics.jasura.JAException;
import com.android.dao.DeviceTableDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.ClockInOut;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.SplitedOrder;
import com.android.emobilepos.models.TimeClock;
import com.android.emobilepos.models.realms.Device;
import com.android.emobilepos.models.realms.Payment;
import com.android.support.CardParser;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.crashlytics.android.Crashlytics;
import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManager;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManagerListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import drivers.star.utils.Communication;
import drivers.star.utils.PrinterFunctions;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import main.EMSDeviceManager;

public class EMSBluetoothStarPrinter extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    private int LINE_WIDTH = 32;
    private int PAPER_WIDTH;
    private String portSettings;
    private String portName;
    private String scannedData = "";
    private EMSCallBack callBack, scannerCallBack;
    private StarIoExtManager mStarIoExtManager;
    private Handler handler;
    private ProgressDialog myProgressDialog;
    private EMSDeviceDriver thisInstance;
    private boolean stopLoop = false;
    int connectionRetries = 0;
    private String portNumber = "";
    private EMSDeviceManager edm;
    private CreditCardInfo cardManager;
    boolean isNetworkPrinter = false;

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
        }
        if (didConnect) {
            this.edm.driverDidConnectToDevice(thisInstance, false);
        } else {
            this.edm.driverDidNotConnectToDevice(thisInstance, null, false);
        }

        return didConnect;
    }

    public String getPortName() {
        return portName;
    }

    public StarIOPort getPort() {
        return port;
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
            }

            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            boolean isDestroyed = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (((Activity)activity).isDestroyed()) {
                    isDestroyed = true;
                }
            }
            if (!((Activity)activity).isFinishing() && !isDestroyed && myProgressDialog.isShowing()) {
                myProgressDialog.dismiss();
            }

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


    private void verifyConnectivity() throws StarIOPortException, InterruptedException {
        try {
            connectionRetries++;
            if (port == null || port.retreiveStatus() == null && port.retreiveStatus().offline)
                port = getStarIOPort();
        } catch (StarIOPortException e) {
            releasePrinter();
            Thread.sleep(500);
            port = null;
            if (connectionRetries <= 3) {
                verifyConnectivity();
            } else {
                throw e;
            }
            verifyConnectivity();
        }
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        try {
            setPaperWidth(LINE_WIDTH);
            setStartIOPort();
            if (port == null) {
                verifyConnectivity();
            }
            Thread.sleep(1000);
            printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);
            releasePrinter();
        } catch (StarIOPortException e) {
            return false;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

//    private void releasePort() {
//        try {
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            switch (Build.VERSION.SDK_INT) {
//                case Build.VERSION_CODES.LOLLIPOP:
//                case Build.VERSION_CODES.LOLLIPOP_MR1:
//                case Build.VERSION_CODES.M:
//                    StarIOPort.releasePort(port);
//                    break;
//            }
//        } catch (StarIOPortException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory, boolean fromOnHold) {
        setPaperWidth(LINE_WIDTH);
        setStartIOPort();
        boolean printTransaction = printTransaction(ordID, type, isFromHistory, fromOnHold, null);
        releasePrinter();
        return printTransaction;
    }

    @Override
    public boolean printPaymentDetails(String payID, int type, boolean isReprint, EMVContainer emvContainer) {
        setPaperWidth(LINE_WIDTH);
        setStartIOPort();
        if (port != null) {
            printPaymentDetailsReceipt(payID, type, isReprint, LINE_WIDTH, emvContainer);
        }
        releasePrinter();
        return true;
    }

    private void setStartIOPort() {
        try {
            port = getStarIOPort();
        } catch (StarIOPortException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean printBalanceInquiry(HashMap<String, String> values) {
        setStartIOPort();
        boolean print = printBalanceInquiry(values, LINE_WIDTH);
        releasePrinter();
        return print;
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
    public void printEndOfDayReport(String curDate, String clerk_id, boolean printDetails) {
        setStartIOPort();
        printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);
        releasePrinter();
    }

    @Override
    public boolean printReport(String curDate) {
        try {
            setStartIOPort();
            verifyConnectivity();
            Thread.sleep(1000);
            printReportReceipt(curDate, LINE_WIDTH);
            releasePrinter();
        } catch (StarIOPortException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
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
        try {
            setStartIOPort();
            verifyConnectivity();
            Thread.sleep(1000);
            printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);
            releasePrinter();
        } catch (StarIOPortException e) {
            return false;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void releaseCardReader() {
        if (!isPOSPrinter) {
            callBack = null;
            try {
                if (port != null) {
                    port.writePort(new byte[]{0x04}, 0, 1);
                    stopLoop = true;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (StarIOPortException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {
        callBack = _callBack;
        if (handler == null)
            handler = new Handler();
        if (!isPOSPrinter) {
            StartCardReaderThread temp = new StartCardReaderThread();
            temp.start();
        }
    }

    private class StartCardReaderThread extends Thread {
        public void run() {
            try {
                if (port == null) {
                    port = getStarIOPort();
                }
                stopLoop = false;
                ReceiveThread receiveThread = new ReceiveThread();
                receiveThread.start();
                port.writePort(new byte[]{0x1b, 0x4d, 0x45}, 0, 3);
                handler.post(doUpdateDidConnect);
            } catch (StarIOPortException e) {
                e.printStackTrace();
            }
        }
    }

    class ReceiveThread extends Thread {
        public void run() {
            byte[] mcrData1 = new byte[1];
            try {
                StringBuilder tr1 = new StringBuilder();
                StringBuilder tr2 = new StringBuilder();
                List<String> listTrack = new ArrayList<>();
                String t;
                boolean doneParsing = false;
                while (!stopLoop) {
                    if (port.readPort(mcrData1, 0, 1) > 0) {
                        if (!doneParsing) {
                            t = new String(mcrData1, "windows-1252");
                            if (t.equals("\r") || t.equals("\n")) {
                                for (String data : listTrack) {
                                    if (data.contains("B")) {
                                        if (!data.startsWith("%"))
                                            tr1.append("%");
                                        tr1.append(data);

                                        if (!data.endsWith("?"))
                                            tr1.append("?");
                                    } else if (data.contains("=")) {
                                        if (!data.startsWith(";"))
                                            tr1.append(";");

                                        tr1.append(data);

                                        if (!data.endsWith("?"))
                                            tr1.append("?");
                                    }

                                }
                                cardManager = new CreditCardInfo();
                                CardParser.parseCreditCard(activity, tr1.toString(), cardManager);
                                doneParsing = true;
                                handler.post(doUpdateViews);
                                tr1.setLength(0);

                            } else if (mcrData1[0] == 28 && tr2.length() > 0) {

                                listTrack.add(tr2.toString());
                                tr2.setLength(0);
                            } else {
                                tr2.append(t.trim());
                            }
                        }
                    }
                }
            } catch (StarIOPortException ignored) {
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

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

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        try {
            setStartIOPort();
            verifyConnectivity();
            Thread.sleep(1000);
            printConsignmentPickupReceipt(myConsignment, encodedSig, LINE_WIDTH);
            releasePrinter();
        } catch (StarIOPortException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        try {
            setStartIOPort();
            verifyConnectivity();
            Thread.sleep(1000);
            printOpenInvoicesReceipt(invID, LINE_WIDTH);
            releasePrinter();
        } catch (StarIOPortException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    @Override
    public String printStationPrinter(List<Orders> orders, String ordID, boolean cutPaper,
                                      boolean printHeader) {
        String receipt;
        setStartIOPort();
        receipt = printStationPrinterReceipt(orders, ordID, 42, cutPaper, printHeader);
        releasePrinter();
        return receipt;
    }

    public void print(String str, boolean isLargeFont) {
        setStartIOPort();
        super.print(str, FORMAT, isLargeFont);
        super.cutPaper();
        releasePrinter();
    }

    @Override
    public void openCashDrawer() {
        byte[] data;
        data = PrinterFunctions.createCommandsOpenCashDrawer();
        Communication.Result result;
        try {
            result = Communication.sendCommands(data, port, this.activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        try {
            setStartIOPort();
            verifyConnectivity();
            Thread.sleep(1000);
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
            setStartIOPort();
            verifyConnectivity();
            Thread.sleep(1000);
            printShiftDetailsReceipt(LINE_WIDTH, shiftID);
            releasePrinter();
        } catch (StarIOPortException e) {
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void loadScanner(EMSCallBack _callBack) {
        if (myPref.getPrinterName().toUpperCase().contains("MPOP")) {
            scannerCallBack = _callBack;
            if (handler == null)
                handler = new Handler();
            if (_callBack != null) {
                mStarIoExtManager = new StarIoExtManager(StarIoExtManager.Type.OnlyBarcodeReader, getPortName(), "", 10000,
                        this.activity);
                mStarIoExtManager.setListener(mStarIoExtManagerListener);
                starIoExtManagerConnect();
            } else {
                if (mStarIoExtManager != null) {
                    mStarIoExtManager.disconnect();
                    mStarIoExtManager = null;
                }
            }
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
    public void printReceiptPreview(SplitedOrder splitedOrder) {
        try {
            setPaperWidth(LINE_WIDTH);
            setStartIOPort();
            super.printReceiptPreview(splitedOrder, LINE_WIDTH);
            releasePrinter();
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
        }
        return status != null && !status.offline;
    }

    @Override
    public void printClockInOut(List<ClockInOut> timeClocks, String clerkID) {
        super.printClockInOut(timeClocks, LINE_WIDTH, clerkID);
    }

    private void starIoExtManagerConnect() {
        final Dialog mProgressDialog = new ProgressDialog(EMSBluetoothStarPrinter.this.activity);
        AsyncTask<Void, Void, Boolean> asyncTask = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                if (!((Activity)EMSBluetoothStarPrinter.this.activity).isFinishing())
                    mProgressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                mStarIoExtManager.disconnect();
                return mStarIoExtManager.connect();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!((Activity)EMSBluetoothStarPrinter.this.activity).isFinishing()) {
                    mProgressDialog.dismiss();
                }
            }
        };

        asyncTask.execute();
    }

    StarIoExtManagerListener mStarIoExtManagerListener = new StarIoExtManagerListener() {
        @Override
        public void didBarcodeDataReceive(byte[] data) {
            String[] barcodeDataArray = new String(data).split("\r\n");
            for (String barcodeData : barcodeDataArray) {
                scannedData = barcodeData;
                handler.post(runnableScannedData);
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

    @Override
    public void printFooter() {

        super.printFooter(LINE_WIDTH);
    }

    @Override
    public void printHeader() {
        super.printHeader(LINE_WIDTH);
    }

    private StarIOPort getStarIOPort() throws StarIOPortException {
//        if (!getPortName().toUpperCase().contains("TCP")) {
        releasePrinter();
        port = null;
//        }
        if (port == null || port.retreiveStatus() == null || port.retreiveStatus().offline) {
//            if (getPortName().toUpperCase().contains("TCP")) {
//                String ip = getPortName().replace("TCP:", "");
//                int port = 80;
//                try {
//                    port = TextUtils.isEmpty(portSettings) ? 80 : Integer.parseInt(portSettings);
//                } catch (NumberFormatException e) {
//                    e.printStackTrace();
//                }
////                if (!Global.isIpAvailable(ip, port)) {
////                    throw new StarIOPortException("Host not reachable.");
////                }
//            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        port = StarIOPort.getPort(getPortName(), portSettings, 30000, activity);
                    } catch (StarIOPortException e) {
                        e.printStackTrace();
                        try {
                            port = StarIOPort.getPort(getPortName(), portSettings, 30000, activity);
                        } catch (StarIOPortException e1) {
                            e1.printStackTrace();
                        }
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return port;
    }
}
