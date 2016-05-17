package com.android.support;

import android.app.Activity;

import com.android.database.TaxesHandler;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.Discount;
import com.android.emobilepos.models.Order;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.ordering.OrderingMain_FA;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guarionex on 2/24/2016.
 */
public class OrderCalculator {
    private final OrderProduct product;
    private final Discount discount;
    private List<DataTaxes> orderTaxes;
    private MyPreferences myPref;
    private String taxId;
    private boolean isVAT;
    private Activity activity;
    Tax tax;


    public OrderCalculator(Activity activity,  OrderProduct product, String taxId, Tax tax, Discount discount, List<DataTaxes> orderTaxes) {
        this.product = product;
        this.discount = discount;
        this.orderTaxes = orderTaxes;
        myPref = new MyPreferences(activity);
        this.taxId = taxId;
        isVAT = myPref.getIsVAT();
        this.activity = activity;
        this.tax = tax;
        calculateTaxes(product);
    }


    private void calculateTaxes(OrderProduct product) {
        TaxesHandler taxHandler = new TaxesHandler(activity);
        String taxRate = "0";
        String prod_taxId = "";
        BigDecimal subtotal = new BigDecimal(product.itemSubtotal);
        BigDecimal prodQty = new BigDecimal(product.ordprod_qty);
        if (myPref.getPreferences(MyPreferences.pref_retail_taxes)) {
            if (!taxId.isEmpty()) {
                taxRate = taxHandler.getTaxRate(taxId, product.prod_taxtype, Double.parseDouble(product.overwrite_price));
                prod_taxId = product.prod_taxtype;
            } else {
                taxRate = taxHandler.getTaxRate(product.prod_taxcode, product.prod_taxtype, Double.parseDouble(product.overwrite_price));
                prod_taxId = product.prod_taxcode;
            }
        } else {
            if (!taxId.isEmpty() && tax != null) {
                taxRate = tax.getTaxRate();
                prod_taxId = tax.getTaxId();
            }
        }

        if (isVAT) {
            if (product.prod_istaxable.equals("1")) {
                if (product.prod_price_updated.equals("0")) {
                    BigDecimal curr_prod_price = new BigDecimal(product.overwrite_price);
                    BigDecimal new_prod_price = getProductPrice(curr_prod_price,
                            new BigDecimal(taxRate).divide(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP));
                    new_prod_price = new_prod_price.setScale(4, RoundingMode.HALF_UP);

                    subtotal = new_prod_price.multiply(prodQty).setScale(2, RoundingMode.HALF_UP);

                    product.price_vat_exclusive = new_prod_price.setScale(2, RoundingMode.HALF_UP)
                            .toString();
                    product.prod_price_updated = "1";
                    BigDecimal disc;
                    if (product.discount_is_fixed.equals("0")) {
                        BigDecimal val = subtotal
                                .multiply(Global.getBigDecimalNum(product.disAmount))
                                .setScale(4, RoundingMode.HALF_UP);
                        disc = val.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    } else {
                        disc = new BigDecimal(product.disAmount);
                    }

                    product.discount_value = Global.getRoundBigDecimal(disc);
                    product.disTotal = Global.getRoundBigDecimal(disc);

                    product.itemTotalVatExclusive = Global
                            .getRoundBigDecimal(subtotal.subtract(disc));
                }
                if (prodQty.compareTo(new BigDecimal("1")) == 1) {
                    subtotal = new BigDecimal(product.price_vat_exclusive).setScale(2,
                            RoundingMode.HALF_UP);
                } else {
                    subtotal = new BigDecimal(product.price_vat_exclusive).multiply(prodQty)
                            .setScale(2, RoundingMode.HALF_UP);
                }
            } else
                product.itemTotalVatExclusive = subtotal.toString();
        }
        BigDecimal taxableSubtotal;
        BigDecimal taxAmount=new BigDecimal(0);
        if (product.prod_istaxable.equals("1") &&
                (product.item_void.isEmpty() || product.item_void.equals("0"))) {
            BigDecimal taxableDueAmount = subtotal;

            if (product.discount_is_taxable.equals("1")) {
                BigDecimal taxRateScaled = new BigDecimal(taxRate).divide(new BigDecimal("100")).setScale(4,
                        RoundingMode.HALF_UP);
                subtotal = subtotal.abs().subtract(new BigDecimal(product.discount_value).abs());
                if (product.isReturned && OrderingMain_FA.mTransType != Global.TransactionType.RETURN) {
                    subtotal = subtotal.negate();
                }
                taxAmount = subtotal.multiply(taxRateScaled);
                BigDecimal discountRate = Global.getBigDecimalNum(discount.getProductPrice());


                if (discount != null) {
                    if (discount.getProductDiscountType().equals("Fixed")) {
                        if (discountRate.compareTo(subtotal) == -1) {
                            taxableSubtotal = subtotal.subtract(discountRate)
                                    .multiply(taxRateScaled)
                                    .setScale(4, RoundingMode.HALF_UP);
                            if (discount.getTaxCodeIsTaxable().equals("1")) {
                                subtotal = subtotal.subtract(discountRate);
                            }
                        } else {
                            BigDecimal discountAmount = subtotal;
                            taxableSubtotal = new BigDecimal("0");
                            subtotal = new BigDecimal("0");
                        }
                    } else {
                        BigDecimal discountAmount = subtotal.multiply(discountRate).setScale(4, RoundingMode.HALF_UP);
                        taxableSubtotal = subtotal.subtract(discountAmount).multiply(taxRateScaled)
                                .setScale(4, RoundingMode.HALF_UP);
                        if (discount.getTaxCodeIsTaxable().equals("1")) {
                            subtotal = subtotal.subtract(discountAmount);
                        }
                    }
                } else {
                    taxableSubtotal = subtotal.multiply(taxRateScaled).setScale(4, RoundingMode.HALF_UP);
                }
            }else {
                BigDecimal taxRateScaled = new BigDecimal(taxRate).divide(new BigDecimal("100")).setScale(4,
                        RoundingMode.HALF_UP);
                taxAmount = subtotal.multiply(taxRateScaled).setScale(2, RoundingMode.HALF_UP);

                BigDecimal discountRate = Global.getBigDecimalNum(discount.getProductPrice());
                if (discount != null) {
                    if (discount.getProductDiscountType().equals("Fixed")) {
                        if (discountRate.compareTo(subtotal) == -1) {
                            taxableSubtotal = subtotal.subtract(discountRate)
                                    .multiply(taxRateScaled).setScale(4, RoundingMode.HALF_UP);

                            if (discount.getTaxCodeIsTaxable().equals("1")) {
                                subtotal = subtotal.subtract(discountRate);
                            }
                        } else {
                            taxableSubtotal = new BigDecimal("0");
                            subtotal = new BigDecimal(0);
                        }
                    } else {
                        BigDecimal discountAmount = subtotal.multiply(discountRate).setScale(4, RoundingMode.HALF_UP);
                        taxableSubtotal = subtotal.subtract(discountAmount).multiply(taxRateScaled).setScale(4, RoundingMode.HALF_UP);
                        if (discount.getTaxCodeIsTaxable().equals("1")) {
                            subtotal = subtotal.subtract(discountAmount);
                        }
                    }
                } else {
                   taxableSubtotal = subtotal.multiply(taxRateScaled).setScale(4, RoundingMode.HALF_UP);
                }

            }

            if (myPref.getPreferences(MyPreferences.pref_retail_taxes)) {
                calculateGlobalTax(subtotal, prodQty, isVAT, new BigDecimal(taxRate));
            } else {
                calculateGlobalTax(subtotal, prodQty, isVAT, new BigDecimal(taxRate));
            }
        }
        if (taxAmount.compareTo(new BigDecimal("0")) < -1)
            taxAmount = new BigDecimal(0);

        product.prod_taxValue = taxAmount.toString();
        product.prod_taxId = prod_taxId;
    }

