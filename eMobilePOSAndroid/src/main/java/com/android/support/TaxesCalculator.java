package com.android.support;

import android.app.Activity;

import com.android.database.ProductsHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.OrderProduct;
import com.android.emobilepos.ordering.OrderingMain_FA;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Guarionex on 2/3/2016.
 */
public class TaxesCalculator {
    private final Global global;
    private BigDecimal discount_rate;
    private BigDecimal discount_amount;
    private String discountID;
    private BigDecimal taxableSubtotal = new BigDecimal("0");
    List<HashMap<String, String>> listMapTaxes;
    private Activity activity;
    private MyPreferences myPref;
    private final OrderProduct orderProduct;
    private final String taxID;
    private final int taxSelected;
    private BigDecimal taxableAmount = new BigDecimal(0.00);
    private String[] discountSelected;
    private BigDecimal discountable_sub_total;
    private BigDecimal itemsDiscountTotal;
    private BigDecimal taxableDueAmount = new BigDecimal(0.00);

    public TaxesCalculator(Activity activity, MyPreferences myPref, OrderProduct orderProduct, String taxID, int taxSelected, String[] discount, BigDecimal discountable_sub_total, BigDecimal itemsDiscountTotal, List<HashMap<String, String>> listMapTaxes) {
        this.listMapTaxes = listMapTaxes;
        this.setDiscountable_sub_total(discountable_sub_total);
        this.setItemsDiscountTotal(itemsDiscountTotal);
        global = (Global) activity.getApplication();
        this.activity = activity;
        this.myPref = myPref;
        this.orderProduct = orderProduct;
        this.taxID = taxID;
        this.taxSelected = taxSelected;
        this.discountSelected = discount;
        setDiscountValue();
        calculateTaxes();
    }

