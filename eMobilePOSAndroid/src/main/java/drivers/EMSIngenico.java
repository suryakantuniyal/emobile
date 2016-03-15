package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;

import com.android.emobilepos.models.EMVContainer;
import com.android.emobilepos.models.Orders;
import com.android.emobilepos.models.Payment;
import com.android.support.CardParser;
import com.android.support.ConsignmentTransaction;
import com.android.support.CreditCardInfo;
import com.android.support.Encrypt;
import com.android.support.Global;
import com.android.support.MyPreferences;

import org.springframework.util.support.Base64;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import main.EMSDeviceManager;
import interfaces.EMSCallBack;
import interfaces.EMSDeviceManagerPrinterDelegate;
import rba_sdk.Comm_Settings;
import rba_sdk.Comm_Settings_Constants;
import rba_sdk.Comm_Timeout;
import rba_sdk.ERROR_ID;
import rba_sdk.EventHandlerInterface;
import rba_sdk.MESSAGE_ID;
import rba_sdk.PARAMETER_ID;
import rba_sdk.RBA_API;

public class EMSIngenico extends EMSDeviceDriver implements EMSDeviceManagerPrinterDelegate, EventHandlerInterface {

    private ProgressDialog myProgressDialog;
    private EMSDeviceDriver thisInstance;
    private EMSDeviceManager edm;
    private CreditCardInfo cardManager;
    private Encrypt encrypt;
    private Activity activity;
    private MyPreferences myPref;