    private BigDecimal getProductPrice(BigDecimal prod_with_tax_price, BigDecimal tax) {
        BigDecimal denom = new BigDecimal(1).add(tax);
        return prod_with_tax_price.divide(denom, 2, RoundingMode.HALF_UP);
    }

    private void calculateGlobalTax(BigDecimal subtotal, BigDecimal qty, boolean isVat, BigDecimal taxRate) {
        int size = orderTaxes.size();
        String val = "0";
        BigDecimal taxRateScaled = new BigDecimal("0");
        BigDecimal total_tax = new BigDecimal("0");
        BigDecimal tax_amount;
        for (int j = 0; j < size; j++) {
            val = orderTaxes.get(j).getTax_rate();
            if (val == null || val.isEmpty())
                val = "0";
            taxRateScaled = taxRate.divide(new BigDecimal("100")).setScale(4,
                    RoundingMode.HALF_UP);
            tax_amount = subtotal.multiply(taxRateScaled).setScale(4, RoundingMode.HALF_UP);

            total_tax = total_tax.add(tax_amount);
            DataTaxes tempTaxes = orderTaxes.get(j);
            BigDecimal orderTaxesTotal = tempTaxes.getTax_amount().isEmpty() ? new BigDecimal(0.00) : new BigDecimal(tempTaxes.getTax_amount());
            if (subtotal.compareTo(new BigDecimal("0.00")) != 0) {
                if (isVat)
                    orderTaxesTotal = orderTaxesTotal.add(tax_amount.multiply(qty.abs()).setScale(2, RoundingMode.HALF_UP));
                else
                    orderTaxesTotal = orderTaxesTotal.add(tax_amount).setScale(4, RoundingMode.HALF_UP);
            }
            tempTaxes.setTax_amount(orderTaxesTotal.toString());
        }

        if (isVat)
            total_tax = total_tax.setScale(2, RoundingMode.HALF_UP).multiply(qty.abs());
    }

}
