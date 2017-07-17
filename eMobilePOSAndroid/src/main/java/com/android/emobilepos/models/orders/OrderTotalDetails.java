package com.android.emobilepos.models.orders;

import java.math.BigDecimal;

/**
 * Created by guarionex on 3/10/17.
 */

public class OrderTotalDetails {
    private BigDecimal subtotal = new BigDecimal(0);
    private BigDecimal globalDiscount = new BigDecimal(0);
    private BigDecimal tax = new BigDecimal(0);
    private BigDecimal granTotal = new BigDecimal(0);
    private BigDecimal pointBalance = new BigDecimal(0);
    private BigDecimal pointsSubTotal = new BigDecimal(0);
    private BigDecimal pointsInUse = new BigDecimal(0);
    private BigDecimal pointsAvailable = new BigDecimal(0);
    private BigDecimal pointsAcumulable = new BigDecimal(0);

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getGlobalDiscount() {
        return globalDiscount;
    }

    public void setGlobalDiscount(BigDecimal globalDiscount) {
        this.globalDiscount = globalDiscount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getGranTotal() {
        return granTotal;
    }

    public void setGranTotal(BigDecimal granTotal) {
        this.granTotal = granTotal;
    }

    public BigDecimal getPointBalance() {
        return pointBalance;
    }

    public void setPointBalance(BigDecimal pointBalance) {
        this.pointBalance = pointBalance;
    }

    public BigDecimal getPointsSubTotal() {
        return pointsSubTotal;
    }

    public void setPointsSubTotal(BigDecimal pointsSubTotal) {
        this.pointsSubTotal = pointsSubTotal;
    }

    public BigDecimal getPointsInUse() {
        return pointsInUse;
    }

    public void setPointsInUse(BigDecimal pointsInUse) {
        this.pointsInUse = pointsInUse;
    }

    public BigDecimal getPointsAvailable() {
        return pointsAvailable;
    }

    public void setPointsAvailable(BigDecimal pointsAvailable) {
        this.pointsAvailable = pointsAvailable;
    }

    public BigDecimal getPointsAcumulable() {
        return pointsAcumulable;
    }

    public void setPointsAcumulable(BigDecimal pointsAcumulable) {
        this.pointsAcumulable = pointsAcumulable;
    }
}
