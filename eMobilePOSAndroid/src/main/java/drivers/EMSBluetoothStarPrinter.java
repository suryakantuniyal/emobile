package drivers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.android.emobilepos.R;
import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.emobilepos.models.PaymentDetails;
import com.android.support.CardParser;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManager;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManagerListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;

import drivers.star.utils.Communication;
import drivers.star.utils.PrinterFunctions;
import drivers.star.utils.PrinterSetting;
import main.EMSDeviceManager;
import protocols.EMSCallBack;
import protocols.EMSDeviceManagerPrinterDelegate;
import util.RasterDocument;
import util.RasterDocument.RasPageEndMode;
import util.RasterDocument.RasSpeed;
import util.RasterDocument.RasTopMargin;
import util.StarBitmap;

public class EMSBluetoothStarPrinter extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate {

    private int LINE_WIDTH = 32;
    private int PAPER_WIDTH;
    private String portSettings, portName;

    private StarIOPort portForCardReader;
    private byte[] outputByteBuffer = null;
    private EMSCallBack callBack, scannerCallBack;
    private StarIoExtManager mStarIoExtManager;
    private ReceiveThread receiveThread;
    private Handler handler;// = new Handler();
    private ProgressDialog myProgressDialog;
    private EMSDeviceDriver thisInstance;
    private boolean stopLoop = false;