    public void calculateTaxes() {
        TaxesHandler taxHandler = new TaxesHandler(activity);
        ProductsHandler productsHandler = new ProductsHandler(activity);
        List<String[]> discountList = productsHandler.getDiscounts();
        String taxAmount = "0.00";
        String prod_taxId = "";
        List<String[]> taxList = taxHandler.getTaxes();

        if (myPref.getPreferences(MyPreferences.pref_retail_taxes)) {
            if (!taxID.isEmpty()) {
                taxAmount = Global.formatNumToLocale(
                        Double.parseDouble(taxHandler.getTaxRate(taxID, orderProduct.prod_taxtype,
                                Double.parseDouble(orderProduct.overwrite_price))));
                prod_taxId = orderProduct.prod_taxtype;
            } else {
                taxAmount = Global.formatNumToLocale(Double.parseDouble(taxHandler.getTaxRate(
                        orderProduct.prod_taxcode, orderProduct.prod_taxtype,
                        Double.parseDouble(orderProduct.overwrite_price))));
                prod_taxId = orderProduct.prod_taxcode;
            }
        } else {
            if (!taxID.isEmpty()) {
                taxAmount = taxList.get(taxSelected - 1)[2];
                prod_taxId = taxID;
            }
        }

        BigDecimal tempSubTotal = new BigDecimal(orderProduct.itemSubtotal);
        BigDecimal prodQty = new BigDecimal(orderProduct.ordprod_qty);
        BigDecimal _temp_subtotal = tempSubTotal;
        boolean isVAT = myPref.getIsVAT();
        if (isVAT) {
            if (orderProduct.prod_istaxable.equals("1")) {
                if (orderProduct.prod_price_updated.equals("0")) {
                    BigDecimal _curr_prod_price = new BigDecimal(orderProduct.overwrite_price);
                    BigDecimal _new_prod_price = getProductPrice(_curr_prod_price,
                            new BigDecimal(taxAmount).divide(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP));
                    _new_prod_price = _new_prod_price.setScale(4, RoundingMode.HALF_UP);
                    tempSubTotal = _new_prod_price.multiply(prodQty).setScale(2, RoundingMode.HALF_UP);

                    orderProduct.price_vat_exclusive = _new_prod_price.setScale(2, RoundingMode.HALF_UP)
                            .toString();
                    orderProduct.prod_price_updated = "1";

                    BigDecimal disc;
                    if (orderProduct.discount_is_fixed.equals("0")) {
                        BigDecimal val = tempSubTotal
                                .multiply(Global.getBigDecimalNum(orderProduct.disAmount))
                                .setScale(4, RoundingMode.HALF_UP);
                        disc = val.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    } else {
                        disc = new BigDecimal(orderProduct.disAmount);
                    }

                    orderProduct.discount_value = Global.getRoundBigDecimal(disc);
                    orderProduct.disTotal = Global.getRoundBigDecimal(disc);

                    orderProduct.itemTotalVatExclusive = Global
                            .getRoundBigDecimal(tempSubTotal.subtract(disc));
                }

                if (prodQty.compareTo(new BigDecimal("1")) == 1) {
                    _temp_subtotal = new BigDecimal(orderProduct.price_vat_exclusive).setScale(2,
                            RoundingMode.HALF_UP);
                } else {

                    tempSubTotal = new BigDecimal(orderProduct.price_vat_exclusive).multiply(prodQty)
                            .setScale(2, RoundingMode.HALF_UP);
                    _temp_subtotal = tempSubTotal;
                }
            } else
                orderProduct.itemTotalVatExclusive = _temp_subtotal.toString();
        }
        BigDecimal tempTaxTotal = new BigDecimal("0");
        String taxTotal = "0";

        if (orderProduct.prod_istaxable.equals("1") &&
                (orderProduct.item_void.isEmpty() || orderProduct.item_void.equals("0"))) {
            setTaxableDueAmount(getTaxableDueAmount().add(tempSubTotal));

            if (orderProduct.discount_is_taxable.equals("1")) {
                BigDecimal temp = new BigDecimal(taxAmount).divide(new BigDecimal("100")).setScale(4,
                        RoundingMode.HALF_UP);
                tempSubTotal = tempSubTotal.abs().subtract(new BigDecimal(orderProduct.discount_value).abs());
                if (orderProduct.isReturned && OrderingMain_FA.mTransType != Global.TransactionType.RETURN) {
                    tempSubTotal = tempSubTotal.negate();
                }
                BigDecimal tax1 = tempSubTotal.multiply(temp);
                tempTaxTotal = tax1;
                taxTotal = tax1.toString();
                _temp_subtotal = tempSubTotal;
                if (discountSelected.length > 0) {
                    if (discountSelected[1].equals("Fixed")) {
                        if (getDiscount_rate().compareTo(tempSubTotal) == -1) {
                            setTaxableSubtotal(getTaxableSubtotal().add(tempSubTotal.subtract(getDiscount_rate()).multiply(temp)
                                    .setScale(4, RoundingMode.HALF_UP)));
                            // discount_rate = new BigDecimal("0");
                            if (discountSelected[3].equals("1")) {
                                _temp_subtotal = _temp_subtotal.subtract(getDiscount_rate());
                            }
                        } else {
                            setDiscount_amount(tempSubTotal);
                            setTaxableSubtotal(new BigDecimal("0"));
                            _temp_subtotal = getTaxableSubtotal();
                        }
                    } else {
                        BigDecimal temp2 = tempSubTotal.multiply(getDiscount_rate()).setScale(4, RoundingMode.HALF_UP);
                        setTaxableSubtotal(getTaxableSubtotal()
                                .add(tempSubTotal.subtract(temp2).multiply(temp).setScale(4, RoundingMode.HALF_UP)));
                        if (discountSelected[3].equals("1")) {
                            _temp_subtotal = _temp_subtotal.subtract(temp2);
                        }
                    }
                } else {
                    setTaxableSubtotal(getTaxableSubtotal()
                            .add(tempSubTotal.multiply(temp).setScale(4, RoundingMode.HALF_UP)));
                }
            } else {
                BigDecimal temp = new BigDecimal(taxAmount).divide(new BigDecimal("100")).setScale(4,
                        RoundingMode.HALF_UP);
                BigDecimal tax1 = tempSubTotal.multiply(temp).setScale(2, RoundingMode.HALF_UP);
                tempTaxTotal = tax1;
                taxTotal = tax1.toString();

                if (discountSelected.length > 0) {
                    if (discountSelected[1].equals("Fixed")) {
                        if (getDiscount_rate().compareTo(tempSubTotal) == -1) {
                            setTaxableSubtotal(getTaxableSubtotal().add(tempSubTotal.subtract(getDiscount_rate())
                                    .multiply(temp).setScale(4, RoundingMode.HALF_UP)));
                            // discount_rate = new BigDecimal("0");
                            if (discountSelected[3].equals("1")) {
                                _temp_subtotal = tempSubTotal.subtract(getDiscount_rate());
                            }
                        } else {
                            // discount_amount = tempSubTotal;
                            setTaxableSubtotal(new BigDecimal("0"));
                            _temp_subtotal = getTaxableSubtotal();
                        }
                    } else {
                        BigDecimal temp2 = tempSubTotal.multiply(getDiscount_rate()).setScale(4, RoundingMode.HALF_UP);
                        setTaxableSubtotal(getTaxableSubtotal()
                                .add(tempSubTotal.subtract(temp2).multiply(temp).setScale(4, RoundingMode.HALF_UP)));
                        if (discountSelected[3].equals("1")) {
                            _temp_subtotal = tempSubTotal.subtract(temp2);
                        }
                    }
                } else {
                    setTaxableSubtotal(getTaxableSubtotal()
                            .add(tempSubTotal.multiply(temp).setScale(4, RoundingMode.HALF_UP)));
                }
//                }
            }

            if (myPref.getPreferences(MyPreferences.pref_retail_taxes)) {
                calculateRetailGlobalTax(_temp_subtotal, taxAmount, prodQty, isVAT);
            } else {
                calculateGlobalTax(_temp_subtotal, prodQty, isVAT);
            }

        }

        if (tempTaxTotal.compareTo(new BigDecimal("0")) < -1)
            taxTotal = Double.toString(0.0);

        orderProduct.prod_taxValue = taxTotal;
        orderProduct.prod_taxId = prod_taxId;
    }

