package drivers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.android.emobilepos.R;
import com.android.emobilepos.payment.ProcessCreditCard_FA;
import com.android.support.CreditCardInfo;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.payments.core.CoreRefundResponse;
import com.payments.core.CoreResponse;
import com.payments.core.CoreSale;
import com.payments.core.CoreSaleKeyed;
import com.payments.core.CoreSaleResponse;
import com.payments.core.CoreSettings;
import com.payments.core.CoreSignature;
import com.payments.core.CoreTransactions;
import com.payments.core.admin.AndroidTerminal;
import com.payments.core.common.contracts.CoreAPIListener;
import com.payments.core.common.enums.CoreDeviceError;
import com.payments.core.common.enums.CoreError;
import com.payments.core.common.enums.CoreMessage;
import com.payments.core.common.enums.CoreMode;
import com.payments.core.common.enums.DeviceEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import interfaces.EMSCallBack;
import main.EMSDeviceManager;

public class EMSWalker extends EMSDeviceDriver implements CoreAPIListener {

    private Activity activity;
    private AndroidTerminal terminal;
    private String TERMINAL_ID = "1007";
    private String SECRET = "secretpass";
    private CreditCardInfo cardManager;
    public static CoreSignature signature;
    public boolean isReadingCard = false;
    public boolean failedProcessing = false;
    private ProgressDialog dialog;
    private EMSDeviceManager edm;



    @Override
    public void connect(Activity activity, int paperSize, boolean isPOSPrinter, EMSDeviceManager edm) {
        this.activity = activity;
        myPref = new MyPreferences(this.activity);
        this.edm = edm;
        terminal = new AndroidTerminal(this);
        terminal.setMode(CoreMode.DEMO);
        terminal.initWithConfiguration(activity, "1007", "secretpass");
        LinkedHashMap<String, String> supportedDevices = terminal.listSupportedDevices();

        new connectWalkerAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private class connectWalkerAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            // terminal.init(activity, TERMINAL_ID, SECRET, Currency.EUR);

            terminal.setMode(CoreMode.DEMO);
            terminal.initWithConfiguration(EMSWalker.this.activity, TERMINAL_ID, SECRET);
//            LinkedHashMap<String, String> supportedDevices = terminal.listSupportedDevices();
//            terminal.initDevice(DeviceEnum.NOMAD);
//            terminal.selectBTDevice(1);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (terminal.getDevice().equals(DeviceEnum.NOMAD)) {
                try {
                    EMSCallBack callBack = (EMSCallBack) activity;
                    callBack.readerConnectedSuccessfully(true);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void startReading(CreditCardInfo cardInfo, ProgressDialog dialog) {
        isReadingCard = true;
        this.dialog = dialog;
        if (terminal.getDevice().equals(DeviceEnum.NODEVICE)) {
            CoreSaleKeyed sale = new CoreSaleKeyed(cardInfo.dueAmount);
            sale.setCardHolderName(cardInfo.getCardOwnerName());
            sale.setCardNumber(cardInfo.getCardNumUnencrypted());
            sale.setCardCvv(cardInfo.getCardLast4());
            sale.setCardType(cardInfo.getCardType());
            sale.setExpiryDate(cardInfo.getCardExpMonth() + cardInfo.getCardExpYear());
            sale.setAutoReady(true);
            terminal.processSale(sale);
        } else {
            CoreSale sale = new CoreSale(cardInfo.dueAmount);
            terminal.processSale(sale);
        }

        while (isReadingCard) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean deviceConnected() {
        return !terminal.getDevice().equals(DeviceEnum.NODEVICE);
    }

    public void submitSignature() {
        dialog.setMessage(EMSWalker.this.activity.getString(R.string.processing_credit_card));
        if (signature.checkSignature()) {
            // signature.signatureText();
//            signature.submitSignature();
        }

    }

    @Override
    public void onError(CoreError coreError, String s) {
        System.out.print(s.toString());
        failedProcessing = true;
        isReadingCard = false;
        if (!TextUtils.isEmpty(s))
            Global.showPrompt(EMSWalker.this.activity, R.string.card_credit_card, s);
    }

    @Override
    public void onLoginUrlRetrieved(String arg0) {

    }

    @Override
    public void onMessage(CoreMessage msg) {
        System.out.print(msg.toString());

        if (isReadingCard) {
            if (msg.equals(CoreMessage.DEVICE_NOT_CONNECTED)) {
                failedProcessing = true;
                isReadingCard = false;
            } else if (msg.equals(CoreMessage.CARD_ERROR))
                isReadingCard = false;

        }

    }

    @Override
    public void onRefundResponse(CoreRefundResponse arg0) {

    }

    @Override
    public void onSaleResponse(CoreSaleResponse response) {
        isReadingCard = false;
        try {
            EMSCallBack callBack = (EMSCallBack) activity;
            cardManager = new CreditCardInfo();
            cardManager.setCardOwnerName(response.getCardHolderName());
            cardManager.setCardType(response.getCardType());
            cardManager.authcode = response.getApprovalCode();
            cardManager.transid = response.getUniqueRef();
            cardManager.setWasSwiped(false);
            cardManager.setCardLast4(response.getCardNumber().substring(response.getCardNumber().length() - 4));
            callBack.cardWasReadSuccessfully(true, cardManager);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onSettingsRetrieved(CoreSettings arg0) {
//        if (devicePlugged) {
            terminal.initDevice(DeviceEnum.NOMAD);
            this.edm.driverDidConnectToDevice(this, false);
//            try {
//                EMSCallBack callBack = (EMSCallBack) activity;
//                callBack.readerConnectedSuccessfully(true);
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        } else {
//            terminal.initDevice(DeviceEnum.NODEVICE);
//            this.edm.driverDidNotConnectToDevice(this, msg, false);
//
//        }

    }

    @Override
    public void onSignatureRequired(CoreSignature _signature) {
        signature = _signature;
        try {
            EMSCallBack callBack = (EMSCallBack) activity;
            callBack.startSignature();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onTransactionListResponse(CoreTransactions arg0) {
        System.out.print(arg0.toString());
    }

    @Override
    public void onDeviceConnected(DeviceEnum deviceEnum, HashMap<String, String> arg1) {
        Toast.makeText(this.activity, deviceEnum.name() + " connected", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDeviceDisconnected(DeviceEnum deviceEnum) {
        Toast.makeText(this.activity, deviceEnum.name() + " disconnected", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDeviceError(CoreDeviceError arg0, String arg1) {
        this.edm.driverDidNotConnectToDevice(this, arg1, false);
    }

    @Override
    public void onSelectApplication(ArrayList<String> arg0) {

    }

    @Override
    public void onSelectBTDevice(ArrayList<String> arrayList) {

    }

    @Override
    public void onDeviceConnectionError() {
        this.edm.driverDidNotConnectToDevice(this, activity.getString(R.string.fail_to_connect), false);

    }

    @Override
    public void onAutoConfigProgressUpdate(String s) {

    }

    @Override
    public void onReversalRetrieved(CoreResponse coreResponse) {

    }

}
