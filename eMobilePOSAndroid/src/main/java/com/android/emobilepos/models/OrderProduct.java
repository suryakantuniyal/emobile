package com.android.emobilepos.models;

import android.app.Activity;
import android.text.TextUtils;

import com.android.database.ProductsHandler;
import com.android.support.Global;
import com.google.android.gms.vision.text.Text;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class OrderProduct implements Cloneable, Comparable<OrderProduct> {
    public String addon = "0";
    public String isAdded = "0";
    public String isPrinted = "0";
    public String price_vat_exclusive = "0";
    public String item_void = "";
    public String ordprod_id = "";
    public String ord_id = "";
    public String prod_id = "";
    public String prod_sku = "";
    public String prod_upc = "";
    public String ordprod_qty = "";
    public String overwrite_price = "";
    public String reason_id = "";
    public String ordprod_name = "";
    public String ordprod_desc = "";
    public String ordprod_comment = "";
    public String pricelevel_id = "";
    public String prod_seq = "";
    public String uom_name = "";
    public String uom_conversion = "";
    public String uom_id = "";
    public String prod_taxId = "";
    public BigDecimal prod_taxValue;
    public String discount_id = "";
    public String discount_value = "";
    public String prod_taxcode = "";
    public String prod_istaxable = "";
    public String cat_id = "";
    public String cat_name = "";
    public String prod_price_points = "0";
    public String prod_value_points = "0";
    public String payWithPoints = "false";

    public String itemTotalVatExclusive = "0";
    public String itemTotal = "0";
    public String itemSubtotal = "0";
    public String disAmount = "0", disTotal = "0";
    public String taxAmount = "0", taxTotal = "0";
    public String onHand = "0";
    public String imgURL = "";

    public String tax_position = "";
    public String discount_position = "";
    public String pricelevel_position = "";
    public String uom_position = "";
    public String prod_price = "";
    public String prod_type = "";
    public String tax_type = "";
    public String discount_is_taxable = "0";
    public String discount_is_fixed = "0";
    public String prod_taxtype;

    public String priceLevelName = "";
    public List<ProductAttribute> requiredProductAttributes = new ArrayList<ProductAttribute>();
    public List<OrderProduct> addonsProducts = new ArrayList<OrderProduct>();
    public String hasAddons = "0"; //0 no addons, 1 it has addons
    public String addon_section_name = "";
    public String addon_position = "";

    public String prod_price_updated = "0";

    public boolean isReturned = false;
    public String assignedSeat;
    public int seatGroupId;


    public String consignment_qty;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof String) {
            return this.ordprod_id.equalsIgnoreCase((String) o);
        } else {
            return this.ordprod_id.equalsIgnoreCase(((OrderProduct) o).ordprod_id);
        }
    }

    @Override
    public int compareTo(OrderProduct another) {
        return this.ordprod_id.compareTo(another.ordprod_id);
    }

    public void setOverwritePrice(BigDecimal overwriteAmount, Activity activity) {
        ProductsHandler productsHandler = new ProductsHandler(activity);
        HashMap<String, String> map = productsHandler
                .getDiscountDetail(discount_id);
        BigDecimal prod_qty;
        BigDecimal new_subtotal;
        try {
            prod_qty = new BigDecimal(
                    ordprod_qty);
        } catch (Exception e) {
            prod_qty = new BigDecimal("0");
        }

        if (!map.isEmpty()) {
            if (map.get("discount_type")
                    .toUpperCase(
                            Locale.getDefault())
                    .trim().equals("FIXED")) {
                new_subtotal = overwriteAmount
                        .multiply(prod_qty)
                        .subtract(
                                new BigDecimal(
                                        map.get("discount_price")));

            } else {
                BigDecimal rate = new BigDecimal(
                        map.get("discount_price"))
                        .divide(new BigDecimal(
                                "100"));
                rate = rate.multiply(overwriteAmount
                        .multiply(prod_qty));

                new_subtotal = overwriteAmount.multiply(
                        prod_qty).subtract(rate);

                disTotal = Global
                        .getRoundBigDecimal(rate);
                discount_value = Global
                        .getRoundBigDecimal(rate);

            }
        } else
            new_subtotal = overwriteAmount
                    .multiply(prod_qty);

        overwrite_price = Global.getRoundBigDecimal(overwriteAmount);
        prod_price = Global.getRoundBigDecimal(overwriteAmount);
        itemSubtotal = Global
                .getRoundBigDecimal(new_subtotal);
        itemTotal = Global
                .getRoundBigDecimal(new_subtotal);
        pricelevel_id = "";
        prod_price_updated = "0";
    }

    public static OrderProduct getInstance(String ordprod_id) {
        OrderProduct product = new OrderProduct();
        product.ordprod_id = ordprod_id;
        return product;
    }

    public boolean isAdded() {
        return !TextUtils.isEmpty(isAdded) && isAdded.equalsIgnoreCase("1");
    }
}
