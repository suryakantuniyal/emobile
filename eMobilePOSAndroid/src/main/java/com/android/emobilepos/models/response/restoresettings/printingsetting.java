package com.android.emobilepos.models.response.restoresettings;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class printingsetting {
    @SerializedName("Enabled")
    private boolean Enabled= false;
    @SerializedName("AutomaticPrinting")
    private boolean AutomaticPrinting= false;
    @SerializedName("MupltiplePrints")
    private boolean MupltiplePrints= false;
    @SerializedName("PermitReceipt")
    private boolean PermitReceipt= false;
    @SerializedName("PrinterWidth")
    private String PrinterWidth="";
    @SerializedName("SplitStationByCategories")
    private boolean SplitStationByCategories= false;
    @SerializedName("WholesalePrintOut")
    private boolean WholesalePrintOut= false;
    @SerializedName("HandwrittenSignature")
    private boolean HandwrittenSignature= false;
    @SerializedName("PromptReceiptCC")
    private boolean PromptReceiptCC= false;
    @SerializedName("PrintTransPayments")
    private boolean PrintTransPayments= false;
    @SerializedName("PrintTaxesBreakdown")
    private boolean PrintTaxesBreakdown= false;
    @SerializedName("StarInfo")
    private String StarInfo="";
    @SerializedName("SNBCSetup")
    private String SNBCSetup="";
    @SerializedName("BixolonSetup")
    private bixolonsetupsetting BixolonSetup = new bixolonsetupsetting();
    @SerializedName("PrintPrefs")
    private printprefs PrintPrefs = new printprefs();
    @SerializedName("PrintRasterMode")
    private boolean PrintRasterMode= false;


    public boolean isEnabled() {
        return Enabled;
    }

    public void setEnabled(boolean enabled) {
        Enabled = enabled;
    }

    public boolean isAutomaticPrinting() {
        return AutomaticPrinting;
    }

    public void setAutomaticPrinting(boolean automaticPrinting) {
        AutomaticPrinting = automaticPrinting;
    }

    public boolean isMupltiplePrints() {
        return MupltiplePrints;
    }

    public void setMupltiplePrints(boolean mupltiplePrints) {
        MupltiplePrints = mupltiplePrints;
    }

    public boolean isPermitReceipt() {
        return PermitReceipt;
    }

    public void setPermitReceipt(boolean permitReceipt) {
        PermitReceipt = permitReceipt;
    }

    public String getPrinterWidth() {
        return PrinterWidth;
    }

    public void setPrinterWidth(String printerWidth) {
        PrinterWidth = printerWidth;
    }

    public boolean isSplitStationByCategories() {
        return SplitStationByCategories;
    }

    public void setSplitStationByCategories(boolean splitStationByCategories) {
        SplitStationByCategories = splitStationByCategories;
    }

    public boolean isWholesalePrintOut() {
        return WholesalePrintOut;
    }

    public void setWholesalePrintOut(boolean wholesalePrintOut) {
        WholesalePrintOut = wholesalePrintOut;
    }

    public boolean isHandwrittenSignature() {
        return HandwrittenSignature;
    }

    public void setHandwrittenSignature(boolean handwrittenSignature) {
        HandwrittenSignature = handwrittenSignature;
    }

    public boolean isPromptReceiptCC() {
        return PromptReceiptCC;
    }

    public void setPromptReceiptCC(boolean promptReceiptCC) {
        PromptReceiptCC = promptReceiptCC;
    }

    public boolean isPrintTransPayments() {
        return PrintTransPayments;
    }

    public void setPrintTransPayments(boolean printTransPayments) {
        PrintTransPayments = printTransPayments;
    }

    public boolean isPrintTaxesBreakdown() {
        return PrintTaxesBreakdown;
    }

    public void setPrintTaxesBreakdown(boolean printTaxesBreakdown) {
        PrintTaxesBreakdown = printTaxesBreakdown;
    }

    public String getStarInfo() {
        return StarInfo;
    }

    public void setStarInfo(String starInfo) {
        StarInfo = starInfo;
    }

    public String getSNBCSetup() {
        return SNBCSetup;
    }

    public void setSNBCSetup(String SNBCSetup) {
        this.SNBCSetup = SNBCSetup;
    }

    public bixolonsetupsetting getBixolonSetup() {
        return BixolonSetup;
    }

    public void setBixolonSetup(bixolonsetupsetting bixolonSetup) {
        BixolonSetup = bixolonSetup;
    }

    public printprefs getPrintPrefs() {
        return PrintPrefs;
    }

    public void setPrintPrefs(printprefs printPrefs) {
        PrintPrefs = printPrefs;
    }

    public boolean isPrintRasterMode() {
        return PrintRasterMode;
    }

    public void setPrintRasterMode(boolean printRasterMode) {
        PrintRasterMode = printRasterMode;
    }
}
