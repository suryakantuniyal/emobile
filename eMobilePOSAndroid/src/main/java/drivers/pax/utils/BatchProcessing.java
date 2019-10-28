package drivers.pax.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.widget.EditText;
import android.widget.Toast;

import com.android.support.MyPreferences;
import com.pax.poslink.BatchRequest;
import com.pax.poslink.BatchResponse;
import com.pax.poslink.POSLinkAndroid;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.poslink.POSLinkCreator;

import java.math.BigDecimal;

import static drivers.pax.utils.Constant.TRANSACTION_SUCCESS;
import static drivers.pax.utils.Constant.TRANSACTION_TIMEOUT;
import static drivers.pax.utils.Constant.TRANS_NOT_FOUND;

/**
 * Created by Luis Camayd on 9/12/2019.
 */
public class BatchProcessing {
    private Context context;
    private Activity activity;
    private PosLink poslink;
    private static ProcessTransResult ptr;
    private OnBatchProcessedCallback callback;
    private MyPreferences mPref;

    public BatchProcessing(OnBatchProcessedCallback callback, Activity activity) {
        this.callback = callback;
        this.activity = activity;
        mPref = new MyPreferences(activity);
    }
    public BatchProcessing(OnBatchProcessedCallback callback, Context context) {
        this.callback = callback;
        this.context = context;
        mPref = new MyPreferences(context);
    }
    public interface OnBatchProcessedCallback {
        void onBatchProcessedDone(String result);
    }

    public void close() {
        if(activity != null){
            POSLinkAndroid.init(activity, PosLinkHelper.getCommSetting(mPref.getPaymentDevice(),mPref.getPaymentDeviceIP()));
            poslink = POSLinkCreator.createPoslink(activity);
        } else if(context != null){
            POSLinkAndroid.init(context, PosLinkHelper.getCommSetting(mPref.getPaymentDevice(),mPref.getPaymentDeviceIP()));
            poslink = POSLinkCreator.createPoslink(context);
        }
        BatchRequest batchrequest = new BatchRequest();
        batchrequest.EDCType = 0;
        batchrequest.TransType = 1;
        poslink.BatchRequest = batchrequest;
        poslink.SetCommSetting(PosLinkHelper.getCommSetting(mPref.getPaymentDevice(),mPref.getPaymentDeviceIP()));

        // as processTrans is blocked, we must run it in an async task
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    // ProcessTrans is Blocking call, will return when the transaction is complete.
                    ptr = poslink.ProcessTrans();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(activity != null){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            processPaxBatchResponse();
                        }
                    });
                } else if(context != null){
                    new Runnable() {
                        @Override
                        public void run() {
                            processPaxBatchResponse();
                        }
                    }.run();
                }
            }
        }).start();
    }

    private void processPaxBatchResponse() {
        String result;
        if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {
            BatchResponse response = poslink.BatchResponse;
            switch (response.ResultCode) {
                case TRANSACTION_SUCCESS:
                    result = "Batch Closed!" +" CreditAmount:$" + (new BigDecimal(response.CreditAmount).divide(new BigDecimal(100.00)));
                    break;
                case TRANS_NOT_FOUND:
                    result = "Transactions Not Found!";
                    break;
                case TRANSACTION_TIMEOUT:
                    result = "ERROR: Transaction TimeOut!";
                    break;
                default:
                    result = String.format("ERROR\nCODE: %s\nMESSAGE%s",
                            response.ResultCode, response.ResultTxt);
                    break;
            }
        } else if (ptr.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
            result = "Transaction TimeOut!\n" + ptr.Msg;
        } else {
            result = "Transaction Error!\n" + ptr.Msg;
        }
        callback.onBatchProcessedDone(result);
    }
}
