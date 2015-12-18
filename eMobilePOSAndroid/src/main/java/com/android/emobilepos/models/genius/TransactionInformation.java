package com.android.emobilepos.models.genius;

/**
 * Created by Guarionex on 12/18/2015.
 */
public class TransactionInformation {
    private String TransactionType;
    private String TransactionCurrencyCode;
    private String TransactionStatusInformation;

    public String getTransactionType() {
        return TransactionType;
    }

    public void setTransactionType(String transactionType) {
        TransactionType = transactionType;
    }

    public String getTransactionCurrencyCode() {
        return TransactionCurrencyCode;
    }

    public void setTransactionCurrencyCode(String transactionCurrencyCode) {
        TransactionCurrencyCode = transactionCurrencyCode;
    }

    public String getTransactionStatusInformation() {
        return TransactionStatusInformation;
    }

    public void setTransactionStatusInformation(String transactionStatusInformation) {
        TransactionStatusInformation = transactionStatusInformation;
    }
}
