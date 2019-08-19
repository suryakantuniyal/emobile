package com.android.emobilepos.models;

/**
 * Created by Luis Camayd on 8/19/2019.
 */
public class Report {
    private String specialHeader;
    private String header;
    private String summary;
    private String arTransactions;
    private String totalsByShifts;
    private String totalsByTypes;
    private String itemsSold;
    private String departmentSales;
    private String departmentReturns;
    private String payments;
    private String voids;
    private String refunds;
    private String itemsReturned;
    private String footer;
    private String specialFooter;
    private String eNablerWebsite;


    public String getSpecialHeader() {
        return specialHeader;
    }

    public void setSpecialHeader(String specialHeader) {
        this.specialHeader = specialHeader;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getArTransactions() {
        return arTransactions;
    }

    public void setArTransactions(String arTransactions) {
        this.arTransactions = arTransactions;
    }

    public String getTotalsByShifts() {
        return totalsByShifts;
    }

    public void setTotalsByShifts(String totalsByShifts) {
        this.totalsByShifts = totalsByShifts;
    }

    public String getTotalsByTypes() {
        return totalsByTypes;
    }

    public void setTotalsByTypes(String totalsByTypes) {
        this.totalsByTypes = totalsByTypes;
    }

    public String getItemsSold() {
        return itemsSold;
    }

    public void setItemsSold(String itemsSold) {
        this.itemsSold = itemsSold;
    }

    public String getDepartmentSales() {
        return departmentSales;
    }

    public void setDepartmentSales(String departmentSales) {
        this.departmentSales = departmentSales;
    }

    public String getDepartmentReturns() {
        return departmentReturns;
    }

    public void setDepartmentReturns(String departmentReturns) {
        this.departmentReturns = departmentReturns;
    }

    public String getPayments() {
        return payments;
    }

    public void setPayments(String payments) {
        this.payments = payments;
    }

    public String getVoids() {
        return voids;
    }

    public void setVoids(String voids) {
        this.voids = voids;
    }

    public String getRefunds() {
        return refunds;
    }

    public void setRefunds(String refunds) {
        this.refunds = refunds;
    }

    public String getItemsReturned() {
        return itemsReturned;
    }

    public void setItemsReturned(String itemsReturned) {
        this.itemsReturned = itemsReturned;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public String getSpecialFooter() {
        return specialFooter;
    }

    public void setSpecialFooter(String specialFooter) {
        this.specialFooter = specialFooter;
    }

    public String getEnablerWebsite() {
        return eNablerWebsite;
    }

    public void setEnablerWebsite(String eNablerWebsite) {
        this.eNablerWebsite = eNablerWebsite;
    }
}
