package com.android.support;

import android.app.Activity;
import android.content.Context;

import com.android.dao.AssignEmployeeDAO;
import com.android.database.TaxesGroupHandler;
import com.android.database.TaxesHandler;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.Discount;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.orders.Order;
import com.android.emobilepos.models.orders.OrderProduct;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.ordering.OrderLoyalty_FR;
import com.android.emobilepos.ordering.OrderRewards_FR;
import com.android.emobilepos.ordering.OrderingMain_FA;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final Tax taxSelected;
    private BigDecimal taxableAmount = new BigDecimal(0.00);
    private Discount discountSelected;
    private BigDecimal discountable_sub_total;
    private BigDecimal itemsDiscountTotal;
    private BigDecimal taxableDueAmount = new BigDecimal(0.00);
    AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee(false);
    private ArrayList<DataTaxes> listOrderTaxes;

    public TaxesCalculator(Activity activity, OrderProduct orderProduct, String taxID, Tax taxSelected,
                           Discount discount, BigDecimal discountable_sub_total, BigDecimal itemsDiscountTotal) {
        this.setDiscountable_sub_total(discountable_sub_total);
        this.setItemsDiscountTotal(itemsDiscountTotal);
        global = (Global) activity.getApplication();
        this.activity = activity;
        this.myPref = new MyPreferences(activity);

        this.orderProduct = orderProduct;
        this.taxID = taxID;
        this.taxSelected = taxSelected;
        this.discountSelected = discount;
        setTaxValue();
        setupTaxesHolder();
        setDiscountValue();
        calculateTaxes();
    }

    public void calculateTaxes() {
        TaxesHandler taxHandler = new TaxesHandler(activity);
        String taxAmount = "0.00";
        String prod_taxId = "";

        if (myPref.isRetailTaxes()) {
            if (!taxID.isEmpty()) {
                taxAmount = Global.formatNumToLocale(
                        Double.parseDouble(taxHandler.getTaxRate(taxID, orderProduct.getTax_type(),
                                Global.getBigDecimalNum(orderProduct.getFinalPrice()).doubleValue())));
                prod_taxId = orderProduct.getTax_type();
            } else {
                taxAmount = Global.formatNumToLocale(Double.parseDouble(taxHandler.getTaxRate(
                        orderProduct.getProd_taxcode(), orderProduct.getTax_type(),
                        Global.getBigDecimalNum(orderProduct.getFinalPrice()).doubleValue())));
                prod_taxId = orderProduct.getProd_taxcode();
            }
        } else {
            if (!taxID.isEmpty()) {
                taxAmount = taxSelected.getTaxRate();
                prod_taxId = taxID;
            }
        }

        BigDecimal tempSubTotal = orderProduct.getItemSubtotalCalculated();
        BigDecimal prodQty = new BigDecimal(orderProduct.getOrdprod_qty());
        BigDecimal _temp_subtotal = tempSubTotal;
        boolean isVAT = assignEmployee.isVAT();
        if (isVAT) {
            if (orderProduct.getProd_istaxable().equals("1")) {
                if (orderProduct.getProd_price_updated().equals("0")) {
                    BigDecimal _curr_prod_price = Global.getBigDecimalNum(orderProduct.getFinalPrice());
                    BigDecimal _new_prod_price = getProductPrice(_curr_prod_price,
                            new BigDecimal(taxAmount).divide(new BigDecimal("100")).setScale(6, RoundingMode.HALF_UP));
                    _new_prod_price = _new_prod_price.setScale(6, RoundingMode.HALF_UP);
                    tempSubTotal = _new_prod_price.multiply(prodQty).setScale(6, RoundingMode.HALF_UP);

                    orderProduct.setPrice_vat_exclusive(_new_prod_price.setScale(6, RoundingMode.HALF_UP)
                            .toString());
                    orderProduct.setProd_price_updated("1");

                    BigDecimal disc;
                    if (orderProduct.getDiscount_is_fixed().equals("0")) {
                        BigDecimal val = tempSubTotal
                                .multiply(Global.getBigDecimalNum(orderProduct.getDisAmount()))
                                .setScale(6, RoundingMode.HALF_UP);
                        disc = val.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
                    } else {
                        disc = new BigDecimal(orderProduct.getDisAmount());
                    }

                    orderProduct.setDiscount_value(Global.getRoundBigDecimal(disc));
                    orderProduct.setDisTotal(Global.getRoundBigDecimal(disc));

                    orderProduct.setItemTotalVatExclusive(Global
                            .getRoundBigDecimal(tempSubTotal.subtract(disc)));
                }

                if (prodQty.compareTo(new BigDecimal("1")) == 1) {
                    _temp_subtotal = new BigDecimal(orderProduct.getPrice_vat_exclusive()).setScale(2,
                            RoundingMode.HALF_UP);
                } else {

                    tempSubTotal = new BigDecimal(orderProduct.getPrice_vat_exclusive()).multiply(prodQty)
                            .setScale(2, RoundingMode.HALF_UP);
                    _temp_subtotal = tempSubTotal;
                }
            } else
                orderProduct.setItemTotalVatExclusive(_temp_subtotal.toString());
        }
        BigDecimal tempTaxTotal = new BigDecimal("0");
        String taxTotal = "0";

        if (orderProduct.getProd_istaxable().equals("1") &&
                (orderProduct.getItem_void().isEmpty() || orderProduct.getItem_void().equals("0"))) {
            setTaxableDueAmount(getTaxableDueAmount().add(tempSubTotal));

            if (orderProduct.getDiscount_is_taxable().equals("1")) {
                BigDecimal temp = new BigDecimal(taxAmount).divide(new BigDecimal("100")).setScale(6,
                        RoundingMode.HALF_UP);
//                tempSubTotal = tempSubTotal.abs().subtract(new BigDecimal(orderProduct.getDiscount_value()).abs());
                if (orderProduct.isReturned() && OrderingMain_FA.mTransType != Global.TransactionType.RETURN) {
                    tempSubTotal = tempSubTotal.negate();
                }
                BigDecimal tax1 = tempSubTotal.multiply(temp);
                tempTaxTotal = tax1;
                taxTotal = tax1.toString();
                _temp_subtotal = tempSubTotal;
                if (discountSelected != null) {
                    if (discountSelected.getProductDiscountType().equals("Fixed")) {
                        if (getDiscount_rate().compareTo(tempSubTotal) == -1) {
                            setTaxableSubtotal(getTaxableSubtotal().add(tempSubTotal.subtract(getDiscount_rate()).multiply(temp)
                                    .setScale(6, RoundingMode.HALF_UP)));
                            // discount_rate = new BigDecimal("0");
                            if (discountSelected.getTaxCodeIsTaxable().equals("1")) {
                                _temp_subtotal = _temp_subtotal.subtract(getDiscount_rate());
                            }
                        } else {
                            setDiscount_amount(tempSubTotal);
                            setTaxableSubtotal(new BigDecimal("0"));
                            _temp_subtotal = getTaxableSubtotal();
                        }
                    } else {
                        BigDecimal temp2 = tempSubTotal.multiply(getDiscount_rate()).setScale(6, RoundingMode.HALF_UP);
                        setTaxableSubtotal(getTaxableSubtotal()
                                .add(tempSubTotal.subtract(temp2).multiply(temp).setScale(6, RoundingMode.HALF_UP)));
                        if (discountSelected.getTaxCodeIsTaxable().equals("1")) {
                            _temp_subtotal = _temp_subtotal.subtract(temp2);
                        }
                    }
                } else {
                    setTaxableSubtotal(getTaxableSubtotal()
                            .add(tempSubTotal.multiply(temp).setScale(6, RoundingMode.HALF_UP)));
                }
            } else {
                BigDecimal temp = new BigDecimal(taxAmount).divide(new BigDecimal("100")).setScale(6,
                        RoundingMode.HALF_UP);
                BigDecimal tax1 = tempSubTotal.multiply(temp).setScale(6, RoundingMode.HALF_UP);
                tempTaxTotal = tax1;
                taxTotal = tax1.toString();

                if (discountSelected != null && discountSelected.getProductId() != null) {
                    if (discountSelected.getProductDiscountType().equals("Fixed")) {
                        if (getDiscount_rate().compareTo(tempSubTotal) == -1) {
                            setTaxableSubtotal(getTaxableSubtotal().add(tempSubTotal.subtract(getDiscount_rate())
                                    .multiply(temp).setScale(6, RoundingMode.HALF_UP)));
                            // discount_rate = new BigDecimal("0");
                            if (discountSelected.getTaxCodeIsTaxable().equals("1")) {
                                _temp_subtotal = tempSubTotal.subtract(getDiscount_rate());
                            }
                        } else {
                            // discount_amount = tempSubTotal;
                            setTaxableSubtotal(new BigDecimal("0"));
                            _temp_subtotal = getTaxableSubtotal();
                        }
                    } else {
                        BigDecimal temp2 = tempSubTotal.multiply(getDiscount_rate()).setScale(6, RoundingMode.HALF_UP);
                        setTaxableSubtotal(getTaxableSubtotal()
                                .add(tempSubTotal.subtract(temp2).multiply(temp).setScale(6, RoundingMode.HALF_UP)));
                        if (discountSelected.getTaxCodeIsTaxable().equals("1")) {
                            _temp_subtotal = tempSubTotal.subtract(temp2);
                        }
                    }
                } else {
                    setTaxableSubtotal(getTaxableSubtotal()
                            .add(tempSubTotal.multiply(temp).setScale(6, RoundingMode.HALF_UP)));
                }
//                }
            }

            if (myPref.isRetailTaxes()) {
                calculateRetailGlobalTax(_temp_subtotal, taxAmount, prodQty, isVAT);
            } else {
                calculateGlobalTax(_temp_subtotal, prodQty, isVAT);
            }

        }

        if (tempTaxTotal.compareTo(new BigDecimal("0")) < -1)
            taxTotal = Double.toString(0.0);

        orderProduct.setProd_taxValue(new BigDecimal(taxTotal));
        orderProduct.setProd_taxId(prod_taxId);
    }

    private BigDecimal getProductPrice(BigDecimal prod_with_tax_price, BigDecimal tax) {
        BigDecimal denom = new BigDecimal(1).add(tax);
        return prod_with_tax_price.divide(denom, 2, RoundingMode.HALF_UP);
    }

    private void setDiscountValue() {
        if (discountSelected != null && discountSelected.getProductId() != null) {
            setDiscountID(discountSelected.getProductId());
            if (discountSelected.getProductDiscountType().equals("Fixed")) {
                setDiscount_rate(Global.getBigDecimalNum(discountSelected.getProductPrice()));
                setDiscount_amount(Global.getBigDecimalNum(discountSelected.getProductPrice()));

            } else {
                setDiscount_rate(Global.getBigDecimalNum(discountSelected.getProductPrice())
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


        String val = "0";
        BigDecimal temp = new BigDecimal("0");
        BigDecimal _total_tax = new BigDecimal("0");
        for (int j = 0; j < size; j++) {
            val = listMapTaxes.get(j).get("tax_rate");
            if (val == null || val.isEmpty())
                val = "0";
            temp = new BigDecimal(listMapTaxes.get(j).get("tax_rate")).divide(new BigDecimal("100")).setScale(6,
                    RoundingMode.HALF_UP);
            BigDecimal tax_amount = _subtotal.multiply(temp).setScale(6, RoundingMode.HALF_UP);

            _total_tax = _total_tax.add(tax_amount);
            DataTaxes tempTaxes = getListOrderTaxes().get(j);
            BigDecimal orderTaxesTotal = tempTaxes.getTax_amount().isEmpty() ? new BigDecimal(0.00) : new BigDecimal(tempTaxes.getTax_amount());
            if (_subtotal.compareTo(new BigDecimal("0.00")) != 0) {
                if (isVat)
                    orderTaxesTotal = orderTaxesTotal.add(tax_amount.multiply(qty.abs()).setScale(6, RoundingMode.HALF_UP));
                else
                    orderTaxesTotal = orderTaxesTotal.add(tax_amount).setScale(6, RoundingMode.HALF_UP);
            }
            tempTaxes.setTax_amount(orderTaxesTotal.toString());
            getListOrderTaxes().set(j, tempTaxes);
        }

        if (isVat)
            _total_tax = _total_tax.setScale(2, RoundingMode.HALF_UP).multiply(qty.abs());
        setTaxableAmount(getTaxableAmount().add(_total_tax).setScale(4, RoundingMode.HALF_UP));
    }

    private void setupTaxesHolder() {
        int size = listMapTaxes.size();
        listOrderTaxes = new ArrayList<>();
        DataTaxes tempTaxes;
        for (int i = 0; i < size; i++) {
            tempTaxes = new DataTaxes();
            tempTaxes.setTax_name(listMapTaxes.get(i).get("tax_name"));
            tempTaxes.setOrd_id("");
            tempTaxes.setTax_amount("0");
            tempTaxes.setTax_rate(listMapTaxes.get(i).get("tax_rate"));
            getListOrderTaxes().add(tempTaxes);
        }
    }

    private void calculateRetailGlobalTax(BigDecimal _sub_total, String tax_rate, BigDecimal qty, boolean isVat) {
        int size = listMapTaxes.size();
        String val = "0";

        BigDecimal temp = new BigDecimal("0");
        BigDecimal _total_tax = new BigDecimal("0");
        for (int j = 0; j < size; j++) {
            val = listMapTaxes.get(j).get("tax_rate");
            if (val == null || val.isEmpty())
                val = "0";
            temp = new BigDecimal(val).divide(new BigDecimal("100")).setScale(6, RoundingMode.HALF_UP);
            BigDecimal tax_amount = _sub_total.multiply(temp).setScale(6, RoundingMode.HALF_UP);

            _total_tax = _total_tax.add(tax_amount);
            DataTaxes tempTaxes = getListOrderTaxes().get(j);
            BigDecimal orderTaxesTotal = tempTaxes.getTax_amount().isEmpty() ? new BigDecimal(0.00) : new BigDecimal(tempTaxes.getTax_amount());
            if (_sub_total.compareTo(new BigDecimal("0.00")) != 0) {
                if (isVat)
                    orderTaxesTotal = orderTaxesTotal.add(tax_amount.multiply(qty)).setScale(6, RoundingMode.HALF_UP);
                else
                    orderTaxesTotal = orderTaxesTotal.add(tax_amount).setScale(6, RoundingMode.HALF_UP);

            }
            tempTaxes.setTax_amount(orderTaxesTotal.toString());
            getListOrderTaxes().set(j, tempTaxes);
        }

        if (isVat)
            _total_tax = _total_tax.setScale(6, RoundingMode.HALF_UP).multiply(qty);
        setTaxableAmount(getTaxableAmount().add(_total_tax).setScale(6, RoundingMode.HALF_UP));
    }


    public void setTaxValue() {
        TaxesHandler taxesHandler = new TaxesHandler(activity);
        TaxesGroupHandler taxesGroupHandler = new TaxesGroupHandler(activity);
        Global.taxAmount = Global.getBigDecimalNum(taxSelected.getTaxRate());

        if (!myPref.isRetailTaxes()) {
            listMapTaxes = taxesHandler.getTaxDetails(taxID, "");
            if (listMapTaxes.size() > 0 && listMapTaxes.get(0).get("tax_type").equals("G")) {
                listMapTaxes = taxesGroupHandler.getIndividualTaxes(listMapTaxes.get(0).get("tax_id"),
                        listMapTaxes.get(0).get("tax_code_id"));
            }
        } else {
            Tax tax = taxesHandler.getTax(taxID, orderProduct.getTax_type(),
                    Double.parseDouble(orderProduct.getFinalPrice()));
            if (listMapTaxes == null) {
                listMapTaxes = new ArrayList<>();
            } else {
                listMapTaxes.clear();
            }
            HashMap<String, String> mapTax = new HashMap<>();
            mapTax.put("tax_id", taxID);
            mapTax.put("tax_name", tax.getTaxName());
            mapTax.put("tax_rate", tax.getTaxRate());
            listMapTaxes.add(mapTax);
        }
    }

    public static ArrayList<DataTaxes> getDataTaxes(OrderProduct orderProduct, Context context, String taxID) {
        List<HashMap<String, String>> listMapTaxes = getTaxValue(orderProduct, context, taxID);
        int size = listMapTaxes.size();
        ArrayList<DataTaxes> listOrderTaxes = new ArrayList<>();
        DataTaxes tempTaxes;
        for (int i = 0; i < size; i++) {
            tempTaxes = new DataTaxes();
            tempTaxes.setTax_name(listMapTaxes.get(i).get("tax_name"));
            tempTaxes.setOrd_id("");
            tempTaxes.setTax_amount("0");
            tempTaxes.setTax_rate(listMapTaxes.get(i).get("tax_rate"));
            listOrderTaxes.add(tempTaxes);
        }
        return listOrderTaxes;
    }

    public static List<HashMap<String, String>> getTaxValue(OrderProduct orderProduct, Context context, String taxID) {
        List<HashMap<String, String>> listMapTaxes = new ArrayList<>();
        TaxesHandler taxesHandler = new TaxesHandler(context);
        TaxesGroupHandler taxesGroupHandler = new TaxesGroupHandler(context);
        MyPreferences preferences = new MyPreferences(context);
        if (!preferences.isRetailTaxes()) {
            listMapTaxes = taxesHandler.getTaxDetails(taxID, "");
            if (listMapTaxes.size() > 0 && listMapTaxes.get(0).get("tax_type").equals("G")) {
                listMapTaxes = taxesGroupHandler.getIndividualTaxes(listMapTaxes.get(0).get("tax_id"),
                        listMapTaxes.get(0).get("tax_code_id"));
            }
        } else {
            Tax tax = taxesHandler.getTax(taxID, orderProduct.getTax_type(),
                    Double.parseDouble(orderProduct.getFinalPrice()));
            HashMap<String, String> mapTax = new HashMap<>();
            mapTax.put("tax_id", taxID);
            mapTax.put("tax_name", tax.getTaxName());
            mapTax.put("tax_rate", tax.getTaxRate());
            listMapTaxes.add(mapTax);
        }
        return listMapTaxes;
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

    public Discount getDiscountSelected() {
        return discountSelected;
    }

    public void setDiscountSelected(Discount discount) {
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

    private List<DataTaxes> getTaxesHolder() {
        int size = listMapTaxes.size();
        ArrayList<DataTaxes> dataTaxes = new ArrayList<DataTaxes>();
        DataTaxes tempTaxes;
        for (int i = 0; i < size; i++) {
            tempTaxes = new DataTaxes();
            tempTaxes.setTax_name(listMapTaxes.get(i).get("tax_name"));
            tempTaxes.setOrd_id("");
            tempTaxes.setTax_amount("0");
            tempTaxes.setTax_rate(listMapTaxes.get(i).get("tax_rate"));
            dataTaxes.add(tempTaxes);
        }
        return dataTaxes;
    }

    private TaxesCalculator calculateTaxes(OrderProduct orderProduct) {

        TaxesCalculator taxesCalculator = new TaxesCalculator(activity, orderProduct, Global.taxID,
                taxSelected, discountSelected, discountable_sub_total, itemsDiscountTotal);
        return taxesCalculator;

    }

    public void reCalculate(Order order, List<OrderProduct> orderProducts) {
        int size = orderProducts.size();
        taxableSubtotal = new BigDecimal("0.00");
        taxableDueAmount = new BigDecimal("0.00");
        BigDecimal tempTaxableAmount = new BigDecimal("0");
        itemsDiscountTotal = new BigDecimal(0);
        List<DataTaxes> taxesHolder = getTaxesHolder();
        boolean isVAT = assignEmployee.isVAT();

        BigDecimal sub_total;
        BigDecimal gran_total;
        TaxesCalculator taxes = null;
        if (size > 0) {
            BigDecimal amount = new BigDecimal("0.00");
            BigDecimal discountableAmount = new BigDecimal("0");
            BigDecimal prodPrice;
            String val;
            int pointsSubTotal = 0, pointsInUse = 0, pointsAcumulable = 0;
            for (int i = 0; i < size; i++) {
                taxes = calculateTaxes(orderProducts.get(i));
                if (myPref.getPreferences(MyPreferences.pref_show_removed_void_items_in_printout)) {
                    String temp = orderProducts.get(i).getItem_void();

                    if (temp.equals("1"))
                        val = "0.00";
                    else {
                        if (isVAT) {
                            val = orderProducts.get(i).getItemTotalVatExclusive();
                        } else
                            val = orderProducts.get(i).getItemTotal();
                    }
                } else {
                    if (isVAT) {
                        val = orderProducts.get(i).getItemTotalVatExclusive();
                    } else
                        val = String.valueOf(orderProducts.get(i).getItemSubtotalCalculated());
                }
                if (val == null || val.isEmpty())
                    val = "0.00";
                prodPrice = new BigDecimal(val);
                discountableAmount = discountableAmount.add(prodPrice);
                try {
                    if (orderProducts.get(i).getDiscount_value() != null
                            && !orderProducts.get(i).getDiscount_value().isEmpty())
                        itemsDiscountTotal = itemsDiscountTotal.add(new BigDecimal(orderProducts.get(i).getDiscount_value()));
                } catch (NumberFormatException e) {
                }
                amount = amount.add(prodPrice);
                pointsSubTotal += Double.parseDouble(orderProducts.get(i).getProd_price_points());
                pointsAcumulable += Double.parseDouble(orderProducts.get(i).getProd_value_points());
                if (Boolean.parseBoolean(orderProducts.get(i).getPayWithPoints()))
                    pointsInUse += Double.parseDouble(orderProducts.get(i).getProd_price_points());
            }

            discountable_sub_total = discountableAmount.subtract(Global.rewardChargeAmount);
            sub_total = amount.subtract(Global.rewardChargeAmount);
//            tax_amount = tempTaxableAmount;
            gran_total = sub_total.subtract(discount_amount).add(tempTaxableAmount)
                    .subtract(itemsDiscountTotal);

            OrderLoyalty_FR.recalculatePoints(Integer.toString(pointsSubTotal), Integer.toString(pointsInUse),
                    Integer.toString(pointsAcumulable), gran_total.toString());
            OrderRewards_FR.setRewardSubTotal(discountable_sub_total.toString());
        } else {
            discountable_sub_total = new BigDecimal(0);
            sub_total = new BigDecimal(0);
            gran_total = new BigDecimal(0);
            OrderLoyalty_FR.recalculatePoints("0", "0", "0", gran_total.toString());
        }
        order.gran_total = Global.getRoundBigDecimal(gran_total);
        order.ord_discount = Global.getRoundBigDecimal(discountable_sub_total);
        order.ord_subtotal = Global.getRoundBigDecimal(sub_total);
        order.ord_discount_id = discountID;
        order.ord_taxamount = Global.getRoundBigDecimal(taxes.taxableAmount);
    }

    public static OrderProduct getTaxableOrderProduct(Order order, OrderProduct orderProduct, Tax tax) {

        return new OrderProduct();
    }

    public ArrayList<DataTaxes> getListOrderTaxes() {
        return listOrderTaxes;
    }

    public void setListOrderTaxes(ArrayList<DataTaxes> listOrderTaxes) {
        this.listOrderTaxes = listOrderTaxes;
    }
}