    private BigDecimal getProductPrice(BigDecimal prod_with_tax_price, BigDecimal tax) {
        BigDecimal denom = new BigDecimal(1).add(tax);
        return prod_with_tax_price.divide(denom, 2, RoundingMode.HALF_UP);
    }

    private void setDiscountValue() {
        DecimalFormat frmt = new DecimalFormat("0.00");
//        if (position == 0) {
//            discount_rate = new BigDecimal("0");
//            discount_amount = new BigDecimal("0");
//            discountID = "";
//        } else
        if (discountSelected != null && discountSelected.length > 0) {
            setDiscountID(discountSelected[4]);
            if (discountSelected[1].equals("Fixed")) {
                setDiscount_rate(Global.getBigDecimalNum(discountSelected[2]));
                setDiscount_amount(Global.getBigDecimalNum(discountSelected[2]));

            } else {
                setDiscount_rate(Global.getBigDecimalNum(discountSelected[2])
                        .divide(new BigDecimal("100")));
                BigDecimal total = getDiscountable_sub_total().subtract(getItemsDiscountTotal());
                setDiscount_amount(total.multiply(getDiscount_rate()).setScale(2, RoundingMode.HALF_UP));
            }
        } else {
            discount_rate = new BigDecimal("0");
            discount_amount = new BigDecimal("0");
            discountID = "";
        }

        Global.discountAmount = getDiscount_amount();
    }

    private void calculateGlobalTax(BigDecimal _subtotal, BigDecimal qty, boolean isVat) {
        int size = listMapTaxes.size();

        List<BigDecimal> listOrderTaxesTotal = new ArrayList<BigDecimal>();

        String val = "0";
        BigDecimal temp = new BigDecimal("0");
        BigDecimal _total_tax = new BigDecimal("0");
        for (int j = 0; j < size; j++) {
            val = listMapTaxes.get(j).get("tax_rate");
            if (val == null || val.isEmpty())
                val = "0";
            temp = new BigDecimal(listMapTaxes.get(j).get("tax_rate")).divide(new BigDecimal("100")).setScale(4,
                    RoundingMode.HALF_UP);
            BigDecimal tax_amount = _subtotal.multiply(temp).setScale(4, RoundingMode.HALF_UP);

            _total_tax = _total_tax.add(tax_amount);
            DataTaxes tempTaxes = global.listOrderTaxes.get(j);
            BigDecimal orderTaxesTotal = tempTaxes.getTax_amount().isEmpty() ? new BigDecimal(0.00) : new BigDecimal(tempTaxes.getTax_amount());
            if (_subtotal.compareTo(new BigDecimal("0.00")) != 0) {
                if (isVat)
                    orderTaxesTotal = orderTaxesTotal.add(tax_amount.multiply(qty.abs()).setScale(2, RoundingMode.HALF_UP));
                else
                    orderTaxesTotal = orderTaxesTotal.add(tax_amount).setScale(4, RoundingMode.HALF_UP);
            }
            tempTaxes.setTax_amount(orderTaxesTotal.toString());
            global.listOrderTaxes.set(j, tempTaxes);
        }

        if (isVat)
            _total_tax = _total_tax.setScale(2, RoundingMode.HALF_UP).multiply(qty.abs());
        setTaxableAmount(getTaxableAmount().add(_total_tax).setScale(4, RoundingMode.HALF_UP));
    }

