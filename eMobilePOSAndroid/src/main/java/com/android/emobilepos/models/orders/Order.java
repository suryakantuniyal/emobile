package com.android.emobilepos.models.orders;

import android.content.Context;
import android.text.TextUtils;

import com.android.dao.AssignEmployeeDAO;
import com.android.dao.OrderProductAttributeDAO;
import com.android.database.TaxesHandler;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.Discount;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.OrderAttributes;
import com.android.emobilepos.models.realms.ProductAttribute;
import com.android.support.Customer;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.android.support.TaxesCalculator;
import com.google.gson.Gson;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import util.json.JsonUtils;

public class Order implements Cloneable, Serializable {
    public String ord_id = "";
    public String qbord_id = "";
    public String emp_id = "";
    public String cust_id = "";
    public String clerk_id = "";
    public String c_email = "";
    public String c_phone = "";
    public String ord_signature = "";
    public String ord_po = "";
    public String total_lines = "";
    public String total_lines_pay = "0";
    public String ord_total = "";
    public String ord_comment = "";
    public String ord_delivery = "";
    public String ord_timecreated = "";
    public String ord_timesync = "";
    public String qb_synctime = "";
    public String emailed = "";
    public String processed = "";
    public String ord_type = "";
    public String ord_type_name = "";
    public String ord_claimnumber = "";
    public String ord_rganumber = "";
    public String ord_returns_pu = "";
    public String ord_inventory = "";
    public String ord_issync = "";
    public String tax_id = "";
    public String ord_shipvia = "";
    public String ord_shipto = "";
    public String ord_terms = "";
    public String ord_custmsg = "";
    public String ord_class = "";
    public String ord_subtotal = "";
    public String ord_lineItemDiscount = "";
    public String ord_globalDiscount = "";
    public String ord_taxamount = "";
    public String ord_discount = "";
    public String ord_discount_id = "";
    public String ord_latitude = "";
    public String ord_longitude = "";
    public String tipAmount = "";
    public String custidkey = "";
    public String isOnHold = "0"; // 0 - not on hold, 1 - on hold
    public String ord_HoldName = "";
    public String is_stored_fwd = "0";
    public String VAT = "0";
    public String isVoid = "";
    public String gran_total = "";
    public String cust_name = "";
    public String sync_id = "";
    public Customer customer;
    public String assignedTable;

    //private Global global;
    public String associateID;
    public int numberOfSeats;
    public String ord_timeStarted;
    public List<OrderAttributes> orderAttributes;
    private String bixolonTransactionId;
    private List<DataTaxes> listOrderTaxes;
    private List<OrderProduct> orderProducts = new ArrayList<>();
    private boolean retailTaxes;

