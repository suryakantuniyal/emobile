package drivers.ingenico.utils;

import com.ingenico.mpos.sdk.Ingenico;
import com.ingenico.mpos.sdk.callbacks.ApplicationSelectionCallback;
import com.ingenico.mpos.sdk.callbacks.TransactionCallback;
import com.ingenico.mpos.sdk.data.Amount;
import com.ingenico.mpos.sdk.request.CreditSaleTransactionRequest;
import com.ingenico.mpos.sdk.response.TransactionResponse;
import com.roam.roamreaderunifiedapi.data.ApplicationIdentifier;

import java.util.List;

/**
 * Created by Luis Camayd on 12/7/2018.
 */
public class MobilePosSdkHelper {

    private OnIngenicoTransactionCallback callback;

    public MobilePosSdkHelper(OnIngenicoTransactionCallback callback) {
        this.callback = callback;
    }

    public interface OnIngenicoTransactionCallback {
        void onIngenicoTransactionDone(Integer responseCode, TransactionResponse response);
    }

    public void startTransaction(String totalAmount) {
        Ingenico.getInstance().payment().processCreditSaleTransactionWithCardReader(
                getCardSaleTransactionRequest(totalAmount),
                new CreditSaleTransactionCallbackImpl());
    }

    private CreditSaleTransactionRequest getCardSaleTransactionRequest(String totalAmount) {
        Amount saleAmount = createNewAmount(
                totalAmount.replace(".", ""),
                "0",
                "0",
                "0",
                "0",
                "0"
        );

        return new CreditSaleTransactionRequest(
                saleAmount,
                null,
                "0",
                "0",
                "0",
                null
        );
    }

    private Amount createNewAmount(String totalAmount, String subtotalAmount,
                                   String tipAmount, String taxAmount, String discountAmount,
                                   String surchargeAmount) {
        int total = convertStringToInt(totalAmount);
        int subtotal = convertStringToInt(subtotalAmount);
        int tip = convertStringToInt(tipAmount);
        int tax = convertStringToInt(taxAmount);
        int discount = convertStringToInt(discountAmount);
        int surcharge = convertStringToInt(surchargeAmount);
        return new Amount(
                "USD",
                total,
                subtotal,
                tax,
                discount,
                "ROAM Discount",
                tip,
                surcharge
        );
    }

    private static int convertStringToInt(String strValue) {
        int val;
        try {
            val = Integer.parseInt(strValue);
        } catch (NumberFormatException ex) {
            val = 0;
        }
        return val;
    }

    public class CreditSaleTransactionCallbackImpl implements TransactionCallback {

        @Override
        public void updateProgress(Integer integer, String s) {
            // do nothing
        }

        @Override
        public void applicationSelection(
                List<ApplicationIdentifier> appList,
                ApplicationSelectionCallback applicationcallback) {
            // do nothing
        }

        @Override
        public void done(Integer responseCode, TransactionResponse response) {
//            mListener.cacheTransactionResponse(response, responseCode);
//            mProgressDialogListener.hideProgress();
//            if (response != null) {
//                Log.v("eMobilePOS", "Response : " + response.toString());
//                mListener.cacheCreditRefundableTransactionId(getTransactionIdFromResponse(response));
//                promptForCardRemovalIfRequired(responseCode, response);
//            } else {
//                logResult(responseCode, response, showDialog);
//            }
//            clearPendingSignatureIfNeeded(responseCode);
            callback.onIngenicoTransactionDone(responseCode, response);
        }
    }
}