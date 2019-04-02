package drivers.ingenico.utils;

import com.ingenico.mpos.sdk.Ingenico;
import com.ingenico.mpos.sdk.callbacks.ApplicationSelectionCallback;
import com.ingenico.mpos.sdk.callbacks.TransactionCallback;
import com.ingenico.mpos.sdk.constants.ResponseCode;
import com.ingenico.mpos.sdk.constants.UCIFormat;
import com.ingenico.mpos.sdk.data.Amount;
import com.ingenico.mpos.sdk.request.CreditCardRefundTransactionRequest;
import com.ingenico.mpos.sdk.request.CreditSaleTransactionRequest;
import com.ingenico.mpos.sdk.request.VoidTransactionRequest;
import com.ingenico.mpos.sdk.response.TransactionResponse;
import com.roam.roamreaderunifiedapi.data.ApplicationIdentifier;

import java.util.List;

import util.MoneyUtils;

/**
 * Created by Luis Camayd on 12/7/2018.
 */
public class MobilePosSdkHelper {

    public static final String MOBY8500 = "MOBY8500";

    private OnIngenicoTransactionCallback callback;

    public MobilePosSdkHelper(OnIngenicoTransactionCallback callback) {
        this.callback = callback;
    }

    public interface OnIngenicoTransactionCallback {
        void onIngenicoTransactionDone(Integer responseCode, TransactionResponse response);
    }

    public void startTransaction(Boolean isRefund, String totalAmount) {
        if (isRefund) {
            Ingenico.getInstance().payment().processCreditRefundWithCardReader(
                    getCardRefundTransactionRequest(totalAmount),
                    new CardRefundTransactionCallbackImpl());
        } else {
            Ingenico.getInstance().payment().processCreditSaleTransactionWithCardReader(
                    getCardSaleTransactionRequest(totalAmount),
                    new CreditSaleTransactionCallbackImpl());
        }
    }

    public void startVoid(String orderId, String originalTransactionId) {
        Ingenico.getInstance().payment().processVoidTransaction(
                getVoidTransactionRequest(orderId, originalTransactionId),
                new VoidTransactionCallbackImpl()
        );
    }

    private CreditSaleTransactionRequest getCardSaleTransactionRequest(String totalAmount) {
        Amount saleAmount = createNewAmount(
                String.valueOf(MoneyUtils.convertDollarsToCents(totalAmount)),
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

    private CreditCardRefundTransactionRequest getCardRefundTransactionRequest(
            String totalRefundAmount) {
        Amount refundAmount = createNewAmount(
                String.valueOf(MoneyUtils.convertDollarsToCents(totalRefundAmount)),
                "0",
                "0",
                "0",
                "0",
                "0"
        );

        return new CreditCardRefundTransactionRequest(
                refundAmount,
                "0",
                "0",
                "0",
                "",
                "0",
                Boolean.TRUE,
                "",
                null,
                UCIFormat.Ingenico
        );
    }

    private VoidTransactionRequest getVoidTransactionRequest(String ordId, String txnId) {
        return new VoidTransactionRequest(
                txnId,
                "0",
                "0",
                "0",
                ordId
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

    public static String getResponseCodeString(int responseCode) {
        switch (responseCode) {
            case ResponseCode.Success:
                return "Success";
            case ResponseCode.MissingAPIKey:
                return "Missing API Key";
            case ResponseCode.PaymentDeviceNotAvailable:
                return "Payment Device Not Available";
            case ResponseCode.PaymentDeviceError:
                return "Payment Device Not Error";
            case ResponseCode.PaymentDeviceTimeout:
                return "Payment Device Timeouts";
            case ResponseCode.NotSupportedByPaymentDevice:
                return "Not Supported by Payment Device";
            case ResponseCode.CardBlocked:
                return "Card Blocked";
            case ResponseCode.ApplicationBlocked:
                return "Application Blocked";
            case ResponseCode.InvalidCard:
                return "Invalid Card";
            case ResponseCode.HostExpiredCard:
                return "Expired Card";
            case ResponseCode.InvalidApplication:
                return "Invalid Card Application";
            case ResponseCode.TransactionCancelled:
                return "Transaction Cancelled";
            case ResponseCode.CardReaderGeneralError:
                return "Card Reader General Error";
            case ResponseCode.CardInterfaceGeneralError:
                return "Card Not Accepted";
            case ResponseCode.BatteryTooLowError:
                return "Battery Too Low";
            case ResponseCode.BadCardSwipe:
                return "Bad Card Swipe";
            case ResponseCode.TransactionDeclined:
                return "Transaction Declined";
            case ResponseCode.TransactionReversalCardRemovedFailed:
                return "Transaction Reversal Card Removed Failed";
            case ResponseCode.TransactionReversalCardRemovedSuccess:
                return "Transaction Reversal Card Removed Success";
            case ResponseCode.TransactionReversalChipDeclineFailed:
                return "Transaction Reversal Chip Decline  Failed";
            case ResponseCode.TransactionReversalChipDeclineSuccess:
                return "Transaction Reversal Chip Decline Success";
            case ResponseCode.TransactionRefusedBecauseOfTransactionWithPendingSignature:
                return "Transaction Refused Because Of Transaction With Pending Signature";
            case ResponseCode.UnsupportedCard:
            case ResponseCode.HostInconsistentData:
            case ResponseCode.DecryptionError:
                return "Unsupported Card";
        }
        return String.valueOf(responseCode);
    }

    private class CreditSaleTransactionCallbackImpl implements TransactionCallback {

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
            callback.onIngenicoTransactionDone(responseCode, response);
        }
    }

    private class CardRefundTransactionCallbackImpl implements TransactionCallback {

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
            callback.onIngenicoTransactionDone(responseCode, response);
        }
    }

    private class VoidTransactionCallbackImpl implements TransactionCallback {
        @Override
        public void updateProgress(Integer progressCode, String extraMessage) {
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
            callback.onIngenicoTransactionDone(responseCode, response);
        }
    }
}