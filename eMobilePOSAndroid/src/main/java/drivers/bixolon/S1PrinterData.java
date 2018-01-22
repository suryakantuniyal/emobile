package drivers.bixolon;

import java.util.Date;

/**
 * Created by guarionex on 7/24/17.
 */

public class S1PrinterData {
    private int cashierNumber;
    private double totalDailySales;
    private int lastInvoiceNumber;
    private int quantityOfInvoicesToday;
    private int lastCNNumber;
    private int quantityOfCNToday;
    private int lastDebitNoteNumber;
    private int quantityOfDebitNoteToday;
    private int numberNonFiscalDocuments;
    private int quantityNonFiscalDocuments;
    private int dailyClosureCounter;
    private int auditReportsCounter;
    private String RUC;
    private String DV;
    private String registeredMachineNumber;
    private Date currentPrinterDateTime;

    public int getCashierNumber() {
        return cashierNumber;
    }

    public void setCashierNumber(int cashierNumber) {
        this.cashierNumber = cashierNumber;
    }

    public double getTotalDailySales() {
        return totalDailySales;
    }

    public void setTotalDailySales(double totalDailySales) {
        this.totalDailySales = totalDailySales;
    }

    public int getLastInvoiceNumber() {
        return lastInvoiceNumber;
    }

    public void setLastInvoiceNumber(int lastInvoiceNumber) {
        this.lastInvoiceNumber = lastInvoiceNumber;
    }

    public int getQuantityOfInvoicesToday() {
        return quantityOfInvoicesToday;
    }

    public void setQuantityOfInvoicesToday(int quantityOfInvoicesToday) {
        this.quantityOfInvoicesToday = quantityOfInvoicesToday;
    }

    public int getLastCNNumber() {
        return lastCNNumber;
    }

    public void setLastCNNumber(int lastCNNumber) {
        this.lastCNNumber = lastCNNumber;
    }

    public int getQuantityOfCNToday() {
        return quantityOfCNToday;
    }

    public void setQuantityOfCNToday(int quantityOfCNToday) {
        this.quantityOfCNToday = quantityOfCNToday;
    }

    public int getLastDebitNoteNumber() {
        return lastDebitNoteNumber;
    }

    public void setLastDebitNoteNumber(int lastDebitNoteNumber) {
        this.lastDebitNoteNumber = lastDebitNoteNumber;
    }

    public int getQuantityOfDebitNoteToday() {
        return quantityOfDebitNoteToday;
    }

    public void setQuantityOfDebitNoteToday(int quantityOfDebitNoteToday) {
        this.quantityOfDebitNoteToday = quantityOfDebitNoteToday;
    }

    public int getNumberNonFiscalDocuments() {
        return numberNonFiscalDocuments;
    }

    public void setNumberNonFiscalDocuments(int numberNonFiscalDocuments) {
        this.numberNonFiscalDocuments = numberNonFiscalDocuments;
    }

    public int getQuantityNonFiscalDocuments() {
        return quantityNonFiscalDocuments;
    }

    public void setQuantityNonFiscalDocuments(int quantityNonFiscalDocuments) {
        this.quantityNonFiscalDocuments = quantityNonFiscalDocuments;
    }

    public int getDailyClosureCounter() {
        return dailyClosureCounter;
    }

    public void setDailyClosureCounter(int dailyClosureCounter) {
        this.dailyClosureCounter = dailyClosureCounter;
    }

    public int getAuditReportsCounter() {
        return auditReportsCounter;
    }

    public void setAuditReportsCounter(int auditReportsCounter) {
        this.auditReportsCounter = auditReportsCounter;
    }

    public String getRUC() {
        return RUC;
    }

    public void setRUC(String RUC) {
        this.RUC = RUC;
    }

    public String getDV() {
        return DV;
    }

    public void setDV(String DV) {
        this.DV = DV;
    }

    public String getRegisteredMachineNumber() {
        return registeredMachineNumber;
    }

    public void setRegisteredMachineNumber(String registeredMachineNumber) {
        this.registeredMachineNumber = registeredMachineNumber;
    }

    public Date getCurrentPrinterDateTime() {
        return currentPrinterDateTime;
    }

    public void setCurrentPrinterDateTime(Date currentPrinterDateTime) {
        this.currentPrinterDateTime = currentPrinterDateTime;
    }
}
