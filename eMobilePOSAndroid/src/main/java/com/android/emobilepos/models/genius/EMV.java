package com.android.emobilepos.models.genius;

/**
 * Created by Guarionex on 12/18/2015.
 */
public class EMV {
    private ApplicationInformation ApplicationInformation;
    private CardInformation CardInformation;
    private ApplicationCryptogram ApplicationCryptogram;
    private String CvmResults;
    private String IssuerApplicationData;
    private String TerminalVerificationResults;
    private String UnpredictableNumber;
    private Amount Amount;
    private String PosEntryMode;
    private TerminalInformation TerminalInformation;
    private TransactionInformation TransactionInformation;
    private String CryptogramInformationData;
    private String PINStatement;
    private String CvmMethod;
    private String IssuerActionCodeDefault;
    private String IssuerActionCodeDenial;
    private String IssuerActionCodeOnline;
    private String AuthorizationResponseCode;

    public com.android.emobilepos.models.genius.ApplicationInformation getApplicationInformation() {
        return ApplicationInformation;
    }

    public void setApplicationInformation(com.android.emobilepos.models.genius.ApplicationInformation applicationInformation) {
        ApplicationInformation = applicationInformation;
    }

    public com.android.emobilepos.models.genius.CardInformation getCardInformation() {
        return CardInformation;
    }

    public void setCardInformation(com.android.emobilepos.models.genius.CardInformation cardInformation) {
        CardInformation = cardInformation;
    }

    public com.android.emobilepos.models.genius.ApplicationCryptogram getApplicationCryptogram() {
        return ApplicationCryptogram;
    }

    public void setApplicationCryptogram(com.android.emobilepos.models.genius.ApplicationCryptogram applicationCryptogram) {
        ApplicationCryptogram = applicationCryptogram;
    }

    public String getCvmResults() {
        return CvmResults;
    }

    public void setCvmResults(String cvmResults) {
        CvmResults = cvmResults;
    }

    public String getIssuerApplicationData() {
        return IssuerApplicationData;
    }

    public void setIssuerApplicationData(String issuerApplicationData) {
        IssuerApplicationData = issuerApplicationData;
    }

    public String getTerminalVerificationResults() {
        return TerminalVerificationResults;
    }

    public void setTerminalVerificationResults(String terminalVerificationResults) {
        TerminalVerificationResults = terminalVerificationResults;
    }

    public String getUnpredictableNumber() {
        return UnpredictableNumber;
    }

    public void setUnpredictableNumber(String unpredictableNumber) {
        UnpredictableNumber = unpredictableNumber;
    }

    public com.android.emobilepos.models.genius.Amount getAmount() {
        return Amount;
    }

    public void setAmount(com.android.emobilepos.models.genius.Amount amount) {
        Amount = amount;
    }

    public String getPosEntryMode() {
        return PosEntryMode;
    }

    public void setPosEntryMode(String posEntryMode) {
        PosEntryMode = posEntryMode;
    }

    public com.android.emobilepos.models.genius.TerminalInformation getTerminalInformation() {
        return TerminalInformation;
    }

    public void setTerminalInformation(com.android.emobilepos.models.genius.TerminalInformation terminalInformation) {
        TerminalInformation = terminalInformation;
    }

    public com.android.emobilepos.models.genius.TransactionInformation getTransactionInformation() {
        return TransactionInformation;
    }

    public void setTransactionInformation(com.android.emobilepos.models.genius.TransactionInformation transactionInformation) {
        TransactionInformation = transactionInformation;
    }

    public String getCryptogramInformationData() {
        return CryptogramInformationData;
    }

    public void setCryptogramInformationData(String cryptogramInformationData) {
        CryptogramInformationData = cryptogramInformationData;
    }

    public String getPINStatement() {
        return PINStatement;
    }

    public void setPINStatement(String PINStatement) {
        this.PINStatement = PINStatement;
    }

    public String getCvmMethod() {
        return CvmMethod;
    }

    public void setCvmMethod(String cvmMethod) {
        CvmMethod = cvmMethod;
    }

    public String getIssuerActionCodeDefault() {
        return IssuerActionCodeDefault;
    }

    public void setIssuerActionCodeDefault(String issuerActionCodeDefault) {
        IssuerActionCodeDefault = issuerActionCodeDefault;
    }

    public String getIssuerActionCodeDenial() {
        return IssuerActionCodeDenial;
    }

    public void setIssuerActionCodeDenial(String issuerActionCodeDenial) {
        IssuerActionCodeDenial = issuerActionCodeDenial;
    }

    public String getIssuerActionCodeOnline() {
        return IssuerActionCodeOnline;
    }

    public void setIssuerActionCodeOnline(String issuerActionCodeOnline) {
        IssuerActionCodeOnline = issuerActionCodeOnline;
    }

    public String getAuthorizationResponseCode() {
        return AuthorizationResponseCode;
    }

    public void setAuthorizationResponseCode(String authorizationResponseCode) {
        AuthorizationResponseCode = authorizationResponseCode;
    }
}
