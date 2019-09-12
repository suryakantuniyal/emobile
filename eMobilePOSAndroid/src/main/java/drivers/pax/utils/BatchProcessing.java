package drivers.pax.utils;

import android.app.Activity;

import com.pax.poslink.BatchRequest;
import com.pax.poslink.BatchResponse;
import com.pax.poslink.POSLinkAndroid;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.poslink.POSLinkCreator;

import static drivers.pax.utils.Constant.TRANSACTION_SUCCESS;
import static drivers.pax.utils.Constant.TRANSACTION_TIMEOUT;
import static drivers.pax.utils.Constant.TRANS_NOT_FOUND;

/**
 * Created by Luis Camayd on 9/12/2019.
 */
public class BatchProcessing {
    private Activity activity;
    private PosLink poslink;
    private static ProcessTransResult ptr;
    private OnBatchProcessedCallback callback;

    public BatchProcessing(OnBatchProcessedCallback callback, Activity activity) {
        this.callback = callback;
        this.activity = activity;
    }

    public interface OnBatchProcessedCallback {
        void onBatchProcessedDone(String result);
    }

    public void close() {
        POSLinkAndroid.init(activity, PosLinkHelper.getCommSetting());
        poslink = POSLinkCreator.createPoslink(activity);
        BatchRequest batchrequest = new BatchRequest();
        batchrequest.EDCType = 0;
        batchrequest.TransType = 1;
        poslink.BatchRequest = batchrequest;
        poslink.SetCommSetting(PosLinkHelper.getCommSetting());

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
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processPaxBatchResponse();
                    }
                });
            }
        }).start();
    }

    private void processPaxBatchResponse() {
        String result;
        if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {
            BatchResponse response = poslink.BatchResponse;
            switch (response.ResultCode) {
                case TRANSACTION_SUCCESS:
                    result = "Batch Closed!";
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