    private void calculateRetailGlobalTax(BigDecimal _sub_total, String tax_rate, BigDecimal qty, boolean isVat) {
        int size = listMapTaxes.size();
        String val = "0";
        List<BigDecimal> listOrderTaxesTotal = new ArrayList<BigDecimal>();

        BigDecimal temp = new BigDecimal("0");
        BigDecimal _total_tax = new BigDecimal("0");
        for (int j = 0; j < size; j++) {
            val = listMapTaxes.get(j).get("tax_rate");
            if (val == null || val.isEmpty())
                val = "0";
            temp = new BigDecimal(tax_rate).divide(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);
            BigDecimal tax_amount = _sub_total.multiply(temp).setScale(4, RoundingMode.HALF_UP);

            _total_tax = _total_tax.add(tax_amount);
            DataTaxes tempTaxes = global.listOrderTaxes.get(j);
            BigDecimal orderTaxesTotal = tempTaxes.getTax_amount().isEmpty() ? new BigDecimal(0.00) : new BigDecimal(tempTaxes.getTax_amount());
            if (_sub_total.compareTo(new BigDecimal("0.00")) != 0) {
                if (isVat)
                    orderTaxesTotal = orderTaxesTotal.add(tax_amount.multiply(qty)).setScale(2, RoundingMode.HALF_UP);
                else
                    orderTaxesTotal = orderTaxesTotal.add(tax_amount).setScale(4, RoundingMode.HALF_UP);

            }
            tempTaxes.setTax_amount(orderTaxesTotal.toString());
            global.listOrderTaxes.set(j, tempTaxes);
        }

        if (isVat)
            _total_tax = _total_tax.setScale(2, RoundingMode.HALF_UP).multiply(qty);
        setTaxableAmount(getTaxableAmount().add(_total_tax).setScale(4, RoundingMode.HALF_UP));
    }

    public BigDecimal getDiscount_rate() {
        return discount_rate;
    }

    public void setDiscount_rate(BigDecimal discount_rate) {
        this.discount_rate = discount_rate;
    }

    public BigDecimal getDiscount_amount() {
        return discount_amount;
    }

    public void setDiscount_amount(BigDecimal discount_amount) {
        this.discount_amount = discount_amount;
    }

    public String getDiscountID() {
        return discountID;
    }

    public void setDiscountID(String discountID) {
        this.discountID = discountID;
    }

    public BigDecimal getTaxableSubtotal() {
        return taxableSubtotal;
    }

    public void setTaxableSubtotal(BigDecimal taxableSubtotal) {
        this.taxableSubtotal = taxableSubtotal;
    }

    public BigDecimal getTaxableAmount() {
        return taxableAmount;
    }

    public void setTaxableAmount(BigDecimal taxableAmount) {
        this.taxableAmount = taxableAmount;
    }

    public String[] getDiscountSelected() {
        return discountSelected;
    }

    public void setDiscountSelected(String[] discount) {
        this.discountSelected = discount;
    }

    public BigDecimal getDiscountable_sub_total() {
        return discountable_sub_total;
    }

    public void setDiscountable_sub_total(BigDecimal discountable_sub_total) {
        this.discountable_sub_total = discountable_sub_total;
    }

    public BigDecimal getItemsDiscountTotal() {
        return itemsDiscountTotal;
    }

    public void setItemsDiscountTotal(BigDecimal itemsDiscountTotal) {
        this.itemsDiscountTotal = itemsDiscountTotal;
    }

    public BigDecimal getTaxableDueAmount() {
        return taxableDueAmount;
    }

    public void setTaxableDueAmount(BigDecimal taxableDueAmount) {
        this.taxableDueAmount = taxableDueAmount;
    }
}