    private String portNumber = "";
    private EMSDeviceManager edm;
    private CreditCardInfo cardManager;
    private Encrypt encrypt;

    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);

        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);
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

        portName = myPref.printerMACAddress(true, null);
        portNumber = myPref.getStarPort();

        new processConnectionAsync().execute(0);
    }

    @Override
    public boolean autoConnect(Activity activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        boolean didConnect = false;
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        cardManager = new CreditCardInfo();
        encrypt = new Encrypt(activity);
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
                if (portName.contains("TCP") && !portNumber.equals("9100"))
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

            if ((port != null) && (!port.retreiveStatus().offline)) {
                didConnect = true;

            }

            if (didConnect) {
                this.edm.driverDidConnectToDevice(thisInstance, false);
            } else {

                this.edm.driverDidNotConnectToDevice(thisInstance, null, false);
            }

        } catch (StarIOPortException e) {
        } finally {

        }

        return didConnect;
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
            // TODO Auto-generated method stub

            try {

                if (!isPOSPrinter) {
                    portSettings = "mini";
                    port = getStarIOPort();
                    enableCenter = new byte[]{0x1b, 0x61, 0x01};
                    disableCenter = new byte[]{0x1b, 0x61, 0x00};
                } else {
                    if (portName.contains("TCP") && !portNumber.equals("9100"))
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

                StarPrinterStatus status = port.retreiveStatus();

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

            } catch (StarIOPortException e) {
                msg = "Failed: \n" + e.getMessage();
            } finally {

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

    int connectionRetries = 0;

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

            verifyConnectivity();

            Thread.sleep(1000);

            if (!isPOSPrinter) {
                port.writePort(new byte[]{0x1d, 0x57, (byte) 0x80, 0x31}, 0, 4);
                port.writePort(new byte[]{0x1d, 0x21, 0x00}, 0, 3);
                port.writePort(new byte[]{0x1b, 0x74, 0x11}, 0, 3); // set to
                // windows-1252
            }
            printReceipt(ordID, LINE_WIDTH, fromOnHold, saleTypes, isFromHistory, emvContainer);

        } catch (StarIOPortException e) {
            return false;

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory, boolean fromOnHold) {
        return printTransaction(ordID, type, isFromHistory, fromOnHold, null);
    }

    @Override
    public boolean printPaymentDetails(String payID, int type, boolean isReprint, EMVContainer emvContainer) {
        try {

            verifyConnectivity();

            Thread.sleep(1000);

            if (!isPOSPrinter) {
                port.writePort(new byte[]{0x1d, 0x57, (byte) 0x80, 0x31}, 0, 4);
                port.writePort(new byte[]{0x1d, 0x21, 0x00}, 0, 3);
                port.writePort(new byte[]{0x1b, 0x74, 0x11}, 0, 3); // set to
                // windows-1252
            }

            printPaymentDetailsReceipt(payID, type, isReprint, LINE_WIDTH, emvContainer);

        } catch (StarIOPortException e) {
            return false;

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }
        return true;
    }



    public void PrintBitmapImage(Bitmap tempBitmap, boolean compressionEnable) throws StarIOPortException {
        ArrayList<Byte> commands = new ArrayList<Byte>();
        Byte[] tempList;

        RasterDocument rasterDoc = new RasterDocument(RasSpeed.Medium, RasPageEndMode.None, RasPageEndMode.None,
                RasTopMargin.Standard, 0, LINE_WIDTH / 3, 0);
        // Bitmap bm = BitmapFactory.decodeResource(res, source);
        StarBitmap starbitmap = new StarBitmap(tempBitmap, false, 350, PAPER_WIDTH);

        byte[] command = rasterDoc.BeginDocumentCommandData();
        tempList = new Byte[command.length];
        CopyArray(command, tempList);
        commands.addAll(Arrays.asList(tempList));

        command = starbitmap.getImageRasterDataForPrinting();
        tempList = new Byte[command.length];
        CopyArray(command, tempList);
        commands.addAll(Arrays.asList(tempList));

        command = rasterDoc.EndDocumentCommandData();
        tempList = new Byte[command.length];
        CopyArray(command, tempList);
        commands.addAll(Arrays.asList(tempList));

        byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(commands);
        port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);
    }

    private void CopyArray(byte[] srcArray, Byte[] cpyArray) {
        for (int index = 0; index < cpyArray.length; index++) {
            cpyArray[index] = srcArray[index];
        }
    }

    private static byte[] convertFromListByteArrayTobyteArray(List<Byte> ByteArray) {
        byte[] byteArray = new byte[ByteArray.size()];
        for (int index = 0; index < byteArray.length; index++) {
            byteArray[index] = ByteArray.get(index);
        }

        return byteArray;
    }

    @Override
    public boolean printOnHold(Object onHold) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void setBitmap(Bitmap bmp) {
        // TODO Auto-generated method stub
    }

    @Override
    public void playSound() {

    }

    @Override
    public void printEndOfDayReport(String curDate, String clerk_id, boolean printDetails) {
        printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);
    }

    @Override
    public boolean printReport(String curDate) {
        // TODO Auto-generated method stub

        try {
            // port = StarIOPort.getPort(portName, portSettings, 10000,
            // this.activity);
            verifyConnectivity();

            Thread.sleep(1000);
            if (!isPOSPrinter) {
                port.writePort(new byte[]{0x1d, 0x57, (byte) 0x80, 0x31}, 0, 4);
                port.writePort(new byte[]{0x1d, 0x21, 0x00}, 0, 3);
                port.writePort(new byte[]{0x1b, 0x74, 0x11}, 0, 3); // set to
                // windows-1252
            }

            printReportReceipt(curDate, LINE_WIDTH);

        } catch (StarIOPortException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
            // TODO Auto-generated catch block
        } finally {

        }
        return true;
    }

    @Override
    public void registerPrinter() {
        // TODO Auto-generated method stub
        edm.currentDevice = this;

    }

    @Override
    public void unregisterPrinter() {
        // TODO Auto-generated method stub
        edm.currentDevice = null;
    }

    @Override
    public boolean printConsignment(List<ConsignmentTransaction> myConsignment, String encodedSig) {
        // TODO Auto-generated method stub
        try {

            verifyConnectivity();

            Thread.sleep(1000);

            if (!isPOSPrinter) {
                port.writePort(new byte[]{0x1d, 0x57, (byte) 0x80, 0x31}, 0, 4);
                port.writePort(new byte[]{0x1d, 0x21, 0x00}, 0, 3);
                port.writePort(new byte[]{0x1b, 0x74, 0x11}, 0, 3); // set to
                // windows-1252
            }

            printConsignmentReceipt(myConsignment, encodedSig, LINE_WIDTH);

        } catch (StarIOPortException e) {
            return false;

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }
        return true;
    }

    @Override
    public void releaseCardReader() {
        // TODO Auto-generated method stub
        if (!isPOSPrinter) {
            callBack = null;
            try {
                if (portForCardReader != null) {
                    portForCardReader.writePort(new byte[]{0x04}, 0, 1);
                    stopLoop = true;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (StarIOPortException e) {
                e.printStackTrace();

            } finally {
                if (portForCardReader != null) try {
                    StarIOPort.releasePort(portForCardReader);
                    portForCardReader = null;
                } catch (StarIOPortException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {
        // TODO Auto-generated method stub

        callBack = _callBack;
        if (handler == null)
            handler = new Handler();
        if (!isPOSPrinter) {
            StartCardReaderThread temp = new StartCardReaderThread();
            temp.start();
        }
    }

    class StartCardReaderThread extends Thread {
        public void run() {

            try {
                if (portForCardReader == null) {
                    stopLoop = false;
                    portForCardReader = getStarIOPort();

                    receiveThread = new ReceiveThread();
                    receiveThread.start();
                    portForCardReader.writePort(new byte[]{0x1b, 0x4d, 0x45}, 0, 3);
                    handler.post(doUpdateDidConnect);
                }

            } catch (StarIOPortException e) {
            }
        }
    }

    class ReceiveThread extends Thread {
        public void run() {

            byte[] mcrData1 = new byte[1];
            int track = 1;
            try {

                StringBuilder tr1 = new StringBuilder();
                StringBuilder tr2 = new StringBuilder();
                List<String> listTrack = new ArrayList<String>();
                String t = "";
                boolean doneParsing = false;
                int countNameLimiter = 0;

                while (!stopLoop) {

                    if (portForCardReader.readPort(mcrData1, 0, 1) > 0) {

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
                                        // tr1.append("%").append(data).append("?");
                                    } else if (data.contains("=")) {
                                        if (!data.startsWith(";"))
                                            tr1.append(";");

                                        tr1.append(data);

                                        if (!data.endsWith("?"))
                                            tr1.append("?");
                                        // tr1.append(";").append(data).append("?");
                                    }

                                }
                                // tr2.append("%").append(tr1.toString()).append("?");
                                cardManager = new CreditCardInfo();
                                CardParser.parseCreditCard(activity, tr1.toString(), cardManager);
                                doneParsing = true;
                                handler.post(doUpdateViews);
                                tr1.setLength(0);

                            } else if (mcrData1 != null && mcrData1[0] == 28 && tr2.length() > 0) {

                                listTrack.add(tr2.toString());
                                tr2.setLength(0);
                            } else {
                                tr2.append(t.trim());
                            }

                        }

                    }
                }
            } catch (StarIOPortException e) {
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
            }
        }
    }

    // displays data from card swiping
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
        // TODO Auto-generated method stub
        try {
            // port = StarIOPort.getPort(portName, portSettings, 10000,
            // this.activity);
            verifyConnectivity();

            Thread.sleep(1000);
            if (!isPOSPrinter) {
                port.writePort(new byte[]{0x1d, 0x57, (byte) 0x80, 0x31}, 0, 4);
                port.writePort(new byte[]{0x1d, 0x21, 0x00}, 0, 3);
                port.writePort(new byte[]{0x1b, 0x74, 0x11}, 0, 3); // set to
                // windows-1252
            }

            printConsignmentPickupReceipt(myConsignment, encodedSig, LINE_WIDTH);

        } catch (StarIOPortException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
            // TODO Auto-generated catch block
        } finally {
        }
        return true;
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        // TODO Auto-generated method stub
        try {
            verifyConnectivity();

            Thread.sleep(1000);

            if (!isPOSPrinter) {
                port.writePort(new byte[]{0x1d, 0x57, (byte) 0x80, 0x31}, 0, 4);
                port.writePort(new byte[]{0x1d, 0x21, 0x00}, 0, 3);
                port.writePort(new byte[]{0x1b, 0x74, 0x11}, 0, 3); // set to
                // windows-1252
            }

            printOpenInvoicesReceipt(invID, LINE_WIDTH);

        } catch (StarIOPortException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
            // TODO Auto-generated catch block
        } finally {
        }
        return true;
    }

    @Override
    public void printStationPrinter(List<Orders> orders, String ordID) {
        // TODO Auto-generated method stub
        try {
            port = getStarIOPort();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            if (!isPOSPrinter) {
                port.writePort(new byte[]{0x1d, 0x57, (byte) 0x80, 0x31}, 0, 4);
                port.writePort(new byte[]{0x1d, 0x21, 0x00}, 0, 3);
                port.writePort(new byte[]{0x1b, 0x74, 0x11}, 0, 3); // set to
                // windows-1252
            } else {
                port.writePort(new byte[]{0x1b, 0x1d, 0x74, 0x20}, 0, 4);
                byte[] characterExpansion = new byte[]{0x1b, 0x69, 0x00, 0x00};
                characterExpansion[2] = (byte) (1 + '0');
                characterExpansion[3] = (byte) (1 + '0');

                port.writePort(characterExpansion, 0, characterExpansion.length);
                port.writePort(disableCenter, 0, disableCenter.length); // disable
                // center
            }

            printStationPrinterReceipt(orders, ordID, LINE_WIDTH);

            // db.close();
        } catch (StarIOPortException e) {

        } finally {

        }
    }

    @Override
    public void openCashDrawer() {
        String printerName;
        byte[] data;
        printerName = myPref.getPrinterName();

//	     releasePrinter();
        data = PrinterFunctions.createCommandsOpenCashDrawer();

        PrinterSetting setting = new PrinterSetting(this.activity);

        Communication.Result result;

        if (printerName.toUpperCase().contains("MPOP")) {
        try {
            result = Communication.sendCommands(data, getStarIOPort(), this.activity);
        } catch (StarIOPortException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   // 10000mS!!!
    }
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        // TODO Auto-generated method stub
        try {
            // port = StarIOPort.getPort(portName, portSettings, 10000,
            // this.activity);
            verifyConnectivity();

            Thread.sleep(1000);
            if (!isPOSPrinter) {
                port.writePort(new byte[]{0x1d, 0x57, (byte) 0x80, 0x31}, 0, 4);
                port.writePort(new byte[]{0x1d, 0x21, 0x00}, 0, 3);
                port.writePort(new byte[]{0x1b, 0x74, 0x11}, 0, 3); // set to
                // windows-1252
            }

            printConsignmentHistoryReceipt(map, c, isPickup, LINE_WIDTH);

        } catch (StarIOPortException e) {
            return false;

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

        }
        return true;
    }

    @Override
    public void loadScanner(EMSCallBack _callBack) {
        scannerCallBack = _callBack;
        if (handler == null)
            handler = new Handler();
        if (_callBack != null) {
            mStarIoExtManager = new StarIoExtManager(StarIoExtManager.Type.OnlyBarcodeReader, portName, "", 10000,
                    this.activity); // 10000mS!!!
            mStarIoExtManager.setListener(mStarIoExtManagerListener);
            // mStarIoExtManager.disconnect();
            // mStarIoExtManager.connect();
            starIoExtManagerConnect();
        } else {
            if (mStarIoExtManager != null) {
                mStarIoExtManager.disconnect();
                mStarIoExtManager = null;
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

    private void starIoExtManagerConnect() {
        final Dialog mProgressDialog = new ProgressDialog(EMSBluetoothStarPrinter.this.activity);
        AsyncTask<Void, Void, Boolean> asyncTask = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                if (!EMSBluetoothStarPrinter.this.activity.isFinishing())
                    mProgressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                mStarIoExtManager.disconnect();
                return mStarIoExtManager.connect();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!EMSBluetoothStarPrinter.this.activity.isFinishing()) {
                    mProgressDialog.dismiss();
                    if (!result) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
                                EMSBluetoothStarPrinter.this.activity);

                        dialogBuilder.setTitle("Communication Result");
                        dialogBuilder.setMessage("failure.\nPrinter is offline.");
                        dialogBuilder.setPositiveButton("OK", null);
                        dialogBuilder.show();
                    }
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
            // scannedData = new String(data);
            // handler.post(runnableScannedData);
        }
    };

    String scannedData = "";

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
        releasePrinter();
        port = null;
        if (port == null || port.retreiveStatus() == null || port.retreiveStatus().offline) {
            if (portName.toUpperCase().contains("TCP")) {
                String ip = portName.replace("TCP:", "");
                int port = 80;
                try {
                    port = TextUtils.isEmpty(portSettings) ? 80 : Integer.parseInt(portSettings);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (!Global.isIpAvailable(ip, port)) {
                    throw new StarIOPortException("Host not reachable.");
                }
            }
            port = StarIOPort.getPort(portName, portSettings, 10000, activity);
        }
        return port;
    }
}