    public Order() {
        ord_issync = "0";
        isVoid = "0";
        processed = "0"; //need to be 1 when order has been processed or 9 if voided
        ord_timecreated = DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss);
    }

    public Order(Context activity) {
        MyPreferences myPref = new MyPreferences(activity);
        ord_issync = "0";
        isVoid = "0";
        processed = "0"; //need to be 1 when order has been processed or 9 if voided
        ord_timecreated = DateUtils.getDateAsString(new Date(), DateUtils.DATE_yyyy_MM_ddTHH_mm_ss);
        AssignEmployee assignEmployee = AssignEmployeeDAO.getAssignEmployee();
        emp_id = String.valueOf(assignEmployee != null ? assignEmployee.getEmpId() : "");
        custidkey = myPref.getCustIDKey();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    public String toJson() {
        Gson gson = JsonUtils.getInstance();
        String json = gson.toJson(this, Order.class);
        return json;
    }

    public List<DataTaxes> getListOrderTaxes() {
        return listOrderTaxes;
    }

    public void setListOrderTaxes(List<DataTaxes> listOrderTaxes) {
        this.listOrderTaxes = listOrderTaxes;
    }

    public boolean isSync() {
        return ord_issync.equalsIgnoreCase("1");
    }

    public List<OrderProduct> getOrderProducts() {
        if (orderProducts == null) {
            orderProducts = new ArrayList<>();
        }
        return orderProducts;
    }

    public void setOrderProducts(List<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }

    public boolean isOnHold() {
        return isOnHold != null && isOnHold.equals("1");
    }

    public OrderTotalDetails getOrderTotalDetails(Discount discount, Tax tax, boolean isVAT, Context context) {
        OrderTotalDetails totalDetails = new OrderTotalDetails();
        final List<OrderProduct> orderProducts = new ArrayList<>(getOrderProducts());
        if (!orderProducts.isEmpty()) {
            for (OrderProduct orderProduct : orderProducts) {
                setupProductTax(context, orderProduct);
                if (isVAT) {
                    setVATTax(tax);
                }
                totalDetails.setPointsSubTotal(totalDetails.getPointsSubTotal()
                        .add(Global.getBigDecimalNum(orderProduct.getProd_price_points())));
                totalDetails.setPointsAcumulable(totalDetails.getPointsAcumulable()
                        .add(Global.getBigDecimalNum(orderProduct.getProd_value_points())));
                if (Boolean.parseBoolean(orderProduct.getPayWithPoints())) {
                    totalDetails.setPointsInUse(totalDetails.getPointsInUse().add(new BigDecimal(orderProduct.getProd_price_points())));
                }
                totalDetails.setSubtotal(totalDetails.getSubtotal()
                        .add(orderProduct.getItemSubtotalCalculated()).setScale(6, RoundingMode.HALF_UP));
                totalDetails.setTax(totalDetails.getTax()
                        .add(orderProduct.getProd_taxValue()).setScale(6, RoundingMode.HALF_UP));
                totalDetails.setGranTotal(totalDetails.getGranTotal()
                        .add(orderProduct.getGranTotalCalculated()).setScale(6, RoundingMode.HALF_UP));
            }
            if (discount != null) {
                if (discount.isFixed()) {
                    BigDecimal discAmount = Global.getBigDecimalNum(discount.getProductPrice());
                    if (discAmount.compareTo(totalDetails.getGranTotal()) > -1) {
                        discAmount = totalDetails.getGranTotal();
                    }
                    totalDetails.setGlobalDiscount(discAmount);
                    totalDetails.setGranTotal(totalDetails.getGranTotal().subtract(discAmount).setScale(6, RoundingMode.HALF_UP));
                } else {
                    BigDecimal disAmout = totalDetails.getSubtotal()
                            .multiply(Global.getBigDecimalNum(discount.getProductPrice())
                                    .divide(new BigDecimal(100)).setScale(6, RoundingMode.HALF_UP));
                    totalDetails.setGlobalDiscount(disAmout);
                    totalDetails.setGranTotal(totalDetails.getGranTotal()
                            .subtract(disAmout).setScale(6, RoundingMode.HALF_UP));
                }
            }
            setOrderGlobalDataTaxes(orderProducts);
        }
        return totalDetails;
    }

    private void setOrderGlobalDataTaxes(List<OrderProduct> orderProducts) {
        BigDecimal taxableAmount = new BigDecimal(0);
        if (getListOrderTaxes() != null) {
            for (OrderProduct product : orderProducts) {
                if (product.isTaxable()) {
                    taxableAmount = taxableAmount.add(product.getItemSubtotalCalculated());
                }
            }

            for (DataTaxes taxes : getListOrderTaxes()) {
                BigDecimal rate = Global.getBigDecimalNum(taxes.getTax_rate()).divide(new BigDecimal("100")).setScale(6,
                        RoundingMode.HALF_UP);
                BigDecimal tax_amount = taxableAmount.multiply(rate).setScale(6, RoundingMode.HALF_UP);
            }
        }
    }

    private void setupProductTax(Context context, OrderProduct orderProduct) {
        TaxesHandler taxHandler = new TaxesHandler(context);
        MyPreferences preferences = new MyPreferences(context);
        Tax tax;
        List<BigDecimal> taxes = new ArrayList<>();
        BigDecimal totalTaxAmount = new BigDecimal(0);
        if (preferences.isRetailTaxes()) {
            if (!Global.taxID.isEmpty()) {
                tax = taxHandler.getTax(Global.taxID, orderProduct.getProd_taxId(), Double.parseDouble(TextUtils.isEmpty(orderProduct.getProd_price()) ? "0" : orderProduct.getProd_price()));
            } else {
                tax = taxHandler.getTax(orderProduct.getProd_taxcode(), orderProduct.getProd_taxId(), Double.parseDouble(TextUtils.isEmpty(orderProduct.getProd_price()) ? "0" : orderProduct.getProd_price()));
            }
//            BigDecimal taxAmount = orderProduct.getProductPriceTaxableAmountCalculated()
//                    .multiply(new BigDecimal(tax.getTaxRate())
//                            .divide(new BigDecimal(100)))
//                    .setScale(2, RoundingMode.HALF_UP);
//            totalTaxAmount = totalTaxAmount.add(taxAmount);
            taxes.add(new BigDecimal(tax.getTaxRate()));
            totalTaxAmount = new BigDecimal(orderProduct.getTaxTotal());//TaxesCalculator.calculateTax(orderProduct.getProductPriceTaxableAmountCalculated(), taxes);
        } else {
            if (!Global.taxID.isEmpty()) {
                tax = taxHandler.getTax(Global.taxID, "", Double.parseDouble(TextUtils.isEmpty(orderProduct.getProd_price()) ? "0" : orderProduct.getProd_price()));
                if (listOrderTaxes != null && getListOrderTaxes() != null && !getListOrderTaxes().isEmpty()) {
                    for (DataTaxes dataTaxes : getListOrderTaxes()) {
                        taxes.add(new BigDecimal(dataTaxes.getTax_rate()));
//                        BigDecimal taxAmount = orderProduct.getProductPriceTaxableAmountCalculated()
//                                .multiply(new BigDecimal(dataTaxes.getTax_rate())
//                                        .divide(new BigDecimal(100)))
//                                .setScale(6, RoundingMode.HALF_UP);
//                        totalTaxAmount = TaxesCalculator.calculateTax(orderProduct, )//totalTaxAmount.add(taxAmount);
                    }

                    totalTaxAmount = TaxesCalculator.calculateTax(orderProduct.getProductPriceTaxableAmountCalculated(), taxes);
                } else {
//                    BigDecimal taxAmount = orderProduct.getProductPriceTaxableAmountCalculated()
//                            .multiply(new BigDecimal(tax.getTaxRate())
//                                    .divide(new BigDecimal(100)))
//                            .setScale(6, RoundingMode.HALF_UP);
                    taxes.add(new BigDecimal(tax.getTaxRate()));
                    totalTaxAmount = TaxesCalculator.calculateTax(orderProduct.getProductPriceTaxableAmountCalculated(), taxes);
                }

            } else {
                tax = taxHandler.getTax(orderProduct.getProd_taxcode(), "", Double.parseDouble(TextUtils.isEmpty(orderProduct.getProd_price()) ? "0" : orderProduct.getProd_price()));
//                BigDecimal taxAmount = orderProduct.getProductPriceTaxableAmountCalculated()
//                        .multiply(new BigDecimal(tax.getTaxRate())
//                                .divide(new BigDecimal(100)))
//                        .setScale(6, RoundingMode.HALF_UP);
//                totalTaxAmount = totalTaxAmount.add(taxAmount);
                taxes.add(new BigDecimal(tax.getTaxRate()));
                totalTaxAmount = TaxesCalculator.calculateTax(orderProduct.getProductPriceTaxableAmountCalculated(), taxes);
            }
        }
        orderProduct.setTaxAmount(tax != null ? tax.getTaxRate() : "0");
        orderProduct.setProd_taxValue(totalTaxAmount);
//        orderProduct.setProd_taxId(tax != null ? tax.getTaxId() : "");
        orderProduct.setProd_taxId(tax != null ? tax.getTaxType() : "");
    }

    public void setRetailTax(Context context, String taxID) {
        TaxesHandler taxesHandler = new TaxesHandler(context);
        final List<OrderProduct> orderProducts = new ArrayList<>(getOrderProducts());
        for (OrderProduct product : orderProducts) {
            Tax tax;
            if (taxID != null) {
                tax = taxesHandler.getTax(taxID, product.getProd_taxId(),
                        Global.getBigDecimalNum(product.getFinalPrice()).doubleValue());
                List<Tax> taxes = taxesHandler.getProductTaxes(taxID, product.getProd_taxId(),
                        product);
                product.setTaxes(taxes);
            } else {
                tax = taxesHandler.getTax(product.getProd_taxcode(), product.getProd_taxId(),
                        Global.getBigDecimalNum(product.getFinalPrice()).doubleValue());
                List<Tax> taxes = taxesHandler.getProductTaxes(taxID, product.getProd_taxId(),
                        product);
                product.setTaxes(taxes);
            }
            product.setTaxAmount(tax != null ? tax.getTaxRate() : "0");
//            product.setProd_taxId(tax != null ? tax.getTaxId() : "");
            product.setProd_taxId(tax != null ? tax.getTaxType() : "");
            List<BigDecimal> prodTaxesRates = new ArrayList<>();
            for (Tax t : product.getTaxes()) {
                prodTaxesRates.add(new BigDecimal(t.getTaxRate()));
            }

            BigDecimal taxTotal = TaxesCalculator.calculateTax(product.getProductPriceTaxableAmountCalculated(), prodTaxesRates);
//                    Global.getBigDecimalNum(product.getFinalPrice())
//                    .multiply(Global.getBigDecimalNum(product.getOrdprod_qty()))
//                    .multiply(Global.getBigDecimalNum(tax.getTaxRate())).divide(new BigDecimal(100)).setScale(6, RoundingMode.HALF_UP);
            product.setTaxTotal(String.valueOf(taxTotal));
        }
    }

    public void setVATTax(Tax tax) {
        for (OrderProduct product : getOrderProducts()) {
            if (product.isTaxable()) {
                BigDecimal subTotal;
                if (product.getProd_price_updated().equals("0")) {
                    BigDecimal taxRate = Global.getBigDecimalNum(tax.getTaxRate()).divide(new BigDecimal("100")).setScale(6, RoundingMode.HALF_UP);
                    BigDecimal denom = new BigDecimal(1).add(taxRate);
                    BigDecimal vatPrice = Global.getBigDecimalNum(product.getFinalPrice()).divide(denom, 2, RoundingMode.HALF_UP);
                    subTotal = vatPrice.multiply(Global.getBigDecimalNum(product.getOrdprod_qty())).setScale(6, RoundingMode.HALF_UP);
                    product.setPrice_vat_exclusive(vatPrice.setScale(6, RoundingMode.HALF_UP)
                            .toString());
                    product.setProd_price_updated("1");
                    BigDecimal disc;
                    if (!product.isDiscountFixed()) {
                        BigDecimal val = subTotal
                                .multiply(Global.getBigDecimalNum(product.getDisAmount()))
                                .setScale(6, RoundingMode.HALF_UP);
                        disc = val.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
                    } else {
                        disc = new BigDecimal(product.getDisAmount());
                    }
                    product.setDiscount_value(String.valueOf(Global.getRoundBigDecimal(disc)));
                    product.setDisTotal(String.valueOf(Global.getRoundBigDecimal(disc)));

                    product.setItemTotalVatExclusive(String.valueOf(Global
                            .getRoundBigDecimal(subTotal.subtract(disc))));
                }
                BigDecimal qty = Global.getBigDecimalNum(product.getOrdprod_qty());
                if (qty.compareTo(new BigDecimal("1")) == 1) {
                    subTotal = new BigDecimal(product.getPrice_vat_exclusive()).setScale(2,
                            RoundingMode.HALF_UP);
                } else {
                    subTotal = new BigDecimal(product.getPrice_vat_exclusive()).multiply(qty)
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }
        }
    }

    public boolean isRetailTaxes() {
        return retailTaxes;
    }

    public void setRetailTaxes(boolean retailTaxes) {
        this.retailTaxes = retailTaxes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Order) {
            Order order = (Order) obj;
            return order.ord_id.equalsIgnoreCase(this.ord_id);
        }
        return super.equals(obj);
    }

    public String getBixolonTransactionId() {
        return bixolonTransactionId;
    }

    public void setBixolonTransactionId(String bixolonTransactionId) {
        this.bixolonTransactionId = bixolonTransactionId;
    }

    public boolean isAllProductsRequiredAttrsCompleted() {
        for (OrderProduct product : orderProducts) {
            if (!product.isAttributesCompleted()) {
                return false;
            }
        }
        return true;
    }

    public void setProductRequiredAttributeCompleted() {
        for (OrderProduct product : orderProducts) {
            product.setAttributesCompleted(true);
            List<ProductAttribute> attributes = OrderProductAttributeDAO.getByProdId(product.getProd_id());
            for (ProductAttribute attribute : attributes) {
                product.setAttributesCompleted(product.getRequiredProductAttributes().contains(attribute));
            }
        }
    }

    @Override
    public String toString() {
        return String.format("id:%s - total:%s - hold:%b - status:%s", this.ord_id, ord_total, isOnHold(), processed);
    }

    public boolean isReturn() {
        return ord_type.equalsIgnoreCase("1");
    }

    public double getTotalLines() {
        if (orderProducts == null || orderProducts.isEmpty()) {
            return 0;
        } else {
            int count = 0;
            for (OrderProduct orderProduct : getOrderProducts()) {
                double consigmentQty = TextUtils.isEmpty(orderProduct.getConsignment_qty()) ? 0 : Double.parseDouble(orderProduct.getConsignment_qty());
                double qty = Double.parseDouble(orderProduct.getOrdprod_qty());
                if (consigmentQty == 0 && qty > 0) {
                    count++;
                } else if (consigmentQty > 0 && consigmentQty > qty) {
                    count++;
                }
            }
            return count;
        }

    }
}
