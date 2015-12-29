package com.android.emobilepos.models.genius;

/**
 * Created by Guarionex on 12/18/2015.
 */
public class GeniusResponse {
    private String Status;
    private String AmountApproved;
    private String AuthorizationCode;
    private String Cardholder;
    private String AccountNumber;
    private String PaymentType;
    private String EntryMode;
    private String ErrorMessage;
    private String Token;
    private String TransactionDate;
    private String TransactionType;
    private String ResponseType;
    private String ValidationKey;
     private AdditionalParameters AdditionalParameters;


    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getAmountApproved() {
        return AmountApproved;
    }

    public void setAmountApproved(String amountApproved) {
        AmountApproved = amountApproved;
    }

    public String getAuthorizationCode() {
        return AuthorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        AuthorizationCode = authorizationCode;
    }

    public String getCardholder() {
        return Cardholder;
    }

    public void setCardholder(String cardholder) {
        Cardholder = cardholder;
    }

    public String getAccountNumber() {
        return AccountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        AccountNumber = accountNumber;
    }

    public String getPaymentType() {
        return PaymentType;
    }

    public void setPaymentType(String paymentType) {
        PaymentType = paymentType;
    }

    public String getEntryMode() {
        return EntryMode;
    }

    public void setEntryMode(String entryMode) {
        EntryMode = entryMode;
    }

    public String getErrorMessage() {
        return ErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        ErrorMessage = errorMessage;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    public String getTransactionDate() {
        return TransactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        TransactionDate = transactionDate;
    }

    public String getTransactionType() {
        return TransactionType;
    }

    public void setTransactionType(String transactionType) {
        TransactionType = transactionType;
    }

    public String getResponseType() {
        return ResponseType;
    }

    public void setResponseType(String responseType) {
        ResponseType = responseType;
    }

    public String getValidationKey() {
        return ValidationKey;
    }

    public void setValidationKey(String validationKey) {
        ValidationKey = validationKey;
    }

    public com.android.emobilepos.models.genius.AdditionalParameters getAdditionalParameters() {
        return AdditionalParameters;
    }

    public void setAdditionalParameters(com.android.emobilepos.models.genius.AdditionalParameters additionalParameters) {
        AdditionalParameters = additionalParameters;
    }
}
