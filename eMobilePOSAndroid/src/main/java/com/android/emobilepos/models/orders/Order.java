package com.android.emobilepos.models.orders;

import android.content.Context;

import com.android.dao.AssignEmployeeDAO;
import com.android.database.TaxesHandler;
import com.android.emobilepos.models.DataTaxes;
import com.android.emobilepos.models.Discount;
import com.android.emobilepos.models.Tax;
import com.android.emobilepos.models.realms.AssignEmployee;
import com.android.emobilepos.models.realms.OrderAttributes;
import com.android.support.Customer;
import com.android.support.DateUtils;
import com.android.support.Global;
import com.android.support.MyPreferences;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import util.json.JsonUtils;

public class Order implements Cloneable {
    public String ord_id = "";
    public String qbord_id = "";
    public String emp_id = "";
    public String cust_id = "";
    public String clerk_id = "";
    public String c_email = "";
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

    //private Global global;

    public String assignedTable;
    public String associateID;
    public int numberOfSeats;
    public String ord_timeStarted;
    public List<OrderAttributes> orderAttributes;
    private List<DataTaxes> listOrderTaxes;
    private List<OrderProduct> orderProducts = new ArrayList<>();

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
        return orderProducts;
    }

    public void setOrderProducts(List<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }

    public boolean isOnHold() {
        return isOnHold != null && isOnHold.equals("1");
    }

    public OrderTotalDetails getOrderTotalDetails(Discount discount, Tax tax) {
        OrderTotalDetails totalDetails = new OrderTotalDetails();
        if (getOrderProducts() != null && !getOrderProducts().isEmpty()) {
            for (OrderProduct orderProduct : getOrderProducts()) {
                orderProduct.setTaxAmount(tax != null ? tax.getTaxRate() : "0");
                orderProduct.setProd_taxId(tax != null ? tax.getTaxId() : "");
                orderProduct.setTax_type(tax != null ? tax.getTaxType() : "");

                totalDetails.setSubtotal(totalDetails.getSubtotal()
                        .add(orderProduct.getItemSubtotalCalculated()).setScale(6, RoundingMode.HALF_UP));
                totalDetails.setTax(totalDetails.getTax()
                        .add(orderProduct.getTaxAmountCalculated()).setScale(6, RoundingMode.HALF_UP));
                totalDetails.setGranTotal(totalDetails.getGranTotal()
                        .add(orderProduct.getGranTotalCalculated()).setScale(6, RoundingMode.HALF_UP));
            }
            if (discount != null) {
                if (discount.isFixed()) {
                    totalDetails.setGranTotal(totalDetails.getGranTotal().subtract(Global.getBigDecimalNum(discount.getProductPrice())).setScale(6, RoundingMode.HALF_UP));
                } else {
                    BigDecimal disAmout = totalDetails.getSubtotal()
                            .multiply(Global.getBigDecimalNum(discount.getProductPrice())
                                    .divide(new BigDecimal(100)).setScale(6, RoundingMode.HALF_UP));
                    totalDetails.setGlobalDiscount(disAmout);
                    totalDetails.setGranTotal(totalDetails.getGranTotal()
                            .subtract(disAmout).setScale(6, RoundingMode.HALF_UP));
                }
            }
        }
        return totalDetails;
    }

    public void setRetailTax(Context context, String taxID) {
        TaxesHandler taxesHandler = new TaxesHandler(context);
        for (OrderProduct product : getOrderProducts()) {
            Tax tax;
            if (taxID != null) {
                tax = taxesHandler.getTax(taxID, product.getTax_type(),
                        Global.getBigDecimalNum(product.getFinalPrice()).doubleValue());
            }else{
                tax = taxesHandler.getTax(product.getProd_taxcode(), product.getTax_type(),
                        Global.getBigDecimalNum(product.getFinalPrice()).doubleValue());
            }
            product.setTaxAmount(tax != null ? tax.getTaxRate() : "0");
            product.setProd_taxId(tax != null ? tax.getTaxId() : "");
            product.setTax_type(tax != null ? tax.getTaxType() : "");
        }

    }
}
