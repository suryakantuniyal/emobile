package com.android.support;

import android.content.Context;

import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Guarionex on 2/3/2016.
 */
public class TaxesCalculator {

    public static BigDecimal calculateTax(BigDecimal taxableAmount, List<BigDecimal> rates) {
        BigDecimal totalTaxAmount = new BigDecimal(0);
        BigDecimal taxRateGlobal = new BigDecimal(0);
        for (BigDecimal rate : rates) {
            taxRateGlobal = taxRateGlobal.add(rate);
        }
        BigDecimal taxAmount = taxableAmount
                .multiply(taxRateGlobal.divide(new BigDecimal(100)))
                .setScale(2, RoundingMode.HALF_UP);
        totalTaxAmount = totalTaxAmount.add(Global.getRoundBigDecimal(Global.getRoundBigDecimal(taxAmount, 2), 2));
        return totalTaxAmount;
    }

    public static BigDecimal calculateTax(BigDecimal taxableAmount, BigDecimal rate) {
        BigDecimal totalTaxAmount = new BigDecimal(0);
        BigDecimal taxAmount = taxableAmount
                .multiply(rate
                        .divide(new BigDecimal(100)))
                .setScale(2, RoundingMode.HALF_UP);
        totalTaxAmount = totalTaxAmount.add(Global.getRoundBigDecimal(Global.getRoundBigDecimal(taxAmount, 2), 2));

        return totalTaxAmount;
    }

    public static BigDecimal taxRounder(BigDecimal amount) {
        BigDecimal roundBigDecimal = Global.getRoundBigDecimal(Global.getRoundBigDecimal(amount, 3), 2);
        return roundBigDecimal;
    }

    public static HashMap<String, String[]> getOrderTaxes(Context context, List<DataTaxes> taxes, Order order) {
        MyPreferences preferences = new MyPreferences(context);
        HashMap<String, String[]> prodTaxes = new HashMap<>();
        if (preferences.isRetailTaxes() && order.getOrderProducts() != null) {
            for (OrderProduct product : order.getOrderProducts()) {
                if (product.getTaxes() != null) {
                    for (Tax tax : product.getTaxes()) {
                        if (prodTaxes.containsKey(tax.getTaxRate())) {
                            BigDecimal taxAmount = new BigDecimal(prodTaxes.get(tax.getTaxRate())[1]);
                            taxAmount = taxAmount.add(TaxesCalculator.taxRounder(tax.getTaxAmount()));
                            String[] arr = new String[2];
                            arr[0] = tax.getTaxName();
                            arr[1] = String.valueOf(taxAmount);
                            prodTaxes.put(tax.getTaxRate(), arr);
                        } else {
                            BigDecimal taxAmount = TaxesCalculator.taxRounder(tax.getTaxAmount());
                            String[] arr = new String[2];
                            arr[0] = tax.getTaxName();
                            arr[1] = String.valueOf(taxAmount);
                            prodTaxes.put(tax.getTaxRate(), arr);
                        }
                    }
                }
            }
            Iterator it = prodTaxes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String[]> pair = (Map.Entry<String, String[]>) it.next();

                it.remove();
            }
        } else {
            if (taxes != null) {
                for (DataTaxes tax : taxes) {
                    BigDecimal taxAmount = new BigDecimal(0);
                    List<BigDecimal> rates = new ArrayList<>();
                    rates.add(new BigDecimal(tax.getTax_rate()));
                    //Applied here order.getOrderProducts null check by gurleen
                    if (order.getOrderProducts() != null) {
                        for (OrderProduct product : order.getOrderProducts()) {
                            taxAmount = taxAmount.add(TaxesCalculator.calculateTax(product.getProductPriceTaxableAmountCalculated(), rates));
                        }
                    }
                    String[] arr = new String[2];
                    arr[0] = tax.getTax_name();
                    arr[1] = String.valueOf(taxAmount);
                    prodTaxes.put(tax.getTax_rate(), arr);
                }
            }
        }
        return prodTaxes;
    }
}
