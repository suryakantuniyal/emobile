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
}