    private Handler handler;
    private EMSCallBack callBack, _scannerCallBack;
    private EventHandlerInterface sdkEventHandler;
    private boolean barcodeReaderLoaded = false;
    private boolean mIsDebit = false;

    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);

        if (handler == null)
            handler = new Handler();

        thisInstance = this;
        this.edm = edm;
        encrypt = new Encrypt(activity);
        sdkEventHandler = this;

        new processConnectionAsync().execute(0);
    }

    @Override
    public boolean autoConnect(Activity _activity, EMSDeviceManager edm, int paperSize, boolean isPOSPrinter,
                               String _portName, String _portNumber) {
        this.activity = _activity;
        myPref = new MyPreferences(this.activity);

        thisInstance = this;
        this.edm = edm;
        encrypt = new Encrypt(activity);
        boolean didConnect = false;
        sdkEventHandler = this;

        RBA_API.Initialize();
        RBA_API.SetMessageCallBack(sdkEventHandler);

        Comm_Settings commSettings = new Comm_Settings();
        commSettings.Interface_id = Comm_Settings_Constants.BLUETOOTH_INTERFACE;
        commSettings.BT_Name = "";

        setCommTimeOuts();
        ERROR_ID connectionRequest;
        try {
            connectionRequest = RBA_API.Connect(commSettings);
        } catch (Exception e) {
            connectionRequest = ERROR_ID.RESULT_ERROR;
        }
        switch (connectionRequest) {
            case RESULT_SUCCESS:
            case RESULT_ERROR_ALREADY_CONNECTED:
                didConnect = true;
                break;
            default:
                break;
        }

        if (didConnect) {
            edm.driverDidConnectToDevice(thisInstance, false);
        } else {

            edm.driverDidNotConnectToDevice(thisInstance, "", false);
        }

        return didConnect;
    }

    public class processConnectionAsync extends AsyncTask<Integer, String, String> {

        String msg = new String("Failed to connect");
        boolean didConnect = false;

        @Override
        protected void onPreExecute() {
            myProgressDialog = new ProgressDialog(activity);
            myProgressDialog.setMessage("Connecting Printer...");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.setCancelable(false);
            myProgressDialog.show();

        }

        @Override
        protected String doInBackground(Integer... params) {
            RBA_API.Initialize();
            RBA_API.SetMessageCallBack(sdkEventHandler);

            Comm_Settings commSettings = new Comm_Settings();
            commSettings.Interface_id = Comm_Settings_Constants.BLUETOOTH_INTERFACE;
            commSettings.BT_Name = "";

            setCommTimeOuts();
            ERROR_ID connectionRequest = RBA_API.Connect(commSettings);

            switch (connectionRequest) {
                case RESULT_SUCCESS:
                case RESULT_ERROR_ALREADY_CONNECTED:

                    didConnect = true;
                    break;
                default:
                    msg = "Failed to connect: \nError - " + connectionRequest;
                    break;
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

    /*
     * ------------------------------ Set Comm Timeouts
     * ------------------------------
     */
    public void setCommTimeOuts() {
        Comm_Timeout comm_time_outs = new Comm_Timeout();
        comm_time_outs.connectTimeOut = java.lang.Integer.parseInt("3000");
        comm_time_outs.receiveTimeOut = java.lang.Integer.parseInt("3000");
        comm_time_outs.sendTimeOut = java.lang.Integer.parseInt("3000");
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType saleTypes, boolean isFromHistory, boolean fromOnHold, EMVContainer emvContainer) {
        EMSDeviceManagerPrinterDelegate currentDevice = Global.mainPrinterManager.currentDevice;
        currentDevice.printTransaction(ordID, saleTypes, isFromHistory, fromOnHold);
        return true;
    }

    @Override
    public boolean printTransaction(String ordID, Global.OrderType type, boolean isFromHistory, boolean fromOnHold) {
        printTransaction(ordID, type, isFromHistory, fromOnHold, null);
        return true;
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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean printConsignmentPickup(List<ConsignmentTransaction> myConsignment, String encodedSignature) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean printConsignmentHistory(HashMap<String, String> map, Cursor c, boolean isPickup) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean printOpenInvoices(String invID) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean printOnHold(Object onHold) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setBitmap(Bitmap bmp) {
        // TODO Auto-generated method stub

    }

    @Override
    public void playSound() {

    }

    @Override
    public boolean printReport(String curDate) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void registerAll() {
        this.registerPrinter();
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
    public void loadCardReader(EMSCallBack _callBack, boolean isDebitCard) {
        // TODO Auto-generated method stub
        callBack = _callBack;
        mIsDebit = isDebitCard;
        if (handler == null)
            handler = new Handler();

        new Thread(new Runnable() {
            public void run() {

                RBA_API.SetParam(PARAMETER_ID.P23_REQ_FORM_NAME, "CCOD.K3Z");
                RBA_API.SetParam(PARAMETER_ID.P23_REQ_PROMPT_INDEX, "Slide, Tap or Insert Card");
                RBA_API.SetParam(PARAMETER_ID.P23_REQ_ENABLE_DEVICES, "MCS");
                RBA_API.SetParam(PARAMETER_ID.P23_REQ_OPTIONS, "1");

                ERROR_ID result = RBA_API.ProcessMessage(MESSAGE_ID.M23_CARD_READ);
                if (result == ERROR_ID.RESULT_SUCCESS) {
                    handler.post(doUpdateDidConnect);
                }
            }
        }).start();
    }

    // displays data from card swiping
    private Runnable doUpdateViews = new Runnable() {
        public void run() {
            try {
                if (callBack != null) {
                    // Toast.makeText(activity, "card was swiper calling
                    // callback..", Toast.LENGTH_LONG).show();
                    callBack.cardWasReadSuccessfully(true, cardManager);
                    //getAcctNum();
                    // MSG31_PinEntry("4123456789012345");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    private Runnable doUpdateDidConnect = new Runnable() {
        public void run() {
            try {
                if (callBack != null) {
                    // Toast.makeText(activity, "update did connected..",
                    // Toast.LENGTH_LONG).show();
                    callBack.readerConnectedSuccessfully(true);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    private String scannedData = "";

    private Runnable runnableScannedData = new Runnable() {
        public void run() {
            try {
                if (_scannerCallBack != null)
                    _scannerCallBack.scannerWasRead(scannedData);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    @Override
    public void loadScanner(EMSCallBack _callBack) {
        // TODO Auto-generated method stub
        _scannerCallBack = _callBack;
        if (handler == null)
            handler = new Handler();
        if (_callBack != null && !barcodeReaderLoaded) {
            new Thread(new Runnable() {
                public void run() {

                    if (barcodeReaderLoaded)
                        MSG94_BarcodeConfig("01", "00", null); // disable

                    MSG94_BarcodeConfig("30", "01", "03,04");
                    MSG94_BarcodeConfig("11", "01", null);
                    MSG94_BarcodeConfig("01", "01", null);

                }
            }).start();
        }
    }

    @Override
    public void releaseCardReader() {
        MSG94_BarcodeConfig("01", "00", null);
        barcodeReaderLoaded = false;
    }

    @Override
    public void openCashDrawer() {
        // TODO Auto-generated method stub

    }

    @Override
    public void printHeader() {
        // TODO Auto-generated method stub

    }

    @Override
    public void printFooter() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isUSBConnected() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void toggleBarcodeReader() {

    }

    @Override
    public void printReceiptPreview(View view) {

    }

    @Override
    public void salePayment(BigInteger amount) {

    }

    @Override
    public void saleReversal(BigInteger amount, String originalTransactionId) {

    }

    @Override
    public void refund(BigInteger amount) {

    }

    @Override
    public void refundReversal(Payment payment) {

    }

    @Override
    public void printEMVReceipt(String text) {

    }

    @Override
    public void PinPadMessageCallBack(MESSAGE_ID msgID) {
        // TODO Auto-generated method stub
        switch (msgID) {

            case M00_OFFLINE: {
                String reasonCode = RBA_API.GetParam(PARAMETER_ID.P00_RES_REASON_CODE);
            }
            break;

            case M09_SET_ALLOWED_PAYMENT: {

                String status = RBA_API.GetParam(PARAMETER_ID.P09_RES_CARD_STATUS);
                String cardType = RBA_API.GetParam(PARAMETER_ID.P09_RES_CARD_TYPE);
                String msgVersion = RBA_API.GetParam(PARAMETER_ID.P09_RES_MSG_VERSION);
                String transactionType = RBA_API.GetParam(PARAMETER_ID.P09_RES_TRANSACTION_TYPE);
            }
            break;

            case M20_SIGNATURE: {

                String status = RBA_API.GetParam(PARAMETER_ID.P20_RES_STATUS);
            }
            break;

            case M21_NUMERIC_INPUT: {

            }
            break;

            case M23_CARD_READ: {
                String exitType = RBA_API.GetParam(PARAMETER_ID.P23_RES_EXIT_TYPE);

                switch (Integer.parseInt(exitType)) {
                    case 0: {
                        String track1 = RBA_API.GetParam(PARAMETER_ID.P23_RES_TRACK1);
                        String track2 = RBA_API.GetParam(PARAMETER_ID.P23_RES_TRACK2);
                        String track3 = RBA_API.GetParam(PARAMETER_ID.P23_RES_TRACK3);

                        StringBuilder raw_data = new StringBuilder();
                        if (track1 != null && !track1.isEmpty())
                            raw_data.append("%").append(track1).append("?");
                        if (track2 != null && !track2.isEmpty())
                            raw_data.append(";").append(track2).append("?");

                        cardManager = new CreditCardInfo();
                        CardParser.parseCreditCard(activity, raw_data.toString(), cardManager);
                        if (mIsDebit) {
                            //handler.post(doUpdateViews);
                            //String retValue = MSG29_GetVariable("00398");
                            //MSG31_PinEntry(cardManager.getCardNumUnencrypted());
                            //getAcctNum();
                            String retValue = MSG29_GetVariable("00398");

                            if (retValue == null) {
                                // For Testing This Value Will Be Hardcoded
                                MSG31_PinEntry("4123456789012345");
                            } else {
                                MSG31_PinEntry(retValue);
                            }
                        } else
                            handler.post(doUpdateViews);
                    }
                    break;
                    default:
                        break;
                }
            }
            break;

            case M24_FORM_ENTRY: {
            }
            break;

            case M27_ALPHA_INPUT: {
            }
            break;

            case M31_PIN_ENTRY: {
                String status = RBA_API.GetParam(PARAMETER_ID.P31_RES_STATUS);

                switch (Integer.parseInt(status)) {
                    case 0: {
                        String pinData = RBA_API.GetParam(PARAMETER_ID.P31_RES_PIN_DATA);
                        String encPINBlock = pinData.substring(0, 15);
                        String keySerialNum = pinData.substring(16);

                        cardManager.setDebitPinBlock(encPINBlock);
                        cardManager.setDebitPinSerialNum(keySerialNum);
                        handler.post(doUpdateViews);
                    }
                    break;
                    default:
                }
            }
            break;

            case M33_00_EMV_TRANSACTION_INITIATION: {
            }
            break;

            case M33_01_EMV_STATUS: {
            }
            break;

            case M33_02_EMV_TRANSACTION_PREPARATION_RESPONSE: {
            }
            break;

            case M33_03_EMV_AUTHORIZATION_REQUEST: {
            }
            break;

            case M33_05_EMV_AUTHORIZATION_CONFIRMATION: {
            }
            break;

            case M33_06_EMV_TERMINATE: {
            }
            break;

            case M33_07_EMV_TERMINAL_CAPABILITIES: {
            }

            case M41_CARD_READ: {
            }
            break;

            case M50_AUTHORIZATION: {
            }
            break;

            case M95_BARCODE_GET: {
                String status = RBA_API.GetParam(PARAMETER_ID.P95_RES_STATUS);

                if (status.equals("9")) {
                    String base64String = RBA_API.GetParam(PARAMETER_ID.P95_RES_BARCODE_DATA);
                    try {
                        scannedData = new String(Base64.decode(base64String));
                        handler.post(runnableScannedData);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (status.equals("8")) {
                }
            }
            break;
            default: {
            }
            break;
        }
    }

    /*
     * ----------------------------------- Barcode Scanner Configuration
     * (94.xxxx) -----------------------------------
     */
    public void MSG94_BarcodeConfig(String typeCode, String actCode, String symbology) {
        RBA_API.SetParam(PARAMETER_ID.P94_REQ_ACTION_CODE, actCode);
        RBA_API.SetParam(PARAMETER_ID.P94_REQ_TYPE_CODE, typeCode);
        RBA_API.SetParam(PARAMETER_ID.P94_REQ_SYMBOLOGY_LIST, symbology);

        ERROR_ID result = RBA_API.ProcessMessage(MESSAGE_ID.M94_BARCODE_SET);
        if (result == ERROR_ID.RESULT_SUCCESS) {
            barcodeReaderLoaded = true;
        } else {
        }
    }

    /*
     * ------------------------------ PIN Entry (31)
     * ------------------------------
     */
    public void MSG31_PinEntry(String accountNumber) {

        RBA_API.SetParam(PARAMETER_ID.P31_REQ_CUSTOMER_ACC_NUM, accountNumber);
        RBA_API.SetParam(PARAMETER_ID.P31_REQ_PROMPT_INDEX_NUMBER, "14");
        RBA_API.SetParam(PARAMETER_ID.P31_REQ_SET_ENCRYPTION_CONFIGURATION, "*");
        RBA_API.SetParam(PARAMETER_ID.P31_REQ_SET_KEY_TYPE, "*");

        ERROR_ID result = RBA_API.ProcessMessage(MESSAGE_ID.M31_PIN_ENTRY);
        if (result == ERROR_ID.RESULT_SUCCESS) {

        }

    }

    @Override
    public void printStationPrinter(List<Orders> orderProducts, String ordID) {
        // TODO Auto-generated method stub

    }

    @Override
	public void printEndOfDayReport(String curDate, String clerk_id, boolean printDetails) {
		//printEndOfDayReportReceipt(curDate, LINE_WIDTH, printDetails);
    }

	@Override
	public void printShiftDetailsReport(String shiftID) {
	}

	public void getAcctNum() {
		String retValue = MSG29_GetVariable("00398");

        if (retValue == null) {
            // For Testing This Value Will Be Hardcoded
            MSG31_PinEntry("4123456789012345");
        } else {
            MSG31_PinEntry(retValue);
        }
    }

    public String MSG29_GetVariable(String variable) {
        if (!getConnectionStatus()) {
            return null;
        }

        RBA_API.SetParam(PARAMETER_ID.P29_REQ_VARIABLE_ID, variable);

        ERROR_ID result = RBA_API.ProcessMessage(MESSAGE_ID.M29_GET_VARIABLE);
        if (result != ERROR_ID.RESULT_SUCCESS) {
            return null;
        } else {
            String varID = RBA_API.GetParam(PARAMETER_ID.P29_RES_VARIABLE_ID);
            String status = RBA_API.GetParam(PARAMETER_ID.P29_RES_STATUS);

            switch (Integer.parseInt(status)) {
                case 2: {
                    String varData = RBA_API.GetParam(PARAMETER_ID.P29_RES_VARIABLE_DATA);
                    return varData;
                }
                default:
                    return null;
            }
        }
    }

    /*
     * ------------------------------ Get Connection Status
     * ------------------------------
     */
    public boolean getConnectionStatus() {
        return RBA_API.GetConnectionStatus() == RBA_API.ConnectionStatus.CONNECTED;
    }

}
