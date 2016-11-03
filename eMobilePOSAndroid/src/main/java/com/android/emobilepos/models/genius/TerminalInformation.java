package com.android.emobilepos.models.genius;

/**
 * Created by Guarionex on 12/18/2015.
 */
public class TerminalInformation {
    private String TerminalType;
    private String IfdSerialNumber;
    private String TerminalCountryCode;
    private String TerminalID;
    private String TerminalActionCodeDefault;
    private String TerminalActionCodeDenial;
    private String TerminalActionCodeOnline;

    public String getTerminalType() {
        return TerminalType;
    }

    public void setTerminalType(String terminalType) {
        TerminalType = terminalType;
    }

    public String getIfdSerialNumber() {
        return IfdSerialNumber;
    }

    public void setIfdSerialNumber(String ifdSerialNumber) {
        IfdSerialNumber = ifdSerialNumber;
    }

    public String getTerminalCountryCode() {
        return TerminalCountryCode;
    }

    public void setTerminalCountryCode(String terminalCountryCode) {
        TerminalCountryCode = terminalCountryCode;
    }

    public String getTerminalID() {
        return TerminalID;
    }

    public void setTerminalID(String terminalID) {
        TerminalID = terminalID;
    }

    public String getTerminalActionCodeDefault() {
        return TerminalActionCodeDefault;
    }

    public void setTerminalActionCodeDefault(String terminalActionCodeDefault) {
        TerminalActionCodeDefault = terminalActionCodeDefault;
    }

    public String getTerminalActionCodeDenial() {
        return TerminalActionCodeDenial;
    }

    public void setTerminalActionCodeDenial(String terminalActionCodeDenial) {
        TerminalActionCodeDenial = terminalActionCodeDenial;
    }

    public String getTerminalActionCodeOnline() {
        return TerminalActionCodeOnline;
    }

    public void setTerminalActionCodeOnline(String terminalActionCodeOnline) {
        TerminalActionCodeOnline = terminalActionCodeOnline;
    }
}
