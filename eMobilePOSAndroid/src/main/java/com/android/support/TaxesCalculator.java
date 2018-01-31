package com.android.support;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Created by Guarionex on 2/3/2016.
 */
public class TaxesCalculator {

    public static BigDecimal calculateTax(BigDecimal taxableAmount, List<BigDecimal> rates) {
        BigDecimal totalTaxAmount = new BigDecimal(0);
        for (BigDecimal rate : rates) {
            BigDecimal taxAmount = taxableAmount
                    .multiply(rate
                            .divide(new BigDecimal(100)))
                    .setScale(6, RoundingMode.HALF_UP);
            totalTaxAmount = totalTaxAmount.add(Global.getRoundBigDecimal(Global.getRoundBigDecimal(taxAmount, 3), 2));
        }
        return totalTaxAmount;
    }

    public static BigDecimal calculateTax(BigDecimal taxableAmount, BigDecimal rate) {
        BigDecimal totalTaxAmount = new BigDecimal(0);
        BigDecimal taxAmount = taxableAmount
                .multiply(rate
                        .divide(new BigDecimal(100)))
                .setScale(6, RoundingMode.HALF_UP);
        totalTaxAmount = totalTaxAmount.add(Global.getRoundBigDecimal(Global.getRoundBigDecimal(taxAmount, 3), 2));

        return totalTaxAmount;
    }

    public static BigDecimal taxRounder(BigDecimal amount) {
        BigDecimal roundBigDecimal = Global.getRoundBigDecimal(Global.getRoundBigDecimal(amount, 3), 2);
        return roundBigDecimal;
    }
}
