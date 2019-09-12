package com.android.support;

import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;

import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.GroupTax;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Guarionex on 2/3/2016.
 */
public class TaxesCalculator {

    public static BigDecimal calculateTax(BigDecimal taxableAmount, List<BigDecimal> rates) {
        BigDecimal totalTaxRate = new BigDecimal(0);
        for (BigDecimal rate : rates) {
            totalTaxRate = totalTaxRate.add(rate);
        }
        BigDecimal taxAmount = taxableAmount
                .multiply(totalTaxRate.divide(new BigDecimal(100)));
        return taxRounder(taxAmount);
    }

    public static BigDecimal calculateTax(BigDecimal taxableAmount, BigDecimal rate) {
        BigDecimal taxAmount = taxableAmount
                .multiply(rate.divide(new BigDecimal(100)));
        return taxRounder(taxAmount);
    }

    public static BigDecimal taxRounder(BigDecimal amount) {
        return Global.getRoundBigDecimal(Global.getRoundBigDecimal(amount, 3), 2);
    }

    public static void calculateOrderTaxesAmount(Order order) {
        List<DataTaxes> dataTaxes = order.getListOrderTaxes();
        if (dataTaxes != null) {
            BigDecimal taxTotal = BigDecimal.ZERO;
            for (DataTaxes dataTax : dataTaxes) {
                for (OrderProduct orderProduct : order.getOrderProducts()) {
                    taxTotal = taxTotal.add(
                            calculateTax(
                                    orderProduct.getProductPriceTaxableAmountCalculated(),
                                    new BigDecimal(dataTax.getTax_rate())));
                }
                dataTax.setTax_amount(String.valueOf(taxTotal));
                taxTotal = BigDecimal.ZERO;
            }
        }
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

    public static void setIvuTaxesFields(Order order,
                                         EditText tax1, EditText tax2, EditText tax3) {
        //set default taxes values to zero
        BigDecimal tax1Total = new BigDecimal(0.00);
        BigDecimal tax2Total = new BigDecimal(0.00);
        BigDecimal tax3Total = new BigDecimal(0.00);

        for (OrderProduct orderProduct : order.getOrderProducts()) {
            if (orderProduct.getTaxes() != null) {
                for (Tax tax : orderProduct.getTaxes()) {
                    switch (tax.getPrTax().toUpperCase()) {
                        case "TAX1":
                            tax1Total = tax1Total.add(tax.getTaxAmount());
                            break;
                        case "TAX2":
                            tax2Total = tax2Total.add(tax.getTaxAmount());
                            break;
                        case "TAX3":
                            tax3Total = tax3Total.add(tax.getTaxAmount());
                            break;
                    }
                }
            }
        }

        tax1.setText(Global.getCurrencyFormat(String.valueOf(tax1Total)));
        tax2.setText(Global.getCurrencyFormat(String.valueOf(tax2Total)));
        tax3.setText(Global.getCurrencyFormat(String.valueOf(tax3Total)));
    }

    public static void setIvuTaxesLabels(List<GroupTax> groupTaxRate, TextView tax1Lbl,
                                    TextView tax2Lbl, TextView tax3Lbl) {
        if (groupTaxRate != null) {
            Collections.sort(groupTaxRate, new Comparator<GroupTax>() {
                @Override
                public int compare(GroupTax o1, GroupTax o2) {
                    return o1.getPrTax().compareTo(o2.getPrTax());
                }
            });
            for (GroupTax groupTax : groupTaxRate) {
                switch (groupTax.getPrTax().toUpperCase()) {
                    case "TAX1":
                        tax1Lbl.setText(groupTax.getTaxName());
                        break;
                    case "TAX2":
                        tax2Lbl.setText(groupTax.getTaxName());
                        break;
                    case "TAX3":
                        tax3Lbl.setText(groupTax.getTaxName());
                        break;
                }
            }
        }
    }

    public static void setIvuTaxesFields(List<GroupTax> groupTaxRate, EditText subtotal,
                                      EditText tax1, EditText tax2, EditText tax3) {
        double subtotalDbl = Global.formatNumFromLocale(
                NumberUtils.cleanCurrencyFormatedNumber(subtotal));
        //set default taxes values to zero
        BigDecimal tax1Rate = new BigDecimal(0.00);
        BigDecimal tax2Rate = new BigDecimal(0.00);
        BigDecimal tax3Rate = new BigDecimal(0.00);

        for (GroupTax groupTax : groupTaxRate) {
            switch (groupTax.getPrTax().toUpperCase()) {
                case "TAX1":
                    tax1Rate = new BigDecimal(Double.parseDouble(groupTax.getTaxRate()));
                    break;
                case "TAX2":
                    tax2Rate = new BigDecimal(Double.parseDouble(groupTax.getTaxRate()));
                    break;
                case "TAX3":
                    tax3Rate = new BigDecimal(Double.parseDouble(groupTax.getTaxRate()));
                    break;
            }
        }

        BigDecimal tax1Dbl = new BigDecimal(subtotalDbl).multiply(tax1Rate);
        BigDecimal tax2Dbl = new BigDecimal(subtotalDbl).multiply(tax2Rate);
        BigDecimal tax3Dbl = new BigDecimal(subtotalDbl).multiply(tax3Rate);

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);
        tax1.setText(df.format(tax1Dbl.doubleValue()));
        tax2.setText(df.format(tax2Dbl.doubleValue()));
        tax3.setText(df.format(tax3Dbl.doubleValue()));
    }
}
